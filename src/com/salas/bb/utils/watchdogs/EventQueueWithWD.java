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
// $Id: EventQueueWithWD.java,v 1.13 2006/05/30 08:25:28 spyromus Exp $
//

package com.salas.bb.utils.watchdogs;

import com.salas.bb.utils.i18n.Strings;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TimerTask;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Alternative events dispatching queue. The benefit over the default Event Dispatch queue is that
 * you can add as many watchdog timers as you need and they will trigger arbitrary actions.
 * <p/>
 * Timers can be of two types:
 * <ul>
 * <li><b>Repetitive </b>- action can be triggered multiple times for the same "lengthy" event.
 * </li>
 * <li><b>Non-repetitive </b>- action can be triggered only once per event.</li>
 * </ul>
 * <p/>
 * Queue records the time of the event dispatching start. This time is used by the timers to
 * check if the event is being dispatched for too long. If so the expired timers trigger associated
 * actions.
 * <p/>
 * In order to use this queue application should call <code>install()</code> method. This method
 * will create, initialize and register the alternative queue as appropriate. It also will return
 * the instance of the queue for further interractions. Here's an example of how it can be done:
 * <p/>
 * <pre>
 * 
 *  EventQueueWithWD queue = EventQueueWithWD.install();
 *  Action edtOverloadReport = ...;
 *
 *  // install single-shot wg to report EDT overload after 10-seconds timeout
 *  queue.addWatchdog(10000, edtOverloadReport, false);
 *  
 * </pre>
 */
public final class EventQueueWithWD extends EventQueue
{
    private static final Logger LOG = Logger.getLogger(EventQueueWithWD.class.getName());

    private static final String MSG_EXCEPTION = Strings.error("failed.to.dispatch.event");

    // Main timer
    private java.util.Timer     timer;

    // Group of informational fields for describing the event
    private Object              eventChangeLock;
    private long                eventDispatchingStart;
    private AWTEvent            event;

    /**
     * Hidden utility constructor.
     */
    private EventQueueWithWD()
    {
        timer = new java.util.Timer(true);

        eventDispatchingStart = -1;
        event = null;
        eventChangeLock = new Object();
    }

    /**
     * Install alternative queue.
     *
     * @return instance of queue installed.
     */
    public static EventQueueWithWD install()
    {
        EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        EventQueueWithWD newEventQueue = new EventQueueWithWD();
        eventQueue.push(newEventQueue);

        return newEventQueue;
    }

    /**
     * Record the event and continue with usual dispatching.
     *
     * @param anEvent event to dispatch.
     */
    protected void dispatchEvent(AWTEvent anEvent)
    {
        setEventDispatchingStart(anEvent, System.currentTimeMillis());

        try
        {
            super.dispatchEvent(anEvent);
        } catch (Throwable e)
        {
            LOG.log(Level.SEVERE, MSG_EXCEPTION, e);
        }

        setEventDispatchingStart(null, -1);
    }

    /**
     * Register event and dispatching start time.
     *
     * @param anEvent   event.
     * @param timestamp dispatching start time.
     */
    private void setEventDispatchingStart(AWTEvent anEvent, long timestamp)
    {
        synchronized (eventChangeLock)
        {
            event = anEvent;
            eventDispatchingStart = timestamp;
        }
    }

    /**
     * Add watchdog timer. Timer will trigger <code>listener</code> if the queue dispatching
     * event longer than specified <code>maxProcessingTime</code>. If the timer is
     * <code>repetitive</code> then it will trigger additional events if the processing 2x, 3x and
     * further longer than <code>maxProcessingTime</code>.
     *
     * @param maxProcessingTime maximum processing time.
     * @param listener          listener for events. The listener will receive <code>AWTEvent</code>
     *                          as source of event.
     * @param repetitive        TRUE to trigger consequent events for 2x, 3x and further periods.
     */
    public void addWatchdog(long maxProcessingTime, ActionListener listener, boolean repetitive)
    {
        Watchdog checker = new Watchdog(maxProcessingTime, listener, repetitive);
        timer.schedule(checker, maxProcessingTime, maxProcessingTime);
    }

    /**
     * Checks if the processing of the event is longer than the specified
     * <code>maxProcessingTime</code>. If so then listener is notified.
     */
    private class Watchdog extends TimerTask
    {
        // Settings
        private long            maxProcessingTime;
        private ActionListener  listener;
        private boolean         repetitive;

        // Event reported as "lengthy" for the last time. Used to prevent
        // repeatitive behaviour in non-repeatitive timers.
        private int             lastReportedEventHashCode;

        /**
         * Creates timer.
         *
         * @param aMaxProcessingTime    maximum event processing time before listener is notified.
         * @param aListener             listener to notify.
         * @param aRepeatitive          TRUE to allow consequent notifications for the same event.
         */
        public Watchdog(long aMaxProcessingTime, ActionListener aListener, boolean aRepeatitive) throws IllegalArgumentException
        {
            if (aListener == null)
                throw new IllegalArgumentException(Strings.error("unspecified.listener"));
            if (aMaxProcessingTime < 0)
                throw new IllegalArgumentException(Strings.error("max.locking.period.should.be.greater.than.zero"));

            maxProcessingTime = aMaxProcessingTime;
            listener = aListener;
            lastReportedEventHashCode = -1;
            repetitive = aRepeatitive;
        }

        public void run()
        {
            long time;
            AWTEvent currentEvent;

            // Get current event requisites
            synchronized (eventChangeLock)
            {
                time = eventDispatchingStart;
                currentEvent = event;
            }

            long currentTime = System.currentTimeMillis();

            // Check if event is being processed longer than it's allowed
            if (time != -1 && (currentTime - time > maxProcessingTime) &&
                (repetitive || System.identityHashCode(currentEvent) != lastReportedEventHashCode))
            {
                listener.actionPerformed(new ActionEvent(currentEvent, -1, null));
                lastReportedEventHashCode = System.identityHashCode(currentEvent);
            }
        }
    }
}
