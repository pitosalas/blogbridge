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
// $Id: DataFeed.java,v 1.87 2008/03/17 10:53:21 spyromus Exp $
//

package com.salas.bb.domain;

import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.ReaderPreferenceReadWriteLock;
import com.salas.bb.core.FeedDisplayModeManager;
import com.salas.bb.domain.utils.ArticleDateComparator;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.parser.Channel;
import com.salas.bb.utils.parser.Item;
import com.salas.bb.utils.swinghtml.TextProcessor;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract feed acting as a base for any feed whith own (non-virtual) articles.
 */
public abstract class DataFeed extends AbstractFeed
{
    private static final Logger LOG = Logger.getLogger(DataFeed.class.getName());

    public static final String PROP_PURGE_LIMIT             = "purgeLimit";
    public static final String PROP_LAST_POLL_TIME          = "lastPollTime";
    public static final String PROP_LAST_UPDATE_SERVER_TIME = "lastUpdateServerTime";
    public static final String PROP_RETRIEVALS              = "retrievals";
    public static final String PROP_TOTAL_POLLED_ARTICLES   = "totalPolledArticles";
    public static final String PROP_INITIALIZED             = "initialized";
    public static final String PROP_FORMAT                  = "format";
    public static final String PROP_LANGUAGE                = "language";
    public static final String PROP_MARK_READ_WHEN_NO_KEYWORDS = "markReadWhenNoKeywords";
    public static final String PROP_UPDATE_PERIOD           = "updatePeriod";
    public static final String PROP_LAST_FETCH_ARTICLE_KEYS = "lastFetchArticleKeys";

    static final int DEFAULT_LAST_UPDATE_SERVER_TIME        = -1;
    static final int INIT_TIME_UNINITIALIZED                = -1;
    static final int DEFAULT_LAST_POLL_TIME                 = 0;
    static final int DEFAULT_TOTAL_POLLED_ARTICLES          = 0;
    static final int DEFAULT_RETRIEVALS                     = 0;
    static final boolean DEFAULT_MARK_READ_WHEN_NO_KEYWORDS = false;
    public static final int DEFAULT_PURGE_LIMIT             = 30;

    static final int PURGE_LIMIT_INHERITED                  = -1;
    static final int UPDATE_PERIOD_INHERITED                = -1;

    /**
     * Flag, showing if it's allowed to purge unread records or they should
     * be preserved during cleanup. TRUE to allow removal.
     */
    private static boolean globalPurgeUnread = true;

    /**
     * Global purge limit. If purge limit of feed isn't set this value is taken.
     * @see #purgeLimit
     */
    private static int globalPurgeLimit = 30;

    /**
     * Global update period. If update period of feed isn't set this value is taken.
     * @see #updatePeriod
     */
    private static long globalUpdatePeriod = Constants.MILLIS_IN_HOUR;

    /**
     * Time of initialization of the feed.
     */
    private long initTime;

    /**
     * List of all articles in the feed.
     */
    private final List<IArticle> articles;

    /**
     * List of simple match keys of all articles which should be marked as read on addition.
     * This list is a part of a trick called "marking read articles as read after import".
     */
    protected List readArticlesKeys;

    /**
     * List of simple match keys of all articles which should be marked as pinned on addition.
     * This list is a part of a trick called "marking pinned articles as pinned after import".
     */
    protected List pinnedArticlesKeys;

    /**
     * Maximum number of articles to have. This value holds the specific limit to current
     * feed. If it's equal to (-1) the value will be taken from parent guide.
     */
    private int purgeLimit;

    /**
     * The time of last successful update operation. In conjunction with
     * <code>getUpdatePeriod()</code> call it's necessary to determine if it's time
     * to update the feed.
     */
    private long lastPollTime;

    /**
     * Time of last update according to the server. The time is given in the
     * server's time-zone which allows us to query server for changes since this
     * last update time. Note, that if server has no last update time reporting
     * facility this field will always be equal to <code>-1</code> and a feed
     * will always be fetched fully for further analysis.
     */
    private long lastUpdateServerTime;

    /**
     * Total number of articles pased through the feed since initialization. In conjuntion
     * with initialization time this number is used to determine the activity of the
     * feed.
     */
    private int totalPolledArticles;

    /**
     * Total number of retrievals -- the successful update attempts.
     */
    private int retrievals;

    /**
     * Format of the feed. It's a simple string message in free format holding the
     * information about the format of the feed. In most cases it will be something, like
     * RSS 2.0 or Atom 0.3 and etc.
     */
    private String format;

    /**
     * Language in which the feed is written. Free-format string with language information.
     */
    private String language;

    /**
     * Update period of the feed in ms. It can be inherited (-1) or specified exclusively
     * for every feed.
     */
    private long updatePeriod;

    /**
     * Counter of unread articles.
     */
    private int unreadArticlesCount;
    private final ReadWriteLock unreadArticlesCountLock;

    /**
     * Listener for changes in articles.
     */
    private ArticlesListener articlesListener;

    /**
     * The match keys of all articles seen in the feed during last fetch.
     */
    private String[] lastFetchArticleKeys;

    /**
     * Creates a feed.
     */
    protected DataFeed()
    {
        articles = new ArrayList<IArticle>();

        unreadArticlesCount = 0;
        unreadArticlesCountLock = new ReaderPreferenceReadWriteLock();

        readArticlesKeys = new ArrayList();
        pinnedArticlesKeys = new ArrayList();

        articlesListener = new ArticlesListener();

        purgeLimit = PURGE_LIMIT_INHERITED;
        updatePeriod = UPDATE_PERIOD_INHERITED;

        resetFeedStatistics();

        format = null;
        language = null;
    }

    /**
     * Clears initialization and last poll times, number of retievals and total polled articles
     * count. 
     */
    protected void resetFeedStatistics()
    {
        initTime = INIT_TIME_UNINITIALIZED;
        lastPollTime = DEFAULT_LAST_POLL_TIME;
        lastUpdateServerTime = DEFAULT_LAST_UPDATE_SERVER_TIME;
        totalPolledArticles = DEFAULT_TOTAL_POLLED_ARTICLES;
        retrievals = DEFAULT_RETRIEVALS;
    }

    /**
     * Returns the Article at the specified index.
     *
     * @param index index of article in feed.
     *
     * @return article object.
     */
    public synchronized IArticle getArticleAt(int index)
    {
        return articles.get(index);
    }

    /**
     * Returns number of articles in feed.
     *
     * @return number of articles.
     */
    public synchronized int getArticlesCount()
    {
        return articles.size();
    }

    /**
     * Returns number of articles this feed owns.
     *
     * @return number of articles.
     */
    public int getOwnArticlesCount()
    {
        return getArticlesCount();
    }

    /**
     * Returns the list of all articles which are currently in the feed.
     *
     * @return all articles at this moment.
     */
    public synchronized IArticle[] getArticles()
    {
        return articles.toArray(new IArticle[articles.size()]);
    }

    /**
     * Returns the articles list to the child.
     *
     * @return articles list.
     */
    protected List<IArticle> getArticlesList()
    {
        return articles;
    }

    /**
     * Adds article to the list. Duplicates aren't added.
     *
     * @param article article to add.
     *
     * @return TRUE if added article.
     *
     * @throws NullPointerException if article is null.
     * @throws IllegalStateException if article belongs to the other feed already.
     */
    public synchronized boolean appendArticle(IArticle article)
    {
        if (article == null) throw new NullPointerException(Strings.error("unspecified.article"));

        return insertArticle(getArticlesCount(), article);
    }

    /**
     * Insert article to the list at a given position. Duplicates aren't added.
     *
     * @param index     index to add article at.
     * @param article   article to add.
     *
     * @return TRUE if added article.
     *
     * @throws NullPointerException      if article is null.
     * @throws IndexOutOfBoundsException if article index points to missing position.
     * @throws IllegalStateException     if article belongs to the other feed already.
     */
    public synchronized boolean insertArticle(int index, IArticle article)
    {
        if (article == null) throw new NullPointerException(Strings.error("unspecified.article"));
        if (article.getFeed() != null)
            throw new IllegalStateException(Strings.error("article.belongs.to.the.feed.already"));

        int count = getArticlesCount();
        if (index < 0 || index > count)
            throw new IndexOutOfBoundsException(Strings.error("index.is.out.of.bounds.of.article.list"));

        boolean added = false;
        if (article.getID() > 0 || !articles.contains(article))
        {
            articles.add(index, article);
            article.setFeed(this);
            added = true;

            // Automatically mark as read if we have the key of this article in our
            // read-list
            if (isArticleOnReadList(article)) article.setRead(true);

            // Automaticall mark as pinned if we have the key of this article in our
            // pinned-list
            if (isArticleOnPinnedList(article)) article.setPinned(true);

            // Increment counter of unread articles if the article is not read
            article.addListener(articlesListener);
            if (!article.isRead()) setUnreadArticlesCount(unreadArticlesCount + 1);

            article.setNew(true);
            try
            {
                fireArticleAdded(article);
            } finally
            {
                article.setNew(false);
            }
        }

        return added;
    }

    /**
     * Removes article from the list.
     *
     * @param article article to remove.
     *
     * @return <code>TRUE</code> if article has been removed.
     *
     * @throws NullPointerException if article is null.
     */
    public synchronized boolean removeArticle(IArticle article)
    {
        if (article == null) throw new NullPointerException(Strings.error("unspecified.article"));

        boolean removed;

        removed = articles.remove(article);
        if (removed)
        {
            article.removeListener(articlesListener);
            if (!article.isRead()) setUnreadArticlesCount(unreadArticlesCount - 1);

            fireArticleRemoved(article);
        }

        return removed;
    }

    /**
     * Returns unread articles count.
     *
     * @return count.
     */
    public int getUnreadArticlesCount()
    {
        int count = 0;
        try
        {
            unreadArticlesCountLock.readLock().acquire();
            count = unreadArticlesCount;
            unreadArticlesCountLock.readLock().release();
        } catch (InterruptedException e)
        {
            LOG.log(Level.SEVERE, Strings.error("interrupted"), e);
        }

        return count;
    }

    /**
     * Returns the number of pinned articles.
     *
     * @return number of pinned articles.
     */
    private int getPinnedArticlesCount()
    {
        int count = 0;
        for (IArticle article : articles) if (article.isPinned()) count++;
        return count;
    }

    /**
     * Sets new unread articles count.
     *
     * @param count new unread articles count.
     *
     * @throws IllegalArgumentException if count less than 0 or greater than number of articles.
     */
    private void setUnreadArticlesCount(int count)
        throws IllegalArgumentException
    {
        if (count < 0 || count > getArticlesCount())
            throw new IllegalArgumentException(MessageFormat.format(
                Strings.error("unread.articles.count.ran.out.of.allowed.range"),
                count, getArticlesCount()));

        try
        {
            unreadArticlesCountLock.writeLock().acquire();
            int oldValue = unreadArticlesCount;
            unreadArticlesCount = count;
            unreadArticlesCountLock.writeLock().release();

            firePropertyChanged(PROP_UNREAD_ARTICLES_COUNT, oldValue, count, false, true);
        } catch (InterruptedException e)
        {
            LOG.log(Level.SEVERE, Strings.error("interrupted"), e);
        }
    }

    /**
     * Returns comma-delimetered list of keys of read articles. This method returns not the same
     * value which might be set by appropriate setter. This is because this method returns current
     * situation representationa and setter only suggests what articles should be marked as read.
     *
     * @return list of keys.
     */
    public synchronized String getReadArticlesKeys()
    {
        String keys;

        if (!isInitialized())
        {
            // If the feed was received with SyncIn (on its own or as part of SyncFull)
            // and isn't polled yet, then it will have no articles and SyncOut (SyncFull)
            // will save empty list of read articles keys, which isn't right. So, we reuse
            // what we have got.
            if (readArticlesKeys != null)
            {
                keys = StringUtils.join(readArticlesKeys.iterator(), ",");
            } else
            {
                keys = Constants.EMPTY_STRING;
            }
        } else
        {
            List<String> keysList = new ArrayList<String>(getUnreadArticlesCount());
            for (int i = 0; i < getArticlesCount(); i++)
            {
                final IArticle a = getArticleAt(i);
                if (a.isRead()) keysList.add(a.getSimpleMatchKey());
            }

            keys = StringUtils.join(keysList.iterator(), ",");
        }

        return keys;
    }

    /**
     * Returns a comma-delimetered list of keys of pinned articles. This method returns not the same
     * value which might be set by appropriate setter. This is because this method returns current
     * situation representationa and setter only suggests what articles should be marked as pinned.
     *
     * @return list of keys.
     */
    public synchronized String getPinnedArticlesKeys()
    {
        String keys;

        if (!isInitialized())
        {
            // If the feed was received with SyncIn (on its own or as part of SyncFull)
            // and isn't polled yet, then it will have no articles and SyncOut (SyncFull)
            // will save empty list of pinned articles keys, which isn't right. So, we reuse
            // what we have got.
            if (pinnedArticlesKeys != null)
            {
                keys = StringUtils.join(pinnedArticlesKeys.iterator(), ",");
            } else
            {
                keys = Constants.EMPTY_STRING;
            }
        } else
        {
            List<String> keysList = new ArrayList<String>();
            for (int i = 0; i < getArticlesCount(); i++)
            {
                final IArticle a = getArticleAt(i);
                if (a.isPinned()) keysList.add(a.getSimpleMatchKey());
            }

            keys = StringUtils.join(keysList.iterator(), ",");
        }

        return keys;
    }

    /**
     * Sets comma-delimetered list of articles' keys. This list will be taken in account when new
     * articles will be added. If article has the key mentioned in the list then it will be
     * selected.
     *
     * @param keys list of keys. Null is ok.
     */
    public synchronized void setReadArticlesKeys(String keys)
    {
        readArticlesKeys.clear();

        if (keys != null)
        {
            // parse list of keys
            readArticlesKeys = parseKeysToList(keys);

            // check if we have articles to mark already
            for (int i = 0; i < getArticlesCount(); i++)
            {
                IArticle article = getArticleAt(i);
                if (isArticleOnReadList(article)) article.setRead(true);
            }
        }
    }

    /**
     * Sets a comma-delimetered list of articles' keys. This list will be taken in account when new
     * articles will be added. If article has the key mentioned in the list then it will be
     * selected.
     *
     * @param keys list of keys. Null is ok.
     */
    public synchronized void setPinnedArticlesKeys(String keys)
    {
        pinnedArticlesKeys.clear();

        if (keys != null)
        {
            // parse list of keys
            pinnedArticlesKeys = parseKeysToList(keys);

            // check if we have articles to mark already
            for (int i = 0; i < getArticlesCount(); i++)
            {
                IArticle article = getArticleAt(i);
                if (isArticleOnPinnedList(article)) article.setPinned(true);
            }
        }
    }

    /**
     * Parses the list of keys into the Java list.
     *
     * @param keys keys string.
     *
     * @return array.
     */
    private static ArrayList<String> parseKeysToList(String keys)
    {
        StringTokenizer st = new StringTokenizer(keys, ",");
        ArrayList<String> list = new ArrayList<String>(st.countTokens());
        while (st.hasMoreTokens())
        {
            String key = st.nextToken();

            // Special treatment for old 8-byte keys based on links
            // (convert to positive 4-byte equivalents)
            if (key.length() == 16)
            {
                long positive = 0x100000000L - Long.parseLong(key.substring(8), 16);
                key = Long.toHexString(positive);
            }

            list.add(key);
        }
        return list;
    }

    /**
     * Returns <code>TRUE</code> if article should be marked as pinned because of being on the
     * pinned articles list.
     *
     * @param article article to check.
     *
     * @return <code>TRUE</code> if it is.
     */
    private boolean isArticleOnPinnedList(IArticle article)
    {
        return pinnedArticlesKeys.size() > 0 && pinnedArticlesKeys.contains(article.getSimpleMatchKey());
    }

    /**
     * Returns <code>TRUE</code> if article should be marked as Read because of being on the
     * read articles list.
     *
     * @param article article to check.
     *
     * @return <code>TRUE</code> if it is.
     */
    private boolean isArticleOnReadList(IArticle article)
    {
        return readArticlesKeys.size() > 0 && readArticlesKeys.contains(article.getSimpleMatchKey());
    }

    /**
     * Returns the format of this feed.
     *
     * @return format.
     */
    public String getFormat()
    {
        return format;
    }

    /**
     * Sets the format of the feed.
     *
     * @param aFormat new format.
     */
    public void setFormat(String aFormat)
    {
        String oldFormat = format;
        format = aFormat;
        firePropertyChanged(PROP_FORMAT, oldFormat, format);
    }

    /**
     * Returns language of feed.
     *
     * @return language.
     */
    public String getLanguage()
    {
        return language;
    }

    /**
     * Sets the language of the feed.
     *
     * @param aLanguage language.
     */
    public void setLanguage(String aLanguage)
    {
        String oldLanguage = language;
        language = aLanguage;
        firePropertyChanged(PROP_LANGUAGE, oldLanguage, language);
    }

    /**
     * Returns TRUE if automatic new articles scanning is enabled.
     *
     * @return TRUE if automatic new articles scanning is enabled.
     */
    public boolean isAutoFeedsDiscovery()
    {
        return isAutoFeedsDiscoveryInParentGuides();
    }

    /**
     * Returns <code>TRUE</code> if auto-feed discovery is enabled in at least one parent guide.
     *
     * @return <code>TRUE</code> if auto-feed discovery is enabled in at least one parent guide.
     */
    private boolean isAutoFeedsDiscoveryInParentGuides()
    {
        boolean guideAutoFeedsDiscovery = false;

        // WARNING: Synchronization of parentGuides required !!!
        IGuide[] parentGuides = getParentGuides();
        for (int i = 0; !guideAutoFeedsDiscovery && i < parentGuides.length; i++)
        {
            guideAutoFeedsDiscovery = parentGuides[i].isAutoFeedsDiscovery();
        }

        return guideAutoFeedsDiscovery;
    }

    /**
     * Returns purge limit setting for this particular feed.
     *
     * @return limit setting or <code>PURGE_LIMIT_INHERITED</code>.
     *
     * @see #PURGE_LIMIT_INHERITED
     */
    public int getPurgeLimit()
    {
        return purgeLimit;
    }

    /**
     * Returns currently set purge limit. If specific purge limit isn't set the global is taken.
     *
     * @return purge limit.
     */
    public int getPurgeLimitCombined()
    {
        return purgeLimit == PURGE_LIMIT_INHERITED ? getGlobalPurgeLimit() : purgeLimit;
    }

    /**
     * Returns global value of purge limit. It will be used when the feed has no own
     * limit setting.
     *
     * @return default limit.
     */
    public static int getGlobalPurgeLimit()
    {
        return globalPurgeLimit;
    }

    /**
     * Sets new value for global purge limit. This value is used when the feed has no own
     * limit setting. Note that setting this value will not induce cleanup.
     *
     * @param limit new limit value.
     */
    public static void setGlobalPurgeLimit(int limit)
    {
        globalPurgeLimit = limit;
    }

    /**
     * Sets purge limit.
     *
     * @param aPurgeLimit new purge limit.
     */
    public void setPurgeLimit(int aPurgeLimit)
    {
        int oldLimit = getPurgeLimitCombined();
        purgeLimit = aPurgeLimit;
        int newLimit = getPurgeLimitCombined();

        firePropertyChanged(PROP_PURGE_LIMIT, oldLimit, newLimit, true, false);

        // if purge limit became lower then cleaning may be necessary
        if (oldLimit > newLimit) clean();
    }

    /**
     * Returns TRUE if the feed is in the Manual update mode.
     *
     * @return TRUE if in manual mode.
     */
    protected boolean isOnlyManual()
    {
        return getUpdatePeriod() == 0;
    }

    /**
     * Returns TRUE if this feed is updatable, meaning that it's not invalid for some reason
     * and it's proper time to call <code>update()</code> method. The behaviod may differ
     * if the update operation was called manually to this particular feed and not as a part
     * of a bigger update operation (update guide or update all).
     *
     * @param manual if <code>TRUE</code> then the update was requested manually (not through periodic check).
     * @param allowInvisible <code>TRUE</code> if invisible feed is allowed for update.
     *
     * @return <code>TRUE</code> if it's updatable.
     */
    public final boolean isUpdatable(boolean manual, boolean allowInvisible)
    {
        return getID() != -1 &&
            !isProcessing() &&
            (allowInvisible || canBenefitFromUpdate()) &&
            isUpdatable(manual);
    }

    /**
     * Returns <code>TRUE</code> if considering current feed classes set,
     * the feed can benefit for the update and it is going to change its status.
     *
     * @return <code>TRUE</code> if update could help this feed become visible.
     */
    private boolean canBenefitFromUpdate()
    {
        int m = getClassesMask() & FeedClass.MASK_UNUPDATABLE;
        return FeedDisplayModeManager.getInstance().isVisible(m);
    }

    /**
     * Returns TRUE if this feed is updatable, meaning that it's not invalid for some reason
     * and it's proper time to call <code>update()</code> method. The behaviod may differ
     * if the update operation was called manually to this particular feed and not as a part
     * of a bigger update operation (update guide or update all).
     *
     * @param manual if TRUE then the update was requested manually (not through periodic check).
     *
     * @return <code>TRUE</code> if it's updatable.
     */
    protected boolean isUpdatable(boolean manual)
    {
        return manual || (!isInvalid() && !isOnlyManual() && (getLastPollTime() + getUpdatePeriodCombined()) < System.currentTimeMillis());
    }

    /**
     * Updates the feed contents using internal algorithms specific to each feed.
     */
    public void update()
    {
        long updateTime = System.currentTimeMillis();

        // Fetch the feed data
        Channel channel = null;
        try
        {
            channel = fetchFeed();
        } catch (Exception e)
        {
            setInvalidnessReason(Strings.message("feed.invalidness.reason.bad.data"));
            LOG.log(Level.SEVERE, MessageFormat.format(Strings.error("feed.fetching.errored.internal.error"),
                toString()), e);
        }

        // Convert the list of items into articles before entering the
        // synchronized block to minimize locking
        StandardArticle[] articles = null;
        if (channel != null)
        {
            articles = new StandardArticle[channel.getItemsCount()];
            for (int i = 0; i < articles.length; i++) articles[i] = createArticle(channel.getItemAt(i));
        }

        synchronized (this)
        {
            if (channel != null)
            {
                updateFeed(channel);
                updateArticles(articles);
                clean();
            }

            setLastPollTime(updateTime);
            setInitTime(updateTime);
            setRetrievals(getRetrievals() + 1);
        }
    }

    /**
     * Removes the articles from the feed to fit into limit parameter.
     * If removal of unread articles isn't allowed then the read articles may be
     * removed even if they are in the head of the feed to match the limit setting.
     *
     * @param limit               clean limit.
     * @param overridePurgeUnread <code>TRUE</code> to override purge unread setting
     *                            and allow removing of unread articles.
     */
    public synchronized void clean(final int limit, boolean overridePurgeUnread)
    {
        // Calculate number of articles we need to remove
        boolean purgeUnread = overridePurgeUnread || isPurgeUnread();
        int total = getArticlesCount();
        int unread = getUnreadArticlesCount();
        int pinned = getPinnedArticlesCount();
        int toRemove = calcArticlesToRemove(total, unread, pinned, limit, purgeUnread);

        if (toRemove > 0)
        {
            processingStarted();

            // Choose articles that can potentially be deleted
            List<IArticle> canBeDeleted = new ArrayList<IArticle>();
            for (int i = 0; i < total; i++)
            {
                IArticle article = getArticleAt(i);
                if ((purgeUnread || article.isRead()) && !article.isPinned()) canBeDeleted.add(article);
            }

            if (canBeDeleted.size() > 0)
            {
                // Sort article by pubdate
                Collections.sort(canBeDeleted, new ArticleDateComparator());

                // Move on from the tail and remove only allowed articles
                toRemove = Math.min(toRemove, canBeDeleted.size());
                for (int i = 0; i < toRemove; i++)
                {
                    IArticle article = canBeDeleted.get(i);
                    removeArticle(article);
                }
            }

            processingFinished();
        }
    }

    /**
     * Removes the articles from the feed to fit into the current purge limit setting.
     * If removal of unread articles isn't allowed then the read articles may be
     * removed even if they are in the head of the feed to match the limit setting.
     */
    public synchronized void clean()
    {
        clean(getPurgeLimitCombined(), false);
    }

    /**
     * Calculates number of articles to remove from the tail depending on the
     * settings and counts.
     *
     * @param total         total number of articles.
     * @param unread        number of unread articles.
     * @param pinned        number of pinned articles.
     * @param limit         limit (desired number of articles to have).
     * @param purgeUnread   TRUE to allow removal of unread articles.
     *
     * @return number of articles which can be safely removed from the tail (respecting
     *         <code>purgeUnread</code> flag, of course).
     */
    static int calcArticlesToRemove(int total, int unread, int pinned, int limit, boolean purgeUnread)
    {
        // Pinned articles never count
        total = total - pinned;

        // Calculate number of articles, free to be removed
        int dynamic = purgeUnread ? total : total - unread;

        // Calculate number of articles we need to leave untouched
        int leave = Math.max(limit, total - dynamic);

        // Return number of articles we need to remove [0;inf)
        return Math.max(0, total - leave);
    }

    /**
     * Returns TRUE if purging is allowed to remove unread articles.
     *
     * @return TRUE if purging is allowed to remove unread articles.
     */
    public boolean isPurgeUnread()
    {
        return isGlobalPurgeUnread();
    }

    /**
     * Returns TRUE if purgin of unread articles is enabled globally.
     *
     * @return TRUE if purgin of unread articles is enabled globally.
     */
    public static boolean isGlobalPurgeUnread()
    {
        return globalPurgeUnread;
    }

    /**
     * Changes the state of default flag (for all feeds) showing whether it's
     * allowed to purge unread articles or no.
     *
     * @param value TRUE to allow.
     */
    public static void setGlobalPurgeUnread(boolean value)
    {
        globalPurgeUnread = value;
    }

    /**
     * Fetches the feed by some specific means.
     *
     * @return the feed or NULL if there was an error or no updates required.
     */
    protected abstract Channel fetchFeed();

    /**
     * Updates the feed properties from the channel object.
     *
     * @param channel channel object.
     */
    protected void updateFeed(Channel channel)
    {
        String channelFormat = channel.getFormat();
        if (channelFormat != null) setFormat(channelFormat);

        String channelLanguage = channel.getLanguage();
        if (channelLanguage != null) setLanguage(channelLanguage);

        long newUpdatePeriod = channel.getUpdatePeriod();
        if (getUpdatePeriod() == -1 && newUpdatePeriod != -1) setUpdatePeriod(newUpdatePeriod);
        setLastUpdateServerTime(channel.getLastUpdateServerTime());
    }

    /**
     * Updates the list of articles from the list of items taken from the channel object.
     * This implementation analyzes the incoming list of articles and carefully selects
     * the items for addition to the feed.
     *
     * @param incomingArticles articles to get updates from.
     */
    protected void updateArticles(StandardArticle[] incomingArticles)
    {
        boolean canAddMore = true;
        int added = 0;

        // In this version of feed update logic we scan through all articles
        // and see if they were seen during the previous fetch. If they were
        // then they are ignored. If they weren't we are trying to add them
        // to the local feed. In case when some article is reposted it may
        // come that the article wasn't seen during the last fetch attempt,
        // but it's still in the local feed version -- it will also be ignored.
        // After all articles are scanned through we save the list of all
        // article match keys for the future use.

        int purgeLimit = getPurgeLimitCombined();
        List<String> keys = new ArrayList<String>(incomingArticles.length);
        try
        {
            for (StandardArticle article : incomingArticles)
            {
                String matchKey = article.getSimpleMatchKey();
                keys.add(matchKey);

                if (canAddMore &&
                    !isArticleSeen(matchKey) &&
                    !isDuplicate(article) &&
                    insertArticle(0, article))
                {
                    added++;
                    if (added == purgeLimit) canAddMore = false;
                }
            }
        } finally
        {
            setTotalPolledArticles(getTotalPolledArticles() + added);
            setLastFetchArticleKeys(keys.toArray(new String[keys.size()]));
        }
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
        // This method is overriden by QueryFeed to control the duplicates
        return false;
    }

    /**
     * Returns <code>TRUE</code> if the article with such match key was seen during last fetch.
     *
     * @param aKey key.
     *
     * @return <code>TRUE</code> if the article with such match key was seen during last fetch.
     */
    private boolean isArticleSeen(String aKey)
    {
        if (lastFetchArticleKeys == null || aKey == null) return false;
        boolean res = false;

        for (int i = 0; !res && i < lastFetchArticleKeys.length; i++)
        {
            res = aKey.equals(lastFetchArticleKeys[i]);
        }

        return res;
    }

    /**
     * Sets the list of last seen article keys.
     *
     * @param aKeys keys.
     */
    public void setLastFetchArticleKeys(String[] aKeys)
    {
        String[] old = lastFetchArticleKeys;
        lastFetchArticleKeys = aKeys;

        if (!Arrays.equals(old, lastFetchArticleKeys))
        {
            firePropertyChanged(PROP_LAST_FETCH_ARTICLE_KEYS, old, lastFetchArticleKeys);
        }
    }

    /**
     * Returns the list of last seen article keys.
     *
     * @return the list.
     */
    public String[] getLastFetchArticleKeys()
    {
        return lastFetchArticleKeys;
    }

// Below is an old version
//    /**
//     * Updates the list of articles from the list of items taken from the channel object.
//     * This implementation analyzes the incoming list of articles and carefully selects
//     * the items for addition to the feed.
//     *
//     * @param channel channel object.
//     */
//    protected void updateArticles(Channel channel)
//    {
//        int insertionIndex = 0;
//        boolean canAddMore = true;
//        int added = 0;
//
//        // We take the time of update start as a base for articles without publication
//        // date set. We will create our own dates for them starting from now to the
//        // past (as we assume that the recent articles go before the old ones in the feed).
//        long baseTime = System.currentTimeMillis();
//
//        int purgeLimit = getPurgeLimitCombined();
//        int count = channel.getItemsCount();
//        for (int i = 0; canAddMore && i < count; i++)
//        {
//            Item item = channel.getItemAt(i);
//
//            StandardArticle article = createArticle(item);
//
//            // Create our own date for article if it has no own publication date
//            // NOTE: There are pieces of code which count on that they
//            //       never see NULL as publication date. Review carefully this
//            //       if you are going to remove these lines.
//            if (article.getPublicationDate() == null)
//            {
//                article.setPublicationDate(new Date(baseTime--));
//            }
//
//            boolean addedArticle = insertArticle(insertionIndex, article);
//            insertionIndex = articles.indexOf(article) + 1;
//
//            // If we have detected the old article (existing already in the list)
//            // then we may need to stop reading following articles or continue
//            // depending on the publication date of this one. If the date is in
//            // the future then most probably it was a mistake and we continue
//            // reading.
//            if (addedArticle)
//            {
//                added++;
//            } else
//            {
//                Date pubDate = article.getPublicationDate();
//                if (pubDate.after(new Date())) addedArticle = true;
//            }
//
//            if (!addedArticle || added == purgeLimit) canAddMore = false;
//        }
//
//        setTotalPolledArticles(getTotalPolledArticles() + added);
//    }

    /**
     * Creates article from the given parsed item.
     *
     * @param item item to create article from.
     *
     * @return article.
     */
    private StandardArticle createArticle(Item item)
    {
        String text = TextProcessor.filterText(item.getText());
        String title = TextProcessor.filterTitle(item.getTitle(), text);

        StandardArticle article = new LazyArticle(text);
        article.setRead(false);
        article.setTitle(title);
        article.setAuthor(item.getAuthor());
        article.setLink(item.getLink());
        article.setPublicationDate(item.getPublicationDate());
        article.setSubject(StringUtils.unescape(item.getSubject()));
        article.getPlainText(); // stimulate plain text creation
        article.computeSimpleMatchKey();

        return article;
    }

    /**
     * Returns <code>true</code> if this feed has valid XML url and was successfully initialized.
     *
     * @return <code>true</code> if initialized.
     */
    public boolean isInitialized()
    {
        return initTime != INIT_TIME_UNINITIALIZED;
    }

    /**
     * Returns time of initialization.
     *
     * @return time of initialization or (-1) if still not initialized.
     */
    public long getInitTime()
    {
        return initTime;
    }

    /**
     * Sets the time of initialization. The setting will work out only if the feed
     * isn't initialized.
     *
     * @param aInitTime time of initialization.
     */
    public void setInitTime(long aInitTime)
    {
        if (initTime == INIT_TIME_UNINITIALIZED && aInitTime != INIT_TIME_UNINITIALIZED)
        {
            initTime = aInitTime;
            firePropertyChanged(PROP_INITIALIZED, false, true, false, true);
        }
    }

    /**
     * Returns timestamp of last successful poll.
     *
     * @return timestamp or DEFAULT_LAST_POLL_TIME if not polled yet.
     *
     * @see #DEFAULT_LAST_POLL_TIME
     */
    public long getLastPollTime()
    {
        return lastPollTime;
    }

    /**
     * Sets last successful poll time.
     *
     * @param time timestamp.
     */
    public void setLastPollTime(long time)
    {
        long old = lastPollTime;
        lastPollTime = time;
        firePropertyChanged(PROP_LAST_POLL_TIME, old, time);
    }

    /**
     * Returns last feed update time (in a server time-zone).
     *
     * @return last feed update time.
     */
    public long getLastUpdateServerTime()
    {
        return lastUpdateServerTime;
    }

    /**
     * Sets last feed update time (in a server time-zone).
     *
     * @param time timestamp.
     */
    public void setLastUpdateServerTime(long time)
    {
        long old = lastUpdateServerTime;
        lastUpdateServerTime = time;
        firePropertyChanged(PROP_LAST_UPDATE_SERVER_TIME, old, time);
    }

    /**
     * Returns update period of this feed. The period may be specific to feed or taken
     * from parent guide.
     *
     * @return update period in ms.
     */
    public long getUpdatePeriodCombined()
    {
        return updatePeriod < 0 ? getGlobalUpdatePeriod() : updatePeriod;
    }

    /**
     * Returns update period of this feed.
     *
     * @return update period in ms or <code>UPDATE_PERIOD_INHERITED</code>.
     *
     * @see #UPDATE_PERIOD_INHERITED
     */
    public long getUpdatePeriod()
    {
        return updatePeriod;
    }

    /**
     * Sets the period of updates to the feed. The period can be inherited, meaning that
     * defaul global value will be taken, or specific to the feed.
     *
     * @param period period in ms.
     *
     * @see #UPDATE_PERIOD_INHERITED
     */
    public void setUpdatePeriod(long period)
    {
        long oldPeriod = getUpdatePeriod();
        updatePeriod = period;
        long newPeriod = getUpdatePeriod();

        firePropertyChanged(PROP_UPDATE_PERIOD, oldPeriod, newPeriod, true, false);
    }

    /**
     * Returns global update period.
     *
     * @return update period in ms.
     */
    public static long getGlobalUpdatePeriod()
    {
        return globalUpdatePeriod;
    }

    /**
     * Sets new global update period. The setting of update period will not induce
     * immediate update.
     *
     * @param period period in ms.
     */
    public static void setGlobalUpdatePeriod(long period)
    {
        globalUpdatePeriod = period;
    }

    /**
     * Returns number of retrievals.
     *
     * @return number of retrievals.
     */
    public int getRetrievals()
    {
        return retrievals;
    }

    /**
     * Sets number of retrievals.
     *
     * @param aRetrievals number of retrievals.
     */
    public void setRetrievals(int aRetrievals)
    {
        int old = retrievals;
        retrievals = aRetrievals;
        firePropertyChanged(PROP_RETRIEVALS, new Integer(old), new Integer(retrievals));
    }

    /**
     * Total number of articles passed through this feed since its first initialization.
     *
     * @return articles count.
     */
    public int getTotalPolledArticles()
    {
        return totalPolledArticles;
    }

    /**
     * Sets total number of articles passed through this feed.
     *
     * @param value count.
     */
    public void setTotalPolledArticles(int value)
    {
        int old = totalPolledArticles;
        totalPolledArticles = value;
        firePropertyChanged(PROP_TOTAL_POLLED_ARTICLES, new Integer(old), new Integer(value));
    }

    /**
     * Listens to changes in contained articles and updates own state.
     */
    private class ArticlesListener implements IArticleListener
    {
        /**
         * Invoked when the property of the article has been changed.
         *
         * @param article  article.
         * @param property property of the article.
         * @param oldValue old property value.
         * @param newValue new property value.
         */
        public void propertyChanged(IArticle article, String property, Object oldValue,
                                    Object newValue)
        {
            if (IArticle.PROP_READ.equals(property))
            {
                synchronized (DataFeed.this)
                {
                    boolean readNow = (Boolean)newValue;
                    setUnreadArticlesCount(unreadArticlesCount + (readNow ? -1 : 1));
                }
            }
        }
    }
}
