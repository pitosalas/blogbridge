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
// $Id: LinkTrackHTMLEditorKit.java,v 1.5 2006/09/22 14:32:20 spyromus Exp $
//

package com.salas.bb.utils.uif.html;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/**
 * <code>HTMLEditorKit</code> that tracks the mouse entering and exiting
 * the <code>JEditorKit</code> so that hyperlink mouseover/mouseout events
 * are not lost.
 */
public class LinkTrackHTMLEditorKit extends HTMLEditorKit
{
    /**
     * The hyperlink mouse event listener.
     */
    private LinkController linkHandler = new LinkController();

    /**
     * Called when the kit is being installed into the a JEditorPane.
     * Installs a custom mouse listener to replace the one provided by the SDK.
     *
     * @param c the JEditorPane
     */
    public void install(JEditorPane c)
    {
        super.install(c);

        // Remove built-in mouse event tracker
        MouseListener[] listeners = c.getMouseListeners();
        for (int i = 0; i < listeners.length; i++)
        {
            if (listeners[i] instanceof HTMLEditorKit.LinkController)
            {
                c.removeMouseListener(listeners[i]);
                c.removeMouseMotionListener((MouseMotionListener)listeners[i]);
            }
        }

        // Install our own hyperlink mouse event tracker
        c.addMouseListener(linkHandler);
        c.addMouseMotionListener(linkHandler);
        c.addMouseWheelListener(linkHandler);
    }

    /**
     * Called when the kit is being removed from the JEditorPane.
     * Uninstalls our custom mouse listener.
     *
     * @param c the JEditorPane
     */
    public void deinstall(JEditorPane c)
    {
        super.deinstall(c);

        // Remove our hyperlink mouse event listener
        c.removeMouseListener(linkHandler);
        c.removeMouseMotionListener(linkHandler);
        c.removeMouseWheelListener(linkHandler);
    }

    /**
     * Class to watch the associated component and fire hyperlink events on it when appropriate.
     * Adds tracking of mouse exit events to that link hovers are not left lingering.
     */
    public static class LinkController extends HTMLEditorKit.LinkController
        implements MouseWheelListener
    {
        /**
         * Invoked when the mouse exits the component. Processes this event just like
         * a mouse movement to outside the component, which clears any lingering
         * hyperlink hovers.
         *
         * @param e The mouse event
         */
        public void mouseExited(MouseEvent e)
        {
            // Patch the MouseEvent to indicate a position outside the component
            MouseEvent e2 = new MouseEvent(e.getComponent(), e.getID(), e.getWhen(),
                    e.getModifiers(), -1, -1, e.getClickCount(), e.isPopupTrigger(),
                    e.getButton());

            // Treat like a mouse movement event to a position outside the component
            super.mouseMoved(e2);

            // Just in case the superclass does something intelligent in a future SDK
            super.mouseExited(e);
        }

        /**
         * {@inheritDoc}
         */
        public void mouseWheelMoved(MouseWheelEvent e)
        {
            Object source = e.getSource();
            Component cmp = (Component)source;

            // Dispatch the event to make the page scroll if necessary
            Container parent = cmp.getParent();
            if (parent != null) parent.dispatchEvent(e);

            // This is a dirty hack to never show link cursor and selected link after wheeling.
            super.mouseMoved(new MouseEvent(cmp, MouseEvent.MOUSE_MOVED, e.getWhen(), 0, -1, -1, 0, false));
        }
    }
}