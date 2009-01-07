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

package com.salas.bb.persistence.backend.migration;

import com.salas.bb.persistence.backend.HsqlPersistenceManager;

import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract schema migration class with tools.
 */
public abstract class AbstractSchema implements ISchemaMigrationStep
{
    private static final String MSG_FAILED_ADD_COLUMN = "Failed to add column {0} to {1}";
    private static final String MSG_FAILED_DROP_COLUMN = "Failed to drop column {0}.{1}";
    private static final String MSG_FAILED_QUERY = "Failed to run query {0}";
    private static final String MSG_FAILED_UPDATE = "Failed to run update query {0}";
    private static final String MSG_FAILED_DROP_CONSTRAINT = "Failed to drop constraint {0}.{1}";
    private static final String MSG_FAILED_RENAME_COLUMN = "Failed to rename {0}.{1} to {2}";
    private static final String MSG_FAILED_ADD_TABLE = "Failed to add table {0}";
    private static final String MSG_FAILED_RENAME_TABLE = "Failed to rename {0} to {2}";

    private static final String STMT_ADD_COLUMN = "ALTER TABLE {0} ADD COLUMN {1}";
    private static final String STMT_DROP_COLUMN = "ALTER TABLE {0} DROP COLUMN {1}";
    private static final String STMT_DROP_CONSTRAINT = "ALTER TABLE {0} DROP CONSTRAINT {1}";
    private static final String STMT_RENAME_COLUMN = "ALTER TABLE {0} ALTER COLUMN {1} RENAME TO {2}";
    private static final String STMT_RENAME_TABLE = "ALTER TABLE {0} RENAME TO {1}";

    /**
     * Migrates from some version to the other.
     *
     * @param con connection to use.
     * @param pm  persistence manager to use for data operations.
     *
     * @throws MigrationException in case of any problems with procedure.
     */
    public abstract void perform(Connection con, HsqlPersistenceManager pm) throws MigrationException;

    /**
     * Selects rows from the database.
     *
     * @param con       connection.
     * @param query     SQL query string.
     *
     * @return an array of rows.
     *
     * @throws MigrationException exception.
     */
    Object[][] selectRows(Connection con, String query) throws MigrationException
    {
        List<Object[]> rows = null;

        try
        {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Get number of columns
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();

            while (rs.next())
            {
                if (rows == null) rows = new ArrayList<Object[]>();

                Object[] row = new Object[cols];
                for (int i = 0; i < cols; i++) row[i] = rs.getObject(i + 1);

                rows.add(row);
            }
        } catch (SQLException e)
        {
            throw new MigrationException(MessageFormat.format(MSG_FAILED_QUERY, query), e);
        }

        return rows == null ? new Object[0][] : rows.toArray(new Object[rows.size()][]);
    }

    /**
     * Updates database and return number of updated rows.
     *
     * @param con       connection.
     * @param query     query to run.
     *
     * @return number of updated rows.
     *
     * @throws MigrationException exception.
     */
    int update(Connection con, String query) throws MigrationException
    {
        return update(con, query, null);
    }

    /**
     * Updates database and return number of updated rows.
     *
     * @param con       connection.
     * @param query     query to run.
     * @param failMessage the message to dump on failure.
     *
     * @return number of updated rows.
     *
     * @throws MigrationException exception.
     */
    int update(Connection con, String query, String failMessage)
        throws MigrationException
    {
        int updated;

        try
        {
            updated = con.createStatement().executeUpdate(query);
        } catch (SQLException e)
        {
            String msg = failMessage;
            if (msg == null) msg = MessageFormat.format(MSG_FAILED_UPDATE, query);
            throw new MigrationException(msg, e);
        }

        return updated;
    }

    /**
     * Adds a table.
     *
     * @param con       connection.
     * @param def       definition.
     *
     * @throws MigrationException exception.
     */
    void addTable(Connection con, String def)
            throws MigrationException
    {
        update(con, def, MessageFormat.format(MSG_FAILED_ADD_TABLE, def));
    }

    /**
     * Adds the column to the table.
     *
     * @param con       connection.
     * @param table     table.
     * @param colDef    column definition.
     *
     * @throws MigrationException exception.
     */
    void addColumn(Connection con, String table, String colDef)
        throws MigrationException
    {
        String query = MessageFormat.format(STMT_ADD_COLUMN, table, colDef);
        String msg = MessageFormat.format(MSG_FAILED_ADD_COLUMN, colDef, table);

        update(con, query, msg);
    }

    /**
     * Drops the column from the table.
     *
     * @param con       connection.
     * @param table     table.
     * @param columnName column definition.
     *
     * @throws MigrationException exception.
     */
    void dropColumn(Connection con, String table, String columnName)
        throws MigrationException
    {
        String query = MessageFormat.format(STMT_DROP_COLUMN, table, columnName);
        String msg = MessageFormat.format(MSG_FAILED_DROP_COLUMN, table, columnName);

        update(con, query, msg);
    }

    /**
     * Drops constraint.
     *
     * @param con       connection.
     * @param table     table name.
     * @param constraintName    constraint name.
     *
     * @throws MigrationException migration exception.
     */
    void dropConstraint(Connection con, String table, String constraintName)
        throws MigrationException
    {
        String query = MessageFormat.format(STMT_DROP_CONSTRAINT, table, constraintName);
        String msg = MessageFormat.format(MSG_FAILED_DROP_CONSTRAINT, table, constraintName);

        update(con, query, msg);
    }

    void renameColumn(Connection con, String table, String columnName, String newName)
        throws MigrationException
    {
        String query = MessageFormat.format(STMT_RENAME_COLUMN, table, columnName, newName);
        String msg = MessageFormat.format(MSG_FAILED_RENAME_COLUMN, table, columnName, newName);

        update(con, query, msg);
    }

    void renameTable(Connection con, String table, String newName)
        throws MigrationException
    {
        String query = MessageFormat.format(STMT_RENAME_TABLE, table, newName);
        String msg = MessageFormat.format(MSG_FAILED_RENAME_TABLE, table, newName);

        update(con, query, msg);
    }
}
