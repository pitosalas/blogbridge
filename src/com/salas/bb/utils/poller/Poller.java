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
// $Id: Poller.java,v 1.52 2008/02/15 09:08:44 spyromus Exp $
//
package com.salas.bb.utils.poller;

import EDU.oswego.cs.dl.util.concurrent.BoundedPriorityQueue;
import EDU.oswego.cs.dl.util.concurrent.Executor;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.*;
import com.salas.bb.utils.ConnectionState;
import com.salas.bb.utils.concurrency.ExecutorFactory;
import com.salas.bb.utils.concurrency.NamingThreadFactory;
import com.salas.bb.utils.concurrency.SimpleLock;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.net.auth.AuthCancelException;
import com.salas.bb.utils.opml.Helper;
import com.salas.bb.utils.opml.ImporterAdv;
import com.salas.bbutilities.opml.ImporterException;
import com.salas.bbutilities.opml.objects.DefaultOPMLFeed;
import com.salas.bbutilities.opml.objects.DirectOPMLFeed;
import com.salas.bbutilities.opml.objects.OPMLGuide;
import com.salas.bbutilities.opml.objects.QueryOPMLFeed;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Poller takes care of periodical feeds updates. It makes regular scans
 * of the feeds lists attempting to find the feeds requiring to be updated.
 * The period of these scans is defined by <code>scanPeriod</code> property.</p>
 *
 * <p>Poller is always dedicated to some guides set and works only with it.
 * This decision was made to simplify scanning of feeds and does not limit
 * the functionality as the application always have only one active guide set.</p>
 *
 * <p>This class has convenient methods to force updates of the feeds out of
 * the schedule. They are:</p>
 * <ul>
 *  <li><code>update()</code> - updates all feeds (indirectly) in all guides.</li>
 *  <li><code>update(IGuide)</code> - updates all feeds (indirectly) in given guide.</li>
 *  <li><code>update(IFeed)</code> - updates the feed. The feed can be updated directly
 *      (meaning that the updating of this particular feed is required) or indirectly
 *      (meaning that the updating of the feed happens as a part of bigger update:
 *      guide or whole set).
 * </ul>
 *
 * <p>Before actually doing the update Poller asks each of the feeds whether they
 * can be updated or not. The feed decides, taking in account the fact of
 * direct or indirect call, last poll date and other factors, if it would like to be
 * updated. If it would like to then the Poller puts the feed in queue for updates.</p>
 *
 * <p>Poller owns some number of worker threads which are waiting for the tasks
 * in the queue. Once they grab the task from the queue, they follow update procedure.
 * After they finish they move back to the fetching of the next task.</p>
 */
public final class Poller implements Runnable
{
    private static final Logger LOG = Logger.getLogger(Poller.class.getName());

    /** Maximum time to live for worker thread in a pool (ms). */
    private static final int THREAD_KEEP_ALIVE_TIME = 15000;

    /** Number of worker threads. */
    private static final int WORKERS = 5;
    /** Polling queue size. */
    private static final int QUEUE_SIZE = 5000;

    /** Guides set which is under scan. */
    private GuidesSet guidesSet;

    /**
     * This lock is used to controll access to the feed update method to
     * avoid concurrency issues.
     */
    private final SimpleLock feedUpdateLock;

    /** Polling tasks executor. */
    private final Executor   executor;

    /** Connection state interface. */
    private final ConnectionState connectionState;

    /** <code>TRUE</code> when updates skipped due to being offline. */
    private boolean skippedWhenOffline;

    /** When <code>TRUE</code> feeds are allowed to be updated with manual commands. */
    private boolean updateFeedsManually;
    /** When <code>TRUE</code> reading lists are allowed to be updated with manual commands. */
    private boolean updateReadingListsManually;
    private boolean noFeedPolling;

    /**
     * Creates poller.
     *
     * @param aConnectionState  connection state interface.
     */
    public Poller(ConnectionState aConnectionState)
    {
        updateFeedsManually = true;
        updateReadingListsManually = true;

        connectionState = aConnectionState;
        connectionState.addPropertyChangeListener(ConnectionState.PROP_ONLINE,
            new ConnectionStateListener());

        feedUpdateLock = new SimpleLock();

        skippedWhenOffline = false;

        int threads = WORKERS;
        Integer cntProperty = Integer.getInteger("poller.workers");
        if (cntProperty != null) threads = cntProperty;
        noFeedPolling = System.getProperty("poller.noFeedPolling") != null;

        if (LOG.isLoggable(Level.CONFIG)) LOG.config("Number of worker threads: " + threads);

        // Create a pool of executors with minimum thread priority
        executor = ExecutorFactory.createPooledExecutor(
            new NamingThreadFactory("Poller", Thread.MIN_PRIORITY),
            threads, THREAD_KEEP_ALIVE_TIME,
            new BoundedPriorityQueue(QUEUE_SIZE, new PollerTaskPrioritizer()),
            ExecutorFactory.BlockedPolicy.DISCARD);

        // Note: The policy is "Discard" a requst if the queue is full
        // (will be retried in 10 seconds)
    }

    /**
     * Enables / disables updating feeds with manual commands.
     *
     * @param update <code>TRUE</code> to allow.
     */
    public void setUpdateFeedsManually(boolean update)
    {
        updateFeedsManually = update;
    }

    /**
     * Enables / disables updating reading lists with manual commands.
     *
     * @param update <code>TRUE</code> to allow.
     */
    public void setUpdateReadingListsManually(boolean update)
    {
        updateReadingListsManually = update;
    }

    /**
     * Sets the guides set to scan for updates.
     *
     * @param set guides set.
     */
    public void setGuidesSet(GuidesSet set)
    {
        guidesSet = set;
    }

    /**
     * Orders to stars full scan of the set immediately.
     */
    public void update()
    {
        update(true);
    }

    /**
     * Orders to stars full scan of the set immediately.
     *
     * @param manual TRUE if update was requested manually.
     */
    private void update(boolean manual)
    {
        if (guidesSet == null) return;

        StandardGuide[] guides = guidesSet.getStandardGuides(null);
        for (StandardGuide guide : guides) update(guide, manual);
    }

    /**
     * Orders to scan the specified guide.
     *
     * @param guide guide to scan for updates.
     *
     * @throws NullPointerException if guide isn't specified.
     */
    public void update(IGuide guide)
    {
        update(guide, true);
    }

    /**
     * Orders to scan the specified guide.
     *
     * @param guide guide to scan for updates.
     * @param manual TRUE if update was requested manually.
     *
     * @throws NullPointerException if guide isn't specified.
     */
    private void update(IGuide guide, boolean manual)
    {
        if (guide == null) throw new NullPointerException(Strings.error("unspecified.guide"));

        if (!manual || updateFeedsManually)
        {
            IFeed[] feeds = guide.getFeeds();
            for (IFeed feed : feeds)
            {
                if (feed instanceof DataFeed) update((DataFeed)feed, manual);
            }
        }

        // Update reading lists
        if (guide instanceof StandardGuide && (!manual || updateReadingListsManually))
        {
            ReadingList[] readingLists = ((StandardGuide)guide).getReadingLists();
            for (ReadingList list : readingLists) update(list, manual);
        }
    }

    /**
     * Orders to perform update of the selected feed.
     *
     * @param feed    feed to update.
     * @param manual  <code>TRUE</code> if it's manual update request.
     *
     * @throws NullPointerException if feed isn't specified.
     */
    public void update(DataFeed feed, boolean manual)
    {
        update(feed, manual, false);
    }

    /**
     * Orders to perform update of the selected feed.
     *
     * @param feed    feed to update.
     * @param manual  <code>TRUE</code> if it's manual update request.
     * @param allowInvisible <code>TRUE</code> if invisible feed is allowed for update.
     *
     * @throws NullPointerException if feed isn't specified.
     */
    public void update(DataFeed feed, boolean manual, boolean allowInvisible)
    {
        if (noFeedPolling) return;
        if (feed == null) throw new NullPointerException(Strings.error("unspecified.feed"));

        feedUpdateLock.lock();
        try
        {
            // If feed wishes to be updated schedule the update
            if (feed.isUpdatable(manual, allowInvisible))
            {
                // Starting processing (will finish in PollerTask.finishPolling())
                feed.processingStarted();

                PollerTask pollerTask = new PollerTask(feed);

                scheduleTask(pollerTask);
            }
        } finally
        {
            feedUpdateLock.unlock();
        }
    }

    /**
     * Checks if update of a reading list is required and carries on
     * with update if it is.
     *
     * @param list      list to check and update.
     * @param manual    <code>TRUE</code> if user requested immediate update.
     */
    private void update(ReadingList list, boolean manual)
    {
        if ((manual || list.isUpdatable()) && !list.isUpdating())
        {
            list.setUpdating(true);
            scheduleTask(new ReadingListUpdatePollerTask(list));
        }
    }

    /**
     * Schedule or execute task immediately.
     *
     * @param aPollerTask task.
     */
    private void scheduleTask(Runnable aPollerTask)
    {
        try
        {
            executor.execute(aPollerTask);
        } catch (InterruptedException e)
        {
            LOG.severe(Strings.error("interrupted"));
            aPollerTask.run();
        }
    }

    /**
     * Simple initiation of the scan.
     */
    public void run()
    {
        if (connectionState.isOnline())
        {
            update(false);
        } else
        {
            skippedWhenOffline = true;
        }
    }

    /**
     * Listens for connection to go online.
     */
    private class ConnectionStateListener implements PropertyChangeListener
    {
        /**
         * Called when connection state changes.
         *
         * @param evt property change event.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (connectionState.isOnline() && skippedWhenOffline) run();
        }
    }

    /**
     * The task for updating reading list.
     */
    private static class ReadingListUpdatePollerTask implements Runnable
    {
        private final ReadingList list;

        /**
         * Creates task for updating reading list.
         *
         * @param aList list to update.
         */
        public ReadingListUpdatePollerTask(ReadingList aList)
        {
            list = aList;
        }

        /**
         * Invoked when it's time to run updates.
         */
        public void run()
        {
            boolean setPollTime = true;
            try
            {
                final OPMLGuide newGuide = fetchGuide();

                if (newGuide != null)
                {
                    updateListInfo(newGuide);
                    updateFeedsList(newGuide);
                }

                list.setMissing(false);
            } catch (FileNotFoundException e)
            {
                list.setMissing(true);
                setPollTime = true;
            } catch (Throwable e)
            {
                setPollTime = false;
                if (!(e.getCause() instanceof AuthCancelException))
                {
                    LOG.log(Level.SEVERE, Strings.error("unhandled.exception"), e);
                }
            } finally
            {
                list.setUpdating(false);
            }

            if (setPollTime) list.setLastPollTime(System.currentTimeMillis());
        }

        /**
         * Fetches new version of guide for the given list.
         *
         * @return new version of guide.
         *
         * @throws FileNotFoundException when reading list is no loger there.
         */
        private OPMLGuide fetchGuide()
            throws FileNotFoundException
        {
            OPMLGuide[] opmlGuide;

            try
            {
                opmlGuide = new ImporterAdv().process(list.getURL(), true).getGuides();
            } catch (ImporterException e)
            {
                if (e.getCause() instanceof FileNotFoundException)
                {
                    throw (FileNotFoundException)e.getCause();
                }

                LOG.log(Level.WARNING, MessageFormat.format(
                    Strings.error("failed.to.fetch.the.reading.list.list.0"),
                    list.getURL()), e);
                opmlGuide = null;
            }

            return opmlGuide == null ? null
                : opmlGuide.length == 0
                    ? new OPMLGuide("", "", false, null, null, false, 0, false, false)
                    : opmlGuide[0];
        }

        /**
         * Updates reading list information.
         *
         * @param aGuide OPML guide parsed from the source.
         */
        private void updateListInfo(OPMLGuide aGuide)
        {
            list.setTitle(aGuide.getTitle());
        }

        /**
         * Updates list of associated feeds.
         *
         * @param aGuide OPML guide parsed from the source.
         *
         * @throws InterruptedException         when interrupted.
         * @throws InvocationTargetException    when invocation failed.
         */
        private void updateFeedsList(OPMLGuide aGuide)
            throws InvocationTargetException, InterruptedException
        {
            URL baseURL = list.getURL();
            List feeds = aGuide.getFeeds();
            Set<String> urls = new HashSet<String>();

            GlobalModel model = GlobalController.SINGLETON.getModel();
            int limit = model.getUserPreferences().getFeedImportLimit();

            // Collect all XML URL's of all direct feeds
            List<DirectFeed> feedsList = new ArrayList<DirectFeed>(feeds.size());
            for (int i = 0; limit > 0 && i < feeds.size(); i++)
            {
                DefaultOPMLFeed feed = (DefaultOPMLFeed)feeds.get(i);

                // Convert query feed to normal direct feed
                if (feed instanceof QueryOPMLFeed)
                {
                    QueryFeed qFeed = Helper.createQueryFeed((QueryOPMLFeed)feed);
                    feed = new DirectOPMLFeed(feed.getTitle(), qFeed.getXmlURL().toString(), null,
                        feed.getRating(), feed.getReadArticlesKeys(), feed.getPinnedArticlesKeys(), feed.getLimit(),
                        null, null, null, null, null, null, false, qFeed.getType().getType(), false, 0, null);
                }

                if (feed instanceof DirectOPMLFeed)
                {
                    DirectOPMLFeed doFeed = (DirectOPMLFeed)feed;
                    String feedURL = doFeed.getXmlURL();

                    try
                    {
                        URL url = new URL(baseURL, feedURL);
                        if (!urls.contains(url.toString()))
                        {
                            urls.add(url.toString());

                            DirectFeed dFeed = new DirectFeed();
                            dFeed.setXmlURL(url);
                            Helper.populateDirectFeedProperties(baseURL, dFeed, doFeed);

                            feedsList.add(dFeed);
                            limit--;
                        }
                    } catch (MalformedURLException e)
                    {
                        // Ignore malformed URLs from the list
                    }
                }
            }

            DirectFeed[] directFeeds = feedsList.toArray(new DirectFeed[feedsList.size()]);

            final List<DirectFeed> addFeeds = new LinkedList<DirectFeed>();
            final List<DirectFeed> removeFeeds = new LinkedList<DirectFeed>();
            list.collectDifferences(directFeeds, addFeeds, removeFeeds);

            // After this stage we have the list of feeds to add and feeds to remove.
            // When we face the problem of redirected feeds when a feed redirects itself
            // after adding to the list, the existing feed appears in the "to remove" list,
            // and it's an indicator of that we have this situation. 

            // So if there's anything to remove, we need to check if any of feeds we are
            // adding match these
            if (removeFeeds.size() > 0)
            {
                List<DirectFeed> dontAddFeeds = new LinkedList<DirectFeed>();
                List<DirectFeed> dontRemoveFeeds = new LinkedList<DirectFeed>();

                for (DirectFeed addFeed : addFeeds)
                {
                    URL oldURL = addFeed.getXmlURL();

                    try
                    {
                        URL newURL = getRedirectionURL(oldURL, new LinkedList<String>());
                        if (newURL != null && !newURL.toString().equals(oldURL.toString()))
                        {
                            String newURLS = newURL.toString();

                            // See if a feed with a resolved URL is among those for removal
                            // and don't remove it if so.
                            for (DirectFeed removeFeed : removeFeeds)
                            {
                                if (removeFeed.getXmlURL().toString().equals(newURLS))
                                {
                                    dontRemoveFeeds.add(removeFeed);
                                    dontAddFeeds.add(addFeed);
                                    break;
                                }
                            }
                        }
                    } catch (IOException e)
                    {
                        if (GlobalController.getConnectionState().isOnline())
                        {
                            LOG.log(Level.INFO, "Failed to resolve redirection for " + oldURL, e);
                        }
                    }
                }

                // Rebuild an add-list if there's something we don't need to add
                if (dontAddFeeds.size() > 0) addFeeds.removeAll(dontAddFeeds);

                // Rebuild a remove-list if there's something we don't need to remove
                if (dontRemoveFeeds.size() > 0) removeFeeds.removeAll(dontRemoveFeeds);
            }

            if (addFeeds.size() > 0 || removeFeeds.size() > 0)
            {
                // Call updates in EDT
                SwingUtilities.invokeAndWait(new Runnable()
                {
                    public void run()
                    {
                        GlobalController.updateReadingList(list, addFeeds, removeFeeds);
                    }
                });
            }
        }

        /**
         * Checks for the redirection from the given URL to somewhere else and
         * returns the new URL. If it's looped, the NULL is returned.
         *
         * @param url source URL.
         * @param visited the list of visited URL's.
         *
         * @return new URL.
         *
         * @throws IOException in case of network I/O problem.
         */
        private static URL getRedirectionURL(URL url, List<String> visited)
            throws IOException
        {
            if (url == null) return null;
            if (visited == null) visited = new ArrayList<String>(); else
            if (visited.contains(url.toString())) return null;

            URLConnection con = url.openConnection();
            if (con instanceof HttpURLConnection)
            {
                HttpURLConnection hcon = (HttpURLConnection)con;
                String newLocation = getNewPermanentLocation(hcon);

                // If the redirection takes place and it's permanent,
                // follow the new location
                if (newLocation != null)
                {
                    visited.add(url.toString());
                    url = getRedirectionURL(new URL(url, newLocation), visited);
                }
            }

            return url;
        }

        /**
         * Connects using the connection and reads the headers. If there's a permanent
         * redirection instruction, returns new location.
         *
         * @param hcon  connection to use.
         *
         * @return new location if there was the permanent redirection instruction in
         *         the headers.
         *
         * @throws IOException in case of network I/O problem.
         */
        private static String getNewPermanentLocation(HttpURLConnection hcon)
            throws IOException
        {
            String newLocation = null;

            hcon.setInstanceFollowRedirects(false);
            hcon.setAllowUserInteraction(false);
            hcon.connect();
            try
            {
                if (isRedirectionCode(hcon.getResponseCode()))
                {
                    newLocation = hcon.getHeaderField("Location");
                }
            } finally
            {
                hcon.disconnect();
            }

            return newLocation;
        }

        /**
         * Returns <code>TRUE</code> if the code is one of the redirection codes.
         *
         * @param responseCode code.
         *
         * @return <code>TRUE</code> if the code is one of the redirection codes.
         */
        private static boolean isRedirectionCode(int responseCode)
        {
            return responseCode == HttpURLConnection.HTTP_MULT_CHOICE ||
                responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                responseCode == HttpURLConnection.HTTP_SEE_OTHER ||
                responseCode == HttpURLConnection.HTTP_USE_PROXY ||
                responseCode == 307;
        }
    }

    /**
     * Prioritizes tasks so that reading list updates go first.
     */
    private static class PollerTaskPrioritizer implements Comparator
    {
        /**
         * Compares its two arguments for order.  Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.<p>
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         *
         * @return a negative integer, zero, or a positive integer as the
         *         first argument is less than, equal to, or greater than the
         *         second.
         *
         * @throws ClassCastException if the arguments' types prevent them from
         *                            being compared by this comparator.
         */
        public int compare(Object o1, Object o2)
        {
            boolean rl1 = o1 instanceof ReadingListUpdatePollerTask;
            boolean rl2 = o2 instanceof ReadingListUpdatePollerTask;

            return (rl1 && rl2) || (!rl1 && !rl2) ? 0 :
                   (rl1 && !rl2) ? -1 : 1;
        }
    }
}
