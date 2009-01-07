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
// $Id: ViewModePreferences.java,v 1.11 2008/02/28 15:59:52 spyromus Exp $
//

package com.salas.bb.domain.prefs;

import com.salas.bb.utils.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.Preferences;

/**
 * View mode preferences collection.
 */
public final class ViewModePreferences
{
    /** Categories visibility property name. */
    public static final String CATEGORIES_VISIBLE       = "categoriesVisible";
    /** Author visibility property name. */
    public static final String AUTHOR_VISIBLE           = "authorVisible";
    /** Date visibility property name. */
    public static final String DATE_VISIBLE             = "dateVisible";
    /** Time visibility property name. */
    public static final String TIME_VISIBLE             = "timeVisible";
    /** Pin visibility property name. */
    public static final String PIN_VISIBLE              = "pinVisible";
    /** URL visibility property name. */
    public static final String URL_VISIBLE              = "urlVisible";
    /** Color code property name. */
    public static final String COLOR_CODE_VISIBLE       = "colorCodeVisible";

    /** The list of listeners. */
    private final List<IViewModePreferencesChangeListener> listeners;

    /** List of enableness flags of the author field for all view modes. */
    private final boolean[] author;
    /** List of enableness flags of the categories field for all view modes. */
    private final boolean[] categories;
    /** List of enableness flags of the date field for all view modes. */
    private final boolean[] date;
    /** The state of time field for all view modes. */
    private final boolean[] time;
    /** List of enableness flags of the pin field for all view modes. */
    private final boolean[] pin;
    /** List of enableness flags of the article URL field for all view modes. */
    private final boolean[] url;
    /** List of flags for the color code. */
    private final boolean[] colorCode;

    /**
     * Creates preferences.
     */
    public ViewModePreferences()
    {
        listeners = new CopyOnWriteArrayList<IViewModePreferencesChangeListener>();

        author = new boolean[] { false, true, true };
        categories = new boolean[] { false, false, true};
        date = new boolean[] { true, true, true };
        time = new boolean[] { false, false, true };
        pin = new boolean[] { true, true, true };
        colorCode = new boolean[] { true, true, true };
        url = new boolean[] { false, false, false };
    }

    /**
     * Returns <code>TRUE</code> if author should be visible.
     *
     * @param mode view mode.
     *
     * @return <code>TRUE</code> if author should be visible.
     */
    public boolean isAuthorVisible(int mode)
    {
        return author[mode];
    }

    /**
     * Sets the flag of author visibility assigned to some mode.
     *
     * @param mode  mode.
     * @param flag  flag.
     */
    public void setAuthorVisible(int mode, boolean flag)
    {
        boolean oldValue = author[mode];
        author[mode] = flag;

        if (oldValue != flag) fireModeChanged(mode);
    }

    /**
     * Returns <code>TRUE</code> if categories should be visible.
     *
     * @param mode view mode.
     *
     * @return <code>TRUE</code> if categories should be visible.
     */
    public boolean isCategoriesVisible(int mode)
    {
        return categories[mode];
    }

    /**
     * Sets the flag of categories visibility assigned to some mode.
     *
     * @param mode  mode.
     * @param flag  flag.
     */
    public void setCategoriesVisible(int mode, boolean flag)
    {
        boolean oldValue = categories[mode];
        categories[mode] = flag;

        if (oldValue != flag) fireModeChanged(mode);
    }

    /**
     * Returns <code>TRUE</code> if URL should be visible.
     *
     * @param mode view mode.
     *
     * @return <code>TRUE</code> if URL should be visible.
     */
    public boolean isUrlVisible(int mode)
    {
        return url[mode];
    }

    /**
     * Sets the flag of URL visibility assigned to some mode.
     *
     * @param mode  mode.
     * @param flag  flag.
     */
    public void setUrlVisible(int mode, boolean flag)
    {
        boolean oldValue = url[mode];
        url[mode] = flag;

        if (oldValue != flag) fireModeChanged(mode);
    }

    /**
     * Returns <code>TRUE</code> if date should be visible.
     *
     * @param mode view mode.
     *
     * @return <code>TRUE</code> if date should be visible.
     */
    public boolean isDateVisible(int mode)
    {
        return date[mode];
    }

    /**
     * Sets the date visibility flag.
     *
     * @param mode  mode.
     * @param flag  flag.
     */
    public void setDateVisible(int mode, boolean flag)
    {
        boolean oldValue = date[mode];
        date[mode] = flag;

        if (oldValue != flag) fireModeChanged(mode);
    }

    /**
     * Returns <code>TRUE</code> if time should be visible.
     *
     * @param mode view mode.
     *
     * @return <code>TRUE</code> if time should be visible.
     */
    public boolean isTimeVisible(int mode)
    {
        return time[mode];
    }

    /**
     * Sets the time visibility flag.
     *
     * @param mode  mode.
     * @param flag  flag.
     */
    public void setTimeVisible(int mode, boolean flag)
    {
        boolean oldValue = time[mode];
        time[mode] = flag;

        if (oldValue != flag) fireModeChanged(mode);
    }

    /**
     * Returns <code>TRUE</code> if pin should be visible.
     *
     * @param mode view mode.
     *
     * @return <code>TRUE</code> if pin should be visible.
     */
    public boolean isPinVisible(int mode)
    {
        return pin[mode];
    }

    /**
     * Sets the pin visibility flag.
     *
     * @param mode  mode.
     * @param flag  flag.
     */
    public void setPinVisible(int mode, boolean flag)
    {
        boolean oldValue = pin[mode];
        pin[mode] = flag;

        if (oldValue != flag) fireModeChanged(mode);
    }

    /**
     * Returns <code>TRUE</code> if color code should be visible.
     *
     * @param mode view mode.
     *
     * @return <code>TRUE</code> if color code should be visible.
     */
    public boolean isColorCodeVisible(int mode)
    {
        return colorCode[mode];
    }

    /**
     * Sets the color code visibility flag.
     *
     * @param mode  mode.
     * @param flag  flag.
     */
    public void setColorCodeVisible(int mode, boolean flag)
    {
        boolean oldValue = colorCode[mode];
        colorCode[mode] = flag;

        if (oldValue != flag) fireModeChanged(mode);
    }

    /**
     * Restores preferences from the storage.
     *
     * @param prefs preferences storage.
     */
    public void restore(Preferences prefs)
    {
        restore(prefs, AUTHOR_VISIBLE, author);
        restore(prefs, CATEGORIES_VISIBLE, categories);
        restore(prefs, DATE_VISIBLE, date);
        restore(prefs, TIME_VISIBLE, time);
        restore(prefs, PIN_VISIBLE, pin);
        restore(prefs, URL_VISIBLE, url);
        restore(prefs, COLOR_CODE_VISIBLE, colorCode);
    }

    /**
     * Takes the property from preferences and analyzes it. The property should be
     * 3 characters corresponding to three modes. When the character is '1' it means
     * that the field is enabled.
     *
     * @param prefs     preferences storage.
     * @param property  property name.
     * @param field     field mode list.
     */
    private void restore(Preferences prefs, String property, boolean[] field)
    {
        String value = prefs.get(property, null);
        if (value != null && value.length() == 3)
        {
            for (int i = 0; i < 3; i++) field[i] = value.charAt(i) == '1';
        }
    }

    /**
     * Stores modes to the preferences storage.
     *
     * @param prefs preference storage.
     */
    public void store(Preferences prefs)
    {
        store(prefs, AUTHOR_VISIBLE, author);
        store(prefs, CATEGORIES_VISIBLE, categories);
        store(prefs, DATE_VISIBLE, date);
        store(prefs, TIME_VISIBLE, time);
        store(prefs, PIN_VISIBLE, pin);
        store(prefs, URL_VISIBLE, url);
        store(prefs, COLOR_CODE_VISIBLE, colorCode);
    }

    /**
     * Stores single field to modes map into preferences storage.
     *
     * @param prefs     preferences storage.
     * @param property  property name.
     * @param field     field mode list.
     */
    private void store(Preferences prefs, String property, boolean[] field)
    {
        StringBuffer buf = new StringBuffer(3);
        for (boolean f : field) buf.append(f ? 1 : 0);

        prefs.put(property, buf.toString());
    }

    /**
     * Restores preferences from the storage.
     *
     * @param prefs preferences storage.
     */
    public void restore(Map prefs)
    {
        restore(prefs, AUTHOR_VISIBLE, author);
        restore(prefs, CATEGORIES_VISIBLE, categories);
        restore(prefs, DATE_VISIBLE, date);
        restore(prefs, TIME_VISIBLE, time);
        restore(prefs, PIN_VISIBLE, pin);
        restore(prefs, URL_VISIBLE, url);
        restore(prefs, COLOR_CODE_VISIBLE, colorCode);
    }

    /**
     * Takes the property from preferences and analyzes it. The property should be
     * 3 characters corresponding to three modes. When the character is '1' it means
     * that the field is enabled.
     *
     * @param prefs     preferences storage.
     * @param property  property name.
     * @param field     field mode list.
     */
    private void restore(Map prefs, String property, boolean[] field)
    {
        byte[] bytes = (byte[])prefs.get(property);
        String value = bytes == null ? null : StringUtils.fromUTF8(bytes);
        if (value != null && value.length() == 3)
        {
            for (int i = 0; i < 3; i++) field[i] = value.charAt(i) == '1';
        }
    }

    /**
     * Stores modes to the preferences storage.
     *
     * @param prefs preference storage.
     */
    public void store(Map prefs)
    {
        store(prefs, AUTHOR_VISIBLE, author);
        store(prefs, CATEGORIES_VISIBLE, categories);
        store(prefs, DATE_VISIBLE, date);
        store(prefs, TIME_VISIBLE, time);
        store(prefs, PIN_VISIBLE, pin);
        store(prefs, URL_VISIBLE, url);
        store(prefs, COLOR_CODE_VISIBLE, colorCode);
    }

    /**
     * Stores single field to modes map into preferences storage.
     *
     * @param prefs     preferences storage.
     * @param property  property name.
     * @param field     field mode list.
     */
    private void store(Map prefs, String property, boolean[] field)
    {
        StringBuffer buf = new StringBuffer(3);
        for (boolean f : field) buf.append(f ? 1 : 0);

        prefs.put(property, StringUtils.toUTF8(buf.toString()));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * View mode preferences bean.
     */
    public static class ViewModeBean
    {
        /** Preference to wrap with a bean. */
        private final ViewModePreferences prefs;
        /** The mode we are adjusting. */
        private final int mode;

        /**
         * Creates bean for mode preferences manipulation.
         *
         * @param aPrefs    prefs.
         * @param aMode     mode.
         */
        public ViewModeBean(ViewModePreferences aPrefs, int aMode)
        {
            prefs = aPrefs;
            mode = aMode;
        }

        /**
         * Returns <code>TRUE</code> if author should be visible.
         *
         * @return <code>TRUE</code> if author should be visible.
         */
        public boolean isAuthorVisible()
        {
            return prefs.isAuthorVisible(mode);
        }

        /**
         * Sets the flag of author visibility assigned to some mode.
         *
         * @param flag  flag.
         */
        public void setAuthorVisible(boolean flag)
        {
            prefs.setAuthorVisible(mode, flag);
        }

        /**
         * Returns <code>TRUE</code> if categories should be visible.
         *
         * @return <code>TRUE</code> if categories should be visible.
         */
        public boolean isCategoriesVisible()
        {
            return prefs.isCategoriesVisible(mode);
        }

        /**
         * Sets the flag of categories visibility assigned to some mode.
         *
         * @param flag  flag.
         */
        public void setCategoriesVisible(boolean flag)
        {
            prefs.setCategoriesVisible(mode, flag);
        }

        /**
         * Returns <code>TRUE</code> if URLs should be visible.
         *
         * @return <code>TRUE</code> if URLs should be visible.
         */
        public boolean isUrlVisible()
        {
            return prefs.isUrlVisible(mode);
        }

        /**
         * Sets the flag of URLs visibility assigned to some mode.
         *
         * @param flag  flag.
         */
        public void setUrlVisible(boolean flag)
        {
            prefs.setUrlVisible(mode, flag);
        }

        /**
         * Returns <code>TRUE</code> if date should be visible.
         *
         * @return <code>TRUE</code> if date should be visible.
         */
        public boolean isDateVisible()
        {
            return prefs.isDateVisible(mode);
        }

        /**
         * Sets the date visibility flag.
         *
         * @param flag  flag.
         */
        public void setDateVisible(boolean flag)
        {
            prefs.setDateVisible(mode, flag);
        }

        /**
         * Returns <code>TRUE</code> if time should be visible.
         *
         * @return <code>TRUE</code> if time should be visible.
         */
        public boolean isTimeVisible()
        {
            return prefs.isTimeVisible(mode);
        }

        /**
         * Sets the time visibility flag.
         *
         * @param flag  flag.
         */
        public void setTimeVisible(boolean flag)
        {
            prefs.setTimeVisible(mode, flag);
        }

        /**
         * Returns <code>TRUE</code> if pin should be visible.
         *
         * @return <code>TRUE</code> if pin should be visible.
         */
        public boolean isPinVisible()
        {
            return prefs.isPinVisible(mode);
        }

        /**
         * Sets the pin visibility flag.
         *
         * @param flag  flag.
         */
        public void setPinVisible(boolean flag)
        {
            prefs.setPinVisible(mode, flag);
        }

        /**
         * Returns <code>TRUE</code> if color code should be visible.
         *
         * @return <code>TRUE</code> if color code should be visible.
         */
        public boolean isColorCodeVisible()
        {
            return prefs.isColorCodeVisible(mode);
        }

        /**
         * Sets the color code visibility flag.
         *
         * @param flag  flag.
         */
        public void setColorCodeVisible(boolean flag)
        {
            prefs.setColorCodeVisible(mode, flag);
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds listener.
     *
     * @param l listener.
     */
    public void addListener(IViewModePreferencesChangeListener l)
    {
        listeners.add(l);
    }

    /**
     * Removes listener.
     *
     * @param l listener.
     */
    public void removeListener(IViewModePreferencesChangeListener l)
    {
        listeners.remove(l);
    }

    /**
     * Fires view mode change event.
     *
     * @param mode mode.
     */
    private void fireModeChanged(int mode)
    {
        for (IViewModePreferencesChangeListener listener : listeners)
        {
            listener.viewModeChanged(mode);
        }
    }
}
