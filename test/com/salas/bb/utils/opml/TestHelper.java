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
// $Id: TestHelper.java,v 1.14 2007/04/30 13:43:27 spyromus Exp $
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
import java.util.Arrays;
import java.util.Date;

/**
 * This suite contains tests for <code>Helper</code> unit.
 */
public class TestHelper extends TestCase
{
    /**
     * Tests creation of the guide with reading list.
     */
    public void testCreateGuideWithRL()
    {
        DirectOPMLFeed opmlFeed = new DirectOPMLFeed("Feed", "file://test", "", 1, null, null, -1,
            null, null, null, null, null, null, false, 0, false, 1, false);

        OPMLReadingList opmlRL = new OPMLReadingList("ReadingList", "file://test");
        opmlRL.setFeeds(Arrays.asList(opmlFeed));

        OPMLGuide opmlGuide = new OPMLGuide("Guide", "", false, null, null, false, 0, false, false);
        opmlGuide.add(opmlRL);

        // Checking
        StandardGuide guide = (StandardGuide)Helper.createGuide(null, opmlGuide, new Date());
        assertEquals(1, guide.getReadingLists().length);
        assertEquals(1, guide.getFeedsCount());

        ReadingList list = guide.getReadingLists()[0];
        assertEquals(1, list.getFeeds().length);
    }

    /**
     * Tests converting OPML guide into domain object moving publication properties.
     */
    public void testCreateGuideWithPublicationProperties()
    {
        OPMLGuide opmlGuide = new OPMLGuide("Guide", "", true, "title", "tags", true, 2, false, false);

        // Checking
        StandardGuide guide = (StandardGuide)Helper.createGuide(null, opmlGuide, new Date());
        assertEquals(opmlGuide.isPublishingEnabled(), guide.isPublishingEnabled());
        assertEquals(opmlGuide.isPublishingPublic(), guide.isPublishingPublic());
        assertEquals(opmlGuide.getPublishingTitle(), guide.getPublishingTitle());
        assertEquals(opmlGuide.getPublishingTags(), guide.getPublishingTags());
        assertEquals(opmlGuide.getPublishingRating(), guide.getPublishingRating());
    }

    /**
     * Tests converting OPML guide into domain object moving notification properties.
     */
    public void testCreateGuideWithNotificationProperties()
    {
        OPMLGuide opmlGuide = new OPMLGuide("Guide", "", true, "title", "tags", true, 0, false, false);

        // Checking
        StandardGuide guide = (StandardGuide)Helper.createGuide(null, opmlGuide, new Date());
        assertEquals(opmlGuide.isNotificationsAllowed(), guide.isNotificationsAllowed());

        opmlGuide = new OPMLGuide("Guide", "", true, "title", "tags", true, 0, false, true);

        // Checking
        guide = (StandardGuide)Helper.createGuide(null, opmlGuide, new Date());
        assertEquals(opmlGuide.isNotificationsAllowed(), guide.isNotificationsAllowed());
    }

    /**
     * Tests creating a feed with custom mode, type and sorting order.
     *
     * @throws MalformedURLException in case of URL format problems.
     */
    public void testCreateFeedWithCustomModeAndType() throws MalformedURLException
    {
        DirectOPMLFeed opmlFeed = new DirectOPMLFeed("Feed", "file://test", "", 1, null, null, -1,
            null, null, null, null, null, null, false, 1, true, 2, false);

        DirectFeed feed = Helper.createDirectFeed(new URL("file://test"), opmlFeed);

        assertEquals(1, feed.getType().getType());
        assertTrue(feed.isCustomViewModeEnabled());
        assertEquals(2, feed.getCustomViewMode());
        assertFalse(feed.getAscendingSorting());
    }
}
