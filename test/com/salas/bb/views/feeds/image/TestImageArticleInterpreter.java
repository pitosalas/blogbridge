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
// $Id: TestImageArticleInterpreter.java,v 1.4 2006/06/14 14:33:10 spyromus Exp $
//

package com.salas.bb.views.feeds.image;

import junit.framework.TestCase;

/**
 * This suite contains tests for <code>ImageArticleInterpreter</code> unit.
 */
public class TestImageArticleInterpreter extends TestCase
{
    /**
     * Tests detection of image URL.
     */
    public void testGetImageURLNULL()
    {
        assertNull(ImageArticleInterpreter.getImageURL(null, (String)null));
    }

    /**
     * Tests finding single image URL.
     */
    public void testGetImageURLSingleImage()
    {
        assertEquals("http://test/image", ImageArticleInterpreter.getImageURL(null,
            "<html> test <img border=\"0\" src=\"http://test/image\"> </html>").toString());
        assertEquals("http://test/image", ImageArticleInterpreter.getImageURL(null,
            "<html> test <img border='0' src='http://test/image' /> </html>").toString());
        assertEquals("http://test/image&a=1", ImageArticleInterpreter.getImageURL(null,
            "<html> test <img border='0' src='http://test/image&amp;a=1' /> </html>").toString());
    }

    /**
     * Tests skipping broken image URL's to the next link.
     */
    public void testGetImageURLBrokenImage()
    {
        assertEquals("http://test/image", ImageArticleInterpreter.getImageURL(null,
            "<html> test <img border=\"0\" src=\"htp://test/broken\">" +
                "<img border=\"0\" src=\"http://test/image\"> </html>").toString());
    }
}
