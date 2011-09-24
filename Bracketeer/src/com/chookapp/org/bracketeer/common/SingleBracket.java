package com.chookapp.org.bracketeer.common;

import org.eclipse.jface.text.Position;

public class SingleBracket
{
    Position _position;
    boolean _isOpening; // is this an opening bracket, such as "("
    
    public SingleBracket(int offset, boolean isOpening)
    {
        _position = new Position(offset, 1);
        _isOpening = isOpening;
    }
    
    public Position getPosition()
    {
        return _position;
    }
    
    public boolean isOpening()
    {
        return _isOpening;
    }
    
    @Override
    public String toString()
    {
        return String.format("[offset=%1$d, isOpening=%2$b]", _position.offset, _isOpening);
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
