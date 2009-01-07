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
// $Id: AbstractSynchronization.java,v 1.11 2006/06/05 14:06:01 spyromus Exp $
//

package com.salas.bb.service.sync;

import com.salas.bb.core.GlobalModel;
import com.salas.bb.service.ServicePreferences;
import com.salas.bb.utils.DateUtils;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.i18n.Strings;

import java.util.Date;
import java.text.MessageFormat;

/**
 * Abstract synchronization implementation.
 */
abstract class AbstractSynchronization
{
    protected GlobalModel         model;
    protected ServicePreferences  servicePreferences;

    /**
     * Creates synchronization module.
     *
     * @param aModel model to operate.
     */
    AbstractSynchronization(GlobalModel aModel)
    {
        if (aModel == null)
            throw new IllegalArgumentException(Strings.error("unspecified.model"));
        if (aModel.getServicePreferences() == null)
            throw new IllegalArgumentException(Strings.error("sync.unspecified.service.preferences"));

        model = aModel;
        servicePreferences = aModel.getServicePreferences();
    }

    /**
     * Returns number of steps necessary to synchronize the stuff.
     *
     * @return synchronization steps.
     */
    public int getSynchronizationSteps()
    {
        boolean syncFeeds = servicePreferences.isSyncFeeds();
        boolean syncPreferences = servicePreferences.isSyncPreferences();
        return (syncFeeds ? 1 : 0) + (syncPreferences ? 1 : 0);
    }

    /**
     * Perform synchronization. Notifies {@link IProgressListener} about steps and manages
     * process completely if <code>managerProcess</code> flag set.
     *
     * @param progressListener  listener to notify.
     * @param manageProgress    manage progress.
     *
     * @return statistics or NULL on failure.
     */
    public Stats doSynchronization(IProgressListener progressListener, boolean manageProgress)
    {
        String email = servicePreferences.getEmail();
        String password = servicePreferences.getPassword();

        return doSynchronization(progressListener, manageProgress, email, password);
    }

    /**
     * Perform synchronization. Notifies {@link IProgressListener} about steps and manages
     * process completely if <code>managerProcess</code> flag set.
     *
     * @param progressListener  listener to notify.
     * @param manageProgress    manage progress.
     * @param aEmail            email of user account.
     * @param aPassword         password of user account.
     *
     * @return statistics or NULL on failure.
     */
    public Stats doSynchronization(IProgressListener progressListener,
                                   boolean manageProgress, String aEmail, String aPassword)
    {
        if (progressListener == null && manageProgress)
            throw new IllegalArgumentException(Strings.error("unspecified.progress.listener"));

        if (manageProgress) reportProcessStart(progressListener);

        Stats stats = doSynchronization(progressListener, aEmail, aPassword);

        if (manageProgress) reportProcessFinish(progressListener, stats);

        return stats;
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
    protected abstract Stats doSynchronization(IProgressListener aProgressListener, String aEmail,
                                               String aPassword);

    /**
     * Reports start of the synchronization process.
     *
     * @param progressListener listener.
     */
    private void reportProcessStart(IProgressListener progressListener)
    {
        if (progressListener != null)
        {
            progressListener.processStarted(getProcessStartMessage(), getSynchronizationSteps());
        }
    }

    /**
     * Returns the message to be reported on synchronization start.
     *
     * @return message.
     */
    protected abstract String getProcessStartMessage();

    /**
     * Reports finish of the synchronization process.
     *
     * @param progressListener listener.
     * @param stats            synchronization stats.
     */
    private void reportProcessFinish(IProgressListener progressListener, Stats stats)
    {
        String message;

        if (stats == null)
        {
            message = Strings.message("service.sync.failed.to.perform");
        } else
        {
            message = stats.getStatsText();

            if (stats.hasFailed())
            {
                if (message == null)
                {
                    message = Strings.message("service.sync.communication.problem");
                } else
                {
                    message = MessageFormat.format(Strings.message("service.sync.failed.for.some.reason"),
                        new Object[] { message });
                }
            }
        }

        if (progressListener != null) progressListener.processFinished(message);
    }

    /**
     * Returns TRUE if the automatic sync can occur as it doesn't break the schedule.
     *
     * @return TRUE if sync can proceed.
     */
    public boolean isSyncTime()
    {
        int syncMode = servicePreferences.getSyncMode();
        Date lastSyncOutDate = servicePreferences.getLastSyncOutDate();
        int syncPeriod = servicePreferences.getSyncPeriod();

        return syncSettingsProvided() &&
            syncMode != ServicePreferences.SYNC_MODE_MANUAL &&
            (syncMode == ServicePreferences.SYNC_MODE_EACH_RUN ||
             DateUtils.dayDiffFromToday(lastSyncOutDate) >= syncPeriod * Constants.MILLIS_IN_DAY);
    }

    /**
     * Checks if all settings provided and we can continue making synchronization.
     *
     * @return TRUE if its OK.
     */
    protected boolean syncSettingsProvided()
    {
        return servicePreferences.getEmail() != null &&
            servicePreferences.getEmail().length() > 0 &&
            servicePreferences.getPassword() != null &&
            servicePreferences.getPassword().length() > 0;
    }

    /**
     * Prepares start message.
     *
     * @param action    action message (synchronizing, loading, saving).
     * @param prefs     preferences message.
     * @param feeds     feeds message.
     * @param service   service message.
     *
     * @return complete string.
     */
    protected String prepareProcessStartMessage(String action, String prefs, String feeds, String service)
    {
        boolean syncFeeds = servicePreferences.isSyncFeeds();
        boolean syncPreferences = servicePreferences.isSyncPreferences();

        StringBuffer message = new StringBuffer(action);
        if (syncPreferences) message.append(" ").append(prefs);
        if (syncFeeds)
        {
            if (syncPreferences) message.append(",");
            message.append(" ").append(feeds);
        }
        message.append(" ").append(service);

        return message.toString();
    }

    /**
     * Marker class for statistics.
     */
    public abstract static class Stats
    {
        private boolean failed;
        private String  text;

        /**
         * Default constructor.
         */
        protected Stats()
        {
            failed = false;
            text = null;
        }

        /**
         * Full constructor.
         *
         * @param aText     text.
         * @param aFailed   failed flag.
         */
        protected Stats(String aText, boolean aFailed)
        {
            text = aText;
            failed = aFailed;
        }

        /**
         * Registers failure.
         *
         * @param error failure.
         */
        protected void registerFailure(String error)
        {
            failed = true;
            text = error;
        }

        /**
         * Returns TRUE if operation has failed.
         *
         * @return TRUE if operation has failed.
         */
        public boolean hasFailed()
        {
            return failed;
        }

        /**
         * Returns textual stats.
         *
         * @return stats.
         */
        public final String getStatsText()
        {
            return failed ? text : getCustomText();
        }

        /**
         * Returns custom text to be told if not failed.
         *
         * @return text.
         */
        protected abstract String getCustomText();
    }
}
