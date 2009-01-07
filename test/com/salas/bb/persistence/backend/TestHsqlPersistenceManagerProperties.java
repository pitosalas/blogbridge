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
// $Id: TestHsqlPersistenceManagerProperties.java,v 1.1 2008/02/20 16:11:06 spyromus Exp $
//

package com.salas.bb.persistence.backend;

import com.salas.bb.persistence.PersistenceException;

/**
 * This suite contains tests for <code>HsqlPersistenceManager</code> unit.
 */
public class TestHsqlPersistenceManagerProperties extends AbstractHsqlPersistenceTestCase
{
    private static final String MISSING = "missing";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        initManager("/resources");

        pm.getConnection().createStatement().executeUpdate("DELETE FROM APP_PROPERTIES");
    }
    
    /**
     * Asking for missing property.
     *
     * @throws PersistenceException if database fails.
     */
    public void testAskingForMissingProperty()
        throws PersistenceException
    {
        assertNull(pm.getApplicationProperty(MISSING));
    }

    /**
     * Setting application property and retreiving the value.
     *
     * @throws PersistenceException if database fails.
     */
    public void testSettingApplicationProperty()
        throws PersistenceException
    {
        String key = "test";
        String value = "value";
        pm.setApplicationProperty(key, value);
        assertEquals(value, pm.getApplicationProperty(key));
    }

    /**
     * Updating application property and retreiving the value.
     *
     * @throws PersistenceException if database fails.
     */
    public void testUpdatingApplicationProperty()
        throws PersistenceException
    {
        String key = "test";
        String value = "value";
        String value2 = "value2";
        pm.setApplicationProperty(key, value);
        pm.setApplicationProperty(key, value2);
        assertEquals(value2, pm.getApplicationProperty(key));
    }

    /**
     * Removing an application property.
     *
     * @throws PersistenceException if database fails.
     */
    public void testDeletingApplicationProperty()
        throws PersistenceException
    {
        String key = "test";
        pm.setApplicationProperty(key, "value");
        pm.setApplicationProperty(key, null);
        assertNull(pm.getApplicationProperty(key));
    }
}