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
// $Id: TestEventQueueWithWD.java,v 1.5 2006/01/08 05:28:18 kyank Exp $
//

package com.salas.bb.utils.watchdogs;

import junit.framework.TestCase;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @see EventQueueWithWD
 */
public class TestEventQueueWithWD extends TestCase
{
    /**
     * Tests how watchdogs of both types are triggered.
     *
     * @throws Exception in error case.
     */
    public void testWatchdogs() throws Exception
    {
        // Install our alternative queue
        EventQueueWithWD queue = EventQueueWithWD.install();

        // Create both types of actions
        CounterAction repeatitiveAction = new CounterAction();
        CounterAction singleShotAction = new CounterAction();

        // Register actions as watchdogs actions
        queue.addWatchdog(90, repeatitiveAction, true);
        queue.addWatchdog(100, singleShotAction, false);

        // Start "lengthy" task
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                try
                {
                    Thread.sleep(300);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        });

        // Repeatitive dog should be called 2 times
        // Single-shot dog should be called one time only
        assertEquals(1, singleShotAction.getCounter());

        int counter = repeatitiveAction.getCounter();
        assertTrue(counter == 2 || counter == 3);
    }

    /**
     * Action which counts number of times it's called.
     */
    private static class CounterAction extends AbstractAction
    {
        private int counter = 0;

        public void actionPerformed(ActionEvent e)
        {
            counter++;
        }

        public int getCounter()
        {
            return counter;
        }
    }
}
