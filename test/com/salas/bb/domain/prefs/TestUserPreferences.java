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
// $Id: TestUserPreferences.java,v 1.28 2008/02/28 15:59:46 spyromus Exp $
//

package com.salas.bb.domain.prefs;

import static com.salas.bb.domain.prefs.UserPreferences.*;
import com.salas.bb.utils.StringUtils;
import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.util.prefs.Preferences;

/**
 * @see UserPreferences
 */
public class TestUserPreferences extends TestCase
{
    private UserPreferences prefs;
    private Preferences p;

    protected void setUp() throws Exception
    {
        prefs = new UserPreferences();
        p = Preferences.userNodeForPackage(TestUserPreferences.class);
    }

    /**
     * Checks each property.
     *
     * @throws Exception in case of any errors.
     */
    public void testPreferences() throws Exception
    {
        checkIntProperty(PROP_AUTO_PURGE_INTERVAL_MINUTES, KEY_AUTO_PURGE_INTERVAL_MINUTES);
        checkBooleanProperty(PROP_BACKGROUND_DEBUG_MODE, KEY_BACKGROUND_DEBUG_MODE);
        checkStringProperty(PROP_INTERNET_BROWSER, KEY_INTERNET_BROWSER);
        checkBooleanProperty(PROP_MARK_READ_WHEN_CHANGING_CHANNELS, KEY_MARK_READ_WHEN_CHANGING_CHANNELS);
        checkBooleanProperty(PROP_MARK_READ_WHEN_CHANGING_GUIDES, KEY_MARK_READ_WHEN_CHANGING_GUIDES);

        checkIntProperty(PROP_PURGE_COUNT, KEY_PURGE_COUNT);
        checkIntProperty(PROP_RSS_POLL_MIN, KEY_RSS_POLL_MIN);

        checkBooleanProperty(PROP_MARK_READ_AFTER_DELAY, KEY_MARK_READ_AFTER_DELAY);
        checkIntProperty(PROP_MARK_READ_AFTER_SECONDS, KEY_MARK_READ_AFTER_SECONDS);

        checkBooleanProperty(PROP_USE_PERSISTENCE, KEY_USE_PERSISTENCE);

        checkIntProperty(PROP_GOOD_CHANNEL_STARZ, KEY_GOOD_CHANNEL_STARZ);

        checkBooleanProperty(PROP_SORTING_ENABLED);
        checkBooleanProperty(PROP_REVERSED_SORT_BY_CLASS_1);
        checkBooleanProperty(PROP_REVERSED_SORT_BY_CLASS_2);
        checkIntProperty(PROP_SORT_BY_CLASS_1);
        checkIntProperty(PROP_SORT_BY_CLASS_2);
        checkIntProperty(PROP_FEED_SELECTION_DELAY);

        checkBooleanProperty(PROP_PRESERVE_UNREAD);
        checkBooleanProperty(PROP_COPY_LINKS_IN_HREF_FORMAT);
        checkBooleanProperty(PROP_AA_TEXT);

        checkIntProperty(PROP_TAGS_STORAGE);
        checkStringProperty(PROP_TAGS_DELICIOUS_USER);
        checkStringProperty(PROP_TAGS_DELICIOUS_PASSWORD);
        checkBooleanProperty(PROP_TAGS_AUTOFETCH);

        checkBooleanProperty(PROP_CHECKING_FOR_UPDATES_ON_STARTUP);

        checkBooleanProperty(PROP_SHOW_TOOLBAR_LABELS);
        checkBooleanProperty(PROP_SHOW_UNREAD_BUTTON_MENU);

        checkBooleanProperty(PROP_SHOW_TOOLBAR);
        checkLongProperty(PROP_READING_LIST_UPDATE_PERIOD);
        checkIntProperty(PROP_FEED_IMPORT_LIMIT);

        checkBooleanProperty(PROP_UPDATE_FEEDS);
        checkBooleanProperty(PROP_UPDATE_READING_LISTS);

        checkIntProperty(PROP_ON_READING_LIST_UPDATE_ACTIONS);
        checkIntProperty(PROP_GUIDE_SELECTION_MODE);

        checkBooleanProperty(PROP_PROXY_ENABLED);
        checkStringProperty(PROP_PROXY_HOST);
        checkIntProperty(PROP_PROXY_PORT);

        checkBooleanProperty(PROP_SHOWING_NEW_PUB_ALERT);
        checkBooleanProperty(PROP_BROWSE_ON_DBL_CLICK);

        checkBooleanProperty(PROP_PING_ON_RL_PUBLICATION);
        checkStringProperty(PROP_PING_ON_RL_PUBLICATION_URL);

        checkBooleanProperty(PROP_NOTIFICATIONS_ENABLED);
        checkBooleanProperty(PROP_SOUND_ON_NEW_ARTICLES);
        checkBooleanProperty(PROP_SOUND_ON_NO_UNREAD);
        checkIntProperty(PROP_NOTIFICATIONS_SHOW_PERIOD);

        checkStringProperty(PROP_NO_DISCOVERY_EXTENSIONS);

        checkBooleanProperty(PROP_SHOW_APPICON_IN_SYSTRAY);
        checkBooleanProperty(PROP_MINIMIZE_TO_SYSTRAY);

        checkIntProperty(PROP_DIB_MODE);

        checkIntProperty(PROP_BRIEF_SENTENCES);
        checkIntProperty(PROP_BRIEF_MIN_LENGTH);
        checkIntProperty(PROP_BRIEF_MAX_LENGTH);

        checkBooleanProperty(PROP_PIN_TAGGING);
        checkStringProperty(PROP_PIN_TAGS);

        checkIntProperty(PROP_PAGE_SIZE);

        checkStringProperty(PROP_WH_IGNORE);
        checkBooleanProperty(PROP_WH_NOSELFLINKS);
        checkBooleanProperty(PROP_WH_SUPPRESS_SAME_SOURCE_LINKS);
        checkLongProperty(PROP_WH_SETTINGS_CHANGE_TIME);

        checkBooleanProperty(PROP_ALWAYS_USE_ENGLISH);
    }

    /**
     * Checks the boolean property with key equal to name.
     *
     * @param name          name of property.
     *
     * @throws Exception in case of any problems.
     */
    private void checkBooleanProperty(String name) throws Exception
    {
        checkBooleanProperty(name, name);
    }

    /**
     * Checks the boolean property.
     *
     * @param name          name of property.
     * @param key           key in preferences storage the property associated with.
     *
     * @throws Exception in case of any problems.
     */
    private void checkBooleanProperty(String name, String key) throws Exception
    {
        Method propsGetter = Preferences.class.getMethod("getBoolean", String.class, Boolean.TYPE);

        checkProperty(name, key, Boolean.TYPE, Boolean.TRUE, Boolean.FALSE, propsGetter);
    }

    /**
     * Checks the integer property with key equal to name.
     *
     * @param name          name of property.
     *
     * @throws Exception in case of any problems.
     */
    private void checkIntProperty(String name) throws Exception
    {
        checkIntProperty(name, name);
    }

    /**
     * Checks the integer property.
     *
     * @param name          name of property.
     * @param key           key in preferences storage the property associated with.
     *
     * @throws Exception in case of any problems.
     */
    private void checkIntProperty(String name, String key) throws Exception
    {
        Method propsGetter = Preferences.class.getMethod("getInt", String.class, Integer.TYPE);

        checkProperty(name, key, Integer.TYPE, 1, 2, propsGetter);
    }

    /**
     * Checks the long property with key equal to name.
     *
     * @param name          name of property.
     *
     * @throws Exception in case of any problems.
     */
    private void checkLongProperty(String name) throws Exception
    {
        checkLongProperty(name, name);
    }

    /**
     * Checks the long property.
     *
     * @param name          name of property.
     * @param key           key in preferences storage the property associated with.
     *
     * @throws Exception in case of any problems.
     */
    private void checkLongProperty(String name, String key) throws Exception
    {
        Method propsGetter = Preferences.class.getMethod("getLong", String.class, Long.TYPE);

        checkProperty(name, key, Long.TYPE, (long)1, (long)2, propsGetter);
    }

    /**
     * Checks the string property.
     *
     * @param name          name of property.
     *
     * @throws Exception in case of any problems.
     */
    private void checkStringProperty(String name)
        throws Exception
    {
        checkStringProperty(name, name);
    }

    /**
     * Checks the string property.
     *
     * @param name          name of property.
     * @param key           key in preferences storage the property associated with.
     *
     * @throws Exception in case of any problems.
     */
    private void checkStringProperty(String name, String key) throws Exception
    {
        Method propsGetter = Preferences.class.getMethod("get", String.class, String.class);

        checkProperty(name, key, String.class, "a", "b", propsGetter);
    }

    /**
     * Checks property of any type.
     *
     * @param name          name of property.
     * @param key           key in preferences storage the property associated with.
     * @param paramClass    class of property.
     * @param s1            first sample value.
     * @param s2            second sample value.
     * @param propsGetter   getter method for preferences storage.
     *
     * @throws Exception in case of any problems.
     */
    private void checkProperty(String name, String key, Class paramClass, Object s1, Object s2,
        Method propsGetter) throws Exception
    {
        String capName = StringUtils.capitalise(name);

        Class clazz = UserPreferences.class;
        Method getter = clazz.getMethod(((Boolean.TYPE == paramClass) ? "is" : "get") + capName);
        Method setter = clazz.getMethod("set" + capName, paramClass);

        // Store test
        setter.invoke(prefs, s1);
        prefs.storeIn(p);
        assertEquals(s1, getter.invoke(prefs));
        assertEquals(propsGetter.invoke(p, key, s2), s1);

        setter.invoke(prefs, s2);
        prefs.storeIn(p);
        assertEquals(s2, getter.invoke(prefs));
        assertEquals(s2, propsGetter.invoke(p, key, s1));

        // Restore test
        p.clear();
        setter.invoke(prefs, s1);
        prefs.storeIn(p);
        setter.invoke(prefs, s2);
        prefs.restoreFrom(p);
        assertEquals(s1, getter.invoke(prefs));
    }
}
