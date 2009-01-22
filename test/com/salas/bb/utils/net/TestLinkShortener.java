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
// $Id$
//

package com.salas.bb.utils.net;

import junit.framework.TestCase;

/**
 * Tests shortener.
 */
public class TestLinkShortener extends TestCase
{
    public void testShorteningNull()
        throws LinkShorteningException
    {
        assertNull(LinkShortener.process(null));
    }

    public void testShorteningEmptyString()
        throws LinkShorteningException
    {
        assertEquals(" ", LinkShortener.process(" "));
    }

    public void testShorteningNonUrl()
    {
        try
        {
            LinkShortener.process("hello");
            fail("LinkShorteningException expected");
        } catch (LinkShorteningException e)
        {
            // Expected
        }
    }

    public void testShorteningLong()
        throws LinkShorteningException
    {
        String link = "http://some.com/very/long/link.html";
        String res  = LinkShortener.process(link);
        assertFalse(res.equals(link));
    }

    public void testShorteningShort()
        throws LinkShorteningException
    {
        String link = "http://abc.com/";
        String res  = LinkShortener.process(link);
        assertTrue(res.equals(link));
    }
}
