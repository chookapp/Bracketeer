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

import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;

public interface IBracketeerProcessorsFactory
{

    BracketeerProcessor createProcessorFor(IEditorPart part, IDocument doc);

}
