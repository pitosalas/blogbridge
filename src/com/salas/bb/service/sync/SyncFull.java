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
// $Id: SyncFull.java,v 1.8 2006/06/05 14:06:01 spyromus Exp $
//

package com.salas.bb.service.sync;

import com.salas.bb.core.GlobalModel;
import com.salas.bb.utils.i18n.Strings;

/**
 * Full synchronization module.
 */
public class SyncFull extends AbstractSynchronization
{
    /**
     * Creates synchronization module.
     *
     * @param aModel model to operate.
     */
    public SyncFull(GlobalModel aModel)
    {
        super(aModel);
    }

    /**
     * Returns number of steps necessary to synchronize the stuff.
     *
     * @return synchronization steps.
     */
    public int getSynchronizationSteps()
    {
        return super.getSynchronizationSteps() * 2;
    }

    /**
     * Performs the step-by-step synchronization and collects stats.
     *
     * @param aProgressListener listener to notify.
     * @param aEmail            email of user account.
     * @param aPassword         password of user account.
     *
     * @return statistics.
     */
    protected Stats doSynchronization(IProgressListener aProgressListener,
                                      String aEmail, String aPassword)
    {
        Stats stats;

        // Do sync-in and record stats
        SyncIn syncIn = new SyncIn(model, false);
        Stats syncInStats = syncIn.doSynchronization(aProgressListener, aEmail, aPassword);

        // If sync-in was successful we continue with sync-out
        if (!syncInStats.hasFailed())
        {
            // Do sync-out and record stats
            SyncOut syncOut = new SyncOut(model);
            Stats syncOutStats = syncOut.doSynchronization(aProgressListener, aEmail, aPassword);

            // If sync-out was successful we create our own stats
            if (!syncOutStats.hasFailed())
            {
                stats = new SyncFullStats(syncInStats, syncOutStats);
            } else
            {
                stats = new SyncFullStats(syncOutStats.getStatsText());
            }
        } else
        {
            stats = new SyncFullStats(syncInStats.getStatsText());
        }

        return stats;
    }

    /**
     * Returns the message to be reported on synchronization start.
     *
     * @return message.
     */
    protected String getProcessStartMessage()
    {
        return prepareProcessStartMessage(
            Strings.message("service.sync.message.synchronizing"),
            Strings.message("service.sync.message.preferences"),
            Strings.message("service.sync.message.guides.and.feeds"),
            Strings.message("service.sync.message.with.blogbridge.service")
        );
    }

    /**
     * Simple holder of stats.
     */
    private static class SyncFullStats extends Stats
    {
        private String  text;

        /**
         * Creates stats object holding error message.
         *
         * @param error error message.
         */
        public SyncFullStats(String error)
        {
            registerFailure(error);
        }

        /**
         * Creates stats object.
         *
         * @param aSyncInStats  in-stats.
         * @param aSyncOutStats out-stats.
         */
        public SyncFullStats(Stats aSyncInStats, Stats aSyncOutStats)
        {
            text = (aSyncInStats.getStatsText() + "\n" + aSyncOutStats.getStatsText()).trim();
        }

        /**
         * Returns custom text to be told if not failed.
         *
         * @return text.
         */
        protected String getCustomText()
        {
            return text;
        }
    }
}
