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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElseStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
//import org.eclipse.cdt.internal.core.model.ASTCache;
//import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.ui.IEditorPart;

import com.chookapp.org.bracketeer.cdt.core.internals.CPairMatcher;
import com.chookapp.org.bracketeer.common.BracketsPair;
import com.chookapp.org.bracketeer.common.IBracketeerProcessingContainer;
import com.chookapp.org.bracketeer.common.SingleBracket;
import com.chookapp.org.bracketeer.common.Utils;
import com.chookapp.org.bracketeer.extensionpoint.BracketeerProcessor;

public class BracketeerCdtProcessor extends BracketeerProcessor
{
    protected final static char[] BRACKETS = { '{', '}', '(', ')', '[', ']', '<', '>' };
    
    /* Lonely brackets is different from BRACKETS because matching an 
     * angular bracket is heuristic. So I don't want to have false positives */
    protected final static String LONELY_BRACKETS = "()[]{}"; //$NON-NLS-1$
    
    private CPairMatcher _matcher;

    private ICElement _celem;
    private IDocument _doc;
    
    private IASTTranslationUnit _ast;

//    @SuppressWarnings("restriction")
//    class AstRunner implements ASTCache.ASTRunnable
//    {
//        IBracketeerProcessingContainer _container;
//        
//        public AstRunner(IBracketeerProcessingContainer container)
//        {
//            _container = container;
//        }
//        
//        @Override
//        public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException
//        {            
//            ClosingBracketHintVisitor visitor = new ClosingBracketHintVisitor(_container, 
//                                                                              _cancelProcessing,
//                                                                              _hintConf);        
//            ast.accept(visitor);
//            return Status.OK_STATUS;
//        }
//        
//    }
    
    public BracketeerCdtProcessor(IEditorPart part, IDocument doc) 
    {
        super(doc);
        
        _celem = CDTUITools.getEditorInputCElement(part.getEditorInput());
        _matcher = new CPairMatcher(BRACKETS);
        _doc = doc;
    }   
    
    private BracketsPair getMatchingPair(int offset) throws BadLocationException
    {
        IRegion region = _matcher.match(_doc, offset);
        if( region == null )
            return null;
        
        if( region.getLength() < 1 )
            throw new RuntimeException(Messages.BracketeerCdtProcessor_ErrLength);

        boolean isAnchorOpening = (ICharacterPairMatcher.LEFT == _matcher.getAnchor());        
        int targetOffset =  isAnchorOpening ? (region.getOffset() + region.getLength()) : (region.getOffset() + 1);
        
        offset--;
        targetOffset--;
        

        if( isAnchorOpening )
            return new BracketsPair(offset, _doc.getChar(offset), 
                                    targetOffset, _doc.getChar(targetOffset));
        else
            return new BracketsPair(targetOffset, _doc.getChar(targetOffset), 
                                    offset, _doc.getChar(offset));
   
    }

    private SingleBracket getLonelyBracket(int offset, List<Position> inactiveCode) throws BadLocationException
    {
        final int charOffset = offset - 1;
        char prevChar;
   
        prevChar = _doc.getChar(Math.max(charOffset, 0));
        if (LONELY_BRACKETS.indexOf(prevChar) == -1) return null;
        final String partition= TextUtilities.getContentType(_doc, ICPartitions.C_PARTITIONING, charOffset, false);
        for( String partName : ICPartitions.ALL_CPARTITIONS )
        {
            if (partName.equals( partition ))
                return null;
            for (Position pos : inactiveCode)
            {
                if(pos.includes(offset))
                    return null;
            }
        }
        
        return new SingleBracket(charOffset, Utils.isOpenningBracket(prevChar), prevChar);
    }


    @Override
    protected void processDocument(IDocument doc,
                                   IBracketeerProcessingContainer container)
    {
        if(Activator.DEBUG)
            Activator.trace("starting process..."); //$NON-NLS-1$
        
        try
        {
            _doc = doc;
            updateAst();
            processBrackets(container);
            processAst(container);
        }
        catch (BadLocationException e)
        {
            _cancelProcessing.set(true);
        }
        
        if(Activator.DEBUG)
            Activator.trace("process ended (" + _cancelProcessing + ")"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void processBrackets(IBracketeerProcessingContainer container) throws BadLocationException
    {
        List<Position> inactiveCode = collectInactiveCodePositions(_ast);
        _matcher.updateInactiveCodePositions(inactiveCode);
        for(int i = 1; i < _doc.getLength()+1; i++)
        {            
            if( _cancelProcessing.get() )
                break;
            
            BracketsPair pair = getMatchingPair(i);
            if(pair != null)
            {
                if(Activator.DEBUG)
                    Activator.trace("matching pair added: " + pair.toString()); //$NON-NLS-1$
                container.add(pair);
                continue;
            }
            
            SingleBracket single = getLonelyBracket(i, inactiveCode);
            if( single != null )
                container.add(single);
        }
    }
    
//    @SuppressWarnings("restriction")
    private void processAst(IBracketeerProcessingContainer container) throws BadLocationException
    {
        if(_ast == null )
            return;

//        AstRunner runner = new AstRunner(container);
//        ASTProvider provider = CUIPlugin.getDefault().getASTProvider();
//        
//        if( provider.runOnAST(_celem, ASTProvider.WAIT_ACTIVE_ONLY, null, runner) == Status.OK_STATUS)
//            return;

        ClosingBracketHintVisitor visitor = new ClosingBracketHintVisitor(container, 
                                                                          _cancelProcessing,
                                                                          _hintConf);

        _ast.accept(visitor);
        //runner.runOnAST(null, ast);

        IASTPreprocessorStatement[] stmts = _ast.getAllPreprocessorStatements();
        PreprocessorVisitor preVisotor = new PreprocessorVisitor(container, 
                                                                 _cancelProcessing,
                                                                 _hintConf);
        preVisotor.visit(stmts);
    }

    
    private void updateAst()
    {
        try
        {
            _ast = null;
            if( _celem == null )
                return;
            
            ITranslationUnit tu = (ITranslationUnit) _celem;
            IASTTranslationUnit ast;
            ast = tu.getAST(null, ITranslationUnit.AST_SKIP_ALL_HEADERS |
                            ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT |
                            ITranslationUnit.AST_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS |
                            ITranslationUnit.AST_PARSE_INACTIVE_CODE);

            _ast = ast;
        }
        catch (CoreException e)
        {
            Activator.log(e);
        }
    }
    
    /**
     * copied from org.eclipse.cdt.internal.ui.editor.InactiveCodeHighlighting.
     * 
     * Collect source positions of preprocessor-hidden branches 
     * in the given translation unit.
     * 
     * @param translationUnit  the {@link IASTTranslationUnit}, may be <code>null</code>
     * @return a {@link List} of {@link IRegion}s
     */
    private List<Position> collectInactiveCodePositions(IASTTranslationUnit translationUnit) {
        if (translationUnit == null) {
            return Collections.emptyList();
        }
        String fileName = translationUnit.getFilePath();
        if (fileName == null) {
            return Collections.emptyList();
        }
        List<Position> positions = new ArrayList<Position>();
        int inactiveCodeStart = -1;
        boolean inInactiveCode = false;
        Stack<Boolean> inactiveCodeStack = new Stack<Boolean>();

        IASTPreprocessorStatement[] preprocStmts = translationUnit.getAllPreprocessorStatements();

        for (IASTPreprocessorStatement statement : preprocStmts) {
            IASTFileLocation floc= statement.getFileLocation();
            if (floc == null || !fileName.equals(floc.getFileName())) {
                // preprocessor directive is from a different file
                continue;
            }
            if (statement instanceof IASTPreprocessorIfStatement) {
                IASTPreprocessorIfStatement ifStmt = (IASTPreprocessorIfStatement)statement;
                inactiveCodeStack.push(Boolean.valueOf(inInactiveCode));
                if (!ifStmt.taken()) {
                    if (!inInactiveCode) {
                        inactiveCodeStart = floc.getNodeOffset();
                        inInactiveCode = true;
                    }
                }
            } else if (statement instanceof IASTPreprocessorIfdefStatement) {
                IASTPreprocessorIfdefStatement ifdefStmt = (IASTPreprocessorIfdefStatement)statement;
                inactiveCodeStack.push(Boolean.valueOf(inInactiveCode));
                if (!ifdefStmt.taken()) {
                    if (!inInactiveCode) {
                        inactiveCodeStart = floc.getNodeOffset();
                        inInactiveCode = true;
                    }
                }
            } else if (statement instanceof IASTPreprocessorIfndefStatement) {
                IASTPreprocessorIfndefStatement ifndefStmt = (IASTPreprocessorIfndefStatement)statement;
                inactiveCodeStack.push(Boolean.valueOf(inInactiveCode));
                if (!ifndefStmt.taken()) {
                    if (!inInactiveCode) {
                        inactiveCodeStart = floc.getNodeOffset();
                        inInactiveCode = true;
                    }
                }
            } else if (statement instanceof IASTPreprocessorElseStatement) {
                IASTPreprocessorElseStatement elseStmt = (IASTPreprocessorElseStatement)statement;
                if (!elseStmt.taken() && !inInactiveCode) {
                    inactiveCodeStart = floc.getNodeOffset();
                    inInactiveCode = true;
                } else if (elseStmt.taken() && inInactiveCode) {
                    int inactiveCodeEnd = floc.getNodeOffset();
                    positions.add(createInactiveCodePosition(inactiveCodeStart, inactiveCodeEnd, false));
                    inInactiveCode = false;
                }
            } else if (statement instanceof IASTPreprocessorElifStatement) {
                IASTPreprocessorElifStatement elifStmt = (IASTPreprocessorElifStatement)statement;
                if (!elifStmt.taken() && !inInactiveCode) {
                    inactiveCodeStart = floc.getNodeOffset();
                    inInactiveCode = true;
                } else if (elifStmt.taken() && inInactiveCode) {
                    int inactiveCodeEnd = floc.getNodeOffset();
                    positions.add(createInactiveCodePosition(inactiveCodeStart, inactiveCodeEnd, false));
                    inInactiveCode = false;
                }
            } else if (statement instanceof IASTPreprocessorEndifStatement) {
                try {
                    boolean wasInInactiveCode = inactiveCodeStack.pop().booleanValue();
                    if (inInactiveCode && !wasInInactiveCode) {
                        int inactiveCodeEnd = floc.getNodeOffset() + floc.getNodeLength();
                        positions.add(createInactiveCodePosition(inactiveCodeStart, inactiveCodeEnd, true));
                    }
                    inInactiveCode = wasInInactiveCode;
                }
                catch( EmptyStackException e) {}
            }
        }
        if (inInactiveCode) {
            // handle unterminated #if - http://bugs.eclipse.org/255018
            int inactiveCodeEnd = _doc.getLength();
            positions.add(createInactiveCodePosition(inactiveCodeStart, inactiveCodeEnd, true));
        }
        return positions;
    }

    /**
     * Create a highlight position aligned to start at a line offset. The region's start is
     * decreased to the line offset, and the end offset decreased to the line start if
     * <code>inclusive</code> is <code>false</code>. 
     * 
     * @param startOffset  the start offset of the region to align
     * @param endOffset  the (exclusive) end offset of the region to align
     * @param inclusive whether  the last line should be included or not
     * @param key  the highlight key
     * @return a position aligned for background highlighting
     */
    private Position createInactiveCodePosition(int startOffset, int endOffset, boolean inclusive) 
    {
        final IDocument document= _doc;
        try {
            if (document != null) {
                int start= document.getLineOfOffset(startOffset);
                int end= document.getLineOfOffset(endOffset);
                startOffset= document.getLineOffset(start);
                if (!inclusive) {
                    endOffset= document.getLineOffset(end);
                }
            }
        } catch (BadLocationException x) {
            // concurrent modification?
        }
        return new Position(startOffset, endOffset - startOffset);
    }
    
  
}
