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
// $Id: AbstractDisplayModeManager.java,v 1.4 2008/02/28 11:35:39 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.UifUtilities;

import java.awt.*;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Abstract display mode manager is a foundation for all filtered display
 * management.
 */
public abstract class AbstractDisplayModeManager
{
    private static final Logger LOG = Logger.getLogger(AbstractDisplayModeManager.class.getName());

    /**
     * Listeners.
     */
    protected final java.util.List<IDisplayModeManagerListener> listeners;

    /**
     * Map of classes (keys) to modes (values).
     */
    protected final Map<Integer, Color> colors;

    /**
     * The list of class priorities.
     */
    private final int[] classPriorities;

    /**
     * The prefix to use for user preferences access.
     */
    private final String propertyPrefix;

    /**
     * Creates DMM.
     *
     * @param classPriorities   list of classes in priority order.
     * @param propertyPrefix    prefix to use for user preferences access.
     */
    protected AbstractDisplayModeManager(int[] classPriorities, String propertyPrefix)
    {
        colors = new HashMap<Integer, Color>();
        listeners = new CopyOnWriteArrayList<IDisplayModeManagerListener>();
        this.classPriorities = classPriorities;
        this.propertyPrefix = propertyPrefix;
    }

    /**
     * Adds listener to receive color chnage notifications.
     *
     * @param aListener listener.
     */
    public void addListener(IDisplayModeManagerListener aListener)
    {
        if (!listeners.contains(aListener)) listeners.add(aListener);
    }

    /**
     * Removes listener from the list.
     *
     * @param l listener.
     */
    public void removeListener(IDisplayModeManagerListener l)
    {
        listeners.remove(l);
    }

    /**
     * Removes all existing color mappings.
     */
    public void clear()
    {
        synchronized (colors)
        {
            colors.clear();
        }
    }

    /**
     * Returns <code>TRUE</code> if the object with this class is visible according to user preferences.
     *
     * @param cl classes to test.
     *
     * @return <code>TRUE</code> if visible.
     */
    public boolean isVisible(int cl)
    {
        return getColor(cl, false) != null;
    }

    /**
     * Sets the color for the given class.
     *
     * @param cl    class.
     * @param color color or <code>NULL</code> for invisibility.
     */
    public void setColor(int cl, Color color)
    {
        Color oldColor;
        synchronized (colors)
        {
            oldColor = getColor(cl, false);
            colors.put(cl, color);
        }

        fireColorsChanged(cl, oldColor, color);
    }

    /**
     * Fires change in color of the class to all listeners, if colors are different.
     *
     * @param cl        class.
     * @param oldColor  old color.
     * @param newColor  new color.
     */
    private void fireColorsChanged(int cl, Color oldColor, Color newColor)
    {
        if (areColorsDifferent(oldColor, newColor))
        {
            for (IDisplayModeManagerListener listener : listeners)
            {
                listener.onClassColorChanged(cl, oldColor, newColor);
            }
        }
    }

    /**
     * Returns <code>TRUE</code> if colors are different. <code>NULL</code>'s aren't different.
     *
     * @param first     first color sample.
     * @param second    second color sample.
     *
     * @return <code>TRUE</code> if colors are different.
     */
    private static boolean areColorsDifferent(Color first, Color second)
    {
        return first == null
            ? second != null
            : second == null || !first.equals(second);
    }

    /**
     * Stores user preferences to preferences object.
     *
     * @param prefs preferences to put data in.
     *
     * @throws IllegalArgumentException if preferences object isn't specified.
     */
    public void storePreferences(Preferences prefs)
    {
        if (prefs == null) throw new IllegalArgumentException(Strings.error("unspecified.preferences"));

        synchronized (colors)
        {
            for (Map.Entry<Integer, Color> mapping : colors.entrySet())
            {
                Integer cl = mapping.getKey();
                Color color = mapping.getValue();

                String prefKey = propertyPrefix + cl;
                String prefValue = UifUtilities.colorToHex(color);
                prefs.put(prefKey, prefValue);
            }
        }
    }

    /**
     * Restores user preferences from preferences object.
     *
     * @param prefs preferences to read data from.
     *
     * @throws IllegalArgumentException if preferences object isn't specified.
     */
    public void restorePreferences(Preferences prefs)
    {
        if (prefs == null) throw new IllegalArgumentException(Strings.error("unspecified.preferences"));

        boolean cleared = false;

        try
        {
            String[] keys = prefs.keys();
            for (String key : keys)
            {
                if (key != null && key.startsWith(propertyPrefix))
                {
                    if (!cleared)
                    {
                        colors.clear();
                        cleared = true;
                    }

                    String colorValue = prefs.get(key, null);
                    restoreMapping(key, colorValue);
                }
            }
        } catch (BackingStoreException e)
        {
            LOG.log(Level.WARNING, Strings.error("problems.with.backing.store"), e);
        }
    }

    /**
     * Restores single color mapping.
     *
     * @param aKey          key of mapping having class ID in format "<propertyPrefix>&lt;class_id&gt;".
     * @param aColorValue   color value (null or string in format "#rrggbb").
     */
    private void restoreMapping(String aKey, String aColorValue)
    {
        try
        {
            Integer cl = Integer.decode(aKey.substring(5));
            if (aColorValue != null)
            {
                Color color = StringUtils.isEmpty(aColorValue)
                    ? null
                    : Color.decode(aColorValue);

                setColor(cl, color);
            }
        } catch (RuntimeException e)
        {
            // Failed to process key or value -- skip to the next color
            LOG.warning(MessageFormat.format(Strings.error("failed.to.process.key"), aKey));
        }
    }

    /**
     * Returns the color for a given class.
     *
     * @param cl class.
     *
     * @return color or <code>NULL</code> if should be invisible.
     */
    public Color getColor(int cl)
    {
        return getColor(cl, false);
    }

    /**
     * Returns the color for a given class.
     *
     * @param cl class.
     * @param selected TRUE when selected.
     *
     * @return color or <code>NULL</code> if should be invisible.
     */
    public Color getColor(int cl, boolean selected)
    {
        Color color = getDefaultColor(selected);

        int index = 0;
        synchronized (colors)
        {
            while (color != null && index < classPriorities.length)
            {
                if ((cl & classPriorities[index]) != 0) color = getClassColor(classPriorities[index], selected);
                index++;
            }
        }

        return color;
    }

    /**
     * Returns the default color.
     *
     * @param selected TRUE when selected.
     * 
     * @return color.
     */
    protected Color getDefaultColor(boolean selected)
    {
        return Color.BLACK;
    }

    /**
     * Returns color for a single class.
     *
     * @param cl class.
     * @param selected TRUE when selected.
     *
     * @return color.
     */
    private Color getClassColor(int cl, boolean selected)
    {
        Color aColor;
        if (colors.containsKey(cl))
        {
            aColor = colors.get(cl);
        } else
        {
            aColor = getDefaultColor(selected);
        }

        return aColor;
    }
}
