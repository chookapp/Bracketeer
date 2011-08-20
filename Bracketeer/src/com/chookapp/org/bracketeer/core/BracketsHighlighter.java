/*******************************************************************************
 * Copyright (c) Gil Barash - chookapp@yahoo.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gil Barash - initial API and implementation
 *    
 * Thanks to:
 *    emil.crumhorn@gmail.com - Some of the code was copied from the 
 *    "eclipsemissingfeatrues" plugin. 
 *******************************************************************************/

package com.chookapp.org.bracketeer.core;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.services.IDisposable;

import com.chookapp.org.bracketeer.Activator;
import com.chookapp.org.bracketeer.common.BracketsPair;
import com.chookapp.org.bracketeer.extensionpoint.IBracketeerProcessor;


public class BracketsHighlighter implements CaretListener, Listener, PaintListener, IDisposable, IPainter {

	private static final int UNMATCHED_BRACKET_COLOR_CODE = 20;
    private ITextViewer _textViewer;
	private IBracketeerProcessor _processor;
	private IPaintPositionManager _positionManager;
	
	boolean _isActive;
	
	private Object _bracketsListsLock;
	private List<BracketPosition> _bracketsToClear;
	private List<BracketPosition> _bracketsToPaint;
	
	
	
	public BracketsHighlighter()
	{
	    _textViewer = null;
	    _processor = null;
	    _positionManager = null;
	    
	    _isActive = false;
	    
	    _bracketsListsLock = new Object();
	    _bracketsToClear = new LinkedList<BracketPosition>();
	    _bracketsToPaint = new LinkedList<BracketPosition>();
	}
	
	@Override
	public void dispose() {
		if( _textViewer == null )
			return;
		
		deactivate(false);
	}
	
	/************************************************************
	 * public methods
	 ************************************************************/
	
	public void Init(IBracketeerProcessor processor, ITextViewer textViewer) {
		
		_processor = processor;
		_textViewer = textViewer;
        
        ITextViewerExtension2 extension= (ITextViewerExtension2) textViewer;
        extension.addPainter(this);
	}	
	
	public ITextViewer getTextViewer()
	{
		return _textViewer;
	}
	
	/************************************************************
	 * listeners
	 ************************************************************/
	
	@Override
	public void caretMoved(CaretEvent event) {
//		_caretOffset = event.caretOffset;
	}
	
	/*
	 * Events:
	 * - MouseHover
	 * 
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	@Override
	public void handleEvent(Event event) {
		StyledText st = (StyledText)(_textViewer.getTextWidget());
		int caret;
		try
		{
			caret = st.getOffsetAtLocation(new Point(event.x, event.y));
			caret = ((ProjectionViewer)_textViewer).widgetOffset2ModelOffset(caret);
		}
		catch(SWTException e)
		{
			Activator.log(e);
			return;			
		}
		catch(IllegalArgumentException e)
		{
			return;
		}
		
		try
		{
		    mouseHoverAt(st, caret);
		} 
		catch(Exception e )
		{
		    Activator.log(e);
		}
	}
	
	@Override
	public void paintControl(PaintEvent e) {
	}
	
    /************************************************************
     * IPainter interface
     ************************************************************/  

    @Override
    public void paint(int reason)
    {
        if(!_isActive)
        {
            _isActive = true;
            
            StyledText st = _textViewer.getTextWidget();
            
            st.addCaretListener(this);
            st.addListener(SWT.MouseHover, this);
            st.addPaintListener(this);
        }
    }

    @Override
    public void deactivate(boolean redraw)
    {
        if(!_isActive)
            return;
        
        _isActive = false;
        
        StyledText st = _textViewer.getTextWidget();
        if( st == null )
            return;
        
        st.removeCaretListener(this);
        st.removeListener(SWT.MouseHover, this);
        st.removePaintListener(this);
    }

    @Override
    public void setPositionManager(IPaintPositionManager manager)
    {
        _positionManager = manager;
    }
	
	/************************************************************
	 * the work itself
	 ************************************************************/	

    private void mouseHoverAt(StyledText st, int origCaret)
    {

        int startPoint = Math.max(0, origCaret - 2);
        int endPoint = Math.min(_textViewer.getDocument().getLength(),
                                origCaret + 2);

        List<BracketsPair> listOfPairs = new LinkedList<BracketsPair>();
        
        for (int caret = startPoint; caret < endPoint; caret++)
        {
            BracketsPair pair = _processor.getMatchingPair(caret + 1);
            if (pair == null)
                continue;
            
            if( !pair.isValid() )
                continue;
            
            if( !listOfPairs.contains(pair) )
                listOfPairs.add(pair);
        }

        if(listOfPairs.isEmpty())
            return;
        
        synchronized (_bracketsListsLock)
        {            
            _bracketsToClear.addAll(_bracketsToPaint);
            _bracketsToPaint = new LinkedList<BracketPosition>();
            int colorCode = 1;
            int colorCodeStep = 1;
            
            if( listOfPairs.get(0).getBrackets().get(0).isOpening() )
            {
                colorCode = listOfPairs.size();
                colorCodeStep = -1;
            }
            
            for (BracketsPair bracketsPair : listOfPairs)
            {
                createPositionsFromPair(bracketsPair, colorCode);
                colorCode += colorCodeStep;                
            }            
        }
        
        drawHighlights();
    }

    private void drawHighlights()
    {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                try {
                    synchronized (_bracketsListsLock)
                    {
                        clearHighlighting();
                        _textViewer.getTextWidget().update();
                        paintHighlighting();
                    }
                }
                catch (Exception err) {
                    Activator.log(err);
                }
            }

            private void clearHighlighting()
            {
                StyledText st = _textViewer.getTextWidget();
                
                for (BracketPosition pos : _bracketsToClear)
                {
                    IRegion widgetRange = getWidgetRange(pos.getPosition().getOffset(), 
                                                         pos.getPosition().getLength());
                    
                    if( widgetRange != null )
                    {
                        int offset = widgetRange.getOffset();
                        int length = widgetRange.getLength() + 1;
                        
                        if( offset + length + 1 >= st.getCharCount() )
                        {
                            st.redraw();
                        }
                        else
                        {
                            /* copied from "SourceViewerDecorationSupoprt"... */
                            
                            char ch = st.getTextRange(offset + 1, 1).charAt(0);
                            if (ch == '\r' || ch == '\n') {
                                // at the end of a line, redraw up to the next line start
                                int nextLine = st.getLineAtOffset(offset + 1) + 1;
                                if (nextLine >= st.getLineCount()) {
                                    /*
                                     * Panic code: should not happen, as offset is not the last offset,
                                     * and there is a delimiter character at offset.
                                     */
                                    st.redraw();
                                    continue;
                                }

                                int nextLineOffset = st.getOffsetAtLine(nextLine);
                                length = nextLineOffset - offset;                            
                            }
                            
                            st.redrawRange(offset, length, true);
                        }
                    }
                    else
                    {
                        st.redraw();
                    }
                    
                    _positionManager.unmanagePosition(pos.getPosition());
                }
                
                _bracketsToClear = new LinkedList<BracketPosition>();
            }
            
            private void paintHighlighting()
            {
                StyledText st = _textViewer.getTextWidget();
                GC gc = new GC(st);
                
                for (BracketPosition pos : _bracketsToPaint)
                {
                    try
                    {
                        paintBracket(gc, st, pos);
                    }
                    catch (BadLocationException e)
                    {
                        Activator.log(e);
                    }
                }
                
                gc.dispose();
            }

            private void paintBracket(GC gc, StyledText st, BracketPosition brPos) throws BadLocationException
            {
                Position pos = brPos.getPosition();
                if (pos.isDeleted)
                    return;

                IRegion widgetRange = getWidgetRange(pos.getOffset(), pos.getLength());
                if( widgetRange == null )
                    return;
                
                int offset = widgetRange.getOffset();
                int length = widgetRange.getLength();
                if (length != 1)
                    throw new IllegalArgumentException(String.format("length %1$d != 1", length));
                
                Point p = st.getLocationAtOffset(offset);
                
                Color bg = new Color(Display.getDefault(),getBgColorCode(brPos.getColorCode()));
                Color fg = new Color(Display.getDefault(),getFgColorCode(brPos.getColorCode()));
                
                Color oldBackground = gc.getBackground();
                Color oldForeground = gc.getForeground();
                
                gc.setBackground(bg);
                gc.setForeground(fg);
                
                gc.drawText(_textViewer.getDocument().get(pos.getOffset(), 1), p.x, p.y);
                
                gc.setBackground(oldBackground);
                gc.setForeground(oldForeground);                
                
                bg.dispose();
                fg.dispose();
            }

            private IRegion getWidgetRange(int offset, int length)
            {                
                if (_textViewer instanceof ITextViewerExtension5) {
                    ITextViewerExtension5 extension= (ITextViewerExtension5) _textViewer;
                    IRegion widgetRange= extension.modelRange2WidgetRange(new Region(offset, length));
                    if (widgetRange == null)
                        return null;

                    try {
                        // don't draw if the pair position is really hidden and widgetRange just
                        // marks the coverage around it.
                        IDocument doc= _textViewer.getDocument();
                        int startLine= doc.getLineOfOffset(offset);
                        int endLine= doc.getLineOfOffset(offset + length);
                        if (extension.modelLine2WidgetLine(startLine) == -1 || extension.modelLine2WidgetLine(endLine) == -1)
                            return null;
                    } catch (BadLocationException e) {
                        return null;
                    }

                    return widgetRange;

                } else {
                    IRegion region= _textViewer.getVisibleRegion();
                    if (region.getOffset() > offset || region.getOffset() + region.getLength() < offset + length)
                        return null;
                    offset -= region.getOffset();
                    
                    return new Region(offset, length);
                }
            }

            private RGB getFgColorCode(int colorCode)
            {
                return new RGB(255,255,255);
            }

            private RGB getBgColorCode(int colorCode)
            {
                if( colorCode == UNMATCHED_BRACKET_COLOR_CODE )
                    return new RGB(250,0,0);
                
                return new RGB(0+(colorCode*50),
                               0+(colorCode*50),
                               0+(colorCode*50));
            }
            
        });        
    }

    private void createPositionsFromPair(BracketsPair bracketsPair,
                                         int colorCode)
    {
        if( bracketsPair.getBrackets().size() == 1 )
            colorCode = UNMATCHED_BRACKET_COLOR_CODE;
        
        for (BracketsPair.Bracket bracket : bracketsPair.getBrackets())
        {
            BracketPosition pos = new BracketPosition(bracket.getOffset() - 1, 
                                                      colorCode);
            _bracketsToPaint.add(pos);
            _positionManager.managePosition(pos.getPosition());
        }        
    }

	
}
