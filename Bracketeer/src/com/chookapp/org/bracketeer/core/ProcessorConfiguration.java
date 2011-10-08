package com.chookapp.org.bracketeer.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import com.chookapp.org.bracketeer.Activator;
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
    
    private PairConfiguration _pairConf;
    private SingleBracketConfiguration _singleConf;
    private String _name;
    
    private IPreferenceStore _prefStore;
    
    private List<IProcessorConfigurationListener> _listeners;
    
    public ProcessorConfiguration(IConfigurationElement confElement)
    {
        _pairConf = new PairConfiguration();
        _singleConf = new SingleBracketConfiguration();
        
        _name = confElement.getAttribute("name");
        
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
    
    private void updateConfiguartion()
    {
        RGB[] defColor = new RGB[2]; // 0 - BG, 1 - FG 

        defColor[0] = null;
        defColor[1] = null;

        for( int fgIndex = 0; fgIndex < 2; fgIndex++ )
        {
            boolean foregound = (fgIndex == 1);  

            /* default */ 
            
            if( !_prefStore.getBoolean( PreferencesConstants.preferencePath(_name) +
                                        PreferencesConstants.Highlights.getAttrPath(0, foregound) +
                                        PreferencesConstants.Highlights.UseDefault ) )
            {
                defColor[fgIndex] = PreferenceConverter.getColor(_prefStore, PreferencesConstants.preferencePath(_name) +
                                                                             PreferencesConstants.Highlights.getAttrPath(0, foregound) +
                                                                             PreferencesConstants.Highlights.Color );
            }
            
            /* pair */
            
            for (int pairIdx = 0; pairIdx < PreferencesConstants.MAX_PAIRS; pairIdx++)
            {
                if( _prefStore.getBoolean( PreferencesConstants.preferencePath(_name) +
                                           PreferencesConstants.Highlights.getAttrPath(pairIdx+1, foregound) +
                                           PreferencesConstants.Highlights.UseDefault ) )
                {
                    _pairConf.setColor(foregound, pairIdx, defColor[fgIndex]);
                }
                else
                {
                    _pairConf.setColor(foregound, pairIdx, 
                                       PreferenceConverter.getColor( _prefStore, PreferencesConstants.preferencePath(_name) +
                                                                                 PreferencesConstants.Highlights.getAttrPath(pairIdx+1, foregound) +
                                                                                 PreferencesConstants.Highlights.Color ) );
                }
            }
            
            _pairConf.setEnableSurrounding(_prefStore.getBoolean(PreferencesConstants.preferencePath(_name) +
                                                                 PreferencesConstants.Surrounding.Enable));
            _pairConf.setEnableHovering(_prefStore.getBoolean(PreferencesConstants.preferencePath(_name) +
                                                              PreferencesConstants.Hovering.Enable));
            _pairConf.setSurroundingPairsCount(_prefStore.getInt(PreferencesConstants.preferencePath(_name) +
                                                                 PreferencesConstants.Surrounding.NumBracketsToShow));
            _pairConf.setSurroundingPairsToInclude(_prefStore.getString(PreferencesConstants.preferencePath(_name) +
                                                                        PreferencesConstants.Surrounding.ShowBrackets));
            
            /* single */ 
            
            int pairIdx = PreferencesConstants.MAX_PAIRS + 1;
            
            if( _prefStore.getBoolean( PreferencesConstants.preferencePath(_name) +
                                       PreferencesConstants.Highlights.getAttrPath(pairIdx, foregound) +
                                       PreferencesConstants.Highlights.UseDefault ) )
            {
                _singleConf.setColor(foregound, defColor[fgIndex]);
            }
            else
            {
                _singleConf.setColor(foregound, 
                                     PreferenceConverter.getColor( _prefStore, PreferencesConstants.preferencePath(_name) +
                                                                               PreferencesConstants.Highlights.getAttrPath(pairIdx, foregound) +
                                                                               PreferencesConstants.Highlights.Color ) );
            }
        }
        
        /* notify listeners */
        
        for( IProcessorConfigurationListener listener : _listeners )
        {
            listener.configurationUpdated();
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
