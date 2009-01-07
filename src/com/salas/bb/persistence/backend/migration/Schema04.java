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
// $Id: Schema04.java,v 1.3 2007/02/07 15:41:50 spyromus Exp $
//

package com.salas.bb.persistence.backend.migration;

import com.salas.bb.persistence.backend.HsqlPersistenceManager;

import java.sql.Connection;

/**
 * Migration from 4.2 to 4.5.
 */
public class Schema04 extends AbstractSchema
{
    /**
     * Migrates from some version to the other.
     *
     * @param con connection to use.
     * @param pm  persistence manager to use for data operations.
     *
     * @throws MigrationException in case of any problems with procedure.
     */
    public void perform(Connection con, HsqlPersistenceManager pm) throws MigrationException
    {
        String enabled = "DEDUP_ENABLED BIT DEFAULT '0' NOT NULL";
        String from = "DEDUP_FROM INTEGER DEFAULT '-1' NOT NULL";
        String to = "DEDUP_TO INTEGER DEFAULT '-1' NOT NULL";

        addColumn(con, "SEARCHFEEDS", enabled);
        addColumn(con, "SEARCHFEEDS", from);
        addColumn(con, "SEARCHFEEDS", to);
        addColumn(con, "QUERYFEEDS", enabled);
        addColumn(con, "QUERYFEEDS", from);
        addColumn(con, "QUERYFEEDS", to);
        addColumn(con, "DIRECTFEEDS", "SYNC_HASH INTEGER DEFAULT '0' NOT NULL");
    }
}
