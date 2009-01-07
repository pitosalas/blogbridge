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
// $Id: GotoScript.java,v 1.5 2006/01/08 05:28:17 kyank Exp $
//

package com.salas.bb.stresstest.scripts;

/**
 * Test how goto next / prev unread commands work.
 */
public class GotoScript extends AbstractStressScript
{
    private static final String KEY_GUIDES          = "stress.goto.guides";
    private static final String KEY_FEEDS           = "stress.goto.feeds";
    private static final String KEY_ARTICLES        = "stress.goto.articles";
    private static final String KEY_URL_TEMPLATE    = "stress.goto.feed.url.template";

    private static final int DEF_GUIDES             = 5;
    private static final int DEF_FEEDS              = 5;
    private static final int DEF_ARTICLES           = 10;

    private int             cfgGuides;
    private int             cfgFeedsPerGuide;
    private int             cfgArticlesPerFeed;
    private String          cfgUrlTemplate;

    /** Creates script. */
    public GotoScript()
    {
        cfgGuides = getIntSystemProperty(KEY_GUIDES, DEF_GUIDES);
        cfgFeedsPerGuide = getIntSystemProperty(KEY_FEEDS, DEF_FEEDS);
        cfgArticlesPerFeed = getIntSystemProperty(KEY_ARTICLES, DEF_ARTICLES);
        cfgUrlTemplate = getSystemProperty(KEY_URL_TEMPLATE, null);
    }

    /**
     * Pre-run initialization.
     */
    public void childInit()
    {
        setCleanupParameters(60, cfgArticlesPerFeed + 1);
        getUserPreferences().setMarkReadWhenChangingChannels(false);
        getUserPreferences().setMarkReadWhenChangingGuides(false);
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
        createGuides(cfgGuides);
        populateGuides(cfgFeedsPerGuide, cfgArticlesPerFeed, cfgUrlTemplate, false);

        gotoNextUnreadStress();
        gotoPrevUnreadStress();

        removeGuides();
    }

    // * select first guide, first feed, first article
    // * quickly post all necessary "SPACE" key event to walk through all of the feeds
    // * check if all feeds are completely read
    private void gotoNextUnreadStress()
    {
        markAllGuidesRead(false);
        simulateForwardReadingOfAllArticles();
        checkIfAllGuidesRead();
    }

    // * select last guide, last feed, last article
    // * quickly post all necessary "SHIFT-SPACE" key event to walk through all of the feeds
    // * check if all feeds are completely read
    private void gotoPrevUnreadStress()
    {
        markAllGuidesRead(false);
        simulateBackwardReadingOfAllArticles();
        checkIfAllGuidesRead();
    }
}
