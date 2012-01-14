package com.chookapp.org.bracketeer.core;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.services.IDisposable;

public class Popup implements IDisposable
{
    private Shell _shell;

    public Popup(final StyledText parent, String txt)
    {
        _shell = new Shell(parent.getShell(), SWT.NO_FOCUS | SWT.ON_TOP | SWT.TOOL);
        Text label = new Text(_shell, SWT.READ_ONLY | SWT.MULTI);
        label.setBackground(parent.getBackground());
        label.setForeground(parent.getForeground());
        label.setFont(parent.getFont());
        label.setText(txt);
        label.setTabs(parent.getTabs());
        Point txtSize = label.computeSize(parent.getSize().x, SWT.DEFAULT);
        label.setSize(txtSize);
        
        _shell.setSize(txtSize);
        Point parentLocation = parent.getDisplay().map(parent, null, 0, 0);
        _shell.setLocation(parentLocation.x, parentLocation.y - txtSize.y);
        _shell.setVisible(true);
    }

    @Override
    public void dispose()
    {
        _shell.close();
        _shell.dispose();
    }
}
