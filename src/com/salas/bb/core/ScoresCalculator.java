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
// $Id: ScoresCalculator.java,v 1.15 2008/02/28 15:59:49 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.domain.DataFeed;
import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.SearchFeed;
import com.salas.bb.domain.prefs.StarzPreferences;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.concurrency.CachingCalculator;

import java.beans.PropertyChangeEvent;

/**
 * Calculator of different scores for the feed.
 */
public final class ScoresCalculator
{
    private double                  activityWeight;
    private double                  inlinksWeight;
    private double                  clickthroughsWeight;
    private double                  feedViewsWeight;
    private int                     topActivity;

    private static int              maxClickthroughs = 0;
    private static int              maxFeedViews = 0;

    /**
     * Creates calculator.
     */
    ScoresCalculator()
    {
        initThreading();
        loadPreferences(null);
    }

    /**
     * Registers and updates the max feed views (if current value is lower).
     *
     * @param views number of views.
     *
     * @return <code>TRUE</code> if updated the value.
     */
    public static synchronized boolean registerMaxFeedViews(int views)
    {
        boolean updated = false;

        if (maxFeedViews < views)
        {
            maxFeedViews = views;
            updated = true;
        }

        return updated;
    }

    /**
     * Registers and updates the max clickthroughs (if current value is lower).
     *
     * @param clicks number of views.
     *
     * @return <code>TRUE</code> if updated the value.
     */
    public static synchronized boolean registerMaxClickthroughs(int clicks)
    {
        boolean updated = false;

        if (maxClickthroughs < clicks)
        {
            maxClickthroughs = clicks;
            updated = true;
        }

        return updated;
    }

    /**
     * Calculates final score.
     *
     * @param feed   feed to calculate score for.
     *
     * @return score.
     */
    public int calcFinalScore(IFeed feed)
    {
        int score = -1;
        if (feed instanceof DataFeed)
        {
            score = 0;
            if (((DataFeed)feed).isInitialized())
            {
                score = getRatingOfFeed(feed);
            }
        } else if (feed instanceof SearchFeed)
        {
            score = getRatingOfFeed(feed);
        }

            return score;
    }

    private int getRatingOfFeed(IFeed feed)
    {
        int rating = feed.getRating();
        if (rating == -1)
        {
            rating = feed instanceof SearchFeed ? IFeed.RATING_MAX : calcBlogStarzScore(feed);
        }

        return rating;
    }

    /**
     * Calculates BlogStarz score.
     *
     * @param feed feed to calculte the score for.
     *
     * @return the score.
     */
    public int calcBlogStarzScore(IFeed feed)
    {
        return (Integer)calculator.getValue(feed);
    }

    private int calculateTheScore(IFeed feed)
    {
        int score;

        double activity = (feed instanceof DataFeed) ? calcActivity((DataFeed)feed) : -1;
        double inlinks = (feed instanceof DirectFeed) ? calcInlinks((DirectFeed)feed) : -1;
        double clickthroughs = calcClickthroughsScore(feed);
        double feedViews = calcFeedViewsScore(feed);

        double calcInlinksWeight = inlinks > -1 ? inlinksWeight : 0;
        double calcActivityWeight = activity > -1 ? activityWeight : 0;
        double totalWeight = calcActivityWeight + calcInlinksWeight +
            clickthroughsWeight + feedViewsWeight;

        score = (int)((
            activity * calcActivityWeight +
            inlinks * calcInlinksWeight +
            clickthroughs * clickthroughsWeight +
            feedViews * feedViewsWeight) * 4 / totalWeight);

        return score;
    }

    /**
     * Calculates the clickthroughs score.
     *
     * @param feed feed.
     *
     * @return score.
     */
    public double calcClickthroughsScore(IFeed feed)
    {
        return maxClickthroughs == 0 ? 0 : (double)feed.getClickthroughs() / maxClickthroughs;
    }

    /**
     * Calculates the feed views score.
     *
     * @param feed feed.
     *
     * @return score.
     */
    public double calcFeedViewsScore(IFeed feed)
    {
        return maxFeedViews == 0 ? 0 : (double)feed.getViews() / maxFeedViews;
    }

    /**
     * Calculates importance score for the feed.
     *
     * @param feed   feed.
     *
     * @return score.
     */
    public double calcInlinksScore(DirectFeed feed)
    {
        return feed.isInitialized() ? calcInlinks(feed) : 0;
    }

    /**
     * Calculate feed interest score in range [0;1] or -1 if not initialized.
     *
     * @param feed feed to compute score for.
     *
     * @return interest score.
     */
    private double calcInlinks(DirectFeed feed)
    {
        return calcInlinksScore(feed.getInLinks());
    }

    /**
     * Converts inbound links count into a score value in range [0:1].
     *
     * @param inboundLinks number of links.
     *
     * @return score.
     */
    public static double calcInlinksScore(int inboundLinks)
    {
        double aResult = -1;
        if (inboundLinks >= 0)
        {
            if (inboundLinks < 100)
            {
                aResult = 0;
            } else if (inboundLinks < 250)
            {
                aResult = 0.25;
            } else if (inboundLinks < 1000)
            {
                aResult = 0.5;
            } else if (inboundLinks < 2000)
            {
                aResult = 0.75;
            } else
            {
                aResult = 1;
            }
        }
        return aResult;
    }

    /**
     * Calculates pure feed activity in range [0;1].
     *
     * @param feed feed to compute score for.
     *
     * @return feed activity.
     */
    public double calcActivity(DataFeed feed)
    {
        double activity = 0;

        int total = feed.getTotalPolledArticles();
        long initTime = feed.getInitTime();

        if (initTime > 0 && total > 0)
        {
            long time = System.currentTimeMillis();

            activity = ((double)total / (time - initTime)) /
                ((double)topActivity / Constants.MILLIS_IN_DAY);

            if (activity > 1) activity = 1;
        }

        return activity;
    }

    // ---------------------------------------------------------------------------------------------
    // Recalculation
    // ---------------------------------------------------------------------------------------------

    private Calculator      calculator;

    // Initializes multi-threaded calculator
    private void initThreading()
    {
        calculator = new Calculator(1);
    }

    /**
     * Marks cached score value for this feed as invalid and starts immediate background
     * recalculation. If feed wasn't calculated yet new cache record will be created.
     *
     * @param feed feed to invalidate.
     */
    public void invalidateFeed(IFeed feed)
    {
        calculator.invalidateKey(feed);
    }

    /**
     * Marks whole cache as invalid and starts background recalculation of all previously
     * calculated channels.
     */
    public void invalidateAll()
    {
        calculator.invalidateAll();
    }

    /**
     * Called when some feed no longer need the score to be cached.
     *
     * @param feed feed deleted.
     */
    public void feedRemoved(IFeed feed)
    {
        calculator.removeKey(feed);
    }

    /**
     * Load initial preferences.
     *
     * @param prefs prefs.
     */
    public void loadPreferences(StarzPreferences prefs)
    {
        if (prefs == null)
        {
            activityWeight = -1;
            inlinksWeight = -1;
            clickthroughsWeight = -1;
            feedViewsWeight = -1;

            topActivity = -1;
        } else
        {
            activityWeight = prefs.getActivityWeight();
            inlinksWeight = prefs.getInlinksWeight();
            clickthroughsWeight = prefs.getClickthroughsWeight();
            feedViewsWeight = prefs.getFeedViewsWeight();

            topActivity = prefs.getTopActivity();
        }

        invalidateAll();
    }

    /**
     * Listens to changes in properties.
     *
     * @param evt event object.
     *
     * @return TRUE if event was dispatched.
     */
    public boolean dispatchPropertyChangeEvent(PropertyChangeEvent evt)
    {
        String prop = evt.getPropertyName();
        final Object source = evt.getSource();
        boolean invalidate = false;

        if (source instanceof StarzPreferences)
        {
            StarzPreferences prefs = (StarzPreferences)source;

            if (StarzPreferences.PROP_ACTIVITY_WEIGHT.equals(prop))
            {
                activityWeight = prefs.getActivityWeight();
                invalidate = true;
            } else if (StarzPreferences.PROP_INLINKS_WEIGHT.equals(prop))
            {
                inlinksWeight = prefs.getInlinksWeight();
                invalidate = true;
            } else if (StarzPreferences.PROP_TOP_ACTIVITY.equals(prop))
            {
                topActivity = prefs.getTopActivity();
                invalidate = true;
            } else if (StarzPreferences.PROP_CLICKTHROUGHS_WEIGHT.equals(prop))
            {
                clickthroughsWeight = prefs.getClickthroughsWeight();
                invalidate = true;
            } else if (StarzPreferences.PROP_FEED_VIEWS_WEIGHT.equals(prop))
            {
                feedViewsWeight = prefs.getFeedViewsWeight();
                invalidate = true;
            }

            if (invalidate) invalidateAll();
        }
        
        return invalidate;
    }

    /**
     * Simple calculator of the channel scores.
     */
    private class Calculator extends CachingCalculator
    {
        public Calculator(int threads)
        {
            super(threads);
            startThreads();
        }

        protected String getThreadsBaseName()
        {
            return "SC";
        }

        protected Object calculate(Object key)
        {
            return calculateTheScore((IFeed)key);
        }
    }
}
