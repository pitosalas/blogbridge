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
// $Id $
//

package com.salas.bb.discovery.filter;

import junit.framework.TestCase;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * Tests extension filter.
 */
public class TestExtensionURLFilter extends ExtensionURLFilterTestCase
{
    /**
     * Tests converting the list of extensions into the filter pattern.
     */
    public void testExtensionsToPattern()
    {
        // Extensions aren't given or invalid
        assertNull(ExtensionURLFilter.extensionsToPattern(null));
        assertNull(ExtensionURLFilter.extensionsToPattern(new String[0]));
        assertNull(ExtensionURLFilter.extensionsToPattern(new String[] { null }));
        assertNull(ExtensionURLFilter.extensionsToPattern(new String[] { "" }));
        assertNull(ExtensionURLFilter.extensionsToPattern(new String[] { "", "" }));
        assertNull(ExtensionURLFilter.extensionsToPattern(new String[] { "", null, " " }));

        // Extensions are given
        assertEquals("\\.(a)$", ExtensionURLFilter.extensionsToPattern(new String[] { "a" }));
        assertEquals("\\.(a|b)$", ExtensionURLFilter.extensionsToPattern(new String[] { "a", "b" }));
        assertEquals("\\.(a|b|c)$", ExtensionURLFilter.extensionsToPattern(new String[] { "a", "", "b", null, "c" }));
    }

    /**
     * Tests conversion of property text into the list of extensions.
     */
    public void testStringToExtensions()
    {
        // Empty list conversion
        assertNull(DynamicExtensionURLFilter.stringToExtensions(null));
        assertNull(DynamicExtensionURLFilter.stringToExtensions(""));

        // Normal conversion
        assertArray(new String[] { "a" }, DynamicExtensionURLFilter.stringToExtensions("a"));
        assertArray(new String[] { "a" }, DynamicExtensionURLFilter.stringToExtensions(" a"));
        assertArray(new String[] { "a" }, DynamicExtensionURLFilter.stringToExtensions(" a "));
        assertArray(new String[] { "a" }, DynamicExtensionURLFilter.stringToExtensions(" a, "));
        assertArray(new String[] { "a" }, DynamicExtensionURLFilter.stringToExtensions(" ,a "));
        assertArray(new String[] { "a" }, DynamicExtensionURLFilter.stringToExtensions(",a "));
        assertArray(new String[] { "a" }, DynamicExtensionURLFilter.stringToExtensions("a,"));
        assertArray(new String[] { "a" }, DynamicExtensionURLFilter.stringToExtensions("a ,"));

        assertArray(new String[] { "a", "b" }, DynamicExtensionURLFilter.stringToExtensions("a b"));
        assertArray(new String[] { "a", "b" }, DynamicExtensionURLFilter.stringToExtensions("a, b"));
        assertArray(new String[] { "a", "b" }, DynamicExtensionURLFilter.stringToExtensions("a , b"));
        assertArray(new String[] { "a", "b" }, DynamicExtensionURLFilter.stringToExtensions("a ,b"));

        assertArray(new String[] { "a", "b", "c", "d" }, DynamicExtensionURLFilter.stringToExtensions("a b, c d"));
        assertArray(new String[] { "a", "b", "c", "d" }, DynamicExtensionURLFilter.stringToExtensions(" a  b  c , d "));
    }

    /**
     * Matching test.
     */
    public void testMatch()
    {
        ExtensionURLFilter f = new ExtensionURLFilter(new String[] { "avi", "MOV", "mpG" });

        assertTrue("Simple", f.matches(url("test.avi")));
        assertTrue("Non-matching letter case", f.matches(url("test.mov")));
        assertTrue("Query", f.matches(url("test.mpg?test=a")));
    }

    /**
     * No matching test.
     */
    public void testNoMatch()
    {
        ExtensionURLFilter f = new ExtensionURLFilter(new String[] { "avi" });

        assertFalse("Simple", f.matches(url("test.xml")));
        assertFalse("More prefix letters", f.matches(url("test.xavi")));
        assertFalse("More suffix letters", f.matches(url("test.avix")));
        assertFalse("No stop", f.matches(url("testavi")));
        assertFalse("No URL", f.matches(null));

        f = new ExtensionURLFilter((String)null);

        assertFalse("Filter is not initialized", f.matches(url("test.any")));
    }

    /**
     * Checks if two arrays are equal.
     *
     * @param target    target array.
     * @param array     array to check.
     */
    private void assertArray(String[] target, String[] array)
    {
        if (target == null)
        {
            assertNull(array);
        } else
        {
            assertNotNull(array);
            assertEquals(target.length, array.length);

            for (int i = 0; i < target.length; i++)
            {
                assertEquals("Index: " + i, target[i], array[i]);
            }
        }
    }
}
