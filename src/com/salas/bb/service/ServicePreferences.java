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
// $Id: ServicePreferences.java,v 1.20 2006/08/02 13:00:27 spyromus Exp $
//

package com.salas.bb.service;

import com.salas.bb.utils.Constants;
import com.salas.bb.utils.i18n.Strings;

import java.util.Date;
import java.util.prefs.Preferences;

/**
 * Preferences object for Service.
 */
public class ServicePreferences
{
    // Constants

    /** Manual synchronization mode. */
    public static final int SYNC_MODE_MANUAL = 0;

    /** Synchronization on every run and exit. */
    public static final int SYNC_MODE_EACH_RUN = 1;

    /** Synchronization on every first run and exit every defined number of days. */
    public static final int SYNC_MODE_PERIODICAL = 2;

    /** Success status. */
    public static final String SYNC_STATUS_SUCCESS = Strings.message("service.sync.status.successful");
    /** Failure status. */
    public static final String SYNC_STATUS_FAILURE = Strings.message("service.sync.status.errored");

    // Defaults

    private static final String DEFAULT_FULL_NAME               = Constants.EMPTY_STRING;
    private static final String DEFAULT_EMAIL                   = Constants.EMPTY_STRING;
    private static final String DEFAULT_PASSWORD                = Constants.EMPTY_STRING;
    private static final String DEFAULT_LAST_SYNC_IN_STATUS     = Strings.message("service.sync.not.performed");
    private static final String DEFAULT_LAST_SYNC_OUT_STATUS    = Strings.message("service.sync.not.performed");
    private static final int DEFAULT_LAST_SYNC_OUT_FEEDS_COUNT  = -1;

    private static final Date DEFAULT_REG_DATE                  = null;
    private static final Date DEFAULT_LAST_SYNC_IN_DATE         = null;
    private static final Date DEFAULT_LAST_SYNC_OUT_DATE        = null;

    private static final int DEFAULT_SYNC_MODE                  = SYNC_MODE_MANUAL;
    private static final int DEFAULT_SYNC_PERIOD                = 1;

    private static final boolean DEFAULT_SYNC_FEEDS             = true;
    private static final boolean DEFAULT_SYNC_PREFERENCES       = true;

    // Keys

    /** Key of user email preference. */
    public static final String KEY_EMAIL                    = "service.email";
    /** Key of user password preference. */
    public static final String KEY_PASSWORD                 = "service.password";
    /** Key of full user name. */
    public static final String KEY_FULL_NAME                = "service.fullName";
    /** Key of registration date preference. */
    public static final String KEY_REG_DATE                 = "service.regDate";

    /** Key of last in-synchronization date. */
    public static final String KEY_LAST_SYNC_IN_DATE        = "service.lastSyncInDate";
    /** Key of last in-synchronization status. */
    public static final String KEY_LAST_SYNC_IN_STATUS      = "service.lastSyncInStatus";
    
    private static final String KEY_LAST_SYNC_OUT_DATE      = "service.lastSyncOutDate";
    private static final String KEY_LAST_SYNC_OUT_STATUS    = "service.lastSyncOutStatus";
    private static final String KEY_LAST_SYNC_OUT_FEEDS_COUNT = "service.lastSyncOutFeedsCount";
    private static final String KEY_SYNC_MODE               = "service.syncMode";
    private static final String KEY_SYNC_PERIOD             = "service.syncPeriod";
    private static final String KEY_SYNC_FEEDS              = "service.syncFeeds";
    private static final String KEY_SYNC_PREFERENCES        = "service.syncPreferences";

    /** Name of feeds synchronization flag property. */
    public static final String PROP_SYNC_FEEDS              = "syncFeeds";
    /** Name of preferences synchronization flag property. */
    public static final String PROP_SYNC_PREFERENCES        = "syncPreferences";

    // Properties

    private String fullName             = DEFAULT_FULL_NAME;
    private String email                = DEFAULT_EMAIL;
    private String password             = DEFAULT_PASSWORD;
    private String lastSyncInStatus     = DEFAULT_LAST_SYNC_IN_STATUS;
    private String lastSyncOutStatus    = DEFAULT_LAST_SYNC_OUT_STATUS;

    private Date regDate                = DEFAULT_REG_DATE;
    private Date lastSyncInDate         = DEFAULT_LAST_SYNC_IN_DATE;
    private Date lastSyncOutDate        = DEFAULT_LAST_SYNC_OUT_DATE;

    private int syncMode                = DEFAULT_SYNC_MODE;
    private int syncPeriod              = DEFAULT_SYNC_PERIOD;

    private boolean syncFeeds           = DEFAULT_SYNC_FEEDS;
    private boolean syncPreferences     = DEFAULT_SYNC_PREFERENCES;
    private int lastSyncOutFeedsCount   = DEFAULT_LAST_SYNC_OUT_FEEDS_COUNT;

    /**
     * Returns account email.
     *
     * @return email.
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * Sets account email.
     *
     * @param aEmail email.
     */
    public void setEmail(String aEmail)
    {
        this.email = aEmail;
    }

    /**
     * Returns account password.
     *
     * @return password.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Sets account password.
     *
     * @param aPassword password.
     */
    public void setPassword(String aPassword)
    {
        this.password = aPassword;
    }

    /**
     * Returns date of last sync in.
     *
     * @return date.
     */
    public Date getLastSyncInDate()
    {
        return lastSyncInDate;
    }

    /**
     * Sets date of last sync in.
     *
     * @param aLastSyncInDate date.
     */
    public void setLastSyncInDate(Date aLastSyncInDate)
    {
        this.lastSyncInDate = aLastSyncInDate;
    }

    /**
     * Returns date of last sync out.
     *
     * @return date.
     */
    public Date getLastSyncOutDate()
    {
        return lastSyncOutDate;
    }

    /**
     * Sets date of last sync out.
     *
     * @param aLastSyncOutDate date.
     */
    public void setLastSyncOutDate(Date aLastSyncOutDate)
    {
        this.lastSyncOutDate = aLastSyncOutDate;
    }

    /**
     * Returns current synchronization mode.
     *
     * @return mode.
     */
    public int getSyncMode()
    {
        return syncMode;
    }

    /**
     * Sets new synchronization mode.
     *
     * @param aSyncMode mode.
     */
    public void setSyncMode(int aSyncMode)
    {
        this.syncMode = aSyncMode;
    }

    /**
     * Returns current synchronization period (for <code>SYNC_MODE_PERIODICAL</code>).
     *
     * @return period in days.
     */
    public int getSyncPeriod()
    {
        return syncPeriod;
    }

    /**
     * Sets synchronization period in days (for <code>SYNC_MODE_PERIODICAL</code>).
     *
     * @param aSyncPeriod period in days.
     */
    public void setSyncPeriod(int aSyncPeriod)
    {
        this.syncPeriod = aSyncPeriod;
    }

    /**
     * Returns status of last synchronization (in).
     *
     * @return status.
     */
    public String getLastSyncInStatus()
    {
        return lastSyncInStatus;
    }

    /**
     * Sets last synchronization (in) status.
     *
     * @param aLastSyncInStatus status.
     */
    public void setLastSyncInStatus(String aLastSyncInStatus)
    {
        this.lastSyncInStatus = aLastSyncInStatus;
    }

    /**
     * Returns status of last synchronization (out).
     *
     * @return last status.
     */
    public String getLastSyncOutStatus()
    {
        return lastSyncOutStatus;
    }

    /**
     * Sets last synchronization (out) status.
     *
     * @param aLastSyncOutStatus status.
     */
    public void setLastSyncOutStatus(String aLastSyncOutStatus)
    {
        this.lastSyncOutStatus = aLastSyncOutStatus;
    }

    /**
     * Returns full name of user used for last registration.
     *
     * @return full name.
     */
    public String getFullName()
    {
        return fullName;
    }

    /**
     * Sets full name of user used for last registration.
     *
     * @param aFullName full name.
     */
    public void setFullName(String aFullName)
    {
        this.fullName = aFullName;
    }

    /**
     * Returns registration date.
     *
     * @return registration date.
     */
    public Date getRegDate()
    {
        return regDate;
    }

    /**
     * Sets registration date.
     *
     * @param aRegDate registration date.
     */
    public void setRegDate(Date aRegDate)
    {
        this.regDate = aRegDate;
    }

    /**
     * Returns TRUE if account information is entered.
     *
     * @return TRUE if account information is entered.
     */
    public boolean isAccountInformationEntered()
    {
        return email != null && password != null &&
            email.trim().length() > 0 &&
            password.trim().length() > 0;
    }

    /**
     * Returns TRUE if synchronization of feeds is necessary.
     *
     * @return TRUE if synchronization of feeds is necessary.
     */
    public boolean isSyncFeeds()
    {
        return syncFeeds;
    }

    /**
     * Controls synchronization of feeds.
     *
     * @param value TRUE if synchronization of feeds should be made.
     */
    public void setSyncFeeds(boolean value)
    {
        syncFeeds = value;
    }

    /**
     * Returns TRUE if synchronization of preferences is necessary.
     *
     * @return TRUE if synchronization of preferences is necessary.
     */
    public boolean isSyncPreferences()
    {
        return syncPreferences;
    }

    /**
     * Controls sycnhronization of preferences.
     *
     * @param value TRUE if synchronization of preferences is necessary.
     */
    public void setSyncPreferences(boolean value)
    {
        syncPreferences = value;
    }

    /**
     * Sets the number of feeds which were sent to service during the last sync-out.
     *
     * @param value number of feeds.
     */
    public void setLastSyncOutFeedsCount(int value)
    {
        lastSyncOutFeedsCount = value;
    }

    /**
     * Returns the number of feeds which were sent to service during the last sync-out.
     *
     * @return number of feeds.
     */
    public int getLastSyncOutFeedsCount()
    {
        return lastSyncOutFeedsCount;
    }

    /**
     * Read all the Preferences from persistent preferences into this object.
     * On Windows, the persistent store is the Registry.
     *
     * @param prefs object representing persistent Preferences.
     */
    public void restoreFrom(Preferences prefs)
    {
        setFullName(prefs.get(KEY_FULL_NAME, DEFAULT_FULL_NAME));
        setEmail(prefs.get(KEY_EMAIL, DEFAULT_EMAIL));
        setPassword(prefs.get(KEY_PASSWORD, DEFAULT_PASSWORD));

        long regDateLong = prefs.getLong(KEY_REG_DATE, 0);
        setRegDate(regDateLong == 0 ? null : new Date(regDateLong));

        long lastSyncInDateLong = prefs.getLong(KEY_LAST_SYNC_IN_DATE, 0);
        long lastSyncOutDateLong = prefs.getLong(KEY_LAST_SYNC_OUT_DATE, 0);

        setLastSyncInDate(lastSyncInDateLong == 0 ? null : new Date(lastSyncInDateLong));
        setLastSyncOutDate(lastSyncOutDateLong == 0 ? null : new Date(lastSyncOutDateLong));
        setLastSyncInStatus(prefs.get(KEY_LAST_SYNC_IN_STATUS, DEFAULT_LAST_SYNC_IN_STATUS));
        setLastSyncOutStatus(prefs.get(KEY_LAST_SYNC_OUT_STATUS, DEFAULT_LAST_SYNC_OUT_STATUS));
        setLastSyncOutFeedsCount(prefs.getInt(KEY_LAST_SYNC_OUT_FEEDS_COUNT,
            DEFAULT_LAST_SYNC_OUT_FEEDS_COUNT));

        setSyncMode(prefs.getInt(KEY_SYNC_MODE, DEFAULT_SYNC_MODE));
        setSyncPeriod(prefs.getInt(KEY_SYNC_PERIOD, DEFAULT_SYNC_PERIOD));
        setSyncFeeds(prefs.getBoolean(KEY_SYNC_FEEDS, DEFAULT_SYNC_FEEDS));
        setSyncPreferences(prefs.getBoolean(KEY_SYNC_PREFERENCES, DEFAULT_SYNC_PREFERENCES));
    }

    /**
     * Write all the preferences from this Object to persistent preferences.
     * On Windows, this is the registry.
     *
     * @param prefs object representing persistent Preferences.
     */
    public void storeIn(Preferences prefs)
    {
        prefs.put(KEY_FULL_NAME, getFullName());
        prefs.put(KEY_EMAIL, getEmail());
        prefs.put(KEY_PASSWORD, getPassword());
        prefs.putLong(KEY_REG_DATE, getRegDate() == null
                ? 0 : getRegDate().getTime());
        prefs.putLong(KEY_LAST_SYNC_IN_DATE, getLastSyncInDate() == null
                ? 0 : getLastSyncInDate().getTime());
        prefs.putLong(KEY_LAST_SYNC_OUT_DATE, getLastSyncOutDate() == null
                ? 0 : getLastSyncOutDate().getTime());
        prefs.put(KEY_LAST_SYNC_IN_STATUS, getLastSyncInStatus());
        prefs.put(KEY_LAST_SYNC_OUT_STATUS, getLastSyncOutStatus());
        prefs.putInt(KEY_LAST_SYNC_OUT_FEEDS_COUNT, getLastSyncOutFeedsCount());
        prefs.putInt(KEY_SYNC_MODE, getSyncMode());
        prefs.putInt(KEY_SYNC_PERIOD, getSyncPeriod());
        prefs.putBoolean(KEY_SYNC_FEEDS, isSyncFeeds());
        prefs.putBoolean(KEY_SYNC_PREFERENCES, isSyncPreferences());
    }

    /**
     * Returns email with full name. Like
     *
     * <pre>"Full Name" &lt;my.email@email.net&gt;</pre>
     *
     * @return full email.
     */
    public String getFullEmail()
    {
        String eml = getEmail();
        String fname = getFullName();

        if (eml != null && fname != null)
        {
            eml = "\"" + fname + "\" <" + eml + ">";
        }

        return eml;
    }
}
