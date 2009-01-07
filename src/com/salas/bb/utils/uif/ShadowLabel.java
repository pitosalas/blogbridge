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
// $Id: ShadowLabel.java,v 1.3 2006/01/08 05:07:31 kyank Exp $
//

package com.salas.bb.utils.uif;

import javax.swing.*;
import java.awt.*;

/**
 * Label with a small white shadow.
 */
public class ShadowLabel extends JLabel
{
    private final Color shadowColor;

    private boolean shadow = false;

    /**
     * Creates a shadow label.
     *
     * @param aShadowColor  shadow color.
     */
    public ShadowLabel(Color aShadowColor)
    {
        shadowColor = aShadowColor;
    }

    /**
     * Creates a shadow label.
     *
     * @param text                The text to be displayed by the label.
     * @param icon                The image to be displayed by the label.
     * @param horizontalAlignment One of the following constants defined in <code>SwingConstants</code>:
     *                            <code>LEFT</code>, <code>CENTER</code>, <code>RIGHT</code>,
     *                            <code>LEADING</code> or <code>TRAILING</code>.
     * @param aShadowColor        color of the shadow.
     */
    public ShadowLabel(String text, Icon icon, int horizontalAlignment, Color aShadowColor)
    {
        super(text, icon, horizontalAlignment);
        shadowColor = aShadowColor;
    }

    /**
     * Returns the color of text. When painting shadow it returns the shadow color.
     *
     * @return color.
     */
    public Color getForeground()
    {
        return shadow ? shadowColor : super.getForeground();
    }

    /**
     * Paints a shadow and component.
     *
     * @param g graphics context.
     */
    protected void paintComponent(Graphics g)
    {
        Rectangle b = getBounds();
        Graphics g2 = g.create(0, 1, b.width, b.height);

        shadow = true;
        super.paintComponent(g2);

        shadow = false;
        super.paintComponent(g);
    }
}
