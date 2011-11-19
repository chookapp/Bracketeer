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
package com.chookapp.org.bracketeer.commands;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

import com.chookapp.org.bracketeer.core.IActiveProcessorListener;

public class SourceProvider extends AbstractSourceProvider implements IActiveProcessorListener
{
    public final static String PLUGIN_NAME = "com.chookapp.org.bracketeer.pluginName"; //$NON-NLS-1$
    
    private String _pluginName; 
    
    public SourceProvider()
    {
        _pluginName = ""; //$NON-NLS-1$
    }

    @Override
    public void dispose()
    {
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map getCurrentState()
    {
        Map<String, String> currentState = new HashMap<String, String>(1);
        currentState.put(PLUGIN_NAME, _pluginName);
        return currentState;
    }

    @Override
    public String[] getProvidedSourceNames()
    {
        return new String[] {PLUGIN_NAME};
    }
   
    @Override
    public void activeProcessorChanged(String processorName)
    {
        if( processorName == null ) 
            processorName = ""; //$NON-NLS-1$
        
        if( _pluginName.equals(processorName) )
            return;
        
        _pluginName = processorName;
        fireSourceChanged(ISources.WORKBENCH, PLUGIN_NAME, processorName);
    }

}
