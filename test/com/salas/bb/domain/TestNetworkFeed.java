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
// $Id: TestNetworkFeed.java,v 1.3 2006/01/08 05:28:16 kyank Exp $
//

package com.salas.bb.domain;

import junit.framework.TestCase;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * This suite contains tests for <code>NetworkFeed</code> unit.
 * It covers:
 * <ul>
 *  <li>Reseting of invalidness reason if the fetching is successful.</li>
 *  <li>Reseting of invalidness reason if the fetching is failed.</li>
 * </ul>
 */
public class TestNetworkFeed extends TestCase
{
    /**
     * Creates test suite.
     */
    public TestNetworkFeed()
    {
        turnOffLogging();
    }

    private void turnOffLogging()
    {
        Logger logger = Logger.getLogger(NetworkFeed.class.getName());
        logger.setLevel(Level.OFF);
    }

    /**
     * Tests how the invalidness reason is being updated on successful fetching.
     */
    public void testUpdateIROnSuccessfulFetching()
    {
        DummyNetworkFeed nf = new DummyNetworkFeed();
        nf.setInvalidnessReason("Some error.");

        URL workingURL = getWorkingURL();
        nf.setXmlURL(workingURL);
        nf.fetchFeed();

        assertNull("Invalidness reason should be reset after successful fetching.",
            nf.getInvalidnessReason());
        assertFalse("Feed should be valid after successful fetching.", nf.isInvalid());
    }

    /**
     * Tests how the invalidness reason is being updated on failed fetching.
     */
    public void testUpdateIROnFailedFetching()
    {
        DummyNetworkFeed nf = new DummyNetworkFeed();
        nf.setInvalidnessReason(null);

        URL badURL = getBadURL();
        nf.setXmlURL(badURL);
        nf.fetchFeed();

        assertNotNull("Invalidness reason should be set after failed fetching.",
            nf.getInvalidnessReason());
        assertTrue("Feed should be invalid after failed fetching.", nf.isInvalid());
    }

    private URL getBadURL()
    {
        URL url = null;

        try
        {
            url = new URL("file://bad-url");
        } catch (MalformedURLException e)
        {
            fail("Failed to create bad URL.");
        }

        return url;
    }

    private URL getWorkingURL()
    {
        URL url = null;

        try
        {
            url = new URL("http://feeds.feedburner.com/BlogBridge");
        } catch (MalformedURLException e)
        {
            fail("Failed to create bad URL.");
        }

        return url;
    }
}
