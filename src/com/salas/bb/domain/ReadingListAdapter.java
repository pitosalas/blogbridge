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
// $Id: ReadingListAdapter.java,v 1.2 2006/01/08 04:48:17 kyank Exp $
//

package com.salas.bb.domain;

/**
 * Adapter of reading list events.
 */
public class ReadingListAdapter implements IReadingListListener
{
    /**
     * Invoked when new feed has been added to the reading list.
     *
     * @param list reading list the feed was added to.
     * @param feed added feed.
     */
    public void feedAdded(ReadingList list, IFeed feed)
    {
    }

    /**
     * Invoked when the feed has been removed from the reading list.
     *
     * @param list reading list the feed was removed from.
     * @param feed removed feed.
     */
    public void feedRemoved(ReadingList list, IFeed feed)
    {
    }

    /**
     * Invoked when the property of the list has been changed.
     *
     * @param list     list owning the property.
     * @param property property name.
     * @param oldValue old property value.
     * @param newValue new property value.
     */
    public void propertyChanged(ReadingList list, String property, Object oldValue, Object newValue)
    {
    }
}
