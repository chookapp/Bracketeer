package com.chookapp.org.bracketeer.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;


public class BracketeerToggleCommandHendler extends AbstractHandler 
{
    public BracketeerToggleCommandHendler()
    {
    }
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        State state = event.getCommand().getState(BracketeerToggleState.STATE_ID);
        state.setValue(! (Boolean)state.getValue());
        return null;
    }
   

}
