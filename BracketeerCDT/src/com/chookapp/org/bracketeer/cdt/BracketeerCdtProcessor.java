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
    }
   
    
    private BracketsPair getMatchingPair(IDocument doc, int offset)
    {
        IRegion region = _matcher.match(doc, offset);
        if( region == null )
            return null;
        
        if( region.getLength() < 1 )
            throw new RuntimeException(Messages.BracketeerCdtProcessor_ErrLength);

        boolean isAnchorOpening = (ICharacterPairMatcher.LEFT == _matcher.getAnchor());        
        int targetOffset =  isAnchorOpening ? region.getOffset() + region.getLength() : region.getOffset() + 1;
        
        offset--;
        targetOffset--;
        
        try
        {
            if( isAnchorOpening )
                return new BracketsPair(offset, doc.getChar(offset), 
                                        targetOffset, doc.getChar(targetOffset));
            else
                return new BracketsPair(targetOffset, doc.getChar(targetOffset), 
                                        offset, doc.getChar(offset));
        }
        catch (BadLocationException e)
        {
            Activator.log(e);
        }
        return null;
    }

    private SingleBracket getLonelyBracket(IDocument doc, int offset)
    {
        final int charOffset = offset - 1;
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
            
            return new SingleBracket(charOffset, Utils.isOpenningBracket(prevChar), prevChar);
        }
        catch (BadLocationException e)
        {
        }
        return null;
    }


    @Override
    protected void processDocument(IDocument doc,
                                   IBracketeerProcessingContainer container)
    {
        if(Activator.DEBUG)
            Activator.trace("starting process..."); //$NON-NLS-1$
        
        processBrackets(doc, container);
        processAst(container);
        
        if(Activator.DEBUG)
            Activator.trace("process ended (" + _cancelProcessing + ")"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void processBrackets(IDocument doc,
                                 IBracketeerProcessingContainer container)
    {
        for(int i = 1; i < doc.getLength(); i++)
        {            
            if( _cancelProcessing )
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
    
//    @SuppressWarnings("restriction")
    private void processAst(IBracketeerProcessingContainer container)
    {
        if( _celem == null )
            return;
        
//        AstRunner runner = new AstRunner(container);
//        ASTProvider provider = CUIPlugin.getDefault().getASTProvider();
//        
//        if( provider.runOnAST(_celem, ASTProvider.WAIT_ACTIVE_ONLY, null, runner) == Status.OK_STATUS)
//            return;
        
        try
        {
            ITranslationUnit tu = (ITranslationUnit) _celem;
            IASTTranslationUnit ast;
            ast = tu.getAST(null, ITranslationUnit.AST_SKIP_ALL_HEADERS |
                                  ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT |
                                  ITranslationUnit.AST_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS |
                                  ITranslationUnit.AST_PARSE_INACTIVE_CODE);
            if( ast == null )
                return;

            ClosingBracketHintVisitor visitor = new ClosingBracketHintVisitor(container, 
                                                                              _cancelProcessing,
                                                                              _hintConf);
            
            ast.accept(visitor);
            //runner.runOnAST(null, ast);
            
            IASTPreprocessorStatement[] stmts = ast.getAllPreprocessorStatements();
            PreprocessorVisitor preVisotor = new PreprocessorVisitor(container, 
                                                                     _cancelProcessing,
                                                                     _hintConf);
            preVisotor.visit(stmts);
        }
        catch (CoreException e)
        {
            Activator.log(e);
            return;            
        }
    }

  
}
