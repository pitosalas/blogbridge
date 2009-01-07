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
// $Id: HsqlFeedsPM.java,v 1.52 2008/02/28 15:59:51 spyromus Exp $
//

package com.salas.bb.persistence.backend;

import com.salas.bb.domain.*;
import com.salas.bb.domain.querytypes.QueryType;
import com.salas.bb.persistence.PersistenceException;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HSQL feeds persistence manager.
 */
final class HsqlFeedsPM
{
    private static final Logger LOG = Logger.getLogger(HsqlFeedsPM.class.getName());

    /** Persistence manager context. */
    private final HsqlPersistenceManager context;

    /** Feed is not in database yet. */
    private static final String MSG_NOT_IN_DB = Strings.error("db.feed.is.not.in.database");
    /** Feed is already in database. */
    private static final String MSG_ALREADY_IN_DB = Strings.error("db.feed.is.already.in.database");
    /** Feed is of unsupported type. */
    private static final String MSG_UNSUPPORTED_TYPE = Strings.error("db.feed.is.of.unsupported.type");

    /**
     * Creates manager.
     *
     * @param aContext context to communicate with.
     */
    public HsqlFeedsPM(HsqlPersistenceManager aContext)
    {
        context = aContext;
    }

    /**
     * Inserts the feed into database.
     *
     * @param feed feed to insert.
     *
     * @throws NullPointerException if feed isn't specified.
     * @throws IllegalStateException if feed is already in database.
     * @throws IllegalArgumentException if feed is of unsupported type.
     * @throws SQLException if database operation fails.
     */
    public void insertFeed(IFeed feed)
        throws SQLException
    {
        if (feed == null)
            throw new NullPointerException(HsqlPersistenceManager.MSG_FEED_UNSPECIFIED);
        if (feed.getID() != -1) throw new IllegalStateException(MSG_ALREADY_IN_DB);

        if (!hasSupportedType(feed)) throw new IllegalArgumentException(MSG_UNSUPPORTED_TYPE);

        // Insert general Feed record
        feed.setID(insertGeneralFeed(feed));

        // Insert a stats record
        long now = System.currentTimeMillis();
        PreparedStatement stmt = context.getPreparedStatement("INSERT INTO FEEDSTATS " +
            "(FEEDID, INIT_TIME, RESET_TIME) VALUES (?, ?, ?)");
        stmt.setLong(1, feed.getID());
        stmt.setLong(2, now);
        stmt.setLong(3, now);
        int rows = stmt.executeUpdate();
        if (rows == 0) throw new SQLException("Failed to add a feed stats row");
    }

    /**
     * Returns <code>TRUE</code> if the feed has supported type.
     *
     * @param feed feed.
     *
     * @return <code>TRUE</code> if the feed has supported type.
     */
    private static boolean hasSupportedType(IFeed feed)
    {
        return (feed instanceof DirectFeed) || (feed instanceof QueryFeed) ||
            (feed instanceof SearchFeed);
    }

    /**
     * Inserts record into DirectFeeds table associated with given Feeds table ID.
     *
     * @param feedId        ID from Feeds table.
     * @param directFeed    direct feed to take data from.
     *
     * @throws SQLException if database fails.
     */
    private void insertDirectFeed(long feedId, DirectFeed directFeed)
        throws SQLException
    {
        PreparedStatement stmt = context.getPreparedStatement("INSERT INTO DIRECTFEEDS " +
            "(FEEDID, TITLE, AUTHOR, DESCRIPTION, CUSTOMTITLE, CUSTOMAUTHOR, " +
            "CUSTOMDESCRIPTION, DEAD, SITEURL, XMLURL, INLINKS, LASTMETADATAUPDATETIME, " +
            "USERTAGS, UNSAVEDUSERTAGS, TAGSDESCRIPTION, TAGSEXTENDED, DISABLED, SYNC_HASH) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        try
        {
            stmt.setLong(1, feedId);
            stmt.setString(2, directFeed.getBaseTitle());
            stmt.setString(3, directFeed.getBaseAuthor());
            stmt.setString(4, directFeed.getBaseDescription());
            stmt.setString(5, directFeed.getCustomTitle());
            stmt.setString(6, directFeed.getCustomAuthor());
            stmt.setString(7, directFeed.getCustomDescription());
            stmt.setBoolean(8, directFeed.isDead());
            URL siteURL = directFeed.getSiteURL();
            stmt.setString(9, siteURL == null ? null : siteURL.toString());
            URL xmlURL = directFeed.getXmlURL();
            stmt.setString(10, xmlURL == null ? null : xmlURL.toString());
            stmt.setInt(11, directFeed.getInLinks());
            stmt.setLong(12, directFeed.getLastMetaDataUpdateTime());
            stmt.setString(13, StringUtils.arrayToQuotedKeywords(directFeed.getUserTags()));
            stmt.setBoolean(14, directFeed.hasUnsavedUserTags());
            stmt.setString(15, directFeed.getTagsDescription());
            stmt.setString(16, directFeed.getTagsExtended());
            stmt.setBoolean(17, directFeed.isDisabled());
            stmt.setInt(18, directFeed.getSyncHash());

//            ReadingList list = directFeed.getReadingList();
//            stmt.setObject(18, list == null ? null : new Integer((int)list.getID()));

            int rows = stmt.executeUpdate();
            if (rows == 0) throw new SQLException(
                Strings.error("db.failed.to.insert.row.into.directfeeds"));
        } finally
        {
            stmt.close();
        }
    }

    /**
     * Inserts records into SearchFeed table associated with given Feeds table ID.
     *
     * @param aFeedId       ID from Feeds table.
     * @param aSearchFeed   data feed.
     *
     * @throws SQLException if database fails.
     */
    private void insertSearchFeed(long aFeedId, SearchFeed aSearchFeed)
        throws SQLException
    {
        PreparedStatement stmt = context.getPreparedStatement("INSERT INTO SEARCHFEEDS " +
            "(FEEDID, ARTICLESLIMIT, RATING, TITLE, QUERY, DEDUP_ENABLED, DEDUP_FROM, DEDUP_TO) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

        try
        {
            stmt.setLong(1, aFeedId);
            stmt.setInt(2, aSearchFeed.getArticlesLimit());
            stmt.setInt(3, aSearchFeed.getRating());
            stmt.setString(4, aSearchFeed.getBaseTitle());
            stmt.setString(5, aSearchFeed.getQuery().serializeToString());

            stmt.setBoolean(6, aSearchFeed.isDedupEnabled());
            stmt.setInt(7, aSearchFeed.getDedupFrom());
            stmt.setInt(8, aSearchFeed.getDedupTo());

            int rows = stmt.executeUpdate();
            if (rows == 0) throw new SQLException(Strings.error("db.failed.to.insert.row.into.searchfeeds"));
        } finally
        {
            stmt.close();
        }
    }

    /**
     * Inserts records into DataFeeds table associated with given Feeds table ID.
     *
     * @param feedId        ID from Feeds table.
     * @param dataFeed      data feed.
     *
     * @throws SQLException if database fails.
     */
    private void insertDataFeed(long feedId, DataFeed dataFeed)
        throws SQLException
    {
        PreparedStatement stmt = context.getPreparedStatement("INSERT INTO DATAFEEDS " +
            "(FEEDID, INITTIME, LASTPOLLTIME, RETRIEVALS, FORMAT, LANGUAGE, PURGELIMIT, " +
            "UPDATEPERIOD, TOTALPOLLEDARTICLES, " +
            "RATING, LASTUPDATESERVERTIME, LASTFETCHARTICLEKEYS) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        try
        {
            stmt.setLong(1, feedId);
            stmt.setLong(2, dataFeed.getInitTime());
            stmt.setLong(3, dataFeed.getLastPollTime());
            stmt.setInt(4, dataFeed.getRetrievals());
            stmt.setString(5, dataFeed.getFormat());
            stmt.setString(6, dataFeed.getLanguage());
            stmt.setInt(7, dataFeed.getPurgeLimit());
            stmt.setLong(8, dataFeed.getUpdatePeriod());
            stmt.setInt(9, dataFeed.getTotalPolledArticles());
            stmt.setInt(10, dataFeed.getRating());
            stmt.setLong(11, dataFeed.getLastUpdateServerTime());

            String[] keys = dataFeed.getLastFetchArticleKeys();
            stmt.setString(12, keys == null ? null : StringUtils.join(keys, ","));

            int rows = stmt.executeUpdate();
            if (rows == 0) throw new SQLException(Strings.error("db.failed.to.insert.row.into.datafeeds"));

            if (dataFeed instanceof DirectFeed) insertDirectFeed(feedId, (DirectFeed)dataFeed);
            else if (dataFeed instanceof QueryFeed) insertQueryFeed(feedId, (QueryFeed)dataFeed);
        } finally
        {
            stmt.close();
        }
    }

    /**
     * Inserts record into QueryFeeds table associated with given DataFeeds table ID.
     *
     * @param feedId        ID from Feeds table.
     * @param queryFeed     query feed to take data from.
     *
     * @throws SQLException if database fails.
     */
    private void insertQueryFeed(long feedId, QueryFeed queryFeed)
        throws SQLException
    {
        PreparedStatement stmt = context.getPreparedStatement("INSERT INTO QUERYFEEDS " +
            "(FEEDID, TITLE, QUERYTYPE, KEYWORDS, DEDUP_ENABLED, DEDUP_FROM, DEDUP_TO) " +
            "VALUES (?, ?, ?, ?, ?, ? ,?)");

        try
        {
            QueryType queryType = queryFeed.getQueryType();
            int type = queryType == null ? -1 : queryType.getType();

            stmt.setLong(1, feedId);
            stmt.setString(2, queryFeed.getBaseTitle());
            stmt.setInt(3, type);
            stmt.setString(4, queryFeed.getParameter());

            stmt.setBoolean(5, queryFeed.isDedupEnabled());
            stmt.setInt(6, queryFeed.getDedupFrom());
            stmt.setInt(7, queryFeed.getDedupTo());

            int rows = stmt.executeUpdate();
            if (rows == 0) throw new SQLException(Strings.error("db.failed.to.insert.row.into.queryfeeds"));
        } finally
        {
            stmt.close();
        }
    }

    /**
     * Inserts record into Feeds table for a feed of a given type.
     *
     * @param feed feed to insert.
     *
     * @return ID of inserted feed.
     *
     * @throws SQLException if database fails.
     */
    private long insertGeneralFeed(IFeed feed)
        throws SQLException
    {
        PreparedStatement stmt = context.getPreparedStatement(
            "INSERT INTO FEEDS (INVALIDNESSREASON, TYPE, FEEDTYPE) VALUES (?, ?, ?)");

        // Find out the type of the feed.
        int type;
        if (feed instanceof DirectFeed)
        {
            type = 0;
        } else if (feed instanceof QueryFeed)
        {
            type = 1;
        } else if (feed instanceof SearchFeed)
        {
            type = 2;
        } else throw new IllegalArgumentException(MSG_UNSUPPORTED_TYPE);

        try
        {
            stmt.setString(1, feed.getInvalidnessReason());
            stmt.setInt(2, type);
//            stmt.setLong(3, feed.getGuide().getID());
            stmt.setInt(3, feed.getType().getType());

            int rows = stmt.executeUpdate();
            if (rows == 0) throw new SQLException(Strings.error("db.row.was.not.added.for.new.feed"));
        } finally
        {
            stmt.close();
        }

        long id = context.getInsertedID();

        insertGeneralFeedProperties(id, feed);

        if (feed instanceof DataFeed)
        {
            insertDataFeed(id, (DataFeed)feed);
        } else
        {
            insertSearchFeed(id, (SearchFeed)feed);
        }

        return id;
    }

    /**
     * Inserts record with properties.
     *
     * @param aFeedId   feed ID.
     * @param aFeed     feed.
     *
     * @throws SQLException if database fails.
     */
    private void insertGeneralFeedProperties(long aFeedId, IFeed aFeed)
        throws SQLException
    {
        PreparedStatement stmt = context.getPreparedStatement(
            "INSERT INTO FEEDSPROPERTIES (FEEDID, LASTVISITTIME, " +
                "CUSTOMVIEWMODEENABLED, CUSTOMVIEWMODE, VIEWS, CLICKTHROUGHS, " +
                "ASCENDINGSORTING, ASA, ASA_FOLDER, ASA_NAMEFORMAT, ASE, ASE_FOLDER, ASE_NAMEFORMAT) " +
                "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        try
        {
            stmt.setLong(1, aFeedId);
            stmt.setLong(2, aFeed.getLastVisitTime());
            stmt.setBoolean(3, aFeed.isCustomViewModeEnabled());
            stmt.setInt(4, aFeed.getCustomViewMode());
            stmt.setInt(5, aFeed.getViews());
            stmt.setInt(6, aFeed.getClickthroughs());
            stmt.setObject(7, aFeed.getAscendingSorting());
            stmt.setBoolean(8, aFeed.isAutoSaveArticles());
            stmt.setString(9, aFeed.getAutoSaveArticlesFolder());
            stmt.setString(10, aFeed.getAutoSaveArticlesNameFormat());
            stmt.setBoolean(11, aFeed.isAutoSaveEnclosures());
            stmt.setString(12, aFeed.getAutoSaveEnclosuresFolder());
            stmt.setString(13, aFeed.getAutoSaveEnclosuresNameFormat());

            int rows = stmt.executeUpdate();
            if (rows == 0) throw new SQLException(Strings.error("db.row.was.not.added.at.feedsproperties"));
        } finally
        {
            stmt.close();
        }
    }

    /**
     * Removes the feed from database.
     *
     * @param feed feed to remove.
     *
     * @throws NullPointerException if feed isn't specified.
     * @throws IllegalStateException if feed is not in database.
     * @throws SQLException if database operation fails.
     */
    public void removeFeed(IFeed feed)
        throws SQLException
    {
        if (feed == null)
            throw new NullPointerException(HsqlPersistenceManager.MSG_FEED_UNSPECIFIED);
        if (feed.getID() == -1) throw new IllegalStateException(MSG_NOT_IN_DB);
        if (!hasSupportedType(feed)) throw new IllegalArgumentException(MSG_UNSUPPORTED_TYPE);

        PreparedStatement stmt = context.getPreparedStatement("DELETE FROM FEEDS WHERE ID=?");
        stmt.setLong(1, feed.getID());

        try
        {
            int rows = stmt.executeUpdate();
            if (rows == 0)
            {
                LOG.log(Level.SEVERE, MessageFormat.format(
                    Strings.error("db.no.rows.deleted.from.feeds.feedid.0"),
                    feed.getID()));
            }

            HsqlPersistenceManager.clearFeedID(feed);
        } finally
        {
            stmt.close();
        }
    }

    /**
     * Moves feed from dest guide to destination guide.
     *
     * @param feed      feed to move.
     * @param source    destination guide.
     * @param dest      dest guide.
     *
     * @throws NullPointerException if feed or destination guides aren't specified.
     * @throws IllegalStateException if feed or guide are transient.
     * @throws SQLException if database operation fails.
     */
    public void moveFeed(IFeed feed, IGuide source, IGuide dest)
        throws SQLException, IllegalStateException
    {
        if (feed == null)
            throw new NullPointerException(HsqlPersistenceManager.MSG_FEED_UNSPECIFIED);
        if (source == null)
            throw new NullPointerException(Strings.error("unspecified.source.guide"));
        if (dest == null)
            throw new NullPointerException(Strings.error("unspecified.destination.guide"));

        long sourceGuideId = source.getID();
        long destGuideId = dest.getID();
        long feedId = feed.getID();

        if (feedId == -1) throw new IllegalStateException(Strings.error("db.feed.is.transient"));
        if (sourceGuideId == -1) throw new IllegalStateException(Strings.error("db.source.guide.is.transient"));
        if (destGuideId == -1) throw new IllegalStateException(Strings.error("db.destination.guide.is.transient"));

        PreparedStatement updFeed = context.getPreparedStatement(
            "UPDATE FEEDS2GUIDES SET GUIDEID=?, POSITN=? WHERE FEEDID=? AND GUIDEID=?");

        try
        {
            updFeed.setLong(1, destGuideId);
            updFeed.setInt(2, dest.indexOf(feed));
            updFeed.setLong(3, feedId);
            updFeed.setLong(4, sourceGuideId);

            int rows = updFeed.executeUpdate();
            if (rows == 0)
            {
                LOG.log(Level.SEVERE, MessageFormat.format(
                    Strings.error("db.failed.to.move.feed.feedid.0.sourceguideid.1.destguideid.2"),
                    feedId, sourceGuideId, destGuideId));
            }
        } finally
        {
            updFeed.close();
        }
    }

    /**
     * Updates the feed in database.
     *
     * @param feed feed to update.
     * @param property  name of property being updated or NULL if full update required.
     *
     * @throws NullPointerException if feed isn't specified.
     * @throws IllegalStateException if feed is not in database.
     * @throws SQLException if database operation fails.
     */
    public void updateFeed(IFeed feed, String property)
        throws SQLException
    {
        if (feed == null)
            throw new NullPointerException(HsqlPersistenceManager.MSG_FEED_UNSPECIFIED);
        if (feed.getID() == -1) throw new IllegalStateException(MSG_NOT_IN_DB);
        if (!hasSupportedType(feed)) throw new IllegalArgumentException(MSG_UNSUPPORTED_TYPE);

        if (!updateFeedsTable(feed, property))
        {
            if (feed instanceof DataFeed)
            {
                updateDataFeedsTable((DataFeed)feed);
                if (feed instanceof DirectFeed)
                {
                    updateDirectFeedsTable((DirectFeed)feed);
                } else if (feed instanceof QueryFeed)
                {
                    updateQueryFeedsTable((QueryFeed)feed);
                }
            } else if (feed instanceof SearchFeed)
            {
                updateSearchFeedsTable((SearchFeed)feed, property);
            }
        }
    }

    /**
     * Updates the SearchFeeds table with data taken from feed object.
     *
     * @param aFeed  feed to take data from.
     * @param property  name of property being updated or NULL if full update required.
     *
     * @throws SQLException if database error happens.
     */
    private void updateSearchFeedsTable(SearchFeed aFeed, String property)
        throws SQLException
    {
        PreparedStatement stmt = null;

        try
        {
            if (SearchFeed.PROP_QUERY.equals(property))
            {
                stmt = context.getPreparedStatement("UPDATE SEARCHFEEDS SET " +
                "QUERY=? WHERE FEEDID=?");

                stmt.setString(1, aFeed.getQuery().serializeToString());
                stmt.setLong(2, aFeed.getID());
            } else
            {
                stmt = context.getPreparedStatement("UPDATE SEARCHFEEDS SET " +
                "TITLE=?, ARTICLESLIMIT=?, RATING=?, DEDUP_ENABLED=?, DEDUP_FROM=?, DEDUP_TO=? " +
                "WHERE FEEDID=?");

                stmt.setString(1, aFeed.getBaseTitle());
                stmt.setInt(2, aFeed.getArticlesLimit());
                stmt.setInt(3, aFeed.getRating());
                stmt.setBoolean(4, aFeed.isDedupEnabled());
                stmt.setInt(5, aFeed.getDedupFrom());
                stmt.setInt(6, aFeed.getDedupTo());
                stmt.setLong(7, aFeed.getID());
            }

            int rows = stmt.executeUpdate();
            if (rows == 0) logNoUpdate(aFeed, "SEARCHFEEDS");
        } finally
        {
            if (stmt != null) stmt.close();
        }
    }

    /**
     * Updates the QueryFeeds table with data taken from feed object.
     *
     * @param feed  feed to take data from.
     *
     * @throws SQLException if database error happens.
     */
    private void updateQueryFeedsTable(QueryFeed feed)
        throws SQLException
    {
        PreparedStatement stmt = context.getPreparedStatement("UPDATE QUERYFEEDS SET " +
            "TITLE=?, QUERYTYPE=?, KEYWORDS=?, DEDUP_ENABLED=?, DEDUP_FROM=?, DEDUP_TO=? " +
            "WHERE FEEDID=?");

        try
        {
            QueryType queryType = feed.getQueryType();
            int type = queryType == null ? -1 : queryType.getType();

            stmt.setString(1, feed.getBaseTitle());
            stmt.setInt(2, type);
            stmt.setString(3, feed.getParameter());
            stmt.setBoolean(4, feed.isDedupEnabled());
            stmt.setInt(5, feed.getDedupFrom());
            stmt.setInt(6, feed.getDedupTo());
            stmt.setLong(7, feed.getID());

            int rows = stmt.executeUpdate();
            if (rows == 0) logNoUpdate(feed, "QUERYFEEDS");
        } finally
        {
            stmt.close();
        }
    }

    /**
     * Updates the Feeds table with data taken from feed object.
     *
     * @param feed  feed to take data from.
     * @param property  property which has been updated in the feed or NULL if doesn't matter.
     *
     * @return <code>TRUE</code> if updated.
     *
     * @throws SQLException if database error happens.
     */
    private boolean updateFeedsTable(IFeed feed, String property)
        throws SQLException
    {
        PreparedStatement stmt = null;
        boolean updated = false;

        try
        {
            // If it was a property which isn't stored in this table then skip update
            if (property == null ||
                (updated = (property.equals(IFeed.PROP_INVALIDNESS_REASON) ||
                    property.equals(IFeed.PROP_TYPE))))
            {
                stmt = context.getPreparedStatement("UPDATE FEEDS SET " +
                    "INVALIDNESSREASON=?, FEEDTYPE=? WHERE ID=?");

                stmt.setString(1, feed.getInvalidnessReason());
                stmt.setInt(2, feed.getType().getType());
                stmt.setLong(3, feed.getID());

                int rows = stmt.executeUpdate();
                if (rows == 0) logNoUpdate(feed, "FEEDS");
            }
        } finally
        {
            if (stmt != null) stmt.close();
        }

        try
        {
            if (!updated && (property == null ||
                    property.equals(IFeed.PROP_LAST_VISIT_TIME) ||
                    property.equals(IFeed.PROP_CUSTOM_VIEW_MODE_ENABLED) ||
                    property.equals(IFeed.PROP_CUSTOM_VIEW_MODE) ||
                    property.equals(IFeed.PROP_LAST_UPDATE_TIME) ||
                    property.equals(IFeed.PROP_VIEWS) ||
                    property.equals(IFeed.PROP_CLICKTHROUGHS) ||
                    property.equals(IFeed.PROP_ASCENDING_SORTING) ||
                    property.startsWith(IFeed.PROP_AUTO_SAVE_ARTICLES) ||
                    property.startsWith(IFeed.PROP_AUTO_SAVE_ENCLOSURES)))
            {
                // if it wasn't specific property we should tell FALSE to
                // make the other (following) updates
                updated = property != null;

                stmt = context.getPreparedStatement(
                    "UPDATE FEEDSPROPERTIES SET " +
                    "LASTVISITTIME=?, CUSTOMVIEWMODEENABLED=?," +
                    "CUSTOMVIEWMODE=?, LASTUPDATETIME=?, VIEWS=?, CLICKTHROUGHS=?, " +
                    "ASCENDINGSORTING=?, ASA=?, ASA_FOLDER=?, ASA_NAMEFORMAT=?, " +
                    "ASE=?, ASE_FOLDER=?, ASE_NAMEFORMAT=? WHERE FEEDID=?");

                stmt.setLong(1, feed.getLastVisitTime());
                stmt.setBoolean(2, feed.isCustomViewModeEnabled());
                stmt.setInt(3, feed.getCustomViewMode());
                stmt.setLong(4, feed.getLastUpdateTime());
                stmt.setInt(5, feed.getViews());
                stmt.setInt(6, feed.getClickthroughs());
                stmt.setObject(7, feed.getAscendingSorting());
                stmt.setBoolean(8, feed.isAutoSaveArticles());
                stmt.setString(9, feed.getAutoSaveArticlesFolder());
                stmt.setString(10, feed.getAutoSaveArticlesNameFormat());
                stmt.setBoolean(11, feed.isAutoSaveEnclosures());
                stmt.setString(12, feed.getAutoSaveEnclosuresFolder());
                stmt.setString(13, feed.getAutoSaveEnclosuresNameFormat());
                stmt.setLong(14, feed.getID());

                int rows = stmt.executeUpdate();
                if (rows == 0)
                {
                    // Perhaps, there's no row in feeds properties table
                    insertGeneralFeedProperties(feed.getID(), feed);
                }
            }
        } finally
        {
            if (stmt != null) stmt.close();
        }

        return updated;
    }

    /**
     * Updates the DataFeeds table with data taken from feed object.
     *
     * @param dataFeed  feed to take data from.
     *
     * @throws SQLException if database error happens.
     */
    private void updateDataFeedsTable(DataFeed dataFeed)
        throws SQLException
    {
        PreparedStatement stmt = context.getPreparedStatement("UPDATE DATAFEEDS SET " +
            "INITTIME=?, LASTPOLLTIME=?, RETRIEVALS=?, FORMAT=?, LANGUAGE=?, PURGELIMIT=?, " +
            "UPDATEPERIOD=?, " +
            "TOTALPOLLEDARTICLES=?, RATING=?, LASTUPDATESERVERTIME=?, LASTFETCHARTICLEKEYS=? " +
            "WHERE FEEDID=?");

        try
        {
            stmt.setLong(1, dataFeed.getInitTime());
            stmt.setLong(2, dataFeed.getLastPollTime());
            stmt.setInt(3, dataFeed.getRetrievals());
            stmt.setString(4, dataFeed.getFormat());
            stmt.setString(5, dataFeed.getLanguage());
            stmt.setInt(6, dataFeed.getPurgeLimit());
            stmt.setLong(7, dataFeed.getUpdatePeriod());
            stmt.setInt(8, dataFeed.getTotalPolledArticles());
            stmt.setInt(9, dataFeed.getRating());
            stmt.setLong(10, dataFeed.getLastUpdateServerTime());

            String[] keys = dataFeed.getLastFetchArticleKeys();
            stmt.setString(11, keys == null ? null : StringUtils.join(keys, ","));
            stmt.setLong(12, dataFeed.getID());

            int rows = stmt.executeUpdate();
            if (rows == 0) logNoUpdate(dataFeed, "DATAFEEDS");
        } finally
        {
            stmt.close();
        }
    }

    /**
     * Updates the DirectFeeds table with data taken from feed object.
     *
     * @param directFeed  feed to take data from.
     *
     * @throws SQLException if database error happens.
     */
    private void updateDirectFeedsTable(DirectFeed directFeed)
        throws SQLException
    {
        PreparedStatement stmt = context.getPreparedStatement("UPDATE DIRECTFEEDS SET " +
            "TITLE=?, AUTHOR=?, DESCRIPTION=?, CUSTOMTITLE=?, CUSTOMAUTHOR=?, " +
            "CUSTOMDESCRIPTION=?, DEAD=?, SITEURL=?, XMLURL=?, INLINKS=?, " +
            "LASTMETADATAUPDATETIME=?, USERTAGS=?, UNSAVEDUSERTAGS=?, TAGSDESCRIPTION=?, " +
            "TAGSEXTENDED=?, DISABLED=?, SYNC_HASH=? " +
            "WHERE FEEDID=?");

        try
        {
            stmt.setString(1, directFeed.getBaseTitle());
            stmt.setString(2, directFeed.getBaseAuthor());
            stmt.setString(3, directFeed.getBaseDescription());
            stmt.setString(4, directFeed.getCustomTitle());
            stmt.setString(5, directFeed.getCustomAuthor());
            stmt.setString(6, directFeed.getCustomDescription());
            stmt.setBoolean(7, directFeed.isDead());
            URL siteURL = directFeed.getSiteURL();
            stmt.setString(8, siteURL == null ? null : siteURL.toString());
            URL xmlURL = directFeed.getXmlURL();
            stmt.setString(9, xmlURL == null ? null : xmlURL.toString());
            stmt.setInt(10, directFeed.getInLinks());
            stmt.setLong(11, directFeed.getLastMetaDataUpdateTime());
            stmt.setString(12, StringUtils.arrayToQuotedKeywords(directFeed.getUserTags()));
            stmt.setBoolean(13, directFeed.hasUnsavedUserTags());
            stmt.setString(14, directFeed.getTagsDescription());
            stmt.setString(15, directFeed.getTagsExtended());
            stmt.setBoolean(16, directFeed.isDisabled());
            stmt.setInt(17, directFeed.getSyncHash());

            stmt.setLong(18, directFeed.getID());

            int rows = stmt.executeUpdate();
            if (rows == 0) logNoUpdate(directFeed, "DIRECTFEEDS");
        } finally
        {
            stmt.close();
        }
    }

    /**
     * Puts message in log about no updates at some table.
     *
     * @param feed  feed.
     * @param table table name.
     */
    private static void logNoUpdate(IFeed feed, String table)
    {
        LOG.log(Level.SEVERE, MessageFormat.format(
            Strings.error("db.hsql.updated.0.rows.at.0.feedid.1"),
            table, feed.getID()));
    }

    /**
     * Adds a feed to a guide.
     *
     * @param aGuide    guide to add feed to.
     * @param aFeed     feed to add.
     *
     * @throws SQLException if database operation fails.
     * @throws IllegalStateException if guide is transient.
     */
    public void addFeedToGuide(IGuide aGuide, IFeed aFeed)
        throws SQLException
    {
        if (aGuide.getID() == -1)
        {
            throw new IllegalStateException(Strings.error("db.guide.should.be.persistent"));
        }

        PreparedStatement stmt = context.getPreparedStatement(
            "INSERT INTO FEEDS2GUIDES (FEEDID, GUIDEID, POSITN) VALUES (?, ?, ?)");

        stmt.setLong(1, aFeed.getID());
        stmt.setLong(2, aGuide.getID());
        stmt.setLong(3, aGuide.indexOf(aFeed));

        int rows = stmt.executeUpdate();
        if (rows == 0) throw new SQLException(MessageFormat.format(
            Strings.error("db.feed.was.not.linked.to.guide"),
            aFeed, aGuide));
    }

    /**
     * Removes a feed from a guide.
     *
     * @param aGuide    guide to remove feed from.
     * @param aFeed     feed to remove.
     *
     * @throws SQLException if database operation fails.
     * @throws IllegalStateException if guide is transient.
     * @throws PersistenceException if removing feeds with no refs failed.
     */
    public void removeFeedFromGuide(IGuide aGuide, IFeed aFeed)
        throws SQLException, PersistenceException
    {
        if (aGuide.getID() == -1)
        {
            throw new IllegalStateException(Strings.error("db.guide.should.be.persistent"));
        }

        PreparedStatement stmt = context.getPreparedStatement(
            "DELETE FROM FEEDS2GUIDES WHERE FEEDID=? AND GUIDEID=?");

        stmt.setLong(1, aFeed.getID());
        stmt.setLong(2, aGuide.getID());

        int rows = stmt.executeUpdate();
        if (rows == 0) throw new SQLException(MessageFormat.format(
            Strings.error("db.feed.was.not.linked.to.guide"),
            aFeed, aGuide));

        context.removeFeedIfNoRefs(aFeed);
    }

    /**
     * Adds a feed to a reading list.
     *
     * @param aList     reading list.
     * @param aFeed     feed to add.
     *
     * @throws SQLException if database operation fails.
     * @throws IllegalStateException if the list is transient.
     */
    public void addFeedToReadingList(ReadingList aList, IFeed aFeed)
        throws SQLException
    {
        if (aList.getID() == -1) throw new IllegalStateException(Strings.error("db.reading.list.is.transient"));

        PreparedStatement stmt = context.getPreparedStatement(
            "INSERT INTO FEEDS2READINGLISTS (FEEDID, READINGLISTID, POSITN) VALUES (?, ?, ?)");

        StandardGuide guide = aList.getParentGuide();

        stmt.setLong(1, aFeed.getID());
        stmt.setLong(2, aList.getID());
        stmt.setLong(3, guide == null ? 0 : guide.indexOf(aFeed));

        int rows = stmt.executeUpdate();
        if (rows == 0) throw new SQLException(MessageFormat.format(
            Strings.error("db.feed.was.not.linked.to.reading.list"),
            aFeed, aList)); }

    /**
     * Removes a feed from the reading list.
     *
     * @param aList  list.
     * @param aFeed  feed.
     *
     * @throws SQLException if database operation fails.
     * @throws IllegalStateException if the list is transient.
     * @throws PersistenceException if removing feeds with no refs failed.
     */
    public void removeFeedFromReadingList(ReadingList aList, IFeed aFeed)
        throws SQLException, PersistenceException
    {
        if (aList.getID() == -1) throw new IllegalStateException(Strings.error("db.reading.list.is.transient"));

        PreparedStatement stmt = context.getPreparedStatement(
            "DELETE FROM FEEDS2READINGLISTS WHERE FEEDID=? AND READINGLISTID=?");

        stmt.setLong(1, aFeed.getID());
        stmt.setLong(2, aList.getID());

        int rows = stmt.executeUpdate();
        if (rows == 0) throw new SQLException(MessageFormat.format(
            Strings.error("db.feed.was.not.linked.to.reading.list"),
            aFeed, aList));
        context.removeFeedIfNoRefs(aFeed);
    }

    /**
     * Updates link information between the guide and the feed.
     *
     * @param guide guide.
     * @param feed  feed.
     *
     * @throws SQLException if DB update failed.
     */
    public void updateFeedLink(StandardGuide guide, IFeed feed)
        throws SQLException
    {
        long guideId = guide.getID();
        long feedId = feed.getID();

        if (guideId == -1) throw new IllegalStateException(MessageFormat.format(
            Strings.error("db.guide.is.transient.0"), guide));
        if (feedId == -1) throw new IllegalStateException(MessageFormat.format(
            Strings.error("db.feed.is.transient.0"), feed));

        StandardGuide.FeedLinkInfo info = guide.getFeedLinkInfo(feed);
        if (info != null)
        {
            // We don't update feed position here as it's useless

            PreparedStatement stmt = context.getPreparedStatement(
                "UPDATE FEEDS2GUIDES SET LASTSYNCTIME=? " +
                    "WHERE GUIDEID=? AND FEEDID=?");

            stmt.setLong(1, info.getLastSyncTime());
            stmt.setLong(2, guideId);
            stmt.setLong(3, feedId);

            int rows = stmt.executeUpdate();
            if (rows == 0) throw new SQLException(MessageFormat.format(
                Strings.error("db.feed.link.was.not.updated.guideid.0.feedid.1"),
                guideId, feedId));
        }
    }

    /**
     * Updates the positions of all feeds in the guide.
     *
     * @param guide guide.
     *
     * @throws SQLException in case of database error.
     */
    public void updateFeedsPositions(IGuide guide)
        throws SQLException
    {
        long guideId = guide.getID();

        if (guideId == -1) throw new IllegalStateException(MessageFormat.format(
            Strings.error("db.guide.is.transient.0"), guide));

        Statements stmts = new Statements();

        int cnt = guide.getFeedsCount();
        for (int i = 0; i < cnt; i++)
        {
            IFeed feed = guide.getFeedAt(i);
            addUpdateFeedPositionBatch(guide, feed, i, stmts);
        }

        stmts.execute();
    }

    /**
     * Updates the position of feed.
     *
     * @param guide guide.
     * @param feed  feed.
     *
     * @throws SQLException in case of database error.
     */
    public void updateFeedPosition(IGuide guide, IFeed feed)
        throws SQLException
    {
        Statements stmts = new Statements();
        addUpdateFeedPositionBatch(guide, feed, guide.indexOf(feed), stmts);
        stmts.execute();
    }

    /**
     * Adds an item to the statements batches.
     *
     * @param guide     guide.
     * @param feed      feed.
     * @param position  position.
     * @param stmts     statements.
     *
     * @throws SQLException in case of database error.
     */
    private void addUpdateFeedPositionBatch(IGuide guide, IFeed feed, int position, Statements stmts) throws SQLException
    {
        long guideId = guide.getID();
        long feedId = feed.getID();

        if (guideId == -1) throw new IllegalStateException(MessageFormat.format(
            Strings.error("db.guide.is.transient.0"), guide));
        if (feedId == -1) throw new IllegalStateException(MessageFormat.format(
            Strings.error("db.feed.is.transient.0"), feed));

        ReadingList[] readingLists = !(feed instanceof DirectFeed) ? null : ((DirectFeed)feed).getReadingLists();
        if (readingLists == null || readingLists.length == 0)
        {
            // Not a part of reading list
            stmts.addGuideBatch(guideId, feedId, position);
        } else
        {
            for (ReadingList list : readingLists)
            {
                if (list.getParentGuide() == guide) stmts.addReadingListBatch(list.getID(), feedId, position);
            }
        }
    }

    /**
     * Simple holder for statements.
     */
    private class Statements
    {
        private static final String UPDATE_FEEDS2GUIDES = "UPDATE FEEDS2GUIDES SET POSITN=? WHERE GUIDEID=? AND FEEDID=?";
        private static final String UPDATE_FEEDS2READINGLISTS = "UPDATE FEEDS2READINGLISTS SET POSITN=? WHERE READINGLISTID=? AND FEEDID=?";

        private PreparedStatement stmtGuides;
        private PreparedStatement stmtReadingLists;

        /**
         * Adds the batch to the statement.
         *
         * @param gid   guide id.
         * @param fid   feed id.
         * @param pos   position.
         *
         * @throws SQLException in case of database error.
         */
        public void addGuideBatch(long gid, long fid, int pos) throws SQLException
        {
            if (LOG.isLoggable(Level.FINE)) LOG.fine("Statements.addGuideBatch(" + gid + ", " + fid + ", " + pos + ")");

            if (stmtGuides == null) stmtGuides = context.getPreparedStatement(UPDATE_FEEDS2GUIDES);

            stmtGuides.setInt(1, pos);
            stmtGuides.setLong(2, gid);
            stmtGuides.setLong(3, fid);
            stmtGuides.addBatch();
        }

        /**
         * Adds the batch to the statement.
         *
         * @param rid   reading list id.
         * @param fid   feed id.
         * @param pos   position.
         *
         * @throws SQLException in case of database error.
         */
        public void addReadingListBatch(long rid, long fid, int pos) throws SQLException
        {
            if (LOG.isLoggable(Level.FINE)) LOG.fine("Statements.addReadingListBatch(" + rid + ", " + fid + ", " + pos + ")");

            if (stmtReadingLists == null) stmtReadingLists = context.getPreparedStatement(UPDATE_FEEDS2READINGLISTS);

            stmtReadingLists.setInt(1, pos);
            stmtReadingLists.setLong(2, rid);
            stmtReadingLists.setLong(3, fid);
            stmtReadingLists.addBatch();
        }

        /**
         * Executes the statements.
         *
         * @throws SQLException in case of DB error.
         */
        public void execute() throws SQLException
        {
            try
            {
                if (stmtGuides != null) stmtGuides.executeBatch();
                if (stmtReadingLists != null) stmtReadingLists.executeBatch();
            } finally
            {
                if (stmtGuides != null) stmtGuides.close();
                if (stmtReadingLists != null) stmtReadingLists.close();
            }
        }
    }
}
