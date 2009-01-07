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
// $Id: LAFTheme.java,v 1.13 2008/02/28 15:59:52 spyromus Exp $
//

package com.salas.bb.views.themes;

import javax.swing.*;
import java.awt.*;

/**
 * Theme which is completely based on current LAF.
 */
public class LAFTheme extends AbstractTheme
{
    private static final String NAME = "LAF";
    private static final String TITLE = "Look And Feel";

    private static final JLabel LABEL = new JLabel();
    private static final JList  LIST = new JList();

    /**
     * Creates default LAF theme.
     */
    public LAFTheme()
    {
        super(NAME, TITLE);
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
        Color color = null;

        if ((ThemeKey.Color.ARTICLE_DATE_SEL_FG == key) ||
            (ThemeKey.Color.ARTICLE_DATE_UNSEL_FG == key) ||
            (ThemeKey.Color.ARTICLE_TEXT_SEL_FG == key) ||
            (ThemeKey.Color.ARTICLE_TEXT_UNSEL_FG == key) ||
            (ThemeKey.Color.ARTICLE_TITLE_SEL_FG == key) ||
            (ThemeKey.Color.ARTICLE_TITLE_UNSEL_FG == key) ||
            (ThemeKey.Color.ARTICLEGROUP_FG == key))
        {
            color = UIManager.getColor("Label.foreground");
        } else if (ThemeKey.Color.ARTICLELIST_FEEDNAME_FG == key)
        {
            color = UIManager.getColor("SimpleInternalFrame.activeTitleForeground");
            if (color == null) color = UIManager.getColor("InternalFrame.activeTitleForeground");
            if (color == null) color = UIManager.getColor("Label.foreground");
        } else if ((ThemeKey.Color.ARTICLE_SEL_BG == key) ||
            (ThemeKey.Color.FEEDSLIST_SEL_BG == key))
        {
            color = LIST.getSelectionBackground();
        } else if ((ThemeKey.Color.ARTICLE_UNSEL_BG == key) ||
            (ThemeKey.Color.ARTICLEGROUP_BG == key) ||
            (ThemeKey.Color.ARTICLELIST_BG == key) ||
            (ThemeKey.Color.FEEDSLIST_BG == key) ||
            (ThemeKey.Color.FEEDSLIST_ALT_BG == key))
        {
            color = LIST.getBackground();
        } else if ((ThemeKey.Color.BLOGLINK_DISC_BG == key) ||
            (ThemeKey.Color.BLOGLINK_UNDISC_BG == key))
        {
            // TODO change this to more appropriate value
            color = LIST.getSelectionBackground();
        } else if (ThemeKey.Color.FEEDSLIST_FG == key)
        {
            color = LIST.getForeground();
        } else if (ThemeKey.Color.FEEDSLIST_SEL_FG == key)
        {
            color = LIST.getSelectionForeground();
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
        Font font = null;

        if ((ThemeKey.Font.ARTICLE_TEXT == key) ||
            (ThemeKey.Font.ARTICLE_TITLE == key) ||
            (ThemeKey.Font.ARTICLE_DATE == key) ||
            (ThemeKey.Font.ARTICLEGROUP == key) ||
            (ThemeKey.Font.ARTICLELIST_FEEDNAME == key) ||
            (ThemeKey.Font.MAIN == key))
        {
            font = LABEL.getFont();
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
        return 0;
    }
}
