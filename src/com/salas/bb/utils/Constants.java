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
// $Id: Constants.java,v 1.30 2007/09/12 15:25:25 spyromus Exp $
//

package com.salas.bb.utils;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.forms.util.LayoutStyle;
import com.jgoodies.uif.util.Resizer;
import com.jgoodies.uif.util.SystemUtils;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.utils.TextRange;

import javax.swing.border.Border;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Global constants.
 */
public final class Constants
{
    /** Milliseconds in hour. */
    public static final long MILLIS_IN_HOUR = 3600000;

    /** Milliseconds in day. */
    public static final long MILLIS_IN_DAY = 86400000;

    /** Milliseconds in year. The year has 365 days. */
    public static final long MILLIS_IN_YEAR = 365 * MILLIS_IN_DAY;

    /** Milliseconds in month. The month has 30 days. */
    public static final long MILLIS_IN_MONTH = 30 * MILLIS_IN_DAY;

    /** Milliseconds in week. */
    public static final long MILLIS_IN_WEEK = 7 * MILLIS_IN_DAY;

    /** Number of milliseconds in second. */
    public static final long MILLIS_IN_SECOND = 1000;

    /** Number of seconds in minute. */
    public static final int SECONDS_IN_MINUTE = 60;

    /** Number of seconds in day. */
    public static final long SECONDS_IN_DAY = SECONDS_IN_MINUTE * 60 * 24;

    /** Number of milliseconds in minute. */
    public static final long MILLIS_IN_MINUTE = MILLIS_IN_SECOND * SECONDS_IN_MINUTE;

    /** Number of bits in byte. */
    public static final int BITS_IN_BYTE = 8;

    /** Number of bytes in kilobyte. */
    public static final int BYTES_IN_KILOBYTE = 1024;

    /** Zero-insets. */
    public static final Insets INSETS_NONE = new Insets(0, 0, 0, 0);

    /** Empty string. */
    public static final String EMPTY_STRING = "";

    /** Empty list of text ranges. */
    public static final TextRange[] EMPTY_TEXT_RANGE_LIST = new TextRange[0];

    /** Resizer with aspect ratio 3/2. */
    public static final Resizer RESIZER_3TO2 = new Resizer(3.0f / 2);

    /** Empty feeds list. */
    public static final IFeed[] EMPTY_FEEDS_LIST = new IFeed[0];

    /** Date and time formatter. */
    public static final SimpleDateFormat DATE_TIME_FORMAT =
        new SimpleDateFormat("dd-MM-yyyy HH:mm");

    /** Maximum size of the article text in bytes. Safety valve. */
    public static final int ARTICLE_SIZE_LIMIT = 32000;
    /** Maximum number of words in excerpt for title. */
    public static final int WORDS_IN_EXCERPT = 10;

    /** Separator of URL's. */
    public static final String URL_SEPARATOR = ";";

    /** Empty strings list. */
    public static final String[] EMPTY_STRING_LIST = new String[0];

    /**
     * A standardized Border that describes the border around
     * a dialog button bar.
     */
    public static final Border DIALOG_BUTTON_BAR_BORDER =
        Borders.createEmptyBorder(
            Sizes.dluY(0), LayoutStyle.getCurrent().getDialogMarginX(),
            Sizes.dluY(0), LayoutStyle.getCurrent().getDialogMarginX()
        );

    /**
     * The factor to use when doing absolute sizing in pixels to make an
     * allowance for screen resolution deviations.
     */
    public static final double SIZE_FACTOR = Toolkit.getDefaultToolkit().getScreenResolution() /
            (SystemUtils.IS_OS_LINUX ? 75.0 : 96.0);

    /** Number of hours in a day. */
    public static final int HOURS_IN_DAY = 24;
    /** Number of days in a week. */
    public static final int DAYS_IN_WEEK = 7;

    /** The names of hours: 4:00AM, 8:00AM etc */
    public static final String[] HOUR_NAMES;

    static
    {
        // Initialize hour names
        HOUR_NAMES = new String[24];
        DateFormat fmt = DateFormat.getTimeInstance(DateFormat.SHORT);
        Calendar cal = new GregorianCalendar(2000, 1, 1, 0, 0);
        for (int i = 0; i < 24; i++)
        {
            HOUR_NAMES[i] = fmt.format(cal.getTime());
            cal.add(Calendar.HOUR_OF_DAY, 1);
        }
    }

    private Constants()
    {
        // Hidden constructor of utility class.
    }
}
