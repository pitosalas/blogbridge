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
// $Id: TestEncodingDetector.java,v 1.3 2006/01/08 05:28:19 kyank Exp $
//

package com.salas.bb.utils.xml;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @see EncodingDetector
 */
public class TestEncodingDetector extends TestCase
{
    /**
     * Tests normal xml without BOM and declaration header.
     */
    public void testNormal()
    {
        check("<a></a>", "UTF-8", '<');
        check("<xmla></xmla>", "UTF-8", '<');
    }

    /**
     * Tests BOM-based detection.
     */
    public void testBOM()
    {
        check(new String(new char[] { (char)0xff, (char)0xfe, ' ', 0, '1', 0}), "UTF-16", ' ');
        check(new String(new char[] { (char)0xfe, (char)0xff, ' ', 0, '1', 0}), "UTF-16", ' ');
        check(new String(new char[] { (char)0xef, (char)0xbb, (char)0xbf, '1', 0}), "UTF-8", '1');
    }

    /**
     * Tests detection based on bytes order.
     */
    public void testBytes()
    {
        check(new String(new char[] { (char)0, '<', (char)0, 'a', (char)0, '>' }), "UnicodeBig", (char)0);
        check(new String(new char[] { '<', (char)0, 'a', (char)0, '>', (char)0 }), "UnicodeLittle", '<');
    }

    /**
     * Tests declaration-based detection.
     */
    public void testDeclaration()
    {
        check("<?xml version='1.0'?>", "UTF-8", '<');
        check("<?xml version=\"1.0\"?>", "UTF-8", '<');

        check("<?xml encoding='windows-1251' version=\"1.0\"?>", "windows-1251", '<');
        check("<?xml encoding=\"windows-1251\" version=\"1.0\"?>", "windows-1251", '<');
        check("<?xml encoding = \"windows-1251\" version=\"1.0\"?>", "windows-1251", '<');

        check("<?xml version=\"1.0\" encoding='windows-1251'?>", "windows-1251", '<');
        check("<?xml version=\"1.0\" encoding=\"windows-1251\"?>", "windows-1251", '<');
        check("<?xml version=\"1.0\" encoding = \"windows-1251\"?>", "windows-1251", '<');

        check("<?xml encoding=' windows-1251 ' ?>", "windows-1251", '<');
    }

    /**
     * Tests reading of declaration header.
     *
     * @throws Exception in case of errors.
     */
    public void testReadDeclarationHeader() throws Exception
    {
        assertFalse(EncodingDetector.readDeclarationHeader(null));

        assertFalse(EncodingDetector.readDeclarationHeader(streamForString("<?xmla")));
        assertFalse(EncodingDetector.readDeclarationHeader(streamForString("<?xmm ")));
        assertFalse(EncodingDetector.readDeclarationHeader(streamForString(" <?xml ")));
        assertFalse(EncodingDetector.readDeclarationHeader(streamForString("<? xml ")));
        assertFalse(EncodingDetector.readDeclarationHeader(streamForString("<?xml")));

        assertTrue(EncodingDetector.readDeclarationHeader(streamForString("<?xml ")));
    }

    /**
     * Tests reading attribute values with optional spaces.
     *
     * @throws Exception in case of errors.
     */
    public void testReadAttributeValue() throws Exception
    {
        InputStream is;
        StringBuffer buf = null;

        assertEquals(-1, EncodingDetector.readAttributeValue(null, buf));

        // Empty stream
        is = streamForString("");
        buf = new StringBuffer();
        assertEquals(-1, EncodingDetector.readAttributeValue(is, buf));
        assertEquals(0, buf.length());

        // Empty stream
        is = streamForString(" 'abc' ");
        buf = new StringBuffer();
        assertEquals(' ', EncodingDetector.readAttributeValue(is, buf));
        assertEquals("abc", buf.toString());
    }

    /**
     * Tests reading of encoding attribute.
     *
     * @throws Exception in case of errors.
     */
    public void testReadAttributeValueString() throws Exception
    {
        InputStream is;

        assertEquals(null, EncodingDetector.readAttributeValue(null, (String)null));
        assertEquals(null, EncodingDetector.readAttributeValue(null, "encoding"));

        // Empty stream
        is = streamForString("");
        assertEquals(null, EncodingDetector.readAttributeValue(is, (String)null));
        assertEquals(null, EncodingDetector.readAttributeValue(is, "encoding"));

        // No attribute
        assertEquals(null, EncodingDetector.readAttributeValue(streamForString("encoding?>"), "encoding"));
        assertEquals(null, EncodingDetector.readAttributeValue(streamForString("encoding ?>"), "encoding"));
        assertEquals(null, EncodingDetector.readAttributeValue(streamForString("encodin='a' ?>"), "encoding"));
        assertEquals(null, EncodingDetector.readAttributeValue(streamForString("encoding enc='a'?>"), "encoding"));

        // Attribute present
        assertEquals("utf-8", EncodingDetector.readAttributeValue(streamForString("version='1.0' encoding='utf-8'?>"), "encoding"));
        assertEquals("utf-8", EncodingDetector.readAttributeValue(streamForString("version='1.0' encoding = 'utf-8' ?>"), "encoding"));
        assertEquals("utf-8", EncodingDetector.readAttributeValue(streamForString("version encoding = 'utf-8' ?>"), "encoding"));
        assertEquals("utf-8", EncodingDetector.readAttributeValue(streamForString("encoding = 'utf-8' version?>"), "encoding"));

    }

    /**
     * Tests reading of attribute values without spaces in the beginning.
     *
     * @throws Exception in case of errors.
     */
    public void testReadAttributeValueNoSpace() throws Exception
    {
        InputStream is;
        StringBuffer buf;

        // Empty stream
        is = streamForString("");
        buf = new StringBuffer();
        assertEquals(-1, EncodingDetector.readAttributeValueNoSpace(is, -1, buf));
        assertEquals(0, buf.length());

        // Unexpected chars -- non-quotes: "a"
        is = streamForString("");
        buf = new StringBuffer();
        assertEquals('a', EncodingDetector.readAttributeValueNoSpace(is, 'a', buf));
        assertEquals(0, buf.length());

        // Unclosed quotes: "'abc"
        is = streamForString("abc");
        buf = new StringBuffer();
        assertEquals(-1, EncodingDetector.readAttributeValueNoSpace(is, '\'', buf));
        assertEquals(0, buf.length());

        // Unclosed quotes: '"abc'
        is = streamForString("abc");
        buf = new StringBuffer();
        assertEquals(-1, EncodingDetector.readAttributeValueNoSpace(is, '"', buf));
        assertEquals(0, buf.length());

        // Success
        is = streamForString("abc'");
        buf = new StringBuffer();
        assertEquals(-1, EncodingDetector.readAttributeValueNoSpace(is, '\'', buf));
        assertEquals("abc", buf.toString());

        // Success
        is = streamForString("abc\"");
        buf = new StringBuffer();
        assertEquals(-1, EncodingDetector.readAttributeValueNoSpace(is, '"', buf));
        assertEquals("abc", buf.toString());

        // Success
        is = streamForString("a'b'c\"");
        buf = new StringBuffer();
        assertEquals(-1, EncodingDetector.readAttributeValueNoSpace(is, '"', buf));
        assertEquals("a'b'c", buf.toString());

        // Success
        is = streamForString("a\"b\"c'");
        buf = new StringBuffer();
        assertEquals(-1, EncodingDetector.readAttributeValueNoSpace(is, '\'', buf));
        assertEquals("a\"b\"c", buf.toString());

        // Success
        is = streamForString("abc' ");
        buf = new StringBuffer();
        assertEquals(' ', EncodingDetector.readAttributeValueNoSpace(is, '\'', buf));
        assertEquals("abc", buf.toString());
    }

    /**
     * Tests skipping of attributes values.
     *
     * @throws Exception in case of errors.
     */
    public void testSkipAttributeValue() throws Exception
    {
        InputStream is;
        StringBuffer buf = null;

        // Empty stream
        is = streamForString("");
        assertEquals(-1, EncodingDetector.readAttributeValueNoSpace(is, -1, buf));

        // Unexpected chars -- non-quotes: "a"
        is = streamForString("");
        assertEquals('a', EncodingDetector.readAttributeValueNoSpace(is, 'a', buf));

        // Unclosed quotes: "'abc"
        is = streamForString("abc");
        assertEquals(-1, EncodingDetector.readAttributeValueNoSpace(is, '\'', buf));

        // Unclosed quotes: '"abc'
        is = streamForString("abc");
        assertEquals(-1, EncodingDetector.readAttributeValueNoSpace(is, '"', buf));

        // Success
        is = streamForString("abc'");
        assertEquals(-1, EncodingDetector.readAttributeValueNoSpace(is, '\'', buf));

        // Success
        is = streamForString("abc\"");
        assertEquals(-1, EncodingDetector.readAttributeValueNoSpace(is, '"', buf));

        // Success
        is = streamForString("a'b'c\"");
        assertEquals(-1, EncodingDetector.readAttributeValueNoSpace(is, '"', buf));

        // Success
        is = streamForString("a\"b\"c'");
        assertEquals(-1, EncodingDetector.readAttributeValueNoSpace(is, '\'', buf));

        // Success
        is = streamForString("abc' ");
        assertEquals(' ', EncodingDetector.readAttributeValueNoSpace(is, '\'', buf));
    }

    /**
     * Tests skipping whitespaces.
     *
     * @throws Exception in case of errors.
     */
    public void testSkipWhitespace() throws Exception
    {
        InputStream is;

        assertEquals(-1, EncodingDetector.skipWhitepace(null));

        is = streamForString("");
        assertEquals(-1, EncodingDetector.skipWhitepace(is));

        is = streamForString("a");
        assertEquals('a', EncodingDetector.skipWhitepace(is));

        is = streamForString("   b");
        assertEquals('b', EncodingDetector.skipWhitepace(is));

        is = streamForString("\t\t\tc");
        assertEquals('c', EncodingDetector.skipWhitepace(is));

        is = streamForString("\n\n\nd");
        assertEquals('d', EncodingDetector.skipWhitepace(is));
    }

    /** Creates stream for string. */
    private InputStream streamForString(String text)
    {
        char[] chars = text.toCharArray();
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < chars.length; i++) bytes[i] = (byte)chars[i];
        return new ByteArrayInputStream(bytes);
    }

    /** Checks the detection. */
    private void check(String xml, String encoding, char firstCharInStream)
    {
        InputStream is = streamForString(xml);
        try
        {
            EncodingDetector.DetectionResult result = EncodingDetector.detectEncoding(is);
            assertEquals(encoding, result.getEncoding());
            assertEquals(firstCharInStream, (char)result.getStream().read());
        } catch (IOException e)
        {
            e.printStackTrace();
            fail();
        }
    }
}
