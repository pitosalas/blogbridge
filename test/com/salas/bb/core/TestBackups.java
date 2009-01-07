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
// $Id: TestBackups.java,v 1.6 2006/11/15 11:50:24 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.domain.*;
import com.salas.bb.domain.querytypes.QueryType;
import com.salas.bb.utils.TUtils;
import com.salas.bb.utils.FileUtils;
import com.salas.bbutilities.opml.Importer;
import com.salas.bbutilities.opml.objects.OPMLGuide;
import com.salas.bbutilities.opml.objects.DirectOPMLFeed;
import com.salas.bbutilities.opml.objects.QueryOPMLFeed;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This suite contains tests for <code>Backups</code> unit.
 * It covers:
 * <ul>
 *  <li>TODO: put here</li>
 * </ul>
 */
public class TestBackups extends TestCase
{
    /** Note that this value is necessary for rotation test. */
    private static final int LAST_BACKUPS_TO_KEEP = 1;

    private Backups backups;
    private File    backupsDir;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        backupsDir = TUtils.resetDir("backups");
        backups = new Backups(backupsDir, LAST_BACKUPS_TO_KEEP);
    }

    protected void tearDown()
        throws Exception
    {
        FileUtils.rmdir(backupsDir);
        super.tearDown();
    }

    /**
     * Tests saving empty backup. Empty backups also should be saved.
     */
    public void testSaveBackupEmpty()
        throws IOException
    {
        GuidesSet set = new GuidesSet();

        backups.saveBackup(set);

        File[] backupFiles = backupsDir.listFiles();
        assertEquals("Wrong number of files in backup folder.", 1, backupFiles.length);

        File backupFile = backupFiles[0];
        OPMLGuide[] guides = restoreBackup(backupFile);

        assertEquals("Should be no guides.", 0, guides.length);
    }

    /**
     * Tests saving the backup of two guides with direct and query feeds.
     */
    public void testSaveBackup()
        throws IOException
    {
        GuidesSet set = new GuidesSet();

        StandardGuide guide1 = new StandardGuide();
        guide1.setTitle("1");
        guide1.setIconKey("1");
        set.add(guide1);

        DirectFeed feed1 = new DirectFeed();
        feed1.setBaseTitle("1t");
        feed1.setXmlURL(new URL("file://1x"));
        feed1.setRating(4);
        guide1.add(feed1);

        StandardGuide guide2 = new StandardGuide();
        guide2.setTitle("2");
        guide2.setIconKey("2");
        set.add(guide2);

        QueryFeed feed2 = new QueryFeed();
        feed2.setBaseTitle("2t");
        feed2.setQueryType(QueryType.getQueryType(QueryType.TYPE_DELICIOUS));
        feed2.setParameter("2p");
        guide2.add(feed2);

        // Do backup and check

        backups.saveBackup(set);

        File[] backupFiles = backupsDir.listFiles();
        assertEquals("Wrong number of files in backup folder.", 1, backupFiles.length);

        File backupFile = backupFiles[0];
        OPMLGuide[] guides = restoreBackup(backupFile);

        // Check guides: see above init

        assertEquals("Should be 2 guides.", 2, guides.length);
        assertEquals("1", guides[0].getTitle());
        assertEquals("1", guides[0].getIcon());
        assertEquals(1, guides[0].getFeeds().size());

        DirectOPMLFeed opmlFeed1 = (DirectOPMLFeed)guides[0].getFeeds().get(0);
        assertEquals("1t", opmlFeed1.getTitle());
        assertEquals(4, opmlFeed1.getRating());

        QueryOPMLFeed opmlFeed2 = (QueryOPMLFeed)guides[1].getFeeds().get(0);
        assertEquals("2t", opmlFeed2.getTitle());
    }

    /**
     * Tests initialization failures.
     */
    public void testInitFailure()
        throws IOException
    {
        try
        {
            backups = new Backups(null, 1);
            fail("Empty backup dirs aren't allowed.");
        } catch (NullPointerException e)
        {
            // Expected
        }

        try
        {
            backups = new Backups(backupsDir, -1);
            fail("Limit should be positive.");
        } catch (IllegalArgumentException e)
        {
            // Expected
        }

        try
        {
            backups = new Backups(backupsDir, 0);
            fail("Limit shuld be positive.");
        } catch (IllegalArgumentException e)
        {
            // Expected
        }

        try
        {
            backups.saveBackup(null);
            fail("Set should be specified.");
        } catch (NullPointerException e)
        {
            // Expected
        }
    }

    /**
     * Tests naming of backup files.
     */
    public void testBackupNaming()
        throws IOException
    {
        GuidesSet set = new GuidesSet();

        backups.saveBackup(set);

        String[] backupFiles = backupsDir.list();
        String backupFile = backupFiles[0];

        SimpleDateFormat filenameFormat =
            new SimpleDateFormat("^'~'yyyy-MM-dd_HHmm'[0-9][0-9].opml'$");

        // Name: ~YYYY-mm-dd_HHMMSS.opml
        assertTrue("Wrong file name format: " + backupFile,
            backupFile.matches(filenameFormat.format(new Date())));
    }

    /**
     * Tests rotation of backup files. Rotation - it's when the new file overjumps the limit
     * of allowed backups. The oldest should be removed.
     */
    public void testBackupRotation()
        throws InterruptedException, IOException
    {
        GuidesSet set = new GuidesSet();

        backups.saveBackup(set);
        String[] backupFiles = backupsDir.list();
        String firstBackupFileName = backupFiles[0];

        // Sleep second and make another backup
        Thread.sleep(1000);
        backups.saveBackup(set);

        backupFiles = backupsDir.list();
        assertEquals("Only one backup file is allowed.", 1, backupFiles.length);
        assertFalse("Old backup file is left: ", backupFiles[0].equals(firstBackupFileName));
    }

    private static OPMLGuide[] restoreBackup(File aBackupFile)
    {
        Importer importer = new Importer();
        OPMLGuide[] guides = new OPMLGuide[0];

        try
        {
            guides = importer.process(aBackupFile.toURL(), false).getGuides();
        } catch (Exception e)
        {
            e.printStackTrace();
            fail("Failed to restore backup OPML.");
        }

        return guides;
    }
}
