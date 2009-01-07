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
// $Id: ShowArticlePropertiesAction.java,v 1.11 2006/01/08 04:42:24 kyank Exp $
//

package com.salas.bb.core.actions.article;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.dialogs.ArticlePropertiesDialog;
import com.salas.bb.utils.ThreadedAction;
import com.salas.bb.domain.IArticle;

import java.awt.event.ActionEvent;

/**
 * Shows properties dialog for currently selected article.
 */
public final class ShowArticlePropertiesAction extends ThreadedAction
{
    private static ShowArticlePropertiesAction instance;

    /**
     * Hidden constructor of singleton class.
     */
    private ShowArticlePropertiesAction()
    {
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized ShowArticlePropertiesAction getInstance()
    {
        if (instance == null) instance = new ShowArticlePropertiesAction();
        return instance;
    }

    /**
     * Invoked before forking the thread.
     *
     * @return <code>TRUE</code> to continue with action.
     */
    protected boolean beforeFork()
    {
        return GlobalModel.SINGLETON.getSelectedArticle() != null;
    }

    /**
     * Actual action.
     *
     * @param event original event object.
     */
    protected void doAction(ActionEvent event)
    {
        final IArticle article = GlobalModel.SINGLETON.getSelectedArticle();
        if (article != null)
        {
            new ArticlePropertiesDialog(GlobalController.SINGLETON.getMainFrame(), article).open();
        }
    }
}
