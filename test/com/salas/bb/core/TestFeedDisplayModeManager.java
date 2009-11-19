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
// $Id: TestFeedDisplayModeManager.java,v 1.8 2008/02/28 15:59:50 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.FeedClass;
import com.salas.bb.utils.uif.UifUtilities;
import junit.framework.TestCase;

import java.awt.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * This test suite contains tests for <code>FeedDisplayModeManager</code> class.
 * It covers:
 */
public class TestFeedDisplayModeManager extends TestCase
{
    private FeedDisplayModeManager manager;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        manager = new FeedDisplayModeManager();
    }

    /**
     * Tests default values.
     */
    public void testDefaults()
    {
        assertEquals("Wrong color.", Color.GRAY, manager.getColor(FeedClass.INVALID));

        assertNull("Wrong color.", manager.getColor(FeedClass.LOW_RATED));

        assertEquals("Wrong color.", Color.BLACK, manager.getColor(FeedClass.READ));
        assertEquals("Wrong color.", Color.BLACK, manager.getColor(FeedClass.UNDISCOVERED));

        assertNull("Should be invisible.", manager.getColor(FeedClass.DISABLED));
    }

    /**
     * Tests setting and getting colors.
     */
    public void testGetSetColor()
    {
        manager.setColor(FeedClass.READ, Color.RED);
        assertEquals("Wrong color.", Color.RED, manager.getColor(FeedClass.READ));

        manager.setColor(FeedClass.READ, null);
        assertNull("Wrong color.", manager.getColor(FeedClass.READ));
    }

    /**
     * Tests returning the color for a channel. Validates the priority of the classes:
     * INVALID, READ, LOW_RATED, HAS_NO_KEYWORDS, UNDISCOVERED
     */
    public void testGetColor()
    {
        manager.setColor(FeedClass.DISABLED, null);
        manager.setColor(FeedClass.INVALID, Color.RED);
        manager.setColor(FeedClass.READ, Color.GRAY);
        manager.setColor(FeedClass.LOW_RATED, Color.GREEN);

        // DISABLED

        assertNull("Wrong color.", manager.getColor(FeedClass.DISABLED));
        assertNull("Wrong color.", manager.getColor(FeedClass.DISABLED | FeedClass.INVALID));
        assertNull("Wrong color.", manager.getColor(FeedClass.DISABLED | FeedClass.INVALID |
            FeedClass.READ));
        assertNull("Wrong color.", manager.getColor(FeedClass.DISABLED | FeedClass.INVALID |
            FeedClass.READ | FeedClass.LOW_RATED));

        assertNull("Wrong color.", manager.getColor(FeedClass.DISABLED | FeedClass.INVALID |
            FeedClass.READ | FeedClass.LOW_RATED | FeedClass.UNDISCOVERED));

        // Invalid

        assertEquals("Wrong color.",
            Color.RED, manager.getColor(FeedClass.INVALID));

        assertEquals("Wrong color.",
            Color.RED, manager.getColor(FeedClass.INVALID | FeedClass.READ));

        assertEquals("Wrong color.",
            Color.RED, manager.getColor(FeedClass.INVALID | FeedClass.READ |
            FeedClass.LOW_RATED));

        assertEquals("Wrong color.",
            Color.RED, manager.getColor(FeedClass.INVALID | FeedClass.READ |
            FeedClass.LOW_RATED | FeedClass.UNDISCOVERED));

        // Read

        assertEquals("Wrong color.", Color.GRAY, manager.getColor(FeedClass.READ));

        // Low Rated

        assertEquals("Wrong color.", Color.GREEN, manager.getColor(FeedClass.LOW_RATED));

        assertEquals("Wrong color.",
            Color.GREEN, manager.getColor(FeedClass.READ | FeedClass.LOW_RATED));

        assertEquals("Wrong color.",
            Color.GREEN, manager.getColor(FeedClass.LOW_RATED | FeedClass.UNDISCOVERED));

        // Undiscovered

        assertEquals("Wrong color.", Color.BLACK, manager.getColor(FeedClass.UNDISCOVERED));
    }

    /**
     * Tests that if any of the classes have NULL-color it should be prioritized.
     */
    public void testGetColorNull()
    {
        manager.setColor(FeedClass.READ, null);

        assertNull("Wrong color.", manager.getColor(FeedClass.INVALID | FeedClass.READ |
            FeedClass.LOW_RATED | FeedClass.UNDISCOVERED));
    }

    /**
     * Tests the visibility of the channel.
     */
    public void testIsVisible()
    {
        manager.setColor(FeedClass.READ, Color.YELLOW);

        DirectFeed channel = new DirectFeed();
        assertTrue("Wrong state.", manager.isVisible(channel));

        manager.setColor(FeedClass.READ, null);
        assertFalse("Wrong state.", manager.isVisible(channel));
    }

    /**
     * Tests how complete clearing works.
     */
    public void testClear()
    {
        Color mappingColor = Color.GRAY;
        int mappingClass = FeedClass.INVALID;

        assertEquals("Select some existing default mapping.",
            mappingColor, manager.getColor(mappingClass));

        manager.clear();

        assertEquals("Wrong color: should be default.", Color.BLACK, manager.getColor(mappingClass));
    }

    /**
     * Tests storing default colors in preferences.
     *
     * @throws BackingStoreException if backing store fails.
     */
    public void testStorePreferences()
        throws BackingStoreException
    {
        Preferences prefs = Preferences.userNodeForPackage(TestFeedDisplayModeManager.class);
        prefs.clear();

        manager.clear();
        manager.setColor(FeedClass.INVALID, Color.GRAY);
        manager.setColor(FeedClass.LOW_RATED, null);

        manager.storePreferences(prefs);

        String color;
        color = prefs.get("cdmm." + FeedClass.INVALID, null);
        assertNotNull("Missing color mapping.", color);
        assertEquals("Wrong color.", UifUtilities.colorToHex(Color.GRAY), color);

        color = prefs.get("cdmm." + FeedClass.LOW_RATED, null);
        assertNotNull("Missing color mapping.", color);
        assertEquals("Wrong color.", "", color);

        color = prefs.get("cdmm." + FeedClass.UNDISCOVERED, null);
        assertNull("Color mapping isn't missing.", color);
    }

    /**
     * Tests restoring preferences.
     *
     * @throws BackingStoreException if backing store fails.
     */
    public void testRestorePreferences()
        throws BackingStoreException
    {
        Preferences prefs = Preferences.userNodeForPackage(TestFeedDisplayModeManager.class);
        prefs.clear();

        prefs.put("cdmm." + FeedClass.INVALID, "#00ff00");
        prefs.put("cdmm." + FeedClass.READ, "");

        manager.clear();
        manager.restorePreferences(prefs);

        assertNull("Wrong mapping.", manager.getColor(FeedClass.READ));
        assertEquals("Wrong color.", Color.GREEN, manager.getColor(FeedClass.INVALID));
    }

    /**
     * Tests the situation when the preferences have no CDMM keys which means that installation
     * is fresh. In this case the default values should be preserved.
     *
     * @throws BackingStoreException if backing store fails.
     */
    public void testRestorePreferencesNoKeys()
        throws BackingStoreException
    {
        manager.clear();
        manager.setColor(FeedClass.INVALID, Color.RED);

        Preferences prefs = Preferences.userNodeForPackage(TestFeedDisplayModeManager.class);
        prefs.clear();

        manager.restorePreferences(prefs);

        assertEquals("Wrong or missing color.", Color.RED, manager.getColor(FeedClass.INVALID));
    }

    /**
     * Tests convertions of color into string.
     */
    public void testColor2String()
    {
        assertEquals("Wrong result.", "", UifUtilities.colorToHex(null));

        assertEquals("Wront color.", "#000000", UifUtilities.colorToHex(Color.BLACK));
        assertEquals("Wront color.", "#ff0000", UifUtilities.colorToHex(Color.RED));
        assertEquals("Wront color.", "#00ff00", UifUtilities.colorToHex(Color.GREEN));
        assertEquals("Wront color.", "#0000ff", UifUtilities.colorToHex(Color.BLUE));
        assertEquals("Wront color.", "#012033",
            UifUtilities.colorToHex(Color.decode("#012033")));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Tests invalid input for <code>isVisible()</code> method.
     */
    public void testIsVisibleFail()
    {
        try
        {
            manager.isVisible(null);
            fail("Channel should be specified. IAE expected.");
        } catch (IllegalArgumentException e)
        {
            // Expected
        }
    }

    /**
     * Tests invalid input for <code>getColor()</code> method.
     */
    public void testGetColorFail()
    {
        try
        {
            manager.getColor(null, false);
            fail("Channel should be specified. IAE expected.");
        } catch (IllegalArgumentException e)
        {
            // Expected
        }
    }

    /**
     * Tests invalid input for <code>storePrefereces()</code> method.
     */
    public void testStorePreferencesFail()
    {
        try
        {
            manager.storePreferences(null);
            fail("Preferences should be specified. IAE expected.");
        } catch (IllegalArgumentException e)
        {
            // Expected
        }
    }

    /**
     * Tests invalid input for <code>restorePrefereces()</code> method.
     */
    public void testRestorePreferencesFail()
        throws BackingStoreException
    {
        try
        {
            manager.restorePreferences(null);
            fail("Preferences should be specified. IAE expected.");
        } catch (IllegalArgumentException e)
        {
            // Expected
        }

        Preferences prefs = Preferences.userNodeForPackage(TestFeedDisplayModeManager.class);
        prefs.clear();

        // Put invalid records
        prefs.put("cdmm.", "test1");
        prefs.put("cdmm.a", "test2");
        prefs.put("cdmm.1", "test3");

        // Put valid record for verification
        prefs.put("cdmm.2", "");
        manager.restorePreferences(prefs);

        assertNull("Wrong mapping.", manager.getColor(2));
    }
}
