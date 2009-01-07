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
// $Id: Locker.java,v 1.13 2006/12/02 11:34:44 spyromus Exp $
//

package com.salas.bb.utils.locker;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * The locker makes sure that only one instance of it connected to the same lock file
 * is started at a time. The locking process is based on the file lock introduced in
 * Java 1.4. The lock can be established only once and is released automatically on
 * the application exit.
 */
public class Locker
{
    private static final Logger LOG = Logger.getLogger(Locker.class.getName());

    /**
     * Tries to lock the given file for writing.
     *
     * @param lockFile lock file.
     *
     * @return the lock, if it was established, or <code>NULL</code>.
     */
    public static FileLock tryLocking(File lockFile)
    {
        FileLock lock = null;

        try
        {
            if (!lockFile.exists())
            {
                File dir = lockFile.getParentFile();
                if (!dir.exists()) dir.mkdirs();
                lockFile.createNewFile();
                lockFile.deleteOnExit();
            }

            FileChannel lockChannel = new FileOutputStream(lockFile).getChannel();
            lock = lockChannel.tryLock();
        } catch (IOException e)
        {
            LOG.log(Level.WARNING, "Unable to create lock", e);
        }

        return lock;
    }
}
