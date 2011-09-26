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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

import com.chookapp.org.bracketeer.Activator;
import com.chookapp.org.bracketeer.common.BracketeerProcessingContainer;
import com.chookapp.org.bracketeer.common.BracketsPair;
import com.chookapp.org.bracketeer.common.SingleBracket;
import com.chookapp.org.bracketeer.extensionpoint.BracketeerProcessor;


public class BracketsHighlighter implements CaretListener, Listener, 
    PaintListener, IDisposable, IPainter, ProcessingThreadListener 
{

	private static final int UNMATCHED_BRACKET_COLOR_CODE = 20;
    private ISourceViewer _sourceViewer;
    private StyledText _textWidget;
    private IEditorPart _part;
	private ProcessingThread _processingThread;
	private IPaintPositionManager _positionManager;	
	
	private boolean _isActive;
	
	private Object _objectsToPaintListsLock;
	private List<PaintableObject> _hoveredPairsToPaint;
	private List<PaintableObject> _surroundingPairsToPaint;
	private List<PaintableObject> _singleBracketsToPaint;
	
	public BracketsHighlighter()
	{
	    _sourceViewer = null;
	    _processingThread = null;
	    _positionManager = null;
	    _textWidget = null;
	    
	    _isActive = false;
	    
	    _objectsToPaintListsLock = new Object();
	    _hoveredPairsToPaint = new LinkedList<PaintableObject>();
	    _surroundingPairsToPaint = new LinkedList<PaintableObject>();
	    _singleBracketsToPaint = new LinkedList<PaintableObject>();
	}
	
	@Override
	public void dispose() {
		if( _sourceViewer == null )
			return;
		
		deactivate(false);
		
		if (_processingThread != null)
		{
		    _processingThread.dispose();
		    _processingThread = null;
		}
	}
	
	/************************************************************
	 * public methods
	 * @param part 
	 ************************************************************/
	
	public void Init(BracketeerProcessor processor, IEditorPart part, ITextViewer textViewer) {
		
	    _processingThread = new ProcessingThread(part, processor);
	    _processingThread.addListener(this);
		_sourceViewer = (ISourceViewer) textViewer;
		_part = part;
		_textWidget = _sourceViewer.getTextWidget();
        
        ITextViewerExtension2 extension= (ITextViewerExtension2) textViewer;
        extension.addPainter(this);
	}	
	
	public ISourceViewer getSourceViewer()
	{
		return _sourceViewer;
	}
	
	/************************************************************
	 * listeners
	 ************************************************************/
	
	@Override
	public void caretMoved(CaretEvent event) 
	{
	    int caret = event.caretOffset;
	    caret = ((ProjectionViewer)_sourceViewer).widgetOffset2ModelOffset(caret);
	    caret -= 1;
	    caretMovedTo(caret);
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
		int caret;
		try
		{
			caret = _textWidget.getOffsetAtLocation(new Point(event.x, event.y));
			caret = ((ProjectionViewer)_sourceViewer).widgetOffset2ModelOffset(caret);
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
		    mouseHoverAt(_textWidget, caret);
		} 
		catch(Exception e )
		{
		    Activator.log(e);
		}
	}
	
	@Override
	public void paintControl(PaintEvent event) 
	{
	    IRegion region = computeClippingRegion(event);
	    if (region == null)
	        return;

	    int startOfset = region.getOffset();
	    int length = region.getLength();

	    for (PaintableObject paintObj : _singleBracketsToPaint)
        {
            if(paintObj.getPosition().overlapsWith(startOfset, length))
                paintObj.paint(event.gc, _textWidget, _sourceViewer.getDocument(),
                               getWidgetRange(paintObj.getPosition().getOffset(), paintObj.getPosition().getLength()));
        }

	    List<PaintableObject> pairsToPaint;
	    if( _hoveredPairsToPaint.isEmpty() )
	        pairsToPaint = _surroundingPairsToPaint;
	    else
	        pairsToPaint = _hoveredPairsToPaint;
	        
	    for (PaintableObject paintObj : pairsToPaint)
        {
            if(paintObj.getPosition().overlapsWith(startOfset, length))
                paintObj.paint(event.gc, _textWidget, _sourceViewer.getDocument(),
                               getWidgetRange(paintObj.getPosition().getOffset(), paintObj.getPosition().getLength()));
        }
	    
	   
	}

	public ITextViewer getTextViewer()
	{
	    return _sourceViewer;
	}

	@Override
	public void processingContainerUpdated()
	{	
	    _textWidget.getDisplay().asyncExec(new Runnable()
        {            
            @Override
            public void run()
            {                
                int caret = _textWidget.getCaretOffset();
                caret = ((ProjectionViewer)_sourceViewer).widgetOffset2ModelOffset(caret);
                caret -= 1;
                
                boolean update = updateSurroundingPairsToPaint(caret);
                update |= updateSingleBrackets();
                update |= clearHoveredPairsToPaint();
                
                if(update)
                {
                    // TODO: optimize? (redraw only the needed sections)
                    _textWidget.redraw();
                }
            }
        });
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
            
            StyledText st = _sourceViewer.getTextWidget();
            
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
        
        StyledText st = _sourceViewer.getTextWidget();
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

    private void caretMovedTo(int caretOffset)
    {        
        boolean update = updateSurroundingPairsToPaint(caretOffset);
        update |= clearHoveredPairsToPaint();
        
        if(update)
        {
            // TODO: optimize? (redraw only the needed sections)
            _textWidget.redraw();
        }
    }
    
    private boolean updateSurroundingPairsToPaint(int caretOffset)
    {
        BracketeerProcessingContainer cont = _processingThread.getBracketContainer();
        List<BracketsPair> listOfPairs = cont.getPairsSurrounding(caretOffset, 4);
        listOfPairs = sortPairs(listOfPairs);
        
        // do nothing if _surroundingPairsToPaint is equal to listOfPairs
        if(areEqualPairs(listOfPairs, _surroundingPairsToPaint))
            return false;
        
        clearSurroundingPairsToPaint();
        synchronized (_surroundingPairsToPaint)
        {            
            addPaintableObjectsPairs(listOfPairs, 1, 1, _surroundingPairsToPaint);
        }
        
        return true;
    }
    
    private boolean updateSingleBrackets()
    {
        BracketeerProcessingContainer cont = _processingThread.getBracketContainer();
        List<SingleBracket> list = cont.getSingleBrackets();

        // do nothing if _surroundingPairsToPaint is equal to listOfPairs
        if(areEqualSingle(list, _singleBracketsToPaint))
            return false;
        
        clearSingleBracketsToPaint();
        synchronized (_singleBracketsToPaint)
        {            
            addPaintableObjectsSingles(list, _singleBracketsToPaint);
        }
        
        return true;
    }


    private List<BracketsPair> sortPairs(List<BracketsPair> listOfPairs)
    {
        List<BracketsPair> ret = new ArrayList<BracketsPair>(listOfPairs.size());
        
        for (BracketsPair pair : listOfPairs)
        {
            int i = 0;
            while( i < ret.size() )
            {
                if( ret.get(i).getOpeningBracket().getPosition().offset <
                    pair.getOpeningBracket().getPosition().offset )
                {
                    break;
                }
                i++;
            }
            ret.add(i, pair);
        }
        
        return ret;
    }

    private void mouseHoverAt(StyledText st, int origCaret)
    {

//        int startPoint = Math.max(0, origCaret - 2);
//        int endPoint = Math.min(_sourceViewer.getDocument().getLength(),
//                                origCaret + 2);

        
        int length = 4;
        int startPoint = origCaret-2;
        
        BracketeerProcessingContainer cont = _processingThread.getBracketContainer();
        List<BracketsPair> listOfPairs = cont.getMatchingPairs(startPoint, length);
        listOfPairs = sortPairs(listOfPairs);
        
        if(listOfPairs.isEmpty())
            return;        
        
        // do nothing if _hoveredPairsToPaint is equal to listOfPairs
        if(areEqualPairs(listOfPairs, _hoveredPairsToPaint))
            return;
        
        clearHoveredPairsToPaint();        
        synchronized (_hoveredPairsToPaint)
        {           
            addPaintableObjectsPairs(listOfPairs, 1, 1, _hoveredPairsToPaint);
        }
        
        // TODO: optimize? (redraw only the needed sections)
        _textWidget.redraw();
                
        //drawHighlights();
    }

    private boolean areEqualPairs(List<BracketsPair> listOfPairs,
                                  List<PaintableObject> pairsToPaint)    
    {
        if( listOfPairs.size()*2 != pairsToPaint.size() )
            return false;
        
        for (BracketsPair bracketsPair : listOfPairs)
        {
            for( SingleBracket bracket : bracketsPair.getBrackets() )
            {
                boolean found = false;
                for (PaintableObject paintableObject : pairsToPaint)
                {
                    if(paintableObject.getPosition().equals(bracket.getPosition()))
                    {
                        found = true;
                        break;
                    }
                }
                if(!found)
                    return false;
            }
        }
        
        return true;
    }

    private boolean areEqualSingle(List<SingleBracket> list,
                                   List<PaintableObject> singlesToPaint)    
    {
        if( list.size() != singlesToPaint.size() )
            return false;
        
        for (SingleBracket bracket : list)
        {
            boolean found = false;
            for (PaintableObject paintableObject : singlesToPaint)
            {
                if(paintableObject.getPosition().equals(bracket.getPosition()))
                {
                    found = true;
                    break;
                }
            }
            if(!found)
                return false;
        }
        
        return true;
    }
    
    private void addPaintableObjectsPairs(List<BracketsPair> listOfPairs,
                                          int colorCode, int colorCodeStep,
                                          List<PaintableObject> paintableObjectsList)
    {
        for (BracketsPair bracketsPair : listOfPairs)
        {
            for( SingleBracket bracket : bracketsPair.getBrackets() )
            {
                paintableObjectsList.add(new PaintableObject(bracket.getPosition(),
                                                      new RGB(255,255,255),
                                                      new RGB(0+(colorCode*50),
                                                              0+(colorCode*50),
                                                              0+(colorCode*50))));
            }
            colorCode += colorCodeStep;                
        }
    }

    private void addPaintableObjectsSingles(List<SingleBracket> listOfSingles,
                                            List<PaintableObject> paintableObjectsList)
    {
        for (SingleBracket bracket : listOfSingles)
        {
            paintableObjectsList.add(new PaintableObject(bracket.getPosition(),
                                                         new RGB(255,255,255),
                                                         new RGB(250, 0, 0)));
        }
    }


    private boolean clearHoveredPairsToPaint()
    {
        synchronized (_hoveredPairsToPaint)
        {
            if(!_hoveredPairsToPaint.isEmpty())
            {
                _hoveredPairsToPaint.clear();
                return true;
            }
        }
        return false;
    }

    private boolean clearSurroundingPairsToPaint()
    {
        synchronized (_surroundingPairsToPaint)
        {
            if(!_surroundingPairsToPaint.isEmpty())
            {
                _surroundingPairsToPaint.clear();
                return true;
            }
        }
        return false;
    }
    
    private boolean clearSingleBracketsToPaint()
    {
        synchronized (_singleBracketsToPaint)
        {
            if(!_singleBracketsToPaint.isEmpty())
            {
                _singleBracketsToPaint.clear();
                return true;
            }
        }
        return false;
    }
    
    /**
     * (Copied from AnnotationPainter)
     * 
     * Computes the model (document) region that is covered by the paint event's clipping region. If
     * <code>event</code> is <code>null</code>, the model range covered by the visible editor
     * area (viewport) is returned.
     *
     * @param event the paint event or <code>null</code> to use the entire viewport
     * @param isClearing tells whether the clipping is need for clearing an annotation
     * @return the model region comprised by either the paint event's clipping region or the
     *         viewport
     * @since 3.2
     */
    private IRegion computeClippingRegion(PaintEvent event) 
    {
        if (event == null) {
           
            // trigger a repaint of the entire viewport
            int vOffset= getInclusiveTopIndexStartOffset();
            if (vOffset == -1)
                return null;

            // http://bugs.eclipse.org/bugs/show_bug.cgi?id=17147
            int vLength= getExclusiveBottomIndexEndOffset() - vOffset;

            return new Region(vOffset, vLength);
        }

        int widgetOffset;
        try {
            int widgetClippingStartOffset= _textWidget.getOffsetAtLocation(new Point(0, event.y));
            int firstWidgetLine= _textWidget.getLineAtOffset(widgetClippingStartOffset);
            widgetOffset= _textWidget.getOffsetAtLine(firstWidgetLine);
        } catch (IllegalArgumentException ex1) {
            try {
                int firstVisibleLine= JFaceTextUtil.getPartialTopIndex(_textWidget);
                widgetOffset= _textWidget.getOffsetAtLine(firstVisibleLine);
            } catch (IllegalArgumentException ex2) { // above try code might fail too
                widgetOffset= 0;
            }
        }

        int widgetEndOffset;
        try {
            int widgetClippingEndOffset= _textWidget.getOffsetAtLocation(new Point(0, event.y + event.height));
            int lastWidgetLine= _textWidget.getLineAtOffset(widgetClippingEndOffset);
            widgetEndOffset= _textWidget.getOffsetAtLine(lastWidgetLine + 1);
        } catch (IllegalArgumentException ex1) {
            // happens if the editor is not "full", e.g. the last line of the document is visible in the editor
            try {
                int lastVisibleLine= JFaceTextUtil.getPartialBottomIndex(_textWidget);
                if (lastVisibleLine == _textWidget.getLineCount() - 1)
                    // last line
                    widgetEndOffset= _textWidget.getCharCount();
                else
                    widgetEndOffset= _textWidget.getOffsetAtLine(lastVisibleLine + 1) - 1;
            } catch (IllegalArgumentException ex2) { // above try code might fail too
                widgetEndOffset= _textWidget.getCharCount();
            }
        }

        IRegion clippingRegion= getModelRange(widgetOffset, widgetEndOffset - widgetOffset);

        return clippingRegion;
    }
	
    /**
     * Returns the document offset of the upper left corner of the source viewer's view port,
     * possibly including partially visible lines.
     *
     * @return the document offset if the upper left corner of the view port
     */
    private int getInclusiveTopIndexStartOffset() 
    {

        if (_textWidget != null && !_textWidget.isDisposed()) {
            int top= JFaceTextUtil.getPartialTopIndex(_sourceViewer);
            try {
                IDocument document= _sourceViewer.getDocument();
                return document.getLineOffset(top);
            } catch (BadLocationException x) {
            }
        }

        return -1;
    }
    
    /**
     * Returns the first invisible document offset of the lower right corner of the source viewer's view port,
     * possibly including partially visible lines.
     *
     * @return the first invisible document offset of the lower right corner of the view port
     */
    private int getExclusiveBottomIndexEndOffset() 
    {

        if (_textWidget != null && !_textWidget.isDisposed()) {
            int bottom= JFaceTextUtil.getPartialBottomIndex(_sourceViewer);
            try {
                IDocument document= _sourceViewer.getDocument();

                if (bottom >= document.getNumberOfLines())
                    bottom= document.getNumberOfLines() - 1;

                return document.getLineOffset(bottom) + document.getLineLength(bottom);
            } catch (BadLocationException x) {
            }
        }

        return -1;
    }
    
    /**
     * Returns the model region that corresponds to the given region in the
     * viewer's text widget.
     *
     * @param offset the offset in the viewer's widget
     * @param length the length in the viewer's widget
     * @return the corresponding document region
     * @since 3.2
     */
    private IRegion getModelRange(int offset, int length) 
    {
        if (offset == Integer.MAX_VALUE)
            return null;

        if (_sourceViewer instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension= (ITextViewerExtension5) _sourceViewer;
            return extension.widgetRange2ModelRange(new Region(offset, length));
        }

        IRegion region= _sourceViewer.getVisibleRegion();
        return new Region(region.getOffset() + offset, length);
    }
    
    private IRegion getWidgetRange(int offset, int length)
    {                
        if (_sourceViewer instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension= (ITextViewerExtension5) _sourceViewer;
            IRegion widgetRange= extension.modelRange2WidgetRange(new Region(offset, length));
            if (widgetRange == null)
                return null;

            try {
                // don't draw if the pair position is really hidden and widgetRange just
                // marks the coverage around it.
                IDocument doc= _sourceViewer.getDocument();
                int startLine= doc.getLineOfOffset(offset);
                int endLine= doc.getLineOfOffset(offset + length);
                if (extension.modelLine2WidgetLine(startLine) == -1 || extension.modelLine2WidgetLine(endLine) == -1)
                    return null;
            } catch (BadLocationException e) {
                return null;
            }

            return widgetRange;

        } else {
            IRegion region= _sourceViewer.getVisibleRegion();
            if (region.getOffset() > offset || region.getOffset() + region.getLength() < offset + length)
                return null;
            offset -= region.getOffset();
            
            return new Region(offset, length);
        }
    }

   
}
