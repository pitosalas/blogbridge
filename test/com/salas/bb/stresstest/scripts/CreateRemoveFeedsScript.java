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
// $Id: CreateRemoveFeedsScript.java,v 1.3 2006/01/08 05:28:17 kyank Exp $
//

package com.salas.bb.stresstest.scripts;

/**
 * This script simply creates <code>cfgGuides</code> number of guides and
 * <code>cfgFeedsPerGuide</code> number of feeds in each. Each feed has
 * <code>cfgArticlesPerFeed</code> number of articles in it and each feed is
 * generated from <code>cfgUrlTemplate</code>.
 */
public class CreateRemoveFeedsScript extends AbstractStressScript
{
    private static final String KEY_GUIDES          = "stress.crfeeds.guides";
    private static final String KEY_FEEDS           = "stress.crfeeds.feeds";
    private static final String KEY_ARTICLES        = "stress.crfeeds.articles";
    private static final String KEY_URL_TEMPLATE    = "stress.crfeeds.feed.url.template";

    private static final int DEF_GUIDES             = 5;
    private static final int DEF_FEEDS              = 5;
    private static final int DEF_ARTICLES           = 10;

    private int             cfgGuides;
    private int             cfgFeedsPerGuide;
    private int             cfgArticlesPerFeed;
    private String          cfgUrlTemplate;

    /** Creates script. */
    public CreateRemoveFeedsScript()
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
        boolean waitForInit = loop % 2 == 0;

        createGuides(cfgGuides);
        populateGuides(cfgFeedsPerGuide, cfgArticlesPerFeed, cfgUrlTemplate, waitForInit);
        removeGuides();
    }
}
