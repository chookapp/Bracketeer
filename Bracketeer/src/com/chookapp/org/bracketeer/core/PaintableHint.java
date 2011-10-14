package com.chookapp.org.bracketeer.core;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

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
    public boolean equals(Object obj)
    {
        if( !(obj instanceof PaintableHint))
            return false;
        
        if( !_txt.equals(((PaintableHint)obj)._txt))
            return false;
        
        return super.equals(obj);
    }
    
    @Override
    protected void innerPaint(GC gc, StyledText st, IDocument doc,
                              IRegion widgetRange, Rectangle rect)
    {
        gc.drawText(_txt, rect.x, rect.y);
    }

    private boolean check(IDocument doc) throws BadLocationException
    {
        IRegion region = doc.getLineInformationOfOffset(_position.getOffset());
        int startOffset = _position.getOffset() + 1;
        int endOffset = region.getOffset() + region.getLength();
        
//        // is this the last char in the document?
//        if( startOffset >= doc.getLength() )
//            return true;
        
        endOffset = Math.min(endOffset, doc.getLength()-1);
        
        // is the last char in the line?
        if( startOffset >= endOffset )
            return true;
        
        String str = doc.get(startOffset, endOffset-startOffset);
        for (char c : str.toCharArray())
        {
            if( c != '\t' && c != ' ' )
                return false;
        }
        return true;
    }

    public Rectangle getWidgetRect(GC gc, StyledText st, IDocument doc,
                                   IRegion widgetRange)
    {
        try
        {
            if( !check(doc) )
                return null;
            
            int offset = widgetRange.getOffset();
            
            Point p = st.getLocationAtOffset(offset);
            p.x += gc.getAdvanceWidth(doc.getChar(_position.getOffset()));
            
            FontMetrics metrics = gc.getFontMetrics();
            Rectangle rect = new Rectangle(p.x, p.y, 
                                           metrics.getAverageCharWidth() *(_txt.length()),
                                           metrics.getHeight());
            return rect;
        }
        catch (BadLocationException e)
        {
            Activator.log(e);
        }
        return null;
    }

}
