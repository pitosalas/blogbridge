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
// $Id: TestAbstractFeed.java,v 1.13 2007/05/28 11:42:11 spyromus Exp $
//

package com.salas.bb.domain;

import junit.framework.TestCase;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This tests suite contains tests for <code>AbstractFeed</code> class.
 * It covers: construction, getting and setting propertoes, managing processing state,
 * counting unread articles and working with listeners.
 */
public class TestAbstractFeed extends TestCase
{
    private AbstractFeed feed;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        feed = new DummyFeed();
    }

    // ---------------------------------------------------------------------------------------------
    // Unit tests
    // ---------------------------------------------------------------------------------------------

    /**
     * Tests default initialization of properties.
     */
    public void testConstruction()
    {
        assertEquals("Wrong title.", "Dummy", feed.getTitle());
        assertEquals("Guide wasn't assigned.", 0, feed.getParentGuides().length);
        assertNull("Feed is valid.", feed.getInvalidnessReason());
        assertFalse("Feed is valid.", feed.isInvalid());
        assertFalse("Feed is in passive state.", feed.isProcessing());
    }

    /**
     * Tests storing of guide reference.
     */
    public void testGetSetGuide()
    {
        assertEquals("Holder guide wasn't assigned.", 0, feed.getParentGuides().length);

        IGuide guide = new StandardGuide();
        feed.addParentGuide(guide);

        assertTrue("Wrong guide.", feed.belongsTo(guide));
    }

    /**
     * Tests how processing started event is being handled. Processing can be started
     * multiple times.
     */
    public void testProcessingStarted()
    {
        assertFalse("Feed is in passive state.", feed.isProcessing());

        feed.processingStarted();
        assertTrue("Feed is in active state.", feed.isProcessing());

        feed.processingStarted();
        assertTrue("Feed is in active state.", feed.isProcessing());
    }

    /**
     * Tests how processing finished event is being handled. Processing can be finished
     * multiple times.
     */
    public void testProcessingFinished()
    {
        assertFalse("Feed is in passive state.", feed.isProcessing());

        feed.processingStarted();
        feed.processingFinished();
        assertFalse("Feed is in passive state.", feed.isProcessing());

        feed.processingStarted();
        feed.processingStarted();
        feed.processingFinished();
        assertTrue("Feed is in active state.", feed.isProcessing());
        feed.processingFinished();
        assertFalse("Feed is in passive state.", feed.isProcessing());

        Logger logger = Logger.getLogger(AbstractFeed.class.getName());
        Level oldLevel = logger.getLevel();
        logger.setLevel(Level.OFF);
        try
        {
            feed.processingFinished();
            assertFalse("Feed is in passive state.", feed.isProcessing());
        } finally
        {
            logger.setLevel(oldLevel);
        }
    }

    /**
     * Tests how the feed read state is reported.
     */
    public void testIsRead()
    {
        IArticle article = feed.getArticleAt(0);

        article.setRead(false);
        assertFalse("The article is unread.", feed.isRead());

        article.setRead(true);
        assertTrue("The article is read.", feed.isRead());
    }

    /**
     * Tests how the feed is marked as read.
     */
    public void testSetRead()
    {
        IArticle article = feed.getArticleAt(0);
        article.setRead(false);

        feed.setRead(true);
        assertTrue("The article is read.", feed.isRead());
        assertTrue("The article should be read.", article.isRead());

        feed.setRead(false);
        assertFalse("The article is unread.", feed.isRead());
        assertFalse("The article should be unread.", article.isRead());
    }

    /**
     * Tests how the number of unread articles is returned.
     */
    public void testGetUnreadArticlesCount()
    {
        IArticle article = feed.getArticleAt(0);

        article.setRead(false);
        assertEquals("Wrong number of unread articles.", 1, feed.getUnreadArticlesCount());

        article.setRead(true);
        assertEquals("Wrong number of unread articles.", 0, feed.getUnreadArticlesCount());
    }

    /**
     * Tests getting and setting of invalidness reason.
     */
    public void testGetSetInvalidnessReason()
    {
        assertNull("Feed should be valid initially.", feed.getInvalidnessReason());

        feed.setInvalidnessReason("Some reason");
        assertEquals("Wrong reason.", "Some reason", feed.getInvalidnessReason());

        feed.setInvalidnessReason(null);
        assertNull("Feed should be valid.", feed.getInvalidnessReason());
    }

    /**
     * Tests reporting invalid state based on invalidness reason setting.
     */
    public void testIsInvalid()
    {
        assertFalse("Feed should be valid initially.", feed.isInvalid());

        feed.setInvalidnessReason("Some reason");
        assertTrue("Feed should be invalid.", feed.isInvalid());

        feed.setInvalidnessReason(null);
        assertFalse("Feed should be valid.", feed.isInvalid());
    }

    // ------------------------------------------------------------------------
    // Words Equal
    // ------------------------------------------------------------------------

    private static final String[] SET_EMPTY = {};
    private static final String[] SET_1     = { "abc", "123", "qwe", "456" };
    private static final String[] SET_1_1   = { "aBc", "123", "QwE", "456" };
    private static final String[] SET_2     = { "asd", "000", "fgh", "jkl", "poi" };

    /**
     * Tests comparing empty sets.
     */
    public void testWordsEqual_Empty()
    {
        assertFalse("Both sets are empty", AbstractFeed.wordsEqual(SET_EMPTY, SET_EMPTY, 2, 3));
        assertFalse("First set is empty", AbstractFeed.wordsEqual(SET_EMPTY, SET_1, 2, 3));
        assertFalse("Second set is empty", AbstractFeed.wordsEqual(SET_1, SET_EMPTY, 2, 3));
    }

    /**
     * Tests comparing the sets with not enough words.
     */
    public void testWordsEqual_NotEnough()
    {
        assertFalse("Not enough words in the first set", AbstractFeed.wordsEqual(SET_1, SET_2, 2, 4));
    }

    /**
     * Tests comparing the sets with not equal words.
     */
    public void testWordsEqual_NotEqual()
    {
        assertFalse("Sets are not equal", AbstractFeed.wordsEqual(SET_1, SET_2, 2, 3));
    }

    /**
     * Tests comparing the sets with equal words.
     */
    public void testWordsEqual_Equal()
    {
        assertTrue("Sets are equal", AbstractFeed.wordsEqual(SET_1, SET_1, 0, 3));
        assertTrue("Sets are equal", AbstractFeed.wordsEqual(SET_1, SET_1_1, 0, 3));
    }

    // ---------------------------------------------------------------------------------------------
    // Helper classes and methods
    // ---------------------------------------------------------------------------------------------

    /**
     * Dummy feed with single article.
     */
    private static class DummyFeed extends AbstractFeed
    {
        private IArticle art;

        /**
         * Creates dummy.
         */
        public DummyFeed()
        {
            art = new StandardArticle("");
        }

        /**
         * Returns title of feed.
         *
         * @return title.
         */
        public String getTitle()
        {
            return "Dummy";
        }

        /**
         * Returns number of articles in feed.
         *
         * @return number of articles.
         */
        public int getArticlesCount()
        {
            return 1;
        }

        /**
         * Returns number of articles this feed owns.
         *
         * @return number of articles.
         */
        public int getOwnArticlesCount()
        {
            return getArticlesCount();
        }

        /**
         * Returns the Article at the specified index.
         *
         * @param index index of article in feed.
         *
         * @return article object.
         */
        public IArticle getArticleAt(int index)
        {
            return art;
        }

        /**
         * Returns the list of all articles which are currently in the feed.
         *
         * @return all articles at this moment.
         */
        public IArticle[] getArticles()
        {
            return new IArticle[] { art };
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
