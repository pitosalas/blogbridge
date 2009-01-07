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
// $Id: ImageFetcher.java,v 1.16 2008/04/07 18:26:04 spyromus Exp $
//

package com.salas.bb.utils.uif.images;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Image fetcher is an utility framework entry point. This framework is aimed on
 * non-blocking loading of images while keeping the internal media pipeline free.
 * It solves the problem when the application requires several pictures (big or
 * from slow servers) and requires some local resource. As both of these types
 * of images walk through the same pipeline the first slow group may block the
 * trafic and application will freeze. With this small framework we separate the
 * slow group from global media pipeline in their own processing threads.
 *
 * <p>Framework has several thread and a queue of tasks. The loading of images is
 * similar to the usual <code>Toolkit.getDefaultToolkit().getImage(...)</code>
 * calls and can simply replace them.</p>
 *
 * <p>In addition, it uses modern <code>URLInputStream</code> implementation which
 * knows how to control bandwidth, do seamless resuming and pause/unpause. When
 * the actual fetching starts (the moment when image consumers request for data)
 * the framework registers activity in <code>NetManager</code> and continue with
 * data loading.</p>
 */
public final class ImageFetcher
{
    private static final Logger LOG = Logger.getLogger(ImageFetcher.class.getName());

    private static Cache cache;
    private static Map<String, Image> downloadedImages;

    static
    {
        downloadedImages = new HashMap<String, Image>();
    }

    /**
     * Sets the cache to use.
     *
     * @param aCache cache.
     */
    public static void setCache(Cache aCache)
    {
        cache = aCache;
    }

    /**
     * Clears the images loading queue.
     */
    public static void clearQueue()
    {
        // Removes everything from the queue
        SavingImageSource.clearQueues();
        downloadedImages.clear();
        cache.verifyLimits();
    }

    /**
     * Loads image from the source URL.
     *
     * @param source source URL.
     *
     * @return image.
     */
    public static synchronized Image load(URL source)
    {
        Image img;

        // See if we have an image in cache
        img = cache.get(source);
        if (img == null)
        {
            // There's no cached image, see if we are downloading something
            String cacheFN = cache.urlToFilename(source);
            img = getDownloadedImage(cacheFN);
            if (img == null)
            {
                // Image is not being downloaded, create a temp file and start downloading
                File cacheFile = cache.getCachedFile(source);
                try
                {
                    File tempFile = new File(cacheFile.getAbsolutePath() + ".tmp");
                    tempFile.createNewFile();
                    tempFile.deleteOnExit();

                    img = loadImage(source, tempFile, cacheFile);

                    // Register the download
                    registerDownloadedImage(cacheFN, img);
                } catch (IOException e)
                {
                    LOG.log(Level.WARNING, "Error loading image", e);
                }
            }
        }

        return img;
    }

    /**
     * Creates a producer for source and returns an image.
     *
     * @param source    source URL.
     * @param temp      file to hold temporary information.
     * @param dest      final destination file to hold cached image data.
     *
     * @return image.
     */
    public static Image loadImage(URL source, File temp, File dest)
    {
        // Create an image producer
        SavingImageSource producer = null;
        try
        {
            producer = new SavingImageSource(source, temp, dest);
        } catch (IOException e)
        {
            LOG.log(Level.WARNING, "Error creating image producer", e);
        }

        // Start loading the image
        return producer == null ? null : Toolkit.getDefaultToolkit().createImage(producer);
    }

    /**
     * Creates a producer for source and returns an image.
     *
     * @param file file to load an image from.
     *
     * @return image.
     */
    public static Image loadImage(File file)
    {
        // Create an image producer
        SavingImageSource producer = null;
        try
        {
            producer = new SavingImageSource(file);
        } catch (IOException e)
        {
            LOG.log(Level.WARNING, "Error creating image producer", e);
        }

        // Start loading the image
        return producer == null ? null : Toolkit.getDefaultToolkit().createImage(producer);
    }

    /**
     * Invoked when loading of an image starts to avoid duplicate loadings.
     *
     * @param fn    cached image file name.
     * @param img   image.
     */
    public static void registerDownloadedImage(String fn, Image img)
    {
        downloadedImages.put(fn, img);
    }

    /**
     * Returns an image that is currently downloaded by its cached name.
     *
     * @param fn    cached image file name.
     *
     * @return image or <code>NULL</code> if the image isn't currently downloaded.
     */
    public static Image getDownloadedImage(String fn)
    {
        return downloadedImages.get(fn);
    }

    /**
     * Invoked when the image is completely downloaded and available to the cache.
     *
     * @param fn cached image filename.
     */
    public static void unregisterDownloadedImage(String fn)
    {
        synchronized (ImageFetcher.class)
        {
            downloadedImages.remove(fn);
        }
        
        cache.verifyLimits();
    }
}
