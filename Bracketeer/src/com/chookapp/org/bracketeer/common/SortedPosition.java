package com.chookapp.org.bracketeer.common;

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.Position;

public class SortedPosition extends Position implements Comparable<SortedPosition>
{
    //private Position _position;
    
    Map<SortedPosition, ? extends Object> _container;

    public SortedPosition(int offset, int length)
    {
        super(offset, length);
        _container = null;
    }
    
    
    public void setContainer(Map<SortedPosition, ? extends Object> container)
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
            synchronized(_container)
            {
                Object r = _container.remove(this);
                Assert.isNotNull(r);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public void update(int a_offset, int a_length)
    {
        Object r = null;
        if( _container != null )
        {
            synchronized(_container)
            {
                r = _container.remove(this);
                Assert.isNotNull(r);
                offset = a_offset;
                length = a_length;
                ((Map<SortedPosition, Object>)_container).put(this, r);
            }
        }
        else
        {
            offset = a_offset;
            length = a_length;
        }
    }

}