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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;

import com.chookapp.org.bracketeer.core.ProcessorsRegistry;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.swt.widgets.Link;


public class HighlightingBracketsPrefPage extends ChangingFieldsPrefPage 
                                          implements IWorkbenchPreferencePage
{  
    
    class TabInfo
    {    
        public java.util.List<BooleanFieldEditor> _highlighUseDefualtFE; // 0 - FG, 1 - BG
        public java.util.List<ColorFieldEditor> _highlighColorFE; // 0 - FG, 1 - BG
        public java.util.List<Composite> _highlighColorFEparent; // 0 - FG, 1 - BG
        public List _highlighList;
        public String _name;
        public Composite _surroundingComposite;
        public BooleanFieldEditor _surroundingEnableFE;
        public ComboFieldEditor _highlighStyleFE;
        public Composite _highlighStyleFEparent;
        public Composite _annotationComposite;
        public BooleanFieldEditor _popupEn;
        public Composite _popupWithoutHint;
        
        public TabInfo()
        {
            _highlighUseDefualtFE = new ArrayList<BooleanFieldEditor>();
            _highlighColorFE = new ArrayList<ColorFieldEditor>();
            _highlighColorFEparent = new ArrayList<Composite>();
        }
    }
   

    private java.util.List<TabInfo> _tabInfos;

    /**
     * Create the preference page.
     */
    public HighlightingBracketsPrefPage()
    {
        _tabInfos = new ArrayList<TabInfo>();
        //setDescription(Messages.HighlightingBracketsPrefPage_Description);
    }

    /**
     * Create contents of the preference page.
     * @param parent
     */
    @Override
    public Control createPageContents(Composite parent)
    {
        Composite container = new Composite(parent, SWT.NULL);
        container.setLayout(new FillLayout(SWT.HORIZONTAL));
        
        TabFolder tabFolder = new TabFolder(container, SWT.NONE);
        
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
            
            TabItem tbtmNewItem = new TabItem(tabFolder, SWT.NONE);
            tbtmNewItem.setText(pluginName);
            
            Composite composite = new Composite(tabFolder, SWT.NONE);
            tbtmNewItem.setControl(composite);
            composite.setLayout(new GridLayout(1, false));
            
            Group grpHighlight = new Group(composite, SWT.NONE);
            grpHighlight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            grpHighlight.setText(Messages.HighlightingBracketsPrefPage_BrktHighlighting);
            grpHighlight.setLayout(new GridLayout(3, false));
            
            Composite composite_13 = new Composite(grpHighlight, SWT.NONE);
            composite_13.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
            composite_13.setLayout(new GridLayout(1, false));
            
            List list = new List(composite_13, SWT.BORDER);
            list.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateHihglightFieldEditors();
                }
            });
            list.setItems(new String[] {Messages.HighlightingBracketsPrefPage_DefaultItem, 
                                        Messages.HighlightingBracketsPrefPage_Pair1, 
                                        Messages.HighlightingBracketsPrefPage_Pair2, 
                                        Messages.HighlightingBracketsPrefPage_Pair3, 
                                        Messages.HighlightingBracketsPrefPage_Pair4, 
                                        Messages.HighlightingBracketsPrefPage_MissingPair});
            GridData gd_list = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
            gd_list.widthHint = 119;
            list.setLayoutData(gd_list);
            list.setSize(71, 177);
            list.setSelection(0);
            tabInfo._highlighList = list;
            
            Composite composite_1 = new Composite(grpHighlight, SWT.NONE);
            composite_1.setLayout(new GridLayout(1, false));
            
            Group grpForegroundColor = new Group(composite_1, SWT.NONE);
            grpForegroundColor.setText(Messages.HighlightingBracketsPrefPage_FgColor);
            grpForegroundColor.setLayout(new GridLayout(1, false));
            
            Composite composite_2 = new Composite(grpForegroundColor, SWT.NONE);
            BooleanFieldEditor bfe = new BooleanFieldEditor(PreferencesConstants.preferencePath(pluginName) +
                                                            PreferencesConstants.Highlights.getAttrPath(0, true) +
                                                            PreferencesConstants.Highlights.UseDefault, 
                                                            Messages.HighlightingBracketsPrefPage_UseDef, BooleanFieldEditor.DEFAULT, composite_2);
            addField(bfe);
            tabInfo._highlighUseDefualtFE.add(bfe);
            
            Composite composite_10 = new Composite(grpForegroundColor, SWT.NONE);
            GridLayout gl_composite_10 = new GridLayout(1, false);
            gl_composite_10.marginLeft = 10;
            composite_10.setLayout(gl_composite_10);
            
            Composite composite_4 = new Composite(composite_10, SWT.NONE);
            ColorFieldEditor cfe = new ColorFieldEditor(PreferencesConstants.preferencePath(pluginName) +
                                                        PreferencesConstants.Highlights.getAttrPath(0, true) +
                                                        PreferencesConstants.Highlights.Color, Messages.HighlightingBracketsPrefPage_Color, composite_4);
            addField(cfe);
            tabInfo._highlighColorFE.add(cfe);
            tabInfo._highlighColorFEparent.add(composite_4);
            
            Group grpBackgroundColor = new Group(composite_1, SWT.NONE);
            grpBackgroundColor.setText(Messages.HighlightingBracketsPrefPage_BgColor);
            grpBackgroundColor.setLayout(new GridLayout(1, false));
            
            Composite composite_3 = new Composite(grpBackgroundColor, SWT.NONE);
            bfe = new BooleanFieldEditor(PreferencesConstants.preferencePath(pluginName) +
                                         PreferencesConstants.Highlights.getAttrPath(0, false) +
                                         PreferencesConstants.Highlights.UseDefault, 
                                         Messages.HighlightingBracketsPrefPage_UseDef, BooleanFieldEditor.DEFAULT, composite_3);
            addField(bfe);
            tabInfo._highlighUseDefualtFE.add(bfe);
            
            Composite composite_14 = new Composite(grpBackgroundColor, SWT.NONE);
            GridLayout gl_composite_14 = new GridLayout(1, false);
            gl_composite_14.marginLeft = 10;
            composite_14.setLayout(gl_composite_14);
            
            Composite composite_5 = new Composite(composite_14, SWT.NONE);
            cfe = new ColorFieldEditor(PreferencesConstants.preferencePath(pluginName) +
                                       PreferencesConstants.Highlights.getAttrPath(0, false) +
                                       PreferencesConstants.Highlights.Color, Messages.HighlightingBracketsPrefPage_Color, composite_5);
            addField(cfe);
            tabInfo._highlighColorFE.add(cfe);
            tabInfo._highlighColorFEparent.add(composite_5);
            
            Composite composite_15 = new Composite(composite_14, SWT.NONE);
            composite_15.setSize(230, 25);
            ComboFieldEditor cofe = new ComboFieldEditor(PreferencesConstants.preferencePath(tabInfo._name) +
                                                         PreferencesConstants.Highlights.getAttrPath(0, false) +
                                                         PreferencesConstants.Highlights.HighlightTypeAttr,
                                                         Messages.HighlightingBracketsPrefPage_BgStyle, 
                                                         new String[][]{{PreferencesConstants.Highlights.HighlightTypeValNone, Messages.HighlightingBracketsPrefPage_None}, 
                                                                        {PreferencesConstants.Highlights.HighlightTypeValSolid, Messages.HighlightingBracketsPrefPage_Solid},
                                                                        {PreferencesConstants.Highlights.HighlightTypeValOutline, Messages.HighlightingBracketsPrefPage_Outline}}, 
                                                         composite_15);
            addField(cofe);
            tabInfo._highlighStyleFE = cofe;
            tabInfo._highlighStyleFEparent = composite_15;
            
            Group grpAnnotation = new Group(grpHighlight, SWT.NONE);
            grpAnnotation.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
            grpAnnotation.setText(Messages.HighlightingBracketsPrefPage_grpAnnotation_text);
            grpAnnotation.setLayout(new GridLayout(1, false));
            
            Composite composite_19 = new Composite(grpAnnotation, SWT.NONE);
            composite_19.setLayout(new GridLayout(1, false));
            
            tabInfo._annotationComposite = composite_19;
            Composite composite_20 = new Composite(composite_19, SWT.NONE);
            bfe = new BooleanFieldEditor(PreferencesConstants.preferencePath(tabInfo._name) +
                                         PreferencesConstants.Annotations.Enable, 
                                         Messages.HighlightingBracketsPrefPage_enableAnnotation, BooleanFieldEditor.DEFAULT, composite_20);
            addField(bfe);
            
            Link link = new Link(composite_19, SWT.NONE);
            GridData gd_link = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
            gd_link.horizontalIndent = 10;
            link.setLayoutData(gd_link);
            link.setBounds(0, 0, 54, 17);
            link.setText(Messages.HighlightingBracketsPrefPage_annotationLink);
            link.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    PreferencesUtil.createPreferenceDialogOn(getShell(), "org.eclipse.ui.editors.preferencePages.Annotations", null, null); //$NON-NLS-1$
                }
            });
            
            Group grpSurroundingBrackets = new Group(composite, SWT.NONE);
            grpSurroundingBrackets.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            grpSurroundingBrackets.setText(Messages.HighlightingBracketsPrefPage_SurroundingBrkt);
            grpSurroundingBrackets.setLayout(new GridLayout(1, false));
            
            Composite composite_6 = new Composite(grpSurroundingBrackets, SWT.NONE);
            bfe = new BooleanFieldEditor(PreferencesConstants.preferencePath(pluginName) +
                                          PreferencesConstants.Surrounding.Enable, 
                                          Messages.HighlightingBracketsPrefPage_Enable, BooleanFieldEditor.DEFAULT, composite_6);
            addField(bfe);
            tabInfo._surroundingEnableFE = bfe;          
            
            Composite composite_7 = new Composite(grpSurroundingBrackets, SWT.NONE);
            GridLayout gl_composite_7 = new GridLayout(2, false);
            gl_composite_7.marginLeft = 10;
            composite_7.setLayout(gl_composite_7);
            tabInfo._surroundingComposite = composite_7;
            
            Composite composite_8 = new Composite(composite_7, SWT.NONE);
            GridLayout gl_composite_8 = new GridLayout(1, false);
            gl_composite_8.marginWidth = 10;
            composite_8.setLayout(gl_composite_8);
            
            Group grpPairsToShow = new Group(composite_8, SWT.NONE);
            grpPairsToShow.setText(Messages.HighlightingBracketsPrefPage_PairsToShow);
            grpPairsToShow.setLayout(new GridLayout(2, false));
            
            Composite composite_16 = new Composite(composite_7, SWT.NONE);
            composite_16.setLayout(new GridLayout(1, false));
            
            // If we want to re-enable design mode, we should comment out this field addition
            addField(new StringPartCheckBoxes(PreferencesConstants.preferencePath(pluginName) +
                                              PreferencesConstants.Surrounding.ShowBrackets, 
                                              grpPairsToShow, 
                                              element.getAttribute(ProcessorsRegistry.SUPPORTED_BRACKETS_ATTR)));           
            
            Composite composite_12 = new Composite(composite_16, SWT.NONE);
            composite_12.setLayout(new GridLayout(3, false));
            
            Composite composite_11 = new Composite(composite_12, SWT.NONE);
            SpinnerFieldEditor spinner = new SpinnerFieldEditor(PreferencesConstants.preferencePath(pluginName) +
                                                                PreferencesConstants.Surrounding.NumBracketsToShow, 
                                                                Messages.HighlightingBracketsPrefPage_ShowUpTo, composite_11);
            addField(spinner);
            spinner.getSpinner().setMinimum(1);
            spinner.getSpinner().setMaximum(PreferencesConstants.MAX_PAIRS);
            
            Label lblNewLabel = new Label(composite_12, SWT.NONE);
            lblNewLabel.setAlignment(SWT.RIGHT);
            lblNewLabel.setText(Messages.HighlightingBracketsPrefPage_Pairs);
            
            
            
            Composite composite_17 = new Composite(composite_16, SWT.NONE);
            composite_17.setLayout(new GridLayout(3, false));
            
            Composite composite_18 = new Composite(composite_17, SWT.NONE);
            spinner = new SpinnerFieldEditor(PreferencesConstants.preferencePath(pluginName) +
                                             PreferencesConstants.Surrounding.MinDistanceBetweenBrackets, 
                                             Messages.HighlightingBracketsPrefPage_ShowPairsWhichAreAtLeast, composite_18);
            addField(spinner);
            lblNewLabel = new Label(composite_17, SWT.NONE);
            lblNewLabel.setAlignment(SWT.RIGHT);
            lblNewLabel.setText(Messages.HighlightingBracketsPrefPage_charsApart);

            
            
            Group grpHovering = new Group(composite, SWT.NONE);
            grpHovering.setLayout(new GridLayout(1, false));
            grpHovering.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            grpHovering.setText(Messages.HighlightingBracketsPrefPage_Hover);
            
            Composite composite_9 = new Composite(grpHovering, SWT.NONE);
            addField(new BooleanFieldEditor(PreferencesConstants.preferencePath(pluginName) +
                                            PreferencesConstants.Hovering.Enable, Messages.HighlightingBracketsPrefPage_ShowHoveredPairs, 
                                            BooleanFieldEditor.DEFAULT, composite_9));
            
            Composite composite_21 = new Composite(grpHovering, SWT.NONE);
            bfe = new BooleanFieldEditor(PreferencesConstants.preferencePath(pluginName) +
                                         PreferencesConstants.Hovering.PopupEnable, 
                                         Messages.HighlightingBracketsPrefPage_ShowPopup, 
                                         BooleanFieldEditor.DEFAULT, composite_21);
            addField(bfe);
            tabInfo._popupEn = bfe;
            
            Composite composite_22 = new Composite(grpHovering, SWT.NONE);
            GridData gd_composite_22 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
            gd_composite_22.horizontalIndent = 20;
            composite_22.setLayoutData(gd_composite_22);
            bfe = new BooleanFieldEditor(PreferencesConstants.preferencePath(pluginName) +
                                         PreferencesConstants.Hovering.PopupOnlyWithoutHint, 
                                         Messages.HighlightingBracketsPrefPage_PopupOnlyWithoutHint, 
                                         BooleanFieldEditor.DEFAULT, composite_22);
            addField(bfe);
            tabInfo._popupWithoutHint = composite_22;
        }
        
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), "com.choockapp.org.bracketeer.highlight_pref"); //$NON-NLS-1$
        return container;
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
        String attr;
        
        for (TabInfo tabInfo : _tabInfos)
        {
            // (idx 0 has already been added in the objects constructor)
            for( int idx = 1; idx < PreferencesConstants.MAX_PAIRS+2; idx++)
            {
                for (int i = 0; i < 2; i++)
                {
                    attr = PreferencesConstants.preferencePath(tabInfo._name) +
                            PreferencesConstants.Highlights.getAttrPath(idx, i == 0) +
                            PreferencesConstants.Highlights.UseDefault;

                    _prefNames.add(attr);
                    
                    attr = PreferencesConstants.preferencePath(tabInfo._name) +
                            PreferencesConstants.Highlights.getAttrPath(idx, i == 0) +
                            PreferencesConstants.Highlights.Color;

                    _prefNames.add(attr);
                }
                
                attr = PreferencesConstants.preferencePath(tabInfo._name) +
                        PreferencesConstants.Highlights.getAttrPath(idx, false) +
                        PreferencesConstants.Highlights.HighlightTypeAttr;

                _prefNames.add(attr);
            }
        }        
        
        super.initialize();
        updateAll();
    }
    
    @Override
    protected void updateAll()
    {
        updateHihglightFieldEditors();
        updateSurroundingEnable();
    }
    
    private void updateHihglightFieldEditors()
    {
        for (TabInfo tabInfo : _tabInfos)
        {
            int idx = tabInfo._highlighList.getSelectionIndex();
            for (int i = 0; i < 2; i++)
            {
                tabInfo._highlighUseDefualtFE.get(i).store();
                tabInfo._highlighUseDefualtFE.get(i).setPreferenceName(PreferencesConstants.preferencePath(tabInfo._name) +
                                                                       PreferencesConstants.Highlights.getAttrPath(idx, i == 0) +
                                                                       PreferencesConstants.Highlights.UseDefault );
                if( idx == 0 )
                    tabInfo._highlighUseDefualtFE.get(i).setLabelText(Messages.HighlightingBracketsPrefPage_UseSysDef);
                else
                    tabInfo._highlighUseDefualtFE.get(i).setLabelText(Messages.HighlightingBracketsPrefPage_UseDef);
                
                tabInfo._highlighUseDefualtFE.get(i).load();
                
                tabInfo._highlighColorFE.get(i).store();
                tabInfo._highlighColorFE.get(i).setPreferenceName(PreferencesConstants.preferencePath(tabInfo._name) +
                                                                  PreferencesConstants.Highlights.getAttrPath(idx, i == 0) +
                                                                  PreferencesConstants.Highlights.Color );
                tabInfo._highlighColorFE.get(i).load();
                
                tabInfo._highlighColorFE.get(i).setEnabled(!tabInfo._highlighUseDefualtFE.get(i).getBooleanValue(), 
                                                           tabInfo._highlighColorFEparent.get(i));
            }
            
            tabInfo._highlighStyleFE.store();
            tabInfo._highlighStyleFE.setPreferenceName(PreferencesConstants.preferencePath(tabInfo._name) +
                                                       PreferencesConstants.Highlights.getAttrPath(idx, false) +
                                                       PreferencesConstants.Highlights.HighlightTypeAttr);
            tabInfo._highlighStyleFE.load();
            tabInfo._highlighStyleFE.setEnabled(!tabInfo._highlighUseDefualtFE.get(1).getBooleanValue(), 
                                                tabInfo._highlighStyleFEparent);
            
            setEnable(tabInfo._annotationComposite, idx == 5);
            
            setEnable(tabInfo._popupWithoutHint, tabInfo._popupEn.getBooleanValue());
            
        }
    }
    
    private void updateSurroundingEnable()
    {
        for (TabInfo tabInfo : _tabInfos)
        {
            setEnable(tabInfo._surroundingComposite, 
                      tabInfo._surroundingEnableFE.getBooleanValue());
        }
    }  
    
    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        super.propertyChange(event);
        for (TabInfo tabInfo : _tabInfos)
        {
            if( event.getSource() == tabInfo._surroundingEnableFE )
                updateSurroundingEnable();
            
            for( BooleanFieldEditor bfe : tabInfo._highlighUseDefualtFE )            
                if( event.getSource() == bfe )
                    updateHihglightFieldEditors();
            
            if( event.getSource() == tabInfo._popupEn )
                updateHihglightFieldEditors();
        }
    }
}
