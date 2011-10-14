package com.chookapp.org.bracketeer.cdt;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import com.chookapp.org.bracketeer.common.BracketsPair;
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
            
            String hint = declerator.getRawSignature();            
            _container.add(new Hint("func", startLoc, endLoc, hint));
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
