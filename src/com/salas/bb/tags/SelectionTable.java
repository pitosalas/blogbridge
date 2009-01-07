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
// $Id: SelectionTable.java,v 1.8 2006/05/31 10:39:45 spyromus Exp $
//

package com.salas.bb.tags;

import com.salas.bb.utils.uif.UifUtilities;
import com.salas.bb.utils.uif.table.TableSorter;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;

/**
 * Table for displaying tags collections.
 */
class SelectionTable extends JTable
{
    private static final int WIDTH_SELECTOR = 20;
    private static final int COL_SELECTOR   = 0;
    private static final int COL_TAG        = 1;

    private SampleTagsModel  model;
    private TableSorter      sorter;

    /**
     * Creates table.
     */
    public SelectionTable()
    {
        model = new SampleTagsModel();
        sorter = new TableSorter(model);
        setModel(sorter);
        sorter.addMouseListenerToHeaderInTable(this);

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        UifUtilities.setTableColWidth(this, 0, WIDTH_SELECTOR);

        addKeyListener(new KeyAdapter()
        {
            /**
             * Invoked when a key has been pressed.
             */
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_SPACE)
                {
                    int colSelector = COL_SELECTOR;
                    int selectedRow = getSelectedRow();
                    int selectedColumn = getSelectedColumn();
                    if (selectedRow > -COL_TAG && selectedColumn != colSelector)
                    {
                        Boolean oldValue = (Boolean)sorter.getValueAt(selectedRow, colSelector);
                        Boolean newValue = Boolean.valueOf(!oldValue.booleanValue());
                        sorter.setValueAt(newValue, selectedRow, colSelector);
                        sorter.fireTableRowsUpdated(selectedRow, selectedRow);
                    }
                }
            }
        });
    }

    /**
     * Sets the tags which should be selected in the table.
     *
     * @param availableTags available tags.
     * @param selectedTags  selected tags.
     */
    public void setTags(String[] availableTags, String[] selectedTags)
    {
        model.setAvailableTags(availableTags);
        model.setSelectedTags(selectedTags);
        sorter.sortByColumn(COL_TAG);
    }

    /**
     * Returns the list of currently selected tags.
     *
     * @return list of selected tags.
     */
    public String[] getSelectedTags()
    {
        return model.getSelectedTags();
    }

    /**
     * Model for tags selection table.
     */
    private static class SampleTagsModel extends DefaultTableModel
    {
        private static final Class[] CLASSES = new Class[] { Boolean.class, String.class };
        private static final String[] COLUMNS = {
            Strings.message("tags.suggest.table.selector"),
            Strings.message("tags.suggest.table.tag")
        };

        private final List  tags;
        private final Set   selected;

        /**
         * Creates model.
         */
        public SampleTagsModel()
        {
            super();

            tags = new ArrayList();
            selected = new HashSet();
        }

        /**
         * Returns class of the column specified by index.
         *
         * @param column    index of the column.
         *
         * @return class of the column.
         */
        public Class getColumnClass(int column)
        {
            return CLASSES[column];
        }

        /**
         * Returns <code>TRUE</code> if selector column questioned.
         *
         * @param row       row.
         * @param column    column.
         *
         * @return <code>TRUE</code> for selector column only.
         */
        public boolean isCellEditable(int row, int column)
        {
            return column == COL_SELECTOR;
        }

        /**
         * Returns number of columns.
         *
         * @return number of columns.
         */
        public int getColumnCount()
        {
            return COLUMNS.length;
        }

        /**
         * Returns number of tags currently available.
         *
         * @return number of tags rows.
         */
        public int getRowCount()
        {
            return tags == null ? 0 : tags.size();
        }

        /**
         * Returns the name of the column.
         *
         * @param column column index.
         *
         * @return name of the column.
         */
        public String getColumnName(int column)
        {
            return COLUMNS[column];
        }

        /**
         * Returns the value (state of check mark or tag name).
         *
         * @param row       row.
         * @param column    column.
         *
         * @return value.
         */
        public Object getValueAt(int row, int column)
        {
            Object value;

            if (column == COL_SELECTOR)
            {
                value = Boolean.valueOf(isSelected(getTagAt(row)));
            } else
            {
                value = getTagAt(row);
            }

            return value;
        }

        /**
         * Sets the value of check mark.
         *
         * @param aValue    <code>Boolean</code> value.
         * @param row       row.
         * @param column    column.
         */
        public void setValueAt(Object aValue, int row, int column)
        {
            if (column == COL_SELECTOR)
            {
                String tag = getTagAt(row);
                boolean selectedTag = ((Boolean)aValue).booleanValue();

                if (selectedTag) selected.add(tag); else selected.remove(tag);
            }
        }

        /**
         * Returns <code>TRUE</code> if the tag appears to be among selected tags.
         *
         * @param tag   tag to verify.
         *
         * @return <code>TRUE</code> if the tag appears to be among selected tags.
         */
        private boolean isSelected(String tag)
        {
            return selected.contains(tag);
        }

        /**
         * Returns tag at a given row.
         *
         * @param aRow row.
         *
         * @return tag.
         */
        private String getTagAt(int aRow)
        {
            return (String)tags.get(aRow);
        }

        /**
         * Adds tags, which are mentioned in the parameter, but currently missing in the list
         * to the model and selects them. If the tags aren't sepecified then the check marks
         * are simply cleared.
         *
         * @param aTags tags to add and select.
         */
        public void setSelectedTags(String[] aTags)
        {
            setAvailableTags(aTags);

            selected.clear();
            if (aTags != null) selected.addAll(Arrays.asList(aTags));

            fireTableRowsUpdated(0, getRowCount() - COL_TAG);
        }

        /**
         * Adds missing tags to the tags list.
         *
         * @param aTags tags to add.
         */
        public void setAvailableTags(String[] aTags)
        {
            if (aTags != null)
            {
                int added = 0;
                for (int i = 0; i < aTags.length; i++)
                {
                    String tag = aTags[i];
                    if (!tags.contains(tag))
                    {
                        tags.add(tag);
                        added++;
                    }
                }

                if (added > 0)
                {
                    int currentSize = tags.size();
                    fireTableRowsInserted(currentSize - added, currentSize - COL_TAG);
                }
            }
        }

        /**
         * Returns the list of currently selected tags.
         *
         * @return list of selected tags.
         */
        public String[] getSelectedTags()
        {
            return (String[])selected.toArray(new String[selected.size()]);
        }
    }
}
