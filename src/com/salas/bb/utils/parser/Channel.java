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
// $Id: Channel.java,v 1.7 2006/02/16 10:04:19 spyromus Exp $
//

package com.salas.bb.utils.parser;

import java.net.URL;
import java.util.Arrays;
import java.util.Date;

/**
 * Channel taken from the feed. Each channel has several optional fields and items.
 */
public class Channel
{
    private String  title;
    private String  description;
    private String  author;
    private URL     siteURL;
    private String  format;
    private String  language;
    private long    updatePeriod;

    private Item[]  items;
    private long    creationTime;

    /**
     * The time of last feed update according to the server.
     * May or may not be set.
     */
    private long    lastUpdateServerTime;

    /**
     * Creates the channel.
     */
    public Channel()
    {
        creationTime = System.currentTimeMillis();

        items = new Item[0];
        lastUpdateServerTime = -1;
    }

    /**
     * Returns title of the channel.
     *
     * @return title or NULL.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Sets title of the channel.
     *
     * @param aTitle title.
     */
    public void setTitle(String aTitle)
    {
        title = aTitle;
    }

    /**
     * Returns description of the channel.
     *
     * @return description or NULL.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Sets description of the channel.
     *
     * @param aDescription description.
     */
    public void setDescription(String aDescription)
    {
        description = aDescription;
    }

    /**
     * Returns author of the channel.
     *
     * @return author or NULL.
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * Sets author of the channel.
     *
     * @param aAuthor author.
     */
    public void setAuthor(String aAuthor)
    {
        author = aAuthor;
    }

    /**
     * Returns URL of the site associated with the channel.
     *
     * @return site URL or NULL.
     */
    public URL getSiteURL()
    {
        return siteURL;
    }

    /**
     * Sets URL of the site associated with the channel.
     *
     * @param aSiteURL site URL.
     */
    public void setSiteURL(URL aSiteURL)
    {
        siteURL = aSiteURL;
    }

    /**
     * Returns format of the channel.
     *
     * @return format or NULL.
     */
    public String getFormat()
    {
        return format;
    }

    /**
     * Sets format of the channel.
     *
     * @param aFormat format.
     */
    public void setFormat(String aFormat)
    {
        format = aFormat;
    }

    /**
     * Returns language of the channel.
     *
     * @return language or NULL.
     */
    public String getLanguage()
    {
        return language;
    }

    /**
     * Sets language of the channel.
     *
     * @param aLanguage language.
     */
    public void setLanguage(String aLanguage)
    {
        language = aLanguage;
    }

    /**
     * Returns number of items in the channel.
     *
     * @return number of items.
     */
    public int getItemsCount()
    {
        return items.length;
    }

    /**
     * Returns item at the given index.
     *
     * @param index index of the item.
     *
     * @return item.
     */
    public Item getItemAt(int index)
    {
        return items[index];
    }

    /**
     * Adds item to the list of items.
     *
     * @param aItem item to add.
     */
    public void addItem(Item aItem)
    {
        // Every next item without publication date/time gets current channel creation
        // time and decreases it by 1ms to shift the next item without date a bit to the past.
        // Every item with publication date earlier than current updates it to let
        // further dateless items follow it.
        Date pubDate = aItem.getPublicationDate();
        if (pubDate == null)
        {
            aItem.setPublicationDate(new Date(creationTime--));
        } else if (pubDate.getTime() < creationTime)
        {
            creationTime = pubDate.getTime();
        }

        int index = Arrays.binarySearch(items, aItem, Item.COMPARATOR);

        // If index is positive, we have articles with the same timestamp and we reuse
        // their index for upcoming insertion, otherwise -- we convert to insertion index.
        if (index < 0) index = -index - 1;

        insertItem(aItem, index);
    }

    /**
     * Inserts the item at a given position.
     *
     * @param item  item.
     * @param index index.
     */
    private void insertItem(Item item, int index)
    {
        Item[] newItems = new Item[items.length + 1];
        System.arraycopy(items, 0, newItems, 0, index);
        newItems[index] = item;
        System.arraycopy(items, index, newItems, index + 1, items.length - index);
        items = newItems;
    }

    /**
     * Returns update period in milliseconds or (-1) if not set.
     *
     * @return period or (-1).
     */
    public long getUpdatePeriod()
    {
        return updatePeriod;
    }

    /**
     * Sets update period in milliseconds.
     *
     * @param period period value or -1.
     */
    public void setUpdatePeriod(long period)
    {
        updatePeriod = period;
    }

    /**
     * Returns time of the last update according to a server.
     *
     * @return timestamp.
     */
    public long getLastUpdateServerTime()
    {
        return lastUpdateServerTime;
    }

    /**
     * Sets time of the last update according to a server.
     *
     * @param time  timestamp.
     */
    public void setLastUpdateServerTime(long time)
    {
        lastUpdateServerTime = time;
    }
}
