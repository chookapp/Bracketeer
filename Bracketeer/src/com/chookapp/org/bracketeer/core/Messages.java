package com.chookapp.org.bracketeer.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "com.chookapp.org.bracketeer.core.messages"; //$NON-NLS-1$
    public static String BracketeerProcessingContainer_listsnerNotFound;
    public static String BracketsHighlighter_ErrBracketNotFound;
    public static String BracketsHighlighter_ErrHintNotFound;
    public static String BracketsHighlighter_ErrPairNotFound;
    public static String BracketsHighlighter_ErrUnexpectedEvent;
    public static String BracketsHighlighter_MatchNotHighlighetd;
    public static String BracketsHighlighter_UnableToGetEditor;
    public static String BracketsHighlighter_UnableToGetResource;
    public static String BracketsHighlighter_UnableToPaint_SourceViewer;
    public static String PartListener_ErrWorkbanch;
    public static String ProcessorConfiguration_ErrListenerNotFound;
    public static String ProcessorConfiguration_ErrUnkEllipsis;
    public static String ProcessorsRegistry_ErrProcExists;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
