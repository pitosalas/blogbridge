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
// $Id: TestSearchFeedEvents.java,v 1.8 2006/07/18 10:19:49 spyromus Exp $
//

package com.salas.bb.domain;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.domain.query.articles.ArticleStatusProperty;
import com.salas.bb.domain.query.ICriteria;
import com.salas.bb.domain.query.general.StringEqualsCO;
import com.salas.bb.domain.utils.DomainEventsListener;
import com.salas.bb.core.SearchFeedsManager;

import java.util.Date;

/**
 * This suite contains tests for <code>SearchFeed</code> unit.
 */
public class TestSearchFeedEvents extends MockObjectTestCase
{
    private static int articlesCounter = 0;

    private SearchFeed searchFeed;
    private DirectFeed dataFeed;
    private Mock listener;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        listener = new Mock(IFeedListener.class);

        dataFeed = new DirectFeed();

        StandardGuide standardGuide = new StandardGuide();
        standardGuide.add(dataFeed);

        GuidesSet guidesSet = new GuidesSet();
        guidesSet.add(standardGuide);

        DomainEventsListener domainEventsListener = new DomainEventsListener(guidesSet);

        searchFeed = new SearchFeed();
        searchFeed.setBaseTitle("Unread");
        searchFeed.setArticlesLimit(1);
        searchFeed.setQuery(createArticlesQuery(false));
        searchFeed.addListener((IFeedListener)listener.proxy());

        standardGuide.add(searchFeed);

        SearchFeedsManager manager = new SearchFeedsManager(guidesSet);
        domainEventsListener.addDomainListener(manager);
    }

    /**
     * Tests events when adding single article matching the criteria.
     */
    public void testSingleAdditionOfMatchingArticle()
    {
        searchFeed.setArticlesLimit(1);
        assertEquals(0, searchFeed.getArticlesCount());

        // adding unread article
        StandardArticle unreadArticle = createArticle(false);

        expectArticleAdded(unreadArticle);
        expectUnreadCounterChange(0, 1);

        dataFeed.appendArticle(unreadArticle);

        listener.verify();
    }

    /**
     * Firing events on the query change.
     */
    public void testQueryChange()
    {
        searchFeed.setArticlesLimit(1);
        assertEquals(0, searchFeed.getArticlesCount());

        // We have four articles: two read and two unread.
        // The indices of articles reflect their order, meaning the greater index --
        // the newer article.
        StandardArticle unreadArticle1 = createArticle(false);
        StandardArticle unreadArticle2 = createArticle(false);
        StandardArticle readArticle1 = createArticle(true);
        StandardArticle readArticle2 = createArticle(true);

        // When we will be adding articles we expect the first unread to be added
        // first and later replaced by second unread as it's newer and limit is set
        // to 1.
        expectArticleAdded(unreadArticle1);
        expectUnreadCounterChange(0, 1);
        expectArticleAdded(unreadArticle2);
        expectArticleRemoved(unreadArticle1);

        dataFeed.appendArticle(unreadArticle1);
        dataFeed.appendArticle(unreadArticle2);
        dataFeed.appendArticle(readArticle1);
        dataFeed.appendArticle(readArticle2);

        listener.verify();

        // Wen we will be changing query we expect the unread article to be replaced
        // with read article.
        Query readQuery = createArticlesQuery(true);

        expectUnreadCounterChange(1, 0);
        expectQueryChange(searchFeed.getQuery(), readQuery);
        expectProcessingChange(true);
        expectArticleRemoved(unreadArticle2);
        expectArticleAdded(readArticle1);
        expectArticleRemoved(readArticle1);
        expectArticleAdded(readArticle2);
        expectProcessingChange(false);
        expectUpdateTimeChange();
                                         
        searchFeed.setQuery(readQuery);

        listener.verify();
    }

    private void expectUpdateTimeChange()
    {
        listener.expects(once()).method("propertyChanged").with(same(searchFeed),
            eq(DirectFeed.PROP_LAST_UPDATE_TIME), not(eq(-1l)), not(eq(-1l)));
    }

    /**
     * Tests replacement of leaving articles (which do not match any more) with the
     * articles from back-pack (which aren't visible because of articlesLimit).
     */
    public void testReplacement()
    {
        searchFeed.setArticlesLimit(1);
        assertEquals(0, searchFeed.getArticlesCount());

        // We have two unread articles. We will be reading them and watching
        // how the changes in visibility are reported.
        StandardArticle unreadArticle1 = createArticle(false);
        StandardArticle unreadArticle2 = createArticle(false);

        expectArticleAdded(unreadArticle1);
        expectUnreadCounterChange(0, 1);
        expectArticleAdded(unreadArticle2);
        expectArticleRemoved(unreadArticle1);

        dataFeed.appendArticle(unreadArticle1);
        dataFeed.appendArticle(unreadArticle2);

        listener.verify();

        // Now read the second to let it go and to let first appear
        expectArticleRemoved(unreadArticle2);
        expectUnreadCounterChange(1, 0);

        unreadArticle2.setRead(true);

        listener.verify();
    }

    private void expectProcessingChange(boolean falseToTrue)
    {
        listener.expects(once()).method("propertyChanged").with(same(searchFeed),
            eq(SearchFeed.PROP_PROCESSING), eq(!falseToTrue), eq(falseToTrue));
    }

    private void expectQueryChange(Query oldQuery, Query newQuery)
    {
        listener.expects(once()).method("propertyChanged").with(same(searchFeed),
            eq(SearchFeed.PROP_QUERY), same(oldQuery), same(newQuery));
    }

    private void expectArticleRemoved(StandardArticle aUnreadArticle)
    {
        listener.expects(once()).method("articleRemoved").with(same(searchFeed),
            same(aUnreadArticle));
    }

    private void expectArticleAdded(StandardArticle aUnreadArticle)
    {
        listener.expects(once()).method("articleAdded").with(same(searchFeed),
            same(aUnreadArticle));
    }

    private void expectUnreadCounterChange(int from, int to)
    {
        listener.expects(once()).method("propertyChanged").with(same(searchFeed),
            eq(IFeed.PROP_UNREAD_ARTICLES_COUNT), eq(from), eq(to));
    }

    private static synchronized StandardArticle createArticle(boolean read)
    {
        String txt = Integer.toString(articlesCounter);

        StandardArticle article = new StandardArticle(txt);
        article.setTitle(txt);
        article.setRead(read);
        article.setPublicationDate(new Date(articlesCounter));

        articlesCounter++;

        return article;
    }

    private static Query createArticlesQuery(boolean read)
    {
        Query sampleQuery = new Query();
        ICriteria sampleCriteria = sampleQuery.addCriteria();
        sampleCriteria.setProperty(ArticleStatusProperty.INSTANCE);
        sampleCriteria.setComparisonOperation(StringEqualsCO.INSTANCE);
        sampleCriteria.setValue(read
            ? ArticleStatusProperty.VALUE_READ
            : ArticleStatusProperty.VALUE_UNREAD);

        return sampleQuery;
    }
}
