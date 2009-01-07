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
// $Id: AddSmartFeedAction.java,v 1.21 2007/05/01 16:13:41 spyromus Exp $
//

package com.salas.bb.core.actions.feed;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.actions.IToolbarCommandAction;
import com.salas.bb.dialogs.AddSmartFeedDialog;
import com.salas.bb.domain.DataFeed;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.QueryFeed;
import com.salas.bb.domain.SearchFeed;
import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.utils.uif.IconSource;
import com.salas.bb.views.mainframe.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Adds smart feed.
 *
 * Action is enabled/disabled by <code>ActionsManager</code>.
 */
public class AddSmartFeedAction extends AbstractAction implements IToolbarCommandAction
{
    private static final String RESOURCE_ADDSMARTFEED_TOOLBAR_PRESSEDICON =
        "toolbar.addSmartFeed.pressedicon";

    private static AddSmartFeedAction instance;

    /**
     * Hidden constructor of singleton class.
     */
    protected AddSmartFeedAction()
    {
        setEnabled(false);
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized AddSmartFeedAction getInstance()
    {
        if (instance == null) instance = new AddSmartFeedAction();
        return instance;
    }

    /**
     * Invoked when action occurs.
     *
     * @param e action event details object.
     */
    public void actionPerformed(ActionEvent e)
    {
        if (GlobalController.SINGLETON.checkForNewSubscription()) return;

        GlobalController controller = GlobalController.SINGLETON;
        MainFrame mainFrame = controller.getMainFrame();
        AddSmartFeedDialog dialog = new AddSmartFeedDialog(mainFrame);

        dialog.open(DataFeed.getGlobalPurgeLimit());

        if (!dialog.hasBeenCanceled())
        {
            IFeed feed;
            String title = dialog.getFeedTitle();
            int purgeLimit = dialog.getFeedArticlesLimit();

            int from = dialog.getDedupFrom();
            int to = dialog.getDedupTo();
            boolean isDedup = dialog.isDedupEnabled();

            if (dialog.isQueryFeed())
            {
                int queryType = dialog.getFeedQueryType();
                String parameter = dialog.getFeedParameter();

                QueryFeed qfeed = controller.createQueryFeed(null, title, queryType, parameter, purgeLimit);
                if (qfeed != null) qfeed.setDedupProperties(isDedup, from, to);
                feed = qfeed;
            } else
            {
                Query searchQuery = dialog.getFeedSearchQuery();
                SearchFeed sfeed = controller.createSearchFeed(null, title, searchQuery, purgeLimit);
                if (sfeed != null) sfeed.setDedupProperties(isDedup, from, to, false);
                controller.updateSearchFeed(sfeed);
                
                feed = sfeed;
            }

            if (feed != null)
            {
                feed.setType(dialog.getFeedType());
                feed.setCustomViewModeEnabled(dialog.isCustomViewModeEnabled());
                feed.setCustomViewMode(dialog.getViewMode());

                dialog.commitAutoSaveProperties(feed);
                
                controller.selectFeed(feed, true);
            }
        }
    }

    /**
     * Locates the icon for the pressed state of this command when it lives in a toolbar.
     *
     * @return the icon.
     */
    public Icon getPressedIcon()
    {
        return IconSource.getIcon(RESOURCE_ADDSMARTFEED_TOOLBAR_PRESSEDICON);
    }
}
