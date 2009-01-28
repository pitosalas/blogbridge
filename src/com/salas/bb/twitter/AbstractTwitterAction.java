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
// $Id$
//

package com.salas.bb.twitter;

import com.salas.bb.core.GlobalController;

import javax.swing.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.UnsupportedEncodingException;

/**
 * Abstract base class for twitter actions.
 */
public abstract class AbstractTwitterAction extends AbstractAction
{
    private static final Pattern PATTERN_SCREEN_NAME =
        Pattern.compile("http://(www\\.)?twitter\\.com/([^/\\?\\#\\s]+)($|#|\\?)");

    /**
     * Returns preferences.
     *
     * @return preferences.
     */
    protected TwitterPreferences getPreferences()
    {
        return GlobalController.SINGLETON.getModel().getUserPreferences().getTwitterPreferences();
    }

    /**
     * Returns the username from the URL or NULL.
     *
     * @param url URL to analyze.
     *
     * @return screen name.
     */
    static String urlToScreenName(URL url)
    {
        String name = null;

        String urls = url.toString();
        Matcher m = PATTERN_SCREEN_NAME.matcher(urls);
        try
        {
            if (m.find()) name = URLDecoder.decode(m.group(2), "UTF-8");
        } catch (UnsupportedEncodingException e)
        {
            // Failed transformation -- ignore
        }

        return name;
    }
}
