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
import org.eclipse.wb.swt.FieldLayoutPreferencePage;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Group;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.widgets.Button;
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
    private java.util.List<FEInfo> _dynamicFe;
    
    /**
     * Create the preference page.
     */
    public HintsPrefPage()
    {
        _tabInfos = new ArrayList<TabInfo>();
        _dynamicFe = new ArrayList<FEInfo>();
        setDescription("Configuring hints");
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

        // If we want to re-enable design mode, we should comment out this "for", and comment in this stub
//                 IConfigurationElement element = null; // stub
        for (IConfigurationElement element : config) 
        {
            String pluginName = element.getAttribute("name");
            TabInfo tabInfo = new TabInfo();
            _tabInfos.add(tabInfo);
            tabInfo._name = pluginName;
            String basePref = PreferencesConstants.Hints.preferencePath(pluginName, PreferencesConstants.Hints.DEFAULT_TYPE);

            TabItem tbtmNewItem = new TabItem(tabFolder, SWT.NONE);
            tbtmNewItem.setText(pluginName);

            Composite composite = new Composite(tabFolder, SWT.NONE);
            tbtmNewItem.setControl(composite);
            composite.setLayout(new GridLayout(1, false));

            Composite composite_2 = new Composite(composite, SWT.NONE);
            addField(new BooleanFieldEditor(PreferencesConstants.Hints.Globals.SHOW_IN_EDITOR,
                                            "Display hints in editor", BooleanFieldEditor.DEFAULT, composite_2));

//            Composite composite_3 = new Composite(composite, SWT.NONE);
//            addField(new BooleanFieldEditor(PreferencesConstants.Hints.Globals.SHOW_ON_HOVER,
//                                            "Display tooltip on hover", BooleanFieldEditor.DEFAULT, composite_3));

            Composite composite_1 = new Composite(composite, SWT.NONE);
            composite_1.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1));
            composite_1.setBounds(0, 0, 64, 64);
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
            list.add("- Default -");
            IConfigurationElement[] hints = element.getChildren("Hint");
            for (IConfigurationElement hint : hints)
            {
                String hintType = hint.getAttribute("type");
                list.add(hintType);
            }
            list.setSelection(0);

            Composite composite_5 = new Composite(composite_1, SWT.NONE);
            composite_5.setLayout(new GridLayout(1, false));
            composite_5.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 1, 1));
            composite_5.setBounds(0, 0, 64, 64);

            Group grpWhenToShow = new Group(composite_5, SWT.NONE);
            grpWhenToShow.setText("When to show");
            grpWhenToShow.setLayout(new GridLayout(1, false));

            Composite composite_6 = new Composite(grpWhenToShow, SWT.NONE);
            BooleanFieldEditor bfe = new BooleanFieldEditor(basePref + PreferencesConstants.Hints.WhenToShow.USE_DEFAULT, 
                                                            "Use default", BooleanFieldEditor.DEFAULT, composite_6);
            addField(bfe);
            addDynamicFE(bfe, PreferencesConstants.Hints.WhenToShow.USE_DEFAULT);
            tabInfo._whenToShowUseDef = bfe;
            tabInfo._whenToShowUseDefParent = composite_6;

            Composite composite_15 = new Composite(grpWhenToShow, SWT.NONE);
            GridLayout gl_composite_15 = new GridLayout(1, false);
            gl_composite_15.marginLeft = 10;
            composite_15.setLayout(gl_composite_15);
            
            Composite composite_7 = new Composite(composite_15, SWT.NONE);
            bfe = new BooleanFieldEditor(basePref + PreferencesConstants.Hints.WhenToShow.SHOW_IN_EDITOR,
                                         "Show hints in editor", BooleanFieldEditor.DEFAULT, composite_7);
            addField(bfe);
            addDynamicFE(bfe, PreferencesConstants.Hints.WhenToShow.SHOW_IN_EDITOR);
            tabInfo._showInEditor = bfe;
            tabInfo._showInEditorParent = composite_7;

            Composite composite_14 = new Composite(composite_15, SWT.NONE);
            SpinnerFieldEditor spinner = new SpinnerFieldEditor(basePref + PreferencesConstants.Hints.WhenToShow.MIN_LINES_DISTANCE,
                                                                "Min lines between brackets", composite_14);
            addField(spinner);
            addDynamicFE(spinner, PreferencesConstants.Hints.WhenToShow.MIN_LINES_DISTANCE);
            tabInfo._whenToShowMinLines = composite_14;

            Composite composite_20 = new Composite(composite_5, SWT.NONE);
            composite_20.setLayout(new GridLayout(2, false));

            Group grpFont = new Group(composite_20, SWT.NONE);
            grpFont.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
            grpFont.setText("Font");
            grpFont.setBounds(0, 0, 209, 147);
            grpFont.setLayout(new GridLayout(1, false));

            Composite composite_8 = new Composite(grpFont, SWT.NONE);
            bfe = new BooleanFieldEditor(basePref + PreferencesConstants.Hints.Font.USE_DEFAULT,
                                         "Use default", BooleanFieldEditor.DEFAULT, composite_8);
            addField(bfe);
            addDynamicFE(bfe, PreferencesConstants.Hints.Font.USE_DEFAULT);
            tabInfo._fontUseDef = bfe;
            tabInfo._fontUseDefParent = composite_8;

            Composite composite_9 = new Composite(grpFont, SWT.NONE);
            composite_9.setBounds(0, 0, 64, 64);
            GridLayout gl_composite_9 = new GridLayout(1, false);
            gl_composite_9.marginLeft = 10;
            composite_9.setLayout(gl_composite_9);
            tabInfo._fontGrp = composite_9;

            Group grpForegroundColor = new Group(composite_9, SWT.NONE);
            grpForegroundColor.setText("Foreground color");
            grpForegroundColor.setLayout(new GridLayout(1, false));

            Composite composite_21 = new Composite(grpForegroundColor, SWT.NONE);
            bfe = new BooleanFieldEditor(basePref + PreferencesConstants.Hints.Font.FG_DEFAULT, 
                                         "Use system default", BooleanFieldEditor.DEFAULT, composite_21);
            addField(bfe);
            addDynamicFE(bfe, PreferencesConstants.Hints.Font.FG_DEFAULT);
            tabInfo._fontFgDef = bfe;

            Composite composite_10 = new Composite(grpForegroundColor, SWT.NONE);
            ColorFieldEditor cfe = new ColorFieldEditor(basePref + PreferencesConstants.Hints.Font.FG_COLOR,
                                                        "color", composite_10);
            cfe.getLabelControl(composite_10).setText("Color:");
            addField(cfe);
            addDynamicFE(cfe, PreferencesConstants.Hints.Font.FG_COLOR);
            tabInfo._fontFgColor = composite_10;

            Group grpBackgroundColor = new Group(composite_9, SWT.NONE);
            grpBackgroundColor.setText("Background color");
            grpBackgroundColor.setLayout(new GridLayout(1, false));

            Composite composite_22 = new Composite(grpBackgroundColor, SWT.NONE);
            bfe = new BooleanFieldEditor(basePref + PreferencesConstants.Hints.Font.BG_DEFAULT, 
                                         "Use system default", BooleanFieldEditor.DEFAULT, composite_22);
            addField(bfe);
            addDynamicFE(bfe, PreferencesConstants.Hints.Font.BG_DEFAULT);
            tabInfo._fontBgDef = bfe;

            Composite composite_11 = new Composite(grpBackgroundColor, SWT.NONE);
            cfe = new ColorFieldEditor(basePref + PreferencesConstants.Hints.Font.BG_COLOR,
                                         "color", composite_11);
            cfe.getLabelControl(composite_11).setText("Color:");
            addField(cfe);
            addDynamicFE(cfe, PreferencesConstants.Hints.Font.BG_COLOR);
            tabInfo._fontBgColor = composite_11;

            Composite composite_12 = new Composite(composite_9, SWT.NONE);
            bfe = new BooleanFieldEditor(basePref + PreferencesConstants.Hints.Font.ITALIC,
                                         "Italic", BooleanFieldEditor.DEFAULT, composite_12);
            addField(bfe);
            addDynamicFE(bfe, PreferencesConstants.Hints.Font.ITALIC);

            Group grpShow = new Group(composite_20, SWT.NONE);
            grpShow.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
            grpShow.setSize(160, 183);
            grpShow.setText("Display");
            grpShow.setLayout(new GridLayout(1, false));

            Composite composite_13 = new Composite(grpShow, SWT.NONE);
            bfe = new BooleanFieldEditor(basePref + PreferencesConstants.Hints.Display.USE_DEFAULT,
                                         "Use default", BooleanFieldEditor.DEFAULT, composite_13);
            addField(bfe);
            addDynamicFE(bfe, PreferencesConstants.Hints.Display.USE_DEFAULT);
            tabInfo._displayUseDef = bfe;
            tabInfo._displayUseDefParnet = composite_13;

            Composite composite_18 = new Composite(grpShow, SWT.NONE);
            GridLayout gl_composite_18 = new GridLayout(1, false);
            gl_composite_18.marginLeft = 10;
            composite_18.setLayout(gl_composite_18);
            tabInfo._displayGrp = composite_18;

            Composite composite_19 = new Composite(composite_18, SWT.NONE);
            spinner = new SpinnerFieldEditor(basePref + PreferencesConstants.Hints.Display.MAX_LENGTH,
                                             "Max length", composite_19);
            addField(spinner);
            addDynamicFE(spinner, PreferencesConstants.Hints.Display.MAX_LENGTH);

            Composite composite_17 = new Composite(composite_18, SWT.NONE);
            bfe = new BooleanFieldEditor(basePref + PreferencesConstants.Hints.Display.STRIP_WHITESPACE,
                                         "Strip whitespaces", BooleanFieldEditor.DEFAULT, composite_17);
            addField(bfe);
            addDynamicFE(bfe, PreferencesConstants.Hints.Display.STRIP_WHITESPACE);

            Composite composite_16 = new Composite(composite_18, SWT.NONE);
            {
                RadioGroupFieldEditor radioGroupFieldEditor = new RadioGroupFieldEditor(basePref + PreferencesConstants.Hints.Display.Ellipsis.ATTR, 
                                                                                        "Ellipsis:", 1, 
                                                                                        new String[][]{{"In the middle", PreferencesConstants.Hints.Display.Ellipsis.VAL_MID},
                                                                                                       {"At the end", PreferencesConstants.Hints.Display.Ellipsis.VAL_END}},
                                                                                                       composite_16, false);
                radioGroupFieldEditor.setIndent(0);
                addField(radioGroupFieldEditor);
                addDynamicFE(radioGroupFieldEditor, PreferencesConstants.Hints.Display.Ellipsis.ATTR);
            }
        }
        return container;
    }

    private void addDynamicFE(FieldEditor fe, String attrSuffix)
    {
        _dynamicFe.add(new FEInfo(fe, attrSuffix));
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
                
                for (FEInfo feInfo : _dynamicFe)
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
                    event.getSource() == tabInfo._showInEditor)
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
            int idx = tabInfo._hintsList.getSelectionIndex();
            String type;
            if(idx == 0 )
                type = PreferencesConstants.Hints.DEFAULT_TYPE;
            else
                type = tabInfo._hintsList.getItem(idx);
            
            String basePref = PreferencesConstants.Hints.preferencePath(tabInfo._name, type);
            
            for (FEInfo feInfo : _dynamicFe)
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
        }
        
    }

    
}
