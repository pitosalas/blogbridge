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
// $Id: UpdatesDownloader.java,v 1.9 2006/12/13 18:06:55 spyromus Exp $
//

package com.salas.bb.updates;

import com.jgoodies.uif.application.Application;
import com.salas.bb.core.ApplicationLauncher;
import com.salas.bb.updates.ui.DownloadsProgressDialog;
import com.salas.bb.utils.net.Downloader;
import com.salas.bb.utils.net.IStreamProgressListener;
import com.salas.bb.utils.OSSettings;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.text.MessageFormat;

/**
 * Updates downloader class accepts the list of locations to download and
 * performs the downloading operation for the one by one. User can cancel the
 * process at any moment.
 */
public class UpdatesDownloader
{
    private static final Logger LOG = Logger.getLogger(UpdatesDownloader.class.getName());

    private static final String UPDATES_PATH = "updates";

    /**
     * Downloads all given locations to updates folder.
     *
     * @param locations locations to download.
     */
    public void download(Location[] locations)
    {
        if (locations == null || locations.length == 0) return;

        final DownloadsProgressDialog dialog;

        // Create progress dialog
        JFrame frame = Application.getDefaultParentFrame();
        dialog = new DownloadsProgressDialog(frame, locations);

        // Create downloader component
        IStreamProgressListener progressListener = dialog.getProgressListener();
        Downloader downloader = new Downloader(progressListener);

        // Initialize target directory
        File targetDirectory = getTargetDirectory();

        List successful = new ArrayList();
        List failed = new ArrayList();

        // Open progress dialog and start downloading packages
        openProgressDialog(dialog, downloader, locations, targetDirectory);
        try
        {
            for (int i = 0; i < locations.length; i++)
            {
                Location location = locations[i];
                try
                {
                    URL locationURL = new URL(location.getLink());
                    downloader.download(locationURL, targetDirectory);
                    successful.add(location);
                } catch (IOException e)
                {
                    LOG.log(Level.SEVERE, MessageFormat.format(
                        Strings.error("invalid.url"),
                        new Object[] { location.getLink() }), e);
                    failed.add(location);
                } finally
                {
                    dialog.nextDownload();
                }
            }

            dialog.allDone(successful, failed);
        } catch (InterruptedException e)
        {
            LOG.info("Downloading canceled...");
        }
    }

    /**
     * Creates directory if it doesn't exist.
     *
     * @return target directory for updates.
     */
    private File getTargetDirectory()
    {
        File targetDirectory = OSSettings.getDesktopDirectory();

        if (targetDirectory == null)
        {
            targetDirectory = new File(ApplicationLauncher.getContextPath() + UPDATES_PATH);
            if (!targetDirectory.exists()) targetDirectory.mkdir();
        }

        return targetDirectory;
    }

    /**
     * Opens progress dialog.
     *
     * @param aDialog       dialog.
     * @param aDownloader   downloader to cancel when user cancels the dialog.
     * @param aLocations    locations we are going to download.
     * @param aDirectory    directory, where the packages are downloaded to.
     */
    private void openProgressDialog(final DownloadsProgressDialog aDialog,
                                    final Downloader aDownloader, final Location[] aLocations,
                                    final File aDirectory)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                aDialog.open(
                    new InstallAction(aLocations, aDirectory),
                    new CancelAction(aDownloader),
                    new ExitAction());
            }
        });
    }

    private class ExitAction implements Runnable
    {
        public void run()
        {
            Application.close();
        }
    }

    private class InstallAction implements Runnable
    {
        private final Location[] locations;
        private final File directory;

        public InstallAction(Location[] locations, File directory)
        {
            this.directory = directory;
            this.locations = locations;
        }

        public void run()
        {
            Location mainPackage = Location.getMainPackage(Arrays.asList(locations));
            try
            {
                PackageInstaller.startInstaller(mainPackage, directory);
            } catch (IOException e)
            {
                LOG.log(Level.WARNING, Strings.error("updates.failed.to.start.package.installation"), e);
            }
        }
    }

    private class CancelAction implements Runnable
    {
        private final Downloader downloader;

        public CancelAction(Downloader downloader)
        {
            this.downloader = downloader;
        }

        public void run()
        {
            downloader.cancel();
        }
    }
}
