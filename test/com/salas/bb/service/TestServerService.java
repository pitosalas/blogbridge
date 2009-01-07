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
// $Id: TestServerService.java,v 1.4 2006/11/14 13:19:46 spyromus Exp $
//

package com.salas.bb.service;

import junit.framework.TestCase;

import java.util.*;

import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.ResourceID;

/**
 * @see ServerService
 */
public class TestServerService extends TestCase
{
    /**
     * Small setup.
     */
    protected void setUp()
        throws Exception
    {
        System.setProperty(ResourceID.URL_SERVICE,
            "http://localhost:8080/bbservice/servlet/Service");
    }

    /**
     * @see ServerService#convertFieldToServiceValue
     */
    public void testConvertFieldToServiceValue()
    {
        Object value;
        Vector vector;

        // Test null-conversion
        assertNull(ServerService.convertFieldToServiceValue(null));

        // String conversion
        value = ServerService.convertFieldToServiceValue("a");
        assertTrue(value instanceof byte[]);
        assertTrue(Arrays.equals(StringUtils.toUTF8("a"), (byte[])value));

        // String array conversion
        value = ServerService.convertFieldToServiceValue(new String[] { "a", "b" });
        assertTrue(value instanceof Vector);
        vector = (Vector)value;
        assertEquals(2, vector.size());
        assertTrue(vector.get(0) instanceof byte[]);
        assertTrue(Arrays.equals(StringUtils.toUTF8("a"), (byte[])vector.get(0)));
        assertTrue(vector.get(1) instanceof byte[]);
        assertTrue(Arrays.equals(StringUtils.toUTF8("b"), (byte[])vector.get(1)));

        // String vector conversion
        value = ServerService.convertFieldToServiceValue(new Vector() { { add("a"); add("b"); } });
        assertTrue(value instanceof Vector);
        vector = (Vector)value;
        assertEquals(2, vector.size());
        assertTrue(vector.get(0) instanceof byte[]);
        assertTrue(Arrays.equals(StringUtils.toUTF8("a"), (byte[])vector.get(0)));
        assertTrue(vector.get(1) instanceof byte[]);
        assertTrue(Arrays.equals(StringUtils.toUTF8("b"), (byte[])vector.get(1)));
    }

    /**
     * Simple test of basic conversion. The value conversion itself is delegated to
     * <code>convertFieldToServerValue</code>.
     *
     * @see ServerService#convertToServiceFields
     */
    public void testConvertToServiceFields()
    {
        Map fields, newFields;
        Object value;

        // Null-conversion
        assertNull(ServerService.convertToServiceFields(null));

        // Normal conversion
        fields = new HashMap(2);
        fields.put("a", "b");
        fields.put("b", new String[] { "a", "b" });

        newFields = ServerService.convertToServiceFields(fields);
        assertNotNull(newFields);
        assertEquals(2, newFields.size());

        value = newFields.get("a");
        assertNotNull(value);
        assertTrue(value instanceof byte[]);

        value = newFields.get("b");
        assertNotNull(value);
        assertTrue(value instanceof Vector);
    }

    /**
     * @see ServerService#convertFieldToClientValue
     */
    public void testConvertFieldToClientValue()
    {
        // Null-conversion
        assertNull(ServerService.convertFieldToClientValue(null));

        // String conversion
        assertEquals("a", ServerService.convertFieldToClientValue(StringUtils.toUTF8("a")));

        // String vector conversion
        assertTrue(Arrays.equals(
            new String[] { "a", "b" },
            (String[])ServerService.convertFieldToClientValue(new Vector() {{
                add(StringUtils.toUTF8("a"));
                add(StringUtils.toUTF8("b"));
            }})
        ));
    }

    /**
     * Simple test of basic conversion. The value conversion itself is delegated to
     * <code>convertFieldToClientValue</code>.
     *
     * @see ServerService#convertToClientFields
     */
    public void testConvertToClientFields()
    {
        Map fields, newFields;
        Object value;
        String[] array;

        // Null-conversion
        assertNull(ServerService.convertToClientFields(null));

        // Normal conversion
        fields = new Hashtable(2);
        fields.put("a", StringUtils.toUTF8("b"));
        fields.put("b", new Vector() {{
            add(StringUtils.toUTF8("a"));
            add(StringUtils.toUTF8("b"));
        }});

        newFields = ServerService.convertToClientFields(fields);
        assertNotNull(newFields);
        assertEquals(2, newFields.size());

        value = newFields.get("a");
        assertEquals("b", value);

        value = newFields.get("b");
        assertTrue(value instanceof String[]);
        array = (String[])value;
        assertEquals(2, array.length);
        assertEquals("a", array[0]);
        assertEquals("b", array[1]);
    }
}
