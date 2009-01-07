// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2007 by R. Pito Salas
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
// $Id: AdvancedPreferencesPlugin.java,v 1.3 2007/08/24 06:11:21 spyromus Exp $
//

package com.salas.bb.plugins.domain;

import org.jdom.Element;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Advanced preferences plug-in lets a user to specify the values
 * for hidden advanced preferences.
 */
public class AdvancedPreferencesPlugin implements IPlugin
{
    private static final Map<String, PropertyHandler> PREF_MAPPING;

    private Map<String, String> props;

    static {
        Map<String, PropertyHandler> map = new HashMap<String, PropertyHandler>();
        /** The list of extensions not to discover. */
        map.put("discovery.skipExtensions", new StringPH("noDiscoveryExtensions"));
        /** Enables grouping of articles. */
        map.put("articles.showGroups", new BooleanPH("render.isGroupingEnabled"));
        /** Enables showing empty groups. */
        map.put("articles.showEmptyGroups", new BooleanPH("render.isShowEmptyGroups"));
        /** The delay between clicking on a feed cell and the selection. */
        map.put("feeds.selectionDelay", new IntegerPH("feedSelectionDelay"));
        /** The number of updates per day considered as top activity. */
        map.put("starz.topActivity", new IntegerPH("starz.topActivity"));
        /** The number of highlights per day considered as top activity. */
        map.put("starz.maxHightlights", new IntegerPH("starz.topHighlights"));
        /** Don't update Search Feeds with new articles automatically. */
        map.put("searchfeeds.dontUpdateAutomatically", new BooleanPH("searchfeeds.dontUpdateAutomatically"));
        PREF_MAPPING = Collections.unmodifiableMap(map);
    }

    /**
     * Creates the plug-in.
     *
     * @param el element.
     */
    public AdvancedPreferencesPlugin(Element el)
    {
        props = PluginUtils.getPluginProperties(el);
    }

    // ------------------------------------------------------------------------
    // IPlugin implementation
    // ------------------------------------------------------------------------

    /**
     * Returns the name of plug-in type (Theme, Actions ...).
     *
     * @return the name of plug-in type.
     */
    public String getTypeName()
    {
        return "Preferences";
    }

    /** Initializes plug-in. */
    public void initialize()
    {
    }

    // ------------------------------------------------------------------------
    // Advanced Plug-in Implementation
    // ------------------------------------------------------------------------

    /**
     * Invoked when the plug-in is expected to provide its own preferences.
     * 
     * @param prefs application preferences object.
     */
    public void overridePreferences(Preferences prefs)
    {
        if (props != null)
        {
            for (Map.Entry<String, String> prop : props.entrySet())
            {
                String key = prop.getKey();
                PropertyHandler ph = mapProperty(key);
                if (ph != null) ph.setValue(prefs, prop.getValue());
            }
        }
    }

    /**
     * Maps a property from the plug-in namespace to the application namespace.
     *
     * @param prop property name in the plug-in namespace.
     *
     * @return property name.
     */
    private static PropertyHandler mapProperty(String prop)
    {
        return PREF_MAPPING.get(prop);
    }

    // ------------------------------------------------------------------------
    // Property types
    // ------------------------------------------------------------------------

    /**
     * Property handler.
     */
    private abstract static class PropertyHandler
    {
        private final String key;

        /**
         * Creates the handler.
         *
         * @param key property key to set.
         */
        public PropertyHandler(String key)
        {
            this.key = key;
        }

        /**
         * Sets the property value if it's valid.
         *
         * @param prefs preferences object where to set the property.
         * @param value property value.
         */
        public void setValue(Preferences prefs, String value)
        {
            if (isValid(value)) prefs.put(key, value);
        }

        /**
         * Returns <code>TRUE</code> if the value fits the type of the property.
         *
         * @param value value.
         *
         * @return <code>TRUE</code> if fine.
         */
        protected abstract boolean isValid(String value);
    }

    /** Boolean property handler. */
    private static class BooleanPH extends PropertyHandler
    {
        /**
         * Creates the handler.
         *
         * @param key property key to set.
         */
        public BooleanPH(String key)
        {
            super(key);
        }

        /**
         * Returns <code>TRUE</code> if the value fits the type of the property.
         *
         * @param value value.
         *
         * @return <code>TRUE</code> if fine.
         */
        protected boolean isValid(String value)
        {
            return value != null && value.toLowerCase().trim().matches("(true|false)");
        }
    }

    /** String property handler. */
    private static class StringPH extends PropertyHandler
    {
        /**
         * Creates the handler.
         *
         * @param key property key to set.
         */
        public StringPH(String key)
        {
            super(key);
        }

        /**
         * Returns <code>TRUE</code> if the value fits the type of the property.
         *
         * @param value value.
         *
         * @return <code>TRUE</code> if fine.
         */
        protected boolean isValid(String value)
        {
            return true;
        }
    }

    /** Integer property handler. */
    private static class IntegerPH extends PropertyHandler
    {
        /**
         * Creates the handler.
         *
         * @param key property key to set.
         */
        public IntegerPH(String key)
        {
            super(key);
        }

        /**
         * Returns <code>TRUE</code> if the value fits the type of the property.
         *
         * @param value value.
         *
         * @return <code>TRUE</code> if fine.
         */
        protected boolean isValid(String value)
        {
            return value.matches("[0-9]+");
        }
    }
}
