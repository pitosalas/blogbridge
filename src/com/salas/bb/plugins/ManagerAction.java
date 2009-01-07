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
// $Id: ManagerAction.java,v 1.2 2007/04/06 10:16:10 spyromus Exp $
//

package com.salas.bb.plugins;

import com.jgoodies.uif.application.Application;
import com.salas.bb.core.GlobalController;
import com.salas.bb.plugins.gui.ManagerDialog;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.views.mainframe.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * The action calling the manager.
 */
public class ManagerAction extends AbstractAction
{
    private static ManagerAction instance;

    /** Creates action. */
    private ManagerAction()
    {
    }

    /**
     * Returns instance of the action.
     *
     * @return instance.
     */
    public static synchronized ManagerAction getInstance()
    {
        if (instance == null) instance = new ManagerAction();
        return instance;
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e event object.
     */
    public void actionPerformed(ActionEvent e)
    {
        MainFrame mainFrame = GlobalController.SINGLETON.getMainFrame();
        ManagerDialog dialog = new ManagerDialog(mainFrame);
        boolean changed = dialog.openDialog();

        if (changed)
        {
            String btnLater = Strings.message("plugin.manager.exit.later");
            String btnNow = Strings.message("plugin.manager.exit.now");
            Object[] options = new Object[] { btnLater, btnNow };

            int res = JOptionPane.showOptionDialog(mainFrame,
                Strings.message("plugin.manager.exit.wording"),
                dialog.getTitle(),
                -1,
                JOptionPane.QUESTION_MESSAGE,
                ManagerDialog.getIcon(),
                options,
                btnLater);

            if (res > -1 && options[res] == btnNow)
            {
                Application.close();
            }
        }
    }
}
