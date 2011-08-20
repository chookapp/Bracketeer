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

package com.chookapp.org.bracketeer.common;

import java.util.ArrayList;
import java.util.List;

public class BracketsPair
{
    private List<Integer> _locations;
    
    private BracketsPair()
    {
        _locations = new ArrayList<Integer>();
    }
    
    public BracketsPair(int offset)
    {
        this();
        _locations.add(offset);
    }
    
    public BracketsPair(int offset1, int offset2)
    {
        this();
        _locations.add(offset1);
        _locations.add(offset2);
    }

    public List<Integer> getLocations()
    {
        return _locations;
    }           
}
