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
// $Id: UpdateGuideAction.java,v 1.9 2006/01/08 04:42:25 kyank Exp $
//

package com.salas.bb.core.actions.guide;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.IGuide;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Reloads currently selected channel from its source URL.
 *
 * Action is enabled/disabled by <code>ActionsManager</code>.
 */
public final class UpdateGuideAction extends AbstractAction
{
    private static UpdateGuideAction instance;

    /**
     * Hidden singleton constructor.
     */
    private UpdateGuideAction()
    {
        setEnabled(false);
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized UpdateGuideAction getInstance()
    {
        if (instance == null) instance = new UpdateGuideAction();
        return instance;
    }

    /**
     * Invoked when action occurs.
     *
     * @param e action event details object.
     */
    public void actionPerformed(ActionEvent e)
    {
        final IGuide[] selectedGuides = GlobalController.SINGLETON.getSelectedGuides();
        for (int i = 0; i < selectedGuides.length; i++)
        {
            IGuide selectedGuide = selectedGuides[i];
            GlobalController.SINGLETON.getPoller().update(selectedGuide);
        }
    }
}
