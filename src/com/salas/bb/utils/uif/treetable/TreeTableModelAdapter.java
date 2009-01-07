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
// $Id: TreeTableModelAdapter.java,v 1.5 2006/05/29 12:50:07 spyromus Exp $
//

package com.salas.bb.utils.uif.treetable;

import com.salas.bb.utils.i18n.Strings;

import javax.swing.table.AbstractTableModel;
import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import java.util.logging.Logger;
import java.text.MessageFormat;

/**
 * Adapter for tree table model.
 */
public class TreeTableModelAdapter extends AbstractTableModel implements TreeModelListener
{
    private static final Logger LOG = Logger.getLogger(TreeTableModelAdapter.class.getName());

    private JTree           tree;
    private TreeTableModel  model;

    /**
     * Creates adapater.
     *
     * @param aModel    model to adopt.
     * @param aTree     tree component model applied to.
     */
    public TreeTableModelAdapter(TreeTableModel aModel, JTree aTree)
    {
        tree = aTree;
        model = aModel;

        model.addTreeModelListener(this);

        tree.addTreeExpansionListener(new TreeExpansionListener()
        {
            public void treeExpanded(TreeExpansionEvent event)
            {
                // Don't use fireTableRowsInserted() here;
                // the selection model would get updated twice.

                fireTableDataChanged();
            }

            public void treeCollapsed(TreeExpansionEvent event)
            {
                fireTableDataChanged();
            }
        });
    }

    /**
     * Returns the number of columns in the model. A
     * <code>JTable</code> uses this method to determine how many columns it
     * should create and display by default.
     *
     * @return the number of columns in the model
     * @see #getRowCount
     */
    public int getColumnCount()
    {
        return model.getColumnCount();
    }

    /**
     * Returns a default name for the column using spreadsheet conventions:
     * A, B, C, ... Z, AA, AB, etc.  If <code>column</code> cannot be found,
     * returns an empty string.
     *
     * @param column the column being queried.
     *
     * @return a string containing the default name of <code>column</code>.
     */
    public String getColumnName(int column)
    {
        return model.getColumnName(column);
    }

    /**
     * Returns <code>Object.class</code> regardless of <code>columnIndex</code>.
     *
     * @param column the column being queried.
     *
     * @return the Object.class
     */
    public Class getColumnClass(int column)
    {
        return model.getColumnClass(column);
    }

    /**
     * Returns the number of rows in the model. A <code>JTable</code> uses this method to
     * determine how many rows it should display. This method should be quick, as it
     * is called frequently during rendering.
     *
     * @return the number of rows in the model.
     *
     * @see #getColumnCount
     */
    public int getRowCount()
    {
        return tree.getRowCount();
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and
     * <code>rowIndex</code>.
     *
     * @param row   the row whose value is to be queried.
     * @param column the column whose value is to be queried.
     *
     * @return the value Object at the specified cell.
     */
    public Object getValueAt(int row, int column)
    {
        return model.getValueAt(nodeForRow(row), column);
    }

    /**
     * Returns TRUE if the cell is editable.
     *
     * @param row       the row being queried.
     * @param column    the column being queried.
     *
     * @return TRUE if the cell is editable.
     */
    public boolean isCellEditable(int row, int column)
    {
        return model.isCellEditable(nodeForRow(row), column);
    }

    /**
     * This empty implementation is provided so users don't have to implement
     * this method if their data model is not editable.
     *
     * @param value     value to assign to cell.
     * @param row       row of cell.
     * @param column    column of cell.
     */
    public void setValueAt(Object value, int row, int column)
    {
        model.setValueAt(value, nodeForRow(row), column);
    }

    /**
     * Returns node for a given row specified by index.
     *
     * @param row row index.
     *
     * @return node.
     */
    private Object nodeForRow(int row)
    {
        TreePath treePath = tree.getPathForRow(row);

        if (treePath == null) LOG.warning(MessageFormat.format(Strings.error("no.path.for.row"),
            new Object[] { new Integer(row) }));

        return treePath == null ? null : treePath.getLastPathComponent();
    }

    /**
     * <p>Invoked after a node (or a set of siblings) has changed in some way. The node(s) have not
     * changed locations in the tree or altered their children arrays, but other attributes have
     * changed and may affect presentation. Example: the name of a file has changed, but it is in
     * the same location in the file system.</p> <p>To indicate the root has changed, childIndices
     * and children will be null. </p>
     * <p/>
     * <p>Use <code>e.getPath()</code> to get the parent of the changed node(s).
     * <code>e.getChildIndices()</code> returns the index(es) of the changed node(s).</p>
     */
    public void treeNodesChanged(TreeModelEvent e)
    {
        TreePath treePath = e.getTreePath();
        if (tree.isExpanded(treePath))
        {
            int row = tree.getRowForPath(treePath) + e.getChildIndices()[0] + 1;
            fireTableRowsUpdated(row, row);
        }
    }

    /**
     * <p>Invoked after nodes have been inserted into the tree.</p>
     * <p/>
     * <p>Use <code>e.getPath()</code> to get the parent of the new node(s).
     * <code>e.getChildIndices()</code> returns the index(es) of the new node(s) in ascending
     * order.</p>
     */
    public void treeNodesInserted(TreeModelEvent e)
    {
        TreePath treePath = e.getTreePath();
        if (tree.isExpanded(treePath))
        {
            int row = tree.getRowForPath(treePath) + e.getChildIndices()[0];
            fireTableRowsInserted(row, row);
        }
    }

    /**
     * <p>Invoked after nodes have been removed from the tree.  Note that if a subtree is removed
     * from the tree, this method may only be invoked once for the root of the removed subtree, not
     * once for each individual set of siblings removed.</p>
     * <p/>
     * <p>Use <code>e.getPath()</code> to get the former parent of the deleted node(s).
     * <code>e.getChildIndices()</code> returns, in ascending order, the index(es) the node(s) had
     * before being deleted.</p>
     */
    public void treeNodesRemoved(TreeModelEvent e)
    {
        TreePath treePath = e.getTreePath();
        if (tree.isExpanded(treePath))
        {
            int row = tree.getRowForPath(treePath) + e.getChildIndices()[0];
            fireTableRowsDeleted(row, row);
        }
    }

    /**
     * <p>Invoked after the tree has drastically changed structure from a given node down.  If the
     * path returned by e.getPath() is of length one and the first element does not identify the
     * current root node the first element should become the new root of the tree.<p>
     * <p/>
     * <p>Use <code>e.getPath()</code> to get the path to the node. <code>e.getChildIndices()</code>
     * returns null.</p>
     */
    public void treeStructureChanged(TreeModelEvent e)
    {
    }
}
