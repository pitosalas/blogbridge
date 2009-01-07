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
// $Id: TableMap.java,v 1.5 2006/01/08 05:05:22 kyank Exp $
//

package com.salas.bb.utils.uif.table;

import javax.swing.table.*;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

/**
 * Intermediary between table and model.
 */
public class TableMap extends AbstractTableModel implements TableModelListener
{
    protected TableModel model;

    /**
     * Returns mapped model.
     *
     * @return model.
     */
    public TableModel getModel()
    {
        return model;
    }

    /**
     * Sets table model for mapping.
     *
     * @param aModel model.
     */
    public void setModel(TableModel aModel)
    {
        if (model != null) model.removeTableModelListener(this);
        model = aModel;
        if (model != null) model.addTableModelListener(this);
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and <code>rowIndex</code>.
     *
     * @param rowIndex      the row whose value is to be queried.
     * @param columnIndex   the column whose value is to be queried.
     *
     * @return the value Object at the specified cell.
     */
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        return model.getValueAt(rowIndex, columnIndex);
    }

    /**
     * This empty implementation is provided so users don't have to implement this method if their
     * data model is not editable.
     *
     * @param aValue      value to assign to cell
     * @param rowIndex    row of cell
     * @param columnIndex column of cell
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        model.setValueAt(aValue, rowIndex, columnIndex);
    }

    /**
     * Returns the number of rows in the model. A <code>JTable</code> uses this method to determine
     * how many rows it should display.  This method should be quick, as it is called frequently
     * during rendering.
     *
     * @return the number of rows in the model
     *
     * @see #getColumnCount
     */
    public int getRowCount()
    {
        return (model == null) ? 0 : model.getRowCount();
    }

    /**
     * Returns the number of columns in the model. A <code>JTable</code> uses this method to
     * determine how many columns it should create and display by default.
     *
     * @return the number of columns in the model
     *
     * @see #getRowCount
     */
    public int getColumnCount()
    {
        return (model == null) ? 0 : model.getColumnCount();
    }

    /**
     * Returns a default name for the column using spreadsheet conventions: A, B, C, ... Z, AA, AB,
     * etc.  If <code>column</code> cannot be found, returns an empty string.
     *
     * @param column the column being queried
     *
     * @return a string containing the default name of <code>column</code>
     */
    public String getColumnName(int column)
    {
        return model.getColumnName(column);
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
        return model.getColumnClass(columnIndex);
    }

    /**
     * Returns false.  This is the default implementation for all cells.
     *
     * @param rowIndex    the row being queried
     * @param columnIndex the column being queried
     *
     * @return false
     */
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return model.isCellEditable(rowIndex, columnIndex);
    }

    /**
     * This fine grain notification tells listeners the exact range of cells, rows, or columns that
     * changed.
     *
     * @param e event object.
     */
    public void tableChanged(TableModelEvent e)
    {
        fireTableChanged(e);
    }
}
