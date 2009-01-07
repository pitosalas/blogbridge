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
// $Id: Theme.java,v 1.10 2006/02/22 13:37:21 spyromus Exp $
//

package com.salas.bb.views.themes;

import java.awt.*;
import java.util.Properties;

/**
 * Visual theme implementation.
 */
public class Theme extends AbstractTheme
{
    /** Main font override. */
    private static Font mainFontOverride;

    private ITheme      defaultTheme;
    private Properties  properties;

    /**
     * Creates theme.
     *
     * @param aName         name of the theme.
     * @param aTitle        title of the theme.
     * @param aProperties   theme properties.
     * @param aDefaultTheme default theme to use if value isn't specified.
     */
    public Theme(String aName, String aTitle, Properties aProperties, ITheme aDefaultTheme)
    {
        super(aName, aTitle);

        defaultTheme = aDefaultTheme;
        properties = aProperties;
    }

    /**
     * Returns color defined by the theme.
     *
     * @param key the key.
     *
     * @return color or <code>NULL</code> if color isn't defined by this theme.
     */
    public Color getColor(ThemeKey.Color key)
    {
        Color color;
        String value;

        value = (String)properties.get(key.getKey());
        if (value != null)
        {
            color = Color.decode(value);
        } else
        {
            color = defaultTheme.getColor(key);
        }

        return color;
    }

    /**
     * Returns font defined by the theme.
     *
     * @param key the key.
     *
     * @return font or <code>NULL</code> if font isn't defined by this theme.
     */
    public Font getFont(ThemeKey.Font key)
    {
        Font font = getMainFont();

        if (key != ThemeKey.Font.MAIN)
        {
            int delta = getFontSizeDelta(key);
            if (delta != 0) font = font.deriveFont(font.getSize() + (float)delta);
        }

        return font;
    }

    /**
     * Returns the delta to be applied to the size of the main font to get the font for the given
     * key.
     *
     * @param key the key.
     *
     * @return the delta.
     */
    public int getFontSizeDelta(ThemeKey.Font key)
    {
        if (key == ThemeKey.Font.MAIN) return 0;

        String valKey = key.getKey() + ".sizedelta";
        String value = (String)properties.get(valKey);

        int delta;

        if (value != null)
        {
            delta = Integer.parseInt(value);
        } else
        {
            delta = defaultTheme.getFontSizeDelta(key);
        }

        return delta;
    }

    /**
     * Returns main font.
     *
     * @return main font.
     */
    private Font getMainFont()
    {
        Font font = getMainFontOverride();

        if (font == null) font = getMainFontDirect();

        return font;
    }

    /**
     * Returns the main font specified directly in the theme definition.
     *
     * @return theme font.
     */
    public Font getMainFontDirect()
    {
        Font font;
        ThemeKey.Font key = ThemeKey.Font.MAIN;
        String value = (String)properties.get(key.getKey());

        if (value != null)
        {
            font = Font.decode(value);
        } else
        {
            font = defaultTheme.getFont(key);
        }
        return font;
    }

    /**
     * Returns current main font override.
     *
     * @return the font or <code>NULL</code>.
     */
    public static Font getMainFontOverride()
    {
        return mainFontOverride;
    }

    /**
     * Sets or resets alternative main font.
     *
     * @param font the font or <code>NULL</code>.
     */
    public static void setMainFontOverride(Font font)
    {
        mainFontOverride = font;
    }
}
