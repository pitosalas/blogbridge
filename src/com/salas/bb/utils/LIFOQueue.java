// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2007 by R. Pito Salas
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
// $Id: LIFOQueue.java,v 1.1 2007/07/18 16:09:04 spyromus Exp $
//

package com.salas.bb.utils;

import EDU.oswego.cs.dl.util.concurrent.Channel;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * LIFO channel based on ultra-fast LinkedList.
 */
public class LIFOQueue extends LinkedList<Object> implements Channel
{
    final Lock lock = new ReentrantLock();
    final Condition nonEmpty = lock.newCondition();

    /**
     * Puts an object into the queue.
     *
     * @param o object.
     */
    public void put(Object o)
    {
        lock.lock();
        try
        {
            add(o);
            nonEmpty.signal();
        } finally
        {
            lock.unlock();
        }
    }

    /**
     * Puts an object into the queue.
     *
     * @param o object.
     * @param l max time to way (not used as the queue is unbounded).
     *
     * @return always <code>TRUE</code>.
     */
    public boolean offer(Object o, long l)
    {
        put(o);
        return true;
    }

    /**
     * Takes the object, but doesn't remove it from the queue.
     *
     * @return object or <code>NULL</code>.
     */
    public Object take()
    {
        Object o;

        lock.lock();
        try
        {
            o = isEmpty() ? null : getLast();
        } finally
        {
            lock.unlock();
        }

        return o;
    }

    /**
     * Takes (and removes) an object from the queue or waits for it given amount
     * of time.
     *
     * @param l time to wait.
     *
     * @return object or <code>NULL</code>.
     *
     * @throws InterruptedException if interrupted during the waiting phase.
     */
    public Object poll(long l) throws InterruptedException
    {
        Object o = null;

        lock.lock();
        try
        {
            if (isEmpty()) nonEmpty.await(l, TimeUnit.MILLISECONDS);
            if (!isEmpty()) o = removeLast();
        } finally
        {
            lock.unlock();
        }
        
        return o;
    }
}
