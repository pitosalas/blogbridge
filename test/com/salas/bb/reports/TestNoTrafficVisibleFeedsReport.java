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
// $Id: TestNoTrafficVisibleFeedsReport.java,v 1.1 2007/09/25 11:14:10 spyromus Exp $
//

package com.salas.bb.reports;

import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.StandardArticle;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.DateUtils;
import junit.framework.TestCase;

import java.util.Date;

/**
 * Tests NoTrafficVisibleFeedsReport.
 */
public class TestNoTrafficVisibleFeedsReport extends TestCase
{
    /** A week ago. */
    private final Date WEEK_AGO = new Date(DateUtils.getTodayTime() - Constants.MILLIS_IN_WEEK);
    /** A day ago. */
    private final Date DAY_AGO = new Date(DateUtils.getTodayTime() - Constants.MILLIS_IN_DAY);
    /** A month ago. */
    private final Date MONTH_AGO = new Date(DateUtils.getTodayTime() - Constants.MILLIS_IN_MONTH);

    /** Test feed. */
    private DirectFeed feed;

    /**
     * Prepares environment.
     *
     * @throws Exception in case of error.
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        feed = new DirectFeed();
    }

    /** Testing no articles. */
    public void testIsEligible_NoArticles()
    {
        assertFalse(NoTrafficVisibleFeedsReport.isEligible(feed, WEEK_AGO));
    }

    /** Testsig having articles, but not this week. */
    public void testIsEligible_NoTraffic()
    {
        // Add a sample article from month ago
        StandardArticle art = new StandardArticle("a");
        art.setPublicationDate(MONTH_AGO);
        feed.appendArticle(art);

        assertTrue(NoTrafficVisibleFeedsReport.isEligible(feed, WEEK_AGO));
    }

    /** Testing having traffic. */
    public void testIsEligible_Traffic()
    {
        // Add a sample article from month ago
        StandardArticle art = new StandardArticle("a");
        art.setPublicationDate(DAY_AGO);
        feed.appendArticle(art);

        assertFalse(NoTrafficVisibleFeedsReport.isEligible(feed, WEEK_AGO));
    }
}
