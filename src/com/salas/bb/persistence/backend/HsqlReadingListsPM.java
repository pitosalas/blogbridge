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
// $Id: HsqlReadingListsPM.java,v 1.9 2006/05/30 08:25:27 spyromus Exp $
//

package com.salas.bb.persistence.backend;

import com.salas.bb.domain.StandardGuide;
import com.salas.bb.domain.ReadingList;
import com.salas.bb.domain.DirectFeed;
import com.salas.bb.persistence.PersistenceException;
import com.salas.bb.utils.i18n.Strings;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.MessageFormat;

/**
 * HSQL manager of reading lists.
 */
final class HsqlReadingListsPM
{
    private static final Logger LOG = Logger.getLogger(HsqlReadingListsPM.class.getName());

    /** Persistence manager context. */
    private final HsqlPersistenceManager context;

    /** Reading list is already in database. */
    private static final String MSG_ALREADY_IN_DB = Strings.error("db.reading.list.is.already.in.database");
    /** Reading list is not in database yet. */
    private static final String MSG_NOT_IN_DB = Strings.error("db.reading.list.is.not.in.database");
    /** Reading list is not specified. */
    private static final String MSG_UNSPECIFIED = Strings.error("unspecified.reading.list");
    /** Reading list has no guide assigned and cannot be saved. */
    private static final String MSG_NO_GUIDE_ASSIGNED = Strings.error("db.no.guide.assigned.to.reading.list");
    /** Reading list has guide assigned which isn't in the database yet. */
    private static final String MSG_GUIDE_TRANSIENT = Strings.error("db.guide.is.transient");

    /**
     * Creates manager.
     *
     * @param aContext context used for communication back.
     */
    public HsqlReadingListsPM(HsqlPersistenceManager aContext)
    {
        context = aContext;
    }

    /**
     * Inserts reading list into database.
     *
     * @param list  reading list to insert.
     *
     * @throws NullPointerException if reading list isn't specified.
     * @throws IllegalStateException if reading list is already in database, if it has no
     *                               guide assigned, or the guide is transient.
     * @throws SQLException if database operation fails.
     */
    public void insertReadingList(ReadingList list)
        throws SQLException
    {
        if (list == null) throw new NullPointerException(MSG_UNSPECIFIED);
        if (list.getID() != -1) throw new IllegalStateException(MSG_ALREADY_IN_DB);

        StandardGuide guide = list.getParentGuide();
        if (guide == null) throw new IllegalStateException(MSG_NO_GUIDE_ASSIGNED);
        if (guide.getID() == -1) throw new IllegalStateException(MSG_GUIDE_TRANSIENT);

        PreparedStatement stmt = context.getPreparedStatement(
            "INSERT INTO READINGLISTS (GUIDEID, TITLE, URL, LASTPOLLTIME, LASTUPDATESERVERTIME," +
                "LASTSYNCTIME)" +
            " VALUES (?, ?, ?, ?, ?, ?)");

        try
        {
            stmt.setLong(1, guide.getID());
            stmt.setString(2, list.getTitle());
            stmt.setString(3, list.getURL().toString());
            stmt.setLong(4, list.getLastPollTime());
            stmt.setLong(5, list.getLastUpdateServerTime());
            stmt.setLong(6, list.getLastSyncTime());

            int rows = stmt.executeUpdate();
            if (rows == 0) throw new SQLException(Strings.error("db.failed.to.insert.row.for.reading.list"));

            list.setID(context.getInsertedID());
        } finally
        {
            stmt.close();
        }
    }

    /**
     * Removes reading list from database.
     *
     * @param list list to remove.
     *
     * @throws NullPointerException if list isn't specified.
     * @throws IllegalStateException if list is not in database.
     * @throws SQLException if database operation fails.
     */
    public void removeReadingList(ReadingList list)
        throws SQLException, PersistenceException
    {
        if (list == null) throw new NullPointerException(MSG_UNSPECIFIED);
        if (list.getID() == -1) throw new IllegalStateException(MSG_NOT_IN_DB);

        PreparedStatement stmt = context.getPreparedStatement(
            "DELETE FROM READINGLISTS WHERE ID=?");

        try
        {
            stmt.setLong(1, list.getID());
            int rows = stmt.executeUpdate();

            if (rows == 0) throw new SQLException(Strings.error("db.failed.to.remove.row.with.reading.list"));
        } finally
        {
            stmt.close();
        }

        // Clear ID's
        list.setID(-1);

        // Remove unlinked feeds
        DirectFeed[] feeds = list.getFeeds();
        for (int i = 0; i < feeds.length; i++)
        {
            DirectFeed feed = feeds[i];
            list.remove(feed);
            context.removeFeedIfNoRefs(feed);
        }
    }

    /**
     * Updates reading list information in database.
     *
     * @param list reading list to update.
     *
     * @throws NullPointerException if reading list isn't specified.
     * @throws IllegalStateException if reading list is not in database.
     * @throws SQLException if database operation fails.
     */
    public void updateReadingList(ReadingList list)
        throws SQLException
    {
        if (list == null) throw new NullPointerException(MSG_UNSPECIFIED);
        if (list.getID() == -1) throw new IllegalStateException(MSG_NOT_IN_DB);

        PreparedStatement stmt = context.getPreparedStatement(
                "UPDATE READINGLISTS SET TITLE=?, URL=?, LASTPOLLTIME=?, LASTSYNCTIME=?, " +
                    "LASTUPDATESERVERTIME=? WHERE ID=?");

        try
        {
            stmt.setString(1, list.getTitle());
            stmt.setString(2, list.getURL().toString());
            stmt.setLong(3, list.getLastPollTime());
            stmt.setLong(4, list.getLastSyncTime());
            stmt.setLong(5, list.getLastUpdateServerTime());
            stmt.setLong(6, list.getID());

            int rows = stmt.executeUpdate();
            if (rows == 0)
            {
                LOG.log(Level.SEVERE, MessageFormat.format(
                    Strings.error("db.hsql.updated.0.rows.at.readinglists.listid.0"),
                    new Object[] { new Long(list.getID()) }));
            }
        } finally
        {
            stmt.close();
        }
    }
}
