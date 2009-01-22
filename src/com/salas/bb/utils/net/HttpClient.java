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
// $Id$
//

package com.salas.bb.utils.net;

import com.salas.bb.utils.StringUtils;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.io.InputStream;

/**
 * HTTP utility class.
 */
public abstract class HttpClient
{
    /**
     * Performs the GET HTTP request and returns the contents.
     *
     * @param link      link to access.
     *
     * @return contents of the response body.
     *
     * @throws IOException in case of any error.
     */
    public static String get(String link)
        throws IOException
    {
        return get(link, null, null);
    }

    /**
     * Performs the GET HTTP request and returns the contents.
     *
     * @param url       URL to access.
     *
     * @return contents of the response body.
     *
     * @throws IOException in case of any error.
     */
    public static String get(URL url)
        throws IOException
    {
        return get(url, null, null);
    }

    /**
     * Performs the GET HTTP request and returns the contents.
     *
     * @param link      link to access.
     * @param user      basic authentication user name or NULL.
     * @param password  basic authentication password or NULL.
     *
     * @return contents of the response body.
     *
     * @throws IOException in case of any error.
     */
    public static String get(String link, String user, String password)
        throws IOException
    {
        return get(new URL(link), user, password);
    }

    /**
     * Performs the GET HTTP request and returns the contents.
     *
     * @param url       URL to access.
     * @param user      basic authentication user name or NULL.
     * @param password  basic authentication password or NULL.
     *
     * @return contents of the response body.
     *
     * @throws IOException in case of any error.
     */
    public static String get(URL url, String user, String password)
        throws IOException
    {
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        if (user != null && password != null) con.setRequestProperty("Authorization", StringUtils.createBasicAuthToken(user, password));
        InputStream stream = con.getInputStream();

        String response = null;
        try
        {
            StringBuffer buf = new StringBuffer();
            int ch;
            while ((ch = stream.read()) != -1) buf.append((char)ch);
            response = buf.toString();
        } finally
        {
            stream.close();
        }
        return response;
    }
}
