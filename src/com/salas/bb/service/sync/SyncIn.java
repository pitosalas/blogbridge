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
// $Id: SyncIn.java,v 1.96 2008/02/28 15:59:50 spyromus Exp $
//

package com.salas.bb.service.sync;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.application.Application;
import com.salas.bb.core.DeletedObjectsRepository;
import com.salas.bb.core.FeedDisplayModeManager;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.*;
import com.salas.bb.domain.prefs.StarzPreferences;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.domain.querytypes.QueryType;
import com.salas.bb.domain.utils.FeedCheckBox;
import com.salas.bb.domain.utils.ReadingListCheckBox;
import com.salas.bb.imageblocker.ImageBlocker;
import com.salas.bb.plugins.Manager;
import com.salas.bb.sentiments.SentimentsConfig;
import com.salas.bb.service.ServerService;
import com.salas.bb.service.ServerServiceException;
import com.salas.bb.service.ServicePreferences;
import com.salas.bb.utils.DateUtils;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.opml.Helper;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.CheckBoxList;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.views.settings.FeedRenderingSettings;
import com.salas.bb.views.settings.RenderingSettingsNames;
import com.salas.bbutilities.opml.Importer;
import com.salas.bbutilities.opml.ImporterException;
import com.salas.bbutilities.opml.objects.OPMLGuideSet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ingoing synchronization module.
 */
public class SyncIn extends AbstractSynchronization
{
    private static final Logger LOG = Logger.getLogger(SyncIn.class.getName());

    private static final String MSG_ERROR_DURING_SYNC_IN = Strings.error("sync.error.during.sync.in");

    /** <code>TRUE</code> to copy the layout of the server side. */
    private boolean copyServiceLayout;

    /**
     * Creates synchronization modules for the model.
     *
     * @param aModel model.
     * @param aCopyServiceSide <code>TRUE</code> to copy the layout of the server side.
     */
    public SyncIn(GlobalModel aModel, boolean aCopyServiceSide)
    {
        super(aModel);
        copyServiceLayout = aCopyServiceSide;
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
        SyncInStats stats = new SyncInStats();

        try
        {
            if (servicePreferences.isSyncFeeds())
            {
                if (progress != null) progress.processStep(Strings.message("service.sync.in.loading.guides.and.feeds"));
                loadFeeds(aEmail, aPassword, stats);
                if (progress != null) progress.processStepCompleted();
            }

            if (servicePreferences.isSyncPreferences())
            {
                if (progress != null) progress.processStep(Strings.message("service.sync.in.loading.preferences"));
                loadPreferences(aEmail, aPassword, stats);
                if (progress != null) progress.processStepCompleted();
            }

            servicePreferences.setLastSyncInStatus(ServicePreferences.SYNC_STATUS_SUCCESS);
        } catch (ServerServiceException e1)
        {
            // If the cause of service exception is another exception then log it
            if (e1.getCause() != null)
            {
                LOG.log(Level.SEVERE, MSG_ERROR_DURING_SYNC_IN, e1);
                stats.registerFailure(null);
            } else
            {
                stats.registerFailure(e1.getMessage());
            }
        } catch (ImporterException e1)
        {
            LOG.log(Level.SEVERE, MSG_ERROR_DURING_SYNC_IN, e1);
            stats.registerFailure(null);
        }

        if (stats.hasFailed()) servicePreferences.setLastSyncInStatus(ServicePreferences.SYNC_STATUS_FAILURE);

        // update last sync out date
        servicePreferences.setLastSyncInDate(new Date());

        return stats;
    }

    /**
     * Returns the message to be reported on synchronization start.
     *
     * @return message.
     */
    protected String getProcessStartMessage()
    {
        return prepareProcessStartMessage(
            Strings.message("service.sync.message.loading"),
            Strings.message("service.sync.message.preferences"),
            Strings.message("service.sync.message.guides.and.feeds"),
            Strings.message("service.sync.message.from.blogbridge.service"));
    }

    /**
     * Loads preferences from service.
     *
     * @param email     email of user account.
     * @param password  password of user account.
     * @param stats     stats to fill.
     *
     * @throws ServerServiceException in case of service failure.
     */
    private void loadPreferences(String email, String password, SyncInStats stats)
        throws ServerServiceException
    {
        UserPreferences up = model.getUserPreferences();

        Map prefs = ServerService.syncRestorePrefs(email, password);

        Date lastUpdateTimeO = up.getLastUpdateTime();
        long localUpdateTime = lastUpdateTimeO == null ? -1 : lastUpdateTimeO.getTime();
        long remoteUpdateTime = getLong(prefs, "timestamp", -1);

        int loaded;

        // If local changes happened after saving preferences to the service, do not update them
        if (copyServiceLayout || localUpdateTime < remoteUpdateTime)
        {
            // Image blocker list
            String ibExpressions = getPreferenceValue(prefs, ImageBlocker.KEY);
            ImageBlocker.setExpressions(ibExpressions);

            SentimentsConfig.syncIn(prefs);

            loadUserPreferences(prefs);
            loaded = prefs.size();
        } else loaded = 0;

        // Loading What's Hot preferences blog with independent change timestamp
        long local = DateUtils.localToUTC(up.getWhSettingsChangeTime());
        long remote = getLong(prefs, UserPreferences.PROP_WH_SETTINGS_CHANGE_TIME, 0);
        if (copyServiceLayout || remote > local)
        {
            // If the settings were changed remotely, copy them
            up.setWhIgnore(getString(prefs, UserPreferences.PROP_WH_IGNORE,
                UserPreferences.DEFAULT_WH_IGNORE));
            up.setWhNoSelfLinks(getBoolean(prefs, UserPreferences.PROP_WH_NOSELFLINKS,
                UserPreferences.DEFAULT_WH_NOSELFLINKS));
            up.setWhSuppressSameSourceLinks(getBoolean(prefs, UserPreferences.PROP_WH_SUPPRESS_SAME_SOURCE_LINKS,
                UserPreferences.DEFAULT_WH_SUPPRESS_SAME_SOURCE_LINKS));
            up.setWhTargetGuide(getString(prefs, UserPreferences.PROP_WH_TARGET_GUIDE,
                UserPreferences.DEFAULT_WH_TARGET_GUIDE));
            up.setWhSettingsChangeTime(remote);
        }

        stats.loadedPreferences = loaded;
    }

    private static String getPreferenceValue(Map preferences, String key)
    {
        return StringUtils.fromUTF8((byte[])preferences.get(key));
    }

    /**
     * Synchronizes feeds (loads from service and merges them with local list).
     *
     * @param email     email of user.
     * @param password  password of user.
     * @param stats     stats to fill.
     *
     * @throws ServerServiceException   in case of error with service.
     * @throws ImporterException        in case of problems with OPML.
     */
    private void loadFeeds(String email, String password, final SyncInStats stats)
        throws ServerServiceException, ImporterException
    {
        String opml = ServerService.syncRestore(email, password);

        // parse output from server w/ possible empty guides
        final Importer im = new Importer();
        im.setAllowEmptyGuides(true);

        OPMLGuideSet set = im.processFromString(opml, false);

        GuidesSet remoteSet = Helper.createGuidesSet(null, set);
        final GuidesSet localSet = model.getGuidesSet();
        final Changes changes = evaluateChanges(localSet, remoteSet, copyServiceLayout);

        // EDT
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                public void run()
                {
                    if (adjustChangesByUser(changes))
                    {
                        IGuide selectedGuide = model.getSelectedGuide();
                        IFeed selectedFeed = model.getSelectedFeed();

                        performChanges(localSet, changes, stats);
                        restoreSelection(selectedGuide, selectedFeed);

                        // Update synchronization times
                        localSet.onSyncInCompletion();
                    }
                }
            });
        } catch (Throwable e)
        {
            if (e instanceof InvocationTargetException) e = e.getCause();
            LOG.log(Level.SEVERE, MSG_ERROR_DURING_SYNC_IN, e.getCause());
        }
    }

    /**
     * Shows alert and allows user to adjust his.
     *
     * @param aChanges changes we are going to make.
     *
     * @return <code>TRUE</code> if user has accepted the changes.
     */
    static boolean adjustChangesByUser(Changes aChanges)
    {
        boolean accepted = true;

        List<IFeed> newFeeds = aChanges.getAddFeeds();

        if (newFeeds.size() > 0 || aChanges.getAddReadingLists().size() > 0)
        {
            ChangesConfirmationDialog dialog =
                new ChangesConfirmationDialog(Application.getDefaultParentFrame(),
                    aChanges.getAddReadingLists(), newFeeds);

            dialog.open();

            accepted = !dialog.hasBeenCanceled();
        }

        return accepted;
    }

    /**
     * Restores selected feed and guide.
     *
     * @param aSelectedGuide    selected guide.
     * @param aSelectedFeed     selected feed.
     */
    private static void restoreSelection(IGuide aSelectedGuide, IFeed aSelectedFeed)
    {
        if (aSelectedFeed == null || aSelectedGuide == null) return;

        GlobalController controller = GlobalController.SINGLETON;
        if (controller != null)
        {
            // EDT !!!
            controller.selectGuide(aSelectedGuide, false);
            controller.selectFeed(aSelectedFeed);
        }
    }

    /**
     * Performs changes.
     *
     * @param set       set to update.
     * @param changes   changes.
     * @param stats     stats to fill in.
     */
    static void performChanges(GuidesSet set, Changes changes, SyncInStats stats)
    {
        int oldGuidesCount = set.getGuidesCount();

        performChangesAddFeeds(set, changes.getAddFeeds(), stats);
        performChangesAddReadingLists(set, changes.getAddReadingLists(), stats);
        performChangesRemoveReadingLists(changes.getRemoveReadingLists());
        performChangesRemoveFeeds(changes.getRemoveFeeds());

        performChangesUpdateFeeds(changes);
        performChangesUpdateGuides(changes.getUpdateGuides());

        performChangesRemoveEmptyGuides(set);

        if (stats != null)
        {
            stats.createdGuides = set.getGuidesCount() - oldGuidesCount;
            if (stats.createdGuides < 0) stats.createdGuides = 0;
        }
    }

    /**
     * Applies changes to the guides.
     *
     * @param guidePairs guide pairs.
     */
    private static void performChangesUpdateGuides(List<GuidePair> guidePairs)
    {
        for (GuidePair pair : guidePairs)
        {
            IGuide localGuide = pair.local;
            IGuide remoteGuide = pair.remote;

            transferGuideProperties(localGuide, remoteGuide);
        }
    }

    /**
     * Transfers properties from one guide to another.
     *
     * @param guide     target guide.
     * @param pattern   pattern guide.
     */
    static void transferGuideProperties(IGuide guide, IGuide pattern)
    {
        guide.setIconKey(pattern.getIconKey());
        guide.setPublishingEnabled(pattern.isPublishingEnabled());
        guide.setPublishingTitle(pattern.getPublishingTitle());
        guide.setPublishingTags(pattern.getPublishingTags());
        guide.setPublishingPublic(pattern.isPublishingPublic());
        guide.setPublishingRating(pattern.getPublishingRating());
        guide.setAutoFeedsDiscovery(pattern.isAutoFeedsDiscovery());
        guide.setNotificationsAllowed(pattern.isNotificationsAllowed());
    }

    /**
     * Applies read articles match keys to feeds.
     *
     * @param changes set of changes.
     */
    private static void performChangesUpdateFeeds(Changes changes)
    {
        Map<DataFeed, FeedPair> aFeeds = changes.getUpdateFeedsKeys();
        for (FeedPair pair : aFeeds.values())
        {
            DataFeed local = (DataFeed)pair.local;
            DataFeed remote = (DataFeed)pair.remote;

            local.setReadArticlesKeys(remote.getReadArticlesKeys());
            local.setPinnedArticlesKeys(remote.getPinnedArticlesKeys());
        }

        Collection<FeedPair> updateFeeds = changes.getUpdateFeeds();
        for (FeedPair pair : updateFeeds)
        {
            IFeed localFeed = pair.local;
            IFeed remoteFeed = pair.remote;

            // Move properties from remote feed to local
            localFeed.setCustomViewMode(remoteFeed.getCustomViewMode());
            localFeed.setCustomViewModeEnabled(remoteFeed.isCustomViewModeEnabled());
            localFeed.setRating(remoteFeed.getRating());
            localFeed.setType(remoteFeed.getType());

            // Feed type specific operations
            if (localFeed instanceof DirectFeed)
            {
                DirectFeed localDFeed = (DirectFeed)localFeed;
                DirectFeed remoteDFeed = (DirectFeed)remoteFeed;

                localDFeed.setCustomAuthor(remoteDFeed.getCustomAuthor());
                localDFeed.setCustomDescription(remoteDFeed.getCustomDescription());
                localDFeed.setCustomTitle(remoteDFeed.getCustomTitle());
                localDFeed.setDisabled(remoteDFeed.isDisabled());
                localDFeed.setPurgeLimit(remoteDFeed.getPurgeLimit());

                localDFeed.setUserTags(remoteDFeed.getUserTags());
                localDFeed.setTagsDescription(remoteDFeed.getTagsDescription());
                localDFeed.setTagsExtended(remoteDFeed.getTagsExtended());
            } else if (localFeed instanceof QueryFeed)
            {
                QueryFeed localQFeed = (QueryFeed)localFeed;
                QueryFeed remoteQFeed = (QueryFeed)remoteFeed;

                localQFeed.setPurgeLimit(remoteQFeed.getPurgeLimit());
                localQFeed.setQueryType(remoteQFeed.getQueryType());
                localQFeed.setParameter(remoteQFeed.getParameter());
            } else
            {
                SearchFeed localSFeed = (SearchFeed)localFeed;
                SearchFeed remoteSFeed = (SearchFeed)remoteFeed;

                localSFeed.setQuery(remoteSFeed.getQuery());
            }

            if (localFeed instanceof DataFeed)
            {
                DataFeed localDFeed = (DataFeed)localFeed;
                DataFeed removeDFeed = (DataFeed)remoteFeed;

                localDFeed.setUpdatePeriod(removeDFeed.getUpdatePeriod());
            }
        }
    }

    /**
     * Removes all reading lists schedule for removal and leaves feeds unassociated.
     *
     * @param aReadingLists reading lists.
     */
    private static void performChangesRemoveReadingLists(List<ReadingList> aReadingLists)
    {
        for (ReadingList list : aReadingLists)
        {
            StandardGuide guide = list.getParentGuide();
            guide.remove(list, true);
        }
    }

    /**
     * Adds reading lists and connects required feeds in the guide.
     *
     * @param aSet          guides set.
     * @param aReadingLists reading lists.
     * @param aStats        statistics.
     */
    private static void performChangesAddReadingLists(GuidesSet aSet, List<ReadingList> aReadingLists,
                                                      SyncInStats aStats)
    {
        for (ReadingList list : aReadingLists)
        {
            performChangesAddReadingList(aSet, list, aStats);
        }
    }

    /**
     * Adds reading list to the guide and associates all required feeds.
     *
     * @param aSet      guides set.
     * @param aList     reading list.
     * @param aStats    statistics.
     */
    private static void performChangesAddReadingList(GuidesSet aSet, ReadingList aList,
                                                     SyncInStats aStats)
    {
        StandardGuide guide = findOrCreateGuide(aSet, aList.getParentGuide());

        ReadingList newList = new ReadingList(aList.getURL());
        newList.setTitle(aList.getTitle());

        guide.add(newList);

        DirectFeed[] feedsToConnect = aList.getFeeds();
        for (DirectFeed feed : feedsToConnect)
        {
            DirectFeed existingFeed = aSet.findDirectFeed(feed.getXmlURL());
            boolean existing = true;
            if (existingFeed == null)
            {
                existingFeed = feed;

                feed.removeParentGuide(feed.getParentGuides()[0]);
                feed.removeAllReadingLists();

                if (aStats != null) aStats.addedFeeds++;
                existing = false;
            }

            newList.add(existingFeed);

            if (!existing) GlobalController.SINGLETON.getPoller().update(existingFeed, false);
        }
    }

    /**
     * Removes empty guides.
     *
     * @param set guides set.
     */
    private static void performChangesRemoveEmptyGuides(GuidesSet set)
    {
        StandardGuide[] guides = set.getStandardGuides(null);
        for (StandardGuide guide : guides)
        {
            if (guide.getFeedsCount() == 0) set.remove(guide);
        }
    }

    /**
     * Moves feeds marked for addition to the guides.
     *
     * @param aSet      local set.
     * @param aFeeds    feeds list.
     * @param aStats    stats to fill in.
     */
    private static void performChangesAddFeeds(GuidesSet aSet, List<IFeed> aFeeds,
                                               SyncInStats aStats)
    {
        for (IFeed feed : aFeeds)
        {
            IGuide guide = feed.getParentGuides()[0];
            if (guide.hasDirectLinkWith(feed))
            {
                StandardGuide localGuide = findOrCreateGuide(aSet, guide);

                // Disconnect feed from its old parent guide
                feed.removeParentGuide(guide);
                if (feed instanceof DirectFeed) ((DirectFeed)feed).removeAllReadingLists();

                // Find existing feed
                IFeed existingFeed = aSet.findFeed(feed);
                if (existingFeed != null) feed = existingFeed;

                localGuide.add(feed);
                StandardGuide.FeedLinkInfo info = localGuide.getFeedLinkInfo(feed);
                info.setLastSyncTime(System.currentTimeMillis());

                if (aStats != null) aStats.addedFeeds++;
            }
        }
    }

    /**
     * Finds a guide in the set of local guides or create a new one.
     *
     * @param aSet          set.
     * @param aPatternGuide pattern-guide.
     *
     * @return guide.
     */
    static StandardGuide findOrCreateGuide(GuidesSet aSet, IGuide aPatternGuide)
    {
        String title = aPatternGuide.getTitle();

        StandardGuide guide = findGuideByName(aSet.getStandardGuides(null), title);
        if (guide == null)
        {
            guide = new StandardGuide();
            guide.setTitle(title);
            transferGuideProperties(guide, aPatternGuide);

            aSet.add(guide);
        }

        return guide;
    }

    /**
     * Removes feed mentioned in the list.
     *
     * @param guideFeeds feeds list.
     */
    private static void performChangesRemoveFeeds(List<GuideFeedPair> guideFeeds)
    {
        for (GuideFeedPair guideFeed : guideFeeds)
        {
            IGuide localGuide = guideFeed.guide;
            IFeed localFeed = guideFeed.feed;

            // To be removed from some guide feed should be unassigned from the
            // reading lists in that guide first and then removed from the guide
            // itself

            if (localFeed instanceof DirectFeed)
            {
                DirectFeed dfeed = (DirectFeed)localFeed;
                ReadingList[] lists = dfeed.getReadingLists();
                for (ReadingList list : lists)
                {
                    if (list.getParentGuide() == localGuide) list.remove(dfeed);
                }
            }

            localGuide.remove(localFeed);
        }
    }

    /**
     * Compares two sets of guides and evaluates necessary changes.
     * This is the core of synchronization logic.
     *
     * @param aLocalSet     local set of guides.
     * @param aRemoteSet    remote set of guides.
     * @param copyServiceLayout <code>TRUE</code> to make a complete copy of the service side.
     *
     * @return changes.
     */
    static Changes evaluateChanges(GuidesSet aLocalSet, GuidesSet aRemoteSet,
                                   boolean copyServiceLayout)
    {
        Changes changes = new Changes();

        StandardGuide[] localGuides = aLocalSet.getStandardGuides(null);
        StandardGuide[] remoteGuides = aRemoteSet.getStandardGuides(null);

        DeletedObjectsRepository dfr = GlobalController.SINGLETON.getDeletedFeedsRepository();

        // Scan through remote guides and verify each guide user has.
        // If user has no some guide then put the guide in the list
        // of guides to add. If user has the guide, verify its contents.
        // Later we could scan the lists of guides for addition and
        // guides for removal in order to detect the renaming.
        for (StandardGuide remoteGuide : remoteGuides)
        {
            String remoteGuideTitle = remoteGuide.getTitle();

            StandardGuide localGuide = findGuideByName(localGuides, remoteGuideTitle);

            if (localGuide == null)
            {
                // There's no guide with such name locally -- add feeds that weren't removed
                for (int j = 0; j < remoteGuide.getFeedsCount(); j++)
                {
                    IFeed feed = remoteGuide.getFeedAt(j);
                    if (remoteGuide.hasDirectLinkWith(feed) &&
                        (copyServiceLayout || !dfr.wasDeleted(remoteGuideTitle, feed.getMatchKey())))
                            changes.addFeed(feed);
                }

                // Reading lists
                ReadingList[] readingLists = remoteGuide.getReadingLists();
                for (ReadingList rl : readingLists)
                {
                    if (copyServiceLayout || !dfr.wasDeleted(remoteGuideTitle, rl.getURL().toString()))
                        changes.addReadingList(rl);
                }
            } else
            {
                if (localGuide.getLastUpdateTime() < remoteGuide.getLastUpdateTime())
                {
                    changes.updateGuide(localGuide, remoteGuide);
                }

                evaluateChangesInGuide(localGuide, remoteGuide, changes, copyServiceLayout);
            }
        }

        // Scan through the list of local guides and mark for removal those not
        // mentioned in the list of remote.
        for (StandardGuide localGuide : localGuides)
        {
            StandardGuide remoteGuide = findGuideByName(remoteGuides, localGuide.getTitle());

            if (remoteGuide == null)
            {
                ReadingList[] readingLists = localGuide.getReadingLists();
                for (ReadingList readingList : readingLists)
                {
                    if (copyServiceLayout || readingList.getLastSyncTime() != -1)
                    {
                        changes.removeReadingList(readingList);
                    }
                }

                // Remove whole guide
                for (int j = 0; j < localGuide.getFeedsCount(); j++)
                {
                    IFeed feed = localGuide.getFeedAt(j);
                    StandardGuide.FeedLinkInfo info;

                    if (copyServiceLayout || ((info = localGuide.getFeedLinkInfo(feed)) != null &&
                        info.getLastSyncTime() != -1))
                    {
                        changes.removeFeed(localGuide, feed);
                    }
                }
            }
        }

        // Find changes in feeds
        FeedsList localFeeds = aLocalSet.getFeedsList();

        for (int i = 0; i < localFeeds.getFeedsCount(); i++)
        {
            IFeed localFeed = localFeeds.getFeedAt(i);
            IFeed remoteFeed = aRemoteSet.findFeed(localFeed);
            if (remoteFeed != null && (copyServiceLayout ||
                localFeed.getLastUpdateTime() < remoteFeed.getLastUpdateTime()))
            {
                changes.updateFeed(localFeed, remoteFeed);
            }
        }

        return changes;
    }

    /**
     * Looks for a guide with the given name.
     *
     * @param aGuides   guides list.
     * @param aName     name.
     *
     * @return target guide.
     */
    private static StandardGuide findGuideByName(StandardGuide[] aGuides, String aName)
    {
        StandardGuide guide = null;

        for (int i = 0; guide == null && i < aGuides.length; i++)
        {
            StandardGuide standardGuide = aGuides[i];
            if (aName.equals(standardGuide.getTitle())) guide = standardGuide;
        }

        return guide;
    }

    /**
     * Evaluates changes within single guide.
     *
     * @param aLocalGuide   local guide.
     * @param aRemoteGuide  remote guide.
     * @param aChanges      changes.
     * @param aClearNew     <code>TRUE</code> to remove any local feeds which aren't on the service.
     */
    static void evaluateChangesInGuide(StandardGuide aLocalGuide,
                                       StandardGuide aRemoteGuide, Changes aChanges, boolean aClearNew)
    {
        evaluateChangesInReadingLists(aLocalGuide, aRemoteGuide, aChanges, aClearNew);
        evaluateChangesInFeeds(aLocalGuide, aRemoteGuide, aChanges, aClearNew);
    }

    /**
     * Evaluates changes within single guide reading lists.
     *
     * @param aLocalGuide   local guide.
     * @param aRemoteGuide  remote guide.
     * @param aChanges      changes.
     * @param aClearNew     <code>TRUE</code> to remove any local feeds which aren't on the service.
     */
    static void evaluateChangesInReadingLists(StandardGuide aLocalGuide,
                                              StandardGuide aRemoteGuide, Changes aChanges, boolean aClearNew)
    {
        // Add new reading lists from the remote source
        ReadingList[] listsR = aRemoteGuide.getReadingLists();
        for (ReadingList listR : listsR)
        {
            ReadingList listL = findReadingList(aLocalGuide, listR);

            // A reading list has been added remotely
            if (listL == null) aChanges.addReadingList(listR);
        }

        // Remove local reading lists
        ReadingList[] listsL = aLocalGuide.getReadingLists();
        for (ReadingList listL : listsL)
        {
            ReadingList listR = findReadingList(aRemoteGuide, listL);

            // A reading list has been removed remotely or has not been sent to the service yet
            if (listR == null && (aClearNew || listL.getLastSyncTime() != -1))
            {
                aChanges.removeReadingList(listL);
            }
        }
    }

    /**
     * Looks for the same list in the guide.
     *
     * @param aGuide    guide.
     * @param aList     target list.
     *
     * @return list object or <code>NULL</code>.
     */
    private static ReadingList findReadingList(StandardGuide aGuide, ReadingList aList)
    {
        ReadingList[] lists = aGuide.getReadingLists();
        return findReadingList(lists, aList);
    }

    /**
     * Looks for the same list in the list.
     *
     * @param aLists        list of lists.
     * @param targetList    target list.
     *
     * @return list object or <code>NULL</code>.
     */
    private static ReadingList findReadingList(ReadingList[] aLists, ReadingList targetList)
    {
        ReadingList list = null;

        URL urlT = targetList.getURL();
        if (urlT != null)
        {
            String urlTS = urlT.toString();

            for (int i = 0; list == null && i < aLists.length; i++)
            {
                ReadingList readingList = aLists[i];
                URL url = readingList.getURL();
                if (url != null && url.toString().equals(urlTS)) list = readingList;
            }
        }

        return list;
    }

    /**
     * Evaluates changes within single guide.
     *
     * @param aLocalGuide   local guide.
     * @param aRemoteGuide  remote guide.
     * @param aChanges      changes.
     * @param aClearNew     <code>TRUE</code> to remove any local feeds which aren't on the service.
     */
    static void evaluateChangesInFeeds(StandardGuide aLocalGuide,
                                       StandardGuide aRemoteGuide, Changes aChanges, boolean aClearNew)
    {
        DeletedObjectsRepository dfr = GlobalController.SINGLETON.getDeletedFeedsRepository();

        // Add new feeds from remote source
        for (int i = 0; i < aRemoteGuide.getFeedsCount(); i++)
        {
            IFeed remoteFeed = aRemoteGuide.getFeedAt(i);
            if (!aRemoteGuide.hasDirectLinkWith(remoteFeed)) continue;

            IFeed localFeed = findFeed(aLocalGuide, remoteFeed);
            if (!aLocalGuide.hasDirectLinkWith(localFeed)) localFeed = null;

            // A feed has been added remotely
            if (localFeed == null)
            {
                if (aClearNew || !dfr.wasDeleted(aLocalGuide.getTitle(), remoteFeed.getMatchKey()))
                {
                    aChanges.addFeed(remoteFeed);
                }
            } else
            {
                if (remoteFeed instanceof DataFeed)
                {
                    aChanges.addUpdateFeedsKeys((DataFeed)localFeed, (DataFeed)remoteFeed);
                }
            }
        }

        // Remove local feeds
        for (int i = 0; i < aLocalGuide.getFeedsCount(); i++)
        {
            IFeed localFeed = aLocalGuide.getFeedAt(i);
            if (!aLocalGuide.hasDirectLinkWith(localFeed)) continue;

            IFeed remoteFeed = findFeed(aRemoteGuide, localFeed);
            if (!aRemoteGuide.hasDirectLinkWith(remoteFeed)) remoteFeed = null;

            StandardGuide.FeedLinkInfo info;

            // A feed has been removed remotely or has not been sent to service yet
            if (remoteFeed == null &&
                (aClearNew || ((info = aLocalGuide.getFeedLinkInfo(localFeed)) != null &&
                                info.getLastSyncTime() != -1)))
            {
                aChanges.removeFeed(aLocalGuide, localFeed);
            }
        }
    }

    /**
     * Finds a feed in the guide which is the same as the pattern-feed.
     * The similarity depends on the type of a feed.
     *
     * @param aGuide        guide.
     * @param aPatternFeed  pattern-feed.
     *
     * @return similar feed or <code>NULL</code>.
     */
    private static IFeed findFeed(StandardGuide aGuide, IFeed aPatternFeed)
    {
        IFeed targetFeed = null;
        for (int i = 0; targetFeed == null && i < aGuide.getFeedsCount(); i++)
        {
            IFeed feed = aGuide.getFeedAt(i);
            if (feedsAreTheSame(feed, aPatternFeed)) targetFeed = feed;
        }

        return targetFeed;
    }

    /**
     * Finds all feeds in the guide which is the same as the pattern-feed.
     * The similarity depends on the type of a feed.
     *
     * @param aGuide        guide.
     * @param aPatternFeed  pattern-feed.
     *
     * @return similar feeds.
     */
//    private static List findFeeds(StandardGuide aGuide, IFeed aPatternFeed)
//    {
//        List feeds = new ArrayList();
//        for (int i = 0; i < aGuide.getFeedsCount(); i++)
//        {
//            IFeed feed = aGuide.getFeedAt(i);
//            if (feedsAreTheSame(feed, aPatternFeed)) feeds.add(feed);
//        }
//
//        return feeds;
//    }

    /**
     * Compares two feeds and says if they are similar by their properties.
     *
     * @param aFeed         first feed.
     * @param aPatternFeed  second feed.
     *
     * @return <code>TRUE</code> if similar.
     */
    private static boolean feedsAreTheSame(IFeed aFeed, IFeed aPatternFeed)
    {
        boolean similar = false;

        if (aPatternFeed instanceof DirectFeed)
        {
            if (aFeed instanceof DirectFeed)
            {
                similar = feedsAreTheSameDirect((DirectFeed)aPatternFeed, (DirectFeed)aFeed);
            }
        } else if (aPatternFeed instanceof QueryFeed)
        {
            if (aFeed instanceof QueryFeed)
            {
                QueryFeed patFeed = (QueryFeed)aPatternFeed;
                QueryFeed matFeed = (QueryFeed)aFeed;

                QueryType patType = patFeed.getQueryType();
                QueryType matType = matFeed.getQueryType();

                String patParam = patFeed.getParameter();
                String matParam = matFeed.getParameter();

                similar = patType == matType && patParam.equals(matParam);
            }
        } else if (aPatternFeed instanceof SearchFeed)
        {
            if (aFeed instanceof SearchFeed)
            {
                String patQuery = ((SearchFeed)aPatternFeed).getQuery().serializeToString();
                String matQuery = ((SearchFeed)aFeed).getQuery().serializeToString();

                similar = patQuery.equals(matQuery);
            }
        }

        return similar;
    }

    /**
     * Checks if two direct feeds are the same.
     *
     * @param aPatternFeed  the feed to match against the other.
     * @param aMatchFeed    the target feed.
     *
     * @return <code>TRUE</code> if matches.
     */
    static boolean feedsAreTheSameDirect(DirectFeed aPatternFeed, DirectFeed aMatchFeed)
    {
        URL patXmlURL = aPatternFeed.getXmlURL();
        URL matXmlURL = aMatchFeed.getXmlURL();

        return patXmlURL != null && matXmlURL != null &&
            (aPatternFeed.calcSyncHash() == aMatchFeed.getSyncHash() ||
             matXmlURL.toString().equalsIgnoreCase(patXmlURL.toString()));
    }

    /**
     * Simple holder of stats.
     */
    private static class SyncInStats extends Stats
    {
        private int createdGuides;
        private int addedFeeds;
        private int loadedPreferences;

        /**
         * Returns custom text to be told if not failed.
         *
         * @return text.
         */
        protected String getCustomText()
        {
            StringBuffer message = new StringBuffer();

            if (createdGuides > 0) message.append(MessageFormat.format(
                Strings.message("service.sync.in.status.guides.created"),
                createdGuides));
            if (addedFeeds > 0) message.append(MessageFormat.format(
                Strings.message("service.sync.in.status.feeds.added"),
                addedFeeds));
            if (loadedPreferences > 0) message.append(MessageFormat.format(
                Strings.message("service.sync.in.status.preferences.loaded"),
                loadedPreferences));

            return message.toString();
        }
    }

    /** Changes to apply to local set of guides. */
    public static class Changes
    {
        private List<IFeed> addFeeds = new ArrayList<IFeed>();
        private Map<IFeed, FeedPair> updateFeeds = new IdentityHashMap<IFeed, FeedPair>();
        private List<GuidePair> updateGuides = new ArrayList<GuidePair>();
        private List<GuideFeedPair> removeFeeds = new ArrayList<GuideFeedPair>();
        private Map<DataFeed, FeedPair> updateFeedsKeys = new IdentityHashMap<DataFeed, FeedPair>();

        private List<ReadingList> addReadingLists = new ArrayList<ReadingList>();
        private List<ReadingList> removeReadingLists = new ArrayList<ReadingList>();

        /**
         * Puts a feed into the additions list.
         *
         * @param feed feed.
         */
        public void addFeed(IFeed feed)
        {
            addFeeds.add(feed);
        }

        /**
         * Puts a feed into the removals list.
         *
         * @param guide guide to remove from.
         * @param feed  feed.
         */
        public void removeFeed(IGuide guide, IFeed feed)
        {
            removeFeeds.add(new GuideFeedPair(guide, feed));
        }

        /**
         * Puts a feed into the updates list.
         *
         * @param local     local feed.
         * @param remote    remote feed.
         */
        public void updateFeed(IFeed local, IFeed remote)
        {
            if (!updateFeeds.containsKey(local))
            {
                updateFeeds.put(local, new FeedPair(local, remote));
            }
        }

        /**
         * Upts a guide into the updates list.
         *
         * @param local     local guide.
         * @param remote    remote guide.
         */
        public void updateGuide(IGuide local, IGuide remote)
        {
            updateGuides.add(new GuidePair(local, remote));
        }

        /**
         * Puts a reading list into the additions list.
         *
         * @param list list.
         */
        public void addReadingList(ReadingList list)
        {
            addReadingLists.add(list);
        }

        /**
         * Puts a reading list into the removals list.
         *
         * @param list list.
         */
        public void removeReadingList(ReadingList list)
        {
            removeReadingLists.add(list);
        }

        /**
         * Returns the list of feeds to be added.
         *
         * @return added feeds list.
         */
        public List<IFeed> getAddFeeds()
        {
            return addFeeds;
        }

        /**
         * Returns the list of guide-feeds pairs to be removed.
         *
         * @return removed guide-feeds list.
         *
         * @see GuideFeedPair
         */
        public List<GuideFeedPair> getRemoveFeeds()
        {
            return removeFeeds;
        }

        /**
         * Returns the list of feeds to update.
         *
         * @return feeds.
         */
        public Collection<FeedPair> getUpdateFeeds()
        {
            return updateFeeds.values();
        }

        /**
         * Returns the list of guides to update.
         *
         * @return guides.
         */
        public List<GuidePair> getUpdateGuides()
        {
            return updateGuides;
        }

        /**
         * Returns the list of reading lists to be added.
         *
         * @return added reading lists list.
         */
        public List<ReadingList> getAddReadingLists()
        {
            return addReadingLists;
        }

        /**
         * Returns the list of reading lists to be removed.
         *
         * @return removed reading lists list.
         */
        public List<ReadingList> getRemoveReadingLists()
        {
            return removeReadingLists;
        }

        /**
         * Registers read keys for a feed.
         *
         * @param localFeed feed.
         * @param remoteFeed remote feed.
         */
        public void addUpdateFeedsKeys(DataFeed localFeed, DataFeed remoteFeed)
        {
            updateFeedsKeys.put(localFeed, new FeedPair(localFeed, remoteFeed));
        }

        /**
         * Returns the map of feeds to read articles keys.
         *
         * @return map of feeds to keys.
         */
        public Map<DataFeed, FeedPair> getUpdateFeedsKeys()
        {
            return updateFeedsKeys;
        }
    }

    /**
     * Guide - Feed pair holder.
     */
    static class GuideFeedPair
    {
        final IGuide    guide;
        final IFeed     feed;

        /**
         * Creates object.
         *
         * @param aGuide    guide.
         * @param aFeed     feed.
         */
        public GuideFeedPair(IGuide aGuide, IFeed aFeed)
        {
            guide = aGuide;
            feed = aFeed;
        }
    }

    /**
     * Simple pair of guides.
     */
    static class GuidePair
    {
        final IGuide    local;
        final IGuide    remote;

        /**
         * Creates holder.
         *
         * @param aLocal    local guide.
         * @param aRemote   remote guide.
         */
        public GuidePair(IGuide aLocal, IGuide aRemote)
        {
            local = aLocal;
            remote = aRemote;
        }
    }

    /**
     * Simple feed pair holder.
     */
    static class FeedPair
    {
        final IFeed local;
        final IFeed remote;

        /**
         * Creates holder.
         *
         * @param aLocal    local feed.
         * @param aRemote   remote feed.
         */
        public FeedPair(IFeed aLocal, IFeed aRemote)
        {
            local = aLocal;
            remote = aRemote;
        }
    }

    /**
     * The dialog for accepting / rejecting changes and modifying the list
     * of feeds to add.
     */
    private static class ChangesConfirmationDialog extends AbstractDialog
    {
        private final java.util.List<IFeed> addFeeds;
        private final java.util.List<ReadingList> addReadingLists;

        private final CheckBoxList lstAddFeeds;
        private final CheckBoxList lstAddReadingLists;

        /**
         * Creates alert.
         *
         * @param frame     parent frame.
         * @param newLists  new reading lists
         * @param newFeeds  new feeds.
         */
        public ChangesConfirmationDialog(Frame frame, List<ReadingList> newLists, List<IFeed> newFeeds)
        {
            super(frame);

            addFeeds = newFeeds;
            lstAddFeeds = new CheckBoxList();
            lstAddFeeds.setListData(FeedCheckBox.wrap(addFeeds));

            addReadingLists = newLists;
            lstAddReadingLists = new CheckBoxList();
            lstAddReadingLists.setListData(ReadingListCheckBox.wrap(addReadingLists));

            enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        }

        /**
         * Builds dialog content.
         *
         * @return content.
         */
        protected JComponent buildContent()
        {
            JPanel panel = new JPanel(new BorderLayout());

            panel.add(buildBody(), BorderLayout.CENTER);
            panel.add(buildButtonBarWithOKCancel(), BorderLayout.SOUTH);

            return panel;
        }

        /**
         * Creates body part.
         *
         * @return body part.
         */
        private Component buildBody()
        {
            BBFormBuilder builder = new BBFormBuilder("p, 4dlu, 100dlu, 0, p");

            JComponent wording = ComponentsFactory.createWrappedMultilineLabel(
                Strings.message("service.sync.in.wording"));

            builder.append(wording, 5);
            builder.appendUnrelatedComponentsGapRow(2);

            boolean feeds = addFeeds.size() > 0;
            boolean lists = addReadingLists.size() > 0;

            if (feeds)
            {
                builder.append(Strings.message("service.sync.in.confirmation.feeds.to.add"), 3,
                    CheckBoxList.createAllNonePanel(lstAddFeeds), 1);
                builder.appendRow("50dlu:grow");
                builder.append(new JScrollPane(lstAddFeeds), 5,
                    CellConstraints.FILL, CellConstraints.FILL);

                if (lists) builder.appendUnrelatedComponentsGapRow(2);
            }

            if (lists)
            {
                builder.append(Strings.message("service.sync.in.confirmation.readinglists.to.add"), 3,
                    CheckBoxList.createAllNonePanel(lstAddReadingLists), 1);
                builder.appendRow("50dlu:grow");
                builder.append(new JScrollPane(lstAddReadingLists), 5,
                    CellConstraints.FILL, CellConstraints.FILL);

                builder.appendUnrelatedComponentsGapRow(2);
            }

            return builder.getPanel();
        }

        /**
         * Handles window events depending on the state of the <code>defaultCloseOperation</code>
         * property.
         *
         * @see #setDefaultCloseOperation
         */
        protected void processWindowEvent(WindowEvent e)
        {
            super.processWindowEvent(e);

            if (e.getID() == WindowEvent.WINDOW_OPENED)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        pack();
                    }
                });
            }
        }

        /**
         * Shows dialog and updates the list of feeds to add.
         */
        public void open()
        {
            super.open();

            if (!hasBeenCanceled())
            {
                addFeeds.clear();

                ListModel model = lstAddFeeds.getModel();
                for (int i = 0; i < model.getSize(); i++)
                {
                    FeedCheckBox fcb = (FeedCheckBox)model.getElementAt(i);
                    if (fcb.isSelected()) addFeeds.add(fcb.getFeed());
                }

                addReadingLists.clear();

                model = lstAddReadingLists.getModel();
                for (int i = 0; i < model.getSize(); i++)
                {
                    ReadingListCheckBox rlcb = (ReadingListCheckBox)model.getElementAt(i);
                    if (rlcb.isSelected()) addReadingLists.add(rlcb.getList());
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Loading preferences
    // ---------------------------------------------------------------------------------------------

    /**
     * Restores user preferences.
     *
     * @param prefs prefs.
     */
    private void loadUserPreferences(Map<String, Object> prefs)
    {
        loadGeneralPreferences(prefs);
        loadGuidesPreferences(prefs);
        loadFeedsPreferences(prefs);
        loadArticlesPreferences(prefs);
        loadTagsPreferences(prefs);
        loadReadingListsPrefereneces(prefs);
        loadAdvancedPreferences(prefs);
        Manager.restoreState(prefs);
    }

    /**
     * Loads general preferences into the model.
     *
     * @param prefs preferences map to take info from.
     */
    private void loadGeneralPreferences(Map prefs)
    {
        UserPreferences up = model.getUserPreferences();
//        FeedRenderingSettings frs = model.getGlobalRenderingSettings();

        up.setCheckingForUpdatesOnStartup(getBoolean(prefs,
            UserPreferences.PROP_CHECKING_FOR_UPDATES_ON_STARTUP,
            up.isCheckingForUpdatesOnStartup()));
// Disabled as we don't like what happens when synchronizing fonts across platforms
//        frs.setMainContentFont(getFont(prefs,
//            RenderingSettingsNames.MAIN_CONTENT_FONT,
//            frs.getMainContentFont()));
        up.setShowToolbar(getBoolean(prefs,
            UserPreferences.PROP_SHOW_TOOLBAR,
            up.isShowToolbar()));

        // Behavior
        up.setMarkReadWhenChangingChannels(getBoolean(prefs,
            UserPreferences.PROP_MARK_READ_WHEN_CHANGING_CHANNELS,
            up.isMarkReadWhenChangingChannels()));
        up.setMarkReadWhenChangingGuides(getBoolean(prefs,
            UserPreferences.PROP_MARK_READ_WHEN_CHANGING_GUIDES,
            up.isMarkReadWhenChangingGuides()));
        up.setMarkReadAfterDelay(getBoolean(prefs,
            UserPreferences.PROP_MARK_READ_AFTER_DELAY,
            up.isMarkReadAfterDelay()));
        up.setMarkReadAfterSeconds(getInt(prefs,
            UserPreferences.PROP_MARK_READ_AFTER_SECONDS,
            up.getMarkReadAfterSeconds()));

        // Updates and Cleanups
        up.setRssPollInterval(getInt(prefs,
            UserPreferences.PROP_RSS_POLL_MIN,
            up.getRssPollInterval()));
        up.setPurgeCount(getInt(prefs,
            UserPreferences.PROP_PURGE_COUNT,
            up.getPurgeCount()));
        up.setPreserveUnread(getBoolean(prefs,
            UserPreferences.PROP_PRESERVE_UNREAD,
            up.isPreserveUnread()));
    }

    /**
     * Loads guides preferences from the map.
     *
     * @param prefs preferences map.
     */
    private void loadGuidesPreferences(Map prefs)
    {
        UserPreferences up = model.getUserPreferences();
        FeedRenderingSettings frs = model.getGlobalRenderingSettings();

        up.setPingOnReadingListPublication(getBoolean(prefs,
            UserPreferences.PROP_PING_ON_RL_PUBLICATION,
            up.isPingOnReadingListPublication()));
        up.setPingOnReadingListPublicationURL(getString(prefs,
            UserPreferences.PROP_PING_ON_RL_PUBLICATION_URL,
            up.getPingOnReadingListPublicationURL()));

        frs.setBigIconInGuides(getBoolean(prefs,
            RenderingSettingsNames.IS_BIG_ICON_IN_GUIDES,
            frs.isBigIconInGuides()));
        frs.setShowUnreadInGuides(getBoolean(prefs,
            "showUnreadInGuides",
            frs.isShowUnreadInGuides()));
        frs.setShowIconInGuides(getBoolean(prefs,
            RenderingSettingsNames.IS_ICON_IN_GUIDES_SHOWING,
            frs.isShowIconInGuides()));
        frs.setShowTextInGuides(getBoolean(prefs,
            RenderingSettingsNames.IS_TEXT_IN_GUIDES_SHOWING,
            frs.isShowTextInGuides()));

        up.setGuideSelectionMode(getInt(prefs,
            UserPreferences.PROP_GUIDE_SELECTION_MODE,
            up.getGuideSelectionMode()));
    }

    /**
     * Loads feeds preferences from the map.
     *
     * @param prefs prefrences map.
     */
    private void loadFeedsPreferences(Map prefs)
    {
        UserPreferences up = model.getUserPreferences();
        FeedRenderingSettings frs = model.getGlobalRenderingSettings();

        frs.setShowStarz(getBoolean(prefs, "showStarz", frs.isShowStarz()));
        frs.setShowUnreadInFeeds(getBoolean(prefs, "showUnreadInFeeds", frs.isShowUnreadInFeeds()));
        frs.setShowActivityChart(getBoolean(prefs, "showActivityChart", frs.isShowActivityChart()));

        getFilterColor(prefs, FeedClass.DISABLED);
        getFilterColor(prefs, FeedClass.INVALID);
        getFilterColor(prefs, FeedClass.LOW_RATED);
        getFilterColor(prefs, FeedClass.READ);
        getFilterColor(prefs, FeedClass.UNDISCOVERED);

        up.setSortingEnabled(getBoolean(prefs,
            UserPreferences.PROP_SORTING_ENABLED,
            up.isSortingEnabled()));
        up.setSortByClass1(getInt(prefs,
            UserPreferences.PROP_SORT_BY_CLASS_1,
            up.getSortByClass1()));
        up.setSortByClass2(getInt(prefs,
            UserPreferences.PROP_SORT_BY_CLASS_2,
            up.getSortByClass2()));
        up.setReversedSortByClass1(getBoolean(prefs,
            UserPreferences.PROP_REVERSED_SORT_BY_CLASS_1,
            up.isReversedSortByClass1()));
        up.setReversedSortByClass2(getBoolean(prefs,
            UserPreferences.PROP_REVERSED_SORT_BY_CLASS_2,
            up.isReversedSortByClass2()));
    }

    /**
     * Loads articles preferences.
     *
     * @param prefs prefs.
     */
    private void loadArticlesPreferences(Map prefs)
    {
        UserPreferences up = model.getUserPreferences();
        FeedRenderingSettings frs = model.getGlobalRenderingSettings();

        frs.setGroupingEnabled(getBoolean(prefs, "groupingEnabled", frs.isGroupingEnabled()));
        frs.setSuppressingOlderThan(getBoolean(prefs,
            "suppressingOlderThan", frs.isSuppressingOlderThan()));
        frs.setDisplayingFullTitles(getBoolean(prefs,
            "displayingFullTitles", frs.isDisplayingFullTitles()));
        frs.setSortingAscending(getBoolean(prefs,
            "sortingAscending", frs.isSortingAscending()));
        frs.setSuppressOlderThan(getInt(prefs,
            "suppressOlderThan", frs.getSuppressOlderThan()));

        up.setCopyLinksInHrefFormat(getBoolean(prefs,
            UserPreferences.PROP_COPY_LINKS_IN_HREF_FORMAT, up.isCopyLinksInHrefFormat()));
        frs.setShowEmptyGroups(getBoolean(prefs, "showEmptyGroups", frs.isShowEmptyGroups()));
        up.setBrowseOnDblClick(getBoolean(prefs,
            UserPreferences.PROP_BROWSE_ON_DBL_CLICK, up.isBrowseOnDblClick()));

        up.getViewModePreferences().restore(prefs);
    }

    /**
     * Loads tags preferences from the map.
     *
     * @param prefs prefs.
     */
    private void loadTagsPreferences(Map prefs)
    {
        UserPreferences up = model.getUserPreferences();

        up.setTagsStorage(getInt(prefs, UserPreferences.PROP_TAGS_STORAGE, up.getTagsStorage()));
        up.setTagsDeliciousUser(getString(prefs,
            UserPreferences.PROP_TAGS_DELICIOUS_USER, up.getTagsDeliciousUser()));
        up.setTagsDeliciousPassword(getString(prefs,
            UserPreferences.PROP_TAGS_DELICIOUS_PASSWORD, up.getTagsDeliciousPassword()));
        up.setTagsAutoFetch(getBoolean(prefs,
            UserPreferences.PROP_TAGS_AUTOFETCH, up.isTagsAutoFetch()));

        up.setPinTagging(getBoolean(prefs,
            UserPreferences.PROP_PIN_TAGGING, up.isPinTagging()));
        up.setPinTags(getString(prefs,
            UserPreferences.PROP_PIN_TAGS, up.getPinTags()));
    }

    /**
     * Loads reading lists preferences from the map.
     *
     * @param prefs preferences.
     */
    private void loadReadingListsPrefereneces(Map prefs)
    {
        UserPreferences up = model.getUserPreferences();

        up.setReadingListUpdatePeriod(getLong(prefs,
            UserPreferences.PROP_READING_LIST_UPDATE_PERIOD, up.getReadingListUpdatePeriod()));
        up.setOnReadingListUpdateActions(getInt(prefs,
            UserPreferences.PROP_ON_READING_LIST_UPDATE_ACTIONS,
            up.getOnReadingListUpdateActions()));
        up.setUpdateFeeds(getBoolean(prefs, UserPreferences.PROP_UPDATE_FEEDS, up.isUpdateFeeds()));
        up.setUpdateReadingLists(getBoolean(prefs,
            UserPreferences.PROP_UPDATE_READING_LISTS, up.isUpdateReadingLists()));
    }

    /**
     * Loads advanced preferences from the map.
     *
     * @param prefs preferences.
     */
    private void loadAdvancedPreferences(Map prefs)
    {
        UserPreferences up = model.getUserPreferences();
        StarzPreferences sp = model.getStarzPreferences();

        up.setFeedSelectionDelay(getInt(prefs,
            UserPreferences.PROP_FEED_SELECTION_DELAY, up.getFeedSelectionDelay()));
        up.setAntiAliasText(getBoolean(prefs,
            UserPreferences.PROP_AA_TEXT, up.isAntiAliasText()));

        sp.setTopActivity(getInt(prefs,
            StarzPreferences.PROP_TOP_ACTIVITY, sp.getTopActivity()));
        sp.setTopHighlights(getInt(prefs,
            StarzPreferences.PROP_TOP_HIGHLIGHTS, sp.getTopHighlights()));

        up.setShowToolbarLabels(getBoolean(prefs,
            UserPreferences.PROP_SHOW_TOOLBAR_LABELS, up.isShowToolbarLabels()));
        up.setShowUnreadButtonMenu(getBoolean(prefs,
            UserPreferences.PROP_SHOW_UNREAD_BUTTON_MENU, up.isShowUnreadButtonMenu()));
        up.setFeedImportLimit(getInt(prefs,
            UserPreferences.PROP_FEED_IMPORT_LIMIT, up.getFeedImportLimit()));
    }

    /**
     * Returns string value taken from the properties map.
     *
     * @param prefs     preferences map.
     * @param name      name of the property.
     * @param def       default value.
     *
     * @return value.
     */
    public static String getString(Map prefs, String name, String def)
    {
        byte[] bytes = (byte[])prefs.get(name);
        return bytes == null ? def : StringUtils.fromUTF8(bytes);
    }

    /**
     * Reads the value of color for a given feed class into the feed display mode manager.
     *
     * @param prefs     preferences map.
     * @param feedClass feed class.
     */
    private static void getFilterColor(Map prefs, int feedClass)
    {
        String str = getString(prefs, "cdmm." + feedClass, null);
        if (str != null)
        {
            Color color = null;
            if (!StringUtils.isEmpty(str)) color = Color.decode(str);
            FeedDisplayModeManager.getInstance().setColor(feedClass, color);
        }
    }

    /**
     * Returns boolean value taken from the properties map.
     *
     * @param prefs     preferences map.
     * @param name      name of the property.
     * @param def       default value.
     *
     * @return value.
     */
    public static boolean getBoolean(Map prefs, String name, boolean def)
    {
        String obj = getString(prefs, name, null);
        return obj == null ? def : "true".equals(obj);
    }

    /**
     * Returns int value taken from the properties map.
     *
     * @param prefs     preferences map.
     * @param name      name of the property.
     * @param def       default value.
     *
     * @return value.
     */
    public static int getInt(Map prefs, String name, int def)
    {
        String obj = getString(prefs, name, null);
        return obj == null ? def : Integer.parseInt(obj);
    }

    /**
     * Returns long value taken from the properties map.
     *
     * @param prefs     preferences map.
     * @param name      name of the property.
     * @param def       default value.
     *
     * @return value.
     */
    private static long getLong(Map prefs, String name, long def)
    {
        String obj = getString(prefs, name, null);
        return obj == null ? def : Long.parseLong(obj);
    }
}
