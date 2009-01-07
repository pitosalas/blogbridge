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
// $Id: Schema08.java,v 1.1 2007/09/17 12:14:11 spyromus Exp $
//

package com.salas.bb.persistence.backend.migration;

import com.salas.bb.persistence.backend.HsqlPersistenceManager;

import java.sql.Connection;

/**
 * (5.13) Adds four tables for statistics phase 2.
 */
public class Schema08 extends AbstractSchema
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
        addTable(con, "CREATE CACHED TABLE FEEDREADSTATS(" +
            "ID INTEGER NOT NULL, " +
            "TS BIGINT NOT NULL, " +
            "CNT BIGINT DEFAULT '0' NOT NULL, " +
            "CONSTRAINT FK_FEEDREADSTATS_FEEDS " +
                "FOREIGN KEY(ID) REFERENCES FEEDS(ID) ON DELETE CASCADE);");

        addTable(con, "CREATE CACHED TABLE FEEDPINSTATS(" +
            "ID INTEGER NOT NULL, " +
            "TS BIGINT NOT NULL, " +
            "CNT BIGINT DEFAULT '0' NOT NULL, " +
            "CONSTRAINT FK_FEEDPINSTATS_FEEDS " +
                "FOREIGN KEY(ID) REFERENCES FEEDS(ID) ON DELETE CASCADE);");

        addTable(con, "CREATE CACHED TABLE GUIDEREADSTATS(" +
            "ID INTEGER NOT NULL, " +
            "TS BIGINT NOT NULL, " +
            "CNT BIGINT DEFAULT '0' NOT NULL, " +
            "CONSTRAINT FK_GUIDEREADSTATS_GUIDES " +
                "FOREIGN KEY(ID) REFERENCES GUIDES(ID) ON DELETE CASCADE);");

        addTable(con, "CREATE CACHED TABLE GUIDEPINSTATS(" +
            "ID INTEGER NOT NULL, " +
            "TS BIGINT NOT NULL, " +
            "CNT BIGINT DEFAULT '0' NOT NULL, " +
            "CONSTRAINT FK_GUIDEPINSTATS_GUIDES " +
                "FOREIGN KEY(ID) REFERENCES GUIDES(ID) ON DELETE CASCADE);");
    }
}