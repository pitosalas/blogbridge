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
// $Id: SwitchGuideAction.java,v 1.12 2006/01/27 15:23:23 spyromus Exp $
//

package com.salas.bb.core.actions.guide;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.IGuide;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * This action switches guides. It takes ID of the guide from the <code>actionCommand</code>
 * property. Trick here is that this command is automatically set to be equal to the key
 * character pressed. Please take this into account when consider refactoring or
 * change of shortcuts.
 */
public final class SwitchGuideAction extends AbstractAction
{
    private static SwitchGuideAction instance;

    /**
     * Hidden constructor of singleton class.
     */
    private SwitchGuideAction()
    {
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized SwitchGuideAction getInstance()
    {
        if (instance == null)
        {
            instance = new SwitchGuideAction();
        }
        return instance;
    }

    /**
     * Invoked when action occurs.
     *
     * @param e action event details object.
     */
    public void actionPerformed(final ActionEvent e)
    {
        // Tricky part here. <code>ActionCommand</code> contains character of a pressed key.
        // As soon as we use CTRL-number sequence we can freely convert this code into number and
        // use for our own purpose.
        int index = Integer.parseInt(e.getActionCommand()) - 1;
        final GuidesSet cgs = GlobalModel.SINGLETON.getGuidesSet();
        if (index < cgs.getGuidesCount())
        {
            final IGuide guide = cgs.getGuideAt(index);
            if (GlobalModel.SINGLETON.getSelectedGuide() != guide)
            {
                // EDT !!!
                GlobalController.SINGLETON.selectGuideAndFeed(guide);
            }
        }
    }
}
