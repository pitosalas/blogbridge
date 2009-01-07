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
// $Id: DateAfterCO.java,v 1.5 2007/02/20 15:15:55 spyromus Exp $
//

package com.salas.bb.domain.query.general;

import com.salas.bb.utils.TimeRange;
import com.salas.bb.utils.i18n.Strings;

/**
 * Comparison operation which checks whether the date is after synthetic date range:
 * today, yesterday, last week and etc.
 */
public class DateAfterCO extends DateAbstractCO
{
    /** Instance of this operation. */
    public static final DateAfterCO INSTANCE = new DateAfterCO();

    /**
     * Creates operation.
     */
    public DateAfterCO()
    {
        super(Strings.message("query.operation.after"), "after");
    }

    /**
     * Matches target timestamp against range.
     *
     * @param aTargetTimestamp timestamp.
     * @param aTimeRange       time range to check against.
     *
     * @return <code>TRUE</code> if the timestamp matches.
     */
    protected boolean match(long aTargetTimestamp, TimeRange aTimeRange)
    {
        return aTimeRange.isAfter(aTargetTimestamp);
    }
}
