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
// $Id: GotoNextUnreadAction.java,v 1.28 2007/09/19 15:55:01 spyromus Exp $
//

package com.salas.bb.core.actions.article;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.core.IArticleListNavigationListener;
import com.salas.bb.core.actions.IToolbarCommandAction;
import com.salas.bb.domain.IArticle;
import com.salas.bb.utils.uif.IconSource;
import com.salas.bb.views.ArticleListPanel;
import com.salas.bb.views.INavigationModes;
import com.salas.bb.views.feeds.IFeedDisplay;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Moves selection to next unread article if possible.
 *
 * Action is enabled/disabled by <code>ActionsManager</code>.
 */
public class GotoNextUnreadAction extends AbstractAction implements IToolbarCommandAction
{
    private static final String RESOURCE_GOTO_NEXT_UNREAD_TOOLBAR_PRESSEDICON =
        "toolbar.gotonextunread.pressedicon";

    private static GotoNextUnreadAction instance;

    /**
     * Hidden constructor of singleton class.
     */
    protected GotoNextUnreadAction()
    {
        setEnabled(false);
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized GotoNextUnreadAction getInstance()
    {
        if (instance == null) instance = new GotoNextUnreadAction();
        return instance;
    }

    /**
     * Return Icon to be used when this command is on the toolbar.
     *
     * @see IToolbarCommandAction#getPressedIcon()
     */
    public Icon getPressedIcon()
    {
        return IconSource.getIcon(RESOURCE_GOTO_NEXT_UNREAD_TOOLBAR_PRESSEDICON);
    }
    
    /**
     * Invoked when action occurs.
     *
     * @param e action event details object.
     */
    public void actionPerformed(ActionEvent e)
    {
        GlobalController controller = GlobalController.SINGLETON;
        ArticleListPanel articlesListPanel = controller.getMainFrame().getArticlesListPanel();
        IFeedDisplay feedDisplay = articlesListPanel.getFeedView();

        GlobalModel model = GlobalModel.SINGLETON;
        IArticle selectedArticle = model.getSelectedArticle();
        boolean articleWasSelected = feedDisplay.selectNextArticle(INavigationModes.MODE_UNREAD);

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
            IArticleListNavigationListener nav = controller.getNavigationListener();
            nav.nextFeed(INavigationModes.MODE_UNREAD);
        }
    }
}
