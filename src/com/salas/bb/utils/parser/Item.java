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
// $Id: Item.java,v 1.6 2008/06/26 13:41:57 spyromus Exp $
//

package com.salas.bb.utils.parser;

import com.salas.bb.utils.i18n.Strings;

import java.net.URL;
import java.util.Comparator;
import java.util.Date;

/**
 * Item of the channel taken from feed. Each item has several optional fields
 * and mandatory content. The <code>text</code> is mandatory.
 */
public class Item
{
    public static final DateComparator COMPARATOR = new DateComparator();
    
    private String  title;
    private String  subject;
    private String  text;
    private String  author;
    private Date    publicationDate;
    private URL     link;
    private String  uri;

    /**
     * Creates channel item.
     *
     * @param aText text of the item.
     *
     * @throws NullPointerException if text is not specified.
     */
    public Item(String aText)
    {
        if (aText == null) throw new NullPointerException(Strings.error("unspecified.text"));

        text = aText;
    }

    /**
     * Returns title of the item.
     *
     * @return title or NULL.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Sets title of the item.
     *
     * @param aTitle title.
     */
    public void setTitle(String aTitle)
    {
        title = aTitle;
    }

    /**
     * Returns subject of the item.
     *
     * @return subject or NULL.
     */
    public String getSubject()
    {
        return subject;
    }

    /**
     * Sets subject of the item.
     *
     * @param aSubject subject.
     */
    public void setSubject(String aSubject)
    {
        subject = aSubject;
    }

    /**
     * Returns text of the item.
     *
     * @return text.
     */
    public String getText()
    {
        return text;
    }

    /**
     * Returns author of the item.
     *
     * @return author or NULL.
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * Sets author of the item.
     *
     * @param aAuthor author.
     */
    public void setAuthor(String aAuthor)
    {
        author = aAuthor;
    }

    /**
     * Returns publication date of the item.
     *
     * @return publication date.
     */
    public Date getPublicationDate()
    {
        return publicationDate;
    }

    /**
     * Sets publication date of the item.
     *
     * @param aPublicationDate publication date.
     */
    public void setPublicationDate(Date aPublicationDate)
    {
        publicationDate = aPublicationDate;
    }

    /**
     * Returns link to the item HTML page.
     *
     * @return link or NULL.
     */
    public URL getLink()
    {
        return link;
    }

    /**
     * Sets link to the item HTLM page.
     *
     * @param aLink link.
     */
    public void setLink(URL aLink)
    {
        link = aLink;
    }

    /**
     * Returns an item URI (Atom has it in "id").
     *
     * @return URI.
     */
    public String getUri()
    {
        return uri;
    }

    /**
     * Sets item URI.
     *
     * @param uri URI.
     */
    public void setUri(String uri)
    {
        this.uri = uri;
    }

    /**
     * Compares two items by dates.
     */
    public static class DateComparator implements Comparator
    {
        /**
         * Compares its two arguments for order.
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         *
         * @return a negative integer, zero, or a positive integer as the first argument
         *         is less than, equal to, or greater than the second.
         *
         * @throws ClassCastException if the arguments' types prevent them from being
         *                            compared by this Comparator.
         */
        public int compare(Object o1, Object o2)
        {
            Item i1 = (Item)o1;
            Item i2 = (Item)o2;

            long t1 = i1.getPublicationDate().getTime();
            long t2 = i2.getPublicationDate().getTime();

            return -(t1 < t2 ? -1 : t1 == t2 ? 0 : 1);
        }
    }
}
