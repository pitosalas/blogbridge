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
// $Id: DNDPopup.java,v 1.3 2006/01/11 09:13:35 spyromus Exp $
//

package com.salas.bb.utils.dnd;

import com.salas.bb.utils.uif.IconSource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * Popup with image of dragged component.
 */
public class DNDPopup extends JPopupMenu implements MouseMotionListener
{
    private static final Icon COPY_ICON;
    private static final int COPY_ICON_WIDTH;
    private static final int COPY_ICON_HEIGHT;

    private static final int OFFSET_RIGHT = 10;
    private static final int OFFSET_DOWN = 10;

    private JLabel label;
    private boolean copying;

    static
    {
        COPY_ICON = IconSource.getIcon("copy.mac.icon");
        COPY_ICON_WIDTH = COPY_ICON.getIconWidth();
        COPY_ICON_HEIGHT = COPY_ICON.getIconHeight();
    }

    /**
     * Creates popup window.
     */
    public DNDPopup()
    {
        copying = false;
        setBorder(BorderFactory.createEmptyBorder());
        setDoubleBuffered(true);

        label = new JLabel();
        add(label);
    }

    /**
     * Set <code>TRUE</code> when the item is being copied.
     *
     * @param flag <code>TRUE</code> to indicate copy operation.
     */
    public void setCopying(boolean flag)
    {
        copying = flag;
        if (isVisible()) repaint();
    }

    /**
     * Sets the image to be displayed by tool tip.
     *
     * @param img image to display.
     */
    public void setImage(final Image img)
    {
        if (img == null) return;
        label.setIcon(new ImageIcon(img));
        pack();
    }

    /**
     * Moves this component to a new location. The top-left corner of
     * the new location is specified by point <code>p</code>. Point
     * <code>p</code> is given in the parent's coordinate space.
     *
     * @param p the point defining the top-left corner
     *          of the new location, given in the coordinate space of this
     *          component's parent
     *
     * @see #getLocation
     * @see #setBounds
     * @since JDK1.1
     */
    public void setLocation(Point p)
    {
        final Point i = getInvoker().getLocationOnScreen();
        setLocation(p.x + i.x + OFFSET_RIGHT, p.y + i.y + OFFSET_DOWN);
    }

    /**
     * Perform custom painting.
     *
     * @param g graphics context.
     */
    public void paint(Graphics g)
    {
        label.getIcon().paintIcon(null, g, 0, 0);

        if (copying)
        {
            Dimension size = getSize();
            COPY_ICON.paintIcon(this, g,
                size.width - COPY_ICON_WIDTH, size.height - COPY_ICON_HEIGHT);
        }
    }

    /**
     * Invoked when a mouse button is pressed on a component and then
     * dragged.  <code>MOUSE_DRAGGED</code> events will continue to be
     * delivered to the component where the drag originated until the
     * mouse button is released (regardless of whether the mouse position
     * is within the bounds of the component).
     * <p/>
     * Due to platform-dependent Drag&Drop implementations,
     * <code>MOUSE_DRAGGED</code> events may not be delivered during a native
     * Drag&Drop operation.
     *
     * @param e mouse event object.
     */
    public void mouseDragged(MouseEvent e)
    {
        if (isVisible()) setLocation(e.getPoint());
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     *
     * @param e mouse event object.
     */
    public void mouseMoved(MouseEvent e)
    {
    }
}
