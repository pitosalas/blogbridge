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
// $Id: DateUtils.java,v 1.18 2008/02/28 15:59:54 spyromus Exp $
//

package com.salas.bb.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Different date-related utils.
 */
public final class DateUtils
{
    private static final AtomicLong TOMORROW = new AtomicLong(-1);
    private static final AtomicLong TODAY = new AtomicLong(-1);

    /**
     * Hidden utility-class constructor.
     */
    private DateUtils()
    {
    }

    /**
     * Difference in days between specified date and present moment. Note that
     * this isn't exact difference. Instead of it it's a virtual difference meaning
     * that 23:59 of Monday is one day away from 00:01 of Tuesday.
     *
     * @param dateBefore date before current moment.
     *
     * @return difference in virtual days (absolute).
     */
    public static long dayDiffFromToday(Date dateBefore)
    {
        long time1 = System.currentTimeMillis();
        long time2 = dateBefore == null ? 0 : dateBefore.getTime();

        return dayDiff(time1, time2);
    }

    /**
     * Returns difference in days between two moments. Note that there's 1-day difference between
     * 23:59 of Monday and 00:00 of Tuesday.
     *
     * @param time1 first moment.
     * @param time2 second moment.
     *
     * @return difference in days (absolute).
     */
    public static long dayDiff(long time1, long time2)
    {
        time1 = trimTime(time1);
        time2 = trimTime(time2);

        return (long)Math.ceil((double)Math.abs(time1 - time2) / Constants.MILLIS_IN_DAY);
    }

    /**
     * Resets hours, minutes, seconds and milliseconds.
     *
     * @param time time to trim.
     *
     * @return trimmed time.
     */
    public static long trimTime(long time)
    {
        final int offset = TimeZone.getDefault().getOffset(time);
        return time - ((time + offset) % Constants.MILLIS_IN_DAY);
    }

    /**
     * Returns time in human-readable form.
     *
     * @param time  time to convert.
     *
     * @return sring.
     */
    public static String millisToString(long time)
    {
        long orig = time;
        int days = (int)(time / Constants.MILLIS_IN_DAY);
        time -= days * Constants.MILLIS_IN_DAY;
        int hours = (int)(time / Constants.MILLIS_IN_HOUR);
        time -= hours * Constants.MILLIS_IN_HOUR;
        int minutes = (int)(time / Constants.MILLIS_IN_MINUTE);
        time -= minutes * Constants.MILLIS_IN_MINUTE;
        int seconds = (int)(time / Constants.MILLIS_IN_SECOND);
        time -= seconds * Constants.MILLIS_IN_SECOND;
        int millis = (int)time;

        StringBuffer buf = new StringBuffer();
        putComponent(buf, days, "day");
        putComponent(buf, hours, "hr");
        putComponent(buf, minutes, "min");
        putComponent(buf, seconds, "sec");
        if (millis > 0 || orig == 0)
        {
            if (buf.length() > 0) buf.append(", ");
            buf.append(millis).append(" ms");
        }

        return buf.toString();
    }

    // Puts date component into buffer.
    private static void putComponent(StringBuffer buf, int count, String main)
    {
        if (count <= 0) return;

        if (buf.length() > 0) buf.append(", ");

        buf.append(count).append(" ").append(main);

        if (count > 1) buf.append("s");
    }

    /**
     * Returns string representation of <code>date</code>.
     *
     * @param date date to convert.
     *
     * @return date and time in string.
     */
    public static String dateToString(Date date)
    {
        return (date == null) ? Constants.EMPTY_STRING : Constants.DATE_TIME_FORMAT.format(date);
    }

    /**
     * Returns the first millisecond of tomorrow.
     *
     * @return tomorrow time.
     */
    public static long getTomorrowTime()
    {
        final long current = System.currentTimeMillis();

        synchronized (TOMORROW)
        {
            if (current > TOMORROW.get())
            {
                GregorianCalendar cal = new GregorianCalendar();
                cal.add(Calendar.DATE, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                TOMORROW.set(cal.getTimeInMillis());
            }
        }

        return TOMORROW.get();
    }

    /**
     * Returns the first millisecond of today.
     *
     * @return today time.
     */
    public static long getTodayTime()
    {
        final long current = System.currentTimeMillis();

        synchronized (TODAY)
        {
            if (current + Constants.MILLIS_IN_DAY > TODAY.get())
            {
                GregorianCalendar cal = new GregorianCalendar();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                TODAY.set(cal.getTimeInMillis());
            }
        }

        return TODAY.get();
    }

    /**
     * Converts local time to UTC (GMT).
     *
     * @param local local time.
     *
     * @return UTC time.
     */
    public static long localToUTC(long local)
    {
        int offset = TimeZone.getDefault().getRawOffset();
        return local - offset;
    }

    /**
     * Returns <code>TRUE</code> if the given date is somewhere today.
     *
     * @param date date.
     *
     * @return <code>TRUE</code> if the given date is somewhere today.
     */
    public static boolean isToday(Date date)
    {
        long ms = date == null ? 0 : date.getTime();
        return ms >= getTodayTime() && ms < getTomorrowTime();
    }

    /**
     * Returns <code>TRUE</code> if the date is older than given number of ms.
     *
     * @param date  date.
     * @param ms    milliseconds.
     *
     * @return <code>TRUE</code> if the date is older.
     */
    public static boolean olderThan(Date date, long ms)
    {
        if (date == null) return false;
        
        long t = date.getTime();
        long n = System.currentTimeMillis();

        return t < (n - ms);
    }
}
