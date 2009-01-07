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
// $Id: RemoteStylesheet.java,v 1.4 2007/10/01 17:03:27 spyromus Exp $
//

package com.salas.bb.views.stylesheets;

import com.salas.bb.views.stylesheets.loader.DirectLoader;
import com.salas.bb.views.stylesheets.loader.ILoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages suggestions list style.
 */
class RemoteStylesheet extends AbstractStylesheet
{
    private static final Logger LOG = Logger.getLogger(RemoteStylesheet.class.getName());
    
    private final URL       baseURL;
    private final String    name;
    private final ILoader   loader;
    private final boolean   initialized;

    /**
     * Creates a stylesheet.
     *
     * @param baseURL   base URL of the stylesheet.
     * @param name      stylesheet path relative to the URL.
     * @param cacheDir  the directory to use for caches.
     */
    public RemoteStylesheet(URL baseURL, String name, File cacheDir)
    {
        this.baseURL = baseURL;
        this.name = name;
        this.loader = new DirectLoader(); //new CachingLoader(cacheDir, super.getLoader());

        boolean init = true;
        try
        {
            update();
        } catch (IOException e)
        {
            LOG.log(Level.WARNING, "Failed to initialize stylesheet.", e);
            init = false;
        }

        initialized = init;
    }

    /**
     * Returns <code>TRUE</code> if was successfully initialized.
     *
     * @return <code>TRUE</code> if was successfully initialized.
     */
    public boolean isInitialized()
    {
        return initialized;
    }

    /**
     * Gets updated stylesheet.
     *
     * @return new stylesheet or <code>NULL</code> if nothing changed.
     *
     * @throws IOException if loading failed.
     */
    protected String getUpdatedStylesheet()
        throws IOException
    {
        return getLoader().loadStylesheet(baseURL, name);
    }


    /**
     * Returns base URL of this stylesheet to resolveURI relative icon addresses (can be <code>NULL</code>).
     *
     * @return base URL.
     */
    protected URL getStylesheetBaseURL()
    {
        return baseURL;
    }

    /**
     * Returns the loader to use.
     *
     * @return loader.
     */
    protected ILoader getLoader()
    {
        return loader;
    }

    /**
     * Checks for updates of the stylesheet and prerequisites.
     *
     * @throws java.io.IOException if loading failed.
     */
    public void update()
        throws IOException
    {
        // All remote stylesheets share the same cache folder so far
        // and we have no right to remove everything.
        // StylesheetManager does this.
//        loader.clearCache();

        super.update();
    }
}
