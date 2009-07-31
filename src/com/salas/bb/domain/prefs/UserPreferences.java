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
// $Id: UserPreferences.java,v 1.45 2008/02/28 15:59:52 spyromus Exp $
//

package com.salas.bb.domain.prefs;

import com.jgoodies.binding.beans.Model;
import com.salas.bb.core.CustomProxySelector;
import com.salas.bb.domain.AbstractArticle;
import com.salas.bb.domain.FeedClass;
import com.salas.bb.domain.ReadingList;
import com.salas.bb.remixfeeds.prefs.BloggingPreferences;
import com.salas.bb.twitter.TwitterPreferences;
import com.salas.bb.utils.Constants;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 * Preferences Object for BlogBridge.
 */
public final class UserPreferences extends Model
{
    /** Key for <code>markReadWhenChangingChannels</code> preference. */
    public static final String KEY_MARK_READ_WHEN_CHANGING_CHANNELS =
        "state.markReadWhenChangingChannels";
    /** Property name for <code>markReadWhenChangingChannels</code> preference. */
    public static final String PROP_MARK_READ_WHEN_CHANGING_CHANNELS =
        "markReadWhenChangingChannels";

    /** Key for <code>markReadWhenChangingGuides</code> preference. */
    public static final String KEY_MARK_READ_WHEN_CHANGING_GUIDES =
        "state.markReadWhenChangingGuides";
    /** Property name for <code>markReadWhenChangingGuides</code> preference. */
    public static final String PROP_MARK_READ_WHEN_CHANGING_GUIDES =
        "markReadWhenChangingGuides";

    /** Key for <code>backgroundDebugMode</code> preference. */
    public static final String KEY_BACKGROUND_DEBUG_MODE = "state.backgroundDebugMode";
    /** Background debug mode property. */
    public static final String PROP_BACKGROUND_DEBUG_MODE = "backgroundDebugMode";

    /** Key for <code>autoPurgeInterval</code> preference. */
    public static final String KEY_AUTO_PURGE_INTERVAL_MINUTES = "state.autoPurgeIntervalMinutes";
    /** Auto-purge interval in minutes property. */
    public static final String PROP_AUTO_PURGE_INTERVAL_MINUTES = "autoPurgeIntervalMinutes";

    /** Key for <code>purgeCount</code> preference. */
    public static final String KEY_PURGE_COUNT = "state.purgeCount";
    /** Property name for <code>purgeCount</code> preference. */
    public static final String PROP_PURGE_COUNT = "purgeCount";

    /** Key for <code>webStatInterval</code> preference. */
    public static final String KEY_WEBSTAT_INTERVAL = "state.webStatIntervalMinutes";
    /** Name of property holding interval between web stats collection runs. */
    public static final String PROP_WEBSTAT_INTERVAL = "webStatIntervalString";

    /** Key for <code>rssPollInterval</code> preference. */
    public static final String KEY_RSS_POLL_MIN = "state.rssPollIntervalMinutes";
    /** Property name for <code>rssPollInterval</code> preference. */
    public static final String PROP_RSS_POLL_MIN = "rssPollInterval";

    /** Key for <code>internetBrowser</code> preference. */
    public static final String KEY_INTERNET_BROWSER = "state.internetBrowser";
    /** Name of property holding internet browser command. */
    public static final String PROP_INTERNET_BROWSER = "internetBrowser";

    /** Key for <code>markReadAfterDelay</code> preference. */
    public static final String KEY_MARK_READ_AFTER_DELAY = "state.markReadAfterDelay";
    /** Property name for <code>markReadAfterDelay</code> preference. */
    public static final String PROP_MARK_READ_AFTER_DELAY = "markReadAfterDelay";

    /** Key for <code>markReadAfterSeconds</code> preference. */
    public static final String KEY_MARK_READ_AFTER_SECONDS = "state.markReadAfterSeconds";
    /** Property name for <code>markReadAfterSeconds</code> preference. */
    public static final String PROP_MARK_READ_AFTER_SECONDS = "markReadAfterSeconds";

    /** Key for <code>usingPersistence</code>preference. */
    public static final String KEY_USE_PERSISTENCE = "state.usingPersistence";
    /** Name of property holding flag of whether to use persistence or no. */
    public static final String PROP_USE_PERSISTENCE = "usingPersistence";

    /** Key for <code>goodChannelStarz</code> preference. */
    public static final String KEY_GOOD_CHANNEL_STARZ = "state.goodChannelStarz";
    /** Name of property holding minimum number of starz for channel to be good. */
    public static final String PROP_GOOD_CHANNEL_STARZ = "goodChannelStarz";

    /** If sorting of feeds enabled. */
    public static final String PROP_SORTING_ENABLED = "sortingEnabled";
    /** Class of primary sort. */
    public static final String PROP_SORT_BY_CLASS_1 = "sortByClass1";
    /** Reverse primary sort order. */
    public static final String PROP_REVERSED_SORT_BY_CLASS_1 = "reversedSortByClass1";
    /** Class of secondary sort. */
    public static final String PROP_SORT_BY_CLASS_2 = "sortByClass2";
    /** Reverse secondary sort order. */
    public static final String PROP_REVERSED_SORT_BY_CLASS_2 = "reversedSortByClass2";

    /** Delay between selection of cell and actual feed selection event. */
    public static final String PROP_FEED_SELECTION_DELAY = "feedSelectionDelay";

    /** Preserve unread articles from cleaning. */
    public static final String PROP_PRESERVE_UNREAD = "preserveUnread";

    /** Copy article links to clipboard in HREF format. */
    public static final String PROP_COPY_LINKS_IN_HREF_FORMAT = "copyLinksInHrefFormat";

    /** Show all text anti-aliased. */
    public static final String PROP_AA_TEXT = "antiAliasText";

    /** Tags storage property name. */
    public static final String PROP_TAGS_STORAGE = "tagsStorage";
    /** Tagging system auto-fetch. */
    public static final String PROP_TAGS_AUTOFETCH = "tagsAutoFetch";

    /** Delicious user password. */
    public static final String PROP_TAGS_DELICIOUS_PASSWORD = "tagsDeliciousPassword";
    /** Delicious user name. */
    public static final String PROP_TAGS_DELICIOUS_USER = "tagsDeliciousUser";

    /** Do not store tags remotely. */
    public static final int TAGS_STORAGE_NONE       = 0;
    /** Store tags at BB Service. */
    public static final int TAGS_STORAGE_BB_SERVICE = 1;
    /** Store tags at del.icio.us. */
    public static final int TAGS_STORAGE_DELICIOUS  = 2;

    /** Checking for updates on startup property. */
    public static final String PROP_CHECKING_FOR_UPDATES_ON_STARTUP = "checkingForUpdatesOnStartup";

    /** Use English as the interface language. */
    public static final String PROP_ALWAYS_USE_ENGLISH = "alwaysUseEnglish";
    private boolean alwaysUseEnglish = DEFAULT_ALWAYS_USE_ENGLISH;

    /** Toolbar labels enableness state. */
    public static final String PROP_SHOW_TOOLBAR_LABELS = "showToolbarLabels";
    /** There's a menu when user presses over the unread button in the feeds and guides lists. */
    public static final String PROP_SHOW_UNREAD_BUTTON_MENU = "showUnreadButtonMenu";

    /** Show / hide toolbar. */
    public static final String PROP_SHOW_TOOLBAR = "showToolbar";

    /** Get Latest commands over the guide(s) should update reading lists. */
    public static final String PROP_UPDATE_READING_LISTS = "updateReadingLists";
    /** Get Latest commands over the guide(s) should update feeds. */
    public static final String PROP_UPDATE_FEEDS = "updateFeeds";

    /**
     * Update period of reading list.
     *
     * @see ReadingList#PERIOD_ONCE_PER_RUN
     * @see ReadingList#PERIOD_NEVER
     * @see ReadingList#PERIOD_DAILY
     * @see ReadingList#PERIOD_DAILY
     */
    public static final String PROP_READING_LIST_UPDATE_PERIOD = "readingListUpdatePeriod";

    /** Limitation on number of feeds to import. */
    public static final String PROP_FEED_IMPORT_LIMIT = "feedImportLimit";

    /** Actions to take on a reading list update event. */
    public static final String PROP_ON_READING_LIST_UPDATE_ACTIONS = "onReadingListUpdateActions";
    /** Update a reading list silently. */
    public static final int RL_UPDATE_NONE      = 0;
    /** Update a reading list and show notification in the end. */
    public static final int RL_UPDATE_NOTIFY    = 1;
    /** Show a confirmation dialog before the updates of reading list. */
    public static final int RL_UPDATE_CONFIRM   = 2;

    // Defaults

    public static final boolean DEFAULT_MARK_READ_WHEN_CHANGING_CHANNELS = false;
    public static final boolean DEFAULT_MARK_READ_WHEN_CHANGING_GUIDES = false;
    private static final boolean DEFAULT_BACKGROUNDDEBUGMODE        = false;
    private static final int DEFAULT_PURGE_INTERVAL_MINUTES         = 60;
    private static final String DEFAULT_INTERNET_BROWSER            = Constants.EMPTY_STRING;
    public static final boolean DEFAULT_MARK_READ_AFTER_DELAY      = true;
    public static final int DEFAULT_MARK_READ_AFTER_SECONDS        = 5;
    private static final boolean DEFAULT_USE_PERSISTENCE            = true;
    private static final boolean DEFAULT_COPY_LINKS_IN_HREF_FORMAT  = false;
    private static final boolean DEFAULT_AA_TEXT                    = false;
    private static final int DEFAULT_TAGS_STORAGE                   = TAGS_STORAGE_BB_SERVICE;
    private static final boolean DEFAULT_TAGS_AUTOFETCH             = false;
    private static final String DEFAULT_TAGS_DELICIOUS_PASSWORD     = Constants.EMPTY_STRING;
    private static final String DEFAULT_TAGS_DELICIOUS_USER         = Constants.EMPTY_STRING;
    public static final boolean DEFAULT_CHECKING_FOR_UPDATES_ON_STARTUP = true;
    public static final boolean DEFAULT_SHOW_TOOLBAR_LABELS         = true;
    public static final boolean DEFAULT_SHOW_TOOLBAR                = true;
    public static final boolean DEFAULT_UPDATE_READING_LISTS        = true;
    public static final boolean DEFAULT_UPDATE_FEEDS                = true;
    private static final int DEFAULT_ON_READING_LIST_UPDATE_ACTIONS = RL_UPDATE_NONE;
    public static final boolean DEFAULT_ALWAYS_USE_ENGLISH         = false;

    /** Default for shoing the unread button menu on click. */
    public static final boolean DEFAULT_SHOW_UNREAD_BUTTON_MENU = true;

    /** Default web statistics update period in minutes. */
    public static final int DEFAULT_WEBSTAT_MINS = 240;
    /** Default number of articles to leave in the feed max. */
    public static final int DEFAULT_PURGE_COUNT = 30;
    /** Default feed polling interval (in minutes). */
    public static final int DEFAULT_RSS_POLL_MIN = 60;

    /** Feeds sorting default state. */
    public static final boolean DEFAULT_SORTING_ENABLED = true;
    /** Default primary sorting order class mask. */
    public static final int DEFAULT_SORT_BY_CLASS_1 = FeedClass.READ;
    /** Default primary sorting order direction. */
    public static final boolean DEFAULT_REVERSED_SORT_BY_CLASS_1 = false;
    /** Default secondary sorting order class mask. */
    public static final int DEFAULT_SORT_BY_CLASS_2 = FeedClass.LOW_RATED;
    /** Default secondary sorting order direction. */
    public static final boolean DEFAULT_REVERSED_SORT_BY_CLASS_2 = false;

    /** Deffult mode of viewing only good channels. */
    public static final boolean DEFAULT_SHOW_ONLY_GOOD_CHANNELS = true;

    /** Default number of starz in Good channels. */
    public static final int DEFAULT_GOOD_CHANNEL_STARZ = 1;

    /** Default feed selected delay (in ms). */
    private static final int DEFAULT_FEED_SELECTION_DELAY = 200;

    /** Default value for "preserve unread" flag. **/
    public static final boolean DEFAULT_PRESERVE_UNREAD = false;

    /** Default feed import limitation. */
    private static final int DEFAULT_FEED_IMPORT_LIMITATION = 100;
    /** Maximum for feed import limitation. */
    public static final int MAX_FEED_IMPORT_LIMITATION = 500;

    // If TRUE, then whenever selection moves to another Channel, we mark all Articles read.
    private boolean markReadWhenChangingChannels = DEFAULT_MARK_READ_WHEN_CHANGING_CHANNELS;

    // If TRUE, whenever selection moves to another Channel Guide,
    // we mark all Articles of all Channels as read.
    private boolean markReadWhenChangingGuides = DEFAULT_MARK_READ_WHEN_CHANGING_GUIDES;

    // If TRUE, Background processes are running in Debug mode.
    private boolean backgroundDebugMode = DEFAULT_BACKGROUNDDEBUGMODE;

    // Equals the interval in minutes between auto purges.
    private int autoPurgeInterval = DEFAULT_PURGE_INTERVAL_MINUTES;

    // Equals the number of articles to leave after auto purge.
    private int purgeCount = DEFAULT_PURGE_COUNT;

    // Equals the interval in minutes between webStatCollections.
    private int webStatInterval = DEFAULT_WEBSTAT_MINS;

    // Equals the interval in minutes between polling
    private int rssPollInterval = DEFAULT_RSS_POLL_MIN;

    // Path to user-specified internet browser.
    private String internetBrowser = DEFAULT_INTERNET_BROWSER;

    // If True, then selected article will be marked as read after some delay.
    private boolean markReadAfterDelay = DEFAULT_MARK_READ_AFTER_DELAY;

    // Number of seconds in delay before article selected and gets marked as read.
    private int markReadAfterSeconds = DEFAULT_MARK_READ_AFTER_SECONDS;

    // Use persistence to store data or in-memory model only.
    private boolean usingPersistence = DEFAULT_USE_PERSISTENCE;

    // Number of starz to consider channel as good.
    private int goodChannelStarz = DEFAULT_GOOD_CHANNEL_STARZ;

    // Show only good channels.
    private boolean showOnlyGoodChannels = DEFAULT_SHOW_ONLY_GOOD_CHANNELS;

    // If sorting of feeds enabled.
    private boolean sortingEnabled = DEFAULT_SORTING_ENABLED;

    // Class(es) to use for primary sorting.
    private int sortByClass1 = DEFAULT_SORT_BY_CLASS_1;

    // Is primary sorting should be in reversed order?
    private boolean reversedSortByClass1 = DEFAULT_REVERSED_SORT_BY_CLASS_1;

    // Class(es) to use for secondary sorting.
    private int sortByClass2 = DEFAULT_SORT_BY_CLASS_2;

    // Is seconday sorting should be in reversed order?
    private boolean reversedSortByClass2 = DEFAULT_REVERSED_SORT_BY_CLASS_2;

    // Delay between feed cell selection and actual event.
    private int feedSelectionDelay = DEFAULT_FEED_SELECTION_DELAY;

    // Preserve unread articles from cleaning.
    private boolean preserveUnread = DEFAULT_PRESERVE_UNREAD;

    // Copy article links to clipboard in HREF format.
    private boolean copyLinksInHrefFormat = DEFAULT_COPY_LINKS_IN_HREF_FORMAT;

    // Show all text anti-aliased
    private boolean antiAliasText = DEFAULT_AA_TEXT;

    /**
     * Tags storage type.
     *
     * @see #TAGS_STORAGE_NONE
     * @see #TAGS_STORAGE_BB_SERVICE
     * @see #TAGS_STORAGE_DELICIOUS
     */
    private int tagsStorage = DEFAULT_TAGS_STORAGE;
    /** Automatic fetching of shared tags when opening tags window. */
    private boolean tagsAutoFetch = DEFAULT_TAGS_AUTOFETCH;

    /** Name of del.icio.us service user. */
    private String tagsDeliciousUser = DEFAULT_TAGS_DELICIOUS_USER;
    /** Password of del.icio.us service user. */
    private String tagsDeliciousPassword = DEFAULT_TAGS_DELICIOUS_PASSWORD;

    /** Checking for updates on startup. */
    private boolean checkingForUpdatesOnStartup = DEFAULT_CHECKING_FOR_UPDATES_ON_STARTUP;

    /** Displaying toolbar labels. */
    private boolean showToolbarLabels = DEFAULT_SHOW_TOOLBAR_LABELS;

    /** Showing unread button menu. */
    private boolean showUnreadButtonMenu = DEFAULT_SHOW_UNREAD_BUTTON_MENU;

    /** Show / hide toolbar. */
    private boolean showToolbar = DEFAULT_SHOW_TOOLBAR;

    /**
     * Update period of reading list.
     *
     * @see ReadingList#PERIOD_ONCE_PER_RUN
     * @see ReadingList#PERIOD_NEVER
     * @see ReadingList#PERIOD_DAILY
     * @see ReadingList#PERIOD_DAILY
     */
    private long readingListUpdatePeriod = ReadingList.DEFAULT_PERIOD;

    /** Maximum number of feeds to import from OPML or some other place. */
    private int feedImportLimit = DEFAULT_FEED_IMPORT_LIMITATION;

    /** Get Latest commands over the guide(s) should update reading lists. */
    private boolean updateReadingLists = DEFAULT_UPDATE_READING_LISTS;
    /** Get Latest commands over the guide(s) should update feeds. */
    private boolean updateFeeds = DEFAULT_UPDATE_FEEDS;

    /**
     * Actions to take on a reading list update event.
     *
     * @see #RL_UPDATE_NONE
     * @see #RL_UPDATE_NOTIFY
     * @see #RL_UPDATE_CONFIRM
     */
    private int onReadingListUpdateActions = DEFAULT_ON_READING_LIST_UPDATE_ACTIONS;

    /** Guide selection mode -- what to do when a guide is selected. */
    public static final String PROP_GUIDE_SELECTION_MODE = "guideSelectionMode";
    /** Guide Selection Mode: Do not select feeds on guide selection. */
    public static final int GSM_NO_FEED = 0;
    /** Guide Selection Mode: Select first feed in a guide. */
    public static final int GSM_FIRST_FEED = 1;
    /** Guide Selection Mode: Select last seen feed. */
    public static final int GSM_LAST_SEEN_FEED = 2;
    /** Default Guide Selection Mode. */
    public static final int DEFAULT_GUIDE_SELECTION_MODE = GSM_LAST_SEEN_FEED;
    /** Guide selection mode: What to do when a guide is selected. */
    private int guideSelectionMode = DEFAULT_GUIDE_SELECTION_MODE;

    /** Proxy flag property name. */
    public static final String PROP_PROXY_ENABLED = "proxyEnabled";
    /** Proxy host property name. */
    public static final String PROP_PROXY_HOST = "proxyHost";
    /** Proxy port property name. */
    public static final String PROP_PROXY_PORT = "proxyPort";
    /** Proxy exclusions list. */
    public static final String PROP_PROXY_EXCLUSIONS = "proxyExclusions";

    /** Showing sync dialog on publication. */
    public static final String PROP_SHOWING_NEW_PUB_ALERT = "showingNewPubAlert";
    /** Default for new pub alert showing flag. */
    public static final boolean DEFAULT_SHOWING_NEW_PUB_ALERT = true;
    /** Flag of new publication alert dialog box showing. */
    private boolean showingNewPubAlert = DEFAULT_SHOWING_NEW_PUB_ALERT;

    /** Browse on double click property name. */
    public static final String PROP_BROWSE_ON_DBL_CLICK = "browseOnDblClick";
    /** Browse on double click default. */
    public static final boolean DEFAULT_BROWSE_ON_DBL_CLICK = false;
    /** When <code>TRUE</code> the browser window is opening on double click. */
    private boolean browseOnDblClick = DEFAULT_BROWSE_ON_DBL_CLICK;

    /** The name of last update time property. */
    private static final String PROP_LAST_UPDATE_TIME = "lastUpdateTime";
    /** The time when preferences were updated. */
    private Date lastUpdateTime;

    /** The name of the ping flag property. */
    public static final String PROP_PING_ON_RL_PUBLICATION = "pingOnReadingListPublication";
    private static final boolean DEFAULT_PING_ON_RL_PUBLICATION = false;
    private boolean pingOnReadingListPublication = DEFAULT_PING_ON_RL_PUBLICATION;

    /** The name of the ping URL property. */
    public static final String PROP_PING_ON_RL_PUBLICATION_URL = "pingOnReadingListPublicationURL";
    private static final String DEFAULT_PING_ON_RL_PUBLICATION_URL = "";
    private String pingOnReadingListPublicationURL = DEFAULT_PING_ON_RL_PUBLICATION_URL;

    /** The name of global notifications flag. */
    public static final String PROP_NOTIFICATIONS_ENABLED = "notificationsEnabled";
    private static final boolean DEFAULT_NOTIFICATIONS_ENABLED = true;
    private boolean notificationsEnabled = DEFAULT_NOTIFICATIONS_ENABLED;

    /** The name of the sound-on-new-articles flag. */
    public static final String PROP_SOUND_ON_NEW_ARTICLES = "soundOnNewArticles";
    private static final boolean DEFAULT_SOUND_ON_NEW_ARTICLES = false;
    private boolean soundOnNewArticles = DEFAULT_SOUND_ON_NEW_ARTICLES;

    /** The name of the sound-on-no-unread flag. */
    public static final String PROP_SOUND_ON_NO_UNREAD = "soundOnNoUnread";
    private static final boolean DEFAULT_SOUND_ON_NO_UNREAD = true;
    private boolean soundOnNoUnread = DEFAULT_SOUND_ON_NO_UNREAD;

    /** The name of global notifications period (in seconds, or -1 for "forever".). */
    public static final String PROP_NOTIFICATIONS_SHOW_PERIOD = "notificationsShowPeriod";
    private static final int DEFAULT_NOTIFICATIONS_SHOW_PERIOD = -1;
    private int notificationsShowPeriod = DEFAULT_NOTIFICATIONS_SHOW_PERIOD;

    /** The name of user defined list of no-discovery file extensions. */
    public static final String PROP_NO_DISCOVERY_EXTENSIONS = "noDiscoveryExtensions";
    private static final String DEFAULT_NO_DISCOVERY_EXTENSIONS = "";
    private String noDiscoveryExtensions = DEFAULT_NO_DISCOVERY_EXTENSIONS;

    /** The name of show icon in systray flag. */
    public static final String PROP_SHOW_APPICON_IN_SYSTRAY = "showAppIconInSystray";
    private static final boolean DEFAULT_SHOW_APPICON_IN_SYSTRAY = false;
    private boolean showAppIconInSystray = DEFAULT_SHOW_APPICON_IN_SYSTRAY;

    /** The name of minimize to systray flag. */
    public static final String PROP_MINIMIZE_TO_SYSTRAY = "minimizeToSystray";
    private static final boolean DEFAULT_MINIMIZE_TO_SYSTRAY = false;
    private boolean minimizeToSystray = DEFAULT_MINIMIZE_TO_SYSTRAY;

    /** View mode preferences. */
    private final ViewModePreferences viewModePreferences = new ViewModePreferences();

    /** Blogging preferences. */
    private final BloggingPreferences bloggingPreferences = new BloggingPreferences();

    /** Twitter preferences. */
    private final TwitterPreferences twitterPreferences = new TwitterPreferences();

    /** Mac OS X dock icon badge mode. */
    public static final int DIB_MODE_INVISIBLE = 0;
    public static final int DIB_MODE_SHOW_UNREAD_ARTICLES = 1;
    public static final int DIB_MODE_SHOW_UNREAD_FEEDS = 2;

    public static final String PROP_DIB_MODE = "dockIconBadgeMode";
    private static final int DEFAULT_DIB_BADGE_MODE = DIB_MODE_SHOW_UNREAD_ARTICLES;
    private int dockIconBadgeMode = DEFAULT_DIB_BADGE_MODE;// The list of properties we monitor to update the badge

    /**
     * The set of property names affecting the feed visibility in some way.
     */
    public static final Set FEED_VISIBILITY_PROPERTIES = new HashSet()
    {
        {
            add(PROP_GOOD_CHANNEL_STARZ);
        }
    };

    /** Transient property recording the last selected preferences dialog page. */
    private transient int selectedPrefsPage = -1;

    /** Number of sentences to show in a brief mode. */
    public static final String PROP_BRIEF_SENTENCES = "briefSentences";
    /** Number of characters to show minimum. */
    public static final String PROP_BRIEF_MAX_LENGTH = "briefMaxLength";
    /** Number of characters to show maximum. */
    public static final String PROP_BRIEF_MIN_LENGTH = "briefMinLength";

    /** Setting tags when pinning something. */
    public static final String PROP_PIN_TAGGING = "pinTagging";
    private static final boolean DEFAULT_PIN_TAGGING = false;
    private boolean pinTagging = DEFAULT_PIN_TAGGING;

    /** The list of tags to set when pinning. */
    public static final String PROP_PIN_TAGS = "pinTags";
    private static final String DEFAULT_PIN_TAGS = "";
    private String pinTags = DEFAULT_PIN_TAGS;

    /** Articles list page size. */
    public static final String PROP_PAGE_SIZE = "pageSize";
    private static final int DEFAULT_PAGE_SIZE = DEFAULT_PURGE_COUNT;
    private int pageSize = DEFAULT_PAGE_SIZE;

    /** What's hot: ignore patterns. */
    public static final String PROP_WH_IGNORE = "whIgnore";
    public static final String DEFAULT_WH_IGNORE = "http://technorati.com/*";
    private String whIgnore = DEFAULT_WH_IGNORE;

    /** What's hot: Don't count when a blog links to itself. */
    public static final String PROP_WH_NOSELFLINKS = "whNoSelfLinks";
    public static final boolean DEFAULT_WH_NOSELFLINKS = true;
    private boolean whNoSelfLinks = DEFAULT_WH_NOSELFLINKS;

    /** What's hot: Suppress hot links referred by the same source only. */
    public static final String PROP_WH_SUPPRESS_SAME_SOURCE_LINKS = "whSuppressSameSourceLinks";
    public static final boolean DEFAULT_WH_SUPPRESS_SAME_SOURCE_LINKS = true;
    private boolean whSuppressSameSourceLinks = DEFAULT_WH_SUPPRESS_SAME_SOURCE_LINKS;

    /** Target guide for the WH. */
    public static final String PROP_WH_TARGET_GUIDE = "whTargetGuide";
    public static final String DEFAULT_WH_TARGET_GUIDE = "";
    private String whTargetGuide = DEFAULT_WH_TARGET_GUIDE;

    /** The last time WH settings were changed. */
    public static final String PROP_WH_SETTINGS_CHANGE_TIME = "whSettingsChangeTime";
    private static final long DEFAULT_WH_SETTINGS_CHANGE_TIME = 0;
    private long whSettingsChangeTime = DEFAULT_WH_SETTINGS_CHANGE_TIME;

    /** Cleanup wizard auto-mode frequency (millisecs). */
    public static final String PROP_CW_FREQUENCY = "cwFrequency";
    private static final long DEFAULT_CW_FREQUENCY = 0;
    private long cwFrequency = DEFAULT_CW_FREQUENCY;

    /** Cleanup wizard last cleanup timestamp. */
    public static final String PROP_CW_LAST_CLEANUP = "cwLastCleanup";
    private static final long DEFAULT_CW_LAST_CLEANUP = 0;
    private long cwLastCleanup = DEFAULT_CW_LAST_CLEANUP;

    /**
     * Returns view mode preferences.
     *
     * @return view mode preferences.
     */
    public ViewModePreferences getViewModePreferences()
    {
        return viewModePreferences;
    }

    /**
     * Returns blogging preferences object.
     *
     * @return blogging preferences.
     */
    public BloggingPreferences getBloggingPreferences()
    {
        return bloggingPreferences;
    }

    /**
     * Returns twitter preferences.
     *
     * @return preferences.
     */
    public TwitterPreferences getTwitterPreferences()
    {
        return twitterPreferences;
    }

    /**
     * Returns TRUE when marking of channel as read required when changing channels.
     *
     * @return TRUE if marking required.
     */
    public boolean isMarkReadWhenChangingChannels()
    {
        return markReadWhenChangingChannels;
    }

    /**
     * Sets new value for flag. TRUE to mark channel as read when switching to the other.
     *
     * @param newValue new flag value.
     */
    public void setMarkReadWhenChangingChannels(boolean newValue)
    {
        boolean oldValue = isMarkReadWhenChangingChannels();
        markReadWhenChangingChannels = newValue;
        firePropertyChange(KEY_MARK_READ_WHEN_CHANGING_CHANNELS, oldValue, newValue);
    }

    /**
     * Returns TRUE when marking of guide as read required when changing guides.
     *
     * @return TRUE if marking required.
     */
    public boolean isMarkReadWhenChangingGuides()
    {
        return markReadWhenChangingGuides;
    }

    /**
     * Sets new value for flag. TRUE to mark guide as read when switching to the other.
     *
     * @param newValue new flag value.
     */
    public void setMarkReadWhenChangingGuides(boolean newValue)
    {
        boolean oldValue = isMarkReadWhenChangingGuides();
        markReadWhenChangingGuides = newValue;
        firePropertyChange(KEY_MARK_READ_WHEN_CHANGING_GUIDES, oldValue, newValue);
    }

    /**
     * Returns TRUE when background processes set to run in debug mode.
     *
     * @return TRUE for background mode.
     */
    public boolean isBackgroundDebugMode()
    {
        return backgroundDebugMode;
    }

    /**
     * Sets new value for flag.
     *
     * @param newValue TRUE to run all background processes in debug mode.
     */
    public void setBackgroundDebugMode(boolean newValue)
    {
        boolean oldValue = isBackgroundDebugMode();
        backgroundDebugMode = newValue;
        firePropertyChange(KEY_BACKGROUND_DEBUG_MODE, oldValue, newValue);
    }

    /**
     * Returns interval between auto-purges.
     *
     * @return interval in minutes.
     */
    public int getAutoPurgeIntervalMinutes()
    {
        return autoPurgeInterval;
    }

    /**
     * Sets new value of interval between auto-purges.
     *
     * @param val interval in minutes.
     */
    public void setAutoPurgeIntervalMinutes(int val)
    {
        int oldvalue = getAutoPurgeIntervalMinutes();
        autoPurgeInterval = val;
        firePropertyChange(KEY_AUTO_PURGE_INTERVAL_MINUTES, oldvalue, val);
    }

    /**
     * Returns string representation of value of interval between auto-purges (in minutes).
     *
     * @return string representation of interval value.
     */
    public String getAutoPurgeIntervalMinutesString()
    {
        return Integer.toString(autoPurgeInterval);
    }

    /**
     * Sets value of interval between auto-purges in minutes presented as string.
     *
     * @param val new interval value.
     */
    public void setAutoPurgeIntervalMinutesString(String val)
    {
        setAutoPurgeIntervalMinutes(Integer.parseInt(val));
    }

    /**
     * Returns number of articles to leave after auto-purge.
     *
     * @return number of articles.
     */
    public int getPurgeCount()
    {
        return purgeCount;
    }

    /**
     * Sets number of articles to leave after auto-purge.
     *
     * @param count number of articles.
     */
    public void setPurgeCount(int count)
    {
        int oldvalue = getPurgeCount();
        purgeCount = count;
        firePropertyChange(PROP_PURGE_COUNT, oldvalue, count);
    }

    /**
     * Returns number of articles to leave after auto-purge as string.
     *
     * @return number of articles.
     */
    public String getPurgeCountString()
    {
        return Integer.toString(purgeCount);
    }

    /**
     * Sets number of articles to leave after auto-purge as string.
     *
     * @param val number of articles.
     */
    public void setPurgeCountString(String val)
    {
        setPurgeCount(Integer.parseInt(val));
    }

    /**
     * Returns inteval in minutes between webStat collections.
     *
     * @return minutes.
     */
    public int getWebStatInterval()
    {
        return webStatInterval;
    }

    /**
     * Sets interval in minutes between webStat collections.
     *
     * @param count minutes.
     */
    public void setWebStatInterval(int count)
    {
        int oldvalue = getWebStatInterval();
        webStatInterval = count;
        firePropertyChange(KEY_WEBSTAT_INTERVAL, oldvalue, count);
    }

    /**
     * Returns interval in minutes between webStat collections as string.
     *
     * @return minutes as string.
     */
    public String getWebStatIntervalString()
    {
        return Integer.toString(webStatInterval);
    }

    /**
     * Sets interval in minutes between webStat collections as string.
     *
     * @param val minutes in string.
     */
    public void setWebStatIntervalString(String val)
    {
        setWebStatInterval(Integer.parseInt(val));
    }

    /**
     * Returns interval in minutes between RSS polling.
     *
     * @return minutes.
     */
    public int getRssPollInterval()
    {
        return rssPollInterval;
    }

    /**
     * Sets interval in minutes between RSS polling.
     *
     * @param count minutes.
     */
    public void setRssPollInterval(int count)
    {
        int oldvalue = getRssPollInterval();
        rssPollInterval = count;
        firePropertyChange(PROP_RSS_POLL_MIN, oldvalue, count);
    }

    /**
     * Returns interval in minutes between RSS polling as string.
     *
     * @return minutes as string.
     */
    public String getRssPollIntervalString()
    {
        return Integer.toString(rssPollInterval);
    }

    /**
     * Sets interval in minutes between RSS polling as string.
     *
     * @param val minutes as string.
     */
    public void setRssPollIntervalString(String val)
    {
        setRssPollInterval(Integer.parseInt(val));
    }

    /**
     * Returns browser executible.
     *
     * @return executible.
     */
    public String getInternetBrowser()
    {
        return internetBrowser;
    }

    /**
     * Sets browser executible.
     *
     * @param value executible.
     */
    public void setInternetBrowser(String value)
    {
        String oldValue = getInternetBrowser();
        internetBrowser = value;
        firePropertyChange(KEY_INTERNET_BROWSER, oldValue, value);
    }

    /**
     * Returns TRUE when marking of article as read on delay is enabled.
     *
     * @return TRUE if marking is enabled.
     */
    public boolean isMarkReadAfterDelay()
    {
        return markReadAfterDelay;
    }

    /**
     * Sets new value of "Marking on delay" flag.
     *
     * @param newValue TRUE to enable.
     */
    public void setMarkReadAfterDelay(boolean newValue)
    {
        boolean oldValue = isMarkReadAfterDelay();
        markReadAfterDelay = newValue;
        firePropertyChange(KEY_MARK_READ_AFTER_DELAY, oldValue, newValue);
    }

    /**
     * Returns number of seconds to wait after article selection to mark it as read.
     *
     * @return number of seconds.
     */
    public int getMarkReadAfterSeconds()
    {
        return markReadAfterSeconds;
    }

    /**
     * Sets number of seconds to wait after article selection before marking it as read.
     *
     * @param newValue number of seconds.
     */
    public void setMarkReadAfterSeconds(int newValue)
    {
        int oldValue = getMarkReadAfterSeconds();
        markReadAfterSeconds = newValue;
        firePropertyChange(KEY_MARK_READ_AFTER_SECONDS, oldValue, newValue);
    }

    /**
     * Returns number of seconds to wait after article selection to mark it as read as string.
     *
     * @return number of seconds.
     */
    public String getMarkReadAfterSecondsString()
    {
        return Integer.toString(markReadAfterSeconds);
    }

    /**
     * Sets number of seconds to wait after article selection before marking it as read as string.
     *
     * @param newValue number of seconds.
     */
    public void setMarkReadAfterSecondsString(String newValue)
    {
        setMarkReadAfterSeconds(Integer.parseInt(newValue));
    }

    /**
     * Returns TRUE if using persistence.
     *
     * @return TRUE if user wishes to use persistence.
     */
    public boolean isUsingPersistence()
    {
        return usingPersistence;
    }

    /**
     * Sets the value of the flag of using persistence. The changes will not occur immediately.
     * They are effective on restart.
     *
     * @param aUsingPersistence TRUE to use persistence.
     */
    public void setUsingPersistence(boolean aUsingPersistence)
    {
        boolean oldValue = usingPersistence;
        usingPersistence = aUsingPersistence;
        firePropertyChange(KEY_USE_PERSISTENCE, oldValue, usingPersistence);
    }

    /**
     * Returns minimum number of starz for channel to be good.
     *
     * @return number of starz.
     */
    public int getGoodChannelStarz()
    {
        return goodChannelStarz;
    }

    /**
     * Sets number of starz for channel to be good.
     *
     * @param starz minimum number of starz.
     */
    public void setGoodChannelStarz(int starz)
    {
        int oldValue = goodChannelStarz;
        goodChannelStarz = starz;
        firePropertyChange(PROP_GOOD_CHANNEL_STARZ, oldValue, goodChannelStarz);
    }

    /**
     * Returns <code>true</code> when showing of only good channel required.
     *
     * @return <code>true</code> to show only good channels.
     */
    public boolean isShowOnlyGoodChannels()
    {
        return showOnlyGoodChannels;
    }

    /**
     * Returns TRUE if sorting of feeds is enabled.
     *
     * @return TRUE if sorting of feeds is enabled.
     */
    public boolean isSortingEnabled()
    {
        return sortingEnabled;
    }

    /**
     * Enables / disables sorting of feeds.
     *
     * @param value TRUE to enable sorting of feeds.
     */
    public void setSortingEnabled(boolean value)
    {
        boolean oldValue = sortingEnabled;
        sortingEnabled = value;
        firePropertyChange(PROP_SORTING_ENABLED, oldValue, value);
    }

    /**
     * Returns mask for primary sorting of the feeds list.
     *
     * @return mask for primary sorting of the feeds list.
     */
    public int getSortByClass1()
    {
        return sortByClass1;
    }

    /**
     * Sets the mask for primary sorting of the feeds list.
     *
     * @param classMask mask for primary sorting of the feeds list.
     */
    public void setSortByClass1(int classMask)
    {
        int oldMask = sortByClass1;
        sortByClass1 = classMask;
        firePropertyChange(PROP_SORT_BY_CLASS_1, oldMask, sortByClass1);
    }

    /**
     * Returns TRUE if primary sorting should be in reversed order.
     *
     * @return TRUE if primary sorting should be in reversed order.
     */
    public boolean isReversedSortByClass1()
    {
        return reversedSortByClass1;
    }

    /**
     * Sets the flag showing the order of the primary sorting.
     *
     * @param value TRUE to sort in reversed order.
     */
    public void setReversedSortByClass1(boolean value)
    {
        boolean oldValue = reversedSortByClass1;
        reversedSortByClass1 = value;
        firePropertyChange(PROP_REVERSED_SORT_BY_CLASS_1, oldValue, value);
    }

    /**
     * Returns mask for secondary sorting of the feeds list.
     *
     * @return mask for secondary sorting of the feeds list.
     */
    public int getSortByClass2()
    {
        return sortByClass2;
    }

    /**
     * Sets the mask for secondary sorting of the feeds list.
     *
     * @param classMask mask for secondary sorting of the feeds list.
     */
    public void setSortByClass2(int classMask)
    {
        int oldMask = sortByClass2;
        sortByClass2 = classMask;
        firePropertyChange(PROP_SORT_BY_CLASS_2, oldMask, sortByClass2);
    }

    /**
     * Returns TRUE if secondary sorting should be in reversed order.
     *
     * @return TRUE if secondary sorting should be in reversed order.
     */
    public boolean isReversedSortByClass2()
    {
        return reversedSortByClass2;
    }

    /**
     * Sets the flag showing the order of the secondary sorting.
     *
     * @param value TRUE to sort in reversed order.
     */
    public void setReversedSortByClass2(boolean value)
    {
        boolean oldValue = reversedSortByClass2;
        reversedSortByClass2 = value;
        firePropertyChange(PROP_REVERSED_SORT_BY_CLASS_2, oldValue, value);
    }

    /**
     * Returns feed selection delay in millis.
     *
     * @return feed selection delay in millis.
     */
    public int getFeedSelectionDelay()
    {
        return feedSelectionDelay;
    }

    /**
     * Sets feed selection delay.
     *
     * @param aFeedSelectionDelay feed selection delay.
     */
    public void setFeedSelectionDelay(int aFeedSelectionDelay)
    {
        int oldValue = feedSelectionDelay;
        feedSelectionDelay = aFeedSelectionDelay;
        firePropertyChange(PROP_FEED_SELECTION_DELAY, oldValue, feedSelectionDelay);
    }

    /**
     * Returns TRUE if unread articles should be preserved from cleaning.
     *
     * @return TRUE if unread articles should be preserved from cleaning.
     */
    public boolean isPreserveUnread()
    {
        return preserveUnread;
    }

    /**
     * Sets the value of unread articles preservation flag.
     *
     * @param value TRUE to preserve unread articles during cleaning.
     */
    public void setPreserveUnread(boolean value)
    {
        boolean oldValue = preserveUnread;
        preserveUnread = value;
        firePropertyChange(PROP_PRESERVE_UNREAD, oldValue, preserveUnread);
    }

    /**
     * Returns TRUE if it's necessary to copy links to clipboard in HREF format.
     *
     * @return TRUE if it's necessary to copy links to clipboard in HREF format.
     */
    public boolean isCopyLinksInHrefFormat()
    {
        return copyLinksInHrefFormat;
    }

    /**
     * Sets the value of copy links format flag.
     *
     * @param aValue TRUE to copy links to clipboard in HREF format.
     */
    public void setCopyLinksInHrefFormat(boolean aValue)
    {
        copyLinksInHrefFormat = aValue;
    }

    /**
     * Returns TRUE if all text should be anti-aliased.
     *
     * @return TRUE if all text should be anti-aliased.
     */
    public boolean isAntiAliasText()
    {
        return antiAliasText;
    }

    /**
     * Sets the value of text anti-aliasing flag.
     *
     * @param aAntiAliasText TRUE if all text should be anti-aliased.
     */
    public void setAntiAliasText(boolean aAntiAliasText)
    {
        antiAliasText = aAntiAliasText;
    }

    /**
     * Returns the type of tags storage.
     *
     * @return tags storage.
     *
     * @see #TAGS_STORAGE_NONE
     * @see #TAGS_STORAGE_BB_SERVICE
     * @see #TAGS_STORAGE_DELICIOUS
     */
    public int getTagsStorage()
    {
        return tagsStorage;
    }

    /**
     * Sets the type of tags storage.
     *
     * @param aTagsStorage new tags storage type.
     *
     * @see #TAGS_STORAGE_NONE
     * @see #TAGS_STORAGE_BB_SERVICE
     * @see #TAGS_STORAGE_DELICIOUS
     */
    public void setTagsStorage(int aTagsStorage)
    {
        int oldValue = tagsStorage;
        tagsStorage = aTagsStorage;
        firePropertyChange(PROP_TAGS_STORAGE, oldValue, tagsStorage);
    }

    /**
     * Returns <code>TRUE</code> if shared tags should be fetched automatically
     * upon tags window opening.
     *
     * @return <code>TRUE</code> if shared tags should be fetched automatically.
     */
    public boolean isTagsAutoFetch()
    {
        return tagsAutoFetch;
    }

    /**
     * Sets new value of flag showing TRUE if shared tags should be fetched
     * automatically upon tags window opening.
     *
     * @param aValue <code>TRUE</code> if shared tags should be fetched automatically.
     */
    public void setTagsAutoFetch(boolean aValue)
    {
        boolean oldValue = tagsAutoFetch;
        tagsAutoFetch = aValue;

        firePropertyChange(PROP_TAGS_AUTOFETCH, oldValue, aValue);
    }

    /**
     * Returns del.icio.us user password.
     *
     * @return password.
     */
    public String getTagsDeliciousPassword()
    {
        return tagsDeliciousPassword;
    }

    /**
     * Sets del.icio.us user password.
     *
     * @param aPassword new password.
     */
    public void setTagsDeliciousPassword(String aPassword)
    {
        String oldValue = tagsDeliciousPassword;
        tagsDeliciousPassword = aPassword;

        firePropertyChange(PROP_TAGS_DELICIOUS_PASSWORD, oldValue, aPassword);
    }

    /**
     * Returns the name of del.icio.us user.
     *
     * @return user name.
     */
    public String getTagsDeliciousUser()
    {
        return tagsDeliciousUser;
    }

    /**
     * Sets the name of del.icio.us user.
     *
     * @param aUsername new user name.
     */
    public void setTagsDeliciousUser(String aUsername)
    {
        String oldValue = tagsDeliciousUser;
        tagsDeliciousUser = aUsername;

        firePropertyChange(PROP_TAGS_DELICIOUS_USER, oldValue, aUsername);
    }

    /**
     * Returns <code>TRUE</code> to check for updates on startup.
     *
     * @return <code>TRUE</code> to check for updates on startup.
     */
    public boolean isCheckingForUpdatesOnStartup()
    {
        return checkingForUpdatesOnStartup;
    }

    /**
     * Sets new value for "checking for updates on startup flag".
     *
     * @param value new value.
     */
    public void setCheckingForUpdatesOnStartup(boolean value)
    {
        boolean oldValue = checkingForUpdatesOnStartup;
        checkingForUpdatesOnStartup = value;

        firePropertyChange(PROP_CHECKING_FOR_UPDATES_ON_STARTUP, oldValue, value);
    }

    /**
     * Returns <code>TRUE</code> if toolbar labels should be displayed.
     *
     * @return <code>TRUE</code> if toolbar labels should be displayed.
     */
    public boolean isShowToolbarLabels()
    {
        return showToolbarLabels;
    }

    /**
     * Changes toolbar labels visibility flag.
     *
     * @param value <code>TRUE</code> to show toolbar labels.
     */
    public void setShowToolbarLabels(boolean value)
    {
        boolean old = showToolbarLabels;
        showToolbarLabels = value;

        firePropertyChange(PROP_SHOW_TOOLBAR_LABELS, old, value);
    }

    /**
     * Returns <code>TRUE</code> if the unread button menu should be shown on a click.
     *
     * @return <code>TRUE</code> if the unread button menu should be shown on a click.
     */
    public boolean isShowUnreadButtonMenu()
    {
        return showUnreadButtonMenu;
    }

    /**
     * Changes the unread button menu visibility.
     *
     * @param value <code>TRUE</code> to show the menu.
     */
    public void setShowUnreadButtonMenu(boolean value)
    {
        boolean old = showUnreadButtonMenu;
        showUnreadButtonMenu = value;

        firePropertyChange(PROP_SHOW_UNREAD_BUTTON_MENU, old, value);
    }

    /**
     * Returns <code>TRUE</code> if the toolbar should be shown.
     *
     * @return <code>TRUE</code> if the toolbar should be shown.
     */
    public boolean isShowToolbar()
    {
        return showToolbar;
    }

    /**
     * Shows / hides the toolbar.
     *
     * @param show <code>TRUE</code> to show the toolbar.
     */
    public void setShowToolbar(boolean show)
    {
        boolean old = showToolbar;
        showToolbar = show;

        firePropertyChange(PROP_SHOW_TOOLBAR, old, show);
    }

    /**
     * Returns update period of reading list.
     *
     * @return update period of reading list.
     *
     * @see ReadingList#PERIOD_ONCE_PER_RUN
     * @see ReadingList#PERIOD_NEVER
     * @see ReadingList#PERIOD_DAILY
     * @see ReadingList#PERIOD_DAILY
     */
    public long getReadingListUpdatePeriod()
    {
        return readingListUpdatePeriod;
    }

    /**
     * Sets update period of reading list.
     *
     * @param period new period.
     *
     * @see ReadingList#PERIOD_ONCE_PER_RUN
     * @see ReadingList#PERIOD_NEVER
     * @see ReadingList#PERIOD_DAILY
     * @see ReadingList#PERIOD_DAILY
     */
    public void setReadingListUpdatePeriod(long period)
    {
        long old = readingListUpdatePeriod;
        readingListUpdatePeriod = period;

        firePropertyChange(PROP_READING_LIST_UPDATE_PERIOD, old, period);
    }

    /**
     * Returns maximum number of feeds to import.
     *
     * @return maximum number of feeds to import.
     */
    public int getFeedImportLimit()
    {
        return feedImportLimit;
    }

    /**
     * Sets new feed import limit value.
     *
     * @param limit limit.
     */
    public void setFeedImportLimit(int limit)
    {
        int old = feedImportLimit;
        feedImportLimit = limit;

        firePropertyChange(PROP_FEED_IMPORT_LIMIT, old, limit);
    }

    /**
     * Returns <code>TRUE</code> if Get Latest series of commands should update feeds.
     *
     * @return <code>TRUE</code> if Get Latest series of commands should update feeds.
     */
    public boolean isUpdateFeeds()
    {
        return updateFeeds;
    }

    /**
     * Enables and disables updating of feeds on manual Get Latest commands.
     *
     * @param update <code>TRUE</code> to allow feeds updates on manual commands.
     */
    public void setUpdateFeeds(boolean update)
    {
        boolean old = updateFeeds;
        updateFeeds = update;

        firePropertyChange(PROP_UPDATE_FEEDS, old, updateFeeds);
    }

    /**
     * Returns <code>TRUE</code> if Get Latest series of commands should update reading lists
     * when called manually.
     *
     * @return <code>TRUE</code> if Get Latest series of commands should update reading lists.
     */
    public boolean isUpdateReadingLists()
    {
        return updateReadingLists;
    }

    /**
     * Enables and disables updating of reading lists on manual Get Latest commands.
     *
     * @param update <code>TRUE</code> to allow reading lists updates.
     */
    public void setUpdateReadingLists(boolean update)
    {
        boolean old = updateReadingLists;
        updateReadingLists = update;

        firePropertyChange(PROP_UPDATE_READING_LISTS, old, updateReadingLists);
    }

    /**
     * Returns the code of an action to take when there's an reading list update event fired.
     *
     * @return an action code.
     *
     * @see #RL_UPDATE_NONE
     * @see #RL_UPDATE_NOTIFY
     * @see #RL_UPDATE_CONFIRM
     */
    public int getOnReadingListUpdateActions()
    {
        return onReadingListUpdateActions;
    }

    /**
     * Sets the code of an action to take when there's an reading list update event fired.
     *
     * @param action an action code.
     *
     * @see #RL_UPDATE_NONE
     * @see #RL_UPDATE_NOTIFY
     * @see #RL_UPDATE_CONFIRM
     */
    public void setOnReadingListUpdateActions(int action)
    {
        int old = onReadingListUpdateActions;
        onReadingListUpdateActions = action;

        firePropertyChange(PROP_ON_READING_LIST_UPDATE_ACTIONS, old, action);
    }

    /**
     * Returns guide selection mode.
     *
     * @return mode.
     *
     * @see #GSM_FIRST_FEED
     * @see #GSM_LAST_SEEN_FEED
     * @see #GSM_NO_FEED
     */
    public int getGuideSelectionMode()
    {
        return guideSelectionMode;
    }

    /**
     * Sets new guide selection mode.
     *
     * @param mode new mode.
     *
     * @see #GSM_FIRST_FEED
     * @see #GSM_LAST_SEEN_FEED
     * @see #GSM_NO_FEED
     */
    public void setGuideSelectionMode(int mode)
    {
        int oldValue = guideSelectionMode;
        guideSelectionMode = mode;

        firePropertyChange(PROP_GUIDE_SELECTION_MODE, oldValue, mode);
    }

    /**
     * Returns the state of the proxy.
     *
     * @return <code>TRUE</code> when proxy is enabled.
     */
    public boolean isProxyEnabled()
    {
        return CustomProxySelector.INSTANCE.isProxyEnabled();
    }

    /**
     * Sets new proxy state.
     *
     * @param enabled <code>TRUE</code> to enable proxy.
     */
    public void setProxyEnabled(boolean enabled)
    {
        boolean oldValue = isProxyEnabled();
        CustomProxySelector.INSTANCE.setProxyEnabled(enabled);

        firePropertyChange(PROP_PROXY_ENABLED, oldValue, enabled);
    }

    /**
     * Returns proxy host.
     *
     * @return proxy host.
     */
    public String getProxyHost()
    {
        return CustomProxySelector.INSTANCE.getProxyHost();
    }

    /**
     * Sets proxy host.
     *
     * @param host host.
     */
    public void setProxyHost(String host)
    {
        String oldValue = getProxyHost();
        CustomProxySelector.INSTANCE.setProxyHost(host);

        firePropertyChange(PROP_PROXY_HOST, oldValue, host);
    }

    /**
     * Returns proxy port.
     *
     * @return port.
     */
    public int getProxyPort()
    {
        return CustomProxySelector.INSTANCE.getProxyPort();
    }

    /**
     * Sets proxy port.
     *
     * @param port port.
     */
    public void setProxyPort(int port)
    {
        int oldValue = getProxyPort();
        CustomProxySelector.INSTANCE.setProxyPort(port);

        firePropertyChange(PROP_PROXY_PORT, oldValue, port);
    }

    /**
     * Returns the list of domains / IP's excluded from proxying.
     *
     * @return exclusions.
     */
    public String getProxyExclusions()
    {
        return CustomProxySelector.INSTANCE.getProxyExclusions();
    }

    /**
     * Sets the new list of excluded domains / IP's.
     *
     * @param excl list.
     */
    public void setProxyExclusions(String excl)
    {
        String old = getProxyExclusions();
        CustomProxySelector.INSTANCE.setProxyExclusions(excl);

        firePropertyChange(PROP_PROXY_EXCLUSIONS, old, excl);
    }

    /**
     * Returns <code>TRUE</code> when the new publication alert should be shown.
     *
     * @return <code>TRUE</code> to show.
     */
    public boolean isShowingNewPubAlert()
    {
        return showingNewPubAlert;
    }

    /**
     * Sets the new publication alert showing flag.
     *
     * @param show <code>TRUE</code> to show alert on new publication.
     */
    public void setShowingNewPubAlert(boolean show)
    {
        boolean oldValue = showingNewPubAlert;
        showingNewPubAlert = show;

        firePropertyChange(PROP_SHOWING_NEW_PUB_ALERT, oldValue, show);
    }

    /**
     * Returns the state of browse on double click flag.
     *
     * @return <code>TRUE</code> to open browser on double click over the article title.
     */
    public boolean isBrowseOnDblClick()
    {
        return browseOnDblClick;
    }

    /**
     * Sets the state of browse on double click flag.
     *
     * @param flag <code>TRUE</code> to open browser on double click over the article title.
     */
    public void setBrowseOnDblClick(boolean flag)
    {
        boolean oldValue = browseOnDblClick;
        browseOnDblClick = flag;

        firePropertyChange(PROP_BROWSE_ON_DBL_CLICK, oldValue, flag);
    }

    /**
     * Returns the date of last properties update.
     *
     * @return the date of last properties update.
     */
    public Date getLastUpdateTime()
    {
        return lastUpdateTime;
    }

    /**
     * Sets the date of last update.
     *
     * @param date new date.
     */
    public void setLastUpdateTime(Date date)
    {
        Date oldDate = lastUpdateTime;
        lastUpdateTime = date;

        firePropertyChange(PROP_LAST_UPDATE_TIME, oldDate, lastUpdateTime);
    }

    /**
     * Returns <code>TRUE</code> when pinging of the reading lists on publication is enabled.
     *
     * @return the value of the flag.
     */
    public boolean isPingOnReadingListPublication()
    {
        return pingOnReadingListPublication;
    }

    /**
     * Sets the value of the publication pinging flag.
     *
     * @param flag <code>TRUE</code> to ping upon publication.
     */
    public void setPingOnReadingListPublication(boolean flag)
    {
        boolean oldValue = pingOnReadingListPublication;
        pingOnReadingListPublication = flag;

        firePropertyChange(PROP_PING_ON_RL_PUBLICATION, oldValue, flag);
    }

    /**
     * Returns a URL to ping upon reading list publication.
     *
     * @return the URL.
     */
    public String getPingOnReadingListPublicationURL()
    {
        return pingOnReadingListPublicationURL;
    }

    /**
     * Sets a URL to ping upon reading list publication.
     *
     * @param url the URL.
     */
    public void setPingOnReadingListPublicationURL(String url)
    {
        String oldValue = pingOnReadingListPublicationURL;
        pingOnReadingListPublicationURL = url;

        firePropertyChange(PROP_PING_ON_RL_PUBLICATION_URL, oldValue, url);
    }

    /**
     * Returns <code>TRUE</code> when notifications are on.
     *
     * @return <code>TRUE</code> when notifications are on.
     */
    public boolean isNotificationsEnabled()
    {
        return notificationsEnabled;
    }

    /**
     * Turns notifications on/off.
     *
     * @param flag <code>TRUE</code> to turn them on.
     */
    public void setNotificationsEnabled(boolean flag)
    {
        boolean oldVal = notificationsEnabled;
        notificationsEnabled = flag;

        firePropertyChange(PROP_NOTIFICATIONS_ENABLED, oldVal, flag);
    }

    /**
     * Returns <code>TRUE</code> when there should be a sound when new articles arrive.
     *
     * @return <code>TRUE</code> when there should be a sound when new articles arrive.
     */
    public boolean isSoundOnNewArticles()
    {
        return soundOnNewArticles;
    }

    /**
     * Turns on/off the sound when new articles arrive.
     *
     * @param flag <code>TRUE</code> to turn them on.
     */
    public void setSoundOnNewArticles(boolean flag)
    {
        boolean oldVal = soundOnNewArticles;
        soundOnNewArticles = flag;

        firePropertyChange(PROP_SOUND_ON_NEW_ARTICLES, oldVal, flag);
    }

    /**
     * Returns <code>TRUE</code> if there should be a sound when no unread articles left.
     *
     * @return <code>TRUE</code> if there should be a sound when no unread articles left.
     */
    public boolean isSoundOnNoUnread()
    {
        return soundOnNoUnread;
    }

    /**
     * Sets the sound-on-no-unread flag.
     *
     * @param flag <code>TRUE</code> to make sound.
     */
    public void setSoundOnNoUnread(boolean flag)
    {
        boolean old = soundOnNoUnread;
        soundOnNoUnread = flag;

        firePropertyChange(PROP_SOUND_ON_NO_UNREAD, old, flag);
    }

    /**
     * Returns number of seconds to wait before hiding event notification message / balloon.
     *
     * @return number of seconds to wait before hiding event notification message / balloon.
     */
    public int getNotificationsShowPeriod()
    {
        return notificationsShowPeriod;
    }

    /**
     * Sets the notification display period.
     *
     * @param seconds number of seconds or -1 for "forever".
     */
    public void setNotificationsShowPeriod(int seconds)
    {
        int oldValue = notificationsShowPeriod;
        notificationsShowPeriod = seconds;

        firePropertyChange(PROP_NOTIFICATIONS_SHOW_PERIOD, oldValue, seconds);
    }

    /**
     * Returns the list of user-defined no-discovery file extensions.
     *
     * @return extensions list.
     */
    public String getNoDiscoveryExtensions()
    {
        return noDiscoveryExtensions;
    }

    /**
     * Sets the list of user-defined no-discovery file extensions.
     *
     * @param exts new list of extensions.
     */
    public void setNoDiscoveryExtensions(String exts)
    {
        String old = noDiscoveryExtensions;
        noDiscoveryExtensions = exts;

        firePropertyChange(PROP_NO_DISCOVERY_EXTENSIONS, old, exts);
    }

    /**
     * Returns <code>TRUE</code> if application icon should be shown in systray all the time.
     *
     * @return <code>TRUE</code> if application icon should be shown in systray all the time.
     */
    public boolean isShowAppIconInSystray()
    {
        return showAppIconInSystray;
    }

    /**
     * Changes the value of show icon in systray flag.
     *
     * @param show new value.
     */
    public void setShowAppIconInSystray(boolean show)
    {
        boolean oldShow = showAppIconInSystray;
        showAppIconInSystray = show;

        firePropertyChange(PROP_SHOW_APPICON_IN_SYSTRAY, oldShow, show);
    }

    /**
     * Returns <code>TRUE</code> if the application should minimize to systray.
     *
     * @return <code>TRUE</code> if the application should minimize to systray.
     */
    public boolean isMinimizeToSystray()
    {
        return minimizeToSystray;
    }

    /**
     * Sets the value of minimize to systray flag.
     *
     * @param toSystray new value.
     */
    public void setMinimizeToSystray(boolean toSystray)
    {
        boolean oldToSystray = minimizeToSystray;
        minimizeToSystray = toSystray;

        firePropertyChange(PROP_MINIMIZE_TO_SYSTRAY, oldToSystray, toSystray);
    }

    /**
     * Returns dock icon badge mode.
     *
     * @return mode.
     *
     * @see #DIB_MODE_INVISIBLE
     * @see #DIB_MODE_SHOW_UNREAD_ARTICLES
     * @see #DIB_MODE_SHOW_UNREAD_FEEDS
     */
    public int getDockIconBadgeMode()
    {
        return dockIconBadgeMode;
    }

    /**
     * Sets dock icon badge mode.
     *
     * @param mode new mode.
     *
     * @see #DIB_MODE_INVISIBLE
     * @see #DIB_MODE_SHOW_UNREAD_ARTICLES
     * @see #DIB_MODE_SHOW_UNREAD_FEEDS
     */
    public void setDockIconBadgeMode(int mode)
    {
        int oldMode = dockIconBadgeMode;
        dockIconBadgeMode = mode;

        firePropertyChange(PROP_DIB_MODE, oldMode, mode);
    }

    /**
     * Returns the number of sentences in a brief mode.
     *
     * @return sentences.
     */
    public int getBriefSentences()
    {
        return AbstractArticle.getBriefSentences();
    }

    /**
     * Sets new sentence limit for brief mode.
     *
     * @param limit limit.
     */
    public void setBriefSentences(int limit)
    {
        int old = getBriefSentences();
        AbstractArticle.setBriefSentences(limit);

        firePropertyChange(PROP_BRIEF_SENTENCES, old, limit);
    }

    /**
     * Returns the minimum number of characters to show in a brief mode (if available).
     *
     * @return minimum.
     */
    public int getBriefMinLength()
    {
        return AbstractArticle.getBriefMinLength();
    }

    /**
     * Sets new minimum length for brief mode.
     *
     * @param min length.
     */
    public void setBriefMinLength(int min)
    {
        int old = getBriefMinLength();
        AbstractArticle.setBriefMinLength(min);

        firePropertyChange(PROP_BRIEF_MIN_LENGTH, old, min);
    }

    /**
     * Returns the maximum number of characters to show in a brief mode (if available).
     *
     * @return max length.
     */
    public int getBriefMaxLength()
    {
        return AbstractArticle.getBriefMaxLength();
    }

    /**
     * Sets new maximum length for brief mode.
     *
     * @param max length.
     */
    public void setBriefMaxLength(int max)
    {
        int old = getBriefMaxLength();
        AbstractArticle.setBriefMaxLength(max);

        firePropertyChange(PROP_BRIEF_MAX_LENGTH, old, max);
    }

    /**
     * Returns <code>TRUE</code> if pin tagging is enabled.
     *
     * @return <code>TRUE</code> if pin tagging is enabled.
     */
    public boolean isPinTagging()
    {
        return pinTagging;
    }

    /**
     * Enables / disables pin tagging.
     *
     * @param en <code>TRUE</code> to enable.
     */
    public void setPinTagging(boolean en)
    {
        boolean old = pinTagging;
        pinTagging = en;
        firePropertyChange(PROP_PIN_TAGGING, old, pinTagging);
    }

    /**
     * Returns a string with the list of pin tags separated by spaces.
     *
     * @return pin tags.
     */
    public String getPinTags()
    {
        return pinTags;
    }

    /**
     * Sets the tags to use when pinning.
     *
     * @param tags space-separated list of tags.
     */
    public void setPinTags(String tags)
    {
        String old = pinTags;
        pinTags = tags;
        firePropertyChange(PROP_PIN_TAGS, old, tags);
    }

    /**
     * Returns the unprocessed list (record per line) of patterns for links to ignore.
     *
     * @return patterns.
     */
    public String getWhIgnore()
    {
        return whIgnore;
    }

    /**
     * Sets new list of patterns to ignore.
     *
     * @param patterns patterns.
     */
    public void setWhIgnore(String patterns)
    {
        String old = whIgnore;
        whIgnore = patterns;
        firePropertyChange(PROP_WH_IGNORE, old, patterns);
    }

    /**
     * Returns <code>TRUE</code> when links to self aren't counted.
     *
     * @return <code>TRUE</code> when links to self aren't counted.
     */
    public boolean isWhNoSelfLinks()
    {
        return whNoSelfLinks;
    }

    /**
     * Sets the value of the "don't count links to self" flag.
     *
     * @param dontCount <code>TRUE</code> not to count links to self.
     */
    public void setWhNoSelfLinks(boolean dontCount)
    {
        boolean old = whNoSelfLinks;
        whNoSelfLinks = dontCount;
        firePropertyChange(PROP_WH_NOSELFLINKS, old, whNoSelfLinks);
    }

    /**
     * Returns <code>TRUE</code> when ignoring hot links referred by the same sources.
     *
     * @return <code>TRUE</code> when ignoring hot links referred by the same sources.
     */
    public boolean isWhSuppressSameSourceLinks()
    {
        return whSuppressSameSourceLinks;
    }

    /**
     * Sets the suppression of the hot links referred only by the same source.
     *
     * @param suppress <code>TRUE</code> to suppress.
     */
    public void setWhSuppressSameSourceLinks(boolean suppress)
    {
        boolean old = whSuppressSameSourceLinks;
        whSuppressSameSourceLinks = suppress;
        firePropertyChange(PROP_WH_SUPPRESS_SAME_SOURCE_LINKS, old, whSuppressSameSourceLinks);
    }

    /**
     * Returns the title of the target guide for the WH operations.
     *
     * @return guide title.
     */
    public String getWhTargetGuide()
    {
        return whTargetGuide;
    }

    /**
     * Setst the title of the guide for the WH operations.
     *
     * @param title title of the target guide.
     */
    public void setWhTargetGuide(String title)
    {
        String old = whTargetGuide;
        whTargetGuide = title;
        firePropertyChange(PROP_WH_TARGET_GUIDE, old, title);
    }

    /**
     * Returns the time WH settings were last changed.
     *
     * @return timestamp in UTC.
     */
    public long getWhSettingsChangeTime()
    {
        return whSettingsChangeTime;
    }

    /**
     * Sets the time WH settings were last changed.
     *
     * @param time time in milliseconds.
     */
    public void setWhSettingsChangeTime(long time)
    {
        long old = whSettingsChangeTime;
        whSettingsChangeTime = time;
        firePropertyChange(PROP_WH_SETTINGS_CHANGE_TIME, old, time);
    }

    /**
     * Returns the time Cleanup Wizard auto-cleanup frequency.
     *
     * @return milliseconds.
     */
    public long getCwFrequency()
    {
        return cwFrequency;
    }

    /**
     * Sets the time Cleanup Wizard auto-cleanup frequency.
     *
     * @param time time in milliseconds.
     */
    public void setCwFrequncy(long time)
    {
        long old = cwFrequency;
        cwFrequency = time;
        firePropertyChange(PROP_CW_FREQUENCY, old, time);
    }

    /**
     * Returns the time Cleanup Wizard last cleanup.
     *
     * @return timestamp.
     */
    public long getCwLastCleanup()
    {
        return cwLastCleanup;
    }

    /**
     * Sets the time Cleanup Wizard last cleanup.
     *
     * @param time time in milliseconds.
     */
    public void setCwLastCleanup(long time)
    {
        long old = cwLastCleanup;
        cwLastCleanup = time;
        firePropertyChange(PROP_CW_LAST_CLEANUP, old, time);
    }

    /**
     * Returns TRUE if the language setting is to be overriden.
     *
     * @return TRUE to use English.
     */
    public boolean isAlwaysUseEnglish()
    {
        return alwaysUseEnglish;
    }

    /**
     * Enables / disables the user of English.
     *
     * @param alwaysUseEnglish TRUE to enable.
     */
    public void setAlwaysUseEnglish(boolean alwaysUseEnglish)
    {
        boolean old = this.alwaysUseEnglish;
        this.alwaysUseEnglish = alwaysUseEnglish;
        firePropertyChange(PROP_ALWAYS_USE_ENGLISH, old, alwaysUseEnglish);
    }

    /**
     * Read all the Preferences from persistent preferences into this object. On Windows, the
     * persistent store is the Registry.
     *
     * @param prefs object representing persistent Preferences.
     */
    public void restoreFrom(Preferences prefs)
    {
        setBackgroundDebugMode(prefs.getBoolean(KEY_BACKGROUND_DEBUG_MODE,
            DEFAULT_BACKGROUNDDEBUGMODE));
        setMarkReadWhenChangingChannels(prefs.getBoolean(KEY_MARK_READ_WHEN_CHANGING_CHANNELS,
            DEFAULT_MARK_READ_WHEN_CHANGING_CHANNELS));
        setMarkReadWhenChangingGuides(prefs.getBoolean(KEY_MARK_READ_WHEN_CHANGING_GUIDES,
            DEFAULT_MARK_READ_WHEN_CHANGING_GUIDES));
        setAutoPurgeIntervalMinutes(prefs.getInt(KEY_AUTO_PURGE_INTERVAL_MINUTES,
            DEFAULT_PURGE_INTERVAL_MINUTES));
        setPurgeCount(prefs.getInt(KEY_PURGE_COUNT, DEFAULT_PURGE_COUNT));
        setRssPollInterval(prefs.getInt(KEY_RSS_POLL_MIN, DEFAULT_RSS_POLL_MIN));
        setInternetBrowser(prefs.get(KEY_INTERNET_BROWSER, DEFAULT_INTERNET_BROWSER));
        setMarkReadAfterDelay(prefs.getBoolean(KEY_MARK_READ_AFTER_DELAY,
            DEFAULT_MARK_READ_AFTER_DELAY));
        setMarkReadAfterSeconds(prefs.getInt(KEY_MARK_READ_AFTER_SECONDS,
            DEFAULT_MARK_READ_AFTER_SECONDS));
        setUsingPersistence(prefs.getBoolean(KEY_USE_PERSISTENCE, DEFAULT_USE_PERSISTENCE));
        setGoodChannelStarz(prefs.getInt(KEY_GOOD_CHANNEL_STARZ, DEFAULT_GOOD_CHANNEL_STARZ));

        // Sorting of feeds
        setSortingEnabled(prefs.getBoolean(PROP_SORTING_ENABLED, DEFAULT_SORTING_ENABLED));
        setSortByClass1(prefs.getInt(PROP_SORT_BY_CLASS_1, DEFAULT_SORT_BY_CLASS_1));
        setReversedSortByClass1(prefs.getBoolean(PROP_REVERSED_SORT_BY_CLASS_1,
            DEFAULT_REVERSED_SORT_BY_CLASS_1));
        setSortByClass2(prefs.getInt(PROP_SORT_BY_CLASS_2, DEFAULT_SORT_BY_CLASS_2));
        setReversedSortByClass2(prefs.getBoolean(PROP_REVERSED_SORT_BY_CLASS_2,
            DEFAULT_REVERSED_SORT_BY_CLASS_2));

        setFeedSelectionDelay(prefs.getInt(PROP_FEED_SELECTION_DELAY,
            DEFAULT_FEED_SELECTION_DELAY));

        setPreserveUnread(prefs.getBoolean(PROP_PRESERVE_UNREAD, DEFAULT_PRESERVE_UNREAD));

        setCopyLinksInHrefFormat(prefs.getBoolean(PROP_COPY_LINKS_IN_HREF_FORMAT,
            DEFAULT_COPY_LINKS_IN_HREF_FORMAT));
        setAntiAliasText(prefs.getBoolean(PROP_AA_TEXT, DEFAULT_AA_TEXT));

        setTagsStorage(prefs.getInt(PROP_TAGS_STORAGE, DEFAULT_TAGS_STORAGE));
        setTagsDeliciousUser(prefs.get(PROP_TAGS_DELICIOUS_USER, DEFAULT_TAGS_DELICIOUS_USER));
        setTagsDeliciousPassword(prefs.get(PROP_TAGS_DELICIOUS_PASSWORD,
            DEFAULT_TAGS_DELICIOUS_PASSWORD));
        setTagsAutoFetch(prefs.getBoolean(PROP_TAGS_AUTOFETCH,
            DEFAULT_TAGS_AUTOFETCH));

        setCheckingForUpdatesOnStartup(prefs.getBoolean(PROP_CHECKING_FOR_UPDATES_ON_STARTUP,
            DEFAULT_CHECKING_FOR_UPDATES_ON_STARTUP));

        setShowToolbarLabels(prefs.getBoolean(PROP_SHOW_TOOLBAR_LABELS,
            DEFAULT_SHOW_TOOLBAR_LABELS));
        setShowUnreadButtonMenu(prefs.getBoolean(PROP_SHOW_UNREAD_BUTTON_MENU,
            DEFAULT_SHOW_UNREAD_BUTTON_MENU));

        setShowToolbar(prefs.getBoolean(PROP_SHOW_TOOLBAR, DEFAULT_SHOW_TOOLBAR));
        setReadingListUpdatePeriod(prefs.getLong(PROP_READING_LIST_UPDATE_PERIOD,
            ReadingList.DEFAULT_PERIOD));
        setFeedImportLimit(prefs.getInt(PROP_FEED_IMPORT_LIMIT, DEFAULT_FEED_IMPORT_LIMITATION));

        setUpdateFeeds(prefs.getBoolean(PROP_UPDATE_FEEDS, DEFAULT_UPDATE_FEEDS));
        setUpdateReadingLists(prefs.getBoolean(PROP_UPDATE_READING_LISTS,
            DEFAULT_UPDATE_READING_LISTS));

        setOnReadingListUpdateActions(prefs.getInt(PROP_ON_READING_LIST_UPDATE_ACTIONS,
            DEFAULT_ON_READING_LIST_UPDATE_ACTIONS));
        setGuideSelectionMode(prefs.getInt(PROP_GUIDE_SELECTION_MODE,
            DEFAULT_GUIDE_SELECTION_MODE));

        setProxyEnabled(prefs.getBoolean(PROP_PROXY_ENABLED, false));
        setProxyHost(prefs.get(PROP_PROXY_HOST, ""));
        setProxyPort(prefs.getInt(PROP_PROXY_PORT, 80));
        setProxyExclusions(prefs.get(PROP_PROXY_EXCLUSIONS, ""));

        setShowingNewPubAlert(prefs.getBoolean(PROP_SHOWING_NEW_PUB_ALERT,
            DEFAULT_SHOWING_NEW_PUB_ALERT));

        setBrowseOnDblClick(prefs.getBoolean(PROP_BROWSE_ON_DBL_CLICK,
            DEFAULT_BROWSE_ON_DBL_CLICK));

        long lastUpdateTimeL = prefs.getLong(PROP_LAST_UPDATE_TIME, -1);
        setLastUpdateTime(lastUpdateTimeL == -1 ? null : new Date(lastUpdateTimeL));

        setPingOnReadingListPublication(prefs.getBoolean(PROP_PING_ON_RL_PUBLICATION,
            DEFAULT_PING_ON_RL_PUBLICATION));
        setPingOnReadingListPublicationURL(prefs.get(PROP_PING_ON_RL_PUBLICATION_URL,
            DEFAULT_PING_ON_RL_PUBLICATION_URL));

        viewModePreferences.restore(prefs);
        bloggingPreferences.restore(prefs);
        twitterPreferences.restore(prefs);

        setNotificationsEnabled(prefs.getBoolean(PROP_NOTIFICATIONS_ENABLED,
            DEFAULT_NOTIFICATIONS_ENABLED));
        setNotificationsShowPeriod(prefs.getInt(PROP_NOTIFICATIONS_SHOW_PERIOD,
            DEFAULT_NOTIFICATIONS_SHOW_PERIOD));

        setSoundOnNewArticles(prefs.getBoolean(PROP_SOUND_ON_NEW_ARTICLES,
            DEFAULT_SOUND_ON_NEW_ARTICLES));
        setSoundOnNoUnread(prefs.getBoolean(PROP_SOUND_ON_NO_UNREAD,
            DEFAULT_SOUND_ON_NO_UNREAD));

        setNoDiscoveryExtensions(prefs.get(PROP_NO_DISCOVERY_EXTENSIONS,
            DEFAULT_NO_DISCOVERY_EXTENSIONS));

        setShowAppIconInSystray(prefs.getBoolean(PROP_SHOW_APPICON_IN_SYSTRAY,
            DEFAULT_SHOW_APPICON_IN_SYSTRAY));
        setMinimizeToSystray(prefs.getBoolean(PROP_MINIMIZE_TO_SYSTRAY,
            DEFAULT_MINIMIZE_TO_SYSTRAY));

        setDockIconBadgeMode(prefs.getInt(PROP_DIB_MODE, DEFAULT_DIB_BADGE_MODE));

        setBriefSentences(prefs.getInt(PROP_BRIEF_SENTENCES, AbstractArticle.DEFAULT_BRIEF_SENTENCES));
        setBriefMinLength(prefs.getInt(PROP_BRIEF_MIN_LENGTH, AbstractArticle.DEFAULT_BRIEF_MIN_LENGTH));
        setBriefMaxLength(prefs.getInt(PROP_BRIEF_MAX_LENGTH, AbstractArticle.DEFAULT_BRIEF_MAX_LENGTH));

        setPinTagging(prefs.getBoolean(PROP_PIN_TAGGING, DEFAULT_PIN_TAGGING));
        setPinTags(prefs.get(PROP_PIN_TAGS, DEFAULT_PIN_TAGS));

        setPageSize(prefs.getInt(PROP_PAGE_SIZE, DEFAULT_PAGE_SIZE));

        setWhIgnore(prefs.get(PROP_WH_IGNORE, DEFAULT_WH_IGNORE).replaceAll("\\s", "\n"));
        setWhNoSelfLinks(prefs.getBoolean(PROP_WH_NOSELFLINKS, DEFAULT_WH_NOSELFLINKS));
        setWhSuppressSameSourceLinks(prefs.getBoolean(PROP_WH_SUPPRESS_SAME_SOURCE_LINKS,
                DEFAULT_WH_SUPPRESS_SAME_SOURCE_LINKS));
        setWhTargetGuide(prefs.get(PROP_WH_TARGET_GUIDE, DEFAULT_WH_TARGET_GUIDE));
        setWhSettingsChangeTime(prefs.getLong(PROP_WH_SETTINGS_CHANGE_TIME, DEFAULT_WH_SETTINGS_CHANGE_TIME));

        setAlwaysUseEnglish(prefs.getBoolean(PROP_ALWAYS_USE_ENGLISH, DEFAULT_ALWAYS_USE_ENGLISH));

        setCwFrequncy(prefs.getLong(PROP_CW_FREQUENCY, DEFAULT_CW_FREQUENCY));
        setCwLastCleanup(prefs.getLong(PROP_CW_LAST_CLEANUP, DEFAULT_CW_LAST_CLEANUP));
    }

    /**
     * Write all the preferences from this Object to persistent preferences. On Windows, this is the
     * registry.
     *
     * @param prefs object representing persistent Preferences.
     */
    public void storeIn(Preferences prefs)
    {
        prefs.putBoolean(KEY_BACKGROUND_DEBUG_MODE, isBackgroundDebugMode());
        prefs.putBoolean(KEY_MARK_READ_WHEN_CHANGING_CHANNELS, isMarkReadWhenChangingChannels());
        prefs.putBoolean(KEY_MARK_READ_WHEN_CHANGING_GUIDES, isMarkReadWhenChangingGuides());
        prefs.putInt(KEY_AUTO_PURGE_INTERVAL_MINUTES, getAutoPurgeIntervalMinutes());
        prefs.putInt(KEY_PURGE_COUNT, getPurgeCount());
        prefs.putInt(KEY_RSS_POLL_MIN, getRssPollInterval());
        prefs.put(KEY_INTERNET_BROWSER, getInternetBrowser());
        prefs.putBoolean(KEY_MARK_READ_AFTER_DELAY, isMarkReadAfterDelay());
        prefs.putInt(KEY_MARK_READ_AFTER_SECONDS, getMarkReadAfterSeconds());
        prefs.putBoolean(KEY_USE_PERSISTENCE, isUsingPersistence());
        prefs.putInt(KEY_GOOD_CHANNEL_STARZ, getGoodChannelStarz());

        // Sorting of feeds
        prefs.putBoolean(PROP_SORTING_ENABLED, isSortingEnabled());
        prefs.putInt(PROP_SORT_BY_CLASS_1, getSortByClass1());
        prefs.putBoolean(PROP_REVERSED_SORT_BY_CLASS_1, isReversedSortByClass1());
        prefs.putInt(PROP_SORT_BY_CLASS_2, getSortByClass2());
        prefs.putBoolean(PROP_REVERSED_SORT_BY_CLASS_2, isReversedSortByClass2());

        prefs.putInt(PROP_FEED_SELECTION_DELAY, getFeedSelectionDelay());

        prefs.putBoolean(PROP_PRESERVE_UNREAD, isPreserveUnread());
        prefs.putBoolean(PROP_COPY_LINKS_IN_HREF_FORMAT, isCopyLinksInHrefFormat());
        prefs.putBoolean(PROP_AA_TEXT, isAntiAliasText());

        prefs.putInt(PROP_TAGS_STORAGE, getTagsStorage());
        prefs.put(PROP_TAGS_DELICIOUS_USER, getTagsDeliciousUser());
        prefs.put(PROP_TAGS_DELICIOUS_PASSWORD, getTagsDeliciousPassword());
        prefs.putBoolean(PROP_TAGS_AUTOFETCH, isTagsAutoFetch());

        prefs.putBoolean(PROP_CHECKING_FOR_UPDATES_ON_STARTUP, isCheckingForUpdatesOnStartup());

        prefs.putBoolean(PROP_SHOW_TOOLBAR_LABELS, isShowToolbarLabels());
        prefs.putBoolean(PROP_SHOW_UNREAD_BUTTON_MENU, isShowUnreadButtonMenu());

        prefs.putBoolean(PROP_SHOW_TOOLBAR, isShowToolbar());
        prefs.putLong(PROP_READING_LIST_UPDATE_PERIOD, getReadingListUpdatePeriod());
        prefs.putInt(PROP_FEED_IMPORT_LIMIT, getFeedImportLimit());

        prefs.putBoolean(PROP_UPDATE_FEEDS, isUpdateFeeds());
        prefs.putBoolean(PROP_UPDATE_READING_LISTS, isUpdateReadingLists());

        prefs.putInt(PROP_ON_READING_LIST_UPDATE_ACTIONS, getOnReadingListUpdateActions());
        prefs.putInt(PROP_GUIDE_SELECTION_MODE, getGuideSelectionMode());

        prefs.putBoolean(PROP_PROXY_ENABLED, isProxyEnabled());
        prefs.put(PROP_PROXY_HOST, getProxyHost());
        prefs.putInt(PROP_PROXY_PORT, getProxyPort());
        prefs.put(PROP_PROXY_EXCLUSIONS, getProxyExclusions());

        prefs.putBoolean(PROP_SHOWING_NEW_PUB_ALERT, isShowingNewPubAlert());
        prefs.putBoolean(PROP_BROWSE_ON_DBL_CLICK, isBrowseOnDblClick());

        Date time = getLastUpdateTime();
        prefs.putLong(PROP_LAST_UPDATE_TIME, time == null ? -1 : time.getTime());

        prefs.putBoolean(PROP_PING_ON_RL_PUBLICATION, isPingOnReadingListPublication());
        prefs.put(PROP_PING_ON_RL_PUBLICATION_URL, getPingOnReadingListPublicationURL());

        viewModePreferences.store(prefs);
        bloggingPreferences.store(prefs);
        twitterPreferences.store(prefs);
        
        prefs.putBoolean(PROP_NOTIFICATIONS_ENABLED, isNotificationsEnabled());
        prefs.putBoolean(PROP_SOUND_ON_NEW_ARTICLES, isSoundOnNewArticles());
        prefs.putBoolean(PROP_SOUND_ON_NO_UNREAD, isSoundOnNoUnread());
        prefs.putInt(PROP_NOTIFICATIONS_SHOW_PERIOD, getNotificationsShowPeriod());

        prefs.put(PROP_NO_DISCOVERY_EXTENSIONS, getNoDiscoveryExtensions());

        prefs.putBoolean(PROP_SHOW_APPICON_IN_SYSTRAY, isShowAppIconInSystray());
        prefs.putBoolean(PROP_MINIMIZE_TO_SYSTRAY, isMinimizeToSystray());

        prefs.putInt(PROP_DIB_MODE, getDockIconBadgeMode());

        prefs.putInt(PROP_BRIEF_SENTENCES, getBriefSentences());
        prefs.putInt(PROP_BRIEF_MIN_LENGTH, getBriefMinLength());
        prefs.putInt(PROP_BRIEF_MAX_LENGTH, getBriefMaxLength());

        prefs.putBoolean(PROP_PIN_TAGGING, isPinTagging());
        prefs.put(PROP_PIN_TAGS, getPinTags());

        prefs.putInt(PROP_PAGE_SIZE, getPageSize());

        prefs.put(PROP_WH_IGNORE, getWhIgnore().replaceAll("\\n", " "));
        prefs.putBoolean(PROP_WH_NOSELFLINKS, isWhNoSelfLinks());
        prefs.putBoolean(PROP_WH_SUPPRESS_SAME_SOURCE_LINKS, isWhSuppressSameSourceLinks());
        prefs.put(PROP_WH_TARGET_GUIDE, getWhTargetGuide());
        prefs.putLong(PROP_WH_SETTINGS_CHANGE_TIME, getWhSettingsChangeTime());

        prefs.putBoolean(PROP_ALWAYS_USE_ENGLISH, isAlwaysUseEnglish());

        prefs.putLong(PROP_CW_FREQUENCY, getCwFrequency());
        prefs.putLong(PROP_CW_LAST_CLEANUP, getCwLastCleanup());
    }

    /**
     * Returns the index of last selected preferences page.
     *
     * @return index or <code>-1</code>.
     */
    public int getSelectedPrefsPage()
    {
        return selectedPrefsPage;
    }

    /**
     * Sets the index of last selected preferences page.
     *
     * @param index index.
     */
    public void setSelectedPrefsPage(int index)
    {
        this.selectedPrefsPage = index;
    }

    /**
     * Returns the maximum page size (in articles).
     *
     * @return page size.
     */
    public int getPageSize()
    {
        return pageSize;
    }

    /**
     * Sets the page size.
     *
     * @param pageSize page size.
     */
    public void setPageSize(int pageSize)
    {
        int old = this.pageSize;
        this.pageSize = pageSize;

        firePropertyChange(PROP_PAGE_SIZE, old, pageSize);
    }
}