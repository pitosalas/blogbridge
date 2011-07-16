/*
 * BlogBridge -- RSS feed reader, manager, and web based service
 * Copyright (C) 2002-2011 by R. Pito Salas
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Contact: R. Pito Salas
 * mailto:pitosalas@users.sourceforge.net
 * More information: about BlogBridge
 * http://www.blogbridge.com
 * http://sourceforge.net/projects/blogbridge
 */

package com.salas.bb.utils;

import com.salas.bb.utils.net.BBHttpClient;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Instapaper functions.
 */
public class Instapaper
{
    private static final Pattern PATTERN = Pattern.compile("(<div id=\"story\">.*)<div class=\"bar bottom\">", Pattern.MULTILINE | Pattern.DOTALL);

    /**
     * Mobilizes the document behind the URL.
     *
     * @param url URL.
     *
     * @return content.
     *
     * @throws IOException I/O error.
     */
    public static String mobilize(String url)
        throws IOException
    {
        String mobileURL = "http://www.instapaper.com/m?u=" + StringUtils.encodeForURL(url);
        String srcHTML = BBHttpClient.get(new URL(mobileURL));
        return processMobilizedString(srcHTML);
    }

    /**
     * Processes the mobilized string by removing everything else from it.
     *
     * @param html HTML.
     *
     * @return result.
     */
    public static String processMobilizedString(String html)
    {
        Matcher m = PATTERN.matcher(html);
        return m.find() ? "<!-- mob-start -->\n" + m.group(1).trim() + "\n<!-- mob-end -->" : "";
    }
}
