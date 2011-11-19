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
 *******************************************************************************/
package com.chookapp.org.bracketeer.core;

import org.eclipse.jface.text.Position;

public class BracketPosition
{
    private Position _pos;
    private int _colorCode;
    
    public BracketPosition(int offset, int colorCode)
    {
        _pos = new Position(offset, 1);
        _colorCode = colorCode;
    }
    
    public Position getPosition()
    {
        return _pos;
    }

    public int getColorCode()
    {
        return _colorCode;
    }
}
