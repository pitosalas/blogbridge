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
// $Id: FileUtils.java,v 1.10 2007/03/21 18:57:20 spyromus Exp $
//

package com.salas.bb.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Follection of utils for file system calls.
 */
public final class FileUtils
{
    /**
     * Hidden constructor of utility class.
     */
    private FileUtils()
    {
    }

    /**
     * Copies file from it's location to destination dir.
     *
     * @param file      file to copy.
     * @param destDir   dir.
     *
     * @throws java.io.IOException if I/O error happens.
     */
    public static void copyFileToDir(File file, File destDir)
        throws IOException
    {
        String name = file.getName();
        String filename = destDir.getAbsolutePath() + File.separator + name;

        File destFile = new File(filename);
        destFile.createNewFile();

        copyFileToFile(file, destFile);
    }

    /**
     * Copies one file to another.
     *
     * @param source    source file.
     * @param dest      destination file.
     *
     * @throws java.io.IOException if I/O error happens.
     */
    public static void copyFileToFile(File source, File dest)
        throws IOException
    {
        FileChannel sourceChannel = null;
        FileChannel targetChannel = null;

        try
        {
            sourceChannel = new FileInputStream(source).getChannel();
            targetChannel = new FileOutputStream(dest).getChannel();
            sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
            targetChannel.force(true);
        } finally
        {
            if (sourceChannel != null) sourceChannel.close();
            if (targetChannel != null) targetChannel.close();
        }
    }

    /**
     * Creates hierarchy of folders.
     *
     * @param aFolder folder to create.
     *
     * @return <code>TRUE</code> if was successfully created.
     */
    public static boolean createFoldersHierarchy(File aFolder)
    {
        boolean created = true;

        if (aFolder != null && !aFolder.exists())
        {
            File parent = aFolder.getParentFile();
            created = createFoldersHierarchy(parent) && aFolder.mkdir();
        }

        return created;
    }

    /**
     * Removes dir and all sub-dirs.
     *
     * @param dir dir to start removing from.
     */
    public static void rmdir(File dir)
    {
        File[] nodes = dir.listFiles();

        if (nodes != null)
        {
            for (File node : nodes)
            {
                if (node.isFile()) node.delete(); else rmdir(node);
            }

            dir.delete();
        }
    }

    /**
     * Copies a directory or a file from source to destination.
     *
     * @param src   source.
     * @param dst   destination.
     *
     * @throws java.io.IOException in case of I/O error.
     */
    public static void copyRec(File src, File dst)
        throws IOException
    {
        if (src == null || dst == null) return;
        if (dst.isFile()) return;

        if (src.isFile())
        {
            // Copy file
            copyFileToDir(src, dst);
        } else
        {
            // Construct destination directory file
            String name = src.getName();
            File newDst = new File(dst, name);

            if (!newDst.exists())
            {
                // Create the destination directory if it doesn't exist yet
                newDst.mkdir();
            } else if (newDst.isFile())
            {
                throw new IOException("There's a file " + newDst.getName() + " in place of directory");
            }

            // Copy files and directories from the source to a new destination recursively
            for (File file : src.listFiles()) copyRec(file, newDst);
        }
    }
}
