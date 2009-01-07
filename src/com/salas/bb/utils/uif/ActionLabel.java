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
// $Id: ActionLabel.java,v 1.2 2006/01/08 05:07:31 kyank Exp $
//

package com.salas.bb.utils.uif;

import com.salas.bb.core.GlobalController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Label which behaves as link, but calls some action listener.
 */
public final class ActionLabel extends JLabel implements MouseListener
{
    private ActionListener  listener;
    private String          overText;
    private Color           originalForeground;

    /**
     * Creates label.
     *
     * @param text      text of label.
     * @param listener  listener.
     */
    public ActionLabel(String text, ActionListener listener)
    {
        this(text, listener, null);
    }

    /**
     * Creates label.
     *
     * @param text      text of label.
     * @param listener  listener.
     * @param overText  text to display in status line when mouse is over the label.
     */
    public ActionLabel(String text, ActionListener listener, String overText)
    {
        setText(text);
        setOverText(overText);
        setActionListener(listener);

        addMouseListener(this);
        originalForeground = getForeground();
    }

    /**
     * Sets the listener of click events.
     *
     * @param aListener listener of action.
     */
    public void setActionListener(ActionListener aListener)
    {
        listener = aListener;
    }

    /**
     * Sets over text.
     *
     * @param aText text to display in status line when mouse is over the lable.
     */
    public void setOverText(String aText)
    {
        overText = aText;
    }

    /**
     * Intercepts the call and saves foreground.
     *
     * @param fg color.
     */
    public void setForeground(Color fg)
    {
        super.setForeground(fg);
        originalForeground = fg;
    }

    /**
     * Invoked when the mouse has been clicked on a component.
     *
     * @param e event object
     */
    public void mouseClicked(final MouseEvent e)
    {
        if ((e.getClickCount() == 1) && (e.getButton() == MouseEvent.BUTTON1))
        {
            listener.actionPerformed(new ActionEvent(this, 0, getText()));
        }
    }

    /**
     * Invoked when the mouse enters a component.
     *
     * @param e event object
     */
    public void mouseEntered(final MouseEvent e)
    {
        super.setForeground(Color.BLUE);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        GlobalController.SINGLETON.setStatus(overText);
    }

    /**
     * Invoked when the mouse exits a component.
     *
     * @param e event object
     */
    public void mouseExited(final MouseEvent e)
    {
        super.setForeground(originalForeground);
        this.setCursor(Cursor.getDefaultCursor());
        GlobalController.SINGLETON.setStatus(null);
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param e event object
     */
    public void mousePressed(final MouseEvent e)
    {
    }

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param e event object
     */
    public void mouseReleased(final MouseEvent e)
    {
    }
}
