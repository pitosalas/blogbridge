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
// $Id: UpDownBorder.java,v 1.3 2006/01/08 05:07:31 kyank Exp $
//

package com.salas.bb.utils.uif;

import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 * Border with two color lines: one at the top and one at the bottom. The top line has
 * white color and the bottom line has defined color.
 */
public class UpDownBorder extends AbstractBorder
{
    private static final Insets INSETS = new Insets(1, 1, 1, 1);

    private final Color color;

    /**
     * Creates border.
     *
     * @param aColor border.
     */
    public UpDownBorder(Color aColor)
    {
        color = aColor;
    }

    /**
     * Returns insets of this border.
     *
     * @param c component.
     *
     * @return insets.
     */
    public Insets getBorderInsets(Component c)
    {
        return INSETS;
    }

    /**
     * Paints border.
     *
     * @param c         component.
     * @param g         graphics context.
     * @param x         x.
     * @param y         y.
     * @param width     width.
     * @param height    height.
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
    {
        g.setColor(Color.WHITE);
        g.drawLine(0, 0, width - 1, 0);

        g.setColor(color);
        g.drawLine(0, height - 1, width - 1, height - 1);
    }
}
