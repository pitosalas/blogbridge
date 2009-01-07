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
// $Id: ISearchResult.java,v 1.3 2007/06/07 09:37:39 spyromus Exp $
//

package com.salas.bb.search;

/**
 * Result of the search operation.
 */
public interface ISearchResult
{
    /**
     * Returns the number of result items.
     *
     * @return items.
     */
    int getItemsCount();

    /**
     * Returns the result item at a given index.
     *
     * @param index index.
     *
     * @return item.
     */
    ResultItem getItem(int index);

    /**
     * Subscribes the listener to changes notifications.
     *
     * @param l listener.
     */
    void addChangesListener(ISearchResultListener l);

    /**
     * Unsubscribes the listener from changes notifications.
     *
     * @param l listener.
     */
    void removeChangesListener(ISearchResultListener l);
}
