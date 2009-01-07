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
// $Id: DirectRetriesPolicy.java,v 1.2 2006/01/08 05:00:09 kyank Exp $
//

package com.salas.bb.utils.net;

/**
 * Makes fixed number of retries with constant interval between them.
 */
public class DirectRetriesPolicy implements IRetriesPolicy
{
    private static final int DEFAULT_RETRIES    = 3;
    private static final long DEFAULT_INTERVAL  = 1000;

    private int     retries;
    private long    interval;

    /**
     * Creates the policy with default number of retries (3) and interval (1 sec).
     */
    public DirectRetriesPolicy()
    {
        this(DEFAULT_RETRIES, DEFAULT_INTERVAL);
    }

    /**
     * Creates the policy.
     *
     * @param aRetries  retries number.
     * @param aInterval interval (in ms).
     */
    public DirectRetriesPolicy(int aRetries, long aInterval)
    {
        retries = aRetries;
        interval = aInterval;
    }

    /**
     * Returns period to wait before retrying connecting and reading.
     *
     * @param failure description of a failure.
     *
     * @return time in a future to retry or -1 if the another try should not be taken.
     */
    public long getTimeBeforeRetry(Failure failure)
    {
        int retry = failure.getAttemptNumber();

        return retry < retries ? interval : -1;
    }
}
