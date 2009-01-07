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
// $Id $
//

package com.salas.bb.persistence.backend.migration;

import com.salas.bb.persistence.PersistenceException;
import com.salas.bb.persistence.backend.HsqlPersistenceManager;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Migration from 3.04 to 3.05.
 */
public class Schema02 extends AbstractSchema
{
    private static final Logger LOG = Logger.getLogger(Schema02.class.getName());

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
        Object[][] rows = selectRows(con, "SELECT TITLE, FEEDKEY FROM DELETEDFEEDS DF, GUIDES G WHERE DF.GUIDEID = G.ID");

        update(con, "DELETE FROM DELETEDFEEDS");
        dropConstraint(con, "DELETEDFEEDS", "FK_DELETEDFEEDS_GUIDE");
        dropColumn(con, "DELETEDFEEDS", "GUIDEID");
        addColumn(con, "DELETEDFEEDS", "GUIDETITLE VARCHAR(255) NOT NULL");
        renameColumn(con, "DELETEDFEEDS", "FEEDKEY", "OBJECTKEY");
        renameTable(con, "DELETEDFEEDS", "DELETEDOBJECTS");

        // Add deleted feed records back
        for (int i = 0; i < rows.length; i++)
        {
            Object[] row = rows[i];
            try
            {
                pm.addDeletedObjectRecord((String)row[0], (String)row[1]);
            } catch (PersistenceException e)
            {
                LOG.log(Level.WARNING, "Failed to re-enter the deleted feed record.", e);
            }
        }
    }

}
