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
// $Id: DeleteGuide.java,v 1.1 2007/10/04 13:29:47 spyromus Exp $
//

package com.salas.bb.reports.actions;

import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.IGuide;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Deletes a guide.
 */
public class DeleteGuide extends AbstractAction
{
    /** Guide to delete. */
    private final IGuide guide;

    /**
     * Creates an action.
     *
     * @param guide guide.
     */
    public DeleteGuide(IGuide guide)
    {
        super(Strings.message("report.action.delete"));
        this.guide = guide;
    }

    /**
     * Invoked when the action is performed.
     *
     * @param e event.
     */
    public void actionPerformed(ActionEvent e)
    {
        if (guide == null || guide.getID() == -1) return;
        GlobalModel.SINGLETON.getGuidesSet().remove(guide);
    }
}
