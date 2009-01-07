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
// $Id: URLImageProducer.java,v 1.6 2006/02/17 12:59:44 spyromus Exp $
//

package com.salas.bb.utils.uif.images;

import sun.awt.image.URLImageSource;

import java.awt.image.ImageConsumer;
import java.net.URL;

/**
 * Simple <code>ImageProducer</code> implementation which is not clean subclass of the
 * <code>URLImageSource</code> because the original class is from hidden API.</p>
 *
 * <p>Before it starts, it registers the network activity at <code>NetManager</code>.</p>
 */
class URLImageProducer extends URLImageSource implements Runnable
{
    /** When birth time is after birth limit time, the image doesn't start fetching. */
    private static long birthLimitTime = -1;

    private final IImageFetcherCallback callback;
    private final long birthTime;

    private boolean                     producing;

    /**
     * Creates a producer.
     *
     * @param aUrl      url to load image from.
     * @param aCallback callback to use to notify about loading start.
     */
    public URLImageProducer(URL aUrl, IImageFetcherCallback aCallback)
    {
        super(aUrl);

        birthTime = System.currentTimeMillis();
        callback = aCallback;
        producing = false;
    }

    /**
     * Registers the specified <code>ImageConsumer</code> object as a consumer and starts an
     * immediate reconstruction of the image data which will then be delivered to this consumer and
     * any other consumer which might have already been registered with the producer.  This method
     * differs from the addConsumer method in that a reproduction of the image data should be
     * triggered as soon as possible.
     *
     * @param ic the specified <code>ImageConsumer</code>
     *
     * @see #addConsumer
     */
    public synchronized void startProduction(ImageConsumer ic)
    {
        addConsumer(ic);

        if (!producing) callback.startLoading(this);
    }

    /**
     * Called from worker thread.
     */
    public void run()
    {
        if (canDoFetch()) doFetch();
    }

    /**
     * Returns <code>TRUE</code> if this producer is young enough to
     * do fetch.
     *
     * @return <code>TRUE</code> if capable of fetches.
     */
    public boolean canDoFetch()
    {
        boolean b;

        synchronized (URLImageProducer.class)
        {
            b = birthTime > birthLimitTime;
        }

        return b;
    }

    /**
     * Sets birth limit time. The images born before this time will not
     * start fetching data.
     *
     * @param time time.
     */
    public static synchronized void setBirthLimitTime(long time)
    {
        birthLimitTime = time;
    }

    /**
     * Returns current birth limit time.
     *
     * @return current birth limit time.
     */
    public static synchronized long getBirthLimitTime()
    {
        return birthLimitTime;
    }
}
