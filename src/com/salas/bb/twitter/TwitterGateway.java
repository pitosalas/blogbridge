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

package com.salas.bb.twitter;

import com.salas.bb.core.GlobalController;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.net.HttpClient;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.exception.OAuthException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Twitter gateway.
 */
public class TwitterGateway
{
    private static final Logger LOG = Logger.getLogger(TwitterGateway.class.getName());

    // Screen name extraction pattern
    public static final Pattern PATTERN_SCREEN_NAME =
        Pattern.compile("http://(www\\.)?twitter\\.com/([^/\\?#\\s]+)($|#|\\?)");
    private static final Pattern PATTERN_HASHTAG =
        Pattern.compile("^http://search\\.twitter\\.com/search(\\.(json|atom)?)?\\?q=(#|%23)([^&\\s\\+]+)($|&)");

    /**
     * Posts an update to the twitter account.
     *
     * @param status status message.
     *
     * @throws IOException if fails to communicate.
     * @throws OAuthException OAuth error.
     */
    public static void update(String status)
        throws IOException, OAuthException
    {
        reply(status, null);
    }

    /**
     * Posts a reply to the twitter account.
     *
     * @param status    status message.
     * @param replyToId ID of the original message or NULL.
     *
     * @throws IOException if fails to communicate.
     * @throws OAuthException OAuth error.
     */
    public static void reply(String status, String replyToId)
        throws IOException, OAuthException
    {
        URL url = new URL("http://twitter.com/statuses/update.json");

        Map<String, String> data = new HashMap<String, String>();
        data.put("status", status);
        data.put("source", "blogbridge");
        if (replyToId != null) data.put("in_reply_to_status_id", replyToId);

        post(url, data);
    }

    /**
     * Returns TRUE if the current user has friendship with the given screenname.
     *
     * @param screenname user to check friendship with.
     *
     * @return TRUE if follows.
     *
     * @throws IOException if fails to communicate.
     */
    public static boolean isFollowing(String screenname)
        throws IOException
    {
        TwitterPreferences prefs = getPreferences();

        String userA = encode(prefs.getScreenName());
        String userB = encode(screenname);
        URL url = new URL("http://twitter.com/friendships/exists.json?user_a=" + userA + "&user_b=" + userB);
        String res;

        try
        {
            res = get(url);
        } catch (OAuthException e)
        {
            throw new IOException(e.getMessage(), e.getCause());
        }

        return res != null && res.contains("true");
    }

    /**
     * Requests to follow the given user.
     *
     * @param screenName user.
     *
     * @throws IOException if fails to communicate.
     * @throws OAuthException OAuth error.
     */
    public static void follow(String screenName)
        throws IOException, OAuthException
    {
        screenName = encode(screenName);

        URL url = new URL("http://twitter.com/friendships/create/" + screenName + ".json");

        Map<String, String> data = new HashMap<String, String>();
        data.put("follow", "true");

        post(url, data);
    }

    /**
     * Requests to unfollow the given user.
     *
     * @param screenName user.
     *
     * @throws IOException if fails to communicate.
     * @throws OAuthException OAuth error.
     */
    public static void unfollow(String screenName)
        throws IOException, OAuthException
    {
        screenName = encode(screenName);

        URL url = new URL("http://twitter.com/friendships/destroy/" + screenName + ".json");
        post(url, null);
    }

    /**
     * Returns HTML for the user info popup.
     *
     * @param screenName screen name.
     *
     * @return HTML.
     *
     * @throws IOException if fails to communicate.
     */
    public static String userInfoHTML(String screenName)
        throws IOException
    {
        URL url = new URL("http://twitter.com/users/show/" + screenName + ".json");
        String response;
        try
        {
            response = get(url);
        } catch (OAuthException e)
        {
            throw new IOException(e.getMessage(), e.getCause());
        }

        String html;

        try
        {
            JSONObject data = new JSONObject(response);
            String description          = data.getString("description");
            String name                 = data.getString("name");
            String profile_image_url    = data.getString("profile_image_url");
            String screen_name          = data.getString("screen_name");
            String location             = data.getString("location");
            String followers            = data.getString("followers_count");
            String updates              = data.getString("statuses_count");
            String friends              = data.getString("friends_count");

            html  = "<html><div style='padding:10px'>";
            html += "<table border='0'>";
            html += "<tr valign='top'>";
            if (isShowingPics())
            {
                html += "<td><img src='" + profile_image_url + "'></td>";
                html += "<td width='10'>&nbsp;</td>";
            }
            html += "<td width='300'>";
            html += "<p><strong>" + name + "</strong> (" + screen_name + ")</p>";
            if (StringUtils.isNotEmpty(location)) html += "<p>" + location + "</p>";
            if (StringUtils.isNotEmpty(description)) html += "<br><p>" + description + "</p><br>";
            html += "<p>Followers: " + followers + "</p>";
            html += "<p>Friends: " + friends + "</p>";
            html += "<p>Updates: " + updates + "</p>";
            html += "</td></tr></table>";
            html += "</div>";
        } catch (JSONException e)
        {
            LOG.log(Level.INFO, "Failed to load screen name info", e);
            html = null;
        }

        return html;
    }

    /**
     * Search.
     *
     * @param query query.
     *
     * @return Formatted HTML.
     *
     * @throws IOException if communication fails.
     */
    public static String search(String query)
        throws IOException
    {
        URL url = new URL("http://search.twitter.com/search.json?q=" + encode(query) + "&rpp=5");
        String response = HttpClient.get(url);

        String html;

        try
        {
            JSONObject data = new JSONObject(response);
            JSONArray results = data.getJSONArray("results");

            html  = "<html><div style='padding:10px'>";
            html += "<table border='0'>";

            int count = results.length();
            for (int i = 0; i < count; i++)
            {
                JSONObject record = results.getJSONObject(i);

                html += "<tr valign='top'>";
                if (isShowingPics())
                {
                    html += "<td><img src='" + record.getString("profile_image_url") + "'></td>";
                    html += "<td width='10'>&nbsp;</td>";
                }
                html += "<td width='300'>";
                html += "<p><strong>" + record.getString("from_user") + "</strong>: " + record.getString("text") + "</p>";
                html += "</td>";
                html += "</tr>";
            }

            html += "</table></div>";
        } catch (JSONException e)
        {
            LOG.log(Level.INFO, "Failed to load search results for '" + query + "'", e);
            html = null;
        }

        return html;
    }

    /**
     * Returns preferences.
     *
     * @return preferences.
     */
    private static TwitterPreferences getPreferences()
    {
        return GlobalController.SINGLETON.getModel().getUserPreferences().getTwitterPreferences();
    }

    /**
     * Authenticated GET request.
     *
     * @param url URL.
     *
     * @return response.
     *
     * @throws IOException if communication fails.
     * @throws OAuthException OAuth exception.
     */
    private static String get(URL url)
        throws IOException, OAuthException
    {
        TwitterPreferences prefs = getPreferences();
        OAuthConsumer consumer = prefs.getConsumer();
        return HttpClient.get(url, consumer);
    }

    /**
     * Authenticated POST request.
     *
     * @param url  URL.
     * @param data data map.
     *
     * @throws IOException if communication fails.
     * @throws OAuthException OAuth error.
     */
    private static void post(URL url, Map<String, String> data)
        throws IOException, OAuthException
    {
        TwitterPreferences prefs = getPreferences();
        HttpClient.post(url, data, prefs.getConsumer());
    }

    /**
     * URL-encodes the string.
     *
     * @param str string.
     *
     * @return string.
     *
     * @throws UnsupportedEncodingException if UTF-8 isn't supported -- nonsense.
     */
    private static String encode(String str)
        throws UnsupportedEncodingException
    {
        return URLEncoder.encode(str, "UTF-8");
    }

    /**
     * Returns the username from the URL or NULL.
     *
     * @param url URL to analyze.
     *
     * @return screen name.
     */
    public static String urlToScreenName(URL url)
    {
        if (url == null) return null;

        String name = null;

        String urls = url.toString();
        Matcher m = PATTERN_SCREEN_NAME.matcher(urls);
        try
        {
            if (m.find()) name = URLDecoder.decode(m.group(2), "UTF-8");
        } catch (UnsupportedEncodingException e)
        {
            // Failed transformation -- ignore
        }

        return name;
    }

    /**
     * Extracts the hashtag from the search URL.
     *
     * @param url URL.
     *
     * @return hashtag.
     */
    public static String urlToHashtag(URL url)
    {
        if (url == null) return null;

        String tag = null;
        
        String urls = url.toString();
        Matcher m = PATTERN_HASHTAG.matcher(urls);
        try
        {
            if (m.find()) tag = URLDecoder.decode(m.group(4), "UTF-8");
        } catch (UnsupportedEncodingException e)
        {
            // Failed transformation -- ignore
        }

        return tag;
    }

    /**
     * Returns TRUE if profile pics loading is enabled.
     *
     * @return TRUE if profile pics loading is enabled.
     */
    private static boolean isShowingPics()
    {
        return getPreferences().isProfilePics();
    }
}
