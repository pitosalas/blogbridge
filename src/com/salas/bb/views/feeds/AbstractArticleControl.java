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
// $Id: AbstractArticleControl.java,v 1.2 2007/11/08 12:22:07 spyromus Exp $
//

package com.salas.bb.views.feeds;

import com.salas.bb.domain.IArticle;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;

/**
 * Abstract article control.
 */
public abstract class AbstractArticleControl extends JLabel
{
    private final Icon iconSel;
    private final Icon iconUnsel;

    private final IArticle article;
    private String msgUnsel;
    private String msgSel;

    /**
     * Creates pin icon.
     *
     * @param article   article of the icon.
     * @param iconSel   selected icon.
     * @param iconUnsel deselected icon.
     * @param msgUnsel  message to show when deselected.
     * @param msgSel    message to show when selected.
     */
    public AbstractArticleControl(IArticle article, Icon iconSel, Icon iconUnsel, String msgUnsel, String msgSel)
    {
        super(iconSel);
        this.article = article;
        this.iconSel = iconSel;
        this.iconUnsel = iconUnsel;
        this.msgUnsel = MessageFormat.format(Strings.message("articledisplay.abstract.action"), msgUnsel).intern();
        this.msgSel = MessageFormat.format(Strings.message("articledisplay.abstract.action"), msgSel).intern();

        updateState();

        enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
    }

    /**
     * Tracks clicks.
     *
     * @param e event.
     */
    protected void processMouseEvent(MouseEvent e)
    {
        if (e.getID() == MouseEvent.MOUSE_PRESSED && article != null)
        {
            onToggleState(article);
            updateState();
        } else if (e.getID() == MouseEvent.MOUSE_ENTERED)
        {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else if (e.getID() == MouseEvent.MOUSE_EXITED)
        {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Invoked when article state is toggled.
     *
     * @param article article.
     */
    protected abstract void onToggleState(IArticle article);

    /**
     * Invoked when the control needs to know current article state.
     *
     * @param article article.
     *
     * @return <code>TRUE</code> to show selected icon and message.
     */
    protected abstract boolean getCurrentState(IArticle article);

    /**
     * Updates pin state.
     */
    public void updateState()
    {
        boolean selected = article != null && getCurrentState(article);
        setIcon(selected ? iconSel : iconUnsel);
        setToolTipText(selected ? msgUnsel : msgSel);
    }
}