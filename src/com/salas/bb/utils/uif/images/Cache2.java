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
// $Id: Cache2.java,v 1.1 2006/11/17 11:15:17 spyromus Exp $
//

package com.salas.bb.utils.uif.images;

import EDU.oswego.cs.dl.util.concurrent.Executor;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageConsumer;
import java.awt.image.ColorModel;
import java.lang.ref.SoftReference;

import com.salas.bb.utils.concurrency.ExecutorFactory;
import com.salas.bb.utils.i18n.Strings;

import javax.imageio.ImageIO;

/**
 * Cache of images. The cache is disk-based, but it also has memory map of images which are
 * currently loaded by someone. The cache uses separate thread for flushing of images to disk.
 * The maximum disk usage amount can be specified and the writer thread will check if current
 * saved data is not exceeding the limit after writing the next image. If the limits are exceeded
 * it will remove several last-used images to get back into the limits.
 */
public class Cache2
{
    private static final Logger LOG = Logger.getLogger(Cache2.class.getName());

    private File cacheFolder;
    private long sizeLimit;

    private Executor executor;

    /**
     * Creates cache.
     *
     * @param aCacheFolder  folder to use for caching.
     * @param aSizeLimit    maximum folder contents size.
     */
    public Cache2(File aCacheFolder, long aSizeLimit)
    {
        cacheFolder = aCacheFolder;
        sizeLimit = aSizeLimit;

        executor = ExecutorFactory.createPooledExecutor("Cached Images Writer", 1, 10000);
        if (!cacheFolder.exists()) cacheFolder.mkdir();
    }

    /**
     * Puts the image into cache.
     *
     * @param url   URL of the image.
     * @param image image to cache.
     */
    public void put(URL url, Image image)
    {
        if (url == null || image == null || isLocal(url)) return;

        // Record in memory map
        recordMemoryImage(image, url);

        if (LOG.isLoggable(Level.FINE)) LOG.fine("Put " + url);

        image.getSource().addConsumer(new Cache2.ImageWaiter(url, image));
    }

    /**
     * Invoked when image is ready for writing.
     *
     * @param url   URL of the image.
     * @param image image to cache.
     */
    private void onImageReady(URL url, Image image)
    {
        Cache2.WriteTask writeTask = new Cache2.WriteTask(url, image);

        try
        {
            executor.execute(writeTask);
        } catch (InterruptedException e)
        {
            LOG.warning(Strings.error("img.image.caching.executed.directly"));
            writeTask.run();
        }
    }

    /**
     * Gets the image from cache.
     *
     * @param url   URL of the image.
     *
     * @return image or <code>NULL</code>.
     */
    public Image get(URL url)
    {
        if (url == null) return null;

        // Check the memory references list
        Image image = lookupMemoryImage(url);

        // Return <code>NULL</code> if the link is local or image if it was found
        if (image != null || isLocal(url)) return image;

        File file = new File(cacheFolder, urlToFilename(url));
        if (file.exists())
        {
            try
            {
                url = file.toURI().toURL();
                image = ImageFetcher.load(url);
                file.setLastModified(System.currentTimeMillis());

                // Record loaded image in the memory map
                recordMemoryImage(image, url);
            } catch (IOException e)
            {
                LOG.log(Level.SEVERE, Strings.error("img.failed.to.load.image"), e);
            }
        }

        return image;
    }

    private static boolean isLocal(URL url)
    {
        return url.getProtocol().equals("file");
    }

    /**
     * Converts URL into cached file name.
     *
     * @param url URL.
     *
     * @return file name.
     */
    private static String urlToFilename(URL url)
    {
        int hashCode = url.toString().toLowerCase().hashCode();
        return Integer.toHexString(hashCode).toUpperCase();
    }

    /**
     * Task for the writer thread.
     */
    private class WriteTask implements Runnable
    {
        private URL url;
        private Image image;

        /**
         * Creates task.
         *
         * @param aUrl      URL of the image.
         * @param aImage    image to write.
         */
        public WriteTask(URL aUrl, Image aImage)
        {
            url = aUrl;
            image = aImage;
        }

        /**
         * Writes image data to disk.
         */
        public void run()
        {
            File file = new File(cacheFolder, urlToFilename(url));

            if (file.exists()) return;

            if (LOG.isLoggable(Level.FINE)) LOG.fine("Writing " + url);

            boolean created = false;
            try
            {
                // Reserve file
                created = file.createNewFile();

                BufferedImage buf = null;
                if (image instanceof BufferedImage)
                {
                    buf = (BufferedImage)image;
                } else
                {
                    int width = image.getWidth(null);
                    int height = image.getHeight(null);

                    // We save only non-empty images
                    if (width > 0 && height > 0)
                    {
                        buf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                        Graphics g = buf.createGraphics();
                        g.drawImage(image, 0, 0, null);
                    }
                }

                // If buffer was successfully created and filled
                if (buf != null)
                {
                    ImageIO.write(buf, "PNG", file);
                    verifyLimits();
                }
            } catch (Throwable e)
            {
                LOG.log(Level.WARNING, Strings.error("img.cache.writing.failed"), e);
                if (created) file.delete();
            }
        }

        /** Checks if we still in limits and removes some old files if we aren't. */
        private void verifyLimits()
        {
            long size = calcSize();
            if (size > sizeLimit)
            {
                removeOldEntries(size - sizeLimit);
            }
        }

        /**
         * Removes some entries to free minimum <code>size</code> number of bytes.
         *
         * @param size amount to free.
         */
        private void removeOldEntries(long size)
        {
            File[] files = cacheFolder.listFiles();
            Arrays.sort(files, new Cache2.FileAccessComparator());

            long leftToFree = size;
            for (int i = 0; leftToFree > 0 && i < files.length - 1; i++)
            {
                File file = files[i];
                if (file.isFile() && file.exists())
                {
                    if (file.delete()) leftToFree -= file.length();
                }
            }
        }

        /**
         * Returns the size of directory contents.
         *
         * @return size.
         */
        private long calcSize()
        {
            long size = 0;

            File[] files = cacheFolder.listFiles();
            for (int i = 0; i < files.length; i++)
            {
                File file = files[i];
                size += file.isFile() ? file.length() : 0;
            }

            return size;
        }
    }

    /**
     * Compares files by their modification times.
     */
    private static class FileAccessComparator implements Comparator
    {
        /**
         * Compares two files by their modification times.
         *
         * @param o1    first file.
         * @param o2    second file.
         *
         * @return result.
         */
        public int compare(Object o1, Object o2)
        {
            File f1 = (File)o1;
            File f2 = (File)o2;

            long l1 = f1.lastModified();
            long l2 = f2.lastModified();

            return l1 == l2 ? 0 : l1 < l2 ? -1 : 1;
        }
    }

    /**
     * Waits for image to be loaded and calls engine to save it on to drive.
     */
    private class ImageWaiter implements ImageConsumer
    {
        private final URL imageURL;
        private final Image image;

        /**
         * Creates consumer-waiter.
         *
         * @param aImageURL original image URL.
         * @param anImage   image object.
         */
        public ImageWaiter(URL aImageURL, Image anImage)
        {
            imageURL = aImageURL;
            image = anImage;
        }

        /**
         * The imageComplete method is called when the ImageProducer is finished delivering all of
         * the pixels that the source image contains, or when a single frame of a multi-frame
         * animation has been completed, or when an error in loading or producing the image has
         * occured.  The ImageConsumer should remove itself from the list of consumers registered
         * with the ImageProducer at this time, unless it is interested in successive frames.
         *
         * @param status the status of image loading.
         */
        public void imageComplete(int status)
        {
            if (status == STATICIMAGEDONE || status == SINGLEFRAMEDONE)
            {
                image.getSource().removeConsumer(this);
                if (status == STATICIMAGEDONE) onImageReady(imageURL, image);
            }
        }

        /**
         * Sets the ColorModel object used for the majority of the pixels reported using the
         * setPixels method calls.
         *
         * @param model the specified <code>ColorModel</code>.
         */
        public void setColorModel(ColorModel model)
        {
        }

        /**
         * The dimensions of the source image are reported using the setDimensions method call.
         *
         * @param width  the width of the source image.
         * @param height the height of the source image.
         */
        public void setDimensions(int width, int height)
        {
        }

        /**
         * Sets the hints that the ImageConsumer uses to process the pixels delivered by the
         * ImageProducer.
         *
         * @param hintflags a set of hints that the ImageConsumer uses to process the pixels.
         */
        public void setHints(int hintflags)
        {
        }

        /**
         * Delivers the pixels of the image with one or more calls to this method.
         *
         * @param x         the coordinate of the upper-left corner of the area of pixels to be set.
         * @param y         the coordinate of the upper-left corner of the area of pixels to be set.
         * @param w         the width of the area of pixels.
         * @param h         the height of the area of pixels.
         * @param model     the specified <code>ColorModel</code>.
         * @param pixels    the array of pixels.
         * @param off       the offset into the <code>pixels</code> array.
         * @param scansize  the distance from one row of pixels to the next in the.
         *                  <code>pixels</code> array.
         */
        public void setPixels(int x, int y, int w, int h, ColorModel model, byte pixels[], int off,
            int scansize)
        {
        }

        /**
         * The pixels of the image are delivered using one or more calls to the setPixels method.
         *
         * @param x         the coordinate of the upper-left corner of the area of pixels to be set.
         * @param y         the coordinate of the upper-left corner of the area of pixels to be set.
         * @param w         the width of the area of pixels.
         * @param h         the height of the area of pixels.
         * @param model     the specified <code>ColorModel</code>.
         * @param pixels    the array of pixels.
         * @param off       the offset into the <code>pixels</code> array.
         * @param scansize  the distance from one row of pixels to the next in the
         *                  <code>pixels</code> array.
         */
        public void setPixels(int x, int y, int w, int h, ColorModel model, int pixels[], int off,
            int scansize)
        {
        }

        /**
         * Sets the extensible list of properties associated with this image.
         *
         * @param props the list of properties to be associated with this image.
         */
        public void setProperties(Hashtable props)
        {
        }
    }

    // --------------------------------------------------------------------------------------------
    // Memory map for loaded images
    // --------------------------------------------------------------------------------------------

    // Memory map
    private final Map memoryMap = new HashMap();

    /**
     * Records an image in the memory map.
     *
     * @param image image.
     * @param url   its URL.
     */
    private void recordMemoryImage(Image image, URL url)
    {
        if (image == null || url == null) return;

        String key = url.toString();
        synchronized (memoryMap)
        {
            memoryMap.put(key, new SoftReference(image));
        }

        cleanMemoryMap();
    }

    /**
     * Looks up an image by its URL.
     *
     * @param url URL of an image.
     *
     * @return image or <code>NULL</code>.
     */
    private Image lookupMemoryImage(URL url)
    {
        Image image = null;

        if (url != null)
        {
            String key = url.toString();
            synchronized (memoryMap)
            {
                SoftReference r = (SoftReference)memoryMap.get(key);
                image = r == null ? null : (Image)r.get();
            }
        }

        return image;
    }

    /** Runs through the memory map and removes zombi-records. */
    private void cleanMemoryMap()
    {
        synchronized (memoryMap)
        {
            Iterator it = memoryMap.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry entry = (Map.Entry)it.next();
                SoftReference r = (SoftReference)entry.getValue();
                if (r.get() == null) it.remove();
            }
        }
    }
}
