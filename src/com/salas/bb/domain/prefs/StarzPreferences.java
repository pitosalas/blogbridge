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
// $Id: StarzPreferences.java,v 1.4 2008/02/28 15:59:52 spyromus Exp $
//

package com.salas.bb.domain.prefs;

import com.jgoodies.binding.beans.Model;

import java.util.prefs.Preferences;

/**
 * Preferences Object for BlogBridge.
 */
public final class StarzPreferences extends Model
{
    // Keys and names

    /** Key for <code>topActivity</code> preference. */
    public static final String KEY_TOP_ACTIVITY = "starz.topActivity";
    /** Name of property holding top number of new articles to consider channel as active. */
    public static final String PROP_TOP_ACTIVITY = "topActivity";

    /** Key for <code>topHighlights</code> preference. */
    public static final String KEY_TOP_HIGHLIGHTS = "starz.topHighlights";
    /** Name of property holding top number of highlighted keywords in interesting channel. */
    public static final String PROP_TOP_HIGHLIGHTS = "topHighlights";

    /** Key for <code>activityWeight</code> preference. */
    public static final String KEY_ACTIVITY_WEIGHT = "starz.activityWeight";
    /** Name of property holding weight of activity parameter. */
    public static final String PROP_ACTIVITY_WEIGHT = "activityWeight";

    /** Key for <code>inlinksWeight</code> preference. */
    public static final String KEY_INLINKS_WEIGHT = "starz.inlinksWeight";
    /** Name of property holding weight of importance parameter. */
    public static final String PROP_INLINKS_WEIGHT = "inlinksWeight";

    /** Key for <code>clickthroughsWeight</code> preference. */
    static final String KEY_CLICKTHROUGHS_WEIGHT = "starz.clickthroughsWeight";
    /** Name of property holding clickthroughs weight. */
    public static final String PROP_CLICKTHROUGHS_WEIGHT = "clickthroughsWeight";
    /** Key for <code>feedViewsWeight</code> property. */
    static final String KEY_FEED_VIEWS_WEIGHT = "starz.feedViewsWeight";
    /** Name of property holding feed views weight. */
    public static final String PROP_FEED_VIEWS_WEIGHT = "feedViewsWeight";

    // Defaults

    private static final int DEFAULT_TOP_ACTIVITY = 10;
    private static final int DEFAULT_TOP_HIGHLIGHTS = 10;
    private static final int DEFAULT_ACTIVITY_WEIGHT = 0;
    private static final int DEFAULT_INLINKS_WEIGHT = 2;
    private static final int DEFAULT_CLICKTHROUGHS_WEIGHT = 2;
    private static final int DEFAULT_FEED_VIEWS_WEIGHT = 2;

    // Properties

    // Number of new articles per day (24h) in channel to consider it very active.
    private int topActivity = DEFAULT_TOP_ACTIVITY;

    // Number of highlights per channel to consider it top-most.
    private int topHighlights = DEFAULT_TOP_HIGHLIGHTS;

    // Weight of activity score in range [0;4]
    private int activityWeight = DEFAULT_ACTIVITY_WEIGHT;

    // Weight of importance score in range [0;4]
    private int inlinksWeight = DEFAULT_INLINKS_WEIGHT;

    // Weight of clickthroughs score in range [0;4]
    private int clickthroughsWeight = DEFAULT_CLICKTHROUGHS_WEIGHT;

    // Weight of feed views score in range [0;4]
    private int feedViewsWeight = DEFAULT_FEED_VIEWS_WEIGHT;

    /**
     * Returns number of new articles per day to be present in the channel to consider it
     * top active.
     *
     * @return number of articles per day.
     */
    public int getTopActivity()
    {
        return topActivity;
    }

    /**
     * Sets number of new articles per day to be present in the channel to consider it
     * top active.
     *
     * @param value number of articles per day.
     */
    public void setTopActivity(int value)
    {
        int oldValue = topActivity;
        topActivity = value;
        firePropertyChange(PROP_TOP_ACTIVITY, oldValue, topActivity);
    }

    /**
     * Returns number of highlighted keywords to be present in the channel to consider it
     * top active.
     *
     * @return number of highlights per channel.
     */
    public int getTopHighlights()
    {
        return topHighlights;
    }

    /**
     * Sets number of highlights for top channel.
     *
     * @param value number of highlights.
     */
    public void setTopHighlights(int value)
    {
        int oldValue = topHighlights;
        topHighlights = value;
        firePropertyChange(PROP_TOP_HIGHLIGHTS, oldValue, topHighlights);
    }

    /**
     * Returns activity weight.
     *
     * @return weight.
     */
    public int getActivityWeight()
    {
        return activityWeight;
    }

    /**
     * Sets activity weight.
     *
     * @param value activity waight.
     */
    public void setActivityWeight(int value)
    {
        int oldValue = activityWeight;
        activityWeight = value;
        firePropertyChange(PROP_ACTIVITY_WEIGHT, oldValue, activityWeight);
    }

    /**
     * Returns importance weight.
     *
     * @return weight.
     */
    public int getInlinksWeight()
    {
        return inlinksWeight;
    }

    /**
     * Sets importance weight.
     *
     * @param value importance weight.
     */
    public void setInlinksWeight(int value)
    {
        int oldValue = inlinksWeight;
        inlinksWeight = value;
        firePropertyChange(PROP_INLINKS_WEIGHT, oldValue, inlinksWeight);
    }

    /**
     * Returns the weight of clickthroughs.
     *
     * @return the weight.
     */
    public int getClickthroughsWeight()
    {
        return clickthroughsWeight;
    }

    /**
     * Sets clickthroughs weight.
     *
     * @param weight weight.
     */
    public void setClickthroughsWeight(int weight)
    {
        int oldValue = clickthroughsWeight;
        clickthroughsWeight = weight;
        firePropertyChange(PROP_CLICKTHROUGHS_WEIGHT, oldValue, clickthroughsWeight);
    }

    /**
     * Returns feed views weight.
     *
     * @return weight.
     */
    public int getFeedViewsWeight()
    {
        return feedViewsWeight;
    }

    /**
     * Returns feed views weight.
     *
     * @param weight weight.
     */
    public void setFeedViewsWeight(int weight)
    {
        int oldValue = feedViewsWeight;
        feedViewsWeight = weight;
        firePropertyChange(PROP_FEED_VIEWS_WEIGHT, oldValue, feedViewsWeight);
    }

    /**
     * Read all the Preferences from persistent preferences into this object. On Windows, the
     * persistent store is the Registry.
     *
     * @param prefs object representing persistent Preferences.
     */
    public void restoreFrom(Preferences prefs)
    {
        setTopActivity(prefs.getInt(KEY_TOP_ACTIVITY, DEFAULT_TOP_ACTIVITY));
        setTopHighlights(prefs.getInt(KEY_TOP_HIGHLIGHTS, DEFAULT_TOP_HIGHLIGHTS));

        setActivityWeight(prefs.getInt(KEY_ACTIVITY_WEIGHT, DEFAULT_ACTIVITY_WEIGHT));
        setInlinksWeight(prefs.getInt(KEY_INLINKS_WEIGHT, DEFAULT_INLINKS_WEIGHT));
        setClickthroughsWeight(prefs.getInt(KEY_CLICKTHROUGHS_WEIGHT, DEFAULT_CLICKTHROUGHS_WEIGHT));
        setFeedViewsWeight(prefs.getInt(KEY_FEED_VIEWS_WEIGHT, DEFAULT_FEED_VIEWS_WEIGHT));
    }

    /**
     * Write all the preferences from this Object to persistent preferences. On Windows, this is the
     * registry.
     *
     * @param prefs object representing persistent Preferences.
     */
    public void storeIn(Preferences prefs)
    {
        prefs.putInt(KEY_TOP_ACTIVITY, getTopActivity());
        prefs.putInt(KEY_TOP_HIGHLIGHTS, getTopHighlights());

        prefs.putInt(KEY_ACTIVITY_WEIGHT, getActivityWeight());
        prefs.putInt(KEY_INLINKS_WEIGHT, getInlinksWeight());
        prefs.putInt(KEY_CLICKTHROUGHS_WEIGHT, getClickthroughsWeight());
        prefs.putInt(KEY_FEED_VIEWS_WEIGHT, getFeedViewsWeight());
    }
}