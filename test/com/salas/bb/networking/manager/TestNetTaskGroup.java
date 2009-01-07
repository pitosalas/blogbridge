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
// $Id: TestNetTaskGroup.java,v 1.4 2006/01/08 05:28:16 kyank Exp $
//

package com.salas.bb.networking.manager;

import junit.framework.TestCase;
import com.salas.bb.utils.net.URLInputStream;

import java.net.URL;
import java.beans.PropertyChangeListener;

/**
 * @see NetTaskGroup
 */
public class TestNetTaskGroup extends TestCase
{
    /**
     * Tests creation of group.
     */
    public void testCreationGroup()
    {
        NetTaskGroup group = new NetTaskGroup("A");

        assertNull(group.getFeed());
        assertNull(group.getParent());
        assertEquals(-1, (int)group.getProgress());
        assertEquals(-1, group.getSize());
        assertNull(group.getSourceURL());
        assertNull(group.getStartTime());
        assertEquals(-1, group.getStatus());
        assertEquals("A", group.getTitle());

        assertEquals("A", group.toString());
    }

    /**
     * Tests adding of NULL-tasks.
     */
    public void testAddTaskNull()
    {
        NetTaskGroup group = new NetTaskGroup("A");
        group.addTask(null);

        assertEquals(0, group.getTaskCount());
    }

    /**
     * Tests adding of tasks.
     */
    public void testAddTask()
    {
        NetTaskGroup group = new NetTaskGroup("A");
        NetTask task = new NetTask("A", "B", new URLInputStream(getTestURL()));

        group.addTask(task);

        assertTrue(task.getParent() == group);
        assertEquals(1, group.getTaskCount());
        assertTrue(task == group.getTask(0));

        PropertyChangeListener[] pcl = task.getPropertyChangeListeners();
        assertEquals(1, pcl.length);
        assertTrue(group == pcl[0]);

        // duplicate addition handling
        group.addTask(task);
        assertEquals(1, group.getTaskCount());
        assertEquals(1, task.getPropertyChangeListeners().length);
    }

    /**
     * Tests removing of tasks.
     */
    public void testRemoveTask()
    {
        NetTaskGroup group = new NetTaskGroup("A");
        NetTask task = new NetTask("A", "B", new URLInputStream(getTestURL()));
        group.addTask(task);

        group.removeTask(task);

        assertNull(task.getParent());
        assertEquals(0, group.getTaskCount());
        assertEquals(0, task.getPropertyChangeListeners().length);

        // duplicate removal handling
        group.removeTask(task);

        assertNull(task.getParent());
        assertEquals(0, group.getTaskCount());
        assertEquals(0, task.getPropertyChangeListeners().length);
    }

    /**
     * @see NetTaskGroup#indexOf
     */
    public void testIndexOf()
    {
        NetTaskGroup group = new NetTaskGroup("A");
        URLInputStream stream = new URLInputStream(getTestURL());
        NetTask task = new NetTask("A", "B", stream);

        group.addTask(task);

        assertEquals(-1, group.indexOf(new NetTask("A", "B", new URLInputStream(getTestURL()))));
        assertEquals(0, group.indexOf(task));
    }

    /**
     * Tests calculation of group progress.
     */
    public void testGetProgress()
    {
        NetTaskGroup group = new NetTaskGroup("A");

        FreeEventStream stream1 = new FreeEventStream(getTestURL());
        NetTask task1 = new NetTask("A", "B", stream1);
        group.addTask(task1);

        FreeEventStream stream2 = new FreeEventStream(getTestURL());
        NetTask task2 = new NetTask("C", "D", stream2);
        group.addTask(task2);

        assertEquals("Both of tasks have unknown progress -- group progress should be 0.",
            0, (int)group.getProgress());

        stream1.fireConnected0(10);

        assertEquals("First task has known size and progress -- group should count only it.",
            0, (int)group.getProgress());

        stream1.fireRead0(5);
        stream2.fireConnected0(-1);
        stream2.fireRead0(100);

        assertEquals("50% of first task and 0% of the second = 25% overall.",
            25, (int)group.getProgress());

        stream1.fireErrored0();

        assertEquals("50% of first task and 0% of the second = 25% overall.",
            25, (int)group.getProgress());

        stream2.fireFinished0();

        assertEquals("First task is 50% read and the second is 100%. Average is 75%.",
            (100 + 50) / 2, (int)group.getProgress());
    }

    /**
     * Tests group aborting command.
     */
    public void testAbort()
    {
        NetTaskGroup group = new NetTaskGroup("A");

        FreeEventStream stream1 = new FreeEventStream(getTestURL());
        NetTask task1 = new NetTask("A", "B", stream1);
        group.addTask(task1);

        FreeEventStream stream2 = new FreeEventStream(getTestURL());
        NetTask task2 = new NetTask("C", "D", stream2);
        group.addTask(task2);

        stream1.fireConnecting0();
        stream1.fireConnected0(100);
        stream1.fireRead0(20);

        stream2.fireConnecting0();
        stream2.fireConnected0(100);
        stream2.fireRead0(20);

        group.abort();

        assertEquals(NetTask.STATUS_ABORTED, task1.getStatus());
        assertTrue(stream1.isClosed());
        assertEquals(NetTask.STATUS_ABORTED, task2.getStatus());
        assertTrue(stream2.isClosed());
    }

    /**
     * Tests group pausing command.
     */
    public void testPause()
    {
        NetTaskGroup group = new NetTaskGroup("A");

        FreeEventStream stream1 = new FreeEventStream(getTestURL());
        NetTask task1 = new NetTask("A", "B", stream1);
        group.addTask(task1);

        FreeEventStream stream2 = new FreeEventStream(getTestURL());
        NetTask task2 = new NetTask("C", "D", stream2);
        group.addTask(task2);

        stream1.fireConnecting0();
        stream1.fireConnected0(100);
        stream1.fireRead0(20);

        stream2.fireConnecting0();
        stream2.fireConnected0(100);
        stream2.fireRead0(20);

        group.pause();

        int status1 = task1.getStatus();
        assertTrue("Task hasn't paused after group.",
            status1 == NetTask.STATUS_PAUSED || status1 == NetTask.STATUS_PAUSING);

        int status2 = task2.getStatus();
        assertTrue("Task hasn't paused after group.",
            status2 == NetTask.STATUS_PAUSED || status2 == NetTask.STATUS_PAUSING);
    }

    /**
     * Tests group resuming command.
     */
    public void testResume()
    {
        NetTaskGroup group = new NetTaskGroup("A");

        FreeEventStream stream1 = new FreeEventStream(getTestURL());
        NetTask task1 = new NetTask("A", "B", stream1);
        group.addTask(task1);

        FreeEventStream stream2 = new FreeEventStream(getTestURL());
        NetTask task2 = new NetTask("C", "D", stream2);
        group.addTask(task2);

        stream1.fireConnecting0();
        stream1.fireConnected0(100);
        stream1.fireRead0(20);

        stream2.fireConnecting0();
        stream2.fireConnected0(100);
        stream2.fireRead0(20);

        group.pause();
        group.resume();

        int status1 = task1.getStatus();
        assertTrue("Task hasn't unpaused after group.",
            status1 == NetTask.STATUS_UNPAUSING || status1 == NetTask.STATUS_RUNNING);

        int status2 = task2.getStatus();
        assertTrue("Task hasn't unpaused after group.",
            status2 == NetTask.STATUS_UNPAUSING || status2 == NetTask.STATUS_RUNNING);
    }

    // Returns test URL.
    private URL getTestURL()
    {
        final String name = TestNetTaskGroup.class.getName().replaceAll("\\.", "/") + ".class";
        final URL resource = TestNetTaskGroup.class.getClassLoader().getResource(name);

        assertNotNull(resource);

        return resource;
    }

}
