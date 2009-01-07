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
// $Id: AddDirectFeedAction.java,v 1.12 2007/02/19 12:52:01 spyromus Exp $
//

package com.salas.bb.core.actions.feed;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.actions.IToolbarCommandAction;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.utils.uif.IconSource;
import com.salas.bb.utils.CommonUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Adds channel after the selected.
 *
 * Action is enabled/disabled by <code>ActionsManager</code>.
 */
public class AddDirectFeedAction extends AbstractAction implements IToolbarCommandAction
{
    private static final String RESOURCE_ADDCHANNEL_TOOLBAR_PRESSEDICON =
        "toolbar.addchannel.pressedicon";

    private static AddDirectFeedAction instance;

    /**
     * Hidden constructor of singleton class.
     */
    protected AddDirectFeedAction()
    {
        setEnabled(false);
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized AddDirectFeedAction getInstance()
    {
        if (instance == null) instance = new AddDirectFeedAction();
        return instance;
    }

    /**
     * Invoked when action occurs.
     *
     * @param e action event details object.
     */
    public void actionPerformed(ActionEvent e)
    {
        if (GlobalController.SINGLETON.checkForNewSubscription()) return;

        GlobalController controller = GlobalController.SINGLETON;

        // Check the clippoard for URL if it's not specified
        String url = takeURLFromClipboardIfPresent();

        IFeed feed = controller.createDirectFeed(url, true);
        if (feed != null)
        {
            IGuide selectedGuide = controller.getModel().getSelectedGuide();
            if (!feed.belongsTo(selectedGuide))
            {
                IGuide[] guides = feed.getParentGuides();
                IGuide guide = guides.length > 0 ? guides[0] : null;
                if (guide != null) controller.selectGuide(guide, false);
            }
            controller.selectFeed(feed, true);
        }
    }

    /**
     * Returns the URL from clipboard or <code>NULL</code> if there's no URL in the clipboard.
     *
     * @return the URL or <code>NULL</code>.
     */
    static String takeURLFromClipboardIfPresent()
    {
        String text = CommonUtils.getTextFromClipboard();
        if (text != null)
        {
            String low = text.trim().toLowerCase();
            if (!low.startsWith("http:") && !low.startsWith("feed:")) text = null;
        }

        return text;
    }

    /**
     * Return Icon to be used when this command is on the toolbar.
     *
     * @see com.salas.bb.core.actions.IToolbarCommandAction#getPressedIcon()
     */
    public Icon getPressedIcon()
    {
        return IconSource.getIcon(RESOURCE_ADDCHANNEL_TOOLBAR_PRESSEDICON);
    }
}
