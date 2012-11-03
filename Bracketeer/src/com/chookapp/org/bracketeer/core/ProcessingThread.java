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
package com.chookapp.org.bracketeer.core;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.services.IDisposable;

import com.chookapp.org.bracketeer.Activator;
import com.chookapp.org.bracketeer.extensionpoint.BracketeerProcessor;

public class ProcessingThread implements Runnable, IDocumentListener, IDisposable
{
    private Thread _thread = null;
    private IDocument _doc;
    private BracketeerProcessor _processor;
    private BracketeerProcessingContainer _bracketContainer;
    //private Lock _bracketContainerLock = new ReentrantLock();
    
    private boolean _documentChanged;
    
//    private boolean _isProcessing;
//    private boolean _cancelProcessing;
//    private Semaphore _processingCanceled = new Semaphore(0);
    private Object _docChangedLock = new Object();
    private boolean _docIsChanging;
    private boolean _disposing;
    
    public ProcessingThread(IDocument doc, BracketeerProcessor processor)
    {
        _processor = processor;
        _documentChanged = false;
        _docIsChanging = false;
        _disposing = false;
        _doc = doc;
        _bracketContainer = new BracketeerProcessingContainer(_doc);
//        _isProcessing = false;        
        
        _doc.addDocumentListener(this);
        
        // initial mapping...
        documentChanged(null);
    }
    
    @Override
    public void dispose()
    {
        _doc.removeDocumentListener(this);
        _disposing = true;
    }
    

    public void run()
    {
        while(!_disposing)
        {                
            while(_documentChanged || _docIsChanging || 
                    _bracketContainer.isUpdatingListeners())
            {
                _documentChanged = false;
                try
                {
                    Thread.sleep(250);
                }
                catch (InterruptedException e)
                {
                    Activator.log(e);
                }
            } // waiting for document to stop changing
            
            _bracketContainer.markAllToBeDeleted();
            boolean reRun = true;
            try
            {
                reRun = !_processor.process(_bracketContainer);
            }
            catch (Exception e)
            {
                Activator.log(e);
            }
            
            synchronized (_docChangedLock)
            {
                if( reRun || _documentChanged )
                {
                    _documentChanged = true;
                    continue;
                }
                _thread = null;
                break;
            }
        }
        
        _bracketContainer.deleteAllMarked();
        _bracketContainer.updateComplete();

    }
    
//    public void run()
//    {
//        synchronized(_lock)
//        {
//            _cancelProcessing = false;
//            _isProcessing = true;
//        }
//        
//        _processor.process(_bracketContainer);
//
//        synchronized(_lock)
//        {
//            _isProcessing = false;
//            if( _cancelProcessing )
//                _processingCanceled.release();
//        }
//    }
//
//    @Override
//    public void documentAboutToBeChanged(DocumentEvent event)
//    {
//        synchronized(_lock)
//        {
//            if( !_isProcessing )
//                return;
//            _cancelProcessing = true;
//        }
//        
//        _processor.cancel();
//        
//        try
//        {                       
//            _processingCanceled.acquire();
//        }
//        catch (InterruptedException e)
//        {
//            Activator.log(e);
//        }
//    }

    public BracketeerProcessingContainer getBracketContainer()
    {
        return _bracketContainer;
    }
    
    
    @Override
    public void documentAboutToBeChanged(DocumentEvent event)
    {
        _docIsChanging = true;
    }
    
    @Override
    public void documentChanged(DocumentEvent event)
    {
        // TODO: optimize - put the event in an events list so that the processor could parse only part of the file

        _docIsChanging = false;
        synchronized (_docChangedLock)
        {
            _documentChanged = true;
            if( _thread == null)
            {
                _thread = new Thread(this, "ProcessingThread"); //$NON-NLS-1$
                _thread.start();
            }
        }
    }

}
