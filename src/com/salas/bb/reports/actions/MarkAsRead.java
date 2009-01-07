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
// $Id: MarkAsRead.java,v 1.1 2007/10/04 13:29:47 spyromus Exp $
//

package com.salas.bb.reports.actions;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Marks a feed or a guide as read / unread.
 */
public class MarkAsRead extends AbstractAction
{
    /** Object to mark: IFeed or IGuide. */
    private final Object obj;
    /** <code>TRUE</code> to mark as read. */
    private final boolean read;

    /**
     * Creates an action.
     *
     * @param obj   object to mark.
     * @param read  <code>TRUE</code> to mark as read.
     */
    private MarkAsRead(Object obj, boolean read)
    {
        super(read
            ? Strings.message("report.action.mark.as.read") 
            : Strings.message("report.action.mark.as.unread"));
        this.obj = obj;
        this.read = read;
    }

    /**
     * Creates an action for a guide.
     *
     * @param guide guide to mark.
     * @param read  <code>TRUE</code> to mark as read.
     *
     * @return action.
     */
    public static MarkAsRead createForGuide(IGuide guide, boolean read)
    {
        return new MarkAsRead(guide, read);
    }

    /**
     * Creates an action for a feed.
     *
     * @param feed  feed to mark.
     * @param read  <code>TRUE</code> to mark as read.
     *
     * @return action.
     */
    public static MarkAsRead createForFeed(IFeed feed, boolean read)
    {
        return new MarkAsRead(feed, read);
    }

    /**
     * Invoked when an action is performed.
     *
     * @param e event.
     */
    public void actionPerformed(ActionEvent e)
    {
        if (obj == null) return;

        if (obj instanceof IGuide)
        {
            IGuide guide = (IGuide)obj;
            if (guide.getID() == -1) return;

            GlobalController.readGuides(read, guide);
        } else
        {
            IFeed feed = (IFeed)obj;
            if (feed.getID() == -1) return;

            IGuide guide = GlobalModel.SINGLETON.getSelectedGuide();
            GlobalController.readFeeds(read, guide, feed);
        }
    }
}
