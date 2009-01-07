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
// $Id: TestQueryFeed.java,v 1.4 2006/11/15 11:50:22 spyromus Exp $
//

package com.salas.bb.domain;

import junit.framework.TestCase;
import com.salas.bb.domain.querytypes.QueryType;

/**
 * This suite contains tests for <code>QueryFeed</code> unit.
 * It covers:
 * <ul>
 *  <li>Processing of <code>toString()</code> and getting XML URL from default feed.</li>
 *  <li>Working with invalidness reason.</li>
 * </ul>
 */
public class TestQueryFeed extends TestCase
{
    private QueryFeed feed;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        feed = new QueryFeed();
    }

    /**
     * Tests default faultless processing of <code>toString()</code>.
     */
    public void testDefaultToString()
    {
        assertNotNull(feed.toString());
    }

    /**
     * Tests returning default NULL-URL.
     */
    public void testDefaultGetXMLURL()
    {
        assertNull(feed.getXmlURL());
    }

    /**
     * Tests that the feed isn't updatable by default.
     */
    public void testDefaultIsUpdatable()
    {
        assertFalse(feed.isUpdatable(true));
        assertFalse(feed.isUpdatable(false));
    }

    /**
     * Tests the presence of invalidness reason when QueryType isn't set.
     */
    public void testInvalidnessReasonWithoutQueryType()
    {
        assertEquals("Query is not supported.", feed.getInvalidnessReason());

        feed.setInvalidnessReason(null);
        assertEquals("Cannot reset the reason if query object still not set.",
            "Query is not supported.", feed.getInvalidnessReason());

        int anyType = QueryType.TYPE_DELICIOUS;
        feed.setQueryType(QueryType.getQueryType(anyType));
        assertNull("Invalidness reason should go away.", feed.getInvalidnessReason());
    }
}
