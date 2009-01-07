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
// $Id: SearchAction.java,v 1.5 2007/06/07 09:37:39 spyromus Exp $
//

package com.salas.bb.search;

import com.salas.bb.core.GlobalController;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Search action.
 */
public final class SearchAction extends AbstractAction
{
    private static SearchAction instance;

    /**
     * Creates action.
     */
    private SearchAction()
    {
        // Enabled back by ActionMonitor
        setEnabled(false);
    }

    /**
     * Returns instance of action.
     *
     * @return instance.
     */
    public synchronized static SearchAction getInstance()
    {
        if (instance == null) instance = new SearchAction();
        return instance;
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e event.
     */
    public void actionPerformed(ActionEvent e)
    {
        SearchListener listener = new SearchListener();

        GlobalController controller = GlobalController.SINGLETON;
        SearchDialog dialog = new SearchDialog(controller.getMainFrame(),
            controller.getSearchEngine(), listener);

        dialog.open();
    }

    /**
     * Listens to selections and focuses on the selected items.
     */
    private class SearchListener extends AbstractAction
    {
        /**
         * Invoked when user selects something in the dialog.
         *
         * @param e event.
         */
        public void actionPerformed(ActionEvent e)
        {
            ResultsList list = (ResultsList)e.getSource();
            ResultItem item = list.getSelectedItem();
            if (item == null) return;

            Object obj = item.getObject();

            GlobalController controller = GlobalController.SINGLETON;
            if (obj instanceof IGuide)
            {
                IGuide guide = (IGuide)obj;
                controller.selectGuide(guide, false);
            } else if (obj instanceof IFeed)
            {
                IFeed feed = (IFeed)obj;
                selectFeed(feed);
            } else if (obj instanceof IArticle)
            {
                IArticle article = (IArticle)obj;
                controller.selectArticle(article);
            }
        }

        /**
         * Selects feed and the best guide.
         *
         * @param aFeed feed.
         */
        private void selectFeed(IFeed aFeed)
        {
            GlobalController aController = GlobalController.SINGLETON;
            IGuide[] guides = aFeed.getParentGuides();
            if (guides.length != 0)
            {
                aController.selectGuide(GlobalController.chooseBestGuide(guides), false);
                aController.selectFeed(aFeed, true);
            }
        }
    }
}
