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
// $Id: TimeRange.java,v 1.10 2006/07/11 09:28:47 spyromus Exp $
//

package com.salas.bb.utils;

import com.salas.bb.utils.i18n.Strings;

/**
 * Holds simple time range.
 */
public final class TimeRange
{
    private static final long DAYS_NEGATIVE = Long.MIN_VALUE;
    private static final long DAYS_0 = 0;
    private static final long DAYS_1 = 1;
    private static final long DAYS_2 = 2;
    private static final long DAYS_7 = 7;
    private static final long DAYS_14 = 14;

    public static final TimeRange TR_FUTURE         = new TimeRange(DAYS_NEGATIVE, DAYS_0);
    public static final TimeRange TR_TODAY          = new TimeRange(DAYS_0, DAYS_1);
    public static final TimeRange TR_YESTERDAY      = new TimeRange(DAYS_1, DAYS_2);
    public static final TimeRange TR_LAST_WEEK      = new TimeRange(DAYS_2, DAYS_7);
    public static final TimeRange TR_TWO_WEEKS_AGO  = new TimeRange(DAYS_7, DAYS_14);
    public static final TimeRange TR_OLDER          = new TimeRange(DAYS_14, Long.MAX_VALUE);

    public static final String TITLE_FUTURE         = Strings.message("timerange.articles.from.the.future");
    public static final String TITLE_TODAY          = Strings.message("timerange.today");
    public static final String TITLE_YESTERDAY      = Strings.message("timerange.yesterday");
    public static final String TITLE_LAST_WEEK      = Strings.message("timerange.this.week");
    public static final String TITLE_TWO_WEEKS_AGO  = Strings.message("timerange.last.week");
    public static final String TITLE_OLDER          = Strings.message("timerange.older");

    /** Titles for time ranges in TIME_RANGES property. */
    public static final String[] TITLES = new String[]
    {
        TITLE_FUTURE, TITLE_TODAY, TITLE_YESTERDAY, TITLE_LAST_WEEK,  TITLE_TWO_WEEKS_AGO,
        TITLE_OLDER
    };

    /** Time ranges. */
    public static final TimeRange[] TIME_RANGES = new TimeRange[]
    {
        TR_FUTURE, TR_TODAY, TR_YESTERDAY, TR_LAST_WEEK, TR_TWO_WEEKS_AGO, TR_OLDER
    };

    private final long  from;
    private final long  to;

    /**
     * Creates simple time range.
     *
     * @param aFrom time from.
     * @param aTo   time to.
     */
    public TimeRange(long aFrom, long aTo)
    {
        from = aFrom == Long.MIN_VALUE ? aFrom : aFrom * Constants.MILLIS_IN_DAY;
        to = aTo == Long.MAX_VALUE ? aTo : aTo * Constants.MILLIS_IN_DAY;
    }

    /**
     * Gets 'from'-boundary of range.
     *
     * @return value in days.
     */
    public long getFrom()
    {
        return from;
    }

    /**
     * Gets 'from'-boundary of range.
     *
     * @return value in days.
     */
    public long getTo()
    {
        return to;
    }

    /**
     * Returns <code>TRUE</code> if given time lays in current range
     * (applied to today in past direction).
     *
     * @param time time.
     *
     * @return <code>TRUE</code> if given time lays in current range.
     */
    public boolean isInRange(long time)
    {
        long diff = calcDeltaFromTomorrow(time);

        return diff >= from && diff < to;
    }

    /**
     * Returns <code>TRUE</code> if given time comes after the range end.
     *
     * @param time time.
     *
     * @return <code>TRUE</code> if given time comes after the range end.
     */
    public boolean isAfter(long time)
    {
        long diff = calcDeltaFromTomorrow(time);

        return diff < from;
    }

    /**
     * Returns <code>TRUE</code> if given time comes before the range start.
     *
     * @param time time.
     *
     * @return <code>TRUE</code> if given time comes before the range start.
     */
    public boolean isBefore(long time)
    {
        long diff = calcDeltaFromTomorrow(time);

        return diff >= to;
    }

    /**
     * Finds the range name appropriate to the given time.
     *
     * @param time time.
     *
     * @return range name.
     */
    public static String findRangeName(long time)
    {
        String range = null;

        for (int i = 0; range == null && i < TIME_RANGES.length; i++)
        {
            TimeRange timeRange = TIME_RANGES[i];
            if (timeRange.isInRange(time)) range = TITLES[i];
        }

        return range;
    }

    /**
     * Finds the range index appropriate to the given time.
     *
     * @param time time.
     *
     * @return range index.
     */
    public static int findRangeIndex(long time)
    {
        int index = -1;

        for (int i = 0; index == -1 && i < TIME_RANGES.length; i++)
        {
            TimeRange timeRange = TIME_RANGES[i];
            if (timeRange.isInRange(time)) index = i;
        }

        return index;
    }

    private static long calcDeltaFromTomorrow(long time)
    {
        return DateUtils.getTomorrowTime() - time;
    }
}
