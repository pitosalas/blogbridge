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
// $Id: MDUpdater.java,v 1.7 2007/04/10 10:33:44 spyromus Exp $
//

package com.salas.bb.discovery;

import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.FeedMetaDataHolder;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.utils.ConnectionState;
import com.salas.bb.utils.i18n.Strings;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Updater is a background task which is walking through the list of subscriptions
 * and updates their meta-data.
 */
public final class MDUpdater implements Runnable
{
    private static final Logger LOG = Logger.getLogger(MDUpdater.class.getName());

    // Meta-data update period (min)
    private static long updatePeriod = UserPreferences.DEFAULT_WEBSTAT_MINS * 60 * 1000;

    private final MDManager metaDataManager;
    private final ConnectionState connectionState;

    private GuidesSet       guidesSet;

    /** Armed into <code>TRUE</code> state when meta-data even happened during being offline. */
    private boolean skippedUpdateDuringOffline;

    /**
     * Creates updater which is talking to meta-data manager.
     *
     * @param aMetaDataManager  meta-data manager to talk to.
     * @param aConnectionState  connection state interface.
     */
    public MDUpdater(MDManager aMetaDataManager, ConnectionState aConnectionState)
    {
        metaDataManager = aMetaDataManager;
        connectionState = aConnectionState;
        guidesSet = null;

        skippedUpdateDuringOffline = false;
        aConnectionState.addPropertyChangeListener(ConnectionState.PROP_SERVICE_ACCESSIBLE,
            new ServiceAccessibilityListener());
    }

    /**
     * Sets new guides set to use.
     *
     * @param aGuidesSet guides set.
     */
    public synchronized void setGuidesSet(GuidesSet aGuidesSet)
    {
        guidesSet = aGuidesSet;
    }

    /**
     * Invoked when someone wishes to execute this runnable task.
     */
    public void run()
    {
        try
        {
            if (connectionState.isServiceAccessible())
            {
                update();
            } else
            {
                skippedUpdateDuringOffline = true;
            }
        } catch (RuntimeException e)
        {
            LOG.log(Level.SEVERE, Strings.error("unhandled.exception"), e);
        }
    }

    /**
     * This operation scans the guides set for all direct feeds with outdated meta-data
     * information and asks {@link #metaDataManager} to update them.
     */
    public void update()
    {
        List<IFeed> feeds;

        synchronized (this)
        {
            feeds = guidesSet == null ? null : guidesSet.getFeeds();
        }

        if (feeds != null)
        {
            long updateTimeThreshold = System.currentTimeMillis() - getUpdatePeriod();
            for (IFeed feed : feeds)
            {
                if (feed instanceof DirectFeed)
                {
                    update((DirectFeed)feed, updateTimeThreshold);
                }
            }
        }
    }

    /**
     * Returns currently set update period.
     *
     * @return update period in milliseconds.
     */
    private static long getUpdatePeriod()
    {
        return updatePeriod;
    }

    /**
     * Sets new update period.
     *
     * @param aUpdatePeriod update period in milliseconds.
     */
    public static void setUpdatePeriod(long aUpdatePeriod)
    {
        updatePeriod = aUpdatePeriod;
    }

    /**
     * Updates given feed if necessary.
     *
     * @param aFeed                 feed to check and update.
     * @param updateTimeThreshold   maximum last update time. If feed has update time
     *                              older than this one it will be updated.
     */
    private void update(DirectFeed aFeed, long updateTimeThreshold)
    {
        long lastUpdate = aFeed.getLastMetaDataUpdateTime();
        if (lastUpdate < updateTimeThreshold)
        {
            URL baseURL = getBaseURL(aFeed);
            FeedMetaDataHolder holder = aFeed.getMetaDataHolder();
            if (baseURL != null && holder != null)
            {
                if (LOG.isLoggable(Level.FINE)) LOG.fine("Updating meta-data of " + aFeed);
                metaDataManager.update(holder, baseURL);
            }
        }
    }

    /**
     * Finds base URL for further updating activity. It's preferrable to have
     * XML URL, then site URL.
     *
     * @param aFeed feed to evaluate base URL from.
     *
     * @return base URL or <code>NULL</code> if not possible get one.
     */
    private URL getBaseURL(DirectFeed aFeed)
    {
        URL baseURL = aFeed.getXmlURL();
        if (baseURL == null) baseURL = aFeed.getSiteURL();

        return baseURL;
    }

    /** Listens to the updates in service accessibility. */
    private class ServiceAccessibilityListener implements PropertyChangeListener
    {
        /**
         * Called when service accessibility changes.
         *
         * @param evt property change event.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (skippedUpdateDuringOffline && connectionState.isServiceAccessible()) run();
        }
    }
}
