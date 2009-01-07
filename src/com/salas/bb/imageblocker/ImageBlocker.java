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
// $Id: ImageBlocker.java,v 1.5 2008/02/22 13:53:10 spyromus Exp $
//

package com.salas.bb.imageblocker;

import com.salas.bb.utils.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

/**
 * Central image blocker interface.
 */
public class ImageBlocker
{
    /** Image blocker patterns key. */
    public static final String KEY = "imageblocker.Expressions";

    private static Pattern pattern;
    private static List<String> expressions = new ArrayList<String>();

    /**
     * Returns TRUE if the URL given matches one of expressions.
     *
     * @param url URL.
     *
     * @return TRUE if blocked, FALSE if not or NULL.
     */
    public static synchronized boolean isBlocked(URL url)
    {
        if (url == null) return false;

        String str = url.toString();
        return pattern != null && pattern.matcher(str).find();
    }

    /**
     * Sets patterns and persists.
     *
     * @param pats      patters as text (pattern per line).
     */
    public static synchronized void setExpressions(String pats)
    {
        clearExpressions();

        if (pats != null)
        {
            String[] patterns = pats.split("\n");
            for (String pattern : patterns)
            {
                if (StringUtils.isNotEmpty(pattern)) addExpression(pattern);
            }
        }
    }

    /**
     * Adds a expression if it's not already there.
     *
     * @param expression expression.
     */
    public static synchronized void addExpression(String expression)
    {
        if (!expressions.contains(expression))
        {
            // Save new expression
            expressions.add(expression);

            // Prepare a pattern
            String[] expr = expressions.toArray(new String[ImageBlocker.expressions.size()]);
            pattern = Pattern.compile(StringUtils.keywordsToPattern(expr));
        }
    }

    /**
     * Clears all expressions.
     */
    public static synchronized void clearExpressions()
    {
        expressions.clear();
        pattern = null;
    }

    /**
     * Returns all expressions.
     *
     * @return expressions.
     */
    public static synchronized List<String> getExpressions()
    {
        return Collections.unmodifiableList(expressions);
    }

    /**
     * Restores preferences from the given object.
     *
     * @param prefs preferences
     */
    public static synchronized void restorePreferences(Preferences prefs)
    {
        setExpressions(prefs.get(KEY, ""));
    }

    /**
     * Stores preferences to a prefs object.
     *
     * @param prefs preferences.
     */
    public static synchronized void storePreferences(Preferences prefs)
    {
        prefs.put(KEY, StringUtils.join(expressions.iterator(), "\n"));
    }
}
