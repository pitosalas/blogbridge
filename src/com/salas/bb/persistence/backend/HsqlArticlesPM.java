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
// $Id: HsqlArticlesPM.java,v 1.19 2008/02/27 08:35:59 spyromus Exp $
//

package com.salas.bb.persistence.backend;

import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.domain.StandardArticle;
import com.salas.bb.utils.i18n.Strings;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HSQL articles persistence manager.
 */
final class HsqlArticlesPM
{
    private static final Logger LOG = Logger.getLogger(HsqlArticlesPM.class.getName());

    /** Persistence manager context. */
    private final HsqlPersistenceManager context;

    /** Unsupported article type. */
    private static final String MSG_UNSUPPORTED_TYPE = Strings.error("db.unsupported.article.type");
    /** Article is not in database yet. */
    private static final String MSG_NOT_IN_DB = Strings.error("db.article.is.not.in.database");
    /** Article parameter is NULL. */
    private static final String MSG_SHOULD_BE_SPECIFIED = Strings.error("unspecified.article");
    /** Article is already in database. */
    private static final String MSG_ALREADY_IN_DB = Strings.error("db.article.is.already.in.database");
    /** Article is not assigned to some feed. */
    private static final String MSG_NO_FEED = Strings.error("db.article.is.not.assigned.to.any.feed");
    /** The feed article is assigned to is transient. */
    private static final String MSG_TRANSIENT_FEED = Strings.error("db.article.is.assigned.to.transient.feed");

    /**
     * Created manager.
     *
     * @param aContext context to communicate to.
     */
    public HsqlArticlesPM(HsqlPersistenceManager aContext)
    {
        context = aContext;
    }

    /**
     * Inserts the article in database.
     *
     * @param article article to insert.
     *
     * @throws NullPointerException if article isn't specified.
     * @throws IllegalStateException if article is already in database, or
     *                               article isn't assigned to feed, or
     *                               feed this article is assigned to is transient.
     * @throws IllegalArgumentException if article is of unsupported type.
     * @throws SQLException if database operation fails.
     */
    public void insertArticle(IArticle article)
        throws SQLException
    {
        if (article == null) throw new NullPointerException(MSG_SHOULD_BE_SPECIFIED);
        if (article.getID() != -1L) throw new IllegalStateException(MSG_ALREADY_IN_DB);

        IFeed feed = article.getFeed();
        if (feed == null) throw new IllegalStateException(MSG_NO_FEED);
        if (feed.getID() == -1L) throw new IllegalStateException(MSG_TRANSIENT_FEED);
        if (!(article instanceof StandardArticle))
            throw new IllegalArgumentException(MSG_UNSUPPORTED_TYPE);

        StandardArticle standardArticle = (StandardArticle)article;
        PreparedStatement stmt = context.getPreparedStatement(
            "INSERT INTO ARTICLES (AUTHOR, TEXT, PLAINTEXT, SIMPLEMATCHKEY, PUBLICATIONDATE, TITLE, " +
                "SUBJECT, READ, PINNED, LINK, FEEDID) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        try
        {
            stmt.setString(1, standardArticle.getAuthor());
            stmt.setString(2, standardArticle.getText());
            stmt.setString(3, standardArticle.getPlainText());
            stmt.setString(4, standardArticle.getSimpleMatchKey());
            Date publicationDate = standardArticle.getPublicationDate();
            stmt.setLong(5, publicationDate == null ? -1L : publicationDate.getTime());
            stmt.setString(6, standardArticle.getTitle());
            stmt.setString(7, standardArticle.getSubject());
            stmt.setBoolean(8, standardArticle.isRead());
            stmt.setBoolean(9, standardArticle.isPinned());
            URL link = standardArticle.getLink();
            stmt.setString(10, link == null ? null : link.toString());
            stmt.setLong(11, feed.getID());
            stmt.executeUpdate();

            // Get ID
            long id = context.getInsertedID();
            article.setID(id);

            // Add a properties record
            stmt = context.getPreparedStatement(
                "INSERT INTO ARTICLE_PROPERTIES (ARTICLEID, POSITIVE_SENTIMENTS, NEGATIVE_SENTIMENTS) " +
                "VALUES (?, ?, ?)");
            stmt.setLong(1, id);
            stmt.setInt(2, article.getPositiveSentimentsCount());
            stmt.setInt(3, article.getNegativeSentimentsCount());
            stmt.executeUpdate();
        } finally
        {
            stmt.close();
        }
    }

    /**
     * Removes article from database.
     *
     * @param article article to remove.
     *
     * @throws NullPointerException if article isn't specified.
     * @throws IllegalStateException if article is not in database.
     * @throws SQLException if database operation fails.
     */
    public void removeArticle(IArticle article)
        throws SQLException
    {
        if (article == null) throw new NullPointerException(MSG_SHOULD_BE_SPECIFIED);
        if (article.getID() == -1L)
        {
            LOG.log(Level.SEVERE, MessageFormat.format(Strings.error("0.title.1"),
                MSG_NOT_IN_DB, article.getTitle()), new Exception("Dump"));

            return;
        }

        PreparedStatement stmt = context.getPreparedStatement("DELETE FROM ARTICLES WHERE ID=?");
        try
        {
            stmt.setLong(1, article.getID());

            int rows = stmt.executeUpdate();
            if (rows == 0)
            {
                IFeed feed = article.getFeed();
                IGuide guide = null;
                if (feed != null)
                {
                    IGuide[] guides = feed.getParentGuides();
                    guide = guides.length == 0 ? null : guides[0];
                }
                String feedId = feed == null ? "no feed" : Long.toString(feed.getID());
                String guideId = guide == null ? "no guide" : Long.toString(guide.getID());

                throw new SQLException(MessageFormat.format(
                    Strings.error("db.hsql.removed.0.rows.for.articleid.0.feedid.1.guideid.2"),
                    article.getID(), feedId, guideId));
            }

            article.setID(-1L);
        } finally
        {
            stmt.close();
        }
    }

    /**
     * Updates article in database.
     *
     * @param article article to update.
     *
     * @throws NullPointerException if article isn't specified.
     * @throws IllegalArgumentException if article is of unsupported type.
     * @throws SQLException if database operation fails.
     */
    public void updateArticle(IArticle article)
        throws SQLException
    {
        if (!checkArticle(article)) return;

        // NOTE: We intentionally don't update text and plaintext fields because
        // a) they never change
        // b) there's the deadlock between LazyArticle.getPlainText() and AbstractArticle.setRead()
        //    being called from different threads
        StandardArticle standardArticle = (StandardArticle)article;
        PreparedStatement stmt = context.getPreparedStatement("UPDATE ARTICLES SET " +
            "AUTHOR=?, SIMPLEMATCHKEY=?, PUBLICATIONDATE=?, TITLE=?, SUBJECT=?, READ=?," +
            "PINNED=?, LINK=? WHERE ID=?");

        try
        {
            stmt.setString(1, standardArticle.getAuthor());
            stmt.setString(2, standardArticle.getSimpleMatchKey());
            Date publicationDate = standardArticle.getPublicationDate();
            stmt.setLong(3, publicationDate == null ? -1L : publicationDate.getTime());
            stmt.setString(4, standardArticle.getTitle());
            stmt.setString(5, standardArticle.getSubject());
            stmt.setBoolean(6, standardArticle.isRead());
            stmt.setBoolean(7, standardArticle.isPinned());
            URL link = standardArticle.getLink();
            stmt.setString(8, link == null ? null : link.toString());
            stmt.setLong(9, article.getID());

            int rows = stmt.executeUpdate();
            if (rows == 0)
            {
                IFeed feed = article.getFeed();
                IGuide[] guides = feed.getParentGuides();
                IGuide guide = guides.length == 0 ? null : guides[0];
                String feedId = feed == null ? "no feed" : Long.toString(feed.getID());
                String guideId = guide == null ? "no guide" : Long.toString(guide.getID());

                LOG.log(Level.SEVERE, MessageFormat.format(
                    Strings.error("db.hsql.updated.0.rows.for.articleid.0.feedid.1.guideid.2"),
                    article.getID(), feedId, guideId));
            }
        } finally
        {
            stmt.close();
        }
    }

    /**
     * Updates article in database.
     *
     * @param article article to update.
     *
     * @throws NullPointerException if article isn't specified.
     * @throws IllegalArgumentException if article is of unsupported type.
     * @throws SQLException if database operation fails.
     */
    public void updateArticleProperties(IArticle article)
        throws SQLException
    {
        if (!checkArticle(article)) return;

        PreparedStatement stmt = context.getPreparedStatement("UPDATE ARTICLE_PROPERTIES SET " +
            "POSITIVE_SENTIMENTS = ?, NEGATIVE_SENTIMENTS = ? " +
            "WHERE ARTICLEID = ?");
        try
        {
            stmt.setInt(1, article.getPositiveSentimentsCount());
            stmt.setInt(2, article.getNegativeSentimentsCount());
            stmt.setLong(3, article.getID());
            stmt.executeUpdate();
        } finally
        {
            stmt.close();
        }
    }

    /**
     * Checks an article for being present, being of a valid type etc.
     *
     * @param article article.
     *
     * @return TRUE if all well.
     *
     * @throws NullPointerException if article isn't specified.
     * @throws IllegalArgumentException if article is of unsupported type.
     * @throws SQLException if database operation fails.
     */
    private static boolean checkArticle(IArticle article)
    {
        if (article == null) throw new NullPointerException(MSG_SHOULD_BE_SPECIFIED);
        if (!(article instanceof StandardArticle)) throw new IllegalArgumentException(MSG_UNSUPPORTED_TYPE);
        if (article.getID() == -1L)
        {
            LOG.log(Level.SEVERE, MessageFormat.format(Strings.error("0.title.1"),
                MSG_NOT_IN_DB, article.getTitle()), new Exception("Dump"));
            return false;
        }

        return true;
    }
}
