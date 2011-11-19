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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

import com.chookapp.org.bracketeer.Activator;

public class PaintableBracket extends PaintableObject
{

    public PaintableBracket(Position position, RGB foreground, RGB background)
    {
        super(position, foreground, background);
    }

    @Override
    protected void innerPaint(GC gc, StyledText st, IDocument doc,
                              IRegion widgetRange, Rectangle rect)
    {
        
        int offset = widgetRange.getOffset();
        int length = widgetRange.getLength();
        if (length != 1)
            throw new IllegalArgumentException(String.format("length %1$d != 1", length)); //$NON-NLS-1$
        
        Point p = st.getLocationAtOffset(offset);
        
        try
        {
            gc.drawText(doc.get(_position.getOffset(), 1), p.x, p.y, _background == null);
        }
        catch (BadLocationException e)
        {
            Activator.log(e);
        }
    }

    
}
