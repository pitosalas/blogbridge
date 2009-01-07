// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2007 by R. Pito Salas
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
// $Id: SavingImageSource.java,v 1.7 2007/11/23 15:24:33 spyromus Exp $
//

package com.salas.bb.utils.uif.images;

import com.salas.bb.networking.manager.NetManager;
import com.salas.bb.networking.manager.NetTask;
import com.salas.bb.utils.net.URLInputStream;
import sun.awt.image.ImageDecoder;
import sun.awt.image.URLImageSource;

import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.io.*;
import java.net.URL;
import java.util.Hashtable;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Image source that saves image data loaded from remote location.
 */
public class SavingImageSource extends URLImageSource implements Runnable
{
    private static final Logger LOG = Logger.getLogger(SavingImageSource.class.getName());

    private static LinkedBlockingQueue<Runnable>    queue;
    private static Executor                         executor;         // Running remote producers
    private static LinkedBlockingQueue<Runnable>    queueLocals;
    private static Executor                         executorLocals;   // Running local producers

    private File                    fileSource; // Source file (input is created from it)
    private URL                     urlSource;  // Source URL (input is created from it)
    private InputStream             input;      // Input stream
    private OutputStream            output;     // File stream

    private volatile boolean        producing;  // TRUE when producer started
    private volatile boolean        produced;   // TRUE when initial production is finished
    private volatile boolean        abort;      // TRUE when processing should be terminated

    private File                    temp;       // Temporary image data
    private File                    dest;       // Destination image file

    private NetTask                 netTask;    // Associated net task

    // Initialize the executor
    static
    {
        queue = new LinkedBlockingQueue<Runnable>();
        executor = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, queue, new ThreadFactory()
        {
            public Thread newThread(Runnable r)
            {
                Thread th = new Thread(r, "Image Loader NG");
                th.setDaemon(true);
                th.setPriority(Thread.MIN_PRIORITY);
                return th;
            }
        });

        queueLocals = new LinkedBlockingQueue<Runnable>();
        executorLocals = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, queueLocals, new ThreadFactory()
        {
            public Thread newThread(Runnable r)
            {
                Thread th = new Thread(r, "Image Loader NG (Locals)");
                th.setDaemon(true);
                th.setPriority(Thread.MIN_PRIORITY);
                return th;
            }
        });
    }

    /**
     * Creates an image by loading data directly from the given file. Caching is disabled.
     *
     * @param source    source file.
     *
     * @throws IOException in case of I/O problem.
     */
    public SavingImageSource(File source)
        throws IOException
    {
        super("http://localhost/");

        this.fileSource = source;
    }

    /**
     * Creates an image from the remote source and saves it first into the temp, and
     * then (upon completion) into the destination file.
     *
     * @param source    source URL.
     * @param temp      temp file.
     * @param dest      destination file.
     *
     * @throws IOException in case of I/O problem.
     */
    public SavingImageSource(URL source, File temp, File dest)
        throws IOException
    {
        super("http://localhost/");

        this.urlSource = source;
        this.dest = dest;
        this.temp = temp;
    }

    /**
     * Removes tasks from queues.
     */
    public static void clearQueues()
    {
        synchronized (SavingImageSource.class)
        {
            for (Runnable runnable : queue) ((SavingImageSource)runnable).abort();
            for (Runnable runnable : queueLocals) ((SavingImageSource)runnable).abort();
        }
    }

    /**
     * Aborts the operation of this image source before it's started.
     */
    private void abort()
    {
        abort = true;
    }


    @Override
    public synchronized void startProduction(ImageConsumer imageConsumer)
    {
        addConsumer(imageConsumer);

        if (producing) return;

        // Start producing
        producing = true;

        try
        {
            // If the image was produced once, we need to disable caching and
            // reused cached image if possible.
            if (produced)
            {
                // If originally downloaded from URL to a cache file, use the cache now
                if (fileSource == null && dest != null && dest.exists()) fileSource = dest;

                // Remove links to the cache and temp so that they aren't overwritten
                dest = null;
                temp = null;
            }

            // Open input stream
            input = openInputStream(fileSource, urlSource);

            // Create an output stream
            File fl = temp == null ? dest : temp;
            output = fl == null ? null : new BufferedOutputStream(new FileOutputStream(fl));

            // Add an observer
            addConsumer(new Consumer());

            // Choose the right executor
            Executor ex = fileSource == null ? executor : executorLocals;
            synchronized (SavingImageSource.class)
            {
                ex.execute(this);
            }
        } catch (FileNotFoundException e)
        {
            LOG.log(Level.WARNING, "Error starting production of an image (file=" +
                fileSource + ", url=" + urlSource + ")", e);
        }
    }

    /**
     * Returns a buffered input stream created from a file or an URL.
     *
     * @param file  file source.
     * @param url   URL source.
     *
     * @return buffered input stream.
     *
     * @throws FileNotFoundException if file is not found.
     */
    private BufferedInputStream openInputStream(File file, URL url)
        throws FileNotFoundException
    {
        InputStream inp;

        if (file == null)
        {
            // Create from URL
            URLInputStream in = new URLInputStream(url);
            netTask = NetManager.register(NetManager.TYPE_ARTICLE_IMAGE, url.toString(), "", in);
            inp = in;
        } else
        {
            // Create from local file
            inp = new FileInputStream(file);
        }

        return new BufferedInputStream(inp);
    }

    @Override
    protected ImageDecoder getDecoder()
    {
        return getDecoder(new SavingInputStream(input, output));
    }

    /**
     * Executed to produce an image.
     */
    public void run()
    {
        try
        {
            if (abort)
            {
                // Abort net task
                if (netTask != null) netTask.abort();

                // Close streams
                if (input != null) input.close();
                if (output != null) output.close();

                // Remove temp
                if (temp != null) temp.delete();

                producing = false;
            } else
            {
                doFetch();
            }
        } catch (Throwable e)
        {
            // Ignore
        } finally
        {
            netTask = null;
        }
    }

    /**
     * Invoked when an image is completely loaded and ready for saving.
     */
    private void onImageComplete()
    {
        try
        {
            try
            {
                if (output != null)
                {
                    output.flush();
                    output.close();
                }

                if (input != null) input.close();

                // Move data from temp to destination
                if (temp != null)
                {
                    if (abort) temp.delete(); else temp.renameTo(dest);
                }
            } finally
            {
                // Image is produced
                producing = false;
                produced = !abort;

                // Unregister
                if (dest != null) ImageFetcher.unregisterDownloadedImage(dest.getName());
            }
        } catch (Throwable e)
        {
            LOG.log(Level.WARNING, "Error closing output", e);
        }
    }

    /**
     * A stream that is reading data from an input stream and writing the same data
     * to the given output stream while returning it to the reader.
     */
    private class SavingInputStream extends FilterInputStream
    {
        private final OutputStream out;

        /**
         * Creates a stream.
         *
         * @param in    stream to read from.
         * @param out   stream to write to.
         */
        private SavingInputStream(InputStream in, OutputStream out)
        {
            super(in);
            this.out = out;
        }

        @Override
        public int read() throws IOException
        {
            checkAbort();

            int b = super.read();
            if (out != null && b != -1) out.write(b);

            return b;
        }

        /**
         * Closes an input stream if aborting is requested.
         *
         * @throws IOException I/O error.
         */
        private void checkAbort() throws IOException
        {
            if (abort) in.close();
        }

        @Override
        public int read(byte b[]) throws IOException
        {
            checkAbort();

            int read = super.read(b);
            if (out != null && read != -1) out.write(b, 0, read);

            return read;
        }

        @Override
        public int read(byte b[], int off, int len) throws IOException
        {
            checkAbort();

            int read = super.read(b, off, len);
            if (out != null && read != -1) out.write(b, off, read);

            return read;
        }

        @Override
        public boolean markSupported()
        {
            // We don't support marking so far as it would require skipping bytes when reading
            // them again from the underlying stream.
            return false;
        }
    }

    /**
     * Image consumer whose goal is to look for image completion and
     * invoke the <code>onImageComplete</code> method.
     */
    private class Consumer implements ImageConsumer
    {
        /**
         * The dimensions of the source image are reported using the setDimensions method call.
         *
         * @param width  the width of the source image
         * @param height the height of the source image
         */
        public void setDimensions(int width, int height)
        {
        }

        /**
         * Sets the extensible list of properties associated with this image.
         *
         * @param props the list of properties to be associated with this image
         */
        public void setProperties(Hashtable<?, ?> props)
        {
        }

        /**
         * Sets the ColorModel object used for the majority of the pixels reported using the setPixels method calls.  Note
         * that each set of pixels delivered using setPixels contains its own ColorModel object, so no assumption should be
         * made that this model will be the only one used in delivering pixel values.  A notable case where multiple
         * ColorModel objects may be seen is a filtered image when for each set of pixels that it filters, the filter
         * determines  whether the pixels can be sent on untouched, using the original ColorModel, or whether the pixels
         * should be modified (filtered) and passed on using a ColorModel more convenient for the filtering process.
         *
         * @param model the specified <code>ColorModel</code>
         *
         * @see java.awt.image.ColorModel
         */
        public void setColorModel(ColorModel model)
        {
        }

        /**
         * Sets the hints that the ImageConsumer uses to process the pixels delivered by the ImageProducer. The
         * ImageProducer can deliver the pixels in any order, but the ImageConsumer may be able to scale or convert the
         * pixels to the destination ColorModel more efficiently or with higher quality if it knows some information about
         * how the pixels will be delivered up front.  The setHints method should be called before any calls to any of the
         * setPixels methods with a bit mask of hints about the manner in which the pixels will be delivered. If the
         * ImageProducer does not follow the guidelines for the indicated hint, the results are undefined.
         *
         * @param hintflags a set of hints that the ImageConsumer uses to process the pixels
         */
        public void setHints(int hintflags)
        {
        }

        /**
         * Delivers the pixels of the image with one or more calls to this method.  Each call specifies the location and
         * size of the rectangle of source pixels that are contained in the array of pixels.  The specified ColorModel
         * object should be used to convert the pixels into their corresponding color and alpha components.  Pixel (m,n) is
         * stored in the pixels array at index (n * scansize + m + off).  The pixels delivered using this method are all
         * stored as bytes.
         *
         * @param x,&nbsp;y the coordinates of the upper-left corner of the area of pixels to be set
         * @param w         the width of the area of pixels
         * @param h         the height of the area of pixels
         * @param model     the specified <code>ColorModel</code>
         * @param pixels    the array of pixels
         * @param off       the offset into the <code>pixels</code> array
         * @param scansize  the distance from one row of pixels to the next in the <code>pixels</code> array
         *
         * @see java.awt.image.ColorModel
         */
        public void setPixels(int x, int y, int w, int h, ColorModel model, byte pixels[], int off, int scansize)
        {
        }

        /**
         * The pixels of the image are delivered using one or more calls to the setPixels method.  Each call specifies the
         * location and size of the rectangle of source pixels that are contained in the array of pixels.  The specified
         * ColorModel object should be used to convert the pixels into their corresponding color and alpha components.
         * Pixel (m,n) is stored in the pixels array at index (n * scansize + m + off).  The pixels delivered using this
         * method are all stored as ints. this method are all stored as ints.
         *
         * @param x,&nbsp;y the coordinates of the upper-left corner of the area of pixels to be set
         * @param w         the width of the area of pixels
         * @param h         the height of the area of pixels
         * @param model     the specified <code>ColorModel</code>
         * @param pixels    the array of pixels
         * @param off       the offset into the <code>pixels</code> array
         * @param scansize  the distance from one row of pixels to the next in the <code>pixels</code> array
         *
         * @see java.awt.image.ColorModel
         */
        public void setPixels(int x, int y, int w, int h, ColorModel model, int pixels[], int off, int scansize)
        {
        }

        /**
         * The imageComplete method is called when the ImageProducer is finished delivering all of the pixels that the
         * source image contains, or when a single frame of a multi-frame animation has been completed, or when an error in
         * loading or producing the image has occured.  The ImageConsumer should remove itself from the list of consumers
         * registered with the ImageProducer at this time, unless it is interested in successive frames.
         *
         * @param status the status of image loading
         *
         * @see java.awt.image.ImageProducer#removeConsumer
         */
        public void imageComplete(int status)
        {
            if (status == STATICIMAGEDONE || status == SINGLEFRAMEDONE)
            {
                removeConsumer(Consumer.this);
                if (status == STATICIMAGEDONE) onImageComplete();
            }
        }
    }
}
