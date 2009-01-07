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
// $Id: FontsHelper.java,v 1.2 2006/01/08 05:07:31 kyank Exp $
//

package com.salas.bb.utils.uif;

import com.salas.bb.utils.StringUtils;

import java.util.*;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

/**
 * Fonts helper assists in choosing the correct font.
 */
public final class FontsHelper
{
    /** Hidden utility class constructor. */
    private FontsHelper()
    {
    }

    /**
     * Chooses font which is the most appropriate to render required characters and
     * preferrably preferred characters. If user has own preferences they are
     * comma-separated in the preferred families list.
     *
     * @param required          characteres which are required to be rendered.
     * @param preferred         preferred characters.
     * @param preferredFamilies preferred font families to be selected.
     *
     * @return font which fits most of all.
     */
    public static Font chooseFont(String required, String preferred, String preferredFamilies)
    {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] allFonts = ge.getAllFonts();

        Font font = selectPreferred(allFonts, preferredFamilies);
        if (font == null)
        {
            Font[] firstSelection = selectFonts(allFonts, required);
            Font[] secondSelection = selectFonts(firstSelection.length > 0
                ? firstSelection : allFonts, preferred);

            font = secondSelection.length > 0 ? secondSelection[0] : null;
        }

        return font;
    }

    /**
     * Selects first plain version of preferred font from the list of available.
     *
     * @param aFonts                fonts to choose from.
     * @param aPreferredFamilies    comma-separated list of preferred families.
     *
     * @return font or <code>NULL</code>.
     */
    private static Font selectPreferred(Font[] aFonts, String aPreferredFamilies)
    {
        Font font = null;

        if (!StringUtils.isEmpty(aPreferredFamilies))
        {
            Map fonts = new HashMap(aFonts.length);
            for (int i = 0; i < aFonts.length; i++)
            {
                Font fnt = aFonts[i];
                if (isPlain(fnt)) fonts.put(fnt.getFamily().toLowerCase(), fnt);
            }

            String[] families = aPreferredFamilies.split(",");
            for (int i = 0; font == null && i < families.length; i++)
            {
                String family = families[i].toLowerCase();
                font = (Font)fonts.get(family);
            }
        }

        return font;
    }

    /**
     * Selects fonts from the given list, which are capable of rendering given string.
     *
     * @param aFonts    fonts to choose from.
     * @param aChars    string to test fonts against.
     *
     * @return fonts capable to render test string.
     */
    private static Font[] selectFonts(Font[] aFonts, String aChars)
    {
        List fonts = new ArrayList(aFonts.length);

        for (int i = 0; i < aFonts.length; i++)
        {
            Font font = aFonts[i];
            if (isPlain(font) && font.canDisplayUpTo(aChars) == -1) fonts.add(font);
        }

        return (Font[])fonts.toArray(new Font[fonts.size()]);
    }

    /**
     * Returns <code>TRUE</code> if font is reported to be plain (neither italic nor bold).
     *
     * @param font  font to check.
     *
     * @return <code>TRUE</code> if font is reported to be plain (neither italic nor bold).
     */
    private static boolean isPlain(Font font)
    {
        String name = font.getFontName().toLowerCase();

        return font.isPlain() &&
            name.indexOf("italic") == -1 &&
            name.indexOf("bold") == -1;
    }
}
