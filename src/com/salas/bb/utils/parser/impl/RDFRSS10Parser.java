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
// $Id: RDFRSS10Parser.java,v 1.1 2007/10/01 14:13:06 spyromus Exp $
//

package com.salas.bb.utils.parser.impl;

import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.module.Extendable;
import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.io.impl.DateParser;
import com.salas.bb.utils.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Date;

/**
 * This parser fixes the problem with RDF files that use &lt;rss version="1.0"> header
 * instead of a valid &lt;rdf:RDF ...>.
 */
public class RDFRSS10Parser extends com.sun.syndication.io.impl.RSS10Parser
{
    /**
     * Creates a parser with a special type.
     */
    public RDFRSS10Parser()
    {
        super("rdf_rss_1.0");
    }

    /**
     * Returns <code>TRUE</code> if the broken RDF-RSS feed is detected.
     *
     * @param document source document.
     *
     * @return <code>TRUE</code> if the broken RDF-RSS feed is detected.
     */
    public boolean isMyType(Document document)
    {
        Element root = document.getRootElement();
        return "rss".equalsIgnoreCase(root.getName()) &&
            "1.0".equals(root.getAttributeValue("version")) &&
            root.getChild("channel") != null &&
            root.getChild("channel", super.getRSSNamespace()) == null;
    }

    @Override
    protected WireFeed parseChannel(Element rssRoot)
    {
        Channel feed = (Channel)super.parseChannel(rssRoot);

        // Parse the pub date
        Element channel = rssRoot.getChild("channel", getRSSNamespace());
        String pubDate = channel.getChildText("pubDate", getRSSNamespace());
        if (StringUtils.isNotEmpty(pubDate))
        {
            Date date = DateParser.parseDate(pubDate);
            if (date != null) feed.setPubDate(date);
        }

        return feed;
    }

    /**
     * Parses an item element of an RSS document looking for item information.
     * <p/>
     * It first invokes super.parseItem and then parses and injects the description property if present.
     * <p/>
     *
     * @param rssRoot the root element of the RSS document in case it's needed for context.
     * @param eItem   the item element to parse.
     *
     * @return the parsed RSSItem bean.
     */
    @Override
    protected Item parseItem(Element rssRoot, Element eItem)
    {
        Item item = super.parseItem(rssRoot, eItem);

        // Parse the pub date
        String pubDate = eItem.getChildText("pubDate", getRSSNamespace());
        if (StringUtils.isNotEmpty(pubDate))
        {
            Date date = DateParser.parseDate(pubDate);
            if (date != null) item.setPubDate(date);
        }

        return item;
    }

    /**
     * Returns the RSS namespace.
     *
     * @return namespace.
     */
    protected Namespace getRSSNamespace()
    {
        // In our case no namespace is defined.
        return null;
    }

    /**
     * It looks for the 'item' elements under the 'channel' elemment.
     */
    protected List getItems(Element rssRoot)
    {
        Element eChannel = rssRoot.getChild("channel", getRSSNamespace());
        return (eChannel != null) ? eChannel.getChildren("item", getRSSNamespace()) : Collections.EMPTY_LIST;
    }

    /**
     * Returns the markup that isn't in the RSS spec.
     *
     * @param e      element.
     * @param ext    extendable.
     * @param basens namespace.
     *
     * @return always returns empty list in this parser.
     */
    protected List extractForeignMarkup(Element e, Extendable ext, Namespace basens)
    {
        return new ArrayList();
    }
}
