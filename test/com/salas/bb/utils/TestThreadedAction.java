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
// $Id: TestThreadedAction.java,v 1.3 2006/01/08 05:28:19 kyank Exp $
//

package com.salas.bb.utils;

import junit.framework.TestCase;

import java.awt.event.ActionEvent;

/**
 * @see ThreadedAction
 */
public class TestThreadedAction extends TestCase
{
    /**
     * Tests the sequence of calls and threading.
     *
     * @throws Exception in case of any errors.
     */
    public void testSequence() throws Exception
    {
        new MyAction().actionPerformed(null);
        Thread.sleep(100);
    }

    /**
     * Tests the aborting of sequence because of beforeFork() == false.
     *
     * @throws Exception in case of any errors.
     */
    public void testSequenceAborted() throws Exception
    {
        new MyActionNoActing().actionPerformed(null);
        Thread.sleep(100);
    }

    /**
     * Action which is always telling "no" to further fork.
     */
    private static class MyActionNoActing extends ThreadedAction
    {
        /**
         * Actual action.
         *
         * @param event original event object.
         */
        protected void doAction(ActionEvent event)
        {
            fail();
        }

        /**
         * Invoked before forking the thread.
         *
         * @return <code>TRUE</code> to continue with action.
         */
        protected boolean beforeFork()
        {
            return false;
        }
    }

    /**
     * Sequence controlling action.
     */
    private static class MyAction extends ThreadedAction
    {
        private Thread originalThread;

        private boolean actionDisabled;
        private boolean actionBeforeForkCalled;
        private boolean actionExecuted;
        private boolean actionEnabled;

        /**
         * Defines an <code>Action</code> object with a default description string and default icon.
         */
        public MyAction()
        {
            originalThread = Thread.currentThread();

            actionDisabled = false;
            actionBeforeForkCalled = false;
            actionExecuted = false;
            actionEnabled = false;
        }

        /**
         * Invoked before forking the thread.
         *
         * @return <code>TRUE</code> to continue with action.
         */
        protected boolean beforeFork()
        {
            assertTrue(!actionDisabled);
            assertTrue(!actionExecuted);
            assertTrue(!actionEnabled);

            actionBeforeForkCalled = true;

            return super.beforeFork();
        }

        /**
         * Actual action.
         *
         * @param event original event object.
         */
        protected void doAction(ActionEvent event)
        {
            assertTrue(actionDisabled);
            assertTrue(actionBeforeForkCalled);
            assertTrue(!actionEnabled);

            assertTrue(Thread.currentThread() != originalThread);

            actionExecuted = true;
        }

        /**
         * Enables or disables the action.
         *
         * @param newValue true to enable the action, false to disable it
         *
         * @see javax.swing.Action#setEnabled
         */
        public void setEnabled(boolean newValue)
        {
            if (newValue)
            {
                assertTrue(actionDisabled);
                assertTrue(actionBeforeForkCalled);
                assertTrue(actionExecuted);
                actionEnabled = true;
            } else
            {
                assertTrue(!actionDisabled);
                assertTrue(actionBeforeForkCalled);
                assertTrue(!actionExecuted);
                actionDisabled = true;
            }

            super.setEnabled(newValue);
        }
    }
}
