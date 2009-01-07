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
// $Id: TestDirectFeedEvents.java,v 1.8 2007/08/29 14:29:53 spyromus Exp $
//

package com.salas.bb.domain;

import com.salas.bb.tags.TestUtils;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import java.net.URL;

/**
 * This suite contains the tests for <code>DirectFeed</code> class which
 * are targeted on checking of correct event firing and handling.
 */
public class TestDirectFeedEvents extends MockObjectTestCase
{
    private static URL testURL1;
    private static URL testURL2;

    private Mock listener;
    private DirectFeed feed;
    private static final String METHOD_PROPERTY_CHANGED = "propertyChanged";

    protected void setUp()
        throws Exception
    {
        super.setUp();

        listener = new Mock(IFeedListener.class);

        feed = new DirectFeed();
        feed.addListener((IFeedListener)listener.proxy());

        synchronized (TestDirectFeedEvents.class)
        {
            if (testURL1 == null) testURL1 = new URL("file://test1");
            if (testURL2 == null) testURL2 = new URL("file://test2");
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------------------------------

    /**
     * Tests firing change in XML URL property.
     * Tests avoing of duplicate events.
     */
    public void testXmlURLPC()
    {
        // Setting URL
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_XML_URL), NULL, eq(testURL1));

        feed.setXmlURL(testURL1);
        feed.setXmlURL(testURL1);

        // Reseting URL
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_XML_URL), eq(testURL1), NULL);

        feed.setXmlURL(null);
        feed.setXmlURL(null);

        listener.verify();
    }

    /**
     * Tests firing change in Rating property.
     * Tests avoing of duplicate events.
     */
    public void testRatingPC()
    {
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_RATING), eq(DirectFeed.RATING_NOT_SET), eq(DirectFeed.RATING_MIN));
        expectFirstChange();

        feed.setRating(DirectFeed.RATING_MIN);
        feed.setRating(DirectFeed.RATING_MIN);

        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_RATING), eq(DirectFeed.RATING_MIN), eq(DirectFeed.RATING_MAX));

        feed.setRating(DirectFeed.RATING_MAX);
        feed.setRating(DirectFeed.RATING_MAX);

        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_RATING), eq(DirectFeed.RATING_MAX), eq(DirectFeed.RATING_NOT_SET));

        feed.setRating(DirectFeed.RATING_NOT_SET);
        feed.setRating(DirectFeed.RATING_NOT_SET);

        listener.verify();
    }

    private void expectFirstChange()
    {
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_LAST_UPDATE_TIME), eq(-1l), not(eq(-1l)));
    }

    private void expectSecondChange()
    {
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_LAST_UPDATE_TIME), not(eq(-1l)), not(eq(-1l)));
    }

    /**
     * Tests firing change in Dead flag.
     * Tests avoing of duplicate events.
     */
    public void testDeadPC()
    {
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_DEAD), eq(Boolean.valueOf(DirectFeed.DEFAULT_DEAD)),
            eq(Boolean.valueOf(!DirectFeed.DEFAULT_DEAD)));

        feed.setDead(!DirectFeed.DEFAULT_DEAD);
        feed.setDead(!DirectFeed.DEFAULT_DEAD);

        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_DEAD), eq(Boolean.valueOf(!DirectFeed.DEFAULT_DEAD)),
            eq(Boolean.valueOf(DirectFeed.DEFAULT_DEAD)));

        feed.setDead(DirectFeed.DEFAULT_DEAD);
        feed.setDead(DirectFeed.DEFAULT_DEAD);

        listener.verify();
    }

    /**
     * Tests firing change in Title property on custom title setting.
     */
    public void testSetCustomTitle()
    {
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_CUSTOM_TITLE), NULL, eq("A"));
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_TITLE), NULL, eq("A"));
        expectFirstChange();

        feed.setCustomTitle("A");
        feed.setCustomTitle("A");

        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_CUSTOM_TITLE), eq("A"), NULL);
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_TITLE), eq("A"), NULL);

        feed.setCustomTitle(null);
        feed.setCustomTitle(null);

        listener.verify();
    }

    /**
     * Tests firing change in Description property on custom description setting.
     */
    public void testSetCustomDescription()
    {
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_CUSTOM_DESCRIPTION), NULL, eq("A"));
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_DESCRIPTION), NULL, eq("A"));
        expectFirstChange();

        feed.setCustomDescription("A");
        feed.setCustomDescription("A");

        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_CUSTOM_DESCRIPTION), eq("A"), NULL);
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_DESCRIPTION), eq("A"), NULL);

        feed.setCustomDescription(null);
        feed.setCustomDescription(null);

        listener.verify();
    }

    /**
     * Tests firing change in Author property on custom author setting.
     */
    public void testSetCustomAuthor()
    {
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_AUTHOR), NULL, eq("A"));
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_CUSTOM_AUTHOR), NULL, eq("A"));
        expectFirstChange();

        feed.setCustomAuthor("A");
        feed.setCustomAuthor("A");

        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_AUTHOR), eq("A"), NULL);
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_CUSTOM_AUTHOR), eq("A"), NULL);
        expectSecondChange();

        TestUtils.sleepABit();
        feed.setCustomAuthor(null);
        feed.setCustomAuthor(null);

        listener.verify();
    }

    /**
     * Tests setting base title.
     */
    public void testSetBaseTitle()
    {
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_BASE_TITLE), NULL, eq("A"));
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_TITLE), NULL, eq("A"));

        feed.setBaseTitle("A");
        feed.setBaseTitle("A");

        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_BASE_TITLE), eq("A"), NULL);
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_TITLE), eq("A"), NULL);

        feed.setBaseTitle(null);
        feed.setBaseTitle(null);

        listener.verify();
    }

    /**
     * Tests setting base title which the custom title is set -- no events for title property
     * change should be fired.
     */
    public void testSetBaseTitleNonOverriding()
    {
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_CUSTOM_TITLE), NULL, eq("B"));
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_TITLE), NULL, eq("B"));
        expectFirstChange();

        feed.setCustomTitle("B");

        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_BASE_TITLE), NULL, eq("A"));

        feed.setBaseTitle("A");

        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_BASE_TITLE), eq("A"), NULL);

        feed.setBaseTitle(null);

        listener.verify();
    }

    /**
     * Tests setting the base description.
     */
    public void testSetBaseDescription()
    {
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_BASE_DESCRIPTION), NULL, eq("A"));
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_DESCRIPTION), NULL, eq("A"));

        feed.setBaseDescription("A");
        feed.setBaseDescription("A");

        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_BASE_DESCRIPTION), eq("A"), NULL);
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_DESCRIPTION), eq("A"), NULL);

        feed.setBaseDescription(null);
        feed.setBaseDescription(null);

        listener.verify();
    }

    /**
     * Tests setting the base author.
     */
    public void testSetBaseAuthor()
    {
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_AUTHOR), NULL, eq("A"));
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_BASE_AUTHOR), NULL, eq("A"));

        feed.setBaseAuthor("A");
        feed.setBaseAuthor("A");

        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_AUTHOR), eq("A"), NULL);
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_BASE_AUTHOR), eq("A"), NULL);

        feed.setBaseAuthor(null);
        feed.setBaseAuthor(null);

        listener.verify();
    }

    /**
     * Tests setting the last meta-data update time directly at feed.
     */
    public void testChangeMetaDataUpdateTimeDirect()
    {
        long time = 1;
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_LAST_METADATA_UPDATE_TIME), eq(new Long(-1)), eq(new Long(time)));

        feed.setLastMetaDataUpdateTime(time);

        listener.verify();
    }

    /**
     * Tests setting the last meta-data update time at meta-data object associate with feed.
     */
    public void testChangeMetaDataUpdateTimeIndirect()
    {
        long time = 1;
        listener.expects(once()).method(METHOD_PROPERTY_CHANGED).with(same(feed),
            eq(DirectFeed.PROP_LAST_METADATA_UPDATE_TIME), eq(new Long(-1)), eq(new Long(time)));

        feed.getMetaDataHolder().setLastUpdateTime(time);

        listener.verify();
    }
}
