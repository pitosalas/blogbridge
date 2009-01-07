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
// $Id: Schema07.java,v 1.1 2007/09/05 12:35:48 spyromus Exp $
//

package com.salas.bb.persistence.backend.migration;

import com.salas.bb.persistence.backend.HsqlPersistenceManager;

import java.sql.Connection;

/**
 * Migration from 5.12 to 5.13.
 */
public class Schema07 extends AbstractSchema
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
        addTable(con, "CREATE CACHED TABLE GUIDESTATS(" +
            "GUIDEID INTEGER NOT NULL PRIMARY KEY, " +
            "COUNT_TOTAL BIGINT DEFAULT '0' NOT NULL, " +
            "COUNT_RESET BIGINT DEFAULT '0', " +
            "INIT_TIME BIGINT, " +
            "RESET_TIME BIGINT, " +
            "CONSTRAINT FK_GUIDESTATS_GUIDES " +
                "FOREIGN KEY(GUIDEID) REFERENCES GUIDES(ID) ON DELETE CASCADE, " +
            "CONSTRAINT UN_GUIDESTATS UNIQUE(GUIDEID));");

        addTable(con, "CREATE CACHED TABLE FEEDSTATS(" +
            "FEEDID INTEGER NOT NULL PRIMARY KEY, " +
            "COUNT_TOTAL BIGINT DEFAULT '0' NOT NULL, " +
            "COUNT_RESET BIGINT DEFAULT '0', " +
            "INIT_TIME BIGINT, " +
            "RESET_TIME BIGINT, " +
            "CONSTRAINT FK_FEEDSTATS_FEEDS " +
                "FOREIGN KEY(FEEDID) REFERENCES FEEDS(ID) ON DELETE CASCADE, " +
            "CONSTRAINT UN_FEEDSTATS UNIQUE(FEEDID));");

        addTable(con, "CREATE CACHED TABLE READSTATS_HOUR(" +
            "HOUR INTEGER NOT NULL PRIMARY KEY, " +
            "COUNT_RESET BIGINT DEFAULT '0' NOT NULL, " +
            "COUNT_TOTAL BIGINT DEFAULT '0' NOT NULL);");

        addTable(con, "CREATE CACHED TABLE READSTATS_DAY(" +
            "DAY INTEGER NOT NULL PRIMARY KEY, " +
            "COUNT_RESET BIGINT DEFAULT '0' NOT NULL, " +
            "COUNT_TOTAL BIGINT DEFAULT '0' NOT NULL);");

        // Create guidestats rows
        long now = System.currentTimeMillis();
        Object[][] res = selectRows(con, "SELECT ID FROM GUIDES");
        for (Object[] row : res)
        {
            int guideId = (Integer)row[0];
            update(con, "INSERT INTO GUIDESTATS (GUIDEID, INIT_TIME, RESET_TIME) VALUES " +
                "(" + guideId + ", " + now + ", " + now +")");
        }

        // Create feedstats rows
        res = selectRows(con, "SELECT ID FROM FEEDS");
        for (Object[] row : res)
        {
            int feedId = (Integer)row[0];
            update(con, "INSERT INTO FEEDSTATS (FEEDID, INIT_TIME, RESET_TIME) VALUES " +
                "(" + feedId + ", " + now + ", " + now +")");
        }

        // Create readstats_hour rows
        for (int i = 0; i < 24; i++) update(con, "INSERT INTO READSTATS_HOUR (HOUR) VALUES (" + i + ")");

        // Create readstats_day rows
        for (int i = 0; i < 7; i++) update(con, "INSERT INTO READSTATS_DAY (DAY) VALUES (" + i + ")");

        // Set global initialization and reset times
        update(con, "INSERT INTO APP_PROPERTIES VALUES ('statsInitTime', '" + now + "')");
        update(con, "INSERT INTO APP_PROPERTIES VALUES ('statsResetTime', '" + now + "')");
    }
}