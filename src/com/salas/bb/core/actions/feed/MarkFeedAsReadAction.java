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
// $Id: MarkFeedAsReadAction.java,v 1.7 2008/02/15 14:25:49 spyromus Exp $
//

package com.salas.bb.core.actions.feed;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.IFeed;
import com.salas.bb.utils.ThreadedAction;

import java.awt.event.ActionEvent;

/**
 * Marks all articles as read in currently selected feeds.
 *
 * Action is enabled/disabled by <code>ActionsManager</code>.
 */
public class MarkFeedAsReadAction extends ThreadedAction
{
    private static MarkFeedAsReadAction instance;

    /**
     * Hidden constructor of singleton class.
     */
    protected MarkFeedAsReadAction()
    {
        setEnabled(false);
    }

    /**
     * Returns initialized instance.
     *
     * @return instance.
     */
    public static synchronized MarkFeedAsReadAction getInstance()
    {
        if (instance == null) instance = new MarkFeedAsReadAction();
        return instance;
    }

    /**
     * Actual action.
     *
     * @param event original event object.
     */
    protected void doAction(ActionEvent event)
    {
        GlobalModel model = GlobalModel.SINGLETON;
        IFeed[] feeds = getFeeds();
        GlobalController.readFeeds(true, model.getSelectedGuide(), feeds);
    }

    /**
     * Returns the list of feeds to mark.
     *
     * @return feeds.
     */
    protected IFeed[] getFeeds()
    {
        return GlobalController.SINGLETON.getSelectedFeeds();
    }
}
