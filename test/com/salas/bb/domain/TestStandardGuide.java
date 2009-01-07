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
// $Id: TestStandardGuide.java,v 1.12 2006/07/07 14:58:29 spyromus Exp $
//

package com.salas.bb.domain;

import junit.framework.TestCase;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * This suite contains the tests for <code>StandardGuide</code> unit.
 * It covers: construction, adding/removing feeds and finding feed index.
 */
public class TestStandardGuide extends TestCase
{
    private StandardGuide guide;

    protected void setUp()
        throws Exception
    {
        guide = new StandardGuide();
    }

    // ---------------------------------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------------------------------

    /**
     * Tests initial state of the guide right after construction.
     */
    public void testConstruction()
    {
        assertEquals("There should be no feeds.", 0, guide.getFeedsCount());
    }

    /**
     * Tests adding new feed.
     */
    public void testAdd()
    {
        IFeed feed = new DummyNetworkFeed();

        guide.add(feed);
        assertEquals("Wrong number of feeds.", 1, guide.getFeedsCount());
        assertTrue("Wrong feed.", feed == guide.getFeedAt(0));

        IGuide[] parentGuides = feed.getParentGuides();
        assertEquals("Should be 1 parent.", 1, parentGuides.length);
        assertTrue("Wrong parent guide.", parentGuides[0] == guide);
    }

    /**
     * Tests handling of incorrect input to feed adding method.
     */
    public void testAddFailure()
    {
        // Add NULL
        try
        {
            guide.add((IFeed)null);
            fail("Feed should be specified. NPE is expected.");
        } catch (NullPointerException e)
        {
            // Expected
        }
    }

    /**
     * Tests removing the feed.
     */
    public void testRemoveSimple()
    {
        IFeed feed = new DummyNetworkFeed();

        guide.add(feed);
        assertTrue("Invalid result.", guide.remove(feed));
        assertEquals("Wrong number of feeds.", 0, guide.getFeedsCount());
        assertEquals("Should be no parent guides.", 0, feed.getParentGuides().length);
    }

    /**
     * Tests handling of incorrect input to feed removing method.
     */
    public void testRemoveFailure()
    {
        // Removing NULL
        try
        {
            guide.remove((IFeed)null);
            fail("Feed should be specified. NPE is expected.");
        } catch (NullPointerException e)
        {
            // Expected
        }

        // Removing feed which isn't part of this guide
        IFeed feed = new DummyNetworkFeed();
        feed.addParentGuide(new StandardGuide());
        assertFalse("Feed belongs to different guide.", guide.remove(feed));
    }

    /**
     * Tests finding of feed index.
     */
    public void testIndexOf()
    {
        IFeed feed1 = new DummyDataFeed();
        feed1.setRating(1);
        IFeed feed2 = new DummyDataFeed();
        feed2.setRating(2);

        guide.add(feed1);
        guide.add(feed2);

        assertEquals("Wrong index.", 0, guide.indexOf(feed1));
        assertEquals("Wrong index.", 1, guide.indexOf(feed2));
    }

    /**
     * Tests handling of incorrect input to feed finding method.
     */
    public void testIndexOfFailure()
    {
        // Getting index of NULL
        try
        {
            guide.indexOf(null);
            fail("Feed should be specified. NPE is expected.");
        } catch (NullPointerException e)
        {
            // Expected
        }

        // Getting index of feed which isn't part of this guide
        IFeed feed = new DummyNetworkFeed();
        feed.addParentGuide(new StandardGuide());
        assertEquals("Feed belongs to different guide.", -1, guide.indexOf(feed));
    }

    /**
     * Adding two feeds with similar titles.
     */
    public void testAddingAlpha()
    {
        DirectFeed feed1 = createNamedFeed("A");
        DirectFeed feed2 = createNamedFeed("A");

        guide.add(feed1);
        guide.add(feed2);

        assertEquals("Both feeds should occupy the same position.", 0, guide.alphaIndexOf(feed1));
        assertEquals("Both feeds should occupy the same position.", 0, guide.alphaIndexOf(feed2));
    }

    /**
     * Tests adjusting alpha-positions on removal.
     */
    public void testRemovingAlpha()
    {
        DirectFeed feed1 = createNamedFeed("A");
        DirectFeed feed2 = createNamedFeed("B");

        guide.add(feed1);
        guide.add(feed2);

        guide.remove(feed1);

        assertEquals("Second second should become first.", 0, guide.alphaIndexOf(feed2));
    }

    /**
     * Tests automatic resorting when feed changes its name.
     */
    public void testRenamingFeedAndResort()
    {
        DirectFeed feedA = createNamedFeed("A");
        DirectFeed feedB = createNamedFeed("B");

        guide.add(feedB);
        guide.add(feedA);

        assertEquals(0, guide.alphaIndexOf(feedA));
        assertEquals(1, guide.alphaIndexOf(feedB));

        // Now rename feedA into feedC

        feedA.setBaseTitle("C");

        assertEquals(1, guide.alphaIndexOf(feedA));
        assertEquals(0, guide.alphaIndexOf(feedB));
    }

    /**
     * Creates feed with given name.
     *
     * @param name name of the feed.
     *
     * @return feed.
     */
    private static DirectFeed createNamedFeed(String name)
    {
        DirectFeed feed = new DirectFeed();
        feed.setBaseTitle(name);
        return feed;
    }

    // ---------------------------------------------------------------------------------------------
    // Duplicates handling
    // ---------------------------------------------------------------------------------------------

    /**
     * When someone adds duplicate feed (the same Java-object) we should simply skip it.
     */
    public void testAddDuplicateFeed()
    {
        DirectFeed feed = createNamedFeed("A");

        guide.add(feed);
        guide.add(feed);

        assertEquals("No duplicates allowed.", 1, guide.getFeedsCount());
        assertTrue(feed == guide.getFeedAt(0));
    }

    /**
     * When the same feed is added manually and present in the reading list it shouldn't
     * be displayed twice.
     */
    public void testAddDuplicateThroughReadingList()
    {
        ReadingList list = new ReadingList(getTestURL());
        guide.add(list);

        DirectFeed feed = createNamedFeed("A");

        guide.add(feed);
        list.add(feed);

        assertEquals("No duplicates allowed.", 1, guide.getFeedsCount());
        assertTrue(feed == guide.getFeedAt(0));
    }

    /**
     * When we have the manually added feed and the one in the reading list and
     * we delete that added manually, the second should appear in the Guide.
     * Then, after it's removed, there should no feeds be left.
     */
    public void testRemoveDuplicate()
    {
        ReadingList list = new ReadingList(getTestURL());
        guide.add(list);

        DirectFeed feed = createNamedFeed("A");

        guide.add(feed);
        list.add(feed);
        guide.remove(feed);

        assertEquals("The one from the reading list is left.", 1, guide.getFeedsCount());
        assertTrue(feed == guide.getFeedAt(0));

        list.remove(feed);

        assertEquals("No feeds is left.", 0, guide.getFeedsCount());
    }

    /**
     * Moving feeds back and forth.
     */
    public void testRepositionFeed()
    {
        DirectFeed feed0 = createNamedFeed("0");
        DirectFeed feed1 = createNamedFeed("1");
        DirectFeed feed2 = createNamedFeed("2");

        guide.add(feed0);
        guide.add(feed1);
        guide.add(feed2);

        // Move feed 0 after feed 2
        guide.moveFeed(feed0, guide, 2);
        assertEquals(2, guide.indexOf(feed0));

        // Move feed 0 back to top
        guide.moveFeed(feed0, guide, 0);
        assertEquals(0, guide.indexOf(feed0));
    }

    /**
     * Returns test URL.
     *
     * @return test URL.
     */
    private URL getTestURL()
    {
        URL url = null;
        try
        {
            url = new URL("file://test");
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
            fail();
        }
        return url;
    }

    // ---------------------------------------------------------------------------------------------
    // Guide <-> Reading List collaboration testing
    // ---------------------------------------------------------------------------------------------

    /**
     * Adding and removing feeds to the guide and to the lists.
     */
    public void testAddingFeeds()
    {
        DirectFeed feed0 = createNamedFeed("file://0");
        DirectFeed feed1 = createNamedFeed("file://1");

        ReadingList list0 = new ReadingList(getTestURL());
        guide.add(list0);
        ReadingList list1 = new ReadingList(getTestURL());
        guide.add(list1);

        list0.add(feed0);
        assertEquals("Feed 0 has been added through the list 0.", 1, guide.getFeedsCount());

        list1.add(feed0);
        assertEquals("Feed 0 already belongs to the list 0 which is part of the guide.",
            1, guide.getFeedsCount());

        guide.add(feed0);
        assertEquals("Feed 0 already belongs to the lists 0 and 1 which are part of the guide.",
            1, guide.getFeedsCount());

        guide.add(feed1);
        assertEquals("New feed 1 was added.", 2, guide.getFeedsCount());

        list0.remove(feed0);
        list1.remove(feed0);
        assertEquals("The feed 0 still exists in the guide as being manually added.",
            2, guide.getFeedsCount());

        guide.remove(feed0);
        assertEquals("The feed 0 has completely gone from the guide and reading lists.",
            1, guide.getFeedsCount());
        assertTrue(feed1 == guide.getFeedAt(0));

        list0.add(feed1);
        guide.remove(feed1);
        assertEquals("The feed 1 exists in the list 0.", 1, guide.getFeedsCount());

        list0.remove(feed1);
        assertEquals("The feed 1 gone from the list 0.", 0, guide.getFeedsCount());

        list0.add(feed0);
        assertEquals("The feed 0 is now in the guide again.", 1, guide.getFeedsCount());

        guide.remove(list0, true);
        assertEquals("The feed 0 has gone with the list 0.", 0, guide.getFeedsCount());
    }
}
