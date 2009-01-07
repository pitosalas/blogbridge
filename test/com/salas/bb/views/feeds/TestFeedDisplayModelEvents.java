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
// $Id: TestFeedDisplayModelEvents.java,v 1.3 2006/01/08 05:28:34 kyank Exp $
//

package com.salas.bb.views.feeds;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import com.salas.bb.domain.*;

import javax.swing.*;
import java.util.Date;
import java.lang.reflect.InvocationTargetException;

/**
 * Testing of feed view model events.
 */
public class TestFeedDisplayModelEvents extends MockObjectTestCase
{
    private Mock            listener;
    private DirectFeed      feed;
    private StandardArticle article;
    private FeedDisplayModel   model;

    /**
     * Initializing the tests.
     */
    protected void setUp()
        throws Exception
    {
        super.setUp();

        listener = new Mock(IFeedDisplayModelListener.class);

        feed = new DirectFeed();

        article = new StandardArticle("test");
        article.setPublicationDate(new Date());

        model = new FeedDisplayModel();
        model.setFeed(feed);
        model.addListener((IFeedDisplayModelListener)listener.proxy());
    }

    /**
     * Testing events when new article is added.
     */
    public void testAddingArticle()
    {
        int groupToday = 1;
        int positionInGroup = 0;

        listener.expects(once()).method("articleAdded").with(same(article),
            eq(groupToday), eq(positionInGroup));

        feed.appendArticle(article);

        postAndWait();

        listener.verify();
    }

    /**
     * Fire event to EDT and wait for execution finish. It's necessary to be
     * sure that all events, scheduled for execution at the moment, will execute
     * before we continue.
     */
    private void postAndWait()
    {
        try
        {
            SwingUtilities.invokeAndWait(new Runnable() { public void run() { } });
        } catch (InterruptedException e)
        {
            e.printStackTrace();
            fail();
        } catch (InvocationTargetException e)
        {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Testing events when article is removed.
     */
    public void testRemovingArticle()
    {
        int groupToday = 1;
        int positionInGroup = 0;

        listener.expects(once()).method("articleAdded").with(same(article),
            eq(groupToday), eq(positionInGroup)).id("addition");
        listener.expects(once()).method("articleRemoved").with(same(article),
            eq(groupToday), eq(positionInGroup)).after("addition");

        feed.appendArticle(article);
        feed.setPurgeLimit(0);

        postAndWait();

        listener.verify();
    }

    /**
     * Tests changing feeds.
     */
    public void testChangingFeeds()
    {
        DirectFeed otherFeed = new DirectFeed();
        StandardArticle otherArticle = new StandardArticle("2");
        otherArticle.setPublicationDate(new Date());
        otherFeed.appendArticle(otherArticle);

        int groupToday = 1;
        int positionInGroup = 0;

        listener.expects(once()).method("articlesRemoved").id("cleanup");
        listener.expects(once()).method("articleAdded").with(same(otherArticle),
            eq(groupToday), eq(positionInGroup)).after("cleanup");

        model.setFeed(otherFeed);

        postAndWait();
        
        listener.verify();
    }
}
