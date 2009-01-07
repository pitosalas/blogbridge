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
// $Id: GURLHandler.java,v 1.1 2007/03/09 15:08:34 spyromus Exp $
//

package com.limegroup.gnutella.gui;

import com.salas.bb.core.GlobalController;
import com.salas.bb.utils.discovery.detector.XMLFormat;

import javax.swing.*;
import java.net.URL;

/**
 * This source file and its native library counter-part are taken from the
 * Limewire project (http://www.limewire.org/) at March 9, 2007. The original
 * project is currently distributed under GPL license. Some modifications
 * were performed to this file in order to integrate the modules with the
 * rest of the BlogBridge application.
 */
public final class GURLHandler
{
    static
    {
        System.loadLibrary("GURL");
    }

    private static final GURLHandler INSTANCE = new GURLHandler();

    private boolean registered = false;

    private boolean enabled = false;
    private String url;

    private GURLHandler()
    {
    }

    /**
     * Returns the instance of the handler.
     *
     * @return instance.
     */
    public static GURLHandler getInstance()
    {
        return INSTANCE;
    }

    /**
     * Called by the native code.
     *
     * @param url URL being sent by Mac OS X.
     */
    @SuppressWarnings("unused")
    private void callback(final String url)
    {
        if (enabled)
        {
            Runnable runner = new Runnable()
            {
                public void run()
                {
                    try
                    {
                        String u = url;
                        if (u.startsWith("feed:")) u = "http:" + u.substring(5);
                        GlobalController.SINGLETON.subscribe(new URL(u), XMLFormat.ATOM);
                    } catch (Throwable t)
                    {
                        // Nothing to do with it
                    }
                }
            };
            SwingUtilities.invokeLater(runner);
        } else
        {
            this.url = url;
        }
    }

    /**
     * Enables the handler.
     */
    public void enable()
    {
        this.enabled = true;
        if (url != null) callback(url);
        this.url = null;
    }

    /** Registers the GetURL AppleEvent handler. */
    public void register()
    {
        if (!registered)
        {
            if (InstallEventHandler() == 0)
            {
                registered = true;
            }
        }
    }

    /**
     * We're nice guys and remove the GetURL AppleEvent handler although
     * this never happens
     */
    protected void finalize()
        throws Throwable
    {
        if (registered)
        {
            RemoveEventHandler();
        }

        super.finalize();
    }

    private synchronized final native int InstallEventHandler();
    private synchronized final native int RemoveEventHandler();
}
