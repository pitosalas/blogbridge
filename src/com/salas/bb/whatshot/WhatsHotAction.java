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
// $Id: WhatsHotAction.java,v 1.2 2007/06/12 13:42:27 spyromus Exp $
//

package com.salas.bb.whatshot;

import com.salas.bb.core.GlobalController;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.IArticle;
import com.salas.bb.search.ResultItem;
import com.salas.bb.search.ResultsList;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * What's Hot action.
 */
public final class WhatsHotAction extends AbstractAction
{
    private static WhatsHotAction instance;

    /**
     * Creates action.
     */
    private WhatsHotAction()
    {
        // Enabled back by ActionMonitor
        setEnabled(false);
    }

    /**
     * Returns instance of action.
     *
     * @return instance.
     */
    public static synchronized WhatsHotAction getInstance()
    {
        if (instance == null) instance = new WhatsHotAction();
        return instance;
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e event.
     */
    public void actionPerformed(ActionEvent e)
    {
        ActionListener listener = new WhatsHotListener();

        GlobalController controller = GlobalController.SINGLETON;
        GuidesSet set = controller.getModel().getGuidesSet();
        Dialog dialog = new Dialog(controller.getMainFrame(), new Engine(set), set, listener);

        dialog.open();
    }

    /**
     * Listens to selections and focuses on the selected items.
     */
    private class WhatsHotListener extends AbstractAction
    {
        /**
         * Invoked when user selects something in the dialog.
         *
         * @param e event.
         */
        public void actionPerformed(ActionEvent e)
        {
            ResultsList list = (ResultsList)e.getSource();
            ResultItem o = list.getSelectedItem();
            if (!(o instanceof HotResultItem)) return;

            HotResultItem item = (HotResultItem)o;
            String link = item.getHotlink();
            IArticle article = item.getArticle();

            GlobalController controller = GlobalController.SINGLETON;
            controller.selectArticle(article, link);
        }
    }
}