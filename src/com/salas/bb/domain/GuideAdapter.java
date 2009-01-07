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
// $Id: GuideAdapter.java,v 1.12 2006/09/27 16:04:22 spyromus Exp $
//

package com.salas.bb.domain;

import com.salas.bb.domain.events.FeedRemovedEvent;

/**
 * Simple empty implementation of all listener methods.
 */
public class GuideAdapter implements IGuideListener
{
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
}
