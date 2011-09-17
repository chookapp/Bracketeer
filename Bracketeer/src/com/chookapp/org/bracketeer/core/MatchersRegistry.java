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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorPart;

import com.chookapp.org.bracketeer.Activator;
import com.chookapp.org.bracketeer.extensionpoint.BracketeerProcessor;
import com.chookapp.org.bracketeer.extensionpoint.IBracketeerProcessorsFactory;

public class MatchersRegistry {
	
	private static final String PROC_FACTORY_ID = "com.chookapp.org.bracketeer.processors_factory";
	
	private List<IBracketeerProcessorsFactory> _processors;
	
	public MatchersRegistry()
	{
		_processors = new LinkedList<IBracketeerProcessorsFactory>();
		
		IConfigurationElement[] config = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(PROC_FACTORY_ID);
		
		for (IConfigurationElement element : config) {
			try {
				final Object o = element.createExecutableExtension("class");
				if (o instanceof IBracketeerProcessorsFactory)
					_processors.add((IBracketeerProcessorsFactory) o);
			} catch (CoreException e) {
				Activator.log(e);
			}			
		}
	}

	public BracketeerProcessor findProcessorFor(IEditorPart part) {
		BracketeerProcessor processorFound = null;
		
		for( IBracketeerProcessorsFactory processorFactory : _processors ) {
			BracketeerProcessor processor = processorFactory.createProcessorFor(part);
			if( processor != null )
			{
				if( processorFound != null )
					throw new RuntimeException("processor already exists");
				
				processorFound = processor;
			}
		}
		return processorFound;
	}
}
