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
// $Id: OpenBlogHomeAction.java,v 1.5 2006/01/08 04:42:24 kyank Exp $
//

package com.salas.bb.core.actions.feed;

import com.salas.bb.core.GlobalModel;
import com.salas.bb.core.GlobalController;
import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.utils.BrowserLauncher;
import com.salas.bb.utils.ThreadedAction;

import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * Opens Blog home page of selected channel in browser.
 *
 * Action is enabled/disabled by <code>ActionsManager</code>.
 */
public final class OpenBlogHomeAction extends ThreadedAction
{
    private static OpenBlogHomeAction instance;

    /**
     * Hidden constructor of singleton class.
     */
    private OpenBlogHomeAction()
    {
        setEnabled(false);
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized OpenBlogHomeAction getInstance()
    {
        if (instance == null) instance = new OpenBlogHomeAction();
        return instance;
    }

    /**
     * Actual action.
     *
     * @param event original event object.
     */
    protected void doAction(ActionEvent event)
    {
        IFeed[] selectedFeeds = GlobalController.SINGLETON.getSelectedFeeds();
        for (int i = 0; i < selectedFeeds.length; i++)
        {
            IFeed feed = selectedFeeds[i];
            if (feed instanceof DirectFeed)
            {
                UserPreferences prefs = GlobalModel.SINGLETON.getUserPreferences();

                URL url = ((DirectFeed)feed).getSiteURL();
                BrowserLauncher.showDocument(url, prefs.getInternetBrowser());
            }
        }
    }
}
