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
// $Id: BrowseFeed.java,v 1.1 2007/10/04 13:29:47 spyromus Exp $
//

package com.salas.bb.reports.actions;

import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.utils.BrowserLauncher;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Browses a feed.
 */
public class BrowseFeed extends AbstractAction
{
    /** Feed to browse. */
    private final IFeed feed;

    /**
     * Creates an action.
     *
     * @param feed feed to browse.
     */
    public BrowseFeed(IFeed feed)
    {
        super(Strings.message("report.action.browse"));
        this.feed = feed;
    }

    /**
     * Invoked when the action is performed.
     *
     * @param e event.
     */
    public void actionPerformed(ActionEvent e)
    {
        if (feed == null || feed.getID() == -1 || !(feed instanceof DirectFeed)) return;
        DirectFeed dFeed = (DirectFeed)feed;

        GlobalModel model = GlobalModel.SINGLETON;
        UserPreferences preferences = model.getUserPreferences();
        BrowserLauncher.showDocument(dFeed.getSiteURL(), preferences.getInternetBrowser());
    }
}
