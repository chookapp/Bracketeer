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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridData;

public class StringPartCheckBoxes extends FieldEditor
{
    private Composite _parent;
    private String _bracketsString;
    private List<Button> _pairsList;
    
    public StringPartCheckBoxes(String name, Composite parent,
                                String bracketsString)
    {
        setPreferenceName(name);
        if( bracketsString == null )
            bracketsString = ""; //$NON-NLS-1$
        _bracketsString = bracketsString;
        _parent = parent;
        _pairsList = new ArrayList<Button>();
        
        createControl(parent);
    }
    
    @Override
    protected void adjustForNumColumns(int numColumns)
    {
        ((GridData)_parent.getLayoutData()).horizontalSpan = numColumns;
    }

    @Override
    protected void doFillIntoGrid(Composite parent, int numColumns)
    {
        _parent = parent;
        
        GridData griddata = new GridData(GridData.FILL_HORIZONTAL);
        griddata.horizontalSpan = numColumns;
        parent.setLayoutData(griddata);
        
        Assert.isTrue(_bracketsString.length() % 2 == 0, Messages.StringPartCheckBoxes_ErrSupportedBrackets);
        for( int i = 0; i < _bracketsString.length(); i += 2 )
        {
            String pair = _bracketsString.substring(i, i+2);
            Button btnCheckButton = new Button(parent, SWT.CHECK);
            btnCheckButton.setText(pair);
            _pairsList.add(btnCheckButton);
        }
    }

    @Override
    protected void doLoad()
    {
        String str = getPreferenceStore().getString(getPreferenceName());
        updateButtons(str);
    }

    @Override
    protected void doLoadDefault()
    {
        String str = getPreferenceStore().getDefaultString(getPreferenceName());
        updateButtons(str);
    }

    @Override
    protected void doStore()
    {
        StringBuilder sb = new StringBuilder();
        for (Button btn : _pairsList)
        {
            if( btn.getSelection() )
                sb.append(btn.getText());
        }
        getPreferenceStore().setValue(getPreferenceName(), sb.toString());
    }

    @Override
    public int getNumberOfControls()
    {
        return 2;
    }
    
    private void updateButtons(String str)
    {
        for (Button btn : _pairsList)
        {
            btn.setSelection(str.contains(btn.getText()));
        }
    }

}
