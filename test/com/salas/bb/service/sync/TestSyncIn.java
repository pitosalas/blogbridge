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
// $Id: TestSyncIn.java,v 1.33 2007/02/07 15:33:43 spyromus Exp $
//

package com.salas.bb.service.sync;

import com.jgoodies.uif.application.Application;
import com.jgoodies.uif.application.ApplicationConfiguration;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.domain.*;
import com.salas.bb.domain.query.ICriteria;
import com.salas.bb.domain.query.articles.ArticleTextProperty;
import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.domain.query.general.StringEqualsCO;
import com.salas.bb.domain.querytypes.QueryType;
import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @see SyncIn
 */
public class TestSyncIn extends TestCase
{
    private GuidesSet local;
    private GuidesSet remote;

    static {
        Application.setConfiguration(new ApplicationConfiguration(
                "bb/test",                  // Root node for prefs and logs
                "",                      // resource.properties URL
                "docs/Help.hs",          // Helpset URL
                "docs/tips/index.txt"));
        ResourceUtils.setBundle(ResourceBundle.getBundle("Resource"));
    }
    
    protected void setUp()
        throws Exception
    {
        super.setUp();
        local = new GuidesSet();
        remote = new GuidesSet();
    }

    // Evaluate changes ----------------------------------------------------------------------------

    /**
     * Test a guide and a feed added remotely.
     */
    public void testAddedRemotely()
    {
        // local is empty, remote has one guide and one feed
        StandardGuide guide = guide("a");
        DirectFeed feed = directFeed(1);
        guide.add(feed);
        remote.add(guide);

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);
        assertChanges(changes, 0, 0, 1, 0);
        assertTrue(feed == changes.getAddFeeds().get(0));
    }

    /**
     * Tests comparing equal layouts. Local and remote feeds are the same.
     */
    public void testLocalIsTheSame()
    {
        // local
        StandardGuide guide1 = guide("a");
        guide1.add(directFeed(1));
        local.add(guide1);

        StandardGuide guide2 = guide("b");
        guide2.add(directFeed(2));
        guide2.add(queryFeed(2));
        guide2.add(searchFeed(2));
        local.add(guide2);

        // remote
        StandardGuide guide1r = guide("a");
        guide1r.add(directFeed(1));
        remote.add(guide1r);

        StandardGuide guide2r = guide("b");
        guide2r.add(directFeed(2));
        guide2r.add(queryFeed(2));
        guide2r.add(searchFeed(2));
        remote.add(guide2r);

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);
        assertChanges(changes, 0, 0, 0, 0);
    }

    /**
     * The feed has already been known to the service and was removed
     * remotely.
     */
    public void testRemovedRemotelySync()
    {
        // local
        StandardGuide guide1 = guide("a");
        DirectFeed feed = directFeed(1);
        addAndMarkAsSynced(guide1, feed);
        local.add(guide1);

        // remote
        remote.add(guide("a"));

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);
        assertChanges(changes, 0, 0, 0, 1);
        assertRemoveFeed(guide1, feed, changes, 0);
    }

    /**
     * Tests the case when user adds a feed locally between two synchronizations.
     * The application suggests no removal because it knows that the feed was
     * added.
     */
    public void testAddedLocally()
    {
        // local
        StandardGuide guide1 = guide("a");
        DirectFeed feed = directFeed(1);
        guide1.add(feed);
        local.add(guide1);

        // remote
        remote.add(guide("a"));

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);
        assertChanges(changes, 0, 0, 0, 0);
    }

    /**
     * Tests the case when user adds a feed locally between two synchronizations.
     * The application suggests removal of the local feed as we wish to copy the service layout.
     */
    public void testAddedLocallyCopyService()
    {
        // local
        StandardGuide guide1 = guide("a");
        DirectFeed feed = directFeed(1);
        guide1.add(feed);
        local.add(guide1);

        // remote
        remote.add(guide("a"));

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, true);
        assertChanges(changes, 0, 0, 0, 1);
    }

    /**
     * There was a guide with one feed and it has been removed remotely.
     */
    public void testRemovedGuideRemotely()
    {
        // local
        StandardGuide guide1 = guide("a");
        DirectFeed feed = directFeed(1);
        addAndMarkAsSynced(guide1, feed);
        local.add(guide1);

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);
        assertChanges(changes, 0, 0, 0, 1);
    }

    /**
     * When one feed is removed from the guide and the similar feed is added
     * to the other guide, we would better reuse the feed instead of flushing
     * read states and other info.
     */
    public void testRepositioning()
    {
        // local
        StandardGuide guide1 = guide("a");
        DirectFeed feed = directFeed(1);
        addAndMarkAsSynced(guide1, feed);
        local.add(guide1);

        // remote
        StandardGuide guide2 = guide("b");
        DirectFeed feed2 = directFeed(1);
        addAndMarkAsSynced(guide2, feed2);
        remote.add(guide2);

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);
        assertChanges(changes, 0, 0, 1, 1);
    }

    // Evaluate Changes within Guide Feeds ---------------------------------------------------------

    /**
     * Tests detection of a feed added remotely.
     */
    public void testECFAddedRemotely()
    {
        SyncIn.Changes changes = new SyncIn.Changes();

        StandardGuide local = guide("a");
        StandardGuide remote = guide("a");
        remote.add(directFeed(1));
        remote.add(queryFeed(1));
        remote.add(searchFeed(1));

        // --- Checking
        SyncIn.evaluateChangesInGuide(local, remote, changes, false);
        assertChanges(changes, 0, 0, 3, 0);
    }

    /**
     * Testing how guide properties are transferred when adding or updating guide.
     *
     * NOTE: Use this test to check how the properties are transferred when adding a new guide or updating
     *       from the service.
     */
    public void testTransferGuideProperties()
    {
        StandardGuide pattern = new StandardGuide();
        pattern.setPublishingRating(3);

        // Creating guide from remote pattern
        StandardGuide guide = new StandardGuide();
        SyncIn.transferGuideProperties(guide, pattern);
        assertEquals(pattern.getPublishingRating(), guide.getPublishingRating());
    }

    /**
     * Tests detection of a feed added remotely (checking properties transfer).
     */
    public void testECFAddedRemotelyPropertiesTransfer()
    {
        SyncIn.Changes changes = new SyncIn.Changes();

        // Empty local
        StandardGuide localG = guide("a");
        local.add(localG);

        // 3 Feeds remotely
        StandardGuide remoteG = guide("a");
        DirectFeed dFeed = directFeed(1);
        dFeed.setPinnedArticlesKeys("a,b");
        QueryFeed qFeed = queryFeed(1);
        qFeed.setPinnedArticlesKeys("c,d");
        SearchFeed sFeed = searchFeed(1);
        remoteG.add(dFeed);
        remoteG.add(qFeed);
        remoteG.add(sFeed);

        // --- Checking
        SyncIn.evaluateChangesInGuide(localG, remoteG, changes, false);
        SyncIn.performChanges(local, changes, null);

        IFeed[] lFeeds = localG.getFeeds();
        assertFeed(dFeed, qFeed, lFeeds[0]);
        assertFeed(dFeed, qFeed, lFeeds[1]);
        assertFeed(dFeed, qFeed, lFeeds[2]);
    }

    /**
     * Checks how the properties match depending on the type of the feed.
     *
     * @param dFeed     direct feed to check against if the target is of DirectFeed type.
     * @param qFeed     query feed to check against if the target is of QueryFeed type.
     * @param target    target feed.
     */
    private void assertFeed(DirectFeed dFeed, QueryFeed qFeed, IFeed target)
    {
        if (target instanceof DirectFeed)
        {
            assertEquals(dFeed.getPinnedArticlesKeys(), ((DirectFeed)target).getPinnedArticlesKeys());
        } else if (target instanceof QueryFeed)
        {
            assertEquals(qFeed.getPinnedArticlesKeys(), ((QueryFeed)target).getPinnedArticlesKeys());
        }
    }

    /**
     * Tests detection of a feed added locally.
     */
    public void testECFAddedLocally()
    {
        SyncIn.Changes changes = new SyncIn.Changes();

        StandardGuide local = guide("a");
        local.add(directFeed(1));
        local.add(queryFeed(1));
        local.add(searchFeed(1));
        StandardGuide remote = guide("a");

        // --- Checking
        SyncIn.evaluateChangesInGuide(local, remote, changes, false);
        assertChanges(changes, 0, 0, 0, 0);
    }

    /**
     * Tests detection of a feed removed remotely.
     */
    public void testECFRemovedRemotely()
    {
        SyncIn.Changes changes = new SyncIn.Changes();

        StandardGuide local = guide("a");
        addAndMarkAsSynced(local, directFeed(1));
        addAndMarkAsSynced(local, queryFeed(1));
        addAndMarkAsSynced(local, searchFeed(1));
        StandardGuide remote = guide("a");

        // --- Checking
        SyncIn.evaluateChangesInGuide(local, remote, changes, false);
        assertChanges(changes, 0, 0, 0, 3);
    }

    /**
     * Tests removal of new feeds added only locally.
     */
    public void testECFAddedLocallyClearNew()
    {
        SyncIn.Changes changes = new SyncIn.Changes();

        StandardGuide local = guide("a");
        local.add(directFeed(1));
        local.add(queryFeed(1));
        local.add(searchFeed(1));
        StandardGuide remote = guide("a");

        // --- Checking
        SyncIn.evaluateChangesInGuide(local, remote, changes, true);
        assertChanges(changes, 0, 0, 0, 3);
    }

    // Reading Lists -------------------------------------------------------------------------------

    /**
     * A guide with reading list has been added remotely.
     */
    public void testRLAddedGuideRemotely()
    {
        // remote: one guide, one reading list, one feed
        StandardGuide guide = guide("a");
        remote.add(guide);
        ReadingList list = readingList(1);
        guide.add(list);
        DirectFeed feed = directFeed(1);
        list.add(feed);

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);
        assertChanges(changes, 1, 0, 0, 0);
    }

    /**
     * A reading list has been added to a guide remotely. The reading list has one feed
     * and we add this feed as well as the reading list record.
     */
    public void testRLAddedRemotely()
    {
        // local: one guide, one feed
        StandardGuide guideL = guide("a");
        local.add(guideL);
        DirectFeed feedL = directFeed(0);
        addAndMarkAsSynced(guideL, feedL);

        // remote: one guide, one reading list, two feeds (one new)
        StandardGuide guideR = guide("a");
        remote.add(guideR);
        ReadingList list = readingList(1);
        guideR.add(list);
        DirectFeed feedR = directFeed(1);
        list.add(feedR);
        guideR.add(directFeed(0));

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);
        assertChanges(changes, 1, 0, 0, 0);
    }

    /**
     * Testing the situation when reading list has been removed remotely.
     */
    public void testRLRemovedRemotely()
    {
        // local: one guide, one feed
        StandardGuide guideL = guide("a");
        local.add(guideL);
        ReadingList list = readingList(1, true);
        guideL.add(list);
        DirectFeed feedL = directFeed(1);
        list.add(feedL);
        addAndMarkAsSynced(guideL, directFeed(0));

        // remote: one guide, one reading list, two feeds (one new)
        StandardGuide guideR = guide("a");
        remote.add(guideR);
        guideR.add(directFeed(0));

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);
        assertChanges(changes, 0, 1, 0, 0);
    }

    /**
     * We test a situation when there was a guide with reading list assigned, then user
     * saved it to the service, renamed guide and restoring it back.
     *
     * What should happen is that we need to: create a guide with correct name, move the
     * reading list.
     */
    public void testRLRenamedGuide()
    {
        // local
        StandardGuide guideL = guide("b");
        local.add(guideL);
        ReadingList listL = readingList(1, true);
        guideL.add(listL);
        DirectFeed feedL = directFeed(2);
        listL.add(feedL);

        // remote
        StandardGuide guideR = guide("a");
        remote.add(guideR);
        ReadingList listR = readingList(1);
        guideR.add(listR);
        DirectFeed feedR = directFeed(2);
        listR.add(feedR);

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);
        assertChanges(changes, 1, 1, 0, 0);
    }

    /**
     * We test a situation when there was a guide with reading list assigned, then user
     * saved it to the service, renamed guide and restoring it back.
     *
     * What should happen is that we need to: create a guide with correct name, move the
     * reading list. There's another new feed in this list recorded remotely -- we should
     * add it.
     */
    public void testRLRenamedGuideNewFeed()
    {
        // local
        StandardGuide guideL = guide("b");
        local.add(guideL);
        ReadingList listL = readingList(1, true);
        guideL.add(listL);
        DirectFeed feedL = directFeed(2);
        listL.add(feedL);

        // remote
        StandardGuide guideR = guide("a");
        remote.add(guideR);
        ReadingList listR = readingList(1);
        guideR.add(listR);
        DirectFeed feedR = directFeed(2);
        listR.add(feedR);
        DirectFeed feedR2 = directFeed(3);
        listR.add(feedR2);

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);
        assertChanges(changes, 1, 1, 0, 0);
    }

    /**
     * We test a situation when there was a guide with reading list assigned, then user
     * saved it to the service, renamed guide and restoring it back.
     *
     * What should happen is that we need to: create a guide with correct name, move the
     * reading list. There were two feeds and now one of them removed -- act appropriately.
     */
    public void testRLRenamedGuideRemovedFeed()
    {
        // local
        StandardGuide guideL = guide("b");
        local.add(guideL);
        ReadingList listL = readingList(1, true);
        guideL.add(listL);
        DirectFeed feedL = directFeed(2);
        listL.add(feedL);
        DirectFeed feedL2 = directFeed(3);
        listL.add(feedL2);

        // remote
        StandardGuide guideR = guide("a");
        remote.add(guideR);
        ReadingList listR = readingList(1);
        guideR.add(listR);
        DirectFeed feedR = directFeed(2);
        listR.add(feedR);

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);

        assertChanges(changes, 1, 1, 0, 0);
    }

    private void assertRemoveFeed(StandardGuide guide, DirectFeed feed, SyncIn.Changes changes,
        int index)
    {
        SyncIn.GuideFeedPair pair = (SyncIn.GuideFeedPair)changes.getRemoveFeeds().get(index);
        assertTrue(feed == pair.feed);
        assertTrue(guide == pair.guide);
    }

    /**
     * We have two guides: "a" (local) - with 2 reading lists, "b" (remote) - with static feed.
     * The "a" guide gets removed and "b" guide gets one similar reading list plus another feed.
     * Interesting part that the "b" guide has no feeds associated with reading list because it
     * isn't fetched yet, so the feeds associated with reading list from "a" guide should be
     * removed.
     *
     * The other reading list and the guide "a" should be removed.
     */
    public void testRLReusing()
    {
        // local - reading list with two feeds
        StandardGuide guideL = guide("a");
        local.add(guideL);
        ReadingList listL = readingList(1, true);
        guideL.add(listL);
        DirectFeed feedL = directFeed(2);
        listL.add(feedL);
        DirectFeed feedL2 = directFeed(3);
        listL.add(feedL2);

        // remote - reading list without feeds + static feed
        StandardGuide guideR = guide("b");
        remote.add(guideR);
        ReadingList listR = readingList(1);
        guideR.add(listR);
        DirectFeed feedR = directFeed(2);
        guideR.add(feedR);

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);

        assertChanges(changes, 1, 1, 1, 0);
    }

    private static void assertChanges(SyncIn.Changes aChanges, int addRL, int removeRL,
        int addFeeds, int removeFeeds)
    {
        assertEquals("Wrong number of RL's to add",
            addRL, aChanges.getAddReadingLists().size());
        assertEquals("Wrong number of RL's to remove",
            removeRL, aChanges.getRemoveReadingLists().size());

        assertEquals("Wrong number of feeds to add",
            addFeeds, aChanges.getAddFeeds().size());
        assertEquals("Wrong number of feeds to remove",
            removeFeeds, aChanges.getRemoveFeeds().size());
    }

    // Evaluate Changes within Guide Reading Lists -------------------------------------------------

    /**
     * A reading list is added locally, but not synchronized yet.
     */
    public void testECRLAddedLocally()
    {
        // local - one reading list + associated feed
        StandardGuide guideL = guide("a");
        ReadingList listL = readingList(1);
        guideL.add(listL);
        listL.add(directFeed(2));

        // remote
        StandardGuide guideR = guide("a");

        // --- Checking
        SyncIn.Changes changes = new SyncIn.Changes();
        SyncIn.evaluateChangesInReadingLists(guideL, guideR, changes, false);
        assertChanges(changes, 0, 0, 0, 0);
    }

    /**
     * A feed is added to the reading list is added locally, but not synchronized yet.
     * The application should report removal when we ask to copy service layout.
     */
    public void testECRLAddedLocallyCopyService()
    {
        // TODO This test makes sense only if we also synchronize the feed lists of RL's

        // local - one reading list + associated feed
//        StandardGuide guideL = guide("a");
//        ReadingList listL = readingList(1);
//        guideL.add(listL);
//        listL.add(directFeed(1));
//        listL.add(directFeed(2));
//
//        // remote
//        StandardGuide guideR = guide("a");
//        ReadingList listR = readingList(1);
//        guideR.add(listR);
//        listR.add(directFeed(2));
//
//        // --- Checking
//        SyncIn.Changes changes = new SyncIn.Changes();
//        SyncIn.evaluateChangesInGuide(guideL, guideR, changes, true);
//        assertChanges(changes, 0, 0, 0, 1);
    }

    /**
     * Testing that disregarding the fact that newly added reading list hasn't been synchronized
     * yet, it is removed because of clear-flag set.
     */
    public void testECRLAddedLocallyClear()
    {
        // local - one reading list + associated feed
        StandardGuide guideL = guide("a");
        ReadingList listL = readingList(1);
        guideL.add(listL);
        listL.add(directFeed(2));

        // remote
        StandardGuide guideR = guide("a");

        // --- Checking
        SyncIn.Changes changes = new SyncIn.Changes();
        SyncIn.evaluateChangesInReadingLists(guideL, guideR, changes, true);
        assertChanges(changes, 0, 1, 0, 0);
    }

    /**
     * Testing that remotely added RL is added to the guide.
     */
    public void testECRLAddedRemotely()
    {
        // local
        StandardGuide guideL = guide("a");

        // remote - one reading list + associated feed
        StandardGuide guideR = guide("a");
        ReadingList listR = readingList(1);
        guideR.add(listR);
        listR.add(directFeed(2));

        // --- Checking
        SyncIn.Changes changes = new SyncIn.Changes();
        SyncIn.evaluateChangesInReadingLists(guideL, guideR, changes, false);
        assertChanges(changes, 1, 0, 0, 0);
    }

    /**
     * Testing that remotely removed RL is marked for removal.
     */
    public void testECRLRemovedRemotely()
    {
        // local
        StandardGuide guideL = guide("a");
        ReadingList listL = readingList(1, true);
        guideL.add(listL);
        listL.add(directFeed(2));

        // remote - one reading list + associated feed
        StandardGuide guideR = guide("a");

        // --- Checking
        SyncIn.Changes changes = new SyncIn.Changes();
        SyncIn.evaluateChangesInReadingLists(guideL, guideR, changes, false);
        assertChanges(changes, 0, 1, 0, 0);
    }

    /**
     * When there are two similar reading lists reported for addition from the service
     * and we already have at least one of them locally we shouldn't add any.
     */
    public void testECRLLocalDuplicate()
    {
        // local
        StandardGuide guideL = guide("a");
        ReadingList listL = readingList(1, true);
        guideL.add(listL);
        listL.add(directFeed(2));

        // remote
        StandardGuide guideR = guide("a");
        ReadingList listR = readingList(1, true);
        guideR.add(listR);
        listR.add(directFeed(2));
        ReadingList listR2 = readingList(1, true);
        guideR.add(listR2);
        listR2.add(directFeed(2));

        // --- Checking
        SyncIn.Changes changes = new SyncIn.Changes();
        SyncIn.evaluateChangesInReadingLists(guideL, guideR, changes, false);
        assertChanges(changes, 0, 0, 0, 0);
    }

    // Resusing ------------------------------------------------------------------------------------

    public void testReusing1()
    {
        // We should never reuse a feed from some reading list when removing reading list
        // and adding the same feed somewhere.
    }

    // Perform changes -----------------------------------------------------------------------------

    /**
     * When the synchronization is over it may come that some guides become empty.
     * It happens generally because the feeds from them were moved here and there.
     * If it is true that the empty guide is not on remote list, it's also true
     * that it can be safely removed.
     */
    public void testRemovingEmptyRedundantGuides()
    {
        // local
        StandardGuide guide1 = guide("a");
        DirectFeed feed = directFeed(1);
        addAndMarkAsSynced(guide1, feed);
        local.add(guide1);

        // remote
        StandardGuide guide2 = guide("b");
        DirectFeed feed2 = directFeed(1);
        addAndMarkAsSynced(guide2, feed2);
        remote.add(guide2);

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);
        SyncIn.performChanges(local, changes, null);

        assertEquals(1, local.getGuidesCount());
        assertEquals("b", (local.getGuideAt(0)).getTitle());
    }

    /**
     * Two guides with two different reading lists, but the same feeds are added.
     */
    public void testPerformChangesRLAddedRemotely()
    {
        // remote - reading list without feeds + static feed
        StandardGuide guideR = guide("a");
        remote.add(guideR);
        ReadingList listR = readingList(1);
        guideR.add(listR);
        DirectFeed feedR = directFeed(1);
        listR.add(feedR);

        StandardGuide guideR2 = guide("b");
        remote.add(guideR2);
        ReadingList listR2 = readingList(2);
        guideR2.add(listR2);
        DirectFeed feedR2 = directFeed(1);
        listR2.add(feedR2);

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);
        SyncIn.performChanges(local, changes, null);

        assertEquals(2, local.getGuidesCount());

        IGuide guidea = local.getGuideAt(0);
        assertEquals("a", guidea.getTitle());
        assertEquals(1, guidea.getFeedsCount());
        DirectFeed feedA = (DirectFeed)guidea.getFeedAt(0);
        assertFalse("There should be no direct link.", guidea.hasDirectLinkWith(feedA));
        assertEquals(feedR.getXmlURL().toString(), feedA.getXmlURL().toString());
        ReadingList listA = feedA.getReadingLists()[0];
        ReadingList[] listsA = ((StandardGuide)guidea).getReadingLists();
        assertEquals(1, listsA.length);
        assertTrue(listA == listsA[0]);

        IGuide guideb = local.getGuideAt(1);
        assertEquals("b", guideb.getTitle());
        assertEquals(1, guideb.getFeedsCount());
        DirectFeed feedB = (DirectFeed)guideb.getFeedAt(0);
        assertFalse("There should be no direct link.", guideb.hasDirectLinkWith(feedB));
        assertEquals(feedR2.getXmlURL().toString(), feedB.getXmlURL().toString());
        ReadingList listB = feedB.getReadingLists()[1];
        ReadingList[] listsB = ((StandardGuide)guideb).getReadingLists();
        assertEquals(1, listsB.length);
        assertTrue(listB == listsB[0]);
    }

    /**
     * Reusing feeds for building other reading lists.
     */
    public void testPerformChangesRLReuse()
    {
        // local
        StandardGuide guideL = guide("a");
        local.add(guideL);
        ReadingList listL1 = readingList(1, true);
        guideL.add(listL1);
        DirectFeed feedL1 = directFeed(1);
        listL1.add(feedL1);

        ReadingList listL2 = readingList(2);
        guideL.add(listL2);
        DirectFeed feedL21 = directFeed(1);
        listL2.add(feedL21);
        DirectFeed feedL22 = directFeed(2);
        listL2.add(feedL22);

        // remote
        StandardGuide guideR = guide("a");
        remote.add(guideR);
        ReadingList listR = readingList(3);
        guideR.add(listR);
        DirectFeed feedR = directFeed(1);
        listR.add(feedR);

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);
        SyncIn.performChanges(local, changes, null);

        assertEquals(1, local.getGuidesCount());

        StandardGuide guide = (StandardGuide)local.getGuideAt(0);
        assertEquals(3, guide.getFeedsCount());

        ReadingList[] lists = guide.getReadingLists();
        assertEquals(2, lists.length);

        ReadingList list1 = lists[0];
        assertEquals(listL2.getURL().toString(), list1.getURL().toString());
        assertEquals(2, list1.getFeeds().length);

        ReadingList list2 = lists[1];
        assertEquals(listR.getURL().toString(), list2.getURL().toString());
        assertEquals(1, list2.getFeeds().length);
    }

    /**
     * An old guide with old feed is replaced with new guide and new feeds.
     */
    public void testPerformChangesRLReplacing()
    {
        // local
        StandardGuide guideL = guide("a");
        local.add(guideL);
        ReadingList listL1 = readingList(1, true);
        guideL.add(listL1);
        DirectFeed feedL1 = directFeed(1);
        listL1.add(feedL1);

        // remote
        StandardGuide guideR = guide("b");
        remote.add(guideR);
        ReadingList listR = readingList(1);
        guideR.add(listR);
        DirectFeed feedR = directFeed(1);
        listR.add(feedR);
        guideR.add(directFeed(1));

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);
        SyncIn.performChanges(local, changes, null);

        assertEquals(1, local.getGuidesCount());

        StandardGuide guide = (StandardGuide)local.getGuideAt(0);
        assertEquals(1, guide.getFeedsCount());

        ReadingList[] lists = guide.getReadingLists();
        assertEquals(1, lists.length);

        ReadingList list1 = lists[0];
        assertEquals(1, list1.getFeeds().length);
    }

    /**
     * User added new guide "a" and reading list "1" with one feed "1". Next, he loads
     * his guide "b" with readig list "1", feed "1" and static feed "1". All feeds and guides
     * should be combined.
     */
    public void testPerformChangesRLAdding()
    {
        // local
        StandardGuide guideL = guide("a");
        local.add(guideL);
        ReadingList listL1 = readingList(1);
        guideL.add(listL1);
        DirectFeed feedL1 = directFeed(1);
        listL1.add(feedL1);

        // remote
        StandardGuide guideR = guide("b");
        remote.add(guideR);
        ReadingList listR = readingList(1);
        guideR.add(listR);
        DirectFeed feedR = directFeed(1);
        listR.add(feedR);
        guideR.add(directFeed(1));

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);
        SyncIn.performChanges(local, changes, null);

        assertEquals(2, local.getGuidesCount());

        StandardGuide guide = (StandardGuide)local.getGuideAt(0);
        assertEquals(1, guide.getFeedsCount());
        ReadingList[] lists = guide.getReadingLists();
        assertEquals(1, lists.length);
        ReadingList list1 = lists[0];
        assertEquals(1, list1.getFeeds().length);

        StandardGuide guide2 = (StandardGuide)local.getGuideAt(1);
        assertEquals(1, guide2.getFeedsCount());
        ReadingList[] lists2 = guide2.getReadingLists();
        assertEquals(1, lists2.length);
        ReadingList list12 = lists2[0];
        assertEquals(1, list12.getFeeds().length);
    }

    /**
     * Tests converting reading list feed to normal.
     */
    public void testPerformChangesRLFeedConversion()
    {
        // local
        StandardGuide guideL = guide("a");
        local.add(guideL);
        ReadingList listL1 = readingList(1, true);
        guideL.add(listL1);
        DirectFeed feedL1 = directFeed(1);
        listL1.add(feedL1);

        // remote
        StandardGuide guideR = guide("a");
        remote.add(guideR);
        DirectFeed feedR = directFeed(1);
        guideR.add(feedR);

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);
        SyncIn.performChanges(local, changes, null);

        assertEquals(1, local.getGuidesCount());

        StandardGuide guide = (StandardGuide)local.getGuideAt(0);
        assertEquals(1, guide.getFeedsCount());
        ReadingList[] lists = guide.getReadingLists();
        assertEquals(0, lists.length);

        DirectFeed feed = (DirectFeed)guide.getFeedAt(0);
        assertEquals("There should be no connection to reading list.",
            0, feed.getReadingLists().length);
    }

    // Read state sync -----------------------------------------------------------------------------

    /**
     * Local feed has 2 articles with one of them read. Remotely user read both of articles
     * and he synchronizes the state back from service. Both articles should be marked as read.
     */
    public void testReadState()
    {
        // Local
        StandardGuide guideL = guide("a");
        local.add(guideL);
        DirectFeed feedL = directFeed(1);
        addAndMarkAsSynced(guideL, feedL);
        StandardArticle artL1 = new StandardArticle("a");
        feedL.appendArticle(artL1);
        StandardArticle artL2 = new StandardArticle("b");
        feedL.appendArticle(artL2);
        feedL.setInitTime(1L);
        artL1.setRead(true);

        // Create illusion that remotely we have both articles read
        artL2.setRead(true);
        String remoteKeys = feedL.getReadArticlesKeys();
        artL2.setRead(false);

        // Remote
        StandardGuide guideR = guide("a");
        remote.add(guideR);
        DirectFeed feedR = directFeed(1);
        addAndMarkAsSynced(guideR, feedR);
        feedR.setReadArticlesKeys(remoteKeys);
        assertEquals(remoteKeys, feedR.getReadArticlesKeys());

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);

        assertEquals(1, changes.getUpdateFeedsKeys().size());

        SyncIn.performChanges(local, changes, null);
        assertEquals(0, feedL.getUnreadArticlesCount());
    }

    // Pin state sync ------------------------------------------------------------------------------

    /**
     * Local feed has 2 articles with one of them pinned. Remotely user pinned both of articles
     * and he synchronizes the state back from service. Both articles should be marked as pinned.
     */
    public void testPinnedState()
    {
        // Local
        StandardGuide guideL = guide("a");
        local.add(guideL);
        DirectFeed feedL = directFeed(1);
        addAndMarkAsSynced(guideL, feedL);
        StandardArticle artL1 = new StandardArticle("a");
        feedL.appendArticle(artL1);
        StandardArticle artL2 = new StandardArticle("b");
        feedL.appendArticle(artL2);
        feedL.setInitTime(1L);
        artL1.setPinned(true);

        // Create illusion that remotely we have both articles read
        artL2.setPinned(true);
        String remoteKeys = feedL.getPinnedArticlesKeys();
        artL2.setPinned(false);

        // Remote
        StandardGuide guideR = guide("a");
        remote.add(guideR);
        DirectFeed feedR = directFeed(1);
        addAndMarkAsSynced(guideR, feedR);
        feedR.setPinnedArticlesKeys(remoteKeys);
        assertEquals(remoteKeys, feedR.getPinnedArticlesKeys());

        // --- Checking
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);

        assertEquals(1, changes.getUpdateFeedsKeys().size());

        SyncIn.performChanges(local, changes, null);
        IArticle[] articles = feedL.getArticles();
        int pinned = 0;
        for (int i = 0; i < articles.length; i++) if (articles[i].isPinned()) pinned++;
        assertEquals(1, pinned);
    }

    // View type / mode sync -----------------------------------------------------------------------

    public void testViewTypeModeSyncDirect()
    {
        // Local
        StandardGuide guideL = guide("a");
        local.add(guideL);
        DirectFeed feedL = directFeed(1);
        addAndMarkAsSynced(guideL, feedL);
        feedL.setInitTime(1L);
        feedL.setCustomViewModeEnabled(false);
        feedL.setCustomViewMode(1);
        feedL.setType(FeedType.IMAGE);

        // Remote
        StandardGuide guideR = guide("a");
        remote.add(guideR);
        DirectFeed feedR = directFeed(1);
        addAndMarkAsSynced(guideR, feedR);
        feedR.setCustomViewModeEnabled(true);
        feedR.setCustomViewMode(0);
        feedR.setType(FeedType.TEXT);

        // Update
        // Rewind update time a bit so that the remote version is newer
        feedL.setLastUpdateTime(System.currentTimeMillis() - 1000);
        SyncIn.Changes changes = SyncIn.evaluateChanges(local, remote, false);
        assertEquals(1, changes.getUpdateFeedsKeys().size());
        SyncIn.performChanges(local, changes, null);

        // Check
        assertEquals(0, feedL.getCustomViewMode());
        assertTrue(feedL.isCustomViewModeEnabled());
        assertEquals(FeedType.TEXT, feedL.getType());
    }

    // Hashes --------------------------------------------------------------------------------------

    /**
     * Checking the feeds matching using the saved sync hash.
     */
    public void testFeedsAreTheSame_Direct()
    {
        // No hash
        DirectFeed d1 = directFeed(1);
        DirectFeed d2 = directFeed(1);
        assertTrue(SyncIn.feedsAreTheSameDirect(d1, d2));

        // Wrong URL but hash
        d2 = directFeed(2);
        d2.setSyncHash(d1.calcSyncHash());
        assertTrue(SyncIn.feedsAreTheSameDirect(d1, d2));

        // Wrong URL, wrong hash
        d2 = directFeed(2);
        assertFalse(SyncIn.feedsAreTheSameDirect(d1, d2));
    }

    // Utilities -----------------------------------------------------------------------------------

    /**
     * Creates a reading list with URL having identifier specified.
     *
     * @param n identifier.
     *
     * @return reading list.
     */
    private ReadingList readingList(int n)
    {
        return readingList(n, false);
    }

    /**
     * Creates a reading list with URL having identifier specified.
     *
     * @param n identifier.
     * @param synced <code>TRUE</code> to mark as synchronized.
     *
     * @return reading list.
     */
    private ReadingList readingList(int n, boolean synced)
    {
        ReadingList list = new ReadingList(getURL(n));
        if (synced) list.setLastSyncTime(1L);
        return list;
    }

    /**
     * Creates a guide with the given title.
     *
     * @param name title.
     *
     * @return guide.
     */
    private StandardGuide guide(String name)
    {
        StandardGuide guide = new StandardGuide();
        guide.setTitle(name);
        return guide;
    }

    /**
     * Creates a search feed with a title and query both having an identifier.
     *
     * @param n         identifier.
     *
     * @return search feed.
     */
    private SearchFeed searchFeed(int n)
    {
        Query query = new Query();
        ICriteria criteria = query.addCriteria();
        criteria.setProperty(ArticleTextProperty.INSTANCE);
        criteria.setComparisonOperation(StringEqualsCO.INSTANCE);
        criteria.setValue(Integer.toString(n));

        SearchFeed feed = new SearchFeed();
        feed.setBaseTitle(Integer.toString(n));
        feed.setQuery(query);

        return feed;
    }

    /**
     * Creates a query feed with a title and query both having an identifier.
     *
     * @param n         identifier.
     *
     * @return query feed.
     */
    private QueryFeed queryFeed(int n)
    {
        QueryFeed feed = new QueryFeed();
        feed.setBaseTitle(Integer.toString(n));
        feed.setQueryType(QueryType.getQueryType(QueryType.TYPE_AMAZON_BOOKS));
        feed.setParameter("test");

        return feed;
    }

    /**
     * Creates a direct feed with a name and URL both including the identifier.
     *
     * @param n         identifier.
     *
     * @return direct feed.
     */
    private DirectFeed directFeed(int n)
    {
        DirectFeed feed = new DirectFeed();
        feed.setBaseTitle(Integer.toString(n));
        feed.setXmlURL(getURL(n));

        return feed;
    }

    /**
     * Adds the feed to the guide and marks it as synchronized.
     *
     * @param guide guide.
     * @param feed  feed.
     */
    private void addAndMarkAsSynced(StandardGuide guide, IFeed feed)
    {
        guide.add(feed);
        guide.getFeedLinkInfo(feed).setLastSyncTime(1L);
    }

    /**
     * Returns some test URL.
     *
     * @param n identifier to put inside the URL.
     *
     * @return test URL.
     */
    private URL getURL(int n)
    {
        URL url = null;
        try
        {
            url = new URL("file://" + n);
        } catch (MalformedURLException e) { fail(); }

        return url;
    }
}
