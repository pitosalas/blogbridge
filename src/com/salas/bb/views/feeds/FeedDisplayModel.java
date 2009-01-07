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
// $Id: FeedDisplayModel.java,v 1.31 2008/02/28 15:59:46 spyromus Exp $
//

package com.salas.bb.views.feeds;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.salas.bb.domain.FeedAdapter;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IArticleListener;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.utils.ArticleDateComparator;
import com.salas.bb.utils.IdentityList;
import com.salas.bb.utils.TimeRange;
import com.salas.bb.utils.uif.UifUtilities;
import static com.salas.bb.views.feeds.IFeedDisplayConstants.*;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Simple model for groupping articles by dates and presenting them in the list.
 * This implementation is thread-unsafe and it means that it's single-threaded.
 * If this model will be used (and it is going to) as the backend for some visual
 * component, all operations should be invoked in EDT thread only.
 */
public class FeedDisplayModel
{
    private static final Logger LOG = Logger.getLogger(FeedDisplayModel.class.getName());

    private static final ArticleDateComparator COMPARATOR_DESC =
        new ArticleDateComparator(true, false);
    private static final ArticleDateComparator COMPARATOR_ASC =
        new ArticleDateComparator(false, false);
    private static final IArticle[] EMPTY_GROUP = new IArticle[0];

    /**
     * TRUE to hide articles when they are marked as read while the
     * unread-only filter is applied.
     */
    private boolean hideArticlesWhenRead;

    private final ValueModel pageCountModel;
    private final IArticleListener  listener;

    private IFeed                   feed;
    private boolean                 ascending;
    private List<IFeedDisplayModelListener> listeners;
    private FeedListener            feedListener;

    /** The list of sorted articles. */
    private IArticle[] sortedArticles;

    /** Groupped articles */
    private IArticle[][] articlesGroups;

    /** Current filtering mode. */
    private int filter;

    /** List of currently visible articles. */
    private List<IArticle> visibleArticles;

    /** Maximum article age to be displayed. */
    private long maxArticleAge;
    /** This is the article to show even if it doesn't match the filter. */
    private IArticle alwaysVisibleArticle;

    /** The time when a feed was changed for the last time. */
    private volatile long feedChangeTime;

    /** Current page number. */
    private int page;
    /** The number of articles on the page (allowed). */
    private int pageSize;
    /** Articles we show on the selected page. */
    private List<IArticle> pageArticles;

    /**
     * Creates model w/o highlights advisor.
     */
    public FeedDisplayModel()
    {
        this(new ValueHolder(0));
    }

    /**
     * Creates model.
     *
     * @param pageCountModel model to update with new number of pages.
     */
    public FeedDisplayModel(ValueModel pageCountModel)
    {
        this.pageCountModel = pageCountModel;
        listener = new ArticleListener();

        ascending = false;
        listeners = new CopyOnWriteArrayList<IFeedDisplayModelListener>();
        feedListener = new FeedListener();
        visibleArticles = new IdentityList<IArticle>();

        feed = null;
        filter = IFeedDisplayConstants.FILTER_ALL;
        maxArticleAge = -1;

        page = 0;
        pageSize = 10; // TODO: test only!
        pageArticles = new IdentityList<IArticle>();

        recalcModel();
    }



    /**
     * Releases listeners and resources.
     */
    public void prepareToDismiss()
    {
        alwaysVisibleArticle = null;

        for (IArticle article : sortedArticles) article.removeListener(listener);

        if (feed != null) feed.removeListener(feedListener);
        fireArticlesRemoved();
    }

    /**
     * Sets the feed to display.
     *
     * @param aFeed feed.
     */
    public void setFeed(IFeed aFeed)
    {
        if (feed != aFeed)
        {
            alwaysVisibleArticle = null;

            if (feed != null) feed.removeListener(feedListener);
            feed = aFeed;
            feedChangeTime = System.currentTimeMillis();
            if (feed != null) feed.addListener(feedListener);

            // Reset the page and recalculate the model
            page = 0;
            recalcModel();
        }
    }

    /**
     * Returns currently loaded feed.
     *
     * @return feed.
     */
    public IFeed getFeed()
    {
        return feed;
    }

    /**
     * Recalculate model.
     */
    private void recalcModel()
    {
        // Unsubscribe from events
        if (sortedArticles != null)
        {
            for (IArticle article : sortedArticles) article.removeListener(listener);
        }

        sortedArticles = EMPTY_GROUP;
        visibleArticles.clear();

        if (feed != null)
        {
            IArticle[] articles = feed.getArticles();
            for (IArticle article : articles) addArticle(article);

            // Calculate pages
            updatePageCount();
            loadPage();
        } else
        {
            clearPage();

            pageCountModel.setValue(0);
        }
    }

    /**
     * Updates the page count basing on visible articles.
     */
    private void updatePageCount()
    {
        int numberOfPages = (int)Math.ceil(visibleArticles == null ? 0 : visibleArticles.size() / (float)pageSize);
        pageCountModel.setValue(numberOfPages);
    }

    /**
     * Clears the page and lets the view kmow.
     */
    private void clearPage()
    {
        articlesGroups = new IArticle[GroupsSetup.getGroupsCount()][];
        pageArticles.clear();
        fireArticlesRemoved();
    }

    /**
     * Loads the page with new articles.
     */
    private void loadPage()
    {
        clearPage();

        // Calculate the offset of the page
        int pageOffset = page * pageSize;

        // We can load something because there are visible articles in the buffer
        for (int i = pageOffset; i < pageOffset + pageSize && i < visibleArticles.size(); i++)
        {
            IArticle article = visibleArticles.get(i);
            addArticleToPage(article);
        }
    }

    private void updatePage()
    {
        // Calculate the offset of the page
        int pageOffset = page * pageSize;

        // We can load something because there are visible articles in the buffer
        IdentityList<IArticle> displayed = new IdentityList<IArticle>();
        for (int i = pageOffset; i < pageOffset + pageSize && i < visibleArticles.size(); i++)
        {
            IArticle article = visibleArticles.get(i);
            displayed.add(article);

            if (!pageArticles.contains(article))
            {
                addArticleToPage(article);
            }
        }

        // Place articles to remove into the removal array to avoid concurrent modification
        List<IArticle> toRemove = null;
        for (IArticle article : pageArticles)
        {
            if (!displayed.contains(article))
            {
                if (toRemove == null) toRemove = new LinkedList<IArticle>();
                toRemove.add(article);
            }
        }

        // Do actual removal
        if (toRemove != null)
        {
            for (IArticle article : toRemove)
            {
                pageArticles.remove(article);

                // Remove from the page only
                int groupIndex = findGroupIndex(article);
                if (groupIndex == -1) groupIndex = 0;

                IArticle[] group = getRawGroup(groupIndex);
                int indexWithinGroup = indexOf(article, group);
                if (indexWithinGroup > -1)
                {
                    articlesGroups[groupIndex] = removeArticle(group, article);
                    fireArticleRemoved(article, applySorting(groupIndex), indexWithinGroup);
                }
            }
        }
    }

    private void addArticleToPage(IArticle article)
    {
        // Find a group for the article
        int groupIndex = findGroupIndex(article);
        if (groupIndex == -1) groupIndex = 0;

        // Find a place in the group for the article
        IArticle[] group = getRawGroup(groupIndex);
        int indexWithinGroup = Arrays.binarySearch(group, article, getArticlesComparator());
        if (indexWithinGroup < 0)
        {
            indexWithinGroup = -indexWithinGroup - 1;

            // Insert it there and to the pagearticles list
            articlesGroups[groupIndex] = insertArticle(group, article, indexWithinGroup);
            pageArticles.add(article);

            // Let the view know about this article
            fireArticleAdded(article, applySorting(groupIndex), indexWithinGroup);
        }
    }

    /**
     * Called when new article should be added to the model.
     * The model will find appropriate place for it in the lists and update
     * them. After that new article event will be fired to notify the listeners.
     *
     * @param aArticle  article to add.
     */
    void onArticleAdded(IArticle aArticle)
    {
        if (addArticle(aArticle))
        {
            updatePageCount();
            updatePage();
        }
    }

    private boolean addArticle(IArticle aArticle)
    {
        if (contains(aArticle)) return false;

        boolean reviewed = false;

        int index = Arrays.binarySearch(sortedArticles, aArticle, getArticlesComparator());

        // If index is positive, we have articles with the same timestamp and we reuse
        // their index for upcoming insertion, otherwise -- we convert to insertion index.
        if (index < 0)
        {
            index = -index - 1;
            sortedArticles = insertArticle(sortedArticles, aArticle, index);

            reviewed = reviewArticle(aArticle);
            aArticle.addListener(listener);
        }

        return reviewed;
    }

    /**
     * Called when some article should be removed from the model. The model will find
     * it and remove from the lists. Also it will fire necessary events to report this
     * fact to the listeners.
     *
     * @param aArticle article to remove.
     */
    void onArticleRemoved(IArticle aArticle)
    {
        if (!contains(aArticle)) return;

        sortedArticles = removeArticle(sortedArticles, aArticle);

        if (isVisible(aArticle)) hideArticle(aArticle);

        aArticle.removeListener(listener);

        updatePageCount();
        updatePage();
    }

    /**
     * Returns number of articles in the model.
     *
     * @return articles count.
     */
    public int getArticlesCount()
    {
        return pageArticles.size();
    }

    /**
     * Returns the article at a given index.
     *
     * @param index index.
     *
     * @return article.
     */
    public IArticle getArticle(int index)
    {
        return pageArticles.get(index);
    }

    /**
     * Sets the order of sorting. Default is descending (latest first).
     *
     * @param asc   <code>TRUE</code> for ascending order, <code>FALSE</code> for descending.
     */
    public void setAscending(boolean asc)
    {
        if (ascending != asc)
        {
            ascending = asc;
            rebuild();
        }
    }

    /**
     * Makes full model rebuild as if the feed was selected again.
     */
    private void rebuild()
    {
        IFeed oldFeed = feed;
        feed = null;
        setFeed(oldFeed);
    }

    /**
     * Returns number of groups in this model. Number of groups doesn't change over
     * the time.
     *
     * @return groups count.
     */
    public int getGroupsCount()
    {
        return GroupsSetup.getGroupsCount();
    }

    /**
     * Returns the group of articles. Sorting order affects the order of groups.
     *
     * @param index group index.
     *
     * @return group index.
     *
     * @see #setAscending(boolean)
     */
    public IArticle[] getGroup(int index)
    {
        return getRawGroup(applySorting(index));
    }

    /**
     * Returns the group of articles.
     *
     * @param index group index.
     *
     * @return group index.
     *
     * @see #setAscending(boolean)
     */
    private IArticle[] getRawGroup(int index)
    {
        IArticle[] group = articlesGroups[index];
        if (group == null) group = EMPTY_GROUP;

        return group;
    }

    /**
     * Returns the name of group. Sorting order affects the order of groups.
     *
     * @param group group index.
     *
     * @return group name.
     *
     * @see #setAscending(boolean)
     */
    public String getGroupName(int group)
    {
        return GroupsSetup.getGroupTitle(applySorting(group));
    }

    /**
     * Applies sorting direction to the group index.
     *
     * @param group group index.
     *
     * @return group index from the head if sorting mode is descending, and
     *               from the tail if otherwise.
     */
    private int applySorting(int group)
    {
        return ascending ? getGroupsCount() - group - 1: group;
    }

    /**
     * Inserts another article into the given array.
     *
     * @param aArticles source articles array.
     * @param aArticle  article to insert.
     * @param aIndex    index to insert at.
     *
     * @return new array.
     */
    static IArticle[] insertArticle(IArticle[] aArticles, IArticle aArticle, int aIndex)
    {
        IArticle[] newArticlesList = new IArticle[aArticles.length + 1];

        copyObjects(aArticles, 0, newArticlesList, 0, aIndex);
        copyObjects(aArticles, aIndex, newArticlesList, aIndex + 1, aArticles.length - aIndex);
        newArticlesList[aIndex] = aArticle;

        return newArticlesList;
    }

    /**
     * Removes the article from given articles array.
     *
     * @param aArticles source articles array.
     * @param aArticle  article to remove.
     *
     * @return new array.
     */
    static IArticle[] removeArticle(IArticle[] aArticles, IArticle aArticle)
    {
        IArticle[] newArticlesList = aArticles;

        int index = indexOf(aArticle, aArticles);
        if (index > -1)
        {
            newArticlesList = new IArticle[aArticles.length - 1];
            copyObjects(aArticles, 0, newArticlesList, 0, index);
            copyObjects(aArticles, index + 1, newArticlesList, index, aArticles.length - index - 1);
        }

        return newArticlesList;
    }

    /**
     * Copies objects from source array to the destination array.
     *
     * @param src       source array.
     * @param srcIndex  start index in source array.
     * @param dest      destination array.
     * @param destIndex start index in destination array.
     * @param len       length of block.
     */
    static void copyObjects(Object[] src, int srcIndex, Object[] dest, int destIndex, int len)
    {
        for (int a = 0; srcIndex < src.length && destIndex < dest.length && a < len; a++)
        {
            dest[destIndex++] = src[srcIndex++];
        }
    }

    /**
     * Returns correct comparator depending on current sorting mode.
     *
     * @return comparator.
     */
    private ArticleDateComparator getArticlesComparator()
    {
        return ascending ? COMPARATOR_ASC : COMPARATOR_DESC;
    }

    /**
     * Breaks the list of articles into groups corresponding to given ranges.
     *
     * @param articles  list of articles.
     * @param ranges    ranges to assign articles to.
     *
     * @return structured articles.
     */
    static IArticle[][] groupArticles(IArticle[] articles, TimeRange[] ranges)
    {
        IArticle[][] groups = new IArticle[ranges.length][];

        for (IArticle article : articles)
        {
            int groupIndex = findGroupIndex(article, ranges);
            if (groupIndex > -1)
            {
                IArticle[] group = groups[groupIndex];
                if (group == null) group = EMPTY_GROUP;

                groups[groupIndex] = insertArticle(group, article, group.length);
            }
        }

        return groups;
    }

    /**
     * Finds the index of the time range the article belongs to.
     *
     * @param aArticle  article.
     *
     * @return index of the range or <code>-1</code>, if doesn't belong to any of them.
     */
    static int findGroupIndex(IArticle aArticle)
    {
        return findGroupIndex(aArticle, GroupsSetup.getGroupTimeRanges());
    }

    /**
     * Finds the index of the time range the article belongs to.
     *
     * @param aArticle  article.
     * @param aRanges   list of ranges to check.
     *
     * @return index of the range or <code>-1</code>, if doesn't belong to any of them.
     */
    static int findGroupIndex(IArticle aArticle, TimeRange[] aRanges)
    {
        long time = aArticle.getPublicationDate().getTime();

        int index = -1;
        for (int i = 0; index == -1 && i < aRanges.length; i++)
        {
            TimeRange range = aRanges[i];
            if (range.isInRange(time)) index = i;
        }

        return index;
    }

    /**
     * Returns <code>TRUE</code>, if exectly this article object is on the list.
     *
     * @param aArticle article to look for.
     *
     * @return <code>TRUE</code>, if exectly this article object is on the list.
     */
    private boolean contains(IArticle aArticle)
    {
        return indexOf(aArticle) > -1;
    }

    /**
     * Finds the index of article.
     *
     * @param aArticle  article.
     *
     * @return index or <code>-1</code>, if not on the list.
     */
    private int indexOf(IArticle aArticle)
    {
        return indexOf(aArticle, sortedArticles);
    }

    /**
     * Finds the index of article.
     *
     * @param aArticle  article.
     * @param aArticles list of articles to scan.
     *
     * @return index or <code>-1</code>, if not on the list.
     */
    private static int indexOf(IArticle aArticle, IArticle[] aArticles)
    {
        int index = -1;

        for (int i = 0; index == -1 && i < aArticles.length; i++)
        {
            if (aArticles[i] == aArticle) index = i;
        }

        return index;
    }

    // --------------------------------------------------------------------------------------------
    // Events
    // --------------------------------------------------------------------------------------------

    /**
     * Adds new listener.
     *
     * @param aListener listener.
     */
    public void addListener(IFeedDisplayModelListener aListener)
    {
        if (!listeners.contains(aListener)) listeners.add(aListener);
    }

    private void fireArticlesRemoved()
    {
        for (IFeedDisplayModelListener l : listeners) l.articlesRemoved();
    }

    /**
     * Fires event to all listeners when new article gets added into the group.
     *
     * @param aArticle          article has been added.
     * @param aGroupIndex       index of the group.
     * @param aIndexWithinGroup index within the group.
     */
    private void fireArticleAdded(IArticle aArticle, int aGroupIndex, int aIndexWithinGroup)
    {
        for (IFeedDisplayModelListener l : listeners)
        {
            l.articleAdded(aArticle, aGroupIndex, aIndexWithinGroup);
        }
    }

    /**
     * Fires event to all listeners when new article gets removed from the group.
     *
     * @param aArticle          article has been removed.
     * @param aGroupIndex       index of the group.
     * @param aIndexWithinGroup index within the group.
     */
    private void fireArticleRemoved(IArticle aArticle, int aGroupIndex, int aIndexWithinGroup)
    {
        for (IFeedDisplayModelListener l : listeners)
        {
            l.articleRemoved(aArticle, aGroupIndex, aIndexWithinGroup);
        }
    }

    /**
     * Sets filter for articles filtering.
     *
     * @param aFilter filter.
     *
     * @see IFeedDisplayConstants#FILTER_ALL
     * @see IFeedDisplayConstants#FILTER_UNREAD
     */
    public void setFilter(int aFilter)
    {
        if (filter != aFilter)
        {
            // Also reset the always visible article
            alwaysVisibleArticle = null;

            filter = aFilter;

            reviewArticles();
        }
    }

    /**
     * Ensures that the article is visible until the next filter mode
     * change or setting different feed.
     *
     * @param article article.
     *
     * @return new page or <code>-1</code> if hasn't changed.
     */
    public int ensureArticleVisibility(IArticle article)
    {
        int newPage = -1;

        alwaysVisibleArticle = article;
        if (article != null)
        {
            reviewArticle(article);
            updatePageCount();

            int articlePage = findPageFor(article);
            if (articlePage != page && articlePage != -1)
            {
                setPage(articlePage);
                newPage = articlePage;
            } else updatePage();
        }
        
        return newPage;
    }

    /**
     * Returns the page number for a given article if it's among the visible
     * articles.
     *
     * @param article article.
     *
     * @return page number or '-1' if invisible.
     */
    private int findPageFor(IArticle article)
    {
        int page = -1;

        int i = visibleArticles.indexOf(article);
        if (i >= 0 && pageSize > 0) page = i / pageSize;

        return page;
    }

    /**
     * Invoked when article changes.
     *
     * @param article article
     */
    private void onArticleChanged(IArticle article)
    {
        if ((filter == FILTER_UNREAD && (hideArticlesWhenRead || !article.isRead())) ||
            filter == FILTER_NEGATIVE || filter == FILTER_NON_NEGATIVE || filter == FILTER_POSITIVE)
        {
            if (reviewArticle(article))
            {
                updatePageCount();
                updatePage();
            }
        }
    }

    /**
     * Reviews all articles in this feed.
     */
    private void reviewArticles()
    {
        boolean updated = false;
        for (IArticle article : sortedArticles) updated |= reviewArticle(article);

        if (updated)
        {
            updatePageCount();
            updatePage();
        }
    }

    /**
     * Reviews given article.
     *
     * @param aArticle article.
     *
     * @return <code>TRUE</code> if updated the article state.
     */
    private boolean reviewArticle(IArticle aArticle)
    {
        boolean updated;

        if (shouldBeVisible(aArticle))
        {
            updated = !isVisible(aArticle) && showArticle(aArticle);
        } else
        {
            updated = isVisible(aArticle) && hideArticle(aArticle);
        }

        return updated;
    }

    /**
     * Hides the article.
     *
     * @param aArticle article to hide.
     *
     * @return <code>TRUE</code> if something was hidden.
     */
    private boolean hideArticle(IArticle aArticle)
    {
        boolean hidden = false;

        int groupIndex = findGroupIndex(aArticle);
        if (groupIndex == -1) groupIndex = 0;

        visibleArticles.remove(aArticle);

        IArticle[] group = getRawGroup(groupIndex);
        int indexWithinGroup = indexOf(aArticle, group);
        if (indexWithinGroup > -1)
        {
            articlesGroups[groupIndex] = removeArticle(group, aArticle);
            if (pageArticles.remove(aArticle))
            {
                hidden = true;
                fireArticleRemoved(aArticle, applySorting(groupIndex), indexWithinGroup);
            }
        }

        return hidden;
    }

    /**
     * Shows the article (exposes outside the view).
     *
     * @param aArticle article to show.
     *
     * @return <code>TRUE</code> if article was shown.
     */
    private boolean showArticle(IArticle aArticle)
    {
        boolean shown = false;

        int index = Collections.binarySearch(visibleArticles, aArticle, getArticlesComparator());
        // If index is positive, we have articles with the same timestamp and we reuse
        // their index for upcoming insertion, otherwise -- we convert to insertion index.
        if (index < 0)
        {
            index = -index - 1;
            visibleArticles.add(index, aArticle);
            shown = true;
        }


// Disabled because articles never enter the paged view
//        fireArticleAdded(aArticle, applySorting(groupIndex), indexWithinGroup);

        return shown;
    }

    /**
     * Returns <code>TRUE</code> if there are some visible articles.
     *
     * @return <code>TRUE</code> if there are some visible articles.
     */
    public boolean hasVisibleArticles()
    {
        return visibleArticles.size() > 0;
    }

    /**
     * Returns <code>TRUE</code> if article is currently visible.
     *
     * @param aArticle article.
     *
     * @return <code>TRUE</code> if article is currently visible.
     */
    private boolean isVisible(IArticle aArticle)
    {
        return visibleArticles.contains(aArticle);
    }

    /**
     * Returns <code>TRUE</code> if article should be visible taking current
     * model mode in account.
     *
     * @param aArticle  article to review.
     *
     * @return <code>TRUE</code> if should be visible.
     */
    private boolean shouldBeVisible(IArticle aArticle)
    {
        if (alwaysVisibleArticle == aArticle) return true;

        boolean filtered =
             filter == FILTER_ALL ||
            (filter == FILTER_PINNED       && aArticle.isPinned()) ||
            (filter == FILTER_UNREAD       && !aArticle.isRead()) ||
            (filter == FILTER_POSITIVE     && aArticle.isPositive()) ||
            (filter == FILTER_NEGATIVE     && aArticle.isNegative()) ||
            (filter == FILTER_NON_NEGATIVE && !aArticle.isNegative());

        return filtered && (maxArticleAge == -1 || isNotOld(aArticle));
    }

    /**
     * Returns <code>TRUE</code> if article is not older than max defined age.
     *
     * @param aArticle article to check.
     *
     * @return <code>TRUE</code> if article isn't older.
     */
    private boolean isNotOld(IArticle aArticle)
    {
        long age = System.currentTimeMillis() - aArticle.getPublicationDate().getTime();
        return age < maxArticleAge;
    }

    /**
     * Suppresses the articles older than given number of millis.
     *
     * @param millis number of millis or <code>-1</code> to turn feature off.
     */
    public void setMaxArticleAge(long millis)
    {
        if (maxArticleAge != millis)
        {
            maxArticleAge = millis;
            reviewArticles();
        }
    }

    // --------------------------------------------------------------------------------------------
    // Paging
    // --------------------------------------------------------------------------------------------

    /**
     * Changes the page to a given.
     *
     * @param page new page.
     */
    public void setPage(int page)
    {
        if (this.page != page)
        {
            this.page = page;
            loadPage();
        }
    }

    /**
     * Returns current page number.
     *
     * @return current page.
     */
    public int getPage()
    {
        return page;
    }

    /**
     * Returns the number of pages possible.
     *
     * @return pages.
     */
    public int getPagesCount()
    {
        return (Integer)pageCountModel.getValue();
    }

    // --------------------------------------------------------------------------------------------
    // DEBUG
    // --------------------------------------------------------------------------------------------

    /**
     * Dumps the state to the console.
     */
    public void dump()
    {
        LOG.warning("--- Model Dump ---");
        LOG.warning("Number of articles: " + getArticlesCount());
        LOG.warning("Articles:");
        for (IArticle article : sortedArticles) LOG.warning("  " + article.getTitle());
    }

    /**
     * Sets the size of the page.
     *
     * @param size size.
     */
    public void setPageSize(int size)
    {
        if (pageSize != size)
        {
            pageSize = size;
            recalcModel();
        }
    }

    // --------------------------------------------------------------------------------------------
    // Feed listener
    // --------------------------------------------------------------------------------------------

    /**
     * Listener of feed events.
     */
    private class FeedListener extends FeedAdapter
    {
        /**
         * Called when some article is added to the feed.
         *
         * @param feed    feed.
         * @param article article.
         */
        public void articleAdded(IFeed feed, final IArticle article)
        {
            if (UifUtilities.isEDT())
            {
                onArticleAdded(article);
            } else SwingUtilities.invokeLater(new UpdateModel(article, UpdateAction.ADDED));
        }

        /**
         * Called when some article is removed from the feed.
         *
         * @param feed    feed.
         * @param article article.
         */
        public void articleRemoved(IFeed feed, final IArticle article)
        {
            if (UifUtilities.isEDT())
            {
                onArticleRemoved(article);
            } else SwingUtilities.invokeLater(new UpdateModel(article, UpdateAction.REMOVED));
        }
    }

    /**
     * Listener for article read/unread state changes.
     */
    private class ArticleListener implements IArticleListener
    {
        private List<String> interestingProperties;

        /**
         * Creates the listener.
         */
        private ArticleListener()
        {
            interestingProperties = new LinkedList<String>();
            interestingProperties.add(IArticle.PROP_READ);
            interestingProperties.add(IArticle.PROP_POSITIVE);
            interestingProperties.add(IArticle.PROP_NEGATIVE);
        }

        /**
         * Invoked when the property of the article has been changed.
         *
         * @param article  article.
         * @param property property of the article.
         * @param oldValue old property value.
         * @param newValue new property value.
         */
        public void propertyChanged(final IArticle article, String property, Object oldValue,
            Object newValue)
        {
            if (interestingProperties.contains(property))
            {
                if (UifUtilities.isEDT())
                {
                    onArticleChanged(article);
                } else SwingUtilities.invokeLater(new UpdateModel(article, UpdateAction.CHANGED));
            }
        }
    }

    /**
     * The set of possible update actions.
     */
    enum UpdateAction { ADDED, REMOVED, CHANGED }

    /**
     * Model update command that respects the time of feed selection
     * and skips the updates that are delivered for the previous feed.
     */
    private class UpdateModel implements Runnable
    {
        private final IArticle article;
        private final UpdateAction action;
        private final long timestamp;

        /**
         * Creates the model update operation.
         *
         * @param article   article.
         * @param action    update action.
         */
        public UpdateModel(IArticle article, UpdateAction action)
        {
            this.article = article;
            this.action = action;
            timestamp = System.currentTimeMillis();
        }

        /**
         * Invoked when it's time to update the model.
         */
        public void run()
        {
            if (timestamp >= feedChangeTime)
            {
                switch (action)
                {
                    case ADDED:
                        onArticleAdded(article);
                        break;
                    case REMOVED:
                        onArticleRemoved(article);
                        break;
                    default:
                        onArticleChanged(article);
                }
            }
        }
    }
}
