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
// $Id: TestArticleMarker.java,v 1.7 2006/01/08 05:28:16 kyank Exp $
//

package com.salas.bb.core;

import com.salas.bb.domain.prefs.UserPreferences;
import junit.framework.TestCase;

import java.beans.PropertyChangeListener;

/**
 * @see ArticleMarker
 */
public class TestArticleMarker extends TestCase
{
    /**
     * Tests how settings are populated from users preferences to this class.
     */
    public void testMarkInterval()
    {
        // Init model
        GlobalModel gm = new GlobalModel(null);
        GlobalModel.setSINGLETON(gm);

        final UserPreferences prefs = gm.getUserPreferences();
        assertNotNull(prefs);

        // Create and register listener for marker
        ArticleMarker am = ArticleMarker.getInstance();
        PropertyChangeListener l = am.getPreferencesListener(
                UserPreferences.KEY_MARK_READ_AFTER_DELAY,
                UserPreferences.KEY_MARK_READ_AFTER_SECONDS);

        prefs.addPropertyChangeListener(UserPreferences.KEY_MARK_READ_AFTER_DELAY, l);
        prefs.addPropertyChangeListener(UserPreferences.KEY_MARK_READ_AFTER_SECONDS, l);

        // Test
        prefs.setMarkReadAfterDelay(false);
        assertFalse(am.isIntervalMarkingEnabled());
        prefs.setMarkReadAfterDelay(true);
        assertTrue(am.isIntervalMarkingEnabled());

        prefs.setMarkReadAfterSeconds(0);
        assertEquals(0, am.getMarkInterval());
        prefs.setMarkReadAfterSeconds(1);
        assertEquals(1, am.getMarkInterval());
    }
}
