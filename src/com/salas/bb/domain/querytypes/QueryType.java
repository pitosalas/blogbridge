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
// $Id: QueryType.java,v 1.10 2007/07/06 14:47:56 spyromus Exp $
//

package com.salas.bb.domain.querytypes;

import com.salas.bb.domain.FeedType;
import com.salas.bb.domain.QueryFeed;
import com.salas.bb.utils.ResourceID;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.parser.Channel;
import com.salas.bb.views.feeds.IFeedDisplayConstants;

import javax.swing.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Type of the query feed. Each type has its own rule for conversion of the list of
 * parameters into the valid query URL. The query type represents some query to some service.
 * Types have the names and associateed icons.
 */
public abstract class QueryType
{
    /** Flickr query. */
    public static final int TYPE_FLICKR         = 1;
    /** Technorati query. */
    public static final int TYPE_TECHNORATI     = 2;
    /** Delicious query. */
    public static final int TYPE_DELICIOUS      = 7;
    /** Connotea query. */
    public static final int TYPE_CONNOTEA       = 8;
    /** Amazon books search. */
    public static final int TYPE_AMAZON_BOOKS   = 9;
    /** Google blogs search. */
    public static final int TYPE_GOOGLE_BLOGSEARCH = 10;
    /** Digg search. */
    public static final int TYPE_DIGG           = 11;
    /** Monster job search. */
    public static final int TYPE_MONSTER        = 12;
    /** Google news search. */
    public static final int TYPE_GOOGLE_NEWS    = 13;
    /** Search Amazon tags. */
    public static final int TYPE_AMAZON_TAGS    = 14;
    /** Twitter search. */
    public static final int TYPE_TWITTER        = 15;

    /** The number of reserved ID's. */
    public static final int RESERVED_IDS        = 1000;

    /** List of all available types. */
    private static final Map<Integer, QueryType> TYPES;
    private final int type;
    private final FeedType feedType;

    static
    {
        QueryType technorati = new DefaultQueryType(TYPE_TECHNORATI, FeedType.TEXT,
            Strings.message("queryfeed.type.technorati.name"),
            ResourceID.ICON_QUERYFEED_TECHNORATI,
            "http://feeds.technorati.com/feed/posts/tag/{0}",
            Strings.message("queryfeed.type.technorati.parameter"),
            Strings.message("queryfeed.type.technorati.description"),
            IFeedDisplayConstants.MODE_BRIEF);

        QueryType flickr = new DefaultQueryType(TYPE_FLICKR, FeedType.IMAGE,
            Strings.message("queryfeed.type.flickr.name"),
            ResourceID.ICON_QUERYFEED_FLICKR,
            "http://www.flickr.com/services/feeds/photos_public.gne?format=rss_200&tags={0}",
            Strings.message("queryfeed.type.flickr.parameter"),
            Strings.message("queryfeed.type.flickr.description"),
            IFeedDisplayConstants.MODE_BRIEF);

        QueryType delicious = new DeliciousQueryType();

        QueryType connotea = new DefaultQueryType(TYPE_CONNOTEA, FeedType.TEXT,
            Strings.message("queryfeed.type.connotea.name"),
            ResourceID.ICON_QUERYFEED_CONNOTEA,
            "http://www.connotea.org/rss/tag/{0}",
            Strings.message("queryfeed.type.connotea.parameter"),
            Strings.message("queryfeed.type.connotea.description"),
            IFeedDisplayConstants.MODE_MINIMAL);

        QueryType amazonBooks = new DefaultQueryType(TYPE_AMAZON_BOOKS, FeedType.TEXT,
            Strings.message("queryfeed.type.amazon.name"),
            ResourceID.ICON_QUERYFEED_AMAZON,
            "http://www.amazon.com/",
            Strings.message("queryfeed.type.amazon.parameter"),
            Strings.message("queryfeed.type.amazon.description"),
            IFeedDisplayConstants.MODE_FULL);

        QueryType amazonTags = new AmazonQueryType();

        QueryType googleBlogsearch = new DefaultQueryType(TYPE_GOOGLE_BLOGSEARCH, FeedType.TEXT,
            Strings.message("queryfeed.type.googleblogsearch.name"),
            ResourceID.ICON_QUERYFEED_GOOGLE,
            "http://blogsearch.google.com/blogsearch_feeds?hl=en" +
                "&q={0}&btnG=Search+Blogs&num={1}&output=atom",
            Strings.message("queryfeed.type.googleblogsearch.parameter"),
            Strings.message("queryfeed.type.googleblogsearch.description"),
            IFeedDisplayConstants.MODE_BRIEF);

        QueryType diggSearch = new DiggQueryType();

        QueryType monster = new DefaultQueryType(TYPE_MONSTER, FeedType.TEXT,
            Strings.message("queryfeed.type.monster.name"),
            ResourceID.ICON_QUERYFEED_MONSTER,
            "http://rss.jobsearch.monster.com/rssquery.ashx?q={0}&cy=us&WT.mc_n=RSS2005_JSR",
            Strings.message("queryfeed.type.monster.parameter"),
            Strings.message("queryfeed.type.monster.description"),
            IFeedDisplayConstants.MODE_FULL);

        QueryType googleNews = new DefaultQueryType(TYPE_GOOGLE_NEWS, FeedType.TEXT,
            Strings.message("queryfeed.type.googlenews.name"),
            ResourceID.ICON_QUERYFEED_GOOGLE,
            "http://news.google.com/news?hl=en&ned=us&q={0}&ie=UTF-8&output=atom&num={1}",
            Strings.message("queryfeed.type.googlenews.parameter"),
            Strings.message("queryfeed.type.googlenews.description"),
            IFeedDisplayConstants.MODE_FULL);

        QueryType twitter = new TwitterQueryType();
        
        TYPES = new HashMap<Integer, QueryType>();
        registerType(technorati);
        registerType(flickr);
        registerType(delicious);
        registerType(connotea);
        registerType(amazonBooks);
        registerType(amazonTags);
        registerType(googleBlogsearch);
        registerType(googleNews);
        registerType(diggSearch);
        registerType(monster);
        registerType(twitter);
    }

    /**
     * Registers new query type.
     *
     * @param queryType type.
     */
    public static void registerType(QueryType queryType)
    {
        if (TYPES.containsKey(queryType.getType()))
        {
            throw new IllegalArgumentException("SmartFeed with this ID is already registered: " +
                queryType.getType());
        }

        TYPES.put(queryType.getType(), queryType);
    }

    /**
     * Creates query type.
     *
     * @param aType type descriptor.
     * @param aFeedType type of the feed.
     */
    QueryType(int aType, FeedType aFeedType)
    {
        type = aType;
        feedType = aFeedType;
    }

    /**
     * Returns type descriptor.
     *
     * @return type descriptor.
     */
    public int getType()
    {
        return type;
    }

    /**
     * Fetches the feed.
     *
     * @param queryFeed feed doing the fetch.
     *
     * @return channel.
     */
    public Channel fetchFeed(QueryFeed queryFeed)
        throws IOException
    {
        return null;
    }

    /**
     * Converts the parameter into valid query URL.
     *
     * @param param parameter.
     * @param limit max number of articles to get (not all services accept it).
     *
     * @return query URL.
     *
     * @throws NullPointerException if parameters are NULL.
     * @throws IllegalArgumentException if parameter is empty or limit is not positive.
     */
    public URL convertToURL(String param, int limit)
    {
        if (StringUtils.isEmpty(param)) throw new NullPointerException(Strings.error("unspecified.parameter"));
        if (limit <= 0) throw new IllegalArgumentException(Strings.error("limit.should.be.postive"));

        URL url = null;
        try
        {
            String spec = formURLString(param, limit);
            if (spec != null) url = new URL(spec);
        } catch (MalformedURLException e)
        {
            throw new RuntimeException(Strings.error("failed.to.create.url"), e);
        }

        return url;
    }

    /**
     * Forms the URL string from the parameter and limit. Both arguments are checked by
     * {@link #convertToURL(String, int)} method.
     *
     * @param param parameter.
     * @param limit maximum articles limit.
     *
     * @return url string.
     */
    protected abstract String formURLString(String param, int limit);

    /**
     * Returns type name.
     *
     * @return type name.
     */
    public abstract String getName();

    /**
     * Returns associated icon.
     *
     * @return icon key or NULL if no icon associated.
     */
    public abstract String getIconKey();

    /**
     * Returns an icon.
     *
     * @return icon.
     */
    public ImageIcon getIcon()
    {
        return null;
    }

    /**
     * Returns the name of parameter. It can be "Keywords", "Tag", "Blog name" and so on.
     *
     * @return name of parameters
     */
    public abstract String getParamterName();

    /**
     * Returns the parameter default.
     *
     * @return default value.
     */
    public abstract String getParameterDefault();

    /**
     * Returns the description of this query type which will be displayed to the user.
     *
     * @return description of this query type.
     */
    public abstract String getQueryDescription();

    /**
     * Validates the entry and returns the error message or NULL in case of success.
     *
     * @param param parameters.
     * @param limit max articles.
     * @param removeDuplicates <code>TRUE</code> to remove duplicate articles.
     * @param maxDupWords number of first duplicate words to use as a filter.
     * 
     * @return error message or NULL.
     */
    public abstract String validateEntry(String param, int limit, boolean removeDuplicates, int maxDupWords);

    /**
     * Returns query type object for a given type desciriminator.
     *
     * @param type type desciriminator
     *
     * @return query type or NULL if not supported.
     *
     * @see #TYPE_FLICKR
     * @see #TYPE_TECHNORATI
     * @see #TYPE_DELICIOUS
     */
    public static QueryType getQueryType(int type)
    {
        return TYPES.get(type);
    }

    /**
     * Returns string representation of the type.
     *
     * @return string representation.
     */
    public String toString()
    {
        return getName();
    }

    /**
     * Returns list of available types.
     *
     * @return available types.
     */
    public static QueryType[] getAvailableTypes()
    {
        Collection<QueryType> queryTypesCol = TYPES.values();

        return queryTypesCol.toArray(new QueryType[queryTypesCol.size()]);
    }

    /**
     * Returns type of the feed.
     *
     * @return feed type.
     */
    public FeedType getFeedType()
    {
        return feedType;
    }

    /**
     * Returns preferred view mode.
     *
     * @return view mode.
     */
    public int getPreferredViewMode()
    {
        return IFeedDisplayConstants.MODE_BRIEF;
    }

    /**
     * Creates and returns the panel with the controls to edit the properties.
     *
     * @param labelColWidth the width in 'dlu' of the label column.
     *
     * @return panel.
     */
    public abstract QueryEditorPanel getEditorPanel(int labelColWidth);
}
