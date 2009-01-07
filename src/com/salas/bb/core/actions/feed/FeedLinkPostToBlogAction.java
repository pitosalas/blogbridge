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
// $Id: FeedLinkPostToBlogAction.java,v 1.2 2008/03/31 15:29:15 spyromus Exp $
//

package com.salas.bb.core.actions.feed;

import com.salas.bb.domain.IFeed;
import com.salas.bb.remixfeeds.PostToBlogAction;
import com.salas.bb.remixfeeds.type.FeedType;

import java.awt.event.ActionEvent;

/**
 * Shows post to blog feature for the given feed by the feed link.
 */
public class FeedLinkPostToBlogAction extends PostToBlogAction
{
    private static FeedLinkPostToBlogAction instance;
    private static FeedLinkFeedType feedType = new FeedLinkFeedType();

    /**
     * Creates an action.
     */
    protected FeedLinkPostToBlogAction()
    {
        super(feedType);
    }

    /**
     * Returns the instance.
     *
     * @return instance.
     */
    public static synchronized FeedLinkPostToBlogAction getInstance()
    {
        if (instance == null) instance = new FeedLinkPostToBlogAction();
        return instance;
    }

    /**
     * Updates this instance.
     */
    public static void update()
    {
        if (instance != null) instance.update_();
    }

    /**
     * Sets the feed to work on.
     *
     * @param feed feed.
     */
    public static void setFeed(IFeed feed)
    {
        feedType.feed = feed;
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e event object.
     */
    public void actionPerformed(ActionEvent e)
    {
        try
        {
            super.actionPerformed(e);
        } finally
        {
            // Release the feed after action is finished
            setFeed(null);
        }
    }

    /**
     * Custom feed type that returns our feed.
     */
    private static class FeedLinkFeedType extends FeedType
    {
        private IFeed feed;

        /**
         * Returns the list of feeds to work on.
         *
         * @return feeds.
         */
        protected IFeed[] getFeeds()
        {
            return new IFeed[] { feed };
        }

        /**
         * Says if dynamic template change is supported by this type (with SHIFT-click over the PTB command).
         *
         * @return TRUE if it is.
         */
        public boolean isTemplateChangeSupported()
        {
            return false;
        }
    }
}
