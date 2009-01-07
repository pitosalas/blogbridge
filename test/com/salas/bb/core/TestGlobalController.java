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
// $Id: TestGlobalController.java,v 1.10 2008/02/28 15:59:50 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.NetworkFeed;
import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

/**
 * This suite contains tests for <code>GlobalController</code> unit.
 * It covers:
 * <ul>
 *  <li>Parsing of multi-URL's.</li>
 *  <li>Fixing URL without protocol specified.</li>
 * </ul>
 */
public class TestGlobalController extends TestCase
{
    /**
     * Tests parsing of multi-URL.
     */
    public void testParseMultiURL()
    {
        assertEquals("Empty set expected for NULL.",
            0, GlobalController.parseMultiURL(null).size());

        assertEquals("Empty set expected for empty URL.",
            0, GlobalController.parseMultiURL("").size());

        assertEquals("Spaces should be trimmed.",
            "http://a", GlobalController.parseMultiURL(" a ").toArray()[0]);

        Set urls = GlobalController.parseMultiURL(";a;");
        assertEquals("The list of URL's still contains single item.", 1, urls.size());
        assertEquals("Wrong URL in list.", "http://a", urls.toArray()[0]);

        urls = GlobalController.parseMultiURL("a; b ; a");
        assertEquals("No duplicates allowed.", 2, urls.size());
        assertTrue("Missing one of the URL's.", urls.contains("http://a"));
        assertTrue("Missing one of the URL's.", urls.contains("http://b"));
    }

    /**
     * Tests various scenarious and verifies that suspicious difference is reported correctly.
     */
    public void testIsSuspiciousDifference()
    {
        assertTrue(GlobalController.isSuspiciousDifference(0, 0));
        assertTrue(GlobalController.isSuspiciousDifference(0, 1));
        assertFalse(GlobalController.isSuspiciousDifference(10, 0));
        assertFalse(GlobalController.isSuspiciousDifference(10, -1));
        assertTrue(GlobalController.isSuspiciousDifference(10, 100));
    }

    /**
     * Tests detecting the best title.
     */
    public void testFindBestTitle()
        throws MalformedURLException
    {
        DirectFeed df1 = new DirectFeed();
        DirectFeed df2 = new DirectFeed();

        df1.setBaseTitle("a");
        df2.setXmlURL(new URL("ftp://test"));

        assertEquals("a", GlobalController.findBestTitle(new NetworkFeed[] { df1, df2 }));
        assertEquals("a", GlobalController.findBestTitle(new NetworkFeed[] { df2, df1 }));
        assertNull(GlobalController.findBestTitle(new NetworkFeed[0]));
    }
}
