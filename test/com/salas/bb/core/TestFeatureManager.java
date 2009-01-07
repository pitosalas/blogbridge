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
// $Id: TestFeatureManager.java,v 1.4 2007/02/20 11:37:39 spyromus Exp $
//

package com.salas.bb.core;

import junit.framework.TestCase;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.prefs.Preferences;

import com.salas.bb.utils.Constants;
import com.salas.bb.utils.DateUtils;

/**
 * Tests feature manager.
 */
public class TestFeatureManager extends TestCase
{
    /** Tests isInt() */
    public void testIsInt()
    {
        Map<String, String> f = new HashMap<String, String>();
        f.put("a", "300");
        f.put("b", "0");
        f.put("c", "");
        f.put("d", "a");
        f.put("e", "-");

        assertTrue(FeatureManager.isInt(f, "a", false));
        assertTrue(FeatureManager.isInt(f, "a", true));
        assertTrue(FeatureManager.isInt(f, "b", false));
        assertTrue(FeatureManager.isInt(f, "b", true));
        assertFalse(FeatureManager.isInt(f, "c", false));
        assertFalse(FeatureManager.isInt(f, "c", true));
        assertFalse(FeatureManager.isInt(f, "d", false));
        assertFalse(FeatureManager.isInt(f, "d", true));
        assertFalse(FeatureManager.isInt(f, "e", false));
        assertTrue(FeatureManager.isInt(f, "e", true));
        assertFalse(FeatureManager.isInt(f, "f", false));
        assertFalse(FeatureManager.isInt(f, "f", true));
    }

    /** Tests isFloat() */
    public void testIsFloat()
    {
        Map<String, String> f = new HashMap<String, String>();
        f.put("a", "30.0");
        f.put("b", "0");
        f.put("c", "");
        f.put("d", "a");
        f.put("e", "-");

        assertTrue(FeatureManager.isFloat(f, "a"));
        assertTrue(FeatureManager.isFloat(f, "b"));
        assertFalse(FeatureManager.isFloat(f, "c"));
        assertFalse(FeatureManager.isFloat(f, "d"));
        assertFalse(FeatureManager.isFloat(f, "e"));
        assertFalse(FeatureManager.isFloat(f, "f"));
    }

    /** Tests dates. */
    public void testIsDate()
    {
        Map<String, String> f = new HashMap<String, String>();
        f.put("a", "30.0");
        f.put("b", "0");
        f.put("c", "");
        f.put("d", "a");
        f.put("e", "-");
        f.put("f", Long.toString(System.currentTimeMillis()));

        assertFalse(FeatureManager.isDate(f, "a"));
        assertTrue(FeatureManager.isDate(f, "b"));
        assertFalse(FeatureManager.isDate(f, "c"));
        assertFalse(FeatureManager.isDate(f, "d"));
        assertFalse(FeatureManager.isDate(f, "e"));
        assertTrue(FeatureManager.isDate(f, "f"));
        assertFalse(FeatureManager.isDate(f, "g"));
    }

    /** Tests booleans. */
    public void testIsBoolean()
    {
        Map<String, String> f = new HashMap<String, String>();
        f.put("a", "30.0");
        f.put("b", "0");
        f.put("c", "");
        f.put("d", "a");
        f.put("e", "-");
        f.put("f", Long.toString(System.currentTimeMillis()));
        f.put("g", "1");

        assertFalse(FeatureManager.isBoolean(f, "a"));
        assertTrue(FeatureManager.isBoolean(f, "b"));
        assertFalse(FeatureManager.isBoolean(f, "c"));
        assertFalse(FeatureManager.isBoolean(f, "d"));
        assertFalse(FeatureManager.isBoolean(f, "e"));
        assertFalse(FeatureManager.isBoolean(f, "f"));
        assertTrue(FeatureManager.isBoolean(f, "g"));
        assertFalse(FeatureManager.isBoolean(f, "h"));
    }

    // ------------------------------------------------------------------------
    // Encode / decode
    // ------------------------------------------------------------------------

    /** Tests encoding and decoding of the collection. */
    public void testEncodeDecode()
    {
        String s = "abc\ndef";
        String e = FeatureManager.encode(s);

        assertEquals("6162630a646566", e);

        String d = FeatureManager.decode(e);

        assertEquals(s, d);
    }

    // ------------------------------------------------------------------------
    // Date Expiration checks
    // ------------------------------------------------------------------------

    /** Checks the detection of not expired plan. */
    public void testIsExpired_no()
    {
        FeatureManager fm = new FeatureManager(null);

        fm.setPlanExpirationDate(new Date(System.currentTimeMillis() + Constants.MILLIS_IN_DAY));
        assertFalse(fm.isExpired());

        fm.setPlanExpirationDate(new Date(System.currentTimeMillis()));
        assertFalse(fm.isExpired());
    }

    /** Checks the detection of expired plan. */
    public void testIsExpired_yes()
    {
        FeatureManager fm = new FeatureManager(null);

        fm.setPlanExpirationDate(new Date(System.currentTimeMillis() - Constants.MILLIS_IN_DAY));
        assertFalse(fm.isExpired());
    }

    // ------------------------------------------------------------------------
    // Synchronization Limit tests
    // ------------------------------------------------------------------------

    private Preferences prefs;
    private FeatureManager fm;

    /**
     * First sync. No attempts.
     */
    public void testCanSynchronize_firstTime()
    {
        initSyncLimitTests(1, null, null);
        assertTrue(fm.canSynchronize());
    }

    /**
     * First sync. Limit set to zero.
     */
    public void testCanSynchronize_limitSet()
    {
        initSyncLimitTests(0, null, null);
        assertFalse(fm.canSynchronize());
    }

    /** Last time was yesterday. */
    public void testCanSynchronize_yesterday()
    {
        initSyncLimitTests(1, DateUtils.getTodayTime() - Constants.MILLIS_IN_DAY, 1);
        assertTrue(fm.canSynchronize());
        assertEquals("Counter should be reset.", 0, prefs.getInt(FeatureManager.KEY_SYNCHRONIZATIONS, -1));
        assertTrue("The date should be set to current moment.",
            prefs.getLong(FeatureManager.KEY_LAST_SYNC_DATE, -1) > DateUtils.getTodayTime());
    }

    /** Simple registration. */
    public void testRegisterSync()
    {
        initSyncLimitTests(1, null, null);
        fm.registerSync();
        assertEquals("Counter should be set.", 1, prefs.getInt(FeatureManager.KEY_SYNCHRONIZATIONS, -1));
        assertTrue("The date should be set to current moment.",
            prefs.getLong(FeatureManager.KEY_LAST_SYNC_DATE, -1) > DateUtils.getTodayTime());
    }

    /** Tests registration of sync attempt. */
    public void testRegisterSync_yesterday()
    {
        initSyncLimitTests(1, DateUtils.getTodayTime() - Constants.MILLIS_IN_DAY, 1);
        fm.registerSync();
        assertEquals("Counter should be set.", 1, prefs.getInt(FeatureManager.KEY_SYNCHRONIZATIONS, -1));
        assertTrue("The date should be set to current moment.",
            prefs.getLong(FeatureManager.KEY_LAST_SYNC_DATE, -1) > DateUtils.getTodayTime());
    }

    private void initSyncLimitTests(Integer fmLimit, Long prLastSyncDate, Integer prSyncs)
    {
        prefs = Preferences.userNodeForPackage(getClass());
        fm = new FeatureManager(prefs);

        if (fmLimit != null) fm.setSynchronizationLimit(fmLimit);

        prefs.putLong(FeatureManager.KEY_LAST_SYNC_DATE, prLastSyncDate == null ? -1 : prLastSyncDate);
        prefs.putInt(FeatureManager.KEY_SYNCHRONIZATIONS, prSyncs == null ? 0 : prSyncs);
    }
}
