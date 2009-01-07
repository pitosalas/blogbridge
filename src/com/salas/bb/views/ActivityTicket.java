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
// $Id: ActivityTicket.java,v 1.5 2006/03/22 08:12:28 spyromus Exp $
//

package com.salas.bb.views;

/**
 * Simple marker of named task.
 */
public class ActivityTicket
{
    static final int TYPE_NETWORK = 0;
    static final int TYPE_DISK = 1;

    private String title;
    private int type;

    /**
     * Creates activity ticket of a given type.
     *
     * @param aType     type of the activity.
     * @param aTitle    title of the activity.
     */
    ActivityTicket(int aType, String aTitle)
    {
        type = aType;
        title = aTitle;
    }

    /**
     * Returns type of the activity.
     *
     * @return type.
     *
     * @see #TYPE_DISK
     * @see #TYPE_NETWORK
     */
    public int getType()
    {
        return type;
    }

    /**
     * Returns text ready to display as description.
     *
     * @return text to display.
     */
    public String getDisplayInfo()
    {
        return title;
    }
}
