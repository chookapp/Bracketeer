package com.chookapp.org.bracketeer.preferences;

import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class LablelessRadioGroupFieldEditor extends RadioGroupFieldEditor
{
    public LablelessRadioGroupFieldEditor(String name, String labelText, int numColumns,
                                          String[][] labelAndValues, Composite parent)
    {
        super(name, labelText, numColumns, labelAndValues, parent);
    }
    
    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        Control control = getRadioBoxControl(parent);
        GridData gd = new GridData();
        gd.horizontalSpan = numColumns;
        gd.horizontalIndent = 0;
        control.setLayoutData(gd);
    }
}
