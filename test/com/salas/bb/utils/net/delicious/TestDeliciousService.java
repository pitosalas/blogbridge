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
// $Id: TestDeliciousService.java,v 1.2 2006/01/08 05:28:18 kyank Exp $
//

package com.salas.bb.utils.net.delicious;

import junit.framework.TestCase;

import java.net.URL;
import java.io.IOException;
import java.util.Arrays;

/**
 * This suite contains tests for <code>DeliciousService</code> unit.
 */
public class TestDeliciousService extends TestCase
{
    private static final String USERNAME        = "bbtest";
    private static final String PASSWORD        = "testbb";

    private static final String testURL         = "http://www.testsite.com/";
    private static final String[] testTags      = { "site", "test" };
    private static final String testDescr       = "Description";
    private static final String testExtended    = "Extended";

    private URL link;

    public TestDeliciousService()
        throws IOException
    {
        link = new URL(testURL);
    }

    private void setupTestTags()
        throws IOException
    {
        assertTrue("Failed to tag test link.",
            DeliciousService.tagLink(link, USERNAME, PASSWORD, testTags,
                testDescr, testExtended));
    }

    private void tearDownTestTags()
        throws IOException
    {
        assertTrue("Failed to untag test link.",
            DeliciousService.untagLink(link, USERNAME, PASSWORD));
    }

    /**
     * Tests untagging of test link.
     */
    public void testUntagLink()
        throws IOException
    {
        setupTestTags();

        tearDownTestTags();

        String[] userTags = DeliciousService.getUserTags(USERNAME, PASSWORD);
        assertEquals("All tags were removed.", 0, userTags.length);

        DeliciousTags[] linkTags = DeliciousService.getLinkTags(link);
        if (linkTags != null)
        {
            assertNull("There should be no our tags",
                DeliciousTags.findTagsByUser(linkTags, USERNAME));
        }
    }

    /**
     * Tests getting tags from the link.
     */
    public void testGetLinkTags()
        throws IOException
    {
        setupTestTags();

        try
        {
            DeliciousTags[] linkTags = DeliciousService.getLinkTags(link);
            assertTrue("There should be at least our tags.",
                linkTags.length >= 1);

            String[] tagsByUser = DeliciousTags.findTagsByUser(linkTags, USERNAME);
            assertNotNull("There should be our user tags present.", tagsByUser);
            assertTrue("Wrong set of tags.", Arrays.equals(testTags, tagsByUser));
        } finally
        {
            tearDownTestTags();
        }
    }

    /**
     * Tests getting tags used by user.
     */
    public void testGetUserTags()
        throws IOException
    {
        setupTestTags();
        
        try
        {
            String[] userTags = DeliciousService.getUserTags(USERNAME, PASSWORD);
            assertTrue("This user should have only tags from above tagging operation.",
                Arrays.equals(testTags, userTags));
        } finally
        {
            tearDownTestTags();
        }
    }

    /**
     * Tests the conversion of potential delicious response to list of tags.
     */
    public void testResponseToTags()
    {
        assertEquals(0, DeliciousService.responseToTags(null).length);
        assertEquals(0, DeliciousService.responseToTags("").length);

        String response = "<?xml version='1.0' standalone='yes'?>\n" +
            "<tags>\n" +
            "  <tag count=\"1\" tag=\"abc\" />\n" +
            "  <tag count=\"1\" tag=\"b\" />\n" +
            "\n" +
            "  <tag count=\"1\" tag=\"dfg\" />\n" +
            "</tags>";
        assertTrue(Arrays.equals(new String[] { "abc", "b", "dfg" },
            DeliciousService.responseToTags(response)));
    }
}
