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
// $Id: SearchFeedsManager.java,v 1.26 2008/02/28 09:58:33 spyromus Exp $
//

package com.salas.bb.core;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import com.jgoodies.uif.application.Application;
import com.salas.bb.domain.*;
import com.salas.bb.domain.events.FeedRemovedEvent;
import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.domain.utils.DomainAdapter;
import com.salas.bb.utils.concurrency.ExecutorFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.prefs.Preferences;

/**
 * Manager of all search feeds, which is doing scans for them.
 */
public class SearchFeedsManager extends DomainAdapter
{
    private static final int KEEP_ALIVE_TIME = 10000;

    private static Boolean dontUpdateAutomatically;

    private GuidesSet       guidesSet;
    private Set<SearchFeed> searchFeeds;

    private Executor executor;

    /**
     * Creates feeds manager of some guides set.
     *
     * @param aSet guides set to work with.
     */
    public SearchFeedsManager(GuidesSet aSet)
    {
        guidesSet = aSet;
        searchFeeds = new CopyOnWriteArraySet<SearchFeed>();

        executor = ExecutorFactory.createPooledExecutor("Search Feeds", 1, Thread.MIN_PRIORITY, KEEP_ALIVE_TIME);

        loadCurrentSearchFeedsInMap();
    }

    private void loadCurrentSearchFeedsInMap()
    {
        StandardGuide[] guides = guidesSet.getStandardGuides(null);
        for (StandardGuide guide : guides) collectSearchFeedsInGuide(guide);
    }

    private void collectSearchFeedsInGuide(StandardGuide aGuide)
    {
        IFeed[] feeds = aGuide.getFeeds();
        for (IFeed feed : feeds)
        {
            if (feed instanceof SearchFeed) searchFeeds.add((SearchFeed)feed);
        }
    }

    public void runAllQueries()
    {
//        List<Integer> sids = new ArrayList<Integer>();
//
//        StandardGuide[] guides = guidesSet.getStandardGuides(null);
//        for (StandardGuide guide : guides)
//        {
//            IFeed[] feeds = guide.getFeeds();
//            for (IFeed feed : feeds)
//            {
//                if (feed instanceof SearchFeed)
//                {
//                    Integer id = System.identityHashCode(feed);
//                    if (!sids.contains(id))
//                    {
//                        sids.add(id);
//                        runQuery((SearchFeed)feed);
//                    }
//                }
//            }
//        }
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        SearchFeed[] sfeeds = searchFeeds.toArray(new SearchFeed[0]);
        processingStarted(sfeeds);
        try
        {
            List<IFeed> feeds = guidesSet.getFeedsList().getFeeds();
            for (IFeed feed : feeds)
            {
                if (feed instanceof DataFeed)
                {
                    for (SearchFeed sfeed : sfeeds) scanFeed(feed, sfeed);

                    try
                    {
                        Thread.sleep(100);
                    } catch (InterruptedException e)
                    {
                        // Ignore
                    }
                }
            }
        } finally
        {
            processingFinished(sfeeds);
        }
    }

    private void processingStarted(SearchFeed[] sfeeds)
    {
        for (SearchFeed sfeed : sfeeds) sfeed.processingStarted();
    }

    private void processingFinished(SearchFeed[] sfeeds)
    {
        for (SearchFeed sfeed : sfeeds) sfeed.processingFinished();
    }

    /**
     * Returns all search feeds that have sentiments clause in them.
     *
     * @return feeds.
     */
    public static List<SearchFeed> findFeedsWithSentimentsClause()
    {
        return GlobalController.getSearchFeedsManager().findFeedsWithSentimentsClause0();
    }

    /**
     * Returns all search feeds that have sentiments clause in them.
     *
     * @return feeds.
     */
    private List<SearchFeed> findFeedsWithSentimentsClause0()
    {
        List<SearchFeed> feeds = new LinkedList<SearchFeed>();

        for (SearchFeed feed : searchFeeds)
        {
            Query query = feed.getQuery();
            if (query.hasSentimentsClause()) feeds.add(feed);
        }

        return feeds;
    }

    /**
     * Runs query for a given search feed. Updates the list of articles.
     *
     * @param aSearchFeed search feed.
     */
    public void runQuery(SearchFeed aSearchFeed)
    {
        aSearchFeed.processingStarted();
        try
        {
            aSearchFeed.reviewArticlesTakenFrom(null);
            StandardGuide[] guides = guidesSet.getStandardGuides(null);
            for (StandardGuide guide : guides) scanGuide(guide, aSearchFeed);
        } finally
        {
            aSearchFeed.processingFinished();
        }
    }

    private void scanGuide(StandardGuide aGuide, SearchFeed aSearchFeed)
    {
        IFeed[] feeds = aGuide.getFeeds();
        for (IFeed feed : feeds) if (feed instanceof DataFeed) scanFeed(feed, aSearchFeed);
    }

    private void scanFeed(IFeed aFeed, SearchFeed aSearchFeed)
    {
        IArticle[] articles = aFeed.getArticles();
        for (IArticle article : articles) aSearchFeed.addArticleIfMatching(article);
    }
    
    // ---------------------------------------------------------------------------------------------
    // Listening to domain events
    // ---------------------------------------------------------------------------------------------

    /**
     * Invoked when new article has been added to the feed.
     *
     * @param feed    feed.
     * @param article article.
     */
    public void articleAdded(IFeed feed, IArticle article)
    {
        if (feed instanceof SearchFeed || isNotUpdatingAutomatically()) return;

        scheduleOrRun(new ProcessArticleAdded(article));
    }

    /**
     * Returns <code>TRUE</code> only if there's a property set that
     * doesn't allow search feeds to be updated dynamically.
     *
     * @return <code>TRUE</code> to not update search feeds.
     */
    private static synchronized boolean isNotUpdatingAutomatically()
    {
        if (dontUpdateAutomatically == null)
        {
            Preferences prefs = Application.getUserPreferences();
            dontUpdateAutomatically = prefs.getBoolean("searchfeeds.dontUpdateAutomatically", false);
        }

        return dontUpdateAutomatically;
    }

    /**
     * Invoked when the article has been removed from the feed.
     *
     * @param feed    feed.
     * @param article article.
     */
    public void articleRemoved(IFeed feed, IArticle article)
    {
        scheduleOrRun(new ProcessArticleRemoved(article));
    }

    /**
     * Schedule the task for execution or run if unable to schedule.
     *
     * @param task task.
     */
    private void scheduleOrRun(Runnable task)
    {
        try
        {
            executor.execute(task);
        } catch (InterruptedException e)
        {
            task.run();
        }
    }

    /**
     * Invoked when new guide has been added to the set.
     *
     * @param set           guides set.
     * @param guide         added guide.
     * @param lastInBatch   <code>TRUE</code> when this is the last even in batch.
     */
    public void guideAdded(GuidesSet set, IGuide guide, boolean lastInBatch)
    {
        if (guide instanceof StandardGuide)
        {
            collectSearchFeedsInGuide((StandardGuide)guide);
        }
    }

    /**
     * Invoked when new feed has been added to the guide.
     *
     * @param guide parent guide.
     * @param feed  added feed.
     */
    public void feedAdded(IGuide guide, IFeed feed)
    {
        if (feed instanceof SearchFeed) searchFeeds.add((SearchFeed)feed);
    }

    /**
     * Invoked when the feed has been removed from the guide.
     *
     * @param event feed removal event.
     */
    public void feedRemoved(FeedRemovedEvent event)
    {
        IFeed feed = event.getFeed();

        if (feed instanceof SearchFeed)
        {
            SearchFeed sfeed = (SearchFeed)feed;

            sfeed.unregisterListeners();
            searchFeeds.remove(sfeed);
        } else if (feed instanceof DataFeed)
        {
            IArticle[] articles = feed.getArticles();
            for (IArticle article : articles) articleRemoved(feed, article);
        }
    }

    /**
     * Invoked when the property of the feed has been changed.
     *
     * @param feed     feed.
     * @param property property of the feed.
     * @param oldValue old property value.
     * @param newValue new property value.
     */
    public void propertyChanged(IFeed feed, String property, Object oldValue, Object newValue)
    {
        if (feed instanceof SearchFeed)
        {
            SearchFeed sfeed = (SearchFeed)feed;
            if (SearchFeed.PROP_QUERY.equals(property) ||
                SearchFeed.PROP_DEDUP_UPDATED.equals(property))
            {
                runQuery(sfeed);
            }
        } else if (isInterestingFeedProperty(property))
        {
            for (SearchFeed searchFeed : searchFeeds)
            {
                searchFeed.reviewArticlesTakenFrom(feed);
                scanFeed(feed, searchFeed);
            }
        }
    }

    /**
     * Invoked when the property of the article has been changed.
     *
     * @param article  article.
     * @param property property of the article.
     * @param oldValue old property value.
     * @param newValue new property value.
     */
// This one creates a deadlock on startup see (BT #248)
//    public void propertyChanged(IArticle article, String property, Object oldValue, Object newValue)
//    {
//        Iterator iterator = searchFeeds.iterator();
//        while (iterator.hasNext())
//        {
//            SearchFeed searchFeed = (SearchFeed)iterator.next();
//            searchFeed.addArticleIfMatching(article);
//        }
//    }

    /**
     * Far not all properties should induce review of results of a query. In this
     * method we check if a property of the feed is interesting to us.
     *
     * @param aProperty name of feed property.
     *
     * @return <code>TRUE</code> if the change of this property is interesting to search feeds.
     */
    private boolean isInterestingFeedProperty(String aProperty)
    {
        return aProperty.equals(IFeed.PROP_TITLE) ||
            aProperty.equals(IFeed.PROP_RATING) ||
            aProperty.equals(DirectFeed.PROP_USER_TAGS);
    }

    /**
     * Reviews whole search feed.
     *
     * @param aSearchFeed search feed with changed query criteria.
     */
    public void queryUpdated(SearchFeed aSearchFeed)
    {
        aSearchFeed.reviewArticlesTakenFrom(null);
    }

    /**
     * Updates a search feed.
     *
     * @param feed feed.
     */
    public static void update(SearchFeed feed)
    {
        GlobalController.getSearchFeedsManager().runQuery(feed);
    }

    /**
     * Processes article removed event. Walks through the list of search feeds and
     * notifies them one by one.
     */
    private class ProcessArticleAdded implements Runnable
    {
        private final IArticle article;

        /**
         * Creates the task with an article.
         *
         * @param article article.
         */
        public ProcessArticleAdded(IArticle article)
        {
            this.article = article;
        }

        /**
         * Executed when processing should start.
         */
        public void run()
        {
            for (SearchFeed searchFeed : searchFeeds) searchFeed.addArticleIfMatching(article);
        }
    }

    /**
     * Processes article removed event. Walks through the list of search feeds and
     * notifies them one by one.
     */
    private class ProcessArticleRemoved implements Runnable
    {
        private final IArticle article;

        /**
         * Creates the task with an article.
         *
         * @param article article.
         */
        public ProcessArticleRemoved(IArticle article)
        {
            this.article = article;
        }

        /**
         * Executed when processing should start.
         */
        public void run()
        {
            for (SearchFeed searchFeed : searchFeeds) searchFeed.removeArticle(article);
        }
    }
}
