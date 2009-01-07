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
// $Id: SearchFeed.java,v 1.55 2007/11/09 16:24:19 spyromus Exp $
//

package com.salas.bb.domain;

import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.domain.utils.ArticleDateComparator;
import com.salas.bb.utils.i18n.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Search feed is a special kind of feeds which is using predefined query to to find
 * the articles of interest.
 */
public class SearchFeed extends AbstractFeed
{
    private static final int MAXIMUM_ARTICLES_LIMIT = Integer.MAX_VALUE;
    private static final ArticleDateComparator articleDateComparator;

    /** Articles limit property. */
    public static final String PROP_ARTICLES_LIMIT = "articlesLimit";
    /** Name of query property. */
    public static final String PROP_QUERY = "query";
    public static final String PROP_DEDUP_ENABLED = "dedupEnabled";
    public static final String PROP_DEDUP_FROM = "dedupFrom";
    public static final String PROP_DEDUP_TO = "dedupTo";
    /** Fake property which is fired if the dedup properties were updated. */
    public static final String PROP_DEDUP_UPDATED = "dedupUpdated";

    private final List<IArticle>  articles;

    private String      baseTitle;
    private int         articlesLimit;
    private Query       query;
    /** TRUE when deduplication functionality is enabled. */
    private boolean     dedupEnabled;
    /** The first word index to look for the match. */
    private int         dedupFrom;
    /** The last word index to look for the match. */
    private int         dedupTo;

    private SearchFeed.ArticlesListener articlesListener;

    static
    {
        articleDateComparator = new NoDupArticleDateComparator();
    }

    /**
     * Creates search feed.
     */
    public SearchFeed()
    {
        // The LinkedList was replaced by the ArrayList because of EDT locks and
        // performance issues
        articles = new ArrayList<IArticle>();
        articlesListener = new ArticlesListener();

        super.setCustomViewModeEnabled(true);

        dedupEnabled = false;
        dedupFrom = 0;
        dedupTo = 0;
    }

    /**
     * Returns the Article at the specified index.
     *
     * @param index index of article in channel.
     *
     * @return article object.
     */
    public synchronized IArticle getArticleAt(int index)
    {
        return articles.get(index);
    }

    /**
     * Returns number of articles in channel.
     *
     * @return number of articles.
     */
    public synchronized int getArticlesCount()
    {
        return Math.min(articles.size(), articlesLimit);
    }

    /**
     * Returns number of articles this feed owns.
     *
     * @return number of articles.
     */
    public int getOwnArticlesCount()
    {
        // Search feed has no own articles
        return 0;
    }

    /**
     * Returns the list of all articles which are currently in the feed.
     *
     * @return all articles at this moment.
     */
    public IArticle[] getArticles()
    {
        int visibleArticlesCount;
        IArticle[] fullList;

        synchronized (this)
        {
            visibleArticlesCount = getArticlesCount();
            fullList = articles.toArray(new IArticle[articles.size()]);
        }

        // Leave only visible articles in the list.
        IArticle[] cropped = new IArticle[visibleArticlesCount];
        System.arraycopy(fullList, 0, cropped, 0, visibleArticlesCount);

        return cropped;
    }

    /**
     * Returns title of feed.
     *
     * @return title.
     */
    public String getTitle()
    {
        return baseTitle;
    }

    /**
     * Gets the base title.
     *
     * @return base title.
     */
    public String getBaseTitle()
    {
        return baseTitle;
    }

    /**
     * Sets the title of feed.
     *
     * @param aTitle feed title.
     */
    public void setBaseTitle(String aTitle)
    {
        String oldTitle = getTitle();
        baseTitle = aTitle;
        firePropertyChanged(IFeed.PROP_TITLE, oldTitle, getTitle());
    }

    /**
     * Returns articles limit.
     *
     * @return articles limit.
     */
    public int getArticlesLimit()
    {
        return articlesLimit;
    }

    /**
     * Sets the limit of articles in this feed. If the limit is bigger than
     * <code>MAXIMUM_ARTICLES_LIMIT</code> then it's assigned to it.
     *
     * @param anArticlesLimit limit.
     *
     * @throws IllegalArgumentException if limit is negative.
     *
     * @see #MAXIMUM_ARTICLES_LIMIT
     */
    public synchronized void setArticlesLimit(int anArticlesLimit)
    {
        if (anArticlesLimit < 0)
            throw new IllegalArgumentException(Strings.error("limit.should.be.non.negative"));
        if (anArticlesLimit > MAXIMUM_ARTICLES_LIMIT) anArticlesLimit = MAXIMUM_ARTICLES_LIMIT;

        int oldLimit = articlesLimit;
        articlesLimit = anArticlesLimit;

        if (isVisible(oldLimit))
        {
            int end = Math.min(articles.size(), articlesLimit);
            for (int i = oldLimit; i < end; i++)
            {
                fireArticleAdded(getArticleAt(i));
            }
        } else
        {
            int size = articles.size();
            for (int i = size - 1; i >= articlesLimit; i--)
            {
                fireArticleRemoved(getArticleAt(i));
            }
        }

        firePropertyChanged(PROP_ARTICLES_LIMIT, new Integer(oldLimit), new Integer(articlesLimit));
    }

    /**
     * Returns query.
     *
     * @return query.
     */
    public Query getQuery()
    {
        return query;
    }

    /**
     * Sets the new query.
     *
     * @param aQuery query to set.
     */
    public void setQuery(Query aQuery)
    {
        if (query == null || !query.equals(aQuery))
        {
            Query oldQuery = query;
            query = aQuery;

            firePropertyChanged(PROP_QUERY, oldQuery, query, true, false);
        }
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
        return "SF" + (query == null ? null : query.serializeToString());
    }

    /**
     * Checks the article agains the query and adds it if the query returned success.
     * The article may be not added if its insertion index is going to be out of limit.
     *
     * @param anArticle article to check.
     *
     * @throws NullPointerException if the article isn't specified.
     */
    public synchronized void addArticleIfMatching(IArticle anArticle)
    {
        if (anArticle == null) throw new NullPointerException(Strings.error("unspecified.article"));

        if (query != null && query.match(anArticle) && !isDuplicate(anArticle))
        {
            // Collections.binarySearch can be used to find the index of potential
            // insertion of new item in the collection basing on its natural order
            // or order, reported by some comparator.
            int articleIndex = Collections.binarySearch(articles, anArticle, articleDateComparator);
            if (articleIndex < 0)
            {
                int insertionIndex = -articleIndex - 1;
                addArticle(anArticle, insertionIndex);
            }
        }
    }

    /**
     * Checks if an article is duplicate of some other article.
     *
     * @param anArticle article to check.
     *
     * @return <code>TRUE</code> if it is.
     */
    private boolean isDuplicate(IArticle anArticle)
    {
        return dedupEnabled && isDuplicate(anArticle, dedupFrom, dedupTo, articles);
    }

    private void addArticle(IArticle anArticle, int insertionIndex)
    {
        int unread = getUnreadArticlesCount();

        articles.add(insertionIndex, anArticle);
        anArticle.addListener(articlesListener);

        if (isVisible(insertionIndex))
        {
            fireArticleAdded(anArticle);

            if (articles.size() > articlesLimit) removeArticle(getArticleAt(articlesLimit));
        }

        int newUnread = getUnreadArticlesCount();
        if (unread != newUnread) firePropertyChanged(PROP_UNREAD_ARTICLES_COUNT,
            new Integer(unread), new Integer(getUnreadArticlesCount()));
    }

    private boolean isVisible(int index)
    {
        return index < articlesLimit;
    }

    /**
     * Removes article from the feed.
     *
     * @param anArticle article.
     */
    public synchronized void removeArticle(IArticle anArticle)
    {
        int unread = getUnreadArticlesCount();

        if (articles.remove(anArticle))
        {
            anArticle.removeListener(articlesListener);
            fireArticleRemoved(anArticle);

            int newUnread = getUnreadArticlesCount();
            if (unread != newUnread) firePropertyChanged(PROP_UNREAD_ARTICLES_COUNT,
                new Integer(unread), new Integer(newUnread));
        }
    }

    /**
     * Reviews all articles from given feed and removes if they are no longer matching.
     *
     * @param feed feed to which article should belong to be reviewed or <code>NULL</code> for any.
     */
    public synchronized void reviewArticlesTakenFrom(IFeed feed)
    {
        int count = articles.size();
        for (int i = 0; i < count; i++)
        {
            int index = count - i - 1;
            IArticle article = getArticleAt(index);
            if (feed == null || article.getFeed() == feed) reviewArticle(article);
        }
    }

    private void reviewArticle(IArticle aArticle)
    {
        if (!query.match(aArticle) || isDuplicate(aArticle))
        {
            int index = articles.indexOf(aArticle);
            removeArticle(aArticle);

// This piece of code makes another article from the matching appear in the list
// instead of deleted one.
            if (isVisible(index) && articles.size() >= articlesLimit)
            {
                fireArticleAdded(getArticleAt(articlesLimit - 1));
            }
        }
    }

    /**
     * Removes all listener registrations.
     */
    public void unregisterListeners()
    {
        for (IArticle article : articles) article.removeListener(articlesListener);
    }

    /**
     * Listens for changes in all articles, this feed is referring to.
     */
    private class ArticlesListener implements IArticleListener
    {
        /**
         * Invoked when the property of the article has been changed.
         *
         * @param article  article.
         * @param property property of the article.
         * @param oldValue old property value.
         * @param newValue new property value.
         */
        public void propertyChanged(IArticle article, String property, Object oldValue,
                                    Object newValue)
        {
            if (!AbstractArticle.PROP_ID.equals(property))
            {
                synchronized (SearchFeed.this)
                {
                    reviewArticle(article);
                }
            }

            if (IArticle.PROP_READ.equals(property))
            {
                boolean readNow = (Boolean)newValue;
                int unread = getUnreadArticlesCount();
                firePropertyChanged(PROP_UNREAD_ARTICLES_COUNT,
                    new Integer(readNow ? unread + 1 : unread - 1),
                    new Integer(unread));
            }
        }
    }

    /**
     * This comparator takes the dates order in account, but if the dates are the same
     * it checks the hash codes.
     */
    private static class NoDupArticleDateComparator extends ArticleDateComparator
    {
        public NoDupArticleDateComparator()
        {
            super(true);
        }

        @Override
        public int compare(IArticle o1, IArticle o2)
        {
            int result = super.compare(o1, o2);
            if (result == 0)
            {
                result = new Integer(o1.hashCode()).compareTo(o2.hashCode());
            }

            return result;
        }
    }


    // ------------------------------------------------------------------------
    // Duplicates Checking
    // ------------------------------------------------------------------------

    /**
     * Returns <code>TRUE</code> if remove duplicates is enabled.
     *
     * @return <code>TRUE</code> if remove duplicates is enabled.
     */
    public boolean isDedupEnabled()
    {
        return dedupEnabled;
    }

    /**
     * Sets remove duplicates flag.
     *
     * @param flag <code>TRUE</code> if remove duplicates is enabled.
     */
    public void setDedupEnabled(boolean flag)
    {
        boolean old = dedupEnabled;
        if (old == flag) return;
        dedupEnabled = flag;

        firePropertyChanged(PROP_DEDUP_ENABLED, old, flag, true, false);
    }

    /**
     * Returns the first word to look for duplicates.
     *
     * @return word number.
     */
    public int getDedupFrom()
    {
        return dedupFrom;
    }

    /**
     * Sets the first word to look for duplicates.
     *
     * @param word number
     */
    public void setDedupFrom(int word)
    {
        int old = dedupFrom;
        dedupFrom = word;

        firePropertyChanged(PROP_DEDUP_FROM, old, word, true, false);
    }

    /**
     * Returns the last word to look for duplicates.
     *
     * @return word number.
     */
    public int getDedupTo()
    {
        return dedupTo;
    }

    /**
     * Sets the last word to look for duplicates.
     *
     * @param word number
     */
    public void setDedupTo(int word)
    {
        int old = dedupTo;
        dedupTo = word;

        firePropertyChanged(PROP_DEDUP_TO, old, word, true, false);
    }

    /**
     * Updates all dedup properties at once and fires an additional event
     * if any of them have changed.
     *
     * @param enabled   <code>TRUE</code> to enable.
     * @param from      the index of the first word.
     * @param to        the index of the last word.
     */
    public void setDedupProperties(boolean enabled, int from, int to)
    {
        setDedupProperties(enabled, from, to, true);
    }

    /**
     * Updates all dedup properties at once and fires an additional event
     * if any of them have changed.
     *
     * @param enabled   <code>TRUE</code> to enable.
     * @param from      the index of the first word.
     * @param to        the index of the last word.
     * @param fireEvent <code>TRUE</code> to fire deduplication event. We may not need it
     *                  during the batch update, to save a trouble of rescanning feeds
     *                  more than once.
     */
    public void setDedupProperties(boolean enabled, int from, int to, boolean fireEvent)
    {
        boolean oldEnabled = isDedupEnabled();
        int oldFrom = getDedupFrom();
        int oldTo = getDedupTo();

        setDedupEnabled(enabled);
        setDedupFrom(from);
        setDedupTo(to);

        if (fireEvent &&
            (oldEnabled != isDedupEnabled() ||
            oldFrom != getDedupFrom() ||
            oldTo != getDedupTo()))
        {
            firePropertyChanged(PROP_DEDUP_UPDATED, false, true);
        }
    }
}
