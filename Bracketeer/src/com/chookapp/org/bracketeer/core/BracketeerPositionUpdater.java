package com.chookapp.org.bracketeer.core;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.Position;

import com.chookapp.org.bracketeer.common.SortedPosition;


public class BracketeerPositionUpdater extends DefaultPositionUpdater
{
    public BracketeerPositionUpdater(String category)
    {
        super(category);
    }

    @Override  
    public void update(DocumentEvent event) {

        try {


            fOffset= event.getOffset();
            fLength= event.getLength();
            fReplaceLength= (event.getText() == null ? 0 : event.getText().length());
            fDocument= event.getDocument();

            Position[] category= fDocument.getPositions(getCategory());
            for (int i= 0; i < category.length; i++) {

                fPosition= new Position(category[i].offset, category[i].length);
                fOriginalPosition.offset= fPosition.offset;
                fOriginalPosition.length= fPosition.length;

                if (notDeleted())
                    adaptToReplace();
                
                if( fPosition.isDeleted )
                    category[i].delete();
                else if( !fPosition.equals(category[i]) )
                    ((SortedPosition)category[i]).update(fPosition.offset, fPosition.length);
                        
            }

        } catch (BadPositionCategoryException x) {
            // do nothing
        } finally {
            fDocument= null;
        }
    }
}
