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
// $Id: IFeedDisplayConfig.java,v 1.17 2008/02/27 15:28:10 spyromus Exp $
//

package com.salas.bb.views.feeds;

import com.salas.bb.views.feeds.html.IArticleDisplayConfig;
import com.salas.bb.views.settings.RenderingSettingsNames;

import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;

/**
 * Feed view basic configuration.
 */
public interface IFeedDisplayConfig
{
    String THEME = RenderingSettingsNames.THEME;
    String FILTER = RenderingSettingsNames.ARTICLE_FILTER;
    String MODE = RenderingSettingsNames.ARTICLE_VIEW_MODE;
    String SORT_ORDER = RenderingSettingsNames.IS_SORTING_ASCENDING;
    String GROUPS_VISIBLE = RenderingSettingsNames.IS_GROUPING_ENABLED;
    String EMPTY_GROUPS_VISIBLE = RenderingSettingsNames.IS_SHOW_EMPTY_GROUPS;
    String FONT_BIAS = RenderingSettingsNames.ARTICLE_FONT_BIAS;

    /** View mode layout change fake property telling the mode number. */
    String VIEW_MODE_LAYOUT = "viewModeLayout";

    /**
     * Returns the advisor object to use for keywords highlighting.
     *
     * @return advisor.
     */
    IHighlightsAdvisor getHighlightsAdvisor();

    /**
     * Returns <code>TRUE</code> if it's required to show groups.
     *
     * @return <code>TRUE</code> if it's required to show groups.
     */
    boolean showGroups();

    /**
     * Returns <code>TRUE</code> if it's required to show empty groups.
     *
     * @return <code>TRUE</code> if it's required to show empty groups.
     */
    boolean showEmptyGroups();

    /**
     * Returns key adapter which is reported of key events happening when
     * component has focus.
     *
     * @return adapter.
     */
    KeyListener getKeyAdapter();

    /**
     * Set configuration properties change listener.
     *
     * @param l listener.
     */
    void setListener(PropertyChangeListener l);

    /**
     * Returns adapter which is listening to the mouse events (press/release/click)
     * from the views. Useful for context menus.
     *
     * @return popup adapter.
     */
    MouseListener getViewPopupAdapter();

    /**
     * Filter to use in order to hide articles.
     *
     * @return filter.
     *
     * @see com.salas.bb.views.feeds.IFeedDisplayConstants#FILTER_ALL
     * @see com.salas.bb.views.feeds.IFeedDisplayConstants#FILTER_KEYWORDS
     * @see com.salas.bb.views.feeds.IFeedDisplayConstants#FILTER_UNREAD
     */
    int getFilter();

    /**
     * Returns <code>TRUE</code> if ascending sorting selected.
     *
     * @return <code>TRUE</code> if ascending sorting selected.
     */
    boolean isAscendingSorting();

    /**
     * Returns the view mode.
     *
     * @return view mode.
     *
     * @see com.salas.bb.views.feeds.IFeedDisplayConstants#MODE_BRIEF
     * @see com.salas.bb.views.feeds.IFeedDisplayConstants#MODE_FULL
     * @see com.salas.bb.views.feeds.IFeedDisplayConstants#MODE_MINIMAL
     */
    int getViewMode();

    /**
     * Returns background color of the feed display.
     *
     * @return background color.
     */
    Color getDisplayBGColor();

    /**
     * Returns font of groups divider component.
     *
     * @return font.
     */
    Font getGroupDividerFont();

    /**
     * Returns the adapter for the article groups.
     *
     * @return popup adapter.
     */
    MouseListener getGroupPopupAdapter();

    /**
     * Returns configuration of articles views.
     *
     * @return configuration of articles views.
     */
    IArticleDisplayConfig getArticleViewConfig();
}
