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
// $Id: TestUifUtilities.java,v 1.2 2006/01/08 05:28:18 kyank Exp $
//

package com.salas.bb.utils.uif;

import junit.framework.TestCase;

import java.awt.*;

/**
 * This suite contains tests for <code>UifUtilities</code> unit.
 */
public class TestUifUtilities extends TestCase
{
    /**
     * Tests applying the bias to the font.
     */
    public void testApplyFontBias()
    {
        Font aFont = new Font("Arial", Font.PLAIN, 5);

        assertEquals("Expected font size to be 5",
            UifUtilities.applyFontBias(aFont, 0).getSize(), 5);

        assertEquals("Expected font size to be 3",
            UifUtilities.applyFontBias(aFont, -2).getSize(), 3);

        assertEquals("Expected font size to be 7",
            UifUtilities.applyFontBias(aFont, +2).getSize(), 7);
    }

    /**
     * Tests situation when font isn't specified.
     */
    public void testApplyFontBiasFailure()
    {
        try
        {
            UifUtilities.applyFontBias(null, -1);
            fail("Font isn't specified. NPE is expected.");
        } catch (NullPointerException expected)
        {
            // Expected behavior
        }
    }
}
