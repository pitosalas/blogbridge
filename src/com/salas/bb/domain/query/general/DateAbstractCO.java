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
// $Id: DateAbstractCO.java,v 1.4 2007/02/20 15:15:55 spyromus Exp $
//

package com.salas.bb.domain.query.general;

import com.salas.bb.domain.query.AbstractComparisonOperation;
import com.salas.bb.utils.TimeRange;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract implementation of date comparison operation. During the matching it accepts
 * stringified timestamp as target value and string name of the time range. The comparison
 * operation by itself is being delegated to the abstract method which should be
 * overriden by concrete implementation.
 */
abstract class DateAbstractCO extends AbstractComparisonOperation implements IDates
{
    protected static final Map<String, TimeRange> RANGES_MAP = new HashMap<String, TimeRange>();

    static {
        RANGES_MAP.put(VALUE_TODAY, TimeRange.TR_TODAY);
        RANGES_MAP.put(VALUE_YESTERDAY, TimeRange.TR_YESTERDAY);
        RANGES_MAP.put(VALUE_LAST_WEEK, TimeRange.TR_LAST_WEEK);
        RANGES_MAP.put(VALUE_TWO_WEEKS_AGO, TimeRange.TR_TWO_WEEKS_AGO);
    }

    /**
     * Creates operation.
     *
     * @param aName name.
     * @param aDescriptor operation descriptor.
     */
    public DateAbstractCO(String aName, String aDescriptor)
    {
        super(aName, aDescriptor);
    }

    /**
     * Compares some target value against value for comparison.
     *
     * @param targetValue     target value.
     * @param comparisonValue comparison value.
     *
     * @return TRUE if the target value matches the condition presented by this comparison operation
     *         in conjunction with comparison value.
     */
    public boolean match(String targetValue, String comparisonValue)
    {
        long targetTimestamp = Long.parseLong(targetValue);
        TimeRange timeRange = nameToRange(comparisonValue);

        return match(targetTimestamp, timeRange);
    }

    /**
     * Matches target timestamp against range.
     *
     * @param aTargetTimestamp  timestamp.
     * @param aTimeRange        time range to check against.
     *
     * @return <code>TRUE</code> if the timestamp matches.
     */
    protected abstract boolean match(long aTargetTimestamp, TimeRange aTimeRange);

    private TimeRange nameToRange(String name)
    {
        return RANGES_MAP.get(name);
    }
}
