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
// $Id: ActionsMonitor.java,v 1.60 2008/04/01 08:20:09 spyromus Exp $
//

package com.salas.bb.core.actions;

import com.salas.bb.core.ControllerAdapter;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.core.actions.article.*;
import com.salas.bb.core.actions.feed.*;
import com.salas.bb.core.actions.guide.*;
import com.salas.bb.discovery.actions.DiscoverInArticlesAction;
import com.salas.bb.discovery.actions.DiscoverInFeedsAction;
import com.salas.bb.domain.*;
import com.salas.bb.remixfeeds.PostToBlogAction;
import com.salas.bb.search.SearchAction;
import com.salas.bb.service.ShowServiceDialogAction;
import com.salas.bb.service.sync.SyncFullAction;
import com.salas.bb.service.sync.SyncInAction;
import com.salas.bb.service.sync.SyncOutAction;
import com.salas.bb.tags.SelectiveShowTagsAction;
import com.salas.bb.tags.ShowArticleTagsAction;
import com.salas.bb.tags.ShowFeedTagsAction;
import com.salas.bb.utils.ConnectionState;
import com.salas.bb.utils.uif.UifUtilities;
import com.salas.bb.views.feeds.image.SaveImageAction;
import com.salas.bb.whatshot.WhatsHotAction;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Listens for events and enables / disables actions.
 */
public final class ActionsMonitor extends ControllerAdapter
{
    private final ConnectionState   connectionState;
    private final IFeedListener     taggingFeedListener;

    private boolean             initFinished;
    private IGuide              selectedGuide;
    private IFeed               selectedFeed;
    private IArticle            selectedArticle;

    /**
     * Creates actions monitor.
     *
     * @param aController       controller.
     * @param aConnectionState  connection state interface.
     */
    private ActionsMonitor(GlobalController aController, ConnectionState aConnectionState)
    {
        connectionState = aConnectionState;
        taggingFeedListener = new TaggingFeedListener();

        aController.addControllerListener(this);
        if (aController.isInitializationFinished()) initializationFinished();

        connectionState.addPropertyChangeListener(new ConnectionStateListener());

        GlobalModel model = aController.getModel();
        guideSelected(model == null ? null : model.getSelectedGuide());
        feedSelected(model == null ? null : model.getSelectedFeed());
        articleSelected(model == null ? null : model.getSelectedArticle());
    }

    /**
     * Starts monitoring the application state.
     *
     * @param aController       controller.
     * @param aConnectionState  connection state interface.
     */
    public static void start(GlobalController aController, ConnectionState aConnectionState)
    {
        new ActionsMonitor(aController, aConnectionState);
    }

    /**
     * Invoked after application changes the guide.
     *
     * @param guide guide to with we have switched.
     */
    public synchronized void guideSelected(IGuide guide)
    {
        selectedGuide = guide;
        if (UifUtilities.isEDT())
        {
            updateGuideActions(selectedGuide, initFinished);
        } else
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    updateGuideActions(selectedGuide, initFinished);
                }
            });
        }
    }

    /**
     * Invoked when article selected / deselected.
     *
     * @param article article selected or NULL if deselected.
     */
    public synchronized void articleSelected(IArticle article)
    {
        selectedArticle = article;
        if (UifUtilities.isEDT())
        {
            updateArticleActions(selectedArticle, initFinished);
        } else
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    updateArticleActions(selectedArticle, initFinished);
                }
            });
        }
    }

    /**
     * Invoked after application changes the feed.
     *
     * @param feed feed to which we have switched.
     */
    public void feedSelected(IFeed feed)
    {
        if (selectedFeed != null) selectedFeed.removeListener(taggingFeedListener);
        selectedFeed = feed;
        if (selectedFeed != null) selectedFeed.addListener(taggingFeedListener);

        if (UifUtilities.isEDT())
        {
            updateFeedActions(selectedFeed, initFinished);
        } else
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    updateFeedActions(selectedFeed, initFinished);
                }
            });
        }
    }

    /**
     * Invoked after application finishes initialization of data.
     */
    public synchronized void initializationFinished()
    {
        initFinished = true;

        if (UifUtilities.isEDT())
        {
            initFinished0();
        } else
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    initFinished0();
                }
            });
        }
    }

    /**
     * Called when initialization is reported to be finished.
     */
    private void initFinished0()
    {
        updateGuideActions(selectedGuide, initFinished);
        updateFeedActions(selectedFeed, initFinished);

        // Enabled if initialization finished
        AddGuideAction.getInstance().setEnabled(true);
        AddDirectFeedAction.getInstance().setEnabled(true);
        AddSmartFeedAction.getInstance().setEnabled(true);

        ImportGuidesAction.getInstance().setEnabled(true);

        SortGuidesByTitleAction.getInstance().setEnabled(true);
        SearchAction.getInstance().setEnabled(true);
        WhatsHotAction.getInstance().setEnabled(true);

        DatabaseCompactAction.getInstance().setEnabled(true);
        DatabaseBackupAction.getInstance().setEnabled(true);
    }

    /**
     * Updates guides actions' enable state.
     *
     * @param aSelectedGuide    guide, which is currently selected.
     * @param aInitFinished     <code>TRUE</code> when initialization finished.
     */
    private void updateGuideActions(IGuide aSelectedGuide, boolean aInitFinished)
    {
        final boolean b = aSelectedGuide != null && aInitFinished;
        final GuidesSet cgs = GlobalModel.SINGLETON.getGuidesSet();
        final int index = b ? cgs.indexOf(aSelectedGuide) : -1;
        final int size = cgs.getGuidesCount();
        final boolean hasChannels = b && aSelectedGuide.getFeedsCount() > 0;

        // Enable actions below when guide selected
        GuidePropertiesAction.getInstance().setEnabled(b);
        DeleteGuideAction.getInstance().setEnabled(b);
        UpdateGuideAction.getInstance().setEnabled(b);
        UpdateAllGuidesAction.getInstance().setEnabled(size > 0);
        MarkGuideReadAction.getInstance().setEnabled(hasChannels);
        MarkGuideUnreadAction.getInstance().setEnabled(hasChannels);
        MarkAllGuidesReadAction.getInstance().setEnabled(size > 0);
        MarkAllGuidesUnreadAction.getInstance().setEnabled(size > 0);

        // Enable merging of guides if there's more than one guide
        MergeGuidesAction.getInstance().setEnabled(b && size > 1);

        // Enable actions below when guides are present
        final boolean hasGuides = aInitFinished && (size > 0);
        GotoNextArticleAction.getInstance().setEnabled(hasGuides);
        GotoNextUnreadAction.getInstance().setEnabled(hasGuides);
        GotoNextUnreadInNextFeedAction.getInstance().setEnabled(hasGuides);
        GotoPreviousArticleAction.getInstance().setEnabled(hasGuides);
        GotoPreviousUnreadAction.getInstance().setEnabled(hasGuides);
        ExportGuidesAction.getInstance().setEnabled(hasGuides);
        GotoNextGuideWithUnreadAction.getInstance().setEnabled(hasGuides);

        PostToBlogAction.update();
        FeedLinkPostToBlogAction.update();
    }

    /**
     * Updates feeds actions' enable state.
     *
     * @param aSelectedFeed selected feed.
     * @param aInitFinished <code>TRUE</code> if initialization has already finished.
     */
    private void updateFeedActions(IFeed aSelectedFeed, boolean aInitFinished)
    {
        // Enable actions below only when channel selected
        final boolean b = aSelectedFeed != null;
        final boolean bInited = b && aInitFinished;

        MarkFeedAsReadAction.getInstance().setEnabled(bInited);
        MarkFeedAsUnreadAction.getInstance().setEnabled(bInited);
        DeleteFeedAction.getInstance().setEnabled(bInited);
        ShowFeedPropertiesAction.getInstance().setEnabled(bInited);
        RatingLowAction.getInstance().setEnabled(bInited);
        RatingHighAction.getInstance().setEnabled(bInited);
        UpdateSelectedFeedsAction.getInstance().setEnabled(bInited);
        DiscoverInFeedsAction.getInstance().setEnabled(bInited);
        PostToBlogAction.update();
        FeedLinkPostToBlogAction.update();

        // Enables opening of home page in browser only if
        // URL is specified.
        boolean isDirect = aSelectedFeed instanceof DirectFeed;
        OpenBlogHomeAction.getInstance().setEnabled(b && isDirect &&
            ((DirectFeed)aSelectedFeed).getSiteURL() != null);

        updateTaggingActions();

        // Updating title of Delete command accroding to the state and nature of a selected feed
        int mode = DeleteFeedAction.DELETE;
        if (aSelectedFeed != null && (aSelectedFeed instanceof DirectFeed) &&
            selectedGuide != null &&
            !selectedGuide.hasDirectLinkWith(aSelectedFeed))
        {
            mode = ((DirectFeed)aSelectedFeed).isDisabled()
                ? DeleteFeedAction.ENABLE : DeleteFeedAction.DISABLE;
        }

        DeleteFeedAction.getInstance().setMode(mode);
    }

    /**
     * Updates articles actions' enable state.
     *
     * @param aSelectedArticle  selected article.
     * @param aInitFinished     <code>TRUE</code> if initialization has already finished.
     */
    private void updateArticleActions(IArticle aSelectedArticle, boolean aInitFinished)
    {
        final boolean b = aSelectedArticle != null;
        final boolean bInited = b && aInitFinished;

        ShowArticlePropertiesAction.getInstance().setEnabled(b);

        final boolean articleUrlPresent = (b && aSelectedArticle.getLink() != null);
        BrowseArticleAction.getInstance().setEnabled(articleUrlPresent);
        ArticleLinkCopyAction.getInstance().setEnabled(articleUrlPresent);
        ArticleLinkSendAction.getInstance().setEnabled(articleUrlPresent);

        MarkArticleReadAction.getInstance().setEnabled(bInited);
        MarkArticleUnreadAction.getInstance().setEnabled(bInited);

        PinUnpinArticleAction.getInstance().setEnabled(bInited);
        PostToBlogAction.update();
        FeedLinkPostToBlogAction.update();
        DiscoverInArticlesAction.getInstance().setEnabled(bInited);

        SaveImageAction.getInstance().setEnabled(b);

        updateTaggingActions();
    }

    /**
     * Updates the state of all tagging actions.
     */
    private void updateTaggingActions()
    {
        boolean taggableArticle = selectedArticle != null &&
            (selectedArticle instanceof StandardArticle) &&
            ((StandardArticle)selectedArticle).getTaggableLink() != null;

        boolean taggableFeed = selectedFeed != null &&
            (selectedFeed instanceof DirectFeed) &&
            ((DirectFeed)selectedFeed).getTaggableLink() != null;

        ShowArticleTagsAction.getInstance().setEnabled(taggableArticle);
        ShowFeedTagsAction.getInstance().setEnabled(taggableFeed);
        SelectiveShowTagsAction.getInstance().setEnabled(taggableFeed || taggableArticle);
    }

    /**
     * Called when connection gets online-offline.
     *
     * @param isOnline <code>TRUE</code> if currently online.
     */
    private void onConnectionChange(boolean isOnline)
    {
    }

    /**
     * Called when the service becomes available or not available.
     *
     * @param isServiceAccessible <code>TRUE</code> if the service is available.
     */
    private void onServiceAvailabilityChange(final boolean isServiceAccessible)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                CheckForUpdatesAction.getInstance().setEnabled(isServiceAccessible);
                SendFeedbackAction.getInstance().setEnabled(isServiceAccessible);
                ShowServiceDialogAction.getInstance().setEnabled(isServiceAccessible);

                // Synchronization actions
                SyncInAction.getInstance().setEnabled(isServiceAccessible);
                SyncOutAction.getInstance().setEnabled(isServiceAccessible);
                SyncFullAction.getInstance().setEnabled(isServiceAccessible);
            }
        });
    }

    /**
     * Listens to changes in the feed and updates the tagging options.
     */
    private class TaggingFeedListener extends FeedAdapter
    {
        /**
         * Called when information in feed changed.
         *
         * @param feed     feed.
         * @param property property of the feed.
         * @param oldValue old property value.
         * @param newValue new property value.
         */
        public void propertyChanged(IFeed feed, String property, Object oldValue, Object newValue)
        {
            if (DirectFeed.PROP_XML_URL.equals(property)) updateTaggingActions();
        }
    }

    /**
     * Connection state listener.
     */
    private class ConnectionStateListener implements PropertyChangeListener
    {
        /**
         * Invoked when connection state changes.
         *
         * @param evt preoperty change event.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            String prop = evt.getPropertyName();

            if (ConnectionState.PROP_ONLINE.equals(prop))
            {
                onConnectionChange(connectionState.isOnline());
            } else
            {
                onServiceAvailabilityChange(connectionState.isServiceAccessible());
            }
        }
    }
}
