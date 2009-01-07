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

import com.salas.bb.utils.i18n.Strings;

import java.awt.*;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Native Java6 notification handler.
 */
class Java6NotificationHandler implements INotificationHandler
{
    private static final Logger LOG = Logger.getLogger(Java6NotificationHandler.class.getName());

    /** Application name. */
    private final String appName;
    /** System tray icon image. */
    private final Image smallImage;

    private ActionListener listener;

    private boolean initOK;

    private final Object iconLock = new Object();
    private Object icon;

    private Method mSystemTray_getSystemTray;
    private Method mSystemTray_add;
    private Method mSystemTray_remove;
    private Method mTrayIcon_addActionListener;
    private Method methodTrayIcon_displayMessage;
    private Method methodTrayIcon_setToolTip;
    private Method methodTrayIcon_setPopupMenu;
    private Method mTrayIcon_setImageAutoSize;

    private Class classTrayIcon;
    private Object enumInfo;
    private PopupMenu appIconMenu;

    /**
     * Creates handler.
     *
     * @param appName       application name.
     * @param smallImage    image for system tray.
     *
     * @throws NotificationsNotSupported if notifications aren't supported.
     */
    public Java6NotificationHandler(String appName, Image smallImage)
        throws NotificationsNotSupported
    {
        this.appName = appName;
        this.smallImage = smallImage;

        initOK = false;
        try
        {
            // Get classes
            Class classSystemTray = Class.forName("java.awt.SystemTray");
            classTrayIcon = Class.forName("java.awt.TrayIcon");
            Class classTrayIconMessageType = Class.forName("java.awt.TrayIcon$MessageType");

            // Get methods
            Method methodSystemTray_isSupported = classSystemTray.getMethod("isSupported");
            Object result = methodSystemTray_isSupported.invoke(null);
            if ((Boolean)result)
            {
                mSystemTray_getSystemTray = classSystemTray.getMethod("getSystemTray");
                mSystemTray_add = classSystemTray.getMethod("add", classTrayIcon);
                mSystemTray_remove = classSystemTray.getMethod("remove", classTrayIcon);
                mTrayIcon_addActionListener = classTrayIcon.getMethod("addActionListener", ActionListener.class);
                mTrayIcon_setImageAutoSize = classTrayIcon.getMethod("setImageAutoSize", Boolean.TYPE);
                methodTrayIcon_displayMessage = classTrayIcon.getMethod("displayMessage",
                    String.class, String.class, classTrayIconMessageType);
                methodTrayIcon_setToolTip = classTrayIcon.getMethod("setToolTip", String.class);
                methodTrayIcon_setPopupMenu = classTrayIcon.getMethod("setPopupMenu", PopupMenu.class);

                Method methodClass_getEnumConstants = Class.class.getMethod("getEnumConstants");
                enumInfo = ((Object[])methodClass_getEnumConstants.invoke(classTrayIconMessageType))[2];

                initOK = true;
            }
        } catch (Throwable e)
        {
            LOG.log(Level.SEVERE, Strings.error("notify.initialization.failed"), e);
        }

        if (!initOK) throw new NotificationsNotSupported();
    }

    /**
     * Shows application icon with the menu.
     *
     * @param appIconMenu menu or <code>NULL</code> if menu isn't required.
     */
    public void showAppIcon(PopupMenu appIconMenu)
    {
        if (!initOK) return;

        synchronized (iconLock)
        {
            if (icon != null) return;

            this.appIconMenu = appIconMenu;

            try
            {
                Constructor constructor = classTrayIcon.getConstructor(new Class[] {
                    Image.class, String.class,PopupMenu.class });
                icon = constructor.newInstance(smallImage, appName, appIconMenu);

                // Set auto-sizing for an image
                mTrayIcon_setImageAutoSize.invoke(icon, true);

                addIcon(icon);

                if (listener != null) addIconActionListener(icon, listener);
            } catch (Exception e)
            {
                LOG.log(Level.SEVERE, Strings.error("notify.failed.to.show.icon"), e);
            }
        }
    }

    /**
     * Hides application icon.
     */
    public void hideAppIcon()
    {
        if (!initOK || icon == null) return;

        try
        {
            synchronized (iconLock)
            {
                // Unregister menu from other icon
                removeIcon(icon);
                resetPopupMenu(icon);
                icon = null;
            }
        } catch (Exception e)
        {
            LOG.log(Level.SEVERE, Strings.error("notify.failed.to.remove.icon"), e);
        }
    }

    /**
     * Shows blogbridge icon (if applicable) in notification area and
     * shows balloon with the given text. If balloon is already visible,
     * its text gets updated.
     *
     * @param event   event name.
     * @param message message text.
     */
    public void showMessage(String event, String message)
    {
        if (!initOK) return;

        Object icn = getIcon();
        displayMessage(icn, event, message);
        setToolTip(icn, message);
    }

    /**
     * Returns or creates icon when necessary.
     *
     * @return tray icon.
     */
    private Object getIcon()
    {
        synchronized (iconLock)
        {
            if (icon == null) showAppIcon(appIconMenu);
        }

        return icon;
    }

    /**
     * Sets an action listener which will be notified when a user clicks over the
     * balloon.
     *
     * @param l listener.
     */
    public void setAppIconActionListener(ActionListener l)
    {
        listener = l;
    }

    /**
     * Sets the URL of an image to use in balloon (if applicable).
     *
     * @param imageURL image URL.
     */
    public void setBalloonImageURL(URL imageURL)
    {
        // Not applicable
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Adds an icon to the system tray.
     *
     * @param icon icon to add.
     *
     * @throws IllegalAccessException       in case of access violation.
     * @throws InvocationTargetException    in case of invocation problems.
     */
    private void addIcon(Object icon)
            throws IllegalAccessException, InvocationTargetException
    {
        Object systray = mSystemTray_getSystemTray.invoke(null);
        mSystemTray_add.invoke(systray, icon);
    }

    /**
     * Removes an icon from the system tray.
     *
     * @param icon icon.
     *
     * @throws IllegalAccessException       in case of access violation.
     * @throws InvocationTargetException    in case of invocation problems.
     */
    private void removeIcon(Object icon)
            throws IllegalAccessException, InvocationTargetException
    {
        Object systray = mSystemTray_getSystemTray.invoke(null);
        mSystemTray_remove.invoke(systray, icon);
    }

    /**
     * Adds an action listener to the icon.
     *
     * @param icon  icon.
     * @param l     listener.
     *
     * @throws IllegalAccessException       in case of access violation.
     * @throws InvocationTargetException    in case of invocation problems.
     */
    private void addIconActionListener(Object icon, ActionListener l)
            throws IllegalAccessException, InvocationTargetException
    {
        mTrayIcon_addActionListener.invoke(icon, l);
    }
    /**
     * Sets icon tooltip.
     *
     * @param icn       icon.
     * @param message   message.
     */
    private void setToolTip(Object icn, String message)
    {
        try
        {
            methodTrayIcon_setToolTip.invoke(icn, message);
        } catch (Exception e)
        {
            LOG.log(Level.SEVERE, Strings.error("notify.failed.to.set.tool.tip"), e);
        }
    }

    /**
     * Resets popup menu of an icon releasing it for other icons.
     *
     * @param icn icon.
     */
    private void resetPopupMenu(Object icn)
    {
        try
        {
            methodTrayIcon_setPopupMenu.invoke(icn, new Object[] { null });
        } catch (Exception e)
        {
            LOG.log(Level.SEVERE, "Failed to reset popup menu.", e);
        }
    }

    /**
     * Displays message.
     *
     * @param icn       system tray icon.
     * @param caption   caption.
     * @param message   message.
     */
    private void displayMessage(Object icn, String caption, String message)
    {
        try
        {
            methodTrayIcon_displayMessage.invoke(icn, caption, message, enumInfo);
        } catch (Exception e)
        {
            LOG.log(Level.SEVERE, Strings.error("notify.failed.to.display.the.message"), e);
        }
    }
}
