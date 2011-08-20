/*******************************************************************************
 * Copyright (c) Gil Barash - chookapp@yahoo.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gil Barash - initial API and implementation
 *    
 * Thanks to:
 *    emil.crumhorn@gmail.com - Some of the code was copied from the 
 *    "eclipsemissingfeatrues" plugin. 
 *******************************************************************************/

package com.chookapp.org.bracketeer.core;

import java.util.HashMap;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPart;

import com.chookapp.org.bracketeer.Activator;
import com.chookapp.org.bracketeer.extensionpoint.IBracketeerProcessor;
import com.chookapp.org.bracketeer.helpers.BackDoors;


public class EditorEvents implements IStartup {

	private HashMap<IWorkbenchPart, BracketsHighlighter> _activeMap;
	private MatchersRegistry _processorsRegistry;
	
	public EditorEvents()
	{
		_activeMap = new HashMap<IWorkbenchPart, BracketsHighlighter>();
		_processorsRegistry = new MatchersRegistry();
	}
	
	public void earlyStartup()
	{
		// hook us on an async as we need the active page
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				// hook the startup editor if any, it doesn't get notified via a normal event
				IEditorPart startupEditorPart = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				if (startupEditorPart != null) {
					activated(Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart());
				}

				Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().addPartListener(new IPartListener() {

					public void partActivated(IWorkbenchPart part) {
						activated(part);
					}

					public void partBroughtToTop(IWorkbenchPart part) {
						activated(part);
					}

					public void partClosed(IWorkbenchPart part) {
						unhook(part);
					}

					public void partDeactivated(IWorkbenchPart part) {
						deactivated(part);
					}

					public void partOpened(IWorkbenchPart part) {
						activated(part);
					}

				});
			}
		});
	}
	
	private void activated(IWorkbenchPart part) {
        try {
//            IEditorPart editorPart = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        	if (!(part instanceof IEditorPart))
        		return;
        	
        	IEditorPart editorPart = (IEditorPart) part;
            ITextViewer viewer = BackDoors.callGetSourceViewer(editorPart);
            if (viewer == null) return;

            hook(editorPart, viewer);
        } catch (Exception err) {
            err.printStackTrace();
        }

    }	
	
	private void deactivated(IWorkbenchPart part) {
		
		// We don't want to unhook, the part may be reactivated...
		// unhook(part);
    }
	

	
	private void hook(final IEditorPart part, final ITextViewer textViewer) {

		BracketsHighlighter oldBracketsHighlighter;
		synchronized (_activeMap) {	
			oldBracketsHighlighter = _activeMap.get(part); 
		}
		
        if (oldBracketsHighlighter != null && 
        		oldBracketsHighlighter.getTextViewer() == textViewer) return;

        if (textViewer == null) return;

        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
            	IBracketeerProcessor processor = null;
            	try 
            	{
            		processor = _processorsRegistry.findProcessorFor(part);
            	} 
            	catch (RuntimeException e)
            	{
            		Activator.log(e);
            		return;
            	}
            	
                if( processor == null )
                	return;
                
            	BracketsHighlighter bracketsHighlighter = new BracketsHighlighter(); 
            	bracketsHighlighter.Init(processor, textViewer);
            	synchronized (_activeMap) {					
            		_activeMap.put(part, bracketsHighlighter);
            	}
            }
        });
    }
	
	private void unhook(final IWorkbenchPart part) {
		synchronized (_activeMap) {
			final BracketsHighlighter oldBracketsHighlighter = _activeMap.get(part);
	        if (oldBracketsHighlighter == null) return;
	        	_activeMap.remove(part);
    	}
        
    }
	
}