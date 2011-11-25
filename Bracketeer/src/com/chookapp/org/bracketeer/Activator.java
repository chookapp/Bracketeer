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
package com.chookapp.org.bracketeer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.chookapp.org.bracketeer.core.PartListener;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.chookapp.org.Braketeer"; //$NON-NLS-1$

    public static final boolean DEBUG = false;

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
	    super.start(context);
	    plugin = this;
	    Display.getDefault().asyncExec(new Runnable() {
	        public void run() {
	            PartListener.getInstance().install();
	        }
	    });
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception 
	{
	    try
	    {
	        PartListener.getInstance().uninstall();
	        plugin = null;
	    }
	    finally
	    {
	        super.stop(context);
	    }
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * @param e
	 */
	public static void log(Throwable e) {
		getDefault().getLog().log(getStatus(e));
	}

	public static void log(String message) {
		getDefault().getLog().log(new Status(Status.ERROR, PLUGIN_ID, message));
	}

	public static void trace(String message) {
        getDefault().getLog().log(new Status(Status.INFO, PLUGIN_ID, message));
    }

	/**
	 * @param e
	 * @return
	 */
	public static IStatus getStatus(Throwable e) {
		return new Status(Status.WARNING, PLUGIN_ID, e.getLocalizedMessage(), e);
	}	
	
}
