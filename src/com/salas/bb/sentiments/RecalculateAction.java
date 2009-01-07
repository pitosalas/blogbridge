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
// $Id: RecalculateAction.java,v 1.5 2008/02/28 10:50:24 spyromus Exp $
//

package com.salas.bb.sentiments;

import com.jgoodies.uif.AbstractDialog;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.core.SearchFeedsManager;
import com.salas.bb.domain.*;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.swingworker.SwingWorker;
import com.salas.bb.utils.uif.ProgressPanel;
import com.salas.bb.utils.uif.UifUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Recalculates all sentiments showing a nice progress dialog.
 */
public class RecalculateAction extends AbstractAction
{
    private static RecalculateAction instance;

    /** Hidden singleton constructor. */
    private RecalculateAction()
    {
    }

    /**
     * Returns an instance.
     *
     * @return instance.
     */
    public static synchronized RecalculateAction getInstance()
    {
        if (instance == null) instance = new RecalculateAction();
        return instance;
    }

    /**
     * Activates an action.
     *
     * @param onlyConnotation TRUE if expressions didn't change and we need to update only connotations.
     *
     * @throws IllegalStateException if called not from within EDT.
     */
    public static void perform(boolean onlyConnotation)
    {
        if (!Calculator.getConfig().isEnabled()) return;

        if (!UifUtilities.isEDT()) throw new IllegalStateException("Not in EDT");
        getInstance().actionPerformed(new ActionEvent(instance, 1, Boolean.toString(onlyConnotation)));
    }

    /**
     * Invoked to activate this action.
     *
     * @param e action.
     */
    public void actionPerformed(ActionEvent e)
    {
        boolean onlyConnotation = Boolean.parseBoolean(e.getActionCommand());

        GuidesSet set = GlobalModel.SINGLETON.getGuidesSet();
        List<IFeed> feeds = set.getFeeds();

        // Create progress dialog
        ProgressDialog dialog = new ProgressDialog(GlobalController.SINGLETON.getMainFrame());

        // Create recalculator and subscribe the dialog to events
        Recalculator rec = new Recalculator(feeds, onlyConnotation);
        rec.addPropertyChangeListener(dialog);
        rec.execute();
    }

    /**
     * Progress dialog.
     */
    private class ProgressDialog extends AbstractDialog implements PropertyChangeListener
    {
        private static final long MAX_INVISIBLE_TIME = 100; // Stay invisible maximum for 2 seconds

        private final ProgressPanel pnlProgress;
        private long startedAt;

        /**
         * Creates a progress dialog.
         *
         * @param owner owner frame.
         */
        public ProgressDialog(Frame owner)
        {
            super(owner, Strings.message("sentiment.analysis"));

            pnlProgress = new ProgressPanel(Strings.message("scanning.articles"));
        }

        /**
         * Returns the content.
         *
         * @return content.
         */
        protected JComponent buildContent()
        {
            return pnlProgress;
        }

        /**
         * Invoked when the state or progress changes.
         *
         * @param evt event.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            String prop = evt.getPropertyName();
            if ("state".equals(prop)) onState((SwingWorker.StateValue)evt.getNewValue()); else
            if ("progress".equals(prop)) onProgress((Integer)evt.getNewValue());
        }

        /**
         * Invoked when state of the recalculation changes.
         *
         * @param state new state.
         */
        private void onState(SwingWorker.StateValue state)
        {
            switch (state)
            {
                case STARTED:
                    startedAt = System.currentTimeMillis();
                    break;
                case DONE:
                    // Hide dialog when done
                    close();
                    break;
            }
        }

        /**
         * Invoked when the scanner progress changes.
         *
         * @param progress [0 - 100].
         */
        private void onProgress(int progress)
        {
            long now = System.currentTimeMillis();
            if (now - startedAt > MAX_INVISIBLE_TIME) open();

            pnlProgress.setProgress(progress);
        }
    }

    /**
     * Recalculates feed article sentiments in the background.
     */
    private static class Recalculator extends SwingWorker<Long, Integer>
    {
        private static final double MAX_PERCENT = 100.0;
        private static final double SEARCH_FEED_PERCENT = 20.0;

        private final List<IFeed> feeds;
        private boolean onlyConnotation;

        /**
         * Creates recalculator.
         *
         * @param feeds             feeds.
         * @param onlyConnotation   TRUE to update only connotation information.
         */
        public Recalculator(List<IFeed> feeds, boolean onlyConnotation)
        {
            this.feeds = feeds;
            this.onlyConnotation = onlyConnotation;
        }

        /**
         * Calculate and report.
         *
         * @return total number of articles scanned.
         *
         * @throws Exception if anything fails.
         */
        protected Long doInBackground()
            throws Exception
        {
            double feedPercents = MAX_PERCENT;
            double searchFeedPercents = 0;

            // See if there are any smart feeds to update and adjust the percents
            List<SearchFeed> searchFeeds = SearchFeedsManager.findFeedsWithSentimentsClause();
            if (!searchFeeds.isEmpty())
            {
                searchFeedPercents = SEARCH_FEED_PERCENT;
                feedPercents -= searchFeedPercents;
            }

            long articles   = 0;
            int total       = feeds.size();
            double kf       = feedPercents / total;
            double ksf      = searchFeedPercents / Math.max(1, searchFeeds.size());

            // Update all feeds first
            int count = 0;
            for (IFeed feed : feeds)
            {
                articles += recalculate(feed);

                count++;
                setProgress((int)(kf * count));
            }

            // Update all search feeds now
            count = 0;
            for (SearchFeed feed : searchFeeds)
            {
                SearchFeedsManager.update(feed);

                count++;
                setProgress((int)(ksf * count + feedPercents));
            }

            return articles;
        }

        /**
         * Recalculate a feed.
         *
         * @param feed feed.
         *
         * @return number of processed articles.
         */
        private int recalculate(IFeed feed)
        {
            // Scan only data feeds that hold physical articles
            if (!(feed instanceof DataFeed)) return 0;

            int articles;

            synchronized (feed)
            {
                articles = feed.getArticlesCount();
                for (int i = 0; i < articles; i++)
                {
                    IArticle article = feed.getArticleAt(i);
                    if (onlyConnotation)
                    {
                        article.recalculateConnotation();
                    } else
                    {
                        article.recalculateSentimentCounts();
                    }
                }
            }

            return articles;
        }
    }

    public static void main(String[] args)
    {
        byte[] a = { 1,2,3 };
        byte[] b = { 1,2,3 };

        System.out.println(a.equals(b));
    }
}
