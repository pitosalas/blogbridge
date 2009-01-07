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
// $Id: ArticleMarker.java,v 1.47 2007/09/19 15:55:00 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.concurrency.ThreadExecutor;
import com.salas.bb.views.feeds.IFeedDisplayListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 * This class declares object which is intended to mark articles as read
 * under several conditions. Currently it supports three types of markings:
 * <ul>
 * <li>When user <b>leaves the channel</b> if <code>markReadWhenChangingChannels</code> property
 * set in user preferences.</li>
 * <li>When user <b>leaves the guide</b> and <code>markReadWhenChangingGuides</code> property
 * set in user preferences.</li>
 * <li>When <b>article selected more than number of seconds</b> specified by
 * <code>markReadAfterSeconds</code> property and when <code>markReadAfterDelay</code>
 * property is set in user preferences.</li>
 * </ul>
 *
 * There's an assumption that when changing guide, <code>channelSelected</code>
 * event always follows <code>guideSelected</code> event.
 */
public final class ArticleMarker extends ControllerAdapter
{
    private static final Logger LOG = Logger.getLogger(ArticleMarker.class.getName());
    private static final ArticleMarker INSTANCE = new ArticleMarker();

    private static final int DEFAULT_INTERVAL = 5;

    private IFeed       currentFeed;
    private IGuide      currentGuideForGuide;

    private Timer       timer;
    private MarkerTask  task;

    private int         markInterval;
    private boolean     intervalMarkingEnabled;

    private FeedDisplayListener    listener;

    /** Hidden singleton constructor. */
    private ArticleMarker()
    {
        listener = new FeedDisplayListener();

        timer = new Timer(true);
        markInterval = DEFAULT_INTERVAL;
        intervalMarkingEnabled = true;
    }

    /**
     * Returns instance of marker.
     *
     * @return instance.
     */
    public static ArticleMarker getInstance()
    {
        return INSTANCE;
    }

    /**
     * Returns listener of feed view.
     *
     * @return listener.
     */
    public FeedDisplayListener getFeedViewListener()
    {
        return listener;
    }

    /**
     * Invoked after application changes the channel.
     *
     * @param feed channel to which we are switching.
     */
    public void feedSelected(IFeed feed)
    {
        if (GlobalModel.SINGLETON.getUserPreferences().isMarkReadWhenChangingChannels())
        {
            final IFeed oldFeed = currentFeed;

            if (oldFeed != null && oldFeed != feed && oldFeed.getID() != -1)
            {
                MARK_EXECUTOR.execute(new MarkFeedAsReadOnSwitch(oldFeed));
            }
        }

        currentFeed = feed;
    }

    /**
     * Invoked after application changes the guide.
     *
     * @param guide guide to with we have switched.
     */
    public void guideSelected(final IGuide guide)
    {
        final IGuide oldGuide = currentGuideForGuide;

        UserPreferences prefs = GlobalModel.SINGLETON.getUserPreferences();
        boolean markReadWhenChangingGuides = prefs.isMarkReadWhenChangingGuides();
        if (markReadWhenChangingGuides && oldGuide != null && oldGuide != guide)
        {
            MARK_EXECUTOR.execute(new MarkGuideReadOnSwitch(oldGuide));
        }

        currentGuideForGuide = guide;
    }

    private static final Executor MARK_EXECUTOR = new ThreadExecutor("Article Marker", 5000);

    /**
     * Returns current mark interval in seconds.
     *
     * @return seconds.
     */
    public int getMarkInterval()
    {
        return markInterval;
    }

    /**
     * Sets new mark interval in seconds. Value becomes active on next article
     * selection.
     *
     * @param seconds interval in seconds.
     */
    public void setMarkInterval(int seconds)
    {
        this.markInterval = seconds;
    }

    /**
     * Returns TRUE if interval marking is currently enabled.
     *
     * @return TRUE if enabled.
     */
    public boolean isIntervalMarkingEnabled()
    {
        return intervalMarkingEnabled;
    }

    /**
     * Enables / disables interval marking.
     *
     * @param enabled TRUE to enable.
     */
    public void setIntervalMarkingEnabled(boolean enabled)
    {
        intervalMarkingEnabled = enabled;
    }

    /**
     * Constructs and returns preference listener which will update interval marking
     * settings in accordance with changes to specified preferences.
     *
     * @param flagPreferenceName          name of boolean flag preference.
     * @param intervalValuePreferenceName name of interval value preference.
     *
     * @return created and initialized preference change listener.
     */
    public PropertyChangeListener getPreferencesListener(String flagPreferenceName,
                                                         String intervalValuePreferenceName)
    {
        return new PreferencesListener(flagPreferenceName, intervalValuePreferenceName);
    }

    /**
     * Marker which marks currently selected article as read.
     */
    private static class MarkerTask extends TimerTask
    {
        private IArticle article;

        /**
         * Constructs task.
         *
         * @param aArticle article to mark.
         */
        public MarkerTask(IArticle aArticle)
        {
            article = aArticle;
        }

        /**
         * The action to be performed by this timer task.
         */
        public void run()
        {
            GlobalModel model = GlobalModel.SINGLETON;
            if (article == model.getSelectedArticle())
            {
                GlobalController.readArticles(true,
                    model.getSelectedGuide(),
                    model.getSelectedFeed(),
                    article);
            }
        }
    }

    /**
     * Listener for changes of preferences.
     */
    private class PreferencesListener implements PropertyChangeListener
    {
        private String flagPreferenceName = null;
        private String intervalValuePreferenceName = null;

        /**
         * Constructs listener.
         *
         * @param flagPreferenceName          name of boolean flag preference.
         * @param intervalValuePreferenceName name of interval value preference.
         */
        public PreferencesListener(String flagPreferenceName, String intervalValuePreferenceName)
        {
            this.flagPreferenceName = flagPreferenceName;
            this.intervalValuePreferenceName = intervalValuePreferenceName;
        }

        /**
         * This method gets called when a bound property is changed.
         *
         * @param evt A PropertyChangeEvent object describing the event source
         *            and the property that has changed.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            final String prop = evt.getPropertyName();
            if (flagPreferenceName != null && flagPreferenceName.equals(prop))
            {
                setIntervalMarkingEnabled((Boolean)evt.getNewValue());
            } else if (intervalValuePreferenceName != null &&
                    intervalValuePreferenceName.equals(prop))
            {
                setMarkInterval((Integer)evt.getNewValue());
            }
        }
    }

    /**
     * Thread that marks the feed as read on feed change.
     */
    private static class MarkFeedAsReadOnSwitch implements Runnable
    {
        private final IFeed feed;

        public MarkFeedAsReadOnSwitch(IFeed aFeed)
        {
            feed = aFeed;
        }

        public void run()
        {
            feed.setRead(true);
        }
    }

    /**
     * Marks guide as read on switch.
     */
    private static class MarkGuideReadOnSwitch implements Runnable
    {
        private final IGuide guide;

        public MarkGuideReadOnSwitch(IGuide aGuide)
        {
            guide = aGuide;
        }

        public void run()
        {
            guide.setRead(true);
        }
    }

    /**
     * Listens to the view and schedules marking of articles as read.
     */
    private class FeedDisplayListener implements IFeedDisplayListener
    {
        /**
         * Invoked when user selects article or article is selected as result of direct invocation
         * of {@link com.salas.bb.views.feeds.IFeedDisplay#selectArticle(com.salas.bb.domain.IArticle)}
         * method.
         *
         * @param lead              lead article.
         * @param selectedArticles  all selected articles.
         */
        public void articleSelected(IArticle lead, IArticle[] selectedArticles)
        {
            // TODO: group marking of articles as read when multiple selected?
            if (task != null) task.cancel();
            if (lead != null && intervalMarkingEnabled && !lead.isRead())
            {
                task = new MarkerTask(lead);
                timer.schedule(task, getMarkInterval() * Constants.MILLIS_IN_SECOND);
            }
        }

        /**
         * Invoked when user clicks on some link at the article text or header. The expected
         * behaviour is openning the link in browser.
         *
         * @param link link clicked.
         */
        public void linkClicked(URL link)
        {
        }

        /**
         * Invoked when user hovers some link with mouse pointer.
         *
         * @param link link hovered or <code>NULL</code> if previously hovered link is no longer
         *             hovered.
         */
        public void linkHovered(URL link)
        {
        }

        /**
         * Invoked when user clicks on some quick-link to the other feed.
         *
         * @param feed feed to select.
         */
        public void feedJumpLinkClicked(IFeed feed)
        {
        }

        /** Invoked when the user made something to zoom content in. */
        public void onZoomIn()
        {
        }

        /** Invoked when the user made something to zoom the content out. */
        public void onZoomOut()
        {
        }
    }
}
