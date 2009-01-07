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
// $Id: SearchEngine.java,v 1.23 2008/12/25 07:50:45 spyromus Exp $
//

package com.salas.bb.search;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.ThreadedExecutor;
import com.salas.bb.domain.*;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.swinghtml.TextProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Search engine which takes the domain and search string as input and returns
 * the list of matching things.
 */
public class SearchEngine
{
    /** Search engine thread. */
    private static final Executor executor;

    /** Result object tis search engine communicates with the outer world through. */
    private final SearchResult result;

    /** The set of guides this engine runs over. */
    private GuidesSet guidesSet;

    static
    {
        executor = new ThreadedExecutor();
    }

    /**
     * Creates search engine.
     */
    public SearchEngine()
    {
        result = new SearchResult();
    }

    /**
     * Registers the set to supervise.
     *
     * @param aGuidesSet set.
     */
    public void setGuidesSet(GuidesSet aGuidesSet)
    {
        guidesSet = aGuidesSet;
    }

    /**
     * Returns the result of a search which will be updated by this search engine.
     * It can be a good idea to add listener to watch the changes right after you
     * get the object.
     *
     * @return result object.
     */
    public ISearchResult getResult()
    {
        return result;
    }

    /**
     * Sets the search text and starts search immediately, while updating the
     * results of the search asynchronously.
     *
     * @param text new text.
     * @param pinnedArticlesOnly <code>TRUE</code> to match only pinned articles.
     */
    public void setSearchText(final String text, final boolean pinnedArticlesOnly)
    {
        // EDT !!!

        result.removeAll();
        if (guidesSet != null && StringUtils.isNotEmpty(text))
        {
            Runnable task = new Runnable()
            {
                public void run()
                {
                    doSearch(text.trim(), pinnedArticlesOnly);
                }
            };

            try
            {
                executor.execute(task);
            } catch (InterruptedException e)
            {
                task.run();
            }
        } else finished();
    }

    /**
     * Performs search for a given text and updates the result.
     *
     * @param text text.
     * @param pinnedArticlesOnly <code>TRUE</code> to match only pinned articles.
     */
    private void doSearch(String text, boolean pinnedArticlesOnly)
    {
        try
        {
            SearchMatcher matcher = createMatcher(text, pinnedArticlesOnly);

            // Check guides
            int guidesCnt = guidesSet.getGuidesCount();
            for (int g = 0; g < guidesCnt; g++)
            {
                IGuide guide = guidesSet.getGuideAt(g);

                if (matcher.matches(guide)) addItem(guide);
            }

            // Check feeds
            FeedsList feedsList = guidesSet.getFeedsList();
            for (int f = 0; f < feedsList.getFeedsCount(); f++)
            {
                IFeed feed = feedsList.getFeedAt(f);
                if (matcher.matches(feed)) addItem(feed);
            }

            // Check articles
            for (int f = 0; f < feedsList.getFeedsCount(); f++)
            {
                IFeed feed = feedsList.getFeedAt(f);
                if (feed instanceof DataFeed)
                {
                    // If this feed is data feed (contains articles)
                    IArticle[] articles = feed.getArticles();

                    for (IArticle article : articles)
                    {
                        if (matcher.matches(article)) addItem(article);
                    }
                }
            }
        } finally
        {
            finished();
        }
    }

    /**
     * Reports finish of search.
     */
    private void finished()
    {
        result.fireFinished();
    }

    /**
     * Adds item to the cache and submits if the last submission was more
     * than <code>NEW_ITEMS_DELAY</code> milis ago.
     *
     * @param item item.
     */
    private void addItem(final Object item)
    {
        result.addItem(item);
    }

    // ---------------------------------------------------------------------------------------------
    // Matchers
    // ---------------------------------------------------------------------------------------------

    /**
     * Creates matcher for pattern.
     *
     * @param pattern pattern.
     * @param aPinnedArticlesOnly <code>TRUE</code> to match only pinned articles.
     *
     * @return matcher.
     */
    static SearchMatcher createMatcher(String pattern, boolean aPinnedArticlesOnly)
    {
        SearchMatcher matcher;

        if (isComplexSeachPattern(pattern))
        {
            // regex matcher is required
            matcher = new RegexMatcher(pattern, aPinnedArticlesOnly);
        } else
        {
            matcher = new SimpleMatcher(pattern, aPinnedArticlesOnly);
        }

        return matcher;
    }

    /**
     * Returns <code>TRUE</code> if the search pattern has complex format.
     *
     * @param pattern pattern text.
     *
     * @return <code>TRUE</code> if the search pattern has complex format.
     */
    public static boolean isComplexSeachPattern(String pattern)
    {
        return pattern.indexOf('"') != -1 ||
            pattern.indexOf('*') != -1 ||
            pattern.indexOf('+') != -1;
    }

    /**
     * Matcher interface for all types of matchers.
     */
    static abstract class SearchMatcher
    {
        private final boolean pinnedArticlesOnly;

        /**
         * Creates the matcher.
         *
         * @param pinnedArticlesOnly <code>TRUE</code> if pinned articles only match.
         */
        protected SearchMatcher(boolean pinnedArticlesOnly)
        {
            this.pinnedArticlesOnly = pinnedArticlesOnly;
        }

        /**
         * Returns <code>TRUE</code> if a guide matches.
         *
         * @param guide guide.
         *
         * @return result.
         */
        public boolean matches(IGuide guide)
        {
            return !pinnedArticlesOnly && matches(guide.getTitle().toLowerCase());
        }

        /**
         * Returns <code>TRUE</code> if a feed matches.
         *
         * @param feed feed.
         *
         * @return result.
         */
        public boolean matches(IFeed feed)
        {
            return !pinnedArticlesOnly && matches(feed.getTitle().toLowerCase());
        }

        /**
         * Returns <code>TRUE</code> if an article matches.
         *
         * @param article guide.
         *
         * @return result.
         */
        public boolean matches(IArticle article)
        {
            if (pinnedArticlesOnly && !article.isPinned()) return false;

            String title = article.getTitle().toLowerCase();
            boolean matches = matches(title);

            if (!matches)
            {
                matches = matches(TextProcessor.toPlainText(article.getPlainText()));
            }

            return matches;
        }

        /**
         * Returns <code>TRUE</code> if the text matches.
         *
         * @param text text.
         *
         * @return result.
         */
        protected abstract boolean matches(String text);
    }

    /**
     * Simply checks if the pattern is in the text.
     */
    private static class SimpleMatcher extends SearchMatcher
    {
        private final String pattern;

        /**
         * Creates matcher.
         *
         * @param aPattern pattern.
         * @param aPinnedArticlesOnly <code>TRUE</code> to match only pinned articles.
         */
        public SimpleMatcher(String aPattern, boolean aPinnedArticlesOnly)
        {
            super(aPinnedArticlesOnly);
            pattern = aPattern;
        }

        /**
         * Returns <code>TRUE</code> if the text matches.
         *
         * @param text text.
         *
         * @return result.
         */
        protected boolean matches(String text)
        {
            return text.indexOf(pattern) != -1;
        }
    }

    /**
     * Checks if the pattern is in the text.
     */
    private static class RegexMatcher extends SearchMatcher
    {
        private final Pattern pattern;

        /**
         * Creates matcher for a given pattern.
         *
         * @param aPattern pattern.
         * @param aPinnedArticlesOnly <code>TRUE</code> to match only pinned articles.
         */
        public RegexMatcher(String aPattern, boolean aPinnedArticlesOnly)
        {
            super(aPinnedArticlesOnly);
            String regex = StringUtils.keywordsToPattern(aPattern.toLowerCase().trim());
            pattern = regex == null ? null : Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        }

        /**
         * Returns <code>TRUE</code> if the text matches.
         *
         * @param text text.
         *
         * @return result.
         */
        protected boolean matches(String text)
        {
            return pattern != null && pattern.matcher(text).find();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Result interaction
    // ---------------------------------------------------------------------------------------------

    /**
     * Search result.
     */
    private static class SearchResult implements ISearchResult
    {
        private final List<ResultItem> items = new ArrayList<ResultItem>();
        private final List<ISearchResultListener> listeners = new ArrayList<ISearchResultListener>();

        /**
         * Returns the number of result items.
         *
         * @return items.
         */
        public int getItemsCount()
        {
            return items.size();
        }

        /**
         * Returns the result item at a given index.
         *
         * @param index index.
         *
         * @return item.
         */
        public ResultItem getItem(int index)
        {
            return items.get(index);
        }

        /**
         * Subscribes the listener to changes notifications.
         *
         * @param l listener.
         */
        public void addChangesListener(ISearchResultListener l)
        {
            if (!listeners.contains(l)) listeners.add(l);
        }

        /**
         * Unsubscribes the listener from changes notifications.
         *
         * @param l listener.
         */
        public void removeChangesListener(ISearchResultListener l)
        {
            listeners.remove(l);
        }

        /**
         * Removes all items.
         */
        public void removeAll()
        {
            items.clear();
            fireItemsRemoved();
        }

        /**
         * Adds new item if it's not there yet.
         *
         * @param item item.
         */
        public void addItem(Object item)
        {
            ResultItem it = new ResultItem(item);
            boolean present = items.contains(it);

            if (!present)
            {
                items.add(it);
                fireItemAdded(it);
            }
        }

        /**
         * Fires item addition event.
         *
         * @param item item.
         */
        private void fireItemAdded(ResultItem item)
        {
            int index = items.indexOf(item);
            for (ISearchResultListener listener : listeners)
            {
                listener.itemAdded(this, item, index);
            }
        }

        /**
         * Fires items removal event.
         */
        private void fireItemsRemoved()
        {
            for (ISearchResultListener listener : listeners)
            {
                listener.itemsRemoved(this);
            }
        }

        /**
         * Fires search finish event.
         */
        public void fireFinished()
        {
            for (ISearchResultListener listener : listeners)
            {
                listener.finished(this);
            }
        }
    }
}
