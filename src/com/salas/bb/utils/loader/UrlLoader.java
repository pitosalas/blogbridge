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
// $Id: UrlLoader.java,v 1.4 2006/01/08 05:00:09 kyank Exp $
//

package com.salas.bb.utils.loader;

import com.salas.bb.utils.net.URLInputStream;

import java.net.URL;
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * Loader of URL resource content.
 */
public class UrlLoader extends AbstractLoader
{
    private URL resourceUrl;

    /**
     * Creates loader of URL resource content.
     *
     * @param message   message to display during loading.
     * @param url       URL to load data from.
     */
    public UrlLoader(String message, URL url)
    {
        super(message);
        resourceUrl = url;
    }

    /**
     * Loads data from the specified url.
     *
     * @throws IOException different IO exceptions.
     */
    protected void processLoad()
        throws IOException
    {
        // Read data from URL into buffer
        StringBuffer sb = new StringBuffer();
        BufferedInputStream stream = new BufferedInputStream(new URLInputStream(resourceUrl));
        int next;
        while ((next = stream.read()) != -1)
        {
            sb.append((char)next);
        }
        stream.close();

        // Convert buffer to
        setData(sb.toString());
    }
}
