package com.chookapp.org.bracketeer.common;

import java.util.ArrayList;
import java.util.Collection;
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

public class BracketeerProcessingContainer implements IDisposable
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
    
    private String _positionCategory;
    private IPositionUpdater _positionUpdater;
    
    public BracketeerProcessingContainer(IDocument doc)
    {
        _singleBrackets = new ArrayList<ObjectContainer<SingleBracket>>();
        _bracketsPairList = new LinkedList<ObjectContainer<BracketsPair>>();
        
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
        synchronized(_bracketsPairList)
        {
            ObjectContainer<BracketsPair> existing = 
                    findExistingObj(_bracketsPairList, pair);
            
            if( existing != null )
            {
                Assert.isTrue(existing.isToDelete());
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
            
            ObjectContainer<BracketsPair> pairContainer =
                    new ObjectContainer<BracketsPair>(pair);
            
            _bracketsPairList.add(pairContainer);
            for (SingleBracket br : pair.getBrackets())
            {
                addPosition(br.getPosition());
            }
        }
    }
    
    private void addPosition(Position position)
    {
        try
        {
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
                delete(bracket.getPosition());
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
            delete(bracket.getPosition());
        }
    }
    
    static <T> List<T> mapObjListToObjList(Collection<ObjectContainer<T>> vals)
    {
        List<T> retVal = new LinkedList<T>();
        for (ObjectContainer<T> mapObj : vals)
        {
            if( !retVal.contains(mapObj.getObject()) && !mapObj.isToDelete() )
                retVal.add(mapObj.getObject());
        }
        return retVal;
    }

    public void add(SingleBracket bracket)
    {
        synchronized(_singleBrackets)
        {
            ObjectContainer<SingleBracket> existing = 
                    findExistingObj(_singleBrackets, bracket);
            
            if( existing != null )
            {
                Assert.isTrue(existing.isToDelete());
                if(  existing.equals(bracket) && !existing.getObject().getPosition().isDeleted )
                {
                    existing.setToDelete(false);
                    return;
                }
                else
                {
                    deleteSingle(existing);
                }
            }
            
            _singleBrackets.add(new ObjectContainer<SingleBracket>(bracket));
            
            addPosition(bracket.getPosition());
        }
    }    
    
    public BracketsPair getMatchingPair(int offset)
    {
        synchronized(_bracketsPairList)
        {
            for (ObjectContainer<BracketsPair> objCont : _bracketsPairList)
            {
                if(!objCont.isToDelete() && 
                   objCont.getObject().getBracketAt(offset) != null )
                {
                    return objCont.getObject();
                }
            }
        }
        return null;
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
                    for (SingleBracket bracket : objCont.getObject().getBrackets())
                    {
                        delete(bracket.getPosition());
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
                    delete(objCont.getObject().getPosition());
                    it.remove();
                }
            }
        }
    }

    public List<BracketsPair> getPairsSurrounding(int offset, int count)
    {
        List<BracketsPair> retVal = new LinkedList<BracketsPair>();
        
        synchronized(_bracketsPairList)
        {
            for (ObjectContainer<BracketsPair> objCont : _bracketsPairList)
            {
                if( objCont.isToDelete() )
                    continue;
                
                BracketsPair pair = objCont.getObject();
                if( pair.hasDeletedPosition() )
                    continue;            
                
                SingleBracket opBr = pair.getOpeningBracket();
                SingleBracket clBr = pair.getClosingBracket();
                
                if( (opBr.getPosition().offset <= offset) &&
                    (clBr.getPosition().offset > offset) )
                {
                    if( !retVal.contains(pair) )
                    {
                        retVal.add(pair);
                        if(retVal.size() == count)
                            break;
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
                if( objCont.isToDelete() )
                    continue;
                
                BracketsPair pair = objCont.getObject();
                
                for (SingleBracket br : pair.getBrackets())
                {
                    Position pos = br.getPosition();
                    if(!pos.isDeleted && pos.overlapsWith(startOffset, length) &&
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
                
                if( !objCont.isToDelete() && !br.getPosition().isDeleted )
                    ret.add(br);
            }
        }
        return ret;
    }

    
}
