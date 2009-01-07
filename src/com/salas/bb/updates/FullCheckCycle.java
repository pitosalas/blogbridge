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
// $Id: FullCheckCycle.java,v 1.8 2006/05/29 12:48:31 spyromus Exp $
//

package com.salas.bb.updates;

import com.salas.bb.updates.ui.NewVersionAvailableDialog;
import com.salas.bb.updates.ui.SimpleCheckProgressDialog;
import com.salas.bb.utils.i18n.Strings;
import com.jgoodies.uif.AbstractDialog;

import javax.swing.*;
import java.io.IOException;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Full updates checking cycle from service query to the updates dialog and
 * downloading of changes.
 */
public class FullCheckCycle
{
    private static final Logger LOG = Logger.getLogger(FullCheckCycle.class.getName());

    private final Frame     frame;
    private final String    currentVersion;
    private final boolean   showProgressDialog;

    private AbstractDialog progressDialog;

    /**
     * Creates full check cycle manager.
     *
     * @param aFrame                parent frame.
     * @param aCurrentVersion       current version ("1.0", "2.1" ...).
     * @param aShowProgressDialog   <code>TRUE</code> to show progress dialog when checking.
     */
    public FullCheckCycle(Frame aFrame, String aCurrentVersion,
        boolean aShowProgressDialog)
    {
        frame = aFrame;
        currentVersion = aCurrentVersion;
        showProgressDialog = aShowProgressDialog;
    }

    /**
     * Perform check and show dialog (optional).
     */
    public void check()
    {
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("Checking updates since version " + currentVersion);
        }

        try
        {
            CheckResult result = queryServiceForNewVersion();
            Location[] locations = result == null ? null
                : (Location[])result.getLocations().values().toArray(new Location[0]);

            if (locations != null && Location.selectApplicable(locations).length > 0)
            {
                if (LOG.isLoggable(Level.FINE))
                {
                    LOG.fine("Updated version " + result.getRecentVersion() + " detected.");
                }

                showNewVersionDialog(result);
            }
        } catch (IOException e)
        {
            // Failed to check fo new version
            LOG.warning(Strings.error("updates.failed.to.check.for.updated.version"));
        }
    }

    /**
     * Query service through checker.
     *
     * @return result of query.
     *
     * @throws IOException in case of any I/O errors.
     */
    private CheckResult queryServiceForNewVersion()
        throws IOException
    {
        CheckResult result;
        Checker checker = new Checker();

        if (showProgressDialog) openProgressDialog();
        try
        {
            result = checker.checkForUpdates(currentVersion);
        } finally
        {
            if (showProgressDialog) hideProgressDialog();
        }

        return result;
    }

    /**
     * Initializes and shows progress dialog.
     */
    private void openProgressDialog()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                progressDialog = new SimpleCheckProgressDialog(frame);
                progressDialog.open();
            }
        });
    }

    /**
     * Hides progress dialog.
     */
    private void hideProgressDialog()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                if (progressDialog != null) progressDialog.close();
            }
        });
    }

    /**
     * Shows new version availability dialog.
     *
     * @param aResult result of the check.
     */
    private void showNewVersionDialog(CheckResult aResult)
    {
        NewVersionAvailableDialog dialog = new NewVersionAvailableDialog(frame);
        dialog.open(aResult);

        if (!dialog.hasBeenCanceled())
        {
            UpdatesDownloader downloader = new UpdatesDownloader();
            downloader.download(dialog.getSelectedLocations());
        }
    }
}
