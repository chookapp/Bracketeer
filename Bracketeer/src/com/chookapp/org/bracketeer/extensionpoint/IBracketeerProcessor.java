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

import com.chookapp.org.bracketeer.common.BracketsPair;

public interface IBracketeerProcessor
{

    /**
     * @param offset the position in which to look for a bracket
     * @return
     * <ul> 
     *  <li> null if the offset doesn't point to a bracket </li>
     *  <li> a pair if the offset points to a bracket (the pair might 
     *       contain only one item if the pair is not found) </li>
     * </ul>   
     */
    BracketsPair getMatchingPair(int offset);
    
}
