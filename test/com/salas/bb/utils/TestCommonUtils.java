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
// $Id: TestCommonUtils.java,v 1.3 2007/11/07 17:16:48 spyromus Exp $
//

package com.salas.bb.utils;

import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This suite contains tests for <code>CommonUtils</code> unit.
 */
public class TestCommonUtils extends TestCase
{
    /**
     * Tests the difference detection on example of strings.
     */
    public void testAreDifferent()
    {
        assertFalse("NULL's aren't different.", CommonUtils.areDifferent(null, null));

        assertTrue("NULL is different to 'a'", CommonUtils.areDifferent(null, "a"));
        assertTrue("NULL is different to 'a'", CommonUtils.areDifferent("a", null));

        assertFalse("String aren't different.", CommonUtils.areDifferent("a", "a"));
        assertTrue("String are different.", CommonUtils.areDifferent("a", "A"));
        assertTrue("String are different.", CommonUtils.areDifferent("a", "b"));
    }

    /**
     * Tests intern'ing of URLs.
     */
    public void testIntern()
    {
        // Create full URL
        String st = "http://site.com:8080/dir/file.ext?query1=1&query2=2#ref";
        URL url = url(st);

        // Parts
        String pr = "http";
        String ho = "site.com";
        int    po = 8080;
        String pa = "/dir/file.ext";
        String qu = "query1=1&query2=2";
        String re = "ref";
        String au = "site.com:8080";

        // Intern and do basic checks
        URL tg = CommonUtils.intern(url);
        assertFalse(tg == url);
        assertEquals(url, tg);

        // Check intern'ed parts
        // NOTE '==' is intentional to check that intern worked
        assertTrue(pr == tg.getProtocol());
        assertTrue(ho == tg.getHost());
        assertTrue(po == tg.getPort());
        assertTrue(pa == tg.getPath());
        assertTrue(qu == tg.getQuery());
        assertTrue(re == tg.getRef());
        assertTrue(au == tg.getAuthority());
    }
    /**
     * Creates an URL.
     *
     * @param s URL text.
     *
     * @return result.
     */
    private URL url(String s)
    {
        URL u = null;

        try
        {
            u = new URL(s);
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
            fail();
        }

        return u;
    }
}
