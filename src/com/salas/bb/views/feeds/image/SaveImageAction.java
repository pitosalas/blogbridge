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
// $Id: SaveImageAction.java,v 1.5 2007/05/30 10:16:08 spyromus Exp $
//

package com.salas.bb.views.feeds.image;

import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.IArticle;
import com.salas.bb.utils.net.Downloader;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * Saves image from selected article. It reuses the same article interpreter
 * as image feed display does to find the link to image which is displayed
 * in the article display.
 */
public class SaveImageAction extends AbstractAction
{
    private static SaveImageAction instance;

    /** Hidden singleton constructor. */
    private SaveImageAction()
    {
        setEnabled(false);
    }

    /**
     * Returns instance.
     *
     * @return instance.
     */
    public static synchronized SaveImageAction getInstance()
    {
        if (instance == null) instance = new SaveImageAction();
        return instance;
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e action object.
     */
    public void actionPerformed(ActionEvent e)
    {
        IArticle[] articles = GlobalModel.SINGLETON.getSelectedArticles();
        if (articles == null) return;

        for (IArticle article : articles)
        {
            URL imageURL = ImageArticleInterpreter.getImageURL(article);
            if (imageURL != null) Downloader.saveResource(imageURL, false);
        }
    }
}
