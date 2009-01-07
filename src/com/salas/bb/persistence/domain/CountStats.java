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
// $Id: CountStats.java,v 1.1 2007/09/05 12:35:48 spyromus Exp $
//

package com.salas.bb.persistence.domain;

/**
 * Statistics based on two counters.
 */
public class CountStats
{
    /** The number of something since initialization. */
    private long countTotal;
    /** The number of something since the last reset. */
    private long countReset;

    /**
     * Creates the count stats.
     *
     * @param countTotal    total count.
     * @param countReset    count since the reset.
     */
    public CountStats(long countTotal, long countReset)
    {
        this.countTotal = countTotal;
        this.countReset = countReset;
    }

    /**
     * Returns counts since init.
     *
     * @return total counts.
     */
    public long getCountTotal()
    {
        return countTotal;
    }

    /**
     * Returns counts since reset.
     *
     * @return counts since reset.
     */
    public long getCountReset()
    {
        return countReset;
    }
}
