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
// $Id: TestSearchFeedsManager.java,v 1.8 2007/02/06 15:34:02 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.domain.*;
import com.salas.bb.domain.query.ICriteria;
import com.salas.bb.domain.query.articles.ArticleStatusProperty;
import com.salas.bb.domain.query.articles.FeedStarzProperty;
import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.domain.query.general.StringEqualsCO;
import com.salas.bb.domain.utils.DomainEventsListener;
import junit.framework.TestCase;

/**
 * This suite contains tests for <code>SearchFeedsManager</code> unit.
 */
public class TestSearchFeedsManager extends TestCase
{
    private StandardArticle     readArticle;
    private StandardArticle     unreadArticle;
    private DataFeed            dataFeed;
    private StandardGuide       standardGuide;
    private GuidesSet           guidesSet;

    private DomainEventsListener domainEventsListener;

    private SearchFeed          searchFeed;
    private Query               searchFeedQuery;

    private SearchFeedsManager  manager;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        readArticle = new StandardArticle("readArticle");
        readArticle.setTitle("read");
        readArticle.setRead(true);

        unreadArticle = new StandardArticle("unreadArticle");
        unreadArticle.setTitle("unread");
        unreadArticle.setRead(false);

        dataFeed = new DirectFeed();
        dataFeed.appendArticle(readArticle);
        dataFeed.appendArticle(unreadArticle);

        standardGuide = new StandardGuide();
        standardGuide.add(dataFeed);

        guidesSet = new GuidesSet();
        guidesSet.add(standardGuide);

        domainEventsListener = new DomainEventsListener(guidesSet);

        searchFeedQuery = new Query();
        ICriteria unreadStatusCriteria = searchFeedQuery.addCriteria();
        unreadStatusCriteria.setProperty(ArticleStatusProperty.INSTANCE);
        unreadStatusCriteria.setComparisonOperation(StringEqualsCO.INSTANCE);
        unreadStatusCriteria.setValue(ArticleStatusProperty.VALUE_UNREAD);

        searchFeed = new SearchFeed();
        searchFeed.setBaseTitle("Unread");
        searchFeed.setArticlesLimit(2);
        searchFeed.setQuery(searchFeedQuery);

        standardGuide.add(searchFeed);

        manager = new SearchFeedsManager(guidesSet);
        domainEventsListener.addDomainListener(manager);
    }

    /**
     * Tests initial scanning of all feeds for the articles to display.
     */
    public void testRunningInitialQuery()
    {
        manager.runQuery(searchFeed);

        assertEquals(1, searchFeed.getArticlesCount());
        assertEquals(unreadArticle, searchFeed.getArticleAt(0));
    }

    /**
     * Tests the reaction of manager on changes of feed properties.
     */
    public void testFeedPropertyHasChanged()
    {
        // Add dependence from feed starz (2 starz required)
        searchFeedQuery.setAndQuery(true);
        ICriteria feedStarz = searchFeedQuery.addCriteria();
        feedStarz.setProperty(FeedStarzProperty.INSTANCE);
        feedStarz.setComparisonOperation(StringEqualsCO.INSTANCE);
        feedStarz.setValue("2");

        // Feed rating is in range [0;4], but property rating is in range [1;5]
        dataFeed.setRating(0);

        manager.runQuery(searchFeed);
        assertEquals("There are not enough starz to select articles.",
            0, searchFeed.getArticlesCount());

        dataFeed.setRating(1);
        assertEquals("There are enough starz to select articles.",
            1, searchFeed.getArticlesCount());

        dataFeed.setRating(0);
        assertEquals("There are not enough starz to select articles.",
            0, searchFeed.getArticlesCount());
    }

    /**
     * Tests the reaction of manager on addition of new articles.
     */
    public void testArticleAdded()
    {
        manager.runQuery(searchFeed);

        StandardArticle newUnreadArticle = new StandardArticle("new unread article");
        newUnreadArticle.setTitle("new unread article");
        newUnreadArticle.setRead(false);
        dataFeed.appendArticle(newUnreadArticle);

        assertEquals("Expecting new article.", 2, searchFeed.getArticlesCount());
        assertTrue(newUnreadArticle == searchFeed.getArticleAt(1) || searchFeed.getArticleAt(0) == newUnreadArticle);
    }

    /**
     * Tests the update of the search feed when its query changes.
     */
    public void testSearchFeedQueryHasChanged()
    {
        manager.runQuery(searchFeed);
        assertEquals(1, searchFeed.getArticlesCount());

        // Setting the criteria which should not match
        searchFeedQuery.setAndQuery(true);
        ICriteria feedStarz = searchFeedQuery.addCriteria();
        feedStarz.setProperty(FeedStarzProperty.INSTANCE);
        feedStarz.setComparisonOperation(StringEqualsCO.INSTANCE);
        feedStarz.setValue("5");

        manager.queryUpdated(searchFeed);
        assertEquals("None of articles meet the criteria.",
            0, searchFeed.getArticlesCount());
    }

    /**
     * Tests the scenario when search feed has articles from some data feed. The data
     * feed gets removed by user. The articles should also leave the search feed.
     */
    public void testFeedRemoved()
    {
        // make sure search feed has the article from data feed
        manager.runQuery(searchFeed);
        assertEquals(1, searchFeed.getArticlesCount());

        standardGuide.remove(dataFeed);

        assertEquals("Feed is removed. Corresponding article should leave search feed.",
            0, searchFeed.getArticlesCount());
    }

    /**
     * Tests the scenario when search feed has article and this article gets removed
     * from its data feed. The article should leave search feed.
     */
    public void testArticleRemoved()
    {
        // make sure search feed has the article from data feed
        manager.runQuery(searchFeed);
        assertEquals(1, searchFeed.getArticlesCount());

        dataFeed.setPurgeLimit(0);

        assertEquals("Feed cleanup failed.", 0, dataFeed.getArticlesCount());
        assertEquals("Article was removed. It should leave search feed as well.",
            0, searchFeed.getArticlesCount());
    }
}
