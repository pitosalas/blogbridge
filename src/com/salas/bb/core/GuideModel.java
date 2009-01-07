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
// $Id: GuideModel.java,v 1.31 2008/02/28 15:59:49 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.domain.FeedClassifier;
import com.salas.bb.domain.FeedsSortOrder;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.Sort;

import javax.swing.*;

/**
 * This list indirectly forwards various requests to the
 * currently selected Channel Guide. There is only ONE ChannelGuideModel which simply
 * forward the requests to the underlying ChannelGuide models which is currently
 * selected.
 *
 * <h2>Sorting</h2>
 *
 * <p>When sorting is enabled model calculates the intermediate scores for each feed
 * and orders them using these scores. The scores are calculated basing on data of feeds
 * and selected first/second sort classes
 * ({@link #calculateScore(com.salas.bb.domain.IFeed, int, int)}). Later when sorting is
 * required the scores are compared as regular integers and position of feed items in the
 * list is evaluated.</p>
 */
public class GuideModel extends AbstractListModel
{
    private final boolean           nonVisual;

    private ScoresCalculator        scoreCalculator;

    private JList                   listComponent;

    private IFeed[]                 feeds;
    private int[]                   channelsClasses;
    private int[]                   channelsScores;

    private int[]                   filteredChannels;
    private int                     filteredChannelsCount;

    private int[]                   sortedChannels;
    private int                     sortedChannelsCount;

    // Settings
    private int                     scoreThreshold;
    private boolean                 sortingEnabled;

    private int                     primarySort;
    private boolean                 primarySortRev;
    private int                     secondarySort;
    private boolean                 secondarySortRev;

    private IGuide                  currentGuide;

    private FeedScoresComparator    feedScoresComparator;

    private IFeed                   selectedFeed;

    /**
     * Creates model and initializes it with calculator of feeds scores.
     *
     * @param aCalculator   calculator of feeds scores.
     * @param aFeedDDM      feed display mode manager instance to control visibility.
     */
    public GuideModel(ScoresCalculator aCalculator, FeedDisplayModeManager aFeedDDM)
    {
        this(aCalculator, true, aFeedDDM);
    }

    /**
     * Creates model and initializes it with calculator of feeds scores.
     *
     * @param aCalculator   calculator of feeds scores.
     * @param visual        TRUE if this model will have visual components attached.
     * @param aFeedDMM      feed display mode manager instance to control visibility.
     */
    public GuideModel(ScoresCalculator aCalculator, boolean visual,
                             FeedDisplayModeManager aFeedDMM)
    {
        scoreThreshold = UserPreferences.DEFAULT_GOOD_CHANNEL_STARZ - 1;
        sortingEnabled = UserPreferences.DEFAULT_SORTING_ENABLED;
        primarySort = UserPreferences.DEFAULT_SORT_BY_CLASS_1;
        primarySortRev = UserPreferences.DEFAULT_REVERSED_SORT_BY_CLASS_1;
        secondarySort = UserPreferences.DEFAULT_SORT_BY_CLASS_2;
        secondarySortRev = UserPreferences.DEFAULT_REVERSED_SORT_BY_CLASS_2;

        scoreCalculator = aCalculator;
        nonVisual = !visual;

        currentGuide = null;
        selectedFeed = null;
        setFeeds(Constants.EMPTY_FEEDS_LIST, true);
    }

    /**
     * Registers component which is using this model. This component will be questioned for
     * selection when the model will be preparing updates.
     *
     * @param aListComponent component.
     */
    public void setListComponent(JList aListComponent)
    {
        listComponent = aListComponent;
    }

    /**
     * Sets the guide to display.
     *
     * @param aGuide guide to display.
     */
    public void setGuide(IGuide aGuide)
    {
        IFeed[] feedsList;
        if (aGuide == null)
        {
            feedsList = Constants.EMPTY_FEEDS_LIST;
        } else
        {
            feedsList = aGuide.getFeeds();
        }

        synchronized (this)
        {
            boolean newGuide = currentGuide != aGuide;
            currentGuide = aGuide;
            setFeeds(feedsList, newGuide);
        }
    }

    /**
     * Returns current guide this model has been initialized with.
     *
     * @return the guide.
     */
    public IGuide getCurrentGuide()
    {
        return currentGuide;
    }

    private void setFeeds(final IFeed[] feedsList, final boolean newGuide)
    {
        // EDT !!!

        // TODO: The problem here is that if we aren't under EDT there will be
        // TODO: a period when new channels list do not corresponds to what we have
        // TODO: on the screen.

        feeds = feedsList;
        rebuild0(newGuide, true);
    }

    /**
     * Recalculates classes and scores.
     */
    private synchronized void recalculate()
    {
        channelsClasses = calculateClasses(feeds);
        channelsScores = calculateScores(feeds, channelsClasses);
    }

    /**
     * Refilter and resort current channels list.
     *
     * @param doFiltering TRUE to refilter feeds.
     */
    public synchronized void rebuild(boolean doFiltering)
    {
        rebuild0(false, doFiltering);
    }

    private void rebuild0(boolean newGuide, boolean doFiltering)
    {
        // EDT!!!

        if (doFiltering) recalculate();

        final int currentViewIndex = newGuide ? -1 : getSelectedChannelIndex();
        final int oldSortedCount = sortedChannelsCount;

        final int currentDataIndex = currentViewIndex == -1 ? -1 : sortedChannels[currentViewIndex];

        if (newGuide || filteredChannels.length < feeds.length)
        {
            filteredChannels = new int[feeds.length];
            sortedChannels = new int[feeds.length];
            doFiltering = true;
        }

        if (newGuide || doFiltering)
        {
            filteredChannelsCount = filterFeeds(feeds, channelsScores.length, channelsClasses,
                filteredChannels);
        }

        sortedChannels = copyFeedsList(filteredChannels, filteredChannelsCount, sortingEnabled,
            channelsScores);
        sortedChannelsCount = filteredChannelsCount;

        final int newViewIndex = currentDataIndex == -1
            ? -1 : dataToView(sortedChannels, sortedChannelsCount, currentDataIndex);

        if (!nonVisual)
        {
            fireChanges(oldSortedCount, sortedChannelsCount, currentViewIndex, newViewIndex);
        }
    }

    /**
     * Returns view index of currently selected item in the list.
     *
     * @return view index.
     */
    private int getSelectedChannelIndex()
    {
        return listComponent == null ? -1 : listComponent.getSelectedIndex();
    }

    /**
     * Sets the starz threshold.
     *
     * @param aScoreThreshold threshold.
     */
    public synchronized void setScoreThreshold(int aScoreThreshold)
    {
        if (scoreThreshold != aScoreThreshold)
        {
            scoreThreshold = aScoreThreshold;

            rebuild(true);
        }
    }

    /**
     * Gets the number of channels in the currently selected
     * ChannelGuide. We do this simply by asking the model what the currently selected
     * ChannelGuide is.
     *
     * @return number of channels in list.
     */
    public synchronized int getSize()
    {
        return sortedChannelsCount;
    }

    /**
     * Called to return the ChannelGuide entry that can be found at the
     * specified index. Note that because this overrides the corresponding method in
     * AbstractListModel, it has to return Object. Caller has to know to cast.
     *
     * @param index index of element.
     *
     * @return element.
     */
    public synchronized Object getElementAt(final int index)
    {
        return feeds[viewToData(index)];
    }

    /**
     * Returns <code>true</code> if specified feed is in current model list.
     *
     * @param feed   feed to check.
     *
     * @return <code>true</code> if present in list.
     */
    public synchronized boolean isPresent(IFeed feed)
    {
        return indexOf(feed) != -1;
    }

    /**
     * Called when feed information chaged and we need to redraw the view.
     *
     * @param feed feed with changed info.
     */
    public synchronized void contentsChangedAt(final IFeed feed)
    {
        if (feed == null) return;

        // If view index is available then we need to repaint corresponding cell
        if (!nonVisual)
        {
            int viewIndex = indexOf(feed);
            if (viewIndex > -1)
            {
                 fireContentsChanged(this, viewIndex, viewIndex);
            }
        }

        // TODO Potential concurrency problem !!!

        // Run this code in another event to let the changed feed cell repaint itself
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                final int viewIndex = indexOf(feed);
                final int dataIndex = viewIndex > -1 ? viewToData(viewIndex) : dataIndexOf(feed);

                // If feed belongs to this guide then we need to recalculate its
                // class and score and rebuild the list.
                if (dataIndex != -1)
                {
                    IFeed channel = feeds[dataIndex];

                    int newClass = FeedClassifier.classify(channel);
                    int newScore = calculateScore(channel, newClass, -1);
                    int oldClass = channelsClasses[dataIndex];

                    boolean doRebuild = false;
                    boolean doFiltering = false;

                    if (oldClass != newClass || channelsScores[dataIndex] != newScore)
                    {
                        channelsClasses[dataIndex] = newClass;
                        channelsScores[dataIndex] = newScore;
                        doRebuild = true;
                        doFiltering = true;
                    }

                    // If this channel became visible add it to the list of filtered channels
                    boolean shouldBeDisplayed = shouldBeDisplayed(channel, newClass);

                    if (viewIndex == -1 && shouldBeDisplayed)
                    {
                        filteredChannels[filteredChannelsCount++] = dataIndex;
                        doRebuild = true;
                        doFiltering = false;
                    } else if (viewIndex != -1 && !shouldBeDisplayed)
                    {
                        doRebuild = true;
                        doFiltering = true;
                    }

                    if (doRebuild) rebuild(doFiltering);
                }
            }
        });
    }

    /**
     * Called when some channels added to the guide being in the model.
     *
     * @param indexFrom index of starting channel.
     * @param indexTo   index of ending channel.
     */
    public synchronized void feedsAdded(final int indexFrom, final int indexTo)
    {
        // Add another slots for new feeds
        int size = filteredChannels.length + indexTo - indexFrom + 1;
        int[] newFiltered = new int[size];
        int[] newSorted = new int[size];
        System.arraycopy(filteredChannels, 0, newFiltered, 0, filteredChannels.length);
        System.arraycopy(sortedChannels, 0, newSorted, 0, filteredChannels.length);

        filteredChannels = newFiltered;
        sortedChannels = newSorted;

        setGuide(currentGuide);
    }

    /**
     * Recalculates classes and scores of all items, filters and sorts the list.
     */
    public synchronized void fullRebuild()
    {
        setGuide(currentGuide);
    }

    /**
     * Returns index of specified feed in current list.
     *
     * @param feed feed to check.
     *
     * @return index of feed or <code>-1</code>.
     */
    public synchronized int indexOf(IFeed feed)
    {
        int viewIndex = -1;

        if (feed != null)
        {
            for (int i = 0; viewIndex == -1 && i < sortedChannelsCount; i++)
            {
                if (feeds[viewToData(i)] == feed) viewIndex = i;
            }
        }

        return viewIndex;
    }

    // Converts data index into view index using the specified list and looking into
    // defined first elements.
    private int dataToView(int[] list, int length, int dataIndex)
    {
        int viewIndex = -1;
        for (int i = 0; viewIndex == -1 && i < length; i++)
        {
            if (list[i] == dataIndex) viewIndex = i;
        }

        return viewIndex;
    }

    // Returns index of feed in original data (-1 if feed isn't present)
    private synchronized int dataIndexOf(IFeed channel)
    {
        int index = -1;
        for (int i = 0; index == -1 && i < feeds.length; i++)
        {
            if (feeds[i] == channel) index = i;
        }

        return index;
    }

    /**
     * Converts view index into data.
     *
     * @param viewIndex view index.
     *
     * @return data index.
     */
    public synchronized int viewToData(int viewIndex)
    {
        return sortedChannels[viewIndex];
    }

    /**
     * Enables / disables sorting of the list.
     *
     * @param enabled TRUE to enable sorting.
     */
    public void setSortingEnabled(boolean enabled)
    {
        if (sortingEnabled != enabled)
        {
            sortingEnabled = enabled;
            rebuild(false);
        }
    }

    /**
     * Sets primary sorting order.
     *
     * @param order sort order.
     *
     * @see FeedsSortOrder
     */
    public void setPrimarySortOrder(int order)
    {
        if (primarySort != order)
        {
            primarySort = order;
            fullRebuild();
        }
    }

    /**
     * Sets secondary sorting order.
     *
     * @param order sort order.
     *
     * @see FeedsSortOrder
     */
    public void setSecondarySortOrder(int order)
    {
        if (secondarySort != order)
        {
            secondarySort = order;
            fullRebuild();
        }
    }

    /**
     * Sets direction of primary sorting.
     *
     * @param reversed <code>TRUE</code> for reversed direction.
     */
    public void setPrimarySortOrderDirection(boolean reversed)
    {
        if (primarySortRev != reversed)
        {
            primarySortRev = reversed;
            fullRebuild();
        }
    }

    /**
     * Sets direction of secondary sorting.
     *
     * @param reversed <code>TRUE</code> for reversed direction.
     */
    public void setSecondarySortOrderDirection(boolean reversed)
    {
        if (secondarySortRev != reversed)
        {
            secondarySortRev = reversed;
            fullRebuild();
        }
    }

    /**
     * Called to notify the model that filter has changed values and the model needs
     * to be recalculated. This method is used instead of listeners approach because
     * we can have multiple changes of filter and we don't need the model to be repaired
     * for all of them -- just for the last in the series.
     */
    public void filterChanged()
    {
        rebuild(true);
    }

    /**
     * Sets all options and doesn't make any updates to the model. Useful to setup everything
     * before setting new guide.
     *
     * @param aScoreThreshold       new score threshold.
     * @param aPrSortOrder          primary sorting order.
     * @param aPrSortOrderReversed  primary sorting order reverse flag.
     * @param aScSortOrder          secondary sorting order.
     * @param aScSortOrderReversed  secondary sorting order reverse flag.
     * @param aSortingEnabled       sorting enableness flag.
     */
    public void setOptions(int aScoreThreshold, int aPrSortOrder, boolean aPrSortOrderReversed,
        int aScSortOrder, boolean aScSortOrderReversed, boolean aSortingEnabled)
    {
        scoreThreshold = aScoreThreshold;
        primarySort = aPrSortOrder;
        primarySortRev = aPrSortOrderReversed;
        secondarySort = aScSortOrder;
        secondarySortRev = aScSortOrderReversed;
        sortingEnabled = aSortingEnabled;
    }

    // ---------------------------------------------------------------------------------------------
    // Calculations
    // ---------------------------------------------------------------------------------------------

    /**
     * Filters list of feeds using their classes and puts in the resulting buffer.
     *
     * @param aFeeds                feeds to filter.
     * @param aFeedsCount           number of feeds to filter.
     * @param aFeedsClasses         classes of feeds.
     * @param aFilteredFeedsList    buffer to fill with filtered feeds list.
     *
     * @return number of feeds were put in the buffer.
     */
    private int filterFeeds(IFeed[] aFeeds, int aFeedsCount, int[] aFeedsClasses,
        int[] aFilteredFeedsList)
    {
        int index = 0;

        for (int i = 0; i < aFeedsCount; i++)
        {
            IFeed feed = aFeeds[i];
            int feedsClass = aFeedsClasses[i];

            if (shouldBeDisplayed(feed, feedsClass)) aFilteredFeedsList[index++] = i;
        }

        return index;
    }

    /**
     * Returns TRUE if feed should be displayed.
     *
     * @param aFeed         feed object.
     * @param aFeedClasses  feed's classes (mask).
     *
     * @return TRUE if feed should be displayed.
     */
    private boolean shouldBeDisplayed(IFeed aFeed, int aFeedClasses)
    {
        // Show if
        //  * currently being processed, or
        //  * selected (showBeVisible == true), or
        //  * should be visible according to filter settings, or
        //  * is not initialized (just added) while valid and not disabled
        return shouldBeVisible(aFeed) || aFeed.isVisible();
    }

    /**
     * Returns <code>TRUE</code> if given feed should be visible.
     *
     * @param aFeed feed to check.
     *
     * @return <code>TRUE</code> if given feed should be visible.
     */
    private boolean shouldBeVisible(IFeed aFeed)
    {
        return aFeed == selectedFeed;
    }

    /**
     * Makes a copy of source feeds list. Sorts it if required using feeds scores.
     *
     * @param source        source list.
     * @param sourceLength  number of items to copy.
     * @param sort          TRUE to sort.
     * @param feedsScores   feeds' scores to use for sorting.
     *
     * @return resulting feeds list.
     */
    private int[] copyFeedsList(int[] source, int sourceLength, boolean sort,
        final int[] feedsScores)
    {
        int[] dest;

        if (sort)
        {
            FeedScoresComparator comp = getFeedScoresComparator();

            synchronized (comp)
            {
                comp.initialize(feedsScores);
                dest = Sort.sort(source, 0, sourceLength, comp);
            }
        } else
        {
            dest = new int[source.length];
            System.arraycopy(source, 0, dest, 0, sourceLength);
        }

        return dest;
    }

    /**
     * Returns feed scores comparator.
     *
     * @return comparator.
     */
    private synchronized FeedScoresComparator getFeedScoresComparator()
    {
        if (feedScoresComparator == null) feedScoresComparator = new FeedScoresComparator();

        return feedScoresComparator;
    }

    /**
     * Calculates classes for the feeds.
     *
     * @param aFeeds list of feeds.
     *
     * @return list of corresponding classes.
     */
    private int[] calculateClasses(IFeed[] aFeeds)
    {
        int[] classes = new int[aFeeds.length];
        for (int i = 0; i < aFeeds.length; i++) classes[i] = aFeeds[i].getClassesMask();
        return classes;
    }

    /**
     * Calculates scores for all feeds in the list with corresponding classes.
     *
     * @param aFeeds        list of feeds.
     * @param aFeedsClasses list of corresponding classes.
     *
     * @return list of scores for the feeds.
     */
    private int[] calculateScores(IFeed[] aFeeds, int[] aFeedsClasses)
    {
        int[] scores = new int[aFeeds.length];

        // Calculate minimum visits across the feeds if it's not provided
        // if one of sorting orders is by visits count
        int minVisits = 0;
        if (primarySort == FeedsSortOrder.VISITS || secondarySort == FeedsSortOrder.VISITS)
        {
            minVisits = getMininumVisits();
        }

        for (int i = 0; i < aFeeds.length; i++)
        {
            scores[i] = calculateScore(aFeeds[i], aFeedsClasses[i], minVisits);
        }

        return scores;
    }

    /**
     * Calculates score of the single feed.
     *
     * @param feed      feed (for score calculation).
     * @param feedClass class of feed.
     * @param minVisits minimum visits.
     *
     * @return final score ready for sorting.
     */
    int calculateScore(IFeed feed, int feedClass, int minVisits)
    {
        int score = 0;

        // Calculate minimum visits across the feeds if it's not provided
        // if one of sorting orders is by visits count
        if (minVisits == -1 && (primarySort == FeedsSortOrder.VISITS || secondarySort == FeedsSortOrder.VISITS))
        {
            minVisits = getMininumVisits();
        }

        // Shift it bits for primary and secondary sorting orders
        score = shiftInSortMark(score, feedClass, feed, primarySort, primarySortRev, minVisits);

        if (primarySort != secondarySort)
        {
            score = shiftInSortMark(score, feedClass, feed, secondarySort, secondarySortRev, minVisits);
        }

        // Order feeds by their rating additionally
        score = (score << 3) | (4 - getFeedRating(feed));

        // Shift in alphabetical order if only it was not among primary or secondary sorting orders
        if (primarySort != FeedsSortOrder.ALPHABETICAL &&
            secondarySort != FeedsSortOrder.ALPHABETICAL)
        {
            score = (score << 10) | (getFeedAlphaOrder(feed) & 0x7ff);
        }

        return score;
    }

    /**
     * Evaluates new bits for given sort order for given feed and its set of classes. After
     * the bits are ready it adds these bits to the <code>score</code> by pushing them from the
     * low end by shifting existing value to the left for necessary amount of bits. Depending
     * on <code>sortOrder</code> this method can request alphabetical order of feed and number
     * of keywords values.
     *
     * @param score         original score to update with new bits.
     * @param feedClass     class of the feed.
     * @param feed          target feed.
     * @param sortOrder     order of sorting.
     * @param reverseSort   <code>TRUE</code> to do reverse sorting.
     * @param minVisits     minimum number of visits across all feeds in this guide.
     *
     * @return updated score.
     */
    int shiftInSortMark(int score, int feedClass, IFeed feed, int sortOrder, boolean reverseSort, int minVisits)
    {
        int value;
        int shift;

        switch (sortOrder)
        {
            case FeedsSortOrder.ALPHABETICAL:
                value = getFeedAlphaOrder(feed) & 0x3ff;
                if (reverseSort) value = 1023 - value;
                shift = 10;
                break;
            case FeedsSortOrder.RATING:
                int feedRating = getFeedRating(feed);
                value = feedRating == -1 ? 0 : feedRating & 7;
                if (!reverseSort) value = 7 - value;
                shift = 3;
                break;
            case FeedsSortOrder.VISITS:
                value = getFeedVisits(feed, minVisits) & 0xff;
                if (!reverseSort) value = 255 - value;
                shift = 8;
                break;
            default:
                value = (feedClass & sortOrder) != 0 ? 1 : 0;
                if (reverseSort) value = 1 - value;
                shift = 1;
                break;
        }

        return (score << shift) | value;
    }

    /**
     * Returns compressed number of visits.
     *
     * @param feed      feed to check.
     * @param minVisits minimum number of visits across all feeds in the guide.
     *
     * @return visits number.
     */
    private int getFeedVisits(IFeed feed, int minVisits)
    {
        return Math.min(255, feed.getViews() - minVisits);
    }

    /**
     * Finds the minimum number of visits among feeds in this guide.
     *
     * @return minimum number of visits.
     */
    int getMininumVisits()
    {
        int min = Integer.MAX_VALUE;
        for (IFeed feed : feeds) min = Math.min(feed.getViews(), min);
        return min;
    }

    /**
     * Returns rating of the feed for further math.
     *
     * @param feed feed.
     *
     * @return rating in range [0;4].
     */
    int getFeedRating(IFeed feed)
    {
        return scoreCalculator.calcFinalScore(feed);
    }

    /**
     * Returns alphabetical order index of the feed within the currently selected guide.
     *
     * @param feed feed.
     *
     * @return order index in range [0;1023].
     */
    synchronized int getFeedAlphaOrder(IFeed feed)
    {
        return currentGuide.alphaIndexOf(feed);
    }

    /**
     * Makes sure that the specified feed is visible.
     *
     * @param aFeed feed to be visible.
     */
    public void ensureVisibilityOf(IFeed aFeed)
    {
        if (selectedFeed != aFeed)
        {
            selectedFeed = aFeed;
            if (selectedFeed == null)
            {
                if (getSize() > 0) fullRebuild();
            } else if (selectedFeed.belongsTo(currentGuide) && indexOf(selectedFeed) == -1)
            {
                fullRebuild();
            }
        }
    }

    /** Simple comparator which is using feeds scores table to compare elements by indeces. */
    private static class FeedScoresComparator implements Sort.IValueComparator
    {
        private int[] feedsScores;

        public int compare(int dataIndex1, int dataIndex2)
        {
            int s1 = feedsScores[dataIndex1];
            int s2 = feedsScores[dataIndex2];

            return s1 == s2 ? 0 : s1 > s2 ? 1 : -1;
        }

        public void initialize(int[] aFeedsScores)
        {
            feedsScores = aFeedsScores;
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Events
    // ---------------------------------------------------------------------------------------------

    /**
     * Intelligent firing of changes in the list. It preserves the item that was selected before
     * the change in list structure to save the selection. If the list has no previously selected
     * element any mode then more rude, but faster processing applied.
     *
     * @param oldLength         number of first elements in list to look at.
     * @param newLength         number of first elements in new list to look at.
     * @param currentViewIndex  index in <code>oldList</code> which was selected before the change.
     * @param newViewIndex      index in <code>newList</code> which should be selected after change.
     */
    private void fireChanges(int oldLength, int newLength, int currentViewIndex, int newViewIndex)
    {
        // Save time on empty calls
        if (oldLength == 0 && newLength == 0) return;

        // If previously selected item is in new list the apply full (intelligent) notification
        // scheme.
        if (newViewIndex != -1)
        {
            // Full scheme

            int changeStart = 0;
            int changeEnd = newLength - 1;

            int diffInPositions = newViewIndex - currentViewIndex;

            if (diffInPositions < 0)
            {
                fireIntervalRemoved(this, 0, -diffInPositions - 1);
            } else if (diffInPositions > 0)
            {
                fireIntervalAdded(this, 0, diffInPositions - 1);
                changeStart = diffInPositions;
            }

            oldLength += diffInPositions;

            int diffInLength = newLength - oldLength;

            if (diffInLength < 0)
            {
                fireIntervalRemoved(this, newLength, oldLength - 1);
            } else if (diffInLength > 0)
            {
                fireIntervalAdded(this, oldLength, newLength - 1);
                changeEnd = oldLength - 1;
            }

            fireContentsChanged(this, changeStart, changeEnd);

            // At this step we will have the list repainted and it's time to make focused index
            // be the same as selected index. 
            if (listComponent != null && newViewIndex != oldLength)
            {
                listComponent.setSelectedIndex(newViewIndex);
            }
        } else
        {
            // Simplified scheme

            // I understand that calling list component from model isn't good,
            // but it's the easiest way to clear selection. Alternatively we can
            // fire event that items removed and then add them back.
            if (listComponent != null) listComponent.clearSelection();

            fireChanges(0, oldLength, newLength);
        }
    }

    /**
     * Fires <code>intervalAdded</code> or <code>intervalRemoved</code> event with
     * possible <code>contentsChanged</code> event to cover whole list of changed
     * items from old to new list size.
     *
     * @param offset    offset to start events from.
     * @param aOldSize  old list size.
     * @param aNewSize  new list size.
     */
    private void fireChanges(int offset, int aOldSize, int aNewSize)
    {
        int o = aOldSize <= aNewSize ? aOldSize : aNewSize;
        int d = aNewSize - aOldSize;

        if (d > 0)
        {
            fireIntervalAdded(this, offset + o, offset + o + d - 1);
        } else if (d < 0)
        {
            fireIntervalRemoved(this, offset + o, offset + o - d - 1);
        }

        if (o > 0) fireContentsChanged(this, offset, offset + o - 1);
    }

    // ---------------------------------------------------------------------------------------------
    // Making sure that all events are fired from EDT
    // ---------------------------------------------------------------------------------------------

    /**
     * Fires change in contents range.
     *
     * @param source    source of event.
     * @param index0    start of range (including).
     * @param index1    end of range (excluding).
     */
    public void fireContentsChanged(Object source, int index0, int index1)
    {
        synchronized (this)
        {
            index0 = Math.min(index0, sortedChannelsCount);
            index1 = Math.min(index1, sortedChannelsCount);
        }
        
        super.fireContentsChanged(source, index0, index1);
    }

    protected void fireIntervalAdded(Object source, int index0, int index1)
    {
        super.fireIntervalAdded(source, index0, index1);
    }

    protected void fireIntervalRemoved(Object source, int index0, int index1)
    {
        super.fireIntervalRemoved(source, index0, index1);
    }
}
