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
// $Id: TestNetTask.java,v 1.6 2007/02/06 15:34:02 spyromus Exp $
//

package com.salas.bb.networking.manager;

import com.salas.bb.utils.net.URLInputStream;
import junit.framework.TestCase;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @see NetTask
 */
public class TestNetTask extends TestCase
{
    /**
     * Tests creation of group.
     */
    public void testCreationGroup()
    {
        long time = System.currentTimeMillis();

        NetTask task = new NetTask("A");
        long startTime = task.getStartTime().getTime();

        assertNull(task.getFeed());
        assertNull(task.getParent());
        assertEquals(-1, (int)task.getProgress());
        assertEquals(-1, task.getSize());
        assertNull(task.getSourceURL());
        assertTrue(startTime >= time && startTime <= time + 100);
        assertEquals(NetTask.STATUS_CONNECTING, task.getStatus());
        assertEquals("A", task.getTitle());

        assertEquals("A", task.toString());
    }

    /**
     * Tests creation of task.
     */
    public void testCreationTask()
    {
        URLInputStream stream = new URLInputStream(getTestURL());
        long time = System.currentTimeMillis();

        NetTask task = new NetTask("A", "B", stream);
        long startTime = task.getStartTime().getTime();

        assertEquals("B", task.getFeed());
        assertNull(task.getParent());
        assertEquals(-1, (int)task.getProgress());
        assertEquals(-1, task.getSize());
        assertTrue(stream.getSourceURL() == task.getSourceURL());
        assertTrue(startTime >= time && startTime <= time + 20);
        assertEquals(NetTask.STATUS_CONNECTING, task.getStatus());
        assertEquals("A", task.getTitle());

        assertEquals("A", task.toString());
    }

    /**
     * Test the chaning of status during operations.
     */
    public void testSetStatus() throws InterruptedException
    {
        NetTask task = new NetTask("A", "B", new URLInputStream(getTestURL()));
        RecordingPropertyChangeListener recorder = new RecordingPropertyChangeListener();

        task.addPropertyChangeListener(recorder);
        task.pause();
        int status = task.getStatus();
        assertTrue("Task hasn't paused.",
            status == NetTask.STATUS_PAUSED || status == NetTask.STATUS_PAUSING);

        task.resume();
        Thread.sleep(200);
        status = task.getStatus();
        assertTrue("Task hasn't unpaused. Current status=" + status,
            status == NetTask.STATUS_UNPAUSING || status == NetTask.STATUS_RUNNING);

        task.abort();
        Thread.sleep(200);
        assertEquals(NetTask.STATUS_ABORTED, task.getStatus());

        PropertyChangeEvent[] recordedEvents = recorder.getRecordedEvents();
        assertEquals(5, recordedEvents.length);
    }

    /**
     * Test the changing of paused status of the stream in response to operations over the task.
     */
    public void testStreamPaused()
        throws InterruptedException
    {
        URLInputStream stream = new URLInputStream(getTestURL());
        NetTask task = new NetTask("A", "B", stream);

        assertFalse(stream.isPaused());
        task.pause();

        // We need to sleep a bit before the check as the operation of pausing / unpausing
        // is async and takes a while to finish.
        Thread.sleep(100);

        assertTrue(stream.isPaused());
        task.resume();

        // We need to sleep a bit before the check as the operation of pausing / unpausing
        // is async and takes a while to finish.
        Thread.sleep(100);

        assertFalse(stream.isPaused());
        task.abort();
        assertFalse(stream.isPaused());
        assertTrue(stream.isClosed());
    }

    /**
     * Tests the status changes of task according the scenario:
     * connecting, connected (size unknown), read 10 bytes and finished
     */
    public void testStatusChangesForCompletedSizeless()
    {
        FreeEventStream stream = new FreeEventStream(getTestURL());
        NetTask task = new NetTask("A", "B", stream);

        stream.fireConnecting0();
        assertEquals(NetTask.STATUS_CONNECTING, task.getStatus());

        stream.fireConnected0(-1);
        assertEquals(NetTask.STATUS_CONNECTING, task.getStatus());
        assertEquals(-1, task.getSize());

        stream.fireRead0(10);
        assertEquals(NetTask.STATUS_RUNNING, task.getStatus());
        assertEquals(-1, (int)task.getProgress());

        stream.fireFinished0();
        assertEquals(NetTask.STATUS_COMPLETED, task.getStatus());
    }

    /**
     * Tests the status changes of task according the scenario:
     * connecting, errored
     */
    public void testStatusChangesForErrored()
    {
        FreeEventStream stream = new FreeEventStream(getTestURL());
        NetTask task = new NetTask("A", "B", stream);

        stream.fireConnecting0();
        assertEquals(NetTask.STATUS_CONNECTING, task.getStatus());

        stream.fireErrored0();
        assertEquals(NetTask.STATUS_ERRORED, task.getStatus());
    }

    /**
     * Tests the status changes of task according the scenario:
     * connecting, connected (size=10), read 5 bytes and errored
     */
    public void testStatusChangesForErroredKnown()
    {
        FreeEventStream stream = new FreeEventStream(getTestURL());
        NetTask task = new NetTask("A", "B", stream);

        stream.fireConnecting0();
        assertEquals(NetTask.STATUS_CONNECTING, task.getStatus());

        stream.fireConnected0(10);
        assertEquals(NetTask.STATUS_CONNECTING, task.getStatus());
        assertEquals(10, task.getSize());
        assertEquals(0, (int)task.getProgress());

        stream.fireRead0(5);
        assertEquals(NetTask.STATUS_RUNNING, task.getStatus());
        assertEquals(50, (int)task.getProgress());

        stream.fireErrored0();
        assertEquals(NetTask.STATUS_ERRORED, task.getStatus());
    }

    // Returns test URL.
    private URL getTestURL()
    {
        final String name = TestNetTask.class.getName().replaceAll("\\.", "/") + ".class";
        final URL resource = TestNetTask.class.getClassLoader().getResource(name);

        assertNotNull(resource);

        return resource;
    }

    /**
     * Records all events to the list.
     */
    private static class RecordingPropertyChangeListener implements PropertyChangeListener
    {
        private List events = new ArrayList();

        /**
         * This method gets called when a bound property is changed.
         *
         * @param evt A PropertyChangeEvent object describing the event source and the property
         *            that has changed.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            events.add(evt);
        }

        /**
         * Returns the list of recorded events.
         *
         * @return list of events.
         */
        public PropertyChangeEvent[] getRecordedEvents()
        {
            return (PropertyChangeEvent[])events.toArray(new PropertyChangeEvent[events.size()]);
        }
    }
}
