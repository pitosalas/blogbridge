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
// $Id: DomainAdapter.java,v 1.13 2007/10/04 09:55:07 spyromus Exp $
//

package com.salas.bb.domain.utils;

import com.salas.bb.domain.*;
import com.salas.bb.domain.events.FeedRemovedEvent;

/**
 * Default implementation of <code>IDomainListener</code> having event method empty.
 */
public class DomainAdapter implements IDomainListener
{
    /**
     * Invoked when new guide has been added to the set.
     *
     * @param set           guides set.
     * @param guide         added guide.
     * @param lastInBatch   <code>TRUE</code> when this is the last even in batch.
     */
    public void guideAdded(GuidesSet set, IGuide guide, boolean lastInBatch)
    {
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
    }

    /**
     * Invoked when new feed has been added to the guide.
     *
     * @param guide parent guide.
     * @param feed  added feed.
     */
    public void feedAdded(IGuide guide, IFeed feed)
    {
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
    }

    /**
     * Invoked when the feed has been removed from the guide.
     *
     * @param event feed removal event.
     */
    public void feedRemoved(FeedRemovedEvent event)
    {
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
    }

    /**
     * Invoked when the feed has been removed from the guide.
     *
     * @param guide parent guide.
     * @param feed  removed feed.
     * @param index index of removed feed.
     */
    public void feedRemoved(IGuide guide, IFeed feed, int index)
    {
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
    }

    /**
     * Invoked after new reading list is added to the guide.
     *
     * @param guide source guide.
     * @param list  reading list added.
     */
    public void readingListAdded(IGuide guide, ReadingList list)
    {
    }

    /**
     * Invoked after reading list is removed from the guide.
     *
     * @param guide source guide.
     * @param list  reading list removed.
     */
    public void readingListRemoved(IGuide guide, ReadingList list)
    {
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
    }

    /**
     * Invoked when new article has been added to the feed.
     *
     * @param feed    feed.
     * @param article article.
     */
    public void articleAdded(IFeed feed, IArticle article)
    {
    }

    /**
     * Invoked when a search feed adds some article to its list.
     *
     * @param feed    feed.
     * @param article article.
     */
    public void articleAddedToSearchFeed(SearchFeed feed, IArticle article)
    {
    }

    /**
     * Invoked when the article has been removed from the feed.
     *
     * @param feed    feed.
     * @param article article.
     */
    public void articleRemoved(IFeed feed, IArticle article)
    {
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
    }

    /**
     * Invoked when new feed has been added to the reading list.
     *
     * @param list reading list the feed was added to.
     * @param feed added feed.
     */
    public void feedAdded(ReadingList list, IFeed feed)
    {
    }

    /**
     * Invoked when the feed has been removed from the reading list.
     *
     * @param list reading list the feed was removed from.
     * @param feed removed feed.
     */
    public void feedRemoved(ReadingList list, IFeed feed)
    {
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
    }
}
