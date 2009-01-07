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

package com.salas.bb.core.actions.article;

import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.utils.CommonUtils;
import com.salas.bb.utils.BrowserLauncher;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.text.MessageFormat;

/**
 * Sends the link of the article to someone using default browser.
 */
public final class ArticleLinkSendAction extends AbstractAction
{
    private static ArticleLinkSendAction instance;

    /**
     * Hidden singleton constructor.
     */
    private ArticleLinkSendAction()
    {
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized ArticleLinkSendAction getInstance()
    {
        if (instance == null) instance = new ArticleLinkSendAction();
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
        IArticle article = model.getSelectedArticle();
        URL url = article.getLink();

        IFeed feed = article.getFeed();
        String subject = MessageFormat.format(Strings.message("article.link.email.subject"),
            new Object[] { article.getTitle(), feed.getTitle() });

        UserPreferences preferences = model.getUserPreferences();
        BrowserLauncher.emailThis(null, subject, url.toString(), preferences.getInternetBrowser());
    }
}
