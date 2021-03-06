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
// $Id$
//

package com.salas.bb.twitter;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.dialogs.UserPreferencesDialog;
import com.salas.bb.domain.IArticle;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Tweet this action.
 */
public class TweetThisAction extends AbstractTwitterAction
{
    private static TweetThisAction instance;

    /** Creates the action. */
    private TweetThisAction()
    {
        // Hidden singleton constructor
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized TweetThisAction getInstance()
    {
        if (instance == null) instance = new TweetThisAction();
        return instance;
    }

    @Override
    protected boolean canAct()
    {
        // Continue if all is good, or if the BB user account isn't registered --
        // the dialog will explain.
        return super.canAct() || !TwitterFeature.isAvaiable();
    }

    /** Invoked when an action occurs. */
    protected void customAction()
    {
        TweetThisDialog ttd = new TweetThisDialog(GlobalController.SINGLETON.getMainFrame());
        ttd.open("", getSelectedArticleLink());
    }

    /**
     * Returns the selected article link.
     *
     * @return link or NULL.
     */
    private String getSelectedArticleLink()
    {
        IArticle article = GlobalController.SINGLETON.getModel().getSelectedArticle();
        return article == null || article.getLink() == null ? null : article.getLink().toString();
    }
}
