/*******************************************************************************
 * Copyright (c) Gil Barash - chookapp@yahoo.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gil Barash - initial API and implementation
 *    
 *******************************************************************************/

package com.chookapp.org.bracketeer.extensionpoint;

import java.util.concurrent.Semaphore;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.IEditorPart;

import com.chookapp.org.bracketeer.Activator;
import com.chookapp.org.bracketeer.common.BracketeerProcessingContainer;
import com.chookapp.org.bracketeer.helpers.Utils;

public abstract class BracketeerProcessor implements IDocumentListener
{
    
    protected boolean _cancelProcessing;
    private Semaphore _processingCanceled = new Semaphore(0);
    protected IEditorPart _part;
    
    protected BracketeerProcessor(IEditorPart part)
    {
        _part = part;
    }
    
    public boolean process(BracketeerProcessingContainer container)
    {
        _cancelProcessing = false;
        IDocument doc = Utils.getPartDocument(_part);
        doc.addDocumentListener(this);
        
        processDocument(doc, container);
        
        doc.removeDocumentListener(this);
        
        if( _cancelProcessing )            
            _processingCanceled.release();
        
        return !_cancelProcessing;
    }        

    @Override
    public void documentAboutToBeChanged(DocumentEvent event)
    {
        try
        {
            Activator.log("doc about to be chnaged");
            _cancelProcessing = true;        
            _processingCanceled.acquire();
        }
        catch (InterruptedException e)
        {
            Activator.log(e);
        }
    }

    @Override
    public void documentChanged(DocumentEvent event)
    {
        // nothing...
    }
    
    /**
     * 
     * @param doc The document to be processed 
     * @param container The contains to add the brackets to
     */
    protected abstract void processDocument(IDocument doc,
                                            BracketeerProcessingContainer container);
}
