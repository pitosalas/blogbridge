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
// $Id: JTreeTable.java,v 1.4 2006/01/08 05:05:23 kyank Exp $
//

package com.salas.bb.utils.uif.treetable;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * Tree-table component.
 */
public class JTreeTable extends JTable
{
    private TreeTableCellRenderer treeRenderer;

    /**
     * Creates tree with given model.
     *
     * @param model model of tree.
     */
    public JTreeTable(TreeTableModel model)
    {
        super();

        treeRenderer = new TreeTableCellRenderer(model);
        super.setModel(new TreeTableModelAdapter(model, treeRenderer));

        // Share the selection models.
        treeRenderer.setSelectionModel(new DefaultTreeSelectionModel()
        {
            {
                setSelectionModel(listSelectionModel);
            }
        });

        // Make the tree and table row heights the same.
        treeRenderer.setRowHeight(getRowHeight());

        setDefaultRenderer(TreeTableModel.class, treeRenderer);
        setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());

        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
    }

    /**
     * Sets the font for this component.
     *
     * @param font the desired <code>Font</code> for this component
     */
    public void setFont(Font font)
    {
        super.setFont(font);
        if (treeRenderer != null) treeRenderer.setFont(font);
    }

    /**
     * Indicates whether root node of the tree should be visible or no.
     *
     * @param vis TRUE to be visible.
     */
    public void setRootVisible(boolean vis)
    {
        treeRenderer.setRootVisible(vis);
    }

    /**
     * Sets the value of the showsRootHandles property, which specifies whether the node handles
     * should be displayed.
     *
     * @param show TRUE to show.
     */
    public void setShowsRootHandles(boolean show)
    {
        treeRenderer.setShowsRootHandles(show);
    }

    /**
     * Registers new cell renderer for tree cells.
     *
     * @param renderer renderer.
     */
    public void setTreeCellRenderer(TreeCellRenderer renderer)
    {
        treeRenderer.setCellRenderer(renderer);
    }

    /**
     * Returns the tree renderer object.
     *
     * @return tree renderer.
     */
    protected JTree getTreeRenderer()
    {
        return treeRenderer;
    }

    /**
     * Returns the renderer of the cells occupied by tree in the table.
     *
     * @return renderer of the tree cells in table.
     */
    protected TableCellRenderer getTreeTableCellRenderer()
    {
        return treeRenderer;
    }

    /**
     * Returns the index of the row that contains the cell currently
     * being edited.  If nothing is being edited, returns -1.
     *
     * @return the index of the row that contains the cell currently
     *         being edited; returns -1 if nothing being edited
     * @see #editingColumn
     */
    public int getEditingRow()
    {
        // Workaround for BasicTableUI anomaly. Make sure the UI never tries to
        // paint the editor. The UI currently uses different techniques to
        // paint the renderers and editors and overriding setBounds() below
        // is not the right thing to do for an editor. Returning -1 for the
        // editing row in this case, ensures the editor is never painted.

        return (getColumnClass(editingColumn) == TreeTableModel.class) ? -1 : editingRow;
    }

    /** Renderer for tree column. */
    private class TreeTableCellRenderer extends JTree implements TableCellRenderer
    {
        protected int visibleRow;

        public TreeTableCellRenderer(TreeModel model)
        {
            super(model);
        }

        public void setBounds(int x, int y, int w, int h)
        {
            super.setBounds(x, 0, w, JTreeTable.this.getHeight());
        }

        public void paint(Graphics g)
        {
            g.translate(0, -visibleRow * getRowHeight());
            super.paint(g);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column)
        {
            Color back = isSelected
                ? table.getSelectionBackground()
                : table.getBackground();

            setBackground(back);
            visibleRow = row;

            return this;
        }
    }

    /** Simple cell editor for tree column. */
    private class TreeTableCellEditor extends AbstractCellEditor implements TableCellEditor
    {
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int r, int c)
        {
            return treeRenderer;
        }

        public Object getCellEditorValue()
        {
            return null;
        }
    }
}

