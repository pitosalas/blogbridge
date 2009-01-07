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
// $Id: NoTrafficVisibleFeedsReport.java,v 1.4 2007/10/03 12:32:30 spyromus Exp $
//

package com.salas.bb.reports;

import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.search.ResultItemType;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.DateUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.UifUtilities;

import javax.swing.*;
import java.util.*;

/**
 * Report that provides the list of visible feeds that have a zero-traffic.
 */
class NoTrafficVisibleFeedsReport extends AbstractTableReport
{
    /** Report data. */
    private List<RFeed> data;

    /**
     * Returns the name of the report.
     *
     * @return name.
     */
    public String getReportName()
    {
        return Strings.message("report.visible.feeds.without.traffic");
    }

    /**
     * Initializes data for the view.
     *
     * @param provider report data provider.
     */
    protected void doInitializeData(IReportDataProvider provider)
    {
        // The threshold date is one week ago
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(DateUtils.getTodayTime() - Constants.MILLIS_IN_WEEK);
        Date threshold = cal.getTime();

        // Scan through all feeds and find some for the report
        data = new LinkedList<RFeed>();
        GuidesSet set = provider.getGuidesSet();
        List<IFeed> feeds = set.getFeeds();
        for (IFeed feed : feeds)
        {
            if (isEligible(feed, threshold)) data.add(new RFeed(feed));
        }

        // Sort feeds
        Collections.sort(data);
    }

    /**
     * Returns if feed is eligible for the report.
     *
     * @param feed      feed.
     * @param threshold date after which if an article is published, the feed is ineligible.
     *
     * @return <code>TRUE</code> if eligible.
     */
    static boolean isEligible(IFeed feed, Date threshold)
    {
        synchronized (feed)
        {
            // If not visible or having no articles, quickly return
            int cnt = feed.getArticlesCount();

            if (!feed.isVisible() || cnt == 0) return false;

            for (int i = 0; i < cnt; i++)
            {
                IArticle article = feed.getArticleAt(i);
                Date pubdate = article.getPublicationDate();

                // The feed is ineligible when there is an article published
                // after the threshold.
                if (pubdate != null && pubdate.after(threshold)) return false;
            }
        }

        return true;
    }

    /**
     * Creates a table for stats display.
     *
     * @param table table component to initialize.
     * @param max   maximum number of rows.
     *
     * @return table.
     */
    protected JPanel createDataTable(JPanel table, int max)
    {
        BBFormBuilder builder = new BBFormBuilder("16px, 2dlu, 50dlu:grow, 2dlu, center:p", table);
        builder.setDefaultDialogBorder();

        // Output header
        builder.append(UifUtilities.boldFont(new JLabel(Strings.message("report.feed"))), 3);
        builder.append(UifUtilities.boldFont(new JLabel(Strings.message("report.days"))));

        // Output data
        int i = 0;
        for (RFeed feed : data)
        {
            if (i++ == max) break;

            builder.append(new JLabel(ResultItemType.FEED.getIcon()));
            builder.append(createFeedLabel(feed.feed));
            builder.append(new JLabel(Integer.toString(feed.getDaysWithoutAPost())));
        }

        return builder.getPanel();
    }

    /**
     * Report data that can be sorted.
     */
    private static class RFeed implements Comparable<RFeed>
    {
        /** Feed. */
        private IFeed feed;
        /** Last article publication date. */
        private long lastPubDate;

        /**
         * Creates the report data record.
         *
         * @param feed feed.
         */
        private RFeed(IFeed feed)
        {
            this.feed = feed;
            lastPubDate = 0;

            synchronized (feed)
            {
                // Find the date of last publication
                int count = feed.getArticlesCount();
                for (int i = 0; i < count; i++)
                {
                    IArticle article = feed.getArticleAt(i);
                    Date date = article.getPublicationDate();
                    long pd = date == null ? 0 : date.getTime();
                    lastPubDate = Math.max(lastPubDate, pd);
                }
            }
        }

        /**
         * Returns the number of days since last publication.
         *
         * @return number of days.
         */
        public int getDaysWithoutAPost()
        {
            long diff = Math.max(0, DateUtils.getTodayTime() - lastPubDate);
            return (int)(diff / Constants.MILLIS_IN_DAY);
        }

        /**
         * Compares this object with the specified object for order.  Returns a negative integer, zero, or a positive
         * integer as this object is less than, equal to, or greater than the specified object.<p>
         *
         * @param o the Object to be compared.
         *
         * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
         *         the specified object.
         */
        public int compareTo(RFeed o)
        {
            return ((Long)lastPubDate).compareTo(o.lastPubDate);
        }
    }
}
