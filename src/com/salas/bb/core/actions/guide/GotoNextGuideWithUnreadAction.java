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
// $Id: GotoNextGuideWithUnreadAction.java,v 1.8 2007/04/17 14:49:14 spyromus Exp $
//

package com.salas.bb.core.actions.guide;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.core.GuidesListModel;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.IGuide;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Moves to next Guide that has any unread articles.
 * <p/>
 * Action is enabled/disabled by <code>ActionsManager</code>.
 */
public final class GotoNextGuideWithUnreadAction extends AbstractAction
{
    private static GotoNextGuideWithUnreadAction instance;

    /**
     * Hidden constructor of singleton class.
     */
    private GotoNextGuideWithUnreadAction()
    {
        setEnabled(false);
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static GotoNextGuideWithUnreadAction getInstance()
    {
        if (instance == null) instance = new GotoNextGuideWithUnreadAction();
        return instance;
    }

    /**
     * Invoked when action occurs.
     *
     * @param e action event details object.
     */
    public void actionPerformed(ActionEvent e)
    {
        final GlobalController controller = GlobalController.SINGLETON;
        final GlobalModel model = controller.getModel();

        if (model != null)
        {
            final IGuide currentGuide = model.getSelectedGuide();
            final GuidesSet cgs = model.getGuidesSet();

            IGuide guide = findNextGuideWithUnread(cgs, currentGuide);

            // EDT !!!
            if (guide != null) GlobalController.SINGLETON.selectGuide(guide, false);
        }
    }

    /**
     * Finds the guide with channels containing unread articles.
     *
     * @param cgs           guides set.
     * @param currentGuide  currebt guide.
     *
     * @return guide or <code>NULL</code> if no such guides found.
     */
    static IGuide findNextGuideWithUnread(final GuidesSet cgs,
        final IGuide currentGuide)
    {
        IGuide guide = currentGuide;
        boolean found = false;

        GuidesListModel glm = GlobalController.SINGLETON.getGuidesListModel();

        while (!found)
        {
            guide = nextGuide(cgs, guide);
            found = guide == currentGuide || (!guide.isRead() && glm.indexOf(guide) != -1);
        }

        return currentGuide == guide ? null : guide;
    }

    /**
     * Returns guide, next to specified in the given set.
     *
     * @param set   set of guides.
     * @param guide guide.
     *
     * @return next guide.
     */
    private static IGuide nextGuide(GuidesSet set, IGuide guide)
    {
        int index = set.indexOf(guide);
        int guidesCount = set.getGuidesCount();
        if (index == -1 || index == guidesCount - 1) index = 0; else index++;

        return set.getGuideAt(index);
    }
}

