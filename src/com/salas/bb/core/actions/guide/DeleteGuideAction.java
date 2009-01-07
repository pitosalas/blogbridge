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
// $Id: DeleteGuideAction.java,v 1.20 2006/01/23 11:10:28 spyromus Exp $
//

package com.salas.bb.core.actions.guide;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.dialogs.guide.DeleteGuideDialog;
import com.salas.bb.domain.IGuide;
import com.salas.bb.domain.StandardGuide;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.utils.GuidesUtils;
import com.salas.bb.views.mainframe.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Deletes guide from the list.
 *
 * SHOULD ALWAYS BE EXECUTED FROM EDT!
 */
public final class DeleteGuideAction extends AbstractAction
{
    private static DeleteGuideAction instance;

    /**
     * Hidden constructor of singleton class.
     */
    private DeleteGuideAction()
    {
        setEnabled(false);
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized DeleteGuideAction getInstance()
    {
        if (instance == null) instance = new DeleteGuideAction();
        return instance;
    }

    /**
     * Actual action.
     *
     * @param event original event object.
     */
    public void actionPerformed(ActionEvent event)
    {
        IGuide[] guides = GlobalController.SINGLETON.getSelectedGuides();

        if (guides.length > 0 && (calculateFeeds(guides) == 0 || processComplexDelete(guides)))
        {
            GlobalController.SINGLETON.deleteGuides(guides);
        }
    }

    /**
     * Returns number of feeds in standard guides.
     *
     * @param aGuides guides.
     *
     * @return number of feeds.
     */
    private int calculateFeeds(IGuide[] aGuides)
    {
        int feeds = 0;

        for (int i = 0; i < aGuides.length; i++)
        {
            IGuide guide = aGuides[i];
            if (guide instanceof StandardGuide) feeds += guide.getFeedsCount();
        }

        return feeds;
    }

    /**
     * Asks user about possible actions:
     * <ul>
     * <li>Delete with all channels.</li>
     * <li>Reassign channels to the other guide.</li>
     * </ul>
     *
     * @param guidesToDelete guide(s) we are going to delete.
     *
     * @return <code>TRUE</code> if user confirms deletion.
     */
    private boolean processComplexDelete(final IGuide[] guidesToDelete)
    {
        final GuidesSet cgs = GlobalModel.SINGLETON.getGuidesSet();
        StandardGuide[] guides = GuidesUtils.filterGuides(cgs.getStandardGuides(null), guidesToDelete);

        final MainFrame mainFrame = GlobalController.SINGLETON.getMainFrame();
        DeleteGuideDialog dialog = new DeleteGuideDialog(mainFrame, guidesToDelete.length > 1);

        dialog.setReassignGuides(guides);
        dialog.open();

        boolean delete = false;
        if (!dialog.hasBeenCanceled())
        {
            if (dialog.isReassigning())
            {
                final StandardGuide destGuide = dialog.getSelectedReassignGuide();
                GlobalController.SINGLETON.mergeGuides(guidesToDelete, destGuide);

                delete = false;
            } else
            {
                delete = true;
            }
        }

        return delete;
    }
}
