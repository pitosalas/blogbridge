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
// $Id: SelectiveShowTagsAction.java,v 1.5 2006/01/08 04:57:14 kyank Exp $
//

package com.salas.bb.tags;

import com.salas.bb.core.GlobalModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * This action opens one dialog or another (feeds vs articles tags) depending
 * on the current focus ownership.
 */
public class SelectiveShowTagsAction extends AbstractAction
{
    private static final SelectiveShowTagsAction INSTANCE = new SelectiveShowTagsAction();

    /**
     * Hidden singleton constructor.
     */
    private SelectiveShowTagsAction()
    {
        // Enableness is adjusted by AcctionsMonitor
        setEnabled(false);
    }

    /**
     * Returns instance of action.
     *
     * @return instance of action.
     */
    public static SelectiveShowTagsAction getInstance()
    {
        return INSTANCE;
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e action event.
     */
    public void actionPerformed(ActionEvent e)
    {
        AbstractShowTagsAction action = getTagsAction();
        action.actionPerformed(e);
    }

    /**
     * Returns action for showing tags window for currently focused object.
     * If article panel is focused then it will be articles tags, otherwise it
     * will be feeds tags.
     *
     * @return action.
     */
    private AbstractShowTagsAction getTagsAction()
    {
        AbstractShowTagsAction action;

        KeyboardFocusManager keyFocusManager =
            KeyboardFocusManager.getCurrentKeyboardFocusManager();

        Component focused = keyFocusManager.getFocusOwner();

        if (!(focused instanceof JList) &&
            GlobalModel.SINGLETON.getSelectedArticle() != null)
        {
            action = ShowArticleTagsAction.getInstance();
        } else
        {
            action = ShowFeedTagsAction.getInstance();
        }

        return action;
    }
}
