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
// $Id: UpdateAllGuidesAction.java,v 1.16 2006/01/08 04:42:25 kyank Exp $
//

package com.salas.bb.core.actions.guide;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.actions.IToolbarCommandAction;
import com.salas.bb.utils.uif.IconSource;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Updates the contents of all guides and channels in application.
 */
public final class UpdateAllGuidesAction extends AbstractAction implements IToolbarCommandAction
{
    private static final String RESOURCE_UPDATE_ALL_GUIDES_TOOLBAR_PRESSED_ICON =
        "toolbar.updateallguides.pressedicon";

    private static UpdateAllGuidesAction instance;

    /**
     * Hidden singleton constructor.
     */
    private UpdateAllGuidesAction()
    {
        setEnabled(false);
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized UpdateAllGuidesAction getInstance()
    {
        if (instance == null) instance = new UpdateAllGuidesAction();
        return instance;
    }

    /**
     * Invoked when action occurs.
     *
     * @param e action event details object.
     */
    public void actionPerformed(final ActionEvent e)
    {
        GlobalController.SINGLETON.getPoller().update();
    }

    /**
     * Return Icon to be used when this command is on the toolbar.
     *
     * @see IToolbarCommandAction#getPressedIcon()
     */
    public Icon getPressedIcon()
    {
        return IconSource.getIcon(RESOURCE_UPDATE_ALL_GUIDES_TOOLBAR_PRESSED_ICON);
    }
}
