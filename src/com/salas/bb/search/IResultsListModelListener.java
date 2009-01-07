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
// $Id: IResultsListModelListener.java,v 1.3 2007/07/11 16:31:33 spyromus Exp $
//

package com.salas.bb.search;

/**
 * Listener of the model.
 */
public interface IResultsListModelListener
{
    /**
     * Invoked when the model is cleared.
     *
     * @param model     model.
     */
    void onClear(IResultsListModel model);

    /**
     * Invoked when an item is added to the model.
     *
     * @param model     model.
     * @param item      item added.
     * @param group     group the item was added to.
     */
    void onItemAdded(IResultsListModel model, ResultItem item, ResultGroup group);

    /**
     * Invoked when an item is removed from the group.
     *
     * @param model     model.
     * @param item      item removed.
     * @param group     group the item was removed from.
     */
    void onItemRemoved(IResultsListModel model, ResultItem item, ResultGroup group);

    /**
     * Invoked when the model adds a group to hold new items.
     *
     * @param model     model.
     * @param group     group added.
     * @param ordered   when <code>TRUE</code> group is added in the order of appearance.
     */
    void onGroupAdded(IResultsListModel model, ResultGroup group, boolean ordered);

    /**
     * Invoked when the model removes a group to hold new items.
     *
     * @param model     model.
     * @param group     group added.
     */
    void onGroupRemoved(IResultsListModel model, ResultGroup group);

    /**
     * Invoked when the model changes a group to hold new items.
     *
     * @param model     model.
     * @param group     group added.
     */
    void onGroupUpdated(IResultsListModel model, ResultGroup group);
}
