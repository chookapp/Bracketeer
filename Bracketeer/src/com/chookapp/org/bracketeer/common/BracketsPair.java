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
package com.chookapp.org.bracketeer.common;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Position;

public class BracketsPair
{
    private List<SingleBracket> _brackets;
    
    public BracketsPair(int openingOffset, char openingChar,
                        int closingOffset, char closingChar)
    {
        _brackets = new ArrayList<SingleBracket>();
        _brackets.add( new SingleBracket(openingOffset, true, openingChar));
        _brackets.add( new SingleBracket(closingOffset, false, closingChar));
    }   

    public List<SingleBracket> getBrackets()
    {
        return _brackets;
    }
    
    @Override
    public String toString()
    {
        return _brackets.toString();
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if(! (obj instanceof BracketsPair) )
            return false;
        
        BracketsPair other = (BracketsPair) obj;
        return getBrackets().equals(other.getBrackets());
        /*
        for (SingleBracket bracket : _brackets)
        {
            if(! other.getBrackets().contains(bracket) )
                return false;
        }
        
        return true;
        */
    }
    
    @Override
    public int hashCode()
    {        
        return _brackets.hashCode();
    }
    
    public SingleBracket getOpeningBracket()
    {
        return _brackets.get(0);
    }

    public SingleBracket getClosingBracket()
    {
        return _brackets.get(1);
    }

    public SingleBracket getBracketAt(int offset)
    {
        for (SingleBracket br : _brackets)
        {
            Position pos = br.getPosition();
            if( pos != null && pos.offset == offset )
                return br;
        }
        return null;
    }

    public boolean hasDeletedPosition()
    {
        for (SingleBracket br : _brackets)
        {
            if( br.getPosition() == null )
                return true;
        }
        return false;
    }

    public int getDistanceBetweenBrackets()
    {        
        return (getClosingBracket().getPositionRaw().getOffset() -
                getOpeningBracket().getPositionRaw().getOffset());
    }
}
