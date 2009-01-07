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
// $Id: TestStringEqualsCO.java,v 1.2 2006/01/08 05:28:16 kyank Exp $
//

package com.salas.bb.domain.query.general;

import junit.framework.TestCase;

/**
 * This suite contains tests for <code>StringEqualsCO</code> unit.
 */
public class TestStringEqualsCO extends TestCase
{
    private StringEqualsCO operation = new StringEqualsCO();

    /**
     * Tests matching.
     */
    public void testMatchPositive()
    {
        assertTrue(operation.match("apple", "apple"));
        assertTrue(operation.match(" apple ", "apple"));
        assertTrue(operation.match(" Apple ", "apple"));
        assertTrue(operation.match("Apple", "\"apple\""));
        assertTrue(operation.match("Apple", "pear apple"));
        assertTrue(operation.match("Apple", "pear apple"));
        assertTrue(operation.match("pear", "pear apple"));
        assertTrue(operation.match(" big apple ", "\"big apple\""));
    }

    /**
     * Tests matching.
     */
    public void testMatchNegative()
    {
        assertFalse(operation.match("pear", "apple"));
        assertFalse(operation.match("big apple", "apple"));
        assertFalse(operation.match("apple", "\"big apple\""));
    }
}
