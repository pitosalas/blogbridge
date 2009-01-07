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
// $Id $
//

package com.salas.bb.core;

import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.domain.*;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.domain.utils.DomainAdapter;
import com.salas.bb.utils.Sound;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.notification.NotificationArea;

import javax.swing.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Collects events, groups them and sends out notifications.
 */
public class EventsNotifier extends DomainAdapter
{
    private static final Logger LOG = Logger.getLogger(EventsNotifier.class.getName());

    /** Minimum time to pass after the last new feed/article evern to report the whole list. */
    private static final long MINIMUM_PAUSE = 10000L;
    /** The period between pause checks. */
    private static final long CHECK_PERIOD = MINIMUM_PAUSE / 5L;

    /** The name of the application. */
    private static final String APP_NAME = "BlogBridge";

    /** New feeds and articles event name. */
    private static final String EVENT_NEW_FEEDS_AND_ARTICLES = Strings.message("event.new.feeds.and.articles");

    /** The timer we use to schedule checks. */
    private final java.util.Timer timer;

    /** The frame we look after. If it's focused, we don't do anything. */
    private JFrame frame;

    /** The counter of new articles to be reported. */
    private int newArticles;
    /** The counter of new feeds across the reading lists to be reported. */
    private int newFeeds;
    /** The list of guides updated with new articles and feeds. */
    private java.util.List<String> updatedGuides;

    /** The time of last new feed/article event. */
    private long lastEventTime;

    /** Current timer task. */
    private CheckTimerTask task;

    /** User preferences. There will be no notifications without this object set. */
    private UserPreferences prefs;

    /** Resource ID for the new article sound. */
    private String soundResourceID;

    /**
     * Creates notifier.
     */
    public EventsNotifier()
    {
        String[] events = new String[] { EVENT_NEW_FEEDS_AND_ARTICLES };
        URL bigImage = null;
        try
        {
            bigImage = ResourceUtils.getURL(ResourceUtils.getString("application.64.icon"));
        } catch (NullPointerException e)
        {
            // We don't care about images we couldn't initialize
            LOG.warning(Strings.error("failed.to.load.image"));
        }

        NotificationArea.init(APP_NAME, events, bigImage);

        timer = new java.util.Timer();
        updatedGuides = new ArrayList<String>();
        soundResourceID = null;

        resetStats();
    }

    /**
     * Sets ID of the resource.
     *
     * @param resourceID resource.
     */
    public void setSoundResourceID(String resourceID)
    {
        this.soundResourceID = resourceID;
    }

    /**
     * Registers the frame we depend on.
     *
     * @param frame frame.
     */
    public void setFrame(JFrame frame)
    {
        this.frame = frame;
    }

    /**
     * Sets user preferences to use.
     *
     * @param prefs preferences.
     */
    public void setUserPreferences(UserPreferences prefs)
    {
        this.prefs = prefs;
    }

    /**
     * Invoked when new article has been added to the feed.
     *
     * @param feed    feed.
     * @param article article.
     */
    public void articleAdded(IFeed feed, IArticle article)
    {
        if (shouldNotify(feed))
        {
            synchronized (this)
            {
                newArticles++;
                updateGuidesList(feed);
                lastEventTime = System.currentTimeMillis();
                startTimer();
            }
        }
    }

    /**
     * Returns <code>TRUE</code> if events in this feed should be reported. The feed
     * can be in no guides -- no notifications. If the feed is in one or more guides
     * the notification should be sent if only there's at least one guide having
     * notifications allowed.
     *
     * @param feed feed to check.
     *
     * @return <code>TRUE</code> if should be reported.
     */
    private boolean shouldNotify(IFeed feed)
    {
        boolean should = false;

        if (prefs != null && prefs.isNotificationsEnabled() && !frame.isFocused() &&
            (!(feed instanceof DirectFeed) || !((DirectFeed)feed).isDisabled()))
        {
            IGuide[] parents = feed.getParentGuides();
            for (int i = 0; !should && i < parents.length; i++)
            {
                should = parents[i].isNotificationsAllowed();
            }
        }

        return should;
    }

    /**
     * Invoked when new feed has been added to the reading list.
     *
     * @param list reading list the feed was added to.
     * @param feed added feed.
     */
    public void feedAdded(ReadingList list, IFeed feed)
    {
        if (shouldNotify(list))
        {
            synchronized (this)
            {
                newFeeds++;
                updateGuidesList(feed);
                lastEventTime = System.currentTimeMillis();
                startTimer();
            }
        }
    }

    /**
     * Tells if the notification for new feed in the list should be sent or no.
     *
     * @param list list the new feed appeared in.
     *
     * @return <code>TRUE</code> if notification is necessary.
     */
    private boolean shouldNotify(ReadingList list)
    {
        boolean should = false;

        if (prefs != null && prefs.isNotificationsEnabled() && !frame.isFocused())
        {
            IGuide parent = list.getParentGuide();
            should = parent != null && parent.isNotificationsAllowed();
        }

        return should;
    }

    /**
     * Registers the guides updated as the result of the feed update.
     *
     * @param feed feed.
     */
    private void updateGuidesList(IFeed feed)
    {
        IGuide[] guides = feed.getParentGuides();
        for (IGuide guide : guides)
        {
            String title = guide.getTitle();
            if (!updatedGuides.contains(title)) updatedGuides.add(title);
        }
    }
    /**
     * Starts timer, which fires every {@link #CHECK_PERIOD} number of ms and
     * checks if {@link #MINIMUM_PAUSE} ms passed since the last event. If desired
     * time ellapsed, it fires message balloon through notification framework. If
     * there's nothing to report, the timer stops itself.
     */
    private void startTimer()
    {
        if (task == null)
        {
            task = new CheckTimerTask();
            timer.schedule(task, CHECK_PERIOD, CHECK_PERIOD);
        }
    }

    /**
     * Fires notification.
     */
    private void fireNotification()
    {
        if (prefs != null && prefs.isNotificationsEnabled())
        {
            String message = "";
            boolean articles = newArticles > 0;
            boolean feeds = newFeeds > 0;

            if (articles)
            {
                message += newArticles == 1
                    ? Strings.message("event.1.article")
                    : MessageFormat.format(Strings.message("event.n.articles"), newArticles);
                if (feeds) message += " " + Strings.message("event.and");
            }

            if (feeds) message += newFeeds == 1
                ? Strings.message("event.1.feed")
                : MessageFormat.format(Strings.message("event.n.feeds"), newFeeds);

            message += " ";
            message += (newArticles + newFeeds > 1)
                ? Strings.message("event.have.been.added.to")
                : Strings.message("event.has.been.added.to");
            message += " ";

            if (updatedGuides.size() == 1)
            {
                message += MessageFormat.format(Strings.message("event.guide.0"), updatedGuides.get(0));
            } else
            {
                message += MessageFormat.format(Strings.message("event.n.guides"),
                    updatedGuides.size());
            }

            NotificationArea.setAppIconTempVisible(true);
            NotificationArea.showMessage(EVENT_NEW_FEEDS_AND_ARTICLES, message);

            if (articles && soundResourceID != null && prefs.isSoundOnNewArticles()) Sound.play(soundResourceID);
        }

        resetStats();
    }

    /**
     * Resets counters and stats.
     */
    private void resetStats()
    {
        updatedGuides.clear();
        newArticles = 0;
        newFeeds = 0;
    }

    /**
     * Checker for events and firer of notifications.
     */
    private class CheckTimerTask extends TimerTask
    {
        private boolean isNothingToReport()
        {
            return newArticles == 0 && newFeeds == 0;
        }
        /**
         * The action to be performed by this timer task.
         */
        public void run()
        {
            synchronized (EventsNotifier.this)
            {
                if (!isNothingToReport() && System.currentTimeMillis() - lastEventTime >= MINIMUM_PAUSE)
                {
                    fireNotification();
                }

                if (isNothingToReport())
                {
                    task = null;
                    cancel();
                }
            }
        }
    }
}
