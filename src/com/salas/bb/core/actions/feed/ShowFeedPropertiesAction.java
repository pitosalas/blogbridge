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
// $Id: ShowFeedPropertiesAction.java,v 1.35 2008/02/15 14:50:25 spyromus Exp $
//

package com.salas.bb.core.actions.feed;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.dialogs.DirectFeedPropertiesDialog;
import com.salas.bb.dialogs.QueryFeedPropertiesDialog;
import com.salas.bb.dialogs.SearchFeedPropertiesDialog;
import com.salas.bb.discovery.MDDiscoveryRequest;
import com.salas.bb.domain.*;
import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.domain.querytypes.QueryType;
import com.salas.bb.service.ServerService;
import com.salas.bb.utils.ThreadedAction;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * Shows properties of the feed.
 *
 * Action is enabled/disabled by <code>ActionsManager</code>.
 */
public class ShowFeedPropertiesAction extends ThreadedAction
{
    private static ShowFeedPropertiesAction instance;

    /**
     * Hidden constructor of singleton class.
     */
    protected ShowFeedPropertiesAction()
    {
        setEnabled(false);
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized ShowFeedPropertiesAction getInstance()
    {
        if (instance == null) instance = new ShowFeedPropertiesAction();
        return instance;
    }

    /**
     * Invoked before forking the thread.
     *
     * @return <code>TRUE</code> to continue with action.
     */
    protected boolean beforeFork()
    {
        return GlobalModel.SINGLETON.getSelectedFeed() != null;
    }

    /**
     * Actual action.
     *
     * @param event original event object.
     */
    protected void doAction(ActionEvent event)
    {
        IFeed feed = getFeed();
        if (feed != null)
        {
            if (feed instanceof DirectFeed)
            {
                showDirectFeedProperties((DirectFeed)feed);
            } else if (feed instanceof QueryFeed)
            {
                showQueryFeedProperties((QueryFeed)feed);
            } else if (feed instanceof SearchFeed)
            {
                showSearchFeedProperties((SearchFeed)feed);
            }
        }
    }

    /**
     * Returns a feed to show the dialog for.
     *
     * @return feed.
     */
    protected IFeed getFeed()
    {
        GlobalModel model = GlobalModel.SINGLETON;
        return model.getSelectedFeed();
    }

    /**
     * Shows properties dialog for a given serach feed.
     *
     * @param searchFeed serach feed.
     */
    private void showSearchFeedProperties(final SearchFeed searchFeed)
    {
        GlobalController controller = GlobalController.SINGLETON;
        SearchFeedPropertiesDialog dialog =
            new SearchFeedPropertiesDialog(searchFeed, controller.getMainFrame());

        int articlesLimit = searchFeed.getArticlesLimit();
        String title = searchFeed.getBaseTitle();
        Query query = searchFeed.getQuery();
        boolean dedupEnabled = searchFeed.isDedupEnabled();
        int dedupFrom = searchFeed.getDedupFrom();
        int dedupTo = searchFeed.getDedupTo();

        if (dialog.open(title, query, articlesLimit, dedupEnabled, dedupFrom, dedupTo))
        {
            final String feedTitle = dialog.getFeedTitle();
            final Query feedSearchQuery = dialog.getFeedSearchQuery();
            final int feedArticlesLimit = dialog.getFeedArticlesLimit();

            final boolean feedDedupEnabled = dialog.isDedupEnabled();
            final int feedDedupFrom = dialog.getDedupFrom();
            final int feedDedupTo = dialog.getDedupTo();

            new SetSearchFeedProperties(searchFeed, feedTitle,
                feedSearchQuery, feedArticlesLimit,
                feedDedupEnabled, feedDedupFrom, feedDedupTo).start();
        }
    }

    /**
     * Shows properties dialog for the query feed. The dialog allows to modify some of the
     * fields and after the changes confirmed they will be moved to the feed object.
     *
     * @param feed feed to watch and change.
     */
    private void showQueryFeedProperties(QueryFeed feed)
    {
        GlobalController controller = GlobalController.SINGLETON;
        QueryFeedPropertiesDialog dialog =
            new QueryFeedPropertiesDialog(feed, controller.getMainFrame());

        // Read-in the properties
        String title = feed.getBaseTitle();
        QueryType queryType = feed.getQueryType();
        int purgeLimit = feed.getPurgeLimit();
        String parameter = feed.getParameter();

        boolean dedupEnabled = feed.isDedupEnabled();
        int dedupFrom = feed.getDedupFrom();
        int dedupTo = feed.getDedupTo();

        // Show dialog and check if something has been changed and accepted
        if (queryType == null)
        {
            JOptionPane.showMessageDialog(controller.getMainFrame(),
                Strings.message("show.feed.properties.dialog.text.unsupported.type"),
                Strings.message("show.feed.properties.dialog.title"),
                JOptionPane.INFORMATION_MESSAGE);
        } else if (dialog.open(title, queryType, purgeLimit, parameter, dedupEnabled, dedupFrom, dedupTo))
        {
            // Accepted
            feed.setBaseTitle(dialog.getFeedTitle());
            feed.setPurgeLimit(dialog.getFeedArticlesLimit());
            boolean dedupChanged = feed.setDedupProperties(dialog.isDedupEnabled(),
                dialog.getDedupFrom(), dialog.getDedupTo());
            boolean paramChanged = feed.changeParameter(dialog.getFeedParameter());

            if (paramChanged) controller.getPoller().update(feed, true, true); else
            if (dedupChanged) feed.reviewArticles();
        }
    }

    /**
     * Shows the properties of regular direct feed. By its nature, feed can be in resolved,
     * unresolved and invalid states. Depending on this the dialog will have different
     * fields enabled for modification. When the feed isn't resolved, it's possible to specify
     * (or "suggest") the correct XML URL by hand, when it's resolved it's possible to change
     * some other properties (general fields, like title, description and author, as well as
     * community fields). The changed properties are moved back to the feed object.
     *
     * @param aFeed feed object to change.
     */
    private void showDirectFeedProperties(final DirectFeed aFeed)
    {
        GlobalController controller = GlobalController.SINGLETON;

        boolean badFeed = aFeed.isInvalid() && !aFeed.isInitialized();

        // Record current XML URL
        URL xmlURL = badFeed ? aFeed.getXmlURL() : null;

        // Show dialog
        DirectFeedPropertiesDialog dialog =
            new DirectFeedPropertiesDialog(controller.getMainFrame(), aFeed);

        dialog.open();

        // Compare new XML URL to that
        URL newXmlURL = aFeed.getXmlURL();
        boolean xmlUrlChanged = (xmlURL == null
            ? newXmlURL != null
            : !xmlURL.toString().equals(newXmlURL.toString()));

        // If the feed is bad (is not initialized even once) and URL changed
        // then we remove it and add new feed with the given XML URL
        if (badFeed && xmlUrlChanged)
        {
            aFeed.setXmlURL(null);
            GuidesSet set = controller.getModel().getGuidesSet();
            DirectFeed existingFeed = set.findDirectFeed(newXmlURL);

            if (existingFeed != null)
            {
                GuidesSet.replaceFeed(aFeed, existingFeed);
            } else
            {
                aFeed.setXmlURL(newXmlURL);

                FeedMetaDataHolder holder = aFeed.getMetaDataHolder();

                // XML URL has changed -- suggest XML URL if it's not local
                if (xmlURL != null && !MDDiscoveryRequest.isLocalURL(newXmlURL))
                {
                    ServerService.metaSuggestFeedUrl(xmlURL.toString(), newXmlURL.toString());
                }

                // set newly discovered URL and mark the data as no longer invalid
                holder.setXmlURL(newXmlURL);
                holder.setInvalid(false);

                controller.updateIfDiscovered(aFeed);
            }
        }
    }

    /**
     * Simple thread setting the search feed properties to avoid UI locking.
     */
    private static class SetSearchFeedProperties extends Thread
    {
        private final SearchFeed searchFeed;
        private final String feedTitle;
        private final Query feedSearchQuery;
        private final int feedArticlesLimit;
        private final boolean dedupEnabled;
        private final int dedupFrom;
        private final int dedupTo;

        /**
         * Creates thread.
         *
         * @param aSearchFeed           search feed to update.
         * @param aFeedTitle            new feed title.
         * @param aFeedSearchQuery      new query.
         * @param aFeedArticlesLimit    new articles limit.
         * @param aDedupEnabled         new remove duplicates flag.
         * @param aDedupFrom            new index of the first dedup word.
         * @param aDedupTo              new index of the last dedup word.
         */
        public SetSearchFeedProperties(SearchFeed aSearchFeed, String aFeedTitle,
                                       Query aFeedSearchQuery, int aFeedArticlesLimit,
                                       boolean aDedupEnabled, int aDedupFrom, int aDedupTo)
        {
            searchFeed = aSearchFeed;
            feedTitle = aFeedTitle;
            feedSearchQuery = aFeedSearchQuery;
            feedArticlesLimit = aFeedArticlesLimit;
            dedupEnabled = aDedupEnabled;
            dedupFrom = aDedupFrom;
            dedupTo = aDedupTo;
        }

        /** Invoked during thread run. */
        public void run()
        {
            searchFeed.setBaseTitle(feedTitle);
            searchFeed.setArticlesLimit(feedArticlesLimit);
            searchFeed.setDedupProperties(dedupEnabled, dedupFrom, dedupTo, false);
            searchFeed.setQuery(feedSearchQuery);
        }
    }
}
