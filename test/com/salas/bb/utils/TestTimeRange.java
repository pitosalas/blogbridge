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
// $Id: TestTimeRange.java,v 1.2 2006/01/08 05:28:19 kyank Exp $
//

package com.salas.bb.utils;

import junit.framework.TestCase;

/**
 * This suite contains tests for <code>TimeRange</code> unit.
 */
public class TestTimeRange extends TestCase
{
    private long today;
    private long yesterday;
    private long dayBeforeYesterday;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        today = System.currentTimeMillis();
        yesterday = today - Constants.MILLIS_IN_DAY;
        dayBeforeYesterday = yesterday - Constants.MILLIS_IN_DAY;
    }

    /**
     * Tests before.
     */
    public void testBefore()
    {
        assertFalse(TimeRange.TR_TODAY.isBefore(today));
        assertTrue(TimeRange.TR_TODAY.isBefore(yesterday));
        assertTrue(TimeRange.TR_TODAY.isBefore(dayBeforeYesterday));

        assertFalse(TimeRange.TR_YESTERDAY.isBefore(today));
        assertFalse(TimeRange.TR_YESTERDAY.isBefore(yesterday));
        assertTrue(TimeRange.TR_YESTERDAY.isBefore(dayBeforeYesterday));
    }

    /**
     * Tests after.
     */
    public void testAfter()
    {
        assertFalse(TimeRange.TR_TODAY.isAfter(today));
        assertFalse(TimeRange.TR_TODAY.isAfter(yesterday));
        assertFalse(TimeRange.TR_TODAY.isAfter(dayBeforeYesterday));

        assertTrue(TimeRange.TR_YESTERDAY.isAfter(today));
        assertFalse(TimeRange.TR_YESTERDAY.isAfter(yesterday));
        assertFalse(TimeRange.TR_YESTERDAY.isAfter(dayBeforeYesterday));
    }

    /**
     * Tests match.
     */
    public void testMatch()
    {
        assertTrue(TimeRange.TR_TODAY.isInRange(today));
        assertFalse(TimeRange.TR_TODAY.isInRange(yesterday));
        assertFalse(TimeRange.TR_TODAY.isInRange(dayBeforeYesterday));

        assertFalse(TimeRange.TR_YESTERDAY.isInRange(today));
        assertTrue(TimeRange.TR_YESTERDAY.isInRange(yesterday));
        assertFalse(TimeRange.TR_YESTERDAY.isInRange(dayBeforeYesterday));
    }
}
