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
// $Id: NetManager.java,v 1.9 2007/11/23 14:48:40 spyromus Exp $
//

package com.salas.bb.networking.manager;

import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.net.URLInputStream;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Manager of network operations. If you have some <code>URLInputStream</code> you are using
 * for doing networking operations and you wish it to be displayed and possibly affected by user,
 * you can register it using <code>register(..)</code> method call. The task will be added to
 * the hierarcy of the networking tasks and removed from there upon completion.
 */
public final class NetManager
{
    public static final int TYPE_POLLING        = 0;
    public static final int TYPE_ARTICLE_IMAGE  = 1;
    public static final int TYPE_DOWNLOADS      = 2;

    private static final NetManager INSTANCE = new NetManager();

    private NetTaskGroup rootTG;
    private NetTaskGroup pollingTG;
    private NetTaskGroup imagesTG;
    private NetTaskGroup downloadsTG;

    /**
     * Hidden singleton constructor.
     */
    private NetManager()
    {
        rootTG = new NetTaskGroup(Strings.message("activity.task.all.tasks"));
        pollingTG = new NetTaskGroup(Strings.message("activity.task.polling.of.feeds"));
        imagesTG = new NetTaskGroup(Strings.message("activity.task.images"));
        downloadsTG = new NetTaskGroup(Strings.message("activity.task.downloads"));

        rootTG.addTask(pollingTG);
        rootTG.addTask(imagesTG);
        rootTG.addTask(downloadsTG);

        ManagingListener listener = new ManagingListener();
        rootTG.addPropertyChangeListener(listener);
    }

    /**
     * Returns the root group of tasks.
     *
     * @return the root group.
     */
    static NetTaskGroup getRootTasksGroup()
    {
        return INSTANCE.rootTG;
    }

    /**
     * Registers the stream within the hierarchy of networking tasks.
     *
     * @param type      type of the task which is going to be performed with the stream.
     * @param title     title of the task.
     * @param feed      title of the associated feed (if any).
     * @param stream    stream to register.
     *
     * @return the task handle registered.
     *
     * @see #TYPE_POLLING
     * @see #TYPE_ARTICLE_IMAGE
     */
    public static NetTask register(int type, String title, String feed, URLInputStream stream)
    {
        NetTaskGroup group = INSTANCE.groupByType(type);
        NetTask task = null;

        if (group != null)
        {
            task = new NetTask(title, feed, stream);
            group.addTask(task);
        }

        return task;
    }

    /**
     * Returns the number of downloads currently in progress.
     *
     * @return downloads count.
     */
    public static int getDownloadsCount()
    {
        NetTaskGroup group = INSTANCE.groupByType(TYPE_DOWNLOADS);
        return group == null ? 0 : group.getTaskCount();
    }

    /**
     * Finds appropriate group for a task of a given type.
     *
     * @param type  type of the task.
     *
     * @return the group.
     */
    private NetTaskGroup groupByType(int type)
    {
        NetTaskGroup group;

        switch (type)
        {
            case TYPE_POLLING:
                group = pollingTG;
                break;
            case TYPE_ARTICLE_IMAGE:
                group = imagesTG;
                break;
            case TYPE_DOWNLOADS:
                group = downloadsTG;
                break;
            default:
                group = null;
                break;
        }

        return group;
    }

    /**
     * Listens for changes in the tasks and manages the tree.
     *
     * For now this listener only removes finished (completed, errored and aborted)
     * tasks from hierarchy.
     */
    private static class ManagingListener implements PropertyChangeListener
    {
        /**
         * This method gets called when a bound property is changed.
         *
         * @param evt A PropertyChangeEvent object describing the event source and the property that has
         *            changed.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            String name = evt.getPropertyName();

            if ("status".equals(name))
            {
                final NetTask task = (NetTask)evt.getSource();
                int status = (Integer)evt.getNewValue();

                boolean finished = status == NetTask.STATUS_ABORTED ||
                    status == NetTask.STATUS_COMPLETED ||
                    status == NetTask.STATUS_ERRORED;

                if (finished && !(task instanceof NetTaskGroup))
                {
                    final NetTaskGroup parent = task.getParent();
                    if (parent != null)
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                parent.removeTask(task);
                            }
                        });
                    }
                }
            }
        }
    }
}
