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
import java.net.URLEncoder;
import java.net.URLConnection;
import java.io.*;
import java.util.Map;
import java.util.ArrayList;

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
        if (user != null && password != null) con.setRequestProperty("Authorization",
            StringUtils.createBasicAuthToken(user, password));

        return readResponse(con);
    }

    /**
     * Sends the POST request and returns the response text.
     *
     * @param url       URL to send the request to.
     * @param data      data map.
     * @param user      user name for the authentication.
     * @param password  password.
     *
     * @return response text.
     *
     * @throws IOException in case when communication fails.
     */
    public static String post(URL url, Map<String, String> data, String user, String password)
        throws IOException
    {
        // Construct data
        ArrayList<String> content = null;

        if (data != null)
        {
            content = new ArrayList<String>(data.size());
            for (Map.Entry<String, String> entry : data.entrySet())
            {
                content.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" +
                    URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
        }

        // Send data
        URLConnection con = url.openConnection();
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        if (user != null && password != null) con.setRequestProperty("Authorization",
            StringUtils.createBasicAuthToken(user, password));


        OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
        try
        {
            if (content != null) wr.write(StringUtils.join(content.iterator(), "&"));
            wr.flush();
        } finally
        {
            wr.close();
        }

        return readResponse(con);
    }

    /**
     * Reads the response text from the given connection.
     *
     * @param con connection.
     *
     * @return response text.
     *
     * @throws IOException in case when communication fails.
     */
    private static String readResponse(URLConnection con)
        throws IOException
    {
        StringBuffer buf = new StringBuffer();
        BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));

        try
        {
            String line;
            while ((line = rd.readLine()) != null) buf.append(line).append("\n");
        } finally
        {
            rd.close();
        }

        return buf.toString().trim();
    }
}
