package com.chookapp.org.bracketeer.core;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;

public class TextUtils
{
    public static IRegion getWidgetRange(ISourceViewer sv, int offset, int length)
    {                
        if (sv instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension= (ITextViewerExtension5) sv;
            IRegion widgetRange= extension.modelRange2WidgetRange(new Region(offset, length));
            if (widgetRange == null)
                return null;

            try {
                // don't draw if the pair position is really hidden and widgetRange just
                // marks the coverage around it.
                IDocument doc= sv.getDocument();
                int startLine= doc.getLineOfOffset(offset);
                int endLine= doc.getLineOfOffset(offset + length);
                if (extension.modelLine2WidgetLine(startLine) == -1 || extension.modelLine2WidgetLine(endLine) == -1)
                    return null;
            } catch (BadLocationException e) {
                return null;
            }

            return widgetRange;

        } else {
            IRegion region= sv.getVisibleRegion();
            if (region.getOffset() > offset || region.getOffset() + region.getLength() < offset + length)
                return null;
            offset -= region.getOffset();
            
            return new Region(offset, length);
        }
    }
    
}
