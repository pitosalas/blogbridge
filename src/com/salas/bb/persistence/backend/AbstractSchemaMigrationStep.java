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
// $Id: AbstractSchemaMigrationStep.java,v 1.8 2006/07/28 10:39:53 spyromus Exp $
//

package com.salas.bb.persistence.backend;

import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.persistence.backend.migration.ISchemaMigrationStep;
import com.salas.bb.persistence.backend.migration.MigrationException;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Abstract database migration step.
 */
abstract class AbstractSchemaMigrationStep implements ISchemaMigrationStep
{
    private static final int ERR_UNKNOWN_CONSTRAINT = -40;
    private static final int ERR_UNKNOWN_TABLE = -22;

    protected Connection connection;
    protected HsqlPersistenceManager manager;

    /**
     * Migrates from some version to the other.
     *
     * @param aConnection connection to use.
     * @param aManager persistence manager to use for data operations.
     *
     * @throws MigrationException in case of any problems with procedure.
     */
    public final void perform(Connection aConnection, HsqlPersistenceManager aManager)
        throws MigrationException
    {
        connection = aConnection;
        manager = aManager;

        try
        {
            perform();
        } catch (SQLException e)
        {
            throw new MigrationException(Strings.error("db.failed.to.update.database"), e);
        }
    }

    /**
     * Migrates from some version to the other.
     *
     * @throws MigrationException in case of any problems with procedure.
     * @throws SQLException in case of any database problems.
     */
    protected abstract void perform() throws MigrationException, SQLException;

    /**
     * Converts String to URL.
     *
     * @param string string to convert.
     *
     * @return URL or <code>NULL</code> if string is <code>NULL</code> or unconvertable.
     */
    protected static URL urlFromString(String string)
    {
        URL url = null;

        if (string != null)
        {
            try
            {
                url = new URL(string);
            } catch (MalformedURLException e)
            {
                // Bad URL
            }
        }

        return url;
    }

    /**
     * Deserializes URL from bytes stream.
     *
     * @param stream stream of bytes.
     *
     * @return URL or NULL if the stream was empty or damaged.
     */
    protected static URL urlFromStream(byte[] stream)
    {
        URL url = null;

        if (stream != null)
        {
            ByteArrayInputStream is = new ByteArrayInputStream(stream);
            try
            {
                ObjectInputStream os = new ObjectInputStream(is);
                url = (URL)os.readObject();
            } catch (Exception e)
            {
                url = null;
            }
        }

        return url;
    }

    /**
     * Looks for a table in database with a given name.
     *
     * @param con       connection to use.
     * @param table     name of the target table.
     *
     * @return <code>TRUE</code> if table is there.
     *
     * @throws SQLException if database error happened.
     */
    protected boolean isTablePresent(Connection con, String table)
        throws SQLException
    {
        boolean present = true;
        try
        {
            con.createStatement().executeQuery("SELECT * FROM " + table + " WHERE 1=0");
        } catch (SQLException e)
        {
            if (e.getErrorCode() == ERR_UNKNOWN_TABLE)
            {
                present = false;
            } else
            {
                throw e;
            }
        }

        return present;
    }

    /**
     * Drops the list of tables if they are in database. It's not a problem if tables aren't there.
     *
     * @param con       connection to use.
     * @param tables    list of tables.
     *
     * @throws SQLException if there's database error.
     */
    protected static void dropTables(Connection con, String[] tables)
        throws SQLException
    {
        for (int i = 0; i < tables.length; i++)
        {
            String table = tables[i];
            dropTable(con, table);
        }
    }

    /**
     * Drops database table with a given name. It's not a problem if table isn't there.
     *
     * @param con       conneciton to use.
     * @param table     name of the table.
     *
     * @throws SQLException if there's database error.
     */
    private static void dropTable(Connection con, String table)
        throws SQLException
    {
        Statement stmt = con.createStatement();
        stmt.execute("DROP TABLE " + table + " IF EXISTS;");
    }

    /**
     * Drops the constraint on the table.
     *
     * @param con           connection to use.
     * @param table         name of the table.
     * @param constraint    constraint name.
     *
     * @throws SQLException if there's database error.
     */
    protected void dropConstraint(Connection con, String table, String constraint)
        throws SQLException
    {
        Statement stmt = con.createStatement();
        try
        {
            stmt.execute("ALTER TABLE " + table + " DROP CONSTRAINT " + constraint + ";");
        } catch (SQLException e)
        {
            int errorCode = e.getErrorCode();
            if (errorCode != ERR_UNKNOWN_CONSTRAINT && errorCode != ERR_UNKNOWN_TABLE) throw e;
        }
    }

    /**
     * Returns <code>TRUE</code> if the statement should be skipped for some reason.
     * For example, it's empty, or it's a forbidden statement ("CREATE USER...", "CREATE ALIAS..."
     * and so on).
     *
     * @param stmt statement questioned.
     *
     * @return <code>TRUE</code> if statement shouldn't be executed.
     */
    static boolean skipStatement(String stmt)
    {
        boolean skip = true;

        if (stmt != null && (stmt = stmt.trim()).length() > 0)
        {
            skip = stmt.startsWith("CREATE USER") || stmt.startsWith("CREATE ALIAS");
        }

        return skip;
    }

    /**
     * Performs update operation and returns number of row changed.
     *
     * @param stmt statement to execute.
     *
     * @return number of rows changed.
     *
     * @throws SQLException if database operation fails.
     */
    int update(String stmt)
        throws SQLException
    {
        return connection.createStatement().executeUpdate(stmt);
    }
}
