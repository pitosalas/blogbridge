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
// $Id: UISException.java,v 1.2 2006/01/08 05:00:09 kyank Exp $
//

package com.salas.bb.utils.net;

import java.io.IOException;

/**
 * General <code>URLInputStream</code> exception.
 */
public abstract class UISException extends IOException
{
    protected int code;

    /**
     * Creates exception.
     *
     * @param aCode     code of error (see HTTP1/1).
     * @param aMessage  error message.
     */
    public UISException(int aCode, String aMessage)
    {
        super(aMessage);

        code = aCode;
    }

    /**
     * Returns code of error.
     *
     * @return code.
     */
    public int getCode()
    {
        return code;
    }
}
