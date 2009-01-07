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
// $Id: IResultsListModel.java,v 1.1 2007/06/07 09:37:39 spyromus Exp $
//

package com.salas.bb.search;

/**
 * The model for the results list class.
 */
public interface IResultsListModel
{
    /**
     * Adds new item to the model. The model will find the group to add this
     * item to first. If the group is not present, the model will create it
     * and notify the list of a new group. The item will be added to the
     * appropriate group and the list will be notified of a new item.
     *
     * @param item item to add.
     */
    void add(ResultItem item);

    /**
     * Removes all items from the model. The model will notify the list
     * of the event.
     */
    void clear();

    /**
     * Adds a listener to receive the events from this model.
     *
     * @param l listener.
     */
    void addListener(IResultsListModelListener l);

    /**
     * Removes the listener.
     *
     * @param l listener.
     */
    void removeListener(IResultsListModelListener l);

    /**
     * Returns the number of items in the model.
     *
     * @return size.
     */
    int size();
}
