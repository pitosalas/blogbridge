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
// $Id: AbstractHsqlPersistenceTestCase.java,v 1.12 2006/10/16 10:12:39 spyromus Exp $
//

package com.salas.bb.persistence.backend;

import junit.framework.TestCase;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

import com.salas.bb.utils.TUtils;
import com.salas.bb.utils.FileUtils;

/**
 * Abstract HSQL database persistence test.
 */
abstract class AbstractHsqlPersistenceTestCase extends TestCase
{
    protected static final String DATABASE_PATH = "/persistence-test";

    /** Prefix to relative path to get to the root of source data -- root classpath. */
    protected static String sourcePrefix = "";
    /** Prefix to repative path to get to the root of destination. */
    protected static String destPrefix = "..";
    /** Context path. */
    protected static String contextPath;

    /** Manager to use in tests. */
    protected HsqlPersistenceManager pm;

    /**
     * Destination test database dir.
     * Created and cleared by this case during setup and tear-down.
     */
    private File destDir;

    // Detect the location we are at and try to set the prefixes for source and dest.
    static
    {
        HsqlPersistenceManager.hasGUI = false;

        detectDataLocation();
        contextPath = destPrefix + DATABASE_PATH + "/";
    }

    private Map loggingLevels;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        destDir = null;
        loggingLevels = new HashMap();
    }

    protected void tearDown()
        throws Exception
    {
        enableAllLogging();

        // Shutdown database
        if (pm != null) pm.shutdown();

        // Clear the data directory
        if (destDir != null && destDir.exists()) FileUtils.rmdir(destDir);

        // Clear everything else
        super.tearDown();
    }

    /**
     * Prepares a manager. Copies database files from the specified database dir and
     * initializes HSQL engine to work with them.
     *
     * @param sourceDatabase path from the root of classpath to database dir.
     */
    protected void initManager(String sourceDatabase)
    {
        initManager(sourceDatabase, true);
    }

    /**
     * Prepares a manager. Copies database files from the specified database dir and
     * initializes HSQL engine to work with them.
     *
     * @param sourceDatabase path from the root of classpath to database dir.
     * @param aDoBackupOnUpgrade <code>TRUE</code> to backup data on schema upgrades.
     */
    protected void initManager(String sourceDatabase, boolean aDoBackupOnUpgrade)
    {
        try
        {
            installDatabase(sourceDatabase);
            pm = new HsqlPersistenceManager(contextPath, aDoBackupOnUpgrade);
        } catch (IOException e)
        {
            fail("Failed to initialize database.");
        }
    }

    /**
     * Prepares database.
     *
     * @param sourceDatabase source database path.
     *
     * @throws java.io.IOException if I/O error happens.
     */
    protected void installDatabase(String sourceDatabase)
        throws IOException
    {
        // Copy database
        destDir = TUtils.resetDir(destPrefix + DATABASE_PATH);
        copyDatabase(destDir, new File(sourcePrefix + sourceDatabase));
    }

    protected static void copyDatabase(File destDir, File sourceDir)
        throws IOException
    {
        if (!sourceDir.exists()) fail("Source dir doesn't exist: " + sourceDir.getPath());
        if (sourceDir.isFile()) fail("Source dir is a file: " + sourceDir.getPath());

        File[] files = sourceDir.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            File file = files[i];
            if (file.getName().startsWith("blogbridge.")) FileUtils.copyFileToDir(file, destDir);
        }
    }

    /**
     * Tries to detect the placement of data and tells the appendixes to use
     * to access requireed dirs.
     */
    private static void detectDataLocation()
    {
        File dataDir = new File("data");
        if (!dataDir.exists())
        {
            File testDir = new File("test");
            if (!testDir.exists())
            {
                File blogbridgeDir = new File("blogbridge");
                if (!blogbridgeDir.exists() || !(new File("blogbridge/test")).exists())
                {
                    throw new RuntimeException("Cannot find data dir.");
                } else
                {
                    sourcePrefix = "blogbridge/target/classes";
                    destPrefix = "blogbridge/target";
                }
            } else
            {
                sourcePrefix = "target/classes";
                destPrefix = "target";
            }
        }
    }

    /**
     * Disables logging of the class.
     *
     * @param clazz class.
     */
    protected void disableLogging(Class clazz)
    {
        if (clazz == null) return;

        synchronized (loggingLevels)
        {
            Level previousLevel = (Level)loggingLevels.get(clazz);

            // Don't disable anything twice
            if (previousLevel != null) return;

            Logger logger = Logger.getLogger(clazz.getName());
            loggingLevels.put(clazz, logger.getLevel());
            logger.setLevel(Level.OFF);
        }
    }

    /**
     * Enables logging of the class.
     *
     * @param clazz class to enable.
     */
    protected void enableLogging(Class clazz)
    {
        if (clazz == null) return;

        synchronized (loggingLevels)
        {
            Level level = (Level)loggingLevels.get(clazz);
            loggingLevels.remove(clazz);

            Logger logger = Logger.getLogger(clazz.getName());
            logger.setLevel(level);
        }
    }

    /**
     * Enables all logging.
     */
    private void enableAllLogging()
    {
        synchronized (loggingLevels)
        {
            Set clazzes = loggingLevels.keySet();
            for (Iterator it = clazzes.iterator(); it.hasNext();)
            {
                Class clazz = (Class)it.next();
                enableLogging(clazz);
            }
        }
    }
}
