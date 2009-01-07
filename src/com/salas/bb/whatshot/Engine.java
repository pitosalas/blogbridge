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
// $Id: Engine.java,v 1.9 2007/11/01 13:01:40 spyromus Exp $
//

package com.salas.bb.whatshot;

import com.salas.bb.domain.*;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.DateUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * The main engine of the What's Hot feature.
 */
public class Engine
{
    /** Max number of percents. */
    private static final double MAX_PERCENTS = 100.0;

    /** The list of filter criteria. */
    private static final List<IHotLinkCriteria> FILTER_CRITERIA = new LinkedList<IHotLinkCriteria>();

    /** Guides set to operate. */
    private final GuidesSet set;

    /**
     * Creates what's hot engine for the set.
     *
     * @param set set.
     */
    public Engine(GuidesSet set)
    {
        this.set = set;
    }

    /**
     * Scans the guides set.
     *
     * @return reults.
     */
    public List<HotLink> scan()
    {
        return scan(null);
    }

    /**
     * Scans the guides set.
     *
     * @param listener progress listener.
     *
     * @return reults.
     */
    public List<HotLink> scan(IProgressListener listener)
    {
        Result res = new Result();

        FeedsList fl = set.getFeedsList();
        List<IFeed> feeds = fl.getFeeds();
        double perc = MAX_PERCENTS / feeds.size();
        int i = 0;
        for (IFeed feed : feeds)
        {
            if (feed instanceof DataFeed) scan(feed, res);
            i++;
            if (listener != null) listener.onProgress((int)(perc * i));
        }

        return filter(res.getHotLinks());
    }

    private void scan(IFeed feed, Result result)
    {
        IArticle[] articles = feed.getArticles();
        for (IArticle article : articles)
        {
            Date pubdate = article.getPublicationDate();
            if (!DateUtils.olderThan(pubdate, Constants.MILLIS_IN_DAY * 7)) scan(article, result);
        }
    }

    private void scan(IArticle article, Result result)
    {
        Collection<String> links = article.getLinks();
        for (String link : links)
        {
            result.register(link, article);
        }
    }

    /**
     * Result has hot links.
     */
    public static class Result extends HashMap<String, HotLink>
    {
        /**
         * Registers an article within a hot link.
         *
         * @param urlS      URL.
         * @param article   article.
         */
        public void register(String urlS, IArticle article)
        {
            urlS = urlS.trim().toLowerCase();
            urlS = urlS.replaceAll("/+$", "");

            try
            {
                // Create a real URL using the base
                URL base = article.getLink();
                URL url = new URL(base, urlS);
                urlS = url.toString();

                // See if it's already there in the cache
                HotLink hl = get(urlS);
                if (hl == null)
                {
                    hl = new HotLink(url);
                    put(urlS, hl);
                }

                hl.add(article);
            } catch (MalformedURLException e)
            {
                // Skipping
            }
        }

        public List<HotLink> getHotLinks()
        {
            Collection<HotLink> hls = values();
            List<HotLink> hlsl = new ArrayList<HotLink>(hls);

            Collections.sort(hlsl);
            return hlsl;
        }
    }

    // ------------------------------------------------------------------------
    // Filtering
    // ------------------------------------------------------------------------

    /**
     * Clears a filter criteria.
     */
    static void clearFilterCriteria()
    {
        FILTER_CRITERIA.clear();
    }

    /**
     * Adds a filtering criteria.
     *
     * @param criteria criteria.
     */
    public static void addFilterCriteria(IHotLinkCriteria criteria)
    {
        FILTER_CRITERIA.add(criteria);
    }

    /**
     * Returns <code>TRUE</code> if the link matches any criteria.
     *
     * @param link link.
     *
     * @return <code>TRUE</code> if the link matches any criteria.
     */
    static boolean matchesFilters(HotLink link)
    {
        for (IHotLinkCriteria criteria : FILTER_CRITERIA)
        {
            if (criteria.matches(link)) return true;
        }

        return false;
    }

    /**
     * Removes all links that match the custom filters from the list.
     *
     * @param links links.
     *
     * @return updated list.
     */
    private List<HotLink> filter(List<HotLink> links)
    {
        List<HotLink> toRemove = new LinkedList<HotLink>();

        for (HotLink link : links)
        {
            if (matchesFilters(link)) toRemove.add(link);
        }

        links.removeAll(toRemove);

        return links;
    }

    // ------------------------------------------------------------------------
    // Supplementary classes
    // ------------------------------------------------------------------------

    /**
     * Hot link has articles.
     */
    public static class HotLink extends ArrayList<IArticle> implements Comparable
    {
        private final URL link;
        private String pageTitle;

        private int cumulativeFeedRating;
        private int numberOfFeedRatings;

        /**
         * Creates a hotlink object.
         *
         * @param link a link.
         */
        public HotLink(URL link)
        {
            this.link = link;
            cumulativeFeedRating = 0;
        }

        public URL getLink()
        {
            return link;
        }

        @Override
        public boolean add(IArticle article)
        {
            boolean added = false;

            IFeed feed = article.getFeed();
            if (!contains(article) && feed != null)
            {
                added = super.add(article);
                int rating = feed.getRating();
                if (rating > -1)
                {
                    cumulativeFeedRating += rating + 1;
                    numberOfFeedRatings++;
                }
            }

            return added;
        }

        /**
         * Sets the title of the corresponding page.
         *
         * @param pageTitle page title.
         */
        public void setPageTitle(String pageTitle)
        {
            this.pageTitle = pageTitle;
        }

        /**
         * Returns the title of the link to display.
         *
         * @return title.
         */
        public String getTitle()
        {
            return pageTitle == null ? link.toString() : pageTitle;
        }

        /**
         * Returns the average rating of feeds involved.
         *
         * @return average rating.
         */
        private double getAverateRating()
        {
            return numberOfFeedRatings == 0 ? 0 : cumulativeFeedRating / numberOfFeedRatings;
        }

        /**
         * Compares two hot link objects.
         *
         * @param o second object.
         *
         * @return <code>TRUE</code> if equal.
         */
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            HotLink hotLink = (HotLink)o;

            return link.toString().equals(hotLink.link.toString());
        }

        /**
         * Returns the hash code.
         *
         * @return code.
         */
        public int hashCode()
        {
            int result = super.hashCode();
            result = 31 * result + link.toString().hashCode();
            return result;
        }

        /**
         * Compares this object with the specified object for order.  Returns a negative integer, zero, or a positive
         * integer as this object is less than, equal to, or greater than the specified object.<p>
         *
         * @param o the Object to be compared.
         *
         * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
         *         the specified object.
         *
         * @throws ClassCastException if the specified object's type prevents it from being compared to this Object.
         */
        public int compareTo(Object o)
        {
            if (getClass() != o.getClass()) throw new ClassCastException();

            HotLink that = (HotLink)o;

            Integer thisCount = size();
            Integer thatCount = that.size();

            int res = thatCount.compareTo(thisCount);
            if (res == 0)
            {
                Double thisRating = getAverateRating();
                Double thatRating = that.getAverateRating();

                res = thatRating.compareTo(thisRating);

                if (res == 0) res = getLink().toString().compareToIgnoreCase(that.getLink().toString());
            }

            return res;
        }
    }
}
