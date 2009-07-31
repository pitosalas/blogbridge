/*
 * BlogBridge -- RSS feed reader, manager, and web based service
 * Copyright (C) 2002-2009 by R. Pito Salas
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

package com.salas.bb.domain;

import java.net.URL;
import java.util.Date;

/**
 * Feed handling type
 */
public abstract class FeedHandlingType
{
    private static final int T_LINK_TITLE                   = 0;
    private static final int T_LINK_TITLE_PUBDATE           = 1;

    public static final FeedHandlingType LINK_TITLE         = new LinkTitleFH();
    public static final FeedHandlingType LINK_TITLE_PUBDATE = new LinkTitlePubdateFH();

    /** Default link handling type. */
    public static final FeedHandlingType DEFAULT            = LINK_TITLE;

    /** All available types. */
    public static final FeedHandlingType[] ALL_TYPES        = { LINK_TITLE, LINK_TITLE_PUBDATE };

    private final String name;
    private final int    key;

    /**
     * Factory constructor.
     *
     * @param name name.
     * @param key  key.
     */
    protected FeedHandlingType(String name, int key)
    {
        this.name = name;
        this.key = key;
    }

    /**
     * Generates an article match key.
     *
     * @param article article.
     *
     * @return key.
     */
    public abstract String generateArticleMatchKey(IArticle article);

    @Override
    public String toString()
    {
        return name;
    }

    /**
     * Return an integer key of the type.
     *
     * @return integer.
     */
    public int toInteger()
    {
        return key;
    }

    /**
     * Converts the key type into the object.
     *
     * @param key key.
     *
     * @return object.
     */
    public static FeedHandlingType toObject(int key)
    {
        FeedHandlingType obj;

        switch (key)
        {
            case T_LINK_TITLE_PUBDATE:
                obj = LINK_TITLE_PUBDATE;
                break;
            default:
                obj = LINK_TITLE;
        }

        return obj;
    }

    /**
     * Default implementation using Link and Title only.
     */
    private static class LinkTitleFH extends FeedHandlingType
    {
        /** Constructor. */
        protected LinkTitleFH()
        {
            super("Default", T_LINK_TITLE);
        }

        /**
         * Generates an article match key.
         *
         * @param article article.
         *
         * @return key.
         */
        public String generateArticleMatchKey(IArticle article)
        {
            URL url = article.getLink();
            String title = article.getTitle();

            long code = (url == null) ? 0 : Math.abs(url.toString().hashCode());
            code = code * 29L + (title == null ? 0 : Math.abs(title.hashCode()));

            return Long.toHexString(code);
        }
    }

    /**
     * Link, title and pub date.
     */
    private static class LinkTitlePubdateFH extends FeedHandlingType
    {
        /** Constructor. */
        protected LinkTitlePubdateFH()
        {
            super("Wiki / CMS", T_LINK_TITLE_PUBDATE);
        }

        /**
         * Generates an article match key.
         *
         * @param article article.
         *
         * @return key.
         */
        public String generateArticleMatchKey(IArticle article)
        {
            URL url = article.getLink();
            String title = article.getTitle();
            Date pubDate = article.getPublicationDate();

            long code = (url == null) ? 0 : Math.abs(url.toString().hashCode());
            code = code * 29L + (title == null ? 0 : Math.abs(title.hashCode()));
            code = code * 29L + (pubDate == null ? 0 : pubDate.getTime());

            return Long.toHexString(code);
        }
    }
}
