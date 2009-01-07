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
// $Id: ISearchResultListener.java,v 1.5 2007/06/07 09:37:39 spyromus Exp $
//

package com.salas.bb.search;

/**
 * The listener of changes in search result.
 */
public interface ISearchResultListener
{
    /**
     * Invoked when new result item is added to the list.
     *
     * @param result    results list object.
     * @param item      item added.
     * @param index     item index.
     */
    void itemAdded(ISearchResult result, ResultItem item, int index);

    /**
     * Invoked when the result item is removed from the list.
     *
     * @param result    results list object.
     * @param item      item removed.
     * @param index     index of the item it was removed from.
     */
//    void itemRemoved(ISearchResult result, ResultItem item, int index);

    /**
     * Invoked when the result items are removed from the list.
     *
     * @param result    results list object.
     */
    void itemsRemoved(ISearchResult result);

    /**
     * Invoked when underlying search is finished.
     *
     * @param result    results list object.
     */
    void finished(ISearchResult result);
}
