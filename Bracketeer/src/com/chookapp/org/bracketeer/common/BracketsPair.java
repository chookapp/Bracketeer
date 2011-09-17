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

public class BracketsPair
{
    private List<SingleBracket> _brackets;
    
    public BracketsPair(int openingOffset, int closingOffset)
    {
        _brackets = new ArrayList<SingleBracket>();
        _brackets.add( new SingleBracket(openingOffset, true));
        _brackets.add( new SingleBracket(closingOffset, false));
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
}
