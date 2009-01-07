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
// $Id: TestCachingLoader.java,v 1.3 2007/02/06 15:34:02 spyromus Exp $
//

package com.salas.bb.views.stylesheets.loader;

import com.salas.bb.utils.FileUtils;
import junit.framework.TestCase;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Tests the caching loader.
 */
public class TestCachingLoader extends TestCase
{
    private static final String CACHE_FOLDER = "test-cache";

    private static final String STYLESHEET_EXISTING = "bbstyles/suggestions.css";
    private static final String STYLESHEET_MISSING = "bbstyles/missing.css";
    private static final String ICON_EXISTING = "images/plus.gif";
    private static final String ICON_MISSING = "images/missing.gif";

    private CountingLoader coloader;
    private CachingLoader cloader;
    private URL base;
    private File cacheDir;

    /** Prepare the environment. */
    protected void setUp()
        throws Exception
    {
        // Create new cache
        cacheDir = new File(CACHE_FOLDER);
        if (cacheDir.exists()) FileUtils.rmdir(cacheDir);

        DirectLoader dloader = new DirectLoader();
        coloader = new TestCachingLoader.CountingLoader(dloader);
        cloader = new CachingLoader(cacheDir, coloader);

        // URLs
        base = new URL("http://www.blogbridge.com/");
    }

    /** Clear after ourselves. */
    protected void tearDown()
        throws Exception
    {
        if (cacheDir.exists()) FileUtils.rmdir(cacheDir);
    }

    /** Tests loading existing stylesheet and its further caching. */
    public void testLoadStylesheet_Existing()
        throws IOException
    {
        String ss = cloader.loadStylesheet(base, STYLESHEET_EXISTING);
        assertNotNull(ss);
        assertEquals(1, coloader.cntStylesheet);

        // Check if it's cached now
        File f = cloader.cacheFile(STYLESHEET_EXISTING);
        assertTrue(f.exists());

        // Load again.
        // There's =2 check because the cachedloader uses subloader to get
        // the cached file too.
        cloader.loadStylesheet(base, STYLESHEET_EXISTING);
        assertEquals("The stylesheet should be cached.", 2, coloader.cntStylesheet);
    }

    /** Tests loading missing stylesheet. */
    public void testLoadStylesheet_Missing()
        throws IOException
    {
        String ss = cloader.loadStylesheet(base, STYLESHEET_MISSING);
        assertNull(ss);
        assertEquals(1, coloader.cntStylesheet);

        // Check it's not cached
        File f = cloader.cacheFile(STYLESHEET_MISSING);
        assertFalse(f.exists());

        // Load again
        cloader.loadStylesheet(base, STYLESHEET_MISSING);
        assertEquals("Should try loading again.", 2, coloader.cntStylesheet);
    }

    /** Tests loading existing icon and its further caching. */
    public void testLoadIcon_Existing()
        throws IOException
    {
        Icon ic = cloader.loadIcon(base, ICON_EXISTING);
        assertNotNull(ic);
        assertEquals(1, coloader.cntIcon);

        // We stopped caching icons because the simple method
        // with object serialization doesn't work and we don't
        // need anyting more complex.

//        // Check if it's cached now
//        File f = cloader.cacheFile(ICON_EXISTING);
//        assertTrue(f.exists());
//
//        // Load again
//        cloader.loadIcon(base, ICON_EXISTING);
//        assertEquals("The icon should be cached.", 1, coloader.cntIcon);
    }

    /** Tests loading missing icon. */
    public void testLoadIcon_Missing()
        throws IOException
    {
        Icon ic = cloader.loadIcon(base, ICON_MISSING);
        assertNull(ic);
        assertEquals(1, coloader.cntIcon);

        // Check it's not cached
        File f = cloader.cacheFile(ICON_MISSING);
        assertFalse(f.exists());

        // Load again
        cloader.loadIcon(base, ICON_MISSING);
        assertEquals("Should try loading again.", 2, coloader.cntIcon);
    }

    /** Tests updating. Should remove all cached items. */
    public void testClearCache()
        throws IOException
    {
        // We stopped caching icons because the simple method
        // with object serialization doesn't work and we don't
        // need anyting more complex.

        cloader.loadStylesheet(base, STYLESHEET_EXISTING);
//        cloader.loadIcon(base, ICON_EXISTING);

        // Files should be there
        assertTrue(cloader.cacheFile(STYLESHEET_EXISTING).exists());
//        assertTrue(cloader.cacheFile(ICON_EXISTING).exists());

        cloader.clearCache();

        // Files shouldn't be there
        assertFalse(cloader.cacheFile(STYLESHEET_EXISTING).exists());
//        assertFalse(cloader.cacheFile(ICON_EXISTING).exists());
    }

    /**
     * Loader that counts the number of invokatios.
     */
    private static class CountingLoader implements ILoader
    {
        private final ILoader subloader;

        private int cntStylesheet = 0;
        private int cntIcon = 0;

        /**
         * Creates counting loader.
         *
         * @param subloader loader.
         */
        public CountingLoader(ILoader subloader)
        {
            this.subloader = subloader;
        }

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
            cntStylesheet++;
            return subloader.loadStylesheet(base, stylesheetURL);
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
            cntIcon++;
            return subloader.loadIcon(base, iconURL);
        }
    }
}
