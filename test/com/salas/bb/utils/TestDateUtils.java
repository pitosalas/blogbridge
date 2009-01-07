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
// $Id: TestDateUtils.java,v 1.5 2007/07/18 15:24:45 spyromus Exp $
//

package com.salas.bb.utils;

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @see DateUtils
 */
public class TestDateUtils extends TestCase
{
    /**
     * @see DateUtils#millisToString
     */
    public void testMillisToString()
    {
        assertEquals("0 ms", DateUtils.millisToString(createTimeSpan(0, 0, 0, 0, 0)));
        assertEquals("1 ms", DateUtils.millisToString(createTimeSpan(0, 0, 0, 0, 1)));
        assertEquals("1 sec, 1 ms", DateUtils.millisToString(createTimeSpan(0, 0, 0, 1, 1)));
        assertEquals("1 min, 1 sec, 1 ms", DateUtils.millisToString(createTimeSpan(0, 0, 1, 1, 1)));
        assertEquals("1 hr, 1 min, 1 sec, 1 ms",
            DateUtils.millisToString(createTimeSpan(0, 1, 1, 1, 1)));
        assertEquals("1 day, 1 hr, 1 min, 1 sec, 1 ms",
            DateUtils.millisToString(createTimeSpan(1, 1, 1, 1, 1)));

        assertEquals("1 day", DateUtils.millisToString(createTimeSpan(1, 0, 0, 0, 0)));
        assertEquals("2 days", DateUtils.millisToString(createTimeSpan(2, 0, 0, 0, 0)));

        assertEquals("1 hr", DateUtils.millisToString(createTimeSpan(0, 1, 0, 0, 0)));
        assertEquals("2 hrs", DateUtils.millisToString(createTimeSpan(0, 2, 0, 0, 0)));

        assertEquals("1 min", DateUtils.millisToString(createTimeSpan(0, 0, 1, 0, 0)));
        assertEquals("2 mins", DateUtils.millisToString(createTimeSpan(0, 0, 2, 0, 0)));

        assertEquals("1 sec", DateUtils.millisToString(createTimeSpan(0, 0, 0, 1, 0)));
        assertEquals("2 secs", DateUtils.millisToString(createTimeSpan(0, 0, 0, 2, 0)));

        assertEquals("1 ms", DateUtils.millisToString(createTimeSpan(0, 0, 0, 0, 1)));
        assertEquals("2 ms", DateUtils.millisToString(createTimeSpan(0, 0, 0, 0, 2)));

        assertEquals("1 hr, 2 secs", DateUtils.millisToString(createTimeSpan(0, 1, 0, 2, 0)));
        assertEquals("1 day, 2 mins, 3 ms", DateUtils.millisToString(createTimeSpan(1, 0, 2, 0, 3)));
    }

    /**
     * @see DateUtils#trimTime
     */
    public void testTrimTime()
    {
        long time = DateUtils.trimTime(createTime(2005, 0, 3, 2, 3, 4));
        assertEquals(new Date(time).toString(), createTime(2005, 0, 3, 0, 0, 0), time);
    }

    /**
     * @see DateUtils#dayDiff
     */
    public void testDayDiff()
    {
        long t_010105_2359 = createTime(2005, 0, 1, 23, 59, 0);
        long t_010205_0000 = createTime(2005, 0, 2, 0, 0, 0);
        long t_010205_2359 = createTime(2005, 0, 2, 23, 59, 0);
        long t_010305_0000 = createTime(2005, 0, 3, 0, 0, 0);

        assertEquals(1, DateUtils.dayDiff(t_010105_2359, t_010205_0000));
        assertEquals(1, DateUtils.dayDiff(t_010205_0000, t_010105_2359));

        assertEquals(1, DateUtils.dayDiff(t_010105_2359, t_010205_2359));
        assertEquals(1, DateUtils.dayDiff(t_010205_2359, t_010105_2359));

        assertEquals(0, DateUtils.dayDiff(t_010205_0000, t_010205_2359));
        assertEquals(0, DateUtils.dayDiff(t_010205_2359, t_010205_0000));

        assertEquals(2, DateUtils.dayDiff(t_010105_2359, t_010305_0000));
        assertEquals(2, DateUtils.dayDiff(t_010305_0000, t_010105_2359));

        assertEquals(1, DateUtils.dayDiff(t_010205_0000, t_010305_0000));
        assertEquals(1, DateUtils.dayDiff(t_010305_0000, t_010205_0000));
    }

    /**
     * @see DateUtils#dayDiffFromToday
     */
    public void testDayDiffFromToday()
    {
        // Required contract
        assertEquals(DateUtils.dayDiff(0, System.currentTimeMillis()),
            DateUtils.dayDiffFromToday(null));

        Calendar cal = new GregorianCalendar();

        cal.roll(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = cal.getTime();
        assertEquals(1, DateUtils.dayDiffFromToday(tomorrow));

        cal = new GregorianCalendar();
        cal.roll(Calendar.DAY_OF_YEAR, -2);
        Date beforeYesterday = cal.getTime();
        assertEquals(2, DateUtils.dayDiffFromToday(beforeYesterday));
    }

    /**
     * Tests converting local time to UTC / GMT.
     */
    public void testLocalToUTC()
    {
        long local = System.currentTimeMillis();
        long utc = DateUtils.localToUTC(local);
        assertFalse(local == utc);
    }

    // Returns time/date built from components.
    private long createTime(int year, int month, int day, int hours, int min, int sec)
    {
        return new GregorianCalendar(year, month, day, hours, min, sec).getTimeInMillis();
    }

    // Returns time span built from components.
    private long createTimeSpan(int days, int hours, int minutes, int seconds, int millis)
    {
        long time = days * Constants.MILLIS_IN_DAY;
        time += hours * Constants.MILLIS_IN_HOUR;
        time += minutes * Constants.MILLIS_IN_MINUTE;
        time += seconds * Constants.MILLIS_IN_SECOND;
        time += millis;

        return time;
    }
}
