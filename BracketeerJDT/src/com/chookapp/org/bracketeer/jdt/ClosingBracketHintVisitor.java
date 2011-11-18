package com.chookapp.org.bracketeer.jdt;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TypeDeclaration;

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

    private boolean shouldContinue()
    {
        return !_cancelProcessing;
    }
}
