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
// $Id: JComboBoxActionItem.java,v 1.4 2006/01/08 05:07:31 kyank Exp $
//

package com.salas.bb.utils.uif;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.jgoodies.uif.action.ActionManager;
import com.jgoodies.uif.action.ToggleAction;
import com.salas.bb.utils.Constants;

/**
 * Combobox action item.
 */
public class JComboBoxActionItem extends JToggleButton
{
    private AbstractAction action;

    /**
     * Creates new combo-box action icon.
     *
     * @param theaction target action.
     */
    public JComboBoxActionItem(AbstractAction theaction)
    {
        super(theaction);
        action = theaction;
        configurePropertiesFromAction(action);
    }

    /**
     * Configures the button's properties from the given action.
     *
     * @param a action to use.
     */
    protected void configurePropertiesFromAction(AbstractAction a)
    {
        super.configurePropertiesFromAction(a);
        Icon icon = getIcon();
        Icon grayIcon = (Icon) a.getValue(ActionManager.SMALL_GRAY_ICON);
        if (grayIcon != null)
        {
            setSelectedIcon(icon);
            setRolloverIcon(icon);
            setIcon(grayIcon);
        }

        if (icon != null)
        {
            setText(null); // Would like to say: putClientProperty("hideActionText", Boolean.TRUE);
        }

        if (a instanceof ToggleAction)
        {
            setModel(((ToggleAction) a).getButtonModel());
        }
    }

    /**
     * This method returns the text of the item in the list for JCombobox.
     *
     * @return returns result of the actions co-named method.
     */
    public String toString()
    {
        if (action == null) return Constants.EMPTY_STRING;
        return (String) action.getValue(Action.NAME);
    }

    /**
     * Returns the listener of combo-box actions.
     *
     * @return listener.
     */
    public static ActionListener getComboBoxListener()
    {
        return new ActionListener()
        {
            /**
             * Invoked when action is performed.
             *
             * @param e action event object.
             */
            public void actionPerformed(ActionEvent e)
            {
                JComboBox cb = (JComboBox) e.getSource();
                JComboBoxActionItem actionedItem = (JComboBoxActionItem) cb.getSelectedItem();
                actionedItem.action.actionPerformed(e);
                actionedItem.getModel().setSelected(true);
            }
        };
    }
}