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
// $Id: AddAndDeleteScript.java,v 1.4 2006/01/27 15:23:24 spyromus Exp $
//

package com.salas.bb.stresstest.scripts;

import com.salas.bb.domain.IGuide;

/**
 * Tests how the feeds are added and removed concurrently.
 */
public class AddAndDeleteScript extends AbstractStressScript
{
    private static final String KEY_URL_TEMPLATE    = "stress.addanddelete.feed.url.template";

    private String          cfgUrlTemplate;

    /** Creates script. */
    public AddAndDeleteScript()
    {
        cfgUrlTemplate = getSystemProperty(KEY_URL_TEMPLATE, null);
    }

    /**
     * Pre-run initialization.
     */
    public void childInit()
    {
        setCleanupParameters(60, 10);
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
        createGuides(10);

        for (int i = 0; i < 10; i++)
        {
            IGuide guide = getGuidesSet().getGuideAt(0);
            getController().selectGuideAndFeed(guide);

            // Subscribe to feeds
            populateGuide(guide, loop, 10, 10, cfgUrlTemplate, false);

            Thread.sleep(1000);

            removeGuide(guide);
        }

        Thread.sleep(10000);
    }
}
