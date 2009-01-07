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
// $Id: ToggleArticleReadAction.java,v 1.6 2007/09/19 15:55:01 spyromus Exp $
//

package com.salas.bb.core.actions.article;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.IArticle;
import com.salas.bb.utils.ThreadedAction;

import java.awt.event.ActionEvent;

/**
 * Marks currently selected article as read/unread.
 */
public final class ToggleArticleReadAction extends ThreadedAction
{
    private static ToggleArticleReadAction instance;

    /**
     * Hidden constructor of singleton class.
     */
    private ToggleArticleReadAction()
    {
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized ToggleArticleReadAction getInstance()
    {
        if (instance == null) instance = new ToggleArticleReadAction();
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

        IArticle selectedArticle = model.getSelectedArticle();
        if (selectedArticle != null)
        {
            boolean newRead = !selectedArticle.isRead();

            GlobalController.readArticles(newRead,
                model.getSelectedGuide(),
                model.getSelectedFeed(),
                model.getSelectedArticles());
        }
    }
}
