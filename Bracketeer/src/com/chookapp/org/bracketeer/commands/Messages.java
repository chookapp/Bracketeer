package com.chookapp.org.bracketeer.commands;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "com.chookapp.org.bracketeer.commands.messages"; //$NON-NLS-1$
    public static String BracketeerToggleState_ErrAttrName;
    public static String BracketeerToggleState_ErrAttrSuffix;
    public static String BracketeerToggleState_SrcProviderNotFound;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
