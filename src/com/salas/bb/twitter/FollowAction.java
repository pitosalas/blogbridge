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
// $Id$
//

package com.salas.bb.twitter;

import com.salas.bb.core.GlobalController;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.net.URLDecoder;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

/**
 * (Un)follow action.
 */
public class FollowAction extends AbstractAction
{
    private static final Pattern PATTERN_SCREEN_NAME =
        Pattern.compile("http://(www\\.)?twitter\\.com/([^/\\?\\#\\s]+)($|#|\\?)");
    
    private static FollowAction instance;

    private final Object lock = new Object();

    private URL         userURL;
    private String      screenName;
    private boolean     followAction;

    /** Creates action. */
    private FollowAction()
    {
        update(false, Strings.message("twitter.unknown"));
    }

    /**
     * Returns instance.
     *
     * @return instance.
     */
    public static synchronized FollowAction getInstance()
    {
        if (instance == null) instance = new FollowAction();
        return instance;
    }

    /**
     * Sets the user URL to figure the action from it.
     *
     * @param url URL.
     */
    public static void setUserURL(URL url)
    {
        getInstance().setUserURL0(url);
    }

    /**
     * Sets user URL.
     *
     * @param url URL.
     */
    private void setUserURL0(URL url)
    {
        synchronized (lock)
        {
            if (userURL == null || url == null || !userURL.toString().equalsIgnoreCase(url.toString()))
            {
                userURL = url;
                checkAndUpdate(url);
            }
        }
    }

    /** Invoked when action is performed. */
    public void actionPerformed(ActionEvent e)
    {
        synchronized (lock)
        {
            if (userURL == null) return;

            try
            {
                if (followAction) TwitterGateway.follow(screenName); else TwitterGateway.unfollow(screenName);
                followAction = !followAction;
                update(true, actionLabel(!followAction));
            } catch (IOException e1)
            {
                e1.printStackTrace();
                // Ignore
            }
        }
    }

    /**
     * Checks the status of the friendship between two people.
     *
     * @param url URL of the user.
     */
    private void checkAndUpdate(final URL url)
    {
        update(false, Strings.message("twitter.checking"));

        if (url == null && !TwitterFeature.isConfigured()) return;

        final String name = urlToScreenName(url);
        if (name != null)
        {
            if (name.equalsIgnoreCase(getPreferences().getScreenName()))
            {
                update(false, Strings.message("twitter.myself"));
            } else
            {
                new Thread("Follows?")
                {
                    public void run()
                    {
                        boolean following = false;
                        boolean error = false;

                        try
                        {
                            following = TwitterGateway.isFollowing(name);
                        } catch (IOException e)
                        {
                            error = true;
                        }

                        updateState(following, error, name, url);
                    }
                }.start();
            }
        }
    }

    private void update(boolean enabled, String text)
    {
        setEnabled(enabled);
        putValue("Name", text);
    }

    /**
     * Returns preferences.
     *
     * @return preferences.
     */
    private TwitterPreferences getPreferences()
    {
        return GlobalController.SINGLETON.getModel().getUserPreferences().getTwitterPreferences();
    }

    private void updateState(final boolean following, final boolean error, final String name, final URL url)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                synchronized (lock)
                {
                    // If it is still the same URL
                    if (userURL == url)
                    {
                        followAction = !following;
                        screenName   = name;

                        update(!error, error ? Strings.message("twitter.unknown") : actionLabel(following));
                    }
                }
            }
        });
    }

    private String actionLabel(boolean following)
    {
        return following ? Strings.message("twitter.unfollow") : Strings.message("twitter.follow");
    }

    /**
     * Returns the username from the URL or NULL.
     *
     * @param url URL to analyze.
     *
     * @return screen name.
     */
    static String urlToScreenName(URL url)
    {
        String name = null;

        String urls = url.toString();
        Matcher m = PATTERN_SCREEN_NAME.matcher(urls);
        try
        {
            if (m.find()) name = URLDecoder.decode(m.group(2), "UTF-8");
        } catch (UnsupportedEncodingException e)
        {
            // Failed transformation -- ignore
        }

        return name;
    }
}
