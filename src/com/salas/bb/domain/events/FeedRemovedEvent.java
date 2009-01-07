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
// $Id: FeedRemovedEvent.java,v 1.2 2006/01/08 04:48:16 kyank Exp $
//

package com.salas.bb.domain.events;

import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;

import java.util.EventObject;

/**
 * <p>This event is being fired by guide when some feed gets removed. It holds information
 * about feed object which has left the guide and its previous index at the times when
 * it was part of that guide.</p>
 *
 * <p>In addition there's a flag which is <code>TRUE</code> when it's the last event fired
 * in the removal batch.</p>
 */
public class FeedRemovedEvent extends EventObject
{
    private IFeed   feed;
    private int     index;
    private boolean lastEvent;

    /**
     * Creates event.
     *
     * @param aGuide        source guide.
     * @param aFeed         removed feed.
     * @param aIndex        index of the feed at past times.
     * @param aLastEvent    <code>TRUE</code> if this event is last in the batch.
     */
    public FeedRemovedEvent(IGuide aGuide, IFeed aFeed, int aIndex, boolean aLastEvent)
    {
        super(aGuide);
        feed = aFeed;
        index = aIndex;
        lastEvent = aLastEvent;
    }

    /**
     * Returns source guide.
     *
     * @return source guide.
     */
    public IGuide getGuide()
    {
        return (IGuide)getSource();
    }

    /**
     * Returns removed feed.
     *
     * @return feed.
     */
    public IFeed getFeed()
    {
        return feed;
    }

    /**
     * Returns past index of the feed in the guide.
     *
     * @return past index.
     */
    public int getIndex()
    {
        return index;
    }

    /**
     * Returns <code>TRUE</code> if it's the last event in the batch.
     *
     * @return <code>TRUE</code> if it's the last event in the batch.
     */
    public boolean isLastEvent()
    {
        return lastEvent;
    }
}
