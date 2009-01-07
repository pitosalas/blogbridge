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
// $Id: AbstractTreeTableModel.java,v 1.4 2006/01/08 05:05:23 kyank Exp $
//

package com.salas.bb.utils.uif.treetable;

import EDU.oswego.cs.dl.util.concurrent.CopyOnWriteArrayList;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;

/**
 * Abstract model for tree-table.
 */
public abstract class AbstractTreeTableModel implements TreeTableModel
{
    protected final List listeners = new CopyOnWriteArrayList();
    protected final Object root;

    /**
     * Creates model with given root.
     *
     * @param root root of tree.
     */
    public AbstractTreeTableModel(Object root)
    {
        this.root = root;
    }

    /**
     * Returns root of tree.
     *
     * @return root.
     */
    public Object getRoot()
    {
        return root;
    }

    /**
     * Returns TRUE if nonde is leaf.
     *
     * @param node node to check.
     *
     * @return TRUE if node is leaf.
     */
    public boolean isLeaf(Object node)
    {
        return getChildCount(node) == 0;
    }

    /**
     * Messaged when the user has altered the value for the item identified
     * by <code>path</code> to <code>newValue</code>.
     * If <code>newValue</code> signifies a truly new value
     * the model should post a <code>treeNodesChanged</code> event.
     *
     * @param path     path to the node that the user has altered
     * @param newValue the new value from the TreeCellEditor
     */
    public void valueForPathChanged(TreePath path, Object newValue)
    {
    }

    /**
     * Returns the index of child in parent.  If <code>parent</code>
     * is <code>null</code> or <code>child</code> is <code>null</code>,
     * returns -1.
     *
     * @param parent a note in the tree, obtained from this data source
     * @param child  the node we are interested in
     * @return the index of the child in the parent, or -1 if either
     *         <code>child</code> or <code>parent</code> are <code>null</code>
     */
    public int getIndexOfChild(Object parent, Object child)
    {
        for (int i = 0; i < getChildCount(parent); i++)
        {
            if (getChild(parent, i).equals(child))
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Adds a listener for the <code>TreeModelEvent</code> posted after the tree changes.
     *
     * @param l the listener to add
     * @see #removeTreeModelListener
     */
    public void addTreeModelListener(TreeModelListener l)
    {
        listeners.add(l);
    }

    /**
     * Removes a listener previously added with <code>addTreeModelListener</code>.
     *
     * @param l the listener to remove
     * @see #addTreeModelListener
     */
    public void removeTreeModelListener(TreeModelListener l)
    {
        listeners.remove(l);
    }

    /**
     * Notifies listeners when nodes change.
     *
     * @param source        source of event.
     * @param path          path from root to the containing node.
     * @param childIndices  indicies of changed children.
     * @param children      changed children.
     */
    protected void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices,
                                        Object[] children)
    {
        TreeModelEvent e = new TreeModelEvent(source, path, childIndices, children);
        Iterator iterator = listeners.iterator();
        while (iterator.hasNext())
        {
            TreeModelListener listener = (TreeModelListener)iterator.next();
            listener.treeNodesChanged(e);
        }
    }

    /**
     * Notifies listeners when nodes are inserted.
     *
     * @param source        source of event.
     * @param path          path from root to the containing node.
     * @param childIndices  indicies of inserted children.
     * @param children      inserted children.
     */
    protected void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices,
                                         Object[] children)
    {
        TreeModelEvent e = new TreeModelEvent(source, path, childIndices, children);;
        Iterator iterator = listeners.iterator();
        while (iterator.hasNext())
        {
            TreeModelListener listener = (TreeModelListener)iterator.next();
            listener.treeNodesInserted(e);
        }
    }

    /**
     * Notifies listeners when nodes are removed.
     *
     * @param source        source of event.
     * @param path          path from root to the containing node.
     * @param childIndices  indicies of removed children.
     * @param children      removed children.
     */
    protected void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices,
                                        Object[] children)
    {
        TreeModelEvent e = new TreeModelEvent(source, path, childIndices, children);;
        Iterator iterator = listeners.iterator();
        while (iterator.hasNext())
        {
            TreeModelListener listener = (TreeModelListener)iterator.next();
            listener.treeNodesRemoved(e);
        }
    }

    /**
     * Notifies listeners when the structure changes..
     *
     * @param source        source of event.
     * @param path          path from root to the containing node.
     * @param childIndices  indicies of changed children.
     * @param children      changed children.
     */
    protected void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices,
                                            Object[] children)
    {
        TreeModelEvent e = new TreeModelEvent(source, path, childIndices, children);;
        Iterator iterator = listeners.iterator();
        while (iterator.hasNext())
        {
            TreeModelListener listener = (TreeModelListener)iterator.next();
            listener.treeStructureChanged(e);
        }
    }

    /**
     * Returns the type for column number <code>column</code>.
     *
     * @return class.
     */
    public Class getColumnClass(int column)
    {
        return Object.class;
    }

    /**
     * By default, make the column with the Tree in it the only editable one.
     * Making this column editable causes the JTable to forward mouse
     * and keyboard events in the Tree column to the underlying JTree.
     */
    public boolean isCellEditable(Object node, int column)
    {
        return getColumnClass(column) == TreeTableModel.class;
    }

    /**
     * Sets the value for node <code>node</code>, at column number <code>column</code>.
     *
     * @param aValue        new value.
     * @param node          node to assign value to.
     * @param column        column to assign value at.
     */
    public void setValueAt(Object aValue, Object node, int column)
    {
    }
}

