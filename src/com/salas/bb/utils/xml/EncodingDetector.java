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
// $Id: EncodingDetector.java,v 1.6 2006/01/08 05:00:10 kyank Exp $
//

package com.salas.bb.utils.xml;

import java.io.InputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.io.ByteArrayInputStream;

/**
 * Encoding detector.
 */
public final class EncodingDetector
{
    private static final int BUFFER_SIZE = 256;

    /**
     * Hidden utility class constructor.
     */
    private EncodingDetector()
    {
    }

    /**
     * Detect encoding of the stream. The detector reads data from the stream, so,
     * the stream will not be at the same position at which it was before call. You need
     * to use the stream returned in result structure.
     *
     * @param is    input stream.
     *
     * @return result structure with encoding and stream to use.
     *
     * @throws IOException in case of errors.
     */
    public static DetectionResult detectEncoding(InputStream is)
        throws IOException
    {
        String encoding = "UTF-8";
        byte[] buf = new byte[4];

        PushbackInputStream pb = new PushbackInputStream(is, BUFFER_SIZE);
        int len = pb.read(buf);

        int unread = len;
        if (len == 4)
        {
            // Convert first four bytes of the buffer into the 32-bit integer
            // and compare to all known signatures. The comments within the if-blocks
            // show what the signature means.

            int nmb = (buf[0] & 0xff) << 24;
            nmb |= (buf[1] & 0xff) << 16;
            nmb |= (buf[2] & 0xff) << 8;
            nmb |= (buf[3] & 0xff);

            int tempNmb;

            if (nmb == 0x3c3f786d)
            {
                // <?xm
                // The start of <?xml version...?> declaration. We can get encoding from it.
                unread = 0;
                pb.unread(buf, 0, 4);
                encoding = detectEncodingByDeclaration(pb, "UTF-8");
            } else if ((nmb & 0xff00ff00) == 0)
            {
                // Standard "UTF-16 BE" signature.
                encoding = "UnicodeBig";
            } else if ((nmb & 0x00ff00ff) == 0)
            {
                // Standard "UTF-16 LE" signature.
                encoding = "UnicodeLittle";
            } else if (nmb == 0x4c6fa794)
            {
                // Standard "EBCDIC" signature.
                unread = 0;
                encoding = detectEncodingByDeclaration(pb, "CP037");
            } else if ((nmb & 0xffffff00) == 0xefbbbf00)
            {
                // Standard "UTF-8" signature.
                unread = 1;
            } else if ((tempNmb = nmb & 0xffff0000) == 0xfeff0000 || tempNmb == 0xfffe0000)
            {
                // Standard "UTF-16" signature.
                encoding = "UTF-16";
                unread = 2;
            }
        }

        // Unread only these from tail
        if (unread > 0) pb.unread(buf, len - unread, unread);

        return new DetectionResult(encoding, pb);
    }

    /**
     * Detects encoding from XML declaration header.
     *
     * @param pb        pushback input stream.
     * @param encoding  default encoding to return.
     *
     * @return detected or default encoding.
     *
     * @throws IOException in case of errors.
     */
    static String detectEncodingByDeclaration(PushbackInputStream pb, String encoding)
        throws IOException
    {
        byte[] buffer = new byte[BUFFER_SIZE];

        int read = fillBuffer(pb, buffer);
        pb.unread(buffer, 0, read);

        if (read > 0)
        {
            String detected = readEncodingDeclaration(new ByteArrayInputStream(buffer, 0, read));
            if (detected != null) encoding = detected.trim();
        }

        return encoding;
    }

    /**
     * Makes attempt to fill the buffer from the stream.
     *
     * @param is        stream.
     * @param buf       buffer.
     *
     * @return number of characters read.
     *
     * @throws IOException I/O error.
     */
    private static int fillBuffer(InputStream is, byte[] buf)
        throws IOException
    {
        int pos;

        boolean reading = true;
        for (pos = 0; reading && pos < buf.length;)
        {
            int read = is.read(buf, pos, buf.length - pos);
            if (read != -1)
            {
                pos += read;
            } else reading = false;
        }

        return pos;
    }

    /**
     * Reads encoding from declaration.
     *
     * @param is    input stream.
     *
     * @return encoding or NULL if no encoding attribute found or not a valid XML
     *         declaration header found in the start of the stream.
     *
     * @throws IOException in case of errors.
     */
    static String readEncodingDeclaration(InputStream is)
        throws IOException
    {
        return !readDeclarationHeader(is) ? null : readAttributeValue(is, "encoding");
    }

    /**
     * Reads the start of the header "<?xml ".
     *
     * @param is    input stream.
     *
     * @return TRUE if the header was found.
     *
     * @throws IOException in case of errors.
     */
    static boolean readDeclarationHeader(InputStream is)
        throws IOException
    {
        if (is == null) return false;

        return is.read() == '<' && is.read() == '?' && is.read() == 'x' && is.read() == 'm' &&
               is.read() == 'l' && Character.isWhitespace((char)is.read());
    }

    /**
     * Reads the value of a given attribute.
     *
     * @param is    input stream.
     * @param name  name of attribute to read.
     *
     * @return the value or NULL if not found.
     *
     * @throws IOException in case of errors.
     */
    static String readAttributeValue(InputStream is, String name)
        throws IOException
    {
        if (name == null || is == null) return null;

        String value = null;
        int ch = 0;
        int length = name.length();

        while (value == null)
        {
            boolean match = false;
            boolean attrNameRead = false;
            while (!attrNameRead)
            {
                if (ch == 0 && !Character.isWhitespace((char)ch)) ch = skipWhitepace(is);

                // reading attribute name
                if (ch == -1) return null;

                int pos = 0;
                match = true;
                while (ch != -1 && ch != '=' && ch != '?' && !Character.isWhitespace((char)ch))
                {
                    match = match && ((pos < length) && ch == name.charAt(pos++));
                    ch = is.read();
                }

                if (ch == -1 || ch == '?') return null;

                if (ch != '=') ch = skipWhitepace(is);

                match = match && pos == length;
                attrNameRead = (ch == '=');
            }

            if (match)
            {
                StringBuffer buf = new StringBuffer(10);
                ch = readAttributeValue(is, buf);
                value = buf.toString();
            } else ch = readAttributeValue(is, (StringBuffer)null);
        }

        return value;
    }

    /**
     * Reads value of attribute starting from quotes (after optional spaces).
     *
     * @param is    input stream.
     * @param buf   buffer to put value in.
     *
     * @return next char after the value quote, or -1 if stream ended, or unexpected char.
     *
     * @throws IOException in case of I/O error.
     */
    static int readAttributeValue(InputStream is, StringBuffer buf)
        throws IOException
    {
        return is == null ? -1 : readAttributeValueNoSpace(is, skipWhitepace(is), buf);
    }

    /**
     * Reads value of attribute starting right away.
     *
     * @param is    input stream.
     * @param ch    first character of value.
     * @param buf   buffer to fill with valid value or NULL for skipping.
     *
     * @return the next character after attribute value.
     *
     * @throws IOException in case of I/O error.
     */
    static int readAttributeValueNoSpace(InputStream is, int ch, StringBuffer buf)
        throws IOException
    {
        int start = buf == null ? -1 : buf.length();
        int quotes;
        if (ch == '\'' || ch == '"')
        {
            quotes = ch;

            boolean read = false;

            ch = is.read();
            while (ch != -1 && !read)
            {
                if (ch != quotes)
                {
                    if (buf != null) buf.append((char)ch);
                    ch = is.read();
                } else
                {
                    read = true;
                }
            }

            if (!read && buf != null)
            {
                buf.delete(start, buf.length());
            } else
            {
                ch = is.read();
            }
        }
        return ch;
    }

    /**
     * Skips all whitespace from current position.
     *
     * @param is    input stream.
     *
     * @return first non-whitespace char or -1 if stream end found.
     *
     * @throws IOException in case of I/O error.
     */
    static int skipWhitepace(InputStream is)
        throws IOException
    {
        if (is == null) return -1;

        int ch = is.read();

        while (ch != -1 && Character.isWhitespace((char)ch)) ch = is.read();

        return ch;
    }

    /**
     * Result of encoding detection. Holds encoding name and stream to use for further
     * I/O operations.
     */
    public static class DetectionResult
    {
        private String      encoding;
        private InputStream stream;

        /**
         * Creates holder.
         *
         * @param encoding  name of encoding.
         * @param stream    stream to use for further input operations.
         */
        public DetectionResult(String encoding, InputStream stream)
        {
            this.encoding = encoding;
            this.stream = stream;
        }

        /**
         * Returns detected encoding.
         *
         * @return encoding.
         */
        public String getEncoding()
        {
            return encoding;
        }

        /**
         * Returns stream to use for further I/ operations.
         *
         * @return stream to use for further I/ operations.
         */
        public InputStream getStream()
        {
            return stream;
        }
    }
}
