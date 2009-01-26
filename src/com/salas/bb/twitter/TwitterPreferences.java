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

import com.jgoodies.binding.beans.Model;

import java.util.prefs.Preferences;

/**
 * Preferences.
 */
public class TwitterPreferences extends Model
{
    private static final String PROP_TWITTER_ENABLED    = "twitter.enabled";
    private static final String PROP_TWITTER_USER       = "twitter.user";
    private static final String PROP_TWITTER_PASSWORD   = "twitter.password";

    public static final String PROP_ENABLED     = "enabled";
    public static final String PROP_USER        = "user";
    public static final String PROP_PASSWORD    = "password";

    private boolean enabled;
    private String  user;
    private String  password;

    /**
     * Returns TRUE when the Twitter support is enabled.
     *
     * @return TRUE when the Twitter support is enabled.
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Enables / disables Twitter support.
     *
     * @param enabled TRUE to enable.
     */
    public void setEnabled(boolean enabled)
    {
        boolean old = this.enabled;
        this.enabled = enabled;
        firePropertyChange(PROP_ENABLED, old, enabled);
    }

    /**
     * Returns the Twitter user name.
     *
     * @return user name.
     */
    public String getUser()
    {
        return user;
    }

    /**
     * Sets the user name.
     *
     * @param user name.
     */
    public void setUser(String user)
    {
        String old = this.user;
        this.user = user;
        firePropertyChange(PROP_USER, old, user);
    }

    /**
     * Returns the password.
     *
     * @return password.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password password.
     */
    public void setPassword(String password)
    {
        String old = this.password;
        this.password = password;
        firePropertyChange(PROP_PASSWORD, old, password);
    }

    /**
     * Persists the information about blogs in the preferences map.
     *
     * @param prefs map.
     */
    public void store(Preferences prefs)
    {
        prefs.putBoolean(PROP_TWITTER_ENABLED, isEnabled());
        prefs.put(PROP_TWITTER_USER, getUser());
        prefs.put(PROP_TWITTER_PASSWORD, getPassword());
    }

    /**
     * Restores the information about blogs from the preferences map.
     *
     * @param prefs map.
     */
    public void restore(Preferences prefs)
    {
        setEnabled(prefs.getBoolean(PROP_TWITTER_ENABLED, false));
        setUser(prefs.get(PROP_TWITTER_USER, null));
        setPassword(prefs.get(PROP_TWITTER_PASSWORD, null));

    }

}
