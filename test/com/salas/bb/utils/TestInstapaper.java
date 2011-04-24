/*
 * BlogBridge -- RSS feed reader, manager, and web based service
 * Copyright (C) 2002-2011 by R. Pito Salas
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Contact: R. Pito Salas
 * mailto:pitosalas@users.sourceforge.net
 * More information: about BlogBridge
 * http://www.blogbridge.com
 * http://sourceforge.net/projects/blogbridge
 */

package com.salas.bb.utils;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

/**
 * Instapaper tests.
 */
public class TestInstapaper extends TestCase
{
    public void testSuccessfulProcessing()
        throws IOException
    {
        String h = html("file1.html");
        String r = Instapaper.processMobilizedString(h);

        assertEquals("<!-- mob-start -->\n<div id=\"story\"> \nstory\n</div>\n<!-- mob-end -->", r);
    }

    private static String html(String name)
        throws IOException
    {
        File path = TUtils.getTestDataPath();
        File f = new File(path, "instapaper/" + name);
        return FileUtils.readFileAsString(f.getAbsolutePath());
    }

}
