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
// $Id: UTF8Reader.java,v 1.3 2006/01/08 05:00:10 kyank Exp $
//

package com.salas.bb.utils.xml;

import java.io.InputStream;
import java.io.IOException;
import java.io.CharConversionException;
import java.io.Reader;

/**
 * High-speed reader of any UTF-8-like stream. It's capable of reading both valid and invalid
 * streams. If it finds invalid UTF-8 sequences it uses invalid bytes as bytes from ISO-8859-1
 * and continues parsing. This approach guaranties that <b>any</b> stream will be parsed,
 * but we do not guaranty the correctness of our own interpretation of invalid sequences.
 */
public final class UTF8Reader extends Reader
{
    private InputStream in;
    private byte[]      buffer;
    private int         start;
    private int         finish;

    private char        secondHalf;

    private int         multibyteChar;
    private int         multibyteCharsToGo;
    private int         multibyteCharsRead;

    /**
     * Creates UTF-8 reader-interpreter for the stream.
     *
     * @param stream source stream.
     */
    public UTF8Reader(InputStream stream)
    {
        in = stream;
        buffer = new byte[8192];

        finish = 0;
        start = 0;

        resetMultibyte();
    }

    /**
     * Close the stream.  Once a stream has been closed, further read(),
     * ready(), mark(), or reset() invocations will throw an IOException.
     * Closing a previously-closed stream, however, has no effect.
     *
     * @throws IOException If an I/O error occurs
     */
    public void close() throws IOException
    {
        if (in != null)
        {
            in.close();
            buffer = null;
            in = null;
            start = 0;
            finish = 0;
        }
    }

    /**
     * Tell whether this stream is ready to be read.
     *
     * @return TRUE if the next read() is guaranteed not to block for input,
     *         false otherwise.  Note that returning false does not guarantee that the
     *         next read will block.
     *
     * @throws IOException If an I/O error occurs
     */
    public boolean ready()
        throws IOException
    {
        return finish > start || in == null || in.available() != 0;
    }

    /**
     * Reads maximum <code>len</code> bytes from stream into the target buffer starting from
     * specified <code>offset</code>.
     *
     * @param buf       target buffer.
     * @param offset    offset in buffer.
     * @param len       max bytes to read in.
     *
     * @return number of bytes read or -1 if the stream is over.
     *
     * @throws IOException in case of I/O error.
     */
    public int read(char[] buf, int offset, int len)
        throws IOException
    {
        int index = 0;
        int ch = 0;

        if (len <= 0) return 0;

        if (secondHalf != 0)
        {
            buf[offset + index] = secondHalf;
            index++;
            secondHalf = 0;
        }

        while (index < len)
        {
            if (finish <= start)
            {
                int readCount = -1;

                if (in != null)
                {
                    int readOffset = 0;

                    // compact if necessary
                    if (multibyteCharsToGo > 0)
                    {
                        int off = start - (1 + multibyteCharsRead);
                        int length = finish - off;
                        System.arraycopy(buffer, off, buffer, 0, length);
                        readOffset = length;
                        start = length;
                    } else
                    {
                        start = 0;
                    }
                    readCount = in.read(buffer, readOffset, buffer.length - readOffset);
                }

                if (readCount <= 0)
                {
                    if (multibyteCharsToGo > 0)
                    {
                        // Stream finished, but we have not finished job yet
                        finish = start;
                        index = saveMultiByteStartAndRewind(buf, offset, index);
                        continue;
                    } else
                    {
                        // Close and exit
                        close();
                        ch = -1;
                        break;
                    }
                } else
                {
                    finish = start + readCount;
                }
            }

            // Get next char
            ch = buffer[start] & 0x0ff;

            if (multibyteCharsToGo > 0)
            {
                // multi-byte sequence continues...
                if ((ch & 0xc0) == 0x80)
                {
                    // valid continuation byte
                    multibyteChar = (multibyteChar << 6) | (ch & 0x3f);
                    multibyteCharsToGo--;
                    multibyteCharsRead++;
                    start++;

                    if (multibyteCharsToGo == 0)
                    {
                        // finished reading multi-byte successfully -- write it to the target
                        // buffer and forget

                        // Unicode supports c <= 0x0010 ffff ...
                        if (multibyteChar > 0x0010ffff)
                        {
                            throw new CharConversionException("UTF-8 encoding of character 0x00" +
                                Integer.toHexString(multibyteChar) +
                                " can't be converted to Unicode.");
                        } else if (multibyteChar > 0xffff)
                        {
                            // Convert UCS-4 char to UTF-16
                            multibyteChar -= 0x10000;
                            secondHalf = (char)(0xDC00 + (multibyteChar & 0x03ff));
                            multibyteChar = 0xD800 + (multibyteChar >> 10);
                        }

                        buf[offset + index++] = (char)multibyteChar;
                        if (secondHalf != 0 && index < len)
                        {
                            buf[offset + index++] = secondHalf;
                            secondHalf = 0;
                        }
                        resetMultibyte();
                    }
                } else
                {
                    // the sequence got broken -- write first byte as is and rewind to the
                    // first continuation byte
                    index = saveMultiByteStartAndRewind(buf, offset, index);
                }
            } else
            {
                // Find multi-byte sequence start, others - ASCII of ISO-8859-1
                if ((ch & 0x0E0) == 0x0C0)
                {
                    // 2 bytes (0x0080 - 0x07FF)
                    multibyteChar = ch & 0x1F;
                    multibyteCharsToGo = 1;
                    ch = -1;
                } else if ((ch & 0x0F0) == 0x0E0)
                {
                    // 3 bytes (0x0800 - 0xFFFF)
                    multibyteChar = ch & 0x0F;
                    multibyteCharsToGo = 2;
                    ch = -1;
                } else if ((ch & 0x0F8) == 0x0F0)
                {
                    // 4 bytes (0x0001 0000  <= c  <= 0x001F FFFF)
                    multibyteChar = ch & 0x07;
                    multibyteCharsToGo = 3;
                    ch = -1;
                }

                // Write if there's anything to write
                if (ch != -1) buf[offset + index++] = (char)ch;
                start++;
            }
        }

        return (index > 0) ? index : (ch == -1) ? -1 : 0;
    }

    /**
     * Saves starting byte of false multi-byte sequence into buffer and rewind to
     * first continuation byte to start further parsing from (if any continuation bytes
     * were read of course).
     *
     * @param buf       target buffer.
     * @param offset    offset in target buffer.
     * @param i         current index in target buffer relative to offset.
     *
     * @return new index value.
     */
    private int saveMultiByteStartAndRewind(char[] buf, int offset, int i)
    {
        start -= multibyteCharsRead;
        buf[offset + i++] = (char)(buffer[start - 1] & 0xFF);
        resetMultibyte();

        return i;
    }

    /**
     * Resets all multi-byte properties into initial state.
     */
    private void resetMultibyte()
    {
        multibyteChar = 0;
        multibyteCharsToGo = 0;
        multibyteCharsRead = 0;
    }
}
