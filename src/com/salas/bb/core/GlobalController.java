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
// $Id: GlobalController.java,v 1.389 2008/04/09 04:34:38 spyromus Exp $
//

package com.salas.bb.core;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.uif.application.Application;
import com.jgoodies.uif.application.ApplicationAdapter;
import com.jgoodies.uif.application.ApplicationEvent;
import com.jgoodies.uif.util.ResourceUtils;
import com.jgoodies.uif.util.SystemUtils;
import com.salas.bb.core.actions.feed.FeedLinkPostToBlogAction;
import com.salas.bb.core.actions.guide.SubscribeToReadingListAction;
import com.salas.bb.core.autosave.AutoSaver;
import com.salas.bb.dialogs.*;
import com.salas.bb.discovery.*;
import com.salas.bb.discovery.filter.CompositeURLFilter;
import com.salas.bb.discovery.filter.DynamicExtensionURLFilter;
import com.salas.bb.discovery.filter.ExtensionURLFilter;
import com.salas.bb.domain.*;
import com.salas.bb.domain.prefs.StarzPreferences;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.domain.query.ICriteria;
import com.salas.bb.domain.query.articles.ArticleTextProperty;
import com.salas.bb.domain.query.articles.Query;
import com.salas.bb.domain.query.general.StringContainsCO;
import com.salas.bb.domain.querytypes.QueryType;
import com.salas.bb.domain.utils.DomainEventsListener;
import com.salas.bb.domain.utils.GuidesUtils;
import com.salas.bb.domain.utils.IDomainListener;
import com.salas.bb.imageblocker.ImageBlocker;
import com.salas.bb.networking.manager.NetManager;
import com.salas.bb.persistence.ChangesMonitor;
import com.salas.bb.persistence.IPersistenceManager;
import com.salas.bb.persistence.PersistenceManagerConfig;
import com.salas.bb.plugins.Manager;
import com.salas.bb.plugins.domain.AdvancedPreferencesPlugin;
import com.salas.bb.plugins.domain.IPlugin;
import com.salas.bb.plugins.domain.Package;
import com.salas.bb.remixfeeds.PostToBlogAction;
import com.salas.bb.search.SearchEngine;
import com.salas.bb.sentiments.ArticleFilterProtector;
import com.salas.bb.sentiments.DomainListener;
import com.salas.bb.sentiments.SentimentsConfig;
import com.salas.bb.service.ServerService;
import com.salas.bb.service.ServicePreferences;
import com.salas.bb.service.sync.SyncFull;
import com.salas.bb.service.sync.SyncFullAction;
import com.salas.bb.service.sync.SyncOut;
import com.salas.bb.tags.TagsRepository;
import com.salas.bb.tags.TagsSaver;
import com.salas.bb.tags.net.*;
import com.salas.bb.updates.FullCheckCycle;
import com.salas.bb.utils.*;
import com.salas.bb.utils.discovery.DiscoveryResult;
import com.salas.bb.utils.discovery.UrlDiscovererException;
import com.salas.bb.utils.discovery.detector.XMLFormat;
import com.salas.bb.utils.discovery.detector.XMLFormatDetector;
import com.salas.bb.utils.discovery.impl.DirectDiscoverer;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.ipc.IIPCListener;
import com.salas.bb.utils.notification.NotificationArea;
import com.salas.bb.utils.poller.Poller;
import com.salas.bb.utils.uif.UifUtilities;
import com.salas.bb.utils.uif.images.ImageFetcher;
import com.salas.bb.views.*;
import com.salas.bb.views.feeds.IFeedDisplay;
import com.salas.bb.views.mainframe.MainFrame;
import com.salas.bb.views.mainframe.UnreadButton;
import com.salas.bb.views.stylesheets.StylesheetManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Implements all the behaviors of commands in the app. A key design question is whether the
 * selection is part of the model or of the controller. I concluded that to have the stickyness of
 * selections in channels, and items, it should be part of the model.
 */
public final class GlobalController implements IIPCListener
{
    private static final Logger LOG = Logger.getLogger(GlobalController.class.getName());

    private static final String AUTO_GUIDE_TITLE = Strings.message("automatically.created.guide.title");

    /** Key for preference to store selected guide ID between application runs. */
    private static final String KEY_SELECTED_GUIDE_ID = "selectedGuideId";
    /** Key for preference to store selected feed ID between application runs. */
    private static final String KEY_SELECTED_FEED_ID = "selectedFeedId";

    /** Number of last backups to keep in backups directory. */
    private static final int LAST_BACKUPS_TO_KEEP = 10;

    private static final String THREAD_NAME_SEARCH_QUERY = "Run Search Feed Query";

    /**
     * Minimum number of feeds to have saved during previous sync-out to start looking for
     * suspicious decreases.
     */
    static final int CHANGE_CHECK_FEEDS_THRESHOLD = 50;
    /**
     * Number of times the current feeds count should be less than the previous to
     * sound the alarm.
     */
    static final int CHANGE_CHECK_DIFF_TIMES = 3;

    /**
     * Singleton instance.
     */
    public static final GlobalController SINGLETON = new GlobalController();

    private List<IControllerListener>   listeners = new CopyOnWriteArrayList<IControllerListener>();
    private boolean                     initializationFinished = false;

    private GlobalModel                 model;
    private MDManager                   metaDataManager;
    private MDUpdater                   metaDataUpdater;

    // Navigation component.
    private NavigatorAdv                navigator;
    private NavigatorAdapter            navigatorAdapter;

    private ScoresCalculator            scoresCalculator;
    private HighlightsCalculator        highlightsCalculator;

    private PropertyChangeDispatcher    propertyChangeDispatcher;
    private BackgroundProccessManager   backManager;

    private GuideModel                  navigationModel;

    private MainFrame                   mainFrame;
    private SelectedFeedListener        selectedFeedListener;
    private DeletedObjectsRepository deletedObjectsRepository;

    private SearchFeedsManager          searchFeedsManager;
    private DomainEventsListener        domainEventsListener;

    private HighlightsCalculator        searchHighlightsCalculator;
    private String                      currentSearchKeywords;

    /** Tags manager controls the viewing and editing the tags of taggable objects. */
    private TagsSaver                   tagsSaver;
    private WrappingStorage             tagsStorage;
    private Poller                      poller;

    private URL                         hoveredLink;
    private SearchEngine                searchEngine;

    private EventsNotifier              eventNotifier;
    DockIconUnreadMonitor               dockIconUnreadMonitor;
    private FeatureManager              featureManager;

    private GuidesListModel             guidesListModel;
    private PinTagger                   pinTagger;
    private AutoSaver autoSaver;

    /** A link that should be highlighted in an article when found. */
    private String                      highlightedArticleLink;

    /**
     * Constructor of the GlobalController. Note that this is called early in the launch of the
     * application, before any of the UI has been built. Do not call any UI related methods here
     * because they will fail.
     */
    private GlobalController()
    {
        if (LOG.isLoggable(Level.FINE)) LOG.fine("Constructing GlobalController");

        hoveredLink = null;
        AbstractFeed.setFeedVisibilityResolver(new IFeedVisibilityResolver()
        {
            public boolean isVisible(IFeed feed)
            {
                return feed != null &&
                    FeedDisplayModeManager.getInstance().isVisible(feed.getClassesMask());
            }
        });

        featureManager = new FeatureManager(Application.getUserPreferences());

        if (NotificationArea.isSupported())
        {
            eventNotifier = new EventsNotifier();
            eventNotifier.setSoundResourceID("sound.new.articles");
        }
        searchEngine = new SearchEngine();
        backManager = new BackgroundProccessManager();

        pinTagger = new PinTagger(this);
        autoSaver = new AutoSaver();

        if (SystemUtils.IS_OS_MAC) dockIconUnreadMonitor = new DockIconUnreadMonitor();
        selectedFeedListener = new SelectedFeedListener();
        deletedObjectsRepository = new DeletedObjectsRepository(PersistenceManagerConfig.getManager());

        searchHighlightsCalculator = new HighlightsCalculator();
        currentSearchKeywords = "";

        highlightsCalculator = new HighlightsCalculator();
        scoresCalculator = new ScoresCalculator();

        guidesListModel = new GuidesListModel();

        navigationModel = new GuideModel(scoresCalculator, false,
            FeedDisplayModeManager.getInstance());

        navigator = new NavigatorAdv(navigationModel, guidesListModel);
        addControllerListener(navigator);
        navigatorAdapter = new NavigatorAdapter();

        propertyChangeDispatcher = new PropertyChangeDispatcher(this);

        setupTagsSupport();
        setupMetaDataSupport();
        setupPolling();

        // register markers
        addControllerListener(ArticleMarker.getInstance());

        setModel(new GlobalModel(scoresCalculator));
        GlobalModel.setSINGLETON(model);

        // Add sentiments domain listener
        addDomainListener(new DomainListener());
    }

    /**
     * Returns the guides list model.
     *
     * @return model.
     */
    public GuidesListModel getGuidesListModel()
    {
        return guidesListModel;
    }

    /**
     * Returns current feature manager.
     *
     * @return manager.
     */
    public FeatureManager getFeatureManager()
    {
        return featureManager;
    }

    /** Configures and schedules stylesheets updater. */
    private void setupStylesUpdater()
    {
        backManager.schedule(StylesheetManager.getUpdater(), 20, 3600);
    }

    /**
     * Returns current deleted feeds repository instance.
     *
     * @return instance.
     */
    public DeletedObjectsRepository getDeletedFeedsRepository()
    {
        return deletedObjectsRepository;
    }

    /**
     * Reselect currently selected feed in the midnight to let it regroup the articles.
     */
    private void setupFeedReselector()
    {
        long delayToTomorrow = DateUtils.getTomorrowTime() - System.currentTimeMillis();
        // Add a second to be sure that tomorrow has come
        delayToTomorrow += 1000L;

        // Call this code every midnight
        backManager.schedule(new Runnable()
        {
            public void run()
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        IFeed selFeed = getModel().getSelectedFeed();
                        if (selFeed != null)
                        {
                            selectFeed(null);
                            selectFeed(selFeed, true);
                        }
                    }
                });
            }
        }, delayToTomorrow / Constants.MILLIS_IN_SECOND, Constants.SECONDS_IN_DAY);
    }

    /** Configures polling. */
    private void setupPolling()
    {
        ConnectionState connectionState = getConnectionState();
        poller = new Poller(connectionState);
    }

    /**
     * Returns current connection state object.
     *
     * @return connection state object.
     */
    public static ConnectionState getConnectionState()
    {
        return ApplicationLauncher.getConnectionState();
    }

    /** Configures meta-data support. */
    private void setupMetaDataSupport()
    {
        ConnectionState connectionState = getConnectionState();

        metaDataManager = new MDManager(connectionState);
        metaDataManager.addDiscoveryListener(new DiscoveryListener());
        metaDataUpdater = new MDUpdater(metaDataManager, connectionState);
    }

    /** Configures and installs tags support. */
    private void setupTagsSupport()
    {
        tagsStorage = new WrappingStorage(new EmptyStorage());
        tagsSaver = new TagsSaver(tagsStorage);
    }

    /**
     * Changes storage to a new type.
     *
     * @param aNewType new type.
     */
    public void changeTagsStorage(int aNewType)
    {
        ITagsStorage storage;

        switch (aNewType)
        {
            case UserPreferences.TAGS_STORAGE_DELICIOUS:
                storage = new DeliciousStorage(new UserPreferencesCallback());
                break;
            case UserPreferences.TAGS_STORAGE_BB_SERVICE:
                storage = new BBServiceStorage(new BBServiceCredentialCallback());
                break;
            default:
                storage = new EmptyStorage();
                break;
        }

        tagsStorage.setCurrentStorage(storage);
    }

    /**
     * Returns currently selected tags networker component.
     *
     * @return networker component.
     */
    public ITagsStorage getTagsStorage()
    {
        return tagsStorage;
    }

    /**
     * Sets new model to control.
     *
     * @param aModel model.
     */
    private void setModel(GlobalModel aModel)
    {
        if (model != null) uninstallModel();

        model = aModel;

        if (model != null) installModel();
    }

    /**
     * Registers application main frame.
     *
     * @param aMainFrame    main frame object.
     */
    public void setMainFrame(MainFrame aMainFrame)
    {
        if (mainFrame == aMainFrame) return;

        mainFrame = aMainFrame;

        if (eventNotifier != null) eventNotifier.setFrame(aMainFrame);

        mainFrame.setMinimizeToSystemTray(model.getUserPreferences().isMinimizeToSystray());
        
        // Search functionality setup
//        mainFrame.getSearchField().setAppIconActionListener(new SearchFieldListener());
//        mainFrame.setSearchResult(searchEngine.getResult());
    }

    /** Installs new model. */
    private void installModel()
    {
        final GuidesSet guidesSet = model.getGuidesSet();
        final StarzPreferences starzPreferences = model.getStarzPreferences();
        final UserPreferences userPreferences = model.getUserPreferences();

        userPreferences.addPropertyChangeListener(propertyChangeDispatcher);
        starzPreferences.addPropertyChangeListener(propertyChangeDispatcher);

        scoresCalculator.loadPreferences(starzPreferences);

        ArticleFilterProtector.init();
        
        navigator.setViewModel(model.getGuideModel());
        navigator.guideSelected(model.getSelectedGuide());
        navigator.feedSelected(model.getSelectedFeed());
        navigator.setGuidesSet(guidesSet);

        domainEventsListener = new DomainEventsListener(guidesSet);
        searchFeedsManager = new SearchFeedsManager(guidesSet);
        domainEventsListener.addDomainListener(searchFeedsManager);
        if (eventNotifier != null) domainEventsListener.addDomainListener(eventNotifier);
        domainEventsListener.addDomainListener(deletedObjectsRepository);
        if (dockIconUnreadMonitor != null)
        {
            dockIconUnreadMonitor.setSet(guidesSet);
            addControllerListener(dockIconUnreadMonitor.getMonitor());
            domainEventsListener.addDomainListener(dockIconUnreadMonitor);
            userPreferences.addPropertyChangeListener(dockIconUnreadMonitor);
            FeedDisplayModeManager.getInstance().addListener(dockIconUnreadMonitor);
        }

        guidesListModel.setGuidesSet(guidesSet);
        addDomainListener(guidesListModel.getDomainListener());
        userPreferences.addPropertyChangeListener(guidesListModel.getUserPreferencesListener());
        addControllerListener(guidesListModel.getControllerListener());

        pinTagger.setUserPreferences(userPreferences);

        // This listener should go after the searchFeedsManager
        addDomainListener(autoSaver);

        tagsSaver.setGuidesSet(guidesSet);
        changeTagsStorage(userPreferences.getTagsStorage());

        metaDataUpdater.setGuidesSet(guidesSet);

        poller.setGuidesSet(guidesSet);
        poller.update();

        searchEngine.setGuidesSet(guidesSet);

        if (eventNotifier != null) eventNotifier.setUserPreferences(userPreferences);

        // Setup URL filter
        CompositeURLFilter urlFilter = new CompositeURLFilter();
        urlFilter.addFilter(new ExtensionURLFilter(ResourceUtils.getString(ResourceID.NO_DISCOVERY_EXTENSIONS)));
        urlFilter.addFilter(new DynamicExtensionURLFilter(model.getUserPreferences(),
            UserPreferences.PROP_NO_DISCOVERY_EXTENSIONS));
        MDDiscoveryLogic.setURLFilter(urlFilter);

        featureManager.setServicePreferences(model.getServicePreferences());
    }

    /** Uninstalls the model. */
    private void uninstallModel()
    {
        final StarzPreferences starzPreferences = model.getStarzPreferences();
        final UserPreferences userPreferences = model.getUserPreferences();

        starzPreferences.removePropertyChangeListener(propertyChangeDispatcher);
        userPreferences.removePropertyChangeListener(propertyChangeDispatcher);

        navigator.setViewModel(null);
        navigator.setGuidesSet(null);
        navigator.guideSelected(null);
        navigator.feedSelected(null);

        tagsSaver.setGuidesSet(null);
        searchEngine.setGuidesSet(null);

        guidesListModel.setGuidesSet(null);

        // Reset URL filter
        MDDiscoveryLogic.setURLFilter(null);
    }

    /**
     * Returns current model.
     *
     * @return model.
     */
    public GlobalModel getModel()
    {
        return model;
    }

    /**
     * Returns the search engine.
     *
     * @return engine.
     */
    public SearchEngine getSearchEngine()
    {
        return searchEngine;
    }

    /**
     * Returns navigation listener.
     *
     * @return navigation listener.
     */
    public IArticleListNavigationListener getNavigationListener()
    {
        return navigatorAdapter;
    }

    /**
     * Returns navigator guide model.
     *
     * @return navigator model.
     */
    GuideModel getNavigationModel()
    {
        return navigationModel;
    }

    /**
     * Returns the background process manager.
     *
     * @return background manager.
     */
    public BackgroundProccessManager getBackgroundProccessManager()
    {
        return backManager;
    }

    /**
     * Returns a search feeds manager.
     *
     * @return manager.
     */
    public static SearchFeedsManager getSearchFeedsManager()
    {
        return SINGLETON.searchFeedsManager;
    }

    /**
     * Changes current guide selection.
     *
     * @param guide new guide to select.
     */
    public void selectGuideAndFeed(final IGuide guide)
    {
        selectGuide(guide, true);
    }

    /**
     * Changes current guide selection.
     *
     * @param guide         new guide to select.
     * @param selectFeed    TRUE to select currently selected feed.
     */
    public void selectGuide(final IGuide guide, final boolean selectFeed)
    {
        final boolean alreadySelected = model.getSelectedGuide() == guide;
        if (!isInitializationFinished() || !alreadySelected)
        {
            if (LOG.isLoggable(Level.FINE))
            {
                LOG.fine("selectGuide: " + (guide == null ? "null" : guide.getTitle()));
            }

            SelectGuideTask task = new SelectGuideTask(guide, selectFeed, alreadySelected);
            if (UifUtilities.isEDT()) task.run(); else SwingUtilities.invokeLater(task);
        }
    }

    /**
     * Selects the feed.
     *
     * @param feed feed to select.
     */
    public void selectFeed(IFeed feed)
    {
        // EDT !!!!
        selectFeed(feed, false);
    }

    /**
     * Selects the feed.
     *
     * @param feed          feed to select.
     * @param selectHidden  <code>TRUE</code> to allow hidden feeds selection.
     */
    public void selectFeed(IFeed feed, boolean selectHidden)
    {
        // EDT !!!!
        if (LOG.isLoggable(Level.FINE)) LOG.fine("selectFeed: " + feed);

        highlightedArticleLink = null;

        // Unregister our listener from selected feed
        IFeed selectedFeed = model.getSelectedFeed();
        if (selectedFeed != null) selectedFeed.removeListener(selectedFeedListener);

        GuideModel guideModel = model.getGuideModel();

        // Allow selection only if
        // (a) feed belongs to current guide, and
        // (b) select hidden feeds mode is on or feed is visible
        if (!model.isSelectable(feed) ||
            (!selectHidden && guideModel.indexOf(feed) == -1)) feed = null;

        // Register the listener to get events about articles manipulations and other feed changes
        if (feed != null) feed.addListener(selectedFeedListener);

        // We have to have this to ensure that even if we have "select hidden" mode disabled
        // we can have currently selected feed preserved from disappearing on starz filter changes.
        navigationModel.ensureVisibilityOf(feed);
        guideModel.ensureVisibilityOf(feed);

        // We need to select feed first order to release selection from previously selected feed
        // *before* calling the model change. Otherwise it will reflect in another loop and
        // incorrect selection.
        if (mainFrame != null) mainFrame.selectFeed(feed);

        final IFeed oldFeed = model.getSelectedFeed();

        ViewModeValueModel vmvm = model.getViewModeValueModel();
        ViewTypeValueModel vtvm = model.getViewTypeValueModel();

        vmvm.recordValue();
        vtvm.recordValue();
        model.setSelectedFeed(feed);

        updateSearchHighlights(oldFeed, feed);

        // Abort loading of all images in previously selected feeds
        ImageFetcher.clearQueue();

        fireFeedSelected(feed);
        vmvm.compareRecordedWithCurrent();
        vtvm.compareRecordedWithCurrent();

        // Reset selected article when no feed selected
        if (feed == null) selectArticle(null);
    }

    /**
     * Selects article.
     *
     * @param aArticle article.
     */
    public void selectArticle(final IArticle aArticle)
    {
        selectArticle(aArticle, null);
    }
    
    /**
     * Selects article.
     *
     * @param aArticle article.
     * @param highlightLink a link to highlight.
     */
    public void selectArticle(final IArticle aArticle, final String highlightLink)
    {
        if (aArticle != null)
        {
            IFeed feed = aArticle.getFeed();
            if (model.getSelectedFeed() != feed)
            {
                // Selecting feed first
                IGuide[] guides = feed.getParentGuides();
                boolean guideSelected = false;
                IGuide currentGuide = model.getSelectedGuide();
                for (int i = 0; !guideSelected && i < guides.length; i++)
                {
                    IGuide guide = guides[i];
                    guideSelected = (guide == currentGuide);
                }

                if (!guideSelected) selectGuide(chooseBestGuide(guides), false);
                selectFeed(feed, true);
            }
        }

        this.highlightedArticleLink = highlightLink;

        // Selecting article
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                model.setSelectedArticles(new IArticle[] { aArticle });
                model.setSelectedArticle(aArticle);
                fireArticleSelected(aArticle);
            }
        });
    }

    /**
     * Returns a link that should be highlighted in an article when found.
     *
     * @return a link or <code>NULL</code>.
     */
    public String getHighlightedArticleLink()
    {
        return highlightedArticleLink;
    }

    /**
     * Sets or resets the search highlights depending on the type of the selected feed.
     *
     * @param oldFeed   previously selected feed.
     * @param newFeed   newly selected feed.
     *
     * @return <code>TRUE</code> if repainting required.
     */
    private boolean updateSearchHighlights(IFeed oldFeed, IFeed newFeed)
    {
        boolean repaintHighlights = false;
        if (newFeed instanceof SearchFeed)
        {
            repaintHighlights = installHighlightsFromSearchFeed((SearchFeed)newFeed);
        } else if (oldFeed != null && oldFeed instanceof SearchFeed)
        {
            resetSearchHighlights();
            repaintHighlights = true;
        }

        return repaintHighlights;
    }

    private void resetSearchHighlights()
    {
        searchHighlightsCalculator.keywordsChanged(Constants.EMPTY_STRING);
        currentSearchKeywords = Constants.EMPTY_STRING;
    }

    private boolean installHighlightsFromSearchFeed(SearchFeed aFeed)
    {
        boolean installed = false;
        String newKeywords = collectKeywordsFromSearchFeed(aFeed);

        if (!newKeywords.equalsIgnoreCase(currentSearchKeywords))
        {
            searchHighlightsCalculator.keywordsChanged(newKeywords);
            currentSearchKeywords = newKeywords;
            installed = true;
        }

        return installed;
    }

    private String collectKeywordsFromSearchFeed(SearchFeed aFeed)
    {
        StringBuffer keywords = new StringBuffer();
        Query query = aFeed.getQuery();
        int criteriaCount = query.getCriteriaCount();
        for (int i = 0; i < criteriaCount; i++)
        {
            ICriteria criteria = query.getCriteriaAt(i);
            if (isKeywordsSearchCriteria(criteria))
            {
                String keywordsList = criteria.getValue();
                String[] keywordsArray = StringUtils.keywordsToArray(keywordsList);

                // Quote if necessary
                for (int j = 0; j < keywordsArray.length; j++)
                {
                    keywordsArray[j] = StringUtils.quoteKeywordIfNecessary(keywordsArray[j]);
                }

                keywords.append("\n").append(StringUtils.join(keywordsArray, "\n"));
            }
        }

        return keywords.toString().trim();
    }

    private static boolean isKeywordsSearchCriteria(ICriteria aCriteria)
    {
        return ArticleTextProperty.INSTANCE.equals(aCriteria.getProperty()) &&
            StringContainsCO.INSTANCE.equals(aCriteria.getComparisonOperation());
    }

    /**
     * Returns main application frame.
     *
     * @return main frame.
     */
    public MainFrame getMainFrame()
    {
        return mainFrame;
    }

    /**
     * Moves all channels to the other guide.
     *
     * @param srcGuide source guide.
     * @param destGuide destination guide.
     */
    public void reassignChannelsTo(final StandardGuide srcGuide, final StandardGuide destGuide)
    {
        if (destGuide == null || destGuide == srcGuide) return;

        IFeed[] feeds = null;
        synchronized (srcGuide)
        {
            int srcFeedsCount = srcGuide.getFeedsCount();
            if (srcFeedsCount > 0)
            {
                feeds = new IFeed[srcFeedsCount];
                for (int i = srcFeedsCount - 1; i >= 0; i--)
                {
                    feeds[i] = srcGuide.getFeedAt(i);
                    srcGuide.remove(feeds[i]);
                }
            }
        }

        if (feeds != null)
        {
            synchronized (destGuide)
            {
                for (IFeed feed : feeds) destGuide.add(feed);
            }
        }
    }

    /**
     * Merges the list of guides with other guide.
     *
     * @param aGuides       guides to merge with the target guide (will be removed).
     * @param aMergeGuide   target guide to merge with.
     */
    public void mergeGuides(final IGuide[] aGuides, final StandardGuide aMergeGuide)
    {
        for (IGuide aGuide : aGuides)
        {
            if (aGuide instanceof StandardGuide)
            {
                reassignChannelsTo((StandardGuide)aGuide, aMergeGuide);
                getModel().getGuidesSet().remove(aGuide);
            }
        }

        selectGuideAndFeed(aMergeGuide);
    }

    // Starts background processes
    private void startBackgroundProcesses()
    {
        // The sequence is imporatant!
        // We wish this code to be executed after the model is set
        backManager.schedule(featureManager.getUpdater(), FeatureManager.UPDATE_PERIOD_SEC);

        backManager.schedule(tagsSaver, 60, 10);
        if (System.getProperty("noMetaDataUpdates") == null) backManager.schedule(metaDataUpdater, 60, 60);
        backManager.schedule(poller, 30, 10);

        // Start post-initialization tasks
        setupFeedReselector();
        setupStylesUpdater();

        // Start SearchFeeds updates
        backManager.scheduleOnce(new Runnable()
        {
            public void run()
            {
                searchFeedsManager.runAllQueries();
            }
        }, 20); 
    }

    /**
     * Returns meta-data manager.
     *
     * @return meta-data manager.
     */
    public MDManager getMetaDataManager()
    {
        return metaDataManager;
    }

    /**
     * Returns current highlights calculator.
     *
     * @return highlights calculator.
     */
    public HighlightsCalculator getHighlightsCalculator()
    {
        return highlightsCalculator;
    }

    /**
     * Returns current search highlights calculator.
     *
     * @return search highlights calculator.
     */
    public HighlightsCalculator getSearchHighlightsCalculator()
    {
        return searchHighlightsCalculator;
    }

    /**
     * Returns channel score calculator.
     *
     * @return channel score calculator.
     */
    public ScoresCalculator getScoreCalculator()
    {
        return scoresCalculator;
    }

    /**
     * Invoked by InformaBackEnd when it finds out that there's no resource under feed's URL.
     *
     * @param feed  feed which was polled.
     */
    public void feedHasGone(DirectFeed feed)
    {
        if (!feed.isDynamic())
        {
            FeedGoneDialog dialog = new FeedGoneDialog(getMainFrame(), feed);
            dialog.open();

            if (!dialog.hasBeenCanceled())
            {
                IGuide[] guides = feed.getParentGuides();
                for (IGuide guide : guides) guide.remove(feed);
            }
        }
    }

    /**
     * Invoked when it's detected that feed has moved to a new location.
     *
     * @param feed          moved feed.
     * @param newLocation   new location.
     */
    public void feedHasMoved(DirectFeed feed, URL newLocation)
    {
        if (feed == null)
            throw new IllegalArgumentException(Strings.error("unspecified.feed"));

        if (newLocation == null)
            throw new IllegalArgumentException(Strings.error("unspecified.location"));

        DirectFeed existingFeed = getModel().getGuidesSet().findDirectFeed(newLocation);
        if (existingFeed != null)
        {
            GuidesSet.replaceFeed(feed, existingFeed);
        } else
        {
            feed.setXmlURL(newLocation);
        }
    }

    /**
     * Indicates that guide has changed its position in list.
     *
     * @param guide       guide.
     * @param newPosition new position.
     */
    public void guideMoved(IGuide guide, int newPosition)
    {
        GuidesPanel cgp = mainFrame.getGudiesPanel();

        cgp.ensureIndexIsVisible(newPosition);
    }

    /**
     * Immediately starts updating of the feed.
     *
     * @param aFeed feed to update.
     */
    public void updateFeed(IFeed aFeed)
    {
        if (aFeed instanceof DataFeed)
        {
            DataFeed dFeed = (DataFeed)aFeed;
            poller.update(dFeed, true, true);
        } else if (aFeed instanceof SearchFeed)
        {
            updateSearchFeed((SearchFeed)aFeed);
        }
    }

    /**
     * Orders meta-data manager to forget given meta-data holders and refreshes
     * articles' highlights.
     *
     * @param holders   holders to forget.
     */
    public void forgetDiscoveries(FeedMetaDataHolder[] holders)
    {
        metaDataManager.forget(holders);
        repaintArticlesListHighlights();
    }

    /**
     * Returns currently active poller.
     *
     * @return poller.
     */
    public Poller getPoller()
    {
        return poller;
    }

    /**
     * Deletes guides and selects appropriate guide after that.
     *
     * @param guides    guides to delete.
     */
    public void deleteGuides(IGuide[] guides)
    {
        IGuide currentGuide = model.getSelectedGuide();
        GuidesSet set = model.getGuidesSet();

        int removedIndex = -1;
        for (IGuide guide : guides)
        {
            if (guide == currentGuide)
            {
                removedIndex = set.indexOf(guide);
            }

            set.remove(guide);
        }

        if (removedIndex != -1) selectGuideAndFeed(findGuideToSelect(set, removedIndex));
    }

    /**
     * Returns the guide to be selected after removal of the other guide from guides set.
     *
     * @param guidesSet     the set.
     * @param removedIndex  index of the removed guide.
     *
     * @return guide to select or <code>NULL</code> if it was the last guide.
     */
    static IGuide findGuideToSelect(GuidesSet guidesSet, int removedIndex)
    {
        IGuide guideToSelect = null;

        if (removedIndex >= 0)
        {
            GuideDisplayModeManager gdmm = GuideDisplayModeManager.getInstance();

            int count = guidesSet.getGuidesCount();
            for (int i = removedIndex; guideToSelect == null && i < count; i++)
            {
                IGuide g = guidesSet.getGuideAt(i);
                if (gdmm.isVisible(g)) guideToSelect = g;
            }

            for (int i = removedIndex - 1; guideToSelect == null && i >= 0; i--)
            {
                IGuide g = guidesSet.getGuideAt(i);
                if (gdmm.isVisible(g)) guideToSelect = g;
            }
        }

        return guideToSelect;
    }

    /**
     * Updates given reading list.
     *
     * @param list          list to update.
     * @param addFeeds      feeds to add to the list.
     * @param removeFeeds   feeds to remove from the guide.
     */
    public static void updateReadingList(ReadingList list, List<DirectFeed> addFeeds,
                                         List<DirectFeed> removeFeeds)
    {
        if (list == null) throw new NullPointerException(Strings.error("unspecified.reading.list"));
        if (addFeeds == null) throw new NullPointerException(Strings.error("unspecified.feeds.to.add"));
        if (removeFeeds == null) throw new NullPointerException(Strings.error("unspecified.feeds.to.remove"));

        if (addFeeds.size() == 0 && removeFeeds.size() == 0) return;

        int action = SINGLETON.getModel().getUserPreferences().getOnReadingListUpdateActions();

        // Update only non-removed lists when expecting no confirmation or when confirmation is granted
        boolean doUpdates = list.getParentGuide() != null &&
            (action != UserPreferences.RL_UPDATE_CONFIRM ||
            confirmReadingListUpdates(list, addFeeds, removeFeeds));

        if (doUpdates)
        {
            GuidesSet set = SINGLETON.getModel().getGuidesSet();

            for (DirectFeed feed : addFeeds)
            {
                URL xmlURL = feed.getXmlURL();
                DirectFeed existingFeed = set.findDirectFeed(xmlURL);

                if (existingFeed != null)
                {
                    feed = existingFeed;
                } else
                {
                    SINGLETON.getPoller().update(feed, false);
                }

                list.add(feed);
            }

            for (DirectFeed removeFeed : removeFeeds) list.remove(removeFeed);

            if (action == UserPreferences.RL_UPDATE_NOTIFY)
            {
                showReadingListUpdateNotification(list, addFeeds, removeFeeds);
            }
        }
    }

    /**
     * Simple notification dialog with a summary of updates.
     *
     * @param list          list updated.
     * @param addFeeds      feeds added.
     * @param removeFeeds   feeds removed.
     */
    private static void showReadingListUpdateNotification(ReadingList list, List addFeeds,
                                                          List removeFeeds)
    {
        String msg = MessageFormat.format(Strings.message("readinglist.updates.message"), 
            list.getTitle(), list.getParentGuide().getTitle(),
            addFeeds.size(), removeFeeds.size());

        JOptionPane.showMessageDialog(SINGLETON.getMainFrame(), msg, Strings.message("readinglist.updates.title"),
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows the dialog box with the list of URL's which are going to be added and the
     * list of feeds which are going to be removed and asks for confirmation from a user.
     *
     * @param list          reading list.
     * @param addFeeds      list of feeds to add.
     * @param removeFeeds   list of feeds to remove.
     *
     * @return <code>TRUE</code> if modification has been accepted.
     */
    private static boolean confirmReadingListUpdates(ReadingList list, List<DirectFeed> addFeeds,
                                                     List<DirectFeed> removeFeeds)
    {
        ReadingListUpdateConfirmationDialog dialog =
            new ReadingListUpdateConfirmationDialog(SINGLETON.getMainFrame(),
                list, addFeeds, removeFeeds);

        dialog.open();

        boolean confirmed = false;
        if (!dialog.hasBeenCanceled())
        {
            List newAddFeeds = dialog.getAddFeeds();
            addFeeds.clear();
            addFeeds.addAll(newAddFeeds);

            List newRemoveFeeds = dialog.getRemoveFeeds();
            removeFeeds.clear();
            removeFeeds.addAll(newRemoveFeeds);

            confirmed = true;
        }

        return confirmed;
    }

    /**
     * Returns the best guide of all to use for feed selection.
     * When the feed is in the same guide we are at this guide is preferred.
     *
     * @param guides    guides.
     *
     * @return the best guide to use.
     */
    public static IGuide chooseBestGuide(IGuide[] guides)
    {
        IGuide guide = null;
        IGuide currentGuide = GlobalModel.SINGLETON.getSelectedGuide();

        if (currentGuide != null)
        {
            // Select current guide
            for (int i = 0; guide == null && i < guides.length; i++)
            {
                IGuide iguide = guides[i];
                if (currentGuide == iguide) guide = currentGuide;
            }
        }

        // If the guide is still unselected, select the first guide
        if (guide == null) guide = guides[0];

        return guide;
    }

    private void autosubscribeIfNecessary()
    {
        String urlToOpen = ApplicationLauncher.getURLToOpen();
        if (StringUtils.isNotEmpty(urlToOpen))
        {
            try
            {
                subscribe(new URL(urlToOpen));
            } catch (MalformedURLException e)
            {
                LOG.warning("Invalid URL specified for subscription.");
            }
        }
    }

    /**
     * Subscribe to a given URL.
     *
     * @param url URL to subscribe to.
     */
    public void subscribe(final URL url)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                subscribe(url, XMLFormatDetector.detectOrAskFormat(url, getMainFrame()));
            }
        });
    }

    /**
     * Subscribes to a given URL with known format.
     *
     * @param url   URL to subscribe to.
     * @param fmt   format.
     */
    public void subscribe(URL url, XMLFormat fmt)
    {
        if (fmt != null)
        {
            if (checkForNewSubscription()) return;

            if (fmt != XMLFormat.OPML)
            {
                // TODO: Ask what to do ???
                DirectFeed feed = createDirectFeed(null, url);
                if (feed != null) selectFeed(feed, true);
            } else
            {
                SubscribeToReadingListAction.subscribe(url);
            }
        } else
        {
            // TODO: Localize
            JOptionPane.showMessageDialog(getMainFrame(),
                "<html><b>Unrecognized format of file.</b>\n\n" +
                    "BlogBridge can't recognize the format of the file.\n" +
                    "Please verify that the file is XML feed (RSS, Atom) or\n" +
                    "Reading List (OPML) and try again.",
                "Subscribe",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Thread class which performs the slow accessing of the persistent database with all the saved
     * channels and items in the background.
     */
    private class OpenDBinBackground extends Thread
    {
        private static final String THREAD_TITLE = "Load used tags";

        private GlobalModel installationModel;

        /**
         * Create database thread.
         *
         * @param aInstallationModel model from installer.
         */
        OpenDBinBackground(GlobalModel aInstallationModel)
        {
            super("OpenDBinBackground");

            installationModel = aInstallationModel;

            setPriority(Thread.MIN_PRIORITY);
        }

        /**
         * Runs the task.
         */
        public void run()
        {
            if (LOG.isLoggable(Level.FINE)) LOG.fine("Loading persistent state from DB.");
            ActivityTicket actTicket = ActivityIndicatorView.startOpeningDatabase();
            getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try
            {
                model.loadingStarted();

                final GuidesSet guidesSet = getModel().getGuidesSet();

                // Load data into model
                IPersistenceManager manager = PersistenceManagerConfig.getManager();
                manager.loadGuidesSet(guidesSet);

                // Connect persistence listeners
                ChangesMonitor changesMonitor = new ChangesMonitor(guidesSet, manager);
                domainEventsListener.addDomainListener(changesMonitor);

                // Copy guides and preferences from installer model if it is present
                if (installationModel != null)
                {
                    GuidesSet installerSet = installationModel.getGuidesSet();
                    int count = installerSet.getGuidesCount();
                    for (int i = 0; i < count; i++)
                    {
                        guidesSet.add(installerSet.getGuideAt(i));
                    }

                    // Copy preferences
                    Preferences appPrefs = Application.getUserPreferences();
                    installationModel.storePreferences(appPrefs);
                    restoreModelPreferencesAndUpdate(model, appPrefs);
                }

                // If database was reset we need to show recovery options
                if (manager.isDatabaseReset())
                {
                    DatabaseRecoverer.performRecovery(model, ApplicationLauncher.getBackupsPath());
                }

                initMaxViewsAndClickthroughs(guidesSet);

                startLoadingUsedTags(guidesSet);

                if (dockIconUnreadMonitor != null) dockIconUnreadMonitor.update();

                model.loadingFinished();

                // Perform sync-on-startup only if the database was OK
                if (!manager.isDatabaseReset())
                {
                    // check if it's time for full sync on startup
                    SyncFull syncFull = new SyncFull(model);
                    if (syncFull.isSyncTime()) SyncFullAction.getInstance().doSync(null);
                }

                model.initTransientState();
                restoreFeedSelection();

                // Extra repainting of highlights to show links to existing feeds correctly
                repaintArticlesListHighlights();
            } catch (Exception e)
            {
                LOG.log(Level.SEVERE, Strings.error("exception.during.opening.db.in.background"), e);
                model.loadingFinished();
            } finally
            {
                getMainFrame().setCursor(Cursor.getDefaultCursor());
                ActivityIndicatorView.finishActivity(actTicket);

                ApplicationLauncher.enableIPC();

                fireInitializationFinished();

                checkForWarnings();

                startBackgroundProcesses();
                autosubscribeIfNecessary();

                checkForNewVersion();
            }

            if (LOG.isLoggable(Level.FINE)) LOG.fine("Done loading persistent state from DB.");

            // Force loading of URLs
            ServerService.getStartingPointsURL();
        }

        /**
         * Initializes maximum feed views and clickthrough counters with values from guides set.
         *
         * @param set set.
         */
        private void initMaxViewsAndClickthroughs(GuidesSet set)
        {
            List<IFeed> feeds = set.getFeeds();
            for (IFeed feed : feeds)
            {
                ScoresCalculator.registerMaxClickthroughs(feed.getClickthroughs());
                ScoresCalculator.registerMaxFeedViews(feed.getViews());
            }
        }

        /**
         * Checks for updates with proactive dialog.
         */
        private void checkForNewVersion()
        {
            if (!ApplicationLauncher.isAutoUpdatesEnabled()) return;

            UserPreferences prefs = model.getUserPreferences();
            boolean checkForUpdateOnStartup = prefs.isCheckingForUpdatesOnStartup();
            if (checkForUpdateOnStartup)
            {
                // Check for updates as soon as the service becomes available
                ConnectionState connectionState = getConnectionState();
                connectionState.callWhenServiceIsAvailable(new CheckForNewVersionTask());
            }
        }

        /**
         * Starts background loading of used tags.
         *
         * @param aGuidesSet current guides set.
         */
        private void startLoadingUsedTags(final GuidesSet aGuidesSet)
        {
            Thread loadUsedTagsThread = new Thread(THREAD_TITLE)
            {
                /** Invoked when running the thread. */
                public void run()
                {
                    final TagsRepository repository = TagsRepository.getInstance();
                    repository.loadFromGuidesSet(aGuidesSet);

                    UserPreferences prefs = model.getUserPreferences();
                    final String user = prefs.getTagsDeliciousUser();
                    final String password = prefs.getTagsDeliciousPassword();

                    if (StringUtils.isNotEmpty(user) && StringUtils.isNotEmpty(password))
                    {
                        try
                        {
                            repository.loadTagsFromDelicious(user, password);
                        } catch (IOException e)
                        {
                            LOG.log(Level.WARNING, Strings.error("failed.to.load.used.tags.from.delicious"), e);
                        }
                    }
                }
            };

            loadUsedTagsThread.start();
        }

        /**
         * Checks for a new version availability.
         */
        private class CheckForNewVersionTask implements Runnable
        {
            /** Invoked when execution begins. */
            public void run()
            {
                MainFrame mainFrame = getMainFrame();
                String currentVersion = ApplicationLauncher.getCurrentVersion();

                FullCheckCycle checker = new FullCheckCycle(mainFrame, currentVersion,
                    false);
                try
                {
                    checker.check();
                } catch (Throwable e)
                {
                    LOG.log(Level.WARNING, Strings.error("failed.to.finish.updates.check"), e);
                }
            }
        }
    }

    /**
     * Checks if the synchronization is possible.
     *
     * @return <code>TRUE</code> if possible.
     */
    public boolean canSynchronize()
    {
        FeatureManager fm = getFeatureManager();
        boolean can = fm.canSynchronize();

        if (!can)
        {
            List<String> warnings = new ArrayList<String>();
            warnings.add(MessageFormat.format(Strings.message("spw.synlimit"),
                fm.getSynchronizationsCount(false), fm.getSynchronizationLimit()));

            showWarningsDialog(warnings);
        }

        return can;
    }

    /**
     * Checks subscription limit violation before adding anything capable of
     * bringing new subscriptions.
     *
     * @return <code>TRUE</code> if the violation takes place.
     */
    public boolean checkForNewSubscription()
    {
        return checkForWarnings(false, true, true);
    }

    /**
     * Checks if current feature set limits are violated.
     */
    public void checkForWarnings()
    {
        checkForWarnings(true, true, false);
    }

    /**
     * Checks if current feature set limits are violated.
     *
     * @param pubL  TRUE to check publication limit.
     * @param subL  TRUE to check subscriptions limit.
     * @param eq    TRUE to check if the numbers are equal to limits.
     *
     * @return <code>TRUE</code> if problems detected.
     */
    public boolean checkForWarnings(boolean pubL, boolean subL, boolean eq)
    {
        final List<String> warnings = new ArrayList<String>();
        final FeatureManager fm = getFeatureManager();

        // Check pub limit
        int pubLimit = fm.getPublicationLimit();
        if (pubL && pubLimit > -1)
        {
            int publications = getModel().getGuidesSet().countPublishedGuides();
            if (publications > pubLimit || (eq && publications == pubLimit))
            {
                warnings.add(MessageFormat.format(Strings.message("spw.publimit"), publications, pubLimit));
            }
        }

        // Check sub limit
        int subLimit = fm.getSubscriptionLimit();
        if (subL && subLimit > -1)
        {
            int subscriptions = getModel().getGuidesSet().getFeedsList().getFeedsCount();
            if (subscriptions > subLimit || (eq && subscriptions == subLimit))
            {
                warnings.add(MessageFormat.format(Strings.message("spw.sublimit"), subscriptions, subLimit));
            }
        }

        // Show the message if necessary
        showWarningsDialog(warnings);

        return warnings.size() > 0;
    }

    /**
     * Shows warnings dialog if necessary.
     *
     * @param warnings warnings.
     */
    private void showWarningsDialog(final List<String> warnings)
    {
        if (warnings.size() > 0)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    PlanWarningsDialog dialog = new PlanWarningsDialog(getMainFrame());
                    dialog.open(getFeatureManager().getPlanName(), warnings);
                }
            });
        }
    }

    /**
     * Restores the guide and feed selected before the application exit last time.
     * Alternatively (if it is the first run, for example) it selects the first found guide.
     */
    private void restoreFeedSelection()
    {
        Preferences prefs = Application.getUserPreferences();
        long guideId = prefs.getLong(KEY_SELECTED_GUIDE_ID, -1);

        // Find the selected guide by ID or take the first guide if set isn't empty
        GuidesSet set = model.getGuidesSet();
        IGuide guide = null;
        if (guideId != -1)
        {
            synchronized (set)
            {
                int count = set.getGuidesCount();
                for (int i = 0; guide == null && i < count; i++)
                {
                    IGuide guideItem = set.getGuideAt(i);
                    if (guideItem.getID() == guideId) guide = guideItem;
                }
            }
        }

        if (guide == null && set.getGuidesCount() > 0) guide = set.getGuideAt(0);

        // Find selected feed by ID and perform selection
        if (guide != null)
        {
            long feedId = prefs.getLong(KEY_SELECTED_FEED_ID, -1);
            IFeed feed = null;
            synchronized (guide)
            {
                int count = guide.getFeedsCount();
                for (int i = 0; feed == null && i < count; i++)
                {
                    IFeed feedItem = guide.getFeedAt(i);
                    if (feedItem.getID() == feedId) feed = feedItem;
                }
            }

            selectGuide(guide, false);
            if (feed != null)
            {
                final IFeed selectionFeed = feed;
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        selectFeed(selectionFeed);
                    }
                });
            }
        }
    }

    /**
     * Saves currently selected feed and guide to restore them later.
     */
    private void storeFeedSelection()
    {
        Preferences prefs = Application.getUserPreferences();

        IFeed selectedFeed = model.getSelectedFeed();
        IGuide selectedGuide = model.getSelectedGuide();

        long guideId = selectedGuide == null ? -1 : selectedGuide.getID();
        long feedId = selectedFeed == null ? -1 : selectedFeed.getID();

        prefs.putLong(KEY_SELECTED_GUIDE_ID, guideId);
        prefs.putLong(KEY_SELECTED_FEED_ID, feedId);
    }

    /**
     * Reads in the last saved persistent state and restores the Model to that state. If there's
     * something wrong with the persisted state then we start over with a default state.
     *
     * @param aModel model of new version if it was detected or <code>null</code> in common case.
     */
    public void restorePersistentState(GlobalModel aModel)
    {
        registerAppCloseEventListener();

        new OpenDBinBackground(aModel).start();

        if (LOG.isLoggable(Level.FINE)) LOG.fine("Done loading persistent state...");
    }

    /**
     * Register a listener with the JGoodies Application object to be called
     * just before app is closed for any reason.
     */
    private void registerAppCloseEventListener()
    {
        // Register listener that is called just before app is closed.
        Application.addApplicationListener(new ApplicationAdapter()
        {
            /**
             * Invoked if the application is closing.
             *
             * @param evt the related <code>ApplicationEvent</code>.
             */
            public void applicationClosing(ApplicationEvent evt)
            {
                prepareToClose(false);

                // Force clean application exit
                // If we will not do this the Application will continue with useless
                // frames and windows disposal procedure which in some cases gets stuck
                // making exit not available. As we don't have anything else to do with
                // application we simply do clean exit here.
                System.exit(0);
            }
        });
    }

    /**
     * Called when application is closing to store the state of model and preferences.
     *
     * @param emergencyExit TRUE if it's an emergency exit.
     */
    public void prepareToClose(boolean emergencyExit)
    {
        // Request termination of background processes
        backManager.requestExit();

        if (initializationFinished)
        {
            storeFeedSelection();

            File backupsDir = new File(ApplicationLauncher.getBackupsPath());
            Backups backups = new Backups(backupsDir, LAST_BACKUPS_TO_KEEP);
            try
            {
                backups.saveBackup(model.getGuidesSet());
            } catch (IOException e)
            {
                LOG.log(Level.SEVERE, Strings.error("failed.to.write.backup"), e);
            }

            if (!emergencyExit) syncOutOnExit();

            model.prepareForApplicationExit();
            storePreferences();
        }
    }

    /**
     * Analyzes the situation with last sync-out dates and feeds counts and decides
     * if the change in feeds count is suspicious -- looks like a damage or something
     * wrong. If so, the confirmation dialog is given to user.
     */
    private void syncOutOnExit()
    {
        if (!getConnectionState().isServiceAccessible()) return;

        SyncOut syncOut = new SyncOut(model);
        if (syncOut.isSyncTime())
        {
            GuidesSet set = model.getGuidesSet();
            ServicePreferences servicePreferences = model.getServicePreferences();

            int feedsCount = set.countFeeds();
            int lastSyncOutFeedsCount = servicePreferences.getLastSyncOutFeedsCount();
            boolean synchronizedBefore = servicePreferences.getLastSyncOutDate() != null;

            boolean doSync = feedsCount > 0 || synchronizedBefore;
            if (doSync && isSuspiciousDifference(feedsCount, lastSyncOutFeedsCount))
            {
                String message;
                if (feedsCount == 0)
                {
                    message = Strings.message("synconexit.clear.the.list.of.your.saved.subscriptions");
                } else
                {
                    message = MessageFormat.format(Strings.message("synconexit.make.a.large.change.0.1"),
                        lastSyncOutFeedsCount, feedsCount);
                }

                int result = JOptionPane.showConfirmDialog(getMainFrame(),
                    MessageFormat.format(Strings.message("synconexit.suspicious.sync.text.0"), message),
                    Strings.message("synconexit.suspicious.sync.title"), JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);

                doSync = result == JOptionPane.YES_OPTION;
            }

            if (doSync && canSynchronize()) syncOut.doSynchronization(null, false);
        }
    }

    /**
     * Returns TRUE if change in feeds number is suspicious.
     *
     * @param aFeedsCount               current feeds count.
     * @param aLastSyncOutFeedsCount    last sync feeds count.
     *
     * @return TRUE if looks suspicious.
     */
    static boolean isSuspiciousDifference(int aFeedsCount, int aLastSyncOutFeedsCount)
    {
        return (aFeedsCount == 0 ||
            (aLastSyncOutFeedsCount > CHANGE_CHECK_FEEDS_THRESHOLD &&
             aLastSyncOutFeedsCount / aFeedsCount > CHANGE_CHECK_DIFF_TIMES));
    }

    /**
     * Restore user prefefences from Preferences file.
     */
    void restorePreferences()
    {
        final Preferences prefs = Application.getUserPreferences();
        overridePreferencesWithPlugins(prefs);

        // Propagate default preferences to data feeds
        DataFeed.setGlobalUpdatePeriod(UserPreferences.DEFAULT_RSS_POLL_MIN *
            Constants.MILLIS_IN_MINUTE);
        DataFeed.setGlobalPurgeUnread(!UserPreferences.DEFAULT_PRESERVE_UNREAD);
        DataFeed.setGlobalPurgeLimit(UserPreferences.DEFAULT_PURGE_COUNT);

        UnreadButton.setShowMenuOnClick(UserPreferences.DEFAULT_SHOW_UNREAD_BUTTON_MENU);

        restoreModelPreferencesAndUpdate(model, prefs);
    }

    /**
     * Restore moel preferences and updates other components depending on them.
     *
     * @param mdl   model.
     * @param prefs preferences object.
     */
    public static void restoreModelPreferencesAndUpdate(GlobalModel mdl, Preferences prefs)
    {
        mdl.restorePreferences(prefs);

        // Update all dependent components
        ImageBlocker.restorePreferences(prefs);
        SentimentsConfig.restorePreferences(prefs);

        setProxySettings(mdl.getUserPreferences());
        GuideDisplayModeManager.getInstance().restorePreferences(prefs);
        FeedDisplayModeManager.getInstance().restorePreferences(prefs);
        NotificationArea.setAppIconAlwaysVisible(mdl.getUserPreferences().isShowAppIconInSystray());
        PostToBlogAction.update();
        FeedLinkPostToBlogAction.update();
    }

    private void overridePreferencesWithPlugins(Preferences prefs)
    {
        List<com.salas.bb.plugins.domain.Package> pkgs = Manager.getEnabledPackages();
        for (Package pkg : pkgs)
        {
            for (IPlugin plugin : pkg)
            {
                if (plugin instanceof AdvancedPreferencesPlugin)
                {
                    AdvancedPreferencesPlugin app = (AdvancedPreferencesPlugin)plugin;
                    app.overridePreferences(prefs);
                }
            }
        }
    }

    /**
     * Sets property settings from the user preferences.
     *
     * @param prefs preferences.
     */
    static void setProxySettings(UserPreferences prefs)
    {
        Properties sys = System.getProperties();
        if (prefs.isProxyEnabled() && StringUtils.isNotEmpty(prefs.getProxyHost()))
        {
            String host = prefs.getProxyHost().trim();
            String port = Integer.toString(prefs.getProxyPort());

            sys.put("http.proxyHost", host);
            sys.put("http.proxyPort", port);
            sys.put("https.proxyHost", host);
            sys.put("https.proxyPort", port);
            sys.put("ftp.proxyHost", host);
            sys.put("ftp.proxyPort", port);
        } else
        {
            sys.remove("http.proxyHost");
            sys.remove("http.proxyPort");
            sys.remove("https.proxyHost");
            sys.remove("https.proxyPort");
            sys.remove("ftp.proxyHost");
            sys.remove("ftp.proxyPort");
        }
    }

    /**
     * Save User Preferences to Preferences file.
     */
    private void storePreferences()
    {
        final Preferences prefs = Application.getUserPreferences();

        // Give mainFrame a chance to save its window position.
        mainFrame.prepareToClose();

        model.storePreferences(prefs);
        GuideDisplayModeManager.getInstance().storePreferences(prefs);
        FeedDisplayModeManager.getInstance().storePreferences(prefs);

        ImageBlocker.storePreferences(prefs);
        SentimentsConfig.storePreferences(prefs);
    }

    /**
     * Exit BlogBridge as a whole.
     */
    public static void exitApplication()
    {
        boolean exitConfirmed = true;

        // Check if we have downloads running
        int downloads = NetManager.getDownloadsCount();
        if (downloads > 0)
        {
            int res = JOptionPane.showConfirmDialog(SINGLETON.getMainFrame(),
                Strings.message("exit.downloads.running"),
                Strings.message("exit"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                Resources.getLargeApplicationIcon());

            exitConfirmed = res == JOptionPane.YES_OPTION;
        }

        if (exitConfirmed) Application.close();
   }

    /**
     * Sets the status of the application or other information to the status bar.
     *
     * @param status status.
     */
    public void setStatus(String status)
    {
        MainFrame frame = getMainFrame();
        if (frame != null) frame.setStatus(status);
    }

    /**
     * Moves feed from one guide to another. It adds feed to the tail of destination guide
     * list.
     *
     * @param feed  feed to move.
     * @param from  old guide.
     * @param to    new guide.
     * @param index new index in destination guide.
     */
    public void moveFeed(final IFeed feed, final StandardGuide from,
                         final StandardGuide to, int index)
    {
        if (feed == null || from == null || to == null) return;

        final UserPreferences prefs = getModel().getUserPreferences();
        if (from == to && prefs.isSortingEnabled())
        {
            // No visible effect
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    Map names = FeedsSortOrder.SORTING_CLASS_NAMES;
                    String firstSortOrder = (String)names.get(prefs.getSortByClass1());
                    String secondSortOrder = (String)names.get(prefs.getSortByClass2());

                    String title = Strings.message("move.feed.title");
                    String msg = MessageFormat.format(Strings.message("move.feeds.no.effect"),
                        firstSortOrder, secondSortOrder);
                    JOptionPane.showMessageDialog(getMainFrame(), msg, title, JOptionPane.INFORMATION_MESSAGE);
                }
            });
        }

        from.moveFeed(feed, to, index);
    }

    /**
     * Moves guide to the new position in the list.
     *
     * @param cg                guide to move.
     * @param insertPosition    index to insert at.
     */
    public void moveGuide(final IGuide cg, final int insertPosition)
    {
        if (cg != null)
        {
            GlobalModel.SINGLETON.getGuidesSet().relocateGuide(cg, insertPosition);
        }
    }

    public static void pinArticles(boolean pinned, IGuide guide, IFeed feed, IArticle ... articles)
    {
        if (articles == null || articles.length == 0) return;

        int cnt = 0;

        // Pin and count
        for (IArticle article : articles)
        {
            if (article.isPinned() != pinned) cnt++;
            article.setPinned(pinned);
        }

        if (pinned && cnt > 0)
        {
            IPersistenceManager pm = PersistenceManagerConfig.getManager();
            pm.getStatisticsManager().articlesPinned(guide, feed, cnt);
        }
    }

    /**
     * Marks articles as (un)read in bulk and updates the statistics.
     *
     * @param read      <code>TRUE</code> to mark as read, otherwise -- unread.
     * @param guide     guide to associate with reading (NULLable).
     * @param feed      feed to associate with reading (NULLable).
     * @param articles  articles to mark.
     */
    public static void readArticles(boolean read, IGuide guide, IFeed feed, IArticle ... articles)
    {
        if (articles == null || articles.length == 0) return;

        int cnt = 0;

        // Mark and count
        for (IArticle article : articles)
        {
            if (article.isRead() != read) cnt++;
            article.setRead(read);
        }
        
        // Record stats if it's reading and the count is greater than 0
        if (read && cnt > 0)
        {
            IPersistenceManager pm = PersistenceManagerConfig.getManager();
            pm.getStatisticsManager().articlesRead(guide, feed, cnt);
        }
    }

    /**
     * Marks feeds as (un)read and updates stats in bulk.
     *
     * @param read      <code>TRUE</code> to mark as read, otherwise -- unread.
     * @param guide     guide to associate with reading (NULLable).
     * @param feeds     feeds to mark.
     */
    public static void readFeeds(boolean read, IGuide guide, IFeed ... feeds)
    {
        if (feeds == null || feeds.length == 0) return;

        for (IFeed feed : feeds)
        {
            int cnt = 0;

            synchronized (feed)
            {
                if (read) cnt = feed.getUnreadArticlesCount();
                feed.setRead(read);
            }

            if (cnt > 0)
            {
                IPersistenceManager pm = PersistenceManagerConfig.getManager();
                pm.getStatisticsManager().articlesRead(guide, feed, cnt);
            }
        }
    }

    /**
     * Marks guides a (un)read and updates stats.
     *
     * @param read      <code>TRUE</code> to mark as read, otherwise -- unread.
     * @param guides    guides to mark.
     */
    public static void readGuides(boolean read, IGuide ... guides)
    {
        if (guides == null || guides.length == 0) return;

        for (IGuide guide : guides)
        {
            IFeed[] feeds = GlobalModel.SINGLETON.getVisibleFeeds(guide);
            readFeeds(read, guide, feeds);
        }
    }

    /**
     * Adds new domain listener.
     *
     * @param l domain listener
     */
    public void addDomainListener(final IDomainListener l)
    {
        domainEventsListener.addDomainListener(l);
    }

    /**
     * Removes a domain listener.
     *
     * @param l listener.
     */
    public void removeDomainListener(IDomainListener l)
    {
        domainEventsListener.removeDomainListener(l);
    }

    /**
     * Adds new controller listener object to the list.
     *
     * @param l listener object reference.
     */
    public void addControllerListener(final IControllerListener l)
    {
        listeners.add(l);
    }

    /**
     * Removes registration of listener object.
     *
     * @param l listener object reference.
     */
    public void removeControllerListener(final IControllerListener l)
    {
        listeners.remove(l);
    }

    /**
     * Fires article selection event.
     *
     * @param article   article selected.
     */
    public void fireArticleSelected(IArticle article)
    {
        for (IControllerListener listener : listeners)
        {
            try
            {
                listener.articleSelected(article);
            } catch (Exception e)
            {
                LOG.log(Level.SEVERE, Strings.error("unhandled.exception"), e);
            }
        }
    }

    /**
     * Fires event after selection of feed.
     *
     * @param feed feed to be selected.
     */
    public void fireFeedSelected(final IFeed feed)
    {
        for (IControllerListener listener : listeners)
        {
            try
            {
                listener.feedSelected(feed);
            } catch (Exception e)
            {
                LOG.log(Level.SEVERE, Strings.error("unhandled.exception"), e);
            }
        }
    }

    /**
     * Fires guide selection event.
     *
     * @param guide guide which was selected.
     */
    public void fireGuideSelected(final IGuide guide)
    {
        for (IControllerListener listener : listeners)
        {
            try
            {
                listener.guideSelected(guide);
            } catch (Exception e)
            {
                LOG.log(Level.SEVERE, Strings.error("unhandled.exception"), e);
            }
        }
    }

    /**
     * Set GlobalController flag that says initializion is finished, and
     * fires initialization finish event.
     */
    public void fireInitializationFinished()
    {
        this.initializationFinished = true;

        for (IControllerListener listener : listeners)
        {
            try
            {
                listener.initializationFinished();
            } catch (Exception e)
            {
                LOG.log(Level.SEVERE, Strings.error("unhandled.exception"), e);
            }
        }
    }

    /**
     * Returns the initialization flag.
     *
     * @return TRUE if initialization finished.
     */
    public boolean isInitializationFinished()
    {
        return initializationFinished;
    }

    /**
     * Adds new guide at the specified position.
     *
     * @param title             title of the guide.
     * @param iconKey           key of icon.
     * @param autoFeedDiscovery auto feed discovery flag.
     *
     * @return created guide or NULL if failed.
     */
    public StandardGuide createStandardGuide(String title, String iconKey,
                                             boolean autoFeedDiscovery)
    {
        StandardGuide cg = new StandardGuide();
        cg.setTitle(title);
        cg.setIconKey(iconKey);
        cg.setAutoFeedsDiscovery(autoFeedDiscovery);

        final GuidesSet guidesSet = getModel().getGuidesSet();

        IGuide selectedGuide = getModel().getSelectedGuide();
        guidesSet.add(selectedGuide == null ? -1 : guidesSet.indexOf(selectedGuide), cg);

        return cg;
    }

    /**
     * Adds new channel to the currently selected guide after currently selected channel.
     *
     * @param url           URL to use for addition or separated list of URL's.
     * @param forceQuery    TRUE to open dialog for URL querying.
     *
     * @return new feed (the first from the list in multi-mode) or
     *         NULL if selected guide isn't Standard Guide or URL's weren't specified.
     */
    public DirectFeed createDirectFeed(String url, boolean forceQuery)
    {
        DirectFeed feed = null;
        IGuide guide = model.getSelectedGuide();

        if (guide == null || guide instanceof StandardGuide)
        {
            if (url == null || forceQuery)
            {
                ValueHolder urlHolder = new ValueHolder(url);

                AddDirectFeedDialog dialog = new AddDirectFeedDialog(getMainFrame(), urlHolder);
                dialog.open();

                url = dialog.hasBeenCanceled() ? null : (String)urlHolder.getValue();
            }

            Set<String> urls = parseMultiURL(url);
            DirectFeed[] feeds = createDirectFeeds(urls, (StandardGuide)guide);

            if (feeds.length > 0) feed = feeds[0];
        }

        return feed;
    }

    /**
     * Parses string with multiple URL's delimitered by URL separator char defined in
     * constants. The set of URL's returned may be empty if source multi-URL was NULL
     * or empty string. The resulting set contains no duplicate URL's and all URL's in                             Ê
     * it are already "fixed".
     *
     * @param multiURL multi-URL or NULL.
     *
     * @return set of unique fixed URL's.
     *
     * @see Constants#URL_SEPARATOR
     * @see com.salas.bb.utils.StringUtils#fixURL(String)
     */
    static Set<String> parseMultiURL(String multiURL)
    {
        Set<String> urls = new HashSet<String>();

        if (multiURL != null)
        {
            StringTokenizer st = new StringTokenizer(multiURL, Constants.URL_SEPARATOR);
            while (st.hasMoreTokens()) urls.add(StringUtils.fixURL(st.nextToken()));
        }

        return urls;
    }

    /**
     * Creates direct feeds out of list of URL's and assigns them to the guide.
     *
     * @param urls  set of URL's.
     * @param guide guide to assign new feeds to.
     *
     * @return list of created feeds.
     */
    public DirectFeed[] createDirectFeeds(Set<String> urls, StandardGuide guide)
    {
        DirectFeed[] feeds;

        if (urls == null)
        {
            feeds = new DirectFeed[0];
        } else
        {
            int count = urls.size();
            feeds = new DirectFeed[count];

            int i = 0;
            for (String url : urls)
            {
                try
                {
                    feeds[i++] = createDirectFeed(guide, StringUtils.fixURL(url));
                } catch (MalformedURLException e)
                {
                    JOptionPane.showMessageDialog(getMainFrame(),
                        Strings.message("invalid.url.message"),
                        Strings.message("invalid.url"),
                        JOptionPane.WARNING_MESSAGE);
                    LOG.warning(MessageFormat.format(Strings.error("invalid.url"), url));
                }
            }
        }

        return feeds;
    }

    /**
     * Adds new direct feed to the guide by specified reference. Meta-data is queried from
     * repository and passed to feed.
     *
     * @param guide     guide.
     * @param reference reference text for feed discovery.
     *
     * @return new feed or <code>NULL</code> if wasn't added.
     * @throws java.net.MalformedURLException in case of bad URL.
     */
    public DirectFeed createDirectFeed(IGuide guide, String reference)
        throws MalformedURLException
    {
        return createDirectFeed(guide, new URL(reference));
    }

    /**
     * Creates direct feed and adds it to the guide.
     *
     * @param guide     guide or <code>NULL</code> for the first guide or new.
     * @param aUrl      URL to load feed from.
     *
     * @return new feed or <code>NULL</code> if wasn't added.
     */
    public DirectFeed createDirectFeed(IGuide guide, URL aUrl)
    {
        if (aUrl == null) return null;

        String reference = aUrl.toString();
        if (guide == null) guide = chooseOrMakeGuideForNewFeed();

        DirectFeed feed;
        boolean proceed = true;
        boolean existing = false;

        DirectFeed otherFeed = model.getGuidesSet().getFeedsList().findDirectFeed(aUrl, true);
        if (otherFeed != null && otherFeed.isInitialized())
        {
            // There's another feed with the same URL, we reuse it
            feed = otherFeed;

            // If that other feed is not in the guide we are adding this
            // duplicate to, we still add a feed. If it's in the same guide,
            // we don't.
            if (guide.indexOf(feed) == -1) guide.add(feed);

            return feed;
        } else
        {
            feed = new DirectFeed();
            feed.setBaseTitle(reference);

            FeedMetaDataHolder metaData = metaDataManager.lookupOrDiscover(aUrl);
            feed.setMetaData(metaData);

            if (metaData.isComplete() || metaData.isDiscoveredInvalid())
            {
                if (metaData.isDiscoveredInvalid())
                {
                    proceed = !processInvalidDiscovery(metaData, feed, aUrl);
                }

                // Checking again because after invalid discovery processing it may change
                if (metaData.isDiscoveredValid())
                {
                    URL xmlURL = metaData.getXmlURL();
                    GuidesSet set = getModel().getGuidesSet();
                    DirectFeed existingFeed = set.findDirectFeed(xmlURL);

                    if (existingFeed != null)
                    {
                        feed = existingFeed;
                        existing = true;
                    } else feed.setXmlURL(xmlURL);
                }

                // This blog is already discovered so repaint highlights
                if (proceed && !existing) repaintArticlesListHighlights();
            }
        }

        if (proceed) guide.add(feed);

        if (!existing)
        {
            if (proceed)
            {
                poller.update(feed, true, true);
            } else feed = null;
        } else checkForDuplicates(feed);

        return feed;
    }

    /**
     * Checks for duplicate feed present, warns a user and removes the feed if user wishes to.
     *
     * @param aFeed feed.
     */
    private void checkForDuplicates(DirectFeed aFeed)
    {
        IGuide[] allGuides = aFeed.getParentGuides();

        if (allGuides.length > 1)
        {
            ShowDuplicateFeeds dialog = new ShowDuplicateFeeds(mainFrame, aFeed);

            dialog.open();
            IGuide[] removals = dialog.getRemovals();
            if (removals != null)
            {
                for (IGuide removal : removals) removal.remove(aFeed);
            }
        }
    }

    /**
     * Finds the best title of all.
     *
     * @param feeds feeds to look for the best title among.
     *
     * @return title.
     */
    static String findBestTitle(NetworkFeed[] feeds)
    {
        String title = null;
        for (NetworkFeed nfeed : feeds)
        {
            String ntitle = nfeed.getTitle();
            if (title == null || (ntitle != null && !ntitle.matches("^[^:]{3,5}://.+")))
            {
                title = ntitle;
            }
        }
        return title;
    }

    /**
     * Creates query feed and adds it to selected, first or new guide.
     *
     * @param guide         guide to assign this new feed to
     *                      (or <code>NULL</code> to choose or make it)
     * @param title         title for new feed.
     * @param queryType     query type.
     * @param parameter     query parameter.
     * @param purgeLimit    purge limit.
     *
     * @return query feed added.
     *
     */
    public QueryFeed createQueryFeed(StandardGuide guide, String title, int queryType,
                                     String parameter, int purgeLimit)
    {
        if (guide == null) guide = chooseOrMakeGuideForNewFeed();

        GuidesSet set = getModel().getGuidesSet();
        QueryType type = QueryType.getQueryType(queryType);

        QueryFeed queryFeed = set.findQueryFeed(type, parameter);

        if (queryFeed == null)
        {
            queryFeed = new QueryFeed();
            queryFeed.setBaseTitle(title);
            queryFeed.setQueryType(type);
            queryFeed.setParameter(parameter);
            queryFeed.setPurgeLimit(purgeLimit);
        } else
        {
            // TODO !!! display notification about duplicate or continue cleanly?
        }

        guide.add(queryFeed);
        poller.update(queryFeed, true, true);

        return queryFeed;
    }

    /**
     * Creates search feed and adds it to the guide or new automatically created guide.
     *
     * @param aGuide        guide to add feed to.
     * @param aTitle        title of the feed.
     * @param aSearchQuery  query of the feed.
     * @param aPurgeLimit   purge limit.
     *
     * @return newly created feed.
     */
    public SearchFeed createSearchFeed(StandardGuide aGuide, String aTitle,
                                       Query aSearchQuery, int aPurgeLimit)
    {
        if (aGuide == null) aGuide = chooseOrMakeGuideForNewFeed();

        GuidesSet set = getModel().getGuidesSet();
        SearchFeed searchFeed = set.findSearchFeed(aSearchQuery);

        if (searchFeed == null)
        {
            searchFeed = new SearchFeed();
            searchFeed.setBaseTitle(aTitle);
            searchFeed.setArticlesLimit(aPurgeLimit);
            searchFeed.setQuery(aSearchQuery);
        } else
        {
            // TODO !!! display notification about duplicate or continue cleanly?
        }

        aGuide.add(searchFeed);

        return searchFeed;
    }

    /**
     * Runs a thread updating the given search feed.
     *
     * @param sfeed feed.
     */
    public void updateSearchFeed(final SearchFeed sfeed)
    {
        if (sfeed == null) return;
        
        new Thread(THREAD_NAME_SEARCH_QUERY)
        {
            public void run()
            {
                searchFeedsManager.runQuery(sfeed);
            }
        }.start();
    }

    /**
     * Chooses guide from existing or creates new guide if none present for a new feed.
     * <ul>
     *  <li>If the guide is currently selected then it will be chosen.</li>
     *  <li>If it's there's no selected guide, but there are guides in the set,
     *      the first from them is chosen.</li>
     *  <li>If there are no guides, the new one is created with default name.</li>
     * </ul>
     *
     * @return guide.
     *
     * @see #AUTO_GUIDE_TITLE
     */
    private StandardGuide chooseOrMakeGuideForNewFeed()
    {
        IGuide guide = model.getSelectedGuide();

        if (guide == null)
        {
            // If guide isn't selected then select first guide from set or create new one.
            GuidesSet guidesSet = getModel().getGuidesSet();
            if (guidesSet.getGuidesCount() == 0)
            {
                guide = createStandardGuide(AUTO_GUIDE_TITLE, null, false);

                // If guide wasn't created -- report
                if (guide == null) LOG.severe(Strings.error("failed.to.automatically.create.a.new.guide"));
            } else
            {
                guide = guidesSet.getGuideAt(0);
            }
        }

        if (guide == null) throw new RuntimeException(Strings.error("failed.to.create.new.guide"));

        // TODO for now we have only StandardGuide's
        return (StandardGuide)guide;
    }

    /**
     * Repaints all highlights in articles list immediately.
     */
    public void repaintArticlesListHighlights()
    {
        MainFrame frame = getMainFrame();
        if (frame != null) frame.repaintArticlesListHighlights();
    }

    /**
     * Adds the feed with defined dataUrl to the specified position in guide. The meta-data
     * repository will not be questioned for discovery, but will be questioned for existing
     * meta-data object. If there's no meta-data yet, then the object will be created.
     *
     * @param guide guide.
     * @param dataUrl data URL.
     * @param aList reading list.
     *
     * @return new feed object.
     */
    public DirectFeed addDirectFeed(StandardGuide guide, URL dataUrl, ReadingList aList)
    {
        if (dataUrl == null) throw new NullPointerException(Strings.error("unspecified.data.url"));

        DirectFeed feed = getModel().getGuidesSet().findDirectFeed(dataUrl);
        if (feed == null)
        {
            feed = new DirectFeed();
            feed.setXmlURL(dataUrl);
        }

        if (aList != null) aList.add(feed); else guide.add(feed);

        return feed;
    }

    /**
     * Updates the feed immediately if the feed is discovered according to its meta-data.
     *
     * @param feed feed.
     */
    public void updateIfDiscovered(DirectFeed feed)
    {
        FeedMetaDataHolder md = feed.getMetaDataHolder();
        if (md != null && md.isDiscoveredValid())
        {
            poller.update(feed, false);
        }
    }

    /**
     * Removes feed from the holder guide, but not from the storage. Notifies model.
     * This method is useful when removing feeds which are not added to the db yet
     * (undiscovered or invalid).
     *
     * @param feed feed to remove.
     */
    public void deleteNonPersistentFeed(IFeed feed)
    {
        IGuide[] guides = feed.getParentGuides();

        if (guides.length != 0)
        {
            if (feed instanceof DirectFeed) ((DirectFeed)feed).setMetaData(null);
            for (IGuide guide : guides) guide.remove(feed);
        }
    }

    /**
     * Starts discovery of link from article's text.
     *
     * @param url link.
     *
     * @return meta-data of channel.
     */
    public FeedMetaDataHolder discoverLinkFromArticle(URL url)
    {
        return metaDataManager.lookup(url);
    }

    /**
     * Discovers meta-data for the link.
     *
     * @param link          link.
     *
     * @return meta-data object.
     */
    public FeedMetaDataHolder discover(URL link)
    {
        return metaDataManager.lookupOrDiscover(link);
    }

    /**
     * Schedules the discovery of feeds in all feeds of the guide.
     *
     * @param guide guide to analyze.
     */
    public void discoverFeedsIn(IGuide guide)
    {
        IFeed[] feeds = guide.getFeeds();
        for (IFeed feed : feeds) discoverFeedsIn(feed);
    }

    /**
     * Schedules the discovery of feeds in all articles of the feed.
     *
     * @param feed feed to analyze.
     */
    public void discoverFeedsIn(IFeed feed)
    {
        if (feed == null) return;

        IArticle[] articles = feed.getArticles();
        for (IArticle article : articles) discoverFeedsIn(article, feed);
    }

    /**
     * Creates requested-by string from feed and its holder guide.
     *
     * @param feed feed.
     *
     * @return string.
     */
    public static String prepareRequestedByFromFeed(IFeed feed)
    {
        StringBuffer buf = new StringBuffer();

        IGuide[] guides = feed.getParentGuides();
        if (guides.length > 0) buf.append(GuidesUtils.getGuidesNames(guides)).append(" / ");
        buf.append(feed.getTitle());

        return buf.toString();
    }

    /**
     * Schedules the discovery of feeds met in the article.
     *
     * @param article   article with links.
     * @param feed      feed - source of request to record in new meta-data wrappers.
     */
    public void discoverFeedsIn(IArticle article, IFeed feed)
    {
        String requestedBy = prepareRequestedByFromFeed(feed);

        // Find the base URL for resolution of relative links
        URL baseUrl = article.getLink();
        if (baseUrl == null && feed instanceof DirectFeed) baseUrl = ((DirectFeed)feed).getXmlURL();

        // Find all links in the article text
        Collection<String> links = article.getLinks();
        if (links == null) return;

        // Request the discovery of all links found one by one
        for (String link : links)
        {
            try
            {
                FeedMetaDataHolder cmd = discover(new URL(baseUrl, link));
                if (cmd.getRequestedBy() == null) cmd.setRequestedBy(requestedBy);
            } catch (MalformedURLException e)
            {
                // No problems about that. Just invalid link in the article.
            }
        }
    }

    /**
     * When it happens that the reference was not discovered we need to
     * tell user about it and give him an opportunity to change his original
     * reference or suggest correct data URL.
     *
     * @param holder        holder of meta-data.
     * @param feed          feed corresponding to this wrapper.
     * @param originalURL   original URL used to discover meta-data.
     *
     * @return <code>TRUE</code> if no processing was done.
     */
    private boolean processInvalidDiscovery(FeedMetaDataHolder holder,
                                            final DirectFeed feed, URL originalURL)
    {
        // we need to ask what to do with current discovery:
        // 1. leave as is - invalid
        // 2. enter the other URL
        // 3. suggest correct data URL

        feed.setInvalidnessReason(Strings.message("feed.invalidness.reason.undiscovered"));

        boolean processingDone = false;

        // If the feed is a part of reading list we can't modify or remove it
        if (feed.getReadingLists().length > 0) return processingDone;

        InvalidDiscoveryDialog dialog = createDialog();
        dialog.setTitle(Strings.message("subscription.error.dialog.title"));

        URL newDiscoveryUrl = originalURL;
        String suggestedUrl = null;
        boolean localReference = MDDiscoveryRequest.isLocalURL(originalURL);

        boolean inputAccepted = false;
        while (!inputAccepted)
        {
            GlobalController.IDDResult res = openDialog(dialog, newDiscoveryUrl, suggestedUrl, localReference);
            int option = res.option;
            inputAccepted = true;

            if (!res.hasBeenCanceled)
            {
                newDiscoveryUrl = res.newDiscoveryURL;
                suggestedUrl = res.newSuggestionURL;

                if (option == InvalidDiscoveryDialog.OPTION_NEW_DISCOVERY)
                {
                    inputAccepted = false;
                    // Check if input is accepted
                    if (newDiscoveryUrl != null)
                    {
                        // Initiate new discovery
                        processingDone = true;
                        inputAccepted = true;

                        IGuide[] guides = feed.getParentGuides();
                        deleteNonPersistentFeed(feed);

                        if (guides != null && guides.length > 0)
                        {
                            DirectFeed newFeed = null;
                            for (IGuide guide : guides)
                            {
                                if (newFeed == null)
                                {
                                    newFeed = createDirectFeed(guide, newDiscoveryUrl);
                                } else
                                {
                                    guide.add(newFeed);
                                }
                            }
                        } else
                        {
                            createDirectFeed(getModel().getSelectedGuide(), newDiscoveryUrl);
                        }
                    }
                } else if (option == InvalidDiscoveryDialog.OPTION_SUGGEST_URL)
                {
                    // Check URL and suggest if it's recognizable
                    try
                    {
                        // Check if we have correct URL
                        URL url = new URL(StringUtils.fixURL(suggestedUrl));
                        DirectDiscoverer dd = new DirectDiscoverer();
                        DiscoveryResult result = dd.discover(url);
                        if (result != null)
                        {
                            ServerService.metaSuggestFeedUrl(originalURL.toString(), suggestedUrl);

                            // set newly discovered URL and mark the data as no longer invalid
                            holder.setXmlURL(url);
                            holder.setInvalid(false);

                            // reset processing flag to continue as if nothing happened
                            processingDone = false;
                        } else
                        {
                            // The specified url isn't pointing to the feed
                            inputAccepted = false;
                        }
                    } catch (MalformedURLException e)
                    {
                        // User supplied malformed URL -- too bad
                        inputAccepted = false;
                    } catch (UrlDiscovererException e)
                    {
                        // Possibly communication error
                        inputAccepted = false;
                    }
                } else if (option == InvalidDiscoveryDialog.OPTION_CANCEL)
                {
                    // Remove invalid feed
                    processingDone = true;

                    deleteNonPersistentFeed(feed);
                }
            }
        }

        return processingDone;
    }

    /**
     * Temporary dialog results holder.
     */
    private static class IDDResult
    {
        private int option;
        private boolean hasBeenCanceled;
        private URL newDiscoveryURL;
        private String newSuggestionURL;
    }

    /**
     * Opens the dialog in EDT and returns the package of results.
     *
     * @param dialog            dialog to open.
     * @param newDiscoveryUrl   new discovery URL.
     * @param suggestedUrl      current suggestion.
     * @param localReference    <code>TRUE</code> when a link is local.
     *
     * @return results.
     */
    private static IDDResult openDialog(final InvalidDiscoveryDialog dialog, final URL newDiscoveryUrl,
                                        final String suggestedUrl, final boolean localReference)
    {
        final IDDResult result = new IDDResult();

        Runnable task = new Runnable()
        {
            public void run()
            {
                result.option = dialog.open(newDiscoveryUrl, suggestedUrl, !localReference);
                result.hasBeenCanceled = dialog.hasBeenCanceled();
                result.newDiscoveryURL = dialog.getNewDiscoveryUrl();
                result.newSuggestionURL = dialog.getSuggestedFeedUrl();
            }
        };

        UifUtilities.invokeAndWait(task, "Failed to open dialog.", Level.SEVERE);

        return result;
    }


    /**
     * Creating invalid discovery dialog in EDT.
     *
     * @return dialog.
     */
    private InvalidDiscoveryDialog createDialog()
    {
        final ValueHolder vh = new ValueHolder();
        Runnable task = new Runnable()
        {
            public void run()
            {
                InvalidDiscoveryDialog dialog = new InvalidDiscoveryDialog(getMainFrame());
                synchronized (vh)
                {
                    vh.setValue(dialog);
                }
            }
        };

        UifUtilities.invokeAndWait(task, "Failed to create invalid discovery dialog.", Level.SEVERE);

        return (InvalidDiscoveryDialog)vh.getValue();
    }

    /**
     * Registeres currently hovered link.
     *
     * @param link hovered link or <code>NULL</code>.
     */
    public void setHoveredHyperLink(URL link)
    {
        if (hoveredLink != link)
        {
            hoveredLink = link;
            setStatus(link == null ? "" : link.toString());
        }
    }

    /**
     * Returns hovered hyper-link URL or <code>NULL</code>.
     *
     * @return link or <code>NULL</code>.
     */
    public URL getHoveredHyperLink()
    {
        return hoveredLink;
    }

    /**
     * Get the feed by currently hovered hyper-link URL.
     *
     * @return feed or <code>NULL</code> if not found.
     */
    public NetworkFeed getFeedByHoveredHyperLink()
    {
        final URL url = getHoveredHyperLink();

        NetworkFeed feed = null;
        if (url != null)
        {
            FeedMetaDataHolder metaData = discoverLinkFromArticle(url);

            if (metaData != null)
            {
                URL xmlURL = metaData.getXmlURL();
                feed = getModel().getGuidesSet().findDirectFeed(xmlURL);
            }
        }

        return feed;
    }

    /**
     * Returns list of selected guides.
     *
     * @return guides.
     */
    public IGuide[] getSelectedGuides()
    {
        GuidesList guidesList = getMainFrame().getGudiesPanel().getGuidesList();
        Object[] guidesO = guidesList.getSelectedValues();

        IGuide[] guides = new IGuide[guidesO.length];
        for (int i = 0; i < guidesO.length; i++)
        {
            guides[i] = (IGuide)guidesO[i];
        }

        return guides;
    }

    /**
     * Returns list of selected feeds.
     *
     * @return feeds.
     */
    public IFeed[] getSelectedFeeds()
    {
        JList feedsList = getMainFrame().getFeedsPanel().getFeedsList();
        Object[] feedsO = feedsList.getSelectedValues();

        IFeed[] feeds = new IFeed[feedsO.length];
        for (int i = 0; i < feedsO.length; i++)
        {
            feeds[i] = (IFeed)feedsO[i];
        }

        return feeds;
    }

    /**
     * Shows new publishing dialog only if it's enabled.
     */
    public void showNewPublishingDialog()
    {
        UserPreferences prefs = model.getUserPreferences();
        if (prefs.isShowingNewPubAlert())
        {
            NewPublicationDialog newPublicationDialog = new NewPublicationDialog(getMainFrame());
            newPublicationDialog.open();

            prefs.setShowingNewPubAlert(!newPublicationDialog.isDoNotShowAgain());

            if (!newPublicationDialog.hasBeenCanceled())
            {
                SyncFullAction.getInstance().actionPerformed(null);
            }
        }
    }

    /**
     * Adapts navigation to the controller.
     */
    private class NavigatorAdapter implements IArticleListNavigationListener
    {
        /**
         * Invoked when list component notifies model about that there's no articles left to switch
         * on and next feed required.
         *
         * @param mode of selecting next feed.
         *
         * @see com.salas.bb.views.INavigationModes#MODE_NORMAL
         * @see com.salas.bb.views.INavigationModes#MODE_UNREAD
         */
        public void nextFeed(int mode)
        {
            NavigatorAdv.NavigationInfoKey key;

            switch (mode)
            {
                case INavigationModes.MODE_NORMAL:
                    key = NavigatorAdv.NavigationInfoKey.NEXT;
                    break;
                case INavigationModes.MODE_UNREAD:
                    key = NavigatorAdv.NavigationInfoKey.NEXT_UNREAD;
                    break;
                default:
                    key = null;
                    break;
            }

            if (key == null)
            {
                LOG.severe(MessageFormat.format(Strings.error("invalid.next.mode"), mode));
            } else
            {
                NavigatorAdv.Destination dest = navigator.getDestination(key);
                selectDestination(dest, true, mode);
            }
        }

        /**
         * Invoked when list component notifies model about that there's no articles left to switch
         * on and previous feed required.
         *
         * @param mode of selecting next feed.
         *
         * @see com.salas.bb.views.INavigationModes#MODE_NORMAL
         * @see com.salas.bb.views.INavigationModes#MODE_UNREAD
         */
        public void prevFeed(int mode)
        {
            NavigatorAdv.NavigationInfoKey key;

            switch (mode)
            {
                case INavigationModes.MODE_NORMAL:
                    key = NavigatorAdv.NavigationInfoKey.PREV;
                    break;
                case INavigationModes.MODE_UNREAD:
                    key = NavigatorAdv.NavigationInfoKey.PREV_UNREAD;
                    break;
                default:
                    key = null;
                    break;
            }

            if (key == null)
            {
                LOG.severe(MessageFormat.format(Strings.error("invalid.prev.mode"), mode));
            } else
            {
                NavigatorAdv.Destination dest = navigator.getDestination(key);
                selectDestination(dest, false, mode);
            }
        }

        private void selectDestination(final NavigatorAdv.Destination dest,
                                       final boolean next, final int mode)
        {
            if (dest == null)
            {
                if (getModel().getUserPreferences().isSoundOnNoUnread()) Sound.play("sound.no.unread");
                return;
            }

            final IGuide guide = dest.guide;
            final IFeed feed = dest.feed;

            if (LOG.isLoggable(Level.FINE))
            {
                LOG.fine("Next: Guide=" + guide.getTitle() + " Feed=" + feed.getTitle());
            }

            Runnable task = new Runnable()
            {
                public void run()
                {
                    selectGuide(guide, false);
                    selectFeed(feed);

                    final MainFrame frame = getMainFrame();
                    final IFeedDisplay feedDisplay = frame.getArticlesListPanel().getFeedView();

                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            if (next)
                            {
                                feedDisplay.selectFirstArticle(mode);
                            } else
                            {
                                feedDisplay.selectLastArticle(mode);
                            }
                        }
                    });
                }
            };

            if (UifUtilities.isEDT()) task.run(); else SwingUtilities.invokeLater(task);
        }
    }

    /**
     * Listens for discovery events.
     */
    private class DiscoveryListener implements IDiscoveryListener
    {
        private static final int MAX_CHARS_IN_TOOLTIP_REFERENCE = 40;

        private Map<URL, ActivityTicket> wrapperToTicket = new IdentityHashMap<URL, ActivityTicket>();

        /**
         * Invoked when discovery of some meta-data object started.
         *
         * @param url URL being discovered.
         */
        public void discoveryStarted(URL url)
        {
            String urlString = url.toString();

            // Get activity ticket
            if (urlString.length() > MAX_CHARS_IN_TOOLTIP_REFERENCE)
            {
                urlString = urlString.substring(0, MAX_CHARS_IN_TOOLTIP_REFERENCE) + "\u2026";
            }

            wrapperToTicket.put(url, ActivityIndicatorView.startDiscovery(urlString));
        }

        /**
         * Invoked when discovery of some meta-data object successfully finished.
         *
         * @param url       URL has been discovered.
         * @param complete  <code>TRUE</code> when discovery is complete and there will be no
         *                  rediscovery scheduled.
         */
        public void discoveryFinished(URL url, boolean complete)
        {
            finishDiscoveryIndication(url);

            if (!complete) return;

            FeedMetaDataHolder holder = metaDataManager.lookup(url);
            DirectFeed[] feeds = findFeedsWatchingMetaData(holder);

            if (feeds.length > 0)
            {
                URL xmlUrl = holder.getXmlURL();
                DirectFeed existingFeed = xmlUrl == null ? null : getModel().getGuidesSet().findDirectFeed(xmlUrl);

                for (DirectFeed feed : feeds)
                {
                    boolean proceed = true;
                    boolean existing = false;

                    // If this feed is not new (just rediscovery), continue
                    if ((existingFeed == null || existingFeed == feed) && feed.isInitialized())
                    {
                        existingFeed = feed;
                        continue;
                    }

                    if (holder.isDiscoveredInvalid() && !feed.isInitialized())
                    {
                        proceed = !processInvalidDiscovery(holder, feed, url);
                    }

                    if (holder.isDiscoveredValid())
                    {
                        // Discovered correctly
                        if (existingFeed != null)
                        {
                            if (existingFeed != feed)
                            {
                                IFeed selectedFeed = getModel().getSelectedFeed();
                                boolean thisFeedSelected = feed == selectedFeed;
                                GuidesSet.replaceFeed(feed, existingFeed);

                                if (thisFeedSelected)
                                {
                                    final IFeed selectFeed = existingFeed;
                                    SwingUtilities.invokeLater(new Runnable()
                                    {
                                        public void run()
                                        {
                                            selectFeed(selectFeed);
                                        }
                                    });
                                }

                                existing = true;
                            }
                        } else
                        {
                            existingFeed = feed;
                            feed.setXmlURL(xmlUrl);
                        }
                    }

                    if (existing && !feed.isInitialized()) checkForDuplicates(feed);
                    if (!existing && proceed) updateIfDiscovered(feed);
                }
            }

            // Update all highlights to reflect new state of the link
            if (holder.isDiscoveredValid()) repaintArticlesListHighlights();
        }

        /**
         * Returns the list of all feeds watching given holder.
         *
         * @param aHolder holder.
         *
         * @return feeds.
         */
        private DirectFeed[] findFeedsWatchingMetaData(FeedMetaDataHolder aHolder)
        {
            List<DirectFeed> watchers = new ArrayList<DirectFeed>();

            List<IFeed> feeds = model.getGuidesSet().getFeeds();
            for (IFeed feed : feeds)
            {
                if (feed instanceof DirectFeed)
                {
                    DirectFeed dfeed = (DirectFeed)feed;
                    if (dfeed.getMetaDataHolder() == aHolder) watchers.add(dfeed);
                }
            }

            return watchers.toArray(new DirectFeed[watchers.size()]);
        }

        /**
         * Invoked when discovery of some meta-data object failed.
         *
         * @param url URL has been failed to discover.
         */
        public void discoveryFailed(URL url)
        {
            finishDiscoveryIndication(url);
        }

        /**
         * Finishes indication of discovery of given URL.
         *
         * @param url URL.
         */
        private void finishDiscoveryIndication(URL url)
        {
            final ActivityTicket ticket = wrapperToTicket.get(url);
            if (ticket != null)
            {
                ActivityIndicatorView.finishActivity(ticket);
                wrapperToTicket.remove(url);
            }
        }
    }

    /**
     * Listens to events from selected feed.
     */
    private class SelectedFeedListener extends FeedAdapter
    {
        /**
         * Called when some article is added to the feed.
         *
         * @param feed    feed.
         * @param article article.
         */
        public void articleAdded(IFeed feed, IArticle article)
        {
            for (IControllerListener listener : listeners)
            {
                try
                {
                    listener.articleAdded(article, feed);
                } catch (Exception e)
                {
                    LOG.log(Level.SEVERE, Strings.error("unhandled.exception"), e);
                }
            }
        }

        /**
         * Called when some article is removed from the feed.
         *
         * @param feed    feed.
         * @param article article.
         */
        public void articleRemoved(IFeed feed, IArticle article)
        {
            for (IControllerListener listener : listeners)
            {
                try
                {
                    listener.articleRemoved(article, feed);
                } catch (Exception e)
                {
                    LOG.log(Level.SEVERE, Strings.error("unhandled.exception"), e);
                }
            }
        }

        /**
         * Called when information in feed changed.
         *
         * @param feed     feed.
         * @param property property of the feed.
         * @param oldValue old property value.
         * @param newValue new property value.
         */
        public void propertyChanged(final IFeed feed, String property, Object oldValue, Object newValue)
        {
            if (IFeed.PROP_TITLE.equals(property))
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        getMainFrame().updateTitle(feed);
                    }
                });
            } else if (SearchFeed.PROP_QUERY.equals(property) && model.getSelectedFeed() == feed)
            {
                if (updateSearchHighlights(feed, feed)) repaintArticlesListHighlights();
            }
        }
    }

    /**
     * Takes credentials from service preferences.
     */
    private class BBServiceCredentialCallback implements ICredentialsCallback
    {
        /**
         * Invoked when storage needs to know current user name.
         *
         * @return user name or <code>NULL</code> if service is disabled.
         */
        public String getUserName()
        {
            String userName = null;
            if (model != null)
            {
                userName = model.getServicePreferences().getEmail();
                if (StringUtils.isEmpty(userName)) userName = null;
            }

            return userName;
        }

        /**
         * Invoked when storage needs to know current user password.
         *
         * @return user password or <code>NULL</code> if service is disabled.
         */
        public String getUserPassword()
        {
            String password = null;
            if (model != null)
            {
                password = model.getServicePreferences().getPassword();
                if (StringUtils.isEmpty(password)) password = null;
            }

            return password;
        }
    }

    /**
     * Takes credentials from user preferences.
     */
    private class UserPreferencesCallback implements ICredentialsCallback
    {
        /**
         * Invoked when service handler needs to know current user name.
         *
         * @return user name or <code>NULL</code> if service is disabled.
         */
        public String getUserName()
        {
            String userName = null;
            if (model != null)
            {
                userName = model.getUserPreferences().getTagsDeliciousUser();
                if (StringUtils.isEmpty(userName)) userName = null;
            }

            return userName;
        }

        /**
         * Invoked when service handler needs to know current user password.
         *
         * @return user password or <code>NULL</code> if service is disabled.
         */
        public String getUserPassword()
        {
            String password = null;
            if (model != null)
            {
                password = model.getUserPreferences().getTagsDeliciousPassword();
                if (StringUtils.isEmpty(password)) password = null;
            }

            return password;
        }
    }

    /**
     * Selects the guide.
     */
    private class SelectGuideTask implements Runnable
    {
        private final IGuide guide;
        private final boolean selectFeed;
        private final boolean alreadySelected;

        /**
         * Creates task to select the guide.
         *
         * @param aGuide            guide to select.
         * @param aSelectFeed       <code>TRUE</code> to select feed.
         * @param aAlreadySelected  <code>TRUE</code> if guide is already selected and only event firing required.
         */
        public SelectGuideTask(IGuide aGuide, boolean aSelectFeed, boolean aAlreadySelected)
        {
            guide = aGuide;
            selectFeed = aSelectFeed;
            alreadySelected = aAlreadySelected;
        }

        /**
         * Selects guide, fires event and selects feed (if necessary).
         */
        public void run()
        {
            if (!alreadySelected) model.setSelectedGuide(guide);

            // We need to fire this event anyway because the name of initially
            // selected guide should appear in the headers
            fireGuideSelected(guide);

            if (!alreadySelected)
            {
                IFeed feed = null;

                int gsm = model.getUserPreferences().getGuideSelectionMode();
                if (selectFeed && gsm != UserPreferences.GSM_NO_FEED)
                {
                    feed = gsm == UserPreferences.GSM_FIRST_FEED
                        ? model.getGuideModel().getSize() == 0
                            ?  null : (IFeed)model.getGuideModel().getElementAt(0)
                        : model.getSelectedFeed();
                }

                selectFeed(feed);
            }

            // STATS: Report the guide selection
            PersistenceManagerConfig.getManager().getStatisticsManager().guideVisited(guide);
        }
    }
}
