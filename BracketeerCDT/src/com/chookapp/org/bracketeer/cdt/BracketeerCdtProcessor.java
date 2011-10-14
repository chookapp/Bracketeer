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

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
//import org.eclipse.cdt.internal.core.model.ASTCache;
//import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
    protected final static String LONELY_BRACKETS = "()[]{}";
    
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
    
    public BracketeerCdtProcessor(IEditorPart part) 
    {
        super(part);
        
        _matcher = new CPairMatcher(BRACKETS);
        _celem = null;
    }
   
    
    private BracketsPair getMatchingPair(IDocument doc, int offset)
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
            Activator.trace("starting process...");
        
        processBrackets(doc, container);
        processAst(container);
        
        if(Activator.DEBUG)
            Activator.trace("process ended (" + _cancelProcessing + ")");
    }

    private void processBrackets(IDocument doc,
                                 IBracketeerProcessingContainer container)
    {
        for(int i = 1; i < doc.getLength(); i++)
        {
            BracketsPair pair = getMatchingPair(doc, i);
            if(pair != null)
            {
                if(Activator.DEBUG)
                    Activator.trace("matching pair added: " + pair.toString());
                container.add(pair);
                continue;
            }
            
            SingleBracket single = getLonelyBracket(doc, i);
            if( single != null )
                container.add(single);
            
            if( _cancelProcessing )
                break;
        }
    }
    
    @SuppressWarnings("restriction")
    private void processAst(IBracketeerProcessingContainer container)
    {
        makeCelem();
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
            ClosingBracketHintVisitor visitor = new ClosingBracketHintVisitor(container, 
                                                                            _cancelProcessing,
                                                                            _hintConf);        
            ast.accept(visitor);
            //runner.runOnAST(null, ast);
        }
        catch (CoreException e)
        {
            Activator.log(e);
            return;            
        }
    }


    private void makeCelem()
    {        
        if( _celem != null )
            return;
        
        
//        
//        IResource resource = (IResource) _part.getEditorInput().getAdapter(IResource.class);
//        if( resource == null )
//            return;
//        _celem = CoreModel.getDefault().create(resource);
//        if( _celem == null )
//            return;
//        if (!(_celem instanceof ITranslationUnit))
//            _celem = null;
        
        _celem = CDTUITools.getEditorInputCElement(_part.getEditorInput());
    }
}
