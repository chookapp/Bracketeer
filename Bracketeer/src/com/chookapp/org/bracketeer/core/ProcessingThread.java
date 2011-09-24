package com.chookapp.org.bracketeer.core;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.services.IDisposable;

import com.chookapp.org.bracketeer.Activator;
import com.chookapp.org.bracketeer.common.BracketeerProcessingContainer;
import com.chookapp.org.bracketeer.extensionpoint.BracketeerProcessor;
import com.chookapp.org.bracketeer.helpers.Utils;

public class ProcessingThread implements Runnable, IDocumentListener, IDisposable
{
    private Thread _thread = null;
    private IDocument _doc;
    private BracketeerProcessor _processor;
    private BracketeerProcessingContainer _bracketContainer;
    //private Lock _bracketContainerLock = new ReentrantLock();
    private List<ProcessingThreadListener> _listeners;
    
    private boolean _documentChanged;
    
//    private boolean _isProcessing;
//    private boolean _cancelProcessing;
//    private Semaphore _processingCanceled = new Semaphore(0);
    private Object _docChangedLock = new Object();
    private boolean _docIsChanging;
    
    public ProcessingThread(IEditorPart part, BracketeerProcessor processor)
    {
        _processor = processor;
        _documentChanged = false;
        _docIsChanging = false;
        _doc = Utils.getPartDocument(part);
        _bracketContainer = new BracketeerProcessingContainer(_doc);
//        _isProcessing = false;
        _listeners = new LinkedList<ProcessingThreadListener>();
        
        _doc.addDocumentListener(this);
        
        // initial mapping...
        documentChanged(null);
    }
    
    @Override
    public void dispose()
    {
        _doc.removeDocumentListener(this);        
    }
    
    public void addListener(ProcessingThreadListener listener)
    {
        _listeners.add(listener);
    }
    
    public void run()
    {
        while(true)
        {                
            while(_documentChanged || _docIsChanging)
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
            boolean reRun = !_processor.process(_bracketContainer);
            
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
        
        for (ProcessingThreadListener listener : _listeners)
        {
            listener.processingContainerUpdated();
        }

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
        // TODO put the event in an events list so that the processor could parse only part of the file

        _docIsChanging = false;
        synchronized (_docChangedLock)
        {
            _documentChanged = true;
            if( _thread == null)
            {
                _thread = new Thread(this, "ProcessingThread");
                _thread.start();
            }
        }
    }

}
