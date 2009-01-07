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
// $Id: DefaultFRS.java,v 1.5 2008/02/28 15:59:46 spyromus Exp $
//

package com.salas.bb.views.settings;

import com.salas.bb.views.themes.ITheme;
import com.salas.bb.views.themes.ThemeSupport;

import java.beans.PropertyChangeListener;
import java.util.HashMap;

/**
 * Default settings for rendering.
 */
public class DefaultFRS extends HashMap<String, Object> implements IFRS
{
    /**
     * Constructs default channel rendering settings object.
     */
    public DefaultFRS()
    {
        super.put(RenderingSettingsNames.ARTICLE_SIZE_LIMIT,
                RenderingSettingsDefaults.ARTICLE_SIZE_LIMIT);

        super.put(RenderingSettingsNames.IS_ARTICLE_DATE_SHOWING,
                RenderingSettingsDefaults.IS_ARTICLE_DATE_SHOWING);

        super.put(RenderingSettingsNames.ARTICLE_VIEW_MODE,
                RenderingSettingsDefaults.ARTICLE_VIEW_MODE);

        super.put(RenderingSettingsNames.IS_GROUPING_ENABLED,
                RenderingSettingsDefaults.IS_GROUPING_ENABLED);

        super.put(RenderingSettingsNames.IS_SHOW_EMPTY_GROUPS,
                RenderingSettingsDefaults.IS_SHOW_EMPTY_GROUPS);

        super.put(RenderingSettingsNames.IS_SORTING_ASCENDING,
                RenderingSettingsDefaults.IS_SORTING_ASCENDING);

        super.put(RenderingSettingsNames.IS_SUPPRESSING_OLDER_THAN,
                RenderingSettingsDefaults.IS_SUPPRESSING_OLDER_THAN);

        super.put(RenderingSettingsNames.SUPPRESS_OLDER_THAN,
                RenderingSettingsDefaults.SUPPRESS_OLDER_THAN);

        super.put(RenderingSettingsNames.ARTICLE_FILTER,
                RenderingSettingsDefaults.ARTICLE_FILTER);

        super.put(RenderingSettingsNames.IS_DISPLAYING_FULL_TITLES,
                RenderingSettingsDefaults.IS_DISPLAYING_FULL_TITLES);
        
        super.put(RenderingSettingsNames.ARTICLE_FONT_BIAS,
                RenderingSettingsDefaults.ARTICLE_FONT_BIAS);
        
        super.put(RenderingSettingsNames.IS_STARZ_SHOWING,
                RenderingSettingsDefaults.IS_STARZ_SHOWING);
        
        super.put(RenderingSettingsNames.IS_UNREAD_IN_FEEDS_SHOWING,
                RenderingSettingsDefaults.IS_UNREAD_IN_FEEDS_SHOWING);
        
        super.put(RenderingSettingsNames.IS_ACTIVITY_CHART_SHOWING,
                RenderingSettingsDefaults.IS_ACTIVITY_CHART_SHOWING);
        
        super.put(RenderingSettingsNames.IS_UNREAD_IN_GUIDES_SHOWING,
                RenderingSettingsDefaults.IS_UNREAD_IN_GUIDES_SHOWING);

        super.put(RenderingSettingsNames.IS_ICON_IN_GUIDES_SHOWING,
                RenderingSettingsDefaults.IS_ICON_IN_GUIDES_SHOWING);

        super.put(RenderingSettingsNames.IS_TEXT_IN_GUIDES_SHOWING,
                RenderingSettingsDefaults.IS_TEXT_IN_GUIDES_SHOWING);

        super.put(RenderingSettingsNames.IS_BIG_ICON_IN_GUIDES,
                RenderingSettingsDefaults.IS_BIG_ICON_IN_GUIDES);
    }

    /**
     * Returns value of a setting under specified key.
     *
     * @param key key name of the setting.
     * @return value of the setting.
     */
    public Object get(String key)
    {
        return super.get(key);
    }

    /**
     * Registers new value of the setting.
     *
     * @param key   key name of the setting.
     * @param value value of the setting or NULL to remove setting value.
     */
    public void set(String key, Object value)
    {
    }

    /**
     * Registers listener of settings changes.
     *
     * @param l new listener object.
     */
    public void addListener(PropertyChangeListener l)
    {
    }

    /**
     * Removes listener of settings changes.
     *
     * @param l listener object.
     */
    public void removeListener(PropertyChangeListener l)
    {
    }

    /**
     * Returns current theme.
     *
     * @return theme or NULL if not defined.
     */
    public ITheme getTheme()
    {
        return ThemeSupport.getDefaultTheme();
    }
}
