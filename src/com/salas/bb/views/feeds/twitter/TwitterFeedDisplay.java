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

package com.salas.bb.views.feeds.twitter;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.uifextras.util.PopupAdapter;
import com.jgoodies.uif.action.ActionManager;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.NetworkFeed;
import com.salas.bb.views.feeds.AbstractFeedDisplay;
import com.salas.bb.views.feeds.IArticleDisplay;
import com.salas.bb.views.feeds.html.ArticlesGroup;
import com.salas.bb.views.feeds.html.IArticleDisplayConfig;
import com.salas.bb.views.feeds.html.IHTMLFeedDisplayConfig;
import com.salas.bb.views.mainframe.MainFrame;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.actions.article.*;
import com.salas.bb.core.actions.ActionsTable;
import com.salas.bb.twitter.FollowAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import java.net.URL;

/**
 * Twitter feed display.
 */
public class TwitterFeedDisplay extends AbstractFeedDisplay
{
    private static final Logger LOG = Logger.getLogger(TwitterFeedDisplay.class.getName());
    private IHTMLFeedDisplayConfig htmlConfig;

    private PopupAdapter userLinkPopupAdapter;

    /**
     * Abstract view.
     *
     * @param aConfig        display configuration.
     * @param pageModel      page model to update when page changes.
     * @param pageCountModel page model with the number of pages (updated by the FeedDisplayModel).
     */
    public TwitterFeedDisplay(IHTMLFeedDisplayConfig aConfig, ValueModel pageModel, ValueModel pageCountModel)
    {
        super(aConfig, pageModel, pageCountModel);
        htmlConfig = aConfig;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        for (ArticlesGroup group : groups) add(group);

        add(noContentPanel);

        enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
    }

    /**
     * Returns current logger.
     *
     * @return logger object.
     */
    protected Logger getLogger()
    {
        return LOG;
    }

    /**
     * Creates new article display for addition to the display.
     *
     * @param aArticle article to create display for.
     *
     * @return display.
     */
    protected IArticleDisplay createNewArticleDisplay(IArticle aArticle)
    {
        IArticleDisplayConfig articleConfig = htmlConfig.getArticleViewConfig();
        return new TwitterArticleDisplay(aArticle, articleConfig);
    }

    /**
     * Returns the view popup adapter.
     *
     * @return view popup adapter.
     */
    protected MouseListener getViewPopupAdapter()
    {
        return null;
    }

    /**
     * Returns the link popup adapter.
     *
     * @return link popup adapter.
     */
    protected MouseListener getLinkPopupAdapter()
    {
        return getArticleUserLinkPopupAdapter();
    }

    /**
     * Returns popup adapter for article user hyper-links.
     *
     * @return popup adapter.
     */
    public synchronized PopupAdapter getArticleUserLinkPopupAdapter()
    {
        if (userLinkPopupAdapter == null)
        {
            userLinkPopupAdapter = new PopupAdapter()
            {
                protected JPopupMenu buildPopupMenu(MouseEvent anevent)
                {
                    GlobalController controller = GlobalController.SINGLETON;
                    MainFrame frame = controller.getMainFrame();
                    JPopupMenu menu = frame.createNonLockingPopupMenu("User Link");

                    // Set links to the actions as the hovered link will be reset upon
                    // the menu opening as the mouse pointer will move away off the link.
                    URL link = controller.getHoveredHyperLink();
                    FollowAction.setUserURL(link);
//                    HyperLinkOpenAction.setLink(link);

//                    menu.add(ActionManager.get(ActionsTable.CMD_ARTICLE_HYPERLINK_OPEN));
                    menu.add(FollowAction.getInstance());

//                    menu.add("Reply");
//                    menu.add("Follow");

                    return menu;
                }
            };
        }

        return userLinkPopupAdapter;
    }

}
