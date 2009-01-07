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
// $Id: AbstractLockupHandler.java,v 1.16 2007/03/23 19:42:47 spyromus Exp $
//

package com.salas.bb.core.actions;

import com.salas.bb.core.ApplicationLauncher;
import com.salas.bb.core.GlobalController;
import com.salas.bb.utils.BrowserLauncher;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract action for handling of lockups. Has tools for gathering information and
 * sending it to the service.
 */
public abstract class AbstractLockupHandler extends AbstractAction
{
    private static final int STACK_DUMP_SIZE_BLOCKED = 50;
    private static final int STACK_DUMP_SIZE_NORMAL = 3;

    /**
     * Creates lockup handler.
     */
    protected AbstractLockupHandler()
    {
    }

    /**
     * Sends message and details to log and service.
     *
     * @param aMessage  message.
     * @param aDetails  details.
     */
    protected void report(String aMessage, String aDetails)
    {
        if (getLogger().isLoggable(Level.INFO)) getLogger().info("Reporting...");
        getLogger().severe(aMessage + "\n" + aDetails);
    }

    /**
     * Terminates application.
     *
     * @param aDoNormalExit TRUE to try follow correct sequence with saving model and preferences.
     */
    protected void terminate(boolean aDoNormalExit)
    {
        if (getLogger().isLoggable(Level.INFO)) getLogger().info("Terminating...");

        if (aDoNormalExit)
        {
            GlobalController controller = GlobalController.SINGLETON;
            if (controller != null)
            {
                controller.prepareToClose(true);
            }
        }

        System.exit(1);
    }

    /**
     * Returns logger to user for logging.
     *
     * @return logger to user for logging.
     */
    protected abstract Logger getLogger();

    /**
     * Collects details: version of VM, locker event, threads dump.
     *
     * @param event locker.
     *
     * @return details.
     */
    protected String collectDetails(AWTEvent event)
    {
        StringBuffer buf = new StringBuffer();

        buf.append(getVMVersion());
        buf.append("\n");

        if (event != null)
        {
            buf.append(event.toString());
            buf.append("\n\n");
        }

        buf.append(getProperties());

        buf.append(getThreadsDump(getLogger()));

        return buf.toString();
    }

    /**
     * Returns application properties.
     *
     * @return application properties.
     */
    private String getProperties()
    {
        StringBuffer buf = new StringBuffer();

        buf.append("Properties:\n");

        buf.append("Installation ID: ");
        buf.append(ApplicationLauncher.getInstallationId()).append("\n");
        buf.append("Installation Runs: ");
        buf.append(ApplicationLauncher.getInstallationRuns()).append("\n");
        buf.append("JGoodies - System Exit Allowed: ");
        buf.append(System.getProperty("jgoodies.SystemExitAllowed")).append("\n");
        buf.append("Context Path: ");
        buf.append(ApplicationLauncher.getContextPath()).append("\n");
        buf.append("Running under JWS: ");
        buf.append(BrowserLauncher.isRunningUnderJWS()).append("\n");
        buf.append("OS: ");
        buf.append(System.getProperty("os.name")).append("\n");
        buf.append("\n");

        buf.append("\n");

        return buf.toString();
    }

    /**
     * Returns VM version information.
     *
     * @return VM information.
     */
    private String getVMVersion()
    {
        OutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);

        sun.misc.Version.print(ps);

        return os.toString();
    }

    /**
     * Prints information about all threads.
     *
     * @param aLogger logger to use for error reporting.
     *
     * @return threads dump.
     */
    public static String getThreadsDump(Logger aLogger)
    {
        ThreadGroup rootTG = findRootTG(Thread.currentThread().getThreadGroup());
        int activeThreads = rootTG.activeCount();
        Thread[] threads = new Thread[activeThreads];
        int actualThreads = rootTG.enumerate(threads, true);

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < actualThreads; i++)
        {
            Thread thread = threads[i];
            buf.append(getThreadInfo(thread));
        }

        buf.append(getDeadlockedThreads());

        return buf.toString();
    }

    /**
     * Gathers info about deadlocked threads.
     *
     * @return threads summary.
     */
    private static String getDeadlockedThreads()
    {
        StringBuffer buf = new StringBuffer();

        // Get ID's of all threads
        ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
        long[] ids = mbean.getAllThreadIds();
        if (ids != null)
        {
            for (long id : ids)
            {
                ThreadInfo threadInfo = mbean.getThreadInfo(id, STACK_DUMP_SIZE_BLOCKED);
                buf.append("\n").append(threadInfo).append("\n");

                // Dump the monitor blocks
                String lockName = threadInfo.getLockName();
                if (lockName != null)
                {
                    buf.append("  waiting for ");
                    buf.append(lockName);
                    buf.append(" blocked by ");
                    buf.append(threadInfo.getLockOwnerName());
                    buf.append("@").append(threadInfo.getLockOwnerId()).append("\n");
                }

                // Write the stack trace of thread
                StackTraceElement[] traces = threadInfo.getStackTrace();
                int steps = requiresDetails(threadInfo)
                    ? traces.length : Math.min(STACK_DUMP_SIZE_NORMAL, traces.length);

                for (int j = 0; j < steps; j++)
                {
                    buf.append("\t").append(traces[j]).append("\n");
                }
            }
        }

        return buf.toString();
    }

    /**
     * Returns <code>TRUE</code> if the thread is reported as BLOCKED by someone or it is EDT
     * thread.
     *
     * @param aThreadInfo thread info to analyze.
     *
     * @return thread info.
     */
    private static boolean requiresDetails(Object aThreadInfo)
    {
        String threadInfo = aThreadInfo.toString();
        return threadInfo.indexOf("BLOCKED") != -1 || threadInfo.indexOf("AWT-EventQueue") != -1;
    }

    /**
     * Prints information about single thread.
     *
     * @param thread thread.
     *
     * @return thread info.
     */
    private static String getThreadInfo(Thread thread)
    {
        return "Thread: " + thread.getName() +
               " Running=" + thread.isAlive() +
               " Daemon=" + thread.isDaemon() +
               " Priority=" + thread.getPriority() + "\n";
    }

    /**
     * Finds root group of threads.
     *
     * @param threadGroup group of threads to find parents for.
     * 
     * @return root group.
     */
    private static ThreadGroup findRootTG(ThreadGroup threadGroup)
    {
        return (threadGroup.getParent() == null) ? threadGroup : threadGroup.getParent();
    }
}
