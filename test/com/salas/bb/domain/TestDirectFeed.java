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
// $Id: TestDirectFeed.java,v 1.5 2008/02/28 15:59:53 spyromus Exp $
//

package com.salas.bb.domain;

import com.salas.bb.utils.parser.Channel;
import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This tests suite contains tests for <code>DirectFeed</code> class.
 * It covers: construction, getting and setting of properties and algorithm of
 * feeds updates.
 */
public class TestDirectFeed extends TestCase
{
    private static URL testURL1;
    private static URL testURL2;

    private DirectFeed feed;

    protected void setUp()
        throws Exception
    {
        feed = new DirectFeed();

        synchronized (TestDirectFeed.class)
        {
            if (testURL1 == null) testURL1 = new URL("file://test1");
            if (testURL2 == null) testURL2 = new URL("file://test2");
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Unit tests
    // ---------------------------------------------------------------------------------------------

    /**
     * Tests initialization of properties upon construction.
     */
    public void testConstruction()
    {
        assertNull("XML URL shouldn't be initialized.", feed.getXmlURL());
        assertNull("Site URL shouldn't be initialized.", feed.getSiteURL());
        assertNull("Title shouldn't be initialized.", feed.getTitle());
        assertNull("Title shouldn't be initialized.", feed.getBaseTitle());
        assertNull("Description shouldn't be initialized.", feed.getDescription());
        assertNull("Description shouldn't be initialized.", feed.getBaseDescription());
        assertNull("Author shouldn't be initialized.", feed.getAuthor());
        assertNull("Author shouldn't be initialized.", feed.getBaseAuthor());
        assertEquals("Rating should be unset.", DirectFeed.RATING_NOT_SET, feed.getRating());
        assertEquals("Wrong default flag value.", DirectFeed.DEFAULT_DEAD, feed.isDead());
    }

    /**
     * Tests getting and setting XML URL.
     */
    public void testGetSetXmlURL()
    {
        feed.setXmlURL(testURL1);
        assertEquals("Wrong URL.", testURL1.toString(), feed.getXmlURL().toString());
    }

    /**
     * Tests getting and setting Site URL.
     */
    public void testGetSetSiteURL()
    {
        feed.setSiteURL(testURL1);
        assertEquals("Wrong URL.", testURL1.toString(), feed.getSiteURL().toString());
    }

    /**
     * Tests getting the title from several sources available.
     */
    public void testGetTitle()
    {
        // Case 1: default
        assertNull("Title shouldn't be initialized.", feed.getTitle());

        // Case 2: XML URL set
        feed.setXmlURL(testURL1);
        assertEquals("Wrong title.", testURL1.toString(), feed.getTitle());

        // Case 3: Base title set (title from the feed XML)
        feed.setBaseTitle("Some Title");
        assertEquals("Wrong title.", "Some Title", feed.getTitle());

        // Case 4: XML URL set, Base setm Custom set
        feed.setCustomTitle("Custom Title");
        assertEquals("Wrong title.", "Custom Title", feed.getTitle());
    }

    /**
     * Tests getting the description from several sources available.
     */
    public void testGetDescription()
    {
        // Case 1: default
        assertNull("Description shouldn't be initialized.", feed.getDescription());

        // Case 2: Base set
        feed.setBaseDescription("Some Description");
        assertEquals("Wrong description.", "Some Description", feed.getDescription());

        // Case 3: Base set, Custom set
        feed.setCustomDescription("Custom Description");
        assertEquals("Wrong description.", "Custom Description", feed.getDescription());
    }

    /**
     * Tests getting the description from several sources available.
     */
    public void testGetAuthor()
    {
        // Case 1: default
        assertNull("Author shouldn't be initialized.", feed.getAuthor());

        // Case 2: Base set
        feed.setBaseAuthor("Some Author");
        assertEquals("Wrong creator.", "Some Author", feed.getAuthor());

        // Case 3: Base set, Custom set
        feed.setCustomAuthor("Custom Author");
        assertEquals("Wrong creator.", "Custom Author", feed.getAuthor());
    }

    /**
     * Tests storing rating.
     */
    public void testGetSetRating()
    {
        // Case 1: default
        assertEquals("Rating should be unset.", -1, feed.getRating());

        // Case 2: manually set
        for (int i = DirectFeed.RATING_MIN; i <= DirectFeed.RATING_MAX; i++)
        {
            feed.setRating(i);
            assertEquals("Wrong rating.", i, feed.getRating());
        }

        // Case 3: Rating unset
        feed.setRating(DirectFeed.RATING_NOT_SET);
        assertEquals("Rating should be unset.", -1, feed.getRating());
    }

    /**
     * Tests storing dead-flag.
     */
    public void testGetSetDead()
    {
        // Case 1: default
        assertEquals("Wrong default value.", DirectFeed.DEFAULT_DEAD, feed.isDead());

        // Case 2: manually set
        feed.setDead(!DirectFeed.DEFAULT_DEAD);
        assertTrue("Wrong flag value.", feed.isDead());
        feed.setDead(DirectFeed.DEFAULT_DEAD);
        assertFalse("Wrong flag value.", feed.isDead());
    }

    /**
     * Tests how the data from the parsed feeds is moved to the feed properties.
     */
    public void testUpdateFeed()
        throws MalformedURLException
    {
        Channel channel = new Channel();
        channel.setTitle("A");
        channel.setDescription("B");
        channel.setAuthor("C");
        channel.setFormat("D");
        channel.setLanguage("E");
        channel.setSiteURL(new URL("http://site"));

        feed.updateFeed(channel);

        assertEquals("Wrong base title.", "A", feed.getBaseTitle());
        assertEquals("Wrong base description.", "B", feed.getBaseDescription());
        assertEquals("Wrong base author.", "C", feed.getBaseAuthor());
        assertEquals("Wrong format.", "D", feed.getFormat());
        assertEquals("Wrong language.", "E", feed.getLanguage());
        assertEquals("Wrong site URL.", "http://site", feed.getSiteURL().toString());

        // Test not overriding by empty values
        feed.updateFeed(new Channel());

        assertEquals("Wrong base title.", "A", feed.getBaseTitle());
        assertEquals("Wrong base description.", "B", feed.getBaseDescription());
        assertEquals("Wrong base author.", "C", feed.getBaseAuthor());
        assertEquals("Wrong format.", "D", feed.getFormat());
        assertEquals("Wrong language.", "E", feed.getLanguage());
        assertEquals("Wrong site URL.", "http://site", feed.getSiteURL().toString());
    }
}
