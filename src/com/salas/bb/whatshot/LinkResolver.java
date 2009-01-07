// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2007 by R. Pito Salas
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
// $Id: LinkResolver.java,v 1.13 2007/11/02 12:32:20 spyromus Exp $
//

package com.salas.bb.whatshot;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.concurrency.ExecutorFactory;
import com.salas.bb.utils.concurrency.NamingThreadFactory;
import org.apache.commons.collections.ReferenceMap;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Link resolver contacts servers to learn titles of the links it's given.
 * 
 */
public class LinkResolver
{
    /** Default scan limit -- number of bytes to read from the stream looking for the title tag before giving up. */
    private static final int DEFAULT_SCAN_LIMIT = 2000;

    /** The map of patterns to replacement strings for the post-processing of titles. */
    private static Map<Pattern, String> postProcessingInstructions = new LinkedHashMap<Pattern, String>();

    /**
     * The map of patterns to the scan limits. The default limit is 2K which means that 2Kb of a resource
     * will be fetched (max) to find the TITLE tag. You can adjust it with this map.
     */
    private static Map<Pattern, Integer> customScanLimits = new LinkedHashMap<Pattern, Integer>();

    /** The list of custom special link resolvers. */
    private static List<ICustomLinkResolver> customLinkResolvers = new LinkedList<ICustomLinkResolver>();

    /** Cache of resolutions. It's memory-sensitive. */
    private static final ReferenceMap CACHE = new ReferenceMap();

    /**
     * Resolution executor.
     */
    private Executor executor;

    /**
     * Listener for the resolution events.
     */
    private final ILinkResolverListener listener;

    static {
        addPostProcessingInstruction(Pattern.compile("^([^:]+):\\s+(.+)\\s+on\\s+technorati", Pattern.CASE_INSENSITIVE),
            "Technorati tag: $1 ($2)");

        addCustomScanLimits(Pattern.compile("^http://(www\\.)?amazon\\.com", Pattern.CASE_INSENSITIVE), 20000);
    }

    /**
     * Creates a link resolver for a given listener.
     *
     * @param listener listner.
     *
     * @throws IllegalArgumentException if listener is <code>NULL</code>.
     */
    public LinkResolver(ILinkResolverListener listener)
    {
        if (listener == null) throw new IllegalArgumentException("Listener can't be NULL");
        
        this.listener = listener;

        executor = ExecutorFactory.createPooledExecutor(new NamingThreadFactory("Link Resolver", Thread.MIN_PRIORITY),
                5, 1000);
    }

    /**
     * Stops link resolution immediately.
     */
    public void stop()
    {
        // Shutdown immediately and don't care about the unprocessed results
        executor = null;
    }

    /**
     * Returns the title of the link in the group or, schedules the
     * resolution and returns the link text.
     *
     * @param group group to resolveURI link for.
     *
     * @return resolved text or link itself.
     */
    public synchronized String resolve(HotResultGroup group)
    {
        // Check local cache
        String title = getFromCache(group);

        // Schedule the task if not in the cache
        if (title == null)
        {
            title = group.getName();
            try
            {
                executor.execute(new ResolutionTask(group));
            } catch (InterruptedException e)
            {
                // Failed to schedule
                e.printStackTrace();
            }
        }

        return title;
    }

    /**
     * Checks if the title for this group is in the cache.
     *
     * @param group group.
     *
     * @return title.
     */
    private String getFromCache(HotResultGroup group)
    {
        return (String)CACHE.get(group.getLink().toString());
    }

    /**
     * Performs the post-processing of the title resolved.
     *
     * @param title title.
     *
     * @return processed title.
     */
    static String postprocessTitle(String title)
    {
        if (title == null || StringUtils.isEmpty(title)) return title;

        for (Map.Entry<Pattern, String> entry : postProcessingInstructions.entrySet())
        {
            title = entry.getKey().matcher(title).replaceAll(entry.getValue());
        }

        return title;
    }

    /**
     * Removes all instructions.
     */
    static void clearPostProcessingInstructions()
    {
        postProcessingInstructions.clear();
    }

    /**
     * Adds a post-processing instruction to the tail of the instructions list.
     *
     * @param matchPattern  pattern to match in the title.
     * @param replacement   replacement to make.
     */
    public static void addPostProcessingInstruction(Pattern matchPattern, String replacement)
    {
        postProcessingInstructions.put(matchPattern, replacement);
    }

    /**
     * Checks if a given URL requires some special treatment.
     *
     * @param url link to check.
     *
     * @return title or <code>NULL</code> if to follow usual procedures.
     */
    static String customLinkResolution(URL url)
    {
        if (url == null) return null;

        String title = null;

        for (ICustomLinkResolver resolver : customLinkResolvers)
        {
            title = resolver.resolve(url);
            if (title != null) break;
        }

        return title;
    }

    /**
     * Clears the list of custom link resolvers.
     */
    static void clearCustomLinkResolvers()
    {
        customLinkResolvers.clear();
    }

    /**
     * Adds a custom link resolver to the end of the list.
     *
     * @param resolver resolver.
     */
    public static void addCustomLinkResolver(ICustomLinkResolver resolver)
    {
        customLinkResolvers.add(resolver);
    }

    /**
     * Adds new pattern for the URL recognition and the limit for the TITLE tag scanning procedure.
     *
     * @param pattern   parrent.
     * @param limit     scan limit in bytes.
     */
    public static void addCustomScanLimits(Pattern pattern, int limit)
    {
        customScanLimits.put(pattern, limit);
    }

    /**
     * Returns a scan limit for a link.
     *
     * @param link link.
     *
     * @return limit.
     */
    private static int getScanLimit(URL link)
    {
        int limit = DEFAULT_SCAN_LIMIT;

        if (link != null)
        {
            String ls = link.toString();
            Set<Map.Entry<Pattern,Integer>> entries = customScanLimits.entrySet();
            for (Map.Entry<Pattern, Integer> entry : entries)
            {
                if (entry.getKey().matcher(ls).find()) return entry.getValue();
            }
        }

        return limit;
    }

    /**
     * Fetches the title from the stream until finds the '&lt;' or
     * the end.
     *
     * @param is    input stream.
     *
     * @return title.
     *
     * @throws IOException in case of I/O error.
     */
    String fetchTitle(InputStream is) throws IOException
    {
        int ch;

        // Found the title tag and the text
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        while ((ch = is.read()) != -1 && ch != '<')
        {
            buf.write(ch);
            if (isTerminated()) return null;
        }

        return buf.toString().trim();
    }

    /**
     * Returns <code>TRUE</code> if executor is no longer workin.
     *
     * @return <code>TRUE</code> if terminated.
     */
    private boolean isTerminated()
    {
        return executor == null;
    }

    /**
     * Resolves a single link into the title.
     */
    private class ResolutionTask implements Runnable
    {
        private final HotResultGroup group;
        private String tag = "<title>";
        private int pos = 0;

        /**
         * Creates a resolver task.
         *
         * @param group group to resolveURI the title for.
         */
        public ResolutionTask(HotResultGroup group)
        {
            this.group = group;
        }

        /**
         * Main task method.
         */
        public void run()
        {
            if (isTerminated()) return;

            try
            {
                if (resolve(group)) listener.onGroupResolved(group);
            } catch (IOException e)
            {
                // Fall through
            }
        }

        /**
         * Invoked to resolveURI the hotlink into the title for this group item.
         *
         * @param group group to resolveURI.
         *
         * @return <code>TRUE</code> if the title was resolved and changed.
         *
         * @throws IOException in case of any I/O errors.
         */
        private boolean resolve(HotResultGroup group)
            throws IOException
        {
            String title;

            // Don't resolveURI invisible groups
            // When they become visible, they will be resolved
            if (!group.isVisible()) return false;

            // Check if the link needs some special treatment.
            URL url = group.getLink();
            title = customLinkResolution(url);

            if (title == null)
            {
                URLConnection con = url.openConnection();
                String contentType = con.getContentType();
                InputStream is = null;

                int max = getScanLimit(url);

                try
                {
                    // Content type
                    if (contentType != null && contentType.startsWith("text/html"))
                    {
                        is = new BufferedInputStream(con.getInputStream());

                        title = resolveFromStream(is, max);
                    }

                    // Sets the title of the page
                    if (title != null) title = StringUtils.unescape(title);
                } finally
                {
                    if (is != null) is.close();
                }
            }

            // Process the title to replace some parts or do any other post-processing
            if (title != null) title = postprocessTitle(title);
            if (StringUtils.isEmpty(title)) title = "[Unresolved] " + url.toString();

            // Remember the resolution in the cache
            CACHE.put(url.toString(), title);
            group.setResolvedTitle(title);

            return true;
        }

        /**
         * Resolves a title from stream.
         *
         * @param is    stream.
         * @param max   maximum characters to load.
         *
         * @return title or <code>NULL</code> if not found.
         *
         * @throws IOException if I/O error happens.
         */
        String resolveFromStream(InputStream is, int max) throws IOException
        {
            String title = null;
            int i = 0;
            int b;
            while (title == null && !isTerminated() && i++ < max && (b = is.read()) != -1) title = resolveChar(b, is);

            return title;
        }

        /**
         * Resolves a character and moves on. Returns a title if recognized.
         *
         * @param b     byte from the stream.
         * @param is    input stream.
         *
         * @return title.
         *
         * @throws IOException if I/O exception happens.
         */
        String resolveChar(int b, InputStream is)
                throws IOException
        {
            if (pos < tag.length())
            {
                // Skip whitespace
                if (b == ' ' || b == '\n' || b == '\r' || b == '\t') return null;
                // Lowercase (but not < or >)
                if (b != '<' && b != '>' && b < 'a') b += ' ';

                // Check against the pattern
                char ch = tag.charAt(pos);
                if (ch != b) pos = 0;
                if (ch == b) pos++;
            }

            return (pos == tag.length()) ? fetchTitle(is) : null;
        }
    }
}
