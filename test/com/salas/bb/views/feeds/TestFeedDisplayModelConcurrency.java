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
// $Id: TestFeedDisplayModelConcurrency.java,v 1.1 2007/06/04 11:49:06 spyromus Exp $
//

package com.salas.bb.views.feeds;

import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.StandardArticle;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

/**
 * This suite contains tests for {@link com.salas.bb.views.feeds.FeedDisplayModel} unit.
 */
public class TestFeedDisplayModelConcurrency
        extends AbstractFeedDisplayTestCase
{
    private FeedDisplayModel    model;
    private DirectFeed          feed1;
    private DirectFeed          feed2;
    private StandardArticle article;

    /** Initializes the tests. */
    protected void setUp()
        throws Exception
    {
        super.setUp();

        model = new FeedDisplayModel();
        feed1 = new DirectFeed();
        feed2 = new DirectFeed();

        article = createArticle(0);
    }

    public void testConcurrency()
            throws InvocationTargetException, InterruptedException
    {
        // Set a feed
        model.setFeed(feed1);

        // Now load the EDT with some task
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    Thread.sleep(500); // should be enough to add an article before the setFeed starts processing
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        });

        // Change the feed from EDT
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                model.setFeed(feed2);
            }
        });

        // Add an article to feed1 from non-EDT thread
        feed1.appendArticle(article);

        // After this the EDT event queue should look like this:
        // * Long task -- being executed
        // * setFeed(feed2)
        // * onArticleAdded(article)

        // Join the EDT thread and check the model
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                // Join the EDT thread
            }
        });

        // The model should be empty
        assertEquals("The onArticleAdded event should be discarded as it was delivered for the wrong feed.",
                0, model.getArticlesCount());
    }
}