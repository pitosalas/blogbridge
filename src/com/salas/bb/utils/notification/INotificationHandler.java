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
// $Id $
//

package com.salas.bb.utils.notification;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.*;
import java.net.URL;

/**
 * Notification handler is platform/java-version specific implementation.
 */
interface INotificationHandler
{
    /**
     * Shows blogbridge icon (if applicable) in notification area and
     * shows balloon with the given text. If balloon is already visible,
     * its text gets updated.
     *
     * @param event   event name.
     * @param message message text.
     */
    void showMessage(String event, String message);

    /**
     * Sets an action listener which will be notified when a user clicks over the
     * balloon.
     *
     * @param l listener.
     */
    void setAppIconActionListener(ActionListener l);

    /**
     * Shows application icon with the menu.
     *
     * @param appIconMenu menu or <code>NULL</code> if menu isn't required.
     */
    void showAppIcon(PopupMenu appIconMenu);

    /**
     * Hides application icon.
     */
    void hideAppIcon();
}
