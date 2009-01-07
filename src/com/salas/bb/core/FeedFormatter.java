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
// $Id: FeedFormatter.java,v 1.8 2006/05/29 12:48:29 spyromus Exp $
//

package com.salas.bb.core;

import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.domain.IFeed;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.util.logging.Logger;
import java.text.MessageFormat;

/**
 * This utility class implements various CGE to Text and CGE to Icon formatting functions.
 */
public class FeedFormatter
{
    private static final Logger LOG = Logger.getLogger(FeedFormatter.class.getName());

    private static final String[] STARS_ICONS_NAMES = {
        "onestaricon.icon", "twostaricon.icon", "threestaricon.icon",
        "fourstaricon.icon", "fivestaricon.icon",
        "onestaricon.silver.icon", "twostaricon.silver.icon", "threestaricon.silver.icon",
        "fourstaricon.silver.icon", "fivestaricon.silver.icon" };
    private static final Icon[] STARS_ICONS;

    private static final String[] LOADING_ICONS_NAME = {
        "loading.1.icon", "loading.2.icon", "loading.3.icon", "loading.4.icon" };
    private static final Icon[] LOADING_ICONS;

    private IFeed feed;
    private Icon starsIcon;
    private int starScore;
    private int goodArticles;

    static
    {
        // Loading icons
        LOADING_ICONS = new Icon[LOADING_ICONS_NAME.length];
        for (int i = 0; i < LOADING_ICONS_NAME.length; i++)
        {
            String iconName = LOADING_ICONS_NAME[i];
            LOADING_ICONS[i] = ResourceUtils.getIcon(iconName);
        }

        // Stars icons
        STARS_ICONS = new Icon[STARS_ICONS_NAMES.length];
        for (int i = 0; i < STARS_ICONS_NAMES.length; i++)
        {
            String iconName = STARS_ICONS_NAMES[i];
            STARS_ICONS[i] = ResourceUtils.getIcon(iconName);
        }
    }

    /**
     * Creates new formatter for specified CGE.
     *
     * @param aFeed    channel guide entry.
     */
    public FeedFormatter(IFeed aFeed)
    {
        feed = aFeed;
        recalcValues();
    }

    /**
     * Recalculates values.
     */
    public void recalcValues()
    {
        GlobalController controller = GlobalController.SINGLETON;
        final ScoresCalculator calc = controller.getScoreCalculator();
        final HighlightsCalculator highCalc = controller.getHighlightsCalculator();

        starScore = calc.calcFinalScore(feed);
        starsIcon = getStarzIcon(starScore, feed.getRating() == -1);

        goodArticles = highCalc.getHighlightedArticles(feed);
    }

    /**
     * Returns icon corresponding to a given starz score.
     *
     * @param starz     score in range [0:4].
     * @param silver    TRUE if silver starz icon required.
     *
     * @return icon.
     */
    public static Icon getStarzIcon(int starz, boolean silver)
    {
        if (starz < 0 || starz > 4)
        {
            LOG.severe(MessageFormat.format(Strings.error("starz.rating.out.of.range"), new Object[] { new Integer(starz) }));
            Thread.dumpStack();
            starz = Math.max(0, Math.min(4, starz));
        }

        if (silver) starz += 5;
        return STARS_ICONS[starz];
    }

    /**
     * Returns stars-icon.
     *
     * @return stars-icon.
     */
    public Icon getStarsIcon()
    {
        return starsIcon;
    }

    /**
     * Returns the icon to paint when channel is loading.
     *
     * @param frame frame.
     *
     * @return loading icon.
     */
    public static Icon getLoadingIcon(int frame)
    {
        return LOADING_ICONS[frame];
    }

    /**
     * Returns number of frames in loading icon.
     *
     * @return loading icon.
     */
    public static int getLoadingIconFrames()
    {
        return LOADING_ICONS_NAME.length;
    }

    /**
     * Returns text with counters.
     *
     * @return counters text.
     */
    public String getCountsText()
    {
        return feed.getUnreadArticlesCount() + "/" + feed.getArticlesCount();
    }

    /**
     * Returns the name of file having star icon with path relative to root.
     *
     * @param rating    rating.
     * @param silver    is silver.
     *
     * @return filename with relative path.
     */
    public static String getStarzFileName(int rating, boolean silver)
    {
        if (silver) rating += 5;
        return ResourceUtils.getString(STARS_ICONS_NAMES[rating]);
    }

    /**
     * Returns number of articles with keywords as text.
     *
     * @return keywords count string.
     */
    public String getKeywordsText()
    {
        return Integer.toString(goodArticles);
    }
}