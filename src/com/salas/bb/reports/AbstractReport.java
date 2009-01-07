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
// $Id: AbstractReport.java,v 1.5 2008/04/01 12:51:27 spyromus Exp $
//

package com.salas.bb.reports;

import com.jgoodies.uifextras.util.PopupAdapter;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.reports.actions.BrowseFeed;
import com.salas.bb.reports.actions.DeleteFeed;
import com.salas.bb.reports.actions.DeleteGuide;
import com.salas.bb.reports.actions.MarkAsRead;
import com.salas.bb.utils.uif.LinkLabel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Map;

/**
 * Abstract report taking care of all little details.
 */
abstract class AbstractReport extends JPanel implements IReport
{
    /** Feed only. Post-to-blog action. */
    public static final String ACTION_POST_TO_BLOG = "post to blog";
    /** Guide and Feed. Mark as read. */
    public static final String ACTION_MARK_AS_READ = "mark as read";
    /** Guide and Feed. Mark as unread. */
    public static final String ACTION_MARK_AS_UNREAD = "mark as unread";
    /** Guide and Feed. Delete. */
    public static final String ACTION_DELETE = "delete";
    /** Feed. Open in browser. */
    public static final String ACTION_BROWSE = "browse";

    /** Flags if the data is already initialized. */
    private volatile boolean initialized;
    /** Flags if the layout is already performed. */
    private volatile boolean laidOut;

    /** Callback reports can use to report clicks. */
    protected IClickCallback clickCallback;

    /**
     * Sets a callback a report can use to report clicks on entities.
     *
     * @param callback callback.
     */
    public void setClickCallback(IClickCallback callback)
    {
        clickCallback = callback;
    }

    /**
     * Initializes data for the view.
     *
     * @param provider report data provider.
     */
    public final synchronized void initializeData(IReportDataProvider provider)
    {
        if (initialized) return;

        doInitializeData(provider);

        initialized = true;
    }

    /**
     * Returns <code>TRUE</code> when data is initialized.
     *
     * @return <code>TRUE</code> when data is initialized.
     */
    public boolean isDataInitialized()
    {
        return initialized;
    }

    /**
     * Initializes data for the view.
     *
     * @param provider report data provider.
     */
    protected abstract void doInitializeData(IReportDataProvider provider);

    /**
     * Prepares the view for the display. Should be called after the initializeData() method.
     *
     * @throws IllegalStateException if called when data is still not initialized.
     */
    public final synchronized void layoutView()
    {
        if (!initialized) throw new IllegalStateException("Not initialized by initializeData()");
        if (laidOut) return;

        doLayoutView();

        laidOut = true;
    }

    /**
     * Prepares the view for the display. Should be called after the initializeData() method.
     */
    protected abstract void doLayoutView();

    /**
     * Invoked when a users presses the reset button. If the report knows what to do, it can be done. Otherwise the
     * report data should be cleared and the initializeData() call expected.
     */
    public synchronized void reset()
    {
        initialized = false;
        laidOut = false;
    }

    @Override
    public String toString()
    {
        return getReportName();
    }

    /**
     * Returns the report page component.
     *
     * @return component.
     */
    public JComponent getReportPage()
    {
        return this;
    }

    /**
     * Returns the map of action names to actions for a given guide.
     *
     * @param guide guide.
     *
     * @return actions.
     */
    protected Map<String, Action> getGuideActionsMap(IGuide guide)
    {
        Map<String, Action> actions = new Hashtable<String, Action>();

        actions.put(ACTION_MARK_AS_READ, MarkAsRead.createForGuide(guide, true));
        actions.put(ACTION_MARK_AS_UNREAD, MarkAsRead.createForGuide(guide, false));
        actions.put(ACTION_DELETE, new DeleteGuide(guide));

        return actions;
    }

    /**
     * Returns the map of action names to actions for a given feed.
     *
     * @param feed feed.
     *
     * @return actions.
     */
    protected Map<String, Action> getFeedActionsMap(IFeed feed)
    {
        Map<String, Action> actions = new Hashtable<String, Action>();

        actions.put(ACTION_MARK_AS_READ, MarkAsRead.createForFeed(feed, true));
        actions.put(ACTION_MARK_AS_UNREAD, MarkAsRead.createForFeed(feed, false));
        actions.put(ACTION_DELETE, new DeleteFeed(feed, null));
        if (feed instanceof DirectFeed)
        {
            actions.put(ACTION_BROWSE, new BrowseFeed(feed));
        }

        return actions;
    }

    /**
     * Returns a guide by its ID or <code>NULL</code> if not found.
     *
     * @param id    guide ID.
     *
     * @return guide by its ID or <code>NULL</code> if not found.
     */
    protected IGuide getGuideById(long id)
    {
        if (id < 0) return null;

        GuidesSet set = GlobalModel.SINGLETON.getGuidesSet();
        return set.findGuideByID(id);
    }

    /**
     * Returns a feed by its ID or <code>NULL</code> if not found.
     *
     * @param id    feed ID.
     *
     * @return feed by its ID or <code>NULL</code> if not found.
     */
    protected IFeed getFeedById(long id)
    {
        if (id < 0) return null;

        GuidesSet set = GlobalModel.SINGLETON.getGuidesSet();
        return set.findFeedByID(id);
    }

    /**
     * Creates a clickable label for guide.
     *
     * @param guide guide.
     *
     * @return lable.
     */
    protected JLabel createGuideLabel(IGuide guide)
    {
        LinkLabel label = LinkLabel.create(guide.getTitle(), new GuideClickAction(guide, guide.getID()));
        label.addMouseListener(new GuidePopupAdapter(guide, -1));
        return label;
    }

    /**
     * Creates a clickable label for guide.
     *
     * @param id    id.
     * @param title title.
     *
     * @return lable.
     */
    protected JLabel createGuideLabel(long id, String title)
    {
        LinkLabel label = LinkLabel.create(title, new GuideClickAction(null, id));
        label.addMouseListener(new GuidePopupAdapter(null, id));
        return label;
    }

    /**
     * Creates a clickable label for feeds.
     *
     * @param feed feed.
     *
     * @return lable.
     */
    protected JLabel createFeedLabel(IFeed feed)
    {
        LinkLabel label = LinkLabel.create(feed.getTitle(), new FeedClickAction(feed, feed.getID()));
        label.addMouseListener(new FeedPopupAdapter(feed, -1));
        return label;
    }

    /**
     * Creates a clickable label for feeds.
     *
     * @param id    id.
     * @param title title.
     *
     * @return lable.
     */
    protected JLabel createFeedLabel(long id, String title)
    {
        LinkLabel label = LinkLabel.create(title, new FeedClickAction(null, id));
        label.addMouseListener(new FeedPopupAdapter(null, id));
        return label;
    }

    /** Feed click action that reports the event to the click callback. */
    private class FeedClickAction extends AbstractAction
    {
        /** Feed ID. */
        private final long id;
        /** Feed to select. */
        private final IFeed feed;

        /**
         * Creates an action. You can specify either of options. Feed takes prio.
         *
         * @param feed  feed to select.
         * @param id    feed id to select.
         */
        private FeedClickAction(IFeed feed, long id)
        {
            this.feed = feed;
            this.id = id;
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e event.
         */
        public void actionPerformed(ActionEvent e)
        {
            if (feed != null)
            {
                clickCallback.feedClicked(feed);
            } else if (id != -1)
            {
                clickCallback.feedClicked(id);
            }
        }
    }

    /** Guide click action that reports the event to the click callback. */
    protected class GuideClickAction extends AbstractAction
    {
        /** Guide ID. */
        private final long id;
        /** Guide to select. */
        private final IGuide guide;

        /**
         * Creates an action. You can specify either of options. Guide takes prio.
         *
         * @param guide guide to select.
         * @param id    guide id to select.
         */
        protected GuideClickAction(IGuide guide, long id)
        {
            this.guide = guide;
            this.id = id;
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e event.
         */
        public void actionPerformed(ActionEvent e)
        {
            if (guide != null)
            {
                clickCallback.guideClicked(guide);
            } else if (id != -1)
            {
                clickCallback.guideClicked(id);
            }
        }
    }

    /**
     * Guide popup adapter shows the popup menu when the gesture is detected.
     */
    protected class GuidePopupAdapter extends PopupAdapter
    {
        private IGuide guide;
        private long id;

        /**
         * Creates a popup adapter.
         *
         * @param guide guide.
         * @param id    guide ID.
         */
        public GuidePopupAdapter(IGuide guide, long id)
        {
            this.guide = guide;
            this.id = id;
        }

        /**
         * Builds a menu for the guide link label.
         *
         * @param event event.
         *
         * @return menu.
         */
        protected JPopupMenu buildPopupMenu(MouseEvent event)
        {
            JPopupMenu menu = new JPopupMenu();

            if (guide == null) guide = getGuideById(id);
            if (guide == null || guide.getID() == -1)
            {
                menu.add("Guide is gone");
            } else
            {
                Map<String, Action> actions = getActions();
                for (Action action : actions.values()) menu.add(action);
            }

            return menu;
        }

        /**
         * Returns the map of actions.
         *
         * @return actions.
         */
        protected Map<String, Action> getActions()
        {
            return getGuideActionsMap(guide);
        }
    }

    /**
     * Feed popup adapter shows the popup menu when the gesture is detected.
     */
    protected class FeedPopupAdapter extends PopupAdapter
    {
        private IFeed feed;
        private long id;

        /**
         * Creates a popup adapter.
         *
         * @param feed  feed.
         * @param id    feed ID.
         */
        public FeedPopupAdapter(IFeed feed, long id)
        {
            this.feed = feed;
            this.id = id;
        }

        /**
         * Builds a menu for the feed link label.
         *
         * @param event event.
         *
         * @return menu.
         */
        protected JPopupMenu buildPopupMenu(MouseEvent event)
        {
            JPopupMenu menu = new JPopupMenu();

            if (feed == null) feed = getFeedById(id);
            if (feed == null || feed.getID() == -1)
            {
                menu.add("Feed is gone");
            } else
            {
                Map<String, Action> actions = getFeedActionsMap(feed);
                for (Action action : actions.values()) menu.add(action);
            }

            return menu;
        }
    }
}
