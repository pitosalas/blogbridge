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
// $Id: HTMLFeedDisplayConfig.java,v 1.14 2007/07/26 09:06:32 spyromus Exp $
//

package com.salas.bb.views.settings;

import com.salas.bb.core.GlobalController;
import com.salas.bb.views.feeds.html.IHTMLFeedDisplayConfig;

import java.awt.event.MouseListener;

/**
 * HTML feed view configuration.
 */
public class HTMLFeedDisplayConfig extends AbstractFeedDisplayConfig
    implements IHTMLFeedDisplayConfig
{
    /**
     * Returns adapter which is listening to the mouse events (press/release/click) from links on
     * the views. Useful for context menus.
     *
     * @return popup adapter.
     */
    public MouseListener getLinkPopupAdapter()
    {
        return GlobalController.SINGLETON.getMainFrame().getArticleHyperLinkPopupAdapter();
    }

    /**
     * Returns adapter which is listening to the mouse events (press/release/click) from the views.
     * Useful for context menus.
     *
     * @return popup adapter.
     */
    public MouseListener getViewPopupAdapter()
    {
        return GlobalController.SINGLETON.getMainFrame().getHTMLDisplayPopupAdapter();
    }

    /**
     * Returns how old in days article should be to be suppressed.
     *
     * @return days.
     */
    public int getMaxArticleAge()
    {
        return RenderingManager.getSuppressOlderThan();
    }

    /**
     * Returns <code>TRUE</code> if suppressing of old articles is required.
     *
     * @return <code>TRUE</code> if suppressing of old articles is required.
     *
     * @see #getMaxArticleAge()
     */
    public boolean isSuppressingOld()
    {
        return RenderingManager.isSuppressingOlderThan();
    }
}
