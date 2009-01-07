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
// $Id: CachingCalculator.java,v 1.15 2007/03/13 22:10:42 spyromus Exp $
//

package com.salas.bb.utils.concurrency;

import com.salas.bb.utils.i18n.Strings;
import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.UnboundedFifoBuffer;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract calculator of values for keys with cache. Calculation and invalidation of values can be
 * performed in multiple threads which can be a great speed-up in some occurances.
 */
public abstract class CachingCalculator
{
    private static final Logger LOG = Logger.getLogger(CachingCalculator.class.getName());

    private int     threadCounter = 1;

    private Map<Object, Holder> keyToHolderMap;
    private Buffer  invalidationQueue;
    private int     workersCount;

    /**
     * Creates cached calculator with specified number of invalidation threads.
     *
     * @param threads threads.
     */
    public CachingCalculator(int threads)
    {
        keyToHolderMap = createKeyToValueMap();
        invalidationQueue = BufferUtils.blockingBuffer(createQueueBuffer());
        workersCount = threads;
    }

    /**
     * Starts all worker threads.
     */
    public void startThreads()
    {
        String name = getThreadsBaseName();
        for (int i = 0; i < workersCount; i++)
        {
            new InvalidatorThread(name, invalidationQueue).start();
        }
    }

    /**
     * Returns base name for all worker-threads. By default it equals to class name.
     *
     * @return base name for threads.
     */
    protected String getThreadsBaseName()
    {
        return CachingCalculator.class.getName();
    }

    /**
     * Create invaliation queue. Queue will be automatically decorated with blocker.
     * By default queue is unbounded.
     *
     * @return queue.
     */
    protected Buffer createQueueBuffer()
    {
        return new UnboundedFifoBuffer(100);
    }

    /**
     * Create map to use to store keys and values.
     * By default the map is <code>IdentityHashMap</code>.
     *
     * @return map.
     */
    protected Map<Object, Holder> createKeyToValueMap()
    {
        return new IdentityHashMap<Object, Holder>();
    }

    /**
     * Returns value for the specified key.
     *
     * @param key key.
     *
     * @return value.
     */
    public Object getValue(Object key)
    {
        Object value;

        Holder holder = getHolderForKey(key);

        holder.lock();
        value = holder.value;
        holder.unlock();

        return value;
    }

    /**
     * Marks key as invalid and schedules immediate recalculation.
     *
     * @param key key to invalidate.
     */
    public synchronized void invalidateKey(Object key)
    {
        invalidateHolder(getHolderForKey(key));
    }

    /**
     * Returns holder of value for the key. If holder isn't in the cache yet, it is created
     * and scheduled for calculation.
     *
     * @param key key to get holder for.
     *
     * @return holder.
     */
    private synchronized Holder getHolderForKey(Object key)
    {
        Holder holder = keyToHolderMap.get(key);
        if (holder == null)
        {
            holder = new Holder(key);
            holder.value = calculate(key);
            keyToHolderMap.put(key, holder);
//            invalidateHolder(holder);
        }
        return holder;
    }

    /**
     * Marks whole cache as invalid and starts background invalidation of all previously
     * calculated keys.
     */
    public synchronized void invalidateAll()
    {
        for (Holder holder : keyToHolderMap.values()) invalidateHolder(holder);
    }

    // Puts the holder in queue for invalidation and locks it.
    private void invalidateHolder(Holder holder)
    {
        if (!invalidationQueue.contains(holder))
        {
            holder.lock();
            invalidationQueue.add(holder);
        }
    }

    /**
     * Called when some key no longer needs to be stored.
     *
     * @param key key to remove.
     */
    public synchronized void removeKey(Object key)
    {
        keyToHolderMap.remove(key);
    }

    /**
     * Calculates value for the given key.
     *
     * @param key key.
     *
     * @return value.
     */
    protected abstract Object calculate(Object key);

    // ---------------------------------------------------------------------------------------------

    /**
     * Holder for channel score.
     */
    public static class Holder extends SimpleLock
    {
        private volatile Object key;
        private volatile Object value;

        /**
         * Constructs holder.
         *
         * @param aKey key.
         */
        public Holder(Object aKey)
        {
            key = aKey;
        }

        /**
         * Returns TRUE only if this object is that too.
         *
         * @param obj object to compare with.
         *
         * @return TRUE only if this object is that too.
         */
        public boolean equals(Object obj)
        {
            return (this == obj);
        }

        /**
         * Returns the hash code of the holder.
         *
         * @return hash code.
         */
        public int hashCode()
        {
            // Inherits the hash code calculation from the Object as it creates the code
            // from memory address which is unique enough.
            return super.hashCode();
        }
    }

    /**
     * Thread, which is busy with recalculating of invalidated values.
     */
    private class InvalidatorThread extends Thread
    {
        private Buffer tasksQueue;

        /**
         * Creates new thread using <b>blocking</b> buffer as queue of tasks.
         *
         * @param tasks queue of tasks.
         */
        public InvalidatorThread(String name, Buffer tasks)
        {
            super(name + " " + threadCounter++);
            setDaemon(true);

            this.tasksQueue = tasks;
        }

        public void run()
        {
            while (true)
            {
                Holder holder = (Holder)tasksQueue.remove();
                try
                {
                    holder.value = calculate(holder.key);
                } catch (Throwable e)
                {
                    LOG.log(Level.SEVERE, Strings.error("unhandled.exception"), e);
                } finally
                {
                    holder.unlock();
                }
            }
        }
    }
}
