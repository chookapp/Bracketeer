/*******************************************************************************
 * Copyright (c) Gil Barash - chookapp@yahoo.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gil Barash - initial API and implementation
 *    
 *******************************************************************************/
package com.chookapp.org.bracketeer.core;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;

import com.chookapp.org.bracketeer.Activator;
import com.chookapp.org.bracketeer.extensionpoint.BracketeerProcessor;
import com.chookapp.org.bracketeer.extensionpoint.IBracketeerProcessorsFactory;

public class ProcessorsRegistry 
{	
	public static final String PROC_FACTORY_ID = "com.chookapp.org.bracketeer.processorsFactory"; //$NON-NLS-1$
	public static final String SUPPORTED_BRACKETS_ATTR = "supportedBrackets"; //$NON-NLS-1$

	private List<IBracketeerProcessorsFactory> _processorFactories;
	private List<ProcessorConfiguration> _processorConfigurations;
	
    public ProcessorsRegistry()
	{
		_processorFactories = new LinkedList<IBracketeerProcessorsFactory>();
		_processorConfigurations = new LinkedList<ProcessorConfiguration>();
		
		IConfigurationElement[] config = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(PROC_FACTORY_ID);
		
		for (IConfigurationElement element : config) {
			try {
				final Object o = element.createExecutableExtension("class"); //$NON-NLS-1$
				_processorFactories.add((IBracketeerProcessorsFactory) o);
				
				_processorConfigurations.add( new ProcessorConfiguration(element) );
				
			} catch (Exception e) {
				Activator.log(e);
			}			
		}
	}
	
//	public static List<String> getPluginNames()
//	{
//	    List<String> ret = new LinkedList<String>();
//	    
//	    IConfigurationElement[] config = Platform.getExtensionRegistry()
//                .getConfigurationElementsFor(PROC_FACTORY_ID);
//        
//        for (IConfigurationElement element : config) {
//            try {                
//                String name = element.getAttribute("name");
//                ret.add(name);
//            } catch (Exception e) {
//                Activator.log(e);
//            }           
//        }
//        
//        return ret;
//	}

	public BracketeerProcessorInfo findProcessorFor(IEditorPart part, IDocument doc) 
	{
	    BracketeerProcessorInfo processorInfoFound = null;
		
		for (int i = 0; i < _processorFactories.size(); i++)
        {
			BracketeerProcessor processor = _processorFactories.get(i).createProcessorFor(part, doc);
			if( processor != null )
			{
				if( processorInfoFound != null )
					throw new RuntimeException(Messages.ProcessorsRegistry_ErrProcExists);
				
				processorInfoFound = new BracketeerProcessorInfo( processor,
				                                                  _processorConfigurations.get(i) );
			}
		}
		return processorInfoFound;
	}
}
