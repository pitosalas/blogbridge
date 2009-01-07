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
// $Id: Helper.java,v 1.26 2007/05/09 14:17:57 spyromus Exp $
//

package com.salas.bb.utils.opml;

import com.salas.bb.domain.*;
import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.domain.querytypes.QueryType;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bbutilities.opml.objects.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * OPML helper.
 */
public final class Helper
{
    private static final Logger LOG = Logger.getLogger(Helper.class.getName());

    /** Hidden utility class constructor. */
    private Helper()
    {
    }

    /**
     * Populates the properties of direct feed from the corresponding properties of
     * an OPML feed.
     *
     * @param baseURL   base URL for relative links resolution.
     * @param aFeed     direct feed to populate properties of.
     * @param opmlFeed  OPML feed to get properties from.
     */
    public static void populateDirectFeedProperties(URL baseURL, DirectFeed aFeed,
                                                    DirectOPMLFeed opmlFeed)
    {
        aFeed.setBaseTitle(opmlFeed.getTitle());
        if (opmlFeed.getHtmlURL() != null)
        {
            URL siteURL;
            try
            {
                siteURL = new URL(baseURL, opmlFeed.getHtmlURL());
                aFeed.setSiteURL(siteURL);
            } catch (MalformedURLException e)
            {
                LOG.log(Level.WARNING, MessageFormat.format(
                    Strings.error("invalid.url"),
                    opmlFeed.getHtmlURL()), e);
            }
        }

        populateDefaultFeedProperties(aFeed, opmlFeed);

        // Set custom fields
        aFeed.setCustomTitle(opmlFeed.getCustomTitle());
        aFeed.setCustomAuthor(opmlFeed.getCustomCreator());
        aFeed.setCustomDescription(opmlFeed.getCustomDescription());

        aFeed.setUserTags(StringUtils.keywordsToArray(opmlFeed.getTags()));
        aFeed.setTagsDescription(opmlFeed.getTagsDescription());
        aFeed.setTagsExtended(opmlFeed.getTagsExtended());

        aFeed.setDisabled(opmlFeed.isDisabled());
    }

    /**
     * Populates data feed properties.
     *
     * @param dfeed data feed.
     * @param ofeed OPML data feed.
     */
    private static void populateDataFeedProperties(DataFeed dfeed, DataOPMLFeed ofeed)
    {
        Long period = ofeed.getUpdatePeriod();
        if (period != null && period > 0) dfeed.setUpdatePeriod(period);
    }

    /**
     * Populates properties common to all data feeds.
     *
     * @param feed      feed.
     * @param opmlFeed  OPML feed.
     */
    private static void populateDefaultFeedProperties(DataFeed feed, DefaultOPMLFeed opmlFeed)
    {
        feed.setReadArticlesKeys(opmlFeed.getReadArticlesKeys());
        feed.setPinnedArticlesKeys(opmlFeed.getPinnedArticlesKeys());
        feed.setPurgeLimit(opmlFeed.getLimit());
        populateAbstractFeedProperties(feed, opmlFeed);
    }

    /**
     * Populates properties common to all abstract feeds.
     *
     * @param feed      feed.
     * @param opmlFeed  OPML feed.
     */
    private static void populateAbstractFeedProperties(AbstractFeed feed, DefaultOPMLFeed opmlFeed)
    {
        feed.setRating(opmlFeed.getRating());

        int viewType = opmlFeed.getViewType();
        if (viewType != -1) feed.setType(FeedType.toObject(viewType));

        feed.setCustomViewModeEnabled(opmlFeed.isViewModeEnabled());
        int viewMode = opmlFeed.getViewMode();
        if (viewMode != -1) feed.setCustomViewMode(viewMode);

        feed.setAscendingSorting(opmlFeed.getAscendingSorting());
    }

    /**
     * Creates a guides set from the list of OPML guide outlines.
     *
     * @param baseURL       base URL for relative links resolution.
     * @param opmlGuideSet  guides set.
     *
     * @return guides set.
     */
    public static GuidesSet createGuidesSet(URL baseURL, OPMLGuideSet opmlGuideSet)
    {
        OPMLGuide[] aGuides = opmlGuideSet.getGuides();
        GuidesSet set = new GuidesSet();

        for (OPMLGuide opmlGuide : aGuides)
        {
            set.add(createGuide(baseURL, opmlGuide, opmlGuideSet.getDateModified()));
        }

        return set;
    }

    /**
     * Creates guide from the OPML outline.
     *
     * @param baseURL                   base URL for relative links resolution.
     * @param aOPMLGuide                guide outline.
     * @param serviceModificationTime   time of service information modification.
     *
     * @return guide.
     */
    public static IGuide createGuide(URL baseURL, OPMLGuide aOPMLGuide,
                                     Date serviceModificationTime)
    {
        long serviceTime = serviceModificationTime == null ? -1 : serviceModificationTime.getTime();
        StandardGuide guide = new StandardGuide();

        guide.setTitle(aOPMLGuide.getTitle());
        guide.setIconKey(aOPMLGuide.getIcon());
        guide.setPublishingEnabled(aOPMLGuide.isPublishingEnabled());
        guide.setPublishingTitle(aOPMLGuide.getPublishingTitle());
        guide.setPublishingTags(aOPMLGuide.getPublishingTags());
        guide.setPublishingPublic(aOPMLGuide.isPublishingPublic());
        guide.setPublishingRating(aOPMLGuide.getPublishingRating());

        guide.setAutoFeedsDiscovery(aOPMLGuide.isAutoFeedsDiscovery());

        guide.setNotificationsAllowed(aOPMLGuide.isNotificationsAllowed());

        OPMLReadingList[] lists = aOPMLGuide.getReadingLists();
        for (OPMLReadingList list : lists)
        {
            try
            {
                URL url = new URL(baseURL, list.getURL());
                ReadingList rlist = new ReadingList(url);
                rlist.setTitle(list.getTitle());
                guide.add(rlist);

                List feeds = list.getFeeds();
                for (Object feed1 : feeds)
                {
                    DirectOPMLFeed opmlFeed = (DirectOPMLFeed)feed1;
                    DirectFeed feed = createDirectFeed(baseURL, opmlFeed);

                    if (feed != null)
                    {
                        feed.setLastUpdateTime(serviceTime);
                        rlist.add(feed);
                    }
                }
            } catch (MalformedURLException e)
            {
                LOG.log(Level.SEVERE, MessageFormat.format(
                    Strings.error("invalid.url"),
                    list.getURL()), e);
            }
        }

        List feeds = aOPMLGuide.getFeeds();
        for (Object ofeed : feeds)
        {
            IFeed feed;
            if (ofeed instanceof DirectOPMLFeed)
            {
                feed = createDirectFeed(baseURL, (DirectOPMLFeed)ofeed);
            } else if (ofeed instanceof QueryOPMLFeed)
            {
                feed = createQueryFeed((QueryOPMLFeed)ofeed);
            } else
            {
                feed = createSearchFeed((SearchOPMLFeed)ofeed);
            }

            if (feed != null)
            {
                feed.setLastUpdateTime(serviceTime);
                guide.add(feed);
            }
        }

        guide.setLastUpdateTime(serviceTime);

        return guide;
    }

    /**
     * Creates direct feed from the OPML outline.
     *
     * @param baseURL       base URL for relative links resolution.
     * @param ofeed     direct feed from the OPML outline.
     *
     * @return direct feed.
     */
    public static DirectFeed createDirectFeed(URL baseURL, DirectOPMLFeed ofeed)
    {
        DirectFeed dfeed;

        try
        {
            dfeed = new DirectFeed();
            dfeed.setXmlURL(new URL(baseURL, ofeed.getXmlURL()));
            populateDirectFeedProperties(baseURL, dfeed, ofeed);
            populateDataFeedProperties(dfeed, ofeed);
        } catch (MalformedURLException e)
        {
            dfeed = null;
        }

        return dfeed;
    }

    /**
     * Creates query feed from the OPML outline.
     *
     * @param ofeed query feed OPML outline.
     *
     * @return query feed.
     */
    public static QueryFeed createQueryFeed(QueryOPMLFeed ofeed)
    {
        QueryFeed feed = new QueryFeed();

        populateDefaultFeedProperties(feed, ofeed);
        populateDataFeedProperties(feed, ofeed);

        feed.setBaseTitle(ofeed.getTitle());

        feed.setParameter(ofeed.getQueryParam());
        feed.setQueryType(QueryType.getQueryType(ofeed.getQueryType()));
        feed.setDedupEnabled(ofeed.isDedupEnabled());
        feed.setDedupFrom(ofeed.getDedupFrom());
        feed.setDedupTo(ofeed.getDedupTo());

        return feed;
    }

    /**
     * Creates search feed from the search feed OPML outline.
     *
     * @param aOPMLFeed search feed outline.
     *
     * @return search feed.
     */
    public static SearchFeed createSearchFeed(SearchOPMLFeed aOPMLFeed)
    {
        SearchFeed feed = new SearchFeed();

        populateAbstractFeedProperties(feed, aOPMLFeed);

        feed.setBaseTitle(aOPMLFeed.getTitle());
        feed.setArticlesLimit(aOPMLFeed.getLimit());

        feed.setDedupProperties(aOPMLFeed.isDedupEnabled(), aOPMLFeed.getDedupFrom(), aOPMLFeed.getDedupTo(), false);
        feed.setQuery(Query.deserializeFromString(aOPMLFeed.getQuery()));

        return feed;
    }
}
