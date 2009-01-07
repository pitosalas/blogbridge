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
// $Id: RenderingSettingsNames.java,v 1.7 2008/02/28 15:59:46 spyromus Exp $
//

package com.salas.bb.views.settings;

/**
 * Names of all possible properties of rendering settings.
 */
public class RenderingSettingsNames
{
    public static final String ARTICLE_FILTER = "articleFilter";
    public static final String IS_GROUPING_ENABLED = "isGroupingEnabled";
    public static final String IS_SHOW_EMPTY_GROUPS = "isShowEmptyGroups";
    public static final String IS_ARTICLE_DATE_SHOWING = "isArticleDateShowing";
    public static final String ARTICLE_VIEW_MODE = "articleViewMode";
    public static final String IS_SUPPRESSING_OLDER_THAN = "isSuppressingOlderThan";
    public static final String SUPPRESS_OLDER_THAN = "suppressOlderThan";
    public static final String IS_SORTING_ASCENDING = "isSortingAscending";
    public static final String ARTICLE_SIZE_LIMIT = "articleSizeLimit";
    public static final String ARTICLE_FONT_BIAS = "fontBias";

    /** Show Starz in feeds list. */
    public static final String IS_STARZ_SHOWING = "isStarzShowing";
    
    /** Show unread count in feeds list. */
    public static final String IS_UNREAD_IN_FEEDS_SHOWING = "isUnreadInFeedsShowing";
    
    /** Show article activity chart in feeds list. */
    public static final String IS_ACTIVITY_CHART_SHOWING = "isActivityChartShowing";
    
    /** Show unread count in guides list. */
    public static final String IS_UNREAD_IN_GUIDES_SHOWING = "isUnreadInGuidesShowing";
    /** Show icons in guides list. */
    public static final String IS_ICON_IN_GUIDES_SHOWING = "showIconInGuides";
    /** Show text in guides list. */
    public static final String IS_TEXT_IN_GUIDES_SHOWING = "showTextInGuides";
    /** Show big icon in guides list. */
    public static final String IS_BIG_ICON_IN_GUIDES = "bigIconInGuides";

    /** Name of key for isDisplayingFullTitles property. */
    public static final String IS_DISPLAYING_FULL_TITLES = "isDisplaingFullTitles";

    /** Name of main content font property. */
    public static final String MAIN_CONTENT_FONT = "mainContentFont";

    /** Theme property. */
    public static final String THEME = "theme";

    /**
     * List of all possible KEYS.
     */
    static final String[] KEYS = new String[]
    {
        IS_GROUPING_ENABLED,
        IS_SHOW_EMPTY_GROUPS,
        IS_ARTICLE_DATE_SHOWING,
        ARTICLE_VIEW_MODE,
        IS_SUPPRESSING_OLDER_THAN,
        SUPPRESS_OLDER_THAN,
        IS_SORTING_ASCENDING,
        ARTICLE_FILTER,
        ARTICLE_SIZE_LIMIT,
        ARTICLE_FONT_BIAS,
        IS_DISPLAYING_FULL_TITLES,
        IS_STARZ_SHOWING,
        IS_UNREAD_IN_FEEDS_SHOWING,
        IS_ACTIVITY_CHART_SHOWING,
        IS_UNREAD_IN_GUIDES_SHOWING,
        IS_ICON_IN_GUIDES_SHOWING,
        IS_TEXT_IN_GUIDES_SHOWING,
        IS_BIG_ICON_IN_GUIDES
    };
}
