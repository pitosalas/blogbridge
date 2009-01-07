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
// $Id: ShowServiceDialogAction.java,v 1.9 2006/01/08 04:57:14 kyank Exp $
//

package com.salas.bb.service;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.core.ApplicationLauncher;
import com.salas.bb.utils.ThreadedAction;

import java.awt.event.ActionEvent;

/**
 * Shows BlogBridge Service dialog box.
 */
public final class ShowServiceDialogAction extends ThreadedAction
{
    private static ShowServiceDialogAction instance;

    /** Hidden constructor of singleton. */
    private ShowServiceDialogAction()
    {
        setEnabled(ApplicationLauncher.getConnectionState().isServiceAccessible());
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized ShowServiceDialogAction getInstance()
    {
        if (instance == null) instance = new ShowServiceDialogAction();
        return instance;
    }

    /**
     * Actual action.
     *
     * @param event original event object.
     */
    protected void doAction(ActionEvent event)
    {
        ServiceDialog dialog = new ServiceDialog(GlobalController.SINGLETON.getMainFrame(),
            GlobalModel.SINGLETON.getServicePreferences());

        dialog.open();
    }
}
