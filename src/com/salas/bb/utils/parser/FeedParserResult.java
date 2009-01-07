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
// $Id: FeedParserResult.java,v 1.2 2006/01/08 05:00:09 kyank Exp $
//

package com.salas.bb.utils.parser;

import java.net.URL;

/**
 * The result of parsing. Holds internal feed hierarchy: channel and items.
 * Also, if the feed has been moved and reported permanent redirection, this
 * holder will hold redirection URL which should be used for further feed
 * updates.
 */
public class FeedParserResult
{
    private Channel channel;
    private URL redirectionURL;

    /**
     * Constructs the parser result object.
     */
    public FeedParserResult()
    {
    }

    /**
     * Returns TRUE if and only if the feed has been <i>permanently</i> moved to
     * different location.
     *
     * @return TRUE if the feed has been permanently moved.
     */
    public boolean hasBeenRedirected()
    {
        return redirectionURL != null;
    }

    /**
     * Returns the URL to which the feed has been <i>permanently</i> moved.
     *
     * @return new URL or NULL if it wasn't.
     */
    public URL getReridrectionURL()
    {
        return redirectionURL;
    }

    /**
     * Sets the redirection URL.
     *
     * @param aRedirectionURL redirection URL.
     */
    public void setRedirectionURL(URL aRedirectionURL)
    {
        redirectionURL = aRedirectionURL;
    }

    /**
     * Returns the root of the parsed feed -- the channel. If this object came from
     * the parser then the channel is always not NULL.
     *
     * @return channel taken from feed.
     */
    public Channel getChannel()
    {
        return channel;
    }

    /**
     * Sets the channel.
     *
     * @param aChannel channel to store.
     */
    public void setChannel(Channel aChannel)
    {
        channel = aChannel;
    }
}
