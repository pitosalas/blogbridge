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
// $Id: MacPopupMenuSeparatorUI.java,v 1.1 2006/02/03 13:27:17 spyromus Exp $
//

package com.salas.bb.utils.uif.laf;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSeparatorUI;
import java.awt.*;

/**
 * Custom popup menu separator.
 */
public class MacPopupMenuSeparatorUI extends BasicSeparatorUI
{
    /** Margin above and below the lines. */
    private static final int margin = 6;

    /**
     * Initializes default colors.
     *
     * @param s separator component.
     */
    protected void installDefaults(JSeparator s)
    {
        LookAndFeel.installColors(s, "Separator.background", "Separator.foreground");
    }

    /**
     * Creates the UI for the component.
     *
     * @param c component.
     *
     * @return UI.
     */
    public static ComponentUI createUI(JComponent c)
    {
        return new MacPopupMenuSeparatorUI();
    }

    /**
     * Paints the component.
     *
     * @param g graphics context.
     * @param c component.
     */
    public void paint(Graphics g, JComponent c)
    {
        Dimension s = c.getSize();

        g.setColor(c.getForeground());
        g.drawLine(1, margin, s.width - 2, margin);

        g.setColor(c.getBackground());
        g.drawLine(1, margin + 1, s.width - 2, margin + 1);
    }

    /**
     * Returns the prefered size for the component.
     *
     * @param c component.
     *
     * @return the size.
     */
    public Dimension getPreferredSize(JComponent c)
    {
        return new Dimension(0, margin * 2 + 2);
    }
}




