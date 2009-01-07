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
// $Id: GroupsSetup.java,v 1.2 2006/01/08 05:12:59 kyank Exp $
//

package com.salas.bb.views.feeds;

import com.salas.bb.utils.TimeRange;

/**
 * Settings for groups. Settings are ready by <code>ArticleList</code> object when it
 * initializes its GUI.
 */
public final class GroupsSetup
{
    /**
     * Hidden utility class constructor.
     */
    private GroupsSetup()
    {
    }

    /**
     * Returns number of groups currently managed.
     *
     * @return number of groups.
     */
    public static int getGroupsCount()
    {
        return TimeRange.TITLES.length;
    }

    /**
     * Returns title of particular group.
     *
     * @param index index of group in list.
     *
     * @return title.
     */
    public static String getGroupTitle(final int index)
    {
        return TimeRange.TITLES[index];
    }

    /**
     * Returns range of times for particular group.
     *
     * @param index index of group in list.
     *
     * @return range of times.
     */
    public static TimeRange getGroupTimeRange(final int index)
    {
        return TimeRange.TIME_RANGES[index];
    }

    /**
     * Returns the list of all available group time ranges.
     *
     * @return list of all available group time ranges.
     */
    public static TimeRange[] getGroupTimeRanges()
    {
        return TimeRange.TIME_RANGES;
    }
}
