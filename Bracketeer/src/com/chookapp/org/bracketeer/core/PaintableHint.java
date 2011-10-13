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

public class PaintableHint extends PaintableObject
{

    private String _txt;

    public PaintableHint(Position position, RGB foreground, RGB background, 
                         String txt)
    {
        super(position, foreground, background);
        _txt = txt;
    }

    @Override
    protected void innerPaint(GC gc, StyledText st, IDocument doc,
                              IRegion widgetRange)
    {
        try
        {
            if( !check(doc) )
                return;
            
            int offset = widgetRange.getOffset();
            int length = widgetRange.getLength();
            if (length != 1)
                throw new IllegalArgumentException(String.format("length %1$d != 1", length));
            
            Point p = st.getLocationAtOffset(offset);
            p.x += gc.getCharWidth(doc.getChar(_position.getOffset()));
            
            gc.drawText(_txt, p.x, p.y);
        }
        catch (BadLocationException e)
        {
            Activator.log(e);
        }
    }

    private boolean check(IDocument doc) throws BadLocationException
    {
        IRegion region = doc.getLineInformationOfOffset(_position.getOffset());
        int startOffset = _position.getOffset() + 1;
        int endOffset = region.getOffset() + region.getLength();
        
        String str = doc.get(startOffset, endOffset-startOffset);
        for (char c : str.toCharArray())
        {
            if( c != '\t' && c != ' ' )
                return false;
        }
        return true;
    }

}
