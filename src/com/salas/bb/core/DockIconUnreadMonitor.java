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

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;
import com.salas.bb.domain.*;
import com.salas.bb.domain.events.FeedRemovedEvent;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.domain.utils.DomainAdapter;
import com.salas.bb.utils.osx.DockIcon;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;

/**
 * Monitors unread counts and updates dock icon with badge.
 */
public class DockIconUnreadMonitor extends DomainAdapter
        implements PropertyChangeListener, IDisplayModeManagerListener
{
    /** Show no badge. */
    public static final int MODE_OFF = 0;
    /** Show the number of unread articles. */
    public static final int MODE_ARTICLES = 1;
    /** Show the number of unread feeds. */
    public static final int MODE_FEEDS = 2;

    // Current mode
    private int mode = MODE_ARTICLES;

    private GuidesSet set;

    /**
     * We use this boolean to avoid scheduling more updates when the previous one isn't
     * performed yet. It's good to know to avoid excessive database usage.
     */
    private SynchronizedBoolean dockIconUpdateArmed = new SynchronizedBoolean(false);
    private Monitor monitor = new Monitor();

    /**
     * Registers guides set to operate.
     *
     * @param set set.
     */
    public void setSet(GuidesSet set)
    {
        this.set = set;
        update();
    }

    /**
     * Gets mode of operation.
     *
     * @return mode of operation.
     *
     * @see #MODE_OFF
     * @see #MODE_ARTICLES
     * @see #MODE_FEEDS
     */
    public int getMode()
    {
        return mode;
    }

    /**
     * Sets mode of operation.
     *
     * @param mode mode of operation.
     *
     * @see #MODE_OFF
     * @see #MODE_ARTICLES
     * @see #MODE_FEEDS
     */
    public void setMode(int mode)
    {
        if (this.mode != mode)
        {
            this.mode = mode;
            update();
        }
    }

    /**
     * Invoked when the property of the feed has been changed.
     *
     * @param feed     feed.
     * @param property property of the feed.
     * @param oldValue old property value.
     * @param newValue new property value.
     */
    public void propertyChanged(IFeed feed, String property, Object oldValue, Object newValue)
    {
        if (IFeed.PROP_UNREAD_ARTICLES_COUNT.equals(property))
        {
            int oldv = ((Integer)oldValue).intValue();
            int newv = ((Integer)newValue).intValue();

            if (mode == MODE_ARTICLES || (mode == MODE_FEEDS && (oldv == 0 || newv == 0)))
            {
                update();
            }
        }
    }

    /**
     * Invoked when the feed has been removed from the guide.
     *
     * @param event feed removal event.
     */
    public void feedRemoved(FeedRemovedEvent event)
    {
        if (event.isLastEvent() && mode != MODE_OFF) update();
    }


    /**
     * Invoked when the feed has been removed from the guide.
     *
     * @param guide parent guide.
     * @param feed  removed feed.
     * @param index index of removed feed.
     */
    public void feedRemoved(IGuide guide, IFeed feed, int index)
    {
        if (mode != MODE_OFF) update();
    }

    /**
     * Invoked when user preferences property changes.
     *
     * @param evt event.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (!UserPreferences.FEED_VISIBILITY_PROPERTIES.contains(evt.getPropertyName())) return;

        // User preferences will affect unread counts, if the starz filter changes
        // or if feeds below the threshhold are changed to show or hide.
        // As a simple and conservative response, just repaint the guide list
        // entirely, and update the unread button.
        update();
    }

    /**
     * Invoked when the color of feed changes.
     *
     * @param feedClass feed class.
     * @param oldColor  old color.
     * @param newColor  new color.
     */
    public void onClassColorChanged(int feedClass, Color oldColor, Color newColor)
    {
        // Changes to feed display colors will affect unread counts if the
        // "hidden" color option is used to hide or show feeds.
        // For simplicity, just repaint all the guides entirely and update the
        // unread button.
        if (oldColor == null || newColor == null) update();
    }

    /**
     * Updates the badge. Turns / on and off too.
     */
    public synchronized void update()
    {
        // There's no need to schedule more updates if the previous one didn't happen yet
        if (dockIconUpdateArmed.get()) return;

        // Arm and call
        dockIconUpdateArmed.set(true);
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                // Disarm to let the next update be scheduled
                dockIconUpdateArmed.set(false);

                // Update the badge
                int unread = getUnreadItemsCount();
                DockIcon.setBadgeCounter(unread);
            }
        });
    }

    /**
     * Gets the number of unread items (0 in <code>MODE_OFF</code>).
     *
     * @return the number of unread items.
     */
    private int getUnreadItemsCount()
    {
        int unread = 0;

        if (set != null)
        {
            StandardGuide[] guides = set.getStandardGuides(null);
            HashSet checkedFeeds = new HashSet();
            for (int i = 0; i < guides.length; i++)
            {
                StandardGuide guide = guides[i];
                IFeed[] feeds = GlobalModel.SINGLETON.getVisibleFeeds(guide);

                for (int j = 0; j < feeds.length; j++)
                {
                    IFeed feed = feeds[j];
                    if (checkedFeeds.contains(feed)) continue;

                    checkedFeeds.add(feed);
                    if (mode == MODE_ARTICLES && (feed instanceof DataFeed))
                    {
                        unread += feed.getUnreadArticlesCount();
                    } else if (mode == MODE_FEEDS && !feed.isRead())
                    {
                        unread++;
                    }
                }
            }
        }

        return unread;
    }

    /**
     * Returns controller listener.
     *
     * @return controller listener.
     */
    public IControllerListener getMonitor()
    {
        return monitor;
    }

    /**
     * Updates the badge when unread data feed deselection is detected. The
     * deselected feed can potentially become invisible and it's necessary to check.
     */
    private class Monitor extends UnreadDataFeedDeselectionMonitor
    {
        /**
         * Invoked when unread data feed deselection detected.
         */
        protected void unreadDataFeedDeselected()
        {
            // We call it in the other EDT event to let the view recalculate itself
            // before asking for visibility status
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    update();
                }
            });
        }
    }
}
