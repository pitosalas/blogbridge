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
// $Id: TestSoftPCLWrapper.java,v 1.5 2006/01/08 05:28:19 kyank Exp $
//

package com.salas.bb.utils;

import junit.framework.TestCase;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * @see SoftPCLWrapper
 */
public class TestSoftPCLWrapper extends TestCase
{
    private static final int MASSIVE_SIZE       = 2000;
    private static final int MASSIVE_LISTENERS  = 1000;

    /**
     * Goal of this code is to test if soft PCL wrapper really not prevents
     * actual PCL's to be garbage collected if necessary and if it really
     * unsubscribes itself if actual PCL is no longer present.
     */
    public void testAutoUnsubscription()
    {
        // Create the event source
        EventsSource es = new EventsSource();

        int afterGC = 0;

        // Register MASSIVE_LISTENERS number of listeners
        for (int i = 0; i < MASSIVE_LISTENERS; i++)
        {
            PropertyChangeListener l = new MassivePCL();
            es.addPropertyChangeListener(new SoftEventsSourcePCLWrapper(l));
        }

        assertEquals(MASSIVE_LISTENERS, es.getListeners().length);

        // Consume all memory
        try
        {
            byte[] buf = new byte[(int)Runtime.getRuntime().maxMemory()];
            buf[0] = 0; // This one is never called
        } catch (OutOfMemoryError e)
        {
        }

        // Fire test event which is necessary for soft-wrappers with garbage
        // collected actual PCL's to unsubscribe.
        es.fireTestChange();

        afterGC = es.getListeners().length;

        assertEquals("MASSIVE_LISTENERS=" + MASSIVE_LISTENERS + ", Left After GC=" + afterGC, 0,
            afterGC);
    }

    /** Simply memory-taking PCL. */
    private static class MassivePCL implements PropertyChangeListener
    {
        private byte[] buffer;

        public MassivePCL()
        {
            buffer = new byte[MASSIVE_SIZE];
        }

        public void propertyChange(PropertyChangeEvent evt)
        {
        }
    }

    /** Soft-wrapper PCL, which is aware of removing itself from EventsSource. */
    private static class SoftEventsSourcePCLWrapper extends SoftPCLWrapper
    {
        public SoftEventsSourcePCLWrapper(PropertyChangeListener listener)
        {
            super(listener);
        }

        protected void removeThisListener(Object source)
        {
            EventsSource es = (EventsSource)source;
            es.removePropertyChangeListener(this);
        }
    }

    /** Simple events sources. Nothing special, just wraps PCS. */
    private static class EventsSource
    {
        private PropertyChangeSupport pcs;

        public EventsSource()
        {
            pcs = new PropertyChangeSupport(this);
        }

        public void addPropertyChangeListener(PropertyChangeListener l)
        {
            pcs.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l)
        {
            pcs.removePropertyChangeListener(l);
        }

        public PropertyChangeListener[] getListeners()
        {
            return pcs.getPropertyChangeListeners();
        }

        public void fireTestChange()
        {
            pcs.firePropertyChange("test", 1, 2);
        }
    }
}
