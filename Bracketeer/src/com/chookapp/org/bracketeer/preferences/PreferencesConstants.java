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
package com.chookapp.org.bracketeer.preferences;

public final class PreferencesConstants
{ 
    public static final int MAX_PAIRS = 4;
    
    public final static class Annotations
    {
        private static final String AnnotationPath = "Annotation."; //$NON-NLS-1$

        public static final String Enable = AnnotationPath + "MissingPair.Enable"; //$NON-NLS-1$
    }
    
    public final static class Surrounding
    {
        private static final String SurroundingPath = "Surrounding."; //$NON-NLS-1$
        
        public static final String Enable = SurroundingPath + "Enable"; //$NON-NLS-1$
        public static final String ShowBrackets = SurroundingPath + "ShowBrackets"; //$NON-NLS-1$
        public static final String NumBracketsToShow = SurroundingPath + "NumBracketsToShow";  //$NON-NLS-1$
        public static final String MinDistanceBetweenBrackets = SurroundingPath + "MinDistanceBetweenBrackets"; //$NON-NLS-1$
    }
    
    public final static class Hovering
    {
        private static final String HoveringPath = "Hovering."; //$NON-NLS-1$
        
        public static final String Enable = HoveringPath + "Enable"; //$NON-NLS-1$
        public static final String PopupEnable = HoveringPath + "EnablePopup"; //$NON-NLS-1$
        public static final String PopupOnlyWithoutHint = HoveringPath + "PopupOnlyWithoutHint"; //$NON-NLS-1$
    }
    
    public final static class Highlights
    {
        private static final String HighlightsPath = "Highlights."; //$NON-NLS-1$
        
        private static final String Default = "Default."; //$NON-NLS-1$
        private static final String MatchingPairPrefix = "MatchingPair"; //$NON-NLS-1$
        private static final String MissingPair = "MissingPair."; //$NON-NLS-1$
        
        private static final String Background = "Background."; //$NON-NLS-1$
        private static final String Foreground = "Foreground."; //$NON-NLS-1$
        
        /**
         * 
         * @param typeId
         *  - 0 is default
         *  - 1 - MAX_PAIRS is matching pairs
         *  - MAX_PAIRS+1 is missing pair
         * @param foregound
         * @return
         */
        public static String getAttrPath(int typeId, boolean foregound)
        {
            String path = HighlightsPath;
            
            if( typeId == 0 )
                path += Default;
            else if( typeId < MAX_PAIRS + 1)
                path += MatchingPairPrefix + typeId + "."; //$NON-NLS-1$
            else // typeId == MAX_PAIRS + 1
                path += MissingPair;
            
            if( foregound )
                path += Foreground;
            else
                path += Background;
            
            return path;
        }
        
        public static final String UseDefault = "UseDefault"; //$NON-NLS-1$
        public static final String Color = "Color"; //$NON-NLS-1$
        public static final String HighlightTypeAttr = "HighlightType"; //$NON-NLS-1$
        
        public static final String HighlightTypeValNone = "None"; //$NON-NLS-1$
        public static final String HighlightTypeValSolid = "Solid"; //$NON-NLS-1$
        public static final String HighlightTypeValOutline = "Outline"; //$NON-NLS-1$
        
    }
    
    public final static class Hints
    {
        public final static class WhenToShow
        {
            private static final String PATH = "WhenToShow."; //$NON-NLS-1$
            
            public static final String USE_DEFAULT = PATH + "UseDefault"; //$NON-NLS-1$
            public static final String SHOW_IN_EDITOR = PATH + "ShowInEditor"; //$NON-NLS-1$
            public static final String MIN_LINES_DISTANCE = PATH + "MinLinesDistance"; //$NON-NLS-1$
        }
        
        public final static class Font
        {
            private static final String PATH = "Font."; //$NON-NLS-1$
            
            public static final String USE_DEFAULT = PATH + "UseDefault"; //$NON-NLS-1$
            public static final String FG_DEFAULT = PATH + "FgSysDefault"; //$NON-NLS-1$
            public static final String FG_COLOR = PATH + "FgColor"; //$NON-NLS-1$
            public static final String BG_DEFAULT = PATH + "BgSysDefault"; //$NON-NLS-1$
            public static final String BG_COLOR = PATH + "BgColor"; //$NON-NLS-1$
            public static final String ITALIC = PATH + "Italic"; //$NON-NLS-1$
        }
        
        public final static class Display
        {
            private static final String PATH = "Display."; //$NON-NLS-1$
            
            public static final String USE_DEFAULT = PATH + "UseDefault"; //$NON-NLS-1$
            public static final String MAX_LENGTH = PATH + "MaxLength"; //$NON-NLS-1$
            public static final String STRIP_WHITESPACE = PATH + "StripWhiteSpaces"; //$NON-NLS-1$
            
            public final static class Ellipsis
            {   
                public static final String ATTR = PATH + "Ellipsis"; //$NON-NLS-1$
                public static final String VAL_END = "end"; //$NON-NLS-1$
                public static final String VAL_MID = "mid"; //$NON-NLS-1$
            }
        }

        public static final String DEFAULT_TYPE = "default"; //$NON-NLS-1$
        
        public final static class Globals
        {
            private static final String PATH = "Hints.Globals."; //$NON-NLS-1$
            public static final String SHOW_IN_EDITOR = PATH + "ShowInEditor"; //$NON-NLS-1$
        }
        
        public final static class Hover
        {
            private static final String PATH = "Hints.Hover."; //$NON-NLS-1$
            public static final String ENABLE = PATH + "Enable"; //$NON-NLS-1$
            public static final String MAX_LEN = PATH + "MaxLength";             //$NON-NLS-1$
        }
        
        // public static final String ENABLED = "enabled";
        
        public static String preferencePath(String hintType)
        {
            return "Hints." + hintType + "."; //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        public static String preferencePath(String pluginName, String hintType)
        {
            return PreferencesConstants.preferencePath(pluginName) + preferencePath(hintType);
        }
    }
    
    public final static class General
    {
        private static final String PATH = "General."; //$NON-NLS-1$
        public static final String HYPERLINK_MODIFIERS = PATH + "HyperlinkModifiers"; //$NON-NLS-1$
    }
    
    public static String preferencePath(String pluginName)
    {
        return pluginName+"."; //$NON-NLS-1$
    }
}
