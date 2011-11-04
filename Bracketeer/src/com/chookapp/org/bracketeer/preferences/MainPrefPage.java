package com.chookapp.org.bracketeer.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.chookapp.org.bracketeer.Activator;


public class MainPrefPage extends FieldEditorPreferencePage
                          implements IWorkbenchPreferencePage
{
    public MainPrefPage()
    {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription(Messages.MainPrefPage_Description);
    }

    @Override
    protected void createFieldEditors()
    {
    }

    @Override
    public void init(IWorkbench workbench)
    {        
    }    

}
