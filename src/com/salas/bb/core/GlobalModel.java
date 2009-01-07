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
// $Id: GlobalModel.java,v 1.140 2008/04/08 08:06:18 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.domain.*;
import com.salas.bb.domain.events.FeedRemovedEvent;
import com.salas.bb.domain.prefs.StarzPreferences;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.domain.utils.DomainAdapter;
import com.salas.bb.domain.utils.DomainEventsListener;
import com.salas.bb.service.ServicePreferences;
import com.salas.bb.utils.dnd.DNDListContext;
import com.salas.bb.views.settings.DefaultFRS;
import com.salas.bb.views.settings.FeedRenderingSettings;
import com.salas.bb.views.settings.RenderingManager;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Root of the model of the whole application. The key state that we keep are the various channel
 * guides, which may be displayed in the main window. This is a singleton object. N.B. This class is
 * persisted as XML using XMLEncoder. XMLEncoder's default behavior is that it will write out all
 * bean properties. Therefore the part of this object's state which is available as a Bean Propertyu
 * is exactly what will be written out. This is subtle so be careful if you play with getXXX and
 * setXXX methods which is how by default you can tell what's a bean property and what is not.
 */
public class GlobalModel
{
    private static final Logger LOG = Logger.getLogger(GlobalModel.class.getName());

    /**
     * Return the one and only GlobalModel. Tricky: during the writing out of the state on disk a
     * copy of GlobalModel is temporarily created
     */
    public static GlobalModel               SINGLETON;

    private final UserPreferences           userPreferences;
    private final ServicePreferences        servicePreferences;
    private final StarzPreferences          starzPreferences;

    private final FeedRenderingSettings     globalRenderingSettings;

    private GuidesSet                       guidesSet;

    private IGuide                          selectedGuide;
    private IFeed                           selectedFeed;
    private IArticle                        selectedArticle;
    private IArticle[]                      selectedArticles;

    private final GuideModel                guideModel;
    
    /** A model for supporting computation of visible feeds. */
    private GuideModel                      vfHelperModel;

    private boolean                         blockChannelUpdated;

    // We use this info block to record the fact of selecteed feed change not passing feed change
    // event to the model. Doing this we avoid refiltering and resorting basing on new feed
    // state, which may result in change of feed position or visibility. These changes break
    // the human-expected navigation path and should be avoided while feed is selected. Later,
    // when user will select another feed this feed will be reported as changed to the model.
    private final FeedChangeInfo            selectedFeedChangeInfo;

    /** Listener of all domain events. */
    private final DomainListener            domainListener;

    /**
     * Holds the mapping between guide (key) and feed object.
     */
    private Map<IGuide, IFeed> selectedFeeds;

    /** View mode value model. */
    private ViewModeValueModel              viewModeValueModel;
    /** View type value model. */
    private ViewTypeValueModel              viewTypeValueModel;

    /**
     * Construct the global state. After construction, the GlobalModel has an empty ChannelGuideSet.
     * Note that this constructor is called either directly (new, when the state is not coming from
     * the persistent state) or via the XMLDecoder call in GlobalController.UnPersistAsXML which
     * instantiates a GlobalModel from its persistent state. In either case the GlobalModel.INSTANCE
     * static contains a reference to the GlobalModel object.
     *
     * @param scoresCalculator scores calculator to use.
     */
    public GlobalModel(ScoresCalculator scoresCalculator)
    {
        this(scoresCalculator, true);
    }

    /**
     * Construct the global state. After construction, the GlobalModel has an empty ChannelGuideSet.
     * Note that this constructor is called either directly (new, when the state is not coming from
     * the persistent state) or via the XMLDecoder call in GlobalController.UnPersistAsXML which
     * instantiates a GlobalModel from its persistent state. In either case the GlobalModel.INSTANCE
     * static contains a reference to the GlobalModel object.
     *
     * @param scoresCalculator  scores calculator to use.
     * @param full              TRUE to perform full initialization. FALSE to create only
     *                          this model object.
     */
    public GlobalModel(ScoresCalculator scoresCalculator, boolean full)
    {
        userPreferences = new UserPreferences();
        servicePreferences = new ServicePreferences();
        starzPreferences = new StarzPreferences();
        globalRenderingSettings = new FeedRenderingSettings();
        globalRenderingSettings.setParent(new DefaultFRS());

        if (full)
        {
            domainListener = new DomainListener();

            guideModel = new GuideModel(scoresCalculator, FeedDisplayModeManager.getInstance());
            vfHelperModel = new GuideModel(scoresCalculator, FeedDisplayModeManager.getInstance());

            RenderingManager.setGlobalSettings(globalRenderingSettings);

            selectedFeedChangeInfo = new FeedChangeInfo();
            selectedFeeds = new IdentityHashMap<IGuide, IFeed>();

            // When some property affecting visibility changes we
            // mark the selected feed info as changed to update its cell
            // when it gets unselected. It can potentially become invisible.
            userPreferences.addPropertyChangeListener(new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent evt)
                {
                    if (UserPreferences.FEED_VISIBILITY_PROPERTIES.contains(evt.getPropertyName()))
                    {
                        selectedFeedChangeInfo.registerChanged();
                    }
                }
            });
        } else
        {
            selectedFeedChangeInfo = null;
            guideModel = null;
            domainListener = null;
        }

        viewModeValueModel = new ViewModeValueModel(globalRenderingSettings);
        viewTypeValueModel = new ViewTypeValueModel();

        setGuidesSet(new GuidesSet());

        selectedGuide = null;
        blockChannelUpdated = false;
    }

    /**
     * Returns the view mode value model.
     *
     * @return view mode value mode.
     */
    public ViewModeValueModel getViewModeValueModel()
    {
        return viewModeValueModel;
    }

    /**
     * Returns the view type value model.
     *
     * @return view type value mode.
     */
    public ViewTypeValueModel getViewTypeValueModel()
    {
        return viewTypeValueModel;
    }

    /**
     * Returns current highlight calculator to be used.
     *
     * @return highlights calculator.
     */
    public HighlightsCalculator getHighlightsCalculator()
    {
        return GlobalController.SINGLETON.getHighlightsCalculator();
    }

    /**
     * Returns calculator of channel score.
     *
     * @return calculator.
     */
    public ScoresCalculator getScoreCalculator()
    {
        return GlobalController.SINGLETON.getScoreCalculator();
    }

    /**
     * Returns channels model.
     *
     * @return model.
     */
    public GuideModel getGuideModel()
    {
        return guideModel;
    }

    /**
     * Returns visible feeds helper mode.
     *
     * @param aGuide target guide.
     *
     * @return model.
     */
    private GuideModel getVFHelperModel(IGuide aGuide)
    {
        int starz = userPreferences.getGoodChannelStarz() - 1;
        int prSortOrder = userPreferences.getSortByClass1();
        int scSortOrder = userPreferences.getSortByClass2();
        boolean isPrSortOrderReversed = userPreferences.isReversedSortByClass1();
        boolean isScSortOrderReversed = userPreferences.isReversedSortByClass2();
        boolean isSortingEnabled = userPreferences.isSortingEnabled();

        vfHelperModel.setOptions(starz, prSortOrder, isPrSortOrderReversed,
            scSortOrder, isScSortOrderReversed, isSortingEnabled);
        vfHelperModel.setGuide(aGuide);

        return vfHelperModel;
    }

    /**
     * Returns <code>TRUE</code> if a feed is currently visible on the screen (in the selected guide).
     *
     * @param feed feed to check.
     *
     * @return <code>TRUE</code> if a feed is currently visible on the screen (in the selected guide).
     */
    public boolean isCurrentlyVisible(IFeed feed)
    {
        return guideModel.isPresent(feed);    
    }

    /**
     * After reading this in from persistent state, initialize parts of the state of GlobalModel
     * which do not get persisted.
     */
    public void initTransientState()
    {
    }

    /**
     * Update the Model with what guide is currently selected.
     *
     * @param guide new guide selection.
     */
    public void setSelectedGuide(final IGuide guide)
    {
        selectedGuide = guide;

        // EDT !!!
        if (guideModel != null) guideModel.setGuide(guide);

        if (guide != null)
        {
            selectedFeed = selectedFeeds.get(guide);
            if (selectedFeed == null && guideModel != null && guideModel.getSize() > 0)
            {
                selectedFeed = (IFeed)guideModel.getElementAt(0);
            }
        } else selectedFeed = null;
    }

    /**
     * Returns currently selected guide.
     *
     * @return selected guide.
     */
    public IGuide getSelectedGuide()
    {
        return selectedGuide;
    }

    /**
     * Returns selected article.
     *
     * @return selected article.
     */
    public IArticle getSelectedArticle()
    {
        return selectedArticle;
    }

    /**
     * Sets selected article.
     *
     * @param aArticle selected article.
     */
    public void setSelectedArticle(IArticle aArticle)
    {
        selectedArticle = aArticle;
    }

    /**
     * Returns the list of currently selected articles.
     *
     * @return articles.
     */
    public IArticle[] getSelectedArticles()
    {
        return selectedArticles;
    }

    /**
     * Sets selected articles.
     *
     * @param aSelectedArticles articles.
     */
    public void setSelectedArticles(IArticle[] aSelectedArticles)
    {
        selectedArticles = aSelectedArticles;
    }

    /**
     * Selects the feed.
     *
     * @param feed feed to select.
     */
    public void setSelectedFeed(final IFeed feed)
    {
        if (feed == null || isSelectable(feed))
        {
            selectedFeed = feed;
            if (feed != null)
            {
                IGuide selectedGuide = getSelectedGuide();
                selectedFeeds.put(selectedGuide, feed);
                feed.setLastVisitTime(System.currentTimeMillis());
                feed.setViews(feed.getViews() + 1);
            }

            viewModeValueModel.setFeed(feed);
            viewTypeValueModel.setFeed(feed);
        }

        firePreviousFeedChanged();

        synchronized (selectedFeedChangeInfo)
        {
            selectedFeedChangeInfo.reset(feed);
        }
    }

    private void firePreviousFeedChanged()
    {
        IFeed feed = null;

        synchronized (selectedFeedChangeInfo)
        {
            if (selectedFeedChangeInfo.changed)
            {
                feed = selectedFeedChangeInfo.feed;
            }
        }

        if (feed != null)
        {
            final IFeed feed1 = feed;
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    guideModel.contentsChangedAt(feed1);
                }
            });
        }
    }


    /**
     * Returns <code>true</code> if feed is selectable.
     *
     * @param feed feed to check.
     *
     * @return result of check.
     */
    public boolean isSelectable(final IFeed feed)
    {
        IGuide selectedGuide = getSelectedGuide();
        return feed == null ||
            (selectedGuide != null &&
             feed.belongsTo(selectedGuide) &&
             selectedGuide.indexOf(feed) != -1);
    }

    /**
     * Returns selected feed.
     *
     * @return selected feed.
     */
    public IFeed getSelectedFeed()
    {
        return selectedFeed;
    }

    /**
     * Sets channel guide set.
     *
     * @param set channel guide set.
     */
    public void setGuidesSet(final GuidesSet set)
    {
        // Connect model domain listener to the new set
        new DomainEventsListener(set).addDomainListener(domainListener);

        guidesSet = set;
    }

    /**
     * Returns the channel guide set.
     *
     * @return set.
     */
    public GuidesSet getGuidesSet()
    {
        return guidesSet;
    }

    /**
     * Sets new singleton.
     *
     * @param model new singleton.
     */
    public static void setSINGLETON(final GlobalModel model)
    {
        SINGLETON = model;
    }

    /**
     * Called to clean up model related resources, kill processes and save XML state just before
     * exiting.
     */
    public void prepareForApplicationExit()
    {
    }

    /**
     * Returns user preferences object.
     *
     * @return the user preferences object.
     */
    public UserPreferences getUserPreferences()
    {
        return userPreferences;
    }

    /**
     * Returns service preferences.
     *
     * @return preferences.
     */
    public ServicePreferences getServicePreferences()
    {
        return servicePreferences;
    }

    /**
     * Returns global rendering settings.
     *
     * @return global rendering settings.
     */
    public FeedRenderingSettings getGlobalRenderingSettings()
    {
        return globalRenderingSettings;
    }

    /**
     * Returns Starz service preferences.
     *
     * @return preferences object.
     */
    public StarzPreferences getStarzPreferences()
    {
        return starzPreferences;
    }

    /**
     * Notifies model about change in selected feed if containing guide is currently displayed.
     *
     * @param feed   changed feed.
     */
    public void feedUpdated(IFeed feed)
    {
        if (blockChannelUpdated) return;

        if (LOG.isLoggable(Level.FINE)) LOG.fine("Feed updated " + feed);

        if (selectedFeedChangeInfo.feed == feed)
        {
            selectedFeedChangeInfo.registerChanged();

            SwingUtilities.invokeLater(new FireFeedUpdated(feed, true));
        } else
        {
            SwingUtilities.invokeLater(new FireFeedUpdated(feed, false));
        }
    }

    /**
     * Called when the initial loading of the feeds from database starts.
     */
    protected void loadingStarted()
    {
        blockChannelUpdated = true;
    }

    /**
     * Called when the initial loading of the feeds finishes.
     */
    protected void loadingFinished()
    {
        if (blockChannelUpdated)
        {
            blockChannelUpdated = false;

            // Doing update of model from within EDT
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    getHighlightsCalculator().invalidateAll();
                    getScoreCalculator().invalidateAll();

                    guideModel.fullRebuild();
                }
            });
        }
    }
    
    /**
     * Given a guide, return a list of feeds which would be displayed
     * in that guide given current filter settings and options.
     * Uses the existing guide model if it corresponds to the requested guide,
     * otherwise creates a temporary GuideModel instance. 
     * @param guide Guide to look in.
     * @return array of visible feeds for that guide.
     */
    public IFeed[] getVisibleFeeds(IGuide guide)
    {
        GuideModel model = (guide == selectedGuide) ? guideModel : getVFHelperModel(guide);

        // !!! EDT
        int numFeeds = model.getSize();
        IFeed[] retFeeds = new IFeed[numFeeds];

        for (int i = 0; i < numFeeds; ++i) retFeeds[i] = (IFeed) model.getElementAt(i);

        return retFeeds;
    }

    /**
     * Returns the number of unread articles in visible data feeds only within the given guide.
     *
     * @param aGuide guide.
     *
     * @return number of unread articles.
     */
    public int getUnreadArticlesCount(IGuide aGuide)
    {
        int count = 0;
        GuideModel mdl = getVFHelperModel(aGuide);
        for (int i = 0; i < mdl.getSize(); i++)
        {
            Object item = mdl.getElementAt(i);
            if (item instanceof DataFeed)
            {
                count += ((DataFeed)item).getUnreadArticlesCount();
            }
        }

        return count;
    }

    /**
     * Updates the last update time of preferences.
     */
    public static void touchPreferences()
    {
        SINGLETON.getUserPreferences().setLastUpdateTime(new Date());
    }

    /** Feed changes information block. Used to store information about selected feed changes. */
    private static class FeedChangeInfo
    {
        private IFeed   feed;
        private boolean changed;

        /**
         * Resets the information and sets new feed.
         *
         * @param aFeed new feed.
         */
        public synchronized void reset(IFeed aFeed)
        {
            feed = aFeed;
            changed = false;
        }

        /**
         * Register change of the feed.
         */
        public synchronized void registerChanged()
        {
            changed = true;
        }
    }

    // ------------------------------------------------------------------------
    // Preferences
    // ------------------------------------------------------------------------

    /**
     * Stores preferences in the object.
     *
     * @param prefs preference storage.
     */
    public void storePreferences(Preferences prefs)
    {
        userPreferences.storeIn(prefs);
        servicePreferences.storeIn(prefs);
        globalRenderingSettings.storeIn(prefs);
        starzPreferences.storeIn(prefs);
    }

    /**
     * Restores preferences.
     *
     * @param prefs preference storage.
     */
    public void restorePreferences(Preferences prefs)
    {
        userPreferences.restoreFrom(prefs);
        servicePreferences.restoreFrom(prefs);
        globalRenderingSettings.restoreFrom(prefs);
        starzPreferences.restoreFrom(prefs);
    }

    // ------------------------------------------------------------------------
    // Helper classes
    // ------------------------------------------------------------------------

    /** Fires feed update event. */
    private class FireFeedUpdated implements Runnable
    {
        private final IFeed feed;
        private final boolean fireEvent;

        public FireFeedUpdated(IFeed aFeed, boolean event)
        {
            feed = aFeed;
            fireEvent = event;
        }

        public void run()
        {
            if (fireEvent)
            {
                synchronized (guideModel)
                {
                    int index = guideModel.indexOf(feed);
                    if (index > -1) guideModel.fireContentsChanged(guideModel, index, index);
                }

                GlobalController.SINGLETON.getMainFrame().updateTitle(feed);
            } else
            {
                guideModel.contentsChangedAt(feed);
            }
        }
    }

    /**
     * Listener of domain events.
     */
    private class DomainListener extends DomainAdapter
    {
        private static final String THREAD_NAME_ARTICLE_SPECIAL_FUNCTIONS = "Article Special Functions";
        private static final String THREAD_NAME_UPDATE_SCORES = "Update Scores";

        /**
         * Invoked when new guide has been added to the set.
         *
         * @param set           guides set.
         * @param guide         added guide.
         * @param lastInBatch   <code>TRUE</code> when this is the last even in batch.
         */
        public void guideAdded(GuidesSet set, IGuide guide, boolean lastInBatch)
        {
            synchronized (guide)
            {
                int count = guide.getFeedsCount();
                for (int i = 0; i < count; i++) feedAdded(guide, guide.getFeedAt(i));
            }
        }

        /**
         * Invoked when the guide has been removed from the set.
         *
         * @param set   guides set.
         * @param guide removed guide.
         * @param index old guide index.
         */
        public void guideRemoved(GuidesSet set, IGuide guide, int index)
        {
            synchronized (guide)
            {
                int count = guide.getFeedsCount();
                for (int i = 0; i < count; i++) feedRemovedCommon(guide, guide.getFeedAt(i));
            }

            // Release the guide being deleted from Visible Feeds helper model
            selectedFeeds.remove(guide);
            if (vfHelperModel != null && vfHelperModel.getCurrentGuide() == guide)
            {
                vfHelperModel.setGuide(null);
            }

            // Release the guide being deleted from the DND list context
            if (DNDListContext.getDestination() == guide) DNDListContext.setDestination(null);
        }

        // -----------------------------------------------------------------------------------------
        // Feeds Stuff
        // -----------------------------------------------------------------------------------------

        /**
         * Invoked when new feed has been added to the guide.
         *
         * @param guide parent guide.
         * @param feed  added feed.
         */
        public void feedAdded(IGuide guide, IFeed feed)
        {
            IArticle[] articles = feed.getArticles();
            for (IArticle article : articles) articleAdded(feed, article);

            if (selectedGuide == guide)
            {
                final int index = guide.indexOf(feed);
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        guideModel.feedsAdded(index, index);
                    }
                });
            }
        }

        /**
         * Invoked when the feed has been removed from the guide.
         *
         * @param event feed removal event.
         */
        public void feedRemoved(FeedRemovedEvent event)
        {
            IGuide guide = event.getGuide();
            final IFeed feed = event.getFeed();

            // Do common processing
            feedRemovedCommon(guide, feed);

            if (selectedGuide == guide && event.isLastEvent())
            {
                final int visibleIndex = guideModel.indexOf(feed);

                // Recalculation of model should happen in EDT
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        guideModel.fullRebuild();

                        if (selectedFeed == feed)
                        {
                            IFeed feedToSelect = null;
                            int count = guideModel.getSize();
                            if (visibleIndex < count && count > 0)
                            {
                                feedToSelect = (IFeed)guideModel.getElementAt(visibleIndex == -1
                                    ? 0 : visibleIndex);
                            } else if (count > 0)
                            {
                                feedToSelect = (IFeed)guideModel.getElementAt(count - 1);
                            }

                            GlobalController.SINGLETON.selectFeed(feedToSelect);
                        }
                    }
                });
            }
        }

        /**
         * Invoked when a feed is moved from one position to another.
         *
         * @param guide       source guide.
         * @param feed        feed moved.
         * @param oldPosition old position.
         * @param newPosition new position.
         */
        public void feedRepositioned(final IGuide guide, final IFeed feed, int oldPosition, int newPosition)
        {
            if (guideModel.getCurrentGuide() == guide)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        // We reselect guide to let GuideModel reload the list
                        // of feeds that is cached at the moment.
                        guideModel.setGuide(guide);
                    }
                });
            }
        }

        /**
         * Common handling of removed feed event. Unregisteres listener from feed,
         * removes the associated selection key and clears the scores.
         *
         * @param guide guide the feed was removed from.
         * @param feed removed feed.
         */
        void feedRemovedCommon(IGuide guide, IFeed feed)
        {
            // Remove selected feed if it's equal to the one we just removed
            IFeed selectedFeed = selectedFeeds.get(guide);
            if (selectedFeed == feed) selectedFeeds.remove(guide);

            // Remove highlights only if this feed is not assigned anywhere else
            if (feed.getParentGuides().length == 0)
            {
                getHighlightsCalculator().feedRemoved(feed);
                getScoreCalculator().feedRemoved(feed);
            }
        }

        /**
         * Special functions for new articles.
         *
         * @param article new article.
         */
        private void newArticleSpecialFunctions(final IArticle article)
        {
            final DataFeed feed = (DataFeed)article.getFeed();
            final boolean autoDiscovery = feed.isAutoFeedsDiscovery();

            // Span new thread only if at least one of the functions is required.
            if (autoDiscovery)
            {
                Thread thread = new Thread(THREAD_NAME_ARTICLE_SPECIAL_FUNCTIONS)
                {
                    public void run()
                    {
                        // Discover links in new articles
                        if (autoDiscovery)
                        {
                            GlobalController.SINGLETON.discoverFeedsIn(article, feed);
                        }
                    }
                };

                thread.start();
            }
        }

        /**
         * Updates the scores (highlights and overall) of the feed.
         *
         * @param feed feed which scores to update.
         * @param updateHighlights TRUE to update highlights.
         */
        private void updateScores(final IFeed feed, final boolean updateHighlights)
        {
            Thread thread = new Thread(THREAD_NAME_UPDATE_SCORES)
            {
                public void run()
                {
                    // Submit invalidation request
                    HighlightsCalculator hcalc = null;
                    if (updateHighlights)
                    {
                        hcalc = getHighlightsCalculator();
                        hcalc.invalidateFeed(feed);
                    }

                    ScoresCalculator scalc = getScoreCalculator();
                    scalc.invalidateFeed(feed);

                    // Wait for calc finish
                    if (updateHighlights) hcalc.getHighlightsCount(feed);
                    if (feed instanceof DirectFeed) scalc.calcBlogStarzScore(feed);

                    // Repaint feed
                    feedUpdated(feed);
                }
            };

            thread.start();
        }

        /**
         * Called when information in feed changed.
         *
         * @param feed     feed.
         * @param property property of the feed.
         * @param oldValue old property value.
         * @param newValue new property value.
         */
        public void propertyChanged(IFeed feed, String property, Object oldValue, Object newValue)
        {
            if (!feed.belongsTo(selectedGuide)) return;

            boolean repaintNecessary = IFeed.PROP_TITLE.equals(property) ||
                DirectFeed.PROP_RATING.equals(property) ||
                DirectFeed.PROP_DISABLED.equals(property) ||
                IFeed.PROP_UNREAD_ARTICLES_COUNT.equals(property) ||
                IFeed.PROP_PROCESSING.equals(property) ||
                IFeed.PROP_INVALIDNESS_REASON.equals(property) ||
                SearchFeed.PROP_QUERY.equals(property) ||
                SearchFeed.PROP_ARTICLES_LIMIT.equals(property);

            boolean updateScore = false;
            boolean updateHighlights = false;
            if (IFeed.PROP_PROCESSING.equals(property) && !(Boolean)newValue)
            {
                // processing finished -- invalidate feed
                updateScore = true;
                updateHighlights = true;
            } else if (IFeed.PROP_CLICKTHROUGHS.equals(property))
            {
                // the number of clickthroughs has changed
                if (ScoresCalculator.registerMaxClickthroughs(feed.getClickthroughs()))
                    updateFeedsWithNonZeroClickthroughs();
                else updateScore = true;
            } else if (IFeed.PROP_VIEWS.equals(property))
            {
                // the number of views has changed
                if (ScoresCalculator.registerMaxFeedViews(feed.getViews()))
                    updateFeedsWithNonZeroViews();
                else updateScore = true;
            }

            if (updateScore) updateScores(feed, updateHighlights);
            if (repaintNecessary) feedUpdated(feed);
        }

        private void updateFeedsWithNonZeroViews()
        {
            ScoresCalculator sc = getScoreCalculator();
            List<IFeed> feeds = getGuidesSet().getFeeds();
            for (IFeed feed : feeds)
            {
                if (feed.getViews() != 0)
                {
                    sc.invalidateFeed(feed);
                    feedUpdated(feed);
                }
            }
        }

        private void updateFeedsWithNonZeroClickthroughs()
        {
            ScoresCalculator sc = getScoreCalculator();
            List<IFeed> feeds = getGuidesSet().getFeeds();
            for (IFeed feed : feeds)
            {
                if (feed.getClickthroughs() != 0)
                {
                    sc.invalidateFeed(feed);
                    feedUpdated(feed);
                }
            }
        }

        // -----------------------------------------------------------------------------------------
        // Articles
        // -----------------------------------------------------------------------------------------

        /**
         * Called when some article is added to the feed.
         *
         * @param feed    feed.
         * @param article article.
         */
        public void articleAdded(final IFeed feed, final IArticle article)
        {
            if (GlobalController.SINGLETON.isInitializationFinished() && feed instanceof DataFeed)
                newArticleSpecialFunctions(article);
        }
    }
}
