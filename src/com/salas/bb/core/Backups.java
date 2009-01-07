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
// $Id: Backups.java,v 1.7 2006/05/30 10:31:15 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.domain.*;
import com.salas.bb.utils.opml.Converter;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bbutilities.opml.export.Exporter;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.*;
import java.text.SimpleDateFormat;

import org.jdom.output.XMLOutputter;
import org.jdom.Document;

/**
 * <p>Backups manager, which saves the OPML backup on demand and controls the population of
 * backups in the working directory.</p>
 *
 * <p>The backups directory and number of last backups to keep are input parameters.</p>
 *
 * <p>The name of backup file is controlled with <code>FILENAME_FORMAT</code> property.
 * At the present moment the names will look like: <code>~2005-05-31_142501.opml</code>
 * which corresponds to: <code>31 May 2005, 14:25:01</code>.</p>
 *
 * <p>The manager utilitizes the same functionality as Synchronization module, meaning
 * that the contents of synchronization and backup OPML are completely identical.</p>
 */
public final class Backups
{
    private static final SimpleDateFormat FILENAME_FORMAT =
        new SimpleDateFormat("'~'yyyy-MM-dd_HHmmss'.opml'");

    private static final String FILENAME_PATTERN =
        "^~[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{6}\\.opml$";

    /**
     * The title of the main OPML outline.
     */
    private static final String OPML_TITLE = "BlogBridge Backup";

    private final int   lastBackupsToKeep;
    private final File  backupsDir;

    /**
     * Initializes backups saver with directory and limit value.
     *
     * @param aBackupsDir           directory to save backups to. If it is not present yet
     *                              it will be created during saving.
     * @param aLastBackupsToKeep    number of backup files to keep (greater than 0). The old
     *                              backup files will be removed.
     *
     * @throws NullPointerException if backups directory isn't told.
     * @throws IllegalArgumentException if last backups to keep is not positive value.
     */
    public Backups(File aBackupsDir, int aLastBackupsToKeep)
    {
        if (aBackupsDir == null) throw new NullPointerException(Strings.error("backup.directory.is.unspecified"));
        if (aLastBackupsToKeep <= 0) throw new IllegalArgumentException(Strings.error("backup.non.positive.limit"));

        backupsDir = aBackupsDir;
        lastBackupsToKeep = aLastBackupsToKeep;
    }

    /**
     * Stores the guides set as OPML in backups folder. If backups folder doesn't exist,
     * it will be created. If this backup will overjump the limitation of backups to keep,
     * the last backups will be removed. Empty guides sets are also allowed.
     *
     * @param set guides set to store.
     *
     * @throws NullPointerException if the set isn't specified.
     */
    public void saveBackup(GuidesSet set)
        throws IOException
    {
        initBackupsDir();
        storeSet(set);
        rotateBackups();
    }

    /**
     * Creates backup directory if the last doesn't exist yet.
     *
     * @throws IOException if creation has failed.
     */
    private void initBackupsDir()
        throws IOException
    {
        if (!backupsDir.exists())
        {
            if (!backupsDir.mkdir()) throw new IOException(Strings.error("backup.failed.to.create.backup.directory"));
        }
    }

    /**
     * Exports the set to OPML and writes to backup file, which name is created from
     * current data and time.
     *
     * @param set   set to export.
     *
     * @throws IOException if output operation fails.
     */
    private void storeSet(GuidesSet set)
        throws IOException
    {
        com.salas.bbutilities.opml.export.Exporter exporter = new Exporter(true);
        Document doc = exporter.export(Converter.convertToOPML(set, OPML_TITLE));

        writeBackupToFile(doc, new File(backupsDir, createBackupFileName()));
    }

    /**
     * Writes backup OPML data to the file.
     *
     * @param aBackupOPMLDocument   exported OPML document.
     * @param aBackupFile           backup file.
     *
     * @throws IOException if output operation fails.
     */
    private static void writeBackupToFile(Document aBackupOPMLDocument, File aBackupFile)
        throws IOException
    {
        FileOutputStream fos = new FileOutputStream(aBackupFile);
        XMLOutputter xo = new XMLOutputter();
        xo.output(aBackupOPMLDocument, fos);
        fos.close();
    }

    /**
     * Creates name of the backup file from current date and time.
     *
     * @return backup file name.
     */
    private String createBackupFileName()
    {
        return FILENAME_FORMAT.format(new Date());
    }

    /**
     * Analyzes the list of backup files and removes oldest which are overjumping the
     * specified number of backups to keep.
     */
    private void rotateBackups()
    {
        File[] backupFiles = backupsDir.listFiles(new BackupFilenameFilter());
        Collection filesToRemove = chooseBackupsToRemove(backupFiles);

        Iterator it = filesToRemove.iterator();
        while (it.hasNext())
        {
            File backupFile = (File)it.next();
            backupFile.delete();
        }
    }

    /**
     * Selects the files from the list which have jumped over the limitation.
     *
     * @param aBackupFiles backup files.
     *
     * @return collection of files to remove.
     */
    private Collection chooseBackupsToRemove(File[] aBackupFiles)
    {
        List backupsToRemove = new ArrayList();

        if (aBackupFiles.length > lastBackupsToKeep)
        {
            SortedSet sortedBackupFiles = new TreeSet(new FilesLastModTimeComparator());
            sortedBackupFiles.addAll(Arrays.asList(aBackupFiles));

            int toRemove = aBackupFiles.length - lastBackupsToKeep;
            Iterator it = sortedBackupFiles.iterator();
            while (toRemove > 0 && it.hasNext())
            {
                Object backupFile = it.next();
                backupsToRemove.add(backupFile);
                toRemove--;
            }
        }

        return backupsToRemove;
    }

    /**
     * Compares files by their modification times.
     */
    private static class FilesLastModTimeComparator implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            long modTime1 = ((File)o1).lastModified();
            long modTime2 = ((File)o2).lastModified();

            return modTime1 > modTime2 ? 1 : modTime1 < modTime2 ? -1 : 0;
        }
    }
    /**
     * Filter for backup files.
     */
    private static class BackupFilenameFilter implements FilenameFilter
    {
        public boolean accept(File dir, String name)
        {
            return name != null && name.matches(FILENAME_PATTERN);
        }
    }
}
