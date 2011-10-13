package com.chookapp.org.bracketeer.core;

public interface IProcessingContainerListener
{
    void containerUpdated(boolean bracketsPairsTouched,
                          boolean singleBracketsTouched, 
                          boolean hintsTouched);
}