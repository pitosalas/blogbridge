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
// $Id: DummyNetworkFeed.java,v 1.4 2006/01/12 12:10:44 spyromus Exp $
//

package com.salas.bb.domain;

import java.net.URL;

/**
 * This dummy network feed has the means to change XML URL on the fly. It's good
 * for the situations where we need to test the cases with broken links.
 */
class DummyNetworkFeed extends NetworkFeed
{
    private URL xmlURL = null;

    /**
     * Returns title of feed.
     *
     * @return title.
     */
    public String getTitle()
    {
        return "DummyNetworkFeed";
    }

    /**
     * Sets new XML URL.
     *
     * @param aXmlURL new URL.
     */
    public void setXmlURL(URL aXmlURL)
    {
        xmlURL = aXmlURL;
    }

    /**
     * Gets XML URL.
     *
     * @return URL.
     */
    public URL getXmlURL()
    {
        return xmlURL;
    }

    /**
     * Returns simple match key, which can be used to detect similarity of feeds. For example, it's
     * XML URL for the direct feeds, query type + parameter for the query feeds, serialized search
     * criteria for the search feeds.
     *
     * @return match key.
     */
    public String getMatchKey()
    {
        return null;
    }
}
