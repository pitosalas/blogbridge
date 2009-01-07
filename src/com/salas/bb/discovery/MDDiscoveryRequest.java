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
// $Id: MDDiscoveryRequest.java,v 1.5 2006/01/08 04:45:31 kyank Exp $
//

package com.salas.bb.discovery;

import com.salas.bb.domain.FeedMetaDataHolder;

import java.net.URL;

/**
 * Discovery request is used to share information across several discovery attempts.
 */
public final class MDDiscoveryRequest
{
    private URL                 url;
    private FeedMetaDataHolder  holder;
    private int                 attempts;
    private boolean             local;

    private boolean             directDiscoveryComplete;
    private boolean             serviceDiscoveryComplete;

    /**
     * Creates discovery request.
     *
     * @param aUrl      URL to discover.
     * @param aHolder   holder to fill with information.
     */
    public MDDiscoveryRequest(URL aUrl, FeedMetaDataHolder aHolder)
    {
        url = aUrl;
        holder = aHolder;
        attempts = 0;

        local = isLocalURL(url);

        directDiscoveryComplete = false;
        serviceDiscoveryComplete = false;
    }

    /**
     * Returns <code>TRUE</code> if given URL is local.
     *
     * @param anURL url to test.
     *
     * @return <code>TRUE</code> if given URL is local.
     */
    public static boolean isLocalURL(URL anURL)
    {
        return anURL != null &&
            (anURL.getProtocol().equals("file") ||
            anURL.getHost().equals("localhost") ||
            anURL.getHost().equals("127.0.0.1"));
    }

    /**
     * Returns URL to discover.
     *
     * @return URL to discover.
     */
    public URL getUrl()
    {
        return url;
    }

    /**
     * Returns <code>TRUE</code> if URL is local.
     *
     * @return <code>TRUE</code> if URL is local.
     */
    public boolean isLocal()
    {
        return local;
    }

    /**
     * Returns holder to fill with information.
     *
     * @return holder to fill.
     */
    public FeedMetaDataHolder getHolder()
    {
        return holder;
    }

    /**
     * Returns <code>TRUE</code> if direct discovery has happened.
     *
     * @return <code>TRUE</code> if direct discovery has happened.
     */
    public boolean isDirectDiscoveryComplete()
    {
        return directDiscoveryComplete;
    }

    /**
     * Sets the value of direct discovery happenning flag.
     *
     * @param flag <code>TRUE</code> when direct discovery is over.
     */
    public void setDirectDiscoveryComplete(boolean flag)
    {
        directDiscoveryComplete = flag;
    }

    /**
     * Returns <code>TRUE</code> if service discovery has happened.
     *
     * @return <code>TRUE</code> if service discovery has happened.
     */
    public boolean isServiceDiscoveryComplete()
    {
        return serviceDiscoveryComplete;
    }

    /**
     * Sets the service discovery completion flag.
     *
     * @param flag <code>TRUE</code> when service discovery is over.
     */
    public void setServiceDiscoveryComplete(boolean flag)
    {
        serviceDiscoveryComplete = flag;
    }

    /**
     * Returns number of previous attempts.
     *
     * @return number of attempts.
     */
    public int getAttempts()
    {
        return attempts;
    }

    /**
     * Adds another attempt count.
     */
    public void addAttemptCount()
    {
        attempts++;
    }
}
