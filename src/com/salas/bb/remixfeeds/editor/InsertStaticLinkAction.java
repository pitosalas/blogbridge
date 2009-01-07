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
// $Id: InsertStaticLinkAction.java,v 1.2 2006/12/06 11:10:06 spyromus Exp $
//

package com.salas.bb.remixfeeds.editor;

import com.salas.bb.utils.StringUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Holder for the titled link with custom title.
 */
class InsertStaticLinkAction extends AbstractInsertLinkAction
{
    private final String link;
    private final String linkTitle;

    /**
     * Creates holder of the titled link with some custom title.
     *
     * @param editor    editor to use.
     * @param link      link.
     * @param linkTitle link title.
     * @param itemTitle item title to show in the drop-down.
     */
    public InsertStaticLinkAction(JEditorPane editor, String link, String linkTitle, String itemTitle)
    {
        super(editor, itemTitle);

        this.link = link;
        this.linkTitle = linkTitle;
    }

    /**
     * Invoked when action is performed.
     *
     * @param e action.
     */
    public void actionPerformed(ActionEvent e)
    {
        String title = linkTitle;
        if (StringUtils.isNotEmpty(getSelectedText())) title = getSelectedText();

        insertLink(link, title);
    }
}
