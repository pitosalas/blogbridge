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
// $Id: GotoPreviousUnreadAction.java,v 1.22 2007/09/19 15:55:01 spyromus Exp $
//

package com.salas.bb.core.actions.article;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.core.IArticleListNavigationListener;
import com.salas.bb.domain.IArticle;
import com.salas.bb.views.INavigationModes;
import com.salas.bb.views.feeds.IFeedDisplay;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Moves selection to the previous unread article.
 *
 * Action is enabled/disabled by <code>ActionsManager</code>.
 */
public final class GotoPreviousUnreadAction extends AbstractAction
{
    private static GotoPreviousUnreadAction instance;

    /**
     * Hidden constructor of singleton class.
     */
    private GotoPreviousUnreadAction()
    {
        setEnabled(false);
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized GotoPreviousUnreadAction getInstance()
    {
        if (instance == null) instance = new GotoPreviousUnreadAction();
        return instance;
    }

    /**
     * Invoked when action occurs.
     *
     * @param e action event details object.
     */
    public void actionPerformed(ActionEvent e)
    {
        GlobalController controller = GlobalController.SINGLETON;
        IFeedDisplay feedDisplay = controller.getMainFrame().getArticlesListPanel().getFeedView();

        GlobalModel model = GlobalModel.SINGLETON;
        final IArticle selectedArticle = model.getSelectedArticle();

        boolean articleWasSelected = feedDisplay.selectPreviousArticle(INavigationModes.MODE_UNREAD);

        if (selectedArticle != null)
        {
            // Mark an article as read and update stats
            GlobalController.readArticles(true,
                model.getSelectedGuide(),
                model.getSelectedFeed(),
                selectedArticle);

            // Focus traversal is asynchronous. During the next article selection
            // it was moved to the next article, but when current article will be
            // removed the focus will be forwarded away from the display. We need
            // to get it back.
            feedDisplay.focus();
        }

        if (!articleWasSelected)
        {
            IArticleListNavigationListener nav = GlobalController.SINGLETON.getNavigationListener();
            nav.prevFeed(INavigationModes.MODE_UNREAD);
        }
    }
}
