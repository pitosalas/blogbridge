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
// $Id: IReportDataProvider.java,v 1.5 2007/09/25 11:14:10 spyromus Exp $
//

package com.salas.bb.reports;

import com.salas.bb.domain.GuidesSet;
import com.salas.bb.persistence.domain.CountStats;
import com.salas.bb.persistence.domain.ReadStats;
import com.salas.bb.persistence.domain.VisitStats;

import java.util.List;

/**
 * Abstract report data provider which is called when the display
 * needs any data.
 */
interface IReportDataProvider
{
    /**
     * Returns the list of count stats for hours of a day.
     *
     * @return stats for hours of a day.
     */
    CountStats[] statGetItemsReadPerHour();

    /**
     * Returns the list of count stats for days of a week.
     *
     * @return stats for days of a week.
     */
    CountStats[] statGetItemsReadPerWeekday();

    /**
     * Returns the list of top most visited guides.
     *
     * @param max maximum number to return.
     *
     * @return records.
     */
    List<VisitStats> statGetMostVisitedGuides(int max);

    /**
     * Returns the list of top most visited feeds.
     *
     * @param max maximum number to return.
     *
     * @return records.
     */
    List<VisitStats> statGetMostVisitedFeeds(int max);

    /**
     * Returns the list of read stats for all guides.
     *
     * @return guides stats.
     */
    List<ReadStats> statGetGuidesReadStats();

    /**
     * Returns the list of read stats for all feeds.
     *
     * @return feeds stats.
     */
    List<ReadStats> statGetFeedsReadStats();

    /**
     * Returns the list of pin stats for all guides.
     *
     * @return guides stats.
     */
    List<ReadStats> statGetGuidesPinStats();

    /**
     * Returns the list of pin stats for all feeds.
     *
     * @return feeds stats.
     */
    List<ReadStats> statGetFeedsPinStats();

    /**
     * Resets data.
     */
    void reset();

    /**
     * Returns the set of guides that currently visible.
     *
     * @return guides.
     */
    GuidesSet getGuidesSet();
}
