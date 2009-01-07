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
// $Id: TestDataFeedEvents.java,v 1.9 2007/08/29 14:29:53 spyromus Exp $
//

package com.salas.bb.domain;

import com.salas.bb.tags.TestUtils;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * This suite contains the tests for <code>DataFeed</code> class which
 * are targeted on checking of correct event firing and handling.
 */
public class TestDataFeedEvents extends MockObjectTestCase
{
    private Mock listener;
    private DummyDataFeed feed;
    private static final String METHOD_PROPERTY_CHANGED = "propertyChanged";

    protected void setUp()
        throws Exception
    {
        super.setUp();

        listener = new Mock(IFeedListener.class);

        feed = new DummyDataFeed();
        feed.addListener((IFeedListener)listener.proxy());
    }

    /**
     * Tests change in initTime property.
     */
    public void testInitTimePC()
    {
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DataFeed.PROP_INITIALIZED), eq(false), eq(true));

        feed.setInitTime(1);
        feed.setInitTime(1);

        listener.verify();
    }

    /**
     * Tests change in lastPollTime property.
     */
    public void testLastPollTimePC()
    {
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DataFeed.PROP_LAST_POLL_TIME), eq(0l), eq(1l));

        feed.setLastPollTime(1);
        feed.setLastPollTime(1);

        listener.verify();
    }

    /**
     * Tests change in retrievals property.
     */
    public void testRetrievalsPC()
    {
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DataFeed.PROP_RETRIEVALS), eq(0), eq(1));

        feed.setRetrievals(1);
        feed.setRetrievals(1);

        listener.verify();
    }

    /**
     * Tests change in purgeLimit property.
     */
    public void testPurgeLimitPC()
    {
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DataFeed.PROP_PURGE_LIMIT), eq(DataFeed.DEFAULT_PURGE_LIMIT), eq(1));
        expectUpdateTimeChange();

        // Property change event should be fired and clean() called as we lower the
        // purge limit
        feed.resetFlags();
        feed.setPurgeLimit(1);
        assertTrue("Clean should be called. DEFAULT -> 1", feed.isCleanCalled());

        // Propety change event should not be fired and clean() not called as the limit unchanged.
        feed.resetFlags();
        feed.setPurgeLimit(1);
        assertFalse("Clean should not be called. 1 -> 1", feed.isCleanCalled());

        listener.verify();

        // Property change event should be fired, but clean() should not be called as limit grew.
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DataFeed.PROP_PURGE_LIMIT), eq(1), eq(DataFeed.DEFAULT_PURGE_LIMIT));

        feed.resetFlags();
        feed.setPurgeLimit(DataFeed.PURGE_LIMIT_INHERITED);
        assertFalse("Clean should not be called. 1 -> DEFAULT", feed.isCleanCalled());
    }

    private void expectUpdateTimeChange()
    {
        listener.expects(once()).method("propertyChanged").with(same(feed),
            eq(DirectFeed.PROP_LAST_UPDATE_TIME), eq(-1l), not(eq(-1l)));
    }

    private void expectUpdateTimeChangeNotFirst()
    {
        listener.expects(once()).method("propertyChanged").with(same(feed),
            eq(DirectFeed.PROP_LAST_UPDATE_TIME), not(eq(-1l)), not(eq(-1l)));
    }

    /**
     * Tests change in update period property.
     */
    public void testUpdatePeriodPC()
    {
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DataFeed.PROP_UPDATE_PERIOD), eq(DataFeed.getGlobalUpdatePeriod()), eq(1l));
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DataFeed.PROP_LAST_UPDATE_TIME), eq(-1l), not(eq(-1l)));

        feed.setUpdatePeriod(1);
        feed.setUpdatePeriod(1);

        listener.verify();

        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DataFeed.PROP_UPDATE_PERIOD), eq(1l), eq(DataFeed.getGlobalUpdatePeriod()));
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DataFeed.PROP_LAST_UPDATE_TIME), not(eq(-1l)), not(eq(-1l)));

        TestUtils.sleepABit();
        feed.setUpdatePeriod(DataFeed.UPDATE_PERIOD_INHERITED);
    }
}
