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
// $Id: UrlDiscovererException.java,v 1.2 2006/01/08 05:00:08 kyank Exp $
//

package com.salas.bb.utils.discovery;

/**
 * Exception, thrown by <code>UrlDiscoverer</code> in case of any unrecoverable errors.
 */
public class UrlDiscovererException extends Exception
{

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public UrlDiscovererException(String message)
    {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause original cause.
     */
    public UrlDiscovererException(Throwable cause)
    {
        super(cause);
    }
}
