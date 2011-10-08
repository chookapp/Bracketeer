package com.chookapp.org.bracketeer.common;

public interface IBracketeerProcessingContainer
{
    /**
     * Adds a pair to the container
     * If the pair already exists nothing happens
     * Adding a pair which has a single bracket shared with another pair is illegal and would cause unexpected results  
     * @param pair the pair to add
     */
    public void add(BracketsPair pair);
    
    /**
     * Adds a single bracket (has a missing pair) to the container
     * If the bracket already exists (as a single bracket) nothing happens
     * Adding a single bracket which is also in a pair is illegal and would cause unexpected results  
     * @param bracket the single bracket to add
     */
    public void add(SingleBracket bracket);
}
