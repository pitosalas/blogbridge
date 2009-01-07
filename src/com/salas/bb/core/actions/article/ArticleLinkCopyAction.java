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
// $Id: ArticleLinkCopyAction.java,v 1.7 2007/05/30 09:59:32 spyromus Exp $
//

package com.salas.bb.core.actions.article;

import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.IArticle;
import com.salas.bb.utils.CommonUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * Copies link of the article into clipboard.
 */
public final class ArticleLinkCopyAction extends AbstractAction
{
    private static ArticleLinkCopyAction instance;

    /**
     * Hidden singleton constructor.
     */
    private ArticleLinkCopyAction()
    {
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized ArticleLinkCopyAction getInstance()
    {
        if (instance == null) instance = new ArticleLinkCopyAction();
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
        if (articles == null || articles.length == 0) return;

        int i = 0;
        URL[] urls = new URL[articles.length];
        for (IArticle article : articles) urls[i++] = article.getLink();

        boolean hrefFormat = model.getUserPreferences().isCopyLinksInHrefFormat();

        CommonUtils.copyURLsToClipboard(urls, hrefFormat);
    }
}
