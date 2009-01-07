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
// $Id: QueryFeed.java,v 1.47 2007/07/06 14:47:57 spyromus Exp $
//

package com.salas.bb.domain;

import EDU.oswego.cs.dl.util.concurrent.Mutex;
import EDU.oswego.cs.dl.util.concurrent.Sync;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.domain.querytypes.QueryType;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.amazon.AmazonException;
import com.salas.bb.utils.amazon.AmazonGateway;
import com.salas.bb.utils.amazon.AmazonItem;
import com.salas.bb.utils.amazon.AmazonSearchIndex;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.parser.Channel;
import com.salas.bb.utils.parser.Item;
import com.salas.bb.views.feeds.IFeedDisplayConstants;

import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Query feed uses third-party services to get the list of articles. Each query feed has
 * query type desriminator assigned. Using query type object feed can convert the query
 * parameter and some other properties into the valid query URL which can be used
 * to get the data from service.
 */
public class QueryFeed extends NetworkFeed
{
    private static final Logger LOG = Logger.getLogger(QueryFeed.class.getName());

    /** Query type property. */
    public static final String PROP_QUERY_TYPE = "queryType";
    /** Parameter property. */
    public static final String PROP_PARAMETER = "parameter";
    public static final String PROP_DEDUP_ENABLED = "dedupEnabled";
    public static final String PROP_DEDUP_FROM = "dedupFrom";
    public static final String PROP_DEDUP_TO = "dedupTo";

    private final Sync  syncParameterChange;

    private String      baseTitle;
    private String      title;
    private QueryType   queryType;
    private String      parameter;
    /** TRUE when deduplication functionality is enabled. */
    private boolean     dedupEnabled;
    /** The first word index to look for the match. */
    private int         dedupFrom;
    /** The last word index to look for the match. */
    private int         dedupTo;

    /**
     * Creates query feed.
     */
    public QueryFeed()
    {
        syncParameterChange = new Mutex();

        baseTitle = null;
        queryType = null;
        parameter = null;
        renderTitle();

        super.setCustomViewModeEnabled(true);

        dedupEnabled = false;
        dedupFrom = 0;
        dedupTo = 0;
    }

    /**
     * Returns reason for being invalid. Query feeds are always invalid when their type isn't
     * specified.
     *
     * @return reason.
     */
    public String getInvalidnessReason()
    {
        String reason = null;

        if (queryType == null) reason = Strings.message("feed.invalidness.reason.unsupported.query");
        if (reason == null) reason = super.getInvalidnessReason();

        return reason;
    }

    /**
     * Returns TRUE if this feed is updatable, meaning that it's not invalid for some reason and
     * it's proper time to call <code>update()</code> method. The behaviod may differ if the update
     * operation was called directly to this particular feed and not as a part of a bigger update
     * operation (update guide or update all).
     *
     * @param direct if TRUE then the update was requested directly (not through guide/set or by
     *               periodic check).
     */
    protected boolean isUpdatable(boolean direct)
    {
        return queryType != null && StringUtils.isNotEmpty(parameter) && super.isUpdatable(direct);
    }

    /**
     * Returns title of feed.
     *
     * @return title.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Renders new title on type or title change. For now, all we do is to append "SmartFeed:" to the name.
     */
    private void renderTitle()
    {
        title = getBaseTitle();
    }

    /**
     * Returns title of feed.
     *
     * @return title.
     */
    public String getBaseTitle()
    {
        return baseTitle;
    }

    /**
     * Sets the title of the feed.
     *
     * @param aTitle title of the feed.
     */
    public void setBaseTitle(String aTitle)
    {
        String oldTitle = getTitle();
        baseTitle = aTitle;

        renderTitle();

        firePropertyChanged(PROP_TITLE, oldTitle, getTitle());
    }

    /**
     * Returns query type.
     *
     * @return query type.
     */
    public QueryType getQueryType()
    {
        return queryType;
    }

    /**
     * Sets new query type.
     *
     * @param aQueryType query type.
     */
    public void setQueryType(QueryType aQueryType)
    {
        int viewMode = aQueryType == null
            ? IFeedDisplayConstants.MODE_BRIEF : aQueryType.getPreferredViewMode();
        setCustomViewMode(viewMode);

        String oldTitle = getTitle();
        QueryType old = queryType;
        queryType = aQueryType;

        renderTitle();

        firePropertyChanged(PROP_QUERY_TYPE, old, queryType, true, false);
        firePropertyChanged(PROP_TITLE, oldTitle, getTitle());

        if (queryType != null) setType(queryType.getFeedType());
    }

    /**
     * Returns the query parameter.
     *
     * @return parameter.
     */
    public String getParameter()
    {
        return parameter;
    }

    /**
     * Sets the query parameter.
     *
     * @param aParameter parameter.
     */
    public void setParameter(String aParameter)
    {
        String old = parameter;
        parameter = aParameter;

        firePropertyChanged(PROP_PARAMETER, old, parameter, true, false);
    }

    /**
     * Sets the options at once and updates the feed if necessary.
     *
     * @param dedupEnabled  deduplication enabled flag.
     * @param dedupFrom     the first dedup word.
     * @param dedupTo       the last dedup word.
     *
     * @return <code>TRUE</code> if changed and updated.
     */
    public boolean setDedupProperties(boolean dedupEnabled, int dedupFrom, int dedupTo)
    {
        boolean oldEnabled = isDedupEnabled();
        int oldFrom = getDedupFrom();
        int oldTo = getDedupTo();

        setDedupEnabled(dedupEnabled);
        setDedupFrom(dedupFrom);
        setDedupTo(dedupTo);

        return (oldEnabled != isDedupEnabled() ||
            oldFrom != getDedupFrom() ||
            oldTo != getDedupTo());
    }

    /**
     * Changes query parameter to something different and resets the feed statistics,
     * which will force it to be updated.
     *
     * @param aNewParameter new parameter.
     *
     * @return TRUE if parameter has been changed.
     */
    public boolean changeParameter(String aNewParameter)
    {
        boolean changed = false;
        try
        {
            syncParameterChange.acquire();
            try
            {
                if (parameter == null || !parameter.equals(aNewParameter))
                {
                    setParameter(aNewParameter);
                    clear(false);
                    resetFeedStatistics();

                    changed = true;
                }
            } finally
            {
                syncParameterChange.release();
            }
        } catch (InterruptedException e)
        {
            LOG.log(Level.SEVERE, Strings.error("interrupted"), e);
        }

        return changed;
    }

    /**
     * Reviews articles with dup-checking.
     */
    public void reviewArticles()
    {
        clear(true);
    }

    /**
     * Removes all articles.
     * 
     * @param checkDup  <code>TRUE</code> to clear with dup-checking.
     */
    private void clear(boolean checkDup)
    {
        if (checkDup && !isDedupEnabled()) return;

        int count = getArticlesCount();
        for (int i = 0; i < count; i++)
        {
            IArticle article = getArticleAt(count - i - 1);
            if (!article.isPinned() && (!checkDup ||
                isDuplicate(article, getDedupFrom(), getDedupTo(), getArticlesList())))
            {
                removeArticle(article);
            }
        }
    }

    /**
     * Gets XML URL.
     *
     * @return URL.
     */
    public URL getXmlURL()
    {
        int limit = getPurgeLimitCombined();
        return queryType == null ? null : queryType.convertToURL(parameter, limit);
    }

    /**
     * Fetches the feed by some specific means.
     *
     * @return the feed or NULL if there was an error or no updates required.
     */
    protected Channel fetchFeed()
    {
        Channel result;

        if (queryType == QueryType.getQueryType(QueryType.TYPE_AMAZON_BOOKS))
        {
            result = fetchAmazonFeed();
        } else result = super.fetchFeed();

        return result;
    }

    /**
     * Fetches amazon feed. Special handling.
     *
     * @return channel.
     */
    private Channel fetchAmazonFeed()
    {
        Channel channel = null;

        String subscriptionId = ResourceUtils.getString("amazon.subscription");
        String partnerId = ResourceUtils.getString("amazon.partner");

        AmazonGateway gateway = new AmazonGateway(subscriptionId, partnerId);
        try
        {
            List<AmazonItem> items = gateway.itemsSearch(getParameter(), AmazonSearchIndex.Books, "daterank",
                getPurgeLimitCombined());

            channel = itemsToChannel(items.toArray(new AmazonItem[items.size()]));
        } catch (AmazonException e)
        {
            LOG.log(Level.SEVERE, Strings.error("feed.failed.to.question.amazon.com"), e);
        }

        return channel;
    }

    /**
     * Converts amazon items list into standard items for display and adds them to
     * channel.
     *
     * @param aAmazonItems items.
     *
     * @return channel.
     */
    private Channel itemsToChannel(AmazonItem[] aAmazonItems)
    {
        Channel channel = new Channel();
        channel.setAuthor("Amazon.com");
        channel.setDescription(MessageFormat.format(Strings.message("feed.queryfeed.querying.for.0"), getParameter()));
        channel.setFormat("XML");
        channel.setLanguage("en_US");
        channel.setUpdatePeriod(Constants.MILLIS_IN_DAY);

        for (AmazonItem amazonItem : aAmazonItems)
        {
            Item item = amazonItemToChannelItem(amazonItem);
            if (item != null) channel.addItem(item);
        }

        return channel;
    }

    /**
     * Converts single amazon item into channel item.
     *
     * @param aAmazonItem amanzon item.
     *
     * @return channel item or <code>NULL</code> if should not be added.
     */
    private Item amazonItemToChannelItem(AmazonItem aAmazonItem)
    {
        String title = aAmazonItem.getAttributeValue("Title");

        if (title == null) return null;

        Item item = new Item(aAmazonItem.toHTML());
        item.setLink(aAmazonItem.getURL());
        item.setTitle(title);

        return item;
    }

    /**
     * Returns simple match key, which can be used to detect similarity of feeds. For example, it's
     * XML URL for the direct feeds, query type + parameter for the query feeds, serialized search
     * criteria for the search feeds.
     *
     * @return match key.
     */
    public String getMatchKey()
    {
        return "QF" + (queryType == null ? "" : queryType.getType()) + " " + parameter;
    }

    /**
     * Returns <code>TRUE</code> if an article is duplicate of some other already registered.
     *
     * @param article article.
     *
     * @return <code>TRUE</code> if an article is duplicate of some other already registered.
     */
    protected boolean isDuplicate(IArticle article)
    {
        return dedupEnabled && isDuplicate(article, dedupFrom, dedupTo, Arrays.asList(getArticles()));
    }

    // ------------------------------------------------------------------------
    // Duplicates Checking
    // ------------------------------------------------------------------------

    /**
     * Returns <code>TRUE</code> if remove duplicates is enabled.
     *
     * @return <code>TRUE</code> if remove duplicates is enabled.
     */
    public boolean isDedupEnabled()
    {
        return dedupEnabled;
    }

    /**
     * Sets remove duplicates flag.
     *
     * @param flag <code>TRUE</code> if remove duplicates is enabled.
     */
    public void setDedupEnabled(boolean flag)
    {
        boolean old = dedupEnabled;
        if (old == flag) return;
        dedupEnabled = flag;

        firePropertyChanged(PROP_DEDUP_ENABLED, old, flag, true, false);
    }

    /**
     * Returns the first word to look for duplicates.
     *
     * @return word number.
     */
    public int getDedupFrom()
    {
        return dedupFrom;
    }

    /**
     * Sets the first word to look for duplicates.
     *
     * @param word number
     */
    public void setDedupFrom(int word)
    {
        int old = dedupFrom;
        dedupFrom = word;

        firePropertyChanged(PROP_DEDUP_FROM, old, word, true, false);
    }

    /**
     * Returns the last word to look for duplicates.
     *
     * @return word number.
     */
    public int getDedupTo()
    {
        return dedupTo;
    }

    /**
     * Sets the last word to look for duplicates.
     *
     * @param word number
     */
    public void setDedupTo(int word)
    {
        int old = dedupTo;
        dedupTo = word;

        firePropertyChanged(PROP_DEDUP_TO, old, word, true, false);
    }
}
