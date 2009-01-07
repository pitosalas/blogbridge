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
// $Id: BrowserLauncher.java,v 1.26 2007/05/18 08:09:42 spyromus Exp $
//

package com.salas.bb.utils;

import com.jgoodies.uif.util.SystemUtils;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bbnative.OpenURL;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BrowserLauncher is intended to cover internal implementation of calling default
 * client browser.
 */
public abstract class BrowserLauncher
{
    private static final Logger LOG = Logger.getLogger(BrowserLauncher.class.getName());
    private static final String THREAD_NAME = "Starting Browser";

    private static BasicService basicService;
    private static boolean nativeErrored = false;

    private static final List MOZILLA_TYPE_BROWSERS;

    // Initialize service
    static
    {
        MOZILLA_TYPE_BROWSERS = new ArrayList();
        MOZILLA_TYPE_BROWSERS.add("netscape");
        MOZILLA_TYPE_BROWSERS.add("mozilla");
        MOZILLA_TYPE_BROWSERS.add("firefox");

        try
        {
            basicService = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
        } catch (UnavailableServiceException e)
        {
            // Leave NULL if JNLP is not available
            basicService = null;
        }
    }

    private BrowserLauncher()
    {
        // Hidden constructor of utility class.
    }

    /**
     * Calls the default handler to show the window for the letter sending.
     *
     * @param recepient the recepient address or NULL for default 'some@address.org';
     * @param subject   the subject of a letter.
     * @param body      the text to send in a body.
     * @param browserExecutable the executable to use or NULL for platform default.
     */
    public static void emailThis(String recepient, String subject, String body, String browserExecutable)
    {
        recepient = recepient == null ? "" : recepient.trim();

        String mailto = "mailto:" + recepient +
            "?subject=" + (subject == null ? "" : StringUtils.encodeForURL(subject).replaceAll("\\+", "%20")) +
            "&body=" + (body == null ? "" : StringUtils.encodeForURL(body).replaceAll("\\+", "%20"));

        try
        {
            showDocument(new URL(mailto), browserExecutable);
        } catch (MalformedURLException e)
        {
            LOG.log(Level.SEVERE, "Failed to build up the mailto-URL.", e);
        }
    }

    /**
     * Shows document in default user browser.
     *
     * @param url               URL to show.
     * @param browserExecutable executable path to start in non-WebStart case.
     */
    public static void showDocument(final URL url, final String browserExecutable)
    {
        if (LOG.isLoggable(Level.FINEST)) LOG.finest("URL: " + url);

        new Thread(new Runnable()
        {
            public void run()
            {
                showDocument0(browserExecutable, url);
            }
        }, THREAD_NAME).start();

    }

    private static void showDocument0(String browserExecutable, URL url)
    {
        if (showDocumentUsingNativeBrowser(browserExecutable, url)) return;

        // If application was started not using WebStart then we cannot
        // show links in default browser.
        if (!isUsingJWSBrowser())
        {
            browserExecutable = getCorrectBrowserExecutable(browserExecutable);

            try
            {
                if (browserExecutable != null &&
                    MOZILLA_TYPE_BROWSERS.contains(browserExecutable.toLowerCase()))
                {
                    showDocumentInMozilla(url, browserExecutable);
                } else
                {
                    // General purpose launch operation
                    Runtime.getRuntime().exec(convertToFullCommand(browserExecutable, url));
                }
            } catch (IOException e)
            {
                JOptionPane.showMessageDialog(null,
                    Strings.message("launch.browser.dialog.text"),
                    Strings.message("launch.browser.dialog.title"),
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } else
        {
            // Open document
            basicService.showDocument(url);
        }
    }

    /**
     * Try calling native browser.
     *
     * @param browserExecutable browser executable.
     * @param url URL to open.
     *
     * @return <code>TRUE</code> if opening was successful.
     */
    private static boolean showDocumentUsingNativeBrowser(String browserExecutable, URL url)
    {
        boolean success = false;

        if (!nativeErrored && SystemUtils.IS_OS_WINDOWS && StringUtils.isEmpty(browserExecutable))
        {
            try
            {
                OpenURL.openURL(url, false);
                success = true;
            } catch (UnsatisfiedLinkError e)
            {
                nativeErrored = true;
            } catch (Throwable e)
            {
                LOG.log(Level.SEVERE, "Failed to use native browser", e);
            }
        }

        return success;
    }

    /**
     * Make an attempt to start mozilla-type browser.
     *
     * @param url               URL to display.
     * @param browserExecutable browser executible.
     *
     * @throws IOException in case of any I/O exception.
     */
    private static void showDocumentInMozilla(URL url, String browserExecutable)
        throws IOException
    {
        // cmd = 'netscape/mozilla/firefox -remote openURL(http://www.javaworld.com)'
        String urls = url.toString().replaceAll("\\s", "%20");
        String cmd = browserExecutable + " -remote openURL(" + urls + ")";
        Process p = Runtime.getRuntime().exec(cmd);
        try
        {
            // wait for exit code -- if it's 0, command worked,
            // otherwise we need to start the browser up.
            int exitCode = p.waitFor();
            if (exitCode != 0)
            {
                // Command failed, start up the browser
                // cmd = 'netscape/mozilla/firefox http://www.javaworld.com'
                cmd = browserExecutable + " " + urls;
                Runtime.getRuntime().exec(cmd);
            }
        } catch (InterruptedException x)
        {
            LOG.log(Level.WARNING, MessageFormat.format(Strings.error("error.calling.browser"),
                new Object[] { cmd }), x);
        }
    }

    /**
     * Returns TRUE when will be using JWS browser.
     *
     * @return TRUE when will be using JWS browser.
     */
    public static boolean isUsingJWSBrowser()
    {
        return basicService != null && basicService.isWebBrowserSupported();
    }

    static String convertToFullCommand(String browserExecutable, final URL url)
    {
        if (browserExecutable == null || url == null) return null;

        // Append mark if there's no URL marks found
        if (browserExecutable.indexOf("$URL$") == -1)
        {
            browserExecutable += " $URL$";
        }

        // Escape URL string
        String urlString = url.toString().replaceAll("\\s", "%20");
        urlString = urlString.replaceAll("\\\\", "\\\\\\\\");
        urlString = urlString.replaceAll("\\$", "\\\\\\$");

        return browserExecutable.replaceAll("\\$URL\\$", urlString);
    }

    /**
     * Returns true if running under JWS.
     *
     * @return true if running under JWS.
     */
    public static boolean isRunningUnderJWS()
    {
        return basicService != null;
    }

    /**
     * Corrects browser executable path. If path is not specified or empty it
     * is taken from <code>OSSettings</code>.
     *
     * @param browserExecutable current executable.
     *
     * @return corrected.
     */
    public static String getCorrectBrowserExecutable(String browserExecutable)
    {
        // If there's no browser executable specified then try to get default browser.
        if (StringUtils.isEmpty(browserExecutable))
        {
            browserExecutable = OSSettings.getDefaultBrowserPath();
        }

        return browserExecutable == null ? null : browserExecutable.trim();
    }

    /**
     * Listens for clicks over the links and call external browser for them.
     */
    public static class LinkListener implements HyperlinkListener
    {
        private UserPreferences preferences;

        /**
         * Creates listener which takes browser executable from preferences.
         *
         * @param aPreferences preferences.
         */
        public LinkListener(UserPreferences aPreferences)
        {
            preferences = aPreferences;
        }

        /**
         * Called when a hypertext link is updated.
         *
         * @param e the event responsible for the update
         */
        public void hyperlinkUpdate(HyperlinkEvent e)
        {
            if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;

            showDocument(e.getURL(), preferences.getInternetBrowser());
        }
    }
}
