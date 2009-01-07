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
// $Id: ThreadExecutor.java,v 1.1 2007/03/22 12:09:33 spyromus Exp $
//

package com.salas.bb.utils.concurrency;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Single-thread executor, keeping the thread alive for a given period.
 */
public class ThreadExecutor implements Executor
{
    private final String name;
    private final long keepAliveTime;

    private Thread thread;
    private Runnable loopTask;
    private BlockingQueue<Runnable> tasks;

    /**
     * Creates the executor.
     *
     * @param name          name of the thread.
     * @param keepAliveTime keep alive time.
     */
    public ThreadExecutor(String name, long keepAliveTime)
    {
        this.name = name;
        this.keepAliveTime = keepAliveTime;
        this.tasks = new LinkedBlockingQueue<Runnable>();
    }

    /**
     * Executes the command. Blocks until the queue has vacant spaces.
     *
     * @param command command.
     */
    public void execute(Runnable command)
    {
        try
        {
            tasks.put(command);
            startThreadIfNeccessary();
        } catch (InterruptedException e)
        {
            // Fall through
        }
    }

    /** Makes sure the thread is started. */
    private synchronized void startThreadIfNeccessary()
    {
        if (thread == null)
        {
            if (loopTask == null) loopTask = new LoopTask();

            thread = new Thread(loopTask, name);
            thread.setDaemon(true);
            thread.start();
        }
    }

    /** Clears the thread. */
    private synchronized void clearThread()
    {
        thread = null;
    }

    /**
     * Main pick-and-run loop. Being executed by the thread. If the queue
     * is empty for a keepAliveTime milliseconds, exits terminating
     * the thread.
     */
    private class LoopTask implements Runnable
    {
        public void run()
        {
            try
            {
                for (;;)
                {
                    Runnable task = tasks.poll(keepAliveTime, TimeUnit.MILLISECONDS);
                    if (task == null) break;

                    task.run();
                }
            } catch (InterruptedException e)
            {
                // Fall through
            } finally
            {
                clearThread();
            }
        }
    }
}
