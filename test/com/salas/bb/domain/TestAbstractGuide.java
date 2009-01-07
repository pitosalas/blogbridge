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
// $Id: TestAbstractGuide.java,v 1.4 2006/01/08 05:28:16 kyank Exp $
//

package com.salas.bb.domain;

import junit.framework.TestCase;

/**
 * This suite contains the tests for <code>AbstractGuide</code> unit.
 * It covers:
 * TODO put details
 */
public class TestAbstractGuide extends TestCase
{
    private AbstractGuide guide;

    protected void setUp()
        throws Exception
    {
        guide = new DummyEmptyGuide();
    }

    // ---------------------------------------------------------------------------------------------
    // Test
    // ---------------------------------------------------------------------------------------------

    /**
     * Tests the construction defaults.
     */
    public void testConstruction()
    {
        assertNull("Title should be unset.", guide.getTitle());
        assertNull("Icon should be unset.", guide.getIconKey());
        assertFalse("Wrong default state.", guide.isAutoFeedsDiscovery());
        assertTrue("No feeds - nothing to read - everything is read.", guide.isRead());
    }

    /**
     * Tests getting and setting title.
     */
    public void testGetSetTitle()
    {
        // TODO implement
    }

    /**
     * Tests getting and setting icon keys.
     */
    public void testGetSetIconKey()
    {
        // TODO implement
    }

    /**
     * Tests getting and setting automatic feeds discovery flag.
     */
    public void testGetSetAutoFeedDiscovery()
    {
        // TODO implement
    }

    /**
     * Tests finding feeds with given XML URL.
     */
    public void testFindFeedsByXmlURL()
    {
        // TODO 2
    }

    /**
     * Tests handling of incorrect input to the finding method.
     */
    public void testFindFeedsByXmlURLFailure()
    {
        // TODO implement
    }

    /**
     * Tests getting and setting read status.
     */
    public void testGetSetRead()
    {
        // TODO 3
    }
}
