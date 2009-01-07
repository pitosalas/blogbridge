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
// $Id: TestHsqlFeedsPM.java,v 1.30 2008/02/28 15:59:52 spyromus Exp $
//

package com.salas.bb.persistence.backend;

import com.salas.bb.domain.*;
import com.salas.bb.domain.query.ICriteria;
import com.salas.bb.domain.query.articles.ArticleTextProperty;
import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.domain.query.general.StringContainsCO;
import com.salas.bb.domain.querytypes.QueryType;
import com.salas.bb.persistence.PersistenceException;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;

/**
 * This suite contains tests for <code>HsqlFeedsPM</code> unit.
 * It covers:
 * <ul>
 *  <li>inserting feeds.</li>
 *  <li>updating feeds.</li>
 *  <li>moving feeds.</li>
 *  <li>removing feeds.</li>
 * </ul>
 * @noinspection JavaDoc
 */
public class TestHsqlFeedsPM extends AbstractHsqlPersistenceTestCase
{
    private HsqlFeedsPM manager;
    private StandardGuide guide;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        // Init most modern database
        initManager("/resources");
        manager = new HsqlFeedsPM(pm);

        // Prepare feed for use in tests
        guide = new StandardGuide();
        guide.setTitle("Test Guide");

        pm.insertGuide(guide, 0);
    }

    /**
     * Tests adding direct feeds.
     */
    public void testInsertFeedDirect()
        throws MalformedURLException, PersistenceException, SQLException
    {
        long updatePeriod = Long.MAX_VALUE - 2;
        long lastPollTime = Long.MAX_VALUE - 1;
        long initTime = Long.MAX_VALUE;

        DirectFeed feed = new DirectFeed();

        // Set feed properties
        URL site = new URL("file://site");
        URL data = new URL("file://data");
        setDirectFeedProperties(feed, "BA", "BD", "BT", "CA", "CD", "CT", false, 3, 1, site,
            data, 1, false, 1, null, true);
        setDataFeedProperties(feed, "F", initTime, "L", lastPollTime, false, 5, 6, 7, updatePeriod);
        setFeedProperties(feed, "IR", 1, 2, true, "1", "2", false, null, null);

        // Insert direct feed
        guide.add(feed);
        manager.insertFeed(feed);
        manager.addFeedToGuide(guide, feed);
        pm.commit();

        // Load it back and verify
        GuidesSet set = new GuidesSet();
        pm.loadGuidesSet(set);
        IGuide guide = set.getGuideAt(0);

        assertEquals("There should be the only feed in list.", 1, guide.getFeedsCount());

        DirectFeed loadedFeed = (DirectFeed)guide.getFeedAt(0);
        verifyFeedProperties(loadedFeed, "IR", 1, 2, true, "1", "2", false, null, null);
        verifyDataFeedProperties(loadedFeed, false, "F", initTime, "L", lastPollTime, false, 5, 6, 7, updatePeriod);
        verifyDirectFeedProperties(loadedFeed, "BA", "BD", "BT", "CA", "CD", "CT", false, 3, 1,
            site, data, 1, false, 1, null, true);
    }

    /**
     * Tests adding query feeds.
     */
    public void testInsertFeedQuery()
        throws MalformedURLException, PersistenceException, SQLException
    {
        QueryFeed feed = new QueryFeed();

        // Set feed properties
        setFeedProperties(feed, "IR", 2, 3, false, null, null, true, "1", "2");
        setDataFeedProperties(feed, "F", 2, "L", 4, false, 5, 6, 7, 8);
        setQueryFeedProperties(feed, "T", QueryType.getQueryType(QueryType.TYPE_FEEDSTER), "a b", true, 5, 6);

        // Insert query feed
        guide.add(feed);
        manager.insertFeed(feed);
        manager.addFeedToGuide(guide, feed);
        pm.commit();

        // Load it back and verify
        GuidesSet set = new GuidesSet();
        pm.loadGuidesSet(set);
        IGuide guide = set.getGuideAt(0);

        assertEquals("There should be the only feed in list.", 1, guide.getFeedsCount());

        QueryFeed loadedFeed = (QueryFeed)guide.getFeedAt(0);
        verifyFeedProperties(loadedFeed, "IR", 2, 3, false, null, null, true, "1", "2");
        verifyDataFeedProperties(loadedFeed, false, "F", 2, "L", 4, false, 5, 6, 7, 8);
        verifyQueryFeedProperties(loadedFeed, "T", QueryType.getQueryType(QueryType.TYPE_FEEDSTER), "a b", true, 5, 6);
    }

    /**
     * Tests saving and loading of search feeds.
     */
    public void testInsertSearchFeed()
        throws PersistenceException, SQLException
    {
        SearchFeed feed = createSearchFeed();

        guide.add(feed);
        manager.insertFeed(feed);
        manager.addFeedToGuide(guide, feed);
        pm.commit();

        // Load it back and verify
        GuidesSet set = new GuidesSet();
        pm.loadGuidesSet(set);
        IGuide guide = set.getGuideAt(0);

        assertEquals("One feed was added.", 1, guide.getFeedsCount());
        verifyFeedProperties(guide.getFeedAt(0), null, 0, 0, false, null, null, false, null, null);
        verifySearchFeedProperties(feed, (SearchFeed)guide.getFeedAt(0));
    }

    /**
     * Tests updating query field.
     */
    public void testUpdateSearchFeedQuery()
        throws PersistenceException, SQLException
    {
        SearchFeed feed = createSearchFeed();

        guide.add(feed);
        manager.insertFeed(feed);
        manager.addFeedToGuide(guide, feed);
        pm.commit();

        Query otherQuery = new Query();
        ICriteria criteria = otherQuery.addCriteria();
        criteria.setProperty(ArticleTextProperty.INSTANCE);
        criteria.setComparisonOperation(StringContainsCO.INSTANCE);
        criteria.setValue("other");

        feed.setQuery(otherQuery);
        feed.setDedupEnabled(false);
        feed.setDedupFrom(11);
        manager.updateFeed(feed, SearchFeed.PROP_QUERY);
        manager.updateFeed(feed, null);

        // Load it back and verify
        GuidesSet set = new GuidesSet();
        pm.loadGuidesSet(set);
        IGuide guide = set.getGuideAt(0);

        assertEquals("One feed was added.", 1, guide.getFeedsCount());
        verifyFeedProperties(guide.getFeedAt(0), null, 0, 0, false, null, null, false, null, null);
        verifySearchFeedProperties(feed, (SearchFeed)guide.getFeedAt(0));
    }

    /**
     * Tests the updating of all other fields (except query).
     */
    public void testUpdateSearchFeedNonQueryFields()
        throws PersistenceException, SQLException
    {
        SearchFeed feed = createSearchFeed();

        guide.add(feed);
        manager.insertFeed(feed);
        manager.addFeedToGuide(guide, feed);
        pm.commit();

        feed.setBaseTitle("other");
        feed.setRating(1);
        feed.setArticlesLimit(1);
        manager.updateFeed(feed, SearchFeed.PROP_TITLE);

        // Load it back and verify
        GuidesSet set = new GuidesSet();
        pm.loadGuidesSet(set);
        IGuide guide = set.getGuideAt(0);

        assertEquals("One feed was added.", 1, guide.getFeedsCount());
        verifyFeedProperties(guide.getFeedAt(0), null, 0, 0, false, null, null, false, null, null);
        verifySearchFeedProperties(feed, (SearchFeed)guide.getFeedAt(0));
    }

    private static SearchFeed createSearchFeed()
    {
        Query query = new Query();
        ICriteria criteria = query.addCriteria();
        criteria.setProperty(ArticleTextProperty.INSTANCE);
        criteria.setComparisonOperation(StringContainsCO.INSTANCE);
        criteria.setValue("test");

        SearchFeed feed = new SearchFeed();
        feed.setQuery(query);
        feed.setBaseTitle("Title");
        feed.setRating(2);
        feed.setArticlesLimit(20);
        feed.setDedupEnabled(true);
        feed.setDedupFrom(2);

        return feed;
    }

    /**
     * Sets properties of the general feed.
     */
    private void setFeedProperties(IFeed feed, String invalidnessReason, int views, int clickthroughs,
                                   boolean asa, String asaf, String asanf, boolean ase, String asef, String asenf)
    {
        feed.setInvalidnessReason(invalidnessReason);
        feed.setViews(views);
        feed.setClickthroughs(clickthroughs);

        feed.setAutoSaveArticles(asa);
        feed.setAutoSaveArticlesFolder(asaf);
        feed.setAutoSaveArticlesNameFormat(asanf);

        feed.setAutoSaveEnclosures(ase);
        feed.setAutoSaveEnclosuresFolder(asef);
        feed.setAutoSaveEnclosuresNameFormat(asenf);
    }

    /**
     * Verifies properties of the general feed.
     */
    private void verifyFeedProperties(IFeed feed, String invalidnessReason, int views, int clickthroughs,
                                      boolean asa, String asaf, String asanf, boolean ase, String asef, String asenf)
    {
        assertEquals("Wrong invalidnessReason.", invalidnessReason, feed.getInvalidnessReason());
        assertEquals(views, feed.getViews());
        assertEquals(clickthroughs, feed.getClickthroughs());

        assertEquals(asa, feed.isAutoSaveArticles());
        assertEquals(asaf, feed.getAutoSaveArticlesFolder());
        assertEquals(asanf, feed.getAutoSaveArticlesNameFormat());
        assertEquals(ase, feed.isAutoSaveEnclosures());
        assertEquals(asef, feed.getAutoSaveEnclosuresFolder());
        assertEquals(asenf, feed.getAutoSaveEnclosuresNameFormat());
    }

    /**
     * Verifies properties of the data feed.
     */
    private void setDataFeedProperties(DataFeed feed, String format,
        long initTime, String language, long lastPollTime, boolean markNoKeywords,
        int purgeLimit, int retrievals, int totalPolled, long updatePeriod)
    {
        feed.setFormat(format);
        feed.setInitTime(initTime);
        feed.setLanguage(language);
        feed.setLastPollTime(lastPollTime);
        feed.setPurgeLimit(purgeLimit);
        feed.setRetrievals(retrievals);
        feed.setTotalPolledArticles(totalPolled);
        feed.setUpdatePeriod(updatePeriod);
    }

    /**
     * Verifies properties of the data feed.
     */
    private void verifyDataFeedProperties(DataFeed feed, boolean autoDiscovery, String format,
        long initTime, String language, long lastPollTime, boolean markNoKeywords, int purgeLimit,
        int retrievals, int totalPolled, long updatePeriod)
    {
        assertEquals("Wrong autoFeedsDiscovery.", autoDiscovery, feed.isAutoFeedsDiscovery());
        assertEquals("Wrong format.", format, feed.getFormat());
        assertEquals("Wrong initTime.", initTime, feed.getInitTime());
        assertEquals("Wrong language.", language, feed.getLanguage());
        assertEquals("Wrong lastPollTime.", lastPollTime, feed.getLastPollTime());
        assertEquals("Wrong purgeLimit.", purgeLimit, feed.getPurgeLimit());
        assertEquals("Wrong retrievals.", retrievals, feed.getRetrievals());
        assertEquals("Wrong totalPolledArticles.", totalPolled, feed.getTotalPolledArticles());
        assertEquals("Wrong updatePeriod.", updatePeriod, feed.getUpdatePeriod());
    }

    /**
     * Sets properties of the direct feed.
     */
    private void setDirectFeedProperties(DirectFeed feed, String baseAuthor,
        String baseDescription, String baseTitle, String customAuthor, String customDescription,
        String customTitle, boolean dead, int inlinks, int rating, URL siteURL, URL xmlURL,
        long lastMetaDataUpdateTime, boolean disabled, int syncHash, ReadingList list,
        Boolean ascendingSorting)
    {
        feed.setBaseAuthor(baseAuthor);
        feed.setBaseDescription(baseDescription);
        feed.setBaseTitle(baseTitle);
        feed.setCustomAuthor(customAuthor);
        feed.setCustomDescription(customDescription);
        feed.setCustomTitle(customTitle);
        feed.setDead(dead);
        feed.setInLinks(inlinks);
        feed.setRating(rating);
        feed.setSiteURL(siteURL);
        feed.setXmlURL(xmlURL);
        feed.setLastMetaDataUpdateTime(lastMetaDataUpdateTime);
        feed.setDisabled(disabled);
        feed.setSyncHash(syncHash);
        feed.addReadingList(list);
        feed.setAscendingSorting(ascendingSorting);
    }

    /**
     * Verifies properties of the direct feed.
     */
    private void verifyDirectFeedProperties(DirectFeed feed, String baseAuthor,
        String baseDescription, String baseTitle, String customAuthor, String customDescription,
        String customTitle, boolean dead, int inlinks, int rating, URL siteURL, URL xmlURL,
        long lastMetaDataUpdateTime, boolean disabled, int syncHash, ReadingList list,
        Boolean ascendingSorting)
    {
        assertEquals("Wrong baseAuthor.", baseAuthor, feed.getBaseAuthor());
        assertEquals("Wrong baseDescription.", baseDescription, feed.getBaseDescription());
        assertEquals("Wrong baseTitle.", baseTitle, feed.getBaseTitle());
        assertEquals("Wrong customAuthor.", customAuthor, feed.getCustomAuthor());
        assertEquals("Wrong customDescription.", customDescription, feed.getCustomDescription());
        assertEquals("Wrong customTitle.", customTitle, feed.getCustomTitle());
        assertEquals("Wrong dead.", dead, feed.isDead());
        assertEquals("Wrong inlinks.", inlinks, feed.getInLinks());
        assertEquals("Wrong rating.", rating, feed.getRating());
        assertEquals("Wrong siteURL.", siteURL.toString(), feed.getSiteURL().toString());
        assertEquals("Wrong xmlURL.", xmlURL.toString(), feed.getXmlURL().toString());
        assertEquals("Wrong lastMetaDataUpdateTime.", lastMetaDataUpdateTime,
            feed.getLastMetaDataUpdateTime());
        assertEquals("Wrong disabled.", disabled, feed.isDisabled());
        assertEquals("Wrong syncHash.", syncHash, feed.getSyncHash());

        ReadingList[] loadedLists = feed.getReadingLists();
        if (list == null)
        {
            assertEquals("No reading lists.", 0, loadedLists.length);
        } else
        {
            assertEquals("Reading list is present.", 1, loadedLists.length);
            assertEquals("Wrong reading list.", list.getID(), loadedLists[0].getID());
        }

        if (ascendingSorting == null) assertNull(feed.getAscendingSorting()); else
            assertEquals(ascendingSorting, feed.getAscendingSorting());
    }

    /**
     * Compares two feeds.
     *
     * @param ethalon   what we should have.
     * @param target    what we have.
     */
    private void verifySearchFeedProperties(SearchFeed ethalon, SearchFeed target)
    {
        assertEquals(ethalon.getQuery(), target.getQuery());
        assertEquals(ethalon.getBaseTitle(), target.getBaseTitle());
        assertEquals(ethalon.getRating(), target.getRating());
        assertEquals(ethalon.getArticlesLimit(), target.getArticlesLimit());
        assertEquals(ethalon.isDedupEnabled(), target.isDedupEnabled());
        assertEquals(ethalon.getDedupFrom(), target.getDedupFrom());
    }

    /**
     * Sets properties of query feed.
     */
    private void setQueryFeedProperties(QueryFeed feed, String title, QueryType type, String param,
                                        boolean dedupEnabled, int dedupFrom, int dedupTo)
    {
        feed.setBaseTitle(title);
        feed.setQueryType(type);
        feed.setParameter(param);
        feed.setDedupEnabled(dedupEnabled);
        feed.setDedupFrom(dedupFrom);
        feed.setDedupTo(dedupTo);
    }

    /**
     * Verifies properties of query feed.
     */
    private void verifyQueryFeedProperties(QueryFeed feed, String title, QueryType type,
                                           String param, boolean dedupEnabled, int dedupFrom, int dedupTo)
    {
        assertEquals("Wrong title.", title, feed.getBaseTitle());
        assertEquals("Wrong queryType.", type, feed.getQueryType());
        assertTrue("Wrong parameter.", param.equals(feed.getParameter()));
        assertEquals(dedupEnabled, feed.isDedupEnabled());
        assertEquals(dedupFrom, feed.getDedupFrom());
        assertEquals(dedupTo, feed.getDedupTo());
    }

    /**
     * Tests handling of bad input when adding feeds.
     */
    public void testInsertFeedFailure()
        throws PersistenceException, SQLException
    {
        // Unspecified feed
        try
        {
            manager.insertFeed(null);
            fail("Feed should be always specified. NPE is expected.");
        } catch (NullPointerException e)
        {
            // Expected
        }

        // Feed assigned to transient guide.
        StandardGuide transientGuide = new StandardGuide();
        DirectFeed feed = new DirectFeed();
        transientGuide.add(feed);
        manager.insertFeed(feed);

        try
        {
            manager.addFeedToGuide(transientGuide, feed);
            fail("Feed is assigned to transient feed. ISE is expected.");
        } catch (IllegalStateException e)
        {
            // Expected
        }

        // Save feed to test the case when we are inserting it twice
        feed = new DirectFeed();
        guide.add(feed);
        manager.insertFeed(feed);
        pm.commit();

        // Feed is already in database
        try
        {
            manager.insertFeed(feed);
            fail("Feed is already in database. ISE is expected.");
        } catch (IllegalStateException e)
        {
            // Expected
        }

        // Remove everything from the guide
        guide.clean();

        // Feed is of unsupported type
        IFeed unsupportedTypeFeed = new UnsupportedTypeFeed();
        guide.add(unsupportedTypeFeed);
        try
        {
            manager.insertFeed(unsupportedTypeFeed);
            fail("Feed has unsupported type. PM knows nothing about it. IAE is expected.");
        } catch (IllegalArgumentException e)
        {
            // Expected
        }
    }

    /**
     * Tests updating direct feeds.
     */
    public void testUpdateFeedDirect()
        throws PersistenceException, MalformedURLException, SQLException
    {
        DirectFeed feed = new DirectFeed();
        ReadingList list = new ReadingList(new URL("file://rl"));
        guide.add(list);
        pm.insertReadingList(list);

        // Insert direct feed
        list.add(feed);
        manager.insertFeed(feed);
        manager.addFeedToReadingList(list, feed);
        pm.commit();

        // Set feed properties
        URL site = new URL("file://site");
        URL data = new URL("file://data");
        setDirectFeedProperties(feed, "BA", "BD", "BT", "CA", "CD", "CT", false, 3, 1, site,
            data, 2, true, 2, null, false);
        setDataFeedProperties(feed, "F", 2, "L", 4, false, 5, 6, 7, 8);
        setFeedProperties(feed, "IR", 3, 4, true, "1", "2", true, "3", "4");

        // Update feed
        manager.updateFeed(feed, null);
        pm.commit();

        // Load it back and verify
        GuidesSet set = new GuidesSet();
        pm.loadGuidesSet(set);
        IGuide guide = set.getGuideAt(0);

        assertEquals("There should be the only feed in list.", 1, guide.getFeedsCount());

        DirectFeed loadedFeed = (DirectFeed)guide.getFeedAt(0);
        verifyFeedProperties(loadedFeed, "IR", 3, 4, true, "1", "2", true, "3", "4");
        verifyDataFeedProperties(loadedFeed, false, "F", 2, "L", 4, false, 5, 6, 7, 8);
        verifyDirectFeedProperties(loadedFeed, "BA", "BD", "BT", "CA", "CD", "CT", false, 3, 1,
            site, data, 2, true, 2, list, false);
    }

    /**
     * Tests updating query feeds.
     */
    public void testUpdateFeedQuery()
        throws PersistenceException, MalformedURLException, SQLException
    {
        QueryFeed feed = new QueryFeed();
        setQueryFeedProperties(feed, "TA", QueryType.getQueryType(QueryType.TYPE_DELICIOUS), "a", false, 0, 0);

        // Insert query feed
        guide.add(feed);
        manager.insertFeed(feed);
        manager.addFeedToGuide(guide, feed);
        pm.commit();

        // Set feed properties
        setFeedProperties(feed, "IR", 4, 5, false, null, null, false, null, null);
        setDataFeedProperties(feed, "F", 2, "L", 4, false, 5, 6, 7, 8);
        setQueryFeedProperties(feed, "T", QueryType.getQueryType(QueryType.TYPE_FEEDSTER), "a b", true, 1, 2);

        // Update feed
        manager.updateFeed(feed, null);
        pm.commit();

        // Load it back and verify
        GuidesSet set = new GuidesSet();
        pm.loadGuidesSet(set);
        IGuide guide = set.getGuideAt(0);

        assertEquals("There should be the only feed in list.", 1, guide.getFeedsCount());

        QueryFeed loadedFeed = (QueryFeed)guide.getFeedAt(0);
        verifyFeedProperties(loadedFeed, "IR", 4, 5, false, null, null, false, null, null);
        verifyDataFeedProperties(loadedFeed, false, "F", 2, "L", 4, false, 5, 6, 7, 8);
        verifyQueryFeedProperties(loadedFeed, "T", QueryType.getQueryType(QueryType.TYPE_FEEDSTER), "a b", true, 1, 2);
    }

    /**
     * Tests handling of bad input when updating feeds.
     */
    public void testUpdateFeedFailure()
        throws PersistenceException, SQLException
    {
        // Unspecified feed
        try
        {
            manager.updateFeed(null, null);
            fail("Feed should be always specified. NPE is expected.");
        } catch (NullPointerException e)
        {
            // Expected
        }

        // Feed isn't in database
        DirectFeed feed = new DirectFeed();

        try
        {
            manager.updateFeed(feed, null);
            fail("Feed is not in database. ISE is expected.");
        } catch (IllegalStateException e)
        {
            // Expected
        }
    }

    /**
     * Tests removing feeds.
     */
    public void testRemoveFeedDirect()
        throws PersistenceException, SQLException
    {
        // Insert sample feed
        DirectFeed feed = new DirectFeed();
        guide.add(feed);
        manager.insertFeed(feed);
        pm.commit();

        // Remove feed
        manager.removeFeed(feed);
        pm.commit();

        // Load the whole set back and verify that article is there and
        // all fields are set properly
        GuidesSet set = new GuidesSet();
        pm.loadGuidesSet(set);

        // We don't check for presence of guides as we will learn about that instantly.
        IGuide loadedGuide = set.getGuideAt(0);
        assertEquals("Guide should contain 0 feed.", 0, loadedGuide.getFeedsCount());
    }

    /**
     * Tests removing feeds.
     */
    public void testRemoveFeedQuery()
        throws PersistenceException, SQLException
    {
        // Insert sample feed
        QueryFeed feed = new QueryFeed();
        feed.setBaseTitle("T");
        feed.setQueryType(QueryType.getQueryType(QueryType.TYPE_DELICIOUS));
        feed.setParameter("a");
        guide.add(feed);
        manager.insertFeed(feed);
        pm.commit();

        // Remove feed
        manager.removeFeed(feed);
        pm.commit();

        // Load the whole set back and verify that article is there and
        // all fields are set properly
        GuidesSet set = new GuidesSet();
        pm.loadGuidesSet(set);

        // We don't check for presence of guides as we will learn about that instantly.
        IGuide loadedGuide = set.getGuideAt(0);
        assertEquals("Guide should contain 0 feed.", 0, loadedGuide.getFeedsCount());
    }

    /**
     * Tests handling of bad input when removing feeds.
     */
    public void testRemoveFeedFailure()
        throws PersistenceException, SQLException
    {
        try
        {
            manager.removeFeed(null);
            fail("Feed should be specified. NPE is expected.");
        } catch (NullPointerException e)
        {
            // Expected
        }

        DirectFeed transientFeed = new DirectFeed();
        try
        {
            manager.removeFeed(transientFeed);
            fail("Feed is transient and cannot be removed. ISE is expected.");
        } catch (IllegalStateException e)
        {
            // Expected
        }
    }

    /**
     * Test moving feed between guides.
     */
    public void testMoveFeed()
        throws PersistenceException, SQLException
    {
        // Insert sample feed
        QueryFeed feed = new QueryFeed();
        feed.setBaseTitle("T");
        feed.setParameter("a");
        guide.add(feed);
        manager.insertFeed(feed);
        manager.addFeedToGuide(guide, feed);
        pm.commit();

        // Add another guide
        StandardGuide destinationGuide = new StandardGuide();
        destinationGuide.setTitle("Dest");
        pm.insertGuide(destinationGuide, 1);
        pm.commit();

        // Move feed from original guide to destination
        guide.moveFeed(feed, destinationGuide, 0);
        manager.moveFeed(feed, guide, destinationGuide);
        pm.commit();

        // Load guides set and verify
        GuidesSet set = new GuidesSet();
        pm.loadGuidesSet(set);

        IGuide firstGuide = set.getGuideAt(0);
        assertEquals("Should be no feeds.", 0, firstGuide.getFeedsCount());

        IGuide secondGuide = set.getGuideAt(1);
        assertEquals("Should be one feed moved from the first guide.",
            1, secondGuide.getFeedsCount());

        IFeed lFeed = secondGuide.getFeedAt(0);
        assertEquals("Some wrong feed.", feed.getID(), lFeed.getID());
    }

    /**
     * Tests repositioning of feeds in a simple guide with two feeds.
     */
    public void testRepositionSimple() throws SQLException, PersistenceException
    {
        // Add first feed
        DirectFeed feed1 = new DirectFeed();
        feed1.setBaseTitle("1");
        guide.add(feed1);
        manager.insertFeed(feed1);
        manager.addFeedToGuide(guide, feed1);
        pm.commit();

        // Add second feed
        DirectFeed feed2 = new DirectFeed();
        feed2.setBaseTitle("2");
        guide.add(feed2);
        manager.insertFeed(feed2);
        manager.addFeedToGuide(guide, feed2);
        pm.commit();

        // Load guides set and verify positions
        GuidesSet set = new GuidesSet();
        pm.loadGuidesSet(set);
        IGuide gd = set.getGuideAt(0);
        assertEquals(2, gd.getFeedsCount());
        assertEquals(feed1.getID(), gd.getFeedAt(0).getID());
        assertEquals(feed2.getID(), gd.getFeedAt(1).getID());

        // Move second feed over the first one to the top
        guide.moveFeed(feed2, guide, 0);
        manager.updateFeedsPositions(guide);
        pm.commit();

        // Load guides set and verify positions
        set = new GuidesSet();
        pm.loadGuidesSet(set);
        gd = set.getGuideAt(0);
        assertEquals(2, gd.getFeedsCount());
        assertEquals(feed2.getID(), gd.getFeedAt(0).getID());
        assertEquals(feed1.getID(), gd.getFeedAt(1).getID());
    }

    /**
     * Tests repositioning of feeds in the guide with a reading list.
     */
    public void testRepositionReadingList() throws MalformedURLException, PersistenceException, SQLException
    {
        // Create a reading list with one feed
        DirectFeed feed = new DirectFeed();
        ReadingList list = new ReadingList(new URL("file://rl"));
        guide.add(list);
        pm.insertReadingList(list);

        // Insert direct feed in the list
        list.add(feed);
        manager.insertFeed(feed);
        manager.addFeedToReadingList(list, feed);
        pm.commit();

        // Add first feed
        DirectFeed feed1 = new DirectFeed();
        feed1.setBaseTitle("1");
        guide.add(feed1);
        manager.insertFeed(feed1);
        manager.addFeedToGuide(guide, feed1);
        pm.commit();

        // Add second feed
        DirectFeed feed2 = new DirectFeed();
        feed2.setBaseTitle("2");
        guide.add(feed2);
        manager.insertFeed(feed2);
        manager.addFeedToGuide(guide, feed2);
        pm.commit();

        // Load guides set and verify positions
        GuidesSet set = new GuidesSet();
        pm.loadGuidesSet(set);
        IGuide gd = set.getGuideAt(0);
        assertEquals(3, gd.getFeedsCount());
        assertEquals(feed.getID(), gd.getFeedAt(0).getID());
        assertEquals(feed1.getID(), gd.getFeedAt(1).getID());
        assertEquals(feed2.getID(), gd.getFeedAt(2).getID());

        // Move second feed over the first two feeds to the top
        guide.moveFeed(feed2, guide, 0);
        manager.updateFeedsPositions(guide);
        pm.commit();

        // Load guides set and verify positions
        set = new GuidesSet();
        pm.loadGuidesSet(set);
        gd = set.getGuideAt(0);
        assertEquals(3, gd.getFeedsCount());
        assertEquals(feed2.getID(), gd.getFeedAt(0).getID());
        assertEquals(feed.getID(), gd.getFeedAt(1).getID());
        assertEquals(feed1.getID(), gd.getFeedAt(2).getID());
    }

    // ---------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------

    /**
     * This feed class is used to simulate feed of unsupported type. The
     * persistence manager knows nothing about how to store it in database and
     * this is exactly what we need for failure tests.
     */
    private static class UnsupportedTypeFeed extends AbstractFeed
    {
        /**
         * Returns title of feed.
         *
         * @return title.
         */
        public String getTitle()
        {
            return null;
        }

        /**
         * Returns number of articles in channel.
         *
         * @return number of articles.
         */
        public int getArticlesCount()
        {
            return 0;
        }

        /**
         * Returns number of articles this feed owns.
         *
         * @return number of articles.
         */
        public int getOwnArticlesCount()
        {
            return 0;
        }

        /**
         * Returns the Article at the specified index.
         *
         * @param index index of article in channel.
         *
         * @return article object.
         */
        public IArticle getArticleAt(int index)
        {
            return null;
        }

        /**
         * Returns the list of all articles which are currently in the feed.
         *
         * @return all articles at this moment.
         */
        public IArticle[] getArticles()
        {
            return new IArticle[0];
        }

        /**
         * Returns simple match key, which can be used to detect similarity of feeds. For example, it's
         * XML URL for the direct feeds, query type + parameter for the query feeds, serialized search
         * criteria for the search feeds.
         *
         * @return match key.
         */
        public String getMatchKey()
        {
            return null;
        }
    }
}
