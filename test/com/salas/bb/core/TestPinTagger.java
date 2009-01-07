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
// $Id: TestPinTagger.java,v 1.1 2007/04/19 11:40:04 spyromus Exp $
//

package com.salas.bb.core;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * Tests pin tagger parts.
 */
public class TestPinTagger extends TestCase
{
    /** Merges empty tags list with some pins. */
    public void testMergeTags_empty()
    {
        assertTags(PinTagger.mergeTags(new String[0], ""));
        assertTags(PinTagger.mergeTags(new String[0], "a b"), "a", "b");
    }

    /** Merges some tags with some tags. */
    public void testMergeTags_normal()
    {
        assertTags(PinTagger.mergeTags(new String[] { "a", "b"}, " c d"), "a", "b", "c", "d");
    }

    /** Merges some tags with duplicates. */
    public void testMergeTags_duplicate()
    {
        assertTags(PinTagger.mergeTags(new String[] { "a", "b"}, " b d"), "a", "b", "d");
    }

    private void assertTags(String[] result, String ... tags)
    {
        assertEquals(tags.length, result.length);

        List<String> res = Arrays.asList(result);
        List<String> tgs = Arrays.asList(tags);

        for (String t : res)
        {
            if (!tgs.contains(t)) fail("Tag '" + t + "' was not expected.");
        }

        for (String t : tgs)
        {
            if (!res.contains(t)) fail("Tag '" + t + "' is missing.");
        }
    }
}
