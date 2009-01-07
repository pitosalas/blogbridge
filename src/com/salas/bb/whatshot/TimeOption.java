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
// $Id: TimeOption.java,v 1.3 2007/09/07 13:53:13 spyromus Exp $
//

package com.salas.bb.whatshot;

import com.salas.bb.utils.Constants;
import com.salas.bb.utils.i18n.Strings;

/**
 * Time option.
 */
public class TimeOption
{
    /** All time. */
    public static final TimeOption ALL =
        new TimeOption(0, Strings.message("timeoption.all"), Long.MAX_VALUE);

    /** This day only. */
    public static final TimeOption TODAY =
        new TimeOption(1, Strings.message("timeoption.today"), Constants.MILLIS_IN_DAY);

    /** This week only. */
    public static final TimeOption THIS_WEEK =
        new TimeOption(2, Strings.message("timeoption.this.week"), 7 * Constants.MILLIS_IN_DAY);

    /** All available options. */
    public static final TimeOption[] OPTIONS = new TimeOption[] { THIS_WEEK, TODAY };

    private final int code;
    private final String title;
    private final long offset;

    /**
     * Creates the option.
     *
     * @param code      code.
     * @param title     title.
     * @param offset    time offset in ms.
     */
    private TimeOption(int code, String title, long offset)
    {
        this.offset = offset;
        this.code = code;
        this.title = title;
    }

    /**
     * Returns the title.
     *
     * @return title.
     */
    public String toString()
    {
        return title;
    }

    /**
     * Returns the code.
     *
     * @return code.
     */
    public int getCode()
    {
        return code;
    }

    /**
     * Returns the offset in ms from NOW.
     *
     * @return offset in ms.
     */
    public long getOffset()
    {
        return offset;
    }

    /**
     * Returns the option by its code.
     *
     * @param code code.
     *
     * @return option.
     */
    public static TimeOption fromCode(int code)
    {
        switch (code)
        {
            case 0: return ALL;
            case 1: return TODAY;
            case 2: return THIS_WEEK;
            default: throw new IllegalArgumentException("The code is undefined: " + code);
        }
    }
}
