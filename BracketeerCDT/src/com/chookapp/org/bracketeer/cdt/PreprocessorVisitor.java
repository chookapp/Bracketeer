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

import java.util.Stack;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElseStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;

import com.chookapp.org.bracketeer.common.Hint;
import com.chookapp.org.bracketeer.common.IBracketeerProcessingContainer;
import com.chookapp.org.bracketeer.common.IHintConfiguration;


public class PreprocessorVisitor
{
    Boolean _cancelProcessing;
    IBracketeerProcessingContainer _container;
    IHintConfiguration _hintConf;
    
    class CondInfo
    {
        public String _cond;
        public IASTFileLocation _fileLoc;  

        public CondInfo(String str, IASTFileLocation fileLocation)
        {
            _cond = str;
            _fileLoc = fileLocation;            
        }
    }
    
    Stack<CondInfo> _stack;
    
    public PreprocessorVisitor(IBracketeerProcessingContainer container,
                               Boolean cancelProcessing,
                               IHintConfiguration hintConf)
    {
        _cancelProcessing = cancelProcessing;
        _container = container;
        _hintConf = hintConf;
        
        _stack = new Stack<CondInfo>();
    }
    
    private String stripEolBackslash(char[] ch)
    {
        String str = new String(ch);
        return stripEolBackslash(str);
    }
    
    private String stripEolBackslash(String str)
    {
        return str.replaceAll("\\\\(\\s*[\r|\n])", "$1");
    }

    public void visit(IASTPreprocessorStatement[] stmts)
    {
        for (IASTPreprocessorStatement stmt : stmts)
        {
            if(_cancelProcessing)
                break;
            
            if( stmt instanceof IASTPreprocessorIfStatement)
            {
                StringBuffer str = new StringBuffer();
                str.append("if( ");
                str.append(stripEolBackslash(((IASTPreprocessorIfStatement)stmt).getCondition()));
                str.append(" )");
                _stack.push(new CondInfo(str.toString(), stmt.getFileLocation()));
            }
            else if( stmt instanceof IASTPreprocessorIfdefStatement)
            {
                StringBuffer str = new StringBuffer();
                str.append("if_defined( ");
                str.append(stripEolBackslash(((IASTPreprocessorIfdefStatement)stmt).getCondition()));
                str.append(" )");
                
                _stack.push(new CondInfo(str.toString(), stmt.getFileLocation()));
            }
            else if( stmt instanceof IASTPreprocessorIfndefStatement)
            {
                StringBuffer str = new StringBuffer();
                str.append("if_not_defined( ");
                str.append(stripEolBackslash(((IASTPreprocessorIfndefStatement)stmt).getCondition()));
                str.append(" )");
                
                _stack.push(new CondInfo(str.toString(), stmt.getFileLocation()));
            }
            else if( stmt instanceof IASTPreprocessorElifStatement)
            {
                CondInfo cond = null;
                if(!_stack.empty())
                {
                    cond = _stack.pop();
                }
                
                if( cond != null )
                {
                    IASTFileLocation location = stmt.getFileLocation();
                    int endLoc = location.getNodeOffset()+location.getNodeLength()-1;
                    int startLoc = cond._fileLoc.getNodeOffset();
                    _container.add(new Hint("preprocess", startLoc, endLoc, cond._cond));
                }
                
                StringBuffer str = new StringBuffer();
                str.append("if( ");
                str.append(stripEolBackslash(((IASTPreprocessorElifStatement)stmt).getCondition()));
                str.append(" )");
                
                _stack.push(new CondInfo(str.toString(), stmt.getFileLocation()));
            }
            else if (stmt instanceof IASTPreprocessorElseStatement)
            {
                CondInfo cond = null;
                if(!_stack.empty())
                {
                    cond = _stack.pop();
                }

                StringBuffer str = new StringBuffer();
                str.append("else_of_");

                if( cond != null )
                {
                    IASTFileLocation location = stmt.getFileLocation();
                    int endLoc = location.getNodeOffset()+location.getNodeLength()-1;
                    int startLoc = cond._fileLoc.getNodeOffset();
                    _container.add(new Hint("preprocess", startLoc, endLoc, cond._cond));
                    str.append(cond._cond);
                }
                
                _stack.push(new CondInfo(str.toString(), stmt.getFileLocation()));
            }
            else if( stmt instanceof IASTPreprocessorEndifStatement)
            {
                if(_stack.empty())
                    continue;
                
                CondInfo cond = _stack.pop();
                
                IASTFileLocation location = stmt.getFileLocation();
                int endLoc = location.getNodeOffset()+location.getNodeLength()-1;
                int startLoc = cond._fileLoc.getNodeOffset();
                _container.add(new Hint("preprocess", startLoc, endLoc, cond._cond));
            }            
        }
    }

}
