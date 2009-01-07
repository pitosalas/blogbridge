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
// $Id: TreeTableModel.java,v 1.2 2006/01/08 05:05:23 kyank Exp $
//

package com.salas.bb.utils.uif.treetable;

import javax.swing.tree.TreeModel;

/**
 * Tree table model.
 */
public interface TreeTableModel extends TreeModel
{
    /**
     * Returns the number of availible columns.
     *
     * @return number of columns.
     */
    public int getColumnCount();

    /**
     * Returns the name for column number <code>column</code>.
     *
     * @param column column index.
     *
     * @return name of column.
     */
    public String getColumnName(int column);

    /**
     * Returns the type for column number <code>column</code>.
     *
     * @param column column index.
     *
     * @return class.
     */
    public Class getColumnClass(int column);

    /**
     * Returns the value to be displayed for node <code>node</code>,
     * at column number <code>column</code>.
     *
     * @param node      node to get value of.
     * @param column    column index.
     *
     * @return value.
     */
    public Object getValueAt(Object node, int column);

    /**
     * Indicates whether the the value for node <code>node</code>,
     * at column number <code>column</code> is editable.
     *
     * @param node      node to check editable state for.
     * @param column    column index.
     *
     * @return TRUE if editable.
     */
    public boolean isCellEditable(Object node, int column);

    /**
     * Sets the value for node <code>node</code>, at column number <code>column</code>.
     *
     * @param aValue        value to set for node.
     * @param node          node to set value to.
     * @param column        column index.
     */
    public void setValueAt(Object aValue, Object node, int column);
}
