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
// $Id: FeedDisplayAdapter.java,v 1.12 2008/02/28 15:59:52 spyromus Exp $
//
package com.salas.bb.views;

import com.jgoodies.uif.action.ActionManager;
import com.salas.bb.core.ArticleMarker;
import com.salas.bb.core.ControllerAdapter;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.core.actions.ActionsTable;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.utils.BrowserLauncher;
import com.salas.bb.views.feeds.IFeedDisplay;
import com.salas.bb.views.feeds.IFeedDisplayListener;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.net.URL;

/**
 * Adapts messages from outer world to the feeds display.
 */
public final class FeedDisplayAdapter extends ControllerAdapter
{
    private final IFeedDisplay display;

    /**
     * Constructs new adapter which will be sending commands to the specified view.
     *
     * @param aDisplay view to pass events to.
     */
    public FeedDisplayAdapter(IFeedDisplay aDisplay)
    {
        display = aDisplay;
        display.addListener(new FeedDisplayListener());

        GlobalModel model = GlobalModel.SINGLETON;
        if (model != null)
        {
            UserPreferences prefs = model.getUserPreferences();
            initMarker(aDisplay, prefs);
        }
    }

    /**
     * Initializes marker of articles for the view.
     *
     * @param aDisplay  view object.
     * @param prefs user preferences.
     */
    private void initMarker(IFeedDisplay aDisplay, UserPreferences prefs)
    {
        // Init listener
        ArticleMarker am = ArticleMarker.getInstance();
        aDisplay.addListener(am.getFeedViewListener());

        PropertyChangeListener l = am.getPreferencesListener(
                UserPreferences.KEY_MARK_READ_AFTER_DELAY,
                UserPreferences.KEY_MARK_READ_AFTER_SECONDS);

        prefs.addPropertyChangeListener(UserPreferences.KEY_MARK_READ_AFTER_DELAY, l);
        prefs.addPropertyChangeListener(UserPreferences.KEY_MARK_READ_AFTER_SECONDS, l);

        // Init with current values
        am.setIntervalMarkingEnabled(prefs.isMarkReadAfterDelay());
        am.setMarkInterval(prefs.getMarkReadAfterSeconds());
    }

    /**
     * Invoked after application changes the feed.
     *
     * @param feed feed to which we are switching.
     */
    public void feedSelected(final IFeed feed)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                display.setFeed(feed);
            }
        });
    }

    /**
     * Invoked when article selected / deselected.
     *
     * @param article article selected or NULL if deselected.
     */
    public void articleSelected(final IArticle article)
    {
        if (display.isArticleSelectionSource()) return;

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                display.selectArticle(article);
            }
        });
    }

    /**
     * Listens to events from the view and resends them where necessary.
     */
    private class FeedDisplayListener implements IFeedDisplayListener
    {
        /**
         * Invoked when user selects article or article is selected as result of direct invocation of
         * {@link com.salas.bb.views.feeds.IFeedDisplay#selectArticle(com.salas.bb.domain.IArticle)}
         * method.
         *
         * @param lead              lead article.
         * @param selectedArticles  all selected articles.
         */
        public void articleSelected(IArticle lead, IArticle[] selectedArticles)
        {
            GlobalModel.SINGLETON.setSelectedArticles(selectedArticles);
            GlobalModel.SINGLETON.setSelectedArticle(lead);
            GlobalController.SINGLETON.fireArticleSelected(lead);
        }

        /**
         * Invoked when user hovers some link with mouse pointer.
         *
         * @param link link hovered or <code>NULL</code> if previously hovered link is no longer
         *             hovered.
         */
        public void linkHovered(URL link)
        {
            GlobalController.SINGLETON.setHoveredHyperLink(link);
        }

        /**
         * Invoked when user clicks on some link at the article text or header. The expected
         * behaviour is openning the link in browser.
         *
         * @param link link clicked.
         */
        public void linkClicked(URL link)
        {
            BrowserLauncher.showDocument(link,
                GlobalModel.SINGLETON.getUserPreferences().getInternetBrowser());
        }

        /**
         * Invoked when user clicks on some quick-link to the other feed.
         *
         * @param feed feed to select.
         */
        public void feedJumpLinkClicked(IFeed feed)
        {
            if (feed != null)
            {
                IGuide selectedGuide = GlobalModel.SINGLETON.getSelectedGuide();
                if (!feed.belongsTo(selectedGuide))
                {
                    IGuide[] guides = feed.getParentGuides();
                    if (guides.length > 0) GlobalController.SINGLETON.selectGuide(guides[0], false);
                }
            }

            GlobalController.SINGLETON.selectFeed(feed, true);
        }

        /** Invoked when the user made something to zoom content in. */
        public void onZoomIn()
        {
            ActionManager.get(ActionsTable.CMD_ARTICLE_FONT_BIGGER).actionPerformed(null);
        }

        /** Invoked when the user made something to zoom the content out. */
        public void onZoomOut()
        {
            ActionManager.get(ActionsTable.CMD_ARTICLE_FONT_SMALLER).actionPerformed(null);
        }
    }
}
