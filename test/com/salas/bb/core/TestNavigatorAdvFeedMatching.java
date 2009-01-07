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
// $Id: TestNavigatorAdvFeedMatching.java,v 1.1 2008/03/17 12:23:06 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.StandardArticle;
import static com.salas.bb.views.feeds.IFeedDisplayConstants.*;
import junit.framework.TestCase;

/** @see com.salas.bb.core.NavigatorAdv */
public class TestNavigatorAdvFeedMatching extends TestCase
{
    // All possible filters
    private static final int[] ALL_FILTERS = { FILTER_ALL, FILTER_NEGATIVE, FILTER_POSITIVE,
        FILTER_NON_NEGATIVE, FILTER_PINNED, FILTER_UNREAD };
    private DirectFeed feed;

    protected void setUp() throws Exception
    {
        super.setUp();
        feed = new DirectFeed();
    }

    /**
     * With empty feeds any filter / unread-state will return false.
     */
    public void testEmptyFeed()
    {
        for (int filter : ALL_FILTERS) check(filter, false, false);
    }

    /**
     * With all-filter we see if there's read / unread articles for the unread-only flag.
     */
    public void testUnfiltered()
    {
        // Append read article
        addArticle(true, false, false, false);
        check(FILTER_ALL, false, true);

        // Append unread article
        addArticle(false, false, false, false);
        check(FILTER_ALL, true, true);
    }

    /**
     * With unread filter we need an unread article to match.
     */
    public void testUnread()
    {
        // Append read article
        addArticle(true, false, false, false);
        check(FILTER_UNREAD, false, false);

        // Append unread article
        addArticle(false, false, false, false);
        check(FILTER_UNREAD, true, true);
    }

    /**
     * With pinned filter we need a pinned article to match.
     */
    public void testPinned()
    {
        // Append pinned read article
        addArticle(true, true, false, false);
        check(FILTER_PINNED, false, true);

        // Append pinned unread article
        addArticle(false, true, false, false);
        check(FILTER_PINNED, true, true);
    }

    /**
     * With positive filter we need a positive article to match.
     */
    public void testPositive()
    {
        // Append positive read article
        addArticle(true, false, true, false);
        check(FILTER_POSITIVE, false, true);

        // Append positive unread article
        addArticle(false, false, true, false);
        check(FILTER_POSITIVE, true, true);
    }

    public void testNegative()
    {
        // Append negative read article
        addArticle(true, false, false, true);
        check(FILTER_NEGATIVE, false, true);

        // Append negative unread article
        addArticle(false, false, false, true);
        check(FILTER_NEGATIVE, true, true);
    }

    public void testNonNegative()
    {
        // Append non-negative read article
        addArticle(true, false, false, false);
        check(FILTER_NON_NEGATIVE, false, true);

        // Append non-negative unread article
        addArticle(false, false, false, false);
        check(FILTER_NON_NEGATIVE, true, true);
    }

    /**
     * Checks the response from the method.
     *
     * @param filter                filter to apply.
     * @param withUnreadOnly        response for the version with the UnreadOnly flag.
     * @param withoutUnreadOnly     response for the version without the UnreadOnly flag.
     */
    private void check(int filter, boolean withUnreadOnly, boolean withoutUnreadOnly)
    {
        assertEquals(withoutUnreadOnly, NavigatorAdv.isFeedMatching(feed, false, filter));
        assertEquals(withUnreadOnly,    NavigatorAdv.isFeedMatching(feed, true, filter));
    }

    /**
     * Creates an article with given properties and adds to the feed.
     *
     * @param read      read.
     * @param pinned    pinned.
     * @param positive  positive.
     * @param negative  negative.
     */
    private void addArticle(boolean read, boolean pinned, boolean positive, boolean negative)
    {
        CustomArticle article = new CustomArticle(read, pinned, positive, negative);
        feed.appendArticle(article);
    }

    /**
     * Custom article.
     */
    private static class CustomArticle extends StandardArticle
    {
        // Use sequence numbers to avoid duplicates
        private static int seq = 0;

        private boolean read;
        private boolean pinned;
        private boolean positive;
        private boolean negative;

        /**
         * Creates an article with given properties.
         *
         * @param read      read.
         * @param pinned    pinned.
         * @param positive  positive.
         * @param negative  negative.
         */
        private CustomArticle(boolean read, boolean pinned, boolean positive, boolean negative)
        {
            super("");
            
            setTitle(Integer.toString(seq++));

            this.read = read;
            this.pinned = pinned;
            this.positive = positive;
            this.negative = negative;
        }

        @Override
        public boolean isRead()
        {
            return read;
        }

        @Override
        public boolean isPinned()
        {
            return pinned;
        }

        @Override
        public boolean isPositive()
        {
            return positive;
        }

        @Override
        public boolean isNegative()
        {
            return negative;
        }
    }
}