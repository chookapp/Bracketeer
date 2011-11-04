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

package com.chookapp.org.bracketeer.common;

public class Utils {
	
    private static final String _openingBrackets = "([{<"; //$NON-NLS-1$

    public static boolean isOpenningBracket(char prevChar)
    {
        return _openingBrackets.indexOf(prevChar) != -1;
    }

}
