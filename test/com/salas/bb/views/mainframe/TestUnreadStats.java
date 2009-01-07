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
// $Id: TestUnreadStats.java,v 1.3 2006/01/08 05:28:35 kyank Exp $
//

package com.salas.bb.views.mainframe;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;
import com.salas.bb.views.mainframe.UnreadStats.DayCount;

/**
 * This suite contains tests for <code>UnreadStats</code> unit.
 */
public class TestUnreadStats extends TestCase
{
    private UnreadStats stats;
    private DayCount dayCount;
    private Calendar cal;

    /**
     * Initialize the tests.
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp()
    {
        stats = new UnreadStats();
        dayCount = new DayCount();
        cal = Calendar.getInstance();
    }

    /** 
     * Test an empty DayCount.
     */
    public void testEmptyDayCount()
    {
        checkDayCount("empty daycount", dayCount, 0, 0);
    }

    /** 
     * Test an empty UnreadStats.
     */
    public void testEmptyStats()
    {
        for (int i = 0; i < UnreadStats.MAX_DAYS; ++i)
        {
            dayCount = stats.getDayCount(i);
            checkDayCount("empty daily DayCount", dayCount, 0, 0);
        }
        checkDayCount("empty older DayCount", stats.getOlderCount(), 0, 0);

        checkTotalDayCount("empty UnreadStats", 0, 0);
    }

    /** 
     * Test a DayCount with one read, one unread.
     */
    public void testSimpleDayCount()
    {
        dayCount.increment(true);
        dayCount.increment(false);

        checkDayCount("simple daycount", dayCount, 1, 1);
    }

    /** 
     * Test multiple DayCounts.
     */
    public void testMultiUnreadCount()
    {
        for (int i = 0; i < UnreadStats.MAX_DAYS + 1; ++i)
        {

            Date prevDate = cal.getTime();

            // set up so that today has 0/0 read/unread,
            // yesterday has 2/1, day before 4/2, etc.

            for (int j = 0; j < i; ++j)
            {
                stats.increment(prevDate, true);
                stats.increment(prevDate, true);
                stats.increment(prevDate, false);
            }

            cal.add(Calendar.DATE, -1);

        }

        int expectedRead = 0, expectedUnread = 0, totalRead = 0, totalUnread = 0;

        for (int i = 0; i < UnreadStats.MAX_DAYS; ++i)
        {
            DayCount dc = stats.getDayCount(i);
            checkDayCount("multi unread count", dc, expectedRead, expectedUnread);

            expectedRead += 2;
            expectedUnread += 1;

            totalRead += expectedRead;
            totalUnread += expectedUnread;
        }

        DayCount dc = stats.getOlderCount();
        checkDayCount("multi unread count", dc, expectedRead, expectedUnread);

        checkTotalDayCount("multi unread count", totalRead, totalUnread);
    }

    /** 
     * Test an old date in the past.
     */
    public void testOldDates()
    {
        // add something at 12am yesterday
        cal.add(Calendar.DATE, -1);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        stats.increment(cal.getTime(), true);

        // add some dates in far past
        cal.set(1950, 4, 15);
        stats.increment(cal.getTime(), true);

        cal.set(1143, 4, 15);
        stats.increment(cal.getTime(), false);

        cal.set(0, 4, 15);
        stats.increment(cal.getTime(), false);

        checkDayCount("old dates", stats.getDayCount(1), 1, 0);
        checkDayCount("old dates", stats.getOlderCount(), 1, 2);

        checkTotalDayCount("old dates", 2, 2);

    }

    /** 
     * Test present future dates.
     */
    public void testPresentAndFutureDates()
    {
        stats.increment(cal.getTime(), true);

        for (int i = 0; i < 30; ++i)
        {
            cal.add(Calendar.HOUR, 1);
            stats.increment(cal.getTime(), false);
        }

        cal.add(Calendar.WEEK_OF_YEAR, 3);
        stats.increment(cal.getTime(), true);

        cal.set(2345, 1, 1);
        stats.increment(cal.getTime(), true);

        checkDayCount("present and future dates", stats.getDayCount(0), 3, 30);
        checkTotalDayCount("present and future dates", 3, 30);

    }

    /** 
     * Utility function to test a DayCount.
     * @param desc string for error
     * @param dc DayCount to test
     * @param read expected read count
     * @param unread expected unread count;
     */
    void checkDayCount(String desc, DayCount dc, int read, int unread)
    {
        int total = read + unread;
        assertEquals(desc + ": read count wrong", read, dc.getRead());
        assertEquals(desc + ": unread count wrong", unread, dc.getUnread());
        assertEquals(desc + ": total count wrong", total, dc.getTotal());
    }

    /** 
     * Utility function to test that total values are correct.
     * @param desc string for error
     * @param read expected read count
     * @param unread expected unread count
     */
    void checkTotalDayCount(String desc, int read, int unread)
    {
        int totalRead = 0;
        int totalUnread = 0;
        DayCount dc;

        for (int i = 0; i < UnreadStats.MAX_DAYS; ++i)
        {
            dc = stats.getDayCount(i);
            totalRead += dc.getRead();
            totalUnread += dc.getUnread();
        }
        dc = stats.getOlderCount();
        totalRead += dc.getRead();
        totalUnread += dc.getUnread();

        assertEquals(desc + ": total read error", totalRead, read);
        assertEquals(desc + ": total unread error", totalUnread, unread);

        dc = stats.getTotalCount();

        assertEquals(desc + ": total read error", dc.getRead(), read);
        assertEquals(desc + ": total unread error", dc.getUnread(), unread);

    }
}
