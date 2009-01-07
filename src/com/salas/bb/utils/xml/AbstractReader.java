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
// $Id: AbstractReader.java,v 1.2 2006/01/08 05:00:10 kyank Exp $
//

package com.salas.bb.utils.xml;

import java.io.Reader;
import java.io.InputStream;
import java.io.IOException;

/**
 * Abstract reader wrapper for <code>InputStream</code>.
 */
abstract class AbstractReader extends Reader
{
    // Size of internal buffer
    private static final int BUFFER_SIZE = 8192;

    // Current position in buffer
    protected int           pos;

    // The length of occupied buffer space
    protected int           length;

    // Wrapped stream
    protected InputStream   stream;

    // Internal buffer
    protected byte[]        buffer;

    /**
     * Creates a wrapper reader for input stream.
     *
     * @param aStream stream to wrap.
     */
    AbstractReader(InputStream aStream)
    {
        super(aStream);

        this.stream = aStream;
        buffer = new byte[BUFFER_SIZE];
    }

    /**
     * Tell whether this stream is ready to be read.
     *
     * @return True if the next read() is guaranteed not to block for input, false otherwise.  Note
     *         that returning false does not guarantee that the next read will block.
     *
     * @throws java.io.IOException If an I/O error occurs
     */
    public boolean ready()
        throws IOException
    {
        return stream == null || (length - pos) > 0 || stream.available() != 0;
    }

    /**
     * Close the stream.  Once a stream has been closed, further read(), ready(), mark(), or reset()
     * invocations will throw an IOException. Closing a previously-closed stream, however, has no
     * effect.
     *
     * @throws java.io.IOException If an I/O error occurs
     */
    public void close()
        throws IOException
    {
        if (stream != null)
        {
            stream.close();
            pos = 0;
            length = 0;
            buffer = null;
            stream = null;
        }
    }

    /**
     * Loads more data into buffer if current position is at the end.
     *
     * @throws IOException in case of I/O error.
     */
    protected void updateBufferIfNecessary()
        throws IOException
    {
        if (pos >= length)
        {
            pos = 0;
            length = stream.read(buffer, 0, buffer.length);
            if (length <= 0) close();
        }
    }
}
