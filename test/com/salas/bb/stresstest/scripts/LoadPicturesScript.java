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
// $Id: LoadPicturesScript.java,v 1.4 2006/01/27 15:23:24 spyromus Exp $
//

package com.salas.bb.stresstest.scripts;

import com.salas.bb.domain.IGuide;

/**
 * Tests how many pictures we can load before OOM.
 * Each feed has X articles with 1 picture in each.
 */
public class LoadPicturesScript extends AbstractStressScript
{
    private static final String KEY_ARTICLES        = "stress.loadpic.articles";
    private static final String KEY_URL_TEMPLATE    = "stress.loadpic.feed.url.template";

    private static final int DEF_ARTICLES           = 10;

    private int             cfgArticlesPerFeed;
    private String          cfgUrlTemplate;

    /** Creates script. */
    public LoadPicturesScript()
    {
        cfgArticlesPerFeed = getIntSystemProperty(KEY_ARTICLES, DEF_ARTICLES);
        cfgUrlTemplate = getSystemProperty(KEY_URL_TEMPLATE, null);
    }

    /**
     * Pre-run initialization.
     */
    public void childInit()
    {
        setCleanupParameters(60, cfgArticlesPerFeed + 1);
        createGuides(1);
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
        IGuide guide = getGuidesSet().getGuideAt(0);
        getController().selectGuideAndFeed(guide);

        // Subscribe to feed
        populateGuide(guide, loop, 1, cfgArticlesPerFeed, cfgUrlTemplate, true);

        getController().selectFeed(guide.getFeedAt(guide.getFeedsCount() - 1));
        waitForPendingEvents();

        // Wait a while to load pictures
        Thread.sleep(3000);
    }
}
