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
// $Id: FeedLinkShowFeedPropertiesAction.java,v 1.1 2008/02/15 14:50:25 spyromus Exp $
//

package com.salas.bb.core.actions.feed;

import com.salas.bb.domain.IFeed;

import java.awt.event.ActionEvent;

/**
 * Shows feed properties for a feed link.
 */
public class FeedLinkShowFeedPropertiesAction extends ShowFeedPropertiesAction
{
    private static IFeed feed;

    /**
     * Enable action after construction.
     */
    public FeedLinkShowFeedPropertiesAction()
    {
        super();
        setEnabled(true);
    }

    /**
     * Sets a feed.
     *
     * @param feed feed.
     */
    public static void setFeed(IFeed feed)
    {
        FeedLinkShowFeedPropertiesAction.feed = feed;
    }

    /**
     * Returns a feed to show the dialog for.
     *
     * @return feed.
     */
    protected IFeed getFeed()
    {
        return feed;
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
        } finally
        {
            // Reset a feed after action
            feed = null;
        }
    }
}
