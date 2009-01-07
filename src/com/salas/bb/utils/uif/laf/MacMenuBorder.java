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
// $Id: MacMenuBorder.java,v 1.1 2006/02/01 15:08:40 spyromus Exp $
//

package com.salas.bb.utils.uif.laf;

import javax.swing.border.Border;
import javax.swing.*;
import java.awt.*;

/**
 * Alternative border for Mac popup menus.
 */
public class MacMenuBorder implements Border
{
    private static final Insets menuInsets = new Insets(4, 0, 4, 0);
    private static final Insets itemInsets = new Insets(0, 0, 0, 0);

    /**
     * Returns the insets of the border.
     *
     * @param component the component for which this border insets value applies.
     */
    public Insets getBorderInsets(Component component)
    {
        boolean isMenu = component instanceof JPopupMenu;
        return (Insets)(isMenu ? MacMenuBorder.menuInsets : MacMenuBorder.itemInsets).clone();
    }

    /**
     * Returns whether or not the border is opaque.  If the border
     * is opaque, it is responsible for filling in it's own
     * background when painting.
     *
     * @return false.
     */
    public boolean isBorderOpaque()
    {
        return false;
    }

    /**
     * Paints the border for the specified component with the specified
     * position and size.
     *
     * @param component the component for which this border is being painted
     * @param graphics the paint graphics
     * @param x the x position of the painted border
     * @param y the y position of the painted border
     * @param width the width of the painted border
     * @param height the height of the painted border
     */
    public void paintBorder(Component component, Graphics graphics, int x,
        int y, int width, int height)
    {
    }
}
