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
// $Id: TestHsqlPersistenceManager.java,v 1.9 2006/09/27 16:04:23 spyromus Exp $
//

package com.salas.bb.persistence.backend;

import com.salas.bb.domain.SearchFeed;
import com.salas.bb.domain.StandardArticle;
import com.salas.bb.domain.query.ICriteria;
import com.salas.bb.domain.query.articles.ArticleTextProperty;
import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.domain.query.general.StringContainsCO;
import com.salas.bb.persistence.PersistenceException;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

/**
 * This suite contains tests for <code>HsqlPersistenceManager</code> unit.
 */
public class TestHsqlPersistenceManager extends AbstractHsqlPersistenceTestCase
{
    /**
     * Tests opening several connections in the same thread.
     *
     * @throws SQLException db error.
     */
    public void testSeveralConnectionsSingleThread()
        throws SQLException
    {
        // Init the most modern database
        initManager("/resources");

        Connection c1 = pm.getConnection();
        Connection c2 = pm.getConnection();

        assertTrue("Connections should be the same.", c1 == c2);
    }

    /**
     * Tests opening several connections in two different threads.
     *
     * @throws SQLException db error.
     * @throws InterruptedException interruption.
     */
    public void testSeveralConnectionsMultiThread()
        throws SQLException, InterruptedException
    {
        // Init the most modern database
        initManager("/resources");

        final Connection c1 = pm.getConnection();

        Thread thread = new Thread()
        {
            public void run()
            {
                try
                {
                    Connection c2 = pm.getConnection();
                    assertTrue("Connection should be the same.", c1 == c2);
                    c2.close();
                } catch (SQLException e)
                {
                    e.printStackTrace();
                    fail();
                }
            }
        };

        thread.start();
        thread.join();
    }

    /**
     * Tests the backuping the database.
     *
     * @throws IOException I/O problem.
     */
    public void testBackuping()
        throws IOException
    {
        // Init the most modern database
        initManager("/resources");

        int version = 1;
        assertFalse("There should be no backup for version 1.", pm.isBackupPresent(version));

        // Create backup folder for version 1 and check again
        pm.makeBackup(version);
        assertBackup(version);
    }

    /**
     * Tests that when removing SearchFeed it doesn't clear the ID's of
     * the articles this contained because the articles do not belong to
     * this feed.
     *
     * NOTE: It was the reason for most of "Article is not in database" errors
     */
    public void testClearFeedIDSearchFeed()
    {
        Query query = new Query();
        ICriteria criteria = query.addCriteria();
        criteria.setProperty(ArticleTextProperty.INSTANCE);
        criteria.setComparisonOperation(StringContainsCO.INSTANCE);
        criteria.setValue("*a*");

        SearchFeed feed = new SearchFeed();
        feed.setQuery(query);
        feed.setArticlesLimit(1);

        StandardArticle article = new StandardArticle("aaa");
        article.setID(1); // Make impression of that it was already saved
        feed.addArticleIfMatching(article);
        assertEquals("Article wasn't added.", 1, feed.getArticlesCount());

        // Checking
        HsqlPersistenceManager.clearFeedID(feed);
        assertFalse("Article ID shouldn't be reset because the article doesn't " +
            "belong to this feed.", -1 == article.getID());
    }

    /**
     * Tests the connection lifecycle.
     *
     * @throws SQLException db error.
     */
    public void testConnectionReopening() throws SQLException
    {
        // Init the most modern database
        initManager("/resources");

        // Get initial connection
        Connection con = pm.getConnection();
        assertNotNull(con);
        assertFalse(con.isClosed());
        assertFalse(isSessionClosed(con));

        // Close connection and verify
        pm.closeConnection();
        assertTrue(con.isClosed());

        // Get new connection and verify
        con = pm.getConnection();
        assertFalse(con.isClosed());
        assertFalse(isSessionClosed(con));
    }

    /**
     * Tests the connection lifecycle.
     *
     * @throws SQLException db error.
     */
    public void testShutdown() throws SQLException
    {
        // Init the most modern database
        initManager("/resources");

        // Shutdown database and verify that the connection is closed
        Connection con = pm.getConnection();
        pm.shutdown();
        assertTrue(con.isClosed());

        // Reopen database
        con = pm.getConnection();
        assertFalse(con.isClosed());
        assertFalse(isSessionClosed(con));
    }

    /**
     * We try to start the engine from the incorrect database files and
     * check how the database is reset and reinitialized.
     *
     * @throws SQLException db error.
     */
    public void testDatabaseReset() throws SQLException
    {
        // Init the most modern database
        initManager("/data/migration/2.23_bad");

        disableLogging(HsqlPersistenceManager.class);
        try
        {
            pm.init();
            assertTrue(pm.isDatabaseReset());
        } catch (PersistenceException e)
        {
            fail(e.getMessage());
        } finally
        {
            enableLogging(HsqlPersistenceManager.class);
        }

        // Check current schema version
        int version = pm.getCurrentSchemaVersion();
        assertTrue("Database wasn't reset", version > 0);

        // Check the status of connection
        Connection con = pm.getConnection();
        assertFalse(con.isClosed());
        assertFalse(isSessionClosed(con));
    }

    /**
     * Checks if the session is closed.
     *
     * @param con   connection.
     *
     * @return TRUE if closed.
     */
    private static boolean isSessionClosed(Connection con)
    {
        boolean closed = true;
        try
        {
            Statement stmt = con.createStatement();
            stmt.executeQuery("SELECT * FROM GUIDES");
            closed = false;
        } catch (SQLException e)
        {
            if (e.getErrorCode() != -33) fail(e.getMessage());
        }

        return closed;
    }

    /**
     * Verifies that backup is present for a given version, i.e. the directory is there
     * and it has .properties and .script files.
     *
     * @param aVersion version to look backup for.
     */
    private void assertBackup(int aVersion)
    {
        assertTrue("There should be backup for version 1.", pm.isBackupPresent(aVersion));

        File backupFolder = new File(contextPath +
            HsqlPersistenceManager.getBackupFolderName(aVersion));
        assertTrue("Backup folder should be present.", backupFolder.exists());

        List filesInBackup = Arrays.asList(backupFolder.list());
        assertTrue("blogbridge.properties wasn't copied.",
            filesInBackup.contains("blogbridge.properties"));
        assertTrue("blogbridge.script wasn't copied.",
            filesInBackup.contains("blogbridge.script"));
    }

    // --------------------------------------------------------------------------------------------
    // Deleted Feeds Management
    // --------------------------------------------------------------------------------------------

    /**
     * Tests adding new deleted object record.
     *
     * @throws PersistenceException error.
     */
    public void testAddDeletedObjectRecord() throws PersistenceException
    {
        String gt = "_gt";
        String fk = "_fk";

        // Init the most modern database
        initManager("/resources");

        // Remove all deleted records
        pm.purgeDeletedObjectRecords();

        // Check if the record is there
        assertFalse(pm.isDeletedObjectRecordPresent(gt, fk));

        // Add new records
        pm.addDeletedObjectRecord(gt, fk);

        // Check if the record is there
        assertTrue(pm.isDeletedObjectRecordPresent(gt, fk));
    }
    
    /**
     * Tests deleting newly added new deleted object record.
     *
     * @throws PersistenceException error.
     */
    public void testDeleteDeletedObjectRecord() throws PersistenceException
    {
        String gt = "_gt";
        String fk = "_fk";

        // Init the most modern database
        initManager("/resources");

        // Remove all deleted records
        pm.purgeDeletedObjectRecords();

        // Check if the record is there
        assertFalse(pm.isDeletedObjectRecordPresent(gt, fk));

        // Add new record and then delete it
        pm.addDeletedObjectRecord(gt, fk);
        pm.removeDeletedObjectRecord(gt, fk);

        // Check if the record is there
        assertFalse(pm.isDeletedObjectRecordPresent(gt, fk));
    }
}
