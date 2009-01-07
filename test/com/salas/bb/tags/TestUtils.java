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
// $Id: TestUtils.java,v 1.4 2007/08/29 14:29:53 spyromus Exp $
//

package com.salas.bb.tags;

import junit.framework.TestCase;

/**
 * This suite contains tests for <code>Utils</code> unit.
 */
public class TestUtils extends TestCase
{
    /**
     * Tests converting the tags lists into summary.
     */
    public void testCreateTagsSummary()
    {
        String[] tags = null;

        assertNull(TagsUtils.createTagsSummary(tags));

        tags = new String[0];
        assertEquals("", TagsUtils.createTagsSummary(tags));

        tags = new String[] { "d", "A", "c", "B", "a" };
        assertEquals("a (2), b, c, d", TagsUtils.createTagsSummary(tags));
    }

    /**
     * Sleep a little bit.
     */
    public static void sleepABit()
    {
        try
        {
            Thread.sleep(5);
        } catch (InterruptedException e)
        {
            // Fall through
        }
    }
}
