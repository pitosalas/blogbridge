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
// $Id: BrowseArticleAction.java,v 1.18 2007/09/19 15:55:01 spyromus Exp $
//

package com.salas.bb.core.actions.article;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.core.actions.IToolbarCommandAction;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.utils.BrowserLauncher;
import com.salas.bb.utils.uif.IconSource;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * Opens article in the browser.
 */
public final class BrowseArticleAction extends AbstractAction implements IToolbarCommandAction
{
    private static final String RESOURCE_BROWSE_ARTICLE_PRESSEDICON =
        "toolbar.browsearticle.pressedicon";

    private static BrowseArticleAction instance;

    /**
     * Hidden singleton constructor.
     */
    private BrowseArticleAction()
    {
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized BrowseArticleAction getInstance()
    {
        if (instance == null) instance = new BrowseArticleAction();
        return instance;
    }

    /**
     * Invoked when action occurs.
     *
     * @param e action event details object.
     */
    public void actionPerformed(ActionEvent e)
    {
        GlobalModel model = GlobalModel.SINGLETON;
        IArticle[] articles = model.getSelectedArticles();
        if (articles == null) return;
        
        for (IArticle article : articles)
        {
            URL url = article.getLink();

            if (url != null)
            {
                // Register clickthrough
                IFeed feed = article.getFeed();
                if (feed != null) feed.setClickthroughs(feed.getClickthroughs() + 1);

                final UserPreferences preferences = model.getUserPreferences();
                BrowserLauncher.showDocument(url, preferences.getInternetBrowser());
            }
        }

        // Mark all articles as read
        GlobalController.readArticles(true,
            model.getSelectedGuide(),
            model.getSelectedFeed(),
            articles);
    }

    /**
     * Return Icon to be used when this command is on the toolbar.
     *
     * @see IToolbarCommandAction#getPressedIcon()
     */
    public Icon getPressedIcon()
    {
        return IconSource.getIcon(RESOURCE_BROWSE_ARTICLE_PRESSEDICON);
    }
}
