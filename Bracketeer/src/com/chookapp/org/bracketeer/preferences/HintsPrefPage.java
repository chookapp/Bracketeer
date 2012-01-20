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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;

import com.chookapp.org.bracketeer.core.ProcessorsRegistry;

public class HintsPrefPage extends ChangingFieldsPrefPage implements IWorkbenchPreferencePage
{

    class TabInfo
    {    
        public String _name;
        public List _hintsList;
        public BooleanFieldEditor _whenToShowUseDef;
        public BooleanFieldEditor _showInEditor;    
        public Composite _whenToShowMinLines;
        public BooleanFieldEditor _fontUseDef;
        public Composite _fontGrp;
        public BooleanFieldEditor _displayUseDef;
        public Composite _displayGrp;
        public BooleanFieldEditor _fontFgDef;
        public Composite _fontFgColor;
        public BooleanFieldEditor _fontBgDef;
        public Composite _fontBgColor;
        public Composite _showInEditorParent;
        public Composite _displayUseDefParnet;
        public Composite _fontUseDefParent;
        public Composite _whenToShowUseDefParent;
        
        public java.util.List<FEInfo> _dynamicFe;
        public BooleanFieldEditor _hoverEn;
        public Composite _hoverMaxLen;
    }
    
    class FEInfo
    {
        public FieldEditor _fe;
        public String _attrSuffix;
        
        public FEInfo(FieldEditor fe, String attrSuffix)
        {
            _fe = fe;
            _attrSuffix = attrSuffix;
        }
        
    }
    
    private java.util.List<TabInfo> _tabInfos;    
    
    /**
     * Create the preference page.
     */
    public HintsPrefPage()
    {
        _tabInfos = new ArrayList<TabInfo>();        
        // setDescription(Messages.HintsPrefPage_Description);
    }

    /**
     * Create contents of the preference page.
     * @param parent
     */
    @Override
    public Control createPageContents(Composite parent)
    {
        Composite container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout(1, false));
        
        TabFolder tabFolder = new TabFolder(container, SWT.NONE);
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        
        IConfigurationElement[] config = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(ProcessorsRegistry.PROC_FACTORY_ID);

        if( config.length == 0 )
        {
            Text txtNoBracketeerEditor = new Text(container, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
            txtNoBracketeerEditor.setText(Messages.MainPrefPage_txtNoBracketeerEditor_text);
            
            return container; 
        }
        
        // If we want to re-enable design mode, we should comment out this "for", and comment in this stub
//        IConfigurationElement element = null; // stub
        for (IConfigurationElement element : config) 
        {
            String pluginName = element.getAttribute("name"); //$NON-NLS-1$
            TabInfo tabInfo = new TabInfo();
            _tabInfos.add(tabInfo);
            tabInfo._name = pluginName;
            tabInfo._dynamicFe = new ArrayList<FEInfo>();
            String basePref = PreferencesConstants.Hints.preferencePath(pluginName, PreferencesConstants.Hints.DEFAULT_TYPE);           
            
            TabItem tbtmNewItem = new TabItem(tabFolder, SWT.NONE);
            tbtmNewItem.setText(pluginName);

            Composite composite = new Composite(tabFolder, SWT.NONE);
            tbtmNewItem.setControl(composite);
            composite.setLayout(new GridLayout(1, false));
            
            IConfigurationElement[] hints = element.getChildren("Hint"); //$NON-NLS-1$
            if( hints.length == 0 )
            {
                Label lable = new Label(composite, NONE);
                lable.setText(Messages.HintsPrefPage_NotSupported);
                
                tabInfo._hintsList = null;
                // If we want to re-enable design mode, we should comment out this line
                continue;
            }

            Composite composite_2 = new Composite(composite, SWT.NONE);
            addField(new BooleanFieldEditor(PreferencesConstants.preferencePath(pluginName)+PreferencesConstants.Hints.Globals.SHOW_IN_EDITOR,
                                            Messages.HintsPrefPage_DisplayHintsInEditor, BooleanFieldEditor.DEFAULT, composite_2));
            
            Composite composite_3 = new Composite(composite, SWT.NONE);
            BooleanFieldEditor bfe = new BooleanFieldEditor(PreferencesConstants.preferencePath(pluginName) +
                                         PreferencesConstants.Hints.Hover.ENABLE,
                                         Messages.HintsPrefPage_HintOnHover, BooleanFieldEditor.DEFAULT, composite_3);
            addField(bfe);
            tabInfo._hoverEn = bfe;
            
            Composite composite_24 = new Composite(composite, SWT.NONE);
            GridLayout gl_composite_24 = new GridLayout(1, false);
            gl_composite_24.verticalSpacing = 0;
            gl_composite_24.marginHeight = 0;
            gl_composite_24.marginLeft = 10;
            composite_24.setLayout(gl_composite_24);
            
            Composite composite_23 = new Composite(composite_24, SWT.NONE);

            SpinnerFieldEditor spinner = new SpinnerFieldEditor(PreferencesConstants.preferencePath(pluginName) +
                                                                PreferencesConstants.Hints.Hover.MAX_LEN,
                                                                Messages.HintsPrefPage_OverrideMaxLength, composite_23);
            spinner.setLabelText(Messages.HintsPrefPage_HintHoverMaxLen);
            addField(spinner);
            tabInfo._hoverMaxLen = composite_23;
            
            Group grpHintsConfiguration = new Group(composite, SWT.NONE);
            grpHintsConfiguration.setText(Messages.HintsPrefPage_grpHintsConfiguration_text);
            grpHintsConfiguration.setLayout(new GridLayout(1, false));

//            Composite composite_3 = new Composite(composite, SWT.NONE);
//            addField(new BooleanFieldEditor(PreferencesConstants.Hints.Globals.SHOW_ON_HOVER,
//                                            "Display tooltip on hover", BooleanFieldEditor.DEFAULT, composite_3));

            Composite composite_1 = new Composite(grpHintsConfiguration, SWT.NONE);
            composite_1.setLayout(new GridLayout(2, false));

            Composite composite_4 = new Composite(composite_1, SWT.NONE);
            composite_4.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
            composite_4.setBounds(0, 0, 64, 64);
            composite_4.setLayout(new GridLayout(1, false));

            List list = new List(composite_4, SWT.BORDER);
            GridData gd_list = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
            gd_list.widthHint = 148;
            list.setLayoutData(gd_list);
            tabInfo._hintsList = list;
            list.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateHintFieldEditors();
                }
            });
            list.add(Messages.HintsPrefPage_DefaultEntry);
            
            for (IConfigurationElement hint : hints)
            {
                String hintType = hint.getAttribute("type"); //$NON-NLS-1$
                list.add(hintType);
            }
            list.setSelection(0);

            Composite composite_5 = new Composite(composite_1, SWT.NONE);
            composite_5.setLayout(new GridLayout(1, false));
            composite_5.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 1, 1));
            composite_5.setBounds(0, 0, 64, 64);

            Group grpWhenToShow = new Group(composite_5, SWT.NONE);
            grpWhenToShow.setText(Messages.HintsPrefPage_WhenToShow);
            grpWhenToShow.setLayout(new GridLayout(1, false));

            Composite composite_6 = new Composite(grpWhenToShow, SWT.NONE);
            bfe = new BooleanFieldEditor(basePref + PreferencesConstants.Hints.WhenToShow.USE_DEFAULT, 
                                         Messages.HintsPrefPage_UseDef, BooleanFieldEditor.DEFAULT, composite_6);
            addField(bfe);
            addDynamicFE(tabInfo, bfe, PreferencesConstants.Hints.WhenToShow.USE_DEFAULT);
            tabInfo._whenToShowUseDef = bfe;
            tabInfo._whenToShowUseDefParent = composite_6;

            Composite composite_15 = new Composite(grpWhenToShow, SWT.NONE);
            GridLayout gl_composite_15 = new GridLayout(1, false);
            gl_composite_15.marginLeft = 10;
            composite_15.setLayout(gl_composite_15);

            Composite composite_7 = new Composite(composite_15, SWT.NONE);
            bfe = new BooleanFieldEditor(basePref + PreferencesConstants.Hints.WhenToShow.SHOW_IN_EDITOR,
                                         Messages.HintsPrefPage_DisplayHintsInEditor, BooleanFieldEditor.DEFAULT, composite_7);
            addField(bfe);
            addDynamicFE(tabInfo, bfe, PreferencesConstants.Hints.WhenToShow.SHOW_IN_EDITOR);
            tabInfo._showInEditor = bfe;
            tabInfo._showInEditorParent = composite_7;

            Composite composite_14 = new Composite(composite_15, SWT.NONE);
            spinner = new SpinnerFieldEditor(basePref + PreferencesConstants.Hints.WhenToShow.MIN_LINES_DISTANCE,
                                             Messages.HintsPrefPage_MinLines, composite_14);
            addField(spinner);
            addDynamicFE(tabInfo, spinner, PreferencesConstants.Hints.WhenToShow.MIN_LINES_DISTANCE);
            tabInfo._whenToShowMinLines = composite_14;

            Composite composite_20 = new Composite(composite_5, SWT.NONE);
            composite_20.setLayout(new GridLayout(2, false));

            Group grpFont = new Group(composite_20, SWT.NONE);
            grpFont.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
            grpFont.setText(Messages.HintsPrefPage_Font);
            grpFont.setBounds(0, 0, 209, 147);
            grpFont.setLayout(new GridLayout(1, false));

            Composite composite_8 = new Composite(grpFont, SWT.NONE);
            bfe = new BooleanFieldEditor(basePref + PreferencesConstants.Hints.Font.USE_DEFAULT,
                                         Messages.HintsPrefPage_UseDef, BooleanFieldEditor.DEFAULT, composite_8);
            addField(bfe);
            addDynamicFE(tabInfo, bfe, PreferencesConstants.Hints.Font.USE_DEFAULT);
            tabInfo._fontUseDef = bfe;
            tabInfo._fontUseDefParent = composite_8;

            Composite composite_9 = new Composite(grpFont, SWT.NONE);
            composite_9.setBounds(0, 0, 64, 64);
            GridLayout gl_composite_9 = new GridLayout(1, false);
            gl_composite_9.marginLeft = 10;
            composite_9.setLayout(gl_composite_9);
            tabInfo._fontGrp = composite_9;

            Group grpForegroundColor = new Group(composite_9, SWT.NONE);
            grpForegroundColor.setText(Messages.HintsPrefPage_FgColor);
            grpForegroundColor.setLayout(new GridLayout(1, false));

            Composite composite_21 = new Composite(grpForegroundColor, SWT.NONE);
            bfe = new BooleanFieldEditor(basePref + PreferencesConstants.Hints.Font.FG_DEFAULT, 
                                         Messages.HintsPrefPage_UseSysDef, BooleanFieldEditor.DEFAULT, composite_21);
            addField(bfe);
            addDynamicFE(tabInfo, bfe, PreferencesConstants.Hints.Font.FG_DEFAULT);
            tabInfo._fontFgDef = bfe;

            Composite composite_10 = new Composite(grpForegroundColor, SWT.NONE);
            ColorFieldEditor cfe = new ColorFieldEditor(basePref + PreferencesConstants.Hints.Font.FG_COLOR,
                                                        Messages.HintsPrefPage_Color, composite_10);
            addField(cfe);
            addDynamicFE(tabInfo, cfe, PreferencesConstants.Hints.Font.FG_COLOR);
            tabInfo._fontFgColor = composite_10;

            Group grpBackgroundColor = new Group(composite_9, SWT.NONE);
            grpBackgroundColor.setText(Messages.HintsPrefPage_BgColor);
            grpBackgroundColor.setLayout(new GridLayout(1, false));

            Composite composite_22 = new Composite(grpBackgroundColor, SWT.NONE);
            bfe = new BooleanFieldEditor(basePref + PreferencesConstants.Hints.Font.BG_DEFAULT, 
                                         Messages.HintsPrefPage_UseSysDef, BooleanFieldEditor.DEFAULT, composite_22);
            addField(bfe);
            addDynamicFE(tabInfo, bfe, PreferencesConstants.Hints.Font.BG_DEFAULT);
            tabInfo._fontBgDef = bfe;

            Composite composite_11 = new Composite(grpBackgroundColor, SWT.NONE);
            cfe = new ColorFieldEditor(basePref + PreferencesConstants.Hints.Font.BG_COLOR,
                                       Messages.HintsPrefPage_Color, composite_11);
            addField(cfe);
            addDynamicFE(tabInfo, cfe, PreferencesConstants.Hints.Font.BG_COLOR);
            tabInfo._fontBgColor = composite_11;

            Composite composite_12 = new Composite(composite_9, SWT.NONE);
            bfe = new BooleanFieldEditor(basePref + PreferencesConstants.Hints.Font.ITALIC,
                                         Messages.HintsPrefPage_Italic, BooleanFieldEditor.DEFAULT, composite_12);
            addField(bfe);
            addDynamicFE(tabInfo, bfe, PreferencesConstants.Hints.Font.ITALIC);

            Group grpShow = new Group(composite_20, SWT.NONE);
            grpShow.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
            grpShow.setSize(160, 183);
            grpShow.setText(Messages.HintsPrefPage_Display);
            grpShow.setLayout(new GridLayout(1, false));

            Composite composite_13 = new Composite(grpShow, SWT.NONE);
            bfe = new BooleanFieldEditor(basePref + PreferencesConstants.Hints.Display.USE_DEFAULT,
                                         Messages.HintsPrefPage_UseDef, BooleanFieldEditor.DEFAULT, composite_13);
            addField(bfe);
            addDynamicFE(tabInfo, bfe, PreferencesConstants.Hints.Display.USE_DEFAULT);
            tabInfo._displayUseDef = bfe;
            tabInfo._displayUseDefParnet = composite_13;

            Composite composite_18 = new Composite(grpShow, SWT.NONE);
            GridLayout gl_composite_18 = new GridLayout(1, false);
            gl_composite_18.marginLeft = 10;
            composite_18.setLayout(gl_composite_18);
            tabInfo._displayGrp = composite_18;

            Composite composite_19 = new Composite(composite_18, SWT.NONE);
            spinner = new SpinnerFieldEditor(basePref + PreferencesConstants.Hints.Display.MAX_LENGTH,
                                             Messages.HintsPrefPage_MaxLen, composite_19);
            addField(spinner);
            addDynamicFE(tabInfo, spinner, PreferencesConstants.Hints.Display.MAX_LENGTH);

            Composite composite_17 = new Composite(composite_18, SWT.NONE);
            bfe = new BooleanFieldEditor(basePref + PreferencesConstants.Hints.Display.STRIP_WHITESPACE,
                                         Messages.HintsPrefPage_StipWhitespace, BooleanFieldEditor.DEFAULT, composite_17);
            addField(bfe);
            addDynamicFE(tabInfo, bfe, PreferencesConstants.Hints.Display.STRIP_WHITESPACE);

            Composite composite_16 = new Composite(composite_18, SWT.NONE);
            {
                RadioGroupFieldEditor radioGroupFieldEditor = new RadioGroupFieldEditor(basePref + PreferencesConstants.Hints.Display.Ellipsis.ATTR, 
                                                                                        Messages.HintsPrefPage_Ellipsis, 1, 
                                                                                        new String[][]{{Messages.HintsPrefPage_Mid, PreferencesConstants.Hints.Display.Ellipsis.VAL_MID},
                                                                                                       {Messages.HintsPrefPage_End, PreferencesConstants.Hints.Display.Ellipsis.VAL_END}},
                                                                                                       composite_16, false);
                radioGroupFieldEditor.setIndent(0);
                addField(radioGroupFieldEditor);
                addDynamicFE(tabInfo, radioGroupFieldEditor, PreferencesConstants.Hints.Display.Ellipsis.ATTR);
            }
        }
        
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), "com.choockapp.org.bracketeer.hints_pref"); //$NON-NLS-1$
        return container;
    }

    private void addDynamicFE(TabInfo tabInfo, FieldEditor fe, String attrSuffix)
    {
        tabInfo._dynamicFe.add(new FEInfo(fe, attrSuffix));
    }

    /**
     * Initialize the preference page.
     */
    public void init(IWorkbench workbench)
    {
        // Initialize the preference page
    }
    
    @Override
    protected void initialize()
    {
        for (TabInfo tabInfo : _tabInfos)
        {
            if( tabInfo._hintsList == null )
                continue;
            
            for(String type : tabInfo._hintsList.getItems())
            {
                String basePref = PreferencesConstants.Hints.preferencePath(tabInfo._name, type);
                
//                _prefNames.add(basePref + PreferencesConstants.Hints.Display.MAX_LENGTH);
//                _prefNames.add(basePref + PreferencesConstants.Hints.Display.STRIP_WHITESPACE);
//                _prefNames.add(basePref + PreferencesConstants.Hints.Display.USE_DEFAULT);
//                _prefNames.add(basePref + PreferencesConstants.Hints.Display.Ellipsis.ATTR);
//                
//                _prefNames.add(basePref + PreferencesConstants.Hints.Font.BG_COLOR);
//                _prefNames.add(basePref + PreferencesConstants.Hints.Font.FG_COLOR);
//                _prefNames.add(basePref + PreferencesConstants.Hints.Font.ITALIC);
//                _prefNames.add(basePref + PreferencesConstants.Hints.Font.USE_DEFAULT);
//                
//                _prefNames.add(basePref + PreferencesConstants.Hints.WhenToShow.MIN_LINES_DISTANCE);
//                _prefNames.add(basePref + PreferencesConstants.Hints.WhenToShow.USE_DEFAULT);
//                _prefNames.add(basePref + PreferencesConstants.Hints.WhenToShow.Criteria.ATTR);
                
                for (FEInfo feInfo : tabInfo._dynamicFe)
                {
                    _prefNames.add(basePref + feInfo._attrSuffix);
                }
            }
        }
        
        super.initialize();
        updateAll();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        super.propertyChange(event);
        for (TabInfo tabInfo : _tabInfos)
        {
            if( event.getSource() == tabInfo._displayUseDef || 
                    event.getSource() == tabInfo._fontUseDef ||
                    event.getSource() == tabInfo._whenToShowUseDef ||
                    event.getSource() == tabInfo._fontBgDef ||
                    event.getSource() == tabInfo._fontFgDef ||
                    event.getSource() == tabInfo._showInEditor ||
                    event.getSource() == tabInfo._hoverEn)
            {
                updateAll();
            }
        }
    }
    
    @Override
    protected void updateAll()
    {
        updateHintFieldEditors();
    }

    protected void updateHintFieldEditors()
    {
        for (TabInfo tabInfo : _tabInfos)
        {
            if( tabInfo._hintsList == null )
                continue;
            
            int idx = tabInfo._hintsList.getSelectionIndex();
            String type;
            if(idx == 0 )
                type = PreferencesConstants.Hints.DEFAULT_TYPE;
            else
                type = tabInfo._hintsList.getItem(idx);
            
            String basePref = PreferencesConstants.Hints.preferencePath(tabInfo._name, type);
            
            for (FEInfo feInfo : tabInfo._dynamicFe)
            {
                feInfo._fe.store();
                feInfo._fe.setPreferenceName(basePref + feInfo._attrSuffix);
                feInfo._fe.load();
            }
            
            setEnable(tabInfo._showInEditorParent, !tabInfo._whenToShowUseDef.getBooleanValue());
            
            boolean disableAll;
            {
                String typeToCheck;
                if( tabInfo._whenToShowUseDef.getBooleanValue() )
                    typeToCheck = PreferencesConstants.Hints.DEFAULT_TYPE;
                else
                    typeToCheck = type;
                String prefToCheck = PreferencesConstants.Hints.preferencePath(tabInfo._name, typeToCheck) + 
                        PreferencesConstants.Hints.WhenToShow.SHOW_IN_EDITOR;
                
                disableAll = !getPreferenceStore().getBoolean(prefToCheck);
                
                setEnable( tabInfo._fontGrp.getParent(), !disableAll);
                setEnable( tabInfo._displayGrp.getParent(), !disableAll);
            }   
            
            setEnable(tabInfo._whenToShowMinLines, !tabInfo._whenToShowUseDef.getBooleanValue() && !disableAll);           
            
            setEnable(tabInfo._fontGrp, !tabInfo._fontUseDef.getBooleanValue() && !disableAll);
            if( !tabInfo._fontUseDef.getBooleanValue() && !disableAll )
            {
                setEnable(tabInfo._fontBgColor, !tabInfo._fontBgDef.getBooleanValue());
                setEnable(tabInfo._fontFgColor, !tabInfo._fontFgDef.getBooleanValue());
            }
            
            setEnable(tabInfo._displayGrp, !tabInfo._displayUseDef.getBooleanValue() && !disableAll);
           
            setEnable(tabInfo._whenToShowUseDefParent, idx != 0);
            setEnable(tabInfo._fontUseDefParent, idx != 0 && !disableAll);
            setEnable(tabInfo._displayUseDefParnet, idx != 0 && !disableAll);
            
            setEnable(tabInfo._hoverMaxLen, tabInfo._hoverEn.getBooleanValue());
        }
        
    }

    
}
