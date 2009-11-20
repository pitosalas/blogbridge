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
// $Id: IArticleDisplayConfig.java,v 1.8 2008/02/28 15:59:45 spyromus Exp $
//

package com.salas.bb.views.feeds.html;

import com.salas.bb.domain.prefs.ViewModePreferences;
import com.salas.bb.views.feeds.IHighlightsAdvisor;

import javax.swing.border.Border;
import java.awt.*;
import java.net.URL;

/**
 * Configuration of articles views.
 */
public interface IArticleDisplayConfig
{
    /** Link types for highlighting. */
    public enum LinkType { NORMAL, UNREGISTERED, REGISTERED, SEARCH };

    /**
     * Returns the length of text excerpt in brief mode.
     *
     * @return number of characters in excerpt.
     */
    int getBriefModeTextLength();

    /**
     * Returns border which should be displayed around the article view.
     *
     * @param aSelected <code>TRUE</code> if article view is selected.
     * @param aFocused  <code>TRUE</code> if article view is focused.
     *
     * @return border.
     */
    Border getBorder(boolean aSelected, boolean aFocused);

    /**
     * Returns foreground color of view title.
     *
     * @param aSelected <code>TRUE</code> if article view is selected.
     *
     * @return color.
     */
    Color getTitleFGColor(boolean aSelected);

    /**
     * Returns foreground color of view date.
     *
     * @param aSelected <code>TRUE</code> if article view is selected.
     *
     * @return color.
     */
    Color getDateFGColor(boolean aSelected);

    /**
     * Returns global background color for the view.
     *
     * @param aSelected <code>TRUE</code> if article view is selected.
     *
     * @return color.
     */
    Color getGlobalBGColor(boolean aSelected);

    /**
     * Returns the background color of the title.
     *
     * @param aSelected <code>TRUE</code> if article view is selected.
     *
     * @return color.
     */
    Color getTitleBGColor(boolean aSelected);

    /**
     * Returns the color of the text.
     *
     * @param aSelected <code>TRUE</code> if selected.
     *
     * @return color.
     */
    Color getTextColor(boolean aSelected);

    /**
     * Returns the background color of the text area.
     *
     * @param aSelected <code>TRUE</code> if article view is selected.
     *
     * @return color.
     */
    Color getTextBGColor(boolean aSelected);

    /**
     * Returns <code>TRUE</code> when single-line titles are enabled.
     *
     * @return <code>TRUE</code> when single-line titles are enabled.
     */
    boolean isSingleLineTitles();

    /**
     * Returns TRUE to automatically expand minified articles on selection.
     *
     * @return TRUE to expand.
     */
    boolean isAutoExpandingMini();

    /**
     * Returns maximum length of single-line title.
     *
     * @return maximum length of single-line title.
     */
    int getMaxSingleLineTitleLength();

    /**
     * Returns the font to be used for painting title.
     *
     * @param aRead <code>TRUE</code> if font for read state is required.
     *
     * @return font.
     */
    Font getTitleFont(boolean aRead);

    /**
     * Returns the font to be used for painting date.
     *
     * @return font.
     */
    Font getDateFont();

    /**
     * Returns the font to be used for painting text area.
     *
     * @return font.
     */
    Font getTextFont();

    /**
     * Returns the background color of search-words.
     *
     * @return color.
     */
    Color getSearchwordBGColor();

    /**
     * Returns color of background for the link.
     *
     * @param type type of the link.
     *
     * @return color.
     */
    Color getLinkBGColor(LinkType type);

    /**
     * Returns the type of the link.
     *
     * @param link  link.
     *
     * @return type.
     */
    LinkType getLinkType(String link);

    /**
     * Returns tooltip to use when mouse over the link.
     *
     * @param aLink link.
     *
     * @return tool-tip text.
     */
    String getLinkTooltip(URL aLink);

    /**
     * Returns the advisor object to use for keywords highlighting.
     *
     * @return advisor.
     */
    IHighlightsAdvisor getHighlightsAdvisor();

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
     * Returns view mode preferences.
     *
     * @return view mode preferences.
     */
    ViewModePreferences getViewModePreferences();

    /**
     * Returns <code>TRUE</code> if article should be rendered with date.
     *
     * @return <code>TRUE</code> if article should be rendered with date. 
     */
    boolean isShowingDate();

    /**
     * Returns <code>TRUE</code> if browser should launch on dbl-click over the title.
     *
     * @return <code>TRUE</code> to open browser on double click over the article title.
     */
    boolean isBrowseOnTitleDoubleClick();
}
