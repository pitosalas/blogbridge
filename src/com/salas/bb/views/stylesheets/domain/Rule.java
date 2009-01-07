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
// $Id: Rule.java,v 1.3 2006/10/18 08:00:05 spyromus Exp $
//

package com.salas.bb.views.stylesheets.domain;

import javax.swing.*;
import java.awt.*;

/**
 * Single style sheet rule.
 */
public class Rule implements IRule
{
    private Font    font;
    private Color   color;
    private String  iconURL;
    private Icon    icon;

    /**
     * Creates empty rule.
     */
    public Rule()
    {
    }

    /**
     * Creates the rule.
     *
     * @param font      font.
     * @param color     color.
     * @param iconURL   icon URL.
     */
    public Rule(Font font, Color color, String iconURL)
    {
        this.font = font;
        this.color = color;
        this.iconURL = iconURL;
    }

    /**
     * Returns the font.
     *
     * @return font.
     */
    public Font getFont()
    {
        return font;
    }

    /**
     * Sets the font.
     *
     * @param font font.
     */
    public void setFont(Font font)
    {
        this.font = font;
    }

    /**
     * Returns the color.
     *
     * @return color.
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * Sets the color.
     *
     * @param color color.
     */
    public void setColor(Color color)
    {
        this.color = color;
    }

    /**
     * Returns the icon URL.
     *
     * @return icon URL.
     */
    public String getIconURL()
    {
        return iconURL;
    }

    /**
     * Sets icon URL.
     *
     * @param iconURL URL.
     */
    public void setIconURL(String iconURL)
    {
        this.iconURL = iconURL;
    }

    /**
     * Returns icon from cached storage.
     *
     * @return icon.
     */
    public Icon getIcon()
    {
        return icon;
    }

    /**
     * Sets icon for cached storage.
     *
     * @param icon icon.
     */
    public void setIcon(Icon icon)
    {
        this.icon = icon;
    }

    /**
     * Creates an overriden copy of itself.
     *
     * @param rule rule to override with.
     *
     * @return rule.
     */
    public IRule overrideWith(IRule rule)
    {
        return new Rule(
            rule.getFont() != null ? rule.getFont() : font,
            rule.getColor() != null ? rule.getColor() : color,
            rule.getIconURL() != null ? rule.getIconURL() : iconURL
        );
    }
}
