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
// $Id: SampleHTMLFeedDisplayConfig.java,v 1.7 2008/02/27 15:28:10 spyromus Exp $
//

package com.salas.bb.views.feeds.html;

import com.salas.bb.views.feeds.IFeedDisplayConstants;

import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;

/**
 * Sample HTML feed view configuration for testing purposes only.
 */
class SampleHTMLFeedDisplayConfig extends SampleArticleDisplayConfig
    implements IHTMLFeedDisplayConfig
{
    /**
     * Returns configuration of articles views.
     *
     * @return configuration of articles views.
     */
    public IArticleDisplayConfig getArticleViewConfig()
    {
        return this;
    }

    /**
     * Returns adapter which is listening to the mouse events (press/release/click) from links on
     * the views. Useful for context menus.
     *
     * @return popup adapter.
     */
    public MouseListener getLinkPopupAdapter()
    {
        return null;
    }

    /**
     * Returns adapter which is listening to the mouse events (press/release/click) from the views.
     * Useful for context menus.
     *
     * @return popup adapter.
     */
    public MouseListener getViewPopupAdapter()
    {
        return null;
    }

    /**
     * Returns the adapter for the article groups.
     *
     * @return popup adapter.
     */
    public MouseListener getGroupPopupAdapter()
    {
        return null;
    }

    /**
     * Returns key adapter which is reported of key events happening when component has focus.
     *
     * @return adapter.
     */
    public KeyListener getKeyAdapter()
    {
        return null;
    }

    /**
     * Filter to use in order to hide articles.
     *
     * @return filter.
     *
     * @see com.salas.bb.views.feeds.IFeedDisplayConstants#FILTER_ALL
     * @see com.salas.bb.views.feeds.IFeedDisplayConstants#FILTER_KEYWORDS
     * @see com.salas.bb.views.feeds.IFeedDisplayConstants#FILTER_UNREAD
     */
    public int getFilter()
    {
        return IFeedDisplayConstants.FILTER_ALL;
    }

    /**
     * Returns <code>TRUE</code> if it's required to show empty groups.
     *
     * @return <code>TRUE</code> if it's required to show empty groups.
     */
    public boolean showEmptyGroups()
    {
        return true;
    }

    /**
     * Returns <code>TRUE</code> if it's required to show groups.
     *
     * @return <code>TRUE</code> if it's required to show groups.
     */
    public boolean showGroups()
    {
        return true;
    }

    /**
     * Set configuration properties change listener.
     *
     * @param l listener.
     */
    public void setListener(PropertyChangeListener l)
    {
    }

    /**
     * Returns <code>TRUE</code> if ascending sorting selected.
     *
     * @return <code>TRUE</code> if ascending sorting selected.
     */
    public boolean isAscendingSorting()
    {
        return false;
    }

    /**
     * Returns how old in days article should be to be suppressed.
     *
     * @return days.
     */
    public int getMaxArticleAge()
    {
        return -1;
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
        return false;
    }

    /**
     * Returns background color of the feed display.
     *
     * @return background color.
     */
    public Color getDisplayBGColor()
    {
        return null;
    }

    /**
     * Returns font of groups divider component.
     *
     * @return font.
     */
    public Font getGroupDividerFont()
    {
        return null;
    }
}
