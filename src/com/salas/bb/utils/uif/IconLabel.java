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
// $Id: IconLabel.java,v 1.3 2006/01/08 05:07:31 kyank Exp $
//

package com.salas.bb.utils.uif;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.*;

/**
 * Label, which looks like a button, but does not actual effect.
 */
public class IconLabel extends JLabel
{
    private Icon    normal;
    private Icon    hover;

    /**
     * Creates label with icons.
     *
     * @param aNormal   normal icon.
     * @param aHover    hover icon (optional).
     */
    public IconLabel(Icon aNormal, Icon aHover)
    {
        setIcons(aNormal, aHover);

        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    }

    /**
     * Process mouse events.
     *
     * @param e mouse events.
     */
    protected void processMouseEvent(MouseEvent e)
    {
        super.processMouseEvent(e);

        switch (e.getID())
        {
            case MouseEvent.MOUSE_ENTERED:
                if (hover != null) setIcon(hover);
                break;
            case MouseEvent.MOUSE_EXITED:
                setIcon(normal);
                break;
            case MouseEvent.MOUSE_PRESSED:
                setIcon(normal);
                break;
            case MouseEvent.MOUSE_RELEASED:
                if (hover != null && contains(e.getPoint())) setIcon(hover);
                break;
            default:
                break;
        }
    }

    /**
     * Sets new icons.
     *
     * @param aNormal   normal icon.
     * @param aHover    hover icon (optional).
     */
    public void setIcons(Icon aNormal, Icon aHover)
    {
        if (hover != null && aHover != null && getIcon() == hover)
        {
            setIcon(aHover);
        } else setIcon(aNormal);

        normal = aNormal;
        hover = aHover;
    }
}
