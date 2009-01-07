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
// $Id: ChangesTableModel.java,v 1.2 2006/01/08 04:57:14 kyank Exp $
//

package com.salas.bb.updates.ui;

import com.salas.bb.updates.VersionChange;

import javax.swing.table.DefaultTableModel;

/**
 * Table model for displaying list of changes in the table.
 */
public class ChangesTableModel extends DefaultTableModel
{
    private static final Class[] CLASSES = new Class[] { Integer.class, String.class };

    private VersionChange[] changes;

    /**
     * Creates model.
     *
     * @param aChanges changes to display.
     */
    public ChangesTableModel(VersionChange[] aChanges)
    {
        changes = aChanges;
    }

    /**
     * Returns the number of rows in this data table.
     *
     * @return the number of rows in the model
     */
    public int getRowCount()
    {
        return changes == null ? 0 : changes.length;
    }

    /**
     * Returns the number of columns in this data table.
     *
     * @return the number of columns in the model
     */
    public int getColumnCount()
    {
        return 2;
    }

    /**
     * Returns the column name.
     *
     * @return a name for this column using the string value of the appropriate member in
     *         <code>columnIdentifiers</code>. If <code>columnIdentifiers</code> does not have an
     *         entry for this index, returns the default name provided by the superclass
     */
    public String getColumnName(int column)
    {
        return "";
    }

    /**
     * Returns <code>Object.class</code> regardless of <code>columnIndex</code>.
     *
     * @param columnIndex the column being queried
     *
     * @return the Object.class
     */
    public Class getColumnClass(int columnIndex)
    {
        return CLASSES[columnIndex];
    }

    /**
     * Returns an attribute value for the cell at <code>row</code> and <code>column</code>.
     *
     * @param row    the row whose value is to be queried
     * @param column the column whose value is to be queried
     *
     * @return the value Object at the specified cell
     *
     * @throws ArrayIndexOutOfBoundsException if an invalid row or column was given
     */
    public Object getValueAt(int row, int column)
    {
        VersionChange change = changes[row];
        Object value;

        switch (column)
        {
            case 0:
                value = new Integer(change.getType());
                break;
            case 1:
                value = change.getDetails();
                break;
            default:
                value = null;
        }

        return value;
    }

    /**
     * Returns true regardless of parameter values.
     *
     * @param row    the row whose value is to be queried
     * @param column the column whose value is to be queried
     *
     * @return true
     *
     * @see #setValueAt
     */
    public boolean isCellEditable(int row, int column)
    {
        return false;
    }
}
