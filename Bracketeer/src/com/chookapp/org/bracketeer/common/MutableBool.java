package com.chookapp.org.bracketeer.common;

public class MutableBool
{
    private boolean _val;
    
    public MutableBool(boolean val)
    {
        set(val);
    }
    
    public void set(boolean val)
    {
        _val = val;
    }
    
    public boolean get()
    {
        return _val;
    }
}
