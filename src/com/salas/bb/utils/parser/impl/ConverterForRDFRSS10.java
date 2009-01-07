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
// $Id: ConverterForRDFRSS10.java,v 1.1 2007/10/01 14:13:06 spyromus Exp $
//

package com.salas.bb.utils.parser.impl;

import com.sun.syndication.feed.synd.impl.ConverterForRSS10;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.rss.Item;

/**
 * A specialized converter for the RDFRSS10Parser content.
 */
public class ConverterForRDFRSS10 extends ConverterForRSS10
{
    /**
     * Creates a converter.
     */
    public ConverterForRDFRSS10()
    {
        super("rdf_rss_1.0");
    }

    @Override
    protected SyndEntry createSyndEntry(Item item)
    {
        SyndEntry entry = super.createSyndEntry(item);
        entry.setPublishedDate(item.getPubDate());
        return entry;
    }
}
