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
// $Id: IconSource.java,v 1.7 2006/10/16 08:38:24 spyromus Exp $
//

package com.salas.bb.utils.uif;

import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.utils.i18n.Strings;
import sun.misc.SoftCache;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.image.BufferedImage;

/**
 * Cache of icons. It's based on SUN's <code>SoftCache</code>. When icon is not
 * in cache it's loaded directly, blocking the call to <code>getIcon</code> until
 * finish. This behaviour is necessary to avoid using Image Fetcher threads which
 * are part of old AWT Media framework. We need not rely on them because all of
 * the fetcher threads can be busy with some lengthy loadings from Web and
 * we need immediate results (painting icons).
 *
 * Note: This solution isn't really fair because it uses <code>SoftCache</code>
 *       which isn't part of public code, but internal SUN's tool. They are free
 *       to remove it at any moment, however current version of JRE (1.4.0-1.5) are
 *       supporting it.
 */
public final class IconSource extends SoftCache
{
    private static final Logger LOG = Logger.getLogger(IconSource.class.getName());

    private static final IconSource INSTANCE = new IconSource();

    /** Hidden utility class constructor. */
    private IconSource()
    {
    }

    /**
     * Returns image icon by key. Looks first into the cache and then to disk.
     *
     * @param key key.
     *
     * @return image icon.
     *
     * @see ResourceUtils#getIcon
     */
    public static ImageIcon getIcon(String key)
    {
        if (key == null) return null;

        ImageIcon icon;

        synchronized (INSTANCE)
        {
            icon = (ImageIcon)INSTANCE.get(key);
        }

        return icon;
    }

    /** Called when no object by this key exist. */
    protected Object fill(Object key)
    {
        String iconKey = (String)key;
        String path = ResourceUtils.getString(iconKey);
        if ((path == null) || (path.length() == 0)) return null;

        return loadIcon(path);
    }

    /**
     * Lods icon located at some path.
     *
     * @param path path to the icon.
     *
     * @return icon.
     */
    public static ImageIcon loadIcon(String path)
    {
        return loadIcon(ResourceUtils.getURL(path));
    }

    /**
     * Loads image icon from given URL directly without help of <code>Toolkit</code>.
     *
     * @param location  location of image.
     *
     * @return image icon or NULL.
     */
    public static ImageIcon loadIcon(URL location)
    {
        ImageIcon icon = null;

        try
        {
            BufferedImage image = location == null ? null : ImageIO.read(location);
            if (image != null) icon = new ImageIcon(image);
        } catch (IOException e)
        {
            if (LOG.isLoggable(Level.WARNING))
            {
                LOG.warning(MessageFormat.format(
                    Strings.error("failed.to.load.icon.by.key.from.0"),
                    new Object[] { location }));
            }
        }

        return icon;
    }

    /** Returns TRUE if the icons by the key is cached. */
    static boolean hasInCache(String key)
    {
        boolean contains;

        synchronized (INSTANCE)
        {
            contains = INSTANCE.containsKey(key);
        }

        return contains;
    }
}
