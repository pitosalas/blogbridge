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
// $Id: TestActivityIndicatorView.java,v 1.5 2007/02/06 15:34:02 spyromus Exp $
//

package com.salas.bb.views;

import junit.framework.TestCase;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.utils.ConnectionState;

/**
 * @see ActivityIndicatorView
 */
public class TestActivityIndicatorView extends TestCase
{
    private static final int RACERS = 3;

    protected void setUp()
        throws Exception
    {
        ResourceUtils.setBundlePath("Resource");
    }

    /**
     * Stress-tess with mass activity records creation using <code>RACERS</code> number of threads.
     *
     * @throws Exception in exceptional cases.
     */
    public void testStress() throws Exception
    {
        new ActivityIndicatorView(new ConnectionState(), null);

        final Racer[] r = new Racer[RACERS];
        for (int i = 0; i < RACERS; i++) r[i] = new Racer();
        for (int i = 0; i < RACERS; i++) r[i].start();
        for (int i = 0; i < RACERS; i++) r[i].join();

        assertEquals(0, ActivityIndicatorView.getNumberOfTasks(ActivityTicket.TYPE_NETWORK));
    }

    /**
     * Racer class starting and finishing <code>RECORDS</code> number of polling activities.
     */
    private static class Racer extends Thread
    {
        private static final int RECORDS = 100;

        public void run()
        {
            for (int i = 0; i < RECORDS; i++)
            {
                ActivityTicket at = ActivityIndicatorView.startPolling(Integer.toString(i));

                try
                {
                    Thread.sleep(50);
                } catch (InterruptedException e)
                {
                    // Nothing to do here.
                }

                ActivityIndicatorView.finishActivity(at);
            }
        }
    }
}
