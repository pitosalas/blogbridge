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
// $Id: TestIPC.java,v 1.2 2008/02/28 15:59:50 spyromus Exp $
//

package com.salas.bb.utils.ipc;

import junit.framework.TestCase;

import java.net.URL;

/**
 * Tests IPC parts.
 */
public class TestIPC extends TestCase
{
    /** Empty URL. */
    public void testArgToURL_Empty()
    {
        assertNull(IPC.argToURL(null));
        assertNull(IPC.argToURL(""));
    }

    /** Valid URL. */
    public void testArgToURL_URL_Valid()
    {
        assertEquals("http://www.blogbridge.com/", IPC.argToURL("http://www.blogbridge.com/").toString());
    }

    /** Invalid URL. */
    public void testArgToURL_URL_Invalid()
    {
        assertNull(IPC.argToURL("http://a:-11/"));
    }

    /** Valid file. */
    public void testArgToURL_File_Valid()
    {
        String expect = "file:/c:/boot.ini";
        URL url = IPC.argToURL("c:/boot.ini");
        if (url == null)
        {
            expect = "file:/etc/fstab";
            url = IPC.argToURL("/etc/fstab");
        }
        assertEquals(expect, url.toString());
    }

    /** Invalid file. */
    public void testArgToURL_File_Invalid()
    {
        assertNull(IPC.argToURL("c:/invalid"));
    }
}
