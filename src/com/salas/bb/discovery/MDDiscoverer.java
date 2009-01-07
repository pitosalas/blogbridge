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
// $Id: MDDiscoverer.java,v 1.15 2007/04/10 10:33:45 spyromus Exp $
//

package com.salas.bb.discovery;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.FJTaskRunnerGroup;
import com.salas.bb.domain.FeedMetaDataHolder;
import com.salas.bb.utils.ConnectionState;
import com.salas.bb.utils.concurrency.ExecutorFactory;
import com.salas.bb.utils.i18n.Strings;

import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Meta-data discoverer. Uses external services and own local means to discover
 * information about given URL's.
 */
class MDDiscoverer
{
    private static final Logger LOG = Logger.getLogger(MDDiscoverer.class.getName());

    // The name of discoverer threads group
    private static final String THREAD_NAME = "Discoverer";

    // Maximum number of threads to spawn
    private static final int MAX_THREADS = 5;

    // Time to wait before rescheduling task
    private static final int RESCHEDULE_DELAY_MS = 15000;

    // Time to keep discovery threads in pool active
    private static final int THREAD_KEEP_ALIVE_TIME_MS = 60000;

    // The list of protocols allowed to discovery
    private static final List ALLOWED_PROTOCOLS;

    private final FJTaskRunnerGroup eventsRunner;
    private final Executor          executor;
    private final Set<String>       schedule;
    private final Timer             rescheduleTimer;
    private final List<IDiscoveryListener> listeners;
    private final ConnectionState   connectionState;

    static
    {
        ALLOWED_PROTOCOLS = Arrays.asList("http", "file", "https", "ftp");
    }

    /**
     * Creates discoverer.
     *
     * @param aConnectionState connection state interface.
     */
    public MDDiscoverer(ConnectionState aConnectionState)
    {
        this(ExecutorFactory.createPooledExecutor(THREAD_NAME,
            MAX_THREADS, THREAD_KEEP_ALIVE_TIME_MS), aConnectionState);
    }

    /**
     * Creates discoverer with given executor.
     *
     * @param anExecutor executor.
     * @param aConnectionState connection state interface.
     */
    MDDiscoverer(Executor anExecutor, ConnectionState aConnectionState)
    {
        connectionState = aConnectionState;
        eventsRunner = new FJTaskRunnerGroup(1);
        executor = anExecutor;
        schedule = new HashSet<String>();
        rescheduleTimer = new Timer(true);
        listeners = new CopyOnWriteArrayList<IDiscoveryListener>();
    }

    /**
     * Scedules discovery of the given URL. Discovered meta-data will be put into
     * specified holder.
     *
     * @param url       URL to discover if <code>NULL</code> then URL will be taken from XML URL of
     *                  holder.
     * @param holder    holder of resulting meta-data.
     *
     * @throws NullPointerException if holder isn't specified or URL is not specified.
     */
    public void scheduleDiscovery(URL url, FeedMetaDataHolder holder)
    {
        if (holder == null) throw new NullPointerException(Strings.error("unspecified.holder"));
        if (url == null) url = holder.getXmlURL();
        if (url == null) throw new NullPointerException(Strings.error("unspecified.url"));

        String protocol = url.getProtocol().toLowerCase();
        if (!ALLOWED_PROTOCOLS.contains(protocol)) return;

        if (LOG.isLoggable(Level.FINE)) LOG.fine("Schedule discovery: " + url);

        boolean schedulingRequired = false;
        String urlString = url.toString();

        synchronized (schedule)
        {
            if (!schedule.contains(urlString))
            {
                schedule.add(urlString);
                schedulingRequired = true;
            }
        }

        if (schedulingRequired)
        {
            MDDiscoveryRequest request = new MDDiscoveryRequest(url, holder);
            schedule(request);
        }
    }

    /**
     * Schedules discovery request.
     *
     * @param aRequest request.
     */
    private void schedule(MDDiscoveryRequest aRequest)
    {
        try
        {
            executor.execute(new DiscoverTask(aRequest));
        } catch (InterruptedException e)
        {
            LOG.log(Level.WARNING, Strings.error("interrupted"), e);
        }
    }

    /**
     * Reschedules discovery after some delay defined at {@link #RESCHEDULE_DELAY_MS}.
     * The call isn't blocking. It puts the timer task and continues.
     *
     * @param request request to reschedule.
     */
    private void rescheduleDiscovery(MDDiscoveryRequest request)
    {
        if (LOG.isLoggable(Level.FINE)) LOG.fine("Discovery rescheduled: " + request.getUrl());

        request.addAttemptCount();
        rescheduleTimer.schedule(new ScheduleRequestTimerTask(request), RESCHEDULE_DELAY_MS);
    }

    /**
     * Marking URL as no longer scheduled.
     *
     * @param url URL to mark.
     */
    private void markAsNotScheduled(URL url)
    {
        synchronized (schedule)
        {
            schedule.remove(url.toString());
        }
    }

    /**
     * Invoked when some discovery starts.
     *
     * @param request   discovery request.
     */
    private void discoveryStarted(MDDiscoveryRequest request)
    {
        URL url = request.getUrl();

        if (LOG.isLoggable(Level.FINE)) LOG.fine("Discovery started: " + url);

        if (request.getAttempts() == 0) fireDiscoveryStarted(url);
    }

    /**
     * Invoked when some discovery successfully finishes.
     *
     * @param request   discovery request.
     */
    private void discoveryFinished(MDDiscoveryRequest request)
    {
        FeedMetaDataHolder holder = request.getHolder();

        boolean reschedule = !holder.isComplete() ||
            (!request.isServiceDiscoveryComplete() && !request.isLocal());

        if (holder.isComplete())
        {
            URL url = request.getUrl();

            if (LOG.isLoggable(Level.FINE)) LOG.fine("Discovery finished: " + url);

            if (!reschedule) markAsNotScheduled(url);
        }

        fireDiscoveryFinished(request.getUrl(), holder.isComplete());

        if (reschedule) rescheduleDiscovery(request);
    }

    /**
     * Invoked when discovery fails for some reason.
     *
     * @param request   discovery request.
     * @param cause     cause of the error.
     */
    private void discoveryFailed(MDDiscoveryRequest request, Exception cause)
    {
        URL url = request.getUrl();

        LOG.log(Level.WARNING, MessageFormat.format(
            Strings.error("discovery.failed.0"), url), cause);

        markAsNotScheduled(url);
        fireDiscoveryFailed(url);
    }

    /**
     * Adds discovery listener.
     *
     * @param aListener listener.
     */
    public void addListener(IDiscoveryListener aListener)
    {
        if (!listeners.contains(aListener)) listeners.add(aListener);
    }

    /**
     * Fires event about started discovery.
     *
     * @param url URL.
     */
    private void fireDiscoveryStarted(URL url)
    {
        FireEventTask task = new FireEventTask(listeners, url, false)
        {
            protected void fireEvent(IDiscoveryListener listener, URL url, boolean complete)
            {
                listener.discoveryStarted(url);
            }
        };

        runFireEventTask(task);
    }

    /**
     * Fires event about finished discovery.
     *
     * @param url URL.
     * @param complete <code>TRUE</code> when discovery is complete.
     */
    private void fireDiscoveryFinished(URL url, boolean complete)
    {
        FireEventTask task = new FireEventTask(listeners, url, complete)
        {
            protected void fireEvent(IDiscoveryListener listener, URL url, boolean complete)
            {
                listener.discoveryFinished(url, complete);
            }
        };

        runFireEventTask(task);
    }

    /**
     * Fires event about failed discovery.
     *
     * @param url URL.
     */
    private void fireDiscoveryFailed(URL url)
    {
        FireEventTask task = new FireEventTask(listeners, url, false)
        {
            protected void fireEvent(IDiscoveryListener listener, URL url, boolean complete)
            {
                listener.discoveryFailed(url);
            }
        };

        runFireEventTask(task);
    }

    /**
     * Calls fire event task in events thread or runs in directly.
     *
     * @param aTask task.
     */
    private void runFireEventTask(FireEventTask aTask)
    {
        try
        {
            eventsRunner.execute(aTask);
        } catch (InterruptedException e)
        {
            aTask.run();
        }
    }

    /**
     * Task to deal with discovery.
     */
    private class DiscoverTask implements Runnable
    {
        private MDDiscoveryRequest  request;

        /**
         * Creates task to discover given URL and put information into given holder.
         *
         * @param aRequest  discovery request.
         */
        public DiscoverTask(MDDiscoveryRequest aRequest)
        {
            request = aRequest;
        }

        /**
         * Runs discovery task.
         */
        public void run()
        {
            try
            {
                discoveryStarted(request);
                MDDiscoveryLogic.processDiscovery(request, connectionState);
                discoveryFinished(request);
            } catch (Exception e)
            {
                discoveryFailed(request, e);
            }
        }
    }

    /**
     * Task to schedule discovery request.
     */
    private class ScheduleRequestTimerTask extends TimerTask
    {
        private final MDDiscoveryRequest request;

        /**
         * Creates task.
         *
         * @param aRequest request to schedule.
         */
        public ScheduleRequestTimerTask(MDDiscoveryRequest aRequest)
        {
            request = aRequest;
        }

        /**
         * Invoked on execution.
         */
        public void run()
        {
            schedule(request);
        }
    }

    private abstract static class FireEventTask implements Runnable
    {
        private final List<IDiscoveryListener> listeners;
        private final URL       url;
        private final boolean   complete;

        public FireEventTask(List<IDiscoveryListener> aListeners, URL aUrl, boolean aComplete)
        {
            complete = aComplete;
            listeners = aListeners;
            url = aUrl;
        }

        public void run()
        {
            for (IDiscoveryListener listener : listeners) fireEvent(listener, url, complete);
        }

        protected abstract void fireEvent(IDiscoveryListener listener, URL url, boolean complete);
    }
}
