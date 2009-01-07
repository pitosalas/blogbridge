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
// $Id: TestDeliciousQueryType.java,v 1.1 2006/11/15 11:50:23 spyromus Exp $
//

package com.salas.bb.domain.querytypes;

import junit.framework.TestCase;

import java.net.URL;
import java.net.MalformedURLException;

import com.salas.bb.domain.querytypes.QueryType;

/**
 * Tests some aspects of query types.
 */
public class TestDeliciousQueryType extends TestCase
{
    private DeliciousQueryType qt;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        qt = new DeliciousQueryType();
    }

    /** Tests handling empty parameters. */
    public void testUserTagsToURL_Empty()
    {
        assertNull(qt.formURLString(null, new String[0], 0));
        assertNull(qt.formURLString("", new String[0], 0));
    }

    /** User specified. */
    public void testUserTagsToURL_User()
    {
        assertEquals("http://del.icio.us/rss/jack",
            qt.formURLString("", new String[] { "jack" }, 0));
    }

    /** Tags specified. */
    public void testUserTagsToURL_Tags()
    {
        assertEquals("http://del.icio.us/rss/tag/jack+ripper",
            qt.formURLString("jack ripper", new String[0], 0));
    }

    /** User and tags specified. */
    public void testUserTagsToURL_UserTags()
    {
        assertEquals("http://del.icio.us/rss/jack/the+ripper",
            qt.formURLString("the ripper", new String[] { "jack" }, 0));
    }

    public void testConvertToURL_User()
        throws MalformedURLException
    {
        URL url = new URL("http://del.icio.us/rss/jack");

        assertEquals(url, qt.convertToURL("[jack]", 1));
        assertEquals(url, qt.convertToURL(" [jack] ", 1));
        assertEquals(url, qt.convertToURL(" [ jack ] ", 1));
        assertEquals(url, qt.convertToURL(" [ jack ] [mack]", 1));
    }

    public void testConvertToURL_Tags()
        throws MalformedURLException
    {
        URL url1 = new URL("http://del.icio.us/rss/tag/jack");
        URL url2 = new URL("http://del.icio.us/rss/tag/jack+mack");

        assertEquals(url1, qt.convertToURL("jack", 1));
        assertEquals(url1, qt.convertToURL(" jack ", 1));
        assertEquals(url2, qt.convertToURL(" jack mack ", 1));
    }
    
    public void testConvertToURL_UserTags()
        throws MalformedURLException
    {
        URL url1 = new URL("http://del.icio.us/rss/jack/stack");
        URL url2 = new URL("http://del.icio.us/rss/jack/high+stack");

        assertEquals(url1, qt.convertToURL("stack [jack]", 1));
        assertEquals(url1, qt.convertToURL("[jack] stack", 1));
        assertEquals(url2, qt.convertToURL("high stack [jack]", 1));
    }
}
