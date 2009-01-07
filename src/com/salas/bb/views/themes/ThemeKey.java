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
// $Id: ThemeKey.java,v 1.10 2008/02/28 15:59:52 spyromus Exp $
//

package com.salas.bb.views.themes;

/**
 * Theme key is a key used to get properties from themes.
 */
public abstract class ThemeKey
{
    private String key;

    /**
     * Hidden constructor of enumeration.
     *
     * @param key key value.
     */
    private ThemeKey(String key)
    {
        this.key = key;
    }

    /**
     * Returns key value.
     *
     * @return key value.
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Key of color value stored in the theme.
     */
    public static final class Color extends ThemeKey
    {
        /** Background color of unselected article. */
        public static final Color ARTICLE_UNSEL_BG;
        /** Background color of selected article. */
        public static final Color ARTICLE_SEL_BG;
        /** Foreground color of text of unselected article. */
        public static final Color ARTICLE_TEXT_UNSEL_FG;
        /** Foreground color of text of selected article. */
        public static final Color ARTICLE_TEXT_SEL_FG;
        /** Foreground color of unselected article title. */
        public static final Color ARTICLE_TITLE_UNSEL_FG;
        /** Foreground color of selected article title. */
        public static final Color ARTICLE_TITLE_SEL_FG;
        /** Foreground color of date of unselected article. */
        public static final Color ARTICLE_DATE_UNSEL_FG;
        /** Foreground color of date of selected article. */
        public static final Color ARTICLE_DATE_SEL_FG;

        /** Background color of article group header. */
        public static final Color ARTICLEGROUP_BG;
        /** Foreground color of article group header. */
        public static final Color ARTICLEGROUP_FG;

        /** Background color of articles list. */
        public static final Color ARTICLELIST_BG;
        /** Foreground color of feed name in the article list header. */
        public static final Color ARTICLELIST_FEEDNAME_FG;

        /** Background color of feeds list. */
        public static final Color FEEDSLIST_BG;
        /** Foreground color of feeds list. */
        public static final Color FEEDSLIST_FG;
        /** Alternating background color for cells. */
        public static final Color FEEDSLIST_ALT_BG;
        /** Selected background color for cells. */
        public static final Color FEEDSLIST_SEL_BG;
        /** Selected foreground color for cells. */
        public static final Color FEEDSLIST_SEL_FG;

        /** Background color of undiscovered blog link. */
        public static final Color BLOGLINK_UNDISC_BG;
        /** Background color of discovered blog link. */
        public static final Color BLOGLINK_DISC_BG;

        static
        {
            ARTICLE_UNSEL_BG          = new Color("article.unselected.background");
            ARTICLE_SEL_BG            = new Color("article.selected.background");
            ARTICLE_TEXT_UNSEL_FG     = new Color("article.text.unselected.foreground");
            ARTICLE_TEXT_SEL_FG       = new Color("article.text.selected.foreground");
            ARTICLE_TITLE_UNSEL_FG    = new Color("article.title.unselected.foreground");
            ARTICLE_TITLE_SEL_FG      = new Color("article.title.selected.foreground");
            ARTICLE_DATE_UNSEL_FG     = new Color("article.date.unselected.foreground");
            ARTICLE_DATE_SEL_FG       = new Color("article.date.selected.foreground");
            ARTICLEGROUP_BG           = new Color("articlegroup.background");
            ARTICLEGROUP_FG           = new Color("articlegroup.foreground");
            ARTICLELIST_BG            = new Color("articlelist.background");
            ARTICLELIST_FEEDNAME_FG   = new Color("articlelist.feedname.foreground");
            FEEDSLIST_BG              = new Color("feedslist.background");
            FEEDSLIST_FG              = new Color("feedslist.foreground");
            FEEDSLIST_ALT_BG          = new Color("feedslist.alt.background");
            FEEDSLIST_SEL_BG          = new Color("feedslist.sel.background");
            FEEDSLIST_SEL_FG          = new Color("feedslist.sel.foreground");
            BLOGLINK_UNDISC_BG        = new Color("bloglink.undiscovered.background");
            BLOGLINK_DISC_BG          = new Color("bloglink.discovered.background");
        }

        /**
         * Hidden constructor of enumeration.
         *
         * @param key key value.
         */
        private Color(String key)
        {
            super(key);
        }
    }

    /**
     * Key of font value stored in the theme.
     */
    public static final class Font extends ThemeKey
    {
        /** Main font. */
        public static final Font MAIN;
        /** Font of article text. */
        public static final Font ARTICLE_TEXT;
        /** Font of article title. */
        public static final Font ARTICLE_TITLE;
        /** Font of article date. */
        public static final Font ARTICLE_DATE;

        /** Font of article group header. */
        public static final Font ARTICLEGROUP;

        /** Font of feed name in the articles list header. */
        public static final Font ARTICLELIST_FEEDNAME;

        static
        {
            MAIN                    = new Font("font");
            ARTICLE_TEXT            = new Font("article.text.font");
            ARTICLE_TITLE           = new Font("article.title.font");
            ARTICLE_DATE            = new Font("article.date.font");
            ARTICLEGROUP            = new Font("articlegroup.font");
            ARTICLELIST_FEEDNAME    = new Font("articlelist.feedname.font");
        }

        /**
         * Hidden constructor of enumeration.
         *
         * @param key key value.
         */
        private Font(String key)
        {
            super(key);
        }
    }
}
