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
// $Id: NetTaskGroup.java,v 1.8 2007/11/23 14:54:04 spyromus Exp $
//

package com.salas.bb.networking.manager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simple group of tasks.
 */
public class NetTaskGroup extends NetTask implements PropertyChangeListener, INetTaskGroupListener
{
    private final List<NetTask> subtasks;
    private final List<INetTaskGroupListener> ntgListeners;

    /**
     * Creates tasks group.
     *
     * @param title title of group.
     */
    public NetTaskGroup(String title)
    {
        super(title);

        subtasks = new ArrayList<NetTask>();
        ntgListeners = new CopyOnWriteArrayList<INetTaskGroupListener>();

        startTime = null;
    }

    /**
     * Adds task to group.
     *
     * @param task task.
     */
    public void addTask(NetTask task)
    {
        if (task == null) return;

        synchronized (subtasks)
        {
            if (!subtasks.contains(task) && subtasks.add(task))
            {
                // Make sure we have only one registration of ourselves
                task.removePropertyChangeListener(this);
                task.addPropertyChangeListener(this);

                if (task instanceof NetTaskGroup)
                {
                    ((NetTaskGroup)task).addListener(this);
                }

                task.setParent(this);

                fireTaskAdded(this, task);
            }
        }
    }

    /**
     * Removes task from group.
     *
     * @param task task.
     */
    public void removeTask(NetTask task)
    {
        if (task == null) return;

        synchronized (subtasks)
        {
            int index = indexOf(task);
            if (subtasks.remove(task))
            {
                task.setParent(null);
                task.removePropertyChangeListener(this);
                fireTaskRemoved(this, task, index);
            }
        }
    }

    /**
     * Returns number of tasks in this group.
     *
     * @return number of tasks.
     */
    public int getTaskCount()
    {
        return subtasks.size();
    }

    /**
     * Returns task by index.
     *
     * @param index index.
     *
     * @return task.
     */
    public NetTask getTask(int index)
    {
        return subtasks.get(index);
    }

    /**
     * Returns index of sub-task.
     *
     * @param aTask task.
     *
     * @return index.
     */
    public int indexOf(NetTask aTask)
    {
        return subtasks.indexOf(aTask);
    }

    /**
     * Returns task progress.
     *
     * @return progress.
     */
    public float getProgress()
    {
        float progress = 0;

        synchronized (subtasks)
        {
            int tasksNum = subtasks.size();
            for (int i = 0; i < tasksNum; i++)
            {
                NetTask task = subtasks.get(i);
                float taskProgress = task.getProgress();
                if ((int)taskProgress != -1) progress += taskProgress;
            }

            progress = tasksNum > 0 ? progress / tasksNum : -1;
        }

        return progress;
    }

    /**
     * Closes the stream.
     */
    public void abort()
    {
        synchronized (subtasks)
        {
            for (NetTask task : subtasks) task.abort();
        }
    }

    /**
     * Returns the status of task.
     *
     * @return status.
     */
    public int getStatus()
    {
        return -1;
    }

    /**
     * Pauses the stream.
     */
    public void pause()
    {
        synchronized (subtasks)
        {
            for (NetTask task : subtasks) task.pause();
        }
    }

    /**
     * Resumes the stream.
     */
    public void resume()
    {
        synchronized (subtasks)
        {
            for (NetTask task : subtasks) task.resume();
        }
    }

    /**
     * Adds a listener to the list.
     *
     * @param l listener.
     */
    public void addListener(INetTaskGroupListener l)
    {
        ntgListeners.add(l);
    }

    /**
     * Removes a listener from the list.
     *
     * @param l listener.
     */
    public void removeListener(INetTaskGroupListener l)
    {
        ntgListeners.remove(l);
    }

    /**
     * Fires event about new task added.
     *
     * @param group group owning the task.
     * @param task  added task.
     */
    protected void fireTaskAdded(NetTaskGroup group, NetTask task)
    {
        for (INetTaskGroupListener listener : ntgListeners) listener.taskAdded(group, task);
    }

    /**
     * Fires event about existing task removed.
     *
     * @param group group which owned that task.
     * @param task  removed task.
     * @param index index of the task within the group.
     */
    protected void fireTaskRemoved(NetTaskGroup group, NetTask task, int index)
    {
        for (INetTaskGroupListener listener : ntgListeners) listener.taskRemoved(group, task, index);
    }

    /**
     * This method gets called when a bound property is changed.
     *
     * @param evt A PropertyChangeEvent object describing the event source and the property that has
     *            changed.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        NetTask task = (NetTask)evt.getSource();

        // Simply send the event to higher level
        pcs.firePropertyChange(evt);

        if (task.getParent() == this)
        {
            // TODO: Think about calculating *correct* new value for group
            pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Chaning
    // ---------------------------------------------------------------------------------------------

    /**
     * Fired when new task is added to the group.
     *
     * @param group group to which the task was added.
     * @param task  added task.
     */
    public void taskAdded(NetTaskGroup group, NetTask task)
    {
        fireTaskAdded(group, task);
    }

    /**
     * Fired when task is removed from the group.
     *
     * @param group group the task was removed from.
     * @param task  removed task.
     * @param index index of removed task.
     */
    public void taskRemoved(NetTaskGroup group, NetTask task, int index)
    {
        fireTaskRemoved(group, task, index);
    }
}
