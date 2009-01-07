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
// $Id: TestAbstractFeedEvents.java,v 1.4 2007/08/29 14:29:53 spyromus Exp $
//

package com.salas.bb.domain;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This suite contains the tests for <code>AbstractFeed</code> class which
 * are targeted on checking of correct event firing and handling.
 */
public class TestAbstractFeedEvents extends MockObjectTestCase
{
    private Mock listener;
    private AbstractFeed feed;
    private Level oldLevel;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        listener = new Mock(IFeedListener.class);

        feed = new DummyNetworkFeed();
        feed.addListener((IFeedListener)listener.proxy());
    }

    /**
     * Tests that events for identical values aren't fired.
     */
    public void testSkipIdenticalPropertyValues()
    {
        feed.firePropertyChanged("test", null, null);
        feed.firePropertyChanged("test", "a", "a");

        listener.verify();
    }

    /**
     * Tests change in processing property when processing starts and finishes.
     */
    public void testProcessingPC()
    {
        listener.expects(once()).method("propertyChanged").with(same(feed),
            eq(AbstractFeed.PROP_PROCESSING), eq(false), eq(true)).id("started");
        listener.expects(once()).method("propertyChanged").with(same(feed),
            eq(AbstractFeed.PROP_PROCESSING), eq(true), eq(false)).after("started").id("finished");
        listener.expects(once()).method("propertyChanged").with(same(feed),
            eq(AbstractFeed.PROP_PROCESSING), eq(true), eq(false)).after("finished");

        feed.processingStarted();
        feed.processingStarted();
        feed.processingFinished();
        feed.processingFinished();

        disableLogging();
        feed.processingFinished();
        enableLogging();

        listener.verify();
    }

    private void enableLogging()
    {
        Logger logger = Logger.getLogger(AbstractFeed.class.getName());
        logger.setLevel(oldLevel);
    }

    private void disableLogging()
    {
        Logger logger = Logger.getLogger(AbstractFeed.class.getName());
        oldLevel = logger.getLevel();
        logger.setLevel(Level.OFF);
    }

    /**
     * Tests firing propery change events on invalidness reasons changes.
     */
    public void testInvalidnessReasonPC()
    {
        listener.expects(once()).method("propertyChanged").with(same(feed),
            eq(AbstractFeed.PROP_INVALIDNESS_REASON), NULL, eq("r1")).id("1");
        listener.expects(once()).method("propertyChanged").with(same(feed),
            eq(AbstractFeed.PROP_INVALIDNESS_REASON), eq("r1"), eq("r2")).after("1").id("2");
        listener.expects(once()).method("propertyChanged").with(same(feed),
            eq(AbstractFeed.PROP_INVALIDNESS_REASON), eq("r2"), NULL).after("2");

        feed.setInvalidnessReason("r1");
        feed.setInvalidnessReason("r2");
        feed.setInvalidnessReason(null);

        listener.verify();
    }
}
