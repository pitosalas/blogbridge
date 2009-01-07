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
// $Id: ThreadedStatisticsManager.java,v 1.1 2007/10/04 08:49:53 spyromus Exp $
//

package com.salas.bb.persistence.backend;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.persistence.IStatisticsManager;
import com.salas.bb.persistence.PersistenceException;
import com.salas.bb.persistence.domain.CountStats;
import com.salas.bb.persistence.domain.ReadStats;
import com.salas.bb.persistence.domain.VisitStats;
import com.salas.bb.utils.concurrency.ExecutorFactory;

import java.util.List;

/**
 * Knows how to gather stats in a separate thread.
 */
class ThreadedStatisticsManager implements IStatisticsManager
{
    /** Wrapped manager. */
    private final IStatisticsManager man;

    /** Singlethreaded executor. */
    private final Executor executor;

    /**
     * Builds the manager over another manager.
     *
     * @param man manager.
     */
    ThreadedStatisticsManager(IStatisticsManager man)
    {
        this.man = man;
        executor = ExecutorFactory.createPooledExecutor("Statistics", 1, 1000l);
    }

    // --------------------------------------------------------------------------------------------
    // Overriding in threads
    // --------------------------------------------------------------------------------------------

    /**
     * Records visit to a guide.
     *
     * @param guide guide.
     */
    public void guideVisited(final IGuide guide)
    {
        schedule(new Runnable()
        {
            public void run()
            {
                man.guideVisited(guide);
            }
        });
    }

    /**
     * Records visit to a feed.
     *
     * @param feed feed.
     */
    public void feedVisited(final IFeed feed)
    {
        schedule(new Runnable()
        {
            public void run()
            {
                man.feedVisited(feed);
            }
        });
    }

    /**
     * Records marking articles as read.
     *
     * @param guide guide where articles were marked as read (NULLable).
     * @param feed  feed where articles were marked as read (NULLable).
     * @param count number of articles.
     */
    public void articlesRead(final IGuide guide, final IFeed feed, final int count)
    {
        schedule(new Runnable()
        {
            public void run()
            {
                man.articlesRead(guide, feed, count);
            }
        });
    }

    /**
     * Records marking articles as pinned.
     *
     * @param guide guide where articles were marked (NULLable).
     * @param feed  feed where articles were marked (NULLable).
     * @param count number of articles pinned.
     */
    public void articlesPinned(final IGuide guide, final IFeed feed, final int count)
    {
        schedule(new Runnable()
        {
            public void run()
            {
                man.articlesPinned(guide, feed, count);
            }
        });
    }

    /**
     * Resets the statistics.
     */
    public void reset()
    {
        schedule(new Runnable()
        {
            public void run()
            {
                man.reset();
            }
        });
    }

    /**
     * Schedules the task for execution. If terminated, the task is executed immediately.
     *
     * @param task task.
     */
    private void schedule(Runnable task)
    {
        try
        {
            executor.execute(task);
        } catch (InterruptedException e)
        {
            task.run();
        }
    }

    // --------------------------------------------------------------------------------------------
    // Returning directly
    // --------------------------------------------------------------------------------------------

    /**
     * Returns the list of top most visited guides.
     *
     * @param max maximum number to return.
     *
     * @return records.
     *
     * @throws PersistenceException
     *          if fails to query records from database.
     */
    public List<VisitStats> getMostVisitedGuides(int max) throws PersistenceException
    {
        return man.getMostVisitedGuides(max);
    }

    /**
     * Returns the list of top most visited feeds.
     *
     * @param max maximum number to return.
     *
     * @return records.
     *
     * @throws PersistenceException
     *          if fails to query records from database.
     */
    public List<VisitStats> getMostVisitedFeeds(int max) throws PersistenceException
    {
        return man.getMostVisitedFeeds(max);
    }

    /**
     * Returns the list of count stats for hours of a day.
     *
     * @return stats for hours of a day.
     *
     * @throws PersistenceException
     *          if fails to query records from database.
     */
    public CountStats[] getItemsReadPerHour() throws PersistenceException
    {
        return man.getItemsReadPerHour();
    }

    /**
     * Returns the list of count stats for days of a week.
     *
     * @return stats for days of a week.
     *
     * @throws PersistenceException
     *          if fails to query records from database.
     */
    public CountStats[] getItemsReadPerWeekday() throws PersistenceException
    {
        return man.getItemsReadPerWeekday();
    }

    /**
     * Returns the list of read stats for all guides.
     *
     * @return guides stats.
     *
     * @throws PersistenceException
     *          if fails to query records from database.
     */
    public List<ReadStats> getGuidesReadStats() throws PersistenceException
    {
        return man.getGuidesReadStats();
    }

    /**
     * Returns the list of read stats for all feeds.
     *
     * @return feeds stats.
     *
     * @throws PersistenceException
     *          if fails to query records from database.
     */
    public List<ReadStats> getFeedsReadStats() throws PersistenceException
    {
        return man.getFeedsReadStats();
    }

    /**
     * Returns the list of pin stats for all guides.
     *
     * @return guides stats.
     *
     * @throws PersistenceException
     *          if fails to query records from database.
     */
    public List<ReadStats> getGuidesPinStats() throws PersistenceException
    {
        return man.getGuidesPinStats();
    }

    /**
     * Returns the list of pin stats for all feeds.
     *
     * @return feeds stats.
     *
     * @throws PersistenceException
     *          if fails to query records from database.
     */
    public List<ReadStats> getFeedsPinStats() throws PersistenceException
    {
        return man.getFeedsPinStats();
    }
}
