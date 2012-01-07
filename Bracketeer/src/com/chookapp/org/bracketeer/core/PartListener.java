/*******************************************************************************
 * Copyright (c) Gil Barash - chookapp@yahoo.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gil Barash - initial API and implementation
 *******************************************************************************/
package com.chookapp.org.bracketeer.core;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.chookapp.org.bracketeer.Activator;
import com.chookapp.org.bracketeer.commands.SourceProvider;

public class PartListener implements IWindowListener, IPartListener2
{
    private static PartListener sInstance = new PartListener();
    private Collection<IWorkbenchWindow> fWindows= new HashSet<IWorkbenchWindow>();
    private HashMap<IWorkbenchPart, BracketsHighlighter> _activeMap;
    private ProcessorsRegistry _processorsRegistry;
    private List<IActiveProcessorListener> m_listeners;
    
    PartListener()
    {
        _activeMap = new HashMap<IWorkbenchPart, BracketsHighlighter>();
        m_listeners = new LinkedList<IActiveProcessorListener>();
        _processorsRegistry = new ProcessorsRegistry();        
    }
    
    public static PartListener getInstance()
    {
        return sInstance;
    }  
    
    public void install() 
    {
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null)
        {
            Activator.log(Messages.PartListener_ErrWorkbanch);
            return;
        }

        ISourceProviderService srcService = (ISourceProviderService) workbench.getService(ISourceProviderService.class);
        ISourceProvider src = srcService.getSourceProvider(SourceProvider.PLUGIN_NAME);
        m_listeners.add((IActiveProcessorListener) src);

        // listen for new windows
        workbench.addWindowListener(this);
        IWorkbenchWindow[] wnds= workbench.getWorkbenchWindows();
        for (int i = 0; i < wnds.length; i++) 
        {
            IWorkbenchWindow window = wnds[i];
            register(window);
        }
        // register open windows
        //            IWorkbenchWindow ww= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        //            if (ww != null) {
        //                IWorkbenchPage activePage = ww.getActivePage();
        //                if (activePage != null) {
        //                    IWorkbenchPartReference part= activePage.getActivePartReference();
        //                    if (part != null) {
        //                        partActivated(part);
        //                    }
        //                }
        //            }

    }

    public void uninstall() 
    {
        for (Iterator<IWorkbenchWindow> iterator = fWindows.iterator(); iterator.hasNext();) 
        {
            IWorkbenchWindow window = iterator.next();
            unregister(window);
        }      
    }

    private void register(IWorkbenchWindow wnd) 
    {
        wnd.getPartService().addPartListener(this);
        fWindows.add(wnd);
        IWorkbenchPage[] pages = wnd.getPages();
        for (IWorkbenchPage page : pages)
        {
            IEditorReference[] editorRefs = page.getEditorReferences();
            for (IEditorReference editorRef : editorRefs)
            {
                partActivated(editorRef);
            }
        }
        
        IWorkbenchPage page = wnd.getActivePage();
        if( page != null )
        {
            activated(page.getActivePartReference());
        }
    }
    
    /*
     * This function is expected to be closed when a window is closed (including 
     *  when eclipse closes), so the parts have already been closed.
     * This is because I don't dispose the higlighers in this function... 
     */
    private void unregister(IWorkbenchWindow wnd) 
    {
        wnd.getPartService().removePartListener(this);
        fWindows.remove(wnd);
    }
    
    
    /* window events */

    @Override
    public void windowActivated(IWorkbenchWindow window)
    {
    }

    @Override
    public void windowDeactivated(IWorkbenchWindow window)
    {
    }

    @Override
    public void windowOpened(IWorkbenchWindow window) 
    {
        register(window);
    }

    @Override
    public void windowClosed(IWorkbenchWindow window) 
    {
        unregister(window);
    }
    
    
    /* part events */
    
    @Override
    public void partActivated(IWorkbenchPartReference partRef)
    {
        created(partRef);
        activated(partRef);
    }   

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef)
    {
    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef)
    {
        destroyed(partRef);
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partRef)
    {
        deactivated(partRef);
    }

    @Override
    public void partOpened(IWorkbenchPartReference partRef)
    {
        created(partRef);
    }

    @Override
    public void partHidden(IWorkbenchPartReference partRef)
    {
    }

    @Override
    public void partVisible(IWorkbenchPartReference partRef)
    {
    }

    @Override
    public void partInputChanged(IWorkbenchPartReference partRef)
    {
        destroyed(partRef);
        created(partRef);
    }

    private void created(IWorkbenchPartReference partRef) 
    {
        IWorkbenchPart part= partRef.getPart(false);
        try {
            if (!(part instanceof IEditorPart))
                return;
            
            IEditorPart editorPart = (IEditorPart) part;
            ITextViewer viewer = callGetSourceViewer(editorPart);
            if (viewer == null) 
                return;

            hook(editorPart, viewer);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
    
    private void destroyed(IWorkbenchPartReference partRef) 
    {
        IWorkbenchPart part= partRef.getPart(false);
        try {
            if (!(part instanceof IEditorPart))
                return;            

            unhook(part);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
    
    private void activated(IWorkbenchPartReference partRef)
    {
        if( partRef == null )
        {
            deactivated(partRef);
            return;
        }
        IWorkbenchPart part = partRef.getPart(false);
        if( part == null )
        {
            deactivated(partRef);
            return;
        }

        BracketsHighlighter bracketsHighlighter;
        synchronized (_activeMap) 
        { 
            bracketsHighlighter = _activeMap.get(part); 
        }
        if( bracketsHighlighter == null )
        {
            deactivated(partRef);
            return;
        }
        
        String name = bracketsHighlighter.getConfiguration().getName();

        if( Activator.DEBUG )
            Activator.trace("PluginName: " + name); //$NON-NLS-1$
        for (IActiveProcessorListener listener : m_listeners)
        {
            listener.activeProcessorChanged(name);
        }
    }
    
    private void deactivated(IWorkbenchPartReference partRef)
    {
        for (IActiveProcessorListener listener : m_listeners)
        {
            listener.activeProcessorChanged(null);
        }   
    }
    
    private void hook(final IEditorPart part, final ITextViewer textViewer) 
    {
        if (textViewer == null) 
            return;
        
        BracketsHighlighter oldBracketsHighlighter;
        synchronized (_activeMap) 
        { 
            oldBracketsHighlighter = _activeMap.get(part); 
        }
        
        if (oldBracketsHighlighter != null )
        {
            if( oldBracketsHighlighter.getTextViewer() != textViewer )
            {
                Activator.log("Part viewer changed"); //$NON-NLS-1$
                unhook(part);
            }
            else
            {
                // this part is already registered fine...
                return;
            }
        }

        IDocument doc = getPartDocument(part);
        if( doc == null )
            return;
        
        BracketeerProcessorInfo processor = null;
        try 
        {
            processor = _processorsRegistry.findProcessorFor(part, doc);
        } 
        catch (RuntimeException e)
        {
            Activator.log(e);
            return;
        }
        
        if( processor == null )
            return;
        
        BracketsHighlighter bracketsHighlighter = new BracketsHighlighter(); 
        bracketsHighlighter.Init(processor.getProcessor(), 
                                 part, doc, textViewer,
                                 processor.getConfiguration());
        synchronized (_activeMap) 
        {                 
            _activeMap.put(part, bracketsHighlighter);
            
            if( Activator.DEBUG )
                Activator.trace(String.format("Parts active = %1$d", _activeMap.size())); //$NON-NLS-1$
        }
    }
    
    private void unhook(final IWorkbenchPart part) 
    {
        synchronized (_activeMap) 
        {
            BracketsHighlighter oldBracketsHighlighter = _activeMap.get(part);
            if (oldBracketsHighlighter == null)
                return;
     
            oldBracketsHighlighter.dispose();
            
            _activeMap.remove(part);
            
            if( Activator.DEBUG )
                Activator.trace(String.format("Parts active = %1$d", _activeMap.size())); //$NON-NLS-1$
        }
    }
    
    private static IDocument getPartDocument(IEditorPart part)
    {
         ITextEditor editor = (ITextEditor) part.getAdapter(ITextEditor.class);
         IDocument document = null;
         if (editor != null) {
           IDocumentProvider provider = editor.getDocumentProvider();
           if( provider != null )
               document = provider.getDocument(editor.getEditorInput());
         }
         return document;
    }
    
    
    /**
     * Calls AbstractTextEditor.getSourceViewer() through reflection, as that method is normally protected (for some
     * ungodly reason).
     * 
     * @param AbstractTextEditor to run reflection on
     */
    private static ITextViewer callGetSourceViewer(IEditorPart editor) 
    {
        if( editor == null || !(editor instanceof AbstractTextEditor) )
            return null;

        try 
        {            
            Method method = AbstractTextEditor.class.getDeclaredMethod("getSourceViewer"); //$NON-NLS-1$
            method.setAccessible(true);

            return (ITextViewer) method.invoke(editor);
        } 
        catch (Exception e) 
        {
            Activator.log(e);
        }
        
        /*
         * StyledText text = (StyledText) editor.getAdapter(Control.class);
         */
        
        return null;
    }

}
