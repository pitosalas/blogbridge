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
// $Id: FeedLinkMarkFeedAsReadAction.java,v 1.3 2008/02/15 14:50:25 spyromus Exp $
//

package com.salas.bb.core.actions.feed;

import com.salas.bb.domain.IFeed;

import java.awt.event.ActionEvent;

/**
 * Mark feed as read action for the feed link.
 */
public class FeedLinkMarkFeedAsReadAction extends MarkFeedAsReadAction
{
    private static IFeed feed;

    /**
     * Enable the action upon construction.
     */
    public FeedLinkMarkFeedAsReadAction()
    {
        super();
        setEnabled(true);
    }

    /**
     * Sets the feed.
     *
     * @param feed feed.
     */
    public static void setFeed(IFeed feed)
    {
        FeedLinkMarkFeedAsReadAction.feed = feed;
    }

    /**
     * Returns the list of feeds to mark.
     *
     * @return feeds.
     */
    protected IFeed[] getFeeds()
    {
        return new IFeed[] { feed };
    }

    /**
     * Actual action.
     *
     * @param event original event object.
     */
    protected void doAction(ActionEvent event)
    {
        try
        {
            super.doAction(event);
        } catch (Exception e)
        {
            // Release feed after job is done
            feed = null;
        }
    }
}
