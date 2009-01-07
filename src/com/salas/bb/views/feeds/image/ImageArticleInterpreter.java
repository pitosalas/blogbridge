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
// $Id: ImageArticleInterpreter.java,v 1.7 2007/07/13 13:22:52 spyromus Exp $
//

package com.salas.bb.views.feeds.image;

import com.salas.bb.domain.IArticle;
import com.salas.bb.utils.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts article object into meaningful view item data.
 */
class ImageArticleInterpreter
{
    private static final Pattern PAT_IMAGE_URL =
        Pattern.compile("<img [^>]*src=(\"([^\"]+)\"|'([^']+)')[^>]*>");

    /**
     * Detects image URL.
     *
     * @param article   article to scan.
     *
     * @return URL or <code>NULL</code>.
     */
    public static URL getImageURL(IArticle article)
    {
        return getImageURL(article.getLink(), article.getHtmlText());
    }

    /**
     * Detects first valid image URL.
     *
     * @param base base URL.
     * @param html text to scan.
     *
     * @return URL or <code>NULL</code>.
     */
    static URL getImageURL(URL base, String html)
    {
        if (html == null) return null;

        Matcher matcher = PAT_IMAGE_URL.matcher(html);

        URL url = null;

        while (url == null && matcher.find())
        {
            String strURL = matcher.group(2);
            if (strURL == null) strURL = matcher.group(3);
            strURL = StringUtils.quickUnescape(strURL);

            try
            {
                url = new URL(base, strURL);
            } catch (MalformedURLException e)
            {
                url = null;
            }
        }
        return url;
    }

    /**
     * Detects image author.
     *
     * @param article   article.
     *
     * @return author or <code>NULL</code>.
     */
    public static String getAuthor(IArticle article)
    {
        return article.getAuthor();
    }

    /**
     * Detects image comments.
     *
     * @param article   article.
     *
     * @return comments or <code>NULL</code>.
     */
    public static String getComments(IArticle article)
    {
        return article.getPlainText();
    }
}
