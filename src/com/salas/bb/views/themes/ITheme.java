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
// $Id: ITheme.java,v 1.5 2006/02/21 15:04:49 spyromus Exp $
//

package com.salas.bb.views.themes;

import java.awt.*;

/**
 * Theme is a named set of visual properties, which are used in the process of displaying
 * components of the application.
 * <p/>
 * Theme has several methods for getting properties of different types:
 * <ul>
 *  <li><b>getColor()</b> - for getting colors.</li>
 *  <li><b>getFont()</b> - for getting fonts.</li>
 * </ul>
 */
public interface ITheme
{
    /**
     * Returns color defined by the theme.
     *
     * @param key   the key.
     *
     * @return color or <code>NULL</code> if color isn't defined by this theme.
     */
    Color getColor(ThemeKey.Color key);

    /**
     * Returns font defined by the theme.
     *
     * @param key   the key.
     *
     * @return font or <code>NULL</code> if font isn't defined by this theme.
     */
    Font getFont(ThemeKey.Font key);

    /**
     * Returns font defined by the theme.
     *
     * @param key   the key.
     * @param bias  integer font bias to apply.
     *
     * @return resultant Font, or <code>NULL</code> if font isn't defined by this theme.
     */
    Font getFontWithBias(ThemeKey.Font key,  int bias);

    /**
     * Returns the delta to be applied to the size of the main font to
     * get the font for the given key.
     *
     * @param key   the key.
     *
     * @return the delta.
     */
    int getFontSizeDelta(ThemeKey.Font key);

    /**
     * Returns the name of this theme.
     *
     * @return the name of the theme.
     */
    String getName();

    /**
     * Human-readable title of the theme.
     *
     * @return the title.
     */
    String getTitle();
}

