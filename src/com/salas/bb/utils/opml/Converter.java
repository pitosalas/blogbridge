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
// $Id: Converter.java,v 1.28 2007/04/30 13:43:27 spyromus Exp $
//

package com.salas.bb.utils.opml;

import com.salas.bb.domain.*;
import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.domain.querytypes.QueryType;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bbutilities.opml.objects.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URL;

/**
 * Converts domain objects into OPML equivalents.
 */
public final class Converter
{
    private static final Logger LOG = Logger.getLogger(Converter.class.getName());

    /**
     * Hidden utility class.
     */
    private Converter()
    {
    }

    /**
     * Converts guides set to OPML. It takes all standard guides from the set and
     * converts them one by one using {@link #convertToOPML(com.salas.bb.domain.StandardGuide[])}.
     *
     * @param set       set to convert.
     * @param aTitle    set title.
     *
     * @return set.
     *
     * @throws NullPointerException if set isn't specified.
     */
    public static OPMLGuideSet convertToOPML(GuidesSet set, String aTitle)
    {
        OPMLGuide[] guides = convertToOPML(set.getStandardGuides(null));
        return convertToOPML(guides, aTitle);
    }

    /**
     * Converts guides with a title to OPML set.
     *
     * @param aGuides   guides to convert.
     * @param aTitle    title.
     *
     * @return set.
     */
    public static OPMLGuideSet convertToOPML(OPMLGuide[] aGuides, String aTitle)
    {
        return new OPMLGuideSet(aTitle, aGuides, new Date());
    }

    /**
     * Converts guides to OPML equivalents, including all feeds. Each guide
     * conversion is deletegated to {@link #convertToOPML(com.salas.bb.domain.StandardGuide)}.
     *
     * @param aGuides   guides to convert.
     *
     * @return guides.
     *
     * @throws NullPointerException if guides aren't specified.
     *
     * @see #convertToOPML(com.salas.bb.domain.StandardGuide)
     */
    public static OPMLGuide[] convertToOPML(StandardGuide[] aGuides)
    {
        OPMLGuide[] opmlGuides = new OPMLGuide[aGuides.length];

        for (int i = 0; i < aGuides.length; i++)
        {
            StandardGuide guide = aGuides[i];
            opmlGuides[i] = convertToOPML(guide);
        }

        return opmlGuides;
    }

    /**
     * Converts guide and all its feeds into OPML equivalents. The conversion of
     * all feeds is performed with call to
     * {@link #convertToOPML(com.salas.bb.domain.IFeed)}.
     *
     * @param aGuide    guide to convert.
     *
     * @return guide with all feeds.
     *
     * @throws NullPointerException if guide isn't specified.
     *
     * @see #convertToOPML(com.salas.bb.domain.IFeed)
     */
    public static OPMLGuide convertToOPML(StandardGuide aGuide)
    {
        OPMLGuide opmlGuide = new OPMLGuide(aGuide.getTitle(), aGuide.getIconKey(),
            aGuide.isPublishingEnabled(), aGuide.getPublishingTitle(),
            aGuide.getPublishingTags(), aGuide.isPublishingPublic(),
            aGuide.getPublishingRating(), aGuide.isAutoFeedsDiscovery(),
            aGuide.isNotificationsAllowed(), aGuide.isMobile());

        // Reading lists
        ReadingList[] lists = aGuide.getReadingLists();
        for (ReadingList list : lists)
        {
            // Collect and convert feeds
            DirectFeed[] assocFeeds = list.getFeeds();
            ArrayList<DirectOPMLFeed> feeds = new ArrayList<DirectOPMLFeed>(assocFeeds.length);
            for (DirectFeed assocFeed : assocFeeds) feeds.add(convertToOPML(assocFeed));

            // Create reading list
            OPMLReadingList opmlList = new OPMLReadingList(list.getTitle(),
                list.getURL().toString());
            opmlList.setFeeds(feeds);

            opmlGuide.add(opmlList);
        }

        // Feeds
        IFeed[] guideFeeds = aGuide.getFeeds();
        ArrayList<DefaultOPMLFeed> feeds = new ArrayList<DefaultOPMLFeed>(guideFeeds.length);
        for (IFeed feed : guideFeeds)
        {
            DefaultOPMLFeed opmlFeed = null;

            if (aGuide.hasDirectLinkWith(feed))
            {
                try
                {
                    opmlFeed = convertToOPML(feed);
                } catch (Exception e)
                {
                    LOG.log(Level.SEVERE, MessageFormat.format(
                        Strings.error("failed.to.convert.feed.to.opml"), feed), e);
                }

                if (opmlFeed != null) feeds.add(opmlFeed);
            }
        }
        opmlGuide.setFeeds(feeds);

        return opmlGuide;
    }

    /**
     * Converts direct feed to OPML equivalent.
     *
     * @param aFeed feed to convert.
     *
     * @return OPML feed or NULL if feed has no XML URL and cannot be stored.
     *
     * @throws NullPointerException if feed isn't specified.
     */
    public static DirectOPMLFeed convertToOPML(DirectFeed aFeed)
    {
        DirectOPMLFeed opmlFeed = null;

        if (aFeed.getXmlURL() != null)
        {
            String title = aFeed.getBaseTitle();
            String xmlURL = aFeed.getXmlURL().toString();
            String htmlURL = aFeed.getSiteURL() == null ? null : aFeed.getSiteURL().toString();
            int rating = aFeed.getRating();
            String readArticlesKeys = aFeed.getReadArticlesKeys();
            String pinnedArticlesKeys = aFeed.getPinnedArticlesKeys();
            String customDescription = aFeed.getCustomDescription();
            String customAuthor = aFeed.getCustomAuthor();
            String customTitle = aFeed.getCustomTitle();
            int purgeLimit = aFeed.getPurgeLimit();

            String[] userTags = aFeed.getUserTags();
            String tags = userTags == null ? null : StringUtils.arrayToQuotedKeywords(userTags);
            String tagsDescription = aFeed.getTagsDescription();
            String tagsExtended = aFeed.getTagsExtended();

            int viewType = aFeed.getType().getType();
            boolean viewModeEnabled = aFeed.isCustomViewModeEnabled();
            int viewMode = aFeed.getCustomViewMode();

            int handlingType = aFeed.getHandlingType().toInteger();

            opmlFeed = new DirectOPMLFeed(title, xmlURL, htmlURL, rating,
                readArticlesKeys, pinnedArticlesKeys, purgeLimit, customTitle, customAuthor,
                customDescription, tags, tagsDescription, tagsExtended, aFeed.isDisabled(),
                viewType, viewModeEnabled, viewMode, aFeed.getAscendingSorting(), handlingType);

            fillDataFeedProperties(opmlFeed, aFeed);
        }

        return opmlFeed;
    }

    /**
     * Fills data feed properties in the OPML feed.
     *
     * @param ofeed OPML feed.
     * @param dfeed data feed.
     */
    static void fillDataFeedProperties(DataOPMLFeed ofeed, DataFeed dfeed)
    {
        long period = dfeed.getUpdatePeriod();
        if (period > 0) ofeed.setUpdatePeriod(period);
    }

    /**
     * Converts query feed to OPML equivalent.
     *
     * @param aFeed     feed to convert.
     *
     * @return OPML feed.
     *
     * @throws NullPointerException if feed isn't specified.
     */
    public static QueryOPMLFeed convertToOPML(QueryFeed aFeed)
    {
        QueryOPMLFeed opmlFeed = null;

        QueryType qt = aFeed.getQueryType();
        if (qt != null)
        {
            int queryType = qt.getType();
            String title = aFeed.getBaseTitle();
            String queryParam = aFeed.getParameter();
            URL url = aFeed.getXmlURL();
            String xmlURL = url == null ? null : url.toString();
            String readArticlesKeys = aFeed.getReadArticlesKeys();
            String pinnedArticlesKeys = aFeed.getPinnedArticlesKeys();
            int purgeLimit = aFeed.getPurgeLimit();
            int rating = aFeed.getRating();

            int viewType = aFeed.getType().getType();
            boolean viewModeEnabled = aFeed.isCustomViewModeEnabled();
            int viewMode = aFeed.getCustomViewMode();

            opmlFeed = new QueryOPMLFeed(title, queryType, queryParam, xmlURL, readArticlesKeys,
                pinnedArticlesKeys, purgeLimit, rating, viewType, viewModeEnabled, viewMode,
                aFeed.getAscendingSorting(), aFeed.getHandlingType().toInteger());

            opmlFeed.setDedupEnabled(aFeed.isDedupEnabled());
            opmlFeed.setDedupFrom(aFeed.getDedupFrom());
            opmlFeed.setDedupTo(aFeed.getDedupTo());

            fillDataFeedProperties(opmlFeed, aFeed);
        }

        return opmlFeed;
    }

    /**
     * Converts search feed to OPML equivalent.
     *
     * @param aFeed     feed to convert.
     *
     * @return OPML feed.
     *
     * @throws NullPointerException if feed isn't specified.
     */
    public static SearchOPMLFeed convertToOPML(SearchFeed aFeed)
    {
        String title = aFeed.getBaseTitle();
        Query query = aFeed.getQuery();
        int articlesLimit = aFeed.getArticlesLimit();
        int rating = aFeed.getRating();

        int viewType = aFeed.getType().getType();
        boolean viewModeEnabled = aFeed.isCustomViewModeEnabled();
        int viewMode = aFeed.getCustomViewMode();

        SearchOPMLFeed feed = new SearchOPMLFeed(title, query.serializeToString(), articlesLimit, rating,
            viewType, viewModeEnabled, viewMode, aFeed.getAscendingSorting(), aFeed.getHandlingType().toInteger());

        feed.setDedupEnabled(aFeed.isDedupEnabled());
        feed.setDedupFrom(aFeed.getDedupFrom());
        feed.setDedupTo(aFeed.getDedupTo());

        return feed;
    }

    /**
     * Converts the feeds of known types to OPML. If the feed isn't known or cannot be
     * converted NULL will be returned.
     *
     * @param aFeed             feed to convert.
     *
     * @return OPML feed or NULL.
     *
     * @throws NullPointerException if feed isn't specified.
     */
    public static DefaultOPMLFeed convertToOPML(IFeed aFeed)
    {
        DefaultOPMLFeed opmlFeed = null;

        if (aFeed instanceof DirectFeed)
        {
            DirectFeed dFeed = (DirectFeed)aFeed;
            opmlFeed = convertToOPML(dFeed);
        } else if (aFeed instanceof QueryFeed)
        {
            opmlFeed = convertToOPML((QueryFeed)aFeed);
        } else if (aFeed instanceof SearchFeed)
        {
            opmlFeed = convertToOPML((SearchFeed)aFeed);
        }

        return opmlFeed;
    }
}
