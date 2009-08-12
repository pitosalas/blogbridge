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
// $Id: HsqlGuidesPM.java,v 1.26 2007/10/03 11:41:56 spyromus Exp $
//

package com.salas.bb.persistence.backend;

import com.salas.bb.domain.*;
import com.salas.bb.persistence.PersistenceException;
import com.salas.bb.utils.i18n.Strings;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HSQL manager of guides. It takes care only about handling guides.
 */
final class HsqlGuidesPM
{
    private static final Logger LOG = Logger.getLogger(HsqlGuidesPM.class.getName());

    /** Persistence manager context. */
    private final HsqlPersistenceManager context;

    /** Unsupported guide type. */
    private static final String MSG_UNSUPPORTED = Strings.error("db.unsupported.guide.type");
    /** Guide is already in database. */
    private static final String MSG_ALREADY_IN_DB = Strings.error("db.guide.is.already.in.database");
    /** Guide is not in database yet. */
    private static final String MSG_NOT_IN_DB = Strings.error("db.guide.is.not.in.database");

    /**
     * Creates manager.
     *
     * @param aContext context used for communication back.
     */
    public HsqlGuidesPM(HsqlPersistenceManager aContext)
    {
        context = aContext;
    }

    /**
     * Inserts guide into database.
     *
     * @param guide guide to insert.
     * @param position position in the set.
     *
     * @throws NullPointerException if guide isn't specified.
     * @throws IllegalStateException if guide is already in database.
     * @throws IllegalArgumentException if guide is of unsupported type.
     * @throws SQLException if database operation fails.
     */
    public void insertGuide(IGuide guide, int position)
        throws SQLException
    {
        if (guide == null)
            throw new NullPointerException(HsqlPersistenceManager.MSG_GUIDE_UNSPECIFIED);
        if (!(guide instanceof StandardGuide)) throw new IllegalArgumentException(MSG_UNSUPPORTED);
        if (guide.getID() != -1) throw new IllegalStateException(MSG_ALREADY_IN_DB);

        int type = 0;

        PreparedStatement stmt = context.getPreparedStatement(
            "INSERT INTO GUIDES (TITLE, ICONKEY, TYPE, POS, AUTOFEEDSDISCOVERY, " +
                "PUBLISHINGENABLED, PUBLISHINGTITLE, PUBLISHINGTAGS, PUBLISHINGPUBLIC, " +
                "PUBLISHINGURL, LASTPUBLISHINGTIME, PUBLISHINGRATING, NOTIFICATIONSALLOWED, MOBILE)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        try
        {
            stmt.setString(1, guide.getTitle());
            stmt.setString(2, guide.getIconKey());
            stmt.setInt(3, type);
            stmt.setInt(4, position);
            stmt.setBoolean(5, guide.isAutoFeedsDiscovery());
            stmt.setBoolean(6, guide.isPublishingEnabled());
            stmt.setString(7, guide.getPublishingTitle());
            stmt.setString(8, guide.getPublishingTags());
            stmt.setBoolean(9, guide.isPublishingPublic());
            stmt.setString(10, guide.getPublishingURL());
            stmt.setLong(11, guide.getLastPublishingTime());
            stmt.setInt(12, guide.getPublishingRating());
            stmt.setBoolean(13, guide.isNotificationsAllowed());
            stmt.setBoolean(14, guide.isMobile());

            int rows = stmt.executeUpdate();
            if (rows == 0) throw new SQLException(Strings.error("db.failed.to.insert.row.for.guide"));
            guide.setID(context.getInsertedID());

            // Insert a stats record
            long now = System.currentTimeMillis();
            stmt = context.getPreparedStatement("INSERT INTO GUIDESTATS " +
                "(GUIDEID, INIT_TIME, RESET_TIME) VALUES (?, ?, ?)");
            stmt.setLong(1, guide.getID());
            stmt.setLong(2, now);
            stmt.setLong(3, now);
            rows = stmt.executeUpdate();
            if (rows == 0) throw new SQLException("Failed to add a guide stats row");
        } finally
        {
            stmt.close();
        }
    }

    /**
     * Removes guide from database.
     *
     * @param guide guide to remove.
     *
     * @throws NullPointerException if guide isn't specified.
     * @throws IllegalStateException if guide is not in database.
     * @throws SQLException if database operation fails.
     * @throws PersistenceException is database error appears.
     */
    public void removeGuide(IGuide guide)
        throws SQLException, PersistenceException
    {
        guideCheck(guide);

        removeGuideComponents(guide);
        IFeed[] feeds = guide.getFeeds();

        PreparedStatement stmt = context.getPreparedStatement(
            "DELETE FROM GUIDES WHERE ID=?");

        try
        {
            stmt.setLong(1, guide.getID());
            int rows = stmt.executeUpdate();

            if (rows == 0) throw new SQLException(Strings.error("db.failed.to.remove.row.with.guide"));
        } finally
        {
            stmt.close();
        }

        // Clear ID's
        guide.setID(-1);

        // Remove feeds
        for (IFeed feed : feeds)
        {
            feed.removeParentGuide(guide);
            context.removeFeedIfNoRefs(feed);
        }
    }

    /**
     * Removes all components of the guide.
     *
     * @param aGuide guide.
     *
     * @throws PersistenceException if persistent operation fails.
     */
    private void removeGuideComponents(IGuide aGuide)
        throws PersistenceException
    {
        if (aGuide instanceof StandardGuide)
        {
            StandardGuide sguide = (StandardGuide)aGuide;

            // Remove reading lists
            ReadingList[] lists = sguide.getReadingLists();
            for (ReadingList list : lists)
            {
                sguide.remove(list, true);

                context.removeReadingList(list);
            }
        }
    }

    /**
     * Updates guide information in database.
     *
     * @param guide guide to update.
     * @param position position in the set.
     *
     * @throws NullPointerException if guide isn't specified.
     * @throws IllegalStateException if guide is not in database.
     * @throws SQLException if database operation fails.
     */
    public void updateGuide(IGuide guide, int position)
        throws SQLException
    {
        guideCheck(guide);

        PreparedStatement stmt = context.getPreparedStatement(
            "UPDATE GUIDES SET TITLE=?, ICONKEY=?, POS=?, AUTOFEEDSDISCOVERY=?, " +
                "PUBLISHINGENABLED=?, PUBLISHINGTITLE=?, PUBLISHINGTAGS=?, PUBLISHINGPUBLIC=?, " +
                "PUBLISHINGURL=?, LASTPUBLISHINGTIME=?, PUBLISHINGRATING=?, LASTUPDATETIME=?, " +
                "NOTIFICATIONSALLOWED=?, MOBILE=? " +
            "WHERE ID=?");

        try
        {
            stmt.setString(1, guide.getTitle());
            stmt.setString(2, guide.getIconKey());
            stmt.setInt(3, position);
            stmt.setBoolean(4, guide.isAutoFeedsDiscovery());
            stmt.setBoolean(5, guide.isPublishingEnabled());
            stmt.setString(6, guide.getPublishingTitle());
            stmt.setString(7, guide.getPublishingTags());
            stmt.setBoolean(8, guide.isPublishingPublic());
            stmt.setString(9, guide.getPublishingURL());
            stmt.setLong(10, guide.getLastPublishingTime());
            stmt.setInt(11, guide.getPublishingRating());
            stmt.setLong(12, guide.getLastUpdateTime());
            stmt.setBoolean(13, guide.isNotificationsAllowed());
            stmt.setBoolean(14, guide.isMobile());
            stmt.setLong(15, guide.getID());

            int rows = stmt.executeUpdate();
            if (rows == 0)
            {
                LOG.log(Level.SEVERE, MessageFormat.format(
                    Strings.error("db.hsql.updated.0.rows.at.guides.guideid.0"),
                    guide.getID()));
            }
        } finally
        {
            stmt.close();
        }
    }

    public void updateGuidePositions(GuidesSet set)
        throws SQLException
    {
        PreparedStatement stmt = context.getPreparedStatement(
            "UPDATE GUIDES SET POS=? WHERE ID=?");

        int count = set.getGuidesCount();
        int pos = 0;
        for (int i = 0; i < count; i++)
        {
            IGuide guide = set.getGuideAt(i);
            if (guide.getID() != -1)
            {
                stmt.setInt(1, pos++);
                stmt.setLong(2, guide.getID());
                stmt.executeUpdate();
            }
        }
    }

    /**
     * Adds new record to the list of the deleted feeds associated with the guide.
     *
     * @param guide     guide.
     * @param feedKey   feed key.
     *
     * @throws SQLException if database operation fails.
     */
    public void addDeletedFeedToGuide(IGuide guide, String feedKey)
        throws SQLException
    {
        guideCheck(guide);

        PreparedStatement stmt = context.getPreparedStatement(
            "INSERT INTO DELETEDFEEDS (GUIDEID, FEEDKEY) VALUES (?, ?)");

        try
        {
            stmt.setLong(1, guide.getID());
            stmt.setString(2, feedKey);

            int rows = stmt.executeUpdate();
            if (rows == 0)
            {
                LOG.log(Level.SEVERE, MessageFormat.format(
                    Strings.error("db.nothing.added.to.deletedfeeds.guideid.0.feedkey.1"),
                    guide.getID(), feedKey));
            }
        } finally
        {
            stmt.close();
        }
    }

    /**
     * Removes all records about deleted feeds associated with the given guide.
     *
     * @param guide guide.
     *
     * @throws SQLException if database operation fails.
     */
    public void removeDeletedFeedsFromGuide(IGuide guide)
        throws SQLException
    {
        guideCheck(guide);

        PreparedStatement stmt = context.getPreparedStatement(
            "DELETE FROM DELETEDFEEDS WHERE GUIDEID=?");

        try
        {
            stmt.setLong(1, guide.getID());
            stmt.executeUpdate();
        } finally
        {
            stmt.close();
        }
    }

    /**
     * Verification of the guide.
     *
     * @param guide guide to verify.
     */
    private static void guideCheck(IGuide guide)
    {
        if (guide == null)
            throw new NullPointerException(HsqlPersistenceManager.MSG_GUIDE_UNSPECIFIED);
        if (!(guide instanceof StandardGuide)) throw new IllegalArgumentException(MSG_UNSUPPORTED);
        if (guide.getID() == -1) throw new IllegalStateException(MSG_NOT_IN_DB);
    }
}
