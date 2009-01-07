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
// $Id: MergeGuidesAction.java,v 1.15 2006/01/08 04:42:24 kyank Exp $
//

package com.salas.bb.core.actions.guide;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.dialogs.guide.MergeGuideDialog;
import com.salas.bb.domain.IGuide;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.StandardGuide;
import com.salas.bb.domain.utils.GuidesUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Merges one guide with the other.
 *
 * SHOULD ALWAYS BE EXECUTED FROM EDT!
 */
public final class MergeGuidesAction extends AbstractAction
{
    private static MergeGuidesAction instance;

    /**
     * Hidden constructor of singleton class.
     */
    private MergeGuidesAction()
    {
        setEnabled(false);
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized MergeGuidesAction getInstance()
    {
        if (instance == null) instance = new MergeGuidesAction();
        return instance;
    }

    /**
     * Invoked when action occurs.
     *
     * @param e action event details object.
     */
    public void actionPerformed(ActionEvent e)
    {
        final IGuide[] mergeGuides = GlobalController.SINGLETON.getSelectedGuides();
        final GuidesSet cgs = GlobalModel.SINGLETON.getGuidesSet();
        StandardGuide[] guides = GuidesUtils.filterGuides(cgs.getStandardGuides(null), mergeGuides);

        MergeGuideDialog dialog = new MergeGuideDialog(GlobalController.SINGLETON.getMainFrame());
        dialog.setMergeGuides(guides);
        dialog.open();

        if (!dialog.hasBeenCanceled())
        {
            final StandardGuide mergeGuide = dialog.getSelectedMergeGuide();
            if (mergeGuide != null)
            {
                GlobalController.SINGLETON.mergeGuides(mergeGuides, mergeGuide);
            }
        }
    }
}
