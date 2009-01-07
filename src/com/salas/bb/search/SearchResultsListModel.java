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
// $Id: SearchResultsListModel.java,v 1.3 2007/07/11 16:31:33 spyromus Exp $
//

package com.salas.bb.search;

import com.salas.bb.utils.TimeRange;
import com.salas.bb.utils.i18n.Strings;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Search dialog special list model.
 */
public class SearchResultsListModel extends ResultsListModel
{
    /** No grouping. */
    public static final int GROUP_FLAT      = 0;
    /** Group by the kind. */
    public static final int GROUP_KIND      = 1;
    /** Group by the date. */
    public static final int GROUP_DATE      = 2;

    /** The mode to group the items by. */
    private int groupBy = GROUP_KIND;

    /** Groups map. */
    private Map<Integer, ResultGroup<ResultItem>> groups = new TreeMap<Integer, ResultGroup<ResultItem>>();

    /**
     * Sets the group by mode.
     *
     * @param groupBy mode.
     */
    public void setGroupBy(int groupBy)
    {
        this.groupBy = groupBy;

        // Clear all groups for regrouping
        groups.clear();
        fireClear();

        // Add all items one by one to form new groups
        for (ResultItem item : items) addExistingItem(item);
    }

    /** Removes all items from the model. The model will notify the list of the event. */
    @Override
    public void clear()
    {
        super.clear();
        groups.clear();
    }

    @Override
    protected ResultGroup<ResultItem> getGroup(ResultItem item)
    {
        ResultGroup<ResultItem> group;
        Integer key;
        String name;

        if (groupBy == GROUP_KIND)
        {
            // Grouping by the kind
            key = item.getType().getOrder();
            name = item.getType().getName();
        } else if (groupBy == GROUP_DATE)
        {
            // Grouping by dates
            Date date = item.getDate();
            if (date == null)
            {
                name = Strings.message("search.no.date");
                key = Integer.MIN_VALUE;
            } else
            {
                name = TimeRange.findRangeName(date.getTime());
                key = TimeRange.findRangeIndex(date.getTime());
            }
        } else
        {
            // All results
            name = Strings.message("search.all.results");
            key = Integer.MIN_VALUE;
        }

        // Find the group by the key
        group = groups.get(key);
        if (group == null)
        {
            // Add the group
            group = new ResultGroup<ResultItem>(key, name);
            groups.put(key, group);

            // Notify the list of a new group
            fireGroupAdded(group, false);
        }

        return group;
    }
}
