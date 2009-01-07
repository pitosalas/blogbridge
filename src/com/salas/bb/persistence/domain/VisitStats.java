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
// $Id: VisitStats.java,v 1.3 2007/09/17 12:14:11 spyromus Exp $
//

package com.salas.bb.persistence.domain;

/**
 * Entity (guide / feed) visit statistics holder.
 */
public class VisitStats
{
    /** ID of an object. */
    private int objectId;
    /** Title of an object. */
    private String objectTitle;
    /** Total number of visits since initTime. */
    private long countTotal;
    /** Total number of visits since resetTime. */
    private long countReset;
    /** Time of initialization. Never changes. */
    private long initTime;
    /** Time of the last reset. */
    private long resetTime;

    /**
     * Creates a guide stats object.
     *
     * @param objectId      object id.
     * @param objectTitle   object title.
     * @param countTotal    total count.
     * @param countReset    count since the reset.
     * @param initTime      time of init.
     * @param resetTime     time of reset.
     */
    public VisitStats(int objectId, String objectTitle, long countTotal, long countReset,
                           long initTime, long resetTime)
    {
        this.objectId = objectId;
        this.objectTitle = objectTitle;
        this.countTotal = countTotal;
        this.countReset = countReset;
        this.initTime = initTime;
        this.resetTime = resetTime;
    }

    /**
     * Returns total number of visits.
     *
     * @return total visits.
     */
    public long getCountTotal()
    {
        return countTotal;
    }

    /**
     * Returns the number of visits since the reset.
     *
     * @return visits.
     */
    public long getCountReset()
    {
        return countReset;
    }

    /**
     * Returns the time of initialization.
     *
     * @return init time.
     */
    public long getInitTime()
    {
        return initTime;
    }

    /**
     * Returns the time of the last reset.
     *
     * @return reset time.
     */
    public long getResetTime()
    {
        return resetTime;
    }

    /**
     * Returns assigned object ID.
     *
     * @return ID.
     */
    public int getObjectId()
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
}
