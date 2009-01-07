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
// $Id: TestConverter.java,v 1.6 2007/04/30 13:43:27 spyromus Exp $
//

package com.salas.bb.utils.opml;

import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.ReadingList;
import com.salas.bb.domain.StandardGuide;
import com.salas.bbutilities.opml.objects.DirectOPMLFeed;
import com.salas.bbutilities.opml.objects.OPMLGuide;
import com.salas.bbutilities.opml.objects.OPMLReadingList;
import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * This suite contains tests for <code>Converter</code> unit.
 */
public class TestConverter extends TestCase
{
    /**
     * Tests the rules of conversion of RL and unassociated feeds.
     * 
     * @throws MalformedURLException never.
     */
    public void testConvertRL()
        throws MalformedURLException
    {
        StandardGuide guide = new StandardGuide();
        ReadingList list = new ReadingList(new URL("file://test"));
        guide.add(list);
        DirectFeed feedDyn = new DirectFeed();
        feedDyn.setBaseTitle("dyn");
        feedDyn.setXmlURL(new URL("file://dyn"));
        list.add(feedDyn);

        DirectFeed feedStat = new DirectFeed();
        feedStat.setBaseTitle("stat");
        feedStat.setXmlURL(new URL("file://stat"));
        guide.add(feedStat);

        // Checking
        OPMLGuide opmlGuide = Converter.convertToOPML(guide);
        OPMLReadingList[] lists = opmlGuide.getReadingLists();
        assertEquals(1, lists.length);

        OPMLReadingList lst = lists[0];
        List feeds = lst.getFeeds();
        assertEquals(1, feeds.size());
        DirectOPMLFeed feed = (DirectOPMLFeed)feeds.get(0);
        assertEquals("dyn", feed.getTitle());

        feeds = opmlGuide.getFeeds();
        assertEquals(1, feeds.size());
        feed = (DirectOPMLFeed)feeds.get(0);
        assertEquals("stat", feed.getTitle());
    }

    /**
     * Checks converting publication properties.
     */
    public void testConvertingPublishingProperties()
    {
        StandardGuide guide = new StandardGuide();
        guide.setPublishingEnabled(true);
        guide.setPublishingPublic(true);
        guide.setPublishingTags("a b c");
        guide.setPublishingTitle("abc");
        guide.setPublishingRating(2);

        // Checking
        OPMLGuide opmlGuide = Converter.convertToOPML(guide);
        assertEquals(guide.isPublishingEnabled(), opmlGuide.isPublishingEnabled());
        assertEquals(guide.isPublishingPublic(), opmlGuide.isPublishingPublic());
        assertEquals(guide.getPublishingTags(), opmlGuide.getPublishingTags());
        assertEquals(guide.getPublishingTitle(), opmlGuide.getPublishingTitle());
        assertEquals(guide.getPublishingRating(), opmlGuide.getPublishingRating());
    }

    /**
     * Testing how the data feed properties are filled.
     *
     * @throws MalformedURLException never.
     */
    public void testFillDataFeedProperties()
            throws MalformedURLException
    {
        DirectFeed dfeed = new DirectFeed();
        dfeed.setBaseTitle("stat");
        dfeed.setXmlURL(new URL("file://stat"));

        // Set no update period
        dfeed.setUpdatePeriod(-1);

        DirectOPMLFeed ofeed = Converter.convertToOPML(dfeed);
        assertNull(ofeed.getUpdatePeriod());
        
        // Set update period
        dfeed.setUpdatePeriod(1);
        ofeed = Converter.convertToOPML(dfeed);
        assertEquals(new Long(1), ofeed.getUpdatePeriod());
    }
}
