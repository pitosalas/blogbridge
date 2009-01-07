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
// $Id: AbstractTheme.java,v 1.3 2006/01/08 05:13:00 kyank Exp $
//

package com.salas.bb.views.themes;

import com.salas.bb.utils.uif.UifUtilities;

import java.awt.*;

/**
 * Abstract theme with name key and title. This implementation "knows" how to
 * do common operations overy the theme data, like returning the biased fonts
 * and string representation.
 */
public abstract class AbstractTheme implements ITheme
{
    private String      name;
    private String      title;

    /**
     * Creates theme with given name key and title.
     *
     * @param aName     name key.
     * @param aTitle    title.
     */
    protected AbstractTheme(String aName, String aTitle)
    {
        name = aName;
        title = aTitle;
    }

    /**
     * Returns font defined by the theme, with an integer bias applied.
     * The bias gets added to the corresponding font size. So all fonts returned by
     * this theme through this method can have a bias applied. This is used for article, title, and
     * all the other fonts.
     *
     * @param key   font key.
     * @param bias  bias amount.
     *
     * @return font or <code>NULL</code> if font isn't defined by this theme.
     */
    public Font getFontWithBias(ThemeKey.Font key, int bias)
    {
        Font font = getFont(key);
        return font == null ? null : UifUtilities.applyFontBias(font, bias);
    }

    /**
     * Returns the name of this theme.
     *
     * @return the name of the theme.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Human-readable title of the theme.
     *
     * @return the title.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Returns string representation.
     *
     * @return string representation.
     */
    public String toString()
    {
        return title;
    }
}
