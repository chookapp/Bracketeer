package com.chookapp.org.bracketeer.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import com.chookapp.org.bracketeer.Activator;
import com.chookapp.org.bracketeer.common.IHintConfiguration;
import com.chookapp.org.bracketeer.preferences.PreferencesConstants;

public class ProcessorConfiguration implements IPropertyChangeListener
{
    

    public class PairConfiguration
    {
        private RGB[] _fgColors;
        private RGB[] _bgColors;
        
        private int _surroundingPairsCount;
        private boolean _surroundingPairsEnable;
        private String _surroundingPairsToInclude;
        
        private boolean _hoveredPairsEnable;
        
        public PairConfiguration()
        {
            _fgColors = new RGB[PreferencesConstants.MAX_PAIRS];
            _bgColors = new RGB[PreferencesConstants.MAX_PAIRS];
        }

        /* setters */
        
        public void setColor(boolean foregound, int colorIndex, RGB color)
        {
            if(foregound)
                _fgColors[colorIndex] = color;
            else
                _bgColors[colorIndex] = color;
        }
        
        public void setEnableSurrounding(boolean enable)
        {
            _surroundingPairsEnable = enable;
        }

        public void setEnableHovering(boolean enable)
        {
            _hoveredPairsEnable = enable;
        }
        
        public void setSurroundingPairsCount(int count)
        {
            _surroundingPairsCount = count;
        }
        
        public void setSurroundingPairsToInclude(String pairs)
        {
            _surroundingPairsToInclude = pairs;
        }
        
        /* getters */
                
        public RGB getColor(boolean foregound, int colorIndex)
        {
            if(foregound)
                return _fgColors[colorIndex];
            else
                return _bgColors[colorIndex];
        }

        public boolean isSurroundingPairsEnabled()
        {
            return _surroundingPairsEnable;
        }
        
        public int getSurroundingPairsCount()
        {
            return _surroundingPairsCount;
        }
        
        public String getSurroundingPairsToInclude()
        {
            return _surroundingPairsToInclude;
        }

        public boolean isHoveredPairsEnabled()
        {
            return _hoveredPairsEnable;
        }

   
        
    }
    
    public class SingleBracketConfiguration
    {
        private RGB _fgColor;
        private RGB _bgColor;
        
        public SingleBracketConfiguration()
        {
        }
        
        /* setters */
        
        public void setColor(boolean foregound, RGB color)
        {
            if(foregound)
                _fgColor = color;
            else
                _bgColor = color;
        }
        
        /* getters */
        
        public RGB getColor(boolean foregound)
        {
            if(foregound)
                return _fgColor;
            else
                return _bgColor;
        }
    }
    
    public class HintConfiguration implements IHintConfiguration
    {

        
        private HashMap<String, HashMap<String, String>> _attrMaps;
        
        public HintConfiguration()
        {
            _attrMaps = new HashMap<String, HashMap<String,String>>();
        }

        public boolean getBoolAttr(String type, String attr)
        {
            return Boolean.parseBoolean(getAttr(type,attr));
        }
        
        public void setAttr(String type, String attr, boolean val)
        {
            setAttr(type, attr, Boolean.toString(val));
        }
        
        public String getAttr(String type, String attr)
        {
            HashMap<String, String> attrMap = _attrMaps.get(type);
            if( attrMap == null )
                return null;
            return attrMap.get(attr);
        }
        
        public void setAttr(String type, String attr, String val)
        {
            HashMap<String, String> attrMap = _attrMaps.get(type);
            if( attrMap == null )
            {
                attrMap = new HashMap<String, String>();
                _attrMaps.put(type, attrMap);
            }
            
            attrMap.put(attr, val);
        }

        public boolean isEnabled(String type)
        {
            return getBoolAttr(type, PreferencesConstants.Hints.ENABLED);
        }
        
//        public void setEnbaled(String type, boolean en)
//        {
//            setAttr(type, PreferencesConstants.Hints.ENABLED, en);
//        }
        
        public RGB getColor(String type, boolean foreground)
        {
            String str = getAttr(type, foreground ? PreferencesConstants.Hints.FG_COLOR :
                                                    PreferencesConstants.Hints.BG_COLOR );
            if( str == null )
                return null;
            
            return StringConverter.asRGB(str);
        }
        
//        public void setColor(String type, boolean foreground, RGB color)
//        {
//            setAttr(type, foreground ? PreferencesConstants.Hints.FG_COLOR :
//                                       PreferencesConstants.Hints.BG_COLOR, 
//                    StringConverter.asString(color));
//        }
    }
    
    private PairConfiguration _pairConf;
    private SingleBracketConfiguration _singleConf;
    private HintConfiguration _hintConf;
    private String _name;
    
    private IPreferenceStore _prefStore;
    
    private List<IProcessorConfigurationListener> _listeners;
    private List<String> _hintTypes;
    
    public ProcessorConfiguration(IConfigurationElement confElement)
    {
        _pairConf = new PairConfiguration();
        _singleConf = new SingleBracketConfiguration();
        _hintConf = new HintConfiguration();
        
        _hintTypes = new ArrayList<String>();
        
        _name = confElement.getAttribute("name");
        IConfigurationElement[] hints = confElement.getChildren("Hint");
        for (IConfigurationElement hint : hints)
        {
            _hintTypes.add(hint.getAttribute("type"));            
        }
        
        List<IPreferenceStore> stores= new ArrayList<IPreferenceStore>();        
        stores.add(Activator.getDefault().getPreferenceStore());
        
        _prefStore = new ChainedPreferenceStore(stores.toArray(new IPreferenceStore[stores.size()]));
        _prefStore.addPropertyChangeListener(this);

        _listeners = new LinkedList<IProcessorConfigurationListener>();
        
        updateConfiguartion();
    }
    
    public String getName()
    {
        return _name;
    }
    
    public PairConfiguration getPairConfiguration()
    {
        return _pairConf;
    }    
    
    public SingleBracketConfiguration getSingleBracketConfiguration()
    {
        return _singleConf;
    }
    
    public HintConfiguration getHintConfiguration()
    {
        return _hintConf; 
    }
    
    private void updateConfiguartion()
    {
        updateHighlightConf();
        updateHintConf();
        
        /* notify listeners */
        
        for( IProcessorConfigurationListener listener : _listeners )
        {
            listener.configurationUpdated();
        }
    }

    private void updateHintConf()
    {
        List<String> defaultAttrs = new ArrayList<String>();
        defaultAttrs.add(PreferencesConstants.Hints.FG_COLOR);
        defaultAttrs.add(PreferencesConstants.Hints.BG_COLOR);
        defaultAttrs.add(PreferencesConstants.Hints.ENABLED);
        
        for (String hintType : _hintTypes)
        {
            String prefBase = PreferencesConstants.preferencePath(_name) +
                    PreferencesConstants.Hints.preferencePath(hintType);
            
            for (String attr : defaultAttrs)
            {
                String val = _prefStore.getString(prefBase + attr);
                if( val == null || val.isEmpty() )
                    val = null;
                _hintConf.setAttr(hintType, attr, val);
            }
        }
    }

    private void updateHighlightConf()
    {
        RGB[] defColor = new RGB[2]; // 0 - BG, 1 - FG 

        defColor[0] = null;
        defColor[1] = null;
        
        String prefBase = PreferencesConstants.preferencePath(_name);

        for( int fgIndex = 0; fgIndex < 2; fgIndex++ )
        {
            boolean foregound = (fgIndex == 1);  

            /* default */ 
            
            if( !_prefStore.getBoolean( prefBase +
                                        PreferencesConstants.Highlights.getAttrPath(0, foregound) +
                                        PreferencesConstants.Highlights.UseDefault ) )
            {
                defColor[fgIndex] = PreferenceConverter.getColor(_prefStore, prefBase +
                                                                             PreferencesConstants.Highlights.getAttrPath(0, foregound) +
                                                                             PreferencesConstants.Highlights.Color );
            }
            
            /* pair */
            
            for (int pairIdx = 0; pairIdx < PreferencesConstants.MAX_PAIRS; pairIdx++)
            {
                if( _prefStore.getBoolean( prefBase +
                                           PreferencesConstants.Highlights.getAttrPath(pairIdx+1, foregound) +
                                           PreferencesConstants.Highlights.UseDefault ) )
                {
                    _pairConf.setColor(foregound, pairIdx, defColor[fgIndex]);
                }
                else
                {
                    _pairConf.setColor(foregound, pairIdx, 
                                       PreferenceConverter.getColor( _prefStore, prefBase +
                                                                                 PreferencesConstants.Highlights.getAttrPath(pairIdx+1, foregound) +
                                                                                 PreferencesConstants.Highlights.Color ) );
                }
            }
            
            _pairConf.setEnableSurrounding(_prefStore.getBoolean(prefBase +
                                                                 PreferencesConstants.Surrounding.Enable));
            _pairConf.setEnableHovering(_prefStore.getBoolean(prefBase +
                                                              PreferencesConstants.Hovering.Enable));
            _pairConf.setSurroundingPairsCount(_prefStore.getInt(prefBase +
                                                                 PreferencesConstants.Surrounding.NumBracketsToShow));
            _pairConf.setSurroundingPairsToInclude(_prefStore.getString(prefBase +
                                                                        PreferencesConstants.Surrounding.ShowBrackets));
            
            /* single */ 
            
            int pairIdx = PreferencesConstants.MAX_PAIRS + 1;
            
            if( _prefStore.getBoolean( prefBase +
                                       PreferencesConstants.Highlights.getAttrPath(pairIdx, foregound) +
                                       PreferencesConstants.Highlights.UseDefault ) )
            {
                _singleConf.setColor(foregound, defColor[fgIndex]);
            }
            else
            {
                _singleConf.setColor(foregound, 
                                     PreferenceConverter.getColor( _prefStore, prefBase +
                                                                               PreferencesConstants.Highlights.getAttrPath(pairIdx, foregound) +
                                                                               PreferencesConstants.Highlights.Color ) );
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        updateConfiguartion();
    }

    public void addListener(IProcessorConfigurationListener listener)
    {
        _listeners.add(listener);
    }
    
    public void removeListener(IProcessorConfigurationListener listener)
    {
        if( !_listeners.remove(listener) )
            Activator.log("listener was not found");
    }

  
}
