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
// $Id: AbstractFeed.java,v 1.43 2007/11/07 17:16:48 spyromus Exp $
//

package com.salas.bb.domain;

import com.salas.bb.utils.CommonUtils;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.views.feeds.IFeedDisplayConstants;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract implementation of <code>IFeed</code> interface.
 */
public abstract class AbstractFeed implements IFeed
{
    private static final Logger LOG = Logger.getLogger(AbstractFeed.class.getName());

    /** ID of the corresponding record in the database. Default is -1. */
    private long id;

    /** List of event listeners. */
    private final List<IFeedListener> listeners;

    /** Incremented when someone doing something lengthy. */
    private AtomicInteger processingCount;

    /** Holders of this feed. */
    private IGuide[] guides;
    private final Object guidesLock = new Object();

    /** Reason of this feed for being invalid. */
    private String invalidnessReason;

    /** User-set rating of the feed. */
    protected int rating;

    /** Time when feed was visited for last time */
    private long lastVisitTime = 0;

    /** Type of the feed. */
    private FeedType type;

    /** Feed handling type. */
    private FeedHandlingType handlingType;

    /** When <code>TRUE</code> the feed has custom view mode assigned. */
    private boolean customViewModeEnabled;
    private int customViewMode;

    /**
     * Time time indicates the last time when the last of the properties involved in
     * synchronization routines was updated.
     */
    private long lastUpdateTime;

    /** Number of views of this feed. */
    private int views;
    /** Number of times articles of this feed were opened in the browser. */
    private int clickthroughs;
    /** Sorting override. */
    private Boolean ascendingSorting;

    private boolean autoSaveArticles;
    private String autoSaveArticlesFolder;
    private String autoSaveArticlesNameFormat;

    private boolean autoSaveEnclosures;
    private String autoSaveEnclosuresFolder;
    private String autoSaveEnclosuresNameFormat;

    /**
     * Constructs new feed.
     */
    public AbstractFeed()
    {
        guides = new IGuide[0];

        processingCount = new AtomicInteger(0);

        listeners = new CopyOnWriteArrayList<IFeedListener>();

        invalidnessReason = null;

        id = -1;
        rating = RATING_NOT_SET;
        type = FeedType.TEXT;
        handlingType = FeedHandlingType.DEFAULT;
        customViewModeEnabled = false;
        customViewMode = IFeedDisplayConstants.MODE_BRIEF;

        lastUpdateTime = -1;

        views = 0;
        clickthroughs = 0;

        ascendingSorting = null;
        autoSaveArticles = false;
        autoSaveEnclosures = false;
    }

    /**
     * Returns ID of the feed. This ID is used by persistence layer to identify record in database.
     *
     * @return ID of the feed.
     */
    public long getID()
    {
        return id;
    }

    /**
     * Sets the ID of the feed.
     *
     * @param aId ID of the feed.
     */
    public void setID(long aId)
    {
        id = aId;
    }

    /**
     * Adds a guide currently holding this feed.
     *
     * @param aGuide parent guide.
     */
    public void addParentGuide(IGuide aGuide)
    {
        synchronized (guidesLock)
        {
            if (indexOfParentGuide(aGuide) == -1)
            {
                IGuide[] newList = new IGuide[guides.length + 1];
                for (int i = 0; i < guides.length; i++) newList[i] = guides[i];
                newList[guides.length] = aGuide;

                guides = newList;
            }
        }
    }

    /**
     * Removes a guide that no longer holds this feed.
     *
     * @param aGuide guide.
     */
    public void removeParentGuide(IGuide aGuide)
    {
        synchronized (guidesLock)
        {
            int index = indexOfParentGuide(aGuide);
            if (index != -1)
            {
                IGuide[] newList = new IGuide[guides.length - 1];
                for (int i = 0; i < index; i++) newList[i] = guides[i];
                for (int i = index + 1; i < guides.length; i++) newList[i - 1] = guides[i];
                guides = newList;
            }
        }
    }

    /**
     * Returns index of the guide in the list of parent guides.
     *
     * @param aGuide guide to look for.
     *
     * @return index or -1 if not found.
     */
    private int indexOfParentGuide(IGuide aGuide)
    {
        int index = -1;
        for (int i = 0; index == -1 && i < guides.length; i++)
        {
            if (guides[i] == aGuide) index = i;
        }

        return index;
    }

    /**
     * Returns <code>TRUE</code> if the feed belongs to the guide.
     *
     * @param guide guide to check.
     *
     * @return <code>TRUE</code> if the feed belongs to the guide.
     */
    public boolean belongsTo(IGuide guide)
    {
        return indexOfParentGuide(guide) != -1;
    }

    /**
     * Returns <code>TRUE</code> if this feed is assigned to some reading list.
     *
     * @return <code>TRUE</code> if this feed is assigned to some reading list.
     */
    public boolean isDynamic()
    {
        return false;
    }

    /**
     * Returns guides currently holding this feed.
     *
     * @return parent guides.
     */
    public IGuide[] getParentGuides()
    {
        // Dangerous!!! Possible modifications to the guides list.
        return guides;
    }

    /**
     * Notifies feed that processing of it started.
     */
    public void processingStarted()
    {
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("Start Processing  : " + getTitle());
        }

        int cnt = processingCount.getAndIncrement();
        if (cnt == 1) firePropertyChanged(PROP_PROCESSING, FALSE, TRUE, false, true);
    }

    /**
     * Notifies feed that processing of it finished.
     */
    public void processingFinished()
    {
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("Finish Processing : " + getTitle());
        }

        int cnt = processingCount.decrementAndGet();
        if (cnt <= 0)
        {
            if (cnt < 0)
            {
                processingCount.compareAndSet(cnt, 0);

                if (LOG.isLoggable(Level.WARNING))
                {
                    LOG.warning(MessageFormat.format(Strings.error("feed.processing.counter.got.below.0"), getTitle()));
                }
            }
            firePropertyChanged(PROP_PROCESSING, TRUE, FALSE, false, true);
        }
    }

    /**
     * Returns TRUE if the feed is under some lengthy processing.
     *
     * @return TRUE if the feed is under some lengthy processing.
     */
    public boolean isProcessing()
    {
        return processingCount.get() > 0;
    }

    /**
     * TRUE when all articles are read in this feed.
     *
     * @return TRUE when everything is read.
     */
    public synchronized boolean isRead()
    {
        return getUnreadArticlesCount() == 0;
    }

    /**
     * Sets the whole feed read / unread.
     *
     * @param read TRUE if mark as read.
     */
    public synchronized void setRead(boolean read)
    {
        int count = getArticlesCount();
        for (int i = 0; i < count; i++)
        {
            IArticle article = getArticleAt(count - i - 1);
            article.setRead(read);
        }
    }

    /**
     * Returns unread articles count.
     *
     * @return count.
     */
    public int getUnreadArticlesCount()
    {
        int unread = 0;

        // I intentionally don't use method-level sync
        // to keep the synchronized keyword out of the signature
        synchronized (this)
        {
            int count = getArticlesCount();
            for (int i = 0; i < count; i++)
            {
                IArticle article = getArticleAt(i);
                if (!article.isRead()) unread++;
            }
        }

        return unread;
    }

    /**
     * Returns <code>true</code> if this feed is invalid.
     *
     * @return <code>true</code> if invalid.
     */
    public boolean isInvalid()
    {
        return getInvalidnessReason() != null;
    }

    /**
     * Sets the reason of invalidness of this feed. If reason is set to <code>NULL</code> the feed
     * is considered valid.
     *
     * @param reason reason of invalidness or <code>NULL</code>.
     */
    public void setInvalidnessReason(String reason)
    {
        String oldReason = invalidnessReason;
        invalidnessReason = StringUtils.intern(reason);
        firePropertyChanged(PROP_INVALIDNESS_REASON, oldReason, invalidnessReason, false, true);
    }

    /**
     * Returns reason for being invalid.
     *
     * @return reason.
     */
    public String getInvalidnessReason()
    {
        return invalidnessReason;
    }

    /**
     * Adds listener to the list.
     *
     * @param l listener.
     */
    public void addListener(IFeedListener l)
    {
        if (!listeners.contains(l)) listeners.add(l);
    }

    /**
     * Removes listener from the list.
     *
     * @param l listener.
     */
    public void removeListener(IFeedListener l)
    {
        listeners.remove(l);
    }

    /**
     * Fires event about that the feed information has been changed.
     *
     * @param property property of the feed.
     * @param oldValue old property value.
     * @param newValue new property value.
     *
     * @throws NullPointerException if property isn't specified.
     */
    protected void firePropertyChanged(String property, Object oldValue, Object newValue)
    {
        firePropertyChanged(property, oldValue, newValue, false, false);
    }

    /**
     * Fires event about that the feed information has been changed.
     *
     * @param property property of the feed.
     * @param oldValue old property value.
     * @param newValue new property value.
     *
     * @throws NullPointerException if property isn't specified.
     */
    protected void firePropertyChanged(String property, int oldValue, int newValue)
    {
        firePropertyChanged(property, new Integer(oldValue), new Integer(newValue));
    }

    /**
     * Fires event about that the feed information has been changed.
     *
     * @param property property of the feed.
     * @param oldValue old property value.
     * @param newValue new property value.
     * @param syncProperty <code>TRUE</code> if this property is involved in synchronization.
     * @param visibilityProperty <code>TRUE</code> if this property possibly affects visibility.
     *
     * @throws NullPointerException if property isn't specified.
     */
    protected void firePropertyChanged(String property, Object oldValue, Object newValue,
                                       boolean syncProperty, boolean visibilityProperty)
    {
        if (property == null) throw new NullPointerException(Strings.error("unspecified.property"));

        // Do not fire the event if property values are identical
        if (!CommonUtils.areDifferent(oldValue, newValue)) return;

        for (IFeedListener listener : listeners) listener.propertyChanged(this, property, oldValue, newValue);

        if (syncProperty) registerUpdate();
        if (visibilityProperty) invalidateVisibilityCache();
    }

    /**
     * Fires event about new article has been added.
     *
     * @param article article which was added.
     *
     * @throws NullPointerException if article isn't specified.
     */
    protected void fireArticleAdded(IArticle article)
    {
        for (IFeedListener listener : listeners) listener.articleAdded(this, article);
    }

    /**
     * Fires event about new article has been removed.
     *
     * @param article article which was removed.
     */
    protected void fireArticleRemoved(IArticle article)
    {
        for (IFeedListener listener : listeners) listener.articleRemoved(this, article);
    }

    /**
     * Returns string representation of this feed object.
     *
     * @return string representation.
     */
    public String toString()
    {
        return MessageFormat.format(Strings.message("feed.string.representation"), getTitle(),
            getUnreadArticlesCount(), getArticlesCount());
    }

    /**
     * Returns the rating of this feed set by user.
     *
     * @return rating of the feed or (-1) if not set.
     */
    public int getRating()
    {
        return rating;
    }

    /**
     * Sets new rating for the feed.
     *
     * @param aRating new rating in range [0;4] or (-1) to reset.
     *
     * @throws IllegalArgumentException if rating is not within range or (-1).
     *
     * @see #RATING_NOT_SET
     */
    public void setRating(int aRating)
    {
        if (aRating != RATING_NOT_SET && (aRating < RATING_MIN || aRating > RATING_MAX))
            throw new IllegalArgumentException(MessageFormat.format(Strings.error("incorrect.rating.value"), aRating));

        int oldRating = rating;
        rating = aRating;
        firePropertyChanged(PROP_RATING, oldRating, rating, true, true);
    }

    /**
     * Returns time when feed was visited for the last time.
     *
     * @return time     last visit time
     */
    public long getLastVisitTime()
    {
        return lastVisitTime;
    }

    /**
     * Sets time when feed was visited for the last time.
     *
     * @param time      last visit time
     */
    public void setLastVisitTime(final long time)
    {
        final long oldLastVisitTime = lastVisitTime;
        lastVisitTime = time;

        firePropertyChanged(PROP_LAST_VISIT_TIME, oldLastVisitTime, lastVisitTime);
    }

    /**
     * Returns the type of the feed.
     *
     * @return feed type.
     */
    public FeedType getType()
    {
        return type;
    }

    /**
     * Sets the type of the feed.
     *
     * @param aType type.
     */
    public void setType(FeedType aType)
    {
        FeedType oldType = type;
        type = aType;

        firePropertyChanged(PROP_TYPE, oldType, type, true, false);
    }

    /**
     * Gets feed handling type.
     *
     * @return type.
     */
    public FeedHandlingType getHandlingType()
    {
        return handlingType;
    }

    /**
     * Sets feed handling type.
     *
     * @param type type.
     */
    public void setHandlingType(FeedHandlingType type)
    {
        if (type == null) type = FeedHandlingType.DEFAULT;
        FeedHandlingType oldType = handlingType;
        handlingType = type;

        firePropertyChanged(PROP_HANDLING_TYPE, oldType, handlingType, true, false);
    }

    /**
     * When <code>TRUE</code> the feed has preferred view mode set.
     *
     * @return <code>TRUE</code> when feed has its own view mode.
     */
    public boolean isCustomViewModeEnabled()
    {
        return customViewModeEnabled;
    }

    /**
     * Sets the custom view mode enabled / disabled.
     *
     * @param enabled <code>TRUE</code> to enable.
     */
    public void setCustomViewModeEnabled(boolean enabled)
    {
        boolean oldValue = customViewModeEnabled;
        customViewModeEnabled = enabled;

        firePropertyChanged(PROP_CUSTOM_VIEW_MODE_ENABLED, oldValue, enabled, true, false);
    }

    /**
     * Returns custom view mode.
     *
     * @return view mode.
     *
     * @see IFeedDisplayConstants#MODE_MINIMAL
     * @see IFeedDisplayConstants#MODE_BRIEF
     * @see IFeedDisplayConstants#MODE_FULL
     */
    public int getCustomViewMode()
    {
        return customViewMode;
    }

    /**
     * Sets the custom view mode.
     *
     * @param mode mode.
     *
     * @see IFeedDisplayConstants#MODE_MINIMAL
     * @see IFeedDisplayConstants#MODE_BRIEF
     * @see IFeedDisplayConstants#MODE_FULL
     */
    public void setCustomViewMode(int mode)
    {
        if (mode == -1) return;

        int oldValue = customViewMode;
        customViewMode = mode;

        firePropertyChanged(PROP_CUSTOM_VIEW_MODE, oldValue, customViewMode, true, false);
    }

    /**
     * Returns the time of last properties update. This time is necessary for the synchronization
     * engine to learn what object is newer.
     *
     * @return the time or <code>-1</code> if not updated yet.
     */
    public long getLastUpdateTime()
    {
        return lastUpdateTime;
    }

    /**
     * Sets the time of last properties update. When the user changes some property this time is set
     * automatically. This method is necessary for persistence layer to init the object with what is
     * currently in the database.
     *
     * @param time time.
     */
    public void setLastUpdateTime(long time)
    {
        long oldValue = lastUpdateTime;
        lastUpdateTime = time;

        firePropertyChanged(PROP_LAST_UPDATE_TIME, oldValue, time);
    }

    /**
     * Registers last update time.
     */
    protected void registerUpdate()
    {
        setLastUpdateTime(System.currentTimeMillis());
    }

    /**
     * Returns the number of views of this feed.
     *
     * @return views count.
     */
    public int getViews()
    {
        return views;
    }

    /**
     * Sets the number of views of this feed.
     *
     * @param views views count.
     */
    public void setViews(int views)
    {
        int oldVal = this.views;
        this.views = views;

        firePropertyChanged(PROP_VIEWS, oldVal, views);
    }

    /**
     * Returns the number of times articles from this feed were opened in the browser.
     *
     * @return times.
     */
    public int getClickthroughs()
    {
        return clickthroughs;
    }

    /**
     * Sets the number of times articles from this feed have been opened in the browser.
     *
     * @param times times.
     */
    public void setClickthroughs(int times)
    {
        int oldVal = clickthroughs;
        clickthroughs = times;

        firePropertyChanged(PROP_CLICKTHROUGHS, oldVal, clickthroughs);
    }

    /**
     * Returns <code>TRUE</code> if an article has matching words from <code>from</code> to
     * <code>to</code> of the title.
     *
     * @param article   article.
     * @param from      the first word.
     * @param to        the last word.
     * @param articles  articles to check against.
     *
     * @return <code>TRUE</code> if duplicate.
     */
    protected static boolean isDuplicate(IArticle article, int from, int to, List<IArticle> articles)
    {
        // Decrement to map from human to computer indexes
        from--;
        to--;

        String[] words = article.getTitleWords();
//        String words = StringUtils.getWordsInRange(article.getTitle(), from - 1, to - 1);
        if (words.length == 0) return false;

        for (IArticle art : articles)
        {
            if (art == article) continue;

            String[] aw = art.getTitleWords();
//            String aw = StringUtils.getWordsInRange(art.getTitle(), from - 1, to - 1);

            if (wordsEqual(words, aw, from, to)) return true;
        }

        return false;
    }

    /**
     * Compares two sets of words starting from one index and moving on to another. If
     * there are not enough words in one of the sets, they aren't equal.
     *
     * @param words1    first set.
     * @param words2    second set.
     * @param from      index to start from.
     * @param to        the last index (inclusive).
     *
     * @return <code>TRUE</code> if the words in range are equal (ignoring the case) in both sets.
     */
    static boolean wordsEqual(String[] words1, String[] words2, int from, int to)
    {
        if (words1.length <= to || words2.length <= to) return false;

        boolean match = false;
        while (from <= to && (match = words1[from].equalsIgnoreCase(words2[from]))) from++;

        return match;
    }

    /**
     * Returns the mask of a feed meta-classes.
     *
     * @return mask.
     */
    public int getClassesMask()
    {
        return FeedClassifier.classify(this);
    }

    // ------------------------------------------------------------------------
    // Feeds visibility
    // ------------------------------------------------------------------------

    private static IFeedVisibilityResolver feedVisibilityResolver;
    private static final long VISIBILITY_CACHE_EXPIRE_PERIOD = 600000; // 10 minutes

    private volatile long visibilityCacheExpires = 0;
    private volatile boolean visibilityCache = true;
    private final ReentrantReadWriteLock visibilityCL = new ReentrantReadWriteLock();

    /**
     * Registers feed visibility resolver.
     *
     * @param fvis feed visibility resolver.
     */
    public static void setFeedVisibilityResolver(IFeedVisibilityResolver fvis)
    {
        AbstractFeed.feedVisibilityResolver = fvis;
    }

    /**
     * Returns <code>TRUE</code> if feed is visible.
     *
     * @return <code>TRUE</code> if feed is visible.
     */
    public boolean isVisible()
    {
        boolean visible;
        long time = System.currentTimeMillis();

        visibilityCL.readLock().lock();
        if (visibilityCacheExpires < time)
        {
            // Upgrade the lock
            visibilityCL.readLock().unlock();
            visibilityCL.writeLock().lock();

            try
            {
                // Check if is still expired
                if (visibilityCacheExpires < time)
                {
                    // We set the expiration time first to be sure
                    // if the invalidation comes while we are computing isVisibleNoCache
                    // the next time we ask for this flag, it will be calculated again.
                    visibilityCacheExpires = time + VISIBILITY_CACHE_EXPIRE_PERIOD;
                    visibilityCache = isVisibleNoCache();
                }
            } finally
            {
                // Downgrade the lock
                visibilityCL.readLock().lock();
                visibilityCL.writeLock().unlock();
            }
        }
        visible = visibilityCache;
        visibilityCL.readLock().unlock();

        return visible;
    }

    /**
     * Invalidates visibility cache immediately.
     */
    public void invalidateVisibilityCache()
    {
//        visibilityCL.writeLock().lock();
        visibilityCacheExpires = 0;
//        visibilityCL.writeLock().unlock();
    }

    /**
     * Returns <code>TRUE</code> if feed is visible.
     *
     * @return <code>TRUE</code> if feed is visible.
     */
    protected boolean isVisibleNoCache()
    {
        return feedVisibilityResolver == null || isProcessing() || feedVisibilityResolver.isVisible(this);
    }

    /**
     * Returns the state of the articles sort override flag.
     *
     * @return <code>NULL</code> for no override, <code>TRUE / FALSE</code> as a value.
     */
    public Boolean getAscendingSorting()
    {
        return ascendingSorting;
    }

    /**
     * Sets the state of the articles sort override flag.
     *
     * @param asc <code>NULL</code> to clear override, <code>TRUE</code> to sort in ascending order.
     */
    public void setAscendingSorting(Boolean asc)
    {
        Boolean old = ascendingSorting;
        ascendingSorting = asc;
        firePropertyChanged(PROP_ASCENDING_SORTING, old, asc, true, false);
    }

    // ------------------------------------------------------------------------
    // Articles auto-saving
    // ------------------------------------------------------------------------

    /**
     * Enables / disables automatic articles saving.
     *
     * @param en <code>TRUE</code> to enable.
     */
    public void setAutoSaveArticles(boolean en)
    {
        boolean old = autoSaveArticles;
        autoSaveArticles = en;
        firePropertyChanged(PROP_AUTO_SAVE_ARTICLES, old, en);
    }

    /**
     * Returns <code>TRUE</code> if auto-saving of articles is enabled.
     *
     * @return <code>TRUE</code> if auto-saving of articles is enabled.
     */
    public boolean isAutoSaveArticles()
    {
        return autoSaveArticles;
    }

    /**
     * Sets the path to the folder to save articles to.
     *
     * @param folder the folder path.
     */
    public void setAutoSaveArticlesFolder(String folder)
    {
        String old = autoSaveArticlesFolder;
        autoSaveArticlesFolder = folder;
        firePropertyChanged(PROP_AUTO_SAVE_ARTICLES_FOLDER, old, folder);
    }

    /**
     * Returns the path to the folder to save articles to.
     *
     * @return folder path.
     */
    public String getAutoSaveArticlesFolder()
    {
        return autoSaveArticlesFolder;
    }

    /**
     * Sets the format for the file name.
     *
     * @param nameFormat name format.
     */
    public void setAutoSaveArticlesNameFormat(String nameFormat)
    {
        String old = autoSaveArticlesNameFormat;
        autoSaveArticlesNameFormat = nameFormat;
        firePropertyChanged(PROP_AUTO_SAVE_ARTICLES_NAME_FORMAT, old, nameFormat);
    }

    /**
     * Returns the format for the file name.
     *
     * @return name format.
     */
    public String getAutoSaveArticlesNameFormat()
    {
        return autoSaveArticlesNameFormat;
    }

    // ------------------------------------------------------------------------
    // Enclosure auto-saving
    // ------------------------------------------------------------------------

    /**
     * Enables / disables automatic enclosures saving.
     *
     * @param en <code>TRUE</code> to enable.
     */
    public void setAutoSaveEnclosures(boolean en)
    {
        boolean old = autoSaveEnclosures;
        autoSaveEnclosures = en;
        firePropertyChanged(PROP_AUTO_SAVE_ENCLOSURES, old, en);
    }

    /**
     * Returns <code>TRUE</code> if auto-saving of enclosures is enabled.
     *
     * @return <code>TRUE</code> if enabled.
     */
    public boolean isAutoSaveEnclosures()
    {
        return autoSaveEnclosures;
    }

    /**
     * Sets the path to the folder to save enclosures to.
     *
     * @param folder the folder path.
     */
    public void setAutoSaveEnclosuresFolder(String folder)
    {
        String old = autoSaveEnclosuresFolder;
        autoSaveEnclosuresFolder = folder;
        firePropertyChanged(PROP_AUTO_SAVE_ENCLOSURES_FOLDER, old, folder);
    }

    /**
     * Returns the path to the folder to save enclosures to.
     *
     * @return folder path.
     */
    public String getAutoSaveEnclosuresFolder()
    {
        return autoSaveEnclosuresFolder;
    }

    /**
     * Sets the format for the file name.
     *
     * @param nameFormat name format.
     */
    public void setAutoSaveEnclosuresNameFormat(String nameFormat)
    {
        String old = autoSaveEnclosuresNameFormat;
        autoSaveEnclosuresNameFormat = nameFormat;
        firePropertyChanged(PROP_AUTO_SAVE_ENCLOSURES_NAME_FORMAT, old, nameFormat);
    }

    /**
     * Returns the format for the file name.
     *
     * @return name format.
     */
    public String getAutoSaveEnclosuresNameFormat()
    {
        return autoSaveEnclosuresNameFormat;
    }
}