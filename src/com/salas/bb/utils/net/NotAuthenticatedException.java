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
// $Id: NotAuthenticatedException.java,v 1.3 2006/05/31 11:28:31 spyromus Exp $
//

package com.salas.bb.utils.net;

import com.salas.bb.utils.i18n.Strings;

import java.io.IOException;

/**
 * Exception is thrown when user hasn't authenticated to connect to some resource.
 */
public class NotAuthenticatedException extends IOException
{
    /** Constructs object */
    public NotAuthenticatedException()
    {
        super(Strings.message("net.not.authenticated"));
    }
}
