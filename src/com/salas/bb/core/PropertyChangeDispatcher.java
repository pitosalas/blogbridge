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
// $Id: PropertyChangeDispatcher.java,v 1.30 2008/02/28 15:59:46 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.discovery.MDUpdater;
import com.salas.bb.domain.DataFeed;
import com.salas.bb.domain.ReadingList;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.notification.NotificationArea;
import com.salas.bb.views.mainframe.MainFrame;
import com.salas.bb.views.mainframe.UnreadButton;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * This is the listener of all property changes. It decides what to do in response and in
 * what sequence. It's necessary to respond centralized because we need to be sure in
 * exact sequence of events.
 */
public class PropertyChangeDispatcher implements PropertyChangeListener
{
    private GlobalController    controller;

    /**
     * Creates dispatcher.
     *
     * @param aController owning controller.
     */
    public PropertyChangeDispatcher(GlobalController aController)
    {
        controller = aController;
    }

    /**
     * This method gets called when a bound property is changed.
     *
     * @param evt event object.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String prop = evt.getPropertyName();
        boolean rebuildChannelGuideModel = false;

if (UserPreferences.PROP_WEBSTAT_INTERVAL.equals(prop))
        {
            long newPeriod = ((Integer)evt.getNewValue()).longValue() * Constants.MILLIS_IN_MINUTE;
            MDUpdater.setUpdatePeriod(newPeriod);
        } else if (UserPreferences.PROP_GOOD_CHANNEL_STARZ.equals(prop))
        {
            controller.getModel().getGuidesSet().invalidateFeedVisibilityCaches();
            int starz = (Integer)evt.getNewValue() - 1;
            getChannelGuideModel().setScoreThreshold(starz);
            getNavigationModel().setScoreThreshold(starz);
        } else if (UserPreferences.PROP_SORT_BY_CLASS_1.equals(prop))
        {
            int classMask = (Integer)evt.getNewValue();
            getChannelGuideModel().setPrimarySortOrder(classMask);
            getNavigationModel().setPrimarySortOrder(classMask);
        } else if (UserPreferences.PROP_SORT_BY_CLASS_2.equals(prop))
        {
            int classMask = (Integer)evt.getNewValue();
            getChannelGuideModel().setSecondarySortOrder(classMask);
            getNavigationModel().setSecondarySortOrder(classMask);
        } else if (UserPreferences.PROP_REVERSED_SORT_BY_CLASS_1.equals(prop))
        {
            boolean reversed = (Boolean)evt.getNewValue();
            getChannelGuideModel().setPrimarySortOrderDirection(reversed);
            getNavigationModel().setPrimarySortOrderDirection(reversed);
        } else if (UserPreferences.PROP_REVERSED_SORT_BY_CLASS_2.equals(prop))
        {
            boolean reversed = (Boolean)evt.getNewValue();
            getChannelGuideModel().setSecondarySortOrderDirection(reversed);
            getNavigationModel().setSecondarySortOrderDirection(reversed);
        } else if (UserPreferences.PROP_SORTING_ENABLED.equals(prop))
        {
            boolean enabled = (Boolean)evt.getNewValue();
            getChannelGuideModel().setSortingEnabled(enabled);
            getNavigationModel().setSortingEnabled(enabled);
        } else if (getChannelScoreCalculator().dispatchPropertyChangeEvent(evt))
        {
            rebuildChannelGuideModel = true;
        } else if (UserPreferences.PROP_PURGE_COUNT.equals(prop))
        {
            int oldLimit = DataFeed.getGlobalPurgeLimit();
            int limit = (Integer)evt.getNewValue();
            DataFeed.setGlobalPurgeLimit(limit);
            if (limit < oldLimit) controller.getModel().getGuidesSet().clean();
        } else if (UserPreferences.PROP_PRESERVE_UNREAD.equals(prop))
        {
            boolean purgeUnread = !(Boolean)evt.getNewValue();
            DataFeed.setGlobalPurgeUnread(purgeUnread);
            if (purgeUnread) controller.getModel().getGuidesSet().clean();
        } else if (UserPreferences.PROP_RSS_POLL_MIN.equals(prop))
        {
            long oldPeriod = DataFeed.getGlobalUpdatePeriod();
            long newPeriod = ((Integer)evt.getNewValue()).longValue() * Constants.MILLIS_IN_MINUTE;
            DataFeed.setGlobalUpdatePeriod(newPeriod);
            if (newPeriod < oldPeriod) controller.getPoller().update();
        } else if (UserPreferences.PROP_READING_LIST_UPDATE_PERIOD.equals(prop))
        {
            long oldPeriod = ReadingList.getGlobalUpdatePeriod();
            long newPeriod = (Long)evt.getNewValue();
            ReadingList.setGlobalUpdatePeriod(newPeriod);
            if (newPeriod > ReadingList.PERIOD_NEVER &&
                newPeriod < oldPeriod) controller.getPoller().update();
        } else if (UserPreferences.PROP_TAGS_STORAGE.equals(prop))
        {
            int newType = (Integer)evt.getNewValue();
            controller.changeTagsStorage(newType);
        } else if (UserPreferences.PROP_SHOW_TOOLBAR_LABELS.equals(prop))
        {
            boolean visible = (Boolean)evt.getNewValue();
            MainFrame mainFrame = controller.getMainFrame();
            if (mainFrame != null) mainFrame.setToolbarLabelsVisible(visible);
        } else if (UserPreferences.PROP_SHOW_UNREAD_BUTTON_MENU.equals(prop))
        {
            boolean visible = (Boolean)evt.getNewValue();
            UnreadButton.setShowMenuOnClick(visible);
        } else if (UserPreferences.PROP_SHOW_TOOLBAR.equals(prop))
        {
            boolean visible = (Boolean)evt.getNewValue();
            MainFrame mainFrame = controller.getMainFrame();
            if (mainFrame != null) mainFrame.setToolbarVisible(visible);
        } else if (UserPreferences.PROP_UPDATE_FEEDS.equals(prop))
        {
            boolean update = (Boolean)evt.getNewValue();
            controller.getPoller().setUpdateFeedsManually(update);
        } else if (UserPreferences.PROP_UPDATE_READING_LISTS.equals(prop))
        {
            boolean update = (Boolean)evt.getNewValue();
            controller.getPoller().setUpdateReadingListsManually(update);
        } else if (UserPreferences.PROP_PROXY_ENABLED.equals(prop) ||
            UserPreferences.PROP_PROXY_HOST.equals(prop) ||
            UserPreferences.PROP_PROXY_PORT.equals(prop))
        {
            controller.setProxySettings((UserPreferences)evt.getSource());
        } else if (UserPreferences.PROP_SHOW_APPICON_IN_SYSTRAY.equals(prop))
        {
            NotificationArea.setAppIconAlwaysVisible((Boolean)evt.getNewValue());
        } else if (UserPreferences.PROP_MINIMIZE_TO_SYSTRAY.equals(prop))
        {
            MainFrame mainFrame = controller.getMainFrame();
            if (mainFrame != null) mainFrame.setMinimizeToSystemTray((Boolean)evt.getNewValue());
        } else if (UserPreferences.PROP_DIB_MODE.equals(prop))
        {
            int mode = (Integer)evt.getNewValue();
            if (controller.dockIconUnreadMonitor != null) controller.dockIconUnreadMonitor.setMode(mode);
        }

        // Rebuild guide model if necessary
        if (rebuildChannelGuideModel)
        {
            // During initialization this section is called from non-EDT thread (main).
            // That's OK as we don't have GUI operations over the model yet.

            getChannelGuideModel().fullRebuild();
            getNavigationModel().fullRebuild();
            // vfHelperModel need not be rebuilt here
        }
    }

    // Returns current channel guide model
    private GuideModel getChannelGuideModel()
    {
        return controller.getModel().getGuideModel();
    }
    
    // Returns current channel guide model
    private GuideModel getNavigationModel()
    {
        return controller.getNavigationModel();
    }

    // Returns channel score calculator
    private ScoresCalculator getChannelScoreCalculator()
    {
        return controller.getScoreCalculator();
    }

    // Returns highlights calculator
    private HighlightsCalculator getHighlightsCalculator()
    {
        return controller.getHighlightsCalculator();
    }
}
