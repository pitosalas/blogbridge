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
// $Id: GuidesSet.java,v 1.43 2007/10/04 09:55:08 spyromus Exp $
//

package com.salas.bb.domain;

import com.salas.bb.domain.events.FeedRemovedEvent;
import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.domain.querytypes.QueryType;
import com.salas.bb.utils.IdentityList;
import com.salas.bb.utils.StringComparator;
import com.salas.bb.utils.i18n.Strings;

import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Set of guides. The guides are presented in an order. Each time the guide is added, removed
 * or moved the event is fired to notify the listeners.
 *
 * This implementation is completely thread-safe.
 */
public class GuidesSet
{
    private final FeedsList feedsList;
    private final List<IGuide> guides;
    private final List<IGuidesSetListener> listeners;
    private final GuidesListener guidesListener;

    /**
     * Creates empty guides set.
     */
    public GuidesSet()
    {
        feedsList = new FeedsList();
        guides = new IdentityList<IGuide>();
        listeners = new CopyOnWriteArrayList<IGuidesSetListener>();
        guidesListener = new GuidesListener();
    }

    /**
     * Returns feeds list.
     *
     * @return feeds list.
     */
    public FeedsList getFeedsList()
    {
        return feedsList;
    }

    /**
     * Adds guide to the set.
     *
     * @param guide guide to add.
     *
     * @throws NullPointerException if guide isn't specified.
     */
    public synchronized void add(IGuide guide)
    {
        add(-1, guide);
    }

    /**
     * Adds guide to the set.
     *
     * @param index index of the guide to add at (shifts everything down).
     * @param guide guide to add.
     *
     * @throws NullPointerException if guide isn't specified.
     */
    public synchronized void add(int index, IGuide guide)
    {
        add(index, guide, true);
    }

    /**
     * Adds guide to the set.
     *
     * @param index         index of the guide to add at (shifts everything down).
     * @param guide         guide to add.
     * @param lastInBatch   <code>TRUE</code> if it's the last guide in the batch.
     *
     * @throws NullPointerException if guide isn't specified.
     */
    public synchronized void add(int index, IGuide guide, boolean lastInBatch)
    {
        if (guide == null) throw new NullPointerException(Strings.error("unspecified.guide"));

        if (!guides.contains(guide))
        {
            if (index > -1) guides.add(index, guide); else guides.add(guide);

            guide.addListener(guidesListener);

            // move all feeds from the guide to the feeds list
            int count = guide.getFeedsCount();
            for (int i = 0; i < count; i++)
            {
                feedsList.add(guide.getFeedAt(i));
            }

            fireGuideAdded(guide, lastInBatch);
        }
    }

    /**
     * Removes guide from the set.
     *
     * @param guide guide to remove.
     *
     * @return index of the removed guide in the list.
     *
     * @throws NullPointerException if guide isn't specified.
     */
    public synchronized int remove(IGuide guide)
    {
        if (guide == null) throw new NullPointerException(Strings.error("unspecified.guide"));

        int index = guides.indexOf(guide);
        if (index != -1)
        {
            guide.removeChildren();
            guides.remove(guide);
            guide.removeListener(guidesListener);
            fireGuideRemoved(guide, index);
        }

        return index;
    }

    /**
     * Returns guide at specified position.
     *
     * @param index index of guide.
     *
     * @return guide.
     *
     * @throws IndexOutOfBoundsException if index is out of guides list index range.
     */
    public synchronized IGuide getGuideAt(int index)
    {
        return guides.get(index);
    }

    /**
     * Returns number of guides in the set.
     *
     * @return number of guides.
     */
    public synchronized int getGuidesCount()
    {
        return guides.size();
    }

    /**
     * Returns index of guide in the list.
     *
     * @param guide guide.
     *
     * @return index of guide or (-1) if not in list.
     *
     * @throws NullPointerException if guide isn't specified.
     */
    public synchronized int indexOf(IGuide guide)
    {
        if (guide == null) throw new NullPointerException(Strings.error("unspecified.guide"));

        return guides.indexOf(guide);
    }

    /**
     * Returns the list of standard guides which are currently in the set except
     * the specified guide. If guide is not specified then all standard guides will
     * be returned.
     *
     * @param guide the guide to not to return.
     *
     * @return list of guides.
     */
    public synchronized StandardGuide[] getStandardGuides(StandardGuide guide)
    {
        ArrayList<StandardGuide> list = new ArrayList<StandardGuide>(getGuidesCount());

        for (IGuide iGuide : guides)
        {
            if (iGuide instanceof StandardGuide && iGuide != guide) list.add((StandardGuide)iGuide);
        }

        return list.toArray(new StandardGuide[list.size()]);
    }

    /**
     * Returns the titles of all guides in this set. The set contains no duplicates.
     *
     * @return titles of all guides.
     */
    public synchronized Set<String> getGuidesTitles()
    {
        Set<String> titles = new HashSet<String>(getGuidesCount());

        for (IGuide guide : guides) titles.add(guide.getTitle());

        return titles;
    }

    /**
     * Returns the list of all used icon keys.
     *
     * @return list of used icons.
     */
    public synchronized Set<String> getGuidesIconKeys()
    {
        Set<String> keys = new HashSet<String>();

        for (IGuide guide : guides)
        {
            String iconKey = guide.getIconKey();
            if (iconKey != null) keys.add(iconKey);
        }

        return keys;
    }

    /**
     * Marks all guides as read or unread depending on parameter.
     *
     * @param read TRUE to mark as read.
     */
    public synchronized void setRead(boolean read)
    {
        for (IGuide guide : guides) guide.setRead(read);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns static direct feed by its XML URL.
     *
     * @param xmlUrl URL.
     *
     * @return feed or <code>NULL</code>.
     */
    public DirectFeed findDirectFeed(URL xmlUrl)
    {
        return feedsList.findDirectFeed(xmlUrl);
    }

    /**
     * Returns feed similar to that specified.
     *
     * @param feed feed.
     *
     * @return feed or <code>NULL</code> if not found.
     */
    public IFeed findFeed(IFeed feed)
    {
        IFeed result;

        if (feed instanceof DirectFeed)
        {
            result = findDirectFeed(((DirectFeed)feed).getXmlURL());
        } else if (feed instanceof SearchFeed)
        {
            result = findSearchFeed(((SearchFeed)feed).getQuery());
        } else
        {
            QueryFeed qfeed = (QueryFeed)feed;
            result = findQueryFeed(qfeed.getQueryType(), qfeed.getParameter());
        }

        return result;
    }

    /**
     * Returns query feed by its attributes.
     *
     * @param type      type of the query.
     * @param parameter query parameter.
     *
     * @return feed or <code>NULL</code>.
     */
    public QueryFeed findQueryFeed(QueryType type, String parameter)
    {
        return feedsList.findQueryFeed(type, parameter);
    }

    /**
     * Returns search feed by its query.
     *
     * @param query     query.
     *
     * @return feed or <code>NULL</code>.
     */
    public SearchFeed findSearchFeed(Query query)
    {
        return feedsList.findSearchFeed(query);
    }

    /**
     * Returns the list of guides having the given title.
     *
     * @param title title of the guides.
     *
     * @return collection of the guides.
     *
     * @throws NullPointerException if title isn't specified.
     */
    public synchronized Collection<IGuide> findGuidesByTitle(String title)
    {
        if (title == null) throw new NullPointerException(Strings.error("unspecified.title"));

        List<IGuide> guidesCol = new ArrayList<IGuide>();

        for (IGuide guide : guides)
        {
            if (guide.getTitle().equals(title)) guidesCol.add(guide);
        }

        return guidesCol;
    }

    /**
     * Moves the guide to a new position.
     *
     * @param guide     guide.
     * @param position  new position.
     *
     * @throws IllegalArgumentException if guide does not belong to the set.
     * @throws IndexOutOfBoundsException if index felt out of bounds of guides list.
     * @throws NullPointerException if guide isn't specified.
     */
    public synchronized void relocateGuide(IGuide guide, int position)
    {
        if (guide == null) throw new NullPointerException(Strings.error("unspecified.guide"));

        // Verify that guide belongs to this set
        int oldIndex = guides.indexOf(guide);
        if (oldIndex == -1) throw new IllegalArgumentException(Strings.error("guide.does.not.belong.to.the.set"));

        // Verify new position
        int maxPos = guides.size() - 1;
        if (position < 0 || position > maxPos)
            throw new IndexOutOfBoundsException(MessageFormat.format(
                Strings.error("new.guide.position.is.out.of.range"),
                maxPos));

        // Move
        if (oldIndex != position)
        {
            if (guides.remove(guide))
            {
                guides.add(position, guide);
                fireGuideMoved(guide, oldIndex, position);
            }
        }
    }

    /**
     * Adds listener.
     *
     * @param listener listener.
     *
     * @throws NullPointerException if listener isn't specified.
     */
    public void addListener(IGuidesSetListener listener)
    {
        if (listener == null) throw new NullPointerException(Strings.error("unspecified.listener"));

        if (!listeners.contains(listener)) listeners.add(listener);
    }

    /**
     * Removes listener.
     *
     * @param listener listener.
     *
     * @throws NullPointerException if listener isn't specified.
     */
    public void removeListener(IGuidesSetListener listener)
    {
        if (listener == null) throw new NullPointerException(Strings.error("unspecified.listener"));

        listeners.remove(listener);
    }

    /**
     * Fires <code>guideAdded</code> event.
     *
     * @param guide         guide which has been added.
     * @param lastInBatch   <code>TRUE</code> if it's the last guide in the batch.
     */
    private void fireGuideAdded(IGuide guide, boolean lastInBatch)
    {
        for (IGuidesSetListener listener : listeners) listener.guideAdded(this, guide, lastInBatch);
    }

    /**
     * Fires <code>guideRemoved</code> event.
     *
     * @param guide guide which has been removed.
     * @param index old guide index.
     */
    private void fireGuideRemoved(IGuide guide, int index)
    {
        for (IGuidesSetListener listener : listeners) listener.guideRemoved(this, guide, index);
    }

    /**
     * Fires <code>guideMoved</code> event.
     *
     * @param guide     guide which has been removed.
     * @param oldIndex  old guide index.
     * @param newIndex  new guide index.
     */
    private void fireGuideMoved(IGuide guide, int oldIndex, int newIndex)
    {
        for (IGuidesSetListener listener : listeners) listener.guideMoved(this, guide, oldIndex, newIndex);
    }

    /**
     * Removes all guides from set one-by-one.
     */
    public synchronized void clear()
    {
        int count = getGuidesCount();
        for (int i = count - 1; i >= 0; i--)
        {
            remove(getGuideAt(i));
        }
    }

    /**
     * Performs cleaning of all guides.
     */
    public synchronized void clean()
    {
        for (IGuide guide : guides) guide.clean();
    }


    /**
     * Counts all feeds in all guides.
     *
     * @return number of feeds.
     */
    public synchronized int countFeeds()
    {
        int count = 0;
        int guidesCount = getGuidesCount();

        for (int i = 0; i < guidesCount; i++) count += getGuideAt(i).getFeedsCount();

        return count;
    }

    /**
     * Returns collection of unique XML URL's from all URL-having feeds.
     *
     * @return collection of XML URL's.
     */
    public Collection<URL> getFeedsXmlURLs()
    {
        int count = feedsList.getFeedsCount();
        Set<URL> urls = new TreeSet<URL>(new StringComparator<URL>());

        for (int i = 0; i < count; i++)
        {
            IFeed feed = feedsList.getFeedAt(i);
            if (feed instanceof NetworkFeed)
            {
                URL xmlURL = ((NetworkFeed)feed).getXmlURL();
                if (xmlURL != null) urls.add(xmlURL);
            }
        }

        return urls;
    }

    /**
     * Looks for a guide with the given publishing title. Case-insensitive.
     *
     * @param publishingTitle title to look for.
     *
     * @return the guide or <code>NULL</code> if not found.
     */
    public IGuide getGuideByPublishingTitle(String publishingTitle)
    {
        IGuide guide = null;

        for (int i = 0; guide == null && i < guides.size(); i++)
        {
            IGuide iguide = guides.get(i);
            String title = iguide.getPublishingTitle();
            if (title != null && title.equalsIgnoreCase(publishingTitle)) guide = iguide;
        }

        return guide;
    }

    /**
     * Returns list of feeds from all guides. Please note that only standard
     * guides (non-virtual) are taken in account.
     *
     * @return list of all feeds.
     */
    public List<IFeed> getFeeds()
    {
        // TODO !!! review
        List<IFeed> feedsList = new ArrayList<IFeed>();

        StandardGuide[] allGuides = getStandardGuides(null);
        for (StandardGuide guide : allGuides)
        {
            IFeed[] feeds = guide.getFeeds();
            for (IFeed feed : feeds) if (!feedsList.contains(feed)) feedsList.add(feed);
        }

        return feedsList;
    }

    /**
     * Returns the list of guides.
     *
     * @return guides.
     */
    public synchronized IGuide[] getGuides()
    {
        return guides.toArray(new IGuide[guides.size()]);
    }

    /**
     * Updates the times of all objects in all guides with current time.
     *
     * @param syncOut <code>TRUE</code> if it was sync out.
     */
    private void onSyncCompletion(boolean syncOut)
    {
        long time = System.currentTimeMillis();

        StandardGuide[] allGuides = getStandardGuides(null);
        for (StandardGuide guide : allGuides) guide.onSyncCompletion(time, syncOut);
    }

    /**
     * Invoked on sync-out completion.
     */
    public void onSyncOutCompletion()
    {
        onSyncCompletion(true);
    }

    /**
     * Invoked on sync-in completion.
     */
    public void onSyncInCompletion()
    {
        onSyncCompletion(false);
    }

    /**
     * Counts all published guides.
     *
     * @return number of published guides.
     */
    public synchronized int countPublishedGuides()
    {
        int cnt = 0;

        for (IGuide guide : guides)
        {
            if (guide.isPublishingEnabled()) cnt++;
        }

        return cnt;
    }

    /**
     * Invalidates feed visibility caches of all feeds in this set.
     */
    public void invalidateFeedVisibilityCaches()
    {
        List<IFeed> feeds = feedsList.getFeeds();
        for (IFeed feed : feeds)
        {
            feed.invalidateVisibilityCache();
        }
    }

    /**
     * Replaces one feed in guides and reading lists with the other.
     *
     * @param feed          feed.
     * @param replacement   replacement.
     */
    public static void replaceFeed(IFeed feed, IFeed replacement)
    {
        if (feed instanceof DirectFeed && replacement instanceof DirectFeed)
        {
            DirectFeed dfeeds = (DirectFeed)feed;
            DirectFeed dreplacement = (DirectFeed)replacement;

            ReadingList[] lists = dfeeds.getReadingLists();
            for (ReadingList list : lists)
            {
                list.remove(dfeeds);
                list.add(dreplacement);
            }
        }

        IGuide[] parentGuides = feed.getParentGuides();
        for (IGuide parentGuide : parentGuides)
        {
            if (parentGuide.remove(feed)) parentGuide.add(replacement);
        }
    }

    /**
     * Finds a guide with a given ID.
     *
     * @param id    guide id.
     *
     * @return guide or <code>NULL</code> if not found.
     */
    public StandardGuide findGuideByID(Long id)
    {
        if (id != null)
        {
            for (IGuide g : guides)
            {
                if (g.getID() == id && (g instanceof StandardGuide)) return (StandardGuide)g;
            }
        }

        return null;
    }

    /**
     * Finds a feed by ID.
     *
     * @param id feed ID.
     *
     * @return feed or <code>NULL</code> if not found.
     */
    public IFeed findFeedByID(long id)
    {
        List<IFeed> feeds = getFeedsList().getFeeds();
        for (IFeed feed : feeds)
        {
            if (id == feed.getID()) return feed;
        }

        return null;
    }

    /**
     * Listens to guides.
     */
    private class GuidesListener extends GuideAdapter
    {
        /**
         * Invoked when new feed has been added to the guide.
         *
         * @param guide parent guide.
         * @param feed  added feed.
         */
        public void feedAdded(IGuide guide, IFeed feed)
        {
            // Note that it's not feedLinkAdded because we need to record feeds
            // from reading lists as well

            feedsList.add(feed);
        }

        /**
         * Invoked when the feed has been removed from the guide.
         *
         * @param event feed removal event.
         */
        public void feedRemoved(FeedRemovedEvent event)
        {
            IFeed feed = event.getFeed();

            if (!feed.isDynamic() && feed.getParentGuides().length == 0) feedsList.remove(feed);
        }
    }
}
