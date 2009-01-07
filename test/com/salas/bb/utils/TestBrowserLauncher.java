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
// $Id: TestBrowserLauncher.java,v 1.10 2008/02/28 15:59:52 spyromus Exp $
//

package com.salas.bb.utils;

import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @see BrowserLauncher
 */
public class TestBrowserLauncher extends TestCase
{
    /**
     * @see BrowserLauncher#getCorrectBrowserExecutable(java.lang.String)
     */
    public void testGetCorrectBrowserExecutable()
    {
        final String defaultBrowser = OSSettings.getDefaultBrowserPath();

        assertEquals(BrowserLauncher.getCorrectBrowserExecutable(null), defaultBrowser);
        assertEquals(BrowserLauncher.getCorrectBrowserExecutable(""), defaultBrowser);
        assertEquals(BrowserLauncher.getCorrectBrowserExecutable("some"), "some");
    }

    /**
     * @see BrowserLauncher#convertToFullCommand
     */
    public void testConvertToParametersList() throws Exception
    {
        String exec;
        URL link;
        String command;

        // empty browser executable & link
        exec = null;
        link = null;
        assertNull(BrowserLauncher.convertToFullCommand(exec, link));

        // empty executable
        link = new URL("file://a");
        assertNull(BrowserLauncher.convertToFullCommand(exec, link));

        // empty link
        link = null;
        exec = "test";
        assertNull(BrowserLauncher.convertToFullCommand(exec, link));

        // command w/o marks
        link = new URL("file://a");
        exec = "mozilla";
        assertEquals("mozilla file://a", BrowserLauncher.convertToFullCommand(exec, link));

        // command w/ marks
        exec = "mozilla -remote \"openURL($URL$, new-window)\"";
        command = BrowserLauncher.convertToFullCommand(exec, link);
        assertEquals("mozilla -remote \"openURL(file://a, new-window)\"", command);

        // command w/ multi-marks
        exec = "mozilla -remote \"openURL($URL$, new-window)\" || echo $URL$";
        command = BrowserLauncher.convertToFullCommand(exec, link);
        assertEquals("mozilla -remote \"openURL(file://a, new-window)\" || echo file://a", command);
    }

    /**
     * Test escaping of reserved characters during converting URL into browser command.
     */
    public void testConvertToFullCommandEscaping()
        throws MalformedURLException
    {
        assertEquals("Dollar sign in URL should not be treated as group substitution, but as" +
            "normal printed element.", "file://test$9",
            BrowserLauncher.convertToFullCommand("$URL$", new URL("file://test$9")));

        assertEquals("Backslash should be escaped as it's treated as escape sequence start.",
            "file://test\\9",
            BrowserLauncher.convertToFullCommand("$URL$", new URL("file://test\\9")));
    }
}
