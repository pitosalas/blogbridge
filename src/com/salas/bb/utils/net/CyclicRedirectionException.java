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
// $Id: CyclicRedirectionException.java,v 1.3 2006/05/31 11:28:31 spyromus Exp $
//

package com.salas.bb.utils.net;

import com.salas.bb.utils.i18n.Strings;

import java.io.IOException;
import java.util.List;

/**
 * This exception is thrown by <code>ResumingSupport</code> object when cyclic redirection
 * is detected.
 */
public class CyclicRedirectionException extends IOException
{
    private String message;

    /**
     * Creates cyclic redirection exception.
     *
     * @param aVisitedURLsList list of redirection sequence.
     */
    public CyclicRedirectionException(List aVisitedURLsList)
    {
        StringBuffer buf = new StringBuffer(Strings.message("net.cyclic.redirection"));

        for (int i = 0; i < aVisitedURLsList.size(); i++)
        {
            String urlString = (String)aVisitedURLsList.get(i);
            buf.append("  ").append(urlString).append("\n");
        }

        message = buf.toString();
    }

    /**
     * Returns the detail message string of this throwable.
     *
     * @return the detail message string of this <tt>Throwable</tt> instance (which may be <tt>null</tt>).
     */
    public String getMessage()
    {
        return message;
    }
}
