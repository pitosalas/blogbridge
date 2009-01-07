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
// $Id: TestDataFeed.java,v 1.27 2008/02/28 15:59:53 spyromus Exp $
//

package com.salas.bb.domain;

import com.salas.bb.utils.parser.Channel;
import com.salas.bb.utils.parser.Item;
import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * This suite contains tests for <code>DataFeed</code> unit.
 * It covers:
 * <ul>
 *  <li>Initialization of properties upon construction.</li>
 *  <li>Storing of: initialization time, last polling time, number of retrievals,
 *      number of articles polled, purge limit, flags for
 *      marking articles as read when no keywords found and automatic discovery
 *      of links in new articles.</li>
 *  <li>Getting and setting the list of read articles' keys.</li>
 *  <li>Adding, inserting and removing articles.</li>
 * </ul>
 */
public class TestDataFeed extends TestCase
{
    private DataFeed feed;

    protected void setUp()
        throws Exception
    {
        feed = new DummyDataFeed();
    }

    // ---------------------------------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------------------------------

    /**
     * Tests initialization of properties after construction.
     */
    public void testConstruction()
    {
        assertEquals("Wrong purge limit setting.",
            NetworkFeed.PURGE_LIMIT_INHERITED, feed.getPurgeLimit());
        assertEquals("Wrong purge limit setting.",
            NetworkFeed.DEFAULT_PURGE_LIMIT, feed.getPurgeLimitCombined());
        assertEquals("Wrong default init time.",
            NetworkFeed.INIT_TIME_UNINITIALIZED, feed.getInitTime());
        assertEquals("Wrong last poll time.",
            NetworkFeed.DEFAULT_LAST_POLL_TIME, feed.getLastPollTime());
        assertEquals("Wrong last update time.",
            NetworkFeed.DEFAULT_LAST_UPDATE_SERVER_TIME, feed.getLastUpdateServerTime());
        assertEquals("Wrong total polled articles counter.",
            NetworkFeed.DEFAULT_TOTAL_POLLED_ARTICLES, feed.getTotalPolledArticles());
        assertEquals("Wrong number of retrievals.",
            NetworkFeed.DEFAULT_RETRIEVALS, feed.getRetrievals());
    }

    /**
     * Tests saving initialization time.
     */
    public void testGetSetInitTime()
    {
        assertEquals("Wrong default init time.",
            NetworkFeed.INIT_TIME_UNINITIALIZED, feed.getInitTime());

        feed.setInitTime(1);
        assertEquals("Wrong init time.", 1, feed.getInitTime());
    }

    /**
     * Tests reporting initialization state. The feed is initialized when it's initialization
     * time is set.
     */
    public void testInitialized()
    {
        assertFalse("Feed should not be initialized.", feed.isInitialized());

        feed.setInitTime(1);
        assertTrue("Feed should be initialized.", feed.isInitialized());
    }

    /**
     * Tests saving time of last poll.
     */
    public void testGetSetLastPollTime()
    {
        assertEquals("Wrong last poll time.",
            NetworkFeed.DEFAULT_LAST_POLL_TIME, feed.getLastPollTime());

        feed.setLastPollTime(1);
        assertEquals("Wrong last poll time.", 1, feed.getLastPollTime());
    }

    /**
     * Tests saving the counter of retrievals.
     */
    public void testGetSetRetrievals()
    {
        assertEquals("Wrong number of retrievals.",
            NetworkFeed.DEFAULT_RETRIEVALS, feed.getRetrievals());

        feed.setRetrievals(1);
        assertEquals("Wrong number of retrievals.", 1, feed.getRetrievals());
    }

    /**
     * Tests saving the counter of polled articles.
     */
    public void testGetSetTotalPolledArticles()
    {
        assertEquals("Wrong total polled articles counter.",
            NetworkFeed.DEFAULT_TOTAL_POLLED_ARTICLES, feed.getTotalPolledArticles());

        feed.setTotalPolledArticles(10);
        assertEquals("Wrong total polled articles counter.", 10, feed.getTotalPolledArticles());
    }

    /**
     * Tests returning the list of keys of read articles.
     * Feed is initialized.
     */
    public void testGetReadArticlesKeysInitialized()
    {
        StandardArticle art1 = article(1);
        art1.setRead(false);
        feed.appendArticle(art1);

        StandardArticle art2 = article(2);
        art2.setRead(false);
        feed.appendArticle(art2);

        // Make feed to be initialized
        feed.setInitTime(1);

        String keys;

        // Nothing marked as read
        keys = feed.getReadArticlesKeys();
        assertEquals("Should be no keys.", "", keys);

        // Situation 1: there's read article present
        art1.setRead(true);
        keys = feed.getReadArticlesKeys();
        assertEquals("Keys should have single key of read article.",
            art1.getSimpleMatchKey(), keys);

        // Situation 2: there are two read articles present
        art2.setRead(true);
        keys = feed.getReadArticlesKeys();
        assertEquals("Keys should have single key of read article.",
            art1.getSimpleMatchKey() + "," + art2.getSimpleMatchKey(), keys);

        // Situation 3: Feed is empty
        feed = new DummyNetworkFeed();
        keys = feed.getReadArticlesKeys();
        assertEquals("Should be no keys.", "", keys);
    }

    /**
     * Tests returning the list of keys of read articles.
     * Feed is not initialized
     */
    public void testGetReadArticlesKeys()
    {
        StandardArticle art1 = article(1);
        art1.setRead(false);
        feed.appendArticle(art1);

        StandardArticle art2 = article(2);
        art2.setRead(false);
        feed.appendArticle(art2);

        String keys;

        // Nothing marked as read
        keys = feed.getReadArticlesKeys();
        assertEquals("Should be no keys.", "", keys);

        feed.setReadArticlesKeys("12345678,87654321");
        keys = feed.getReadArticlesKeys();
        assertEquals("Keys should be returned as is.", "12345678,87654321", keys);
    }

    /**
     * Tests setting the list of keys of read articles. The articles which are
     * currently in the feed and have the keys mentioned in the list should be
     * marked as read immediately. Also the list of keys should be recorded.
     */
    public void testSetReadArticlesKeys()
    {
        StandardArticle art1 = article(1);
        art1.setRead(false);
        feed.appendArticle(art1);

        StandardArticle art2 = article(2);
        art2.setRead(false);
        feed.appendArticle(art2);

        // Set the key of the first article as read
        String keys = art1.getSimpleMatchKey();
        feed.setReadArticlesKeys(keys);
        assertTrue("Should be marked as read.", art1.isRead());
    }

    /**
     * Tests the clearing of list of read articles keys.
     */
    public void testSetReadArticlesKeysClear()
    {
        String keys;
        feed.setReadArticlesKeys("12345678,87654321");
        keys = feed.getReadArticlesKeys();
        assertEquals("Keys should be returned as is.", "12345678,87654321", keys);

        feed.setReadArticlesKeys(null);
        keys = feed.getReadArticlesKeys();
        assertEquals("Should be no keys.", "", keys);
    }

    /**
     * Tests inheritance of the flag.
     */
    public void testGetSetAutoFeedsDiscoveryInheritance()
    {
        assertEquals("Wrong default setting.", false, feed.isAutoFeedsDiscovery());

        IGuide guide = new StandardGuide();
        feed.addParentGuide(guide);

        guide.setAutoFeedsDiscovery(true);
        assertEquals("Wrong setting.", true, feed.isAutoFeedsDiscovery());

        guide.setAutoFeedsDiscovery(false);
        assertEquals("Wrong setting.", false, feed.isAutoFeedsDiscovery());
    }

    /**
     * Tests saving of the purge limit property.
     */
    public void testGetSetPurgeLimit()
    {
        assertEquals("Wrong purge limit setting.",
            NetworkFeed.PURGE_LIMIT_INHERITED, feed.getPurgeLimit());

        feed.setPurgeLimit(1);
        assertEquals("Wrong purge limit setting.", 1, feed.getPurgeLimit());
    }

    /**
     * Tests appending the article to the tail of feed.
     */
    public void testAppendArticle()
    {
        feed = new DummyNetworkFeed();
        assertEquals("Shoud be no articles.", 0, feed.getArticlesCount());

        StandardArticle art1 = article(1);
        StandardArticle art2 = article(2);

        feed.appendArticle(art1);
        assertEquals("Wrong article number.", 1, feed.getArticlesCount());
        assertTrue("Wrong article added.", art1 == feed.getArticleAt(0));
        assertTrue("Wrong feed.", feed == art1.getFeed());

        feed.appendArticle(art2);
        assertEquals("Wrong article number.", 2, feed.getArticlesCount());
        assertTrue("Wrong article added.", art2 == feed.getArticleAt(1));
    }

    /**
     * Tests appending duplicate article to the tail of feed.
     */
    public void testAppendArticleDuplicate()
    {
        feed = new DummyNetworkFeed();
        assertEquals("Shoud be no articles.", 0, feed.getArticlesCount());

        StandardArticle art1 = new StandardArticle("");

        feed.appendArticle(art1);
        assertEquals("Wrong article number.", 1, feed.getArticlesCount());

        try
        {
            feed.appendArticle(art1);
            fail("The article is already in the feed. ISE is expected.");
        } catch (IllegalStateException e)
        {
            // Expected
        }
    }

    /**
     * Tests handling of addition of NULL.
     */
    public void testAppendArticleNull()
    {
        try
        {
            feed.appendArticle(null);
            fail("NPE is expected.");
        } catch (NullPointerException e)
        {
            // Expected
        }
    }

    /**
     * Tests inserting the article to the tail of feed.
     */
    public void testInsertArticle()
    {
        feed = new DummyNetworkFeed();
        assertEquals("Shoud be no articles.", 0, feed.getArticlesCount());

        StandardArticle art1 = article(1);
        StandardArticle art2 = article(2);

        feed.insertArticle(0, art1);
        assertEquals("Wrong article number.", 1, feed.getArticlesCount());
        assertTrue("Wrong article added.", art1 == feed.getArticleAt(0));

        feed.insertArticle(0, art2);
        assertEquals("Wrong article number.", 2, feed.getArticlesCount());
        assertTrue("Wrong article added.", art2 == feed.getArticleAt(0));
    }

    /**
     * Tests appending duplicate article to the tail of feed.
     */
    public void testInsertArticleDuplicate()
    {
        feed = new DummyNetworkFeed();
        assertEquals("Shoud be no articles.", 0, feed.getArticlesCount());

        StandardArticle art1 = new StandardArticle("");

        feed.insertArticle(0, art1);
        assertEquals("Wrong article number.", 1, feed.getArticlesCount());

        try
        {
            feed.insertArticle(0, art1);
            fail("The article is already in the feed. ISE is expected.");
        } catch (IllegalStateException e)
        {
            // Expected
        }
    }

    /**
     * Tests handling of insertion of NULL.
     */
    public void testInsertArticleNull()
    {
        try
        {
            feed.insertArticle(0, null);
            fail("NPE is expected.");
        } catch (NullPointerException e)
        {
            // Expected
        }
    }

    /**
     * Tests firing IOB exection when inserting article to some missing position.
     */
    public void testInsertArticleAIOB()
    {
        feed = new DummyNetworkFeed();
        assertEquals("Shoud be no articles.", 0, feed.getArticlesCount());

        try
        {
            feed.insertArticle(1, new StandardArticle(""));
            fail("IOB expected.");
        } catch (IndexOutOfBoundsException e)
        {
            // Expected
        }
    }

    /**
     * Tests removing article from feed.
     */
    public void testRemoveArticle()
    {
        feed = new DummyNetworkFeed();
        assertEquals("Shoud be no articles.", 0, feed.getArticlesCount());

        StandardArticle art1 = new StandardArticle("");
        feed.insertArticle(0, art1);

        assertTrue("Should report successful removal.", feed.removeArticle(art1));
        assertEquals("Shoud be no articles.", 0, feed.getArticlesCount());
    }

    /**
     * Tests duplicate removing of articles.
     */
    public void testRemoveArticleDuplicate()
    {
        feed = new DummyNetworkFeed();
        assertEquals("Shoud be no articles.", 0, feed.getArticlesCount());

        StandardArticle art1 = new StandardArticle("");
        feed.insertArticle(0, art1);

        assertTrue("Should report successful removal.", feed.removeArticle(art1));
        assertFalse("Shouldn't report successful removal.", feed.removeArticle(art1));
    }

    /**
     * Tests handling of removing NULL-articles.
     */
    public void testRemoveArticleNull()
    {
        try
        {
            feed.removeArticle(null);
            fail("NPE is expected.");
        } catch (NullPointerException e)
        {
            // Expected
        }
    }

    /**
     * Tests that articles added are being marked as read automatically if their
     * keys are in the list of read articles' keys.
     */
    public void testAutoReadOnAdding()
    {
        String keys;

        feed = new DummyNetworkFeed();
        StandardArticle art1 = article(1);
        StandardArticle art2 = article(2);

        keys = art1.getSimpleMatchKey();

        feed.setReadArticlesKeys(keys);

        feed.appendArticle(art1);
        assertTrue("Should be marked as read.", art1.isRead());
        assertEquals("Wrong unread count.", 0, feed.getUnreadArticlesCount());

        feed.appendArticle(art2);
        assertFalse("Should not be marked as read.", art2.isRead());
        assertEquals("Wrong unread count.", 1, feed.getUnreadArticlesCount());
    }

    /**
     * Tests getting format of the feed from its handle.
     */
    public void testGetFormat()
    {
        assertNull("Format is not known.", feed.getFormat());

        feed.setFormat("A");
        assertEquals("Format is known.", "A", feed.getFormat());
    }

    /**
     * Tests getting language of the feed from its handle.
     */
    public void testGetLanguage()
    {
        assertNull("Language is not known.", feed.getLanguage());

        feed.setLanguage("A");
        assertEquals("Language is known.", "A", feed.getLanguage());
    }

    /**
     * Tests reporting of updatable status by feed when it's invalid.
     */
    public void testIsUpdatableInvalid()
    {
        feed.setInvalidnessReason(null);
        feed.setID(1);
        assertTrue("Feed is valid. It can be updatable.", feed.isUpdatable(false));
        assertTrue("Feed is valid. It can be updatable.", feed.isUpdatable(true));

        feed.setInvalidnessReason(null);
        feed.setID(-1);
        assertFalse("Feed isn't persisted. It cannot be updatable.", feed.isUpdatable(false, true));
        assertFalse("Feed isn't persisted. It cannot be updatable.", feed.isUpdatable(true, true));

        feed.setInvalidnessReason("A");
        feed.setID(1);
        assertFalse("Feed is invalid. It cannot be updatable.", feed.isUpdatable(false));
        assertTrue("Feed is invalid, but questioned manually. It can be updatable.",
            feed.isUpdatable(true));

        feed.setInvalidnessReason("A");
        feed.setID(-1);
        assertFalse("Feed isn't persisted. It cannot be updatable.", feed.isUpdatable(false, true));
        assertFalse("Feed isn't persisted. It cannot be updatable.", feed.isUpdatable(true, true));
    }

    /**
     * Tests getting and setting update period and inheritance of the values.
     */
    public void testGetSetUpdatePeriod()
    {
        feed.setUpdatePeriod(1);
        assertEquals("Wrong period.", 1, feed.getUpdatePeriod());

        feed.setUpdatePeriod(DataFeed.UPDATE_PERIOD_INHERITED);
        assertEquals("Wrong period.", DataFeed.getGlobalUpdatePeriod(),
            feed.getUpdatePeriodCombined());
    }

    /**
     * Tests that the feed is reported to be not updatable if it's not time yet.
     */
    public void testIsUpdatableNotTime()
    {
        // Setting the last update time to a second ago
        long base = System.currentTimeMillis();
        feed.setLastPollTime(base - 1000);
        feed.setID(1);

        // Setting five seconds period -- not time
        feed.setUpdatePeriod(5000);
        assertFalse("Too early for updates.", feed.isUpdatable(false));
        assertTrue("Direct updates are allowed at any moment.", feed.isUpdatable(true));

        // Setting half second period -- time has come
        feed.setUpdatePeriod(500);
        assertTrue("Time of updates has come.", feed.isUpdatable(false));
        assertTrue("Direct updates are allowed at any moment.", feed.isUpdatable(true));
    }

    /**
     * Tests updating the feed data from parsed channel object.
     */
    public void testUpdateFeed()
    {
        Channel channel = new Channel();

        // The parsed channel has information about format and language,
        // the feed we have -- has not. The information should be moved.
        channel.setFormat("A");
        channel.setLanguage("B");

        feed.updateFeed(channel);
        assertEquals("Wrong format.", "A", feed.getFormat());
        assertEquals("Wrong language.", "B", feed.getLanguage());

        // The parsed channel has no information about format and language,
        // the feed we already have -- has. There should be no updates.
        channel.setFormat(null);
        channel.setLanguage(null);

        feed.updateFeed(channel);
        assertEquals("Wrong format.", "A", feed.getFormat());
        assertEquals("Wrong language.", "B", feed.getLanguage());
    }

    /**
     * Tests updating the articles list from parsed empty channel object.
     */
    public void testUpdateArticlesEmpty()
    {
        // Channel has no articles
        feed.updateArticles(new StandardArticle[0]);
        assertEquals("Wrong number of articles.", 0, feed.getArticlesCount());
    }

    /**
     * Tests updating the articles list from parsed channel object.
     */
    public void testUpdateArticles()
    {
        StandardArticle[] incArticles = {article(1)};

        feed.updateArticles(incArticles);
        assertEquals("Wrong number of articles.", 1, feed.getArticlesCount());

        // Continued updates should not add the same item
        feed.updateArticles(incArticles);
        assertEquals("Wrong number of articles.", 1, feed.getArticlesCount());
    }

    /**
     * Tests the updates with detection of existing articles.
     */
    public void testUpdateArticlesOld()
    {
        // First update -- single item
        StandardArticle[] incArticles1 = new StandardArticle[]
        {
            article(2)
        };

        feed.updateArticles(incArticles1);
        assertEquals("Wrong number of articles.", 1, feed.getArticlesCount());
        assertEquals("Wrong article added.", "2", feed.getArticleAt(0).getHtmlText());

        // Second update -- one new item and one old
        StandardArticle[] incArticles2 = new StandardArticle[]
        {
                article(1), article(2), article(3)
        };

        feed.updateArticles(incArticles2);
        assertEquals("Wrong number of articles.", 3, feed.getArticlesCount());
        assertEquals("Wrong article added.", "3", feed.getArticleAt(0).getHtmlText());
        assertEquals("Wrong article added.", "1", feed.getArticleAt(1).getHtmlText());
        assertEquals("Wrong article added.", "2", feed.getArticleAt(2).getHtmlText());
    }

    /**
     * Tests the scenario when there are more articles in the parsed channel then
     * it's allowed by local limits. In this case the older articles should be skipped.
     */
    public void testUpdateArticlesGreaterThanLimit()
    {
        // Configure incoming articles
        StandardArticle[] incArticles = new StandardArticle[]
        {
                article(1), article(2), article(3)
        };

        feed.setPurgeLimit(2);

        // Channel has 3 articles, the limit is 2
        feed.updateArticles(incArticles);
        assertEquals("Wrong number of articles.", 2, feed.getArticlesCount());
        assertEquals("Wrong article added.", "2", feed.getArticleAt(0).getHtmlText());
        assertEquals("Wrong article added.", "1", feed.getArticleAt(1).getHtmlText());
    }

    /**
     * Tests the scenario when the feed contains several articles "from the future" in
     * the top of the articles list. Normally, it will prevent to load the articles
     * following them because they will be treated as "old" because the seen article
     * (the one with future date) is on top of them. To avoid this behaviour it's proposed
     * to continue reading channel after the future article has been seen.
     */
    public void testUpdateArticlesFromFuture()
    {
        // Configure incoming articles
        StandardArticle art1 = article(1);
        StandardArticle art2 = article(2);
        StandardArticle art3 = article(3);

        // Set future date to the first article
        GregorianCalendar cal = new GregorianCalendar();
        cal.roll(Calendar.YEAR, 1);
        Date futureDate = cal.getTime();
        art1.setPublicationDate(futureDate);

        // Add the "future" article during the first update
        feed.updateArticles(new StandardArticle[] { art1 });
        assertEquals("Wrong number of articles.", 1, feed.getArticlesCount());

        // Now add more articles to the channel and run another update
        feed.updateArticles(new StandardArticle[] { art1, art2, art3 });
        assertEquals("Wrong number of articles.", 3, feed.getArticlesCount());
        assertEquals("Wrong order of articles.", "3", feed.getArticleAt(0).getHtmlText());
        assertEquals("Wrong order of articles.", "2", feed.getArticleAt(1).getHtmlText());
        assertEquals("Wrong order of articles.", "1", feed.getArticleAt(2).getHtmlText());
    }

    /**
     * This test ensures that the above test {@link #testUpdateArticlesFromFuture()}
     * doesn't break anything.
     */
    public void testUpdateArticlesFromFutureDuplicate()
    {
        // Configure incoming articles
        StandardArticle art1 = article(1);
        StandardArticle art1_1 = article(1);
        StandardArticle art2 = article(2);
        StandardArticle art3 = article(3);

        // Set future date to the first article
        GregorianCalendar cal = new GregorianCalendar();
        cal.roll(Calendar.YEAR, 1);
        Date futureDate = cal.getTime();
        art1.setPublicationDate(futureDate);

        // Set the dates so that the art2 is newer than art3
        art2.setPublicationDate(new Date(3));
        art3.setPublicationDate(new Date(2));

        // Add the "future" article during the first update
        feed.updateArticles(new StandardArticle[] { art1, art1_1, art2, art3 });

        assertEquals("Wrong number of articles.", 3, feed.getArticlesCount());
        assertEquals("Wrong order of articles.", "3", feed.getArticleAt(0).getHtmlText());
        assertEquals("Wrong order of articles.", "2", feed.getArticleAt(1).getHtmlText());
        assertEquals("Wrong order of articles.", "1", feed.getArticleAt(2).getHtmlText());
    }

    /**
     * There are several feeds with an advertisement article (or articles) in the head of
     * the articles list. There are tests to ensure that the article "from the future" never
     * stops the processing, but there are several registered occasions when the first
     * article is from the far past or has no date at all, but still usually appears at
     * the top. We have to deal with this situation gracefully as well.
     */
    public void testUpdateArticlesAdvert()
    {
        // Configure incoming articles
        StandardArticle artAdvert = article(1);
        StandardArticle art2 = article(2);
        StandardArticle art3 = article(3);

        // We have a feed with advert at first
        feed.updateArticles(new StandardArticle[] { artAdvert });

        assertEquals("Wrong number of articles.", 1, feed.getArticlesCount());
        assertEquals("Wrong article.", "1", feed.getArticleAt(0).getHtmlText());

        // Then the item1 is added to the feed
        feed.updateArticles(new StandardArticle[] { artAdvert, art2 });

        assertEquals("Wrong number of articles.", 2, feed.getArticlesCount());
        assertEquals("Wrong article.", "2", feed.getArticleAt(0).getHtmlText());
        assertEquals("Wrong article.", "1", feed.getArticleAt(1).getHtmlText());

        // And now the item2 is added in front the item1 but after the advert, like this:
        // advert -> item2 -> item1
        feed.updateArticles(new StandardArticle[] { artAdvert, art3, art2 });

        assertEquals("Wrong number of articles.", 3, feed.getArticlesCount());
        assertEquals("Wrong article.", "3", feed.getArticleAt(0).getHtmlText());
        assertEquals("Wrong article.", "2", feed.getArticleAt(1).getHtmlText());
        assertEquals("Wrong article.", "1", feed.getArticleAt(2).getHtmlText());
    }

    /**
     * Tests updating sequence when NULL feed is returned from <code>fetchFeed</code>.
     */
    public void testUpdateNull()
    {
        DummyDataFeed feed = new DummyDataFeed();
        feed.setChannel(null);

        feed.update();
        assertTrue("Feed should update time and counters in any case.",
            feed.getLastPollTime() > DataFeed.DEFAULT_LAST_POLL_TIME);
        assertEquals("Time of initialization should change accordingly.",
            feed.getLastPollTime(), feed.getInitTime());
        assertEquals("Retrieval count should grow.", 1, feed.getRetrievals());
    }

    /**
     * Tests normal update sequence with updates to times and retrieval counts.
     */
    public void testUpdateNotNull()
    {
        Channel channel = new Channel();
        channel.addItem(new Item("1"));

        DummyDataFeed feed = new DummyDataFeed();
        feed.setChannel(channel);

        long base = System.currentTimeMillis();
        feed.update();
        long initTime = feed.getInitTime();
        assertTrue("Time should be updated.", feed.getLastPollTime() >= base);
        assertTrue("Time should be updated.", initTime >= base);
        assertEquals("Wrong retrievals count.", 1, feed.getRetrievals());
        assertEquals("Wrong total articles counter.", 1, feed.getTotalPolledArticles());
        assertTrue("Update method wasn't called.", feed.isUpdateArticlesCalled());
        assertTrue("Update method wasn't called.", feed.isUpdateFeedCalled());

        // Another update
        base = System.currentTimeMillis();
        feed.update();
        assertTrue("Time should be updated.", feed.getLastPollTime() >= base);
        assertEquals("Time should not be updated.", initTime, feed.getInitTime());
        assertEquals("Wrong retrievals count.", 2, feed.getRetrievals());
        assertEquals("Wrong total articles counter.", 1, feed.getTotalPolledArticles());
        assertTrue("Update method wasn't called.", feed.isUpdateArticlesCalled());
        assertTrue("Update method wasn't called.", feed.isUpdateFeedCalled());
    }

    /**
     * Tests work of cleaner.
     *
     * @throws Exception if something goes wrong.
     */
    public void testClean() throws Exception
    {
        // There's nothing to clean
        feed.clean();
        assertEquals("Wrong number of articles.", 0, feed.getArticlesCount());

        // Adding two articles one by one (two updates) while having limit of 1
        // The second update should call cleaner and remove the last article
        DummyDataFeed feed = new DummyDataFeed();
        feed.setPurgeLimit(1);
        Channel channel1 = new Channel();
        channel1.addItem(new Item("1"));

        // First update
        feed.setChannel(channel1);
        feed.update();

        // A little pause to create a gap between items
        Thread.sleep(100);

        Channel channel2 = new Channel();
        Item item = new Item("2");
        channel2.addItem(item);

        // Second update
        feed.setChannel(channel2);
        feed.update();

        assertEquals("Wrong number of articles.", 1, feed.getArticlesCount());
        assertEquals("Wrong article is left.", "2", feed.getArticleAt(0).getHtmlText());
    }

    /**
     * Tests work of cleaner.
     */
    public void testCleanPinned()
    {
        // There's nothing to clean
        feed.clean();
        assertEquals("Wrong number of articles.", 0, feed.getArticlesCount());

        // Adding two articles one by one (two updates) while having limit of 1
        // The second update should call cleaner but the first article should be
        // removed because of a pin.
        DummyDataFeed feed = new DummyDataFeed();
        feed.setPurgeLimit(1);
        DataFeed.setGlobalPurgeUnread(false);
        Channel channel1 = new Channel();
        channel1.addItem(new Item("1"));

        // First update
        feed.setChannel(channel1);
        feed.update();
        feed.getArticleAt(0).setPinned(true);

        Channel channel2 = new Channel();
        Item item = new Item("2");
        channel2.addItem(item);

        // Second update
        feed.setChannel(channel2);
        feed.update();

        assertEquals("Wrong number of articles.", 2, feed.getArticlesCount());
    }

    /**
     * Assume there are 6 articles: 3 pinned and 3 not pinned.
     * When we set the purge limit to 3 (and we know that pinned articles aren't removed)
     * the 3 not pinned articles should stay as otherwise there will be no new articles
     * once the number of pinned articles reaches the purge limit.
     */
    public void testCleanNotCountPinned()
    {
        // Create a sample feed
        DirectFeed feed = new DirectFeed();
        DirectFeed.setGlobalPurgeUnread(true);
        feed.setPurgeLimit(Integer.MAX_VALUE);

        // Generate articles and add them to the feed
        // First 3 are pinned
        for (int i = 0; i < 6; i++)
        {
            String txt = Integer.toString(i);

            StandardArticle article = new StandardArticle(txt);
            article.setRead(false);
            article.setTitle(txt);
            article.setPinned(i < 3);

            feed.appendArticle(article);
        }

        // Set purge limit to 3 and see if articles stay
        feed.setPurgeLimit(3);
        assertEquals("3 unpinned articles should stay", 6, feed.getArticlesCount());
    }

    /**
     * Tests removal of correct items.
     */
    public void testComplexClean()
    {
        DummyDataFeed feed = new DummyDataFeed();

        long time = System.currentTimeMillis();
        IArticle article0 = articleWithTime(time);
        IArticle article1 = articleWithTime(time + 1000);
        IArticle article2 = articleWithTime(time + 2000);
        IArticle article3 = articleWithTime(time + 3000);
        IArticle articlem1 = articleWithTime(time - 1000);
        IArticle articlem2 = articleWithTime(time - 2000);
        IArticle articlem3 = articleWithTime(time - 3000);

        feed.appendArticle(articlem3);
        feed.appendArticle(articlem2);
        feed.appendArticle(articlem1);
        feed.appendArticle(article0);
        feed.appendArticle(article1);
        feed.appendArticle(article2);
        feed.appendArticle(article3);

        feed.setPurgeLimit(2);
        assertEquals(2, feed.getArticlesCount());
        assertTrue(article2 == feed.getArticleAt(0));
        assertTrue(article3 == feed.getArticleAt(1));
    }

    /**
     * Creates a simple test article with the title and text equal to the given index.
     *
     * @param index index.
     *
     * @return article object.
     */
    private static StandardArticle article(long index)
    {
        String text = Long.toString(index);
        StandardArticle article = new StandardArticle(text);
        article.setTitle(text);

        return article;
    }

    /**
     * Creates an article and initializes it with the time given.
     *
     * @param time time of pubication.
     *
     * @return article.
     */
    private static StandardArticle articleWithTime(long time)
    {
        StandardArticle article = article(time);
        article.setPublicationDate(new Date(time));
        return article;
    }

    /**
     * Tests the case when <code>purgeUnread</code> is FALSE meaning that the
     * feed should not remove unread articles during cleanup.
     */
    public void testCleanNoPurgeUnread()
    {
        // Disable purgin unread globally
        boolean globalPurgeUnead = DataFeed.isGlobalPurgeUnread();
        DataFeed.setGlobalPurgeUnread(false);

        try
        {
            // Set purge limit to 1 article
            DummyDataFeed feed = new DummyDataFeed();
            feed.setPurgeLimit(1);

            // Create unread articles and add them to the feed
            StandardArticle art1 = article(1);
            art1.setRead(false);
            feed.appendArticle(art1);

            StandardArticle art2 = article(2);
            art2.setRead(false);
            feed.appendArticle(art2);

            // Clean and check
            feed.clean();
            assertEquals("Unread articles should not be purged.", 2, feed.getArticlesCount());
        } finally
        {
            DataFeed.setGlobalPurgeUnread(globalPurgeUnead);
        }
    }

    /**
     * Tests the case when <code>purgeUnread</code> is FALSE meaning that the
     * feed should not remove unread articles during cleanup. There will be another article
     * which is read. It should be removed.
     */
    public void testCleanNoPurgeUnread2()
    {
        // Disable purgin unread globally
        boolean globalPurgeUnead = DataFeed.isGlobalPurgeUnread();
        DataFeed.setGlobalPurgeUnread(false);

        try
        {
            // Set purge limit to 1 article
            DummyDataFeed feed = new DummyDataFeed();
            feed.setPurgeLimit(1);

            // Create 2 unread articles and one read and add them to the feed
            StandardArticle art1 = article(1);
            art1.setRead(false);
            feed.appendArticle(art1);

            StandardArticle art2 = article(2);
            art2.setRead(true);
            feed.appendArticle(art2);

            StandardArticle art3 = article(3);
            art3.setRead(false);
            feed.appendArticle(art3);

            // Clean and check
            feed.clean();
            assertEquals("Unread articles should not be purged.", 2, feed.getArticlesCount());
            assertTrue("Wrong article.", art1 == feed.getArticleAt(0));
            assertTrue("Wrong article.", art3 == feed.getArticleAt(1));
        } finally
        {
            DataFeed.setGlobalPurgeUnread(globalPurgeUnead);
        }
    }

    /**
     * Tests the case when <code>purgeUnread</code> is FALSE meaning that the
     * feed should not remove unread articles during cleanup.
     *
     * Covers the case when there're two read articles, one unread and purge limit set to 1.
     * Unread article should stay.
     */
    public void testCleanNoPurgeUnread3()
    {
        // Disable purgin unread globally
        boolean globalPurgeUnead = DataFeed.isGlobalPurgeUnread();
        DataFeed.setGlobalPurgeUnread(false);

        try
        {
            // Set purge limit to 1 article
            DummyDataFeed feed = new DummyDataFeed();
            feed.setPurgeLimit(1);

            // Create 2 unread articles and one read and add them to the feed
            StandardArticle art1 = article(1);
            art1.setRead(true);
            feed.appendArticle(art1);

            StandardArticle art2 = article(2);
            art2.setRead(false);
            feed.appendArticle(art2);

            StandardArticle art3 = article(3);
            art3.setRead(true);
            feed.appendArticle(art3);

            // Clean and check
            feed.clean();
            assertEquals("Unread articles should not be purged.", 1, feed.getArticlesCount());
            assertTrue("Wrong article.", art2 == feed.getArticleAt(0));
        } finally
        {
            DataFeed.setGlobalPurgeUnread(globalPurgeUnead);
        }
    }

    /**
     * Tests the computation of number of articles for removal.
     */
    public void testCalcArticlesRemove()
    {
        assertEquals("Nothing, because the total is 0.",
            0, DataFeed.calcArticlesToRemove(0, 0, 0, 3, false));
        assertEquals("Nothing, because the total is 0.",
            0, DataFeed.calcArticlesToRemove(0, 0, 0, 3, true));

        assertEquals("Only one article can be removed because the rest are unread.",
            1, DataFeed.calcArticlesToRemove(5, 4, 0, 3, false));
        assertEquals("2 articles, because only two are unread and it's below the limit.",
            2, DataFeed.calcArticlesToRemove(5, 2, 0, 3, false));
        assertEquals("Nothing can be removed as total equals the limit.",
            0, DataFeed.calcArticlesToRemove(3, 3, 0, 3, false));
        assertEquals("Nothing can be removed as total equals the limit.",
            0, DataFeed.calcArticlesToRemove(3, 2, 0, 3, false));

        assertEquals("2 articles, because removing unread is allowed.",
            2, DataFeed.calcArticlesToRemove(5, 4, 0, 3, true));
        assertEquals("2 articles, because removing unread is allowed.",
            2, DataFeed.calcArticlesToRemove(5, 2, 0, 3, true));
        assertEquals("Nothing can be removed as total equals the limit.",
            0, DataFeed.calcArticlesToRemove(3, 3, 0, 3, true));
        assertEquals("Nothing can be removed as total equals the limit.",
            0, DataFeed.calcArticlesToRemove(3, 2, 0, 3, true));

        // Pinned articles never count
        assertEquals("Pinned articles never count",
            0, DataFeed.calcArticlesToRemove(6, 0, 3, 3, true));
        assertEquals("Pinned articles never count",
            1, DataFeed.calcArticlesToRemove(6, 0, 2, 3, true));
    }

    /**
     * Tests how the last update server time field is populated during updates.
     */
    public void testSavingLastUpdateServerTime()
    {
        Channel chan = new Channel();
        chan.setLastUpdateServerTime(-1);
        chan.addItem(new Item("1"));

        DummyDataFeed feed = new DummyDataFeed();
        feed.setChannel(chan);
        feed.update();

        assertEquals("Wrong last update server time.", -1, feed.getLastUpdateServerTime());

        chan.setLastUpdateServerTime(1);
        chan.addItem(new Item("2"));
        feed.update();

        assertEquals("Wrong last update server time.", 1, feed.getLastUpdateServerTime());
    }

    /**
     * BUG: When pinned articles were being set, read articles list was being reset. 
     */
    public void testSettingKeysSeparately()
    {
        String readKeys = "12345";
        String pinnedKeys = "67890";

        DummyDataFeed feed = new DummyDataFeed();
        feed.setReadArticlesKeys(readKeys);
        feed.setPinnedArticlesKeys(pinnedKeys);

        assertEquals(readKeys, feed.getReadArticlesKeys());
        assertEquals(pinnedKeys, feed.getPinnedArticlesKeys());
    }
}
