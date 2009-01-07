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
// $Id: TestFeedDisplayModel.java,v 1.7 2008/02/28 15:59:53 spyromus Exp $
//

package com.salas.bb.views.feeds;

import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.StandardArticle;
import com.salas.bb.utils.Constants;

/**
 * This suite contains tests for {@link FeedDisplayModel} unit.
 */
public class TestFeedDisplayModel
    extends AbstractFeedDisplayTestCase
{
    private FeedDisplayModel       model;
    private DirectFeed          feed;
    private IArticle            articleTomorrow;
    private IArticle            articleYesterday;
    private IArticle            articleNow;
    private IArticle            article6DaysAgo;
    private IArticle            article13DaysAgo;
    private IArticle            article30DaysAgo;

    /** Initializes the tests. */
    protected void setUp()
        throws Exception
    {
        super.setUp();

        model = new FeedDisplayModel();

        feed = new DirectFeed();

        articleTomorrow = appendArticle(feed, DELTA_TOMORROW);
        article13DaysAgo = appendArticle(feed, DELTA_13_DAYS_AGO);
        articleYesterday = appendArticle(feed, DELTA_YESTERDAY);
        articleNow = appendArticle(feed, DELTA_NOW);
        article6DaysAgo = appendArticle(feed, DELTA_6_DAYS_AGO);
        article30DaysAgo = appendArticle(feed, DELTA_30_DAYS_AGO);
    }

    /**
     * Tests straight sorting order.
     */
    public void testStraightSorting()
    {
        model.setFeed(feed);

        assertEquals(6, model.getArticlesCount());
        assertTrue(model.getArticle(0) == articleTomorrow);
        assertTrue(model.getArticle(1) == articleNow);
        assertTrue(model.getArticle(2) == articleYesterday);
        assertTrue(model.getArticle(3) == article6DaysAgo);
        assertTrue(model.getArticle(4) == article13DaysAgo);
        assertTrue(model.getArticle(5) == article30DaysAgo);
        assertGrouping(false);
    }

    /**
     * Tests reverse sorting order.
     */
    public void testReverseSorting()
    {
        model.setFeed(feed);
        model.setAscending(true);

        assertEquals(6, model.getArticlesCount());
        assertTrue(model.getArticle(5) == articleTomorrow);
        assertTrue(model.getArticle(4) == articleNow);
        assertTrue(model.getArticle(3) == articleYesterday);
        assertTrue(model.getArticle(2) == article6DaysAgo);
        assertTrue(model.getArticle(1) == article13DaysAgo);
        assertTrue(model.getArticle(0) == article30DaysAgo);
        assertGrouping(true);

        // Once again to see how the order is taken when feed changes
        model.setFeed(feed);

        assertEquals(6, model.getArticlesCount());
        assertTrue(model.getArticle(5) == articleTomorrow);
        assertTrue(model.getArticle(4) == articleNow);
        assertTrue(model.getArticle(3) == articleYesterday);
        assertTrue(model.getArticle(2) == article6DaysAgo);
        assertTrue(model.getArticle(1) == article13DaysAgo);
        assertTrue(model.getArticle(0) == article30DaysAgo);
        assertGrouping(true);
    }

    /**
     * Checks grouping.
     */
    private void assertGrouping(boolean asc)
    {
        int group = asc ? model.getGroupsCount() - 1 : 0;
        int delta = asc ? -1 : 1;

        IArticle[] grpFuture = model.getGroup(group);
        assertEquals(GroupsSetup.getGroupTitle(0), model.getGroupName(group));
        assertEquals(1, grpFuture.length);
        assertTrue(grpFuture[0] == articleTomorrow);

        group += delta;
        IArticle[] grpToday = model.getGroup(group);
        assertEquals(GroupsSetup.getGroupTitle(1), model.getGroupName(group));
        assertEquals(1, grpToday.length);
        assertTrue(grpToday[0] == articleNow);

        group += delta;
        IArticle[] grpYesterday = model.getGroup(group);
        assertEquals(GroupsSetup.getGroupTitle(2), model.getGroupName(group));
        assertEquals(1, grpYesterday.length);
        assertTrue(grpYesterday[0] == articleYesterday);

        group += delta;
        IArticle[] grpWeekAgo = model.getGroup(group);
        assertEquals(GroupsSetup.getGroupTitle(3), model.getGroupName(group));
        assertEquals(1, grpWeekAgo.length);
        assertTrue(grpWeekAgo[0] == article6DaysAgo);

        group += delta;
        IArticle[] grp2WeeksAgo = model.getGroup(group);
        assertEquals(GroupsSetup.getGroupTitle(4), model.getGroupName(group));
        assertEquals(1, grp2WeeksAgo.length);
        assertTrue(grp2WeeksAgo[0] == article13DaysAgo);

        group += delta;
        IArticle[] grpOlder = model.getGroup(group);
        assertEquals(GroupsSetup.getGroupTitle(5), model.getGroupName(group));
        assertEquals(1, grpOlder.length);
        assertTrue(grpOlder[0] == article30DaysAgo);
    }

    /**
     * Tests inserting article in the array.
     */
    public void testInsertArticle()
    {
        IArticle[] emptyArticles = new IArticle[0];
        StandardArticle a1 = new StandardArticle("1");
        StandardArticle a2 = new StandardArticle("2");

        IArticle[] resultArticles;

        resultArticles = FeedDisplayModel.insertArticle(emptyArticles, a1, 0);
        assertEquals("Nothing has been added.", 1, resultArticles.length);
        assertTrue("Wrong article added.", a1 == resultArticles[0]);

        resultArticles = FeedDisplayModel.insertArticle(resultArticles, a2, 0);
        assertEquals("Nothing has been added.", 2, resultArticles.length);
        assertTrue("Wrong article added.", a2 == resultArticles[0]);
        assertTrue("Wrong article added.", a1 == resultArticles[1]);

        resultArticles = FeedDisplayModel.insertArticle(emptyArticles, a1, 0);
        resultArticles = FeedDisplayModel.insertArticle(resultArticles, a2, 1);
        assertEquals("Nothing has been added.", 2, resultArticles.length);
        assertTrue("Wrong article added.", a2 == resultArticles[1]);
        assertTrue("Wrong article added.", a1 == resultArticles[0]);
    }

    /**
     * Tests removing article from the array.
     */
    public void testRemoveArticle()
    {
        IArticle[] emptyArticles = new IArticle[0];
        IArticle[] result;

        result = FeedDisplayModel.removeArticle(emptyArticles, articleNow);
        assertEquals(0, result.length);

        IArticle[] someArticles = new IArticle[]
        {
            articleNow, articleTomorrow, articleYesterday
        };

        // Removing missing article
        result = FeedDisplayModel.removeArticle(someArticles, article13DaysAgo);
        assertEquals(3, result.length);

        // Removing article from the head
        result = FeedDisplayModel.removeArticle(someArticles, articleNow);
        assertEquals(2, result.length);
        assertTrue(articleTomorrow == result[0]);
        assertTrue(articleYesterday == result[1]);

        // Removing article from the middle
        result = FeedDisplayModel.removeArticle(someArticles, articleTomorrow);
        assertEquals(2, result.length);
        assertTrue(articleNow == result[0]);
        assertTrue(articleYesterday == result[1]);

        // Removing article from the tail
        result = FeedDisplayModel.removeArticle(someArticles, articleYesterday);
        assertEquals(2, result.length);
        assertTrue(articleNow == result[0]);
        assertTrue(articleTomorrow == result[1]);
    }

    /**
     * Tests sorting the articles in groups.
     */
    public void testSortingInGroupsDesc()
    {
        IArticle article5DaysAgo = createArticle(DELTA_6_DAYS_AGO + DAY);

        // Set newest top order
        model.setAscending(false);
        model.onArticleAdded(article6DaysAgo);
        model.onArticleAdded(article5DaysAgo);

        IArticle[] lastWeekGroup = model.getGroup(3);
        assertEquals(2, lastWeekGroup.length);
        assertTrue("Wrong order.", article5DaysAgo == lastWeekGroup[0]);
    }

    /**
     * Tests sorting the articles in groups.
     */
    public void testSortingInGroupsAsc()
    {
        IArticle article5DaysAgo = createArticle(DELTA_6_DAYS_AGO + DAY);

        // Set newest top order
        model.setAscending(true);
        model.onArticleAdded(article6DaysAgo);
        model.onArticleAdded(article5DaysAgo);

        IArticle[] lastWeekGroup = model.getGroup(2);
        assertEquals(2, lastWeekGroup.length);
        assertTrue("Wrong order.", article6DaysAgo == lastWeekGroup[0]);
    }

    /**
     * Testing switching between various modes.
     */
    public void testSwitchingModes()
    {
        // Model is in Full mode when all articles are visible
        model.onArticleAdded(articleNow);
        articleNow.setRead(true);

        IArticle[] todayGroup = model.getGroup(1);
        assertEquals(1, todayGroup.length);

        // Switching to Unread only mode
        model.setFilter(IFeedDisplayConstants.FILTER_UNREAD);
        todayGroup = model.getGroup(1);
        assertEquals(0, todayGroup.length);

        // Switching to all mode
        model.setFilter(IFeedDisplayConstants.FILTER_ALL);
        todayGroup = model.getGroup(1);
        assertEquals(1, todayGroup.length);
    }

    /**
     * Tests suppression of old articles.
     */
    public void testSuppression()
    {
        model.setMaxArticleAge(2 * Constants.MILLIS_IN_DAY);
        model.setFeed(feed);

        int articles = 0;
        for (int i = 0; i < model.getGroupsCount(); i++)
        {
            articles += model.getGroup(i).length;
        }

        assertEquals("Only future, today's and yesterday's articles should be visible.",
            3, articles);
    }
}
