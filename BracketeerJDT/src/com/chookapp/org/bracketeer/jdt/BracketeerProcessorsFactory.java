package com.chookapp.org.bracketeer.jdt;

import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;

import com.chookapp.org.bracketeer.extensionpoint.BracketeerProcessor;
import com.chookapp.org.bracketeer.extensionpoint.IBracketeerProcessorsFactory;

public class BracketeerProcessorsFactory implements IBracketeerProcessorsFactory 
{

	public BracketeerProcessorsFactory() 
	{
	}
	
	@Override
	public BracketeerProcessor createProcessorFor(IEditorPart part, IDocument doc) 
	{
		if( part.getClass().getName().equals("org.eclipse.jdt.internal.ui.javaeditor.ClassFileEditor") ||
			part.getClass().getName().equals("org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor")) //$NON-NLS-1$
		{
		    return new BracketeerJdtProcessor(part, doc);
		}
		
		return null;
	}

}
