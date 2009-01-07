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
// $Id: TestConnectionState.java,v 1.2 2006/01/08 05:28:19 kyank Exp $
//

package com.salas.bb.utils;

import junit.framework.TestCase;

/**
 * This suite contains tests for <code>ConnectionState</code> unit.
 */
public class TestConnectionState extends TestCase
{
    private ConnectionState state;

    /**
     * Tests setup.
     */
    protected void setUp()
        throws Exception
    {
        super.setUp();

        state = new ConnectionState();
    }

    /**
     * Tests online state.
     */
    public void testOnline()
    {
        state.setOnline(true);
        assertTrue(state.isOnline());

        state.setOnline(false);
        assertFalse(state.isOnline());
    }

    /**
     * Tests service accessibility state.
     */
    public void testServerAccessibility()
    {
        state.setOnline(true);
        state.setServiceAccessible(false);
        assertFalse(state.isServiceAccessible());

        state.setServiceAccessible(true);
        assertTrue(state.isServiceAccessible());

        state.setOnline(false);
        state.setServiceAccessible(false);
        assertFalse(state.isServiceAccessible());

        state.setServiceAccessible(true);
        assertFalse(state.isServiceAccessible());
    }
}
