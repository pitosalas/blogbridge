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
// $Id: TestImageBlocker.java,v 1.4 2008/02/28 15:59:53 spyromus Exp $
//

package com.salas.bb.imageblocker;

import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/** Image blocker tests. */
public class TestImageBlocker extends TestCase
{
    private static final String BLOCK_PATTERN = "http://blocked.site/*";
    private static final URL BLOCKED = url("http://blocked.site/1.png");
    private static final URL NONBLOCKED = url("http://nonblocked.site/2.png");

    protected void setUp()
        throws Exception
    {
        super.setUp();

        ImageBlocker.clearExpressions();
    }

    /** Tests clearing patterns. */
    public void testClearPatterns()
    {
        ImageBlocker.addExpression(BLOCK_PATTERN);
        ImageBlocker.clearExpressions();
        assertTrue(ImageBlocker.getExpressions().isEmpty());
    }

    /** Tests adding patterns and blocking. */
    public void testAddingPatternAndLinkBlocking()
    {
        ImageBlocker.addExpression(BLOCK_PATTERN);
        assertTrue(ImageBlocker.isBlocked(BLOCKED));
    }

    /** Tests blocking. */
    public void testLinkIsNotBlocked()
    {
        assertFalse(ImageBlocker.isBlocked(NONBLOCKED));

        ImageBlocker.addExpression(BLOCK_PATTERN);
        assertFalse(ImageBlocker.isBlocked(NONBLOCKED));
    }

    /** Tests handling NULL-links. */
    public void testLinkIsNotGiven()
    {
        assertFalse(ImageBlocker.isBlocked(null));
    }

    public void testSettingPatterns()
    {
        ImageBlocker.addExpression(BLOCK_PATTERN);
        ImageBlocker.setExpressions("a\nb");
        List<String> pats = ImageBlocker.getExpressions();
        assertTrue(pats.contains("a"));
        assertTrue(pats.contains("b"));
        assertTrue(!pats.contains(BLOCK_PATTERN));
    }

    /**
     * Creates an URL.
     *
     * @param str URL.
     *
     * @return URL.
     */
    private static URL url(String str)
    {
        try
        {
            return new URL(str);
        } catch (MalformedURLException e)
        {
            // Never happens
            return null;
        }
    }
}
