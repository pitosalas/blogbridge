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
// $Id: TestGuideModel.java,v 1.11 2008/02/28 15:59:50 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.domain.*;
import junit.framework.TestCase;

/**
 * This suite contains tests for <code>GuideModel</code> unit.
 * It covers:
 * <ul>
 *  <li>Check for bug with moving feeds.</li>
 * </ul>
 */
public class TestGuideModel extends TestCase
{
    /**
     * <p>Tests the behaviour with removing and adding feeds in a sequence. The bug appeared
     * when doing D'n'D within the same guide. The feed has beed removed and then added
     * back resulting in IOOBE. The bug only appeared when sorting was off.</p>
     *
     * <p>The problem was in <code>copyFeedsList()</code> method's <code>!sort</code> branch
     * where the destination array has been created with the size being out of sync with
     * filtered array.</p>
     */
    public void testAddRemoveFeed()
    {
        ScoresCalculator calc = new ScoresCalculator();
        FeedDisplayModeManager feedDMM = new FeedDisplayModeManager()
        {
            public synchronized boolean isVisible(int channelClass)
            {
                return true;
            }
        };

        // Create and select some guide
        GuideModel model = new GuideModel(calc, false, feedDMM);
        model.setSortingEnabled(false);
        StandardGuide guide = new StandardGuide();
        model.setGuide(guide);

        // Add two feeds to the guide and notify model
        DirectFeed feed0 = new DirectFeed();
        guide.add(feed0);
        model.feedsAdded(0, 0);

        DirectFeed feed1 = new DirectFeed();
        guide.add(feed1);
        model.feedsAdded(1, 1);

        // Remove the last feed and add it back on the first place
        guide.remove(feed1);
        model.fullRebuild();
        guide.add(feed1);
        model.feedsAdded(1, 1);
    }

    /**
     * Tests shifting in invalidness bit.
     */
    public void testShiftInSortMaskInvalidness()
    {
        checkShift(FeedClass.INVALID, FeedsSortOrder.INVALIDNESS);
    }

    /**
     * Tests shifting in rating bit.
     */
    public void testShiftInSortMaskRating()
    {
        CustomScoreCalculationsModel model = new CustomScoreCalculationsModel();
        DirectFeed feed = new DirectFeed();

        int minVisits = 0;
        int score = model.shiftInSortMark(1, 0, feed, FeedsSortOrder.RATING, false, minVisits);
        assertEquals((1 << 3) | 7, score);

        feed.setRating(4);
        score = model.shiftInSortMark(1, 0, feed, FeedsSortOrder.RATING, true, minVisits);
        assertEquals((1 << 3) | 4, score);
    }

    /**
     * Tests shifting in readness bit.
     */
    public void testShiftInSortMaskRead()
    {
        checkShift(FeedClass.READ, FeedsSortOrder.READ);
    }

    /**
     * Tests shifting in undiscoveredness bit.
     */
    public void testShiftInSortMaskUndiscovered()
    {
        checkShift(FeedClass.UNDISCOVERED, FeedsSortOrder.INVALIDNESS);
    }

    /**
     * Tests shifting in alphabetical order.
     */
    public void testShiftInSortMaskAlphabetical()
    {
        DirectFeed feed = new DirectFeed();

        // We put the alpha order of this feed in ID to emulate the reporting
        // of its actual position by parent guide (which is missing in this test)
        feed.setID(1023);

        int score;
        CustomScoreCalculationsModel model = new CustomScoreCalculationsModel();

        int minVisits = 0;
        score = model.shiftInSortMark(1, 0, feed, FeedsSortOrder.ALPHABETICAL, false, minVisits);
        assertEquals((1 << 10) | 1023, score);

        score = model.shiftInSortMark(1, 0, feed, FeedsSortOrder.ALPHABETICAL, true, minVisits);
        assertEquals((1 << 10) | 0, score);
    }

    /**
     * Tests calculation of complex score.
     */
    public void testCalculateScore()
    {
        DirectFeed feed = new DirectFeed();

        // We put the alpha order of this feed in ID to emulate the reporting
        // of its actual position by parent guide (which is missing in this test)
        feed.setID(1023);
        feed.setRating(3);
        feed.setRead(false);

        int targetScore;
        int feedClass;
        int score;

        CustomScoreCalculationsModel model = new CustomScoreCalculationsModel();
        model.setPrimarySortOrder(FeedsSortOrder.ALPHABETICAL);
        model.setSecondarySortOrder(FeedsSortOrder.RATING);
        feedClass = FeedClass.LOW_RATED;

        // 10bits-alphaOrder : 3bit-lowHighRating : 3bit-ratingValue
        // 1023              : 1 (low rated)      : 1 (4-3 for more starz to go first)
        targetScore = ((((1023 << 3) | 4) << 3) | 1);
        score = model.calculateScore(feed, feedClass, 0);
        assertEquals("Wrong calculation of score.", targetScore, score);

        // Sort by visits (less to more) and read flag (read on top)
        // Ratings and alpha sorting should be automatically added
        model.setPrimarySortOrder(FeedsSortOrder.VISITS);
        model.setPrimarySortOrderDirection(true);
        model.setSecondarySortOrder(FeedsSortOrder.READ);
        feedClass = 0;

        // Read Flag (1 bit) : Rating (3 bit) : Alpha Order (10 bit)
        // 0 (unread)        : 1 (4-3)        : 1023
        targetScore = (((0 << 3) | 1) << 10) | 1023;
        score = model.calculateScore(feed, feedClass, 0);
        assertEquals("Wrong calculation of score.", targetScore, score);
    }

    /** Tests handling simple situations without NPE. */
    public void testEnsureVisibilityOf()
    {
        GuideModel model = new GuideModel(null, false, null);
        model.ensureVisibilityOf(new DirectFeed());
        model.ensureVisibilityOf(null);
    }

    /** Verifies that the given combination of feedClass and sortOrder produces right bit shift. */
    private void checkShift(int feedClass, int sortOrder)
    {
        int score;
        CustomScoreCalculationsModel model = new CustomScoreCalculationsModel();
        DirectFeed feed = new DirectFeed();

        int minVisits = 0;
        score = model.shiftInSortMark(1, 0, feed, sortOrder, false, minVisits);
        assertEquals((1 << 1) | 0, score);

        score = model.shiftInSortMark(1, feedClass, feed, sortOrder, false, minVisits);
        assertEquals((1 << 1) | 1, score);

        score = model.shiftInSortMark(1, feedClass, feed, sortOrder, true, minVisits);
        assertEquals((1 << 1) | 0, score);
    }

    /**
     * Model which overriden calls to calculators. Feed keywords count is static.
     * Feed rating is always taken from feed setting. Feed alpha order is taken from
     * <code>ID</code> field (yes, it's hack for exactly this implementation to
     * avoid calling external modules).
     */
    private static class CustomScoreCalculationsModel extends GuideModel
    {
        private int feedKeywords;

        /** Constructs the model. */
        public CustomScoreCalculationsModel()
        {
            super(null, null);
        }

        /**
         * Returns rating of the feed for further math.
         *
         * @param feed feed.
         *
         * @return rating in range [0;4].
         */
        int getFeedRating(IFeed feed)
        {
            return feed.getRating();
        }

        /**
         * Returns alphabetical order index of the feed within the currently selected guide.
         *
         * @param feed feed.
         *
         * @return order index in range [0;1023].
         */
        int getFeedAlphaOrder(IFeed feed)
        {
            return (int)feed.getID();
        }
    }
}
