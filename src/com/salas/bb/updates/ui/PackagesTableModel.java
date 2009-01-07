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
// $Id: PackagesTableModel.java,v 1.3 2006/01/08 04:57:14 kyank Exp $
//

package com.salas.bb.updates.ui;

import com.salas.bb.updates.Location;

import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.ArrayList;

/**
 * Model for table of available package locations.
 */
public class PackagesTableModel extends DefaultTableModel
{
    private static final Class[] CLASSES = new Class[] { Boolean.class, String.class };

    private Location[]   locations;
    private List         selectedLocations;

    /**
     * Creates model for available locations.
     */
    public PackagesTableModel(Location[] aLocations)
    {
        locations = aLocations;
        selectedLocations = new ArrayList(locations.length);
    }

    /**
     * Returns the list of currently selected package locations.
     *
     * @return the list of currently selected package locations.
     */
    public Location[] getSelectedLocations()
    {
        return (Location[])selectedLocations.toArray(new Location[0]);
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
     * Returns the number of rows in this data table.
     *
     * @return the number of rows in the model
     */
    public int getRowCount()
    {
        return locations == null ? 0 : locations.length;
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
        Location location = locations[row];
        Object value;

        switch (column)
        {
            case 0:
                value = Boolean.valueOf(selectedLocations.contains(location));
                break;
            case 1:
                value = location.getDescription();
                break;
            default:
                value = null;
        }

        return value;
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
     * Returns true if cell is editable.
     *
     * @param row    the row whose value is to be queried
     * @param column the column whose value is to be queried
     *
     * @return <code>TRUE</code> for checkmark column.
     *
     * @see #setValueAt
     */
    public boolean isCellEditable(int row, int column)
    {
        return column == 0;
    }

    /**
     * Sets the object value for the cell at <code>column</code> and <code>row</code>.
     * <code>aValue</code> is the new value.  This method will generate a <code>tableChanged</code>
     * notification.
     *
     * @param aValue the new value; this can be null
     * @param row    the row whose value is to be changed
     * @param column the column whose value is to be changed
     *
     * @throws ArrayIndexOutOfBoundsException if an invalid row or column was given
     */
    public void setValueAt(Object aValue, int row, int column)
    {
        boolean add = ((Boolean)aValue).booleanValue();
        Location location = locations[row];

        if (add)
        {
            selectPackage(location);
        } else
        {
            selectedLocations.remove(location);
        }

        fireTableCellUpdated(row, column);
    }

    /**
     * Selects package if it isn't selected yet.
     *
     * @param packageLocation package location.
     */
    public void selectPackage(Location packageLocation)
    {
        if (!selectedLocations.contains(packageLocation)) selectedLocations.add(packageLocation);
    }
}
