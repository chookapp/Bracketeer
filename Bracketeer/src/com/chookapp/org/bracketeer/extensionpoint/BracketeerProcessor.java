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

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

import com.chookapp.org.bracketeer.Activator;
import com.chookapp.org.bracketeer.common.IBracketeerProcessingContainer;
import com.chookapp.org.bracketeer.common.IHintConfiguration;

public abstract class BracketeerProcessor implements IDocumentListener
{
    
    protected Boolean _cancelProcessing;
    protected IDocument _doc;
    protected IHintConfiguration _hintConf;
    
    protected BracketeerProcessor(IDocument doc)
    {
        _doc = doc;
    }
    
    public void setHintConf(IHintConfiguration conf)
    {
        _hintConf = conf;
    }
    
    public boolean process(IBracketeerProcessingContainer container)
    {
        _cancelProcessing = false;
        
        _doc.addDocumentListener(this);
        
        processDocument(_doc, container);        
        postProcess(_doc, container);
        
        _doc.removeDocumentListener(this);
               
        return !_cancelProcessing;
    }        

    private void postProcess(IDocument doc,
                             IBracketeerProcessingContainer container)
    {        
    }

    @Override
    public void documentAboutToBeChanged(DocumentEvent event)
    {
        if( Activator.DEBUG )
            Activator.trace("doc about to be changed"); //$NON-NLS-1$
        _cancelProcessing = true;
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
