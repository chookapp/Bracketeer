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
package com.chookapp.org.bracketeer.cdt;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;

import com.chookapp.org.bracketeer.common.Hint;
import com.chookapp.org.bracketeer.common.IBracketeerProcessingContainer;
import com.chookapp.org.bracketeer.common.IHintConfiguration;

public class ClosingBracketHintVisitor extends ASTVisitor
{
    Boolean _cancelProcessing;
    IBracketeerProcessingContainer _container;
    IHintConfiguration _hintConf;
    
    public ClosingBracketHintVisitor(IBracketeerProcessingContainer container,
                                     Boolean cancelProcessing, 
                                     IHintConfiguration hintConf)
    {
        _cancelProcessing = cancelProcessing;
        _container = container;
        _hintConf = hintConf;
        
        shouldVisitStatements = true;
        shouldVisitDeclarations = true;
    }

    @Override
    public int visit(IASTStatement statement) 
    {
        if( statement instanceof ICPPASTIfStatement )
        {
            /* TODO: specific params: don't show the if hint if there's an "else if" after it (by checking if the elseClause is an instance of ifstatment) */
            
            String hint = ((ICPPASTIfStatement)statement).getConditionExpression().getRawSignature();
            IASTStatement thenClause = ((ICPPASTIfStatement)statement).getThenClause();
            IASTStatement elseClause = ((ICPPASTIfStatement)statement).getElseClause();
            
            boolean showIfHint = (elseClause == null);
            int endLoc = -1;
            if( !showIfHint )
            {
                if (elseClause.getFileLocation().getStartingLineNumber() != 
                        thenClause.getFileLocation().getEndingLineNumber() )
                {
                    showIfHint = true;
                }
                
                // if the else looks like this "} else {", then show the hint on the "{"
                if( !showIfHint && !(elseClause instanceof ICPPASTIfStatement) )
                {
                    endLoc = elseClause.getFileLocation().getNodeOffset();
                    showIfHint = true;
                }
            }
            
            if( showIfHint && !(thenClause instanceof IASTCompoundStatement) )
                showIfHint = false;
            
            if( showIfHint )
            {
                IASTFileLocation location = thenClause.getFileLocation();
                if( endLoc == -1 )
                    endLoc = location.getNodeOffset()+location.getNodeLength()-1;
                int startLoc = statement.getFileLocation().getNodeOffset();
                _container.add(new Hint("if", startLoc, endLoc, "if("+hint+")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } 

            if( elseClause != null && !(elseClause instanceof ICPPASTIfStatement) && 
                    (elseClause instanceof IASTCompoundStatement))
            {
                IASTFileLocation location = elseClause.getFileLocation();
                endLoc = location.getNodeOffset()+location.getNodeLength()-1;
                int startLoc = location.getNodeOffset();
                _container.add(new Hint("if", startLoc, endLoc, "else_of_if("+hint+")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
        
        if( statement instanceof IASTSwitchStatement )
        {
            String hint = ((IASTSwitchStatement) statement).getControllerExpression().getRawSignature();
            IASTFileLocation location = ((IASTSwitchStatement) statement).getBody().getFileLocation();
            int endLoc = location.getNodeOffset()+location.getNodeLength()-1;
            int startLoc = statement.getFileLocation().getNodeOffset();
            _container.add(new Hint("switch", startLoc, endLoc, "switch("+hint+")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        
        if( statement instanceof IASTForStatement )
        {            
            /* TODO: specific params: show also initializer && increment expressions */
            
            IASTStatement body = ((IASTForStatement)statement).getBody();
            if( body instanceof IASTCompoundStatement)
            {
                String hint = ((IASTForStatement)statement).getConditionExpression().getRawSignature();
                IASTFileLocation location = body.getFileLocation();
                int endLoc = location.getNodeOffset()+location.getNodeLength()-1;
                int startLoc = statement.getFileLocation().getNodeOffset();
                _container.add(new Hint("for", startLoc, endLoc, "for("+hint+")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
        
        if( statement instanceof IASTWhileStatement )
        {
            IASTStatement body = ((IASTWhileStatement)statement).getBody();
            if( body instanceof IASTCompoundStatement)
            {
                IASTFileLocation location = body.getFileLocation();
                String hint = ((IASTWhileStatement)statement).getCondition().getRawSignature();
                int endLoc = location.getNodeOffset()+location.getNodeLength()-1;
                int startLoc = statement.getFileLocation().getNodeOffset();
                _container.add(new Hint("while", startLoc, endLoc, "while("+hint+")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
        
        return shouldContinue();
    }

    @Override
    public int visit(IASTDeclaration declaration)
    {
        if( declaration instanceof ICPPASTFunctionDefinition )
        {            
            IASTStatement body = ((ICPPASTFunctionDefinition) declaration).getBody();
            if(!( body instanceof IASTCompoundStatement) )
                return shouldContinue();
            
            IASTFileLocation location = body.getFileLocation();
            int endLoc = location.getNodeOffset()+location.getNodeLength()-1;

            IASTFunctionDeclarator declerator = ((ICPPASTFunctionDefinition) declaration).getDeclarator();
            int startLoc = declerator.getFileLocation().getNodeOffset();
            
            StringBuffer hint = new StringBuffer();
            hint.append(declerator.getName().getRawSignature());            
            /* TODO: specific params: exclude function parameters (show only the name) */
            hint.append('(');
            IASTNode[] decChildren = declerator.getChildren();
            boolean firstParam = true;
            for (int i = 0; i < decChildren.length; i++)
            {
                IASTNode node = decChildren[i];
                if( node instanceof IASTParameterDeclaration)
                {
                    IASTParameterDeclaration param = (IASTParameterDeclaration) node;
                    if( firstParam )
                        firstParam = false;
                    else
                        hint.append(',');
                    hint.append(param.getDeclarator().getName());                    
                }
            }
            hint.append(')');
            
            _container.add(new Hint("function", startLoc, endLoc, hint.toString())); //$NON-NLS-1$
        }
        
        if( declaration instanceof IASTSimpleDeclaration)
        {
            /* TODO: specific params: include type('class' / 'struct') */
            
            IASTDeclSpecifier spec = ((IASTSimpleDeclaration)declaration).getDeclSpecifier();
            if( spec instanceof ICPPASTCompositeTypeSpecifier )
            {
                String hint = ((ICPPASTCompositeTypeSpecifier)spec).getName().getRawSignature();
                if( hint.isEmpty() )
                    return shouldContinue();
                
                IASTFileLocation location = declaration.getFileLocation();
                int endLoc = location.getNodeOffset()+location.getNodeLength()-1;
                int startLoc = location.getNodeOffset();
                _container.add(new Hint("type", startLoc, endLoc, hint)); //$NON-NLS-1$
            }
        }
        
        return shouldContinue();
    }  
    
    private int shouldContinue()
    {
        if( _cancelProcessing )
            return PROCESS_ABORT;
        else
            return PROCESS_CONTINUE;
    }
    
}
