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
// $Id: Strings.java,v 1.6 2007/06/27 08:22:08 spyromus Exp $
//

package com.salas.bb.utils.i18n;

import com.salas.bb.utils.StringUtils;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Manager of i18n strings collections. The strings are stored in "Strings" bundle.
 * @noinspection HardCodedStringLiteral
 */
public abstract class Strings
{
    private static final Logger LOG = Logger.getLogger(Strings.class.getName());
    private static final ResourceBundle DEFAULT_STRINGS_BUNDLE =
            ResourceBundle.getBundle("Strings", Locale.getDefault(), Strings.class.getClassLoader());
    private static final ResourceBundle ERRORS_BUNDLE = ResourceBundle.getBundle("Errors");

    /** Strings bundle to use. */
    private static ResourceBundle stringsBundle = DEFAULT_STRINGS_BUNDLE;

    /**
     * Returns current string bundle.
     *
     * @return bundle.
     */
    public static ResourceBundle getStringsBundle()
    {
        return stringsBundle;
    }

    /**
     * Sets new strings bundle.
     *
     * @param bundle new bundle.
     */
    public static void setStringsBundle(ResourceBundle bundle)
    {
        if (bundle == null) return;
        stringsBundle = bundle;
    }

    /**
     * Returns string for a given key.
     *
     * @param key key to get string for.
     *
     * @return string or key itself (plus message to log at INFO level if some key isn't present).
     */
    public static String message(String key)
    {
        return getWithFullKey(key, stringsBundle);
    }

    /**
     * Returns error string for a given key.
     *
     * @param key key to get string for.
     *
     * @return string or key itself (plus message to log at INFO level if some key isn't present).
     */
    public static String error(String key)
    {
        return getWithFullKey(key, ERRORS_BUNDLE);
    }

    /**
     * Returns string for a given full key (including prefix).
     *
     * @param aFullKey  full key.
     * @param bundle    bundle to grab strings from.
     *
     * @return string or key itself (plus message to log at INFO level if some key isn't present).
     */
    private static String getWithFullKey(String aFullKey, ResourceBundle bundle)
    {
        String value = bundle.getString(aFullKey);
        if (value == null)
        {
            value = aFullKey;
            if (LOG.isLoggable(Level.INFO))
            {
                LOG.info("Key " + aFullKey + " is not defined in Strings bundle.");
            }
        }
        return value;
    }

    /**
     * Returns sliced version of a string. Slice separator is '~'.
     *
     * @param key key.
     */
    public static String[] slices(String key)
    {
        String value = message(key);
        return StringUtils.split(value, "~");
    }
}
