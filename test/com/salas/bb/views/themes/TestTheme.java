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
// $Id: TestTheme.java,v 1.2 2006/02/21 15:20:27 spyromus Exp $
//

package com.salas.bb.views.themes;

import junit.framework.TestCase;

import java.util.Properties;
import java.awt.*;

/**
 * Tests theme mechanisms.
 */
public class TestTheme extends TestCase
{
    /**
     * Tests overloading main font.
     */
    public void testFontMainOverloading()
    {
        Properties thProps = new Properties();
        thProps.put(ThemeKey.Font.MAIN.getKey(), "Verdana-10");
        Theme thMain = new Theme("Main", "Main", thProps, new LAFTheme());

        thProps = new Properties();
        thProps.put(ThemeKey.Font.MAIN.getKey(), "Arial-12");
        Theme thChild = new Theme("Child", "Child", thProps, thMain);

        Font font = thChild.getFont(ThemeKey.Font.MAIN);
        assertEquals("Arial", font.getName());
        assertEquals(12, font.getSize());
    }

    /**
     * Tests overriding main font manually.
     */
    public void testFontMainOverride()
    {
        Properties thProps = new Properties();
        thProps.put(ThemeKey.Font.MAIN.getKey(), "Verdana-10");
        Theme thMain = new Theme("Main", "Main", thProps, new LAFTheme());

        Theme.setMainFontOverride(Font.decode("Arial-12"));

        Font font = thMain.getFont(ThemeKey.Font.MAIN);
        assertEquals("Arial", font.getName());
        assertEquals(12, font.getSize());

        Theme.setMainFontOverride(null);

        font = thMain.getFont(ThemeKey.Font.MAIN);
        assertEquals("Verdana", font.getName());
        assertEquals(10, font.getSize());
    }

    /**
     * Tests overriding font size.
     */
    public void testFontSizeDeltas()
    {
        Properties thProps = new Properties();
        thProps.put(ThemeKey.Font.MAIN.getKey(), "Verdana-10");
        thProps.put(ThemeKey.Font.ARTICLE_DATE.getKey()+".sizedelta", "-1");
        Theme thMain = new Theme("Main", "Main", thProps, new LAFTheme());

        thProps = new Properties();
        thProps.put(ThemeKey.Font.ARTICLE_TEXT.getKey()+".sizedelta", "2");
        Theme thChild = new Theme("Child", "Child", thProps, thMain);

        Font font;

        font = thChild.getFont(ThemeKey.Font.MAIN);
        assertEquals("Verdana", font.getName());
        assertEquals(10, font.getSize());

        font = thChild.getFont(ThemeKey.Font.ARTICLE_DATE);
        assertEquals("Verdana", font.getName());
        assertEquals("Overriden in main.", 9, font.getSize());

        font = thChild.getFont(ThemeKey.Font.ARTICLE_TEXT);
        assertEquals("Verdana", font.getName());
        assertEquals("Overriden in child", 12, font.getSize());
    }
}
