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
// $Id: ExecutorFactory.java,v 1.6 2008/02/15 09:08:44 spyromus Exp $
//

package com.salas.bb.utils.concurrency;

import EDU.oswego.cs.dl.util.concurrent.*;

/**
 * Factory for different excutors.
 */
public final class ExecutorFactory
{
    /** Polled executor blocked request processing policy. */
    public enum BlockedPolicy { RUN, WAIT, DISCARD_OLDEST, DISCARD }

    /** Hidden constructor of utility class. */
    private ExecutorFactory()
    {
    }

    /**
     * Creates pooled executor which is capable of spawning 'threads' number of threads max.
     * Each thread will live <code>keepAliveTime</code> milliseconds without tasks before
     * termination.
     *
     * @param name          name of all new threads.
     * @param threads       maximum number of threads.
     * @param keepAliveTime time to live idle before termination.
     *
     * @return executor.
     */
    public static Executor createPooledExecutor(String name, int threads, long keepAliveTime)
    {
        return createPooledExecutor(name, threads, Thread.NORM_PRIORITY, keepAliveTime);
    }

    /**
     * Creates pooled executor which is capable of spawning 'threads' number of threads max.
     * Each thread will live <code>keepAliveTime</code> milliseconds without tasks before
     * termination.
     *
     * @param name          name of all new threads.
     * @param threads       maximum number of threads.
     * @param priority      thread priority.
     * @param keepAliveTime time to live idle before termination.
     *
     * @return executor.
     */
    public static Executor createPooledExecutor(String name, int threads, int priority, long keepAliveTime)
    {
        return createPooledExecutor(new NamingThreadFactory(name, priority), threads, keepAliveTime);
    }

    /**
     * Creates pooled executor which is capable of spawning 'threads' number of threads max.
     * Each thread will live <code>keepAliveTime</code> milliseconds without tasks before
     * termination.
     *
     * @param threadFactory thread factory to use for new threads creation.
     * @param threads       maximum number of threads.
     * @param keepAliveTime time to live idle before termination.
     *
     * @return executor.
     */
    public static Executor createPooledExecutor(ThreadFactory threadFactory, int threads, long keepAliveTime)
    {
        return createPooledExecutor(threadFactory, threads, keepAliveTime, new LinkedQueue());
    }

    /**
     * Creates pooled executor which is capable of spawning 'threads' number of threads max.
     * Each thread will live <code>keepAliveTime</code> milliseconds without tasks before
     * termination.
     *
     * @param threadFactory thread factory to use for new threads creation.
     * @param threads       maximum number of threads.
     * @param keepAliveTime time to live idle before termination.
     * @param queue         queue to use for scheduling.
     *
     * @return executor.
     */
    public static Executor createPooledExecutor(ThreadFactory threadFactory, int threads, long keepAliveTime,
                                                Channel queue)
    {
        return createPooledExecutor(threadFactory, threads, keepAliveTime, queue, BlockedPolicy.WAIT);
    }

    /**
     * Creates pooled executor which is capable of spawning 'threads' number of threads max.
     * Each thread will live <code>keepAliveTime</code> milliseconds without tasks before
     * termination.
     *
     * @param threadFactory thread factory to use for new threads creation.
     * @param threads       maximum number of threads.
     * @param keepAliveTime time to live idle before termination.
     * @param queue         queue to use for scheduling.
     * @param blockedPolicy policy of processing blocked requests (when all threads are busy
     *                      and there's no room in the queue).
     *
     * @return executor.
     */
    public static Executor createPooledExecutor(ThreadFactory threadFactory, int threads, long keepAliveTime,
                                                Channel queue, BlockedPolicy blockedPolicy)
    {
        PooledExecutor pooledExecutor = new PooledExecutor(queue, threads);
        pooledExecutor.setThreadFactory(threadFactory);
        pooledExecutor.setMinimumPoolSize(threads);
        pooledExecutor.setKeepAliveTime(keepAliveTime);

        if (blockedPolicy == BlockedPolicy.RUN) pooledExecutor.runWhenBlocked();
        if (blockedPolicy == BlockedPolicy.WAIT) pooledExecutor.waitWhenBlocked();
        if (blockedPolicy == BlockedPolicy.DISCARD) pooledExecutor.discardWhenBlocked();
        if (blockedPolicy == BlockedPolicy.DISCARD_OLDEST) pooledExecutor.discardOldestWhenBlocked();

        return pooledExecutor;
    }
}
