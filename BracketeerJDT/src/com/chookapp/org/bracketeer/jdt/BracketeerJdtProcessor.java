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
package com.chookapp.org.bracketeer.jdt;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.ui.IEditorPart;

import com.chookapp.org.bracketeer.common.BracketsPair;
import com.chookapp.org.bracketeer.common.IBracketeerProcessingContainer;
import com.chookapp.org.bracketeer.common.SingleBracket;
import com.chookapp.org.bracketeer.common.Utils;
import com.chookapp.org.bracketeer.extensionpoint.BracketeerProcessor;
import com.chookapp.org.bracketeer.jdt.core.internal.JavaPairMatcher;

public class BracketeerJdtProcessor extends BracketeerProcessor 
{
	protected final static char[] BRACKETS = { '{', '}', '(', ')', '[', ']', '<', '>' };
	
	/* Lonely brackets is different from BRACKETS because matching an 
     * angular bracket is heuristic. So I don't want to have false positives */
    protected final static String LONELY_BRACKETS = "()[]{}"; //$NON-NLS-1$
	
    String[] ALL_JPARTITIONS = {
    		IJavaPartitions.JAVA_CHARACTER,
    		IJavaPartitions.JAVA_DOC,
    		IJavaPartitions.JAVA_MULTI_LINE_COMMENT,
    		IJavaPartitions.JAVA_SINGLE_LINE_COMMENT,
    		IJavaPartitions.JAVA_STRING
    };
    
	private JavaPairMatcher _matcher;
    private ITypeRoot _typeRoot;
	
	protected BracketeerJdtProcessor(IEditorPart part, IDocument doc) 
	{
		super(doc);
		_matcher = new JavaPairMatcher(BRACKETS);
		_typeRoot = JavaUI.getEditorInputTypeRoot(part.getEditorInput());
	}

	@Override
	protected void processDocument(IDocument doc,
			IBracketeerProcessingContainer container) 
	{
		if(Activator.DEBUG)
            Activator.trace("starting process..."); //$NON-NLS-1$
        
        try
        {
            processBrackets(doc, container);
            processAst(doc, container);
        }
        catch (BadLocationException e)
        {
            _cancelProcessing.set(true);
        }
        
        if(Activator.DEBUG)
            Activator.trace("process ended (" + _cancelProcessing + ")"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void processAst(IDocument doc, IBracketeerProcessingContainer container)
    {
        if( _typeRoot == null )
            return;
        
        ASTParser astp = ASTParser.newParser(AST.JLS3);
        astp.setSource(_typeRoot);
        astp.setResolveBindings(false);
        CompilationUnit cu = (CompilationUnit) astp.createAST(null);
        
        ClosingBracketHintVisitor visitor = new ClosingBracketHintVisitor(container, 
                                                                          doc,
                                                                          _cancelProcessing, 
                                                                          _hintConf);        
        cu.accept(visitor);
    }

    private void processBrackets(IDocument doc,
			IBracketeerProcessingContainer container) throws BadLocationException 
	{
		for(int i = 1; i < doc.getLength(); i++)
        {
            if( _cancelProcessing.get() )
                break;
		    
            BracketsPair pair = getMatchingPair(doc, i);
            if(pair != null)
            {
                if(Activator.DEBUG)
                    Activator.trace("matching pair added: " + pair.toString()); //$NON-NLS-1$
                container.add(pair);
                continue;
            }
            
            SingleBracket single = getLonelyBracket(doc, i);
            if( single != null )
                container.add(single);
        }
	}

	private SingleBracket getLonelyBracket(IDocument doc, int offset) throws BadLocationException 
	{
		final int charOffset = offset - 1;
        char prevChar;
   
        prevChar = doc.getChar(Math.max(charOffset, 0));
        if (LONELY_BRACKETS.indexOf(prevChar) == -1) return null;
        final String partition= TextUtilities.getContentType(doc, IJavaPartitions.JAVA_PARTITIONING, charOffset, false);
        for( String partName : ALL_JPARTITIONS )
        {
            if (partName.equals( partition ))
                return null;
        }
        
        return new SingleBracket(charOffset, Utils.isOpenningBracket(prevChar), prevChar);
	}

	private BracketsPair getMatchingPair(IDocument doc, int offset) throws BadLocationException 
	{
		IRegion region = _matcher.match(doc, offset);
        if( region == null )
            return null;
        
        if( region.getLength() < 1 )
            throw new RuntimeException("length is less than 1");

        boolean isAnchorOpening = (ICharacterPairMatcher.LEFT == _matcher.getAnchor());        
        int targetOffset =  isAnchorOpening ? region.getOffset() + region.getLength() : region.getOffset() + 1;
        
        offset--;
        targetOffset--;
    
        if( isAnchorOpening )
            return new BracketsPair(offset, doc.getChar(offset), 
                                    targetOffset, doc.getChar(targetOffset));
        else
            return new BracketsPair(targetOffset, doc.getChar(targetOffset), 
                                    offset, doc.getChar(offset));
	}

}
