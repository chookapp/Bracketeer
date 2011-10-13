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
import com.chookapp.org.bracketeer.common.IBracketeerProcessingContainer;
import com.chookapp.org.bracketeer.common.IHintConfiguration;
import com.chookapp.org.bracketeer.common.Utils;

public abstract class BracketeerProcessor implements IDocumentListener
{
    
    private Semaphore _processingCanceled = new Semaphore(0);
    protected Boolean _cancelProcessing;
    protected IEditorPart _part;
    protected IHintConfiguration _hintConf;
    
    protected BracketeerProcessor(IEditorPart part)
    {
        _part = part;
    }
    
    public void setHintConf(IHintConfiguration conf)
    {
        _hintConf = conf;
    }
    
    public boolean process(IBracketeerProcessingContainer container)
    {
        _cancelProcessing = false;
        IDocument doc = Utils.getPartDocument(_part);
        doc.addDocumentListener(this);
        
        processDocument(doc, container);        
        postProcess(doc, container);
        
        doc.removeDocumentListener(this);
        
        if( _cancelProcessing )            
            _processingCanceled.release();
        
        return !_cancelProcessing;
    }        

    private void postProcess(IDocument doc,
                             IBracketeerProcessingContainer container)
    {
        // TODO: remove hints which are NA (there is something behind them)
        // TODO: use _hintConf to update the hints in the container (elipsis, etc...)
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
                                            IBracketeerProcessingContainer container);
}
