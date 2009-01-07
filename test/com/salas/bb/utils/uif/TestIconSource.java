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
// $Id: TestIconSource.java,v 1.3 2006/01/08 05:28:18 kyank Exp $
//

package com.salas.bb.utils.uif;

import junit.framework.TestCase;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.utils.StringUtils;

import javax.swing.*;
import java.util.ResourceBundle;
import java.util.Enumeration;
import java.util.logging.Logger;

/**
 * @see IconSource
 */
public class TestIconSource extends TestCase
{
    private static final Logger LOG = Logger.getLogger(TestIconSource.class.getName());

    protected void setUp() throws Exception
    {
        ResourceUtils.setBundlePath("Resource");
    }

    public void testIconSourceSpeedImprovement()
    {
        ResourceBundle bundle = ResourceBundle.getBundle("Resource");
        int icons = 0;

        long start1 = System.currentTimeMillis();
        Enumeration keys = bundle.getKeys();
        while (keys.hasMoreElements())
        {
            String key = (String)keys.nextElement();
            String value = bundle.getString(key);
            if (value != null && (value.endsWith("jpg") || value.endsWith("png")))
            {
                Icon icon = IconSource.getIcon(key);
                assertNotNull(icon);
                icons++;
            }
        }

        long start2 = System.currentTimeMillis();
        keys = bundle.getKeys();
        while (keys.hasMoreElements())
        {
            String key = (String)keys.nextElement();
            String value = bundle.getString(key);
            if (value != null && (value.endsWith("jpg") || value.endsWith("png")))
            {
                Icon icon = IconSource.getIcon(key);
                assertNotNull(icon);
            }
        }

        long finish2 = System.currentTimeMillis();

        LOG.fine("Read " + icons + " icons. First loop: " + (start2 - start1) + "ms. " +
            "Second loop: " + (finish2 - start2) + "ms.");

//        System.out.println("Read " + icons + " icons. First loop: " + (start2 - start1) + "ms. " +
//            "Second loop: " + (finish2 - start2) + "ms.");

        assertTrue(start2 - start1 > 2 * (finish2 - start2));
        assertTrue(icons > 0);
    }

    public void testIconSourceMemoryManagement()
        throws InterruptedException
    {
        ResourceBundle bundle = ResourceBundle.getBundle("Resource");
        int icons = 0;

        // Load icons
        Enumeration keys = bundle.getKeys();
        while (keys.hasMoreElements())
        {
            String key = (String)keys.nextElement();
            String value = bundle.getString(key);
            if (value != null && (value.endsWith("jpg") || value.endsWith("png")))
            {
                assertNotNull(IconSource.getIcon(key));
                icons++;
            }
        }

        // Consume all memory
        try
        {
            byte[] buf = new byte[(int)Runtime.getRuntime().maxMemory()];
        } catch (OutOfMemoryError e)
        {
        }

        int cached = 0;
        keys = bundle.getKeys();
        while (keys.hasMoreElements())
        {
            String key = (String)keys.nextElement();
            String value = bundle.getString(key);
            if (value != null && (value.endsWith("jpg") || value.endsWith("png")))
            {
                if (IconSource.hasInCache(key)) cached++;
            }
        }

        assertTrue("Cache isn't cleared. Total: " + icons + " Cached: " + cached, cached < icons);
    }
}
