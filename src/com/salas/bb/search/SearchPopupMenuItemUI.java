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
// $Id: SearchPopupMenuItemUI.java,v 1.1 2006/03/09 12:47:51 spyromus Exp $
//

package com.salas.bb.search;

import javax.swing.*;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

/** Basic search popup menu item UI. */
public class SearchPopupMenuItemUI extends BasicMenuItemUI
{
    private static final int LEFT_WIDTH = 75;
    private static final int RIGHT_WIDTH = 200;

    Rectangle viewR = new Rectangle();
    Rectangle textR = new Rectangle();
    Rectangle iconR = new Rectangle();

    private Color typeForeground;
    private Color typeSelectionForeground;

    /**
     * Creates UI for the component.
     *
     * @param c component.
     *
     * @return UI.
     */
    public static ComponentUI createUI(JComponent c)
    {
        return new SearchPopupMenuItemUI();
    }

    /**
     * Install defaults.
     */
    protected void installDefaults()
    {
        super.installDefaults();

        String prefix = "SearchPopupMenuItem";

        typeForeground = UIManager.getColor(prefix + ".typeForeground");
        if (typeForeground == null) typeForeground = menuItem.getForeground();

        typeSelectionForeground = UIManager.getColor(prefix + ".typeSelectionForeground");
        if (typeSelectionForeground == null) typeSelectionForeground = selectionForeground;

        Color selForeColor = UIManager.getColor(prefix + ".selectionForeground");
        if (selForeColor != null) selectionForeground = selForeColor;
    }

    /**
     * Returns minimum size of the item.
     *
     * @param c component.
     *
     * @return size.
     */
    public Dimension getMinimumSize(JComponent c)
    {
        Font font = c.getFont();
        FontMetrics fm = c.getToolkit().getFontMetrics(font);
        Insets i = c.getInsets();

        int width = LEFT_WIDTH + 1 + RIGHT_WIDTH;
        if (width % 2 == 0) width++;

        int height = fm.getHeight() + i.top + i.bottom;
        if (height % 2 == 0) height++;

        return new Dimension(width, height);
    }

    /**
     * Returns maximum size of the item.
     *
     * @param c component.
     *
     * @return size.
     */
    public Dimension getMaximumSize(JComponent c)
    {
        return getMinimumSize(c);
    }

    /**
     * Returns preferred size of the item.
     *
     * @param c component.
     *
     * @return size.
     */
    public Dimension getPreferredSize(JComponent c)
    {
        return getMinimumSize(c);
    }

    /**
     * Paints menu item.
     *
     * @param g                     context.
     * @param c                     component.
     * @param checkIcon             check-icon.
     * @param arrowIcon             arrow-icon.
     * @param background            background color.
     * @param foreground            foreground color.
     * @param defaultTextIconGap    gap between text and icon.
     */
    protected void paintMenuItem(Graphics g, JComponent c, Icon checkIcon, Icon arrowIcon,
        Color background, Color foreground, int defaultTextIconGap)
    {
        SearchPopupMenuItem menuItem = (SearchPopupMenuItem)c;

        Font holdf = g.getFont();
        Font f = c.getFont();
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics(f);

        // Paint background
        paintBackground(g, menuItem, background);

        Color holdc = g.getColor();
        Insets i = c.getInsets();

        // layout the type text if necessary
        if (menuItem.isFirst())
        {
            iconR.setBounds(0, 0, 0, 0);
            viewR.setBounds(0, i.top, LEFT_WIDTH - 5, fm.getHeight());

            String text = SwingUtilities.layoutCompoundLabel(fm,
                menuItem.getItem().getType().getName(), null,
                SwingUtilities.TOP, SwingUtilities.RIGHT,
                SwingUtilities.TOP, SwingUtilities.RIGHT,
                viewR, iconR, textR, 0);

            paintTypeText(g, fm, textR, text, menuItem);
        }

        // Paint item text
        iconR.setBounds(0, 0, 0, 0);
        viewR.setBounds(LEFT_WIDTH + 1 + 5, i.top,
            RIGHT_WIDTH - 5 - i.right, fm.getHeight());

        String text = SwingUtilities.layoutCompoundLabel(fm,
            menuItem.getItem().toString(), null,
            SwingUtilities.TOP, SwingUtilities.LEFT,
            SwingUtilities.TOP, SwingUtilities.LEFT,
            viewR, iconR, textR, 0);

        paintItemText(g, fm, textR, text, menuItem);

        g.setColor(holdc);
        g.setFont(holdf);
    }

    /**
     * Paints type name text.
     *
     * @param g         graphics context.
     * @param fm        font metrics.
     * @param textR     text rectangle.
     * @param text      text string.
     * @param menuItem  item.
     */
    private void paintTypeText(Graphics g, FontMetrics fm, Rectangle textR, String text,
        SearchPopupMenuItem menuItem)
    {
        boolean armed = menuItem.getModel().isArmed();
        g.setColor(armed ? typeSelectionForeground : typeForeground);

        BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, -1,
            textR.x, textR.y + fm.getAscent());
    }

    /**
     * Paints item text.
     *
     * @param g         graphics context.
     * @param fm        font metrics.
     * @param textR     text rectangle.
     * @param text      text string.
     * @param menuItem  item.
     */
    private void paintItemText(Graphics g, FontMetrics fm, Rectangle textR, String text,
        SearchPopupMenuItem menuItem)
    {
        boolean armed = menuItem.getModel().isArmed();
        g.setColor(armed ? selectionForeground : menuItem.getForeground());

        BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, -1,
            textR.x, textR.y + fm.getAscent());
    }
}
