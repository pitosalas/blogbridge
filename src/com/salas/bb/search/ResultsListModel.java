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
// $Id: ResultsListModel.java,v 1.4 2007/07/11 16:31:33 spyromus Exp $
//

package com.salas.bb.search;

import java.util.ArrayList;
import java.util.List;

/**
 * The model for the search results list.
 */
public class ResultsListModel implements IResultsListModel
{
    /** Items list. */
    protected final List<ResultItem> items = new ArrayList<ResultItem>();

    /** Listeners. */
    private final List<IResultsListModelListener> listeners = new ArrayList<IResultsListModelListener>();

    /** Default group which is used for every item. The concrete implementations will use their own. */
    private ResultGroup<ResultItem> defaultGroup;

    /**
     * Adds new item to the model. The model will find the group to add this item to first. If the group is not present,
     * the model will create it and notify the list of a new group. The item will be added to the appropriate group and
     * the list will be notified of a new item.
     *
     * @param item item to add.
     */
    public void add(ResultItem item)
    {
        if (!items.contains(item))
        {
            items.add(item);
            addExistingItem(item);
        }
    }

    /**
     * Adds the item that is already in the items list. This method is used both
     * when adding new items and when regrouping existing.
     *
     * @param item item to add.
     */
    protected void addExistingItem(ResultItem item)
    {
        // Get the group to add this item to
        ResultGroup<ResultItem> group = getGroup(item);
        group.add(item);

        fireItemAdded(item, group);
    }

    /** Removes all items from the model. The model will notify the list of the event. */
    public void clear()
    {
        defaultGroup = null;
        items.clear();

        fireClear();
    }

    /**
     * Returns the number of items in the model.
     *
     * @return size.
     */
    public int size()
    {
        return items.size();
    }

    /**
     * Adds a listener to receive the events from this model.
     *
     * @param l listener.
     */
    public void addListener(IResultsListModelListener l)
    {
        listeners.add(l);
    }

    /**
     * Removes the listener.
     *
     * @param l listener.
     */
    public void removeListener(IResultsListModelListener l)
    {
        listeners.remove(l);
    }

    // ------------------------------------------------------------------------
    // Private stuff
    // ------------------------------------------------------------------------

    /**
     * Returns the group suitable for the item. If the group is not found,
     * creates one.
     *
     * @param item  item.
     *
     * @return group.
     */
    protected ResultGroup<ResultItem> getGroup(ResultItem item)
    {
        if (defaultGroup == null)
        {
            defaultGroup = new ResultGroup<ResultItem>(0, "Results");
            fireGroupAdded(defaultGroup, false);
        }

        return defaultGroup;
    }

    // ------------------------------------------------------------------------
    // Events
    // ------------------------------------------------------------------------

    /**
     * Fires new item addition.
     *
     * @param item  item added.
     * @param group group the item is added to.
     */
    protected void fireItemAdded(ResultItem item, ResultGroup group)
    {
        for (IResultsListModelListener listener : listeners) listener.onItemAdded(this, item, group);
    }

    /**
     * Fires new item removal.
     *
     * @param item  item added.
     * @param group group the item is added to.
     */
    protected void fireItemRemoved(ResultItem item, ResultGroup group)
    {
        for (IResultsListModelListener listener : listeners) listener.onItemRemoved(this, item, group);
    }

    /**
     * Fires new group addition.
     *
     * @param group group added.
     * @param ordered <code>TRUE</code> to hint that group needs no sorting, just appending to the list.
     */
    protected void fireGroupAdded(ResultGroup group, boolean ordered)
    {
        for (IResultsListModelListener listener : listeners) listener.onGroupAdded(this, group, ordered);
    }

    /**
     * Fires new group removal.
     *
     * @param group group added.
     */
    protected void fireGroupRemoved(ResultGroup group)
    {
        for (IResultsListModelListener listener : listeners) listener.onGroupRemoved(this, group);
    }

    /**
     * Fires new group update.
     *
     * @param group group added.
     */
    protected void fireGroupUpdated(ResultGroup group)
    {
        for (IResultsListModelListener listener : listeners) listener.onGroupUpdated(this, group);
    }

    /**
     * Fires clearing event.
     */
    protected void fireClear()
    {
        for (IResultsListModelListener listener : listeners) listener.onClear(this);
    }
}
