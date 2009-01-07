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
// $Id: TestFeedsList.java,v 1.2 2006/11/15 11:50:22 spyromus Exp $
//

package com.salas.bb.domain;

import junit.framework.TestCase;

import java.net.URL;
import java.net.MalformedURLException;

import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.domain.query.articles.ArticleTextProperty;
import com.salas.bb.domain.query.ICriteria;
import com.salas.bb.domain.query.general.StringEqualsCO;
import com.salas.bb.domain.querytypes.QueryType;

/**
 * Tests flat feeds list.
 */
public class TestFeedsList extends TestCase
{
    private FeedsList list;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        list = new FeedsList();
    }

    // ---------------------------------------------------------------------------------------------
    // Acceptance
    // ---------------------------------------------------------------------------------------------

    /**
     * Looking for direct feeds.
     */
    public void testLookingForDirectFeeds()
        throws MalformedURLException
    {
        URL url1 = new URL("file://1");
        URL url2 = new URL("file://2");

        DirectFeed feed = new DirectFeed();
        feed.setXmlURL(url1);

        list.add(feed);

        assertTrue("Wrong feed.", feed == list.findDirectFeed(url1));
        assertNull("No such feed.", list.findDirectFeed(url2));
    }

    /**
     * Looking for query feeds.
     */
    public void testLookingForQueryFeeds()
    {
        QueryType type = QueryType.getQueryType(QueryType.TYPE_AMAZON_BOOKS);
        QueryType type2 = QueryType.getQueryType(QueryType.TYPE_CONNOTEA);
        String parameter = "a";

        QueryFeed feed = new QueryFeed();
        feed.setQueryType(type);
        feed.setParameter(parameter);
        list.add(feed);

        // Checking
        assertTrue("Wrong feed.", feed == list.findQueryFeed(type, parameter));
        assertNull("No such feed.", list.findQueryFeed(type, "b"));
        assertNull("No such feed.", list.findQueryFeed(type2, parameter));
    }

    /**
     * Looking for search feeds.
     */
    public void testLookingForSearchFeeds()
    {
        // Query 1
        Query query = new Query();
        ICriteria criteria = query.addCriteria();
        criteria.setProperty(ArticleTextProperty.INSTANCE);
        criteria.setComparisonOperation(StringEqualsCO.INSTANCE);
        criteria.setValue("a");

        // Query 2
        Query query2 = new Query();
        ICriteria criteria2 = query.addCriteria();
        criteria2.setProperty(ArticleTextProperty.INSTANCE);
        criteria2.setComparisonOperation(StringEqualsCO.INSTANCE);
        criteria2.setValue("b");

        // Search Feed with query 1
        SearchFeed feed = new SearchFeed();
        feed.setQuery(query);
        list.add(feed);

        // Checking
        assertTrue("Wrong feed.", feed == list.findSearchFeed(query));
        assertNull("No such feed.", list.findSearchFeed(query2));
    }

    // ---------------------------------------------------------------------------------------------
    // Unit tests
    // ---------------------------------------------------------------------------------------------

    /**
     * Accessing feeds through indexes.
     */
    public void testAccessingFeeds()
    {
        assertEquals("List should be empty.", 0, list.getFeedsCount());

        DirectFeed feed = new DirectFeed();

        list.add(feed);
        assertEquals("The feed should be added.", 1, list.getFeedsCount());
        assertTrue("Wrong feed.", feed == list.getFeedAt(0));
    }
}
