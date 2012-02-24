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
package com.chookapp.org.bracketeer.common;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;

public interface IBracketeerProcessingContainer
{
    /**
     * Adds a pair to the container
     * If the pair already exists nothing happens
     * Adding a pair which has a single bracket shared with another pair is illegal and would cause unexpected results  
     * @param pair the pair to add
     * @throws BadLocationException 
     */
    public void add(BracketsPair pair) throws BadLocationException;
    
    /**
     * Adds a single bracket (has a missing pair) to the container
     * If the bracket already exists (as a single bracket) nothing happens
     * Adding a single bracket which is also in a pair is illegal and would cause unexpected results  
     * @param bracket the single bracket to add
     * @throws BadLocationException 
     */
    public void add(SingleBracket bracket) throws BadLocationException;
 
    /**
     * Adds a hint to the container
     * A hint may overlap with a pair or a single bracket
     * @param hint the hint to add
     * @throws BadLocationException 
     */
    public void add(Hint hint) throws BadLocationException;
    
    /**
     * Gets a pair which matches the specified offsets (there can be at most one match)
     * @param openOffset the (absolute) offset of the opening bracket
     * @param closeOffset the (absolute) offset of the closing bracket
     * @return A pair, or null if no such pair found
     */
    public BracketsPair getMatchingPair(int openOffset, int closeOffset);
     
    
    /**
     * Returns all the hints in the container
     */
    public List<Hint> getHints();
}
