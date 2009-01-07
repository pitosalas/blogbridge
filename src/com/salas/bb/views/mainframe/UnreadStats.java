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
// $Id: UnreadStats.java,v 1.5 2006/05/30 08:25:28 spyromus Exp $
//
package com.salas.bb.views.mainframe;

import com.salas.bb.utils.i18n.Strings;

import java.util.Calendar;
import java.util.Date;

/**
 * Stores the read/unread article counts for a series of days.
 */
public class UnreadStats
{
    /** Number days to track. */
    public static final int MAX_DAYS = 7; 

    /** Milliseconds in a day. */
    private static final long MS_PER_DAY = 24 * 60 * 60 * 1000;

    /** Array of counts for days we track individually. */
    private DayCount[] dayCounts = new DayCount[MAX_DAYS];

    /** Count of everything else older. */
    private DayCount olderCount = new DayCount();
    
    /** End of today, in ms. */
    private final long endOfTodayMS;

    /** 
     * Initialize.
     */
    public UnreadStats()
    {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int date = cal.get(Calendar.DATE);
        cal.clear();
        cal.set(year, month, date);
        cal.add(Calendar.DATE, 1);

        endOfTodayMS = cal.getTimeInMillis() - 1;

        for (int i = 0; i < MAX_DAYS; ++i)
            dayCounts[i] = new DayCount();
    }

    /** 
     * Increments our count for a particular day.
     *
     * @param date      date of day to increment.
     * @param isRead    <code>TRUE</code> to increment "read" count; <code>FALSE</code> for "unread".
     */
    void increment(Date date, boolean isRead)
    {
        long age = Math.max((endOfTodayMS - date.getTime()) / MS_PER_DAY, 0);
        if (age < MAX_DAYS)
        {
            dayCounts[(int)age].increment(isRead);
        } else 
        {
            olderCount.increment(isRead);
        }
    }

    /** 
     * Returns count for particular day.
     *
     * @param day index of day to retrieve; 0 = today, 1 = yesterday, etc.
     *
     * @return count values for that day.
     *
     * @throws IllegalArgumentException if day is out of range [0;MAX_DAYS).
     */
    DayCount getDayCount(int day)
    {
        if (day < 0 || day >= MAX_DAYS) throw new IllegalArgumentException(Strings.error("day.value.out.of.range"));

        return dayCounts[day];
    }

    /** 
     * Returns counts for all older days we don't track individually.
     *
     * @return accumulated older count.
     */
    DayCount getOlderCount()
    {
        return olderCount;
    }

    /** 
     * Returns total read + unread for all days.
     *
     * @return total.
     */
    DayCount getTotalCount()
    {
        DayCount total = new DayCount();
        for (int i = 0; i < MAX_DAYS; ++i) total.add(dayCounts[i]);
        total.add(olderCount);

        return total;
    }
    /**
     * DayCount describes the read and unread articles for a single day.
     */
    static final class DayCount
    {
        private int readCount;
        private int unreadCount;

        /** 
         * Increments the count.
         *
         * @param isRead  <code>TRUE</code> to add the read count; <code>FALSE</code> for unread count.
         */
        public void increment(boolean isRead)
        {
            if (isRead)
            {
                readCount++;
            } else
            {
                unreadCount++;
            }
        }

        /** 
         * Returns total articles for day.
         *
         * @return total.
         */
        public int getTotal()
        {
            return readCount + unreadCount;
        }

        /** 
         * Add another DayCount's totals to this day.
         *
         * @param dc  count object to add.
         */
        public void add(final DayCount dc)
        {
            readCount += dc.readCount;
            unreadCount += dc.unreadCount;
        }

        /** 
         * Returns number of read articles.
         *
         * @return read articles count.
         */
        public int getRead()
        {
            return readCount;
        }

        /** 
         * Returns number of unread articles.
         *
         * @return unread articles count.
         */
        public int getUnread()
        {
            return unreadCount;
        }
    }    
}
