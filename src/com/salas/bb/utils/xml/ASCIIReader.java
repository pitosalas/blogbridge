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
// $Id: ASCIIReader.java,v 1.2 2006/01/08 05:00:10 kyank Exp $
//

package com.salas.bb.utils.xml;

import java.io.InputStream;
import java.io.IOException;
import java.io.CharConversionException;

/**
 * Reader wrapper for ASCII characters stream.
 */
public class ASCIIReader extends AbstractReader
{
    /**
     * Creates a wrapper reader for input stream.
     *
     * @param aStream stream to wrap.
     */
    public ASCIIReader(InputStream aStream)
    {
        super(aStream);
    }

    /**
     * Read characters into a portion of an array.  This method will block until some input is
     * available, an I/O error occurs, or the end of the stream is reached.
     *
     * @param cbuf Destination buffer
     * @param off  Offset at which to start storing characters
     * @param len  Maximum number of characters to read
     *
     * @return The number of characters read, or -1 if the end of the stream has been reached
     *
     * @throws java.io.IOException If an I/O error occurs
     */
    public int read(char cbuf[], int off, int len)
        throws IOException
    {
        if (stream == null) return -1;

        if ((off + len) > cbuf.length || off < 0) throw new ArrayIndexOutOfBoundsException();

        int i;
        for (i = 0; i < len; i++)
        {
            updateBufferIfNecessary();

            if (buffer == null) break;

            cbuf[off + i] = convertToChar(buffer[pos++] & 0xff);
        }

        return i == 0 && length <= 0 ? -1 : i;
    }

    /**
     * Converts integer from stream to char.
     *
     * @param ch    char from stream.
     *
     * @return char to put in output buffer.
     *
     * @throws CharConversionException if there's a conversion exception.
     */
    protected char convertToChar(int ch)
        throws CharConversionException
    {
        if (ch >= 0x80)
        {
            throw new CharConversionException("Illegal ASCII character, 0x" +
                Integer.toHexString(ch));
        }

        return (char)ch;
    }
}
