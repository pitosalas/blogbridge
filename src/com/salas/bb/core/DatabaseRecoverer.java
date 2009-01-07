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
// $Id $
//

package com.salas.bb.core;

import com.jgoodies.uif.application.Application;
import com.salas.bb.core.actions.guide.ImportGuidesAction;
import com.salas.bb.utils.opml.ImporterAdv;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.service.sync.SyncInAction;
import com.salas.bb.service.ServicePreferences;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Date;
import java.awt.event.ActionEvent;

/**
 * Database recovery utility.
 */
final class DatabaseRecoverer
{
    private static final Logger LOG = Logger.getLogger(DatabaseRecoverer.class.getName());

    /** Hidden utility constructor. */
    private DatabaseRecoverer()
    {
    }

    /**
     * Performs recovery.
     *
     * @param model model.
     * @param backupsPath backups directory path.
     */
    static void performRecovery(GlobalModel model, String backupsPath)
    {
        Date lastSuccessfulSync = getLastSuccessfulSyncDate(model);
        File[] availableBackups = findAvailableBackups(backupsPath);

        DataRecoverySelectionDialog.DataRecoveryChoice choice = DataRecoverySelectionDialog.ask(
            Application.getDefaultParentFrame(), lastSuccessfulSync, availableBackups);

        switch (choice.getMode())
        {
            case DataRecoverySelectionDialog.DataRecoveryChoice.MODE_FROM_BACKUP:
                performRecoveryFromBackup(choice.getBackupFile(), model);
                break;
            case DataRecoverySelectionDialog.DataRecoveryChoice.MODE_FROM_SERVICE:
                performRecoveryFromService(model);
                break;
            default:
                break;
        }
    }

    private static void performRecoveryFromBackup(File backupFile, GlobalModel model)
    {
        try
        {
            ImportGuidesAction.doImport(new ImporterAdv(), backupFile.toURL().toString(), false,
                false, model, false);
        } catch (MalformedURLException e)
        {
            LOG.log(Level.SEVERE, "Failed to recover from backup.", e);
        }
    }

    private static void performRecoveryFromService(GlobalModel model)
    {
        // We create this event to tell the owner -- parent frame
        ActionEvent event = new ActionEvent(Application.getDefaultParentFrame(), 0, null);

        SyncInAction.getInstance().doSync(event);
    }

    /**
     * Finds all available and good OPML backup files.
     *
     * @param backupsPath backups directory path.
     *
     * @return backups.
     */
    static File[] findAvailableBackups(String backupsPath)
    {
        File[] backups;

        File backupsDir = new File(backupsPath);
        if (backupsDir.exists())
        {
            backups = backupsDir.listFiles(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name != null && name.matches("^~[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{6}\\.opml$");
                }
            });
        } else backups = new File[0];

        return backups;
    }

    /**
     * Returns <code>TRUE</code> if service account information is entered.
     *
     * @param model model.
     *
     * @return <code>TRUE</code> if service info is entered.
     */
    private static boolean isServiceAccountPresent(GlobalModel model)
    {
        String success = ServicePreferences.SYNC_STATUS_SUCCESS;

        ServicePreferences prefs = model.getServicePreferences();

        // If account info is added and any synchronization was successful
        // we report that the account is present and can be used for restoration
        boolean actInfoEntered = prefs.isAccountInformationEntered();
        boolean successfulSync = success.equals(prefs.getLastSyncInStatus()) ||
                                 success.equals(prefs.getLastSyncOutStatus());

        return actInfoEntered && successfulSync;
    }

    /**
     * Returns the date of the last successful synchronization, no matter if it's in or out.
     * We care about the date being stored on the service and whether it's valid or not, so
     * if one of the syncs (at least) was successful we return its date. Otherwise (also if
     * the account isn't registered) the date will be NULL.
     *
     * @param model model.
     *
     * @return the most recent date of the sync or NULL.
     */
    static Date getLastSuccessfulSyncDate(GlobalModel model)
    {
        return model == null ? null : getLastSuccessfulSyncDate(model.getServicePreferences());
    }

    /**
     * Returns the date of the last successful synchronization, no matter if it's in or out.
     * We care about the date being stored on the service and whether it's valid or not, so
     * if one of the syncs (at least) was successful we return its date. Otherwise (also if
     * the account isn't registered) the date will be NULL.
     *
     * @param model model.
     *
     * @return the most recent date of the sync or NULL.
     */
    static Date getLastSuccessfulSyncDate(ServicePreferences prefs)
    {
        if (prefs == null || !prefs.isAccountInformationEntered()) return null;

        Date syncInDate = null;
        if (ServicePreferences.SYNC_STATUS_SUCCESS.equals(prefs.getLastSyncInStatus()))
        {
            syncInDate = prefs.getLastSyncInDate();
        }

        Date syncOutDate = null;
        if (ServicePreferences.SYNC_STATUS_SUCCESS.equals(prefs.getLastSyncOutStatus()))
        {
            syncOutDate = prefs.getLastSyncOutDate();
        }

        return syncInDate != null
            ? (syncOutDate == null || syncInDate.after(syncOutDate))
                ? syncInDate
                : syncOutDate
            : syncOutDate;
    }
}
