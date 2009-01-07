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
// $Id: INetTaskGroupListener.java,v 1.2 2006/01/08 04:53:44 kyank Exp $
//

package com.salas.bb.networking.manager;

import java.util.EventListener;

/**
 * Listener of events within task group.
 */
public interface INetTaskGroupListener extends EventListener
{
    /**
     * Fired when new task is added to the group.
     *
     * @param group group to which the task was added.
     * @param task  added task.
     */
    void taskAdded(NetTaskGroup group, NetTask task);

    /**
     * Fired when task is removed from the group.
     *
     * @param group group the task was removed from.
     * @param task  removed task.
     * @param index index of removed task.
     */
    void taskRemoved(NetTaskGroup group, NetTask task, int index);
}
