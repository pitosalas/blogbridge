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
// $Id: TestStandardGuideEvents.java,v 1.4 2006/07/07 14:58:29 spyromus Exp $
//

package com.salas.bb.domain;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This suite contains tests for <code>StandardGuide</code> unit.
 */
public class TestStandardGuideEvents extends MockObjectTestCase
{
    private StandardGuide guide;
    private Mock listener;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        listener = new Mock(IGuideListener.class);

        guide = new StandardGuide();
        guide.addListener((IGuideListener)listener.proxy());
    }

    /**
     * Adding a feed manually directly to the guide.
     */
    public void testAddFeedManual()
    {
        DirectFeed feed = new DirectFeed();
        listener.expects(once()).method("feedLinkAdded").with(same(guide), same(feed));
        listener.expects(once()).method("feedAdded").with(same(guide), same(feed));

        guide.add(feed);
        listener.verify();
    }

    /**
     * Adding a feed as part of a reading list.
     */
    public void testAddFeedReadingList()
    {
        DirectFeed feed = new DirectFeed();
        ReadingList list = new ReadingList(getTestURL());
        listener.expects(once()).method("readingListAdded").with(same(guide), same(list));
        listener.expects(once()).method("feedAdded").with(same(guide), same(feed));

        list.add(feed);
        guide.add(list);
        listener.verify();
    }

    /**
     * Adding a feed after the reading list is added.
     */
    public void testAddFeedAfterReadingList()
    {
        DirectFeed feed = new DirectFeed();
        ReadingList list = new ReadingList(getTestURL());
        listener.expects(once()).method("readingListAdded").with(same(guide), same(list));
        listener.expects(once()).method("feedAdded").with(same(guide), same(feed));

        guide.add(list);
        list.add(feed);
        listener.verify();
    }

    /**
     * Adding the feed to the guide directly when it's already present as part of the reading list.
     */
    public void testAddVisibleFeedDirectly()
    {
        DirectFeed feed = new DirectFeed();
        ReadingList list = new ReadingList(getTestURL());
        listener.expects(once()).method("readingListAdded").with(same(guide), same(list));
        listener.expects(once()).method("feedAdded").with(same(guide), same(feed));
        listener.expects(once()).method("feedLinkAdded").with(same(guide), same(feed));

        guide.add(list);
        list.add(feed);
        guide.add(feed);
        listener.verify();
    }

    /**
     * Removing a feed directly from the reading list.
     */
    public void testRemoveFeed()
    {
        DirectFeed feed = new DirectFeed();
        listener.expects(once()).method("feedAdded").with(same(guide), same(feed));
        listener.expects(once()).method("feedLinkAdded").with(same(guide), same(feed));
        listener.expects(once()).method("feedLinkRemoved").with(same(guide), same(feed));
        listener.expects(once()).method("feedRemoved"); //.with(same(guide), same(feed));

        guide.add(feed);
        guide.remove(feed);
        listener.verify();
    }

    /**
     * Removing a feed as part of the reading list.
     */
    public void testRemoveFeedReadingList()
    {
        DirectFeed feed = new DirectFeed();
        ReadingList list = new ReadingList(getTestURL());
        listener.expects(once()).method("readingListAdded").with(same(guide), same(list));
        listener.expects(once()).method("feedAdded").with(same(guide), same(feed));
        listener.expects(once()).method("feedRemoved"); //.with(same(guide), same(feed));
        listener.expects(once()).method("readingListRemoved").with(same(guide), same(list));

        guide.add(list);
        list.add(feed);
        guide.remove(list, true);
        listener.verify();
    }

    /**
     * Removing the reading list with saving the feeds (adding them directly to the guide).
     */
    public void testRemoveReadingListSaveFeeds()
    {
        DirectFeed feed = new DirectFeed();
        ReadingList list = new ReadingList(getTestURL());
        listener.expects(once()).method("readingListAdded").with(same(guide), same(list));
        listener.expects(once()).method("feedAdded").with(same(guide), same(feed));
        listener.expects(once()).method("feedLinkAdded").with(same(guide), same(feed));
        listener.expects(once()).method("readingListRemoved").with(same(guide), same(list));

        guide.add(list);
        list.add(feed);
        guide.remove(list, false);
        listener.verify();
    }

    public void testCopyFeed()
    {
        StandardGuide guide2 = new StandardGuide();
        guide2.addListener((IGuideListener)listener.proxy());

        DirectFeed feed = new DirectFeed();
        ReadingList list = new ReadingList(getTestURL());
        listener.expects(once()).method("readingListAdded").with(same(guide), same(list));
        listener.expects(once()).method("feedAdded").with(same(guide), same(feed));

        // Move (copy)
        listener.expects(once()).method("feedAdded").with(same(guide2), same(feed));
        listener.expects(once()).method("feedLinkAdded").with(same(guide2), same(feed));

        guide.add(list);
        list.add(feed);
        guide.moveFeed(feed, guide2, 0); // copy because of feed being locked by the reading list

        listener.verify();
    }

    public void testMoveFeed()
    {
        StandardGuide guide2 = new StandardGuide();
        guide2.addListener((IGuideListener)listener.proxy());

        DirectFeed feed = new DirectFeed();
        listener.expects(once()).method("feedAdded").with(same(guide), same(feed));
        listener.expects(once()).method("feedLinkAdded").with(same(guide), same(feed));

        // Move
        listener.expects(once()).method("feedAdded").with(same(guide2), same(feed));
        listener.expects(once()).method("feedLinkAdded").with(same(guide2), same(feed));
        listener.expects(once()).method("feedLinkRemoved").with(same(guide), same(feed));
        listener.expects(once()).method("feedRemoved"); //.with(same(guide), same(feed));

        guide.add(feed);
        guide.moveFeed(feed, guide2, 0);

        listener.verify();
    }

    public void testRepositionFeed()
    {
        DirectFeed feed0 = new DirectFeed();
        DirectFeed feed1 = new DirectFeed();
        DirectFeed feed2 = new DirectFeed();

        listener.expects(once()).method("feedAdded").with(same(guide), same(feed0));
        listener.expects(once()).method("feedLinkAdded").with(same(guide), same(feed0));
        listener.expects(once()).method("feedAdded").with(same(guide), same(feed1));
        listener.expects(once()).method("feedLinkAdded").with(same(guide), same(feed1));
        listener.expects(once()).method("feedAdded").with(same(guide), same(feed2));
        listener.expects(once()).method("feedLinkAdded").with(same(guide), same(feed2));

        guide.add(feed0);
        guide.add(feed1);
        guide.add(feed2);

        listener.expects(once()).method("feedRepositioned").with(same(guide), same(feed0), eq(0), eq(2));
        listener.expects(once()).method("feedRepositioned").with(same(guide), same(feed0), eq(2), eq(0));

        // Move feed 0 after feed 2
        guide.moveFeed(feed0, guide, 2);

        // Move feed 0 back to top
        guide.moveFeed(feed0, guide, 0);

        // There should be no events
        listener.verify();
    }

    /**
     * Creates test URL.
     *
     * @return url.
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
}
