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
// $Id: StandardGuide.java,v 1.53 2007/11/07 17:16:48 spyromus Exp $
//

package com.salas.bb.domain;

import com.salas.bb.utils.IdentityList;
import com.salas.bb.utils.i18n.Strings;

import java.text.MessageFormat;
import java.util.*;

/**
 * <p>Standard feeds guide. The guide, which has the list of feeds and nothing more.
 * We can add new feeds to this guide and remove them from there.</p>
 *
 * <p>This implementation locks itself when working with feeds list. Every code working
 * with feeds list and counters is required to lock the instance to avoid concurrency
 * problems.</p>
 *
 * <p><strong>Feed Positions</strong></p>
 *
 * <p>We have two separate feed holders: a guide and a reading list. Each reading list
 * belongs to a single guide which makes our life easier. There are several facts we
 * base our work on:</p>
 *
 * <ul>
 *  <li>We sort feeds initially when we finished loading them into the guide, reading
 *      lists and added those reading lists to the guide. After the guide is added to
 *      the model and the model is shown, the feeds will be in correct order.</li>
 *  <li>The only way to change the positions of feeds is manual rearrangement. If some
 *      feed is removed from the guide (or from one of assigned reading lists) we don't
 *      need to recalculate the positions because deleting feeds doesn't affect the order.</li>
 *  <li>We add feeds only to the end of the list. It guaranties that we do minimal
 *      database updates.</li>
 * </ul>
 *
 * <p>All above shows that we:</p>
 *
 * <ul>
 *  <li>Store position information only in the database.</li>
 *  <li>Update position information when a feed is repositioned or inserted.</li>
 *  <li>Sort feeds within the guide during initial loading from the database (once).</li>
 * </ul>
 */
public class StandardGuide extends AbstractGuide
{
    /** The list of feeds added manually to this guide. */
    private final FeedLinkInfoList directFeedLinks;

    /** The combined list of feeds (manual and from reading lists). */
    private final List<IFeed> feeds;
    /** The sorted combined list of feeds. */
    private final List<String> sortedFeeds;
    /** The listener for feed titles changes. */
    private final FeedTitleChangeListener feedTitleChangeListener;

    /** The list of reading lists assigned to this guide. */
    private final List<ReadingList> readingLists;
    /** The listener of changes in the assigned reading lists. */
    private final IReadingListListener readingListsListener;

    /**
     * Creates empty guide.
     */
    public StandardGuide()
    {
        directFeedLinks = new FeedLinkInfoList();

        feeds = new IdentityList<IFeed>();
        sortedFeeds = new ArrayList<String>();
        feedTitleChangeListener = new FeedTitleChangeListener();

        readingLists = new ArrayList<ReadingList>();
        readingListsListener = new ReadingListsListener();
    }

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
    public synchronized IFeed getFeedAt(int index)
    {
        return feeds.get(index);
    }

    /**
     * Returns number of feeds in the guide.
     *
     * @return number of feeds.
     */
    public synchronized int getFeedsCount()
    {
        return feeds.size();
    }

    /**
     * Adds feed to the guide.
     *
     * @param feed feed to add.
     *
     * @throws NullPointerException if feed isn't specified.
     * @throws IllegalStateException if feed is already assigned to some feed.
     */
    public synchronized void add(IFeed feed)
    {
        if (feed == null) throw new NullPointerException(Strings.error("unspecified.feed"));

        if (addDirectFeedLink(feed)) addFeedToList(feed);
    }

    /**
     * Adds feed to the list of feeds. This method fires no events and used mostly as
     * a part of more complex composite actions. Please note that it is DANGEROUS to use
     * this method and should be avoided except as for being part of adding and moving
     * feeds.
     *
     * @param feed  feed to add.
     *
     * @see #removeFeedFromList(IFeed, boolean)
     */
    private void addFeedToList(IFeed feed)
    {
        if (!feeds.contains(feed))
        {
            feeds.add(feed);
            addToSorted(feed.getTitle());
            feed.addListener(feedTitleChangeListener);
            feed.addParentGuide(this);

            fireFeedAdded(feed);
        }
    }

    /**
     * Removes feed from the guide.
     *
     * @param feed feed to remove.
     *
     * @return TRUE if removed.
     *
     * @throws NullPointerException if feed isn't specified.
     */
    public synchronized boolean remove(IFeed feed)
    {
        return remove(feed, true);
    }

    /**
     * Removes feed from the guide.
     *
     * @param feed          feed to remove.
     * @param lastInBatch   <code>TRUE</code> if this removal is last in batch.
     *
     * @return <code>TRUE</code> if removed.
     *
     * @throws NullPointerException if feed isn't specified.
     */
    private boolean remove(IFeed feed, boolean lastInBatch)
    {
        if (feed == null) throw new NullPointerException(Strings.error("unspecified.feed"));

        boolean removed = false;

        if (hasDirectLinkWith(feed))
        {
            boolean canRemove = true;
            if (feed instanceof DirectFeed)
            {
                DirectFeed dfeed = (DirectFeed)feed;
                ReadingList[] lists = dfeed.getReadingLists();
                for (int i = 0; canRemove && i < lists.length; i++)
                {
                    ReadingList list = lists[i];
                    canRemove = !readingLists.contains(list);
                }
            }

            removed = canRemove ? removeFeedFromList(feed, lastInBatch)
                : removeDirectFeedLink(feed);
        }

        return removed;
    }

    /**
     * Removes the feeds in list from this guide one by one.
     *
     * @param feeds feeds to remove.
     */
    public synchronized void remove(IFeed[] feeds)
    {
        for (int i = 0; i < feeds.length; i++)
        {
            IFeed feed = feeds[i];
            remove(feed, i == feeds.length - 1);
        }
    }

    /**
     * Removes feed from the list of feeds. This method makes no listener notifications which
     * makes it very DANGEROUS to use. Please make sure that you fire all necessary events
     * or put the feed back later to maintain the model in consistent state.
     *
     * @param feed          feed to remove.
     * @param lastInBatch   <code>TRUE</code> if it's the last feed in batch removal.
     *
     * @return <code>TRUE</code> if it was removed successfully.
     */
    private boolean removeFeedFromList(IFeed feed, boolean lastInBatch)
    {
        int index = feeds.indexOf(feed);
        boolean removed = feeds.remove(feed);
        feed.removeListener(feedTitleChangeListener);
        removeFromSorted(feed.getTitle());
        feed.removeParentGuide(this);

        removeDirectFeedLink(feed);
        if (removed) fireFeedRemoved(feed, index, lastInBatch);

        return removed;
    }

    /** Removes every reading list and feed associated with this guide. */
    public void removeChildren()
    {
        ReadingList[] lists = getReadingLists();
        for (ReadingList list : lists) remove(list, true);

        super.removeChildren();
    }

    /**
     * Returns index of feed within the guide.
     *
     * @param feed feed to get index for.
     *
     * @return index of feed.
     *
     * @throws NullPointerException  if feed isn't specified.
     * @throws IllegalStateException if feed is assigned to the other guide.
     */
    public synchronized int indexOf(IFeed feed)
    {
        if (feed == null) throw new NullPointerException(Strings.error("unspecified.feed"));

        return feeds.indexOf(feed);
    }

    /**
     * Moves the feed to a different guide or different location within this guide.
     *
     * @param aFeed         feed to move.
     * @param aDestination  destination guide.
     * @param index         new feed index within destination guide.
     *
     * @throws NullPointerException if destination guide isn't specified or
     *                      a feed isn't specified.
     */
    public void moveFeed(IFeed aFeed, StandardGuide aDestination, int index)
    {
        if (aDestination == null)
            throw new NullPointerException(Strings.error("unspecified.destination.guide"));
        if (aFeed == null)
            throw new NullPointerException(Strings.error("unspecified.feed"));

        if (aDestination != this)
        {
            moveFeedToDifferentGuide(aFeed, aDestination);
        } else
        {
            repositionFeed(aFeed, index);
        }
    }

    private synchronized void repositionFeed(IFeed feed, int position)
    {
        int currentPosition = indexOf(feed);
        if (currentPosition != -1)
        {
            feeds.remove(feed);
            feeds.add(position, feed);
            fireFeedRepositioned(feed, currentPosition, position);
        }
    }

    /**
     * Moves feed to the other guide.
     *
     * @param feed  feed to move.
     * @param dest  destination guide.
     */
    private synchronized void moveFeedToDifferentGuide(IFeed feed, StandardGuide dest)
    {
        dest.add(feed);
        remove(feed);
    }

    /**
     * Sets new title of the guide.
     *
     * @param aTitle title.
     */
    public void setTitle(String aTitle)
    {
        super.setTitle(aTitle);

        // When the guide is renamed all previous syncronizations mean nothing
        // to the feeds and reading lists -- we reset their times as if they were
        // newly added.
        setSyncTime(-1, true);
    }

    /**
     * Compares this guide with the other guide object.
     *
     * @param o other guide.
     *
     * @return TRUE if equivalent.
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final StandardGuide that = (StandardGuide)o;

        return feeds.equals(that.feeds);
    }

    /**
     * Returns hash code of this guide object.
     *
     * @return hash code.
     */
    public int hashCode()
    {
        // Reusing hash code of super
        return super.hashCode();
    }

    /**
     * Returns the array of all feeds.
     *
     * @return array of feeds.
     */
    public IFeed[] getFeeds()
    {
        return feeds.toArray(new IFeed[0]);
    }

    // ---------------------------------------------------------------------------------------------
    // Direct linking
    // ---------------------------------------------------------------------------------------------

    /**
     * Removes direct feed link and fires the event if the link was removed.
     *
     * @param feed feed to unlink.
     *
     * @return <code>TRUE</code> if was removed.
     */
    private boolean removeDirectFeedLink(IFeed feed)
    {
        boolean removed;

//        FeedLinkInfo info = directFeedLinks.getInfo(feed);
        if (removed = directFeedLinks.remove(feed))
        {
            // TODO: To modify the feedLinkRemoved event later to contain this (it's necessary for correct deleted feeds management)
            //boolean synched = info.getLastSyncTime();

            fireFeedLinkRemoved(feed);
        }

        return removed;
    }

    /**
     * Adds direct feed link to the list and fires event.
     *
     * @param feed  feed to add.
     *
     * @return <code>TRUE</code> if the feed was added to the list of directly linked feeds.
     */
    private boolean addDirectFeedLink(IFeed feed)
    {
        boolean added = false;

        if (!hasDirectLinkWith(feed))
        {
            directFeedLinks.add(new FeedLinkInfo(feed));
            fireFeedLinkAdded(feed);
            added = true;
        }

        return added;
    }

    /**
     * Returns <code>TRUE</code> only if the feed was added directly to this guide.
     *
     * @param feed feed.
     *
     * @return <code>TRUE</code> only if the feed was added directly to this guide.
     */
    public boolean hasDirectLinkWith(IFeed feed)
    {
        return directFeedLinks.contains(feed);
    }

    /**
     * Returns linking info for the given feed.
     *
     * @param feed feed.
     *
     * @return linking info or <code>NULL</code> if feed has no direct link to the guide.
     */
    public FeedLinkInfo getFeedLinkInfo(IFeed feed)
    {
        return directFeedLinks.getInfo(feed);
    }

    // ---------------------------------------------------------------------------------------------
    // Reading lists
    // ---------------------------------------------------------------------------------------------

    /**
     * Adds reading list to the guide.
     *
     * @param list reading list.
     */
    public void add(ReadingList list)
    {
        if (list == null) return;

        // Add the list
        if (!readingLists.contains(list))
        {
            readingLists.add(list);
            list.setParentGuide(this);
            list.addListener(readingListsListener);

            fireReadingListAdded(list);

            // Add all feeds from the reading list
            DirectFeed[] feeds = list.getFeeds();
            for (DirectFeed feed : feeds) onFeedAddedToReadingList(feed);
        }
    }

    /**
     * Removes reading list and all associated feeds.
     *
     * @param list          reading list.
     * @param removeFeeds   <code>TRUE</code> to remove associated feeds,
     *                      otherwise they will be converted to normal.
     */
    public void remove(ReadingList list, boolean removeFeeds)
    {
        if (list == null) return;

        // Remove the list
        if (readingLists.remove(list))
        {
            DirectFeed[] listFeeds = list.getFeeds();
            for (DirectFeed feed : listFeeds)
            {
                if (!removeFeeds) add(feed);
                list.remove(feed);

                onFeedRemovedFromReadingList(feed);
            }

            list.setParentGuide(null);
            list.removeListener(readingListsListener);
            fireReadingListRemoved(list);
        } else throw new IllegalStateException(MessageFormat.format(
            Strings.error("reading.list.did.not.belong.to.this.guide"),
            list, this));
    }

    /**
     * Returns the list of reading lists associated with this guide.
     *
     * @return the list of reading lists associated with this guide.
     */
    public ReadingList[] getReadingLists()
    {
        return readingLists.toArray(new ReadingList[0]);
    }

    /**
     * Fires event about addition of a reading list.
     *
     * @param list reading list.
     */
    protected void fireReadingListAdded(ReadingList list)
    {
        Iterator iterator = listeners.iterator();
        while (iterator.hasNext())
        {
            IGuideListener listener = (IGuideListener)iterator.next();
            listener.readingListAdded(this, list);
        }
    }

    /**
     * Fires event about removal of a reading list.
     *
     * @param list reading list.
     */
    protected void fireReadingListRemoved(ReadingList list)
    {
        Iterator iterator = listeners.iterator();
        while (iterator.hasNext())
        {
            IGuideListener listener = (IGuideListener)iterator.next();
            listener.readingListRemoved(this, list);
        }
    }

    /**
     * Fires event about feed link property changes.
     *
     * @param info      feed link info.
     * @param prop      property name.
     * @param oldVal    old value.
     * @param newVal    new value.
     */
    private void fireFeedLinkPropertyChanged(FeedLinkInfo info, String prop,
                                             long oldVal, long newVal)
    {
        Iterator iterator = listeners.iterator();
        while (iterator.hasNext())
        {
            IGuideListener listener = (IGuideListener)iterator.next();
            listener.feedLinkPropertyChanged(this, info.getFeed(), prop, oldVal, newVal);
        }
    }


    // ---------------------------------------------------------------------------------------------
    // Feed positioning
    // ---------------------------------------------------------------------------------------------

    /**
     * Initializes positions of feeds using the given map of feeds to their positions.
     *
     * @param feedsToPositions the map of feeds to their positions.
     */
    public void initPositions(final Map feedsToPositions)
    {
        Collections.sort(feeds, new Comparator<IFeed>()
        {
            public int compare(IFeed feed1, IFeed feed2)
            {
                int pos1 = getPosition(feed1);
                int pos2 = getPosition(feed2);

                boolean less;

                if (pos1 == pos2)
                {
                    String title1 = feed1.getTitle();
                    String title2 = feed2.getTitle();

                    less = title1 != null
                        ? (title2 != null && title1.compareTo(title2) == -1)
                        : title2 != null;
                } else less = pos1 < pos2;

                return less ? -1 : 1;
            }

            private int getPosition(IFeed feed)
            {
                Integer pos = (Integer)feedsToPositions.get(feed);
                return pos == null ? Integer.MAX_VALUE : pos;
            }
        });
    }

    // ---------------------------------------------------------------------------------------------
    // Feeds alpha sorting section
    //
    // What we do today to sort feed?
    // We store the titles of all feeds in special array, which is sorted in alphabetical order.
    // When we need to get alphabetical order of the feed we lookup its title in this array
    // and return the index. It allows several feeds with the same title occupy the same index,
    // which is necessary for correct primary/secondary sorting.
    //
    // WHEN NEW FEED ARRIVES we take its title and find the proper place in our sorted titles array.
    // WHEN FEED TITLE CHANGES we remove old title from array and rescan all feeds, adding the
    //     co-titled feeds back. After that we add new title to our array.
    // WHEN FEED IS REMOVED we do exactly what is in the first phase of title change handline.
    // ---------------------------------------------------------------------------------------------

    /**
     * Invoked when title of contained feed changes.
     *
     * @param from  title changed from this value.
     * @param to    title changed to this value.
     */
    private void feedTitleChanged(String from, String to)
    {
        synchronized (sortedFeeds)
        {
            removeFromSorted(from);
            addToSorted(to);
        }
    }

    /**
     * Invoked when new feed arrives and requires to be added to the array of sorted titles.
     *
     * @param feedTitle new feed title.
     */
    private void addToSorted(String feedTitle)
    {
        if (feedTitle == null) return;

        feedTitle = feedTitle.toLowerCase().intern();

        synchronized (sortedFeeds)
        {
            int alphaIndex = Collections.binarySearch(sortedFeeds, feedTitle);
            if (alphaIndex < 0) sortedFeeds.add(-(alphaIndex + 1), feedTitle);
        }
    }

    /**
     * Invoked when we no longer need to store the title for some feed.
     * It doesn't mean that we don't have any other feeds with this title -- we can. To restore
     * their titles we rescan them after removal.
     *
     * @param feedTitle title to remove.
     */
    private void removeFromSorted(String feedTitle)
    {
        if (feedTitle == null) return;

        feedTitle = feedTitle.toLowerCase();

        synchronized (sortedFeeds)
        {
            sortedFeeds.remove(feedTitle);
            IFeed[] feeds = getFeeds();
            for (IFeed feed : feeds) addToSorted(feed.getTitle());
        }
    }

    /**
     * Returns alphabetical index of feed within the guide.
     *
     * @param feed feed to get alpha-index for.
     *
     * @return alphabetical index of feed.
     *
     * @throws NullPointerException  if feed isn't specified.
     * @throws IllegalStateException if feed is assigned to the other guide.
     */
    public int alphaIndexOf(IFeed feed)
    {
        if (feed == null) throw new NullPointerException(Strings.error("unspecified.feed"));

        int index = 0;

        String feedTitle = feed.getTitle();
        if (feedTitle != null)
        {
            feedTitle = feedTitle.toLowerCase();

            synchronized (sortedFeeds)
            {
                index = sortedFeeds.indexOf(feedTitle);
            }
        }

        return index;
    }

    /**
     * Sets massively sync times and makes other housekeeping.
     *
     * @param syncTime time of sync.
     * @param syncOut <code>TRUE</code> if it was sync-out.
     */
    void onSyncCompletion(long syncTime, boolean syncOut)
    {
        setSyncTime(syncTime, syncOut);
    }

    /**
     * Setting sync time of all children.
     *
     * @param syncTime      sync time.
     * @param unconditional <code>TRUE</code> to set time unconditionaly, otherwise only to these
     *                      objects having some sync time already set.
     */
    protected void setSyncTime(long syncTime, boolean unconditional)
    {
        for (ReadingList list : readingLists)
        {
            if (unconditional || list.getLastSyncTime() != -1) list.setLastSyncTime(syncTime);
        }

        for (FeedLinkInfo info : directFeedLinks)
        {
            if (unconditional || info.getLastSyncTime() != -1) info.setLastSyncTime(syncTime);
        }
    }

    /**
     * Invoked when a feed is added to reading list.
     *
     * @param feed feed.
     */
    private void onFeedAddedToReadingList(IFeed feed)
    {
        if (!feeds.contains(feed)) addFeedToList(feed);
    }

    /**
     * Invoked when some feed is removed from associated reading list.
     *
     * @param feed feed.
     */
    private void onFeedRemovedFromReadingList(DirectFeed feed)
    {
        if (!hasDirectLinkWith(feed))
        {
            boolean readingListsContainFeed = false;
            ReadingList[] lists = feed.getReadingLists();
            for (int i = 0; !readingListsContainFeed && i < lists.length; i++)
            {
                readingListsContainFeed = readingLists.contains(lists[i]);
            }

            if (!readingListsContainFeed) removeFeedFromList(feed, true);
        }
    }

    /**
     * Listener of changes in feed title. When title changes the feed should be repositioned
     * int alpha-sorted list.
     */
    private class FeedTitleChangeListener extends FeedAdapter
    {
        /**
         * Called when information in feed changed.
         *
         * @param feed     feed.
         * @param property property of the feed.
         * @param oldValue old property value.
         * @param newValue new property value.
         */
        public void propertyChanged(IFeed feed, String property, Object oldValue, Object newValue)
        {
            if (property.equals(IFeed.PROP_TITLE))
            {
                feedTitleChanged((String)oldValue, (String)newValue);
            }
        }
    }

    /**
     * Monitors the events in reading lists attached to this guide.
     */
    private class ReadingListsListener extends ReadingListAdapter
    {
        /**
         * Invoked when new feed has been added to the reading list.
         *
         * @param list reading list the feed was added to.
         * @param feed added feed.
         */
        public void feedAdded(ReadingList list, IFeed feed)
        {
            onFeedAddedToReadingList(feed);
        }

        /**
         * Invoked when the feed has been removed from the reading list.
         *
         * @param list reading list the feed was removed from.
         * @param feed removed feed.
         */
        public void feedRemoved(ReadingList list, IFeed feed)
        {
            DirectFeed dfeed = (DirectFeed)feed;

            onFeedRemovedFromReadingList(dfeed);
        }
    }

    /**
     * Holder of guide-to-feed linking information.
     */
    public class FeedLinkInfo
    {
        /** The time of last synchronization (saving). */
        public static final String PROP_LAST_SYNC_TIME = "lastSyncTime";

        private final IFeed feed;

        private long lastSyncTime = -1;

        /**
         * Creates link feed information for some feed.
         *
         * @param aFeed feed.
         */
        public FeedLinkInfo(IFeed aFeed)
        {
            feed = aFeed;
        }

        /**
         * Returns linked feed.
         *
         * @return feed.
         */
        public IFeed getFeed()
        {
            return feed;
        }

        /**
         * Returns time of last synchronization.
         *
         * @return time of last synchronization.
         */
        public long getLastSyncTime()
        {
            return lastSyncTime;
        }

        /**
         * Sets time of last synchronization.
         *
         * @param time time of last synchronization.
         */
        public void setLastSyncTime(long time)
        {
            long oldLastSyncTime = lastSyncTime;
            lastSyncTime = time;

            fireFeedLinkPropertyChanged(this, PROP_LAST_SYNC_TIME, oldLastSyncTime, time);
        }
    }

    /**
     * The list of feed linking information holders.
     */
    private static class FeedLinkInfoList extends ArrayList<FeedLinkInfo>
    {
        /**
         * Returns linking info for the feed.
         *
         * @param feed feed.
         *
         * @return info or <code>NULL</code> if the feed is not linked to this guide directly.
         */
        public FeedLinkInfo getInfo(IFeed feed)
        {
            int index = indexOf(feed);
            return index == -1 ? null : get(index);
        }

        /**
         * Searches for the first occurence of the given argument, testing for equality using the
         * <tt>equals</tt> method.
         *
         * @param elem an object.
         *
         * @return the index of the first occurrence of the argument in this list;
         *          returns <tt>-1</tt> if the object is not found.
         *
         * @see Object#equals(Object)
         */
        public int indexOf(Object elem)
        {
            int index = -1;

            if (elem == null)
            {
                index = super.indexOf(elem);
            } else
            {
                for (int i = 0; index == -1 && i < size(); i++)
                {
                    FeedLinkInfo info = get(i);
                    if (elem == info.getFeed()) index = i;
                }
            }

            return index;
        }

        /**
         * Returns the index of the last occurrence of the specified object in this list.
         *
         * @param elem the desired element.
         *
         * @return the index of the last occurrence of the specified object in this list;
         *         returns -1 if the object is not found.
         */
        public int lastIndexOf(Object elem)
        {
            int index = -1;

            if (elem == null)
            {
                index = super.lastIndexOf(elem);
            } else
            {
                for (int i = size() - 1; index == -1 && i >= 0; i--)
                {
                    FeedLinkInfo info = get(i);
                    if (elem == info.getFeed()) index = i;
                }
            }

            return index;
        }

        /**
         * Removes a single instance of the specified element from this
         * collection, if it is present (optional operation).
         *
         * @param feed feed info for which to be removed.
         * @return <tt>true</tt> if the collection contained the specified
         *         element.
         */
        public boolean remove(IFeed feed)
        {
            boolean removed = false;

            int index = indexOf(feed);
            if (index != -1)
            {
                removed = true;
                remove(index);
            }

            return removed;
        }


        /**
         * Returns <tt>true</tt> if this list contains the specified element.
         *
         * @param feed element whose presence in this List is to be tested.
         *
         * @return <code>true</code> if the specified element is present;
         *         <code>false</code> otherwise.
         */
        public boolean contains(IFeed feed)
        {
            return super.contains(feed);
        }
    }
}
