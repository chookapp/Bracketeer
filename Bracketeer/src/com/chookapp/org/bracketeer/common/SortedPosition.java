package com.chookapp.org.bracketeer.common;

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.Position;

class SortedPosition extends Position implements Comparable<SortedPosition>
{
    //private Position _position;
    
    Map<SortedPosition, ?> _container;
    

    public SortedPosition(int offset, int length)
    {
        super(offset, length);
        _container = null;
    }
    
    
    public void setContainer(Map<SortedPosition, ?> container)
    {
        _container = container;
    }
    
    /*
    @Override
    public boolean equals(Object obj)
    {
        if( obj == null )
            return false;
        
        if( !(obj instanceof SortedPosition) )
            return false;
        
        SortedPosition other = (SortedPosition)obj;
        
        return _position.offset == other._position.offset;
    }
    */

    @Override
    public int compareTo(SortedPosition other)
    {            
        return offset - other.offset;
    }
    
    /*
    @Override
    public String toString()
    {
        return _position.toString();
    }
    */

    @Override
    public void delete()
    {
        super.delete();
        if( _container != null )
        {
            Object r = _container.remove(this);
            Assert.isNotNull(r);
        }
    }
    

}