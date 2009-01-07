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
// $Id: TableSorter.java,v 1.8 2006/05/29 12:48:38 spyromus Exp $
//

package com.salas.bb.utils.uif.table;

import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Vector;

/**
 * Sort-filter for any table model.
 */
public class TableSorter extends TableMap
{
    private int     indexes[];
    private Vector  sortingColumns;
    private boolean ascending;
    private int     compares;
    private int     currentColumn = -1;

    /**
     * Creates sorter for no particular model.
     */
    public TableSorter()
    {
        this(null);
    }

    /**
     * Creates sorter for given table model.
     *
     * @param model model.
     */
    public TableSorter(TableModel model)
    {
        indexes = new int[0];
        sortingColumns = new Vector();
        ascending = true;
        setModel(model);
    }

    /**
     * Sets table model for sorting.
     *
     * @param aModel model.
     */
    public void setModel(TableModel aModel)
    {
        super.setModel(aModel);
        reallocateIndexes();
    }

    private int compareRowsByColumn(int row1, int row2, int column)
    {
        Class type = model.getColumnClass(column);
        TableModel data = model;

        // Check for nulls.

        Object o1 = data.getValueAt(row1, column);
        Object o2 = data.getValueAt(row2, column);

        // If both values are null, return 0.
        if (o1 == null && o2 == null)
        {
            return 0;
        } else if (o1 == null)
        { // Define null less than everything.
            return -1;
        } else if (o2 == null)
        {
            return 1;
        }

        /*
         * We copy all returned values from the getValue call in case
         * an optimised model is reusing one object to return many
         * values.  The Number subclasses in the JDK are immutable and
         * so will not be used in this way but other subclasses of
         * Number might want to do this to save space and avoid
         * unnecessary heap allocation.
         */

        if (type.getSuperclass() == java.lang.Number.class)
        {
            Number n1 = (Number)data.getValueAt(row1, column);
            double d1 = n1.doubleValue();
            Number n2 = (Number)data.getValueAt(row2, column);
            double d2 = n2.doubleValue();

            if (d1 < d2)
            {
                return -1;
            } else if (d1 > d2)
            {
                return 1;
            } else
            {
                return 0;
            }
        } else if (type == java.util.Date.class)
        {
            Date d1 = (Date)data.getValueAt(row1, column);
            long n1 = d1.getTime();
            Date d2 = (Date)data.getValueAt(row2, column);
            long n2 = d2.getTime();

            if (n1 < n2)
            {
                return -1;
            } else if (n1 > n2)
            {
                return 1;
            } else
            {
                return 0;
            }
        } else if (type == String.class)
        {
            String s1 = (String)data.getValueAt(row1, column);
            String s2 = (String)data.getValueAt(row2, column);
            int result = s1.compareTo(s2);

            if (result < 0)
            {
                return -1;
            } else if (result > 0)
            {
                return 1;
            } else
            {
                return 0;
            }
        } else if (type == Boolean.class)
        {
            Boolean bool1 = (Boolean)data.getValueAt(row1, column);
            boolean b1 = bool1.booleanValue();
            Boolean bool2 = (Boolean)data.getValueAt(row2, column);
            boolean b2 = bool2.booleanValue();

            if (b1 == b2)
            {
                return 0;
            } else if (b1)
            { // Define false < true
                return 1;
            } else
            {
                return -1;
            }
        } else
        {
            Object v1 = data.getValueAt(row1, column);
            String s1 = v1.toString();
            Object v2 = data.getValueAt(row2, column);
            String s2 = v2.toString();
            int result = s1.compareTo(s2);

            if (result < 0)
            {
                return -1;
            } else if (result > 0)
            {
                return 1;
            } else
            {
                return 0;
            }
        }
    }

    private int compare(int row1, int row2)
    {
        compares++;
        for (int level = 0; level < sortingColumns.size(); level++)
        {
            Integer column = (Integer)sortingColumns.elementAt(level);
            int result = compareRowsByColumn(row1, row2, column.intValue());
            if (result != 0)
            {
                return ascending ? result : -result;
            }
        }
        return 0;
    }

    private void reallocateIndexes()
    {
        int rowCount = model.getRowCount();

        // Set up a new array of indexes with the right number of elements
        // for the new data model.
        indexes = new int[rowCount];

        // Initialise with the identity mapping.
        for (int row = 0; row < rowCount; row++)
        {
            indexes[row] = row;
        }
    }

    /**
     * This fine grain notification tells listeners the exact range of cells, rows, or columns that
     * changed.
     *
     * @param e event object.
     */
    public void tableChanged(TableModelEvent e)
    {
        reallocateIndexes();

        super.tableChanged(e);
    }

    private void checkModel()
    {
        if (indexes.length != model.getRowCount())
        {
            System.err.println(Strings.error("sorter.not.informed.of.change.in.model"));
        }
    }

    private void sort()
    {
        checkModel();

        compares = 0;
        shuttlesort((int[])indexes.clone(), indexes, 0, indexes.length);
    }

    // This is a home-grown implementation which we have not had time
    // to research - it may perform poorly in some circumstances. It
    // requires twice the space of an in-place algorithm and makes
    // NlogN assigments shuttling the values between the two
    // arrays. The number of compares appears to vary between N-1 and
    // NlogN depending on the initial order but the main reason for
    // using it here is that, unlike qsort, it is stable.
    private void shuttlesort(int from[], int to[], int low, int high)
    {
        if (high - low < 2)
        {
            return;
        }
        int middle = (low + high) / 2;
        shuttlesort(to, from, low, middle);
        shuttlesort(to, from, middle, high);

        int p = low;
        int q = middle;

        /* This is an optional short-cut; at each recursive call,
        check to see if the elements in this subset are already
        ordered.  If so, no further comparisons are needed; the
        sub-array can just be copied.  The array must be copied rather
        than assigned otherwise sister calls in the recursion might
        get out of sinc.  When the number of elements is three they
        are partitioned so that the first set, [low, mid), has one
        element and and the second, [mid, high), has two. We skip the
        optimisation when the number of elements is three or less as
        the first compare in the normal merge will produce the same
        sequence of steps. This optimisation seems to be worthwhile
        for partially ordered lists but some analysis is needed to
        find out how the performance drops to Nlog(N) as the initial
        order diminishes - it may drop very quickly.  */

        if (high - low >= 4 && compare(from[middle - 1], from[middle]) <= 0)
        {
            for (int i = low; i < high; i++)
            {
                to[i] = from[i];
            }
            return;
        }

        // A normal merge.

        for (int i = low; i < high; i++)
        {
            if (q >= high || (p < middle && compare(from[p], from[q]) <= 0))
            {
                to[i] = from[p++];
            } else
            {
                to[i] = from[q++];
            }
        }
    }

    /**
     * Converts row from the view to row in the data model.
     *
     * @param viewRow row in view.
     *
     * @return row in data.
     */
    public int convertToDataRow(int viewRow)
    {
        return indexes[viewRow];
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
        checkModel();
        return model.getValueAt(indexes[rowIndex], columnIndex);
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
        checkModel();
        model.setValueAt(aValue, indexes[rowIndex], columnIndex);
    }

    /**
     * Sorts table by column in ascending order.
     *
     * @param column column.
     */
    public void sortByColumn(int column)
    {
        sortByColumn(column, true);
    }

    /**
     * Sorts table by column.
     *
     * @param column    column.
     * @param asc TRUE for ascending order.
     */
    public void sortByColumn(int column, boolean asc)
    {
        sortByColumns(new int[] { column }, asc);
    }

    /**
     * Sorts the table by columns specified by indexes.
     *
     * @param columns   list of columns.
     * @param asc       TRUE for ascending order.
     */
    public void sortByColumns(int[] columns, boolean asc)
    {
        if (columns != null && columns.length > 0)
        {
            ascending = asc;
            currentColumn = columns[0];
            sortingColumns.clear();
            for (int i = 0; i < columns.length; i++)
            {
                sortingColumns.add(new Integer(columns[i]));
            }

            sort();
            super.tableChanged(new TableModelEvent(this));
        }
    }

    /**
     * Add a mouse listener to the Table to trigger a table sort
     * when a column heading is clicked in the JTable.
     *
     * @param table table to bind to.
     */
    public void addMouseListenerToHeaderInTable(JTable table)
    {
        final TableSorter sorter = this;
        final JTable tableView = table;
        tableView.setColumnSelectionAllowed(false);
        MouseAdapter listMouseListener = new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);
                if (e.getClickCount() == 1 && column != -1)
                {
                    boolean asc = true;
                    if (currentColumn == column) asc = !ascending;
                    sorter.sortByColumn(column, asc);
                }
            }
        };
        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }
}
