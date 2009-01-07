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
// $Id: NetTask.java,v 1.7 2006/05/29 12:48:30 spyromus Exp $
//

package com.salas.bb.networking.manager;

import com.salas.bb.utils.net.URLInputStream;
import com.salas.bb.utils.net.IStreamProgressListener;
import com.salas.bb.utils.i18n.Strings;

import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.URL;
import java.io.IOException;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

/**
 * Simple handle for network task.
 */
public class NetTask
{
    private static final Logger LOG = Logger.getLogger(NetTask.class.getName());

    /** Connecting to the source. */
    public static final int STATUS_CONNECTING   = 0;
    /** Task is actively running. */
    public static final int STATUS_RUNNING      = 1;
    /** Task is pausing. */
    public static final int STATUS_PAUSING      = 2;
    /** Task is on pause. */
    public static final int STATUS_PAUSED       = 3;
    /** Task is unpausing. */
    public static final int STATUS_UNPAUSING    = 4;
    /** Task has been successfully completed. */
    public static final int STATUS_COMPLETED    = 5;
    /** Task has been aborted by used. */
    public static final int STATUS_ABORTED      = 6;
    /** Task has beed errored. */
    public static final int STATUS_ERRORED      = 7;

    private final URLInputStream stream;

    private NetTaskGroup    parent;

    private String          title;
    private String          parentFeed;

    private int             status;
    protected Date          startTime;
    private float           progress;
    private long            size;
    private long            read;

    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * Creates task.
     *
     * @param title title of the task.
     */
    protected NetTask(String title)
    {
        this(title, null, null);
    }

    /**
     * Creates task.
     *
     * @param aTitle        title.
     * @param aParentFeed   owner feed.
     * @param aStream       stream.
     */
    public NetTask(String aTitle, String aParentFeed, URLInputStream aStream)
    {
        this.title = aTitle;
        this.parentFeed = aParentFeed;
        this.stream = aStream;

        this.startTime = new Date();
        this.progress = -1;
        this.size = -1;
        this.read = 0;
        this.status = STATUS_CONNECTING;

        if (aStream != null)
        {
            aStream.addListener(new StreamListener());
        }
    }

    /**
     * Returns the title.
     *
     * @return title.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Returns string representation.
     *
     * @return string representation.
     */
    public String toString()
    {
        return title;
    }

    /**
     * Returns task progress.
     *
     * @return progress.
     */
    public float getProgress()
    {
        return progress;
    }

    /**
     * Returns size of resource being fetched.
     *
     * @return size.
     */
    public long getSize()
    {
        return size;
    }

    /**
     * Returns time of activity start.
     *
     * @return time.
     */
    public Date getStartTime()
    {
        return startTime;
    }

    /**
     * Returns source URL of resource.
     *
     * @return URL.
     */
    public URL getSourceURL()
    {
        return stream == null ? null : stream.getSourceURL();
    }

    /**
     * Returns parent feed name (if any).
     *
     * @return parent feed name.
     */
    public String getFeed()
    {
        return parentFeed;
    }

    /**
     * Pauses the stream.
     */
    public synchronized void pause()
    {
        if (stream != null)
        {
            setStatus(STATUS_PAUSING);
            Thread thread = new Thread()
            {
                public void run()
                {
                    stream.setPaused(true);
                    setStatus(STATUS_PAUSED);
                }
            };

            thread.start();
        }
    }

    /**
     * Resumes the stream.
     */
    public synchronized void resume()
    {
        if (stream != null)
        {
            setStatus(STATUS_UNPAUSING);
            Thread thread = new Thread()
            {
                public void run()
                {
                    stream.setPaused(false);
                    setStatus(STATUS_RUNNING);
                }
            };

            thread.start();
        }
    }

    /**
     * Closes the stream.
     */
    public void abort()
    {
        if (stream != null)
        {
            setStatus(STATUS_ABORTED);
            try
            {
                stream.close();
            } catch (IOException e)
            {
                LOG.log(Level.WARNING, Strings.error("net.failed.to.abort.the.stream"), e);
            }
        }
    }

    /**
     * Sets the status.
     *
     * @param aStatus new status.
     */
    private void setStatus(int aStatus)
    {
        int oldStatus = status;
        status = aStatus;
        pcs.firePropertyChange("status", oldStatus, status);
    }

    /**
     * Returns the status of task.
     *
     * @return status.
     */
    public int getStatus()
    {
        return status;
    }

    /**
     * Sets the size of resource.
     *
     * @param aSize size.
     */
    private void setSize(long aSize)
    {
        long oldSize = size;
        size = aSize;
        pcs.firePropertyChange("size", new Long(oldSize), new Long(size));
    }

    /**
     * Sets the number of bytes read (updates progress).
     *
     * @param aRead bytes read.
     */
    private void addRead(long aRead)
    {
        if (size > 0 && aRead > 0)
        {
            read += aRead;
            setProgress(Math.min((float)read / size, 1));
        }
    }

    /**
     * Sets the progress of reading operation.
     *
     * @param aProgress progress.
     */
    private void setProgress(float aProgress)
    {
        float oldProgress = progress;
        progress = aProgress >= 0 ? aProgress * 100 : aProgress;
        pcs.firePropertyChange("progress", new Float(oldProgress), new Float(progress));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds property change listener.
     *
     * @param l listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener l)
    {
        pcs.addPropertyChangeListener(l);
    }

    /**
     * Removes property change listener.
     *
     * @param l listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener l)
    {
        pcs.removePropertyChangeListener(l);
    }

    /**
     * Returns all registered property change listeners.
     *
     * @return listeners.
     */
    protected PropertyChangeListener[] getPropertyChangeListeners()
    {
        return pcs.getPropertyChangeListeners();
    }

    /**
     * Returns parent of this task.
     *
     * @return parent.
     */
    public NetTaskGroup getParent()
    {
        return parent;
    }

    /**
     * Sets parent of this task.
     *
     * @param aParent parent.
     */
    public void setParent(NetTaskGroup aParent)
    {
        parent = aParent;
    }

    // ---------------------------------------------------------------------------------------------

    /** Listener of stream events. */
    private class StreamListener implements IStreamProgressListener
    {
        /**
         * Indicates the the source is being connected.
         *
         * @param source source.
         */
        public void connecting(URLInputStream source)
        {
            setStatus(STATUS_CONNECTING);
        }

        /**
         * Indicates that the source has been successfully connected.
         *
         * @param source source.
         * @param length length of the resource (-1 if unknown).
         */
        public void connected(URLInputStream source, long length)
        {
            // we don't change the status here as we don't know if we are running
            // or paused or errored.
            setSize(length);
            setProgress(length == -1 ? -1 : 0);
        }

        /**
         * Indicates that some bytes has been read.
         *
         * @param source source.
         * @param bytes  bytes.
         */
        public void read(URLInputStream source, long bytes)
        {
            setStatus(STATUS_RUNNING);
            addRead(bytes);
        }

        /**
         * Indicates that the stream has been finished.
         *
         * @param source source.
         */
        public void finished(URLInputStream source)
        {
            setProgress(1);
            setStatus(STATUS_COMPLETED);
        }

        /**
         * Indicates that there's an error happened during reading of stream. (We already did attempts
         * to recover).
         *
         * @param source source.
         * @param ex     cause of error.
         */
        public void errored(URLInputStream source, IOException ex)
        {
            setStatus(STATUS_ERRORED);
        }
    }
}
