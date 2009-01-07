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
// $Id: Downloader.java,v 1.12 2007/05/02 10:27:06 spyromus Exp $
//

package com.salas.bb.utils.net;

import com.jgoodies.uif.application.Application;
import com.salas.bb.networking.manager.NetManager;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Downloader of updates.
 */
public class Downloader
{
    private static final Logger LOG = Logger.getLogger(Downloader.class.getName());

    private static final String THREAD_NAME = "Download File";

    private static File lastSaveDir;

    private boolean                 canceled;
    private IStreamProgressListener listener;

    /**
     * Creates downloader with given progress listener object.
     *
     * @param aListener listener.
     */
    public Downloader(IStreamProgressListener aListener)
    {
        listener = aListener;
    }

    /**
     * Downloads file from location to target directory.
     *
     * @param aLocation     location to take file from.
     * @param aTargetDir    target directory.
     *
     * @return downloaded file.
     *
     * @throws InterruptedException in case if downloading was interrupted.
     * @throws NullPointerException when no location or target directory specified.
     * @throws IOException          if any I/O exception happens.
     */
    public File download(URL aLocation, File aTargetDir)
        throws InterruptedException, IOException
    {
        return download(aLocation, aTargetDir, null);
    }

    /**
     * Downloads file from location to target directory.
     *
     * @param aLocation         location to take file from.
     * @param aTargetDir        target directory.
     * @param aTargetFileName   name of target file or <code>NULL</code>.
     *
     * @return downloaded file.
     *
     * @throws InterruptedException in case if downloading was interrupted.
     * @throws NullPointerException when no location or target directory specified.
     * @throws IOException          if any I/O exception happens.
     */
    public File download(URL aLocation, File aTargetDir, String aTargetFileName)
        throws InterruptedException, IOException
    {
        if (aLocation == null || aTargetDir == null)
            throw new NullPointerException(Strings.error("unspecified.net.location.and.target.directory"));

        canceled = false;

        // Extract or create name for the target file
        String targetFilename = aTargetFileName;
        if (targetFilename == null) targetFilename = getFilename(aLocation);
        if (targetFilename == null) targetFilename = createUniqueName(aLocation);

        // Create file handles for temporary part file and target file
        File targetFile = new File(aTargetDir, targetFilename);
        File partFile = new File(aTargetDir, targetFilename + ".part");

        // If file doesn't exist continue with downloading
        if (!targetFile.exists())
        {
            // Download, copy and remove part file
            downloadToPartFile(aLocation, partFile);
            partFile.renameTo(targetFile);
        }

        return targetFile;
    }

    /**
     * Reads contents of URL to the part file. If part file is already present then
     * we evaluate the position to resume from.
     *
     * @param aLocation location to get resource from.
     * @param aPartFile part file to put data at.
     *
     * @throws IOException          if I/O error happens.
     * @throws InterruptedException in case if downloading was interrupted.
     */
    private void downloadToPartFile(URL aLocation, File aPartFile)
        throws IOException, InterruptedException
    {
        // Determine the position to continue from
        long position = 0;
        if (aPartFile.exists()) position = aPartFile.length();

        OutputStream writer = null;
        InputStream reader = null;

        URLInputStream is;
        try
        {
            is = new URLInputStream(aLocation, (int)position, 0);
            NetManager.register(NetManager.TYPE_DOWNLOADS, aLocation.toString(), null, is);
            if (listener != null) is.addListener(listener);

            reader = new BufferedInputStream(is);
            writer = new BufferedOutputStream(new FileOutputStream(aPartFile, true));

            int ch = -1;
            while (!isCanceled() && (ch = reader.read()) != -1)
            {
                writer.write(ch);
            }

            if (ch != -1) throw new InterruptedException(Strings.error("net.downloading.canceled"));
        } finally
        {
            if (writer != null)
            {
                writer.flush();
                writer.close();
            }

            if (reader != null)
            {
                reader.close();
            }
        }
    }

    /**
     * Cancels downloading.
     */
    public void cancel()
    {
        canceled = true;
    }

    /**
     * Returns <code>TRUE</code> only if downloading has been canceled.
     *
     * @return <code>TRUE</code> only if downloading has been canceled.
     */
    private boolean isCanceled()
    {
        return canceled;
    }

    /**
     * Returns the file name part of the target location for local use (saving of
     * downloaded resource).
     *
     * @param aLocation location.
     *
     * @return file name or <code>NULL</code> if name not specified or location is <code>NULL</code>.
     */
    public static String getFilename(URL aLocation)
    {
        String path = aLocation == null ? null : aLocation.getPath();
        if (path != null)
        {
            int lastPathSeparator = path.lastIndexOf('/');
            if (lastPathSeparator != -1)
            {
                path = path.substring(lastPathSeparator + 1);
            }

            if (path.trim().length() == 0) path = null;
        }

        return path;
    }

    /**
     * Creates unique name for file from its location URL.
     *
     * @param aLocation location URL.
     *
     * @return unique name.
     */
    static String createUniqueName(URL aLocation)
    {
        return Long.toHexString(aLocation.toString().hashCode()).toUpperCase();
    }

    /**
     * Asks user to point the location for the resource.
     *
     * @param link              URL of the image.
     * @param reportCompletion  <code>TRUE</code> to report "Download Complete."
     */
    public static void saveResource(final URL link, final boolean reportCompletion)
    {
        // Get initial file directory
        String name = getFilename(link);
        if (name == null) name = "untitled";
        File dir = lastSaveDir == null ? new File(System.getProperty("user.home")) : lastSaveDir;
        File file = new File(dir, name);

        // Select the target directory
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(file);
        fc.setMultiSelectionEnabled(false);
        int result = fc.showSaveDialog(Application.getDefaultParentFrame());

        if (result == JFileChooser.APPROVE_OPTION)
        {
            // Saving
            final File directory = fc.getSelectedFile().getParentFile();
            final String filename = fc.getSelectedFile().getName();

            lastSaveDir = directory;

            final Downloader downloader = new Downloader(null);
            Thread thread = new Thread(THREAD_NAME)
            {
                public void run()
                {
                    try
                    {
                        downloader.download(link, directory, filename);
                        if (reportCompletion)
                        {
                            JOptionPane.showMessageDialog(
                                Application.getDefaultParentFrame(),
                                MessageFormat.format(Strings.message("net.download.complete"), filename),
                                Strings.message("net.download.dialog.title"),
                                JOptionPane.INFORMATION_MESSAGE
                            );
                        }
                    } catch (InterruptedException e)
                    {
                        LOG.log(Level.SEVERE, Strings.error("interrupted"), e);
                    } catch (IOException e)
                    {
                        JOptionPane.showMessageDialog(Application.getDefaultParentFrame(),
                            MessageFormat.format(Strings.message("net.download.failed"), e.getMessage()),
                            Strings.message("net.download.dialog.title"),
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            thread.start();
        }
    }
}
