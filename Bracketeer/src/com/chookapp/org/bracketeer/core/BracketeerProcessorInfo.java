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