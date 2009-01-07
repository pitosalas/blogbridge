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
// $Id: PackageInstaller.java,v 1.3 2006/01/08 04:57:15 kyank Exp $
//

package com.salas.bb.updates;

import com.jgoodies.uif.application.Application;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Installes packages in system-specific way.
 */
public class PackageInstaller
{
    private static final Logger LOG = Logger.getLogger(PackageInstaller.class.getName());

    /**
     * Starts installer and closes this application.
     *
     * @param aPackage      package to install.
     * @param aDirectory    directory this package was placed to.
     *
     * @throws IOException  if installer cannot be started.
     */
    public static void startInstaller(Location aPackage, File aDirectory)
        throws IOException
    {
        if (aPackage == null) return;

        launchInstaller(aPackage, aDirectory);
        Application.close();
    }

    /**
     * Launches package installation.
     *
     * @param aPackage      package.
     * @param aDirectory    directory.
     *
     * @throws IOException  if installer cannot be started.
     */
    private static void launchInstaller(Location aPackage, File aDirectory)
        throws IOException
    {
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("Launching package type: " + aPackage.getType());
        }

        if (aPackage.getType().equals(Location.TYPE_WINDOWS))
        {
            launchWindowsInstaller(aPackage, aDirectory);
        }
    }

    /**
     * Launches windows package installation.
     *
     * @param aPackage      package.
     * @param aDirectory    directory.
     *
     * @throws IOException  if installer cannot be started.
     */
    private static void launchWindowsInstaller(Location aPackage, File aDirectory)
        throws IOException
    {
        String filename = aPackage.getFilename();
        String command = aDirectory.getAbsolutePath() + File.separator + filename;

        if (LOG.isLoggable(Level.FINE)) LOG.fine("Launching " + command);

        Runtime.getRuntime().exec(command);
    }
}