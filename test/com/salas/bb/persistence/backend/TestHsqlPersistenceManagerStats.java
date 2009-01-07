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
// $Id: TestHsqlPersistenceManagerStats.java,v 1.6 2007/10/04 08:49:53 spyromus Exp $
//

package com.salas.bb.persistence.backend;

import com.salas.bb.domain.*;
import com.salas.bb.persistence.PersistenceException;
import com.salas.bb.persistence.domain.CountStats;
import com.salas.bb.persistence.domain.VisitStats;
import com.salas.bb.utils.Constants;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * This suite contains tests for <code>HsqlPersistenceManager</code> unit.
 */
public class TestHsqlPersistenceManagerStats extends AbstractHsqlPersistenceTestCase
{
    private static final int SLEEP_TIME = 100;
    private static final int MS_RANGE = 2000;

    protected DirectFeed feed;
    protected StandardGuide guide;
    protected long creationTime;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        initManager("/resources");

        // Create a feed
        feed = new DirectFeed();
        feed.setXmlURL(new URL("http://localhost/"));

        creationTime = System.currentTimeMillis();

        // Create a guide
        guide = new StandardGuide();
        guide.setTitle("1");
        guide.add(feed);

        // Insert a guide
        pm.insertGuide(guide, 0);
    }

    /**
     * Checks if the stats table record is created.
     *
     * @throws PersistenceException exception.
     * @throws SQLException exception.
     */
    public void testAddGuide()
        throws PersistenceException, SQLException
    {
        // Check if the row is there
        VisitStats stats = getGuideVisitStats(guide.getID());
        assertNotNull(stats);
        assertEquals(0, stats.getCountTotal());
        assertEquals(0, stats.getCountReset());
        assertTrue("Failed range check. initTime = " + stats.getInitTime() + ", now=" + creationTime,
            Math.abs(stats.getInitTime() - creationTime) < MS_RANGE);
        assertTrue("Failed range check. resetTime = " + stats.getResetTime() + ", now=" + creationTime,
            Math.abs(stats.getResetTime() - creationTime) < MS_RANGE);
    }

    /**
     * Checks if the stats table record is created.
     *
     * @throws PersistenceException exception.
     * @throws SQLException exception.
     */
    public void testAddFeed()
        throws PersistenceException, SQLException
    {
        // Check if the row is there
        VisitStats stats = getFeedVisitStats(feed.getID());
        assertNotNull(stats);
        assertEquals(0, stats.getCountTotal());
        assertEquals(0, stats.getCountReset());
        assertTrue("Failed range check. initTime = " + stats.getInitTime() + ", now=" + creationTime,
            Math.abs(stats.getInitTime() - creationTime) < MS_RANGE);
        assertTrue("Failed range check. resetTime = " + stats.getResetTime() + ", now=" + creationTime,
            Math.abs(stats.getResetTime() - creationTime) < MS_RANGE);
    }

    /** Checks visiting a guide. */
    public void testVisitGuide()
    {
        // Visit a guide
        pm.guideVisited(guide);

        // Check
        VisitStats stats = getGuideVisitStats(guide.getID());
        assertEquals(guide.getTitle(), stats.getObjectTitle());
        assertEquals(1, stats.getCountTotal());
        assertEquals(1, stats.getCountReset());
    }

    /** Checks visiting a transient guide. */
    public void testVisitTransientGuide()
    {
        // Create a non-saved guide
        StandardGuide guide2 = new StandardGuide();
        guide2.setTitle("2");

        // Visit a guide -- should be no error
        pm.guideVisited(guide2);
    }

    /** Checks visiting a feed. */
    public void testVisitFeed()
    {
        // Visit a feed
        pm.feedVisited(feed);

        // Check
        VisitStats stats = getFeedVisitStats(feed.getID());
        assertEquals(feed.getTitle(), stats.getObjectTitle());
        assertEquals(1, stats.getCountTotal());
        assertEquals(1, stats.getCountReset());
    }

    /** Visiting a transient feed. */
    public void testVisitTransientFeed()
    {
        // Create a non-saved feed
        DirectFeed feed2 = new DirectFeed();

        // Visit a feed -- should be no error
        pm.feedVisited(feed2);
    }

    /**
     * Recording hour and day stats.
     *
     * @throws PersistenceException exception.
     *
     * @throws SQLException if database fails.
     */
    public void testReadStats()
        throws PersistenceException, SQLException
    {
        PreparedStatement stmt;

        // Check the number of hour records in the database
        stmt = pm.getPreparedStatement("SELECT COUNT(*) FROM READSTATS_HOUR");
        ResultSet rs = stmt.executeQuery();
        assertTrue(rs.next());
        assertEquals(Constants.HOURS_IN_DAY, rs.getInt(1));

        // Check the number of day records in the database
        stmt = pm.getPreparedStatement("SELECT COUNT(*) FROM READSTATS_DAY");
        rs = stmt.executeQuery();
        assertTrue(rs.next());
        assertEquals(Constants.DAYS_IN_WEEK, rs.getInt(1));

        // Get current hour and day
        Calendar cal = new GregorianCalendar();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int day = cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;

        // Read it
        StandardArticle article = new StandardArticle("");
        feed.appendArticle(article);
        pm.articlesRead(null, null, 1);

        // Check hours
        CountStats[] stats = pm.getItemsReadPerHour();
        assertEquals(Constants.HOURS_IN_DAY, stats.length);
        for (int i = 0; i < Constants.HOURS_IN_DAY; i++)
        {
            assertEquals(i == hour ? 1 : 0, stats[i].getCountTotal());
            assertEquals(i == hour ? 1 : 0, stats[i].getCountReset());
        }

        // Check days
        stats = pm.getItemsReadPerWeekday();
        assertEquals(Constants.DAYS_IN_WEEK, stats.length);
        for (int i = 0; i < Constants.DAYS_IN_WEEK; i++)
        {
            assertEquals(i == day ? 1 : 0, stats[i].getCountTotal());
            assertEquals(i == day ? 1 : 0, stats[i].getCountReset());
        }
    }

    /**
     * Reporting top 3 most visited guides.
     *
     * @throws PersistenceException exception.
     * @throws InterruptedException if sleeping is interrupted.
     */
    public void testTopGuideVisits()
        throws PersistenceException, InterruptedException
    {
        // Create 3 more guides and visit them
        StandardGuide guide2 = addAndVisitGuide("2", 2);
        StandardGuide guide3 = addAndVisitGuide("3", 4);
        StandardGuide guide4 = addAndVisitGuide("4", 1);

        // Wait a little
        sleep();

        // Get 3 most visited guides
        List<VisitStats> list = pm.getMostVisitedGuides(3);
        assertEquals(3, list.size());
        assertVisitStats(guide3.getID(), 4, 4, list.get(0));
        assertVisitStats(guide2.getID(), 2, 2, list.get(1));
        assertVisitStats(guide4.getID(), 1, 1, list.get(2));
    }

    /**
     * Reporting top 3 most visited feeds.
     *
     * @throws PersistenceException exception.
     * @throws MalformedURLException exception.
     * @throws InterruptedException if interrupted.
     */
    public void testTopFeedVisits()
        throws MalformedURLException, PersistenceException, InterruptedException
    {
        // Create 3 more feeds and visit them
        DirectFeed feed2 = addAndVisitFeed("2", 2);
        DirectFeed feed3 = addAndVisitFeed("2", 4);
        DirectFeed feed4 = addAndVisitFeed("2", 1);

        // Wait a little
        sleep();

        // Get 3 most visited feeds
        List<VisitStats> list = pm.getMostVisitedFeeds(3);
        assertEquals(3, list.size());
        assertVisitStats(feed3.getID(), 4, 4, list.get(0));
        assertVisitStats(feed2.getID(), 2, 2, list.get(1));
        assertVisitStats(feed4.getID(), 1, 1, list.get(2));
    }

    /**
     * Reseting time.
     *
     * @throws SQLException if database fails.
     * @throws InterruptedException if interrupted.
     */
    public void testResetTime()
        throws SQLException, InterruptedException
    {
        long reset1 = getResetTime();

        // Sleep a bit
        sleep();
        pm.reset();

        long reset2 = getResetTime();
        assertFalse(reset1 == reset2);
    }

    /**
     * Reseting visit stats.
     */
    public void testResetVisits()
    {
        // Visit guides and feeds
        visit(guide, 2);
        visit(feed, 2);

        // Reset
        pm.reset();

        // Visit more
        visit(guide, 2);
        visit(feed, 2);

        // Check
        assertVisitStats(guide.getID(), 4, 2, getGuideVisitStats(guide.getID()));
        assertVisitStats(feed.getID(), 4, 2, getFeedVisitStats(feed.getID()));
    }

    /**
     * Reseting reading stats.
     *
     * @throws PersistenceException if fails to query records from database.
     */
    public void testResetReadStats()
        throws PersistenceException
    {
        // Get current hour and day
        Calendar cal = new GregorianCalendar();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int day = cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;

        // Create a sample article
        StandardArticle article = new StandardArticle("");
        feed.appendArticle(article);

        // Mark two articles
        pm.articlesRead(null, null, 1);
        pm.articlesRead(null, null, 1);

        // Reset
        pm.reset();

        // Mark two more articles
        pm.articlesRead(null, null, 1);
        pm.articlesRead(null, null, 1);

        // Check Read per hour
        CountStats[] stats = pm.getItemsReadPerHour();
        for (int i = 0; i < Constants.HOURS_IN_DAY; i++)
        {
            assertEquals(i == hour ? 4 : 0, stats[i].getCountTotal());
            assertEquals(i == hour ? 2 : 0, stats[i].getCountReset());
        }

        // Check Read per weekday
        stats = pm.getItemsReadPerWeekday();
        for (int i = 0; i < Constants.DAYS_IN_WEEK; i++)
        {
            assertEquals(i == day ? 4 : 0, stats[i].getCountTotal());
            assertEquals(i == day ? 2 : 0, stats[i].getCountReset());
        }

        // Check Read history 
    }

    /**
     * Returns guide stats by given guide id.
     *
     * @param id ID.
     *
     * @return stats or <code>NULL</code> if row isn't there.
     */
    private VisitStats getGuideVisitStats(long id)
    {
        VisitStats stats = null;

        try
        {
            Statement stmt = pm.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT GS.*, G.TITLE TITLE " +
                "FROM GUIDESTATS GS, GUIDES G " +
                "WHERE G.ID=GUIDEID AND GUIDEID = " + id);
            if (rs.next())
            {
                stats = new VisitStats((int)id,
                    rs.getString("TITLE"),
                    rs.getLong("COUNT_TOTAL"),
                    rs.getLong("COUNT_RESET"),
                    rs.getLong("INIT_TIME"),
                    rs.getLong("RESET_TIME"));
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
            fail("Failed to fetch the guide stats.");
        }

        return stats;
    }

    /**
     * Returns feed stats by given guide ID.
     *
     * @param id ID.
     *
     * @return stats or <code>NULL</code> if row isn't there.
     */
    private VisitStats getFeedVisitStats(long id)
    {
        VisitStats stats = null;

        try
        {
            Statement stmt = pm.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT FS.*, COALESCE(DF.TITLE, DF.XMLURL, QF.TITLE, SF.TITLE) TITLE " +
                "FROM FEEDSTATS FS LEFT JOIN DIRECTFEEDS DF ON DF.FEEDID=FS.FEEDID " +
                    "LEFT JOIN QUERYFEEDS QF ON QF.FEEDID=FS.FEEDID " +
                    "LEFT JOIN SEARCHFEEDS SF ON SF.FEEDID=FS.FEEDID " +
                "WHERE FEEDID = " + id);
            if (rs.next())
            {
                stats = new VisitStats((int)id,
                    rs.getString("TITLE"),
                    rs.getLong("COUNT_TOTAL"),
                    rs.getLong("COUNT_RESET"),
                    rs.getLong("INIT_TIME"),
                    rs.getLong("RESET_TIME"));
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
            fail("Failed to fetch the feed stats.");
        }

        return stats;
    }

    /**
     * Create, add and visit guide.
     *
     * @param title         guide title.
     * @param visitsCount   visit count.
     *
     * @return guide.
     *
     * @throws PersistenceException exception.
     */
    private StandardGuide addAndVisitGuide(String title, int visitsCount)
        throws PersistenceException
    {
        StandardGuide guide = new StandardGuide();
        guide.setTitle(title);

        pm.insertGuide(guide, 0);
        visit(guide, visitsCount);

        return guide;
    }

    /**
     * Visit guide.
     *
     * @param guide         guide.
     * @param visitCount    visit count.
     */
    private void visit(IGuide guide, int visitCount)
    {
        for (int i = 0; i < visitCount; i++) pm.guideVisited(guide);
    }


    /**
     * Create, add and visit feed.
     *
     * @param title         feed title.
     * @param visitsCount   visit count.
     *
     * @return feed.
     *
     * @throws PersistenceException exception.
     * @throws MalformedURLException exception.
     */
    private DirectFeed addAndVisitFeed(String title, int visitsCount)
        throws PersistenceException, MalformedURLException
    {
        DirectFeed feed = new DirectFeed();
        feed.setXmlURL(new URL("http://localhost/" + title));
        guide.add(feed);

        pm.insertFeed(feed);
        visit(feed, visitsCount);

        return feed;
    }

    /**
     * Visit feed.
     *
     * @param feed          feed.
     * @param visitCount    visit count.
     */
    private void visit(IFeed feed, int visitCount)
    {
        for (int i = 0; i < visitCount; i++) pm.feedVisited(feed);
    }

    /**
     * Checks if visit stats are right.
     *
     * @param id            object ID.
     * @param visitTotal    visit total.
     * @param visitReset    visit since reset.
     * @param visit         target visit stats.
     */
    protected static void assertVisitStats(long id, int visitTotal, int visitReset, VisitStats visit)
    {
        assertEquals(id, visit.getObjectId());
        assertEquals(visitTotal, visit.getCountTotal());
        assertEquals(visitReset, visit.getCountReset());
    }

    /**
     * Gets the reset time.
     *
     * @return reset time.
     *
     * @throws SQLException if database fails.
     */
    protected long getResetTime()
        throws SQLException
    {
        PreparedStatement stmt = pm.getPreparedStatement(
            "SELECT value FROM APP_PROPERTIES WHERE name = 'statsResetTime'");
        ResultSet rs = stmt.executeQuery();

        rs.next();

        return Long.parseLong(rs.getString(1));
    }

    /**
     * Sleeps a bit to make stats stand appart.
     *
     * @throws InterruptedException in case interrupted.
     */
    protected static void sleep()
        throws InterruptedException
    {
        Thread.sleep(SLEEP_TIME);
    }
}
