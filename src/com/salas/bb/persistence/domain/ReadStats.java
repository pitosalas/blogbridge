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
// $Id: ReadStats.java,v 1.2 2007/09/20 13:21:04 spyromus Exp $
//

package com.salas.bb.persistence.domain;

/**
 * Entity (guide / feed) read statistics holder.
 */
public class ReadStats
{
    /** ID of an object. */
    private final long objectId;
    /** Title of an object. */
    private final String objectTitle;
    /** List of reading counts. */
    private final int[] counts;
    /** List of reading times. */
    private final long[] times;
    /** Total sum of counts. */
    private final int total;

    /**
     * Creates a guide stats object.
     *
     * @param objectId      object id.
     * @param objectTitle   object title.
     * @param counts        array of counts corresponding to times.
     * @param times         array of times corresponding to counts.
     */
    public ReadStats(long objectId, String objectTitle, int[] counts, long[] times)
    {
        this.objectId = objectId;
        this.objectTitle = objectTitle;
        this.counts = counts;
        this.times = times;

        // Count total
        int t = 0;
        for (int count : counts) t += count;
        total = t;
    }

    /**
     * Returns assigned object ID.
     *
     * @return ID.
     */
    public long getObjectId()
    {
        return objectId;
    }

    /**
     * Returns the title of the object.
     *
     * @return title.
     */
    public String getObjectTitle()
    {
        return objectTitle;
    }

    /**
     * Returns the list of counts.
     *
     * @return counts.
     */
    public int[] getCounts()
    {
        return counts;
    }

    /**
     * Returns the list of times.
     *
     * @return times.
     */
    public long[] getTimes()
    {
        return times;
    }

    /**
     * Returns the sum of all counts.
     *
     * @return counts total.
     */
    public int getTotal()
    {
        return total;
    }
}