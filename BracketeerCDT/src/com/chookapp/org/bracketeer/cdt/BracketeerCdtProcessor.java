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

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.ICharacterPairMatcher;

import com.chookapp.org.bracketeer.cdt.core.internals.CPairMatcher;
import com.chookapp.org.bracketeer.common.BracketsPair;
import com.chookapp.org.bracketeer.extensionpoint.IBracketeerProcessor;

public class BracketeerCdtProcessor implements IBracketeerProcessor
{
    protected final static char[] BRACKETS = { '{', '}', '(', ')', '[', ']', '<', '>' };
    
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
            return null;
        
        if( region.getLength() < 1 )
            throw new RuntimeException("length is less than 1");

        int anchor = _matcher.getAnchor();
        int targetOffset = (ICharacterPairMatcher.RIGHT == anchor) ? region.getOffset() + 1: region.getOffset() + region.getLength();
        
        return new BracketsPair(offset, targetOffset);
    }

}
