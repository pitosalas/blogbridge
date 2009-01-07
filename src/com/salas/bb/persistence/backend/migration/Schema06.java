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
// $Id: Schema06.java,v 1.1 2007/05/01 10:52:02 spyromus Exp $
//

package com.salas.bb.persistence.backend.migration;

import com.salas.bb.persistence.backend.HsqlPersistenceManager;

import java.sql.Connection;

/**
 * Migration from 5.3 to 5.4.
 */
public class Schema06 extends AbstractSchema
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
        addColumn(con, "FEEDSPROPERTIES", "ASA BIT DEFAULT '0' NOT NULL");
        addColumn(con, "FEEDSPROPERTIES", "ASA_FOLDER VARCHAR(255)");
        addColumn(con, "FEEDSPROPERTIES", "ASA_NAMEFORMAT VARCHAR(255)");
        addColumn(con, "FEEDSPROPERTIES", "ASE BIT DEFAULT '0' NOT NULL");
        addColumn(con, "FEEDSPROPERTIES", "ASE_FOLDER VARCHAR(255)");
        addColumn(con, "FEEDSPROPERTIES", "ASE_NAMEFORMAT VARCHAR(255)");
    }
}