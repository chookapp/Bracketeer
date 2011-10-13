package com.chookapp.org.bracketeer.core;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;

import com.chookapp.org.bracketeer.Activator;

public class PaintableBracket extends PaintableObject
{

    public PaintableBracket(Position position, RGB foreground, RGB background)
    {
        super(position, foreground, background);
    }

    @Override
    protected void innerPaint(GC gc, StyledText st, IDocument doc,
                              IRegion widgetRange)
    {
        
        int offset = widgetRange.getOffset();
        int length = widgetRange.getLength();
        if (length != 1)
            throw new IllegalArgumentException(String.format("length %1$d != 1", length));
        
        Point p = st.getLocationAtOffset(offset);
        
        try
        {
            gc.drawText(doc.get(_position.getOffset(), 1), p.x, p.y);
        }
        catch (BadLocationException e)
        {
            Activator.log(e);
        }
    }

    
}
