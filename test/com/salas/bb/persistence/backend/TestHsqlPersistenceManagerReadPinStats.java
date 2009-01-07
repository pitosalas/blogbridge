// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2007 by R. Pito Salas
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
// $Id: TestHsqlPersistenceManagerReadPinStats.java,v 1.4 2007/10/04 08:49:53 spyromus Exp $
//

package com.salas.bb.persistence.backend;

import com.salas.bb.persistence.PersistenceException;
import com.salas.bb.persistence.domain.CountStats;
import com.salas.bb.persistence.domain.ReadStats;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.DateUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/** Testing marking articles read and pinned in feeds and guides, and making reports. */
public class TestHsqlPersistenceManagerReadPinStats extends TestHsqlPersistenceManagerStats
{
    /** Tests reading an article in a feed. */
    public void testRead()
    {
        // Mark an article and provide a guide and a feed
        pm.articlesRead(guide, feed, 1);

        // Check
        assertFeedReadStats(1);
        assertGuideReadStats(1);
        assertReadPerWeekday(1);

        // Mark an article again
        pm.articlesRead(guide, feed, 2);

        // Check
        assertFeedReadStats(3);
        assertGuideReadStats(3);
        assertReadPerWeekday(3);
    }

    /** Tests pinning an article in a feed. */
    public void testPin()
    {
        // Pin an article
        pm.articlesPinned(guide, feed, 1);

        // Check
        assertFeedPinStats(1);
        assertGuidePinStats(1);

        // Pin again
        pm.articlesPinned(guide, feed, 2);

        // Check
        assertFeedPinStats(3);
        assertGuidePinStats(3);
    }

    /** Tests reading an article when no feed selected. */
    public void testReadNoFeedSelected()
    {
        // Mark an article and provide a guide
        pm.articlesRead(guide, null, 1);

        // Check
        assertFeedReadStats();
        assertGuideReadStats(1);
        assertReadPerWeekday(1);

        // Mark an article again
        pm.articlesRead(guide, null, 1);

        // Check
        assertFeedReadStats();
        assertGuideReadStats(2);
        assertReadPerWeekday(2);
    }

    /** Tests pinning an article when no feed selected. */
    public void testPinNoFeedSelected()
    {
        // Pin an article
        pm.articlesPinned(guide, null, 1);

        // Check
        assertFeedPinStats();
        assertGuidePinStats(1);

        // Pin again
        pm.articlesPinned(guide, null, 1);

        // Check
        assertFeedPinStats();
        assertGuidePinStats(2);
    }

    /** Tests reading an article when no guide selected. */
    public void testReadNoGuideSelected()
    {
        // Mark an article and provide a guide and a feed
        pm.articlesRead(null, null, 1);

        // Check
        assertFeedReadStats();
        assertGuideReadStats();
        assertReadPerWeekday(1);

        // Mark an article again
        pm.articlesRead(null, null, 1);

        // Check
        assertFeedReadStats();
        assertGuideReadStats();
        assertReadPerWeekday(2);
    }

    /** Tests pinning an article when no guide selected. */
    public void testPinNoGuideSelected()
    {
        // Pin an article
        pm.articlesPinned(null, null, 1);

        // Check
        assertFeedPinStats();
        assertGuidePinStats();

        // Pin again
        pm.articlesPinned(null, null, 1);

        // Check
        assertFeedPinStats();
        assertGuidePinStats();
    }

    /** Tests acting upon resetting stats. Nothing should happen. */
    public void testReset()
    {
        // Mark an article and provide a guide and a feed
        pm.articlesRead(guide, feed, 1);
        pm.articlesPinned(guide, feed, 1);

        // Check
        assertFeedReadStats(1);
        assertGuideReadStats(1);
        assertFeedPinStats(1);
        assertGuidePinStats(1);

        // Reset stats
        pm.reset();

        // Check -- nothing should be reset
        assertFeedReadStats(1);
        assertGuideReadStats(1);
        assertFeedPinStats(1);
        assertGuidePinStats(1);
    }

    /** Tests automatically removing old records from the database. */
    public void testRemovingOldRecords()
    {
        // Add a year old records
        long today = DateUtils.getTodayTime();
        long yearAgo = today - Constants.MILLIS_IN_YEAR;

        insertReadStatRecord("FEEDREADSTATS", feed.getID(), yearAgo);
        insertReadStatRecord("GUIDEREADSTATS", guide.getID(), yearAgo);
        insertReadStatRecord("FEEDPINSTATS", feed.getID(), yearAgo);
        insertReadStatRecord("GUIDEPINSTATS", guide.getID(), yearAgo);

        // Add today's pins and reads
        pm.articlesRead(guide, feed, 1);
        pm.articlesPinned(guide, feed, 1);

        // Check they are present
        assertRecordsCount(1, "FEEDREADSTATS");
        assertRecordsCount(1, "GUIDEREADSTATS");
        assertRecordsCount(1, "FEEDPINSTATS");
        assertRecordsCount(1, "GUIDEPINSTATS");

        assertGuideReadStats(1);
        assertFeedReadStats(1);
        assertFeedPinStats(1);
        assertGuidePinStats(1);
    }

    /**
     * Inserts a stat record in a table.
     *
     * @param table table.
     * @param id    object id.
     * @param time  time.
     */
    private void insertReadStatRecord(String table, long id, long time)
    {
        try
        {
            PreparedStatement stmt = pm.getPreparedStatement(
                "INSERT INTO " + table + " (ID, TS, CNT) VALUES (?, ?, 1)");
            stmt.setLong(1, id);
            stmt.setLong(2, time);
            assertEquals("Record wasn't added to " + table, 1, stmt.executeUpdate());
        } catch (SQLException e)
        {
            e.printStackTrace();
            fail("Failed to insert the record into " + table);
        }
    }

    /**
     * Asserts that read stats for a feed match the expectations.
     * The stats module returns values in the earlier date first order,
     * where's the parameter is in the latest date first order (reverse)
     * for the greater convenience.
     *
     * @param counts counts.
     */
    private void assertFeedReadStats(Integer ... counts)
    {
        try
        {
            List<ReadStats> stats = pm.getFeedsReadStats();
            assertEntityReadStats(feed.getID(), stats, counts);
        } catch (PersistenceException e)
        {
            e.printStackTrace();
            fail("Failed to fetch");
        }
    }

    /**
     * Asserts that pin stats for a feed match the expectations.
     * The stats module returns values in the earlier date first order,
     * where's the parameter is in the latest date first order (reverse)
     * for the greater convenience.
     *
     * @param counts counts.
     */
    private void assertFeedPinStats(Integer ... counts)
    {
        try
        {
            List<ReadStats> stats = pm.getFeedsPinStats();
            assertEntityReadStats(feed.getID(), stats, counts);
        } catch (PersistenceException e)
        {
            e.printStackTrace();
            fail("Failed to fetch");
        }
    }

    /**
     * Asserts that read stats for a guide match the expectations.
     * The stats module returns values in the earlier date first order,
     * where's the parameter is in the latest date first order (reverse)
     * for the greater convenience.
     *
     * @param counts counts.
     */
    private void assertGuideReadStats(Integer ... counts)
    {
        try
        {
            List<ReadStats> stats = pm.getGuidesReadStats();
            assertEntityReadStats(feed.getID(), stats, counts);
        } catch (PersistenceException e)
        {
            e.printStackTrace();
            fail("Failed to fetch");
        }
    }

    /**
     * Asserts that pin stats for a guide match the expectations.
     * The stats module returns values in the earlier date first order,
     * where's the parameter is in the latest date first order (reverse)
     * for the greater convenience.
     *
     * @param counts counts.
     */
    private void assertGuidePinStats(Integer ... counts)
    {
        try
        {
            List<ReadStats> stats = pm.getGuidesPinStats();
            assertEntityReadStats(feed.getID(), stats, counts);
        } catch (PersistenceException e)
        {
            e.printStackTrace();
            fail("Failed to fetch");
        }
    }

    /**
     * Asserts that the stats contain the record for a given entity and they
     * match the given counts.
     *
     * @param objectId  entity id.
     * @param stats     stats to check.
     * @param counts    counts to verify against.
     */
    private static void assertEntityReadStats(long objectId, List<ReadStats> stats, Integer ... counts)
    {
        for (ReadStats stat : stats)
        {
            if (stat.getObjectId() == objectId)
            {
                int[] oc = stat.getCounts();
                long[] ti = stat.getTimes();

                // Check that the length is fine
                assertEquals("Wrong number of items.", HsqlPersistenceManager.STAT_LAST_N_DAYS, oc.length);
                assertEquals("Wrong number of items.", HsqlPersistenceManager.STAT_LAST_N_DAYS, ti.length);

                // Check that all important values match
                // (back to front)
                int i = 0;
                for (Integer count : counts)
                {
                    int c = oc[oc.length - i - 1];
                    long t = ti[ti.length - i - 1];

                    assertEquals("Wrong count", count.intValue(), c);
                    assertEquals("Wrong time", DateUtils.getTodayTime() - i * Constants.MILLIS_IN_DAY, t);

                    i++;
                }

                return;
            }
        }

        if (counts.length > 0) fail("Stats for the given feed were not found");
    }

    /**
     * Makes sure todays record has the given count.
     *
     * @param count count.
     */
    private void assertReadPerWeekday(int count)
    {
        // Get current day
        Calendar cal = new GregorianCalendar();
        int day = cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;

        try
        {
            CountStats[] stats = pm.getItemsReadPerWeekday();
            assertEquals(Constants.DAYS_IN_WEEK, stats.length);
            for (int i = 0; i < Constants.DAYS_IN_WEEK; i++)
            {
                assertEquals(i == day ? count : 0, stats[i].getCountTotal());
                assertEquals(i == day ? count : 0, stats[i].getCountReset());
            }
        } catch (PersistenceException e)
        {
            e.printStackTrace();
            fail("Failed to check");
        }
    }

    /**
     * Asserts that a table has a given number of records.
     *
     * @param cnt   count.
     * @param table target table.
     */
    private void assertRecordsCount(int cnt, String table)
    {
        try
        {
            PreparedStatement stmt = pm.getPreparedStatement("SELECT COUNT(*) FROM " + table);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
            {
                assertEquals(cnt, rs.getInt(1));
            } else
            {
                fail("No counts for the " + table);
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
            fail("Failed to assert record counts");
        }
    }
}
