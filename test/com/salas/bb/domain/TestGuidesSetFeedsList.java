package com.salas.bb.domain;

import com.salas.bb.domain.query.ICriteria;
import com.salas.bb.domain.query.articles.ArticleTextProperty;
import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.domain.query.general.StringEqualsCO;
import com.salas.bb.domain.querytypes.QueryType;
import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This suite contains tests for <code>GuidesSet</code> unit.
 */
public class TestGuidesSetFeedsList extends TestCase
{
    private GuidesSet set;
    private StandardGuide guide;

    protected void setUp()
        throws Exception
    {
        set = new GuidesSet();
        guide = new StandardGuide();
    }

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
        guide.add(feed);

        set.add(guide);

        assertTrue("Wrong feed.", feed == set.findDirectFeed(url1));
        assertNull("No such feed.", set.findDirectFeed(url2));
    }

    /**
     * Looking for query feeds.
     */
    public void testLookingForQueryFeeds()
    {
        set.add(guide);

        QueryType type = QueryType.getQueryType(QueryType.TYPE_AMAZON_BOOKS);
        QueryType type2 = QueryType.getQueryType(QueryType.TYPE_CONNOTEA);
        String parameter = "a";

        QueryFeed feed = new QueryFeed();
        feed.setQueryType(type);
        feed.setParameter(parameter);

        guide.add(feed);

        // Checking
        assertTrue("Wrong feed.", feed == set.findQueryFeed(type, parameter));
        assertNull("No such feed.", set.findQueryFeed(type, "b"));
        assertNull("No such feed.", set.findQueryFeed(type2, parameter));
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
        guide.add(feed);
        set.add(guide);

        // Checking
        assertTrue("Wrong feed.", feed == set.findSearchFeed(query));
        assertNull("No such feed.", set.findSearchFeed(query2));
    }
}
