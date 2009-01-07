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
// $Id: HsqlPersistenceManager.java,v 1.142 2008/02/28 15:59:51 spyromus Exp $
//

package com.salas.bb.persistence.backend;

import com.jgoodies.uif.application.Application;
import com.salas.bb.domain.*;
import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.domain.querytypes.QueryType;
import com.salas.bb.persistence.IPersistenceManager;
import com.salas.bb.persistence.IStatisticsManager;
import com.salas.bb.persistence.PersistenceException;
import com.salas.bb.persistence.backend.migration.*;
import com.salas.bb.persistence.domain.CountStats;
import com.salas.bb.persistence.domain.ReadStats;
import com.salas.bb.persistence.domain.VisitStats;
import com.salas.bb.utils.*;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.net.auth.IPasswordsRepository;

import javax.swing.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Direct HSQL persistence manager.
 */
public final class HsqlPersistenceManager implements IPersistenceManager, IStatisticsManager
{
    /** Default logger. */
    private static final Logger LOG = Logger.getLogger(HsqlPersistenceManager.class.getName());

    /** Database driver name. */
    private static final String DRIVER = "org.hsqldb.jdbcDriver";

    /** Guide parameter should not be NULL. */
    protected static final String MSG_GUIDE_UNSPECIFIED = Strings.error("unspecified.guide");
    /** RL parameter should not be NULL. */
    private static final String MSG_READING_LIST_UNSPECIFIED = Strings.error("unspecified.reading.list");
    /** Feed parameter should be non-NULL. */
    protected static final String MSG_FEED_UNSPECIFIED = Strings.error("unspecified.feed");
    /** Object key parameter should be non-NULL. */
    protected static final String MSG_OBJECT_UNSPECIFIED = Strings.error("unspecified.object.key");

    /** Path to fresh database script. */
    private static final String RES_SCRIPT = "resources/blogbridge.script";
    /** Path to fresh database properties. */
    private static final String RES_PROPERTIES = "resources/blogbridge.properties";

    /** Application Property: Schema Version. */
    private static final String AP_SCHEMA_VERSION = "schemaVersion";

    /**
     * The collection of migration steps. Each item in the list represent the step to
     * be done for migration to the next schema version. The index of step in the list
     * corresponds to the schema version. For example, first element with index 0
     * "knows" how to migrate from old to the schema version 0, the second - to version 1 and so on. It allows
     * us to add steps to migrate from any old version to new databases automatically
     * by applying "patches" starting from some version up to the most modern.
     */
    private static final ISchemaMigrationStep[] MIGRATION_STEPS = new ISchemaMigrationStep[]
    {
        null,
        new Schema01(), new Schema02(), new Schema03(), new Schema04(), new Schema05(),
        new Schema06(), new Schema07(), new Schema08(), new Schema09(), new Schema10()
    };

    /** <code>TRUE</code> if there's GUI and it's OK to display messages in dialog boxes. */
    public static boolean               hasGUI;

    /** Application context path. It's the directory where all the data is being stored. */
    private final String                contextPath;
    /** Doing backups before upgrading database schema. */
    private final boolean               doBackupOnUpgrade;

    /** URL to database. */
    private final String                databaseUrl;
    /** Name of database user. */
    private final String                databaseUsername;
    /** Password of database user. */
    private final String                databasePassword;

    /** Manager of guides. */
    private final HsqlGuidesPM          guidesManager;
    /** Manager of reading lists. */
    private final HsqlReadingListsPM    readingListsManager;
    /** Manager of feeds. */
    private final HsqlFeedsPM           feedsManager;
    /** Manager of articles. */
    private final HsqlArticlesPM        articlesManager;

    /** Provider of article texts. */
    private final IArticleTextProvider  articleTextProvider;

    private final IPasswordsRepository  passwordsRepository;
    private Connection                  con;

    private boolean                     databaseReset;

    // The cache of prepared statements used during the database loading.
    // They are initialized on demand and closed at the end of the process.
    private PreparedStatement psLoadReadingLists;
    private PreparedStatement psLoadReadingList;
    private PreparedStatement psLoadFeeds;
    private PreparedStatement psLoadSearchFeed;
    private PreparedStatement psLoadQueryFeedPart;
    private PreparedStatement psLoadDirectFeed;
    private PreparedStatement psLoadDataFeedPart;
    private Map<Long, List<IArticle>> articles;

    /** The time when removing of old entity records took place last time. */
    private long lastRemoveOldEntityRecords;

    /** Last N days to put in stats. */
    public static final int STAT_LAST_N_DAYS = 30;

    /** Statistics manager. */
    private IStatisticsManager statisticsManager;

//    private static final int SAVE_EVERY = 100;
//    private int cnt;

    static
    {
        try
        {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e)
        {
            LOG.log(Level.SEVERE, MessageFormat.format(Strings.error("db.driver.was.not.found"), DRIVER), e);
        }

        hasGUI = true;
    }

    /**
     * Creates persistence manager.
     *
     * @param aContextPath          path to database context.
     * @param aDoBackupOnUpgrade    <code>TRUE</code> to do backups on upgrade.
     */
    public HsqlPersistenceManager(String aContextPath, boolean aDoBackupOnUpgrade)
    {
        contextPath = aContextPath;
        doBackupOnUpgrade = aDoBackupOnUpgrade;

        databaseReset = false;

        databaseUrl = "jdbc:hsqldb:file:" + contextPath + "blogbridge";
        databaseUsername = "sa";
        databasePassword = "";

        resetConnection();

        guidesManager = new HsqlGuidesPM(this);
        readingListsManager = new HsqlReadingListsPM(this);
        feedsManager = new HsqlFeedsPM(this);
        articlesManager = new HsqlArticlesPM(this);
        passwordsRepository = new HsqlPasswordsRepository(this);
        articleTextProvider = new ArticleTextProvider();
    }

    /**
     * Returns current context path (working directory).
     *
     * @return path.
     */
    protected String getContextPath()
    {
        return contextPath;
    }

    /**
     * Returns <code>TRUE</code> if the database was reset as the result
     * of corruption detection or unability to upgrade.
     *
     * @return <code>TRUE</code> if the database was reset.
     */
    public boolean isDatabaseReset()
    {
        return databaseReset;
    }

    /**
     * <p>Performs single-time initialization before the actual work. This method
     * can be used to prepare the database or perform a migration of data or for
     * other supplementary things.</p>
     *
     * <p>This method is called only once and before any of the data access or
     * modification calls.</p>
     *
     * @throws PersistenceException if initialization has failed.
     */
    public synchronized void init()
        throws PersistenceException
    {
        if (isDatabaseMissing())
        {
            createDatabase();

            // Update init and reset times
            try
            {
                getPreparedStatement(
                    "UPDATE APP_PROPERTIES " +
                        "SET value = '" + System.currentTimeMillis() + "' " +
                        "WHERE name IN ('statsInitTime', 'statsResetTime')").executeUpdate();
            } catch (SQLException e)
            {
                LOG.log(Level.SEVERE, "Failed to initialize init and reset times for stats", e);
            }
        } else
        {
            int currentSchemeVersion = getCurrentSchemaVersion();

            try
            {
                migrateIfNecessary(currentSchemeVersion);
                commit();
            } catch (MigrationException e)
            {
                Throwable cause = e.getCause();
                if (cause instanceof SQLException &&
                    "08001".equals(((SQLException)cause).getSQLState()))
                {
                    // Database is already in use by another process
                    throw new PersistenceException(Strings.error("db.database.is.locked"), e);
                } else
                {
                    LOG.log(Level.SEVERE, Strings.error("db.was.unable.to.perform.migration"), e);

                    // Backup current database
                    backupDatabaseIfNecessary(currentSchemeVersion);

                    shutdown(true);

                    // Set database reset flag to let the application know what happened
                    databaseReset = true;

                    // Recreate database files
                    deleteDatabase();
                    createDatabase();
                }
            }
        }
    }

    /**
     * Returns TRUE if database file wasn't found.
     *
     * @return TRUE if database file wasn't found.
     */
    private boolean isDatabaseMissing()
    {
        File script = new File(contextPath + "blogbridge.script");
        File properties = new File(contextPath + "blogbridge.properties");

        return !script.exists() || !properties.exists();
    }

    /**
     * Creates database.
     */
    private void createDatabase()
    {
        CommonUtils.copyResourceToFile(RES_SCRIPT, contextPath + "blogbridge.script");
        CommonUtils.copyResourceToFile(RES_PROPERTIES, contextPath + "blogbridge.properties");
    }

    /**
     * Finds and deletes all DB files.
     */
    private void deleteDatabase()
    {
        // Find all DB files
        File workingDir = new File(contextPath);
        File[] dbFiles = workingDir.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name != null && name.startsWith("blogbridge.");
            }
        });

        // Delete all DB files
        if (dbFiles != null) for (File dbFile : dbFiles) dbFile.delete();
    }

    // ---------------------------------------------------------------------------------------------
    // SQL section
    // ---------------------------------------------------------------------------------------------

    /**
     * Loads the list of guides and feeds into the set from database.
     *
     * @param set set to load data into.
     *
     * @throws NullPointerException  if the set isn't specified.
     * @throws IllegalStateException if the set isn't empty.
     * @throws PersistenceException if database operation fails.
     */
    public synchronized void loadGuidesSet(GuidesSet set)
        throws PersistenceException
    {
        if (LOG.isLoggable(Level.FINEST)) LOG.finest("loadGuidesSet");

        try
        {
            loadAllArticles();
            Map<Long, IFeed> allFeeds = loadAllFeeds();

            PreparedStatement psGuides = getPreparedStatement(
                "SELECT ID, TITLE, ICONKEY, AUTOFEEDSDISCOVERY, " +
                    "PUBLISHINGENABLED, PUBLISHINGTITLE, PUBLISHINGTAGS, PUBLISHINGPUBLIC, " +
                    "PUBLISHINGURL, LASTPUBLISHINGTIME, PUBLISHINGRATING, LASTUPDATETIME, NOTIFICATIONSALLOWED " +
                "FROM GUIDES ORDER BY POS");

            List<IGuide> guides = new ArrayList<IGuide>();

            ResultSet rs = psGuides.executeQuery();
            try
            {
                while (rs.next())
                {
                    StandardGuide guide = new StandardGuide();
                    guide.setID(rs.getLong("ID"));
                    guide.setTitle(rs.getString("TITLE"));
                    guide.setIconKey(rs.getString("ICONKEY"));
                    guide.setAutoFeedsDiscovery(rs.getBoolean("AUTOFEEDSDISCOVERY"));

                    // Publishing
                    guide.setPublishingEnabled(rs.getBoolean("PUBLISHINGENABLED"));
                    guide.setPublishingTitle(rs.getString("PUBLISHINGTITLE"));
                    guide.setPublishingTags(rs.getString("PUBLISHINGTAGS"));
                    guide.setPublishingPublic(rs.getBoolean("PUBLISHINGPUBLIC"));
                    guide.setPublishingURL(rs.getString("PUBLISHINGURL"));
                    guide.setLastPublishingTime(rs.getLong("LASTPUBLISHINGTIME"));
                    guide.setPublishingRating(rs.getInt("PUBLISHINGRATING"));
                    guide.setNotificationsAllowed(rs.getBoolean("NOTIFICATIONSALLOWED"));

                    // Warning: This one should be the last because the previous sets will update
                    // this property automatically and we need to reset it correctly
                    guide.setLastUpdateTime(rs.getLong("LASTUPDATETIME"));

                    Map<IFeed, Integer> feedsToOrder = new IdentityHashMap<IFeed, Integer>();
                    loadReadingLists(guide, allFeeds, feedsToOrder);
                    loadFeeds(guide, allFeeds, feedsToOrder);
                    guide.initPositions(feedsToOrder);

                    guides.add(guide);
                }

                for (int i = 0; i < guides.size(); i++)
                {
                      set.add(-1, guides.get(i), i + 1 == guides.size());
                }

                // We commit any changes to database happened during the process
                commit();
            } finally
            {
                rs.close();
                psGuides.close();
                close(psLoadReadingLists);
                psLoadReadingLists = null;
                close(psLoadReadingList);
                psLoadReadingList = null;
                close(psLoadFeeds);
                psLoadFeeds = null;
                close(psLoadSearchFeed);
                psLoadSearchFeed = null;
                close(psLoadQueryFeedPart);
                psLoadQueryFeedPart = null;
                close(psLoadDirectFeed);
                psLoadDirectFeed = null;
                close(psLoadDataFeedPart);
                psLoadDataFeedPart = null;
            }
        } catch (SQLException e)
        {
            throw new PersistenceException(Strings.error("db.failed.to.load.data"), e);
        }
    }

    /**
     * Loads all feeds from database into single id-feed map. ID's are keys of
     * <code>Long</code> type.
     *
     * @return map.
     *
     * @throws SQLException in case of database error.
     * @throws PersistenceException in case of persistence layer error.
     */
    private Map<Long, IFeed> loadAllFeeds()
        throws SQLException, PersistenceException
    {
        PreparedStatement psLoadAllFeeds = getPreparedStatement(
            "SELECT ID, INVALIDNESSREASON, TYPE, LASTVISITTIME, " +
                "FEEDTYPE, CUSTOMVIEWMODEENABLED, CUSTOMVIEWMODE, LASTUPDATETIME, VIEWS, CLICKTHROUGHS, " +
                "ASCENDINGSORTING, ASA, ASA_FOLDER, ASA_NAMEFORMAT, ASE, ASE_FOLDER, ASE_NAMEFORMAT " +
            "FROM FEEDS F LEFT JOIN FEEDSPROPERTIES P ON P.FEEDID=F.ID");

        IFeed[] allFeeds = loadFeeds(psLoadAllFeeds);
        Map<Long, IFeed> idToFeedMap = new HashMap<Long, IFeed>(allFeeds.length);

        for (IFeed feed : allFeeds) idToFeedMap.put(feed.getID(), feed);

        return idToFeedMap;
    }

    /**
     * Loads all reading lists associated with this guide.
     *
     * @param guide         guide to load lists for.
     * @param allFeeds      map of all feeds (id-feed).
     * @param feedsToOrder  the map that has to be filled with feed-order information.
     *
     * @throws SQLException if database operation fails.
     */
    private void loadReadingLists(StandardGuide guide, Map allFeeds, Map<IFeed, Integer> feedsToOrder)
        throws SQLException
    {
        // Create statement if it's missinc
        if (psLoadReadingLists == null)
        {
            psLoadReadingLists = getPreparedStatement(
                "SELECT ID, TITLE, URL, LASTPOLLTIME, LASTUPDATESERVERTIME, LASTSYNCTIME " +
                "FROM READINGLISTS WHERE GUIDEID=?");
        }

        psLoadReadingLists.setLong(1, guide.getID());

        // Load all reading lists one by one
        ResultSet rs = psLoadReadingLists.executeQuery();
        try
        {
            while (rs.next())
            {
                URL url = null;
                try
                {
                    url = new URL(rs.getString("URL"));
                } catch (MalformedURLException e)
                {
                    LOG.log(Level.SEVERE, MessageFormat.format(Strings.error("invalid.url"), url), e);
                }

                if (url != null)
                {
                    ReadingList list = new ReadingList(url);
                    list.setID(rs.getLong("ID"));
                    list.setTitle(rs.getString("TITLE"));
                    list.setLastPollTime(rs.getLong("LASTPOLLTIME"));
                    list.setLastUpdateServerTime(rs.getLong("LASTUPDATESERVERTIME"));
                    list.setLastSyncTime(rs.getLong("LASTSYNCTIME"));

                    loadReadingList(list, allFeeds, feedsToOrder);

                    guide.add(list);
                }
            }
        } finally
        {
            rs.close();
        }
    }

    /**
     * Connects feeds to the reading list.
     *
     * @param aList         list.
     * @param allFeeds      repository of all available feeds.
     * @param feedsToOrder  the map that has to be filled with feed-order information.
     *
     * @throws SQLException if database operation fails.
     */
    private void loadReadingList(ReadingList aList, Map allFeeds, Map<IFeed, Integer> feedsToOrder)
        throws SQLException
    {
        if (psLoadReadingList == null)
        {
            psLoadReadingList = getPreparedStatement(
                "SELECT FEEDID, POSITN FROM FEEDS2READINGLISTS WHERE READINGLISTID=?");
        }

        psLoadReadingList.setLong(1, aList.getID());

        ResultSet rs = psLoadReadingList.executeQuery();
        while (rs.next())
        {
            Long id = rs.getLong("FEEDID");
            DirectFeed feed = (DirectFeed)allFeeds.get(id);
            aList.add(feed);

            feedsToOrder.put(feed, rs.getInt("POSITN"));
        }
    }

    /**
     * Loads all feeds in the guide sorted according to their positions.
     *
     * @param guide         guide to load.
     * @param allFeeds      all feeds in application.
     * @param feedsToOrder  the map that has to be filled with feed-order information.
     *
     * @throws SQLException if database operation fails.
     */
    private void loadFeeds(StandardGuide guide, Map allFeeds, Map<IFeed, Integer> feedsToOrder)
        throws SQLException
    {
        if (psLoadFeeds == null)
        {
            psLoadFeeds = getPreparedStatement("SELECT FEEDID, POSITN, LASTSYNCTIME FROM FEEDS2GUIDES WHERE GUIDEID=?");
        }

        psLoadFeeds.setLong(1, guide.getID());

        ResultSet rs = psLoadFeeds.executeQuery();
        while (rs.next())
        {
            Long id = rs.getLong("FEEDID");
            long lastSyncTime = rs.getLong("LASTSYNCTIME");

            IFeed feed = (IFeed)allFeeds.get(id);
            guide.add(feed);
            feedsToOrder.put(feed, rs.getInt("POSITN"));

            // Populate feed link information block
            StandardGuide.FeedLinkInfo info = guide.getFeedLinkInfo(feed);
            info.setLastSyncTime(lastSyncTime);
        }
    }

    /**
     * Loads all feeds using given database statement.
     *
     * @param aStmt statement.
     *
     * @return the list of loaded feeds.
     *
     * @throws SQLException if database operation fails.
     * @throws PersistenceException if persistent operation fails.
     */
    private IFeed[] loadFeeds(PreparedStatement aStmt)
        throws SQLException, PersistenceException
    {
        // Load all feeds one by one
        ResultSet rs = aStmt.executeQuery();
        List<IFeed> feeds = new ArrayList<IFeed>();
        try
        {
            while (rs.next())
            {
                IFeed feed = null;

                int type = rs.getInt("TYPE");
                long feedId = rs.getLong("ID");
                FeedType feedType = FeedType.toObject(rs.getInt("FEEDTYPE"));
                String invalidnessReason = rs.getString("INVALIDNESSREASON");

                // Load feed in accordance to its type.
                if (type == 0)
                {
                    feed = loadDirectFeed(feedId);
                } else if (type == 1)
                {
                    feed = loadQueryFeed(feedId);
                } else if (type == 2)
                {
                    feed = loadSearchFeed(feedId);
                }

                // If feed was loaded then finish initalization and add to the guide.
                if (feed != null)
                {
                    feed.setID(feedId);
                    feed.setInvalidnessReason(invalidnessReason);
                    feed.setLastVisitTime(rs.getLong("LASTVISITTIME"));
                    feed.setCustomViewModeEnabled(rs.getBoolean("CUSTOMVIEWMODEENABLED"));
                    feed.setCustomViewMode(rs.getInt("CUSTOMVIEWMODE"));
                    feed.setType(feedType);
                    feed.setViews(rs.getInt("VIEWS"));
                    feed.setClickthroughs(rs.getInt("CLICKTHROUGHS"));
                    feed.setAscendingSorting((Boolean)rs.getObject("ASCENDINGSORTING"));

                    feed.setAutoSaveArticles(rs.getBoolean("ASA"));
                    feed.setAutoSaveArticlesFolder(rs.getString("ASA_FOLDER"));
                    feed.setAutoSaveArticlesNameFormat(rs.getString("ASA_NAMEFORMAT"));

                    feed.setAutoSaveEnclosures(rs.getBoolean("ASE"));
                    feed.setAutoSaveEnclosuresFolder(rs.getString("ASE_FOLDER"));
                    feed.setAutoSaveEnclosuresNameFormat(rs.getString("ASE_NAMEFORMAT"));

                    // Warning: This one should be the last because the previous sets will update
                    // this property automatically and we need to reset it correctly
                    feed.setLastUpdateTime(rs.getLong("LASTUPDATETIME"));

                    feeds.add(feed);
                }
            }
        } finally
        {
            rs.close();
            aStmt.close();
        }

        return feeds.toArray(new IFeed[feeds.size()]);
    }

    /**
     * Loads search feed database object.
     *
     * @param aFeedId ID of the feed to load.
     *
     * @return search feed object.
     *
     * @throws SQLException if database operation fails.
     * @throws PersistenceException if persistent operation fails.
     */
    private IFeed loadSearchFeed(long aFeedId)
        throws SQLException, PersistenceException
    {
        if (psLoadSearchFeed == null)
        {
            psLoadSearchFeed = getPreparedStatement(
                "SELECT TITLE, QUERY, ARTICLESLIMIT, RATING, DEDUP_ENABLED, DEDUP_FROM, DEDUP_TO " +
                "FROM SEARCHFEEDS WHERE FEEDID=?");
        }

        psLoadSearchFeed.setLong(1, aFeedId);

        SearchFeed aFeed;

        ResultSet rs = psLoadSearchFeed.executeQuery();
        try
        {
            if (rs.next())
            {
                aFeed = new SearchFeed();
                aFeed.setBaseTitle(rs.getString(1));
                aFeed.setQuery(Query.deserializeFromString(rs.getString(2)));
                aFeed.setArticlesLimit(rs.getInt(3));
                aFeed.setRating(rs.getInt(4));
                aFeed.setDedupProperties(rs.getBoolean(5), rs.getInt(6), rs.getInt(Constants.DAYS_IN_WEEK));
            } else
            {
                throw new PersistenceException(MessageFormat.format(
                    Strings.error("db.feed.was.not.found.in.searchfeeds.table"), aFeedId));
            }
        } finally
        {
            rs.close();
        }

        return aFeed;
    }

    /**
     * Loads query feed object.
     *
     * @param feedId feed id.
     *
     * @return query feed.
     *
     * @throws SQLException if database operation fails.
     * @throws PersistenceException if persistent operation fails.
     */
    private IFeed loadQueryFeed(long feedId)
        throws SQLException, PersistenceException
    {
        QueryFeed feed = new QueryFeed();

        loadDataFeedPart(feed, feedId);
        feed = loadQueryFeedPart(feedId, feed);

        return feed;
    }

    /**
     * Loads data from QueryFeeds table into the query feed object. If the queryType isn't
     * supported the return will be NULL.
     *
     * @param feedId    ID of the feed.
     * @param aFeed     feed object.
     *
     * @return feed or NULL if query type is unsupported.
     *
     * @throws SQLException if database operation fails.
     * @throws PersistenceException if driver isn't registered.
     */
    private QueryFeed loadQueryFeedPart(long feedId, QueryFeed aFeed)
        throws SQLException, PersistenceException
    {
        if (psLoadQueryFeedPart == null)
        {
            psLoadQueryFeedPart = getPreparedStatement(
                "SELECT TITLE, QUERYTYPE, KEYWORDS, DEDUP_ENABLED, DEDUP_FROM, DEDUP_TO " +
                "FROM QUERYFEEDS WHERE FEEDID=?");
        }

        psLoadQueryFeedPart.setLong(1, feedId);

        ResultSet rs = psLoadQueryFeedPart.executeQuery();
        try
        {
            if (rs.next())
            {
                int type = rs.getInt(2);
                QueryType queryType = QueryType.getQueryType(type);

                aFeed.setBaseTitle(rs.getString(1));
                aFeed.setQueryType(queryType);
                aFeed.setParameter(rs.getString(3));
                aFeed.setDedupEnabled(rs.getBoolean(4));
                aFeed.setDedupFrom(rs.getInt(5));
                aFeed.setDedupTo(rs.getInt(6));
            } else
            {
                throw new PersistenceException(MessageFormat.format(
                    Strings.error("db.feed.was.not.found.in.directfeeds.table"), feedId));
            }
        } finally
        {
            rs.close();
        }

        return aFeed;
    }

    /**
     * Loads direct feed from database.
     *
     * @param feedId    feed ID.
     *
     * @return direct feed object.
     *
     * @throws SQLException if database operation fails.
     * @throws PersistenceException if driver isn't registered.
     */
    private IFeed loadDirectFeed(long feedId)
        throws SQLException, PersistenceException
    {
        DirectFeed feed = new DirectFeed();

        if (psLoadDirectFeed == null)
        {
            psLoadDirectFeed = getPreparedStatement(
                "SELECT TITLE, AUTHOR, DESCRIPTION, CUSTOMTITLE, CUSTOMAUTHOR, " +
                "CUSTOMDESCRIPTION, DEAD, SITEURL, XMLURL, INLINKS, " +
                "LASTMETADATAUPDATETIME, USERTAGS, UNSAVEDUSERTAGS, TAGSDESCRIPTION, TAGSEXTENDED, " +
                "DISABLED, SYNC_HASH " +
                "FROM DIRECTFEEDS  " +
                "WHERE FEEDID=?");
        }

        psLoadDirectFeed.setLong(1, feedId);

        ResultSet rs = psLoadDirectFeed.executeQuery();
        try
        {
            if (rs.next())
            {
                try
                {
                    String siteURL = rs.getString("SITEURL");
                    feed.setSiteURL(siteURL == null ? null : new URL(siteURL));
                } catch (MalformedURLException e)
                {
                    throw new PersistenceException(MessageFormat.format(
                        Strings.error("db.currupted.site.url.for.feed"), feedId));
                }

                try
                {
                    String xmlURL = rs.getString("XMLURL");
                    feed.setXmlURL(xmlURL == null ? null : new URL(xmlURL));
                } catch (MalformedURLException e)
                {
                    throw new PersistenceException(MessageFormat.format(
                        Strings.error("db.currupted.xml.url.for.feed.0"), feedId));
                }

                feed.setBaseTitle(rs.getString("TITLE"));
                feed.setBaseAuthor(rs.getString("AUTHOR"));
                feed.setBaseDescription(rs.getString("DESCRIPTION"));
                feed.setCustomTitle(rs.getString("CUSTOMTITLE"));
                feed.setCustomAuthor(rs.getString("CUSTOMAUTHOR"));
                feed.setCustomDescription(rs.getString("CUSTOMDESCRIPTION"));
                feed.setDead(rs.getBoolean("DEAD"));
                feed.setInLinks(rs.getInt("INLINKS"));
                feed.setLastMetaDataUpdateTime(rs.getLong("LASTMETADATAUPDATETIME"));
                feed.setUserTags(StringUtils.keywordsToArray(rs.getString("USERTAGS")));
                feed.setUnsavedUserTags(rs.getBoolean("UNSAVEDUSERTAGS"));
                feed.setTagsDescription(rs.getString("TAGSDESCRIPTION"));
                feed.setTagsExtended(rs.getString("TAGSEXTENDED"));
                feed.setDisabled(rs.getBoolean("DISABLED"));
                feed.setSyncHash(rs.getInt("SYNC_HASH"));
            } else
            {
                throw new PersistenceException(MessageFormat.format(
                    Strings.error("db.feed.was.not.found.in.directfeeds.table"), feedId));
            }
        } finally
        {
            rs.close();
        }

        // Lading the DataFeed part here because setting of some properties in DirectFeed (for
        // example, XML URL) resets some of properties here (for example, lastPollTime).
        loadDataFeedPart(feed, feedId);

        return feed;
    }

    /**
     * Loads data from DATAFEEDS table.
     *
     * @param feed      feed to load data into.
     * @param feedId    feed ID to question in database.
     *
     * @throws SQLException if database operation fails.
     * @throws PersistenceException if driver isn't registered.
     */
    private void loadDataFeedPart(DataFeed feed, long feedId)
        throws SQLException, PersistenceException
    {
        if (psLoadDataFeedPart == null)
        {
            psLoadDataFeedPart = getPreparedStatement(
                "SELECT INITTIME, LASTPOLLTIME, LASTUPDATESERVERTIME, RETRIEVALS, FORMAT, " +
                    "LANGUAGE, PURGELIMIT, LASTFETCHARTICLEKEYS, " +
                    "UPDATEPERIOD, TOTALPOLLEDARTICLES, RATING " +
                "FROM DATAFEEDS " +
                "WHERE FEEDID=?");
        }

        psLoadDataFeedPart.setLong(1, feedId);

        ResultSet rs = psLoadDataFeedPart.executeQuery();
        try
        {
            if (rs.next())
            {
                feed.setInitTime(rs.getLong("INITTIME"));
                feed.setLastPollTime(rs.getLong("LASTPOLLTIME"));
                feed.setLastUpdateServerTime(rs.getLong("LASTUPDATESERVERTIME"));
                feed.setRetrievals(rs.getInt("RETRIEVALS"));
                feed.setFormat(rs.getString("FORMAT"));
                feed.setLanguage(rs.getString("LANGUAGE"));
                feed.setPurgeLimit(rs.getInt("PURGELIMIT"));
                feed.setUpdatePeriod(rs.getLong("UPDATEPERIOD"));
                feed.setTotalPolledArticles(rs.getInt("TOTALPOLLEDARTICLES"));
                feed.setRating(rs.getInt("RATING"));

                String lfa = rs.getString("LASTFETCHARTICLEKEYS");
                String[] keys = lfa == null ? new String[0] : StringUtils.split(lfa, ",");
                for (int i = 0; i < keys.length; i++) keys[i] = keys[i].intern();
                feed.setLastFetchArticleKeys(keys);
            } else
            {
                throw new PersistenceException(MessageFormat.format(
                    Strings.error("db.feed.was.not.found.in.datafeeds.table"), feedId));
            }
        } finally
        {
            rs.close();
        }

        loadArticles(feed, feedId);
    }

    /**
     * Loads all articles from the database.
     *
     * @throws SQLException in case of db error.
     */
    private void loadAllArticles()
        throws SQLException
    {
        articles = new HashMap<Long, List<IArticle>>();

        ResultSet rs = getConnection().createStatement().executeQuery("SELECT ID, AUTHOR, " +
            "PUBLICATIONDATE, TITLE, SUBJECT, READ, PINNED, LINK, SIMPLEMATCHKEY, FEEDID, " +
            "POSITIVE_SENTIMENTS, NEGATIVE_SENTIMENTS " +
            "FROM ARTICLES A LEFT JOIN ARTICLE_PROPERTIES P ON A.ID=P.ARTICLEID");

        try
        {
            while (rs.next())
            {
                LazyArticle article = new LazyArticle(null);
                article.setProvider(articleTextProvider);
                article.setID(rs.getLong("ID"));
                article.setAuthor(rs.getString("AUTHOR"));
                long publicationDate = rs.getLong("PUBLICATIONDATE");
                article.setPublicationDate(publicationDate == -1 ? null : new Date(publicationDate));
                article.setTitle(rs.getString("TITLE"));
                article.setSubject(rs.getString("SUBJECT"));
                article.setRead(rs.getBoolean("READ"));
                article.setPinned(rs.getBoolean("PINNED"));
                String link = rs.getString("LINK");
                try
                {
                    article.setLink(link == null ? null : new URL(link));
                } catch (MalformedURLException e)
                {
                    LOG.log(Level.SEVERE, MessageFormat.format(
                        Strings.error("invalid.url"), link), e);
                }

                article.setSimpleMatchKey(rs.getString("SIMPLEMATCHKEY"));
                article.setSentimentsCounts(rs.getInt("POSITIVE_SENTIMENTS"), rs.getInt("NEGATIVE_SENTIMENTS"));

                // Save article
                long feedId = rs.getLong("FEEDID");
                List<IArticle> arts = articles.get(feedId);
                if (arts == null)
                {
                    arts = new ArrayList<IArticle>();
                    articles.put(feedId, arts);
                }
                arts.add(article);
            }
        } finally
        {
            rs.close();
        }
    }

    /**
     * Loads articles for the feed from database.
     *
     * @param feed feed to load data for.
     * @param feedId ID of the feed.
     *
     * @throws SQLException if database fails to complete the request.
     */
    private void loadArticles(DataFeed feed, long feedId)
        throws SQLException
    {
        List<IArticle> arts = articles.get(feedId);
        if (arts != null) for (IArticle article : arts) feed.appendArticle(article);
    }

    // Guides --------------------------------------------------------------------------------------

    /**
     * Inserts guide and all of its feeds including articles into database.
     *
     * @param guide     guide to insert.
     * @param position  position in the set.
     *
     * @throws NullPointerException if guide isn't specified.
     * @throws IllegalStateException if guide is already in database.
     * @throws IllegalArgumentException if guide is of unsupported type.
     * @throws PersistenceException if database operation fails.
     */
    public void insertGuide(IGuide guide, int position)
        throws PersistenceException
    {
        if (LOG.isLoggable(Level.FINEST)) LOG.finest("insertGuide");
        if (guide == null) throw new NullPointerException(MSG_GUIDE_UNSPECIFIED);

        // Locking objects in correct order
        synchronized (guide)
        {
            synchronized (this)
            {
                try
                {
                    insertGuideHierarchy(guide, position);
                    commit();
                } catch (SQLException e)
                {
                    rollback();
                    throw new PersistenceException(Strings.error("db.failed.to.insert.guide.with.hierarchy"), e);
                }
            }
        }
    }

    /**
     * Inserts guide and all of its feeds including articles into database.
     *
     * @param guide     guide to insert.
     * @param position  position in the set.
     *
     * @throws NullPointerException if guide isn't specified.
     * @throws IllegalStateException if guide is already in database.
     * @throws IllegalArgumentException if guide is of unsupported type.
     * @throws SQLException if database operation fails.
     *
     * @throws PersistenceException if database operation fails.
     */
    private void insertGuideHierarchy(IGuide guide, int position)
        throws SQLException, PersistenceException
    {
        guidesManager.insertGuide(guide, position);

        int count;

        // Save reading lists
        if (guide instanceof StandardGuide)
        {
            ReadingList[] lists = ((StandardGuide)guide).getReadingLists();
            for (ReadingList list : lists)
            {
                insertReadingList(list);

                // Save all feeds from the reading list
                DirectFeed[] feeds = list.getFeeds();
                for (DirectFeed feed : feeds)
                {
                    if (feed.getID() == -1) insertFeedHierarchy(feed);
                    feedsManager.addFeedToReadingList(list, feed);
                }
            }
        }


        // Save directly associated feeds
        count = guide.getFeedsCount();
        for (int i = 0; i < count; i++)
        {
            IFeed feed = guide.getFeedAt(i);
            if (guide.hasDirectLinkWith(feed))
            {
                if (feed.getID() == -1) insertFeedHierarchy(feed);
                feedsManager.addFeedToGuide(guide, feed);
            }
        }
    }

    /**
     * Removes guide from database.
     *
     * @param guide guide to remove.
     *
     * @throws NullPointerException if guide isn't specified.
     * @throws IllegalStateException if guide is not in database.
     * @throws PersistenceException if database operation fails.
     */
    public void removeGuide(IGuide guide)
        throws PersistenceException
    {
        if (LOG.isLoggable(Level.FINEST)) LOG.finest("removeGuide");
        if (guide == null) throw new NullPointerException(MSG_GUIDE_UNSPECIFIED);

        // We have to lock HPM only after we have all other necessary locks
        synchronized (guide)
        {
            synchronized (this)
            {
                try
                {
                    guidesManager.removeGuide(guide);
                    commit();
                } catch (SQLException e)
                {
                    rollback();
                    throw new PersistenceException(Strings.error("db.failed.to.remove.guide"), e);
                }
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
     * @throws PersistenceException if database operation fails.
     */
    public synchronized void updateGuide(IGuide guide, int position)
        throws PersistenceException
    {
        if (LOG.isLoggable(Level.FINEST)) LOG.finest("updateGuide");
        try
        {
            guidesManager.updateGuide(guide, position);
            commit();
        } catch (SQLException e)
        {
            rollback();
            throw new PersistenceException(Strings.error("db.failed.to.update.guide"), e);
        }
    }

    /**
     * Updates guide positions in database.
     *
     * @param set guides set.
     *
     * @throws PersistenceException if database operation fails.
     */
    public synchronized void updateGuidePositions(GuidesSet set)
        throws PersistenceException
    {
        if (LOG.isLoggable(Level.FINEST)) LOG.finest("updateGuidePositions");
        try
        {
            guidesManager.updateGuidePositions(set);
            commit();
        } catch (SQLException e)
        {
            rollback();
            throw new PersistenceException("Failed to update the guide positions.", e);
        }
    }

    /**
     * Adds new record about deleted feed for the guide.
     *
     * @param guide   guide.
     * @param feedKey feed key.
     *
     * @throws PersistenceException if database operation fails.
     */
    public void addDeletedFeedToGuide(IGuide guide, String feedKey)
        throws PersistenceException
    {
        if (LOG.isLoggable(Level.FINEST)) LOG.finest("addDeletedFeedToGuide");
        if (guide == null) throw new NullPointerException(MSG_GUIDE_UNSPECIFIED);

        synchronized (guide)
        {
            synchronized (this)
            {
                try
                {
                    guidesManager.addDeletedFeedToGuide(guide, feedKey);
                    commit();
                } catch (SQLException e)
                {
                    rollback();
                    throw new PersistenceException(Strings.error("db.failed.to.add.deleted.feed.to.guide"), e);
                }
            }
        }
    }

    /**
     * Removes all records about deleted feeds.
     *
     * @param guide guide.
     *
     * @throws PersistenceException if database operation fails.
     */
    public void removeDeletedFeedsFromGuide(IGuide guide)
        throws PersistenceException
    {
        if (LOG.isLoggable(Level.FINEST)) LOG.finest("removeDeletedFeedsFromGuide");
        try
        {
            guidesManager.removeDeletedFeedsFromGuide(guide);
            commit();
        } catch (SQLException e)
        {
            rollback();
            throw new PersistenceException(Strings.error("db.failed.to.remove.deleted.feeds.from.guide"), e);
        }
    }

    // Reading Lists -------------------------------------------------------------------------------

    /**
     * Inserts reading list which is connected to some guide.
     *
     * @param aList reading list.
     *
     * @throws PersistenceException if database operation fails.
     */
    public synchronized void insertReadingList(ReadingList aList)
        throws PersistenceException
    {
        if (LOG.isLoggable(Level.FINEST)) LOG.finest("insertReadingList");
        try
        {
            readingListsManager.insertReadingList(aList);
            commit();
        } catch (SQLException e)
        {
            rollback();
            throw new PersistenceException(Strings.error("db.failed.to.insert.reading.list"), e);
        }
    }

    /**
     * Removes reading list from the database. All connected feeds become disconnected.
     *
     * @param aList reading list.
     *
     * @throws PersistenceException if database operation fails.
     */
    public synchronized void removeReadingList(ReadingList aList)
        throws PersistenceException
    {
        if (LOG.isLoggable(Level.FINEST)) LOG.finest("removeReadingList");
        if (aList == null) throw new NullPointerException(MSG_READING_LIST_UNSPECIFIED);

        try
        {
            readingListsManager.removeReadingList(aList);
            commit();
        } catch (SQLException e)
        {
            rollback();
            throw new PersistenceException(Strings.error("db.failed.to.remove.reading.list"), e);
        }
    }

    /**
     * Updates information about reading list.
     *
     * @param aList reading list.
     *
     * @throws PersistenceException if database operation fails.
     */
    public synchronized void updateReadingList(ReadingList aList)
        throws PersistenceException
    {
        if (LOG.isLoggable(Level.FINEST)) LOG.finest("updateReadingList");
        try
        {
            readingListsManager.updateReadingList(aList);
            commit();
        } catch (SQLException e)
        {
            rollback();
            throw new PersistenceException(Strings.error("db.failed.to.update.reading.list"), e);
        }
    }

    // Feeds ---------------------------------------------------------------------------------------

    /**
     * Adds feed to a guide -- adds the link.
     *
     * @param guide guide.
     * @param feed  feed.
     *
     * @throws PersistenceException if database operation fails.
     */
    public void addFeedToGuide(IGuide guide, IFeed feed)
        throws PersistenceException
    {
        if (guide == null) throw new NullPointerException(MSG_GUIDE_UNSPECIFIED);

        synchronized (guide)
        {
            synchronized (feed)
            {
                synchronized (this)
                {
                    try
                    {
                        if (feed.getID() == -1) insertFeedHierarchy(feed);
                        feedsManager.addFeedToGuide(guide, feed);
                        commit();
                    } catch (SQLException e)
                    {
                        rollback();
                        throw new PersistenceException(Strings.error("db.failed.to.add.feed.to.guide"), e);
                    }
                }
            }
        }
    }

    /**
     * Adds feed to a reading list -- adds the link.
     *
     * @param readingList reading list.
     * @param feed        feed.
     *
     * @throws PersistenceException if database operation fails.
     */
    public void addFeedToReadingList(ReadingList readingList, IFeed feed)
        throws PersistenceException
    {
        if (readingList == null) throw new NullPointerException(MSG_READING_LIST_UNSPECIFIED);
        if (feed == null) throw new NullPointerException(MSG_FEED_UNSPECIFIED);

        Object guide = readingList.getParentGuide();
        if (guide == null) guide = new Object();

        synchronized (guide)
        {
            synchronized (readingList)
            {
                synchronized (feed)
                {
                    synchronized (this)
                    {
                        try
                        {
                            if (feed.getID() == -1) insertFeedHierarchy(feed);
                            feedsManager.addFeedToReadingList(readingList, feed);
                            commit();
                        } catch (SQLException e)
                        {
                            rollback();
                            throw new PersistenceException(Strings.error("db.failed.to.add.feed.to.reading.list"), e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes link between the reading list and the feed.
     *
     * @param readingList reading list.
     * @param feed        feed.
     *
     * @throws PersistenceException if database operation fails.
     */
    public void removeFeedFromReadingList(ReadingList readingList, IFeed feed)
        throws PersistenceException
    {
        if (readingList == null) throw new NullPointerException(MSG_READING_LIST_UNSPECIFIED);
        if (feed == null) throw new NullPointerException(MSG_FEED_UNSPECIFIED);

        synchronized (readingList)
        {
            synchronized (feed)
            {
                synchronized (this)
                {
                    try
                    {
                        feedsManager.removeFeedFromReadingList(readingList, feed);
                        commit();
                    } catch (SQLException e)
                    {
                        rollback();
                        throw new PersistenceException(Strings.error("db.failed.to.remove.feed.from.reading.list"), e);
                    }
                }
            }
        }
    }

    /**
     * Removes link between the guide and the feed.
     *
     * @param guide guide.
     * @param feed  feed.
     *
     * @throws PersistenceException if database operation fails.
     */
    public void removeFeedFromGuide(IGuide guide, IFeed feed)
        throws PersistenceException
    {
        if (guide == null) throw new NullPointerException(MSG_GUIDE_UNSPECIFIED);
        if (feed == null) throw new NullPointerException(MSG_FEED_UNSPECIFIED);

        synchronized (guide)
        {
            synchronized (feed)
            {
                synchronized (this)
                {
                    try
                    {
                        feedsManager.removeFeedFromGuide(guide, feed);
                        commit();
                    } catch (SQLException e)
                    {
                        rollback();
                        throw new PersistenceException(Strings.error("db.failed.to.remove.feed.from.guide"), e);
                    }
                }
            }
        }
    }

    /**
     * Updates the link between the guide and the feed.
     *
     * @param guide guide.
     * @param feed  feed.
     *
     * @throws PersistenceException if database operation fails.
     */
    public void updateFeedLink(StandardGuide guide, IFeed feed)
        throws PersistenceException
    {
        if (guide == null) throw new NullPointerException(MSG_GUIDE_UNSPECIFIED);
        if (feed == null) throw new NullPointerException(MSG_FEED_UNSPECIFIED);

        synchronized (guide)
        {
            synchronized (feed)
            {
                synchronized (this)
                {
                    try
                    {
                        feedsManager.updateFeedLink(guide, feed);
                        commit();
                    } catch (SQLException e)
                    {
                        rollback();
                        throw new PersistenceException(Strings.error("db.failed.to.update.feed.link"), e);
                    }
                }
            }
        }
    }

    /**
     * Inserts the feed into database.
     *
     * @param feed feed to insert.
     *
     * @throws NullPointerException if feed isn't specified.
     * @throws IllegalStateException if feed is already in database,
     *                                  or has no guide assigned,
     *                                  or guide isn't persisted.
     * @throws IllegalArgumentException if feed is of unsupported type.
     * @throws PersistenceException if database operation fails.
     */
    public void insertFeed(IFeed feed)
        throws PersistenceException
    {
        if (LOG.isLoggable(Level.FINEST)) LOG.finest("insertFeed");
        if (feed == null) throw new NullPointerException(MSG_FEED_UNSPECIFIED);

        // Locking objects in correct order
        synchronized (feed)
        {
            synchronized (this)
            {
                try
                {
                    insertFeedHierarchy(feed);
                    commit();
                } catch (SQLException e)
                {
                    rollback();
                    throw new PersistenceException(Strings.error("db.failed.to.insert.feeds.hierarchy"), e);
                }
            }
        }
    }

    /**
     * Inserts whole hierarchy of feed with articles.
     *
     * @param feed feed to insert.
     *
     * @throws SQLException if database operation fails.
     */
    private void insertFeedHierarchy(IFeed feed)
        throws SQLException
    {
        if (feed instanceof DataFeed || feed instanceof SearchFeed)
        {
            feedsManager.insertFeed(feed);
        }

        if (feed instanceof DataFeed)
        {
            int count = feed.getArticlesCount();
            for (int i = 0; i < count; i++)
            {
                IArticle article = feed.getArticleAt(i);
                if (article.getID() == -1) articlesManager.insertArticle(article);
            }
        }
    }

    /**
     * Removes the feed from database.
     *
     * @param feed feed to remove.
     *
     * @throws NullPointerException if feed isn't specified.
     * @throws IllegalStateException if feed is not in database.
     * @throws PersistenceException if database operation fails.
     */
    public synchronized void removeFeed(IFeed feed)
        throws PersistenceException
    {
        if (LOG.isLoggable(Level.FINEST)) LOG.finest("removeFeed");
        try
        {
            feedsManager.removeFeed(feed);
            commit();
        } catch (SQLException e)
        {
            rollback();
            throw new PersistenceException(Strings.error("db.failed.to.remove.feed"), e);
        }
    }

    /**
     * Moves feed from source guide to destination guide.
     *
     * @param feed      feed to move.
     * @param source    source guide to move from.
     * @param dest      destination guide to move to.
     *
     * @throws NullPointerException if feed or source or destination guides aren't specified.
     * @throws IllegalStateException if feed or one of the guides are transient.
     * @throws PersistenceException if database operation fails.
     */
    public synchronized void moveFeed(IFeed feed, IGuide source, IGuide dest)
        throws PersistenceException
    {
        if (LOG.isLoggable(Level.FINEST)) LOG.finest("moveFeed");
        try
        {
            feedsManager.moveFeed(feed, source, dest);
            commit();
        } catch (SQLException e)
        {
            rollback();
            throw new PersistenceException(Strings.error("db.failed.to.move.feed"), e);
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
     * @throws PersistenceException if database operation fails.
     */
    public synchronized void updateFeed(IFeed feed, String property)
        throws PersistenceException
    {
        if (LOG.isLoggable(Level.FINEST)) LOG.finest("updateFeed");
        try
        {
            feedsManager.updateFeed(feed, property);
            commit();
        } catch (SQLException e)
        {
            rollback();
            throw new PersistenceException(Strings.error("db.failed.to.update.feed"), e);
        }
    }

    /**
     * Updates positions of feeds within the guide.
     *
     * @param guide guide to reposition feeds.
     *
     * @throws PersistenceException if database operation fails.
     */
    public void updateFeedsPositions(IGuide guide)
        throws PersistenceException
    {
        if (guide == null) throw new NullPointerException(MSG_GUIDE_UNSPECIFIED);

        synchronized (guide)
        {
            synchronized (this)
            {
                try
                {
                    feedsManager.updateFeedsPositions(guide);
                    commit();
                } catch (SQLException e)
                {
                    rollback();
                    throw new PersistenceException(Strings.error("db.failed.to.update.feeds.pos"), e);
                }
            }
        }
    }

    /**
     * Updates position of a feed within the guide.
     *
     * @param guide guide.
     * @param feed  feed.
     *
     * @throws PersistenceException if database operation fails.
     */
    public void updateFeedPosition(IGuide guide, IFeed feed)
        throws PersistenceException
    {
        if (guide == null) throw new NullPointerException(MSG_GUIDE_UNSPECIFIED);

        synchronized (guide)
        {
            synchronized (this)
            {
                try
                {
                    feedsManager.updateFeedPosition(guide, feed);
                    commit();
                } catch (SQLException e)
                {
                    rollback();
                    throw new PersistenceException(Strings.error("db.failed.to.update.feed.pos"), e);
                }
            }
        }
    }

    // Articles ------------------------------------------------------------------------------------

    /**
     * Inserts the article in database.
     *
     * @param article article to insert.
     *
     * @throws NullPointerException if article isn't specified.
     * @throws IllegalStateException if article is already in database, or
     *                               article isn't assigned to feed, or
     *                               feed this article is assigned to is transient.
     * @throws IllegalArgumentException if article is of unsupported type.
     * @throws PersistenceException if database operation fails.
     */
    public synchronized void insertArticle(IArticle article)
        throws PersistenceException
    {
        if (LOG.isLoggable(Level.FINEST)) LOG.finest("insertArticle");
        try
        {
            articlesManager.insertArticle(article);
            commit();

            if (article instanceof LazyArticle) ((LazyArticle)article).setProvider(articleTextProvider);
        } catch (SQLException e)
        {
            rollback();
            throw new PersistenceException(Strings.error("db.failed.to.insert.article"), e);
        }
    }

    /**
     * Removes article from database.
     *
     * @param article article to remove.
     *
     * @throws NullPointerException if article isn't specified.
     * @throws IllegalStateException if article is not in database.
     * @throws PersistenceException if database operation fails.
     */
    public synchronized void removeArticle(IArticle article)
        throws PersistenceException
    {
        if (LOG.isLoggable(Level.FINEST)) LOG.finest("removeArticle");
        try
        {
            articlesManager.removeArticle(article);
            commit();
        } catch (SQLException e)
        {
            rollback();
            throw new PersistenceException(Strings.error("db.failed.to.remove.article"), e);
        }
    }

    /**
     * Updates article in database.
     *
     * @param article article to update.
     *
     * @throws NullPointerException if article isn't specified.
     * @throws IllegalStateException if article is not in database.
     * @throws IllegalArgumentException if article is of unsupported type.
     * @throws PersistenceException if database operation fails.
     */
    public void updateArticle(IArticle article)
        throws PersistenceException
    {
        if (LOG.isLoggable(Level.FINEST)) LOG.finest("updateArticle");

        synchronized (article)
        {
            synchronized (this)
            {
                try
                {
                    articlesManager.updateArticle(article);
                    commit();
                } catch (SQLException e)
                {
                    MemoryUsage mu = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
                    System.out.println("Init=" + mu.getInit() + ", usage=" + mu.getUsed() + ", " +
                        "commited=" + mu.getCommitted() + ", max=" + mu.getMax());
                    rollback();
                    throw new PersistenceException(Strings.error("db.failed.to.update.article"), e);
                }
            }
        }
    }
    /**
     * Updates article properties in database.
     *
     * @param article article properties to update.
     *
     * @throws NullPointerException if article isn't specified.
     * @throws IllegalStateException if article is not in database.
     * @throws IllegalArgumentException if article is of unsupported type.
     * @throws PersistenceException if database operation fails.
     */
    public void updateArticleProperties(IArticle article)
        throws PersistenceException
    {
        if (LOG.isLoggable(Level.FINEST)) LOG.finest("updateArticleProperties");

        synchronized (article)
        {
            synchronized (this)
            {
                try
                {
                    articlesManager.updateArticleProperties(article);
                    commit();
                } catch (SQLException e)
                {
                    throw new PersistenceException("Failed to update article properties", e);
                }
            }
        }
    }

    // Common --------------------------------------------------------------------------------------

    /**
     * Returns the connection to database.
     *
     * @return connection.
     *
     * @throws SQLException         in case of failed connection establishment.
     */
    synchronized Connection getConnection()
        throws SQLException
    {
        // It's really weird that in some cases when we connect to the database from
        // the different thread than the first connection has been established, we
        // start another HSQLDB instance. And the number of instances grows with the
        // number of connection attempts.
        //
        // The solution is to use the same connection across the application, but
        // we should closely keep an eye on how we use this single connection.
        //
        // This object (HsqlPersistenceManager) is a lock for this connection. If
        // some code requires to use the connection it should establish the monitor
        // of this object first and then continue with database operations.
        if (con == null || con.isClosed())
        {
            con = getConnection0(false);
            con.setAutoCommit(false);
        }

        return con;
    }

    private Connection getConnection0(boolean readonly)
        throws SQLException
    {
        Properties props = new Properties();
        props.setProperty("user", databaseUsername);
        props.setProperty("password", databasePassword);
        props.setProperty("shutdown", "true");
        if (readonly) props.setProperty("hsqldb.files_readonly", "true");

        return DriverManager.getConnection(databaseUrl, props);
    }

    /**
     * Closes connection in current thread if it's present and it's not closed yet.
     *
     * @throws SQLException in case if there's a problem with closing or closed state checking.
     */
    synchronized void closeConnection()
        throws SQLException
    {
        if (con != null && !con.isClosed()) con.close();
    }

    /**
     * Returns prepared statement from cache or creates new one using current connection.
     *
     * @param statement statement to return.
     *
     * @return prepared statement.
     *
     * @throws SQLException         in case of failed connection establishment or
     *                              statement preparation.
     */
    PreparedStatement getPreparedStatement(String statement)
        throws SQLException
    {
        Connection connection = getConnection();
        return connection.prepareStatement(statement);
    }

    /**
     * Returns the ID of last inserted record.
     *
     * @return ID of last inserted record.
     *
     * @throws SQLException if database fails to return ID.
     */
    long getInsertedID()
        throws SQLException
    {
        long id = -1;

        ResultSet rs = getPreparedStatement("CALL IDENTITY()").executeQuery();
        if (rs.next())
        {
            id = rs.getLong(1);
        }

        return id;
    }

    /**
     * Throws the <code>PersistenceException</code>. If the exception passed in is instance of
     * <code>PersistenceException</code> then it's thrown directly. If it's not then the new
     * exception is created wrapping the one passed in.
     *
     * @param e exception to wrap.
     * @param message message for new <code>PersistenceException</code>.
     *
     * @throws PersistenceException is thrown all the time.
     */
    static void rethrow(String message, Exception e)
        throws PersistenceException
    {
        if (e instanceof PersistenceException)
        {
            throw (PersistenceException)e;
        } else
        {
            throw new PersistenceException(message, e);
        }
    }

    /**
     * Commits the transaction or forces reconnect.
     */
    void commit()
    {
        try
        {
            getConnection().commit();
        } catch (Exception e)
        {
            LOG.log(Level.SEVERE, Strings.error("db.failed.to.commit.the.transaction"), e);

            // Force reconnect
            resetConnection();
        }
    }

    /**
     * Resets the connection forcing the next call to <code>getConnection()</code>
     * to open fresh connection to database.
     */
    private synchronized void resetConnection()
    {
        try
        {
            closeConnection();
        } catch (SQLException e)
        {
            // Most probably this is an emergency call, so no logging
        }
    }

    /**
     * Rolls back the transaction or forces reconnect.
     */
    void rollback()
    {
        try
        {
            getConnection().rollback();
        } catch (Exception e)
        {
            LOG.log(Level.SEVERE, Strings.error("db.failed.to.rollback.the.transaction"), e);

            // Force reconnect
            resetConnection();
        }
    }

    /**
     * Clears ID of the feed and all children.
     *
     * @param feed feed ID of which to clear.
     */
    static void clearFeedID(IFeed feed)
    {
        feed.setID(-1);

        if (feed instanceof DataFeed)
        {
            IArticle[] articles = feed.getArticles();
            for (IArticle article : articles)
            {
                if (article.getFeed() == feed) article.setID(-1);
            }
        }
    }

    // Supplementary tools -------------------------------------------------------------------------

    /**
     * Reads the version of schema from database.
     *
     * @return current schema version or (-1) if database is missing.
     */
    int getCurrentSchemaVersion()
    {
        int version = -1;

        try
        {
            version = Integer.parseInt(getApplicationProperty(AP_SCHEMA_VERSION));
        } catch (RuntimeException e)
        {
            // Move on with RuntimeExceptions
            throw e;
        } catch (Exception e)
        {
            // Problems with data in database: old, missing or has bad version
        }

        return version;
    }

    /**
     * Sets the version of database schema.
     *
     * @param version version of schema.
     *
     * @throws PersistenceException in case of any database problem.
     */
    private void setSchemaVersion(int version)
        throws PersistenceException
    {
        setApplicationProperty(AP_SCHEMA_VERSION, Integer.toString(version));
    }

    /**
     * This is the check for migration to be done. If the version of database schema
     * differs from the one used by current application version the migration is
     * performed in steps.
     *
     * @param aCurrentSchemeVersion current scheme version.
     *
     * @return <code>TRUE</code> if migration took place.
     *
     * @throws MigrationException in case if migration failed.
     */
    private synchronized boolean migrateIfNecessary(int aCurrentSchemeVersion)
        throws MigrationException
    {
        if (aCurrentSchemeVersion < 0)
        {
            // Database is damaged
            // The database will be reset.
            backupDatabaseIfNecessary(aCurrentSchemeVersion);
            throw new MigrationException("Corrupted database.", null);
        } else
        {
            // Detect current schema version and if it's equal to most modern return
            int latestVersion = MIGRATION_STEPS.length - 1;

            // >= instead of == because sometimes downgrades happen and application
            // cannot start because it cannot update database with changes which are
            // already there.
            if (aCurrentSchemeVersion >= latestVersion) return false;

            if (LOG.isLoggable(Level.FINE))
            {
                LOG.fine("Migration procedure required. Current db version=" +
                    aCurrentSchemeVersion + ", latest=" + latestVersion);
            }

            if (doBackupOnUpgrade) backupDatabaseIfNecessary(aCurrentSchemeVersion);

            boolean tooOld = latestVersion - aCurrentSchemeVersion > 3;

            // Perform migration in steps
            try
            {
                long globalStart = System.currentTimeMillis();

                Connection connection = getConnection();
                for (int v = aCurrentSchemeVersion + 1; v <= latestVersion; v++)
                {
                    long start = System.currentTimeMillis();

                    MIGRATION_STEPS[v].perform(connection, this);

                    if (LOG.isLoggable(Level.INFO))
                    {
                        LOG.info(MessageFormat.format("Migration step {0} took {1,number} ms",
                            v, System.currentTimeMillis() - start));
                    }
                }

                // Global Migration stats
                if (LOG.isLoggable(Level.INFO))
                {
                    LOG.info(MessageFormat.format("Migration took {0,number} ms",
                        System.currentTimeMillis() - globalStart));
                }

                // Set the most modern version as current and commit changes
                setSchemaVersion(latestVersion);
            } catch (Exception e)
            {
                String msg;

                if (e instanceof SQLException && "08001".equals(((SQLException)e).getSQLState()))
                {
                    msg = Strings.message("db.migration.error.data.access.problem");
                } else if (tooOld)
                {
                    msg = Strings.message("db.migration.error.migration.failure.too.old");
                } else
                {
                    LOG.log(Level.SEVERE, Strings.error("db.migration.problem"), e);

                    msg = Strings.message("db.migration.error.migration.failure.general");
                }

                showMessage(Application.getDefaultParentFrame(),
                    msg, Strings.message("db.migration.title"), JOptionPane.ERROR_MESSAGE);

                // Throw an exception farther
                if (e instanceof MigrationException)
                {
                    throw (MigrationException)e;
                } else
                {
                    throw new MigrationException(Strings.error("db.failed.to.get.connection.for.migration"), e);
                }
            }
        }

        return true;
    }

    /**
     * Shows message box with the message if there's GUI present.
     *
     * @param parent    parent component.
     * @param msg       message to display.
     * @param title     title of the window.
     * @param type      type of the message.
     */
    private static void showMessage(JFrame parent, String msg, String title, int type)
    {
        if (hasGUI) JOptionPane.showMessageDialog(parent, msg, title, type);
    }

    /**
     * Creates a backup of current DB files if necessary.
     *
     * @param currentVersion current DB version.
     */
    private void backupDatabaseIfNecessary(int currentVersion)
    {
        try
        {
            if (!isBackupPresent(currentVersion)) makeBackup(currentVersion);
        } catch (IOException e)
        {
            LOG.log(Level.SEVERE, MessageFormat.format(
                Strings.error("db.failed.to.backup.current.db.files"), currentVersion), e);
        }
    }

    /**
     * Returns <code>TRUE</code> if backup folder for the given version exists and
     * filled with data.
     *
     * @param version   version of database.
     *
     * @return <code>TRUE</code> if backup is there.
     */
    boolean isBackupPresent(int version)
    {
        return new File(contextPath + getBackupFolderName(version)).exists();
    }

    /**
     * Creates the name of backup folder.
     *
     * @param version version of database scheme.
     *
     * @return the name.
     */
    static String getBackupFolderName(int version)
    {
        return "backup-" + version;
    }

    /**
     * Create a backup folder and copy current database files into it.
     *
     * @param version version of the backup.
     *
     * @throws IOException in case when copying operation failed.
     */
    void makeBackup(int version)
        throws IOException
    {
        File backupFolder = new File(contextPath + getBackupFolderName(version));
        makeBackup(backupFolder);
    }

    private void makeBackup(File directory)
        throws IOException
    {
        File workingFolder = new File(contextPath);
        File[] dbFiles = workingFolder.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name != null && name.startsWith("blogbridge.") && !name.endsWith(".lck");
            }
        });

        if (dbFiles != null)
        {
            if (!directory.exists()) directory.mkdir();

            for (File file : dbFiles) FileUtils.copyFileToDir(file, directory);
        }
    }

    /**
     * Shutdown the database.
     */
    public synchronized void shutdown()
    {
        shutdown(false);
    }

    /**
     * Shutdown the database.
     *
     * @param immediately shutdown the database immediately.
     */
    private void shutdown(boolean immediately)
    {
        try
        {
            if (con == null || con.isClosed()) return;

            con.createStatement().execute(immediately ? "SHUTDOWN IMMEDIATELY" : "SHUTDOWN COMPACT");
            closeConnection();
        } catch (Exception e)
        {
            // Forgive all exceptions as this call is absolutely optional
            LOG.log(Level.WARNING, Strings.error("db.failed.to.shutdown.database"), e);
        }
    }

    /**
     * Returns passwords repository.
     *
     * @return passwords repository.
     */
    public IPasswordsRepository getPasswordsRepository()
    {
        return passwordsRepository;
    }

    /**
     * Removes feed only if there are no guides and reading lists referring to it.
     *
     * @param aFeed feed to check.
     *
     * @throws PersistenceException if database operation fails.
     */
    public void removeFeedIfNoRefs(IFeed aFeed)
        throws PersistenceException
    {
        if (!aFeed.isDynamic() && aFeed.getParentGuides().length == 0) removeFeed(aFeed);
    }

    /**
     * Provides the texts of articles.
     */
    private class ArticleTextProvider implements IArticleTextProvider
    {
        private static final String MSG_AT_NOT_FOUND = "Article text was asked, but never found (id={0})";
        private static final String MSG_AT_CANT_LOAD = "Failed to load article text (id={0})";

        private static final String STMT_GET_TEXT = "SELECT text FROM ARTICLES WHERE ID=?";
        private static final String STMT_GET_PLAINTEXT = "SELECT plaintext FROM ARTICLES WHERE ID=?";

        private PreparedStatement psLoadText;
        private PreparedStatement psLoadPlainText;

        /**
         * Returns the text for the article by its ID.
         *
         * @param id article ID.
         *
         * @return text.
         */
        public String getArticleText(long id)
        {
            String text = null;
            Exception ex = null;

            // We do two attempts if there's a statement available.
            // If the first try fails, we reinitialize the statement and try the second time.
            for (int attempt = 0; attempt < 2; attempt++)
            {
                synchronized (HsqlPersistenceManager.this)
                {
                    try
                    {
                        if (psLoadText == null) psLoadText = getPreparedStatement(STMT_GET_TEXT);
                        text = getText(psLoadText, id);
                        ex = null;
                        break;
                    } catch (SQLException e)
                    {
                        ex = e;

                        // Close and release the statement
                        close(psLoadText);
                        psLoadText = null;
                    }
                }
            }

            // If failed, report.
            if (ex != null)
            {
                LOG.log(Level.WARNING, MessageFormat.format(MSG_AT_CANT_LOAD, id), ex);
            }

            return text;
        }

        /**
         * Provides the plain text of an article by its ID.
         *
         * @param id article ID.
         *
         * @return text.
         */
        public String getArticlePlainText(long id)
        {
            return getArticlePlainText0(id);
        }

        private String getArticlePlainText0(long id)
        {
            String text = null;
            Exception ex = null;

            // We do two attempts if there's a statement available.
            // If the first try fails, we reinitialize the statement and try the second time.
            for (int attempt = 0; attempt < 2; attempt++)
            {
                synchronized (HsqlPersistenceManager.this)
                {
                    try
                    {
                        if (psLoadPlainText == null) psLoadPlainText = getPreparedStatement(STMT_GET_PLAINTEXT);
                            text = getText(psLoadPlainText, id);
                            ex = null;
                            break;
                    } catch (SQLException e)
                    {
                        ex = e;

                        // Close and release the statement
                        close(psLoadPlainText);
                        psLoadPlainText = null;
                    }
                }
            }

            // If failed, report.
            if (ex != null)
            {
                LOG.log(Level.WARNING, MessageFormat.format(MSG_AT_CANT_LOAD, id), ex);
            }

            return text;
        }

        /**
         * Returns text by the query and ID.
         *
         * @param stmt  statement.
         * @param id    ID of the article.
         *
         * @return the text.
         * @throws SQLException in case of DB error.
         */
        private String getText(PreparedStatement stmt, long id)
            throws SQLException
        {
            String text = null;
            ResultSet rs = null;

            try
            {
                stmt.setLong(1, id);

                rs = stmt.executeQuery();
                if (rs.next())
                {
                    text = rs.getString(1);
                } else LOG.log(Level.SEVERE, MessageFormat.format(MSG_AT_NOT_FOUND, id ));
            } finally
            {
                if (rs != null) rs.close();
            }

            return text;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Deleted objects repository functions
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Adds deleted object record to the database.
     *
     * @param guideTitle guide title.
     * @param objectKey  object match key.
     *
     * @throws PersistenceException if database operation fails.
     */
    public synchronized void addDeletedObjectRecord(String guideTitle, String objectKey)
        throws PersistenceException
    {
        if (StringUtils.isEmpty(guideTitle)) throw new NullPointerException(MSG_GUIDE_UNSPECIFIED);
        if (StringUtils.isEmpty(objectKey)) throw new NullPointerException(MSG_OBJECT_UNSPECIFIED);
        if (isDeletedObjectRecordPresent(guideTitle, objectKey)) return;

        PreparedStatement stmt = null;
        try
        {
            stmt = getPreparedStatement(
                "INSERT INTO DeletedObjects (guideTitle, objectKey) VALUES (?, ?)");
            stmt.setString(1, guideTitle);
            stmt.setString(2, objectKey);
            stmt.executeUpdate();
            commit();
        } catch (SQLException e)
        {
            rollback();
            throw new PersistenceException("Failed to add deleted object record.", e);
        } finally
        {
            close(stmt);
        }
    }

    /**
     * Removes the deleted object record from the database.
     *
     * @param guideTitle guide title.
     * @param objectKey  object match key.
     *
     * @throws PersistenceException if database operation fails.
     */
    public synchronized void removeDeletedObjectRecord(String guideTitle, String objectKey)
        throws PersistenceException
    {
        if (StringUtils.isEmpty(guideTitle)) throw new NullPointerException(MSG_GUIDE_UNSPECIFIED);
        if (StringUtils.isEmpty(objectKey)) throw new NullPointerException(MSG_OBJECT_UNSPECIFIED);

        PreparedStatement stmt = null;
        try
        {
            stmt = getPreparedStatement(
                "DELETE FROM DeletedObjects WHERE guideTitle=? AND objectKey=?");
            stmt.setString(1, guideTitle);
            stmt.setString(2, objectKey);
            stmt.executeUpdate();
            commit();
        } catch (SQLException e)
        {
            rollback();
            throw new PersistenceException("Failed to delete deleted object record.", e);
        } finally
        {
            close(stmt);
        }
    }

    /**
     * Returns <code>TRUE</code> if a object has been deleted.
     *
     * @param guideTitle guide title.
     * @param objectKey  object match key.
     * @return <code>TRUE</code> if a object has been deleted.
     *
     * @throws PersistenceException if database operation fails.
     */
    public synchronized boolean isDeletedObjectRecordPresent(String guideTitle, String objectKey)
        throws PersistenceException
    {
        if (StringUtils.isEmpty(guideTitle)) throw new NullPointerException(MSG_GUIDE_UNSPECIFIED);
        if (StringUtils.isEmpty(objectKey)) throw new NullPointerException(MSG_OBJECT_UNSPECIFIED);

        boolean exists;

        PreparedStatement stmt = null;
        try
        {
            stmt = getPreparedStatement(
                "SELECT * FROM DeletedObjects WHERE guideTitle=? AND objectKey=?");
            stmt.setString(1, guideTitle);
            stmt.setString(2, objectKey);
            ResultSet rs = stmt.executeQuery();
            exists = rs.next();
            rs.close();
            commit();
        } catch (SQLException e)
        {
            rollback();
            throw new PersistenceException("Failed to fetch a deleted object record.", e);
        } finally
        {
            close(stmt);
        }

        return exists;
    }

    /**
     * Removes all records about deleted keys from the database.
     *
     * @throws PersistenceException if database operation fails.
     */
    public synchronized void purgeDeletedObjectRecords()
        throws PersistenceException
    {
        PreparedStatement stmt = null;
        try
        {
            stmt = getPreparedStatement("DELETE FROM DeletedObjects");
            stmt.executeUpdate();
            commit();
        } catch (SQLException e)
        {
            rollback();
            throw new PersistenceException("Failed to purge deleted object records.", e);
        } finally
        {
            close(stmt);
        }
    }
    
    /**
     * Closes the statement.
     *
     * @param stmt statement.
     */
    private void close(Statement stmt)
    {
        try
        {
            if (stmt != null) stmt.close();
        } catch (SQLException e)
        {
            LOG.log(Level.WARNING, "Failed to close the statement.", e);
        }
    }

    // --- Compacting ---------------------------------------------------------

    /**
     * Compacts database.
     *
     * @throws PersistenceException if database operation fails.
     */
    public synchronized void compact() throws PersistenceException
    {
        try
        {
            getConnection().createStatement().execute("CHECKPOINT DEFRAG");
        } catch (SQLException e)
        {
            throw new PersistenceException("Compacting failed.", e);
        }
    }

    /**
     * Creates complete database backup.
     *
     * @param directory destination directory.
     *
     * @throws PersistenceException if database operation fails.
     */
    public synchronized void backup(File directory)
        throws PersistenceException
    {
        shutdown();

        try
        {
            makeBackup(directory);
        } catch (IOException e)
        {
            throw new PersistenceException("Failed to backup the database.", e);
        }
    }

    // --- Debugging ----------------------------------------------------------

    /**
     * Dumps the numbers of articles and plain texts saved.
     */
    public synchronized void printPlainTextStats()
    {
        try
        {
            Integer total = getNumber("SELECT COUNT(*) FROM ARTICLES");
            Integer plain = getNumber("SELECT COUNT(*) FROM ARTICLES WHERE PLAINTEXT IS NOT NULL");
            System.out.println("Total Articles = " + total + ", Plain Texts Saved = " + plain);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Runs a query and returns the single integer result.
     *
     * @param query query.
     *
     * @return <code>NULL</code> if no rows present, the number if there are.
     * 
     * @throws SQLException if db fails.
     */
    private Integer getNumber(String query)
        throws SQLException
    {
        Integer result = null;

        ResultSet rs = getConnection().createStatement().executeQuery(query);
        try
        {
            if (rs.next()) result = rs.getInt(1);
        } finally
        {
            rs.close();
        }

        return result;
    }

    // --------------------------------------------------------------------------------------------
    // Application properties
    // --------------------------------------------------------------------------------------------

    /**
     * Returns the application property value.
     *
     * @param key key.
     *
     * @return value or <code>NULL</code> if not present yet.
     *
     * @throws PersistenceException if database fails.
     */
    public synchronized String getApplicationProperty(String key)
        throws PersistenceException
    {
        String value = null;

        try
        {
            PreparedStatement stmt = getPreparedStatement(
                "SELECT value FROM APP_PROPERTIES WHERE name = ?");

            stmt.setString(1, key);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) value = rs.getString(1);
        } catch (SQLException e)
        {
            throw new PersistenceException("Failed to get application property (" + key + ")", e);
        }

        return value;
    }

    /**
     * Sets the application property value.
     *
     * @param key   key.
     * @param value value or <code>NULL</code> to delete.
     *
     * @throws PersistenceException if database fails.
     */
    public synchronized void setApplicationProperty(String key, String value)
        throws PersistenceException
    {
        try
        {
            PreparedStatement stmt = getPreparedStatement(
                "UPDATE APP_PROPERTIES SET value = ? WHERE name = ?");

            stmt.setString(1, value);
            stmt.setString(2, key);
            int rows = stmt.executeUpdate();

            if (rows == 0)
            {
                stmt = getPreparedStatement(
                    "INSERT INTO APP_PROPERTIES (name, value) VALUES (?, ?)");

                stmt.setString(1, key);
                stmt.setString(2, value);
                rows = stmt.executeUpdate();

                if (rows == 0)
                {
                    throw new PersistenceException(
                        "Failed to insert new application property (" + key + ")");
                }
            }
        } catch (SQLException e)
        {
            throw new PersistenceException(
                "Failed to set application property (" + key + ")", e);
        }
    }

    // --------------------------------------------------------------------------------------------
    // Statistics
    // --------------------------------------------------------------------------------------------

    /**
     * Returns the statistics manager.
     *
     * @return manager.
     */
    public synchronized IStatisticsManager getStatisticsManager()
    {
        if (statisticsManager == null) statisticsManager = new ThreadedStatisticsManager(this);
        return statisticsManager;
    }

    /**
     * Records visit to a guide.
     *
     * @param guide guide.
     */
    public synchronized void guideVisited(IGuide guide)
    {
        if (guide == null) return;

        long id = guide.getID();

        if (id == -1) return;

        try
        {
            PreparedStatement stmt = getPreparedStatement("UPDATE GUIDESTATS SET " +
                "COUNT_TOTAL = COUNT_TOTAL + 1, " +
                "COUNT_RESET = COUNT_RESET + 1 " +
                "WHERE GUIDEID = ?");

            stmt.setLong(1, id);
            int rows = stmt.executeUpdate();

            if (rows != 1) LOG.log(Level.WARNING,
                "Wrong number of GUIDESTATS rows updated: id=" + id + ", rows = " + rows);

            commit();
        } catch (SQLException e)
        {
            rollback();
            LOG.log(Level.SEVERE, "Failed to update guide stats", e);
        }
    }

    /**
     * Records visit to a feed.
     *
     * @param feed feed.
     */
    public synchronized void feedVisited(IFeed feed)
    {
        if (feed == null) return;

        long id = feed.getID();
        if (id == -1) return;

        try
        {
            PreparedStatement stmt = getPreparedStatement("UPDATE FEEDSTATS SET " +
                "COUNT_TOTAL = COUNT_TOTAL + 1, " +
                "COUNT_RESET = COUNT_RESET + 1 " +
                "WHERE FEEDID = ?");

            stmt.setLong(1, id);
            int rows = stmt.executeUpdate();

            commit();

            if (rows != 1) LOG.log(Level.WARNING,
                "Wrong number of FEEDSTATS rows updated: id=" + id + ", rows = " + rows);
        } catch (SQLException e)
        {
            rollback();
            LOG.log(Level.SEVERE, "Failed to update feed stats", e);
        }
    }

    /**
     * Records marking articles as read.
     *
     * @param guide guide where articles were marked as read (NULLable).
     * @param feed  feed where articles were marked as read (NULLable).
     * @param count number of articles.
     */
    public synchronized void articlesRead(IGuide guide, IFeed feed, int count)
    {
        // Get hour and day
        Calendar c = new GregorianCalendar();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int day = c.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;

        // Update hours table
        try
        {
            PreparedStatement stmt = getPreparedStatement("UPDATE READSTATS_HOUR SET " +
                "COUNT_TOTAL = COUNT_TOTAL + ?, " +
                "COUNT_RESET = COUNT_RESET + ? " +
                "WHERE HOUR = ?");

            stmt.setInt(1, count);
            stmt.setInt(2, count);
            stmt.setInt(3, hour);
            int rows = stmt.executeUpdate();
            if (rows != 1) LOG.log(Level.WARNING, "Wrong number of READSTATS_HOUR rows updated: rows=" + rows);
        } catch (SQLException e)
        {
            LOG.log(Level.SEVERE, "Failed to update READSTATS_HOUR table", e);
        }

        // Update days table
        try
        {
            PreparedStatement stmt = getPreparedStatement("UPDATE READSTATS_DAY SET " +
                "COUNT_TOTAL = COUNT_TOTAL + ?, " +
                "COUNT_RESET = COUNT_RESET + ? " +
                "WHERE DAY = ?");

            stmt.setInt(1, count);
            stmt.setInt(2, count);
            stmt.setInt(3, day);
            int rows = stmt.executeUpdate();
            if (rows != 1) LOG.log(Level.WARNING, "Wrong number of READSTATS_DAY rows updated: rows=" + rows);
        } catch (SQLException e)
        {
            LOG.log(Level.SEVERE, "Failed to update READSTATS_DAY table", e);
        }

        if (feed != null && feed.getID() != -1)
        {
            statUpdateOrInsertCount(
                "UPDATE FEEDREADSTATS SET CNT=CNT+? WHERE ID=? AND TS=?",
                "INSERT INTO FEEDREADSTATS (ID, TS, CNT) VALUES (?, ?, ?)",
                feed.getID(), count);
        }

        if (guide != null && guide.getID() != -1)
        {
            statUpdateOrInsertCount(
                "UPDATE GUIDEREADSTATS SET CNT=CNT+? WHERE ID=? AND TS=?",
                "INSERT INTO GUIDEREADSTATS (ID, TS, CNT) VALUES (?, ?, ?)",
                guide.getID(), count);
        }

        // Cleanup (once a day)
        statRemoveOldEntityRecords();

        commit();
    }

    /**
     * Records marking articles as pinned.
     *
     * @param guide guide where articles were marked (NULLable).
     * @param feed  feed where articles were marked (NULLable).
     * @param count number of articles pinned.
     */
    public synchronized void articlesPinned(IGuide guide, IFeed feed, int count)
    {
        if (feed != null && feed.getID() != -1)
        {
            statUpdateOrInsertCount(
                "UPDATE FEEDPINSTATS SET CNT=CNT+? WHERE ID=? AND TS=?",
                "INSERT INTO FEEDPINSTATS (ID, TS, CNT) VALUES (?, ?, ?)",
                feed.getID(), count);
        }

        if (guide != null && guide.getID() != -1)
        {
            statUpdateOrInsertCount(
                "UPDATE GUIDEPINSTATS SET CNT=CNT+? WHERE ID=? AND TS=?",
                "INSERT INTO GUIDEPINSTATS (ID, TS, CNT) VALUES (?, ?, ?)",
                guide.getID(), count);
        }

        // Cleanup (once a day)
        statRemoveOldEntityRecords();

        commit();
    }

    /**
     * Updates or inserts a record in the read/pin stats table.
     *
     * @param update update statement.
     * @param insert insert statement.
     * @param id     ID of the entity.
     * @param count  number of counts to add.
     */
    private void statUpdateOrInsertCount(String update, String insert, long id, int count)
    {
        long time = DateUtils.getTodayTime();

        try
        {
            PreparedStatement stmt = getPreparedStatement(update);
            stmt.setInt(1, count);
            stmt.setLong(2, id);
            stmt.setLong(3, time);
            if (stmt.executeUpdate() == 0)
            {
                // Insert a row
                stmt = getPreparedStatement(insert);
                stmt.setLong(1, id);
                stmt.setLong(2, time);
                stmt.setInt(3, count);
                if (stmt.executeUpdate() == 0)
                {
                    LOG.warning("Failed to insert new stats record");
                }
            }
        } catch (SQLException e)
        {
            LOG.log(Level.SEVERE, "Failed to update stats", e);
        }
    }

    /** Resets the statistics. */
    public synchronized void reset()
    {
        PreparedStatement stmt;
        long now = System.currentTimeMillis();

        try
        {
            // Reset guide stats
            stmt = getPreparedStatement("UPDATE GUIDESTATS SET COUNT_RESET = 0, RESET_TIME = ?");
            stmt.setLong(1, now);
            stmt.executeUpdate();

            // Reset feed stats
            stmt = getPreparedStatement("UPDATE FEEDSTATS SET COUNT_RESET = 0, RESET_TIME = ?");
            stmt.setLong(1, now);
            stmt.executeUpdate();

            // Reset read stats
            stmt = getPreparedStatement("UPDATE READSTATS_HOUR SET COUNT_RESET = 0");
            stmt.executeUpdate();

            // Reset read stats
            stmt = getPreparedStatement("UPDATE READSTATS_DAY SET COUNT_RESET = 0");
            stmt.executeUpdate();

            // Reset global time
            stmt = getPreparedStatement("UPDATE APP_PROPERTIES SET VALUE = ? WHERE NAME = 'statsResetTime'");
            stmt.setLong(1, now);
            stmt.executeUpdate();

            commit();
        } catch (SQLException e)
        {
            rollback();
            LOG.log(Level.SEVERE, "Failed to reset stats", e);
        }
    }

    /**
     * Returns the list of top most visited guides.
     *
     * @param max maximum number to return.
     *
     * @return records.
     *
     * @throws PersistenceException if fails to query records from database.
     */
    public synchronized List<VisitStats> getMostVisitedGuides(int max)
        throws PersistenceException
    {
        long now = System.currentTimeMillis();

        List<VisitStats> stats;
        try
        {
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try
            {
                stmt = getPreparedStatement(
                    "SELECT GS.*, G.TITLE " +
                    "FROM GUIDESTATS GS LEFT JOIN GUIDES G ON G.ID=GS.GUIDEID " +
                    "WHERE COUNT_RESET > 0 OR COUNT_TOTAL > 0 " +
                    "ORDER BY (COUNT_RESET * 10000.0 / (? - RESET_TIME)) DESC, COUNT_RESET DESC, COUNT_TOTAL DESC, TITLE " +
                    "LIMIT ?");

                stmt.setLong(1, now);
                stmt.setInt(2, max);
                rs = stmt.executeQuery();

                stats = new LinkedList<VisitStats>();
                while (rs.next())
                {
                    stats.add(new VisitStats(
                        rs.getInt("GUIDEID"),
                        rs.getString("TITLE"),
                        rs.getLong("COUNT_TOTAL"),
                        rs.getLong("COUNT_RESET"),
                        rs.getLong("INIT_TIME"),
                        rs.getLong("RESET_TIME")
                    ));
                }
            } finally
            {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            }
        } catch (SQLException e)
        {
            LOG.log(Level.SEVERE, "Failed to get most visited guides", e);
            throw new PersistenceException("Error finding most visited guides", e);
        }

        return stats;
    }

    /**
     * Returns the list of top most visited feeds.
     *
     * @param max maximum number to return.
     *
     * @return records.
     *
     * @throws PersistenceException if fails to query records from database.
     */
    public synchronized List<VisitStats> getMostVisitedFeeds(int max)
        throws PersistenceException
    {
        long now = System.currentTimeMillis();

        List<VisitStats> stats;
        try
        {
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try
            {
                stmt = getPreparedStatement(
                    "SELECT FS.*, COALESCE(DF.TITLE, DF.XMLURL, QF.TITLE, SF.TITLE) TITLE " +
                    "FROM FEEDSTATS FS LEFT JOIN DIRECTFEEDS DF ON DF.FEEDID=FS.FEEDID " +
                        "LEFT JOIN QUERYFEEDS QF ON QF.FEEDID=FS.FEEDID " +
                        "LEFT JOIN SEARCHFEEDS SF ON SF.FEEDID=FS.FEEDID " +
                    "WHERE COUNT_RESET > 0 OR COUNT_TOTAL > 0 " +
                    "ORDER BY (COUNT_RESET * 10000.0 / (? - RESET_TIME)) DESC, COUNT_RESET DESC, COUNT_TOTAL DESC, TITLE " +
                    "LIMIT ?");

                stmt.setLong(1, now);
                stmt.setInt(2, max);
                rs = stmt.executeQuery();

                stats = new LinkedList<VisitStats>();
                while (rs.next())
                {
                    stats.add(new VisitStats(
                        rs.getInt("FEEDID"),
                        rs.getString("TITLE"),
                        rs.getLong("COUNT_TOTAL"),
                        rs.getLong("COUNT_RESET"),
                        rs.getLong("INIT_TIME"),
                        rs.getLong("RESET_TIME")
                    ));
                }
            } finally
            {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            }
        } catch (SQLException e)
        {
            LOG.log(Level.SEVERE, "Failed to get most visited feeds", e);
            throw new PersistenceException("Error finding most visited feeds", e);
        }

        return stats;
    }

    /**
     * Returns the list of count stats for hours of a day.
     *
     * @return stats for hours of a day.
     *
     * @throws PersistenceException if fails to query records from database.
     */
    public synchronized CountStats[] getItemsReadPerHour()
        throws PersistenceException
    {
        CountStats[] stats = new CountStats[Constants.HOURS_IN_DAY];

        try
        {
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try
            {
                stmt = getPreparedStatement("SELECT * FROM READSTATS_HOUR");
                rs = stmt.executeQuery();
                while (rs.next())
                {
                    int hour = rs.getInt("HOUR");
                    stats[hour] = new CountStats(
                        rs.getLong("COUNT_TOTAL"),
                        rs.getLong("COUNT_RESET")
                    );
                }
            } finally
            {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            }
        } catch (SQLException e)
        {
            LOG.log(Level.SEVERE, "Failed to get read stats per hour", e);
            throw new PersistenceException("Error finding read stats per hour", e);
        }

        return stats;
    }

    /**
     * Returns the list of count stats for days of a week.
     *
     * @return stats for days of a week.
     *
     * @throws PersistenceException if fails to query records from database.
     */
    public synchronized CountStats[] getItemsReadPerWeekday()
        throws PersistenceException
    {
        CountStats[] stats = new CountStats[Constants.DAYS_IN_WEEK];

        try
        {
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try
            {
                stmt = getPreparedStatement("SELECT * FROM READSTATS_DAY");
                rs = stmt.executeQuery();
                while (rs.next())
                {
                    int hour = rs.getInt("DAY");
                    stats[hour] = new CountStats(
                        rs.getLong("COUNT_TOTAL"),
                        rs.getLong("COUNT_RESET")
                    );
                }
            } finally
            {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            }
        } catch (SQLException e)
        {
            LOG.log(Level.SEVERE, "Failed to get read stats per day", e);
            throw new PersistenceException("Error finding read stats per day", e);
        }

        return stats;
    }

    /**
     * Returns the list of read stats for all guides.
     *
     * @return guides stats.
     *
     * @throws PersistenceException if fails to query records from database.
     */
    public synchronized List<ReadStats> getGuidesReadStats()
        throws PersistenceException
    {
        return getReadTimestampStats("SELECT s.ID, o.TITLE, TS, CNT " +
            "FROM GUIDEREADSTATS s " +
                "LEFT JOIN GUIDES o ON s.ID=o.ID " +
            "WHERE TS > ? " +
            "ORDER BY TS DESC, CNT DESC");
    }

    /**
     * Returns the list of read stats for all feeds.
     *
     * @return feeds stats.
     *
     * @throws PersistenceException if fails to query records from database.
     */
    public synchronized List<ReadStats> getFeedsReadStats()
        throws PersistenceException
    {
        return getReadTimestampStats("SELECT s.ID, COALESCE(DF.TITLE, DF.XMLURL, QF.TITLE, SF.TITLE) TITLE, TS, CNT " +
            "FROM FEEDREADSTATS s " +
                "LEFT JOIN DIRECTFEEDS DF ON DF.FEEDID=s.ID " +
                "LEFT JOIN QUERYFEEDS QF ON QF.FEEDID=s.ID " +
                "LEFT JOIN SEARCHFEEDS SF ON SF.FEEDID=s.ID " +
            "WHERE TS > ? " +
            "ORDER BY TS DESC, CNT DESC");
    }

    /**
     * Returns the list of pin stats for all guides.
     *
     * @return guides stats.
     *
     * @throws PersistenceException if fails to query records from database.
     */
    public synchronized List<ReadStats> getGuidesPinStats()
        throws PersistenceException
    {
        return getReadTimestampStats("SELECT s.ID, o.TITLE, TS, CNT " +
            "FROM GUIDEPINSTATS s " +
                "LEFT JOIN GUIDES o ON s.ID=o.ID " +
            "WHERE TS > ? " +
            "ORDER BY TS DESC, CNT DESC");
    }

    /**
     * Returns the list of pin stats for all feeds.
     *
     * @return feeds stats.
     *
     * @throws PersistenceException if fails to query records from database.
     */
    public synchronized List<ReadStats> getFeedsPinStats()
        throws PersistenceException
    {
        return getReadTimestampStats("SELECT s.ID, COALESCE(DF.TITLE, DF.XMLURL, QF.TITLE, SF.TITLE) TITLE, TS, CNT " +
            "FROM FEEDPINSTATS s " +
                "LEFT JOIN DIRECTFEEDS DF ON DF.FEEDID=s.ID " +
                "LEFT JOIN QUERYFEEDS QF ON QF.FEEDID=s.ID " +
                "LEFT JOIN SEARCHFEEDS SF ON SF.FEEDID=s.ID " +
            "WHERE TS > ? " +
            "ORDER BY TS DESC, CNT DESC");
    }

    /**
     * Returns the read stats for a given query. You need to provide a
     * query with columns:
     * <ul>
     *   <li>ID - object id.</li>
     *   <li>TITLE - object title.</li>
     *   <li>TS - timestamp.</li>
     *   <li>CNT - count.</li>
     * </ul>
     *
     * @param query stats query.
     *
     * @return stats.
     *
     * @throws PersistenceException if fails to query records form database.
     */
    private List<ReadStats> getReadTimestampStats(String query)
        throws PersistenceException
    {
        long time = DateUtils.getTodayTime() - STAT_LAST_N_DAYS * Constants.MILLIS_IN_DAY;

        // Initialize times array
        long[] times = new long[STAT_LAST_N_DAYS];
        times[0] = time + Constants.MILLIS_IN_DAY;
        for (int i = 1; i < STAT_LAST_N_DAYS; i++) times[i] = times[i - 1] + Constants.MILLIS_IN_DAY;

        // Create storages
        Map<Long, Map<Long, Integer>> stats = new HashMap<Long, Map<Long, Integer>>();
        Map<Long, String> titles = new HashMap<Long, String>();

        // Fetch data
        try
        {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try
            {
                stmt = getPreparedStatement(query);
                stmt.setLong(1, time);
                rs = stmt.executeQuery();
                while (rs.next())
                {
                    long id = rs.getLong("ID");
                    Map<Long, Integer> st = stats.get(id);
                    if (st == null)
                    {
                        st = new HashMap<Long, Integer>();
                        stats.put(id, st);
                        titles.put(id, rs.getString("TITLE"));
                    }

                    long ts = rs.getLong("TS");
                    int cnt = rs.getInt("CNT");
                    st.put(ts, cnt);
                }
            } finally
            {
                if (stmt != null) stmt.close();
                if (rs != null) rs.close();
            }
        } catch (SQLException e)
        {
            throw new PersistenceException("Failed to fetch stats.", e);
        }

        // Convert data
        List<ReadStats> rstats = new LinkedList<ReadStats>();
        for (Map.Entry<Long, Map<Long, Integer>> entry : stats.entrySet())
        {
            long id = entry.getKey();
            String title = titles.get(id);
            Map<Long, Integer> st = stats.get(id);

            int[] cnts = new int[times.length];
            int i = 0;
            for (long t : times)
            {
                cnts[i++] = st.containsKey(t) ? st.get(t) : 0;
            }

            rstats.add(new ReadStats(id, title, cnts, times));
        }
        return rstats;
    }

    /**
     * Removes records from the read / pin stats tables older than 30 days.
     */
    void statRemoveOldEntityRecords()
    {
        long today = DateUtils.getTodayTime();
        if (lastRemoveOldEntityRecords != today)
        {
            lastRemoveOldEntityRecords = today;
            long time = today - STAT_LAST_N_DAYS * Constants.MILLIS_IN_DAY;

            statRemoveOldEntityRecordsFromTable("FEEDREADSTATS", time);
            statRemoveOldEntityRecordsFromTable("FEEDPINSTATS", time);
            statRemoveOldEntityRecordsFromTable("GUIDEREADSTATS", time);
            statRemoveOldEntityRecordsFromTable("GUIDEPINSTATS", time);
        }
    }

    /**
     * Removes entity records from a table.
     *
     * @param table table name.
     * @param time  minimum time value for a record to stay.
     */
    private void statRemoveOldEntityRecordsFromTable(String table, long time)
    {
        try
        {
            PreparedStatement stmt = getPreparedStatement("DELETE FROM " + table + " WHERE TS < ?");
            stmt.setLong(1, time);
            stmt.executeUpdate();
        } catch (SQLException e)
        {
            LOG.log(Level.SEVERE, "Failed to delete old records from " + table, e);
        }
    }
}
