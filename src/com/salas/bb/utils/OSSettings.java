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
// $Id: OSSettings.java,v 1.6 2006/08/23 15:19:26 spyromus Exp $
//

package com.salas.bb.utils;

import com.jgoodies.uif.util.SystemUtils;

import java.io.File;

/**
 * OS-related global settings.
 */
public final class OSSettings
{
    /** Unknown "other" OS. */
    public static final int OS_TYPE_OTHER   = 0;
    /** Windows OS. */
    public static final int OS_TYPE_WINDOWS = 1;
    /** Mac OS X. */
    public static final int OS_TYPE_MAC     = 2;
    /** Linux OS. */
    public static final int OS_TYPE_LINUX   = 3;

    // OS version (undetected by default)
    private static int osType = -1;

    private static final String[] DEFAULT_BROWSER =
    {
        "",
        "rundll32 url.dll,FileProtocolHandler",
        "open",
        "mozilla"
    };

    /**
     * Hidden constructor of utility class.
     */
    private OSSettings()
    {
    }

    /**
     * Returns current OS type.
     *
     * @return current OS type.
     */
    public static synchronized int getOSType()
    {
        if (osType == -1)
        {
            if (SystemUtils.IS_OS_MAC)
            {
                osType = OS_TYPE_MAC;
            } else if (SystemUtils.IS_OS_LINUX)
            {
                osType = OS_TYPE_LINUX;
            } else if (SystemUtils.IS_OS_WINDOWS)
            {
                osType = OS_TYPE_WINDOWS;
            } else
            {
                osType = OS_TYPE_OTHER;
            }
        }

        return osType;
    }

    /**
     * Returns path to default browser for current OS.
     *
     * @return path to default browser for current OS.
     */
    public static String getDefaultBrowserPath()
    {
        return DEFAULT_BROWSER[getOSType()];
    }

    /**
     * Returns desktop directory or <code>NULL</code> if directory isn't known.
     *
     * @return desktop directory or <code>NULL</code> if directory isn't known.
     */
    public static File getDesktopDirectory()
    {
        String path = System.getProperty("user.home") + File.separator + "Desktop";
        File dir = new File(path);

        return dir.exists() ? dir : null;
    }

    /**
     * Returns <code>TRUE</code> if minimization to system tray is supported.
     *
     * @return <code>TRUE</code> if minimization to system tray is supported.
     */
    public static boolean isMinimizeToSystraySupported()
    {
        return SystemUtils.IS_OS_WINDOWS;
    }
}
