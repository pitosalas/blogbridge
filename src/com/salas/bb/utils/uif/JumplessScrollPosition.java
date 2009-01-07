// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2007 by R. Pito Salas
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
// $Id: JumplessScrollPosition.java,v 1.1 2007/06/22 11:34:26 spyromus Exp $
//

package com.salas.bb.utils.uif;

import java.awt.*;

/**
 * Scroll position is measured in pixels along the y-axis. The position
 * is always relative to some anchor component. It can be the head or
 * tail of the component that we use to calculate the offset from. 
 */
public class JumplessScrollPosition
{
    private final Component anchor;
    private final boolean head;
    private final int yOffset;

    /**
     * Creates scroll position object.
     *
     * @param anchor    anchor component.
     * @param head      <code>TRUE</code> if relative to head.
     * @param yoffset   y-offset in pixels.
     */
    public JumplessScrollPosition(Component anchor, boolean head, int yoffset)
    {
        this.anchor = anchor;
        this.head = head;
        this.yOffset = yoffset;
    }

    /**
     * Anchor component.
     *
     * @return anchor component.
     */
    public Component getAnchor()
    {
        return anchor;
    }

    /**
     * Flags if we need to count from the head of the anchor component.
     *
     * @return <code>TRUE</code> means from the head.
     */
    public boolean isHead()
    {
        return head;
    }

    /**
     * Return the offset from the head / tail of the anchor component.
     *
     * @return offset.
     */
    public int getYOffset()
    {
        return yOffset;
    }
}
