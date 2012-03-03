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
package com.chookapp.org.bracketeer.jdt;

import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;

import com.chookapp.org.bracketeer.extensionpoint.BracketeerProcessor;
import com.chookapp.org.bracketeer.extensionpoint.IBracketeerProcessorsFactory;

public class BracketeerProcessorsFactory implements IBracketeerProcessorsFactory 
{

	public BracketeerProcessorsFactory() 
	{
	}
	
	@Override
	public BracketeerProcessor createProcessorFor(IEditorPart part, IDocument doc) 
	{
	    try
	    {
    		if( part.getClass().getName().equals("org.eclipse.jdt.internal.ui.javaeditor.ClassFileEditor") || //$NON-NLS-1$
    			part.getClass().getName().equals("org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor") || //$NON-NLS-1$
    			part.getClass().getSuperclass().getName().equals("org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor") ||  //$NON-NLS-1$
    			part.getClass().getSuperclass().getName().equals("org.eclipse.jdt.internal.ui.javaeditor.ClassFileEditor") ) //$NON-NLS-1$
    		{
    		    return new BracketeerJdtProcessor(part, doc);
    		}		
	    } 
	    catch(Exception e)
	    {	        
	    }
		return null;
	}

}
