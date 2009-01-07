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
// $Id: TestHsqlArticlesPM.java,v 1.13 2008/02/27 08:35:58 spyromus Exp $
//

package com.salas.bb.persistence.backend;

import com.salas.bb.domain.*;
import com.salas.bb.persistence.PersistenceException;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * This suite contains tests for <code>HsqlArticlesPM</code> unit.
 * It covers:
 * <ul>
 *  <li>inserting articles.</li>
 *  <li>updating articles.</li>
 *  <li>removing articles.</li>
 * </ul>
 */
public class TestHsqlArticlesPM extends AbstractHsqlPersistenceTestCase
{
    private HsqlArticlesPM manager;
    private DirectFeed feed;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        // Init most modern database
        initManager("/resources");
        manager = new HsqlArticlesPM(pm);

        // Prepare feed for use in tests
        StandardGuide guide = new StandardGuide();
        guide.setTitle("Test Guide");

        feed = new DirectFeed();
        guide.add(feed);

        pm.insertGuide(guide, 0);
    }

    /**
     * Tests adding articles.
     */
    public void testInsertArticle()
        throws MalformedURLException, SQLException, PersistenceException
    {
        StandardArticle article = new StandardArticle("D");
        article.setPinned(true);
        URL link = new URL("file://test");
        Date date = new Date();

        setSampleProperties(article, link, date);
        feed.appendArticle(article);

        // Insert article into database
        manager.insertArticle(article);
        pm.commit();
        assertTrue("ID isn't set.", article.getID() != -1L);

        checkLoadedArticleProperties(article);

        // Check article_properties table record is created
        PreparedStatement stmt = pm.getPreparedStatement("SELECT * FROM ARTICLE_PROPERTIES WHERE ARTICLEID=?");
        stmt.setLong(1, article.getID());
        ResultSet rs = stmt.executeQuery();
        assertTrue("ARTICLE_PROPERTIES record should be added with new article.", rs.next());
        assertFalse("Only one ARTICLE_PROPERTIES record per article is allowed.", rs.next());
    }

    /**
     * Tests adding minimal articles.
     */
    public void testInsertArticleMinimal()
        throws MalformedURLException, PersistenceException, SQLException
    {
        StandardArticle article = new StandardArticle("A");
        feed.appendArticle(article);

        // Insert article into database
        manager.insertArticle(article);
        pm.commit();
        assertTrue("ID isn't set.", article.getID() != -1);

        // Load the whole set back and verify that article is there and
        // all fields are set properly
        GuidesSet set = new GuidesSet();
        pm.loadGuidesSet(set);

        // We don't check for presence of guides and feeds as we will learn about
        // that instantly.
        IFeed loadedFeed = set.getGuideAt(0).getFeedAt(0);

        assertEquals("There should be the only article.", 1, loadedFeed.getArticlesCount());
        IArticle loadedArticle = loadedFeed.getArticleAt(0);

        assertNull("Wrong author.", loadedArticle.getAuthor());
        assertNull("Wrong link.", loadedArticle.getLink());
        assertNull("Wrong publication date.", loadedArticle.getPublicationDate());
        assertNull("Wrong subject.", loadedArticle.getSubject());
        assertNull("Wrong title.", loadedArticle.getTitle());
        assertNotNull("Wrong text.", loadedArticle.getHtmlText());
        assertFalse("Wrong read state.", loadedArticle.isRead());
        assertFalse("Wrong pinned state.", loadedArticle.isPinned());
    }

    /**
     * Tests handling of bad input when adding articles.
     */
    public void testInsertArticleFailure()
        throws PersistenceException, SQLException
    {
        // Unspecified article
        try
        {
            manager.insertArticle(null);
            fail("Article should be always specified. NPE is expected.");
        } catch (NullPointerException e)
        {
            // Expected
        }

        // Article assigned to transient feed.
        DirectFeed transientFeed = new DirectFeed();
        StandardArticle article = new StandardArticle("");
        transientFeed.appendArticle(article);

        try
        {
            manager.insertArticle(article);
            fail("Article is assigned to transient feed. ISE is expected.");
        } catch (IllegalStateException e)
        {
            // Expected
        }

        // Save article to test the case when we are inserting it twice
        article = new StandardArticle("");
        feed.appendArticle(article);
        manager.insertArticle(article);

        // Article is already in database
        try
        {
            manager.insertArticle(article);
            fail("Article is already in database. ISE is expected.");
        } catch (IllegalStateException e)
        {
            // Expected
        }

        // Remove everything from the feed
        feed.setPurgeLimit(0);
        feed.clean();
        feed.setPurgeLimit(-1);

        // Article is of unsupported type
        IArticle unsupportedTypeArticle = new UnsupportedTypeArticle();
        feed.appendArticle(unsupportedTypeArticle);
        try
        {
            manager.insertArticle(unsupportedTypeArticle);
            fail("Article has unsupported type. PM knows nothing about it. IAE is expected.");
        } catch (IllegalArgumentException e)
        {
            // Expected
        }

        // Article isn't assigned to feed
        StandardArticle unassignedArticle = new StandardArticle("");
        try
        {
            manager.insertArticle(unassignedArticle);
            fail("Article isn't assigned to the feed. ISE is expected.");
        } catch (IllegalStateException e)
        {
            // Expected
        }
    }

    /**
     * Tests updating articles.
     */
    public void testUpdateArticle()
        throws PersistenceException, MalformedURLException, SQLException
    {
        StandardArticle article = addSampleArticle();

        // Update all fields
        URL link = new URL("file://test");
        Date date = new Date();
        setSampleProperties(article, link, date);

        // Put the updates in database
        manager.updateArticle(article);

        checkLoadedArticleProperties(article);
    }

    /**
     * Adds a sample article.
     *
     * @return article.
     *
     * @throws SQLException if database fails.
     */
    private StandardArticle addSampleArticle()
        throws SQLException
    {
        // Add sample article
        StandardArticle article = new StandardArticle("D");
        feed.appendArticle(article);
        manager.insertArticle(article);
        pm.commit();
        return article;
    }

    /**
     * Tests handling of bad input when updating articles.
     */
    public void testUpdateArticleFailure()
        throws PersistenceException, SQLException
    {
        // Unspecified article
        try
        {
            manager.updateArticle(null);
            fail("Article should be always specified. NPE is expected.");
        } catch (NullPointerException e)
        {
            // Expected
        }

        // Article isn't in database
        StandardArticle article = new StandardArticle("");
        disableLogging(HsqlArticlesPM.class);
        try
        {
            manager.updateArticle(article);
        } catch (IllegalStateException e)
        {
            fail("Article is not in database, but ISE is replaced with log message.");
        } finally
        {
            enableLogging(HsqlArticlesPM.class);
        }

        // Article is of unsupported type
        IArticle unsupportedTypeArticle = new UnsupportedTypeArticle();
        unsupportedTypeArticle.setID(1);
        feed.appendArticle(unsupportedTypeArticle);
        try
        {
            manager.updateArticle(unsupportedTypeArticle);
            fail("Article has unsupported type. PM knows nothing about it. IAE is expected.");
        } catch (IllegalArgumentException e)
        {
            // Expected
        }
    }

    /** Tests updating the properties. */
    public void testUpdateArticleProperties()
        throws SQLException, PersistenceException
    {
        StandardArticle article = addSampleArticle();

        // Update article sentiment counts
        int positive = 2;
        int negative = 3;
        article.setSentimentsCounts(positive, negative);
        manager.updateArticleProperties(article);

        IArticle loadedArticle = getTheOnlyArticle();
        assertEquals(positive, loadedArticle.getPositiveSentimentsCount());
        assertEquals(negative, loadedArticle.getNegativeSentimentsCount());
    }

    /**
     * Tests removing articles.
     */
    public void testRemoveArticle()
        throws PersistenceException, SQLException
    {
        // Add sample article
        StandardArticle article = new StandardArticle("");
        feed.appendArticle(article);
        manager.insertArticle(article);
        pm.commit();

        // Remove the article
        manager.removeArticle(article);
        pm.commit();

        assertEquals("ID of removed article should be turned -1.", -1, article.getID());

        // Load the whole set back and verify that article is there and
        // all fields are set properly
        GuidesSet set = new GuidesSet();
        pm.loadGuidesSet(set);

        // We don't check for presence of guides and feeds as we will learn about
        // that instantly.
        IFeed loadedFeed = set.getGuideAt(0).getFeedAt(0);
        assertEquals("Feed should contain 0 articles.", 0, loadedFeed.getArticlesCount());
    }

    /**
     * Tests handling of bad input when removing articles.
     */
    public void testRemoveArticleFailure()
        throws PersistenceException, SQLException
    {
        try
        {
            manager.removeArticle(null);
            fail("Article should be specified. NPE is expected.");
        } catch (NullPointerException e)
        {
            // Expected
        }

        StandardArticle transientArticle = new StandardArticle("");
        disableLogging(HsqlArticlesPM.class);
        try
        {
            manager.removeArticle(transientArticle);
        } catch (IllegalStateException e)
        {
            fail("Article is transient and cannot be removed, but ISE is replaced with log message.");
        } finally
        {
            enableLogging(HsqlArticlesPM.class);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------

    /**
     * Sets sample properties to the article.
     *
     * @param aArticle  article.
     * @param aLink     link.
     * @param aDate     publication date.
     */
    private static void setSampleProperties(StandardArticle aArticle, URL aLink, Date aDate)
    {
        aArticle.setAuthor("A");
        aArticle.setLink(aLink);
        aArticle.setPublicationDate(aDate);
        aArticle.setRead(true);
        aArticle.setPinned(true);
        aArticle.setSubject("B");
        aArticle.setTitle("C");
        aArticle.computeSimpleMatchKey();
    }


    /**
     * Makes sure that all of the articles properties set correctly.
     *
     * @param article article to check against.
     *
     * @throws PersistenceException if persistence fails.
     */
    private void checkLoadedArticleProperties(IArticle article)
        throws PersistenceException
    {
        IArticle loadedArticle = getTheOnlyArticle();
        assertEquals(article, loadedArticle);
        assertEquals(article.isPinned(), loadedArticle.isPinned());
        assertEquals(article.getPlainText(), loadedArticle.getPlainText());
    }

    /**
     * Returns the only article from the only feed and the only guide.
     *
     * @return article.
     *
     * @throws PersistenceException if DB fails.
     */
    private IArticle getTheOnlyArticle()
        throws PersistenceException
    {
        // Load the whole set back and verify that article is there and
        // all fields are set properly
        GuidesSet set = new GuidesSet();
        pm.loadGuidesSet(set);

        // We don't check for presence of guides and feeds as we will learn about
        // that instantly.
        IFeed loadedFeed = set.getGuideAt(0).getFeedAt(0);

        assertEquals("There should be the only article.", 1, loadedFeed.getArticlesCount());
        IArticle loadedArticle = loadedFeed.getArticleAt(0);
        return loadedArticle;
    }

    /**
     * This article class is used to simulate article of unsupported type. The
     * persistence manager knows nothing about how to store it in database and
     * this is exactly what we need for failure tests.
     */
    private static class UnsupportedTypeArticle extends AbstractArticle
    {
        /**
         * Returns HTML version of article text.
         *
         * @return HTML version of text.
         */
        public String getHtmlText()
        {
            return null;
        }
    }
}
