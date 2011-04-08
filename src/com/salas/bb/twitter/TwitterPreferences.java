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
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.utils.StringUtils;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthException;

import java.util.prefs.Preferences;

/**
 * Preferences.
 */
public class TwitterPreferences extends Model
{
    public static final String PROP_TWITTER_ENABLED         = "twitter.enabled";
    public static final String PROP_TWITTER_SCREEN_NAME     = "twitter.screenName";
    public static final String PROP_TWITTER_ACCESS_TOKEN    = "twitter.access_token";
    public static final String PROP_TWITTER_TOKEN_SECRET    = "twitter.token_secret";
    public static final String PROP_TWITTER_PROFILE_PICS    = "twitter.profile.pics";
    public static final String PROP_TWITTER_PASTE_LINK      = "twitter.paste.link";

    public static final String PROP_ENABLED                 = "enabled";
    public static final String PROP_SCREEN_NAME             = "screenName";
    public static final String PROP_PASSWORD                = "password";
    public static final String PROP_PROFILE_PICS            = "profilePics";
    public static final String PROP_PASTE_LINK              = "pasteLink";

    private boolean enabled;
    private String  screenName;
    private String  password;
    private String  accessToken;
    private String  tokenSecret;

    private String  pinToken;
    private String  pinTokenSecret;

    private boolean profilePics;
    private boolean pasteLink;

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
     * Returns TRUE when automatic link pasting is enabled.
     *
     * @return TRUE when automatic link pasting is enabled.
     */
    public boolean isPasteLink()
    {
        return pasteLink;
    }

    /**
     * Enables / disables automatic link pasting.
     *
     * @param pasteLink TRUE to enable.
     */
    public void setPasteLink(boolean pasteLink)
    {
        boolean old = this.pasteLink;
        this.pasteLink = pasteLink;
        firePropertyChange(PROP_PASTE_LINK, old, pasteLink);
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
     * Returns the access token.
     *
     * @return access token.
     */
    public String getAccessToken()
    {
        return accessToken;
    }

    /**
     * Sets the access token.
     *
     * @param accessToken access token.
     */
    public void setAccessToken(String accessToken)
    {
        String old = this.accessToken;
        this.accessToken = accessToken;
        firePropertyChange(PROP_TWITTER_ACCESS_TOKEN, old, accessToken);

        resetURLDependentActions();
    }

    /**
     * Returns the token secret.
     *
     * @return token secret.
     */
    public String getTokenSecret()
    {
        return tokenSecret;
    }

    /**
     * Sets the token secret.
     *
     * @param tokenSecret token secret.
     */
    public void setTokenSecret(String tokenSecret)
    {
        String old = this.tokenSecret;
        this.tokenSecret = tokenSecret;
        firePropertyChange(PROP_TWITTER_TOKEN_SECRET, old, tokenSecret);

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
        if (getAccessToken() == null) prefs.remove(PROP_TWITTER_ACCESS_TOKEN); else prefs.put(PROP_TWITTER_ACCESS_TOKEN, getAccessToken());
        if (getTokenSecret() == null) prefs.remove(PROP_TWITTER_TOKEN_SECRET); else prefs.put(PROP_TWITTER_TOKEN_SECRET, getTokenSecret());
        prefs.putBoolean(PROP_PROFILE_PICS, isProfilePics());
        prefs.putBoolean(PROP_PASTE_LINK, isPasteLink());
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
        setAccessToken(prefs.get(PROP_TWITTER_ACCESS_TOKEN, null));
        setTokenSecret(prefs.get(PROP_TWITTER_TOKEN_SECRET, null));
        setProfilePics(prefs.getBoolean(PROP_PROFILE_PICS, true));
        setPasteLink(prefs.getBoolean(PROP_PASTE_LINK, true));
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

    /**
     * Returns TRUE if twitter is authorized.
     *
     * @return TRUE if authorized.
     */
    public boolean isAuthorized()
    {
        return StringUtils.isNotEmpty(getAccessToken()) &&
            StringUtils.isNotEmpty(getTokenSecret());
    }

    /**
     * Returns consumer object required to sign the requests.
     *
     * @return consumer.
     */
    public OAuthConsumer getConsumer()
    {
        OAuthConsumer c = getDefaultConsumer();

        // Init consumer with tokens if available
        if (getAccessToken() != null && getTokenSecret() != null) {
            c.setTokenWithSecret(getAccessToken(), getTokenSecret());
        }

        return c;
    }

    /**
     * Returns the default consumer, not initialized with user tokens.
     *
     * @return consumer.
     */
    public static OAuthConsumer getDefaultConsumer()
    {
        String consumerKey = ResourceUtils.getString("twitter.consumer_key");
        String consumerSecret = ResourceUtils.getString("twitter.consumer_secret");

        return new DefaultOAuthConsumer(consumerKey, consumerSecret);
    }

    public static DefaultOAuthProvider getDefaultProvider()
    {
        return new DefaultOAuthProvider(
            "http://twitter.com/oauth/request_token",
            "http://twitter.com/oauth/access_token",
            "http://twitter.com/oauth/authorize");
    }

    public String getAuthURL()
    {
        OAuthProvider provider = getDefaultProvider();
        OAuthConsumer consumer = getDefaultConsumer();

        String authURL = null;
        try
        {
            authURL = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);
            pinToken = consumer.getToken();
            pinTokenSecret = consumer.getTokenSecret();
        } catch (OAuthException e)
        {
            // Auth exception
        }

        return authURL;
    }

    public void acquireAccessTokens(String pin)
        throws OAuthException
    {
        OAuthProvider provider = getDefaultProvider();
        OAuthConsumer consumer = getDefaultConsumer();

        consumer.setTokenWithSecret(pinToken, pinTokenSecret);

        pinToken = null;
        pinTokenSecret = null;

        provider.retrieveAccessToken(consumer, pin);

        setAccessToken(consumer.getToken());
        setTokenSecret(consumer.getTokenSecret());
        setScreenName(provider.getResponseParameters().get("screen_name").first());
    }

    public void unauthorize()
    {
        setAccessToken(null);
        setTokenSecret(null);
        setScreenName(null);
    }
}
