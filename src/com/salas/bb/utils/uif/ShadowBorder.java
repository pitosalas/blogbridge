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
// $Id: ShadowBorder.java,v 1.3 2006/01/08 05:07:31 kyank Exp $
//

package com.salas.bb.utils.uif;

import com.jgoodies.uif.util.ResourceUtils;

import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 * The border with shadow. Uses three images from resource bundle with keys "shadow.corner",
 * "shadow.fill.v" and "shadow.fill.h". Paints the border <i>around</i> the content.
 */
public class ShadowBorder extends AbstractBorder
{
    private static final Image SHADOW_CORNER = ResourceUtils.getIcon("shadow.corner").getImage();
    private static final Image SHADOW_FILL_H = ResourceUtils.getIcon("shadow.fill.h").getImage();
    private static final Image SHADOW_FILL_V = ResourceUtils.getIcon("shadow.fill.v").getImage();

    private static final Insets INSETS = new Insets(2, 3, 6, 3);
    private final Color color;

    /**
     * Create border with given color.
     *
     * @param aColor color.
     */
    public ShadowBorder(Color aColor)
    {
        color = aColor;
    }

    /**
     * This default implementation returns a new <code>Insets</code> instance where the
     * <code>top</code>, <code>left</code>, <code>bottom</code>, and <code>right</code> fields are
     * set to <code>0</code>.
     *
     * @param c the component for which this border insets value applies
     *
     * @return the new <code>Insets</code> object initialized to 0
     */
    public Insets getBorderInsets(Component c)
    {
        return INSETS;
    }

    /**
     * This default implementation does no painting.
     *
     * @param c      the component for which this border is being painted
     * @param g      the paint graphics
     * @param x      the x position of the painted border
     * @param y      the y position of the painted border
     * @param w      the width of the painted border
     * @param h      the height of the painted border
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h)
    {
        int ch = SHADOW_CORNER.getHeight(null);
        int cw = SHADOW_CORNER.getWidth(null);
        int l = INSETS.left;
        int r = INSETS.right;
        int t = INSETS.top;
        int b = INSETS.bottom;

        // top-left - fill - top-right

        int o = 0;
        g.drawImage(SHADOW_CORNER, x - l, y - t, x - l + cw - o, y,
            o, ch - o, cw + o, ch - t - o, null);
        g.drawImage(SHADOW_CORNER, x - l, y, x, y + ch - t - o,
            o, ch - o - t, o + l, 0,  null);
        g.drawImage(SHADOW_FILL_V, x + cw - l - o, y - t, x + w + r - cw - o, y,
            0, ch - o, 1, ch - t - o, null);
        g.drawImage(SHADOW_CORNER, x + w + r, y - t, x + w + r - cw - o, y,
            o, ch - o, cw + o, ch - t - o, null);
        g.drawImage(SHADOW_CORNER, x + w + r, y, x + w, y + ch - t,
            o, ch - o - t, o + l, 0, null);

        // left fill - right fill
        g.drawImage(SHADOW_FILL_H, x - l, y + ch - t, x, y + h,
            o, 0, o + l, 1, null);
        g.drawImage(SHADOW_FILL_H, x + w + r, y + ch - t, x + w, y + h,
            o, 0, o + l, 1, null);

        // bottom-left - fill - bottom-right
        g.drawImage(SHADOW_CORNER, x - l, y + h, x + cw - l, y + h + b,
            o, ch - o - b, o + cw, ch - o, null);
        g.drawImage(SHADOW_FILL_V, x + cw - l, y + h, x + w + r - cw, y + h + b,
            0, ch - o - b, 1, ch - o, null);
        g.drawImage(SHADOW_CORNER, x + w + r,  y + h, x + w + r - cw, y + h + b,
            o, ch - o - b, o + cw, ch - o, null);

        // border
        g.setColor(color);
        g.drawRect(x, y, w - 1, h - 1);
    }
}
