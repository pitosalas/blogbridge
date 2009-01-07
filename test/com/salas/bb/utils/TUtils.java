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
// $Id: TUtils.java,v 1.6 2007/10/01 09:30:13 spyromus Exp $
//

package com.salas.bb.utils;

import junit.framework.Assert;

import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Collection of utils for tests.
 */
public final class TUtils
{
    /**
     * Hidden constructor of utility class.
     */
    private TUtils()
    {
    }

    /**
     * Creates the dir or cleans it if exists.
     *
     * @param path path.
     *
     * @return handle to prepared directory.
     */
    public static File resetDir(String path)
    {
        File dir = new File(path);
        if (dir.exists())
        {
            FileUtils.rmdir(dir);
            if (dir.exists()) Assert.fail("Failed to clean dir: " + path);
        }

        if (!dir.mkdir()) Assert.fail("Failed to create dir: " + path);

        return dir;
    }

    /**
     * Dumps the contents of a result set.
     *
     * @param rs result set.
     *
     * @throws SQLException if database errors out.
     */
    public static void dumpResultSet(ResultSet rs)
        throws SQLException
    {
        ResultSetMetaData md = rs.getMetaData();
        int cc = md.getColumnCount();
        int r = 1;
        while (rs.next())
        {
            System.out.println("Record " + (r++));
            for (int i = 1; i <= cc; i++)
            {
                System.out.println("  " + md.getColumnName(i) + ": " + rs.getObject(i));
            }
        }
    }

    /**
     * Returns a path to the test data or throws an exception.
     *
     * @return data path.
     *
     * @throws IllegalStateException if can't find the directory.
     */
    public static File getTestDataPath()
    {
        if (new File("data").exists()) return new File("data");
        if (new File("test/data").exists()) return new File("test/data");

        throw new IllegalStateException("Can't find the data directory. Current path: " +
            new File(".").getAbsolutePath());
    }
}
