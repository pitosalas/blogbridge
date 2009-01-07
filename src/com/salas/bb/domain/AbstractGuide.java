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
// $Id: AbstractGuide.java,v 1.34 2007/04/17 07:03:23 spyromus Exp $
//

package com.salas.bb.domain;

import com.salas.bb.domain.events.FeedRemovedEvent;
import com.salas.bb.utils.CommonUtils;
import com.salas.bb.utils.i18n.Strings;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>Abstract implementation of <code>IGuide</code> inerface. This implementation
 * takes care of work with listeners and exposes the methods to fire all events
 * defined in these listeners.</p>
 *
 * <p>This implementation also provides the services to store title and icon key.
 * If sub-classes do not agree with that, they can override appropriate methods and
 * control the titles and icons themselves.</p>
 *
 * <p>Some default values for holded feeds are being stored in the guides. This
 * abstract implementation holds: update period, automatic feeds discovery flag value.</p>
 */
public abstract class AbstractGuide implements IGuide
{
    /** ID of the guide in database. It equals to (-1) by default. */
    private long id;

    /** The listeners of the guide. */
    protected final List<IGuideListener> listeners;

    /** Title of the guide. */
    private String title;
    /** Icon key association with the guide. */
    private String iconKey;

    /**
     * Flag of publishing state. When it's set and the publishing title is set,
     * publishing happens.
     */
    private boolean publishingEnabled;
    /** The title of the guide when published. */
    private String publishingTitle;
    /** The tags associated with this guide when published. */
    private String publishingTags;
    /** The flag of publishing state. When set, shows that the publication is public. */
    private boolean publishingPublic;
    /** The URL of the published guide. */
    private String publishingURL;
    /** The time of last publishing. */
    private long lastPublishingTime;
    /** The rating necessary to publish a feed. */
    private int publishingRating;

    /**
     * Automatical feed links discovery flag. When this flag is set the links
     * from new articles of all contained feed will be automatically passed to
     * discovery service.
     */
    private boolean autoFeedsDiscovery;

    /**
     * Time time indicates the last time when the last of the properties involved in
     * synchronization routines was updated.
     */
    private long lastUpdateTime;

    /** The flag showing whether the notifications are enabled or not. */
    private boolean notificationsAllowed;

    /**
     * Creates the guide.
     */
    public AbstractGuide()
    {
        listeners = new CopyOnWriteArrayList<IGuideListener>();
        autoFeedsDiscovery = false;
        id = -1;

        publishingEnabled = false;
        publishingPublic = false;
        lastPublishingTime = -1;
        lastUpdateTime = -1;
        publishingRating = 0;
        notificationsAllowed = true;
    }

    /**
     * Returns title of the guide.
     *
     * @return title.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Sets new title of the guide.
     *
     * @param aTitle title.
     */
    public void setTitle(String aTitle)
    {
        String oldTitle = title;
        title = aTitle;
        firePropertyChanged(PROP_TITLE, oldTitle, title);
    }

    /**
     * Returns key of associated icon.
     *
     * @return icon key.
     */
    public String getIconKey()
    {
        return iconKey;
    }

    /**
     * Changes the key of icon.
     *
     * @param key new icon key.
     */
    public void setIconKey(String key)
    {
        String oldKey = iconKey;
        iconKey = key;
        firePropertyChanged(PROP_ICON_KEY, oldKey, iconKey, true);
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

    /** Removes every reading list and feed associated with this guide. */
    public void removeChildren()
    {
        remove(getFeeds());
    }

    /**
     * Returns TRUE if automatic new articles scanning is enabled.
     *
     * @return TRUE if automatic new articles scanning is enabled.
     */
    public boolean isAutoFeedsDiscovery()
    {
        return autoFeedsDiscovery;
    }

    /**
     * Sets the value of <code>autoFeedsDiscovery</code> flag. When set all links
     * from new articles in contained feed will be passed to discovery engine
     * automatically.
     *
     * @param value TRUE for auto-discovery.
     */
    public void setAutoFeedsDiscovery(boolean value)
    {
        boolean oldValue = autoFeedsDiscovery;
        autoFeedsDiscovery = value;
        firePropertyChanged(PROP_AUTO_FEEDS_DISCOVERY, oldValue, value, true);
    }

    /**
     * Returns all feeds (data and virtual) having the specified XML URL.
     *
     * @param xmlURL XML URL.
     * @param includeDisabled <code>TRUE</code> to include disabled feeds.
     *
     * @return collection of feeds.
     *
     * @throws NullPointerException if URL is not specified.
     */
    public synchronized Collection<NetworkFeed> findFeedsByXmlURL(URL xmlURL, boolean includeDisabled)
    {
        if (xmlURL == null) throw new NullPointerException(Strings.error("unspecified.url"));

        String xmlURLS = xmlURL.toString();
        List<NetworkFeed> feeds = new ArrayList<NetworkFeed>();

        // Spin through all feeds and try to find all with similar URL
        int count = getFeedsCount();
        for (int i = 0; i < count; i++)
        {
            IFeed feed = getFeedAt(i);

            // Only network feeds have XML URL
            if (feed instanceof NetworkFeed)
            {
                NetworkFeed nfeed = (NetworkFeed)feed;

                URL feedURL = nfeed.getXmlURL();
                if (feedURL != null && feedURL.toString().equalsIgnoreCase(xmlURLS))
                {
                    if (includeDisabled || !(feed instanceof DirectFeed) ||
                        !((DirectFeed)feed).isDisabled())
                    {
                        feeds.add(nfeed);
                    }
                }
            }
        }

        return feeds;
    }

    /**
     * Returns the read status of this guide. The status depends on the statuses of contained
     * feeds.
     */
    public synchronized boolean isRead()
    {
        // TODO replace this with caching of read state based on events from feeds.

        boolean read = true;
        int count = getFeedsCount();
        for (int i = 0; read && i < count; i++)
        {
            read = getFeedAt(i).isRead();
        }

        return read;
    }

    /**
     * Marks whole guide as read/unread depending on the argument. Iterates through all feeds and
     * makes them read/unread.
     */
    public synchronized void setRead(boolean read)
    {
        int count = getFeedsCount();
        for (int i = 0; i < count; i++)
        {
            getFeedAt(i).setRead(read);
        }
    }

    /**
     * Adds guide listener.
     *
     * @param listener listener.
     *
     * @throws NullPointerException if listener isn't specified.
     */
    public void addListener(IGuideListener listener)
    {
        if (listener == null) throw new NullPointerException(Strings.error("unspecified.listener"));

        if (!listeners.contains(listener)) listeners.add(listener);
    }

    /**
     * Removes guide listener.
     *
     * @param listener listener.
     *
     * @throws NullPointerException if listener isn't specified.
     */
    public void removeListener(IGuideListener listener)
    {
        if (listener == null) throw new NullPointerException(Strings.error("unspecified.listener"));

        listeners.remove(listener);
    }

    /**
     * Fires <code>feedAdded</code> event.
     *
     * @param feed feed that was added.
     *
     * @throws NullPointerException if feed isn't specified.
     */
    protected void fireFeedAdded(IFeed feed)
    {
        if (feed == null) throw new NullPointerException(Strings.error("unspecified.feed"));

        for (IGuideListener listener : listeners) listener.feedAdded(this, feed);
    }

    /**
     * Fires <code>feedLinkAdded</code> event.
     *
     * @param feed feed that was added.
     *
     * @throws NullPointerException if feed isn't specified.
     */
    protected void fireFeedLinkAdded(IFeed feed)
    {
        if (feed == null) throw new NullPointerException(Strings.error("unspecified.feed"));

        for (IGuideListener listener : listeners) listener.feedLinkAdded(this, feed);
    }

    /**
     * Fires <code>feedRemoved</code> event.
     *
     * @param feed feed that was removed.
     * @param index index of removed feed.
     * @param lastInBatch <code>TRUE</code> if this removal was last in batch.
     *
     * @throws NullPointerException if feed isn't specified.
     */
    protected void fireFeedRemoved(IFeed feed, int index, boolean lastInBatch)
    {
        if (feed == null) throw new NullPointerException(Strings.error("unspecified.feed"));

        FeedRemovedEvent event = null;

        for (IGuideListener listener : listeners)
        {
            if (event == null) event = new FeedRemovedEvent(this, feed, index, lastInBatch);
            listener.feedRemoved(event);
        }
    }

    /**
     * Fires <code>feedRepositioned</code> event.
     *
     * @param feed feed that was moved.
     * @param oldIndex old index of the feed.
     * @param newIndex new index of the feed.
     *
     * @throws NullPointerException if feed isn't specified.
     */
    protected void fireFeedRepositioned(IFeed feed, int oldIndex, int newIndex)
    {
        if (feed == null) throw new NullPointerException(Strings.error("unspecified.feed"));

        for (IGuideListener listener : listeners) listener.feedRepositioned(this, feed, oldIndex, newIndex);
    }

    /**
     * Fires <code>feedRemoved</code> event.
     *
     * @param feed feed that was removed.
     *
     * @throws NullPointerException if feed isn't specified.
     */
    protected void fireFeedLinkRemoved(IFeed feed)
    {
        if (feed == null) throw new NullPointerException(Strings.error("unspecified.feed"));

        for (IGuideListener listener : listeners) listener.feedLinkRemoved(this, feed);
    }

    /**
     * Fires <code>propertyChanged</code> event.
     *
     * @param property  property name.
     * @param oldValue  old value.
     * @param newValue  new value.
     *
     * @throws NullPointerException if property name isn't specified.
     */
    protected void firePropertyChanged(String property, Object oldValue, Object newValue)
    {
        firePropertyChanged(property, oldValue, newValue, false);
    }

    /**
     * Fires <code>propertyChanged</code> event.
     *
     * @param property      property name.
     * @param oldValue      old value.
     * @param newValue      new value.
     * @param syncProperty  <code>TRUE</code> when the property is involved in sync.
     *
     * @throws NullPointerException if property name isn't specified.
     */
    protected void firePropertyChanged(String property, Object oldValue, Object newValue,
        boolean syncProperty)
    {
        if (property == null) throw new NullPointerException(Strings.error("unspecified.property"));

        if (!CommonUtils.areDifferent(oldValue, newValue)) return;

        if (syncProperty) setLastUpdateTime(System.currentTimeMillis());

        for (IGuideListener listener : listeners) listener.propertyChanged(this, property, oldValue, newValue);
    }

    /**
     * Returns title of the guide as it's string representation.
     *
     * @return string representation.
     */
    public String toString()
    {
        // The title is necessary to be returned here because some
        // comboboxes use this method to get the text for the items.

        return getTitle();
    }

    /**
     * Returns ID of the guide. This ID is used by persistence layer to identify record in
     * database.
     *
     * @return ID of the guide.
     */
    public long getID()
    {
        return id;
    }

    /**
     * Sets the ID of the guide.
     *
     * @param aId ID of the guide.
     */
    public void setID(long aId)
    {
        id = aId;
    }

    /**
     * Cleans all contained data feeds.
     */
    public void clean()
    {
        IFeed[] feeds = getFeeds();
        for (IFeed feed : feeds) if (feed instanceof DataFeed) ((DataFeed)feed).clean();
    }

    // ---------------------------------------------------------------------------------------------
    // Publishing
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns <code>TRUE</code> if publishing for this guide is enabled.
     *
     * @return <code>TRUE</code> if publishing for this guide is enabled.
     */
    public boolean isPublishingEnabled()
    {
        return publishingEnabled;
    }

    /**
     * Set <code>TRUE</code> to enable publishing of this guide. Note that the publishing may still
     * not work if the publishing title is not set.
     *
     * @param enabled <code>TRUE</code> to enable.
     */
    public void setPublishingEnabled(boolean enabled)
    {
        boolean oldValue = publishingEnabled;
        publishingEnabled = enabled;

        firePropertyChanged(PROP_PUBLISHING_ENABLED, oldValue, enabled, true);
    }

    /**
     * Returns <code>TRUE</code> if the published guide will be visible to everyone.
     *
     * @return <code>TRUE</code> if the published guide will be visible to everyone.
     */
    public boolean isPublishingPublic()
    {
        return publishingPublic;
    }

    /**
     * Sets the state of publication. When <code>TRUE</code> the guide will be
     * visible to everyone when published.
     *
     * @param flag <code>TRUE</code> to make it public.
     */
    public void setPublishingPublic(boolean flag)
    {
        boolean oldValue = publishingPublic;
        publishingPublic = flag;

        firePropertyChanged(PROP_PUBLISHING_PUBLIC, oldValue, flag, true);
    }

    /**
     * Returns the list of tags entered for this guide to be published.
     *
     * @return the list of tags.
     */
    public String getPublishingTags()
    {
        return publishingTags;
    }

    /**
     * Sets the tags used when publishing.
     *
     * @param tags tags.
     */
    public void setPublishingTags(String tags)
    {
        String oldValue = publishingTags;
        publishingTags = tags;

        firePropertyChanged(PROP_PUBLISHING_TAGS, oldValue, tags, true);
    }

    /**
     * Returns the title of this guide when published.
     *
     * @return the title.
     */
    public String getPublishingTitle()
    {
        return publishingTitle;
    }

    /**
     * Sets the title used when publishing.
     *
     * @param title the title.
     */
    public void setPublishingTitle(String title)
    {
        String oldValue = publishingTitle;
        publishingTitle = title;

        firePropertyChanged(PROP_PUBLISHING_TITLE, oldValue, title, true);
    }

    /**
     * Returns the URL which is assigned to the published guide.
     *
     * @return the URL which is assigned to the published guide.
     */
    public String getPublishingURL()
    {
        return publishingURL;
    }

    /**
     * Sets the URL which is assigned to the published guide.
     *
     * @param url the URL which is assigned to the published guide.
     */
    public void setPublishingURL(String url)
    {
        String oldValue = publishingURL;
        publishingURL = url;

        firePropertyChanged(PROP_PUBLISHING_URL, oldValue, url);
    }

    /**
     * Returns the time of last publishing.
     *
     * @return the time or <code>-1</code> if never happened.
     */
    public long getLastPublishingTime()
    {
        return lastPublishingTime;
    }

    /**
     * Sets the time of last publishing.
     *
     * @param time the time.
     */
    public void setLastPublishingTime(long time)
    {
        long oldValue = lastPublishingTime;
        lastPublishingTime = time;

        firePropertyChanged(PROP_LAST_PUBLISHING_TIME, oldValue, time);
    }

    /**
     * Return minimum rating necessary to publish feeds.
     *
     * @return minimum rating.
     */
    public int getPublishingRating()
    {
        return publishingRating;
    }

    /**
     * Sets minimum rating necessary to publish feeds.
     *
     * @param rating minimum rating.
     */
    public void setPublishingRating(int rating)
    {
        int oldVal = publishingRating;
        publishingRating = rating;

        firePropertyChanged(PROP_PUBLISHING_RATING, oldVal, rating, true);
    }

    /**
     * Compares this guide with the other guide object.
     *
     * @param o other guide.
     *
     * @return TRUE if equivalenrt.
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AbstractGuide that = (AbstractGuide)o;

        if (autoFeedsDiscovery != that.autoFeedsDiscovery) return false;
        if (iconKey != null ? !iconKey.equals(that.iconKey) : that.iconKey != null) return false;

        return !(title != null ? !title.equals(that.title) : that.title != null);
    }

    /**
     * Returns hash code of this guide object.
     *
     * @return hash code.
     */
    public int hashCode()
    {
        return title == null ? 0 : title.hashCode();
    }

    /**
     * Returns <code>TRUE</code> if notifications about new articles and feeds in this guide
     * are enabled.
     *
     * @return <code>TRUE</code> if notifications about new articles and feeds in this guide
     * are enabled.
     */
    public boolean isNotificationsAllowed()
    {
        return notificationsAllowed;
    }

    /**
     * Sets the state of notifications flag.
     *
     * @param flag <code>TRUE</code> to enable notifications of changes.
     */
    public void setNotificationsAllowed(boolean flag)
    {
        boolean oldValue = notificationsAllowed;
        notificationsAllowed = flag;

        firePropertyChanged(PROP_NOTIFICATIONS_ALLOWED, oldValue, flag, true);
    }

    /**
     * Returns the mask of a feed meta-classes.
     *
     * @return mask.
     */
    public int getClassesMask()
    {
        return GuideClassifier.classify(this);
    }
}
