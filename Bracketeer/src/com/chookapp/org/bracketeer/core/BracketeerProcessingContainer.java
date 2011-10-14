package com.chookapp.org.bracketeer.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.services.IDisposable;

import com.chookapp.org.bracketeer.Activator;
import com.chookapp.org.bracketeer.common.BracketsPair;
import com.chookapp.org.bracketeer.common.Hint;
import com.chookapp.org.bracketeer.common.IBracketeerProcessingContainer;
import com.chookapp.org.bracketeer.common.SingleBracket;

public class BracketeerProcessingContainer implements IDisposable, IBracketeerProcessingContainer
{
    private class ObjectContainer<T>
    {
        private T _object;
        private boolean _toDelete;
        
        public ObjectContainer(T obj)
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
        
        @Override
        public boolean equals(Object other)
        {
            if( other == null )
                return false;
            
            if( other instanceof ObjectContainer<?> )
            {
                return _object.equals(((ObjectContainer<?>) other)._object);
            }
            
            return _object.equals(other);            
        }
    }    
 
        
    private IDocument _doc;
    
    private List<ObjectContainer<SingleBracket>> _singleBrackets;
    private List<ObjectContainer<BracketsPair>> _bracketsPairList;
    private List<ObjectContainer<Hint>> _hints;
    
    private String _positionCategory;
    private IPositionUpdater _positionUpdater;
    private List<IProcessingContainerListener> _listeners;

    private boolean _bracketsPairsTouched;
    private boolean _singleBracketsTouched;
    private boolean _hintsTouched;
    private boolean _updatingListeners;
    
    public BracketeerProcessingContainer(IDocument doc)
    {
        _singleBrackets = new ArrayList<ObjectContainer<SingleBracket>>();
        _bracketsPairList = new LinkedList<ObjectContainer<BracketsPair>>();
        _hints = new LinkedList<ObjectContainer<Hint>>();
        
        _doc = doc;
        
        _positionCategory = "bracketeerPosition";
        
        _doc.addPositionCategory(_positionCategory);
        _positionUpdater = new DefaultPositionUpdater(_positionCategory);
        _doc.addPositionUpdater(_positionUpdater);
        
        _listeners = new LinkedList<IProcessingContainerListener>();
        
        _updatingListeners = false;
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

    public void addListener(IProcessingContainerListener listener)
    {
        _listeners.add(listener);
    }
    
    public void removeListener(IProcessingContainerListener listener)
    {
        if( !_listeners.remove(listener) )
            Activator.log("listener was not found");        
    }
    
    public BracketsPair getMatchingPair(int openOffset, int closeOffset)
    {
        synchronized(_bracketsPairList)
        {
            for (ObjectContainer<BracketsPair> objCont : _bracketsPairList)
            {
                if(objCont.isToDelete())
                    continue;
                
                BracketsPair pair = objCont.getObject();
                Position openPos = pair.getOpeningBracket().getPosition();
                if(openPos != null && openPos.getOffset() == openOffset)
                {
                    Position closePos = pair.getClosingBracket().getPosition();
                    if(closePos != null && closePos.getOffset() == closeOffset )
                        return pair;
                    
                    Activator.log(String.format("[%1$d,%2$d] paritally found - %3$s", 
                                                openOffset, closeOffset, pair.toString()));
                    return null;
                }
            }
        }
        return null;
    }
    
    public List<BracketsPair> getPairsSurrounding(int offset)
    {
        List<BracketsPair> retVal = new LinkedList<BracketsPair>();
        
        synchronized(_bracketsPairList)
        {
            for (ObjectContainer<BracketsPair> objCont : _bracketsPairList)
            {
                BracketsPair pair = objCont.getObject();
                
                Position opBrPos = pair.getOpeningBracket().getPosition();
                Position clBrPos = pair.getClosingBracket().getPosition();
                if( (opBrPos == null) || (clBrPos == null) )
                    continue;
                
                if( (opBrPos.offset <= offset) && (clBrPos.offset > offset) )
                {
                    if( !retVal.contains(pair) )
                    {
                        retVal.add(pair);                     
                    }
                }
            }
        }
        
        return retVal;
    }

   
    public List<BracketsPair> getMatchingPairs(int startOffset, int length)
    {
        List<BracketsPair> retVal = new LinkedList<BracketsPair>();
        synchronized(_bracketsPairList)
        {
            for (ObjectContainer<BracketsPair> objCont : _bracketsPairList)
            {
                BracketsPair pair = objCont.getObject();
                
                for (SingleBracket br : pair.getBrackets())
                {
                    Position pos = br.getPosition();
                    if(pos != null && pos.overlapsWith(startOffset, length) &&
                       !retVal.contains(pair) )
                    {
                        retVal.add(pair);
                        break;
                    }
                }
            }
        }
        return retVal;        
    }

    public List<SingleBracket> getSingleBrackets()
    {
        List<SingleBracket> ret = new LinkedList<SingleBracket>();
        synchronized(_singleBrackets)
        {
            for (ObjectContainer<SingleBracket> objCont : _singleBrackets)
            {
                SingleBracket br = objCont.getObject();
                
                if( br.getPosition() != null )
                    ret.add(br);
            }
        }
        return ret;
    }
    
    public List<Hint> getHints()
    {
        List<Hint> ret = new LinkedList<Hint>();
        synchronized(_hints)
        {
            for (ObjectContainer<Hint> objCont : _hints)
            {
                Hint hint = objCont.getObject();
                
                if( !hint.hasDeletedPosition() )
                    ret.add(hint);
            }
        }
        return ret;
    }
    
    public List<BracketsPair> getBracketPairs()
    {
        List<BracketsPair> ret = new LinkedList<BracketsPair>();
        synchronized(_bracketsPairList)
        {
            for (ObjectContainer<BracketsPair> objCont : _bracketsPairList)
            {
                BracketsPair pair = objCont.getObject();
                if( !pair.hasDeletedPosition() )
                    ret.add(pair);
            }
        }
        return ret;
    }
    
    public void markAllToBeDeleted()
    {
        synchronized(_bracketsPairList)
        {
            for (ObjectContainer<BracketsPair> objCont : _bracketsPairList)
            {
                objCont.setToDelete(true);
            }
        }        
        
        synchronized (_singleBrackets)
        {
            for (ObjectContainer<SingleBracket> objCont : _singleBrackets)
            {
                objCont.setToDelete(true);
            }            
        }        
        
        synchronized (_hints)
        {
            for (ObjectContainer<Hint> objCont : _hints)
            {
                objCont.setToDelete(true);
            }
        }        
        
    }

    public void deleteAllMarked()
    {
        synchronized(_bracketsPairList)
        {
            Iterator<ObjectContainer<BracketsPair>> it = _bracketsPairList.iterator();
            while(it.hasNext())
            {
                ObjectContainer<BracketsPair> objCont = it.next();
                
                if( objCont.isToDelete() )
                {
                    _bracketsPairsTouched = true;
                    for (SingleBracket bracket : objCont.getObject().getBrackets())
                    {
                        delete(bracket.getPositionRaw());
                    }
                    it.remove();
                }
            }
        }
        
        synchronized (_singleBrackets)
        {
            Iterator<ObjectContainer<SingleBracket>> it = _singleBrackets.iterator();
            while(it.hasNext())
            {
                ObjectContainer<SingleBracket> objCont = it.next();
                
                if( objCont.isToDelete() )
                {
                    _singleBracketsTouched = true;
                    delete(objCont.getObject().getPositionRaw());
                    it.remove();
                }
            }
        }
        
        synchronized (_hints)
        {
            Iterator<ObjectContainer<Hint>> it = _hints.iterator();
            while(it.hasNext())
            {
                ObjectContainer<Hint> objCont = it.next();
                
                if( objCont.isToDelete() )
                {
                    _hintsTouched = true;
                    delete(objCont.getObject().getOriginPositionRaw());
                    delete(objCont.getObject().getHintPositionRaw());
                    it.remove();
                }
            }
        }
        
        if(Activator.DEBUG)
        {
            try
            {
                Activator.trace("Positions tracked = " + _doc.getPositions(_positionCategory).length);
            }
            catch (BadPositionCategoryException e)
            {
                Activator.log(e);
            }
            Activator.trace("Pairs = " + _bracketsPairList.size());
            Activator.trace("Singles = " + _singleBrackets.size());
            Activator.trace("Hints = " + _hints.size());
        }
    }

    public void updateComplete()
    {
        _updatingListeners = true;
        
        for (IProcessingContainerListener listener : _listeners)
        {
            listener.containerUpdated(_bracketsPairsTouched, 
                                      _singleBracketsTouched,
                                      _hintsTouched);
        }
        
        _bracketsPairsTouched = false;
        _singleBracketsTouched = false;
        _hintsTouched = false;
        
        _updatingListeners = false;
    }
    
    public boolean isUpdatingListeners()
    {
        return _updatingListeners;
    }
    
    public void add(BracketsPair pair)
    {
        synchronized(_bracketsPairList)
        {
            ObjectContainer<BracketsPair> existing = 
                    findExistingObj(_bracketsPairList, pair);
            
            if( existing != null )
            {
                if(  existing.equals(pair) && !existing.getObject().hasDeletedPosition() )
                {
                    existing.setToDelete(false);
                    return;
                }
                else
                {
                    deletePair(existing);
                }
            }
            
            _bracketsPairsTouched = true;
            
            ObjectContainer<BracketsPair> pairContainer =
                    new ObjectContainer<BracketsPair>(pair);
            
            _bracketsPairList.add(pairContainer);
            for (SingleBracket br : pair.getBrackets())
            {
                addPosition(br.getPosition());
            }
        }
    }
    
    
    public void add(SingleBracket bracket)
    {
        synchronized(_singleBrackets)
        {
            ObjectContainer<SingleBracket> existing = 
                    findExistingObj(_singleBrackets, bracket);
            
            if( existing != null )
            {
                if( existing.equals(bracket) && existing.getObject().getPosition() != null )
                {
                    existing.setToDelete(false);
                    return;
                }
                else
                {
                    deleteSingle(existing);
                }
            }
            
            _singleBracketsTouched = true;
            
            _singleBrackets.add(new ObjectContainer<SingleBracket>(bracket));
            
            addPosition(bracket.getPosition());
        }
    }    
    
    public void add(Hint hint)
    {
        synchronized(_hints)
        {
            ObjectContainer<Hint> existing = 
                    findExistingObj(_hints, hint);
            
            if( existing != null )
            {
                if( existing.equals(hint) && !existing.getObject().hasDeletedPosition() )
                {
                    existing.setToDelete(false);
                    return;
                }
                else
                {
                    deleteHint(existing);
                }
            }
            
            _hintsTouched = true;
            
            _hints.add(new ObjectContainer<Hint>(hint));
            
            addPosition(hint.getHintPositionRaw());
            addPosition(hint.getOriginPositionRaw());
        }
    }    
    
    private void addPosition(Position position)
    {
        try
        {
            if( position != null )
                _doc.addPosition(_positionCategory, position);
        }
        catch (Exception e)
        {
            Activator.log(e);
        }
    }

    private static <T> ObjectContainer<T> findExistingObj(List<ObjectContainer<T>> objList,
                                                          T obj)
    {
        for (ObjectContainer<T> objCont : objList)
        {
            if(objCont.equals(obj))
                return objCont;
        }
        return null;
    }

    private void delete(Position position)
    {
        try
        {
            _doc.removePosition(_positionCategory, position);
        }
        catch (BadPositionCategoryException e)
        {
            Activator.log(e);
        }
    }
    
    private void deletePair(ObjectContainer<BracketsPair> objCont)
    {   
        synchronized(_bracketsPairList)
        {
            boolean found = _bracketsPairList.remove(objCont);
            Assert.isTrue(found);        
            
            for (SingleBracket bracket : objCont.getObject().getBrackets())
            {
                delete(bracket.getPositionRaw());
            }
        }
    }
    
    private void deleteSingle(ObjectContainer<SingleBracket> objCont)
    {   
        synchronized(_singleBrackets)
        {
            boolean found = _singleBrackets.remove(objCont);
            Assert.isTrue(found);        
            
            SingleBracket bracket = objCont.getObject();
            delete(bracket.getPositionRaw());
        }
    }
    
    private void deleteHint(ObjectContainer<Hint> objCont)
    {   
        synchronized(_hints)
        {
            boolean found = _hints.remove(objCont);
            Assert.isTrue(found);        
            
            Hint hint = objCont.getObject();
            delete(hint.getOriginPositionRaw());
            delete(hint.getHintPositionRaw());
        }
    }
    
    /*
    private static <T> List<T> mapObjListToObjList(Collection<ObjectContainer<T>> vals)
    {
        List<T> retVal = new LinkedList<T>();
        for (ObjectContainer<T> mapObj : vals)
        {
            if( !retVal.contains(mapObj.getObject()) && !mapObj.isToDelete() )
                retVal.add(mapObj.getObject());
        }
        return retVal;
    }
    */
    

    
}
