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
// $Id: DiscoveryResult.java,v 1.2 2006/01/08 05:00:08 kyank Exp $
//

package com.salas.bb.utils.discovery;

import java.net.URL;

/**
 * Result of discovery operation.
 */
public class DiscoveryResult
{

    // Types of source link

    /**
     * Direct feed link.
     */
    public static final int TYPE_FEED = 0;

    /**
     * Link to web log.
     */
    public static final int TYPE_BLOG = 1;

    /**
     * Link to other web resource.
     */
    public static final int TYPE_OTHER = 2;

    // Type of source link
    private int sourceLinkType;

    // Discovered link
    private URL link;

    /**
     * Creates result object.
     *
     * @param aSourceLinkType type of source link.
     * @param aLink           discovereed link.
     *
     * @see #TYPE_FEED
     * @see #TYPE_BLOG
     * @see #TYPE_OTHER
     */
    public DiscoveryResult(int aSourceLinkType, URL aLink)
    {
        sourceLinkType = aSourceLinkType;
        link = aLink;
    }

    /**
     * Returns type of source link.
     *
     * @return type.
     *
     * @see #TYPE_FEED
     * @see #TYPE_BLOG
     * @see #TYPE_OTHER
     */
    public int getSourceLinkType()
    {
        return sourceLinkType;
    }

    /**
     * Returns discovered link.
     *
     * @return link.
     */
    public URL getLink()
    {
        return link;
    }
}
