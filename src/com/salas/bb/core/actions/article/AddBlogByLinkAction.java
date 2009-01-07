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
// $Id: AddBlogByLinkAction.java,v 1.23 2007/02/19 12:52:02 spyromus Exp $
//

package com.salas.bb.core.actions.article;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.core.actions.IToolbarCommandAction;
import com.salas.bb.domain.IGuide;
import com.salas.bb.domain.StandardGuide;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * Adds blog by link. Link is set to this class by someone external.
 */
public final class AddBlogByLinkAction extends AbstractAction implements IToolbarCommandAction
{
    private static AddBlogByLinkAction instance;

    private URL link;

    /**
     * Hidden singleton constructor.
     */
    private AddBlogByLinkAction()
    {
        link = null;
        setEnabled(false);
    }

    /**
     * Sets the link of the blog to add.
     *
     * @param aLink link.
     */
    public static void setLink(URL aLink)
    {
        GlobalController controller = GlobalController.SINGLETON;
        GuidesSet set = controller.getModel().getGuidesSet();

        getInstance().link = aLink;
        getInstance().setEnabled(set.findDirectFeed(aLink) == null);
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized AddBlogByLinkAction getInstance()
    {
        if (instance == null) instance = new AddBlogByLinkAction();
        return instance;
    }

    /**
     * Invoked when action performed.
     *
     * @param e action object.
     */
    public void actionPerformed(ActionEvent e)
    {
        if (GlobalController.SINGLETON.checkForNewSubscription()) return;

        URL lnk = link;
        if (lnk != null)
        {
            final IGuide guide = GlobalModel.SINGLETON.getSelectedGuide();
            if (!(guide instanceof StandardGuide))
            {
                throw new IllegalArgumentException(Strings.error("invalid.guide.type"));
            }

            GlobalController.SINGLETON.createDirectFeed(guide, lnk);
        }
    }

    /**
     * Returns icon to use as pressed icon if this command is on Toolbar.
     * Note in this case it is never on toolbar so a null is OK.
     *
     * @return icon.
     */
    public Icon getPressedIcon()
    {
        return null;
    }
}
