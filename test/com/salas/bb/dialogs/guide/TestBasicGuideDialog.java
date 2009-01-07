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
// $Id: TestBasicGuideDialog.java,v 1.5 2007/01/24 14:46:39 spyromus Exp $
//

package com.salas.bb.dialogs.guide;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @see BasicGuideDialog
 */
public class TestBasicGuideDialog extends TestCase
{
    private BasicGuideDialog.Probe probe;

    protected void setUp() throws Exception
    {
        BasicGuideDialog dialog = new EditGuideDialog(null, true, 1, false);
        probe = dialog.getProbe();
    }

    /**
     * @see EditGuideDialog#validateInformation
     */
    public void testValidateInformation()
    {
        final Collection noPresentTitles = Collections.EMPTY_LIST;
        final Collection presentTitles = Arrays.asList("1", "3", "4");

        // New guide, no present guides, empty title
        assertFalse(probe.validate(null, noPresentTitles, null));
        assertFalse(probe.validate(null, noPresentTitles, ""));
        assertFalse(probe.validate(null, noPresentTitles, " "));

        // New guide, no present guide, title provided
        assertTrue(probe.validate(null, noPresentTitles, "test"));

        // New guide, some guides present, empty title
        assertFalse(probe.validate(null, presentTitles, null));
        assertFalse(probe.validate(null, presentTitles, ""));
        assertFalse(probe.validate(null, presentTitles, " "));

        // New guide, some guides present, title provided
        assertFalse(probe.validate(null, presentTitles, "1"));
        assertTrue(probe.validate(null, presentTitles, "2"));

        // Existing guide, some guides present, empty title
        assertFalse(probe.validate("1", presentTitles, null));
        assertFalse(probe.validate("1", presentTitles, ""));
        assertFalse(probe.validate("1", presentTitles, " "));

        // Existing guide, some guides present, title provided
        assertTrue(probe.validate("1", presentTitles, "1"));    // Title hasn't changed
        assertTrue(probe.validate("1", presentTitles, "2"));
        assertFalse(probe.validate("1", presentTitles, "3"));
    }
}
