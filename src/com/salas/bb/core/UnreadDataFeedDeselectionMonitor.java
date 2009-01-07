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
// $Id $
//

package com.salas.bb.core;

import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.DataFeed;

/**
 * Listens to changes in current feed selection and reports when some data feed
 * with unread articles becomes deselected.
 */
public abstract class UnreadDataFeedDeselectionMonitor extends ControllerAdapter
{
    private IFeed selectedFeed = null;

    /**
     * Invoked when new feed gets selected.
     *
     * @param feed new feed selection.
     */
    public void feedSelected(IFeed feed)
    {
        // With this handler we monitor the cases when the user deselects some low-rating
        // feed, and it is supposed to disappear. It's interesting to learn about disappearing
        // only of DataFeed having any unread articles.
        if (selectedFeed != null &&
            (selectedFeed instanceof DataFeed) &&
            (selectedFeed.getUnreadArticlesCount() > 0))
        {
            unreadDataFeedDeselected();
        }

        selectedFeed = feed;
    }

    /**
     * Invoked when unread data feed deselection detected.
     */
    protected abstract void unreadDataFeedDeselected();
}
