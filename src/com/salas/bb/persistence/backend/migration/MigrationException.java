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
// $Id: MigrationException.java,v 1.1 2006/07/28 10:39:54 spyromus Exp $
//

package com.salas.bb.persistence.backend.migration;

/**
 * Exception is thrown by migration procedures if something goes wrong.
 */
public class MigrationException extends Exception
{
    /**
     * Creates exception with message and cause of problem recorded.
     *
     * @param message   message describing the problem.
     * @param cause     original cause of the problem.
     */
    public MigrationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
