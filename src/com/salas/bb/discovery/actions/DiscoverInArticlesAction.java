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
// $Id: DiscoverInArticlesAction.java,v 1.1 2008/04/01 08:22:22 spyromus Exp $
//

package com.salas.bb.discovery.actions;

import com.salas.bb.core.GlobalModel;

import java.awt.event.ActionEvent;

/**
 * Starts the discovery of new feeds in the selected article(s).
 */
public class DiscoverInArticlesAction extends DiscoverAction
{
    private static final DiscoverInArticlesAction INSTANCE = new DiscoverInArticlesAction();

    /**
     * Creates an action.
     */
    private DiscoverInArticlesAction()
    {
        setEnabled(false);
    }

    /**
     * Returns the instance.
     *
     * @return instance.
     */
    public static DiscoverInArticlesAction getInstance()
    {
        return INSTANCE;
    }

    /**
     * Invoked on action.
     *
     * @param e event.
     */
    public void actionPerformed(ActionEvent e)
    {
        // Get all selected articles and load links from them
        discover(GlobalModel.SINGLETON.getSelectedArticles());
    }
}
