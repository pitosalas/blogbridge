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
// $Id: FeedLinkShowFeedTagsAction.java,v 1.1 2008/02/15 15:01:23 spyromus Exp $
//

package com.salas.bb.core.actions.feed;

import com.salas.bb.domain.IFeed;
import com.salas.bb.tags.ShowFeedTagsAction;

import java.awt.event.ActionEvent;

/**
 * Shows feed tags for a feed behind a feed link.
 */
public class FeedLinkShowFeedTagsAction extends ShowFeedTagsAction
{
    private static IFeed feed;

    /**
     * Open feed constructor.
     */
    public FeedLinkShowFeedTagsAction()
    {
        setEnabled(true);
    }

    /**
     * Sets the feed.
     *
     * @param feed feed.
     */
    public static void setFeed(IFeed feed)
    {
        FeedLinkShowFeedTagsAction.feed = feed;
    }

    /**
     * Returns the list of feeds to handle.
     *
     * @return feeds.
     */
    protected IFeed[] getFeeds()
    {
        return new IFeed[] { feed };
    }

    /**
     * Invoke when action occurs.
     *
     * @param e action object.
     */
    public void actionPerformed(ActionEvent e)
    {
        try
        {
            super.actionPerformed(e);
        } finally
        {
            // Clear the feed
            feed = null;
        }
    }
}
