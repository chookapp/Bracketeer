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
package com.chookapp.org.bracketeer.core;

import com.chookapp.org.bracketeer.extensionpoint.BracketeerProcessor;

public class BracketeerProcessorInfo
{
    private BracketeerProcessor _processor;
    private ProcessorConfiguration _configuration;
    
    public BracketeerProcessorInfo(BracketeerProcessor processor,
                                   ProcessorConfiguration configuration)
    {
        _processor = processor;
        _configuration = configuration;
    }
    
    public BracketeerProcessor getProcessor()
    {
        return _processor;
    }

    public ProcessorConfiguration getConfiguration()
    {
        return _configuration;
    }	    
}