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
// $Id: AbstractLoader.java,v 1.6 2006/01/08 05:00:09 kyank Exp $
//

package com.salas.bb.utils.loader;

/**
 * Abstract loader showing dialog box with cancel button.
 */
public abstract class AbstractLoader extends Thread
{
    private LoaderProgressDialog dialog;
    private Exception ex;
    private Object data;

    /**
     * Creates loader thread. Thread will not start immediately. You should use <code>load()</code>
     * method to start it.
     *
     * @param message message that will be given to the dialog to display while loading.
     */
    public AbstractLoader(String message)
    {
        if (message != null) dialog = new LoaderProgressDialog(message, this);
    }

    /**
     * Runs the task.
     */
    public void run()
    {
        if (dialog != null) dialog.open();

        try
        {
            processLoad();
        } catch (Exception e)
        {
            ex = e;
        }

        if (dialog != null) dialog.close();
    }

    /**
     * Sub-classes should override this operation to make real job.
     *
     * @throws Exception any exception that should be brought back to the client immediately.
     */
    protected abstract void processLoad()
        throws Exception;

    /**
     * Sub-classes should use this method to set the loaded data.
     *
     * @param aData loaded data.
     */
    protected void setData(Object aData)
    {
        data = aData;
    }

    /**
     * Start loading data. If loading was canceled <code>isCanceled()</code> method will
     * return true.
     *
     * @return loaded data object.
     *
     * @throws LoaderException exception thrown by operation.
     */
    public Object load()
        throws LoaderException
    {
        // Reset exception state and data.
        ex = null;
        data = null;

        // Start processing and show modal dialog.
        synchronized (this)
        {
            start();
            try
            {
                wait();
            } catch (InterruptedException e)
            {
                // Job exited or canceled
            }
        }

        // If there was and exception during processing then
        // wrap it into our own and throw.
        if (ex != null)
        {
            throw new LoaderException(ex);
        }

        return data;
    }

    /**
     * Returns true if processing was cancelled.
     *
     * @return TRUE if loading was canceled.
     */
    public boolean isCanceled()
    {
        return dialog.isCanceled();
    }
}
