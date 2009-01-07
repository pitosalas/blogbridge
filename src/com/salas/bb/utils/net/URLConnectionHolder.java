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
// $Id: URLConnectionHolder.java,v 1.2 2006/01/08 05:00:09 kyank Exp $
//

package com.salas.bb.utils.net;

import java.net.URLConnection;
import java.net.URL;

/**
 * Holder for connection data. If when we were connecting to some resource we were redirected
 * permanently somewhere the URL will be recorded in <code>permanentRedirectionURL</code>.
 * Please note that if there were three leaps: OriginalURL -> TempRedir -> PermRedir -> TempRedir,
 * the <code>PermRedir</code> will be recorded in the property.
 */
public class URLConnectionHolder
{
    private URL             permanentRedirectionURL;
    private URLConnection   connection;

    /**
     * Creates holder.
     *
     * @param aConnection               connection to hold.
     * @param aPermanentRedirectionURL  URL we were redirected to.
     */
    public URLConnectionHolder(URLConnection aConnection, URL aPermanentRedirectionURL)
    {
        connection = aConnection;
        permanentRedirectionURL = aPermanentRedirectionURL;
    }

    /**
     * Returns connection.
     *
     * @return connection.
     */
    public URLConnection getConnection()
    {
        return connection;
    }

    /**
     * Returns redirection URL.
     *
     * @return URL or NULL.
     */
    public URL getPermanentRedirectionURL()
    {
        return permanentRedirectionURL;
    }
}
