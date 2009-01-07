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
// $Id $
//

package com.salas.bb.core.actions.article;

import com.salas.bb.utils.CommonUtils;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.views.feeds.html.HTMLFeedDisplay;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Copies the text from an article to the clipboard.
 */
public final class SelectedTextCopyAction extends AbstractAction
{
    private static SelectedTextCopyAction instance;
    private static HTMLFeedDisplay display;

    /**
     * Hidden singleton constructor.
     */
    private SelectedTextCopyAction()
    {
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized SelectedTextCopyAction getInstance()
    {
        if (instance == null) instance = new SelectedTextCopyAction();
        return instance;
    }

    /**
     * Invoked when action occurs.
     *
     * @param e action event details object.
     */
    public void actionPerformed(ActionEvent e)
    {
        CommonUtils.copyTextToClipboard(display.getSelectedText());
    }

    /**
     * Sets the display component to monitor.
     *
     * @param disp display.
     */
    public static void setDisplay(HTMLFeedDisplay disp)
    {
        display = disp;
        instance.setEnabled(display != null && !StringUtils.isEmpty(display.getSelectedText()));
    }
}
