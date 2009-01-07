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
// $Id: TestResumingSupport.java,v 1.5 2008/02/28 15:59:51 spyromus Exp $
//

package com.salas.bb.utils.net;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @see ResumingSupport
 */
public class TestResumingSupport extends TestCase
{
    private static final String TEST_URL = "http://www.google.com/index.html";

    /**
     * Tests how NULL entries are handled.
     */
    public void testBuggyInput()
    {
        try
        {
            ResumingSupport.connect(null);
            fail("NULL's should not be accepted.");
        } catch (IOException e)
        {
            e.printStackTrace();
            fail();
        } catch (IllegalArgumentException e)
        {
            // Expected
        }

        try
        {
            ResumingSupport.resume(null, 0);
            fail("NULL's should not be accepted.");
        } catch (IOException e)
        {
            e.printStackTrace();
            fail();
        } catch (IllegalArgumentException e)
        {
            // Expected
        }
    }

    /**
     * Tests simple direct connecting to resource.
     */
    public void testConnect()
        throws IOException
    {
        URL url = getLocalURL();

        final URLConnection con = ResumingSupport.connect(url).getConnection();
        assertNotNull(con);

        int len = con.getContentLength();
        InputStream in = con.getInputStream();
        while (in.read() != -1) len--;

        assertEquals("Size taken fron connection header does not match actual size.", 0, len);
    }

    /**
     * Test resuming of resumable HTTP content.
     */
    public void testResumeHTTP()
        throws IOException
    {
        URL url = new URL(TEST_URL);
        InputStream in = url.openStream();
        int size = 0;
        while (in.read() != -1) size++;

        int offset = size / 2;
        URLConnection con = ResumingSupport.resume(url, offset).getConnection();
        in = con.getInputStream();
        int read = 0;
        while (in.read() != -1) read++;

        assertEquals(size - offset, read);
    }

    /**
     * Tests resuming when it's not supported by server software.
     */
    public void testResumeUnresumable()
        throws IOException
    {
        URL url = getLocalURL();

        URLConnection con = ResumingSupport.connect(url).getConnection();
        InputStream in = con.getInputStream();
        in.read();
        in.read();
        in.read();
        in.read();
        int ch = in.read();
        int size = 0;
        while (in.read() != -1) size++;

        con = ResumingSupport.resume(url, 4).getConnection();
        in = con.getInputStream();
        assertEquals("Resumed position is wrong.", ch, in.read());
        while (in.read() != -1) size--;
        assertEquals("Resumed position was wrong: " + size, 0, size);
    }
    
    // Returns URL to some local resource.
    private URL getLocalURL()
    {
        String name = ResumingSupport.class.getName().replaceAll("\\.", "/") + ".class";
        URL url = ResumingSupport.class.getClassLoader().getResource(name);

        assertNotNull("Local resource isn't resolved to URL.", url);

        return url;
    }
}
