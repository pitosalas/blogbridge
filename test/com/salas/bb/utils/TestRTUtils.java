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
// $Id: TestRTUtils.java,v 1.2 2006/01/08 05:28:19 kyank Exp $
//

package com.salas.bb.utils;

import junit.framework.TestCase;

/**
 * This suite contains tests for <code>RTUtils</code> unit.
 */
public class TestRTUtils extends TestCase
{
    /**
     * Tests boolean version of callIfPresent().
     */
    public void testCallIfPresentBoolean()
    {
        SampleObject obj = new SampleObject();

        assertTrue("Method wasn't detected.", RTUtils.callIfPresent(obj, "setBoolean", true));
        assertTrue("Value wasn't set; method wasn't called.", obj.value);

        assertFalse("Method was detected.", RTUtils.callIfPresent(obj, "setBooleanFake", true));
    }

    /**
     * Sample object to test conditional methods calling.
     */
    private static class SampleObject
    {
        private boolean value = false;

        /**
         * Sample boolean method.
         *
         * @param value some value.
         */
        public void setBoolean(boolean value)
        {
            this.value = value;
        }
    }
}
