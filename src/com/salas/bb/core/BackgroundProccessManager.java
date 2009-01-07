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
// $Id: BackgroundProccessManager.java,v 1.27 2007/03/13 14:41:58 spyromus Exp $
//

package com.salas.bb.core;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Manager of background processes.
 */
public class BackgroundProccessManager
{
    private static final int MILLIS_IN_SEC = 1000;

    private Timer    daemon;

    /**
     * Constructs manager.
     */
    BackgroundProccessManager()
    {
        daemon = new Timer(true);
    }

    /**
     * Start all Background Processes.
     */
    void startAll()
    {
    }

    /**
     * Request a gracefull exit from the background tasks.
     */
    void requestExit()
    {
        if (daemon != null) daemon.cancel();
    }

    /**
     * Schedules a task for a delayed execution.
     *
     * @param task  task.
     * @param delay delay in seconds.
     *
     * @return task.
     */
    public RunnableTask scheduleOnce(Runnable task, long delay)
    {
        RunnableTask tt = new RunnableTask(task);
        daemon.schedule(tt, delay * MILLIS_IN_SEC);
        return tt;
    }

    /**
     * Schedule task.
     *
     * @param aTask     task.
     * @param aPeriod   period in seconds.
     *
     * @return timer task.
     */
    public RunnableTask schedule(Runnable aTask, long aPeriod)
    {
        return schedule(aTask, 1, aPeriod);
    }

    /**
     * Schedule task.
     *
     * @param aTask     task.
     * @param aDelay    delay in seconds.
     * @param aPeriod   period in seconds.
     *
     * @return timer task.
     */
    public RunnableTask schedule(Runnable aTask, long aDelay, long aPeriod)
    {
        RunnableTask timerTask = new RunnableTask(aTask);
        daemon.schedule(timerTask, aDelay * MILLIS_IN_SEC, aPeriod * MILLIS_IN_SEC);
        return timerTask;
    }

    /**
     * Timer task wrapping some runnable.
     */
    private static class RunnableTask extends TimerTask
    {
        private Runnable runnable;

        /**
         * Creates timer task wrapping some runnable.
         *
         * @param aRunnable runnable.
         */
        public RunnableTask(Runnable aRunnable)
        {
            runnable = aRunnable;
        }

        /** The action to be performed by this timer task. */
        public void run()
        {
            runnable.run();
        }
    }
}
