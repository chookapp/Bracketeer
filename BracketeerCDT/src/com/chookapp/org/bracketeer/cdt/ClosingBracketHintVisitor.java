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

import java.util.EmptyStackException;
import java.util.Stack;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.jface.text.BadLocationException;

import com.chookapp.org.bracketeer.common.BracketsPair;
import com.chookapp.org.bracketeer.common.Hint;
import com.chookapp.org.bracketeer.common.IBracketeerProcessingContainer;
import com.chookapp.org.bracketeer.common.IHintConfiguration;

public class ClosingBracketHintVisitor extends ASTVisitor
{
    
    class ScopeInfo
    {
        public String _str;
        public int _offset;
        public IASTStatement _statement;
        
        public ScopeInfo(String str, int offset, IASTStatement statement)
        {
            _str = str;
            _offset = offset;
            _statement = statement;
        }        
    }

    public class ScopeTraceException extends Exception
    {   
        private static final long serialVersionUID = 6297837237586982280L;
          
        public ScopeTraceException(String message)
        {
            super(message);
        }
    }

    
    Boolean _cancelProcessing;
    IBracketeerProcessingContainer _container;
    IHintConfiguration _hintConf;
    Stack<ScopeInfo> _scopeStack;
    
    public ClosingBracketHintVisitor(IBracketeerProcessingContainer container,
                                     Boolean cancelProcessing, 
                                     IHintConfiguration hintConf)
    {
        _cancelProcessing = cancelProcessing;
        _container = container;
        _hintConf = hintConf;
        
        shouldVisitStatements = true;
        shouldVisitDeclarations = true;
        shouldVisitExpressions = true; // not really visiting expressions, see bug 370637.
        
        _scopeStack = new Stack<ClosingBracketHintVisitor.ScopeInfo>();
    }
    
    @Override
    public int leave(IASTStatement statement)
    {
        try
        {
            if( (statement instanceof IASTSwitchStatement) || 
                    (statement instanceof IASTForStatement) ||
                    (statement instanceof IASTWhileStatement) ||
                    (statement instanceof IASTDoStatement) )
            {
                ScopeInfo scope;
                if( statement instanceof IASTSwitchStatement )
                {
                    scope = _scopeStack.peek();
                    if((scope._statement instanceof IASTCaseStatement) || 
                            (scope._statement instanceof IASTDefaultStatement))
                    {
                        _scopeStack.pop();
                    }
                }

                scope = _scopeStack.pop();
                if(!scope._statement.getClass().equals(statement.getClass()))
                {
                    if(Activator.DEBUG)
                    {
                        Activator.log("Lost track of scope. Expected:" + scope._statement + 
                                      " but was:" + statement);
                    }
                }
            }
        }       
        catch(EmptyStackException e)
        {
            if(Activator.DEBUG)
                Activator.log(e);
        }

        return super.leave(statement);
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
            
            if( statement instanceof IASTDoStatement )
                visitDo((IASTDoStatement) statement);
            
            if( statement instanceof IASTCaseStatement || statement instanceof IASTDefaultStatement )
                visitCase(statement);
            
            if( statement instanceof IASTBreakStatement || statement instanceof IASTContinueStatement )
                visitBreak(statement);
            
        }
        catch(BadLocationException e)
        {
            _cancelProcessing = true;
        }
        catch(Exception e)
        {
            if(!(e instanceof ScopeTraceException || e instanceof EmptyStackException))
                Activator.log(e);
            else if(Activator.DEBUG)
                Activator.log(e);
        }
        return shouldContinue();
    }

    private void visitBreak(IASTStatement statement) throws ScopeTraceException, BadLocationException
    {
        if(_scopeStack.isEmpty())
            throw new ScopeTraceException("break without scope: " + statement);
        
        ScopeInfo scope = _scopeStack.peek();
        
        String hintType;
        if( scope._statement instanceof IASTForStatement )
            hintType = "break-for";
        else if( scope._statement instanceof IASTWhileStatement )
            hintType = "break-while";
        else if( scope._statement instanceof IASTDoStatement )
            hintType = "break-do";
        else if( scope._statement instanceof IASTCaseStatement || scope._statement instanceof IASTDefaultStatement )
            hintType = "break-case";
        else
            throw new ScopeTraceException("Unexpect scope ("+scope._statement+") on break/continue:" + statement);
        
        int endLoc = statement.getFileLocation().getNodeOffset() + statement.getFileLocation().getNodeLength() - 1; 
        _container.add(new Hint(hintType, scope._offset, endLoc, scope._str));
        
    }

    private void visitCase(IASTStatement statement) throws ScopeTraceException
    {
        /* TODO: specific params: don't show the switch part (only the case argument) */
        
        ScopeInfo scope = _scopeStack.peek();
        if( !(scope._statement instanceof IASTSwitchStatement) )
        {
            if(!((scope._statement instanceof IASTCaseStatement) ||
                 (scope._statement instanceof IASTDefaultStatement)) )
            {
                throw new ScopeTraceException("Lost track of stack (in case), found:" + scope._statement);
            }
            
            _scopeStack.pop();
            scope = _scopeStack.peek();
        }
        
        if( !(scope._statement instanceof IASTSwitchStatement) )
        {
            throw new ScopeTraceException("Lost track of stack (in case2), found:" + scope._statement);
        }
        
        String hint = "";
        if(statement instanceof IASTCaseStatement)
        {
            IASTExpression cond = ((IASTCaseStatement)statement).getExpression();
            if( cond != null )
                hint = cond.getRawSignature();
            hint = "case: " + hint;
        }
        else // default
        {
            hint = "default";
        }
        
        int startLoc = statement.getFileLocation().getNodeOffset();
        _scopeStack.push(new ScopeInfo(scope._str + " - " + hint, startLoc, statement)); 
    }

    private void visitDo(IASTDoStatement statement)
    {
        IASTExpression cond = statement.getCondition();
        String hint = "";
        if( cond != null )
            hint = cond.getRawSignature();
        hint = "do_while( " + hint + " )";
        int startLoc = statement.getFileLocation().getNodeOffset();
        _scopeStack.push(new ScopeInfo(hint, startLoc, statement));
    }

    private void visitIf(IASTIfStatement statement) throws BadLocationException
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
            _container.add(new Hint("if", startLoc, endLoc, "if( "+hint+" )")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } 

        if( elseClause != null && !(elseClause instanceof IASTIfStatement) && 
                (elseClause instanceof IASTCompoundStatement))
        {
            IASTFileLocation location = elseClause.getFileLocation();
            endLoc = location.getNodeOffset()+location.getNodeLength()-1;
            int startLoc = location.getNodeOffset();
            _container.add(new Hint("if", startLoc, endLoc, "else_of_if( "+hint+" )")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    private void visitSwitch(IASTSwitchStatement statement) throws BadLocationException
    {        
        String hint = statement.getControllerExpression().getRawSignature();
        IASTFileLocation location = statement.getBody().getFileLocation();
        int endLoc = location.getNodeOffset()+location.getNodeLength()-1;
        int startLoc = statement.getFileLocation().getNodeOffset();
        hint = "switch( "+hint+" )"; //$NON-NLS-1$ //$NON-NLS-2$
        _container.add(new Hint("switch", startLoc, endLoc, hint)); //$NON-NLS-1$ 
        _scopeStack.push(new ScopeInfo(hint, startLoc, statement));
    }


    private void visitFor(IASTForStatement statement) throws BadLocationException
    {
        /* TODO: specific params: show also initializer && increment expressions */

        IASTExpression cond = statement.getConditionExpression();
        String hint = "";
        if( cond != null )
            hint = cond.getRawSignature();
        hint = "for( "+hint+" )"; //$NON-NLS-1$ //$NON-NLS-2$
        int startLoc = statement.getFileLocation().getNodeOffset();
        _scopeStack.push(new ScopeInfo(hint, startLoc, statement));
        
        IASTStatement body = statement.getBody();
        if( body instanceof IASTCompoundStatement)
        {
            IASTFileLocation location = body.getFileLocation();
            int endLoc = location.getNodeOffset()+location.getNodeLength()-1;            
            _container.add(new Hint("for", startLoc, endLoc, hint)); //$NON-NLS-1$ 
        }
    }
    
    private void visitWhile(IASTWhileStatement statement) throws BadLocationException
    {
        IASTExpression cond = statement.getCondition();        
        String hint = "";
        if( cond != null )
            hint = cond.getRawSignature();
        hint = "while( "+hint+" )"; //$NON-NLS-1$ //$NON-NLS-2$
        int startLoc = statement.getFileLocation().getNodeOffset();
        _scopeStack.push(new ScopeInfo(hint, startLoc, statement));
        
        IASTStatement body = statement.getBody();
        if( body instanceof IASTCompoundStatement)
        {
            IASTFileLocation location = body.getFileLocation();
       
            int endLoc = location.getNodeOffset()+location.getNodeLength()-1;
            _container.add(new Hint("while", startLoc, endLoc, hint)); //$NON-NLS-1$ 
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
    
    private void visitFunc(IASTFunctionDefinition declaration) throws BadLocationException
    {
        IASTStatement body = declaration.getBody();
        if(!( body instanceof IASTCompoundStatement) )
            return;
        
        // starting a function empties the stack... (which should already be empty on good flow)
        _scopeStack.clear();
        
        IASTFileLocation location = body.getFileLocation();
        int endLoc = location.getNodeOffset()+location.getNodeLength()-1;

        IASTFunctionDeclarator declerator = declaration.getDeclarator();
        int startLoc = declerator.getFileLocation().getNodeOffset();
        
        StringBuffer hint = new StringBuffer();
        hint.append(declerator.getName().getRawSignature());            
        /* TODO: specific params: exclude function parameters (show only the name) */
        hint.append("( ");
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
                    hint.append(", ");
                hint.append(param.getDeclarator().getName());                    
            }
        }
        hint.append(" )");
        
        _container.add(new Hint("function", startLoc, endLoc, hint.toString())); //$NON-NLS-1$        
    }
    
    private void visitType(IASTSimpleDeclaration declaration) throws BadLocationException
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

        if(spec instanceof ICPPASTNamedTypeSpecifier)
        {
            IASTName name = ((ICPPASTNamedTypeSpecifier) spec).getName();
            addBrackets(name);            
        }
        
    }

    private void addBrackets(IASTName name) throws BadLocationException
    {
        if( name instanceof ICPPASTTemplateId )
        {
            IASTNode[] args = ((ICPPASTTemplateId) name).getTemplateArguments();
            addBrackets(args);
        } 
        else if( name instanceof ICPPASTQualifiedName)
        {
            IASTName[] names = ((ICPPASTQualifiedName) name).getNames();
            for (IASTName n : names)
                addBrackets(n);
        }        
    }

    private void addBrackets(IASTNode[] args) throws BadLocationException
    {
        if(args == null || args.length == 0)
            return;
                
        int startLoc = args[0].getFileLocation().getNodeOffset() - 1;
        IASTFileLocation endFileLoc = args[args.length-1].getFileLocation();
        int endLoc = endFileLoc.getNodeOffset() + endFileLoc.getNodeLength();
        _container.add(new BracketsPair(startLoc, '<', endLoc, '>'));        
    }

    private int shouldContinue()
    {
        if( _cancelProcessing )
            return PROCESS_ABORT;
        else
            return PROCESS_CONTINUE;
    }
    
}
