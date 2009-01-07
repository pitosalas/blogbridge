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
// $Id: SelectBlogByLinkAction.java,v 1.12 2006/03/14 16:27:51 spyromus Exp $
//

package com.salas.bb.core.actions.article;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.NetworkFeed;
import com.salas.bb.domain.IGuide;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * This action is intended to select feed in list who's URL is currently hovered in the article
 * text.
 */
public final class SelectBlogByLinkAction extends AbstractAction
{
    private static SelectBlogByLinkAction instance;
    private static NetworkFeed hoveredFeed;

    /**
     * Hidden singleton constructor.
     */
    private SelectBlogByLinkAction()
    {
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized SelectBlogByLinkAction getInstance()
    {
        if (instance == null) instance = new SelectBlogByLinkAction();
        return instance;
    }

    /**
     * Invoked when action occurs.
     *
     * @param e action event details object.
     */
    public void actionPerformed(ActionEvent e)
    {
        final GlobalController controller = GlobalController.SINGLETON;

        if (hoveredFeed != null)
        {
            // EDT !!!
            IGuide[] guides = hoveredFeed.getParentGuides();
            if (guides.length != 0)
            {
                controller.selectGuide(GlobalController.chooseBestGuide(guides), false);
                controller.selectFeed(hoveredFeed, true);
            }
        }
    }

    /**
     * Sets the feed which is currently hovered.
     *
     * @param aFeed feed.
     */
    public static void setFeed(NetworkFeed aFeed)
    {
        hoveredFeed = aFeed;

        boolean currentFeed = hoveredFeed == GlobalModel.SINGLETON.getSelectedFeed();
        instance.setEnabled(!currentFeed);
    }
}
