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
// $Id: CachingAuthenticator.java,v 1.6 2006/08/08 10:47:55 spyromus Exp $
//

package com.salas.bb.utils.net.auth;

import com.jgoodies.uif.application.Application;
import com.salas.bb.utils.i18n.Strings;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.List;
import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * This authenticator stores the passwords in its own database.
 */
public class CachingAuthenticator extends Authenticator
{
    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(CachingAuthenticator.class);

    private static final String PREF_SAVING_PASSWORDS       = "savingPasswords";
    private static final boolean DEFAULT_SAVING_PASSWORDS   = false;

    // When someone asks for auth. info and we have it we return this information without
    // showing login/password dialog to the user. But if this information is incorrect, we
    // will be questioned again and, at this time, we will need to display dialog. This
    // list contains the hash codes of contexts which were already questioned, so, once
    // someone will ask ask again for them, we will know that the previous information was
    // incorrect.
    private final List                  queriedContexts;
    private final IPasswordsRepository  repository;
    
    private AuthDialog                  authDialog;

    /**
     * Creates authenticator.
     *
     * @param aRepository repository to use for passwords storing.
     */
    public CachingAuthenticator(IPasswordsRepository aRepository)
    {
        queriedContexts = new ArrayList();
        repository = aRepository;
    }

    /**
     * Called when password authorization is needed.  Subclasses should override the default
     * implementation, which returns null.
     *
     * @return The PasswordAuthentication collected from the user, or null if none is provided.
     */
    protected synchronized PasswordAuthentication getPasswordAuthentication()
    {
        String key = getKey();
        PasswordAuthentication auth = repository.getAuthInformation(key);

        if (auth == null || (!isPasswordUnknown(auth) && wasQueried(key)))
        {
            auth = getPasswordAuthentication0(auth);

            // If user canceled authentication, we create a fake record
            if (auth == null) auth = authPasswordUnknown();

            if (isSavingPasswords()) repository.record(key, auth);
        }

        if (isPasswordUnknown(auth)) throw new AuthCancelException(Strings.error("net.user.canceled.authentication"));

        recordQuery(key);

        return auth;
    }

    /**
     * Returns <code>TRUE</code> if the auth info is a special marker.
     *
     * @param auth auth info.
     *
     * @return <code>TRUE</code> if marker.
     */
    private static boolean isPasswordUnknown(PasswordAuthentication auth)
    {
        return auth != null && "~".equals(auth.getUserName()) &&
            auth.getPassword() != null && auth.getPassword().length == 1 &&
            auth.getPassword()[0] == '~';
    }

    /**
     * Returns a special marker authentication info.
     *
     * @return marker auth info.
     */
    private static PasswordAuthentication authPasswordUnknown()
    {
        return new PasswordAuthentication("~", "~".toCharArray());
    }


    /**
     * Records querying of auth. info.
     *
     * @param context context questioned.
     */
    private void recordQuery(String context)
    {
        Integer hashCode = new Integer(context.hashCode());
        if (!queriedContexts.contains(hashCode)) queriedContexts.add(hashCode);
    }

    /**
     * Returns <code>TRUE</code> if our queries list has record about query for this context.
     *
     * @param context context.
     *
     * @return <code>TRUE</code> if our queries list has record about query for this context.
     */
    private boolean wasQueried(String context)
    {
        return queriedContexts.contains(new Integer(context.hashCode()));
    }

    /**
     * Called when password authorization is needed.
     *
     * @param auth authentication info or <code>NULL</code> if not known.
     *
     * @return authentication info.
     *
     * @throws AuthCancelException if user canceled authentication.
     */
    private PasswordAuthentication getPasswordAuthentication0(PasswordAuthentication auth)
        throws AuthCancelException
    {
        String username = null;
        char[] password = null;

        if (auth != null)
        {
            username = auth.getUserName();
            password = auth.getPassword();
        }

        String server = getRequestingProtocol() + "://" + getRequestingHost() + ":" + getRequestingPort();

        AuthDialog dialog = getDialog();
        dialog.open(server, getRequestingPrompt(), username, password, isSavingPasswords());

        if (!dialog.hasBeenCanceled())
        {
            auth = new PasswordAuthentication(dialog.getUsername(), dialog.getPassword());
            setSavingPasswords(dialog.isSavingRequired());
        } else
        {
            auth = null;
        }

        return auth;
    }

    /**
     * Returns the dialog which is initialized lazily.
     *
     * @return the dialog.
     */
    private synchronized AuthDialog getDialog()
    {
        if (authDialog == null) authDialog = new AuthDialog(Application.getDefaultParentFrame());

        return authDialog;
    }

    /**
     * Returns key of realm.
     *
     * @return key.
     */
    private String getKey()
    {
        StringBuffer sb = new StringBuffer();

        sb.append(getRequestingProtocol()).append(':');
        sb.append(getRequestingHost()).append(':');
        sb.append(getRequestingPort()).append(':');
        sb.append(getRequestingScheme()).append(':');
        sb.append(getRequestingPrompt());

        return sb.toString();
    }

    /**
     * Returns the values of <code>savingPasswords</code> flag.
     *
     * @return <code>TRUE</code> to save passwords.
     */
    private boolean isSavingPasswords()
    {
        return PREFERENCES.getBoolean(PREF_SAVING_PASSWORDS, DEFAULT_SAVING_PASSWORDS);
    }

    /**
     * Changes the value of <code>savingPasswords</code> flag.
     *
     * @param saving <code>TRUE</code> to save passwords.
     */
    private void setSavingPasswords(boolean saving)
    {
        PREFERENCES.putBoolean(PREF_SAVING_PASSWORDS, saving);
    }
}
