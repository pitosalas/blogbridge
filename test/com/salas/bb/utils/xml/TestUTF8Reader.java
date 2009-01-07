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
// $Id: TestUTF8Reader.java,v 1.6 2006/01/08 05:28:19 kyank Exp $
//

package com.salas.bb.utils.xml;

import junit.framework.TestCase;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @see UTF8Reader
 */
public class TestUTF8Reader extends TestCase
{
    private static final Logger LOG = Logger.getLogger(TestUTF8Reader.class.getName());

    /**
     * Tests reading plain ASCII characters.
     */
    public void testPlainASCII()
    {
        char[] src = new char[128];
        int[] dst = new int[128];
        for (int i = 0; i < src.length; i++)
        {
            src[i] = (char)i;
            dst[i] = i;
        }

        check(src, dst);
    }

    /**
     * Tests 2-byte multi-byte sequences.
     */
    public void test2Byte()
    {
        // First possible 2-byte sequence 0x80
        check(new char[] { 't', (char)0xc2, (char)0x80, 'e' }, new int[] { 't', 0x80, 'e' });
        check(new char[] { (char)0xc2, (char)0x80 }, new int[] { 0x80 });

        // Something in the middle 0xa9
        check(new char[] { 't', (char)0xc2, (char)0xa9, 'e' }, new int[] { 't', 0xa9, 'e' });
        check(new char[] { (char)0xc2, (char)0xa9 }, new int[] { 0xa9 });

        // Last possible 2-byte sequence 0x7ff
        check(new char[] { 't', (char)0xdf, (char)0xbf, 'e' }, new int[] { 't', 0x7ff, 'e' });
        check(new char[] { (char)0xdf, (char)0xbf }, new int[] { 0x7ff });
    }

    /**
     * Tests 3-byte multi-byte sequences.
     */
    public void test3Byte()
    {
        // First possible 3-byte sequence 0x800
        check(new char[] { 't', (char)0xe0, (char)0xa0, (char)0x80, 'e' },
              new int[] { 't', 0x800, 'e' });
        check(new char[] { (char)0xe0, (char)0xa0, (char)0x80 }, new int[] { 0x800 });

        // Something in the middle 0xd7ff
        check(new char[] { 't', (char)0xed, (char)0x9f, (char)0xbf, 'e' },
              new int[] { 't', 0xd7ff, 'e' });
        check(new char[] { (char)0xed, (char)0x9f, (char)0xbf }, new int[] { 0xd7ff });

        // Last possible 3-byte sequence 0xffff
        check(new char[] { 't', (char)0xef, (char)0xbf, (char)0xbf, 'e' },
              new int[] { 't', 0xffff, 'e' });
        check(new char[] { (char)0xef, (char)0xbf, (char)0xbf }, new int[] { 0xffff });
    }

    /**
     * Tests 4-byte multi-byte sequences.
     */
    public void test4Byte()
    {
        // First possible 4-byte sequence 0x100000
        check(new char[] { 't', (char)0xf0, (char)0x90, (char)0x80, (char)0x80, 'e' },
              new int[] { 't', 0xD800, 0xDC00, 'e' });
        check(new char[] { (char)0xf0, (char)0x90, (char)0x80, (char)0x80 },
              new int[] { 0xD800, 0xDC00 });

        // Something in the middle 0x10ffff
        check(new char[] { 't', (char)0xf4, (char)0x8f, (char)0xbf, (char)0xbf, 'e' },
              new int[] { 't', 0xDBFF, 0xDFFF, 'e' });
        check(new char[] { (char)0xf4, (char)0x8f, (char)0xbf, (char)0xbf },
              new int[] { 0xDBFF, 0xDFFF });

        // Last possible 4-byte sequence 0x1fffff
//        check(new char[] { 't', (char)0xf7, (char)0xbf, (char)0xbf, (char)0xbf, 'e' },
//              new int[] { 't', 0x1fffff, 'e' });
//        check(new char[] { (char)0xf7, (char)0xbf, (char)0xbf, (char)0xbf },
//              new int[] { 0x1fffff });
    }

    // ---------------------------------------------------------------------------------------------
    // Malformed sequences
    // ---------------------------------------------------------------------------------------------

    /**
     * Tests parsing of unexpected continuation byte.
     */
    public void testFirstContinuationByte()
    {
        check(new char[] { 't', (char)0x80, 'e' }, new int[] { 't', 0x80, 'e' });
        check(new char[] { (char)0x80 }, new int[] { 0x80 });
    }

    /**
     * Tests parsing of unexpected continuation byte.
     */
    public void testLastContinuationByte()
    {
        check(new char[] { 't', (char)0xbf, 'e' }, new int[] { 't', 0xbf, 'e' });
        check(new char[] { (char)0xbf }, new int[] { 0xbf });
    }

    /**
     * Tests parsing of unexpected continuation bytes in group.
     */
    public void testContinuationBytes()
    {
        // 2 continuation bytes
        check(new char[] { 't', (char)0x80, (char)0xbf, 'e' }, new int[] { 't', 0x80, 0xbf, 'e' });
        check(new char[] { (char)0x80, (char)0xbf }, new int[] { 0x80, 0xbf });

        // 3 continuation bytes
        check(new char[] { 't', (char)0x80, (char)0xbf, (char)0x80, 'e' },
            new int[] { 't', 0x80, 0xbf, 0x80, 'e' });
        check(new char[] { (char)0x80, (char)0xbf, (char)0x80 }, new int[] { 0x80, 0xbf, 0x80 });

        // 4 continuation bytes
        check(new char[] { 't', (char)0x80, (char)0xbf, (char)0x80, (char)0xbf, 'e' },
            new int[] { 't', 0x80, 0xbf, 0x80, 0xbf, 'e' });
        check(new char[] { (char)0x80, (char)0xbf, (char)0x80, (char)0xbf },
            new int[] { 0x80, 0xbf, 0x80, 0xbf });

        // 5 continuation bytes
        check(new char[] { 't', (char)0x80, (char)0xbf, (char)0x80, (char)0xbf, (char)0x80, 'e' },
            new int[] { 't', 0x80, 0xbf, 0x80, 0xbf, 0x80, 'e' });
        check(new char[] { (char)0x80, (char)0xbf, (char)0x80, (char)0xbf, (char)0x80 },
            new int[] { 0x80, 0xbf, 0x80, 0xbf, 0x80 });
    }

    /**
     * Tests parsing of unexpected continuation bytes.
     */
    public void testSequenceOfAll64PossibleContinuationBytes()
    {
        char[] src = new char[64];
        int[] dst = new int[64];
        for (int i = 0; i < src.length; i++)
        {
            int ch = 0x80 + i;
            src[i] = (char)ch;
            dst[i] = ch;
        }

        check(src, dst);
    }

    /**
     * Tests parsing of unexpected first bytes of 2-byte multi-byte sequence.
     */
    public void testFirstBytesOf2ByteSequnces()
    {
        char[] src = new char[64];
        int[] dst = new int[64];
        for (int i = 0; i < 32; i++)
        {
            int ch = 0xc0 + i;
            src[i*2] = (char)ch;
            src[i*2 + 1] = ' ';
            dst[i*2] = ch;
            dst[i*2 + 1] = ' ';
        }

        check(src, dst);
    }

    /**
     * Tests parsing of unexpected first bytes of 3-byte multi-byte sequence.
     */
    public void testFirstBytesOf3ByteSequnces()
    {
        char[] src = new char[32];
        int[] dst = new int[32];
        for (int i = 0; i < 16; i++)
        {
            int ch = 0xe0 + i;
            src[i*2] = (char)ch;
            src[i*2 + 1] = ' ';
            dst[i*2] = ch;
            dst[i*2 + 1] = ' ';
        }

        check(src, dst);
    }

    /**
     * Tests parsing of unexpected first bytes of 4-byte multi-byte sequence.
     */
    public void testFirstBytesOf4ByteSequnces()
    {
        char[] src = new char[16];
        int[] dst = new int[16];
        for (int i = 0; i < 8; i++)
        {
            int ch = 0xf0 + i;
            src[i*2] = (char)ch;
            src[i*2 + 1] = ' ';
            dst[i*2] = ch;
            dst[i*2 + 1] = ' ';
        }

        check(src, dst);
    }

    /**
     * Tests sequences without last continuation byte.
     */
    public void testSeqencesWithLastContinuationByteMissing()
    {
        check(new char[] { (char)0xc0 }, new int[] { 0xc0 });
        check(new char[] { (char)0xe0, (char)0x80 }, new int[] { 0xe0, 0x80 });
        check(new char[] { (char)0xf0, (char)0x80, (char)0x80 }, new int[] { 0xf0, 0x80, 0x80 });

        // All sequences joined
        check(new char[] { (char)0xc0, (char)0xe0, (char)0x80, (char)0xf0, (char)0x80, (char)0x80 },
            new int[] { 0xc0, 0xe0, 0x80, 0xf0, 0x80, 0x80 });
    }

    /**
     * Tests different unexpected combinations.
     */
    public void testGoodBadCombinations()
    {
        check(new char[] { (char)0x80, (char)0xc2, (char)0xa9, (char)0x80 },
            new int[] { 0x80, 0xa9, 0x80 });
        check(new char[] { 'a', (char)0xc2, (char)0xa9, (char)0xbb, 'b'},
            new int[] { 'a', 0xa9, 0xbb, 'b' });
        check(new char[] { 'a', (char)0xc2, (char)0xa9, (char)0xe1, 'b'},
            new int[] { 'a', 0xa9, 0xe1, 'b' });

    }

    /**
     * Tests sequences broken into several packet blocks.
     */
    public void testBrockenIntoBlocks()
    {
        check(new char[][]
        {
            new char[] { (char)0xc2 },
            new char[] { (char)0xa9 }
        }, new int[] { 0xa9 });

        check(new char[][]
        {
            new char[] { (char)0xc2 },
            new char[] { (char)0x09 }
        }, new int[] { 0xc2, 0x09 });

        check(new char[][]
        {
            new char[] { (char)0xe2, 0x80 },
            new char[] { (char)0x09 }
        }, new int[] { 0xe2, 0x80, 0x09 });
    }

    /**
     * Small real-life practice test.
     *
     * @throws Exception in case of error.
     */
    public void testPracticeTest() throws Exception
    {
        String s = "<?xml version=\"1.0\" encoding=\"utf-8\"?><root>a" + (char)0xc2 + (char)0xa9 +
            (char)0xbb + "b</root>";

        SAXBuilder builder = new SAXBuilder();
        Reader reader = XmlReaderFactory.create(streamForString(s.toCharArray()));
        Document doc = builder.build(reader);

        String text = doc.getRootElement().getText();

        assertEquals('a', text.charAt(0));
        assertEquals('\u00a9', text.charAt(1));
        assertEquals('\u00bb', text.charAt(2));
        assertEquals('b', text.charAt(3));
    }

    /**
     * Check how source is read and if it matches destination.
     *
     * @param src   source.
     * @param dst   destination.
     */
    private void check(char[] src, int[] dst)
    {
        Reader reader = createReader(src);

        check0(reader, dst);
    }

    /**
     * Tests how several source blocks are read into single stream.
     *
     * @param srcs  source packet blocks.
     * @param dst   destination.
     */
    private void check(char[][] srcs, int[] dst)
    {
        Reader reader = createReader(srcs);

        check0(reader, dst);
    }

    /**
     * Checks if the reader will return exactly destination sequence.
     *
     * @param reader    reader.
     * @param dst       destination.
     */
    private void check0(Reader reader, int[] dst)
    {
        try
        {
            int offset = 0;
            int ch;
            while ((ch = reader.read()) != -1)
            {
                if (LOG.isLoggable(Level.FINE))
                {
                    LOG.fine("Pos: " + offset + " Expected: " + Integer.toHexString(dst[offset]) +
                        " Found: " + Integer.toHexString(ch));
                }

                assertEquals("Pos: " + offset, dst[offset], ch);
                offset++;
            }

            assertEquals("Not all bytes read.", dst.length, offset);
        } catch (IOException e)
        {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Creates our reader for "reading" of sequence of chars.
     *
     * @param chars source sequence.
     *
     * @return reader.
     */
    private UTF8Reader createReader(char[] chars)
    {
        return new UTF8Reader(streamForString(chars));
    }

    /**
     * Creates our reader for "reading" of sequence of charse broken into packet blocks.
     *
     * @param charsList list of packets.
     *
     * @return reader.
     */
    private UTF8Reader createReader(char[][] charsList)
    {
        InputStream[] streams = new InputStream[charsList.length];
        for (int i = 0; i < charsList.length; i++)
        {
            char[] chars = charsList[i];
            streams[i] = streamForString(chars);
        }

        return new UTF8Reader(new CombinedInputStream(streams));
    }

    /**
     * Simple configurer of FINE logging.
     */
    private void setFineLogging()
    {
        LOG.setLevel(Level.FINE);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINE);
        LOG.addHandler(handler);
    }

    /**
     * Sets how combined stream does its job.
     */
    public void testCombinedInputStream()
        throws IOException
    {
        InputStream[] streams = new InputStream[]
        {
            streamForString("abcdefg".toCharArray()),
            streamForString("gfedcba".toCharArray()),
            streamForString("1".toCharArray()),
            streamForString("ab".toCharArray())
        };

        InputStream is = new CombinedInputStream(streams);
        byte[] buf = new byte[10];

        try
        {
            assertEquals(7, is.read(buf));
            assertEquals(7, is.read(buf));
            assertEquals(1, is.read(buf));
            assertEquals(2, is.read(buf));
            assertEquals(-1, is.read(buf));
        } catch (IOException e)
        {
            e.printStackTrace();
            fail();
        } finally
        {
            is.close();
        }
    }

    /**
     * Simple combined stream for combining several packet blocks into single stream,
     * but through several reads.
     */
    private static class CombinedInputStream extends InputStream
    {
        private InputStream[]   streams;
        private int             current;

        public CombinedInputStream(InputStream[] streams)
        {
            this.streams = streams;
            current = 0;
        }

        public int read() throws IOException
        {
            int ch = -1;

            while (ch == -1 && current < streams.length)
            {
                InputStream currentStream = streams[current];
                ch = currentStream.read();
                if (ch < 0) current++;
            }

            return ch;
        }

        public int read(byte b[]) throws IOException
        {
            int read = -1;

            // No checks here because it's only for testing purposes
            if (current < streams.length)
            {
                read = streams[current].read(b);
                if (read <= 0)
                {
                    current++;
                    if (current < streams.length) read = streams[current].read(b);
                }
            }

            return read;
        }

        public int read(byte b[], int off, int len) throws IOException
        {
            int read = -1;

            // No checks here because it's only for testing purposes
            if (current < streams.length)
            {
                read = streams[current].read(b, off, len);
                if (read <= 0)
                {
                    current++;
                    if (current < streams.length) read = streams[current].read(b, off, len);
                }
            }

            return read;
        }
    }

    /** Creates stream for string. */
    private InputStream streamForString(char[] text)
    {
        byte[] bytes = new byte[text.length];
        for (int i = 0; i < text.length; i++) bytes[i] = (byte)text[i];
        return new ByteArrayInputStream(bytes);
    }
}
