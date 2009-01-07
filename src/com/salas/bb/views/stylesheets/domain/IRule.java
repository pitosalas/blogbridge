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
// $Id: IRule.java,v 1.2 2006/10/16 16:40:03 spyromus Exp $
//

package com.salas.bb.views.stylesheets.domain;

import javax.swing.*;
import java.awt.*;

/**
 * Rule interface.
 */
public interface IRule
{
    /**
     * Returns the font.
     *
     * @return font.
     */
    Font getFont();

    /**
     * Returns the color.
     *
     * @return color.
     */
    Color getColor();

    /**
     * Returns the icon URL.
     *
     * @return icon URL.
     */
    String getIconURL();

    /**
     * Creates an overriden copy of itself.
     *
     * @param rule rule to override with.
     *
     * @return rule.
     */
    IRule overrideWith(IRule rule);

    /**
     * Returns icon from cached storage.
     *
     * @return icon.
     */
    Icon getIcon();

    /**
     * Sets icon for cached storage.
     *
     * @param icon icon.
     */
    void setIcon(Icon icon);
}
