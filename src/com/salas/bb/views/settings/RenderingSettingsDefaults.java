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
// $Id: RenderingSettingsDefaults.java,v 1.6 2008/02/28 15:59:46 spyromus Exp $
//

package com.salas.bb.views.settings;

/**
 * Default values for rendering settings.
 */
public class RenderingSettingsDefaults
{
    static final Boolean IS_GROUPING_ENABLED = Boolean.TRUE;
    static final Boolean IS_SHOW_EMPTY_GROUPS = Boolean.FALSE;
    static final Boolean IS_ARTICLE_DATE_SHOWING = Boolean.TRUE;
    static final Boolean IS_SUPPRESSING_OLDER_THAN = Boolean.FALSE;
    static final Boolean IS_SORTING_ASCENDING = Boolean.FALSE;
    static final Integer ARTICLE_VIEW_MODE = FeedRenderingSettings.VIEW_MODE_FULL;
    static final Boolean IS_STARZ_SHOWING = Boolean.TRUE;
    static final Boolean IS_UNREAD_IN_FEEDS_SHOWING = Boolean.TRUE;
    static final Boolean IS_ACTIVITY_CHART_SHOWING = Boolean.TRUE;
    static final Boolean IS_UNREAD_IN_GUIDES_SHOWING = Boolean.TRUE;
    static final Boolean IS_ICON_IN_GUIDES_SHOWING = Boolean.TRUE;
    static final Boolean IS_TEXT_IN_GUIDES_SHOWING = Boolean.TRUE;
    static final Boolean IS_BIG_ICON_IN_GUIDES = Boolean.FALSE;

    static final Integer SUPPRESS_OLDER_THAN = 30;
    static final Integer ARTICLE_SIZE_LIMIT = 200;

    static final Integer ARTICLE_FILTER = 0;

    static final Boolean IS_DISPLAYING_FULL_TITLES = Boolean.TRUE;
    static final Integer ARTICLE_FONT_BIAS = 0;
}
