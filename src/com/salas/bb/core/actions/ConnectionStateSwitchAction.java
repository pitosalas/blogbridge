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
// $Id: ConnectionStateSwitchAction.java,v 1.4 2006/05/30 10:31:15 spyromus Exp $
//

package com.salas.bb.core.actions;

import com.salas.bb.core.ApplicationLauncher;
import com.salas.bb.utils.ConnectionState;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * An online-offline switch action.
 */
public final class ConnectionStateSwitchAction extends AbstractAction
    implements PropertyChangeListener
{
    private static ConnectionStateSwitchAction instance;
    private final ConnectionState connectionState;

    private String goOfflineTitle;
    private String goOnlineTitle;

    /** Hidden singleton constructor. */
    private ConnectionStateSwitchAction()
    {
        connectionState = ApplicationLauncher.getConnectionState();
        connectionState.addPropertyChangeListener(ConnectionState.PROP_ONLINE, this);
    }

    /**
     * Returns action instance.
     *
     * @return instance.
     */
    public static synchronized ConnectionStateSwitchAction getInstance()
    {
        if (instance == null) instance = new ConnectionStateSwitchAction();
        return instance;
    }

    /**
     * Sets the value.
     *
     * @param key       key.
     * @param newValue  value.
     */
    public void putValue(String key, Object newValue)
    {
        if ("name".equalsIgnoreCase(key))
        {
            String titles = newValue.toString();
            int mark = titles.indexOf('~');

            goOfflineTitle = mark == -1 ? Strings.message("go.offline") : titles.substring(0, mark);
            goOnlineTitle = mark == -1 ? Strings.message("go.online") : titles.substring(mark + 1);
        } else super.putValue(key, newValue);

        updateNameKey();
    }

    /** Updates the label in the menu. */
    private void updateNameKey()
    {
        super.putValue("Name", connectionState.isOnline() ? goOfflineTitle : goOnlineTitle);
    }

    /**
     * Invoked when user calls this action.
     *
     * @param e action event.
     */
    public void actionPerformed(ActionEvent e)
    {
        connectionState.setOnline(!connectionState.isOnline());
        updateNameKey();
    }

    /**
     * Invoked when we go online or offline.
     *
     * @param evt property change event.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        updateNameKey();
    }
}
