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
// $Id: TestSearchEngine.java,v 1.4 2007/05/10 09:53:59 spyromus Exp $
//

package com.salas.bb.search;

import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.StandardArticle;
import com.salas.bb.domain.StandardGuide;
import junit.framework.TestCase;

/**
 * Tests search engine.
 */
public class TestSearchEngine extends TestCase
{
    /**
     * Tests guide matching.
     */
    public void testMatchesGuide()
    {
        assertTrue(matchGuide("abc", "bc", false));
        assertTrue(matchGuide("bc", "bc", false));
        assertTrue(matchGuide("bcd", "bc", false));
        assertTrue(matchGuide("aBCd", "bc", false));
    }

    /**
     * Tests guide non-matching.
     */
    public void testNotMatchesGuide()
    {
        assertFalse(matchGuide("ab c", "Bc", false));
        assertFalse(matchGuide("b c", "Bc", false));
        assertFalse(matchGuide("b cd", "Bc", false));
        assertFalse(matchGuide("aB Cd", "Bc", false));

        // Pinned articles only
        assertFalse(matchGuide("abc", "bc", true));
        assertFalse(matchGuide("bc", "bc", true));
        assertFalse(matchGuide("bcd", "bc", true));
        assertFalse(matchGuide("aBCd", "bc", true));
    }

    /**
     * Tests feed matching.
     */
    public void testMatchesFeed()
    {
        assertTrue(matchFeed("abc", "bc", false));
        assertTrue(matchFeed("bc", "bc", false));
        assertTrue(matchFeed("bcd", "bc", false));
        assertTrue(matchFeed("aBCd", "bc", false));
    }

    /**
     * Tests feed non-matching.
     */
    public void testNotMatchesFeed()
    {
        assertFalse(matchFeed("ab c", "bc", false));
        assertFalse(matchFeed("b c", "bc", false));
        assertFalse(matchFeed("b cd", "bc", false));
        assertFalse(matchFeed("aB Cd", "BC", false));

        // Pinned articles only
        assertFalse(matchFeed("abc", "bc", true));
        assertFalse(matchFeed("bc", "bc", true));
        assertFalse(matchFeed("bcd", "bc", true));
        assertFalse(matchFeed("aBCd", "bc", true));
    }

    /**
     * Tests article matchinig.
     */
    public void testMatchesArticle()
    {
        assertTrue(matchArticle("title", "text", false, "it", false));
        assertTrue(matchArticle("title", "text", false, "ex", false));

        assertTrue(matchArticle("long title", "long text", false, "g ti", false));
        assertTrue(matchArticle("long title", "long text", false, "g te", false));

        // Pinned articles only
        assertTrue(matchArticle("title", "text", true, "it", true));
        assertTrue(matchArticle("title", "text", true, "ex", true));
    }

    /**
     * Tests article non-matchinig.
     */
    public void testNotMatchesArticle()
    {
        assertFalse(matchArticle("title", "text", false, "ax", false));
        assertFalse(matchArticle("long title", "long text", false, "a ti", false));
        assertFalse(matchArticle("long title", "long text", false, "a te", false));

        // Pinned articles only
        assertFalse(matchArticle("title", "text", false, "it", true));
        assertFalse(matchArticle("title", "text", false, "ex", true));
    }

    /**
     * Checks how the article with given title and text matches given pattern.
     *
     * @param title     title.
     * @param text      text.
     * @param pinned    <code>TRUE</code> if the article should be pinned.
     * @param pattern   pattern.
     * @param pinnedArticlesOnly    <code>TRUE</code> to return pinned articles only.
     *
     * @return <code>TRUE</code> if it matches.
     */
    private static boolean matchArticle(String title, String text, boolean pinned, String pattern, boolean pinnedArticlesOnly)
    {
        StandardArticle article = new StandardArticle(text);
        article.setTitle(title);
        article.setPinned(pinned);

        return SearchEngine.createMatcher(pattern, pinnedArticlesOnly).matches(article);
    }

    /**
     * Checks the feed with the given title for matching with the given pattern.
     *
     * @param title     title of the feed.
     * @param pattern   pattern to check against.
     * @param pinnedArticlesOnly    <code>TRUE</code> to return pinned articles only.
     *
     * @return <code>TRUE</code> if matches.
     */
    private static boolean matchFeed(String title, String pattern, boolean pinnedArticlesOnly)
    {
        DirectFeed feed = new DirectFeed();
        feed.setBaseTitle(title);

        return SearchEngine.createMatcher(pattern, pinnedArticlesOnly).matches(feed);
    }

    /**
     * Checks the guide with the given title for matching with the given pattern.
     *
     * @param title     title of the guide.
     * @param pattern   pattern to check against.
     * @param pinnedArticlesOnly    <code>TRUE</code> to return pinned articles only.
     *
     * @return <code>TRUE</code> if matches.
     */
    private static boolean matchGuide(String title, String pattern, boolean pinnedArticlesOnly)
    {
        StandardGuide guide = new StandardGuide();
        guide.setTitle(title);

        return SearchEngine.createMatcher(pattern, pinnedArticlesOnly).matches(guide);
    }
}
