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
// $Id: LinkLabel.java,v 1.15 2007/09/26 11:04:03 spyromus Exp $
//

package com.salas.bb.utils.uif;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.utils.BrowserLauncher;
import com.salas.bb.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * LinkLabel is label which holds the link to somewhere.
 */
public class LinkLabel extends JLabel
{
    /** The color the link is highlighted. */
    public static final Color HIGHLIGHT_COLOR = Color.BLUE;

    private URL     link;
    private Color   originalForeground;
    private boolean highlightLink;

    /**
     * Creates pure link label without link.
     */
    public LinkLabel()
    {
        this(null);
    }

    /**
     * Creates pure link label without link.
     *
     * @param text  text to put.
     */
    public LinkLabel(String text)
    {
        this(text, null);
    }

    /**
     * Creates pure link label without link.
     *
     * @param text  text to put.
     * @param link  the link.
     */
    public LinkLabel(String text, String link)
    {
        super(text);

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        originalForeground = getForeground();
        highlightLink = false;

        try
        {
            setLink(new URL(link));
        } catch (MalformedURLException e)
        {
            // Not a problem
        }

        super.setForeground(LinkLabel.HIGHLIGHT_COLOR);
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
     * Returns link of current label.
     *
     * @return URL
     */
    public URL getLink()
    {
        return link;
    }

    /**
     * Binds this label to new link.
     *
     * @param aLink new link URL.
     */
    public void setLink(URL aLink)
    {
        this.link = aLink;
        highlightLink = ((aLink != null) &&
            !(Constants.EMPTY_STRING.equals(aLink.toString().trim())));

        String fullLink = aLink != null ? aLink.toString() : Constants.EMPTY_STRING;
        setToolTipText(fullLink);
    }

    /**
     * Sets the highlight link flag.
     *
     * @param high <code>TRUE</code> to highlight link.
     */
    public void setHighlightLink(boolean high)
    {
        highlightLink = high;
        if (high) super.setForeground(originalForeground);
    }

    /**
     * Returns status to be displayed.
     *
     * @return status.
     */
    protected String getStatus()
    {
        return link == null ? null : link.toString();
    }

    /**
     * Handles the event.
     *
     * @param e event.
     */
    protected void processMouseEvent(MouseEvent e)
    {
        super.processMouseEvent(e);

        switch (e.getID())
        {
            case MouseEvent.MOUSE_CLICKED:
                if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1)
                {
                    doAction();
                }
                break;

            case MouseEvent.MOUSE_ENTERED:
                if (highlightLink)
                {
                    super.setForeground(HIGHLIGHT_COLOR);
                    GlobalController.SINGLETON.setHoveredHyperLink(link);
                }
                break;

            case MouseEvent.MOUSE_EXITED:
                if (highlightLink)
                {
                    super.setForeground(originalForeground);
                    GlobalController.SINGLETON.setHoveredHyperLink(null);
                }
                break;
        }
    }

    /**
     * Invoked when user clicks on the link.
     */
    protected void doAction()
    {
        URL aLink = getLink();
        UserPreferences prefs = GlobalModel.SINGLETON.getUserPreferences();
        if (aLink != null) BrowserLauncher.showDocument(aLink, prefs.getInternetBrowser());
    }

    /**
     * Creates a label that does something different than the default action
     * (opening the browser window) when clicked.
     *
     * @param text      text.
     * @param action    action to invoke.
     *
     * @return label.
     */
    public static LinkLabel create(String text, final Action action)
    {
        LinkLabel link = new LinkLabel(text)
        {
            @Override
            protected void doAction()
            {
                action.actionPerformed(null);
            }
        };
        link.setHighlightLink(true);
        return link;
    }
}
