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
// $Id: RealWorldScript.java,v 1.5 2006/01/08 05:28:17 kyank Exp $
//

package com.salas.bb.stresstest.scripts;

import com.salas.bb.domain.DataFeed;
import com.salas.bb.domain.IGuide;

/**
 * Initialization:
 * 1. Create guides
 * 2. Create feeds
 *
 * The script:
 * 1. Walk through all feeds and mark them as read by walking through unread articles.
 * 2. Update random N feeds with random number (from 1 to M) of articles
 * 3. "Read" them
 * 4. Exchange 2 random feeds in 2 random guides.
 */
public class RealWorldScript extends AbstractStressScript
{
    private static final String KEY_GUIDES          = "stress.realworld.guides";
    private static final String KEY_FEEDS           = "stress.realworld.feeds";
    private static final String KEY_ARTICLES        = "stress.realworld.articles";
    private static final String KEY_URL_TEMPLATE    = "stress.realworld.feed.url.template";
    private static final String KEY_CLEANUP_PERIOD  = "stress.realworld.cleanup.period";
    private static final String KEY_CLEANUP_THRESH  = "stress.realworld.cleanup.threshold";
    private static final String KEY_UPDATE_FEEDS    = "stress.realworld.update.feeds";

    private static final int DEF_GUIDES             = 10;
    private static final int DEF_FEEDS              = 10;
    private static final int DEF_ARTICLES           = 3;
    private static final int DEF_CLEANUP_PERIOD     = 1;
    private static final int DEF_CLEANUP_THRESH     = 10;
    private static final int DEF_UPDATE_FEEDS       = 5;

    private int             cfgGuides;
    private int             cfgFeedsPerGuide;
    private int             cfgArticlesPerFeed;
    private String          cfgUrlTemplate;
    private int             cfgCleanupPeriod;
    private int             cfgCleanupThresh;
    private int             cfgUpdateFeeds;

    /** Creates script. */
    public RealWorldScript()
    {
        cfgGuides = getIntSystemProperty(KEY_GUIDES, DEF_GUIDES);
        cfgFeedsPerGuide = getIntSystemProperty(KEY_FEEDS, DEF_FEEDS);
        cfgArticlesPerFeed = getIntSystemProperty(KEY_ARTICLES, DEF_ARTICLES);
        cfgUrlTemplate = getSystemProperty(KEY_URL_TEMPLATE, null);
        cfgCleanupPeriod = getIntSystemProperty(KEY_CLEANUP_PERIOD, DEF_CLEANUP_PERIOD);
        cfgCleanupThresh = getIntSystemProperty(KEY_CLEANUP_THRESH, DEF_CLEANUP_THRESH);
        cfgUpdateFeeds = getIntSystemProperty(KEY_UPDATE_FEEDS, DEF_UPDATE_FEEDS);
    }

    /**
     * Pre-run initialization.
     */
    public void childInit()
    {
        setCleanupParameters(cfgCleanupPeriod, cfgCleanupThresh);

        createGuides(cfgGuides);
        populateGuides(cfgFeedsPerGuide, cfgArticlesPerFeed, cfgUrlTemplate, true);
    }

    /**
     * Makes single test run.
     *
     * @param loop loop number.
     *
     * @throws Exception in error case
     */
    public void run(int loop) throws Exception
    {
        updateRandomFeeds();
        simulateForwardReadingOfAllArticles();
        updateRandomFeeds();
        simulateBackwardReadingOfAllArticles();
        exchangeRandomFeeds();

        Thread.sleep(20000);
    }

    private void updateRandomFeeds()
    {
        DataFeed[] feeds = new DataFeed[cfgUpdateFeeds];
        for (int i = 0; i < cfgUpdateFeeds; i++)
        {
            int guideIndex = (int)(Math.random() * cfgGuides);
            int feedIndex = (int)(Math.random() * cfgFeedsPerGuide);

            IGuide guide = getGuidesSet().getGuideAt(guideIndex);
            DataFeed feed = (DataFeed)guide.getFeedAt(feedIndex);
            feeds[i] = feed;

            feed.setInitTime(-1);
            feed.setInvalidnessReason("Test");
            feed.update();
        }

        // Wait while they are loading
        for (int i = 0; i < feeds.length; i++) waitForInitialization(feeds[i]);
    }

    private void exchangeRandomFeeds()
    {
    }
}
