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
// $Id: IStatisticsManager.java,v 1.1 2007/10/04 08:49:53 spyromus Exp $
//

package com.salas.bb.persistence;

import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.persistence.domain.CountStats;
import com.salas.bb.persistence.domain.ReadStats;
import com.salas.bb.persistence.domain.VisitStats;

import java.util.List;

/**
 * A manager that knows how to record and return statistics
 */
public interface IStatisticsManager
{
    /**
     * Records visit to a guide.
     *
     * @param guide guide.
     */
    void guideVisited(IGuide guide);

    /**
     * Records visit to a feed.
     *
     * @param feed feed.
     */
    void feedVisited(IFeed feed);

    /**
     * Records marking articles as read.
     *
     * @param guide guide where articles were marked as read (NULLable).
     * @param feed  feed where articles were marked as read (NULLable).
     * @param count number of articles.
     */
    void articlesRead(IGuide guide, IFeed feed, int count);

    /**
     * Records marking articles as pinned.
     *
     * @param guide guide where articles were marked (NULLable).
     * @param feed  feed where articles were marked (NULLable).
     * @param count number of articles pinned.
     */
    void articlesPinned(IGuide guide, IFeed feed, int count);

    /**
     * Returns the list of top most visited guides.
     *
     * @param max maximum number to return.
     *
     * @return records.
     *
     * @throws com.salas.bb.persistence.PersistenceException if fails to query records from database.
     */
    List<VisitStats> getMostVisitedGuides(int max)
        throws PersistenceException;

    /**
     * Returns the list of top most visited feeds.
     *
     * @param max maximum number to return.
     *
     * @return records.
     *
     * @throws com.salas.bb.persistence.PersistenceException if fails to query records from database.
     */
    List<VisitStats> getMostVisitedFeeds(int max)
        throws PersistenceException;

    /**
     * Returns the list of count stats for hours of a day.
     *
     * @return stats for hours of a day.
     *
     * @throws com.salas.bb.persistence.PersistenceException if fails to query records from database.
     */
    CountStats[] getItemsReadPerHour()
        throws PersistenceException;

    /**
     * Returns the list of count stats for days of a week.
     *
     * @return stats for days of a week.
     *
     * @throws com.salas.bb.persistence.PersistenceException if fails to query records from database.
     */
    CountStats[] getItemsReadPerWeekday()
        throws PersistenceException;

    /**
     * Returns the list of read stats for all guides.
     *
     * @return guides stats.
     *
     * @throws com.salas.bb.persistence.PersistenceException if fails to query records from database.
     */
    List<ReadStats> getGuidesReadStats()
        throws PersistenceException;

    /**
     * Returns the list of read stats for all feeds.
     *
     * @return feeds stats.
     *
     * @throws com.salas.bb.persistence.PersistenceException if fails to query records from database.
     */
    List<ReadStats> getFeedsReadStats()
        throws PersistenceException;

    /**
     * Returns the list of pin stats for all guides.
     *
     * @return guides stats.
     *
     * @throws com.salas.bb.persistence.PersistenceException if fails to query records from database.
     */
    List<ReadStats> getGuidesPinStats()
        throws PersistenceException;

    /**
     * Returns the list of pin stats for all feeds.
     *
     * @return feeds stats.
     *
     * @throws com.salas.bb.persistence.PersistenceException if fails to query records from database.
     */
    List<ReadStats> getFeedsPinStats()
        throws PersistenceException;

    /**
     * Resets the statistics.
     */
    void reset();
}
