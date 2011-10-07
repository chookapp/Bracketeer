package com.chookapp.org.bracketeer.preferences;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

import com.chookapp.org.bracketeer.Activator;
import com.chookapp.org.bracketeer.core.ProcessorsRegistry;

public class PreferencesInitializer extends AbstractPreferenceInitializer
{
    @Override
    public void initializeDefaultPreferences()
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        
        IConfigurationElement[] config = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(ProcessorsRegistry.PROC_FACTORY_ID);
        
        for (IConfigurationElement element : config) 
        {
            String pluginName = element.getAttribute("name");

            /* the default */
            
            store.setDefault(PreferencesConstants.preferencePath(pluginName) +
                             PreferencesConstants.Highlights.getAttrPath(0, true) +
                             PreferencesConstants.Highlights.UseDefault, false);
            
            PreferenceConverter.setDefault(store, PreferencesConstants.preferencePath(pluginName) +
                                           PreferencesConstants.Highlights.getAttrPath(0, true) +
                                           PreferencesConstants.Highlights.Color, 
                                           new RGB(255,255,255));
            
            store.setDefault(PreferencesConstants.preferencePath(pluginName) +
                             PreferencesConstants.Highlights.getAttrPath(0, false) +
                             PreferencesConstants.Highlights.UseDefault, true);
            
            /* the brackets... */
            
            for (int i = 1 ; i < PreferencesConstants.MAX_PAIRS + 2; i++ )
            {
                            
                store.setDefault(PreferencesConstants.preferencePath(pluginName) +
                                 PreferencesConstants.Highlights.getAttrPath(i, true) +
                                 PreferencesConstants.Highlights.UseDefault, true);
                
                store.setDefault(PreferencesConstants.preferencePath(pluginName) +
                                 PreferencesConstants.Highlights.getAttrPath(i, false) +
                                 PreferencesConstants.Highlights.UseDefault, false);
          
                if( i == PreferencesConstants.MAX_PAIRS + 1)
                {
                    PreferenceConverter.setDefault(store, PreferencesConstants.preferencePath(pluginName) +
                                                   PreferencesConstants.Highlights.getAttrPath(i, false) +
                                                   PreferencesConstants.Highlights.Color, 
                                                   new RGB(250,0,0));   
                }
                else
                {
                    int max = PreferencesConstants.MAX_PAIRS;
                    //int val = (((max-i)+1)*255)/(max+1);
                    int val = (i*255)/(max+1);
                    PreferenceConverter.setDefault(store, PreferencesConstants.preferencePath(pluginName) +
                                                   PreferencesConstants.Highlights.getAttrPath(i, false) +
                                                   PreferencesConstants.Highlights.Color, 
                                                   new RGB(val,val,val));
                }
                
                // TODO: get the real editor background default
                /*
                Color def = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
                PreferenceConverter.setDefault(store, PreferencesConstants.preferencePath(pluginName) +
                                                      PreferencesConstants.Highlights.getAttrPath(i, false) +
                                                      PreferencesConstants.Highlights.Color, def.getRGB());
                */
                
                // TODO: get the real editor background default
                /*
                def = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
                PreferenceConverter.setDefault(store, PreferencesConstants.preferencePath(pluginName) +
                                                      PreferencesConstants.Highlights.getAttrPath(i, false) +
                                                      PreferencesConstants.Highlights.Color, def.getRGB());
                */
            }
            
            store.setDefault(PreferencesConstants.preferencePath(pluginName) +
                             PreferencesConstants.Surrounding.Enable, true);
            store.setDefault(PreferencesConstants.preferencePath(pluginName) +
                             PreferencesConstants.Surrounding.NumBracketsToShow, 
                             PreferencesConstants.MAX_PAIRS);
            store.setDefault(PreferencesConstants.preferencePath(pluginName) +
                             PreferencesConstants.Surrounding.ShowBrackets, 
                             element.getAttribute(ProcessorsRegistry.SUPPORTED_BRACKETS_ATTR));
            
            store.setDefault(PreferencesConstants.preferencePath(pluginName) +
                             PreferencesConstants.Hovering.Enable, true);
        }
    }
}
