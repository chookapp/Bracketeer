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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    // The plug-in ID
    public static final String PLUGIN_ID = "com.chookapp.org.Braketeer.JDT"; //$NON-NLS-1$

    public static final boolean DEBUG = false;
    
	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

	/**
	 * @param e
	 */
    public static void log(Throwable e) {
        Platform.getLog(context.getBundle()).log(getStatus(e));
    }

    public static void log(String message) {
        Platform.getLog(context.getBundle()).log(new Status(Status.ERROR, PLUGIN_ID, message));
    }
    
    public static void trace(String message) {
        System.out.println(message);
    }
    
    /**
     * @param e
     * @return
     */
    public static IStatus getStatus(Throwable e) {
        return new Status(Status.WARNING, PLUGIN_ID, e.getLocalizedMessage(), e);
    }       

}
