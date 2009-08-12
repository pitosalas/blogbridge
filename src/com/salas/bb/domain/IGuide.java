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
// $Id: IGuide.java,v 1.24 2007/04/17 07:03:23 spyromus Exp $
//

package com.salas.bb.domain;

import java.net.URL;
import java.util.Collection;

/**
 * Guide is a named set of feeds. Each guide has a title and associated icon
 */
public interface IGuide
{
    /** Guide title. */
    String PROP_TITLE                   = "title";
    /** Guide icon key. */
    String PROP_ICON_KEY                = "iconKey";
    /** Automatic feeds discovery flag. */
    String PROP_AUTO_FEEDS_DISCOVERY    = "autoFeedsDiscovery";
    /** Publishing flag. */
    String PROP_PUBLISHING_ENABLED      = "publishingEnabled";
    /** Public publication flag. */
    String PROP_PUBLISHING_PUBLIC       = "publishingPublic";
    /** Publishing tags. */
    String PROP_PUBLISHING_TAGS         = "publishingTags";
    /** Publishing title. */
    String PROP_PUBLISHING_TITLE        = "publishingTitle";
    /** The URL of published guide. */
    String PROP_PUBLISHING_URL          = "publishingURL";
    /** The time of last publishing attempt. */
    String PROP_LAST_PUBLISHING_TIME    = "lastPublishingTime";
    /** The rating necessary to publish a feed. */
    String PROP_PUBLISHING_RATING       = "publishingRating";
    /** The time of last update of the property involved in synchronization. */
    String PROP_LAST_UPDATE_TIME        = "lastUpdateTime";
    /** The name of the notifications enabled flag property. */
    String PROP_NOTIFICATIONS_ALLOWED   = "notificationsAllowed";
    /** The name of the mobile flag. */
    String PROP_MOBILE                  = "mobile";

    /**
     * Returns the feed at given position. If the position is out of range [0;size) the IOOB
     * exception will be thrown.
     *
     * @param index index of the feed.
     *
     * @return feed at specified index.
     *
     * @throws IndexOutOfBoundsException if the feed index is out of range [0;size).
     */
    IFeed getFeedAt(int index);

    /**
     * Returns number of feeds in the guide.
     *
     * @return number of feeds.
     */
    int getFeedsCount();

    /**
     * Adds feed to the guide.
     *
     * @param feed feed to add.
     *
     * @throws NullPointerException if feed isn't specified.
     * @throws IllegalStateException if feed is already assigned to some feed.
     */
    void add(IFeed feed);

    /**
     * Removes feed from the guide.
     *
     * @param feed feed to remove.
     *
     * @return TRUE if removed.
     *
     * @throws NullPointerException if feed isn't specified.
     */
    boolean remove(IFeed feed);

    /**
     * Removes the feeds in list from this guide one by one.
     *
     * @param feeds feeds to remove.
     */
    void remove(IFeed[] feeds);

    /**
     * Returns index of feed within the guide.
     *
     * @param feed feed to get index for.
     *
     * @return the index of a feed.
     *
     * @throws NullPointerException if feed isn't specified.
     * @throws IllegalStateException if feed is assigned to the other guide.
     */
    int indexOf(IFeed feed);

    /**
     * Returns guide title.
     *
     * @return title.
     */
    String getTitle();

    /**
     * Sets the title of the guide.
     *
     * @param title title of the guide.
     */
    void setTitle(String title);

    /**
     * Returns icon key.
     *
     * @return icon key.
     */
    String getIconKey();

    /**
     * Sets the key of icon associated with this guide.
     *
     * @param iconKey icon key.
     */
    void setIconKey(String iconKey);

    /**
     * Adds guide listener.
     *
     * @param listener listener.
     *
     * @throws NullPointerException if listener isn't specified.
     */
    void addListener(IGuideListener listener);

    /**
     * Removes guide listener.
     *
     * @param listener listener.
     *
     * @throws NullPointerException if listener isn't specified.
     */
    void removeListener(IGuideListener listener);

    /**
     * Returns the read status of this guide. The status depends on the statuses of contained
     * feeds.
     *
     * @return <code>TRUE</code> if the whole guide is read.
     */
    boolean isRead();

    /**
     * Marks whole guide as read/unread depending on the argument. Iterates through all feeds and
     * makes them read/unread.
     *
     * @param read <code>TRUE</code> to mark as read.
     */
    void setRead(boolean read);

    /**
     * Returns TRUE if automatic new articles scanning is enabled.
     *
     * @return TRUE if automatic new articles scanning is enabled.
     */
    boolean isAutoFeedsDiscovery();

    /**
     * Returns all feeds (data and virtual) having the specified XML URL.
     *
     * @param xmlURL            XML URL.
     * @param includeDisabled <code>TRUE</code> to include disabled feeds.
     *
     * @return collection of feeds.
     *
     * @throws NullPointerException if URL is not specified.
     */
    Collection findFeedsByXmlURL(URL xmlURL, boolean includeDisabled);

    /**
     * Sets the value of <code>autoFeedsDiscovery</code> flag. When set all links
     * from new articles in contained feed will be passed to discovery engine
     * automatically.
     *
     * @param value TRUE for auto-discovery.
     */
    void setAutoFeedsDiscovery(boolean value);

    /**
     * Returns ID of the guide. This ID is used by persistence layer to identify
     * record in database.
     *
     * @return ID of the guide.
     */
    long getID();

    /**
     * Sets the ID of the guide.
     *
     * @param id ID of the guide.
     */
    void setID(long id);

    /**
     * Cleans all contained data feeds.
     */
    void clean();

    /**
     * Returns alphabetical index of feed within the guide.
     *
     * @param feed feed to get alpha-index for.
     *
     * @return alphabetical index of feed.
     *
     * @throws NullPointerException  if feed isn't specified.
     */
    int alphaIndexOf(IFeed feed);

    /**
     * Returns the array of all feeds.
     *
     * @return array of feeds.
     */
    IFeed[] getFeeds();

    /**
     * Returns <code>TRUE</code> only if the feed was added directly to this guide.
     *
     * @param feed  feed.
     *
     * @return <code>TRUE</code> only if the feed was added directly to this guide.
     */
    boolean hasDirectLinkWith(IFeed feed);

    /**
     * Returns <code>TRUE</code> if publishing for this guide is enabled.
     *
     * @return <code>TRUE</code> if publishing for this guide is enabled.
     */
    boolean isPublishingEnabled();

    /**
     * Set <code>TRUE</code> to enable publishing of this guide. Note that
     * the publishing may still not work if the publishing title is not set.
     *
     * @param enabled <code>TRUE</code> to enable.
     */
    void setPublishingEnabled(boolean enabled);

    /**
     * Returns the list of tags entered for this guide to be published.
     *
     * @return the list of tags.
     */
    String getPublishingTags();

    /**
     * Sets the tags used when publishing.
     *
     * @param tags tags.
     */
    void setPublishingTags(String tags);

    /**
     * Returns the title of this guide when published.
     *
     * @return the title.
     */
    String getPublishingTitle();

    /**
     * Sets the title used when publishing.
     *
     * @param title the title.
     */
    void setPublishingTitle(String title);

    /**
     * Returns <code>TRUE</code> if the published guide will be visible to everyone.
     *
     * @return <code>TRUE</code> if the published guide will be visible to everyone.
     */
    boolean isPublishingPublic();

    /**
     * Sets the state of publication. When <code>TRUE</code> the guide will be
     * visible to everyone when published.
     *
     * @param flag <code>TRUE</code> to make it public.
     */
    void setPublishingPublic(boolean flag);

    /**
     * Returns the URL which is assigned to the published guide.
     *
     * @return the URL which is assigned to the published guide.
     */
    String getPublishingURL();

    /**
     * Sets the URL which is assigned to the published guide.
     *
     * @param url the URL which is assigned to the published guide.
     */
    void setPublishingURL(String url);

    /**
     * Returns the time of last publishing.
     *
     * @return the time or <code>-1</code> if never happened.
     */
    long getLastPublishingTime();

    /**
     * Sets the time of last publishing.
     *
     * @param time the time.
     */
    void setLastPublishingTime(long time);

    /**
     * Return minimum rating necessary to publish feeds.
     *
     * @return minimum rating.
     */
    int getPublishingRating();

    /**
     * Sets minimum rating necessary to publish feeds.
     *
     * @param rating minimum rating.
     */
    void setPublishingRating(int rating);

    /**
     * Returns the time of last properties update. This time is necessary for
     * the synchronization engine to learn what object is newer.
     *
     * @return the time or <code>-1</code> if not updated yet.
     */
    long getLastUpdateTime();

    /**
     * Sets the time of last properties update. When the user changes some property
     * this time is set automatically. This method is necessary for persistence layer
     * to init the object with what is currently in the database.
     *
     * @param time time.
     */
    void setLastUpdateTime(long time);

    /**
     * Removes every reading list and feed associated with this guide.
     */
    void removeChildren();

    /**
     * Returns <code>TRUE</code> if notifications about new articles and feeds in this guide
     * are enabled.
     *
     * @return <code>TRUE</code> if notifications about new articles and feeds in this guide
     * are enabled.
     */
    boolean isNotificationsAllowed();

    /**
     * Sets the state of notifications flag.
     *
     * @param flag <code>TRUE</code> to enable notifications of changes.
     */
    void setNotificationsAllowed(boolean flag);

    /**
     * Returns the mask of a feed meta-classes.
     *
     * @return mask.
     */
    int getClassesMask();

    /**
     * Returns <code>TRUE</code> if this guide is mobile.
     *
     * @return <code>TRUE</code> if this guide is mobile.
     */
    boolean isMobile();

    /**
     * Sets the mobility state of the guide.
     *
     * @param value <code>TRUE</code> to make it mobile.
     */
    void setMobile(boolean value);
}
