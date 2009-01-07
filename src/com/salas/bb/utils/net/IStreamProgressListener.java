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
// $Id: IStreamProgressListener.java,v 1.3 2006/01/08 05:00:09 kyank Exp $
//

package com.salas.bb.utils.net;

import java.util.EventListener;
import java.io.IOException;

/**
 * Listener of progress events from <code>URLInputStream</code>.
 */
public interface IStreamProgressListener extends EventListener
{
    /**
     * Indicates the the source is being connected.
     *
     * @param source source.
     */
    void connecting(URLInputStream source);

    /**
     * Indicates that the source has been successfully connected.
     *
     * @param source source.
     * @param length length of the resource (-1 if unknown).
     */
    void connected(URLInputStream source, long length);

    /**
     * Indicates that some bytes has been read.
     *
     * @param source source.
     * @param bytes  bytes.
     */
    void read(URLInputStream source, long bytes);

    /**
     * Indicates that the stream has been finished.
     *
     * @param source source.
     */
    void finished(URLInputStream source);

    /**
     * Indicates that there's an error happened during reading of stream. (We already did attempts
     * to recover).
     *
     * @param source source.
     * @param ex     cause of error.
     */
    void errored(URLInputStream source, IOException ex);
}
