package com.chookapp.org.bracketeer.common;

import org.eclipse.jface.text.Position;

public class Hint
{
    private String _str;
    private String _type;
    
    private Position _originPosition;
    private Position _hintPosition;
    
    public Hint(String type, int originOffset, int hintOffset, String txt)
    {
        _originPosition = new Position(originOffset, 1);
        _hintPosition = new Position(hintOffset, 1);
        _str = txt;
        _type = type;
    }

    public String getTxt()
    {
        return _str;
    }
    
    public String getType()
    {
        return _type;
    }
    
    public Position getHintPosition()
    {
        if (!_hintPosition.isDeleted && _hintPosition.length > 0)
            return _hintPosition;
        else
            return null;
    }
    
    public Position getHintPositionRaw()
    {
        return _hintPosition;
    }
    
    public Position getOriginPosition()
    {
        if (!_originPosition.isDeleted && _originPosition.length > 0)
            return _originPosition;
        else
            return null;
    }
    
    public Position getOriginPositionRaw()
    {
        return _originPosition;
    }

    public void setTxt(String str)
    {
        _str = str;
    }

    public boolean hasDeletedPosition()
    {        
        return ((getHintPosition() == null) || (getOriginPosition() == null));
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if( !(obj instanceof Hint) )
            return false;
            
        Hint other = (Hint) obj;
        return _str.equals(other._str) &&
                _type.equals(other._type) &&
                _originPosition.equals(other._originPosition) &&
                _hintPosition.equals(other._hintPosition);
    }
    
}