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
// $Id: ConnectionIndicator.java,v 1.9 2006/05/31 12:49:31 spyromus Exp $
//

package com.salas.bb.views;

import com.salas.bb.utils.ConnectionState;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Represents connection indicator icon.
 */
public class ConnectionIndicator extends JLabel
        implements PropertyChangeListener
{
    private final ConnectionState connectionState;
    private final Icon connected;
    private final Icon disconnected;

    /**
     * Constructs new indicator object.
     *
     * @param aConnectionState  connection state object.
     * @param connected         icon to display when connected.
     * @param disconnected      icon to display when disconnected.
     */
    public ConnectionIndicator(ConnectionState aConnectionState, Icon connected, Icon disconnected)
    {
        connectionState = aConnectionState;
        this.connected = connected;
        this.disconnected = disconnected;

        connectionState.addPropertyChangeListener(ConnectionState.PROP_ONLINE, this);
        onConnectionChange();
    }

    /**
     * Called when the connection state changes.
     */
    private void onConnectionChange()
    {
        boolean online = connectionState.isOnline();

        setIcon(online ? connected : disconnected);
        setToolTipText(online
            ? Strings.message("activity.online")
            : Strings.message("activity.offline"));
    }

    /**
     * Called when the connection state changes.
     *
     * @param evt property change event.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        onConnectionChange();
    }
}
