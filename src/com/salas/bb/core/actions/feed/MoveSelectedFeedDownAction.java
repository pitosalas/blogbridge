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
// $Id: MoveSelectedFeedDownAction.java,v 1.4 2006/01/08 04:42:24 kyank Exp $
//

package com.salas.bb.core.actions.feed;

import com.salas.bb.core.GuideModel;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.domain.StandardGuide;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Moves current channel down.
 */
public class MoveSelectedFeedDownAction extends AbstractAction
{
    private JList list;

    /**
     * Creates new action listener. It needs list reference to perform correct
     * selection of cells.
     *
     * @param list list with channels.
     */
    public MoveSelectedFeedDownAction(JList list)
    {
        this.list = list;
    }

    /**
     * Invoked when action occurs.
     *
     * @param e action event details object.
     */
    public void actionPerformed(ActionEvent e)
    {
        final IFeed feed = GlobalModel.SINGLETON.getSelectedFeed();
        final IGuide guide = GlobalModel.SINGLETON.getSelectedGuide();

        // Reorderings are allowed only for standard guides
        if (!(guide instanceof StandardGuide)) return;
        StandardGuide sGuide = (StandardGuide)guide;

        final GuideModel model = GlobalModel.SINGLETON.getGuideModel();

        // Move channel down if this one isn't already bottom-most.
        final int index = model.indexOf(feed) + 1;
        if (feed != null && index < model.getSize())
        {
            // Get real index
            IFeed entry = (IFeed)model.getElementAt(index);
            int realIndex = guide.indexOf(entry);

            GlobalController.SINGLETON.moveFeed(feed, sGuide, sGuide, realIndex);

            // Select it back and ensure that it's visible.
            list.setSelectedIndex(index);
            list.ensureIndexIsVisible(index);
        }
    }
}
