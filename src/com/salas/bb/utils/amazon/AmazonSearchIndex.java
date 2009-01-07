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
// $Id: AmazonSearchIndex.java,v 1.3 2006/05/31 11:28:30 spyromus Exp $
//

package com.salas.bb.utils.amazon;

import com.salas.bb.utils.i18n.Strings;

/**
 * Possible search indexes.
 */
public class AmazonSearchIndex
{
    /** Books. */
    public static final AmazonSearchIndex Books = new AmazonSearchIndex(
        "Books", new BooksHTMLFormatter());

    /** DVD's. */
    public static final AmazonSearchIndex DVD = new AmazonSearchIndex(
        "DVD", new BooksHTMLFormatter());

    private final String            name;
    private final IHTMLFormatter    formatter;

    /**
     * Creates index.
     *
     * @param aName          name.
     * @param aFormatter    formatter to use.
     */
    private AmazonSearchIndex(String aName, IHTMLFormatter aFormatter)
    {
        name = aName;
        formatter = aFormatter;
    }

    /**
     * Returns index name.
     *
     * @return name.
     */
    public String toString()
    {
        return name;
    }

    /**
     * Formats item as HTML.
     *
     * @param item item to format.
     *
     * @return HTML text or <code>NULL</code> if item is not specified.
     */
    public String formatAsHTML(AmazonItem item)
    {
        return formatter.format(item);
    }
}
