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
// $Id: PersistenceManagerConfig.java,v 1.5 2006/01/08 04:57:14 kyank Exp $
//

package com.salas.bb.persistence;

import com.salas.bb.persistence.backend.HsqlPersistenceManager;
import com.salas.bb.core.ApplicationLauncher;

/**
 * Configuration of persistence manager. Application can use it to get instances
 * of manager ready to perform persistence tasks.
 */
public final class PersistenceManagerConfig
{
    /**
     * Active persistence manager. Later may be replaced with some configurable setting.
     */
    private static final IPersistenceManager manager;

    static
    {
        manager = new HsqlPersistenceManager(ApplicationLauncher.getContextPath(),
            !ApplicationLauncher.isWeekly());
    }

    /**
     * Hidden constructor of utility class.
     */
    private PersistenceManagerConfig()
    {
    }

    /**
     * Returns instance of manager ready to perform persistence tasks.
     *
     * @return manager.
     */
    public static IPersistenceManager getManager()
    {
        return manager;
    }
}
