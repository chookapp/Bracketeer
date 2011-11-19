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

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.wb.swt.FieldLayoutPreferencePage;

import com.chookapp.org.bracketeer.Activator;

public abstract class ChangingFieldsPrefPage extends FieldLayoutPreferencePage                                              
{
    protected ArrayList<String> _prefNames;
    private IPreferenceStore _tempPrefs;
    private IPreferenceStore _realPrefs;
    
    protected ChangingFieldsPrefPage()
    {
        _prefNames = new ArrayList<String>();
        _tempPrefs = new NonPersistantPreferencesStore();
        // If we want to re-enable design mode, we should comment out this line
        _realPrefs = Activator.getDefault().getPreferenceStore();
        setPreferenceStore(_tempPrefs);
    }
    
    @Override
    protected void addField(FieldEditor editor)
    {
        super.addField(editor);
        _prefNames.add(editor.getPreferenceName());
    }
    
    @Override
    protected void initialize()
    {
        for(String attr : _prefNames)
        {
            _tempPrefs.setDefault(attr, _realPrefs.getDefaultString(attr));
            _tempPrefs.setValue(attr, _realPrefs.getString(attr));
        }
        
        super.initialize();
    }
    
    @Override
    public boolean performOk()
    {
        super.performOk();
        
        for(String attr : _prefNames)
        {
            _realPrefs.setValue(attr, _tempPrefs.getString(attr));
        }

        if (_realPrefs.needsSaving()
                && _realPrefs instanceof IPersistentPreferenceStore) {
            try {
                ((IPersistentPreferenceStore) _realPrefs).save();
            } catch (IOException e) {
                Activator.log(e);
            }
        }

        return true;
    }
    
    @Override
    protected void performDefaults()
    {        
        super.performDefaults();
        for(String attr : _prefNames)
        {
            _tempPrefs.setValue(attr, _realPrefs.getDefaultString(attr));
        }
        updateAll();
    }
    
    protected void setEnable(Composite comp, boolean enable)
    {
        Control[] controls = comp.getChildren();
        if( controls == null )
            return;
        
        for (Control c : controls)
        {
            if( c instanceof Composite && !(c instanceof Spinner))
                setEnable((Composite)c, enable);
            else
                c.setEnabled(enable);
        }
    }
    
    protected abstract void updateAll();
}
