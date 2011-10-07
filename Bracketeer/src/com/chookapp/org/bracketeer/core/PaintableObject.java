package com.chookapp.org.bracketeer.core;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.chookapp.org.bracketeer.Activator;

public class PaintableObject
{
    private Position _position;
    private RGB _foreground;
    private RGB _background;
    
    public PaintableObject(Position position, RGB foreground, RGB background)
    {
        _position = position;
        _foreground = foreground;
        _background = background;
    }

    public Position getPosition()
    {
        return _position;
    }

    public RGB getForeground()
    {
        return _foreground;
    }

    public RGB getBackground()
    {
        return _background;
    }

    public void paint(GC gc, StyledText st, IDocument doc, IRegion widgetRange)
    {
        if(_position.isDeleted || _position.length == 0)
            return;
        
        if( widgetRange == null )
            return;
        
        int offset = widgetRange.getOffset();
        int length = widgetRange.getLength();
        if (length != 1)
            throw new IllegalArgumentException(String.format("length %1$d != 1", length));
        
        Point p = st.getLocationAtOffset(offset);
        
        Color bg = null, fg = null;
        Color oldBackground = null, oldForeground = null;
        
        if( _background != null )
        {
            bg = new Color(Display.getDefault(), _background);
            oldBackground = gc.getBackground();
            gc.setBackground(bg);
        }
        
        if( _foreground != null )
        {
            fg = new Color(Display.getDefault(), _foreground);
            oldForeground = gc.getForeground();
            gc.setForeground(fg);
        }
        
        try
        {
            gc.drawText(doc.get(_position.getOffset(), 1), p.x, p.y);
        }
        catch (BadLocationException e)
        {
            Activator.log(e);
        }
        
        if( _background != null )
        {
            gc.setBackground(oldBackground);
            bg.dispose();
        }
        if( _foreground != null )
        {
            gc.setForeground(oldForeground);                
            fg.dispose();
        }
        
    }
        
   
}
