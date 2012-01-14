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
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
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
        try
        {
            if( statement instanceof IASTIfStatement )        
                visitIf((IASTIfStatement) statement);
            
            if( statement instanceof IASTSwitchStatement )
                visitSwitch((IASTSwitchStatement) statement);
            
            if( statement instanceof IASTForStatement )
                visitFor((IASTForStatement) statement);
            
            if( statement instanceof IASTWhileStatement )
                visitWhile((IASTWhileStatement) statement);
        }
        catch(Exception e)
        {
            Activator.log(e);
        }
        return shouldContinue();
    }

    private void visitIf(IASTIfStatement statement)
    {
        /* TODO: specific params: don't show the if hint if there's an "else if" after it (by checking if the elseClause is an instance of ifstatment) */
        
        String hint = "";
        if( statement.getConditionExpression() != null )
        {
            hint = statement.getConditionExpression().getRawSignature();
        }
        else 
        {
            if( (statement instanceof ICPPASTIfStatement) && 
                    ((ICPPASTIfStatement)statement).getConditionDeclaration() != null )
            {
                hint = ((ICPPASTIfStatement)statement).getConditionDeclaration().getRawSignature();
            }
        }
            
        IASTStatement thenClause = statement.getThenClause();
        IASTStatement elseClause = statement.getElseClause();
        
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
            if( !showIfHint && !(elseClause instanceof IASTIfStatement) )
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

        if( elseClause != null && !(elseClause instanceof IASTIfStatement) && 
                (elseClause instanceof IASTCompoundStatement))
        {
            IASTFileLocation location = elseClause.getFileLocation();
            endLoc = location.getNodeOffset()+location.getNodeLength()-1;
            int startLoc = location.getNodeOffset();
            _container.add(new Hint("if", startLoc, endLoc, "else_of_if("+hint+")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    private void visitSwitch(IASTSwitchStatement statement)
    {
        String hint = statement.getControllerExpression().getRawSignature();
        IASTFileLocation location = statement.getBody().getFileLocation();
        int endLoc = location.getNodeOffset()+location.getNodeLength()-1;
        int startLoc = statement.getFileLocation().getNodeOffset();
        _container.add(new Hint("switch", startLoc, endLoc, "switch("+hint+")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }


    private void visitFor(IASTForStatement statement)
    {
        /* TODO: specific params: show also initializer && increment expressions */
        
        IASTStatement body = statement.getBody();
        if( body instanceof IASTCompoundStatement)
        {
            IASTExpression cond = statement.getConditionExpression();
            
            String hint = "";
            if( cond != null )
                hint = cond.getRawSignature();
            IASTFileLocation location = body.getFileLocation();
            int endLoc = location.getNodeOffset()+location.getNodeLength()-1;
            int startLoc = statement.getFileLocation().getNodeOffset();
            _container.add(new Hint("for", startLoc, endLoc, "for("+hint+")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }
    
    private void visitWhile(IASTWhileStatement statement)
    {
        IASTStatement body = statement.getBody();
        if( body instanceof IASTCompoundStatement)
        {
            IASTFileLocation location = body.getFileLocation();
            IASTExpression cond = statement.getCondition();
            
            String hint = "";
            if( cond != null )
                hint = cond.getRawSignature();
            int endLoc = location.getNodeOffset()+location.getNodeLength()-1;
            int startLoc = statement.getFileLocation().getNodeOffset();
            _container.add(new Hint("while", startLoc, endLoc, "while("+hint+")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }
    
    @Override
    public int visit(IASTDeclaration declaration)
    {
        try
        {
            if( declaration instanceof IASTFunctionDefinition )
                visitFunc((IASTFunctionDefinition) declaration);
            
            if( declaration instanceof IASTSimpleDeclaration)
                visitType((IASTSimpleDeclaration) declaration);
        }
        catch (Exception e)
        {
            Activator.log(e);
        }
        return shouldContinue();
    }  
    
    private void visitFunc(IASTFunctionDefinition declaration)
    {
        IASTStatement body = declaration.getBody();
        if(!( body instanceof IASTCompoundStatement) )
            return;
        
        IASTFileLocation location = body.getFileLocation();
        int endLoc = location.getNodeOffset()+location.getNodeLength()-1;

        IASTFunctionDeclarator declerator = declaration.getDeclarator();
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
    
    private void visitType(IASTSimpleDeclaration declaration)
    {
        /* TODO: specific params: include type('class' / 'struct') */
        
        IASTDeclSpecifier spec = declaration.getDeclSpecifier();
        if( spec instanceof IASTCompositeTypeSpecifier )
        {
            String hint = ((IASTCompositeTypeSpecifier)spec).getName().getRawSignature();
            if( hint.isEmpty() )
                return;
            
            IASTFileLocation location = declaration.getFileLocation();
            int endLoc = location.getNodeOffset()+location.getNodeLength()-1;
            int startLoc = location.getNodeOffset();
            _container.add(new Hint("type", startLoc, endLoc, hint)); //$NON-NLS-1$
        }        
    }
    
    private int shouldContinue()
    {
        if( _cancelProcessing )
            return PROCESS_ABORT;
        else
            return PROCESS_CONTINUE;
    }
    
}
