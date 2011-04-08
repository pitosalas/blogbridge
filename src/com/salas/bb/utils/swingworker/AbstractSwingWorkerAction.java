/*
 * BlogBridge -- RSS feed reader, manager, and web based service
 * Copyright (C) 2002-2011 by R. Pito Salas
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Contact: R. Pito Salas
 * mailto:pitosalas@users.sourceforge.net
 * More information: about BlogBridge
 * http://www.blogbridge.com
 * http://sourceforge.net/projects/blogbridge
 */

package com.salas.bb.utils.swingworker;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutionException;

/**
 * Abstract action that utilizes Swing Worker mechanics to do the job on the worker thread and
 * return control to the EDT when #done.
 *
 * @param <T> type to return from #performInBackground and #get methods.
 * @param <V> internal publish / process type.
 */
public abstract class AbstractSwingWorkerAction<T, V> extends AbstractAction
{
    SwingWorker<T, V> worker;
    private boolean disableWhenWorking;

    public AbstractSwingWorkerAction()
    {
        super();
    }

    public AbstractSwingWorkerAction(String s)
    {
        super(s);
    }

    public AbstractSwingWorkerAction(String s, Icon icon)
    {
        super(s, icon);
    }

    public void setDisableWhenWorking(boolean v)
    {
        this.disableWhenWorking = v;
    }

    public void actionPerformed(final ActionEvent actionEvent)
    {
        worker = new SwingWorker<T, V>()
        {
            protected T doInBackground() throws Exception
            {
                return performInBackground();
            }

            protected void done()
            {
                if (disableWhenWorking) setEnabled(true);
                AbstractSwingWorkerAction.this.done();
            }
        };

        if (disableWhenWorking) setEnabled(false);

        before();
        worker.execute();
    }

    protected void before() { }
    protected T get() throws ExecutionException, InterruptedException { return worker.get(); }
    protected abstract T performInBackground() throws Exception;
    protected void done() { };

}
