/*******************************************************************************
 * Copyright (c) Gil Barash - chookapp@yahoo.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gil Barash - initial API and implementation
 *******************************************************************************/
package com.chookapp.org.bracketeer.core;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

public class PaintableHint extends PaintableObject
{

    private String _txt;
    private boolean _italic;
    private boolean _underline;

    public PaintableHint(Position drawPosition, RGB foreground, RGB background, 
                         boolean italic, String txt)
    {
        super(drawPosition, foreground, background);
        _txt = txt;
        _italic = italic;
        _underline = false;
    }

    @Override
    public boolean equals(Object obj)
    {
        if( !(obj instanceof PaintableHint))
            return false;
        
        PaintableHint other = (PaintableHint)obj;
        if( (!_txt.equals(other._txt)) || (_italic != other._italic) )
        {
            return false;
        }        
        
        return super.equals(obj);
    }
    
    @Override
    protected void innerPaint(GC gc, StyledText st, IDocument doc,
                              IRegion widgetRange, Rectangle rect)
    {
        Font oldFont = null;
        Font newFont = null;
        if(_italic)
        {
             oldFont = gc.getFont();
             FontData[] oldDatas = oldFont.getFontData();
             FontData[] newDatas = new FontData[oldDatas.length];
             for (int i = 0; i < oldDatas.length; i++)
             {
                 FontData oldData = oldDatas[i];
                 FontData fontData = new FontData(oldData.getName(), 
                                                  oldData.getHeight(), 
                                                  SWT.ITALIC);
                 fontData.setLocale(oldData.getLocale());
                 newDatas[i] = fontData;
             }
             newFont = new Font(Display.getDefault(), newDatas);
             gc.setFont(newFont); 
        }
        
        gc.drawText(_txt, rect.x, rect.y, _background == null);
        if(_underline )
            gc.drawLine(rect.x - 1, rect.y + rect.height - 1, 
                        rect.x + rect.width + 1, rect.y + rect.height - 1);
        
        if( newFont != null )
        {
            gc.setFont(oldFont);
            newFont.dispose();
        }
    }

    public boolean isOkToShow(IDocument doc) throws BadLocationException
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
            if( widgetRange == null )
                return null;
            
            if( !isOkToShow(doc) )
                return null;
            
            int offset = widgetRange.getOffset();
            
            Point p = st.getLocationAtOffset(offset);
            p.x += gc.getAdvanceWidth(doc.getChar(_position.getOffset()));
            
            Point metrics = gc.textExtent(_txt);
            Rectangle rect = new Rectangle(p.x, p.y, metrics.x, metrics.y);
            return rect;
        }
        catch (BadLocationException e)
        {
        }
        return null;
    }

    public void setUnderline(boolean underline)
    {
        _underline = underline;        
    }

}
