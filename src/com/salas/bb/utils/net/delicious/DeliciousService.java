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
// $Id: DeliciousService.java,v 1.5 2006/05/19 08:47:01 spyromus Exp $
//

package com.salas.bb.utils.net.delicious;

import com.salas.bb.utils.Assert;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.net.BBHttpClient;
import com.salas.bb.utils.parser.*;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Delicious service representative. This class represents a front-end to del.icio.us
 * service API.</p>
 *
 * <p>It follows the API conventions taken from <a href="http://del.icio.us/doc/api">official API
 * page</a>.</p>
 */
public final class DeliciousService
{
    private static final String SERVICE_URL = "http://del.icio.us/";
    private static final String SERVICE_API_URL = "https://api.del.icio.us/";

    private static final MessageFormat QUERY_FORMAT =
        new MessageFormat("rss/url/?url={0}");
    private static final MessageFormat SEND_TAGS_FORMAT =
        new MessageFormat("v1/posts/add?url={0}&tags={1}&description={2}&extended={3}");
    private static final MessageFormat DELETE_TAGS_FORMAT =
        new MessageFormat("v1/posts/delete?url={0}");

    /**
     * Hidden utility class constructor.
     */
    private DeliciousService()
    {
    }

    /**
     * Returns the list of tags sets assigned by different users to the given URL.
     *
     * @param link link to lookup in tags database.
     *
     * @return list of tags sets.
     *
     * @throws IOException if communication to service fails.
     * @throws NullPointerException if link is not specified.
     */
    public static DeliciousTags[] getLinkTags(URL link)
        throws IOException
    {
        Assert.notNull("Link", link);

        Channel channel = queryService(link.toString());
        return channel != null ? convertChannelToTags(channel) : null;
    }

    /**
     * Puts the link into the bookmarks of some user with given tags and description.
     *
     * @param link          link to place tags for.
     * @param user          user name registered at the service.
     * @param password      password for the user.
     * @param tags          tags to put.
     * @param description   description to assign (can't be empty or <code>NULL</code>).
     * @param extended      extended description (can be <code>NULL</code>).
     *
     * @return <code>TRUE</code> if successfully tagged link.
     *
     * @throws NullPointerException if link, user, password, tags or description aren't specified.
     * @throws IllegalArgumentException if user, password, tags or description are empty.
     * @throws IOException if communication to service fails.
     */
    public static boolean tagLink(URL link, String user, String password, String[] tags,
        String description, String extended) throws IOException
    {
        Assert.notNull("Link", link);
        Assert.notEmpty("User", user);
        Assert.notEmpty("Password", password);
        Assert.notEmpty("Tags", tags);
        Assert.notEmpty("Description", description);

        String urlString = SEND_TAGS_FORMAT.format(new String[] {
            StringUtils.encodeForURL(link.toString()),
            StringUtils.encodeForURL(StringUtils.join(tags, " ")),
            StringUtils.encodeForURL(description),
            extended == null ? "" : StringUtils.encodeForURL(extended)});

        return sendSimpleRequest(urlString, user, password);
    }

    /**
     * Removes the link from bookmarks of user.
     *
     * @param link      link to remove from bookmarks.
     * @param user      user name registered at the service.
     * @param password  password for the user.
     *
     * @return <code>TRUE</code> if link was untagged.
     *
     * @throws NullPointerException if link, user or password aren't specified.
     * @throws IllegalArgumentException if user or password are empty.
     * @throws IOException if communication to service fails.
     */
    public static boolean untagLink(URL link, String user, String password)
        throws IOException
    {
        Assert.notNull("Link", link);
        Assert.notEmpty("User", user);
        Assert.notEmpty("Password", password);

        String urlString = DELETE_TAGS_FORMAT.format(new String[] {
            StringUtils.encodeForURL(link.toString()) });

        return sendSimpleRequest(urlString, user, password);
    }

    /**
     * Returns the list of tags used by given user.
     *
     * @param user      user name registered at the service.
     * @param password  password for the user.
     *
     * @return list of tags used by this user by the moment.
     *
     * @throws NullPointerException if user or password aren't specified.
     * @throws IllegalArgumentException if user or password are empty.
     * @throws IOException if communication to service fails.
     */
    public static String[] getUserTags(String user, String password)
        throws IOException
    {
        Assert.notEmpty("User", user);
        Assert.notEmpty("Password", password);

        return responseToTags(sendApiRequest("v1/tags/get?", user, password));
    }

    /**
     * Converts tags response to tags list.
     *
     * @param response response.
     *
     * @return tags.
     */
    static String[] responseToTags(String response)
    {
        List tags = new ArrayList();

        if (response != null)
        {
            Pattern pattern = Pattern.compile("tag=\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(response);
            while (matcher.find())
            {
                tags.add(matcher.group(1));
            }
        }

        return (String[])tags.toArray(new String[tags.size()]);
    }

    /**
     * Sends simple request from the name of user. In response server returns simple
     * XML which contains either code='done' (success) or code='something went wrong'
     * (failure).
     *
     * @param request       request.
     * @param user          user name.
     * @param password      user password.
     *
     * @return <code>TRUE</code> if server returned successful response.
     *
     * @throws IOException if communication with service fails.
     */
    private static boolean sendSimpleRequest(String request, String user, String password)
        throws IOException
    {
        return sendApiRequest(request, user, password).indexOf("code=\"done\"") != -1;
    }

    /**
     * Sends request and returns output of the server. The reuqest is accompanied with
     * authentication info.
     *
     * @param request       request.
     * @param user          user name.
     * @param password      user password.
     *
     * @return response.
     *
     * @throws IOException if communication with service fails.
     */
    private static String sendApiRequest(String request, String user, String password)
        throws IOException
    {
        return BBHttpClient.get(SERVICE_API_URL + request, user, password);
    }

    /**
     * Queries service for data for given link. Service returns response in RDF format
     * in any case (whether the link is tagged or no). This response is parsed into
     * valid {@link com.salas.bb.utils.parser.Channel}.
     *
     * @param aLink link to query service for.
     *
     * @return channel or <code>NULL</code> in case of any problems.
     *
     * @throws IOException if communication fails.
     */
    static Channel queryService(String aLink)
        throws IOException
    {
        Channel channel = null;
        String query = SERVICE_URL + QUERY_FORMAT.format(new String[] { aLink });

        IFeedParser parser = FeedParserConfig.create();
        try
        {
            FeedParserResult result = parser.parse(new URL(query), null, -1);
            channel = result.getChannel();
        } catch (FeedParserException e)
        {
//            throw new IOException("Failed to parse delicious response for: " + aLink, e);
        }

        return channel;
    }

    /**
     * Converts channel (list of user records) into list of tags. The list can contain
     * duplicate tags.
     *
     * @param aChannel channel to convert.
     *
     * @return list of tags.
     */
    static DeliciousTags[] convertChannelToTags(Channel aChannel)
    {
        int itemsCount = aChannel.getItemsCount();
        List tags = new ArrayList(itemsCount);

        for (int i = 0; i < itemsCount; i++)
        {
            Item item = aChannel.getItemAt(i);
            DeliciousTags itemTags = convertItemToTags(item);
            if (itemTags != null) tags.add(itemTags);
        }

        return (DeliciousTags[])tags.toArray(new DeliciousTags[tags.size()]);
    }

    /**
     * Converts item (user record) to list of tags.
     *
     * @param aItem item.
     *
     * @return tags object or <code>NULL</code> if item has no tags set.
     */
    static DeliciousTags convertItemToTags(Item aItem)
    {
        String user = aItem.getAuthor();
        String subject = aItem.getSubject();
        String[] tags = (subject != null) ? StringUtils.split(subject, " ") : null;
        return tags == null ? null : new DeliciousTags(user, tags);
    }
}
