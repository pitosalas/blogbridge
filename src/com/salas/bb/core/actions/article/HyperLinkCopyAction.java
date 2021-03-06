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
// $Id: HyperLinkCopyAction.java,v 1.11 2007/05/30 09:59:32 spyromus Exp $
//

package com.salas.bb.core.actions.article;

import com.salas.bb.core.GlobalModel;
import com.salas.bb.utils.CommonUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * Copies hovered hyper-link of currently selected article panel to system clipboard.
 */
public final class HyperLinkCopyAction extends AbstractAction
{
    private static HyperLinkCopyAction instance;
    private static URL link;

    /**
     * Hidden singleton constructor.
     */
    private HyperLinkCopyAction()
    {
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized HyperLinkCopyAction getInstance()
    {
        if (instance == null) instance = new HyperLinkCopyAction();
        return instance;
    }

    /**
     * Invoked when action occurs.
     *
     * @param e action event details object.
     */
    public void actionPerformed(ActionEvent e)
    {
        boolean hrefFormat = GlobalModel.SINGLETON.getUserPreferences().isCopyLinksInHrefFormat();

        CommonUtils.copyURLsToClipboard(new URL[] { link }, hrefFormat);
    }

    /**
     * Sets the link to operate. If the link is <code>NULL</code> the action is
     * disabled.
     *
     * @param aLink link.
     */
    public static void setLink(URL aLink)
    {
        link = aLink;
        instance.setEnabled(link != null);
    }
}
