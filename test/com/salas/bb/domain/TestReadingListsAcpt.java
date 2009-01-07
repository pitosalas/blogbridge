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
// $Id: TestReadingListsAcpt.java,v 1.4 2006/01/08 05:28:16 kyank Exp $
//

package com.salas.bb.domain;

import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This suite contains tests to model the functionality of reading lists. It covers
 * several significant and hairy areas: like maintaining the list of reading lists and
 * updating them (adding and removing dynamic feeds etc).
 */
public class TestReadingListsAcpt extends TestCase
{
    /**
     * User is adding a reading list to the guide.
     */
    public void testAddingReadingList()
    {
        // We have some guide
        StandardGuide guide = new StandardGuide();

        // We create a reading list
        ReadingList list = new ReadingList(getTestURL());

        // We wish to add this reading list to the guide
        guide.add(list);

        // At this point reading list should appear in the database
    }

    /**
     * This routine will happen each time the lists manager will
     * scan the guides for reading lists requiring to be updated.
     */
    public void testCheckingForReadingListsToUpdate()
    {
        // We have some guide
        StandardGuide guide = new StandardGuide();

        // We create couple of reading lists:
        //  - the first has just been updated
        //  - the second is new
        ReadingList list1 = new ReadingList(getTestURL());
        ReadingList list2 = new ReadingList(getTestURL());
        list1.setLastPollTime(System.currentTimeMillis());

        // We add them to the guide
        guide.add(list1);
        guide.add(list2);

        // -- The other thread (probably it's reading lists manager)
        // -- questions the guide for its registered reading lists

        ReadingList[] lists = guide.getReadingLists();

        // Then it scans through the list of lists and schedules
        // the updates for those who requires it.
    }

    /**
     * When we have a list of URL's of the feeds which are currently present in
     * a remote reading list we would love to know what are we required to add
     * to the local list and what are we required to remove from it. For this
     * purpose we tell a local list the list of URL's and take the lists of
     * changes in return.
     */
    public void testMatchingFeedsLists()
    {
        // Some reading list we update
        ReadingList list = new ReadingList(getTestURL());

        // Here's the list of feed URLs we took from the OPML
        DirectFeed[] feeds = new DirectFeed[]
        {
            createDirectFeed("http://www.salas.com/index.rdf"),
            createDirectFeed("http://feeds.feedburner.com/noizZze")
        };

        // We request the lists of URL's missing in the local reading list and
        // the list of feeds to be removed from the reading list
        List addFeeds = new ArrayList();
        List removeFeeds = new ArrayList();
        list.collectDifferences(feeds, addFeeds, removeFeeds);

        assertEquals(2, addFeeds.size());
        assertEquals(0, removeFeeds.size());

        // We create direct feed for the first URL and add it to the list (oversimplified method)
        DirectFeed feed1 = new DirectFeed();
        feed1.setXmlURL(feeds[0].getXmlURL());
        list.add(feed1);

        addFeeds.clear();
        removeFeeds.clear();
        list.collectDifferences(feeds, addFeeds, removeFeeds);
        assertEquals(1, addFeeds.size());
        assertEquals(0, removeFeeds.size());

        // We create direct feed for the second URL and add it to the list (oversimplified method)
        DirectFeed feed2 = new DirectFeed();
        feed2.setXmlURL(feeds[1].getXmlURL());
        list.add(feed2);

        addFeeds.clear();
        removeFeeds.clear();
        list.collectDifferences(feeds, addFeeds, removeFeeds);
        assertEquals(0, addFeeds.size());
        assertEquals(0, removeFeeds.size());

        // We test now how the list detects feeds to be removed
        addFeeds.clear();
        removeFeeds.clear();
        list.collectDifferences(new DirectFeed[0], addFeeds, removeFeeds);
        assertEquals(0, addFeeds.size());
        assertEquals(2, removeFeeds.size());
        assertTrue(removeFeeds.contains(feed1));
        assertTrue(removeFeeds.contains(feed2));
    }

    /**
     * Creates direct feed with given URL.
     *
     * @param url   URL.
     *
     * @return direct feed object.
     */
    private DirectFeed createDirectFeed(String url)
    {
        DirectFeed feed = new DirectFeed();

        try
        {
            feed.setXmlURL(new URL(url));
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
            fail();
        }

        return feed;
    }

    /**
     * When we have detected that there's a feed which is to be added,
     * we do this addition whether silently or after user's confirmation.
     */
    public void testAddingNewDynamicFeed()
    {
        // We have a reading list which is the source of a new feed
        ReadingList list = new ReadingList(getTestURL());

        // We have a guide this reading list is associated to
        StandardGuide guide = new StandardGuide();
        guide.add(list);

        // We have an URL of a new feed
        String xmlURLS = "http://feeds.feedburner.com/noizZze";

        // Next, we create an URL object and add the feed to a guide with
        // an appropriate association using the usual path.
        DirectFeed feed = createDirectFeed(xmlURLS);
        list.add(feed);

        // Verification
        DirectFeed[] feeds = list.getFeeds();
        assertEquals(1, feeds.length);
        assertEquals(1, guide.getFeedsCount());
        assertTrue(guide.getFeedAt(0) == feeds[0]);
        assertEquals(xmlURLS, feeds[0].getXmlURL().toString());
    }

    /**
     * Adding new reading list which is not empty yet.
     */
    public void testAddingNewNonEmptyReadingList()
    {
        // We have a reading list which is the source of a new feed
        ReadingList list = new ReadingList(getTestURL());

        // We have an URL of a new feed
        String xmlURLS = "http://feeds.feedburner.com/noizZze";

        // Next, we create an URL object and add the feed to a guide with
        // an appropriate association using the usual path.
        DirectFeed feed = createDirectFeed(xmlURLS);
        list.add(feed);

        // We have a guide this reading list is associated to
        StandardGuide guide = new StandardGuide();
        guide.add(list);

        // Verification
        DirectFeed[] feeds = list.getFeeds();
        assertEquals(1, feeds.length);
        assertEquals(1, guide.getFeedsCount());
        assertTrue(guide.getFeedAt(0) == feeds[0]);
        assertEquals(xmlURLS, feeds[0].getXmlURL().toString());
    }

    /**
     * It may come that the feed has disappeared from the reading list and
     * we should remove it locally, either silently or after the user's confirmation.
     */
    public void testRemovingDynamicFeed()
    {
        // We have a guide an a reading list
        ReadingList list = new ReadingList(getTestURL());
        StandardGuide guide = new StandardGuide();
        guide.add(list);

        // We also have some feed in this guide associated with the list
        DirectFeed feed = createDirectFeed("http://localhost/");
        list.add(feed);

        // -- Now that the preparations are over, we remove this feed from the list
        list.remove(feed);

        assertEquals(0, guide.getFeedsCount());
        assertFalse(list.hasAssociations());
    }

    /**
     * Removing reading list with feeds.
     */
    public void testRemovingReadingListWithFeeds()
    {
        // We have a guide an a reading list
        ReadingList list = new ReadingList(getTestURL());
        StandardGuide guide = new StandardGuide();
        guide.add(list);

        // We also have some feed in this guide associated with the list
        DirectFeed feed = createDirectFeed("http://localhost/");
        list.add(feed);

        // -- Now that the preparations are over, we remove reading list
        guide.remove(list, true);

        assertEquals(0, guide.getFeedsCount());
    }
    
    /**
     * Returns some working URL.
     *
     * @return url.
     */
    private URL getTestURL()
    {
        URL url = null;

        try
        {
            url = new URL("http://localhost/");
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
            fail();
        }

        return url;
    }
}
