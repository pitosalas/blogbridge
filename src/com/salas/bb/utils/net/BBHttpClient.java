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
import oauth.signpost.OAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTP utility class.
 */
public abstract class BBHttpClient
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
     * Performs the GET HTTP request by signing it first with OAuth consumer.
     *
     * @param url       URL to access.
     * @param consumer  consumer to use for signing.
     *
     * @return contents of the response body.
     *
     * @throws OAuthExpectationFailedException OAuth error.
     * @throws OAuthMessageSignerException     OAuth error.
     * @throws OAuthCommunicationException     OAuth error.
     * @throws IOException in case of I/O error.
     */
    public static String get(URL url, OAuthConsumer consumer)
        throws OAuthExpectationFailedException, OAuthMessageSignerException, OAuthCommunicationException, IOException
    {
        HttpClient client = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url.toString());

        if (consumer != null) consumer.sign(httpget);

        return client.execute(httpget, new BasicResponseHandler());
    }

    /**
     * Sends the POST request and returns the response text.
     *
     * @param url       URL to send the request to.
     * @param data      data map.
     * @param consumer  OAuth consumer to sign the request (optional).
     *
     * @return response text.
     *
     * @throws IOException in case when communication fails.
     * @throws OAuthException OAuth exception.
     */
    public static String post(URL url, Map<String, String> data, OAuthConsumer consumer)
        throws IOException, OAuthException
    {
        List<NameValuePair> form = null;
        if (data != null)
        {
            form = new ArrayList<NameValuePair>(data.size());
            for (Map.Entry<String, String> entry : data.entrySet())
            {
                form.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }

        // Send data
        HttpPost httppost = new HttpPost(url.toString());

        if (form != null) {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, "UTF-8");
            httppost.setEntity(entity);
        }

        if (consumer != null) consumer.sign(httppost);

        HttpClient client = new DefaultHttpClient();
        return client.execute(httppost, new BasicResponseHandler());
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
