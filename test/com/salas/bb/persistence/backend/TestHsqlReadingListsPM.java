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
// $Id: TestHsqlReadingListsPM.java,v 1.6 2006/01/08 05:28:16 kyank Exp $
//

package com.salas.bb.persistence.backend;

import com.salas.bb.domain.*;
import com.salas.bb.persistence.PersistenceException;

import java.net.URL;
import java.net.MalformedURLException;
import java.sql.SQLException;

/**
 * This suite contains tests for <code>HsqlReadingListsPM</code> unit.
 */
public class TestHsqlReadingListsPM extends AbstractHsqlPersistenceTestCase
{
    private HsqlReadingListsPM manager;
    private StandardGuide guide;
    private ReadingList list;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        // Init most modern database
        initManager("/resources");
        manager = new HsqlReadingListsPM(pm);

        // Prepare feed for use in tests
        guide = new StandardGuide();
        guide.setTitle("Test Guide");

        pm.insertGuide(guide, 0);

        list = new ReadingList(getTestURL());
        guide.add(list);
        manager.insertReadingList(list);
        pm.commit();
    }

    /**
     * Tests insertion of a reading list.
     */
    public void testInsert()
        throws SQLException, PersistenceException
    {
        assertFalse("List wasn't inserted.", list.getID() == -1);

        assertReadingList(list);
    }

    /**
     * Tests insertion of a reading list with some assigned feeds.
     */
    public void testInsertWithFeeds()
        throws SQLException, PersistenceException
    {
        assertFalse("List wasn't inserted.", list.getID() == -1);

        // Insert feed linked with the reading list
        DirectFeed feed0 = new DirectFeed();
        feed0.setXmlURL(getTestURL());
        feed0.setBaseTitle("0");
        pm.insertFeed(feed0);

        list.add(feed0);
        pm.addFeedToReadingList(list, feed0);

        // Insert feed not linked with the reading list
        DirectFeed feed1 = new DirectFeed();
        feed1.setXmlURL(getTestURL());
        feed1.setBaseTitle("1");
        pm.insertFeed(feed1);

        guide.add(feed1);
        pm.addFeedToGuide(guide, feed1);

        // Load everything back for verification
        GuidesSet set = new GuidesSet();
        pm.loadGuidesSet(set);

        assertEquals(1, set.getGuidesCount());

        StandardGuide gd = (StandardGuide)set.getGuideAt(0);
        ReadingList[] lists = gd.getReadingLists();
        assertEquals(1, lists.length);

        DirectFeed[] associatedFeeds = lists[0].getFeeds();
        assertEquals(1, associatedFeeds.length);
        assertEquals(feed0.getID(), associatedFeeds[0].getID());

        assertEquals(2, gd.getFeedsCount());
    }

    /**
     * Tests updates of a reading list.
     */
    public void testUpdate()
        throws SQLException, PersistenceException
    {
        list.setTitle("A");
        list.setLastPollTime(1);
        list.setLastUpdateServerTime(2);
        manager.updateReadingList(list);

        assertReadingList(list);
    }

    /**
     * Tests removal of a reading list.
     */
    public void testRemove()
        throws SQLException, PersistenceException
    {
        manager.removeReadingList(list);
        pm.commit();

        assertEquals("ID should be cleared.", -1, list.getID());

        GuidesSet gs = new GuidesSet();
        pm.loadGuidesSet(gs);
        assertEquals(1, gs.getGuidesCount());
        IGuide guide = gs.getGuideAt(0);
        assertEquals(0, ((StandardGuide)guide).getReadingLists().length);
    }

    /**
     * Tests removing a reading list with some direct feeds assigned. Those
     * feeds should be smoothly converted into manual feeds by clearing
     * ReadingListID field.
     */
    public void testRemoveWithFeeds()
        throws PersistenceException, SQLException
    {
        // We add a feed to the guide associated with reading list
        DirectFeed feed = new DirectFeed();
        feed.setBaseTitle("0");
        list.add(feed);
        pm.insertFeed(feed);

        // We remove reading list without removing feeds
        guide.remove(list, false);
        assertEquals(1, guide.getFeedsCount());
        manager.removeReadingList(list);
        pm.commit();

        // Now we check what has left there in database
        GuidesSet gs = new GuidesSet();
        pm.loadGuidesSet(gs);
        assertEquals(1, gs.getGuidesCount());
        StandardGuide nguide = (StandardGuide)gs.getGuideAt(0);
        assertEquals(0, nguide.getReadingLists().length);
        assertEquals(0, nguide.getFeedsCount());
    }

    /**
     * Tests removing feeds from reading list.
     */
    public void testRemoveFeedsFromReadingList()
        throws PersistenceException
    {
        // We add a feed to the guide associated with reading list
        DirectFeed feed = new DirectFeed();
        feed.setBaseTitle("0");
        pm.insertFeed(feed);

        list.add(feed);
        pm.addFeedToReadingList(list, feed);

        // We remove feed from the reading list
        list.remove(feed);
        pm.removeFeedFromReadingList(list, feed);

        assertEquals(0, guide.getFeedsCount());
        assertEquals(0, list.getFeeds().length);
    }

    /**
     * Returns simple URL.
     *
     * @return simple URL.
     */
    private URL getTestURL()
    {
        URL testURL = null;

        try
        {
            testURL = new URL("http://localhost/");
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
            fail();
        }

        return testURL;
    }

    /**
     * Checks that there's the only reading list in the guide set and it
     * matches the target reading list specified.
     *
     * @param aTarget reading list to match against.
     */
    private void assertReadingList(ReadingList aTarget)
        throws PersistenceException
    {
        GuidesSet gs = new GuidesSet();
        pm.loadGuidesSet(gs);
        assertEquals(1, gs.getGuidesCount());
        IGuide guide = gs.getGuideAt(0);
        assertEquals(1, ((StandardGuide)guide).getReadingLists().length);
        assertEquals(aTarget, ((StandardGuide)guide).getReadingLists()[0]);
    }
}
