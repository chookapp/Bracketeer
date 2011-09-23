package com.chookapp.org.bracketeer.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.services.IDisposable;

import com.chookapp.org.bracketeer.Activator;
import com.chookapp.org.bracketeer.common.BracketeerProcessingContainer.MapObjectContainer;

public class BracketeerProcessingContainer implements IDisposable
{
    class MapObjectContainer<T>
    {
        private T _object;
        private boolean _toDelete;
        
        public MapObjectContainer(T obj)
        {
            _object = obj;
        }
        
        public T getObject()
        {
            return _object;
        }
        
        public boolean isToDelete()
        {
            return _toDelete;
        }

        public void setToDelete(boolean toDelete)
        {
            _toDelete = toDelete;
        }
    }    
 
        
    private IDocument _doc;
    
    private List<SingleBracket> _singleBrackets;
    private TreeMap<SortedPosition, MapObjectContainer<BracketsPair>> _bracketsPairMap;
    
    private String _positionCategory;
    private DefaultPositionUpdater _positionUpdater;
    
    public BracketeerProcessingContainer(IDocument doc)
    {
        _singleBrackets = new ArrayList<SingleBracket>();
        _bracketsPairMap = new TreeMap<SortedPosition, MapObjectContainer<BracketsPair>>();
        
        _doc = doc;
        
        _positionCategory = "bracketeerPosition";
        
        _doc.addPositionCategory(_positionCategory);
        _positionUpdater = new DefaultPositionUpdater(_positionCategory);
        _doc.addPositionUpdater(_positionUpdater);        
    }
    
    @Override
    public void dispose()
    {
        _doc.removePositionUpdater(_positionUpdater);
        try
        {
            _doc.removePositionCategory(_positionCategory);
        }
        catch (BadPositionCategoryException e)
        {
            Activator.log(e);
        }            
    }
    
    public void add(BracketsPair pair)
    {
        MapObjectContainer<BracketsPair> existing = 
                _bracketsPairMap.get(pair.getBrackets().get(0).getPosition());
        
        if( existing != null && existing.getObject().equals(pair) )
        {
            Assert.isTrue(existing.isToDelete());
            existing.setToDelete(false);
            return;
        }
        
        MapObjectContainer<BracketsPair> pairContainer =
                new MapObjectContainer<BracketsPair>(pair);
        
        for (SingleBracket bracket : pair.getBrackets())
        {
            try
            {
                _doc.addPosition(_positionCategory, bracket.getPosition());
            }
            catch (Exception e)
            {
                Activator.log(e);
            }
            
            SortedPosition spos = bracket.getPosition();
            existing = _bracketsPairMap.get(spos);
            if( existing != null )
            {
                Assert.isTrue(existing.isToDelete());
                delete(existing);
            }
            
            spos.setContainer(_bracketsPairMap);
            existing = _bracketsPairMap.put(spos, pairContainer);
            Assert.isTrue(existing == null);
        }
    }
    
    private void delete(SortedPosition position)
    {
        try
        {
            position.setContainer(null);
            _doc.removePosition(_positionCategory, position);
        }
        catch (BadPositionCategoryException e)
        {
            Activator.log(e);
        }
    }
    
    private void delete(MapObjectContainer<BracketsPair> mapObj)
    {        
        for (SingleBracket bracket : mapObj.getObject().getBrackets())
        {
            SortedPosition spos = bracket.getPosition();
            MapObjectContainer<BracketsPair> existing = _bracketsPairMap.remove(spos);
            Assert.isTrue(mapObj.getObject().equals(existing.getObject()));
            delete(bracket.getPosition());            
        }
    }
    
    static <T> List<T> mapObjListToObjList(Collection<MapObjectContainer<T>> vals)
    {
        List<T> retVal = new LinkedList<T>();
        for (MapObjectContainer<T> mapObj : vals)
        {
            if( !retVal.contains(mapObj.getObject()) && !mapObj.isToDelete() )
                retVal.add(mapObj.getObject());
        }
        return retVal;
    }

    public void add(SingleBracket bracket)
    {
        _singleBrackets.add(bracket);
    }    
        
    //public final Collection<BracketsPair> getBracketsPairs() { return _bracketsPairMap.values(); }
    public final List<SingleBracket> getSingleBrackets() { return _singleBrackets; }

    public BracketsPair getMatchingPair(int offset)
    {
        // TODO: but what if the position is updated when we access it?
        MapObjectContainer<BracketsPair> mapObj = _bracketsPairMap.get(new SortedPosition(offset, 1));
        if(mapObj == null || mapObj.isToDelete())
            return null;
        
        return mapObj.getObject();
    }

    public void markAllToBeDeleted()
    {
        for (MapObjectContainer<BracketsPair> mapObj : _bracketsPairMap.values())
        {
            mapObj.setToDelete(true);
        }
    }

    public void deleteAllMarked()
    {
        Iterator<Entry<SortedPosition, MapObjectContainer<BracketsPair>>> it =
                _bracketsPairMap.entrySet().iterator();
        while(it.hasNext())
        {
            Entry<SortedPosition, MapObjectContainer<BracketsPair>> entry =
                    it.next();
            if(entry.getValue().isToDelete())
            {
                delete(entry.getKey());
                it.remove();
            }
        }
    }

    public List<BracketsPair> getPairsSurrounding(int offset, int count)
    {
        SortedPosition startPos = new SortedPosition(offset,0);
        Collection<MapObjectContainer<BracketsPair>> pairs = 
                _bracketsPairMap.tailMap(startPos, false).values();
        List<BracketsPair> retVal = new LinkedList<BracketsPair>();
        
        for (MapObjectContainer<BracketsPair> mapObj : pairs)
        {
            if( mapObj.isToDelete() )
                continue;
            
            BracketsPair pair = mapObj.getObject();
            
            SingleBracket opBr = pair.getOpeningBracket();
            SingleBracket clBr = pair.getClosingBracket();
            
            if( opBr.getPosition().offset <= offset )
            {
                Assert.isTrue(clBr.getPosition().offset > offset);
                if( !retVal.contains(pair) )
                {
                    retVal.add(pair);
                    if(retVal.size() == count)
                        break;
                }
            }
        }
        
        return retVal;
    }

   
    public List<BracketsPair> getMatchingPairs(int startOfset, int endOfset)
    {
        SortedPosition startPos = new SortedPosition(startOfset,0);
        SortedPosition endPos = new SortedPosition(endOfset,0);
        Collection<MapObjectContainer<BracketsPair>> vals = 
                _bracketsPairMap.subMap(startPos, true, endPos, true).values();
        
        return mapObjListToObjList(vals);
    }

    
}
