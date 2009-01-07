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
// $Id: PollerTask.java,v 1.7 2006/05/29 12:48:38 spyromus Exp $
//

package com.salas.bb.utils.poller;

import com.salas.bb.domain.DataFeed;
import com.salas.bb.views.ActivityTicket;
import com.salas.bb.views.ActivityIndicatorView;
import com.salas.bb.utils.i18n.Strings;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Task for Poller workers.
 */
public final class PollerTask implements Runnable
{
    private static final Logger LOG = Logger.getLogger(PollerTask.class.getName());

    /**
     * Feed which should be updated.
     */
    private DataFeed feed;

    /**
     * Creates a task for feed update.
     *
     * @param aFeed feed to update.
     *
     * @throws NullPointerException if the feed isn't specified.
     */
    public PollerTask(DataFeed aFeed)
    {
        feed = aFeed;
    }

    /**
     * Invoked to when task is running.
     */
    public void run()
    {
        ActivityTicket activityTicket = null;

        try
        {
            // If the feed still not removed from the guide then continue
            if (feed.getID() != -1)
            {
                activityTicket = startPolling(feed);
                feed.update();
            }
        } catch (Exception e)
        {
            LOG.log(Level.SEVERE, Strings.error("unhandled.exception"), e);
        } finally
        {
            finishPolling(feed, activityTicket);
        }
    }

    /**
     * Does the tasks associateed with polling start.
     *
     * @param aFeed feed which is going to be polled.
     *
     * @return activity ticket which is used to control the indication.
     */
    private static ActivityTicket startPolling(DataFeed aFeed)
    {
        if (LOG.isLoggable(Level.FINE)) LOG.fine("Updating: " + aFeed);

        return ActivityIndicatorView.startPolling(aFeed.getTitle());
    }

    /**
     * Does the tasks associated with polling finish.
     *
     * @param aFeed             feed which was being polled.
     * @param aActivityTicket   activity ticket to use for indication termination.
     */
    private static void finishPolling(DataFeed aFeed, ActivityTicket aActivityTicket)
    {
        if (aActivityTicket != null) ActivityIndicatorView.finishActivity(aActivityTicket);

        // Finishing processing (started in Poller.update(DataFeed))
        aFeed.processingFinished();
    }
}
