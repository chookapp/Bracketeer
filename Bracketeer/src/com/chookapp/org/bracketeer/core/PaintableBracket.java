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
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.chookapp.org.bracketeer.preferences.PreferencesConstants;

public class PaintableBracket extends PaintableObject
{
    private String _highlightType;
    private RGB _outlineColor;
    
    public PaintableBracket(Position position, RGB foreground, RGB background, String highlightType)
    {
        super(position, foreground, 
              (highlightType.equals(PreferencesConstants.Highlights.HighlightTypeValSolid)) ? background : null);
        _highlightType = highlightType;
        _outlineColor = background;
    }

    @Override
    protected void innerPaint(GC gc, StyledText st, IDocument doc,
                              IRegion widgetRange, Rectangle widgetRect)
    {
        
        int offset = widgetRange.getOffset();
        int length = widgetRange.getLength();
        if (length != 1)
            throw new IllegalArgumentException(String.format("length %1$d != 1", length)); //$NON-NLS-1$
        
        Point p = st.getLocationAtOffset(offset);
        String txt = null;
        
        try
        {
            txt = doc.get(_position.getOffset(), 1);
        }
        catch (BadLocationException e)
        {
            return;
        }
        
        gc.drawText(txt, p.x, p.y, _background == null);
        
        if( _highlightType.equals(PreferencesConstants.Highlights.HighlightTypeValOutline) )
        {
            Color oldFg = gc.getForeground();
            Color fg = null;
            if( _outlineColor == null )
            {
                gc.setForeground(gc.getBackground());
            }
            else
            {
                fg = new Color(Display.getDefault(), _outlineColor);
                gc.setForeground(fg);
            }
            
            Point metrics = gc.textExtent(txt);
            Rectangle rect = new Rectangle(p.x, p.y, metrics.x-1, metrics.y-1);
            
            gc.drawRectangle(rect);
                            
            gc.setForeground(oldFg);
            if( fg != null )
                fg.dispose();
        }
     
    }

    public PaintableBracket clone(Position newPos)
    {
        PaintableBracket ret = new PaintableBracket(newPos, _foreground, _background, _highlightType);
        ret._outlineColor = _outlineColor;
        return ret; 
    }

    
}
