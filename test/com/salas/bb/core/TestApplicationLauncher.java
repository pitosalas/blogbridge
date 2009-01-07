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
// $Id: TestApplicationLauncher.java,v 1.15 2006/11/27 18:27:00 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bbutilities.VersionUtils;
import junit.framework.TestCase;

/**
 * @see ApplicationLauncher
 */
public class TestApplicationLauncher extends TestCase
{
    /**
     * @see com.salas.bbutilities.VersionUtils#versionCompare
     */
    public void testVersionCompare()
    {
        assertEquals(-1, VersionUtils.versionCompare("0.1.2", "0.1.3"));
        assertEquals(-1, VersionUtils.versionCompare("0.1.2", "0.2.2"));
        assertEquals(-1, VersionUtils.versionCompare("0.1.2", "1.1.2"));
        assertEquals(-1, VersionUtils.versionCompare("0.1.20", "1.1.0"));

        assertEquals(0, VersionUtils.versionCompare("1.0.0", "1.0.0"));
        assertEquals(0, VersionUtils.versionCompare("0.1.0", "0.1.0"));
        assertEquals(0, VersionUtils.versionCompare("0.0.1", "0.0.1"));

        assertEquals(1, VersionUtils.versionCompare("0.1.3", "0.1.2"));
        assertEquals(1, VersionUtils.versionCompare("0.2.2", "0.1.2"));
        assertEquals(1, VersionUtils.versionCompare("1.1.2", "0.1.2"));
        assertEquals(1, VersionUtils.versionCompare("1.1.0", "0.1.20"));


        assertEquals(-1, VersionUtils.versionCompare("", "0.1.3"));
        assertEquals(-1, VersionUtils.versionCompare("a", "0.1.3"));
        assertEquals(0, VersionUtils.versionCompare("", ""));
        assertEquals(1, VersionUtils.versionCompare("0.1.1", ""));
        assertEquals(-1, VersionUtils.versionCompare("0.1.1", "2.1"));
        assertEquals(1, VersionUtils.versionCompare("0.1.1", "a"));
        assertEquals(-1, VersionUtils.versionCompare("0.6.1", "0.8"));
        assertEquals(1, VersionUtils.versionCompare("0.8", "0.6.1"));
    }

    /**
     * Tests converting system properties and/or command-line argument into release type
     * and working path.
     */
    public void testInitReleaseTypeAndPrefix()
    {
        assertTypeAndPath("Final", "bb/final", null, null, null);
        assertTypeAndPath("Final", "bb/final", "final", "final", null);
        assertTypeAndPath("Final", "bb/final", "final", "final", "bb/stable");
        assertTypeAndPath("Stable", "bb/stable", null, null, "bb/stable");
    }

    /**
     * Asserts release type and prefix.
     *
     * @param targetReleaseType     target release type.
     * @param targetPrefix          target prefix.
     * @param propReleaseType       release type system property value.
     * @param propWorkingForlder    working folder system property value.
     * @param argument              first command-line argument.
     */
    private void assertTypeAndPath(String targetReleaseType, String targetPrefix,
        String propReleaseType, String propWorkingForlder, String argument)
    {
        String[] arguments;

        if (argument == null) arguments = new String[0]; else arguments = new String[] { argument };

        ApplicationLauncher.initReleaseTypeAndPrefix(arguments, propReleaseType,
            propWorkingForlder);

        assertEquals(targetReleaseType, ApplicationLauncher.getReleaseType());
        assertEquals(targetPrefix, ApplicationLauncher.getPrefix());
    }

    /** Empty arguments list. */
    public void testCheckCommandLineForURL_Empty()
    {
        assertNull(ApplicationLauncher.checkCommandLineForURL(new String[0]));
    }

    /** URL specified. */
    public void testCheckCommandLineForURL_URL()
    {
        assertEquals("http://test", ApplicationLauncher.checkCommandLineForURL(new String[]
        {
            "http://test"
        }));
    }
    
    /** Open command with URL specified. */
    public void testCheckCommandLineForURL_Open_URL()
    {
        assertEquals("http://test", ApplicationLauncher.checkCommandLineForURL(new String[]
        {
            "-open",
            "http://test"
        }));
    }
}
