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
// $Id: SampleArticleDisplayConfig.java,v 1.9 2008/02/28 15:59:50 spyromus Exp $
//

package com.salas.bb.views.feeds.html;

import com.salas.bb.domain.prefs.ViewModePreferences;
import com.salas.bb.views.feeds.IFeedDisplayConstants;
import com.salas.bb.views.feeds.IHighlightsAdvisor;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.net.URL;

/**
 * Sample article view configuration testing purposes only.
 */
class SampleArticleDisplayConfig implements IArticleDisplayConfig
{
    /**
     * Returns the length of text excerpt in brief mode.
     *
     * @return number of characters in excerpt.
     */
    public int getBriefModeTextLength()
    {
        return 100;
    }

    /**
     * Returns border which should be displayed around the article view.
     *
     * @param aSelected <code>TRUE</code> if article view is selected.
     * @param aFocused  <code>TRUE</code> if article view is focused.
     *
     * @return border.
     */
    public Border getBorder(boolean aSelected, boolean aFocused)
    {
        return BorderFactory.createEmptyBorder();
    }

    /**
     * Returns global background color for the view.
     *
     * @param aSelected <code>TRUE</code> if article view is selected.
     *
     * @return color.
     */
    public Color getGlobalBGColor(boolean aSelected)
    {
        return aSelected ? Color.YELLOW : Color.WHITE;
    }

    /**
     * Returns the color of the text.
     *
     * @param aSelected <code>TRUE</code> if selected.
     *
     * @return color.
     */
    public Color getTextColor(boolean aSelected)
    {
        return Color.BLACK;
    }

    /**
     * Returns the background color of the text area.
     *
     * @param aSelected <code>TRUE</code> if article view is selected.
     *
     * @return color.
     */
    public Color getTextBGColor(boolean aSelected)
    {
        return getGlobalBGColor(aSelected);
    }

    /**
     * Returns the background color of the title.
     *
     * @param aSelected <code>TRUE</code> if article view is selected.
     *
     * @return color.
     */
    public Color getTitleBGColor(boolean aSelected)
    {
        return getGlobalBGColor(aSelected);
    }

    /**
     * Returns foreground color of view title.
     *
     * @param aSelected <code>TRUE</code> if article view is selected.
     *
     * @return color.
     */
    public Color getTitleFGColor(boolean aSelected)
    {
        return aSelected ? Color.RED : Color.BLACK;
    }

    /**
     * Returns foreground color of view date.
     *
     * @param aSelected <code>TRUE</code> if article view is selected.
     *
     * @return color.
     */
    public Color getDateFGColor(boolean aSelected)
    {
        return getTitleFGColor(aSelected);
    }

    /**
     * Returns the font to be used for painting date.
     *
     * @return font.
     */
    public Font getDateFont()
    {
        return Font.decode("Tahoma-11");
    }

    /**
     * Returns the font to be used for painting text area.
     *
     * @return font.
     */
    public Font getTextFont()
    {
        return getDateFont();
    }

    /**
     * Returns the font to be used for painting title.
     *
     * @param aRead <code>TRUE</code> if font for read state is required.
     *
     * @return font.
     */
    public Font getTitleFont(boolean aRead)
    {
        Font fnt = getDateFont();
        return aRead ? fnt : fnt.deriveFont(Font.BOLD);
    }

    /**
     * Returns maximum length of single-line title.
     *
     * @return maximum length of single-line title.
     */
    public int getMaxSingleLineTitleLength()
    {
        return 30;
    }

    /**
     * Returns <code>TRUE</code> when single-line titles are enabled.
     *
     * @return <code>TRUE</code> when single-line titles are enabled.
     */
    public boolean isSingleLineTitles()
    {
        return false;
    }

    /**
     * Returns the background color of search-words.
     *
     * @return color.
     */
    public Color getSearchwordBGColor()
    {
        return Color.CYAN;
    }

    /**
     * Returns color of background for the link.
     *
     * @param type type of the link.
     *
     * @return color.
     */
    public Color getLinkBGColor(LinkType type)
    {
        return Color.RED;
    }

    /**
     * Returns the type of the link.
     *
     * @param link link.
     *
     * @return type.
     */
    public LinkType getLinkType(String link)
    {
        return LinkType.NORMAL;
    }

    /**
     * Returns tooltip to use when mouse over the link.
     *
     * @param aLink link.
     *
     * @return tool-tip text.
     */
    public String getLinkTooltip(URL aLink)
    {
        return "test";
    }

    /**
     * Returns the advisor object to use for keywords highlighting.
     *
     * @return advisor.
     */
    public IHighlightsAdvisor getHighlightsAdvisor()
    {
        return null;
    }

    /**
     * Returns the view mode.
     *
     * @return view mode.
     *
     * @see com.salas.bb.views.feeds.IFeedDisplayConstants#MODE_BRIEF
     * @see com.salas.bb.views.feeds.IFeedDisplayConstants#MODE_FULL
     * @see com.salas.bb.views.feeds.IFeedDisplayConstants#MODE_MINIMAL
     */
    public int getViewMode()
    {
        return IFeedDisplayConstants.MODE_BRIEF;
    }

    /**
     * Returns view mode preferences.
     *
     * @return view mode preferences.
     */
    public ViewModePreferences getViewModePreferences()
    {
        return new ViewModePreferences();
    }

    /**
     * Returns <code>TRUE</code> if article should be rendered with date.
     *
     * @return <code>TRUE</code> if article should be rendered with date.
     */
    public boolean isShowingDate()
    {
        return false;
    }

    /**
     * Returns <code>TRUE</code> if browser should launch on dbl-click over the title.
     *
     * @return <code>TRUE</code> to open browser on double click over the article title.
     */
    public boolean isBrowseOnTitleDoubleClick()
    {
        return false;
    }
}
