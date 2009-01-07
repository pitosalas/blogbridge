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
// $Id: TestThemeLoader.java,v 1.7 2006/02/21 15:04:50 spyromus Exp $
//

package com.salas.bb.views.themes;

import junit.framework.TestCase;

import java.util.Properties;
import java.awt.*;

/**
 * @see ThemeSupport
 */
public class TestThemeLoader extends TestCase
{
    /**
     * @see ThemeSupport#preProcessProperties
     */
    public void testPreProcessProperties()
    {
        Properties props, result;

        // Nothing to pre-process
        props = new Properties();
        props.setProperty("key", "value");

        result = ThemeSupport.preProcessProperties(props, "linux");
        assertEquals(1, result.size());
        assertEquals("value", result.getProperty("key"));

        // Pre-processing shouldn't be done as there's mac-only override
        props = new Properties();
        props.setProperty("key", "value");
        props.setProperty("key.mac", "value-mac");

        result = ThemeSupport.preProcessProperties(props, "linux");
        assertEquals(2, result.size());
        assertEquals("value", result.getProperty("key"));

        // Pre-processing should be done
        props = new Properties();
        props.setProperty("key", "value");
        props.setProperty("key.mac", "value-mac");
        props.setProperty("key.linux", "value-linux");

        result = ThemeSupport.preProcessProperties(props, "linux");
        assertEquals(3, result.size());
        assertEquals("value-linux", result.getProperty("key"));
    }

    /**
     * @see ThemeSupport#createTheme
     */
    public void testCreateTheme()
    {
        Properties props;
        ITheme defaultTheme, theme;

        defaultTheme = new EmptyTheme();

        // Test of basic loading
        props = new Properties();
        props.setProperty("name", "test");
        props.setProperty("title", "test");
        props.setProperty(ThemeKey.Color.ARTICLE_SEL_BG.getKey(), "#000000");
        theme = ThemeSupport.createTheme(props, defaultTheme, "linux");

        assertNotNull(theme);
        assertEquals(Color.BLACK, theme.getColor(ThemeKey.Color.ARTICLE_SEL_BG));
        assertEquals(Color.YELLOW, theme.getColor(ThemeKey.Color.ARTICLE_UNSEL_BG));

        // Tests of hiding
        props = new Properties();
        props.setProperty("name", "test");
        props.setProperty("title", "test");
        props.setProperty("hidden", "linux");
        assertNull(ThemeSupport.createTheme(props, defaultTheme, "linux"));

        props.setProperty("hidden", "windows, linux");
        assertNull(ThemeSupport.createTheme(props, defaultTheme, "linux"));

        props.setProperty("hidden", "windows, linux, mac");
        assertNull(ThemeSupport.createTheme(props, defaultTheme, "linux"));
    }

    /**
     * Empty theme returns YELLOW color and NULL font.
     */
    private static class EmptyTheme implements ITheme
    {
        public Color getColor(ThemeKey.Color key)
        {
            return Color.YELLOW;
        }

        public Font getFont(ThemeKey.Font key)
        {
            return null;
        }

        public int getFontSizeDelta(ThemeKey.Font key)
        {
            return 0;
        }

        public String getName()
        {
            return "EMPTY";
        }

        public String getTitle()
        {
            return null;
        }

        public Font getFontWithBias(com.salas.bb.views.themes.ThemeKey.Font key,
            int articleFontBias)
        {
            return null;
        }
    }
}
