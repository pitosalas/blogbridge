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
// $Id: ThreadedAction.java,v 1.7 2006/01/08 05:04:21 kyank Exp $
//

package com.salas.bb.utils;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Action which is forking another thread to do the task.
 */
public abstract class ThreadedAction extends AbstractAction
{
    /**
     * Defines an <code>Action</code> object with a default description string and default icon.
     */
    protected ThreadedAction()
    {
    }

    /**
     * Defines an <code>Action</code> object with the specified description string and a default
     * icon.
     */
    protected ThreadedAction(String name)
    {
        super(name);
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e event object.
     */
    public void actionPerformed(final ActionEvent e)
    {
        if (beforeFork())
        {
            setEnabled(false);
            Thread thread = new Thread(this.getClass().getName())
            {
                public void run()
                {
                    try
                    {
                        doAction(e);
                    } finally
                    {
                        setEnabled(true);
                    }
                }
            };

            thread.start();
        }
    }

    /**
     * Invoked before forking the thread. Override this method to add a go-no-go decision before starting
     * the fork. See for example: DeleteChannelAction::beforeFork 
     *
     * @return <code>TRUE</code> to continue with action.
     */
    protected boolean beforeFork()
    {
        return true;
    }

    /**
     * Actual action.
     *
     * @param event original event object.
     */
    protected abstract void doAction(ActionEvent event);
}
