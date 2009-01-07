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
// $Id $
//

package com.salas.bb.core;

import junit.framework.TestCase;
import com.salas.bb.service.ServicePreferences;

import java.util.Date;

/**
 * Tests the database recoverer.
 */
public class TestDatabaseRecoverer extends TestCase
{
    public void testGetLastSuccessfulSyncDate_NoDate()
    {
        assertNull(DatabaseRecoverer.getLastSuccessfulSyncDate((GlobalModel)null));
        assertNull(DatabaseRecoverer.getLastSuccessfulSyncDate((ServicePreferences)null));

        ServicePreferences prefs = new ServicePreferences();
        prefs.setLastSyncOutDate(null);
        prefs.setLastSyncInDate(null);
        prefs.setEmail(null);
        assertNull("Account info isn't entered.",
            DatabaseRecoverer.getLastSuccessfulSyncDate(prefs));

        prefs.setEmail("1");
        prefs.setPassword("2");
        assertTrue(prefs.isAccountInformationEntered());
        assertNull("Statuses and dates aren't set",
            DatabaseRecoverer.getLastSuccessfulSyncDate(prefs));

        prefs.setLastSyncInStatus(null);
        prefs.setLastSyncOutStatus(null);
        prefs.setLastSyncInDate(new Date());
        prefs.setLastSyncOutDate(new Date());
        assertNull("Success status isn't set.",
            DatabaseRecoverer.getLastSuccessfulSyncDate(prefs));

        prefs.setLastSyncInStatus(ServicePreferences.SYNC_STATUS_FAILURE);
        prefs.setLastSyncOutStatus(ServicePreferences.SYNC_STATUS_FAILURE);
        assertNull("Success statuses aren't set.",
            DatabaseRecoverer.getLastSuccessfulSyncDate(prefs));
    }

    public void testGetLastSuccessfulSyncDate_NoAccount()
    {
        ServicePreferences prefs = new ServicePreferences();
        prefs.setLastSyncOutDate(new Date());
        prefs.setLastSyncInDate(new Date());
        prefs.setLastSyncInStatus(ServicePreferences.SYNC_STATUS_SUCCESS);
        prefs.setLastSyncOutStatus(ServicePreferences.SYNC_STATUS_SUCCESS);
        prefs.setEmail(null);
        assertNull("Account info isn't entered.",
            DatabaseRecoverer.getLastSuccessfulSyncDate(prefs));
    }

    public void testGetLastSuccessfulSyncDate_In()
    {
        Date inDate = new Date(1);
        Date outDate = new Date(2);

        ServicePreferences prefs = new ServicePreferences();
        prefs.setEmail("1");
        prefs.setPassword("2");
        prefs.setLastSyncInDate(inDate);
        prefs.setLastSyncInStatus(ServicePreferences.SYNC_STATUS_SUCCESS);
        assertTrue(inDate == DatabaseRecoverer.getLastSuccessfulSyncDate(prefs));

        prefs.setLastSyncOutDate(outDate);
        assertTrue(inDate == DatabaseRecoverer.getLastSuccessfulSyncDate(prefs));

        prefs.setLastSyncOutDate(null);
        prefs.setLastSyncOutStatus(ServicePreferences.SYNC_STATUS_SUCCESS);
        assertTrue(inDate == DatabaseRecoverer.getLastSuccessfulSyncDate(prefs));
    }

    public void testGetLastSuccessfulSyncDate_Out()
    {
        Date outDate = new Date(1);
        Date inDate = new Date(2);

        ServicePreferences prefs = new ServicePreferences();
        prefs.setEmail("1");
        prefs.setPassword("2");
        prefs.setLastSyncOutDate(outDate);
        prefs.setLastSyncOutStatus(ServicePreferences.SYNC_STATUS_SUCCESS);
        assertTrue(outDate == DatabaseRecoverer.getLastSuccessfulSyncDate(prefs));

        prefs.setLastSyncInDate(inDate);
        assertTrue(outDate == DatabaseRecoverer.getLastSuccessfulSyncDate(prefs));

        prefs.setLastSyncInDate(null);
        prefs.setLastSyncInStatus(ServicePreferences.SYNC_STATUS_SUCCESS);
        assertTrue(outDate == DatabaseRecoverer.getLastSuccessfulSyncDate(prefs));
    }

    public void testGetLastSuccessfulSyncDate_MostRecent()
    {
        Date outDate = new Date(1);
        Date inDate = new Date(2);

        ServicePreferences prefs = new ServicePreferences();
        prefs.setEmail("1");
        prefs.setPassword("2");
        prefs.setLastSyncOutDate(outDate);
        prefs.setLastSyncOutStatus(ServicePreferences.SYNC_STATUS_SUCCESS);
        prefs.setLastSyncInDate(inDate);
        prefs.setLastSyncInStatus(ServicePreferences.SYNC_STATUS_SUCCESS);

        assertTrue(inDate == DatabaseRecoverer.getLastSuccessfulSyncDate(prefs));
    }
}
