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
// $Id: DummyEmptyGuide.java,v 1.10 2006/01/08 05:28:16 kyank Exp $
//

package com.salas.bb.domain;

/**
 * Empty dummy guide for testing purposes. It has no feeds, but records and
 * returns read state which is TRUE by default.
 */
class DummyEmptyGuide extends AbstractGuide
{
    private boolean read = true;

    /**
     * Returns the feed at given position. If the position is out of range [0;size) the IOOB
     * exception will be thrown.
     *
     * @param index index of the feed.
     *
     * @return feed at specified index.
     *
     * @throws IndexOutOfBoundsException if the feed index is out of range [0;size).
     */
    public IFeed getFeedAt(int index)
    {
        return null;
    }

    /**
     * Returns number of feeds in the guide.
     *
     * @return number of feeds.
     */
    public int getFeedsCount()
    {
        return 0;
    }

    /**
     * Returns index of feed within the guide.
     *
     * @param feed feed to get index for.
     *
     * @throws NullPointerException  if feed isn't specified.
     * @throws IllegalStateException if feed is assigned to the other guide.
     */
    public int indexOf(IFeed feed)
    {
        throw new IllegalStateException("The feed is assigned to the other guide");
    }

    /**
     * Returns the read status of this guide. The status depends on the statuses of contained
     * feeds.
     */
    public synchronized boolean isRead()
    {
        return read;
    }

    /**
     * Marks whole guide as read/unread depending on the argument. Iterates through all feeds
     * and makes them read/unread.
     */
    public synchronized void setRead(boolean read)
    {
        this.read = read;
    }

    /**
     * Adds feed to the guide.
     *
     * @param feed feed to add.
     *
     * @throws NullPointerException  if feed isn't specified.
     * @throws IllegalStateException if feed is already assigned to some feed.
     */
    public void add(IFeed feed)
    {
    }

    /**
     * Returns <code>TRUE</code> only if the feed was added directly to this guide.
     *
     * @param feed feed.
     *
     * @return <code>TRUE</code> only if the feed was added directly to this guide.
     */
    public boolean hasDirectLinkWith(IFeed feed)
    {
        return false;
    }

    /**
     * Removes feed from the guide.
     *
     * @param feed feed to remove.
     *
     * @throws NullPointerException  if feed isn't specified.
     * @throws IllegalStateException if feed is assigned to the other guide.
     */
    public boolean remove(IFeed feed)
    {
        return false;
    }

    /**
     * Removes the feeds in list from this guide one by one.
     *
     * @param feeds feeds to remove.
     */
    public void remove(IFeed[] feeds)
    {
    }

    /**
     * Returns the array of all feeds.
     *
     * @return array of feeds.
     */
    public IFeed[] getFeeds()
    {
        return new IFeed[0];
    }

    /**
     * Returns alphabetical index of feed within the guide.
     *
     * @param feed feed to get alpha-index for.
     *
     * @return alphabetical index of feed.
     *
     * @throws NullPointerException if feed isn't specified.
     */
    public int alphaIndexOf(IFeed feed)
    {
        return 0;
    }
}
