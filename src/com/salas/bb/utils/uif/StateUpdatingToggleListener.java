// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2006 by R. Pito Salas
//
// This program is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software Foundation;
// either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
// without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program;
// if not, write to the Free Software Foundation, Inc., 59 Temple Place,
// Suite 330, Boston, MA 02111-1307 USA
//
// Contact: R. Pito Salas
// mailto:pitosalas@users.sourceforge.net
// More information: about BlogBridge
// http://www.blogbridge.com
// http://sourceforge.net/projects/blogbridge
//
// $Id: StateUpdatingToggleListener.java,v 1.1 2007/04/30 11:12:56 spyromus Exp $
//

package com.salas.bb.utils.uif;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Updates the state of given components depending on the state of the button.
 */
public class StateUpdatingToggleListener implements ChangeListener
{
    private final AbstractButton btn;
    private final Component[] components;

    /**
     * Creates listener that watches button and updates components.
     *
     * @param btn           button to take the state from.
     * @param components    components to update.
     */
    private StateUpdatingToggleListener(AbstractButton btn, Component ... components)
    {
        this.btn = btn;
        this.components = components;

        stateChanged(null);
    }

    /**
     * Invoked when the target of the listener has changed its state.
     *
     * @param e a ChangeEvent object
     */
    public void stateChanged(ChangeEvent e)
    {
        boolean en = btn.isSelected();
        for (Component component : components) component.setEnabled(en);
    }

    /**
     * Installs the listener.
     *
     * @param btn           button to monitor.
     * @param components    components to update.
     */
    public static void install(AbstractButton btn, Component ... components)
    {
        StateUpdatingToggleListener l = new StateUpdatingToggleListener(btn, components);
        btn.addChangeListener(l);
    }
}
