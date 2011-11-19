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
package com.chookapp.org.bracketeer.common;

import org.eclipse.jface.text.Position;

public class SingleBracket
{
    Position _position;
    boolean _isOpening; // is this an opening bracket, such as "("
    char _char;
    
    public SingleBracket(int offset, boolean isOpening, char ch)
    {
        _position = new Position(offset, 1);
        _isOpening = isOpening;
        _char = ch;
    }
    
    public Position getPosition()
    {
        if (!_position.isDeleted && _position.length > 0)
            return _position;
        else
            return null;
    }
    
    public Position getPositionRaw()
    {
        return _position;
    }
    
    public boolean isOpening()
    {
        return _isOpening;
    }
    
    public char getChar()
    {
        return _char;
    }
    
    @Override
    public String toString()
    {
        return String.format("[offset=%1$d, isOpening=%2$b]", _position.offset, _isOpening); //$NON-NLS-1$
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if(! (obj instanceof SingleBracket))
            return false;
        
        SingleBracket other = (SingleBracket) obj;
        
        return (other._isOpening == _isOpening && other._position.equals(_position));
    }
    
    @Override
    public int hashCode()
    {
        return _position.hashCode() ^ (_isOpening ? 2 : 4);
    }

    
}
