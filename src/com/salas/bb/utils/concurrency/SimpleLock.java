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
// $Id: SimpleLock.java,v 1.6 2006/01/08 05:00:08 kyank Exp $
//

package com.salas.bb.utils.concurrency;

/**
 * Simple lock with two states. It's possible to do locking only once.
 */
public class SimpleLock
{
    private boolean  locked;

    /**
     * Creates lock.
     */
    public SimpleLock()
    {
        locked = false;
    }

    /**
     * Makes a try to do locking. If the object is already locked thread will wait for the
     * unlocking. If there are several threads waiting for unlocking the fastest will establish
     * locking when object will get unlocked.
     */
    public synchronized void lock()
    {
        while (locked)
        {
            try
            {
                wait();
            } catch (InterruptedException e)
            {
                // Nothing extraordinary
            }
        }

        locked = true;
    }

    /**
     * Unlocks the object. Note that <b>any</b> thread can unlock the object. This is the feature
     * of this simple lock.
     */
    public synchronized void unlock()
    {
        locked = false;
        notifyAll();
    }

    /**
     * Returns <code>TRUE</code> if this object is locked.
     *
     * @return <code>TRUE</code> if locked.
     */
    public synchronized boolean isLocked()
    {
        return locked;
    }
}
