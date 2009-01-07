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
// $Id: ConnectionChecker.java,v 1.21 2006/01/08 05:04:21 kyank Exp $
//

package com.salas.bb.utils;

import com.salas.bb.service.ServerService;
import com.salas.bb.service.ServerServiceException;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Checks for connection to Internet with defined periods of time.
 */
public final class ConnectionChecker
{
    private static final Logger LOG = Logger.getLogger(ConnectionChecker.class.getName());

    // Constants

    /**
     * Default checking interval (60 seconds).
     */
    public static final int DEFAULT_CHECK_INTERVAL = 60;

    private static final String OS_NAME = System.getProperty("os.name");
    private static final String JAVA_VERSION = System.getProperty("java.version");

    // Members

    private final ConnectionState connectionState;

    private final String    version;
    private final long      installationId;
    private final int       runs;
    private final String    accountEmail;
    private final String    accountPassword;

    private int         checkInterval;
    private boolean     started;

    // infoSent is used to determine whether info for statistics update sent or not.
    // It's too expensive to force server kick database number_of_users times per minute to
    // update stats. So, we can do it only once per session while we have once-per-session
    // info to pass.
    private boolean     infoSent;

    private Timer       timer;
    private TimerTask   task;

    /**
     * Creates connection checker.
     *
     * @param aVersion          version of the application.
     * @param aInstallationId   current installation ID.
     * @param aRuns             number of application appRuns.
     * @param anAccountEmail    user's account email.
     * @param anAccountpassword user's account password.
     * @param aConnectionState  state of connection object to update and consult.
     */
    public ConnectionChecker(String aVersion, long aInstallationId, int aRuns,
        String anAccountEmail, String anAccountpassword, ConnectionState aConnectionState)
    {
        connectionState = aConnectionState;
        version = aVersion;
        installationId = aInstallationId;
        runs = aRuns;
        accountEmail = anAccountEmail;
        accountPassword = anAccountpassword;

        if (LOG.isLoggable(Level.CONFIG))
        {
            LOG.config("Version=" + aVersion + " id=" + aInstallationId + " appRuns=" + aRuns);
            LOG.config("OS=" + OS_NAME + " Java=" + JAVA_VERSION);

            if (LOG.isLoggable(Level.FINE)) LOG.fine("Account Email=" + accountEmail);
        }

        timer = new Timer(true);
        task = new IntervalCheckTask();

        checkInterval = DEFAULT_CHECK_INTERVAL;
        started = false;
        infoSent = false;
    }

    /**
     * Starts checking of connection with currently set interval.
     * Checking is not started if interval is less than 1 second.
     */
    public synchronized void start()
    {
        if (started) return;

        // Register new checker task
        timer.scheduleAtFixedRate(task, 1, checkInterval * Constants.MILLIS_IN_SECOND);
        started = true;
    }

    // Timer task

    /**
     * Runs once per defined interval of time and tries to get
     * measure file from defined URL.
     */
    private class IntervalCheckTask extends TimerTask
    {
        /**
         * The action to be performed by this timer task.
         */
        public void run()
        {
            if (connectionState.isOnline())
            {
                boolean serviceAccessible = isServiceAccessible();
                connectionState.setServiceAccessible(serviceAccessible);

                synchronized (this)
                {
                    if (serviceAccessible && !infoSent)
                    {
                        sendStatisticalInformation();
                    }
                }
            }
        }

        /**
         * Pings the server with an empty request to see if it's accessible.
         *
         * @return <code>TRUE</code> if the service connection attempt was successful.
         */
        private boolean isServiceAccessible()
        {
            if (LOG.isLoggable(Level.FINER)) LOG.finer("Ping");

            boolean connected = ServerService.ping();

            if (connected && LOG.isLoggable(Level.FINER)) LOG.finer("Pong");

            return connected;
        }

        /**
         * Sends package of statistical information to server.
         */
        private void sendStatisticalInformation()
        {
            LOG.fine("Sending statistical information");

            try
            {
                ServerService ss = ServerService.getInstance();
                ss.ping(installationId, version, runs, OS_NAME, JAVA_VERSION, accountEmail,
                    accountPassword);

                infoSent = true;

                LOG.finer("Statistical information successfully sent.");
            } catch (ServerServiceException e)
            {
                // That's ok. Let's try later.
            }
        }
    }
}
