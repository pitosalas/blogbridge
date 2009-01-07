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
// $Id: TestViewModePreferences.java,v 1.3 2008/02/28 15:59:46 spyromus Exp $
//

package com.salas.bb.domain.prefs;

import junit.framework.TestCase;

import java.util.prefs.Preferences;

/**
 * View mode preferences test.
 */
public class TestViewModePreferences extends TestCase
{
    private ViewModePreferences prefs;

    /**
     * Initializes tests.
     */
    protected void setUp()
        throws Exception
    {
        super.setUp();
        prefs = new ViewModePreferences();
    }

    public void testStoreRestore()
    {
        // Initialize the state
        prefs.setAuthorVisible(0, true);
        prefs.setAuthorVisible(1, false);
        prefs.setAuthorVisible(2, false);
        prefs.setCategoriesVisible(0, false);
        prefs.setCategoriesVisible(1, true);
        prefs.setCategoriesVisible(2, false);
        prefs.setDateVisible(0, false);
        prefs.setDateVisible(1, false);
        prefs.setDateVisible(2, true);

        // Store
        Preferences storage = Preferences.userNodeForPackage(TestViewModePreferences.class);
        prefs.store(storage);

        assertEquals("100", storage.get(ViewModePreferences.AUTHOR_VISIBLE, null));
        assertEquals("010", storage.get(ViewModePreferences.CATEGORIES_VISIBLE, null));
        assertEquals("001", storage.get(ViewModePreferences.DATE_VISIBLE, null));

        // Restore
        prefs.setAuthorVisible(0, false);
        prefs.setAuthorVisible(1, false);
        prefs.setAuthorVisible(2, false);
        prefs.setCategoriesVisible(0, false);
        prefs.setCategoriesVisible(1, false);
        prefs.setCategoriesVisible(2, false);
        prefs.setDateVisible(0, false);
        prefs.setDateVisible(1, false);
        prefs.setDateVisible(2, false);

        prefs.restore(storage);
        
        assertTrue(prefs.isAuthorVisible(0));
        assertFalse(prefs.isAuthorVisible(1));
        assertFalse(prefs.isAuthorVisible(2));
        assertFalse(prefs.isCategoriesVisible(0));
        assertTrue(prefs.isCategoriesVisible(1));
        assertFalse(prefs.isCategoriesVisible(2));
        assertFalse(prefs.isDateVisible(0));
        assertFalse(prefs.isDateVisible(1));
        assertTrue(prefs.isDateVisible(2));
    }
}
