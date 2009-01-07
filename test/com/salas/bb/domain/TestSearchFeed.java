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
// $Id: TestSearchFeed.java,v 1.11 2007/02/06 15:34:02 spyromus Exp $
//

package com.salas.bb.domain;

import junit.framework.TestCase;
import com.salas.bb.domain.query.ICriteria;
import com.salas.bb.domain.query.general.StringEqualsCO;
import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.domain.query.articles.ArticleStatusProperty;

import java.util.Date;

/**
 * This suite contains tests for <code>SearchFeed</code> unit.
 */
public class TestSearchFeed extends TestCase
{
    // ---------------------------------------------------------------------------------------------
    // Acceptance Tests
    // ---------------------------------------------------------------------------------------------

    /**
     * Simple feed creating scenario.
     */
    public void testCreatingFeed()
    {
        Query sampleQuery = createUnreadArticlesQuery();
        createSearchFeed(sampleQuery, 10);
    }

    /**
     * Fetching articles from feeds and replacements scenario.
     */
    public void testFetchingData()
    {
        Query sampleQuery = createUnreadArticlesQuery();
        SearchFeed sampleFeed = createSearchFeed(sampleQuery, 1);

        // Suppose we have some article at some data feed
        IArticle articleFromSomeFeed = createArticle(10);
        IArticle olderArticleFromSomeFeed = createArticle(9);
        IArticle newerArticleFromSomeFeed = createArticle(11);

        // We can check if it's matching our criteria and add it to the feed if it is.
        sampleFeed.addArticleIfMatching(articleFromSomeFeed);
        assertEquals(1, sampleFeed.getArticlesCount());

        sampleFeed.addArticleIfMatching(newerArticleFromSomeFeed);
        assertEquals(1, sampleFeed.getArticlesCount());
        assertTrue("Newer article should replace the older.",
            sampleFeed.getArticleAt(0) == newerArticleFromSomeFeed);

        sampleFeed.addArticleIfMatching(olderArticleFromSomeFeed);
        assertEquals(1, sampleFeed.getArticlesCount());
        assertTrue("Older article should not replace the older.",
            sampleFeed.getArticleAt(0) == newerArticleFromSomeFeed);
    }

    /**
     * When user changes query of the search feed all of the articles should be
     * removed and new fetching cycle initiated. As the fetching is internal process
     * the only change should happen to the search feed is clearning of the articles
     * list.
     */
    public void testChangingQuery()
    {
        Query sampleQuery = createUnreadArticlesQuery();
        Query similarQuery = createUnreadArticlesQuery();
        Query differentQuery = createNoArticlesQuery();

        // We initialize the feed with some sample query and article
        SearchFeed sampleFeed = createSearchFeed(sampleQuery, 1);
        sampleFeed.addArticleIfMatching(createArticle(10));

        // Now we change query to the similar one -- article should stay
        sampleFeed.setQuery(similarQuery);
        assertEquals(1, sampleFeed.getArticlesCount());

        // Now we change query to the different one -- articles should be rescanned
// This task is no longer on the search feed object. It's handled by the search feeds manager
//        sampleFeed.setQuery(differentQuery);
//        assertEquals(0, sampleFeed.getArticlesCount());
    }

    /**
     * Tests automatic removing of articles which are no longer matching the criteria.
     */
    public void testAutoRemovingUnmatchingArticles()
    {
        Query sampleQuery = createUnreadArticlesQuery();
        SearchFeed sampleFeed = createSearchFeed(sampleQuery, 1);

        IArticle article = createArticle(10);
        sampleFeed.addArticleIfMatching(article);
        assertEquals(1, sampleFeed.getArticlesCount());

        // Mark article as read. It no longer matches the search feed criteria and
        // should be automatically removed.
        article.setRead(true);
        assertEquals(0, sampleFeed.getArticlesCount());
    }

    /**
     * Tests shifting new articles from invisible pool when visible articles
     * become non-matching.
     */
    public void testShifting()
    {
        Query sampleQuery = createUnreadArticlesQuery();
        SearchFeed sampleFeed = createSearchFeed(sampleQuery, 1);

        // Adding two articles with first one visible from the start
        IArticle article1 = createArticle(10);
        sampleFeed.addArticleIfMatching(article1);
        IArticle article2 = createArticle(9);
        sampleFeed.addArticleIfMatching(article2);
        assertEquals(1, sampleFeed.getArticlesCount());
        assertTrue(article1 == sampleFeed.getArticleAt(0));

        // Marking first article as read, so that it becomes non-matching and shifted out.
        // The second article should take its place.
        article1.setRead(true);
        assertEquals(1, sampleFeed.getArticlesCount());
        assertTrue(article2 == sampleFeed.getArticleAt(0));
    }

    // ---------------------------------------------------------------------------------------------
    // Supplementary functions
    // ---------------------------------------------------------------------------------------------

    private static IArticle createArticle(int aPublicationTimestamp)
    {
        StandardArticle article = new StandardArticle("");
        article.setPublicationDate(new Date(aPublicationTimestamp));
        article.setTitle(Double.toString(Math.random()));
        article.setRead(false);

        return article;
    }

    private SearchFeed createSearchFeed(Query aSampleQuery, int anArticlesLimit)
    {
        SearchFeed searchFeed = new SearchFeed();
        searchFeed.setBaseTitle("Some title");
        searchFeed.setArticlesLimit(anArticlesLimit);
        searchFeed.setQuery(aSampleQuery);

        return searchFeed;
    }

    private Query createNoArticlesQuery()
    {
        Query sampleQuery = new Query();

        ICriteria sampleCriteria = sampleQuery.addCriteria();
        sampleCriteria.setProperty(ArticleStatusProperty.INSTANCE);
        sampleCriteria.setComparisonOperation(StringEqualsCO.INSTANCE);
        sampleCriteria.setValue("read");

        sampleCriteria = sampleQuery.addCriteria();
        sampleCriteria.setProperty(ArticleStatusProperty.INSTANCE);
        sampleCriteria.setComparisonOperation(StringEqualsCO.INSTANCE);
        sampleCriteria.setValue("unread");

        sampleQuery.setAndQuery(true);

        return sampleQuery;
    }

    private Query createUnreadArticlesQuery()
    {
        Query sampleQuery = new Query();
        ICriteria sampleCriteria = sampleQuery.addCriteria();
        sampleCriteria.setProperty(ArticleStatusProperty.INSTANCE);
        sampleCriteria.setComparisonOperation(StringEqualsCO.INSTANCE);
        sampleCriteria.setValue("unread");

        return sampleQuery;
    }
}
