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
// $Id: TestManager.java,v 1.3 2007/03/30 08:33:38 spyromus Exp $
//

package com.salas.bb.plugins;

import static com.salas.bb.plugins.Manager.KEY_PLUGINS_PACKAGES;
import static com.salas.bb.plugins.Manager.KEY_PLUGINS_PACKAGES_TS;
import com.salas.bb.utils.StringUtils;
import junit.framework.TestCase;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Tests plug-ins manager.
 */
public class TestManager extends TestCase
{
    private Preferences prefs;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        prefs = Preferences.userNodeForPackage(TestManager.class);
        prefs.clear();
    }

    public void testStoreState()
    {
        Map<String, Object> p = new HashMap<String, Object>();
        Manager.initialize(new File("."), prefs);

        // Store fake keys
        String pk = "test";
        long ts = 1234L;
        prefs.put(KEY_PLUGINS_PACKAGES, pk);
        prefs.putLong(KEY_PLUGINS_PACKAGES_TS, ts);

        Manager.storeState(p);
        assertEquals(pk, StringUtils.fromUTF8((byte[])p.get(KEY_PLUGINS_PACKAGES)));
        assertEquals(Long.toString(ts), StringUtils.fromUTF8((byte[])p.get(KEY_PLUGINS_PACKAGES_TS)));
    }

    public void testRestoreState()
        throws BackingStoreException
    {
        Map<String, Object> p = new HashMap<String, Object>();
        Manager.initialize(new File("."), prefs);

        // Test data
        String pk0 = "test0.jar";
        String pk1 = "test1.jar";
        long ts0 = 1234L;
        long ts1 = 4321L;

        // 1. p has no info, prefs has no info
        Manager.restoreState(p);
        assertNull(prefs.get(KEY_PLUGINS_PACKAGES, null));
        assertNull(prefs.get(KEY_PLUGINS_PACKAGES_TS, null));

        // 2. p has no info, prefs has
        setPrefs(pk0, ts0);
        Manager.restoreState(p);
        assertPrefs(pk0, ts0);

        // 3. p has info, prefs doesn't
        prefs.clear();
        setPrefs(p, pk0, ts0);
        Manager.restoreState(p);
        assertPrefs(pk0, ts0);

        // 4. p is earlier than prefs
        setPrefs(pk1, ts1);
        setPrefs(p, pk0, ts0);
        Manager.restoreState(p);
        assertPrefs(pk1, ts1);

        // 4. p is later than prefs
        setPrefs(pk0, ts0);
        setPrefs(p, pk1, ts1);
        Manager.restoreState(p);
        assertPrefs(pk1, ts1);
    }

    private void assertPrefs(String pkgs, long ts)
    {
        assertEquals(pkgs, prefs.get(KEY_PLUGINS_PACKAGES, null));
        assertEquals(ts, prefs.getLong(KEY_PLUGINS_PACKAGES_TS, -1));
    }

    private void setPrefs(Map<String, Object> p, String pkgs, long ts)
    {
        p.put(KEY_PLUGINS_PACKAGES, pkgs.getBytes());
        p.put(KEY_PLUGINS_PACKAGES_TS, Long.toString(ts).getBytes());
    }

    private void setPrefs(String pkgs, long ts)
        throws BackingStoreException
    {
        prefs.clear();
        prefs.put(KEY_PLUGINS_PACKAGES, pkgs);
        prefs.putLong(KEY_PLUGINS_PACKAGES_TS, ts);
    }
}
