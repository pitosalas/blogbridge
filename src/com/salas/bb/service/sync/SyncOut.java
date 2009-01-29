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
// $Id: SyncOut.java,v 1.47 2008/02/28 15:59:50 spyromus Exp $
//

package com.salas.bb.service.sync;

import com.salas.bb.core.FeedDisplayModeManager;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.*;
import com.salas.bb.domain.prefs.StarzPreferences;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.imageblocker.ImageBlocker;
import com.salas.bb.plugins.Manager;
import com.salas.bb.sentiments.SentimentsConfig;
import com.salas.bb.service.ServerService;
import com.salas.bb.service.ServerServiceException;
import com.salas.bb.service.ServicePreferences;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.opml.Converter;
import com.salas.bb.utils.uif.UifUtilities;
import com.salas.bb.views.settings.FeedRenderingSettings;
import com.salas.bb.views.settings.RenderingSettingsNames;
import com.salas.bb.twitter.TwitterPreferences;
import com.salas.bbutilities.opml.export.Exporter;
import com.salas.bbutilities.opml.objects.OPMLGuideSet;
import com.salas.bbutilities.opml.utils.Transformation;
import org.jdom.Document;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Outgoing synchronization.
 */
public class SyncOut extends AbstractSynchronization
{
    private static final Logger LOG = Logger.getLogger(SyncOut.class.getName());
    private static final String THREAD_NAME_PING = "Ping RL";

    private static final MessageFormat RL_PUB_URL =
        new MessageFormat("http://www.blogbridge.com/rl/{0,number,#}/{1}.opml");

    /**
     * Creates outgoing synchronization module.
     *
     * @param aModel model to operate.
     */
    public SyncOut(GlobalModel aModel)
    {
        super(aModel);
    }

    /**
     * Performs the step-by-step synchronization and collects stats.
     *
     * @param progress listener to notify.
     * @param aEmail            email of user account.
     * @param aPassword         password of user account.
     *
     * @return statistics.
     */
    protected Stats doSynchronization(IProgressListener progress, String aEmail,
                                      String aPassword)
    {
        SyncOutStats stats = new SyncOutStats();

        try
        {
            // save feeds
            if (servicePreferences.isSyncFeeds())
            {
                if (progress != null) progress.processStep(Strings.message("service.sync.out.saving.guides.and.feeds"));
                storeFeeds(aEmail, aPassword, stats);
                if (progress != null) progress.processStepCompleted();
            }

            // initiate background ping
            pingGuides();

            // save preferences
            if (servicePreferences.isSyncPreferences())
            {
                if (progress != null) progress.processStep(Strings.message("service.sync.out.saving.preferences"));
                storePreferences(aEmail, aPassword, stats);
                if (progress != null) progress.processStepCompleted();
            }

            // if call was successful put appropriate status in preferences
            servicePreferences.setLastSyncOutStatus(ServicePreferences.SYNC_STATUS_SUCCESS);
            servicePreferences.setLastSyncOutFeedsCount(model.getGuidesSet().countFeeds());
        } catch (ServerServiceException e1)
        {
            // synchronization errored out for some reason
            servicePreferences.setLastSyncOutStatus(ServicePreferences.SYNC_STATUS_FAILURE);

            // report only if servervice exception was caused by another error
            if (e1.getCause() != null)
            {
                LOG.log(Level.SEVERE, Strings.error("sync.error.during.sync.out"), e1);
                stats.registerFailure(null);
            } else
            {
                stats.registerFailure(e1.getMessage());
            }
        }

        servicePreferences.setLastSyncOutDate(new Date());

        return stats;
    }

    /**
     * Saves preferences to the service.
     *
     * @param aEmail    account email.
     * @param aPassword account password.
     * @param aStats    stats to fill.
     *
     * @throws ServerServiceException in case of service error.
     */
    private void storePreferences(String aEmail, String aPassword, SyncOutStats aStats)
        throws ServerServiceException
    {
        final Hashtable<String, Object> prefs = new Hashtable<String, Object>();

        // image blocker expressions
        String expressions = StringUtils.join(ImageBlocker.getExpressions().iterator(), "\n");
        prefs.put(ImageBlocker.KEY, StringUtils.toUTF8(expressions));

        SentimentsConfig.syncOut(prefs);
        
        storeGeneralPreferences(prefs);
        storeGuidesPreferences(prefs);
        storeFeedsPreferences(prefs);
        storeArticlesPreferences(prefs);
        storeTagsPreferences(prefs);
        storeReadingListsPreferences(prefs);
        storeAdvancedPreferences(prefs);
        storeWhatsHotPreferences(prefs);
        storeTwitterPreferences(prefs);
        Manager.storeState(prefs);

        // Save current time to record on the service
        setLong(prefs, "timestamp", System.currentTimeMillis());

        ServerService.syncStorePrefs(aEmail, aPassword, prefs);

        // record number of saved preferences
        aStats.savedPreferences = prefs.size();
    }

    /**
     * Saves guides and feeds to the service.
     *
     * @param aEmail    account email.
     * @param aPassword account password.
     * @param aStats    stats to fill.
     *
     * @throws ServerServiceException in case of service error.
     */
    private void storeFeeds(String aEmail, String aPassword, SyncOutStats aStats)
        throws ServerServiceException
    {
        // export information about guides
        GuidesSet guidesSet = model.getGuidesSet();

        // Calculate feed hashes basing on their present XML URLs for later updates upon successful completion
        Map<DirectFeed, Integer> feedHashes = calculateFeedHashes(guidesSet);

        OPMLGuideSet opmlSet = Converter.convertToOPML(guidesSet, "BlogBridge Feeds");
        Document doc = new Exporter(true).export(opmlSet);

        // prepare parameters for server call
        String opml = Transformation.documentToString(doc);

        int userId = ServerService.syncStore(aEmail, aPassword, opml);
        updatePublishedListsURLs(guidesSet, userId);

        // Update synchronization times
        guidesSet.onSyncOutCompletion();

        // Update feed with hashes
        updateFeedsWithHashes(feedHashes);

        // Remove all keys of deleted feeds as we just transfered our feeds list to the service
        GlobalController.SINGLETON.getDeletedFeedsRepository().purge();

        // Count saved guides/feeds
        StandardGuide[] guides = guidesSet.getStandardGuides(null);
        aStats.savedGuides = guides.length;
        aStats.savedFeeds = countFeeds(guides);
    }

    /**
     * Updates feeds with URL hashes.
     *
     * @param hashes hashes.
     */
    static void updateFeedsWithHashes(Map<DirectFeed, Integer> hashes)
    {
        for (Map.Entry<DirectFeed, Integer> en : hashes.entrySet())
        {
            DirectFeed feed = en.getKey();
            int hash = en.getValue();

            feed.setSyncHash(hash);
        }
    }

    /**
     * Calculates hashes for all direct feeds in the set. The hash is calculated from the
     * present XML URL.
     *
     * @param set set to parse.
     *
     * @return hashes.
     */
    static Map<DirectFeed, Integer> calculateFeedHashes(GuidesSet set)
    {
        Map<DirectFeed, Integer> hashes = new IdentityHashMap<DirectFeed, Integer>();

        List<IFeed> feeds = set.getFeeds();
        for (IFeed feed : feeds)
        {
            if (feed instanceof DirectFeed)
            {
                DirectFeed dfeed = (DirectFeed)feed;
                int hash = dfeed.calcSyncHash();

                hashes.put(dfeed, hash);
            }
        }

        return hashes;
    }

    /**
     * Updates URLs of all published guides.
     *
     * @param set       guides set to update.
     * @param userId    user ID.
     */
    private void updatePublishedListsURLs(GuidesSet set, int userId)
    {
        long publishingTime = System.currentTimeMillis();

        int count = set.getGuidesCount();
        for (int i = 0; i < count; i++)
        {
            IGuide guide = set.getGuideAt(i);
            String publishingTitle = guide.getPublishingTitle();
            if (guide.isPublishingEnabled() && StringUtils.isNotEmpty(publishingTitle))
            {
                String url = RL_PUB_URL.format(new Object[] {
                    userId,
                    StringUtils.encodeForURL(publishingTitle)
                });

                guide.setPublishingURL(url);
                guide.setLastPublishingTime(publishingTime);
            }
        }
    }

    /**
     * Returns the message to be reported on synchronization start.
     *
     * @return message.
     */
    protected String getProcessStartMessage()
    {
        return prepareProcessStartMessage(
            Strings.message("service.sync.message.synchronizing"),
            Strings.message("service.sync.message.preferences"),
            Strings.message("service.sync.message.guides.and.feeds"),
            Strings.message("service.sync.message.with.blogbridge.service")
        );
    }

    /**
     * Returns number of feeds in guides total.
     *
     * @param guides guides list.
     *
     * @return total number of feeds.
     */
    private static int countFeeds(IGuide[] guides)
    {
        int cnt = 0;

        for (IGuide guide : guides) cnt += guide.getFeedsCount();

        return cnt;
    }

    /**
     * Simple statistics holder.
     */
    public static class SyncOutStats extends Stats
    {
        private int savedGuides      = -1;
        private int savedFeeds       = -1;
        private int savedPreferences = -1;

        /**
         * Returns custom text to be told if not failed.
         *
         * @return text.
         */
        protected String getCustomText()
        {
            StringBuffer buf = new StringBuffer();

            if (savedGuides > 0) buf.append(MessageFormat.format(
                Strings.message("service.sync.out.status.guides.saved"),
                savedGuides));
            if (savedFeeds > 0) buf.append(MessageFormat.format(
                Strings.message("service.sync.out.status.feeds.saved"),
                savedFeeds));
            if (savedPreferences > 0) buf.append(MessageFormat.format(
                Strings.message("service.sync.out.status.preference.saved"),
                savedPreferences));

            return buf.toString();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Preferences storing
    // ---------------------------------------------------------------------------------------------

    /**
     * Stores general preferences.
     *
     * @param prefs preferences map.
     */
    private void storeGeneralPreferences(Map prefs)
    {
        UserPreferences up = model.getUserPreferences();

        setBoolean(prefs, UserPreferences.PROP_CHECKING_FOR_UPDATES_ON_STARTUP,
            up.isCheckingForUpdatesOnStartup());
// Disabled as we don't like what happens when synchronizing fonts across platforms
//        setFont(prefs, RenderingSettingsNames.MAIN_CONTENT_FONT, frs.getMainContentFont());
        setBoolean(prefs, UserPreferences.PROP_SHOW_TOOLBAR, up.isShowToolbar());

        // Behaviour
        setBoolean(prefs, UserPreferences.PROP_MARK_READ_WHEN_CHANGING_CHANNELS,
            up.isMarkReadWhenChangingChannels());
        setBoolean(prefs, UserPreferences.PROP_MARK_READ_WHEN_CHANGING_GUIDES,
            up.isMarkReadWhenChangingGuides());
        setBoolean(prefs, UserPreferences.PROP_MARK_READ_AFTER_DELAY,
            up.isMarkReadAfterDelay());
        setInt(prefs, UserPreferences.PROP_MARK_READ_AFTER_SECONDS,
            up.getMarkReadAfterSeconds());

        // Updates and Cleanups
        setInt(prefs, UserPreferences.PROP_RSS_POLL_MIN,
            up.getRssPollInterval());
        setInt(prefs, UserPreferences.PROP_PURGE_COUNT,
            up.getPurgeCount());
        setBoolean(prefs, UserPreferences.PROP_PRESERVE_UNREAD,
            up.isPreserveUnread());
    }

    /**
     * Stores guides preferences.
     *
     * @param prefs preferences map.
     */
    private void storeGuidesPreferences(Map prefs)
    {
        UserPreferences up = model.getUserPreferences();
        FeedRenderingSettings frs = model.getGlobalRenderingSettings();

        setBoolean(prefs, UserPreferences.PROP_PING_ON_RL_PUBLICATION, up.isPingOnReadingListPublication());
        setString(prefs, UserPreferences.PROP_PING_ON_RL_PUBLICATION_URL, up.getPingOnReadingListPublicationURL());

        setBoolean(prefs, RenderingSettingsNames.IS_BIG_ICON_IN_GUIDES, frs.isBigIconInGuides());
        setBoolean(prefs, "showUnreadInGuides", frs.isShowUnreadInGuides());
        setBoolean(prefs, RenderingSettingsNames.IS_ICON_IN_GUIDES_SHOWING,
            frs.isShowIconInGuides());
        setBoolean(prefs, RenderingSettingsNames.IS_TEXT_IN_GUIDES_SHOWING,
            frs.isShowTextInGuides());

        setInt(prefs, UserPreferences.PROP_GUIDE_SELECTION_MODE, up.getGuideSelectionMode());
    }

    /**
     * Stores feeds preferences.
     *
     * @param prefs preferences map.
     */
    private void storeFeedsPreferences(Map prefs)
    {
        UserPreferences up = model.getUserPreferences();
        FeedRenderingSettings frs = model.getGlobalRenderingSettings();

        setBoolean(prefs, "showStarz", frs.isShowStarz());
        setBoolean(prefs, "showUnreadInFeeds", frs.isShowUnreadInFeeds());
        setBoolean(prefs, "showActivityChart", frs.isShowActivityChart());

        setFilterColor(prefs, FeedClass.DISABLED);
        setFilterColor(prefs, FeedClass.INVALID);
        setFilterColor(prefs, FeedClass.LOW_RATED);
        setFilterColor(prefs, FeedClass.READ);
        setFilterColor(prefs, FeedClass.UNDISCOVERED);

        setBoolean(prefs, UserPreferences.PROP_SORTING_ENABLED, up.isSortingEnabled());
        setInt(prefs, UserPreferences.PROP_SORT_BY_CLASS_1, up.getSortByClass1());
        setInt(prefs, UserPreferences.PROP_SORT_BY_CLASS_2, up.getSortByClass2());
        setBoolean(prefs, UserPreferences.PROP_REVERSED_SORT_BY_CLASS_1,
            up.isReversedSortByClass1());
        setBoolean(prefs, UserPreferences.PROP_REVERSED_SORT_BY_CLASS_2,
            up.isReversedSortByClass2());
    }

    /**
     * Stores articles preferences.
     *
     * @param prefs preferences map.
     */
    private void storeArticlesPreferences(Map prefs)
    {
        UserPreferences up = model.getUserPreferences();
        FeedRenderingSettings frs = model.getGlobalRenderingSettings();

        setBoolean(prefs, "groupingEnabled", frs.isGroupingEnabled());
        setBoolean(prefs, "suppressingOlderThan", frs.isSuppressingOlderThan());
        setBoolean(prefs, "displayingFullTitles", frs.isDisplayingFullTitles());
        setBoolean(prefs, "sortingAscending", frs.isSortingAscending());
        setInt(prefs, "suppressOlderThan", frs.getSuppressOlderThan());

        setBoolean(prefs, UserPreferences.PROP_COPY_LINKS_IN_HREF_FORMAT,
            up.isCopyLinksInHrefFormat());
        setBoolean(prefs, "showEmptyGroups", frs.isShowEmptyGroups());
        setBoolean(prefs, UserPreferences.PROP_BROWSE_ON_DBL_CLICK, up.isBrowseOnDblClick());

        up.getViewModePreferences().store(prefs);
    }

    /**
     * Stores tags preferences.
     *
     * @param prefs preferences map.
     */
    private void storeTagsPreferences(Map prefs)
    {
        UserPreferences up = model.getUserPreferences();

        setInt(prefs, UserPreferences.PROP_TAGS_STORAGE, up.getTagsStorage());
        setString(prefs, UserPreferences.PROP_TAGS_DELICIOUS_USER, up.getTagsDeliciousUser());
        setString(prefs, UserPreferences.PROP_TAGS_DELICIOUS_PASSWORD,
            up.getTagsDeliciousPassword());
        setBoolean(prefs, UserPreferences.PROP_TAGS_AUTOFETCH, up.isTagsAutoFetch());
        setBoolean(prefs, UserPreferences.PROP_PIN_TAGGING, up.isPinTagging());
        setString(prefs, UserPreferences.PROP_PIN_TAGS, up.getPinTags());
    }

    /**
     * Stores reading lists preferences.
     *
     * @param prefs preferences map.
     */
    private void storeReadingListsPreferences(Map prefs)
    {
        UserPreferences up = model.getUserPreferences();

        setLong(prefs, UserPreferences.PROP_READING_LIST_UPDATE_PERIOD,
            up.getReadingListUpdatePeriod());
        setInt(prefs, UserPreferences.PROP_ON_READING_LIST_UPDATE_ACTIONS,
            up.getOnReadingListUpdateActions());
        setBoolean(prefs, UserPreferences.PROP_UPDATE_FEEDS,
            up.isUpdateFeeds());
        setBoolean(prefs, UserPreferences.PROP_UPDATE_READING_LISTS,
            up.isUpdateReadingLists());
    }

    /**
     * Stores advanced preferences.
     *
     * @param prefs preferences map.
     */
    private void storeAdvancedPreferences(Map prefs)
    {
        UserPreferences up = model.getUserPreferences();
        StarzPreferences sp = model.getStarzPreferences();

        setInt(prefs, UserPreferences.PROP_FEED_SELECTION_DELAY, up.getFeedSelectionDelay());
        setBoolean(prefs, UserPreferences.PROP_AA_TEXT, up.isAntiAliasText());

        setInt(prefs, StarzPreferences.PROP_TOP_ACTIVITY, sp.getTopActivity());
        setInt(prefs, StarzPreferences.PROP_TOP_HIGHLIGHTS, sp.getTopHighlights());

        setBoolean(prefs, UserPreferences.PROP_SHOW_TOOLBAR_LABELS, up.isShowToolbarLabels());
        setBoolean(prefs, UserPreferences.PROP_SHOW_UNREAD_BUTTON_MENU,
            up.isShowUnreadButtonMenu());
        setInt(prefs, UserPreferences.PROP_FEED_IMPORT_LIMIT, up.getFeedImportLimit());
    }

    /**
     * Stores what's hot preferences.
     *
     * @param prefs preferences map.
     */
    private void storeWhatsHotPreferences(Map prefs)
    {
        UserPreferences up = model.getUserPreferences();
        setString(prefs, UserPreferences.PROP_WH_IGNORE, up.getWhIgnore());
        setBoolean(prefs, UserPreferences.PROP_WH_NOSELFLINKS, up.isWhNoSelfLinks());
        setBoolean(prefs, UserPreferences.PROP_WH_SUPPRESS_SAME_SOURCE_LINKS, up.isWhSuppressSameSourceLinks());
        setString(prefs, UserPreferences.PROP_WH_TARGET_GUIDE, up.getWhTargetGuide());
        setLong(prefs, UserPreferences.PROP_WH_SETTINGS_CHANGE_TIME, up.getWhSettingsChangeTime());
    }

    /**
     * Stores Twitter preferences.
     *
     * @param prefs prefs.
     */
    private void storeTwitterPreferences(Map prefs)
    {
        TwitterPreferences tp = model.getUserPreferences().getTwitterPreferences();
        setBoolean(prefs, TwitterPreferences.PROP_TWITTER_ENABLED, tp.isEnabled());
        setString(prefs, TwitterPreferences.PROP_TWITTER_SCREEN_NAME, tp.getScreenName());
        setString(prefs, TwitterPreferences.PROP_TWITTER_PASSWORD, tp.getPassword());
        setBoolean(prefs, TwitterPreferences.PROP_TWITTER_PROFILE_PICS, tp.isProfilePics());
    }

    /**
     * Saves boolean to preferences map.
     *
     * @param prefs     preferences map.
     * @param name      property name.
     * @param value     value.
     */
    public static void setBoolean(Map prefs, String name, boolean value)
    {
        setString(prefs, name, Boolean.toString(value));
    }

    /**
     * Saves integer property to preferences map.
     *
     * @param prefs     preferences map.
     * @param name      property name.
     * @param value     value.
     */
    public static void setInt(Map prefs, String name, int value)
    {
        setString(prefs, name, Integer.toString(value));
    }

    private static void setLong(Map prefs, String name, long value)
    {
        setString(prefs, name, Long.toString(value));
    }

    private static void setFilterColor(Map prefs, int feedClass)
    {
        FeedDisplayModeManager fdmm = FeedDisplayModeManager.getInstance();
        Color color = fdmm.getColor(feedClass);
        setString(prefs, "cdmm." + feedClass, UifUtilities.colorToHex(color));
    }

    public static void setString(Map prefs, String name, String value)
    {
        prefs.put(name, StringUtils.toUTF8(value));
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Pinging
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Ping URL with all published reading lists.
     */
    private void pingGuides()
    {
        Thread thPing = new Thread(new Runnable()
        {
            public void run()
            {
                // Ping URL
                GlobalModel model = GlobalController.SINGLETON.getModel();
                UserPreferences prefs = model.getUserPreferences();
                String url = prefs.getPingOnReadingListPublicationURL().trim();
                if (prefs.isPingOnReadingListPublication() && url.length() > 0 && url.indexOf("%u") != -1)
                {
                    IGuide[] publishedGuides = collectGuides(model.getGuidesSet());
                    pingGuides(publishedGuides, url);
                }
            }
        }, THREAD_NAME_PING);

        thPing.start();
    }

    /**
     * Pings all guides one by one.
     *
     * @param guides    guide list.
     * @param url       URL to ping with guide publication URL included.
     */
    private void pingGuides(IGuide[] guides, String url)
    {
        for (IGuide guide : guides)
        {
            String realURL = url.replaceAll("%u", guide.getPublishingURL());
            try
            {
                ping(new URL(realURL));
            } catch (Throwable e)
            {
                LOG.log(Level.WARNING, Strings.error("sync.failed.to.ping.reading.list.service"), e);
            }
        }
    }

    /**
     * Pings the URL.
     *
     * @param url url to ping.
     *
     * @throws java.io.IOException I/O exception.
     */
    private void ping(URL url) throws IOException
    {
        // Read one byte to make sure that we connected
        InputStream stream = url.openStream();

        //noinspection ResultOfMethodCallIgnored
        stream.read();
        stream.close();
    }

    /**
     * Returns all guides which have publication flag set and the publication URL available.
     *
     * @param set guides set.
     *
     * @return list of guides.
     */
    private IGuide[] collectGuides(GuidesSet set)
    {
        StandardGuide[] guides = set.getStandardGuides(null);
        java.util.List<StandardGuide> rl = new ArrayList<StandardGuide>();
        for (StandardGuide guide : guides)
        {
            String url = guide.getPublishingURL();
            if (guide.isPublishingEnabled() && url != null && url.trim().length() > 0) rl.add(guide);
        }

        return rl.toArray(new IGuide[rl.size()]);
    }
}
