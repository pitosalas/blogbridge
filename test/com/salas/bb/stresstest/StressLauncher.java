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
// $Id: StressLauncher.java,v 1.7 2007/10/11 09:09:39 spyromus Exp $
//

package com.salas.bb.stresstest;

import com.salas.bb.core.ApplicationLauncher;
import com.salas.bb.installation.Installer;

import java.io.*;
import java.util.Properties;
import java.util.prefs.Preferences;

/**
 * Main stress test launcher.
 */
public class StressLauncher
{
    private static final String WORKING_DIR = "bb/stress-test";

    /**
     * Main method.
     *
     * @param args arguments.
     */
    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            System.out.println("Usage: application settings_file_name");
        } else
        {
            String settingsName = args[0];
            args[0] = null;
            setupJPF(args);
            try
            {
                preparePlayground(settingsName, WORKING_DIR);
                installScriptRunner();
                ApplicationLauncher.main(new String[] { WORKING_DIR });
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private static void setupJPF(String[] args)
    {
//        Config config = JPF.createConfig(args);
//        JPF jpf = new JPF(config);
//        HeapTracker tracker = new HeapTracker();
//        jpf.addSearchListener(tracker);
//        jpf.addVMListener(tracker);
//        jpf.run();
    }

    // Resets the database at the playground and loads settings from settings file.
    private static void preparePlayground(String settingsFilename, String dir)
        throws IOException
    {
        loadPreferences(settingsFilename);
        initDataFiles(dir);
        applicationSetup();
    }

    private static void applicationSetup()
    {
        // Register ourselves
        Preferences prefs = Preferences.userRoot().node(WORKING_DIR);
        prefs.putLong(ApplicationLauncher.KEY_INSTALLATION_ID, 1);
        prefs.put(ApplicationLauncher.KEY_INSTALLATION_VERSION,
            ApplicationLauncher.getCurrentVersion());
        prefs.put("setup.acceptedLicense", ApplicationLauncher.getCurrentVersion());

        // Disable tips of the day
        prefs.put("tipOfTheDay.isShowing", Boolean.FALSE.toString());
    }

    private static void initDataFiles(String dir)
        throws IOException
    {
        String contextPath = getContextPath(dir);

        initDatabaseFiles(contextPath);
        initModelFile(contextPath);
    }

    private static void initModelFile(String aContextPath)
        throws IOException
    {
        // Create raw model and persist it
        FileOutputStream fos = new FileOutputStream(aContextPath + "blogbridge.xml");
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        PrintWriter pw = new PrintWriter(bos);

        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<java version=\"1.4.2_04\" class=\"java.beans.XMLDecoder\">");
        pw.println(" <object class=\"com.salas.bb.core.GlobalModel\"/>");
        pw.println("</java>");

        pw.close();
        bos.close();
        fos.close();
    }

    private static void initDatabaseFiles(String aContextPath)
        throws IOException
    {
        File context = new File(aContextPath);
        if (context.exists())
        {
            if (context.isFile())
            {
                throw new IOException("Working directory is a file.");
            }

            if (!clearDirectory(context))
            {
                throw new IOException("Couldn't clear working directory.");
            }
        } else
        {
            if (!context.mkdir())
            {
                throw new IOException("Couldn't create working directory.");
            }
        }

        // Put fresh data files
        Installer.cleanDatabase(aContextPath);
    }

    private static boolean clearDirectory(File directory)
    {
        return true;
    }

    private static void loadPreferences(String settingsFilename)
        throws IOException
    {
        File settings = new File(settingsFilename);
        Properties props = System.getProperties();
        props.load(new FileInputStream(settings));
    }

    private static String getContextPath(String dir)
    {
        return System.getProperty("user.home") + File.separatorChar + '.' + dir + File.separatorChar;
    }

    private static void installScriptRunner() throws Exception
    {
        new StressMaker();
    }
}
