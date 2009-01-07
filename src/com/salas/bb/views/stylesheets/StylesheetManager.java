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
// $Id: StylesheetManager.java,v 1.4 2006/10/18 10:20:09 spyromus Exp $
//

package com.salas.bb.views.stylesheets;

import com.salas.bb.utils.FileUtils;

import java.io.File;
import java.net.URL;

/**
 * Stylesheets manager. Before the use, it requires initialization.
 */
public class StylesheetManager
{
    private static final String SUGGESTIONS_CSS = "suggestions.css";

    private static File cacheDir;
    private static URL  baseURL;

    private static RemoteStylesheet suggestionsStylesheet;

    /** Hidden constructor. */
    private StylesheetManager()
    {
    }

    /**
     * Initializes manager. These settings affect only newly created stylesheets.
     *
     * @param cacheDir  cache directory.
     * @param baseURL   base stylesheets URL.
     */
    public static void init(File cacheDir, URL baseURL)
    {
        StylesheetManager.baseURL = baseURL;
        StylesheetManager.cacheDir = cacheDir;
    }

    /**
     * Returns a stylesheet for suggestions dialog.
     *
     * @return stylesheet.
     */
    public static synchronized IStylesheet getSuggestionsStylesheet()
    {
        RemoteStylesheet rs = suggestionsStylesheet;

        if (rs == null)
        {
            rs = getStylesheet(SUGGESTIONS_CSS);
            if (rs.isInitialized()) suggestionsStylesheet = rs;
        }

        return rs;
    }

    /**
     * Returns or creates a stylesheet with a given name.
     *
     * @param name name.
     *
     * @return stylesheet.
     */
    private static RemoteStylesheet getStylesheet(String name)
    {
        return new RemoteStylesheet(baseURL, name, cacheDir);
    }

    /**
     * Updates all registered stylesheets. Flushes caches etc.
     */
    public static synchronized void update()
    {
        // Remove all caches
        FileUtils.rmdir(cacheDir);

        if (suggestionsStylesheet != null)
        {
            suggestionsStylesheet.resetIconsCache();
            
            // Forget about the stylesheet
            suggestionsStylesheet = null;
        }
    }

    /**
     * Returns update runner.
     *
     * @return runnable update task.
     */
    public static Runnable getUpdater()
    {
        return new Runnable()
        {
            public void run()
            {
                update();
            }
        };
    }
}
