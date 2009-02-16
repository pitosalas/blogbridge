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
// $Id: FeedsList.java,v 1.8 2007/03/01 14:18:09 spyromus Exp $
//

package com.salas.bb.domain;

import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.domain.querytypes.QueryType;
import com.salas.bb.utils.IdentityList;

import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * Flat list of all feeds in the system.
 */
public class FeedsList
{
    private final List<IFeed> feeds = new IdentityList<IFeed>();

    /**
     * Adds a feed.
     *
     * @param feed feed.
     */
    public void add(IFeed feed)
    {
        if (!feeds.contains(feed)) feeds.add(feed);
    }

    /**
     * Removes a feed.
     *
     * @param feed feed.
     */
    public void remove(IFeed feed)
    {
        feeds.remove(feed);
    }

    /**
     * Returns number of feeds currently in repository.
     *
     * @return feeds.
     */
    public int getFeedsCount()
    {
        return feeds.size();
    }

    /**
     * Returns the feed at some index.
     *
     * @param index feed index.
     *
     * @return feed.
     */
    public IFeed getFeedAt(int index)
    {
        return feeds.get(index);
    }

    /**
     * Returns the immutable list of feeds.
     *
     * @return list of feeds.
     */
    public List<IFeed> getFeeds()
    {
        return Collections.unmodifiableList(feeds);
    }

    // ---------------------------------------------------------------------------------------------
    // Finding feeds
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns static direct feed by its XML URL.
     *
     * @param xmlUrl URL.
     *
     * @return feed or <code>NULL</code>.
     */
    public DirectFeed findDirectFeed(URL xmlUrl)
    {
        return findDirectFeed(xmlUrl, false);
    }

    /**
     * Returns static direct feed by its XML URL.
     *
     * @param xmlUrl URL.
     * @param ignoreCase <code>TRUE</code> to compare links case-insensitively.
     *
     * @return feed or <code>NULL</code>.
     */
    public DirectFeed findDirectFeed(URL xmlUrl, boolean ignoreCase)
    {
        DirectFeed feed = null;
        String xmlUrlS = xmlUrl == null ? null : xmlUrl.toString();

        for (int i = 0; feed == null && i < feeds.size(); i++)
        {
            IFeed ifeed = feeds.get(i);
            if (ifeed instanceof DirectFeed)
            {
                DirectFeed dfeed = ((DirectFeed)ifeed);
                URL feedUrl = dfeed.getXmlURL();
                String feedUrlS = feedUrl == null ? null : feedUrl.toString();

                if ((xmlUrlS == null && feedUrlS == null) ||
                    (xmlUrlS != null &&
                        (ignoreCase
                            ? xmlUrlS.equalsIgnoreCase(feedUrlS)
                            : xmlUrlS.equals(feedUrlS)))) feed = dfeed;
            }
        }

        return feed;
    }

    /**
     * Returns query feed by its attributes.
     *
     * @param type      type of the query.
     * @param parameter query parameter.
     *
     * @return feed or <code>NULL</code>.
     */
    public QueryFeed findQueryFeed(QueryType type, String parameter)
    {
        if (type == null) return null;
        
        QueryFeed feed = null;

        for (int i = 0; feed == null && i < feeds.size(); i++)
        {
            IFeed ifeed = feeds.get(i);
            if (ifeed instanceof QueryFeed)
            {
                QueryFeed qfeed = (QueryFeed)ifeed;

                QueryType qfeedType = qfeed.getQueryType();
                String qfeedParameter = qfeed.getParameter();

                if (qfeedType != null && type.getType() == qfeedType.getType() &&
                    ((parameter == null && qfeedParameter == null) ||
                     (parameter != null && parameter.equals(qfeedParameter)))) feed = qfeed;
            }
        }

        return feed;
    }

    /**
     * Returns search feed by its query.
     *
     * @param query     query.
     *
     * @return feed or <code>NULL</code>.
     */
    public SearchFeed findSearchFeed(Query query)
    {
        SearchFeed feed = null;

        for (int i = 0; feed == null && i < feeds.size(); i++)
        {
            IFeed ifeed = feeds.get(i);
            if (ifeed instanceof SearchFeed)
            {
                SearchFeed sfeed = (SearchFeed)ifeed;

                Query sfeedQuery = sfeed.getQuery();

                if ((query == null && sfeedQuery == null) ||
                    (query != null && query.equals(sfeedQuery))) feed = sfeed;
            }
        }

        return feed;
    }
}
