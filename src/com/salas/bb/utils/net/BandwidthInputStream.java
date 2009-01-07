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
// $Id: BandwidthInputStream.java,v 1.2 2006/01/08 05:00:09 kyank Exp $
//

package com.salas.bb.utils.net;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * Bandwidth controlling input stream. By default, the stream is setup to not limit the
 * bandwidth. But it can be adjusted at any moment at run-time to do so. Bandwidth is specified
 * in bytes per second. It's not really 100% precise especially on small files, but for the big
 * once it works very well. The overhead is minimal and it requires no extra threads or timers.
 */
public class BandwidthInputStream extends FilterInputStream
{
    // Number of milliseconds between bandwidth usage synchronizations.
    // (selected in experimental way. If bigger the speed will raise because of no pause in
    // the last block of data. If smaller it will decrease because of too often waits. )
    private static final long SYNC_PERIOD_MS = 75;

    // Bytes per second (0 unlimited)
    private long    bandwidth;

    // Timestamp of last synchronization block start.
    private long    lastSyncTime;

    // Number of bytes allowed to read in synchronization block. Reading may last longer
    // than synchronization block length.
    private int     bytesAllowed;

    // Number of bytes read in current synchronization block.
    private int     bytesRead;

    /**
     * Creates input stream non-limiting bandwidth. Limitation can be set later.
     *
     * @param in input stream to wrap.
     */
    public BandwidthInputStream(InputStream in)
    {
        this(in, 0);
    }

    /**
     * Creates input stream limiting bandwidth.
     *
     * @param in        input stream to wrap.
     * @param bandwidth allowed bandwidth usage (bytes/sec) or 0 for unlimited.
     */
    public BandwidthInputStream(InputStream in, long bandwidth)
    {
        super(in);

        bytesRead = 0;
        lastSyncTime = 0;

        setBandwidth(bandwidth);
    }

    /**
     * Sets the maximum bandwidth usage for this stream.
     *
     * @param bandwidth bandwidth usage in bytes/sec (or &lt;=0 for unlimited).
     */
    public void setBandwidth(long bandwidth)
    {
        if (bandwidth < 1)
        {
            this.bandwidth = 0;
            bytesAllowed = Integer.MAX_VALUE;
        } else
        {
            this.bandwidth = bandwidth;
            final long baLong = bandwidth * SYNC_PERIOD_MS / 1000;
            bytesAllowed = baLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)baLong;
        }
    }

    /**
     * Returns currently selected bandwidth.
     *
     * @return bandwidth or 0 for unlimited.
     */
    public long getBandwidth()
    {
        return bandwidth;
    }

    /**
     * Reads another byte from the stream.
     *
     * @return character or -1 if stream ended.
     *
     * @throws java.io.IOException in case of I/O error.
     */
    public int read() throws IOException
    {
        blockIfNecessary();

        int ch = super.read();
        if (ch != -1) bytesRead++;

        return ch;
    }

    /**
     * Reads another block of characters.
     *
     * @param b     buffer to fill.
     * @param off   offset to start from.
     * @param len   maximum number of chars to read.
     *
     * @return length of actually read data or -1 if stream ended.
     *
     * @throws IOException in case of I/O error.
     */
    public int read(byte b[], int off, int len) throws IOException
    {
        blockIfNecessary();

        len = Math.min(len, (int)(bytesAllowed - bytesRead));

        int read = super.read(b, off, len);
        if (read > 0) bytesRead += read;

        return read;
    }

    /**
     * Skippes specified number of bytes.
     *
     * @param n number of bytes to skip.
     *
     * @return number of bytes actually skipped.
     *
     * @throws IOException in case of I/O error.
     */
    public long skip(long n) throws IOException
    {
        blockIfNecessary();

        n = Math.min(n, (long)(bytesAllowed - bytesRead));

        long skipped = super.skip(n);
        if (skipped > 0) bytesRead += skipped;

        return skipped;
    }

    /**
     * Returns number of bytes available without blocking.
     *
     * @return number of bytes.
     *
     * @throws IOException in case of I/O error.
     */
    public int available() throws IOException
    {
        return Math.min(super.available(), bytesAllowed - bytesRead);
    }

    /**
     * Waits for the end of sync-period if necessary.
     */
    private synchronized void blockIfNecessary()
    {
        if (lastSyncTime == 0)
        {
            // First read
            lastSyncTime = System.currentTimeMillis();
        } else if (bytesRead >= bytesAllowed)
        {
            // We have read all bytes allowed for this synchronization block. See if we need
            // to wait for its end (the reading was fast and there's time left) and block until
            // the synchronization block end is reached.
            long timeToWait = SYNC_PERIOD_MS - (System.currentTimeMillis() - lastSyncTime);
            if (timeToWait > 0)
            {
                try
                {
                    Thread.sleep(timeToWait);
                } catch (InterruptedException e)
                {
                    // Someone interrupts us.
                }
            }

            bytesRead = 0;
            lastSyncTime = System.currentTimeMillis();
        }
    }

    /**
     * Returns number of bytes allowed for reading during sync period.
     *
     * @return bytes.
     */
    int getBytesAllowed()
    {
        return bytesAllowed;
    }
}
