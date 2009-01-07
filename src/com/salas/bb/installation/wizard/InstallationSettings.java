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
// $Id: InstallationSettings.java,v 1.7 2008/02/28 15:59:51 spyromus Exp $
//

package com.salas.bb.installation.wizard;

import com.salas.bb.utils.feedscollections.CollectionItem;

/**
 * Set of preferences taken from wizard pages.
 */
public class InstallationSettings
{
    /** Clean list of guides. */
    public static final int DATA_INIT_CLEAN = 0;

    /** List of guides requested to be initialized from service account. */
    public static final int DATA_INIT_SERVICE = 1;

    /** List of guides to be filled from seleced starting points. */
    public static final int DATA_INIT_POINTS = 2;

    private boolean         saExists;
    private boolean         saUseAccount;
    private String          saEmail;
    private String          saPassword;

    private CollectionItem[] selectedStartingPoints = new CollectionItem[0];

    private int             dataInitMode;

    /**
     * Returns <code>true</code> if user specified existing service account.
     *
     * @return <code>true</code> if existing account specified.
     */
    public boolean isServiceAccountExists()
    {
        return saExists;
    }

    /**
     * Sets the flag of account existence.
     *
     * @param aServiceAccountExists <code>true</code> to skip account creation.
     */
    public void setServiceAccountExists(boolean aServiceAccountExists)
    {
        saExists = aServiceAccountExists;
    }

    /**
     * Returns email of service account.
     *
     * @return email of service account.
     */
    public String getServiceAccountEmail()
    {
        return saEmail;
    }

    /**
     * Sets email of service account.
     *
     * @param email email of service account.
     */
    public void setServiceAccountEmail(String email)
    {
        saEmail = email;
    }

    /**
     * Returns password of service account.
     *
     * @return password of service account.
     */
    public String getServiceAccountPassword()
    {
        return saPassword;
    }

    /**
     * Sets password of service account.
     *
     * @param password   password of service account.
     */
    public void setServiceAccountPassword(String password)
    {
        saPassword = password;
    }

    /**
     * Returns data initialization mode.
     *
     * @return data init mode.
     *
     * @see #DATA_INIT_CLEAN
     * @see #DATA_INIT_POINTS
     * @see #DATA_INIT_SERVICE
     */
    public int getDataInitMode()
    {
        return dataInitMode;
    }

    /**
     * Sets data initialization mode.
     *
     * @param mode desired data initalization mode.
     *
     * @see #DATA_INIT_CLEAN
     * @see #DATA_INIT_POINTS
     * @see #DATA_INIT_SERVICE
     */
    public void setDataInitMode(int mode)
    {
        dataInitMode = mode;
    }

    /**
     * Sets the list of selected starting points.
     *
     * @param aPoints list of starting points.
     */
    public void setStartingPoints(CollectionItem[] aPoints)
    {
        selectedStartingPoints = aPoints == null ? new CollectionItem[0] : aPoints;
    }

    /**
     * Returns non-null list of selected starting points.
     *
     * @return list of selected starting points.
     */
    public CollectionItem[] getSelectedStartingPoints()
    {
        return selectedStartingPoints;
    }

    /**
     * Sets the value of flag to use account.
     *
     * @param b <code>true</code> to use account.
     */
    public void setServiceAccountUse(boolean b)
    {
        saUseAccount = b;
    }

    /**
     * Returns <code>true</code>. if user requested to use account.
     *
     * @return <code>true</code> if new account using requested.
     */
    public boolean isUseAccountSelected()
    {
        return saUseAccount;
    }
}
