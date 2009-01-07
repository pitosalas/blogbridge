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
// $Id: TestAbstractMicrotagQueryType.java,v 1.1 2006/11/15 11:50:23 spyromus Exp $
//

package com.salas.bb.domain.querytypes;

import junit.framework.TestCase;

/**
 * Tests abstract microtag query type.
 */
public class TestAbstractMicrotagQueryType extends TestCase
{
    // --- stripMicroTags -----------------------------------------------------

    /** Tests handling empty string. */
    public void testStripMicroTags_Empty()
    {
        assertEquals("", AbstractMicrotagQueryType.stripMicroTags(""));
    }

    /** Tests removing nothing. */
    public void testStripMicroTags_None()
    {
        assertEquals("a b c", AbstractMicrotagQueryType.stripMicroTags(" a b c "));
    }

    /** Tests removing single tag. */
    public void testStripMicroTags_Single()
    {
        assertEquals("a b", AbstractMicrotagQueryType.stripMicroTags("a [all] b"));
    }

    /** Tests removing multiple tags and collapsing the spaces. */
    public void testStripMicroTags_Multiple()
    {
        assertEquals("a b", AbstractMicrotagQueryType.stripMicroTags("[a] a [b] b[c]"));
    }

    // --- getMicroTags -------------------------------------------------------

    /** Parsing empty string. */
    public void testGetMicroTags_Empty()
    {
        assertEquals(0, AbstractMicrotagQueryType.getMicroTags("").length);
    }

    /** Parsing no-microtags-string. */
    public void testGetMicroTags_None()
    {
        assertEquals(0, AbstractMicrotagQueryType.getMicroTags("a b c").length);
    }

    /** Picking single microtag. */
    public void testGetMicroTags_Single()
    {
        String[] q = { "a [all] b", "[all] b", "a [all]", "a[ all ]"};
        for (int i = 0; i < q.length; i++)
        {
            String[] tags = AbstractMicrotagQueryType.getMicroTags(q[i]);
            assertEquals(1, tags.length);
            assertEquals("all", tags[0]);
        }
    }

    /** Picking multiple microtags. */
    public void testGetMicroTags_Multiple()
    {
        String[] q = { "[a][b]", "[a] [b]", " [a] [ b ]", "a [a] b [b] c"};
        for (int i = 0; i < q.length; i++)
        {
            String[] tags = AbstractMicrotagQueryType.getMicroTags(q[i]);
            assertEquals(2, tags.length);
            assertEquals("a", tags[0]);
            assertEquals("b", tags[1]);
        }
    }
}
