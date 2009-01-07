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
// $Id: FreeEventStream.java,v 1.2 2006/01/08 05:28:16 kyank Exp $
//

package com.salas.bb.networking.manager;

import com.salas.bb.utils.net.URLInputStream;

import java.net.URL;

/**
 * The stream which can be used to simulate different events.
 */
class FreeEventStream extends URLInputStream
{
    /**
     * Creates stream out of URL.
     *
     * @param aSourceUrl non-NULL URL.
     */
    public FreeEventStream(URL aSourceUrl)
    {
        super(aSourceUrl);
    }

    public void fireConnecting0()
    {
        fireConnecting();
    }

    public void fireConnected0(long size)
    {
        fireConnected(size);
    }

    public void fireRead0(int bytes)
    {
        fireRead(bytes);
    }

    public void fireFinished0()
    {
        fireFinished();
    }

    public void fireErrored0()
    {
        fireErrored(null);
    }
}
