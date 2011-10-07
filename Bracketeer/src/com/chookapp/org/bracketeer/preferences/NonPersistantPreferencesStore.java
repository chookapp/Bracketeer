package com.chookapp.org.bracketeer.preferences;

import org.eclipse.jface.preference.PreferenceStore;

public class NonPersistantPreferencesStore extends PreferenceStore
{
    public NonPersistantPreferencesStore()
    {
        super();
    }
    
    public boolean needsSaving()
    {
        return false;
    }
}
