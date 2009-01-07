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

import com.jgoodies.uif.util.SystemUtils;
import com.salas.bb.utils.uif.IconSource;

import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;

/**
 * Notification area interface.
 */
public class NotificationArea
{
    private static INotificationHandler handler;

    private static boolean appIconAlwaysVisible = false;
    private static boolean appIconTempVisible;

    private static PopupMenu appIconMenu;

    /**
     * Initializes the notification area.
     *
     * @param appName       the name of application.
     * @param events        the list of events.
     * @param bigImage      big image (for message icon).
     */
    public static void init(String appName, String[] events, URL bigImage)
    {
        if (SystemUtils.IS_OS_MAC)
        {
            // Using growl for notifications
            handler = new MacOSXNotificationHandler(appName, events, bigImage);
        } else if (SystemUtils.IS_JAVA_6_OR_LATER)
        {
            // Using native notification code
            try
            {
                handler = new Java6NotificationHandler(appName, IconSource.loadIcon(bigImage).getImage());
            } catch (NotificationsNotSupported e)
            {
                handler = null;
            }
        } else handler = null;
    }
    /**
     * Returns <code>TRUE</code> only if notifications are available.
     *
     * @return <code>TRUE</code> only if notifications are available.
     */
    public static boolean isSupported()
    {
        return SystemUtils.IS_JAVA_6_OR_LATER || SystemUtils.IS_OS_MAC;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Application icon
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Registers the app icon menu.
     *
     * @param menu menu.
     */
    public static void setAppIconMenu(PopupMenu menu)
    {
        appIconMenu = menu;

        if (handler != null && isAppIconVisible())
        {
            // Renew the icon menu
            handler.hideAppIcon();
            handler.showAppIcon(appIconMenu);
        }
    }

    /**
     * Changes the global state of app icon visibility.
     *
     * @param visible <code>TRUE</code> to make icon visible.
     */
    public static void setAppIconAlwaysVisible(boolean visible)
    {
        appIconAlwaysVisible = visible;
        updateAppIconState();
    }

    /**
     * Changes the state of temporary icon visibility. If global 'appIconAlwaysVisible' flag
     * is set, the setting of this parameter doesn't change anything.
     *
     * @param visible <code>TRUE</code> to make icon visible.
     */
    public static void setAppIconTempVisible(boolean visible)
    {
        appIconTempVisible = visible;
        updateAppIconState();
    }

    /**
     * Registers listener for application icon actions.
     *
     * @param l listener.
     */
    public static void setAppIconActionListener(ActionListener l)
    {
        if (handler != null) handler.setAppIconActionListener(l);
    }

    /**
     * Updates the status of app icon.
     */
    private static void updateAppIconState()
    {
        if (handler == null) return;

        if (isAppIconVisible()) handler.showAppIcon(appIconMenu); else handler.hideAppIcon();
    }

    /**
     * Returns <code>TRUE</code> when app icon is visible.
     * App icon always visible if 'appIconAlwaysVisible' is set
     * and otherwise visible only if 'appIconTempVisible' is set.
     *
     * @return <code>TRUE</code> when app icon is visible.
     */
    private static boolean isAppIconVisible()
    {
        return appIconAlwaysVisible || appIconTempVisible;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Messaging
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Shows event message.
     *
     * @param event     event.
     * @param message   message.
     */
    public static void showMessage(String event, String message)
    {
        if (handler != null) handler.showMessage(event, message);
    }
}
