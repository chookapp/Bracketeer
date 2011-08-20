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
 *******************************************************************************/

package com.chookapp.org.bracketeer.cdt;

import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ICharacterPairMatcher;

import com.chookapp.org.bracketeer.cdt.core.internals.CPairMatcher;
import com.chookapp.org.bracketeer.common.BracketsPair;
import com.chookapp.org.bracketeer.extensionpoint.IBracketeerProcessor;
import com.chookapp.org.bracketeer.helpers.Utils;

public class BracketeerCdtProcessor implements IBracketeerProcessor
{
    protected final static char[] BRACKETS = { '{', '}', '(', ')', '[', ']', '<', '>' };
    
    /* Lonely brackets is different from BRACKETS because matching an 
     * angular bracket is heuristic. So I don't want to have false positives */
    protected final static String LONELY_BRACKETS = "()[]{}";
    
    private CPairMatcher _matcher;
    private ITextViewer _textViewer;

    public BracketeerCdtProcessor(ITextViewer textViewer)
    {
        _matcher = new CPairMatcher(BRACKETS);
        _textViewer = textViewer;
    }
    
    @Override
    public BracketsPair getMatchingPair(int offset)
    {
        IRegion region = _matcher.match(_textViewer.getDocument(), offset);
        if( region == null )
            return matchLonelyBracket(offset);
        
        if( region.getLength() < 1 )
            throw new RuntimeException("length is less than 1");

        boolean isAnchorOpening = (ICharacterPairMatcher.LEFT == _matcher.getAnchor());        
        int targetOffset =  isAnchorOpening ? region.getOffset() + region.getLength() : region.getOffset() + 1;
        
        BracketsPair pair = new BracketsPair();
        
        pair.addBracket(offset, isAnchorOpening);
        pair.addBracket(targetOffset, !isAnchorOpening);
        
        return pair;
    }

    private BracketsPair matchLonelyBracket(int offset)
    {
        final int charOffset = offset - 1;
        IDocument doc = _textViewer.getDocument();
        char prevChar;
        try
        {
            prevChar = doc.getChar(Math.max(charOffset, 0));
            if (LONELY_BRACKETS.indexOf(prevChar) == -1) return null;
            final String partition= TextUtilities.getContentType(doc, ICPartitions.C_PARTITIONING, charOffset, false);
            for( String partName : ICPartitions.ALL_CPARTITIONS )
            {
                if (partName.equals( partition ))
                    return null;
            }
            
            BracketsPair pair = new BracketsPair();
            pair.addBracket(offset, Utils.isOpenningBracket(prevChar));
            return pair;
        }
        catch (BadLocationException e)
        {
        }
        return null;
    }

}
