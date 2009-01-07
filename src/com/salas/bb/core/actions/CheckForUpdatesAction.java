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
// $Id: CheckForUpdatesAction.java,v 1.3 2007/10/11 09:09:39 spyromus Exp $
//

package com.salas.bb.core.actions;

import com.jgoodies.uif.application.Application;
import com.salas.bb.core.ApplicationLauncher;
import com.salas.bb.updates.FullCheckCycle;
import com.salas.bb.utils.ThreadedAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Checks with the service for available new versions.
 */
public class CheckForUpdatesAction extends ThreadedAction
{
    private static final CheckForUpdatesAction INSTANCE = new CheckForUpdatesAction();

    /**
     * Creates action.
     */
    private CheckForUpdatesAction()
    {
        setEnabled(false);
    }

    /**
     * Returns instance of this action.
     *
     * @return instance.
     */
    public static CheckForUpdatesAction getInstance()
    {
        return INSTANCE;
    }

    /**
     * Actual action.
     *
     * @param event original event object.
     */
    protected void doAction(ActionEvent event)
    {
        String currentVersion = ApplicationLauncher.getCurrentVersion();
        JFrame frame = Application.getDefaultParentFrame();

        FullCheckCycle checker = new FullCheckCycle(frame, currentVersion, true);
        checker.check();
    }
}
