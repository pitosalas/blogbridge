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
// $Id: RomeFeedParser.java,v 1.25 2008/06/26 13:41:57 spyromus Exp $
//

package com.salas.bb.utils.parser;

import com.salas.bb.networking.manager.NetManager;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.net.IPermanentRedirectionListener;
import com.salas.bb.utils.net.URLInputStream;
import com.salas.bb.utils.parser.impl.BBSyndFeedInput;
import com.salas.bb.utils.xml.XmlReaderFactory;
import com.sun.syndication.feed.module.DCModule;
import com.sun.syndication.feed.module.DCSubject;
import com.sun.syndication.feed.module.Module;
import com.sun.syndication.feed.module.SyModule;
import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.totsp.xml.syndication.content.ContentModule;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Gateway to Rome parser.
 */
public class RomeFeedParser implements IFeedParser
{
    private static final List CONTENT_TYPE_PREFERENCE;
    private static final String TYPE_HTML = "html";
    private static final String TYPE_TEXT_HTML = "text/html";

    static
    {
        CONTENT_TYPE_PREFERENCE = Arrays.asList(TYPE_TEXT_HTML, TYPE_HTML, "text/plain", "text", "text/xhtml", "xhtml");
    }

    /**
     * Parses the resource by the given URL and returns the objects.
     *
     * @param xmlURL         XML URL of the resource.
     * @param title          feed title (if known).
     * @param lastUpdateTime time of last update (server time-zone) or (-1) if not known.
     *
     * @return result.
     *
     * @throws FeedParserException
     *                              in case of any problems with parsing.
     * @throws NullPointerException if the URL is NULL.
     * @throws java.io.IOException  if there's a problem with reading feed.
     */
    public FeedParserResult parse(URL xmlURL, String title, long lastUpdateTime)
        throws FeedParserException, IOException
    {
        if (xmlURL == null) throw new NullPointerException(Strings.error("unspecified.url"));

        FeedParserResult result = new FeedParserResult();

        // Create stream for reading the feed and register it
        URLInputStream stream = new URLInputStream(xmlURL, lastUpdateTime);
        if (title == null) title = xmlURL.toString();
        NetManager.register(NetManager.TYPE_POLLING, title, title, stream);
        stream.setRedirectionListener(new RomeFeedParser.RedirectionRecorder(result));

        stream.connect();
        try
        {
            long lastModifiedTime = stream.getLastModifiedTime();
            if (lastModifiedTime == -1) lastModifiedTime = stream.getServerTime();

            if (stream.getResponseCode() != HttpURLConnection.HTTP_NOT_MODIFIED)
            {
                result = parse(stream, result, xmlURL);
            }

            Channel channel = result.getChannel();
            if (channel != null) channel.setLastUpdateServerTime(lastModifiedTime);
        } finally
        {
            stream.close();
        }

        return result;
    }


    /**
     * Parses the resource presented by a stream and returns the objects.
     *
     * @param stream    XML stream.
     * @param rootURL   URL for the relative links resolution.
     *
     * @return result.
     *
     * @throws FeedParserException  in case of any problems with parsing.
     * @throws NullPointerException if the URL is NULL.
     * @throws java.io.IOException  if there's a problem with reading feed.
     */
    public FeedParserResult parse(InputStream stream, URL rootURL)
        throws IOException, FeedParserException
    {
        return parse(stream, new FeedParserResult(), rootURL);
    }

    /**
     * Parses the resource by the given stream.
     *
     * @param aStream   stream to parse as feed.
     * @param aResult   object with result to fill.
     * @param aFeedURL  root URL of a feed for the relative links resolution.
     *
     * @return result.
     *
     * @throws FeedParserException  in case of any problems with parsing.
     * @throws IOException          if there's a problem with reading feed.
     */
    protected FeedParserResult parse(InputStream aStream, FeedParserResult aResult, URL aFeedURL)
        throws IOException, FeedParserException
    {
        try
        {
            SyndFeedInput input = new BBSyndFeedInput();
            SyndFeed feed = input.build(XmlReaderFactory.create(aStream));

            Channel channel = RomeFeedParser.convertFeed(feed, aFeedURL);
            aResult.setChannel(channel);

            // Add items
            for (SyndEntry item : (List<SyndEntry>)feed.getEntries())
            {
                channel.addItem(RomeFeedParser.convertItem(item, aFeedURL));
            }
        } catch (FeedException e)
        {
            throw new FeedParserException(Strings.error("failed.to.parse.the.feed"), e);
        }

        return aResult;
    }

    /**
     * Converts feed object into internal format.
     *
     * @param aFeed     source feed object.
     * @param aFeedURL  root URL of a feed for the relative links resolution.
     *
     * @return internal object.
     *
     * @throws MalformedURLException if URL is not valid.
     */
    private static Channel convertFeed(SyndFeed aFeed, URL aFeedURL)
        throws MalformedURLException
    {
        Channel channel = new Channel();
        channel.setAuthor(aFeed.getAuthor());
        channel.setDescription(aFeed.getDescription());
        channel.setFormat(aFeed.getFeedType());
        channel.setLanguage(aFeed.getLanguage());
        channel.setSiteURL(StringUtils.isEmpty(aFeed.getLink()) ? null
            : new URL(aFeedURL, StringUtils.fixURL(aFeed.getLink())));
        channel.setTitle(aFeed.getTitle());

        long period = getUpdatePeriod(aFeed);
        if (period != -1)
        {
            int updateFrequency = getUpdateFrequency(aFeed);
            if (updateFrequency > 1) period = period / updateFrequency;
        }

        channel.setUpdatePeriod(period);
        return channel;
    }

    /**
     * Returns update frequency of the feed in times.
     *
     * @param aFeed feed.
     *
     * @return frequency.
     */
    private static int getUpdateFrequency(SyndFeed aFeed)
    {
        SyModule module = (SyModule)aFeed.getModule(SyModule.URI);

        return module == null ? -1 : module.getUpdateFrequency();
    }

    /**
     * Returns update period in milliseconds.
     *
     * @param aFeed feed.
     *
     * @return period in ms or <code>-1</code> if not specified.
     */
    private static long getUpdatePeriod(SyndFeed aFeed)
    {
        SyModule module = (SyModule)aFeed.getModule(SyModule.URI);

        return module == null ? -1 : periodToValue(module.getUpdatePeriod());
    }

        /**
     * Converts the name of period to corresponding value.
     *
     * @param periodName period name.
     *
     * @return value in ms or -1 if period name isn't known or NULL.
     */
    private static long periodToValue(String periodName)
    {
        long period = -1;

        if (SyModule.YEARLY.equalsIgnoreCase(periodName))
        {
            period = Constants.MILLIS_IN_YEAR;
        } else if (SyModule.MONTHLY.equalsIgnoreCase(periodName))
        {
            period = Constants.MILLIS_IN_MONTH;
        } else if (SyModule.WEEKLY.equalsIgnoreCase(periodName))
        {
            period = Constants.MILLIS_IN_WEEK;
        } else if (SyModule.HOURLY.equalsIgnoreCase(periodName))
        {
            period = Constants.MILLIS_IN_HOUR;
        } else if (SyModule.DAILY.equalsIgnoreCase(periodName))
        {
            period = Constants.MILLIS_IN_DAY;
        }

        return period;
    }

    /**
     * Converts item object into internal item format.
     *
     * @param anEntry   source item object.
     * @param aFeedURL  root URL of a feed for the relative links resolution.
     *
     * @return internal object.
     */
    private static Item convertItem(SyndEntry anEntry, URL aFeedURL)
    {
        String text = getEntryText(anEntry);
        String title = anEntry.getTitle();
        if (title != null && title.equals("<No Title>")) title = null;

        // Append enclosure to the end of the article
        List enclosures = anEntry.getEnclosures();
        if (enclosures != null && enclosures.size() > 0)
        {
            for (Object en : enclosures)
            {
                SyndEnclosure enclosure = (SyndEnclosure)en;
                String location = enclosure.getUrl();
                if (location != null)
                {
                    long length = enclosure.getLength();
                    text += formatEnclosure(location, length);
                }
            }
        } else
        {
            // Scan links list for possible enclosures.
            // Note: We do this in "else" block because the method is
            // not very reliable and if there are explicit enclosures
            // mention, we'd better not do this.

            List links = anEntry.getLinks();
            if (links != null) for (Object lnk : links)
            {
                SyndLink link = (SyndLink)lnk;

                String rel = link.getRel();
                long length = link.getLength();
                String location = link.getHref();

                if (length > 0 &&
                    (StringUtils.isEmpty(rel) || "enclosure".equalsIgnoreCase(rel)) &&
                    StringUtils.isNotEmpty(location))
                {
                    text += formatEnclosure(location, length);
                }
            }
        }

        Item item = new Item(text);
        item.setAuthor(anEntry.getAuthor());

        URL itemLink;
        try
        {
            String link = anEntry.getLink();
            itemLink = link == null ? null : new URL(aFeedURL, link);
        } catch (MalformedURLException e)
        {
            itemLink = null;
        }
        item.setLink(itemLink);
        item.setPublicationDate(anEntry.getPublishedDate());
        if (item.getPublicationDate() == null) item.setPublicationDate(anEntry.getUpdatedDate());
        item.setTitle(title);

        // URI
        item.setUri(anEntry.getUri());

        // Use subject or categories as subject
        String subject = null;
        List<SyndCategory> categories = (List<SyndCategory>)anEntry.getCategories();
        if (categories != null && !categories.isEmpty())
        {
            List<String> catsStr = new ArrayList<String>();
            for (SyndCategory category : categories)
            {
                String name = category.getName();
                if (StringUtils.isNotEmpty(name)) catsStr.add(name);
            }

            subject = StringUtils.join(catsStr.iterator(), " ");
        } else
        {
            DCModule dc = (DCModule)anEntry.getModule(DCModule.URI);
            if (dc != null)
            {
                DCSubject dcSubject = dc.getSubject();
                if (dcSubject != null) subject = dcSubject.getValue();
            }
        }
        item.setSubject(subject);

        return item;
    }

    /**
     * Formats an enclosure URL and length for inclusion in the article text.
     *
     * @param location  location.
     * @param length    length in bytes.
     *
     * @return string.
     */
    public static String formatEnclosure(String location, long length)
    {
        String[] linkComponents = location.split("/");
        String filename = linkComponents[linkComponents.length - 1];

        return "<p id=\"bbenclosure\">" +
            "<b>" + Strings.message("feed.parser.enclosure") + "</b> <a href='" + location + "'>" +
            filename + "</a>" + (length > 0 ? " (" + StringUtils.sizeToString(length) + ")" : "") +
            "</p>";
    }

    /**
     * Returns the text of an entry.
     *
     * @param anEntry entry.
     *
     * @return text.
     */
    private static String getEntryText(SyndEntry anEntry)
    {
        String text = null;

        // Check if the RSS/RDF content module is present
        Module module = anEntry.getModule(ContentModule.URI);
        if (module != null)
        {
            ContentModule cmod = (ContentModule)module;
            List encodeds = cmod.getEncodeds();
            if (encodeds != null && encodeds.size() > 0)
            {
                text = (String)encodeds.get(0);
            }
        }

        // If there was no content module, check various content types (Atom)
        if (text == null)
        {
            int type = Integer.MAX_VALUE;
            SyndContent content = null;

            // Select the best content of all available
            List<SyndContent> contents = (List<SyndContent>)anEntry.getContents();
            if (contents != null)
            {
                for (SyndContent cont : contents)
                {
                    int contType = getContentType(cont.getType());
                    if (contType < type)
                    {
                        type = contType;
                        content = cont;
                    }
                }
            }

            if (content == null) content = anEntry.getDescription();

            if (content != null)
            {
                String value = content.getValue();

                // For some mysterious reason Rome doesn't unescape the HTML and
                // Text/HTML content. Do so if necessary.
// Commented out as it seems Rome 0.9 started to unescape feeds.
//                if (TYPE_HTML.equals(content.getType()) ||
//                    TYPE_TEXT_HTML.equals(content.getType()))
//                {
//                    value = StringUtils.quickUnescape(value);
//                }

                text = value;
            }
        }

        // Check DC module
        if (StringUtils.isEmpty(text))
        {
            DCModule dcModule = (DCModule)anEntry.getModule(DCModule.URI);
            if (dcModule != null) text = dcModule.getDescription();
        }

        if (StringUtils.isEmpty(text)) text = Strings.message("feed.parser.no.text");

        return text;
    }

    /**
     * Returns content type preference order.
     *
     * @param contentType type.
     *
     * @return order (the lower, the more preferred).
     */
    private static int getContentType(String contentType)
    {
        return contentType == null ? -1
            : CONTENT_TYPE_PREFERENCE.indexOf(contentType.toLowerCase());
    }


    /**
     * Listener of permanent redirections notifications. Once the notification comes
     * the listener records new URL in the associated result object.
     */
    private static class RedirectionRecorder implements IPermanentRedirectionListener
    {
        private FeedParserResult result;

        /**
         * Creates redirection recorder for a given result object.
         *
         * @param aResult result object.
         */
        public RedirectionRecorder(FeedParserResult aResult)
        {
            result = aResult;
        }

        /**
         * Invoked when redirection detected.
         *
         * @param newLocation new location.
         */
        public void redirectedTo(URL newLocation)
        {
            result.setRedirectionURL(newLocation);
        }
    }
}
