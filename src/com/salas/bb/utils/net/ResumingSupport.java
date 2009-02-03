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
// $Id: ResumingSupport.java,v 1.19 2007/04/13 12:56:43 spyromus Exp $
//

package com.salas.bb.utils.net;

import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resuming support module. Returns connection to a given URL. When <code>resume()</code>
 * method is called the module tries to use protocol-specific means to reach the given
 * position in stream faster. If resuming isn't supported (by local file system or old
 * HTTP servers) then it makes a try to read out everything by that position.
 * <p/>
 * Errors handling isn't performed in this module. So, if there will be an error during
 * connection or skipping of already read content the error will be passed to the caller.
 */
public final class ResumingSupport
{
    private static final Logger LOG = Logger.getLogger(ResumingSupport.class.getName());

    /**
     * It may come that we have a cyclic redirections happening. We need some place to
     * record all redirections we had in current connection session to break the loop
     * once we detect that we are being redirected to the place we have already been to.
     * This thread local object will contain the list of URL's we were trying to connect
     * (as <code>String</code>'s). The first URL to be put there is our initial request URL.
     */
    private static ThreadLocal visitedURLs = new ThreadLocal();

    /**
     * Hidden utility class constructor.
     */
    private ResumingSupport()
    {
    }

    /**
     * Connects to a given URL.
     *
     * @param url   URL to connect to.
     *
     * @return connection object.
     *
     * @throws IOException in case of any IO error.
     */
    public static URLConnectionHolder connect(URL url)
        throws IOException
    {
        return resume(url, 0);
    }

    /**
     * Connects to a given URL at specified position.
     *
     * @param url           URL to connecto.
     * @param position      position in the stream to start reading from.
     *
     * @return connection object.
     *
     * @throws IOException in case of any IO error.
     */
    public static URLConnectionHolder resume(URL url, long position)
        throws IOException
    {
        return resume(url, position, -1);
    }

    /**
     * Connects to a given URL at specified position.
     *
     * @param url           URL to connecto.
     * @param position      position in the stream to start reading from.
     * @param lastFetchTime time of last successful fetching.
     *
     * @return connection object.
     *
     * @throws IOException in case of any IO error.
     */
    public static URLConnectionHolder resume(URL url, long position, long lastFetchTime)
        throws IOException
    {
        return resume(url, position, lastFetchTime, null, null, null);
    }

    /**
     * Connects to a given URL at specified position.
     *
     * @param url           URL to connecto.
     * @param position      position in the stream to start reading from.
     * @param lastFetchTime time of last successful fetching.
     * @param userAgent     HTTP user agent.
     * @param username      Basic Authentication user name.
     * @param password      Basic Authenticaiton user password.
     *
     * @return connection object.
     *
     * @throws IOException in case of any IO error.
     */
    public static URLConnectionHolder resume(URL url, long position, long lastFetchTime, String userAgent,
                                             String username, String password)
        throws IOException
    {
        if (url == null) throw new IllegalArgumentException(Strings.error("unspecified.url"));

        clearVisitedURLs();

        URLConnectionHolder holder;
        if (url.getProtocol().equalsIgnoreCase("file"))
        {
            holder = fileResume(url, position, lastFetchTime);
        } else
        {
            holder = remoteResume(url, position, lastFetchTime, userAgent, username, password);
        }

        return holder;
    }

    private static URLConnectionHolder remoteResume(URL url, long position, long lastFetchTime, String userAgent,
                                                    String username, String password)
        throws IOException
    {
        URLConnection con = url.openConnection();

        URLConnectionHolder holder = new URLConnectionHolder(con, null);

        return remoteResume(holder, position, lastFetchTime, userAgent, username, password);
    }

    private static URLConnectionHolder remoteResume(URLConnectionHolder aHolder, long position,
                                                    long lastFetchTime, String userAgent, String username,
                                                    String password) throws IOException
    {
        URL url = aHolder.getConnection().getURL();

        //  Verify that we haven't already been at this location and register it for further checks
        if (isURLVisited(url)) throw new CyclicRedirectionException(getVisitedURLsList());
        registerVisitedURL(url);

        if (aHolder.getConnection() instanceof HttpURLConnection)
        {
            return httpResume(aHolder, position, lastFetchTime, userAgent, username, password);
        } else
        {
            return otherResume(aHolder, position);
        }
    }

    private static URLConnectionHolder httpResume(URLConnectionHolder holder, long aPosition,
                                                  long aLastFetchTime, String userAgent, String username,
                                                  String password) throws IOException, CyclicRedirectionException
    {
        HttpURLConnection httpCon = (HttpURLConnection)holder.getConnection();

        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password))
        {
            httpCon.setRequestProperty("Authorization", StringUtils.createBasicAuthToken(username, password));
        }

        httpCon.setInstanceFollowRedirects(false);
        httpCon.setAllowUserInteraction(false);
        httpCon.setRequestProperty("Accept-Encoding", "gzip");

        if (userAgent == null) userAgent = System.getProperty("http.agent");
        if (userAgent != null) httpCon.setRequestProperty("User-Agent", userAgent);

        if (aPosition > 0)
        {
            httpCon.setRequestProperty("Range", "bytes=" + Long.toString(aPosition) + "-");
        }

        if (aLastFetchTime > 0) httpCon.setIfModifiedSince(aLastFetchTime);

        httpCon.connect();

        int responseCode = httpCon.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_MULT_CHOICE ||
            responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
            responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
            responseCode == HttpURLConnection.HTTP_SEE_OTHER ||
            responseCode == HttpURLConnection.HTTP_USE_PROXY ||
            responseCode == 307)
        {
            // Redirected
            boolean perm = responseCode == HttpURLConnection.HTTP_MOVED_PERM;
            String location = httpCon.getHeaderField("Location");

            if (location == null) throw new IOException(
                MessageFormat.format(Strings.error("unspecified.redirection.location.0"),
                    new Object[] { httpCon.getURL() }));

            URL newURL = new URL(httpCon.getURL(), location);

            httpCon.disconnect();

            // Log the redirection
            if (LOG.isLoggable(Level.FINE))
            {
                String newLoc = newURL.toString();
                String oldLoc = httpCon.getURL().toString();

                LOG.fine("Redirected " + oldLoc + (perm ? " (permanently)" : "") +
                    " to: " + newLoc);
            }

            // Make another loop with new URL
            holder = new URLConnectionHolder(newURL.openConnection(),
                perm ? newURL : holder.getPermanentRedirectionURL());
            holder = remoteResume(holder, aPosition, aLastFetchTime, userAgent, username, password);
        } else if (responseCode == HttpURLConnection.HTTP_OK)
        {
            skipToPosition(httpCon, aPosition);
        }

        return holder;
    }

    private static URLConnectionHolder otherResume(URLConnectionHolder aHolder, long aPosition)
        throws IOException
    {
        URLConnection con = aHolder.getConnection();

        con.connect();
        skipToPosition(con, aPosition);

        return aHolder;
    }

    /**
     * Performs (re)connection to local resource.
     *
     * @param url       URL to explore.
     * @param position  position in stream.
     * @param lastFetchTime time of last successful fetching.
     *
     * @return holder for connection data.
     *
     * @throws IOException in case of any I/O error.
     */
    private static URLConnectionHolder fileResume(URL url, long position, long lastFetchTime)
        throws IOException
    {
        URLConnection con = url.openConnection();

        // For some reason JRE skipps this check
        if (lastFetchTime > 0) con.setIfModifiedSince(lastFetchTime);

        con.connect();

        skipToPosition(con, position);

        return new URLConnectionHolder(con, null);
    }

    /**
     * Skips first bytes up to specified position.
     *
     * @param con       connection.
     * @param position  position.
     *
     * @throws IOException in case of any I/O error.
     */
    private static void skipToPosition(URLConnection con, long position)
        throws IOException
    {
        if (position <= 0) return;

        InputStream in = con.getInputStream();
        while (position > 0)
        {
            long skipped = in.skip(position);
            position = (skipped == 0) ? 0 : position - skipped;
        }
    }

    /**
     * Clears the list of visited URLs.
     */
    private static void clearVisitedURLs()
    {
        getVisitedURLsList().clear();
    }

    /**
     * Registers visited URL.
     *
     * @param url visited URL.
     */
    private static void registerVisitedURL(URL url)
    {
        if (url != null)
        {
            String urlString = url.toString();

            List listOfVisitedURLs = getVisitedURLsList();
            if (!listOfVisitedURLs.contains(urlString)) listOfVisitedURLs.add(urlString);
        }
    }

    /**
     * Returns <code>TRUE</code> if the URL was visited before.
     *
     * @param url   URL to check.
     *
     * @return <code>TRUE</code> if the URL was visited before.
     */
    private static boolean isURLVisited(URL url)
    {
        String urlString = url.toString();
        List listOfVisitedURLs = getVisitedURLsList();

        return listOfVisitedURLs.contains(urlString);
    }

    /**
     * Returns the list of visited URL's in current session.
     *
     * @return list of visited URL's.
     */
    private static List getVisitedURLsList()
    {
        List listOfVisitedURLs;

        synchronized (visitedURLs)
        {
            if (visitedURLs.get() == null) visitedURLs.set(new ArrayList());
            listOfVisitedURLs = (ArrayList)visitedURLs.get();
        }

        return listOfVisitedURLs;
    }
}
