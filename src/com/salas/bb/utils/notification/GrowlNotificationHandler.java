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
import com.salas.bb.utils.osx.OSXSupport;

import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mac-specific Growl-based notifications.
 */
abstract class GrowlNotificationHandler implements INotificationHandler
{
    private static final Logger LOG = Logger.getLogger(GrowlNotificationHandler.class.getName());

    private final String appName;

    private boolean initOK;
    private Method growl_notifyGrowlOf;

    private Object image;
    private Object growl;

    /**
     * Creates handler.
     *
     * @param appName   application name.
     * @param events    list of events.
     * @param bigImage  big image for message icon.
     */
    public GrowlNotificationHandler(String appName, String[] events, URL bigImage)
    {
        this.appName = appName;
        initOK = false;
        try
        {
            // Resolving classes we need
            Class classNSImage = OSXSupport.getCocoaClass("com.apple.cocoa.application.NSImage");
            Class classNSDictionary = OSXSupport.getCocoaClass("com.apple.cocoa.foundation.NSDictionary");
            Class classGrowl = OSXSupport.getCocoaClass("com.growl.Growl");

            // Resolving methods we need
            Method growl_register = classGrowl.getMethod("register", new Class[0]);
            growl_notifyGrowlOf = classGrowl.getMethod("notifyGrowlOf",
                new Class[] { String.class, classNSImage, String.class, String.class, classNSDictionary});

            Constructor constructor;

            // Prepares image
            if (bigImage != null)
            {
                Class classNSData = OSXSupport.getCocoaClass("com.apple.cocoa.foundation.NSData");
                constructor = classNSData.getConstructor(new Class[] { byte[].class });
                Object data = constructor.newInstance(new Object[]{ OSXSupport.loadResourceBytes("resources/bbiconsmall.gif")});
                constructor = classNSImage.getConstructor(new Class[]{classNSData});
                image = constructor.newInstance(new Object[]{data});
            } else image = null;

            // Prepares growl instance
            constructor = classGrowl.getConstructor(new Class[] { String.class, String[].class, String[].class });
            growl = constructor.newInstance(new Object[] { appName, events, events });
            growl_register.invoke(growl, new Object[0]);

            initOK = true;
        } catch (Throwable e)
        {
            LOG.log(Level.WARNING, Strings.error("notify.failed.to.initialize.growl.notifications"), e);
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

        try
        {
            growl_notifyGrowlOf.invoke(growl,
                new Object[] { event, image, appName, message, null });
        } catch (Exception e)
        {
            LOG.log(Level.SEVERE, Strings.error("notify.failed.to.notify.growl"), e);
        }
    }

    /**
     * Sets an action listener which will be notified when a user clicks over the
     * balloon.
     *
     * @param l listener.
     */
    public void setAppIconActionListener(ActionListener l)
    {
        // No way to register listeners yet
    }
}
