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
import java.util.NoSuchElementException;

import com.chookapp.org.bracketeer.Activator;

public class BracketsPair
{
    public class Bracket
    {
        int _offset;
        boolean _isOpening; // is this an opening bracket, such as "("
        
        public Bracket(int offset, boolean isOpening)
        {
            _offset = offset;
            _isOpening = isOpening;
        }
        
        public int getOffset()
        {
            return _offset;
        }
        public boolean isOpening()
        {
            return _isOpening;
        }
        
        @Override
        public String toString()
        {
            return String.format("[offset=%1$d, isOpening=%2$b]", _offset, _isOpening);
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if(! (obj instanceof Bracket))
                return false;
            
            Bracket other = (Bracket) obj;
            
            return (other._isOpening == _isOpening && other._offset == _offset);
        }
    }
    
    private List<Bracket> _brackets;
    
    public BracketsPair()
    {
        _brackets = new ArrayList<Bracket>();
    }
    
    public void addBracket(int offset, boolean isOpening )
    {
        _brackets.add( new Bracket(offset, isOpening));
        if( _brackets.size() > 2 )
            throw new NoSuchElementException("cannot have more than two brackets in a pair");
    }

    public List<Bracket> getBrackets()
    {
        return _brackets;
    }

    public boolean isValid()
    {
        if( _brackets.size() == 0 )
        {
            Activator.log("pair has no brackets in it");            
            return false;
        }
        
        if( _brackets.size() == 2 )
        {
            if( _brackets.get(0).getOffset() == _brackets.get(1).getOffset() )
            {
                Activator.log("pair offstes are the same");
                return false;
            }
            
            if( _brackets.get(0).isOpening() == _brackets.get(1).isOpening() )
            {
                Activator.log("pair openings are the same");
                return false;
            }
        }
        
        return true;
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
        
        for (Bracket bracket : _brackets)
        {
            if(! other.getBrackets().contains(bracket) )
                return false;
        }
        
        return true;
    }
}
