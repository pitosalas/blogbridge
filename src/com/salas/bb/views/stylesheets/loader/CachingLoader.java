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
// $Id: CachingLoader.java,v 1.2 2006/10/16 16:40:03 spyromus Exp $
//

package com.salas.bb.views.stylesheets.loader;

import com.salas.bb.utils.FileUtils;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Caching loader, uses local disk cache to store files.
 */
public class CachingLoader implements ILoader
{
    private final static Logger LOG = Logger.getLogger(CachingLoader.class.getName());

    private final File      cacheDir;
    private final ILoader   subloader;

    /**
     * Creates the caching loader with some cache directory and sub-loader to
     * use a primary source of data.
     *
     * @param cacheDir  cache storage directory.
     * @param subloader sub-loader to cache.
     */
    public CachingLoader(File cacheDir, ILoader subloader)
    {
        this.cacheDir = cacheDir;
        this.subloader = subloader;
    }

    /**
     * Updates the cache.
     */
    public void clearCache()
    {
        FileUtils.rmdir(cacheDir);
    }

    // ----------------------------------------------------------------------------------
    // ILoader implementation
    // ----------------------------------------------------------------------------------

    /**
     * Loads a stylesheet by the URL.
     *
     * @param base          base URL.
     * @param stylesheetURL stylesheet path.
     *
     * @return stylesheet.
     *
     * @throws java.io.IOException if failed loading.
     */
    public String loadStylesheet(URL base, String stylesheetURL)
        throws IOException
    {
        String ss = loadStylesheetLocal(stylesheetURL);
        if (ss == null)
        {
            ss = subloader.loadStylesheet(base, stylesheetURL);
            save(ss, stylesheetURL);
        }

        return ss;
    }

    /**
     * Load an icon by the URL.
     *
     * @param base    base URL.
     * @param iconURL icon URL to load.
     *
     * @return icon or <code>NULL</code> if URL is <code>NULL</code>.
     *
     * @throws java.io.IOException if failed loading.
     */
    public Icon loadIcon(URL base, String iconURL)
        throws IOException
    {
        return subloader.loadIcon(base, iconURL);
    }

    // ----------------------------------------------------------------------------------

    /**
     * Loads a stylesheet from the cache by the URL.
     *
     * @param stylesheetURL stylesheet path.
     *
     * @return stylesheet.
     *
     * @throws java.io.IOException if failed loading.
     */
    private String loadStylesheetLocal(String stylesheetURL)
        throws IOException
    {
        String str = null;

        File f = cacheFile(stylesheetURL);
        if (f.exists()) str = subloader.loadStylesheet(f.toURL(), "");

        return str;
    }

    /**
     * Saves string to a cache file.
     *
     * @param str   string.
     * @param url   cache file URL.
     */
    private void save(String str, String url)
    {
        if (str == null || !ensureCacheDirExists()) return;

        File f = cacheFile(url);
        try
        {
            if (!ensureDirectoryOfFile(f))
            {
                LOG.warning("Failed to create a directory for caching file: " + f);
            } else
            {
                FileWriter fw = new FileWriter(f);
                fw.write(str);
                fw.flush();
                fw.close();
            }
        } catch (IOException e)
        {
            LOG.log(Level.WARNING, "Failed to cache the file: " + f, e);
        }
    }

    /**
     * Returns the file corresponding to a cached file by given URL.
     *
     * @param url   url.
     *
     * @return cached file object (not necessarily exists).
     */
    File cacheFile(String url)
    {
        return new File(cacheDir, url);
    }

    /**
     * Ensures that the directory for file exists.
     *
     * @param f file.
     *
     * @return <code>TRUE</code> if exists and ready.
     */
    private static boolean ensureDirectoryOfFile(File f)
    {
        boolean ok = true;

        File dir = f.getParentFile();
        if (!dir.exists()) ok = dir.mkdirs();

        return ok;
    }

    /**
     * Ensures cache directory is present.
     *
     * @return <code>TRUE</code> if can continue working.
     */
    private boolean ensureCacheDirExists()
    {
        boolean ok = true;

        if (cacheDir.exists())
        {
            if (!cacheDir.isDirectory())
            {
                LOG.warning("The name of the cache directory is used by a file: " + cacheDir);
                ok = false;
            }
        } else
        {
            // Try creating
            ok = cacheDir.mkdirs();

            if (!ok) LOG.warning("Failed to create the cache directory: " + cacheDir);
        }

        return ok;
    }
}
