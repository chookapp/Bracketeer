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

import java.util.Iterator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import com.chookapp.org.bracketeer.common.Hint;
import com.chookapp.org.bracketeer.common.IBracketeerProcessingContainer;
import com.chookapp.org.bracketeer.common.IHintConfiguration;

public class ClosingBracketHintVisitor extends ASTVisitor
{
    Boolean _cancelProcessing;
    IBracketeerProcessingContainer _container;
    IHintConfiguration _hintConf;
    IDocument _doc;
    
    public ClosingBracketHintVisitor(IBracketeerProcessingContainer container,
                                     IDocument doc, Boolean cancelProcessing, 
                                     IHintConfiguration hintConf)
    {
        _cancelProcessing = cancelProcessing;
        _container = container;
        _doc = doc;
        _hintConf = hintConf;        
    }
    
    @Override
    public boolean visit(TypeDeclaration node)
    {
        String hint = node.getName().getIdentifier();
        int startLoc = node.getStartPosition();
        int endLoc = startLoc + node.getLength() - 1;
        _container.add(new Hint("type", startLoc, endLoc, hint)); //$NON-NLS-1$
        return shouldContinue();
    }

    @Override
    public boolean visit(MethodDeclaration node)
    {
        StringBuffer hint = new StringBuffer();
        hint.append(node.getName().getIdentifier());
        /* TODO: specific params: exclude function parameters (show only the name) */
        hint.append('(');
        for (Iterator iterator = node.parameters().iterator(); iterator.hasNext();)
        {
            SingleVariableDeclaration param = (SingleVariableDeclaration) iterator.next();
            hint.append(param.getName());
            if( iterator.hasNext() )
                hint.append(',');
        }
        hint.append(')');
        int startLoc = node.getStartPosition();
        int endLoc = startLoc + node.getLength() - 1;
        _container.add(new Hint("function", startLoc, endLoc, hint.toString())); //$NON-NLS-1$
        return shouldContinue();
    }
    
    @Override
    public boolean visit(ForStatement node)
    {
        /* TODO: specific params: show also initializer && increment expressions */
        String hint = GetNodeText(node.getExpression());
        int startLoc = node.getStartPosition();
        int endLoc = startLoc + node.getLength() - 1;        
        _container.add(new Hint("for", startLoc, endLoc, "for("+hint+")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return shouldContinue();
    }

    @Override
    public boolean visit(EnhancedForStatement node)
    {
        /* TODO: specific params: put 2 checkboxes: the var name & the collection */
        String hint = GetNodeText(node.getExpression());
        int startLoc = node.getStartPosition();
        int endLoc = startLoc + node.getLength() - 1;        
        _container.add(new Hint("foreach", startLoc, endLoc, "foreach("+hint+")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return shouldContinue();
    }
    
    @Override
    public boolean visit(SwitchStatement node)
    {
        String hint = GetNodeText(node.getExpression());
        int startLoc = node.getStartPosition();
        int endLoc = startLoc + node.getLength() - 1;        
        _container.add(new Hint("switch", startLoc, endLoc, "switch("+hint+")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return shouldContinue();
    }
    
    @Override
    public boolean visit(WhileStatement node)
    {
        String hint = GetNodeText(node.getExpression());
        int startLoc = node.getStartPosition();
        int endLoc = startLoc + node.getLength() - 1;        
        _container.add(new Hint("while", startLoc, endLoc, "while("+hint+")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return shouldContinue();
    }
    
    @Override
    public boolean visit(SynchronizedStatement node)
    {
        String hint = GetNodeText(node.getExpression());
        int startLoc = node.getStartPosition();
        int endLoc = startLoc + node.getLength() - 1;        
        _container.add(new Hint("synchronized", startLoc, endLoc, "synchronized("+hint+")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return shouldContinue();
    }

    @Override
    public boolean visit(IfStatement node)
    {
        Statement thenStmt = node.getThenStatement();
        Statement elseStmt = node.getElseStatement();
        String hint = GetNodeText(node.getExpression());

        boolean showIfHint = (elseStmt == null);
        int endLoc = -1;
        
        try
        {
            
            if( !showIfHint )
            {
           
                if(_doc.getLineOfOffset(elseStmt.getStartPosition()) !=
                        _doc.getLineOfOffset(thenStmt.getStartPosition() + thenStmt.getLength()))
                {
                    showIfHint = true;
                }          
                
                // if the else looks like this "} else {", then show the hint on the "{"
                if(!showIfHint && !(elseStmt instanceof IfStatement))
                {
                    endLoc = elseStmt.getStartPosition();
                    showIfHint = true;
                }
            }
        
            if( showIfHint && !(thenStmt instanceof Block))
                showIfHint = false;
            
            if( showIfHint )
            {
                if( endLoc == -1 )
                    endLoc = thenStmt.getStartPosition() + thenStmt.getLength()-1;
                int startLoc = node.getStartPosition();
                _container.add(new Hint("if", startLoc, endLoc, "if("+hint+")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            
            if( elseStmt != null && !(elseStmt instanceof IfStatement) && 
                    (elseStmt instanceof Block))
            {
                endLoc = elseStmt.getStartPosition() + elseStmt.getLength()-1;
                int startLoc = elseStmt.getStartPosition();
                _container.add(new Hint("if", startLoc, endLoc, "else_of_if("+hint+")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            
        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
        
        return shouldContinue();
    }
    
    private String GetNodeText(ASTNode node)
    {
        try
        {
            return _doc.get(node.getStartPosition(), node.getLength());
        }
        catch (BadLocationException e)
        {
            Activator.log(e);
            return "";
        }
    }

    private boolean shouldContinue()
    {
        return !_cancelProcessing;
    }
}
