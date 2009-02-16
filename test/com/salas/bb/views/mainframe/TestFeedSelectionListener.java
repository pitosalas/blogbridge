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
// $Id: TestFeedSelectionListener.java,v 1.5 2008/02/28 15:59:52 spyromus Exp $
//

package com.salas.bb.views.mainframe;

import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.prefs.UserPreferences;
import junit.framework.TestCase;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.beans.PropertyChangeEvent;

/**
 * This suite contains tests for <code>FeedSelectionListener</code>.
 * It covers:
 * <ul>
 *  <li>Zero-delay selection of feeds.</li>
 *  <li>Delayed selection of feeds with various permitted delays.</li>
 *  <li>Quick selection of several feeds with triggering of the last selection only.</li>
 * </ul>
 */
public class TestFeedSelectionListener extends TestCase
{
    private static final long MAX_ZERO_DELAY_TIME = 30;
    private static final double DELAY_PRECISION = 0.4;

    private JList       list;
    private CustomCSL   listener;
    private IFeed       feed0;
    private IFeed       feed1;

    /**
     * Initialization before the tests.
     */
    protected void setUp()
    {
        listener = new CustomCSL(0);

        feed0 = new DirectFeed();
        feed1 = new DirectFeed();
        list = new JList(new IFeed[] { feed0, feed1 });
        list.addListSelectionListener(listener);
    }

    /**
     * Tests that the feed is selected immediately, withing the same thread and
     * it takes limited time (controlled by <code>MAX_ZERO_DELAY_TIME</code>
     * constant).
     */
    public void testFeedSelectionDelayZero()
    {
        // Select the first feed and get the delay without wating while holding the lock
        // to disallow the other threads to access listener. It will guaranty that the
        // threads aren't forked for zero-delays.
        long actualDelay;
        synchronized (listener)
        {
            list.setSelectedIndex(0);
            actualDelay = listener.waitForEvent(0);
        }

        assertFalse("Initial event (valueChanged()) didn't come.", actualDelay == -1);
        assertFalse("The feed wasn't selected in time.", actualDelay == -2);
        assertTrue("Too long selection: allowed=" + MAX_ZERO_DELAY_TIME + " actual=" + actualDelay,
            actualDelay < MAX_ZERO_DELAY_TIME);
    }

    /**
     * Tests that feeds are selected with proper delays (the error stays in defined bounds).
     */
    public void testFeedSelectionDelay()
    {
        for (int i = 0; i < 5; i++)
        {
            // Compute delay [100;500] and corresponding allowed error 33%
            long delay = (i * 100) + 100;
            long error = (long)(delay * DELAY_PRECISION);
            listener.setFeedSelectionDelay(delay);

            // Select feed and wait for selection event
            list.setSelectedIndex(i % 2);
            long actualDelay = listener.waitForEvent(delay + error);

            assertFalse("valueChanged() didn't come: delay=" + delay, actualDelay == -1);
            assertFalse("The feed wasn't selected in time: delay=" + delay, actualDelay == -2);
            assertTrue("Too fast selection: delay=" + delay + ", actual=" + actualDelay,
                actualDelay > (delay - error));

            // Reset the times for the next loop
            listener.reset();
        }
    }

    /**
     * Tests that if the selection events come faster than the delayed selection
     * triggers only one (the last) event will be triggered in the end after the delay.
     */
    public void testQuickFeedSelection()
    {
        GlobalModel.SINGLETON = new GlobalModel(null);
        
        // Compute delay [100;500] and corresponding allowed error 33%
        long delay = 500;
        long error = (long)(delay * DELAY_PRECISION);
        long waitTime = delay - error;
        listener.setFeedSelectionDelay(delay);

        // Select first item and wait for some time (less than delay including error)
        list.setSelectedIndex(0);
        long actualDelay = listener.waitForEvent(waitTime);

        // Fast check to save time
        if (actualDelay >= -1)
        {
            assertFalse("valueChanged() didn't come.", actualDelay == -1);
            assertTrue("The feed wasn't selected faster than required.", actualDelay == -2);
        }

        // Select the second feed before the delay expires to start new delay counting
        // and wait for the calculated time as if the feed was first.
        list.setSelectedIndex(1);
        actualDelay = listener.waitForEvent(delay + error);

        long minDelay = waitTime + (delay - error);
        assertTrue("Too fast selection: min=" + minDelay + ", actual=" + actualDelay, actualDelay > minDelay);
        assertTrue("Wrong feed selected.", feed1 == list.getSelectedValue());
    }

    // ---------------------------------------------------------------------------------------------
    // Helper classes and methods
    // ---------------------------------------------------------------------------------------------

    /**
     * Listener which is recording call times of <code>valueChanged()</code> and
     * <code>selectFeed</code> methods for further measurements. It also allows to wait
     * for a both methods called in a given period of time.
     */
    private static class CustomCSL extends FeedSelectionListener
    {
        private long selectFeedTime;
        private long valueChangedTime;

        /**
         * Creates the custom test listener.
         *
         * @param delay initial delay.
         */
        public CustomCSL(long delay)
        {
            super(delay);
            reset();
        }

        /**
         * Sets the delay. This method is named the same way as the underlying private
         * method intentionally. If at some moment will decide to expose that method
         * to outer world or subclasses we will need to remove this declaration and
         * every test will continue to work as is.
         *
         * @param delay delay.
         */
        public void setFeedSelectionDelay(long delay)
        {
            propertyChange(new PropertyChangeEvent(this,
                UserPreferences.PROP_FEED_SELECTION_DELAY,
                new Integer(-1), new Integer((int)delay)));
        }

        /**
         * Resets the times.
         */
        public synchronized void reset()
        {
            selectFeedTime = -1;
            valueChangedTime = -1;
        }

        /**
         * Select feed if it's not currently selected.
         *
         * @param feed feed to select.
         */
        protected void selectFeed(IFeed feed)
        {
            // Experimental delay to check that test catches the delays correctly
//            try
//            {
//                Thread.sleep(1000);
//            } catch (InterruptedException e)
//            {
//            }

            synchronized (this)
            {
                selectFeedTime = System.currentTimeMillis();
                notifyAll();
            }
        }

        /**
         * Call this whenever user clicks on one of the Channels in the ChannelList.
         *
         * @param e event object.
         */
        public synchronized void valueChanged(final ListSelectionEvent e)
        {
            valueChangedTime = System.currentTimeMillis();
            super.valueChanged(e);
        }

        /**
         * Waits for event if it didn't happen yet and returns the delay between calls
         * to <code>valueChanged()</code> and <code>selectFeed()</code> methods. If the
         * first wasn't called then (-1) will be returned, if the second wasn't called
         * (-2) will be returned.
         *
         * @param period maximum time to wait (in ms).
         *
         * @return delay in ms between calls or (-1) if <code>valueChanged()</code>
         *         wasn't called or (-2) if <code>selectFeed()</code> wasn't called.
         */
        public synchronized long waitForEvent(long period)
        {
            if (selectFeedTime == -1 && period > 0)
            {
                try
                {
                    wait(period);
                } catch (InterruptedException e)
                {
                }
            }

            return valueChangedTime == -1 ? -1
                   : selectFeedTime == -1 ? -2
                       : selectFeedTime - valueChangedTime;
        }
    }
}
