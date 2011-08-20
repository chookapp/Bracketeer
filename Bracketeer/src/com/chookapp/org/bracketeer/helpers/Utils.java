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

package com.chookapp.org.bracketeer.helpers;

import java.lang.reflect.Method;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.chookapp.org.bracketeer.Activator;

public class Utils {
	
    private static final String _openingBrackets = "([{<";
    
    /**
     * Calls AbstractTextEditor.getSourceViewer() through reflection, as that method is normally protected (for some
     * ungodly reason).
     * 
     * @param AbstractTextEditor to run reflection on
     */
    public static ITextViewer callGetSourceViewer(IEditorPart editor) {
        try 
        {
            Method method = AbstractTextEditor.class.getDeclaredMethod("getSourceViewer");
            method.setAccessible(true);

            return (ITextViewer) method.invoke(editor);
        } 
        catch (Exception e) 
        {
            Activator.log(e);
        }
        
        return null;
    }

    public static boolean isOpenningBracket(char prevChar)
    {
        return _openingBrackets.indexOf(prevChar) != -1;
    }

}
