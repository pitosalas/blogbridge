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
// $Id: TestGotoNextGuideWithUnreadAction.java,v 1.7 2006/01/12 12:10:43 spyromus Exp $
//

package com.salas.bb.core.actions.guide;

import junit.framework.TestCase;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.StandardGuide;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.DataFeed;
import com.salas.bb.utils.parser.Channel;

/**
 * @see GotoNextGuideWithUnreadAction
 */
public class TestGotoNextGuideWithUnreadAction extends TestCase
{
    private GuidesSet       cgs;
    private StandardGuide   cg1;
    private StandardGuide   cg2;
    private StandardGuide   cg3;

    protected void setUp() throws Exception
    {
        cgs = new GuidesSet();

        cg1 = new StandardGuide();
        cg1.setTitle("1");
        cg2 = new StandardGuide();
        cg2.setTitle("2");
        cg3 = new StandardGuide();
        cg3.setTitle("3");

        cgs.add(cg1);
        cgs.add(cg2);
        cgs.add(cg3);
    }

    /**
     * [c]
     * [ ]
     * [ ]
     * @see GotoNextGuideWithUnreadAction#findNextGuideWithUnread
     */
    public void testFindNextGuideWithUnread1()
    {
        assertNull(GotoNextGuideWithUnreadAction.findNextGuideWithUnread(cgs, cg1));
    }

    /**
     * [ ]
     * [c]
     * [ ]
     * @see GotoNextGuideWithUnreadAction#findNextGuideWithUnread
     */
    public void testFindNextGuideWithUnread2()
    {
        assertNull(GotoNextGuideWithUnreadAction.findNextGuideWithUnread(cgs, cg2));
    }

    /**
     * [ ]
     * [ ]
     * [c]
     * @see GotoNextGuideWithUnreadAction#findNextGuideWithUnread
     */
    public void testFindNextGuideWithUnread3()
    {
        assertNull(GotoNextGuideWithUnreadAction.findNextGuideWithUnread(cgs, cg3));
    }

    /**
     * [c]
     * [u]
     * [ ]
     * @see GotoNextGuideWithUnreadAction#findNextGuideWithUnread
     */
    public void testFindNextGuideWithUnread4()
    {
        cg2.add(new CustomFeed(true));
        assertTrue(cg2 == GotoNextGuideWithUnreadAction.findNextGuideWithUnread(cgs, cg1));
    }

    /**
     * [c]
     * [ ]
     * [u]
     * @see GotoNextGuideWithUnreadAction#findNextGuideWithUnread
     */
    public void testFindNextGuideWithUnread5()
    {
        cg3.add(new CustomFeed(true));
        assertTrue(cg3 == GotoNextGuideWithUnreadAction.findNextGuideWithUnread(cgs, cg1));
    }

    /**
     * [u]
     * [c]
     * [ ]
     * @see GotoNextGuideWithUnreadAction#findNextGuideWithUnread
     */
    public void testFindNextGuideWithUnread6()
    {
        cg1.add(new CustomFeed(true));
        assertTrue(cg1 == GotoNextGuideWithUnreadAction.findNextGuideWithUnread(cgs, cg2));
    }

    /**
     * [ ]
     * [c]
     * [u]
     * @see GotoNextGuideWithUnreadAction#findNextGuideWithUnread
     */
    public void testFindNextGuideWithUnread7()
    {
        cg3.add(new CustomFeed(true));
        assertTrue(cg3 == GotoNextGuideWithUnreadAction.findNextGuideWithUnread(cgs, cg2));
    }

    /**
     * [u]
     * [ ]
     * [c]
     * @see GotoNextGuideWithUnreadAction#findNextGuideWithUnread
     */
    public void testFindNextGuideWithUnread8()
    {
        cg1.add(new CustomFeed(true));
        assertTrue(cg1 == GotoNextGuideWithUnreadAction.findNextGuideWithUnread(cgs, cg3));
    }

    /**
     * CGE for reporting unread records.
     */
    private static class CustomFeed extends DataFeed
    {
        private boolean hasUnread;

        /**
         * Constructs CGE.
         *
         * @param aHasUnread <code>true</code> to report unread records.
         */
        public CustomFeed(boolean aHasUnread)
        {
            hasUnread = aHasUnread;
        }

        /**
         * Fetches the feed by some specific means.
         *
         * @return the feed or NULL if there was an error or no updates required.
         */
        protected Channel fetchFeed()
        {
            return null;
        }

        /**
         * Returns title of feed.
         *
         * @return title.
         */
        public String getTitle()
        {
            return null;
        }


        /**
         * Sets the flag of unread records.
         *
         * @param aHasUnread <code>true</code> to report unread records.
         */
        public void setHasUnread(boolean aHasUnread)
        {
            hasUnread = aHasUnread;
        }

        /**
         * Returns unread articles count.
         *
         * @return count.
         */
        public int getUnreadArticlesCount()
        {
            return hasUnread ? 1 : 0;
        }

        /**
         * Returns the Article at the specified index.
         *
         * @param index index of article in channel.
         *
         * @return article object.
         */
        public IArticle getArticleAt(int index)
        {
            return null;
        }

        /**
         * Returns simple match key, which can be used to detect similarity of feeds. For example, it's
         * XML URL for the direct feeds, query type + parameter for the query feeds, serialized search
         * criteria for the search feeds.
         *
         * @return match key.
         */
        public String getMatchKey()
        {
            return null;
        }
    }
}
