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
// $Id: RatingHighAction.java,v 1.6 2006/01/08 04:42:24 kyank Exp $
//

package com.salas.bb.core.actions.feed;

import com.salas.bb.core.GlobalController;
import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.IFeed;
import com.salas.bb.utils.ThreadedAction;

import java.awt.event.ActionEvent;

/**
 * Assigns top rating to currently selected channel.
 */
public final class RatingHighAction extends ThreadedAction
{
    private static RatingHighAction instance;

    /**
     * Hidden constructor of singleton class.
     */
    private RatingHighAction()
    {
        setEnabled(false);
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized RatingHighAction getInstance()
    {
        if (instance == null) instance = new RatingHighAction();
        return instance;
    }

    /**
     * Actual action.
     *
     * @param event original event object.
     */
    protected void doAction(ActionEvent event)
    {
        setSelectedFeedsRating(DirectFeed.RATING_MAX);
    }

    /**
     * Sets rating to all selected feeds.
     *
     * @param rating rating to set.
     */
    protected static void setSelectedFeedsRating(int rating)
    {
        IFeed[] selectedFeeds = GlobalController.SINGLETON.getSelectedFeeds();
        for (int i = 0; i < selectedFeeds.length; i++)
        {
            IFeed feed = selectedFeeds[i];
            if (feed instanceof DirectFeed)
            {
                ((DirectFeed)feed).setRating(rating);
            }
        }
    }
}
