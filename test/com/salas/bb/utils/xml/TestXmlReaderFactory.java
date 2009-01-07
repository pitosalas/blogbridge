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
// $Id: TestXmlReaderFactory.java,v 1.2 2006/01/08 05:28:19 kyank Exp $
//

package com.salas.bb.utils.xml;

import junit.framework.TestCase;

import java.util.Map;
import java.util.HashMap;
import java.io.*;

import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.Element;

/**
 * @see XmlReaderFactory
 */
public class TestXmlReaderFactory extends TestCase
{
    /**
     * @see XmlReaderFactory#putKeysInMap
     */
    public void testPutKeysInMap()
    {
        Map map = new HashMap();

        // NULL handling
        XmlReaderFactory.putKeysInMap(null, null, null);
        XmlReaderFactory.putKeysInMap(map, null, null);
        assertEquals(0, map.size());
        XmlReaderFactory.putKeysInMap(map, new String[] { "a" }, null);
        assertEquals(0, map.size());
        XmlReaderFactory.putKeysInMap(map, null, "a");
        assertEquals(0, map.size());
        XmlReaderFactory.putKeysInMap(null, new String[] { "a" }, "a");

        XmlReaderFactory.putKeysInMap(map, new String[0], "a");
        assertEquals(0, map.size());

        XmlReaderFactory.putKeysInMap(map, new String[] { "a", " b", " c ", "d " }, " a ");
        assertEquals(4, map.size());
        assertEquals("a", map.get("a"));
        assertEquals("a", map.get("b"));
        assertEquals("a", map.get("c"));
        assertEquals("a", map.get("d"));
    }

    /**
     * @see XmlReaderFactory#convertEncodingAlias
     */
    public void testConvertEncodingAlias()
    {
        assertNull(XmlReaderFactory.convertEncodingAlias(null));

        assertEquals("utf-8", XmlReaderFactory.convertEncodingAlias("utf-8"));
        assertEquals("Unicode", XmlReaderFactory.convertEncodingAlias("utf-16"));
    }

    /**
     * @see XmlReaderFactory#createReaderForMappedEncoding
     */
    public void testCreateReaderForMappedEncoding()
    {
        InputStream is;

        // NULL handling
        is = new ByteArrayInputStream(new byte[0]);
        assertNull(XmlReaderFactory.createReaderForMappedEncoding(null, null));
        assertNull(XmlReaderFactory.createReaderForMappedEncoding(is, null));
        assertNull(XmlReaderFactory.createReaderForMappedEncoding(null, "utf-8"));

        // Mapped readers
        Reader reader;
        reader = XmlReaderFactory.createReaderForMappedEncoding(is, "utf-8");
        assertTrue(reader instanceof UTF8Reader);
        reader = XmlReaderFactory.createReaderForMappedEncoding(is, "us-ascii");
        assertTrue(reader instanceof ASCIIReader);
        reader = XmlReaderFactory.createReaderForMappedEncoding(is, "iso-8859-1");
        assertTrue(reader instanceof ISO88591Reader);

        // Other readers
        assertNull(XmlReaderFactory.createReaderForMappedEncoding(is, "utf-16"));
    }

    /**
     * @see XmlReaderFactory#createReaderForEncoding
     *
     * @throws IOException in case of I/O errors.
     */
    public void testCreateReaderForEncoding() throws IOException
    {
        InputStream is;

        // NULL handling
        is = new ByteArrayInputStream(new byte[0]);
        assertNull(XmlReaderFactory.createReaderForEncoding(null, null));
        assertNull(XmlReaderFactory.createReaderForEncoding(is, null));
        assertNull(XmlReaderFactory.createReaderForEncoding(null, "utf-8"));

        // Mapped readers
        Reader reader;
        reader = XmlReaderFactory.createReaderForEncoding(is, "utf-8");
        assertTrue(reader instanceof UTF8Reader);
        reader = XmlReaderFactory.createReaderForEncoding(is, "us-ascii");
        assertTrue(reader instanceof ASCIIReader);
        reader = XmlReaderFactory.createReaderForEncoding(is, "iso-8859-1");
        assertTrue(reader instanceof ISO88591Reader);

        // Other readers
        reader = XmlReaderFactory.createReaderForEncoding(is, "utf-16");
        assertTrue(reader instanceof InputStreamReader);
    }

    /**
     * @see XmlReaderFactory#create
     */
    public void testCreate() throws IOException
    {
        assertNull(XmlReaderFactory.create(null));

        check(UTF8Reader.class, "<root/>");
        check(UTF8Reader.class, "<?xml version='1.0'?><root/>");
        check(InputStreamReader.class, "<?xml version=\"1.0\" encoding=\"windows-1251\"?><root/>");
        check(ASCIIReader.class, "<?xml version=\"1.0\" encoding=\"us-ascii\"?><root/>");
        check(ISO88591Reader.class, "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><root/>");
        check(ISO88591Reader.class, "<?xml version=\"1.0\" encoding=\"MacRoman\"?><root/>");
    }

    private void check(Class readerClass, String text)
    {
        try
        {
            Reader reader = XmlReaderFactory.create(streamForString(text));
            assertNotNull(reader);
            assertTrue(reader.getClass() == readerClass);

            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(reader);
            assertNotNull(document);

            Element root = document.getRootElement();
            assertNotNull(root);
            assertEquals("root", root.getName());
        } catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }

    /** Creates stream for string. */
    private InputStream streamForString(String text)
    {
        return new ByteArrayInputStream(text.getBytes());
    }
}
