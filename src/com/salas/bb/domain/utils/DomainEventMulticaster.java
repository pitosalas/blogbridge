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
// $Id: DomainEventMulticaster.java,v 1.14 2007/10/04 09:55:07 spyromus Exp $
//

package com.salas.bb.domain.utils;

import com.salas.bb.domain.*;
import com.salas.bb.domain.events.FeedRemovedEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Very fast and thread safe multicaster listener.</p>
 *
 * <p>There are several great things about this implementation. First of them is that objects
 * of this type mimic usual listeners by implementing the same interface. The other is
 * that they do not require synchronization when adding / removing other listeners to
 * them or firing the events. It happens because the state is always final and if you
 * have reference to some object of this type then it's guarantied to be the same forever.</p>
 *
 * <p>How it mutates then? When you add or remove some listener (the multicaster of this type
 * or usual listener) the other branch on the tree is created and the whole tree is returned
 * in result. When you remove something the tree gets reorganized and also returned in
 * result.</p>
 *
 * <p>More detailed info may be found in JavaWorld's article
 * <a href="http://www.javaworld.com/javaworld/jw-03-1999/jw-03-toolbox_p.html">
 * The Observer pattern and mysteries of the AWTEventMulticaster</a>.</p>
 */
public class DomainEventMulticaster implements IDomainListener
{
    private static final Logger LOG = Logger.getLogger(DomainEventMulticaster.class.getName());

    protected final IDomainListener first, second;

    /**
     * Creates multicaster.
     *
     * @param aFirst    one listener.
     * @param aSecond   the other listener.
     */
    protected DomainEventMulticaster(IDomainListener aFirst, IDomainListener aSecond)
    {
        first = aFirst;
        second = aSecond;
    }

    /**
     * Adds the first listener to the second listener and returns the sum. If you will
     * give only the one from listeners then it will be returned. If you will specify
     * two listeners they will be added together by multicaster object.
     *
     * @param aFirst    first listener.
     * @param aSecond   second listener.
     *
     * @return multicaster.
     */
    public static IDomainListener add(IDomainListener aFirst, IDomainListener aSecond)
    {
        return aFirst == null
            ? aSecond
            : aSecond == null
                ? aFirst
                : new DomainEventMulticaster(aFirst, aSecond);
    }

    /**
     * Removes listener from the listeners list and returns refreshed listeners list.
     * Please note that the list may be both usual listener or multicaster. If it's usual
     * listener then nothing really happens because we can remove something only from
     * multicaster trees.
     *
     * @param aList     list of listeners (multicaster).
     * @param aListener listener to remove.
     *
     * @return updated multicaster.
     */
    public static IDomainListener remove(IDomainListener aList, IDomainListener aListener)
    {
        if(aList == aListener || aList == null)
        {
            return null;
        } else if (!(aList instanceof DomainEventMulticaster))
        {
            return aList;
        } else return ((DomainEventMulticaster)aList).remove(aListener);
    }

    private IDomainListener remove(IDomainListener aListener)
    {
        if (aListener == first)  return second;
        if (aListener == second)  return first;

        IDomainListener a2 = remove(first, aListener);
        IDomainListener b2 = remove(second, aListener);

        return (a2 == first && b2 == second) ? this : add(a2, b2);
    }

    // ---------------------------------------------------------------------------------------------
    // IDomainListener implementation
    // ---------------------------------------------------------------------------------------------

    /**
     * Invoked when new guide has been added to the set.
     *
     * @param set           guides set.
     * @param guide         added guide.
     * @param lastInBatch   <code>TRUE</code> when this is the last even in batch.
     */
    public void guideAdded(GuidesSet set, IGuide guide, boolean lastInBatch)
    {
        first.guideAdded(set, guide, lastInBatch);
        second.guideAdded(set, guide, lastInBatch);
    }

    /**
     * Invoked when the guide has been moved to a new location in list.
     *
     * @param set      guides set.
     * @param guide    guide which has been removed.
     * @param oldIndex old guide index.
     * @param newIndex new guide index.
     */
    public void guideMoved(GuidesSet set, IGuide guide, int oldIndex, int newIndex)
    {
        first.guideMoved(set, guide, oldIndex, newIndex);
        second.guideMoved(set, guide, oldIndex, newIndex);
    }

    /**
     * Invoked when the guide has been removed from the set.
     *
     * @param set   guides set.
     * @param guide removed guide.
     * @param index old guide index.
     */
    public void guideRemoved(GuidesSet set, IGuide guide, int index)
    {
        first.guideRemoved(set, guide, index);
        second.guideRemoved(set, guide, index);
    }

    /**
     * Invoked when new feed has been added to the guide.
     *
     * @param guide parent guide.
     * @param feed  added feed.
     */
    public void feedAdded(IGuide guide, IFeed feed)
    {
        first.feedAdded(guide, feed);
        second.feedAdded(guide, feed);
    }

    /**
     * Invoked when new feed has been added directly to this guide (not through the Reading List).
     * In fact, it doesn't mean that the feed should appear in the guide if it's already there. This
     * event will be followed by <code>feedAdded</code> event if this is the first addition of this
     * feed (not visible yet) and will not, if the feed is already in the list.
     *
     * @param guide parent guide.
     * @param feed  added feed.
     */
    public void feedLinkAdded(IGuide guide, IFeed feed)
    {
        first.feedLinkAdded(guide, feed);
        second.feedLinkAdded(guide, feed);
    }

    /**
     * Invoked when the feed has been removed from the guide.
     *
     * @param event feed removal event.
     */
    public void feedRemoved(FeedRemovedEvent event)
    {
        first.feedRemoved(event);
        second.feedRemoved(event);
    }

    /**
     * Invoked when the feed has been removed directly from the feed. It has nothing to do with the
     * visual representation of the guide because this feed can still be visible in the guide
     * because of its presence in one or more associated reading lists. This even simply means that
     * there's no direct connection between the guide and the feed.
     *
     * @param guide guide.
     * @param feed removed feed.
     */
    public void feedLinkRemoved(IGuide guide, IFeed feed)
    {
        first.feedLinkRemoved(guide, feed);
        second.feedLinkRemoved(guide, feed);
    }

    /**
     * Invoked when feed link property changes its value.
     *
     * @param guide    source guide.
     * @param feed     feed, who's link property has changed.
     * @param property property name.
     * @param oldValue old value.
     * @param newValue new value.
     */
    public void feedLinkPropertyChanged(StandardGuide guide, IFeed feed, String property,
        long oldValue, long newValue)
    {
        first.feedLinkPropertyChanged(guide, feed, property, oldValue, newValue);
        second.feedLinkPropertyChanged(guide, feed, property, oldValue, newValue);
    }

    /**
     * Invoked after new reading list is added to the guide.
     *
     * @param guide source guide.
     * @param list  reading list added.
     */
    public void readingListAdded(IGuide guide, ReadingList list)
    {
        first.readingListAdded(guide, list);
        second.readingListAdded(guide, list);
    }

    /**
     * Invoked after reading list is removed from the guide.
     *
     * @param guide source guide.
     * @param list  reading list removed.
     */
    public void readingListRemoved(IGuide guide, ReadingList list)
    {
        first.readingListRemoved(guide, list);
        second.readingListRemoved(guide, list);
    }

    /**
     * Invoked when a feed is moved from one position to another.
     *
     * @param guide       source guide.
     * @param feed        feed moved.
     * @param oldPosition old position.
     * @param newPosition new position.
     */
    public void feedRepositioned(IGuide guide, IFeed feed, int oldPosition, int newPosition)
    {
        first.feedRepositioned(guide, feed, oldPosition, newPosition);
        second.feedRepositioned(guide, feed, oldPosition, newPosition);
    }

    /**
     * Invoked when the property of the guide has been changed.
     *
     * @param guide    guide owning the property.
     * @param property property name.
     * @param oldValue old property value.
     * @param newValue new property value.
     */
    public void propertyChanged(IGuide guide, String property, Object oldValue, Object newValue)
    {
        first.propertyChanged(guide, property, oldValue, newValue);
        second.propertyChanged(guide, property, oldValue, newValue);
    }

    /**
     * Invoked when new article has been added to the feed.
     *
     * @param feed    feed.
     * @param article article.
     */
    public void articleAdded(IFeed feed, IArticle article)
    {
        first.articleAdded(feed, article);
        second.articleAdded(feed, article);
    }

    /**
     * Invoked when a search feed adds some article to its list.
     *
     * @param feed    feed.
     * @param article article.
     */
    public void articleAddedToSearchFeed(SearchFeed feed, IArticle article)
    {
        try
        {
            first.articleAddedToSearchFeed(feed, article);
            second.articleAddedToSearchFeed(feed, article);
        } catch (Throwable e)
        {
            // Fallthrough
            LOG.log(Level.WARNING, "RTE", e);
        }
    }

    /**
     * Invoked when the article has been removed from the feed.
     *
     * @param feed    feed.
     * @param article article.
     */
    public void articleRemoved(IFeed feed, IArticle article)
    {
        first.articleRemoved(feed, article);
        second.articleRemoved(feed, article);
    }

    /**
     * Invoked when the property of the feed has been changed.
     *
     * @param feed     feed.
     * @param property property of the feed.
     * @param oldValue old property value.
     * @param newValue new property value.
     */
    public void propertyChanged(IFeed feed, String property, Object oldValue, Object newValue)
    {
        first.propertyChanged(feed, property, oldValue, newValue);
        second.propertyChanged(feed, property, oldValue, newValue);
    }

    /**
     * Invoked when the property of the article has been changed.
     *
     * @param article  article.
     * @param property property of the article.
     * @param oldValue old property value.
     * @param newValue new property value.
     */
    public void propertyChanged(IArticle article, String property, Object oldValue, Object newValue)
    {
        first.propertyChanged(article,  property, oldValue, newValue);
        second.propertyChanged(article,  property, oldValue, newValue);
    }

    /**
     * Invoked when new feed has been added to the reading list.
     *
     * @param list reading list the feed was added to.
     * @param feed added feed.
     */
    public void feedAdded(ReadingList list, IFeed feed)
    {
        first.feedAdded(list, feed);
        second.feedAdded(list, feed);
    }

    /**
     * Invoked when the feed has been removed from the reading list.
     *
     * @param list reading list the feed was removed from.
     * @param feed removed feed.
     */
    public void feedRemoved(ReadingList list, IFeed feed)
    {
        first.feedRemoved(list, feed);
        second.feedRemoved(list, feed);
    }

    /**
     * Invoked when the property of the list has been changed.
     *
     * @param list     list owning the property.
     * @param property property name.
     * @param oldValue old property value.
     * @param newValue new property value.
     */
    public void propertyChanged(ReadingList list, String property, Object oldValue, Object newValue)
    {
        first.propertyChanged(list, property, oldValue, newValue);
        second.propertyChanged(list, property, oldValue, newValue);
    }
}
