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
    public static final String PROP_TWITTER_ENABLED         = "twitter.enabled";
    public static final String PROP_TWITTER_SCREEN_NAME     = "twitter.screenName";
    public static final String PROP_TWITTER_PASSWORD        = "twitter.password";
    public static final String PROP_TWITTER_PROFILE_PICS    = "twitter.profile.pics";

    public static final String PROP_ENABLED                 = "enabled";
    public static final String PROP_SCREEN_NAME             = "screenName";
    public static final String PROP_PASSWORD                = "password";
    public static final String PROP_PROFILE_PICS            = "profilePics";

    private boolean enabled;
    private String  screenName;
    private String  password;
    private boolean profilePics;

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
     * Returns TRUE when profile pics should be shown in tooltips.
     *
     * @return profile pics enabled.
     */
    public boolean isProfilePics()
    {
        return profilePics;
    }

    /**
     * Enables / disables profile pics in tooltips.
     *
     * @param profilePics TRUE to show.
     */
    public void setProfilePics(boolean profilePics)
    {
        boolean old = this.profilePics;
        this.profilePics = profilePics;
        firePropertyChange(PROP_PROFILE_PICS, old, profilePics);
    }

    /**
     * Returns the Twitter screen name.
     *
     * @return screen name.
     */
    public String getScreenName()
    {
        return screenName;
    }

    /**
     * Sets the screen name.
     *
     * @param screenName name.
     */
    public void setScreenName(String screenName)
    {
        String old = this.screenName;
        this.screenName = screenName;
        firePropertyChange(PROP_SCREEN_NAME, old, screenName);

        resetURLDependentActions();
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

        resetURLDependentActions();
    }

    /**
     * Persists the information about blogs in the preferences map.
     *
     * @param prefs map.
     */
    public void store(Preferences prefs)
    {
        prefs.putBoolean(PROP_TWITTER_ENABLED, isEnabled());
        if (getScreenName() == null) prefs.remove(PROP_TWITTER_SCREEN_NAME); else prefs.put(PROP_TWITTER_SCREEN_NAME, getScreenName());
        if (getPassword() == null) prefs.remove(PROP_TWITTER_PASSWORD); else prefs.put(PROP_TWITTER_PASSWORD, getPassword());
        prefs.putBoolean(PROP_PROFILE_PICS, isProfilePics());
    }

    /**
     * Restores the information about blogs from the preferences map.
     *
     * @param prefs map.
     */
    public void restore(Preferences prefs)
    {
        setEnabled(prefs.getBoolean(PROP_TWITTER_ENABLED, false));
        setScreenName(prefs.get(PROP_TWITTER_SCREEN_NAME, null));
        setPassword(prefs.get(PROP_TWITTER_PASSWORD, null));
        setProfilePics(prefs.getBoolean(PROP_PROFILE_PICS, true));
    }

    /**
     * Resets actions that depend on URLs after the preferences change.
     */
    private static void resetURLDependentActions()
    {
        FollowAction.getInstance().setUserURL(null);
        ReplyAction.getInstance().setUserURL(null);
        SubscribeAction.getInstance().setUserURL(null);
    }
}
