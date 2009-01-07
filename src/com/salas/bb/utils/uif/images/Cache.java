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
// $Id: Cache.java,v 1.19 2008/04/04 14:35:44 spyromus Exp $
//

package com.salas.bb.utils.uif.images;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;

/**
 * Cache of images. The cache is disk-based, but it also has memory map of images which are
 * currently loaded by someone. The cache uses separate thread for flushing of images to disk.
 * The maximum disk usage amount can be specified and the writer thread will check if current
 * saved data is not exceeding the limit after writing the next image. If the limits are exceeded
 * it will remove several last-used images to get back into the limits.
 */
public class Cache
{
    private static final Logger LOG = Logger.getLogger(Cache.class.getName());

    private File cacheFolder;
    private long sizeLimit;

    /**
     * Creates cache.
     *
     * @param aCacheFolder  folder to use for caching.
     * @param aSizeLimit    maximum folder contents size.
     */
    public Cache(File aCacheFolder, long aSizeLimit)
    {
        cacheFolder = aCacheFolder;
        sizeLimit = aSizeLimit;
        if (!cacheFolder.exists()) cacheFolder.mkdir();
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
        if (url == null || isLocal(url)) return null;

        Image image = null;

        File file = new File(cacheFolder, urlToFilename(url));
        if (file.exists())
        {
            image = ImageFetcher.loadImage(file);
            file.setLastModified(System.currentTimeMillis());
        }

        return image;
    }

    private static boolean isLocal(URL url)
    {
        return url.getProtocol().equals("file");
    }

    public File getCachedFile(URL url)
    {
        return new File(cacheFolder, urlToFilename(url));
    }

    /**
     * Converts URL into cached file name.
     *
     * @param url URL.
     *
     * @return file name.
     */
    public String urlToFilename(URL url)
    {
        int hashCode = url.toString().toLowerCase().hashCode();
        return Integer.toHexString(hashCode).toUpperCase();
    }

    /** Checks if we still in limits and removes some old files if we aren't. */
    public void verifyLimits()
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
        Arrays.sort(files, new FileAccessComparator());

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
        for (File file : files) size += file.length();

        return size;
    }

    /**
     * Compares files by their modification times.
     */
    private static class FileAccessComparator implements Comparator<File>
    {
        /**
         * Compares two files by their modification times.
         *
         * @param f1    first file.
         * @param f2    second file.
         *
         * @return result.
         */
        public int compare(File f1, File f2)
        {
            long l1 = f1.lastModified();
            long l2 = f2.lastModified();

            return l1 == l2 ? 0 : l1 < l2 ? -1 : 1;
        }
    }
}
