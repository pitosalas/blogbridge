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
// $Id: TestChecker.java,v 1.5 2007/10/11 09:09:39 spyromus Exp $
//

package com.salas.bb.updates;

import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.core.ApplicationLauncher;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * This suite contains tests for <code>Checker</code> unit.
 */
public class TestChecker extends TestCase
{
    private static final String LATEST_PRODUCTION_VERSION;
    private static final String PREVIOUS_PRODUCTION_VERSION;

    // This test won't work when started on production version
    private static final boolean skipTest;

    private Checker checker;

    static
    {
        String currentVersion = ApplicationLauncher.getCurrentVersion();
        int dotIndex = currentVersion.indexOf('.');
        String majorVersionNumber = currentVersion.substring(0, dotIndex);

        LATEST_PRODUCTION_VERSION = majorVersionNumber + ".0";

        int majorNumber = Integer.parseInt(majorVersionNumber);

        PREVIOUS_PRODUCTION_VERSION = Integer.toString(majorNumber - 1) + ".0";

        skipTest = LATEST_PRODUCTION_VERSION.equals(currentVersion);
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        ResourceUtils.setBundle(ResourceBundle.getBundle("Resource"));
        checker = new Checker();
    }

    /**
     * Tests returning updates from latest production version. No updates
     * should be reported as there's no other production version deployed.
     */
    public void testCheckNoUpdates()
        throws IOException
    {
        if (skipTest) return;

        CheckResult checkResult = checker.checkForUpdates(LATEST_PRODUCTION_VERSION);
        if (checkResult != null)
        {
            assertTrue("No version after " + LATEST_PRODUCTION_VERSION + " is available.",
                checkResult.getRecentVersion().matches(LATEST_PRODUCTION_VERSION + "(\\.[0-9]+)?"));
        }
    }

    /**
     * Tests returning updates between production version before previous production version.
     * It means that the latest production version availability will be reported.
     */
    public void testCheckUpdatesPresent()
        throws IOException
    {
        if (skipTest) return;

        CheckResult checkResult = checker.checkForUpdates(PREVIOUS_PRODUCTION_VERSION);
        assertNotNull("Version after " + PREVIOUS_PRODUCTION_VERSION + " is available.",
            checkResult);

        assertTrue(checkResult.getRecentVersion().matches(LATEST_PRODUCTION_VERSION + "(\\.[0-9]+)?"));
        assertTrue(checkResult.getReleaseTime() > 0);
        assertTrue(checkResult.getLocations().size() > 0);
        assertTrue(checkResult.getChanges().length > 0);
    }
}
