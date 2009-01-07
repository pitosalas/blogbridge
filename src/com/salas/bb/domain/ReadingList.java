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
// $Id: ReadingList.java,v 1.16 2007/03/13 11:47:24 spyromus Exp $
//

package com.salas.bb.domain;

import com.salas.bb.utils.i18n.Strings;

import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This object holds information about some reading list -- an external
 * OPML file with the list of feeds.
 *
 * It also has an association with some feeds in the guide it belongs too,
 * indicating their correspondence to this list.
 */
public class ReadingList
{
    private static final Logger LOG = Logger.getLogger(ReadingList.class.getName());

    /** Title property. */
    public static final String PROP_TITLE = "title";
    /** Last poll time property. */
    public static final String PROP_LAST_POLL_TIME = "lastPollTime";
    /** Last update server time property. */
    public static final String PROP_LAST_UPDATE_SERVER_TIME = "lastUpdateServerTime";
    /** Name of the last time of sync property. */
    public static final String PROP_LAST_SYNC_TIME = "lastSyncTime";

    /**
     * Time of initialization. This time is used to detect the necessety for an
     * update when in once-per-run mode.
     */
    private static final long INIT_TIME = System.currentTimeMillis();

    /** Updating list(s) once per run. */
    public static final long PERIOD_ONCE_PER_RUN    = -1;
    /** Never updating list(s) automatically. */
    public static final long PERIOD_NEVER           = 0;
    /** Updating list(s) hourly. */
    public static final long PERIOD_HOURLY          = 60 * 60 * 1000;
    /** Updating list(s) daily. */
    public static final long PERIOD_DAILY           = 24 * 60 * 60 * 1000;

    /** Default period. */
    public static final long DEFAULT_PERIOD = PERIOD_ONCE_PER_RUN;

    /**
     * Global update period. If update period of a list isn't set this value is taken.
     */
    private static long globalUpdatePeriod = ReadingList.DEFAULT_PERIOD;

    /** Identity. */
    private long    id;

    /** Source URL. */
    private final URL url;

    /** Title of a list. */
    private String  title;
    /** Time of last synchronization (either in or out). */
    private long    lastSyncTime;
    /** Local time of last poll (to calculate the next poll time). */
    private long    lastPollTime;
    /** Server time of last update (to ask for changes). */
    private long    lastUpdateServerTime;

    /** A guide which has this list among its associated reading lists. */
    private StandardGuide parentGuide;

    /** A list of an associated feeds. */
    private List<DirectFeed>    feeds;

    /** The list of listeners. */
    private CopyOnWriteArrayList<IReadingListListener> listeners;

    /** Shows <code>TRUE</code> when the list is being updated. */
    private boolean updating;

    /** Missing reading list flag. Over time the reading list may disappear. */
    private boolean missing;

    /**
     * Creates Reading List.
     *
     * @param anURL URL of the source OPML.
     *
     * @throws NullPointerException if URL is not set.
     */
    public ReadingList(URL anURL)
    {
        url = anURL;

        id = -1;
        lastPollTime = -1;
        lastSyncTime = -1;
        lastUpdateServerTime = -1;
        feeds = new CopyOnWriteArrayList<DirectFeed>();
        listeners = new CopyOnWriteArrayList<IReadingListListener>();
        missing = false;
        updating = false;
    }

    /**
     * Returns URL of this reading list.
     *
     * @return URL.
     */
    public URL getURL()
    {
        return url;
    }

    /**
     * Returns ID of the list.
     *
     * @return ID.
     */
    public long getID()
    {
        return id;
    }

    /**
     * Sets ID of the list.
     *
     * @param aId ID.
     */
    public void setID(long aId)
    {
        id = aId;
    }

    /**
     * Returns title of the list.
     *
     * @return title.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Sets new title of the list.
     *
     * @param aTitle title.
     */
    public void setTitle(String aTitle)
    {
        String old = title;
        title = aTitle;

        firePropertyChanged(PROP_TITLE, old, title);
    }

    /**
     * Returns last poll time.
     *
     * @return time of last poll.
     */
    public long getLastPollTime()
    {
        return lastPollTime;
    }

    /**
     * Sets last poll time.
     *
     * @param time time of last poll.
     */
    public void setLastPollTime(long time)
    {
        long old = lastPollTime;
        lastPollTime = time;

        firePropertyChanged(PROP_LAST_POLL_TIME, old, lastPollTime);
    }

    /**
     * Returns last server update time.
     *
     * @return time of last resource update according to a server.
     */
    public long getLastUpdateServerTime()
    {
        return lastUpdateServerTime;
    }

    /**
     * Sets last server update time.
     *
     * @param time time of last resource update according to a server.
     */
    public void setLastUpdateServerTime(long time)
    {
        long old = lastUpdateServerTime;
        lastUpdateServerTime = time;

        firePropertyChanged(PROP_LAST_UPDATE_SERVER_TIME, old, time);
    }

    /**
     * Adds some feed to this reading list.
     *
     * @param feed feed to add.
     */
    public void add(DirectFeed feed)
    {
        if (feed == null) return;

        if (!feeds.contains(feed))
        {
            feed.addReadingList(this);
            feeds.add(feed);
            fireFeedAdded(feed);
        }
    }

    /**
     * Remove association between this reading list and a feed.
     *
     * @param feed feed to remove association with.
     */
    public void remove(DirectFeed feed)
    {
        if (feed == null) return;

        if (feeds.contains(feed))
        {
            feed.removeReadingList(this);
            if (feeds.remove(feed)) fireFeedRemoved(feed);
        } else
        {
            // It's basically not a problem. Sometimes the reading list is removed and its updating
            // is still in progress.
            if (LOG.isLoggable(Level.WARNING)) LOG.warning(MessageFormat.format(
                Strings.error("feed.is.not.associated.with.this.reading.list"),
                feed));
        }
    }

    /**
     * Returns <code>TRUE</code> if reading list has associated feeds.
     *
     * @return <code>TRUE</code> if reading list has associated feeds.
     */
    boolean hasAssociations()
    {
        return feeds.size() > 0;
    }

    /**
     * Returns all associated feeds.
     *
     * @return all associated feeds.
     */
    public DirectFeed[] getFeeds()
    {
        return feeds.toArray(new DirectFeed[0]);
    }

    /**
     * Collects the differences between new list of feeds and what's actually associated
     * with this reading list. If there's a feed on the list but it isn't associated
     * with the local reading list yet, it goes to the list for addition. If in
     * the end of scan we found that some feeds weren't mentioned in the new list of feeds
     * they go to the list for removal.
     *
     * @param newFeeds      array of direct feeds of a remote list.
     * @param addFeeds      the list will be populated with direct feeds to add.
     * @param removeFeeds   the list will be populated with direct feeds to remove.
     */
    public void collectDifferences(DirectFeed[] newFeeds, List<DirectFeed> addFeeds, List<DirectFeed> removeFeeds)
    {
        if (newFeeds == null) return;

        // Convert the new list of feeds into map xmlURL->Object
        Map<String, DirectFeed> newFeedsMap = new HashMap<String, DirectFeed>(newFeeds.length);
        for (DirectFeed feed : newFeeds)
        {
            newFeedsMap.put(feed.getXmlURL().toString(), feed);
        }

        // Put old feeds no longer on the new list to remove-list
        // and remove from the new feeds map those present in both lists
        // to leave only new feeds in that lists after this check
        DirectFeed[] associatedFeeds = getFeeds();
        for (DirectFeed feed : associatedFeeds)
        {
            String existingURL = feed.getXmlURL().toString();
            if (!newFeedsMap.containsKey(existingURL))
            {
                removeFeeds.add(feed);
            } else
            {
                newFeedsMap.remove(existingURL);
            }
        }

        // Everything what's left in this map is new to the local reading list copy
        addFeeds.addAll(newFeedsMap.values());
    }

    /**
     * Sets <code>TRUE</code> when the list is being updated.
     *
     * @param flag <code>TRUE</code> when the list if being updated.
     */
    public void setUpdating(boolean flag)
    {
        updating = flag;
    }

    /**
     * Returns <code>TRUE</code> when the list if being updated at the moment.
     *
     * @return <code>TRUE</code> when the list if being updated at the moment.
     */
    public boolean isUpdating()
    {
        return updating;
    }

    /**
     * Returns <code>TRUE</code> if the list can be updated right now.
     *
     * @return <code>TRUE</code> if the list can be updated right now.
     */
    public boolean isUpdatable()
    {
        boolean updatable = false;

        long period = getGlobalUpdatePeriod();
        if (period == PERIOD_ONCE_PER_RUN)
        {
            updatable = lastPollTime < INIT_TIME;
        } else if (period > PERIOD_NEVER)
        {
            updatable = lastPollTime < (System.currentTimeMillis() - period);
        }

        return updatable;
    }

    /**
     * Returns <code>TRUE</code> when reading list is not found during the last polling.
     *
     * @return <code>TRUE</code> when reading list is not found during the last polling.
     */
    public boolean isMissing()
    {
        return missing;
    }

    /**
     * Sets the value of missing flag.
     *
     * @param missing new value.
     */
    public void setMissing(boolean missing)
    {
        this.missing = missing;
    }

    /**
     * Assign new parent guide.
     *
     * @param guide parent guide.
     */
    public void setParentGuide(StandardGuide guide)
    {
        parentGuide = guide;
    }

    /**
     * Returns assigned parent guide.
     *
     * @return parent guide.
     */
    public StandardGuide getParentGuide()
    {
        return parentGuide;
    }

    /**
     * Returns global update period.
     *
     * @return global update period.
     *
     * @see #PERIOD_ONCE_PER_RUN
     * @see #PERIOD_NEVER
     * @see #PERIOD_DAILY
     * @see #PERIOD_DAILY
     */
    public static long getGlobalUpdatePeriod()
    {
        return globalUpdatePeriod;
    }

    /**
     * Sets new global update period.
     *
     * @param period new period in milliseconds.
     *
     * @see #PERIOD_ONCE_PER_RUN
     * @see #PERIOD_NEVER
     * @see #PERIOD_DAILY
     * @see #PERIOD_DAILY
     */
    public static void setGlobalUpdatePeriod(long period)
    {
        globalUpdatePeriod = period;
    }

    /**
     * Returns the time of last synchronization (sync-in or sync-out).
     *
     * @return time.
     */
    public long getLastSyncTime()
    {
        return lastSyncTime;
    }

    /**
     * Sets the time of last synchronization (sync-in or sync-out).
     *
     * @param time time.
     */
    public void setLastSyncTime(long time)
    {
        long old = lastSyncTime;
        lastSyncTime = time;

        firePropertyChanged(PROP_LAST_SYNC_TIME, old, lastSyncTime);
    }

    /**
     * Compares this list to the other.
     *
     * @param o other list.
     *
     * @return <code>TRUE</code> if equal.
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ReadingList that = (ReadingList)o;

        return url.toString().equals(that.url.toString());
    }

    /**
     * Returns hash code of this list.
     *
     * @return has code.
     */
    public int hashCode()
    {
        return url.toString().hashCode();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds listener.
     *
     * @param l listener.
     */
    public void addListener(IReadingListListener l)
    {
        listeners.add(l);
    }

    /**
     * Removes listener.
     *
     * @param l listener.
     */
    public void removeListener(IReadingListListener l)
    {
        listeners.remove(l);
    }

    /**
     * Fires feed addition event.
     *
     * @param feed feed added.
     */
    public void fireFeedAdded(IFeed feed)
    {
        for (IReadingListListener listener : listeners) listener.feedAdded(this, feed);
    }

    /**
     * Fires feed removal event.
     *
     * @param feed feed removed.
     */
    public void fireFeedRemoved(IFeed feed)
    {
        for (IReadingListListener listener : listeners) listener.feedRemoved(this, feed);
    }

    /**
     * Fires property change event.
     *
     * @param property  property.
     * @param oldValue  old value.
     * @param newValue  new value.
     */
    public void firePropertyChanged(String property, Object oldValue, Object newValue)
    {
        for (IReadingListListener listener : listeners)
            listener.propertyChanged(this, property, oldValue, newValue);
    }

    /**
     * Returns <code>TRUE</code> if the specified feed is on the list.
     *
     * @param aFeed feed.
     *
     * @return <code>TRUE</code> if contains.
     */
    public boolean contains(IFeed aFeed)
    {
        return feeds.contains(aFeed);
    }
}
