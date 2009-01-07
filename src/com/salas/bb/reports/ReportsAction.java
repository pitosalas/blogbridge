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
// $Id: ReportsAction.java,v 1.8 2007/10/04 08:49:54 spyromus Exp $
//

package com.salas.bb.reports;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.persistence.IPersistenceManager;
import com.salas.bb.persistence.IStatisticsManager;
import com.salas.bb.persistence.PersistenceException;
import com.salas.bb.persistence.PersistenceManagerConfig;
import com.salas.bb.persistence.domain.CountStats;
import com.salas.bb.persistence.domain.ReadStats;
import com.salas.bb.persistence.domain.VisitStats;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reports action.
 */
public class ReportsAction extends AbstractAction
{
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(ReportsAction.class.getName());

    /** Instance of the action. */
    private static ReportsAction instance;

    /** Hidden singleton constructor. */
    private ReportsAction()
    {
    }

    /**
     * Returns the report action instance.
     *
     * @return action instance.
     */
    public static synchronized ReportsAction getInstance()
    {
        if (instance == null) instance = new ReportsAction();
        return instance;
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e event.
     */
    public void actionPerformed(ActionEvent e)
    {
        new ReportsDialog(GlobalController.SINGLETON.getMainFrame(),
            new ReportDataProvider(),
            new ClickCallback()).open();
    }

    /** Performs operation as requested by reports. */
    private class ClickCallback implements IClickCallback
    {
        /**
         * Invoked when a guide clicked in a report.
         *
         * @param guide guide.
         */
        public void guideClicked(IGuide guide)
        {
            if (guide == null) return;
            GlobalController.SINGLETON.selectGuide(guide, false);
        }

        /**
         * Invoked when a guide clicked in a report.
         *
         * @param id guide ID.
         */
        public void guideClicked(long id)
        {
            GlobalModel model = GlobalModel.SINGLETON;
            GuidesSet set = model.getGuidesSet();
            guideClicked(set.findGuideByID(id));
        }

        /**
         * Invoked when a feed clicked in a report.
         *
         * @param feed feed.
         */
        public void feedClicked(IFeed feed)
        {
            if (feed == null) return;
            IGuide[] guides = feed.getParentGuides();
            if (guides.length > 0)
            {
                GlobalController.SINGLETON.selectGuide(guides[0], false);
                GlobalController.SINGLETON.selectFeed(feed, true);
            }
        }

        /**
         * Invoked when a feed clicked in a report.
         *
         * @param id feed ID.
         */
        public void feedClicked(long id)
        {
            GlobalModel model = GlobalModel.SINGLETON;
            GuidesSet set = model.getGuidesSet();
            feedClicked(set.findFeedByID(id));
        }
    }

    /** Report data provider. */
    private class ReportDataProvider implements IReportDataProvider
    {
        private final IStatisticsManager sm;

        /**
         * Creates a provider.
         */
        public ReportDataProvider()
        {
            IPersistenceManager pm = PersistenceManagerConfig.getManager();
            sm = pm.getStatisticsManager();
        }

        /**
         * Returns the list of count stats for hours of a day.
         *
         * @return stats for hours of a day.
         */
        public CountStats[] statGetItemsReadPerHour()
        {
            CountStats[] stats;

            try
            {
                stats = sm.getItemsReadPerHour();
            } catch (PersistenceException e)
            {
                LOG.log(Level.SEVERE, "Failed to fetch stats.", e);
                stats = null;
            }

            return stats;
        }

        /**
         * Returns the list of count stats for days of a week.
         *
         * @return stats for days of a week.
         */
        public CountStats[] statGetItemsReadPerWeekday()
        {
            CountStats[] stats;

            try
            {
                stats = sm.getItemsReadPerWeekday();
            } catch (PersistenceException e)
            {
                LOG.log(Level.SEVERE, "Failed to fetch stats.", e);
                stats = null;
            }

            return stats;
        }

        /**
         * Returns the list of top most visited guides.
         *
         * @param max maximum number to return.
         *
         * @return records.
         */
        public List<VisitStats> statGetMostVisitedGuides(int max)
        {
            List<VisitStats> vs;

            try
            {
                vs = sm.getMostVisitedGuides(max);
            } catch (PersistenceException e)
            {
                LOG.log(Level.SEVERE, "Failed to fetch stats.", e);
                vs = null;
            }

            return vs;
        }

        /**
         * Returns the list of top most visited feeds.
         *
         * @param max maximum number to return.
         *
         * @return records.
         */
        public List<VisitStats> statGetMostVisitedFeeds(int max)
        {
            List<VisitStats> vs;

            try
            {
                vs = sm.getMostVisitedFeeds(max);
            } catch (PersistenceException e)
            {
                LOG.log(Level.SEVERE, "Failed to fetch stats.", e);
                vs = null;
            }

            return vs;
        }

        /**
         * Returns the list of read stats for all guides.
         *
         * @return guides stats.
         */
        public List<ReadStats> statGetGuidesReadStats()
        {
            List<ReadStats> rs;

            try
            {
                rs = sm.getGuidesReadStats();
            } catch (PersistenceException e)
            {
                LOG.log(Level.SEVERE, "Failed to fetch stats.", e);
                rs = null;
            }

            return rs;
        }

        /**
         * Returns the list of read stats for all feeds.
         *
         * @return feeds stats.
         */
        public List<ReadStats> statGetFeedsReadStats()
        {
            List<ReadStats> rs;

            try
            {
                rs = sm.getFeedsReadStats();
            } catch (PersistenceException e)
            {
                LOG.log(Level.SEVERE, "Failed to fetch stats.", e);
                rs = null;
            }

            return rs;
        }

        /**
         * Returns the list of pin stats for all guides.
         *
         * @return guides stats.
         */
        public List<ReadStats> statGetGuidesPinStats()
        {
            List<ReadStats> rs;

            try
            {
                rs = sm.getGuidesPinStats();
            } catch (PersistenceException e)
            {
                LOG.log(Level.SEVERE, "Failed to fetch stats.", e);
                rs = null;
            }

            return rs;
        }

        /**
         * Returns the list of pin stats for all feeds.
         *
         * @return feeds stats.
         */
        public List<ReadStats> statGetFeedsPinStats()
        {
            List<ReadStats> rs;

            try
            {
                rs = sm.getFeedsPinStats();
            } catch (PersistenceException e)
            {
                LOG.log(Level.SEVERE, "Failed to fetch stats.", e);
                rs = null;
            }

            return rs;
        }

        /**
         * Returns the set of guides that currently visible.
         *
         * @return guides.
         */
        public GuidesSet getGuidesSet()
        {
            return GlobalModel.SINGLETON.getGuidesSet();
        }

        /** Resets data. */
        public void reset()
        {
            sm.reset();
        }
    }
}
