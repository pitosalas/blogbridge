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
// $Id: SearchPopupMenu.java,v 1.5 2007/06/07 09:37:39 spyromus Exp $
//

package com.salas.bb.search;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Search popup menu.
 */
public class SearchPopupMenu extends JPopupMenu
{
    private final MenuItemListener menuItemListener = new MenuItemListener();
    private final ListController listController = new ListController();

    private ResultItem selectedItem;

    /**
     * Creates search popup.
     */
    public SearchPopupMenu()
    {
    }

    /**
     * Adds item to the list at a proper position.
     *
     * @param item item.
     */
    public void add(ResultItem item)
    {
        if (item == null) return;

        listController.add(item);
    }

    /**
     * Removes all the components from this container.
     */
    public void removeAll()
    {
        listController.removeAll();
    }

    /**
     * Removes all the components from this container.
     */
    private void removeAllItems()
    {
        int cnt = getComponentCount();
        for (int i = 0; i < cnt; i++)
        {
            Component component = getComponent(i);
            if (component instanceof SearchPopupMenuItem)
            {
                ((SearchPopupMenuItem)component).removeActionListener(menuItemListener);
            }
        }

        super.removeAll();
    }

    /**
     * Adds separator item to the menu.
     *
     * @param index index.
     */
    private void addSeparator(int index)
    {
        add(new JPopupMenu.Separator(), index);
    }

    /**
     * Adds menu item to a given position.
     *
     * @param menuItem  item.
     * @param index     index.
     */
    private void addMenuItem(SearchPopupMenuItem menuItem, int index)
    {
        menuItem.addActionListener(menuItemListener);
        add(menuItem, index);
        pack();
    }

    /**
     * Returns currently selected item.
     *
     * @return item.
     */
    public ResultItem getSelectedItem()
    {
        return selectedItem;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Invoked on item selection.
     *
     * @param item selected item.
     */
    private void onItemSelected(ResultItem item)
    {
        selectedItem = item;
        firePropertyChange("selectedItem", null, item);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Listens to item selections.
     */
    private class MenuItemListener implements ActionListener
    {
        /**
         * Invoked when search results item is selected.
         *
         * @param e event.
         */
        public void actionPerformed(ActionEvent e)
        {
            SearchPopupMenuItem menuItem = (SearchPopupMenuItem)e.getSource();
            onItemSelected(menuItem.getItem());
        }
    }

    /**
     * Controlls the position of items, creation and removal of groups etc.
     */
    class ListController
    {
        private ArrayList[] groups = new ArrayList[ResultItemType.COUNT];
        private int[] groupSizes = new int[ResultItemType.COUNT];

        public ListController()
        {
            for (int i = 0; i < groups.length; i++) groups[i] = new ArrayList();
        }

        /**
         * Adds an item to the list.
         *
         * @param item item.
         */
        public void add(ResultItem item)
        {
            ResultItemType type = item.getType();
            int typeOrder = type.getOrder();

            // Locate the list of items in group
            ArrayList group = groups[typeOrder];
            boolean first = group.size() == 0;

            SearchPopupMenuItem menuItem = new SearchPopupMenuItem(item);
            menuItem.setFirst(first);

            group.add(menuItem);
            groupSizes[typeOrder]++;

            if (first && !isFirstVisibleGroup(typeOrder))
            {
                int index = -1;
                for (int g = 0; g < typeOrder; g++)
                {
                    int size = groupSizes[g];
                    if (size > 0) index += size + 1;
                }

                // Add separator
                addSeparator(index);
            }

            int index = -2;
            for (int g = 0; g <= typeOrder; g++)
            {
                int size = groupSizes[g];
                if (size > 0) index += size + 1;
            }

            addMenuItem(menuItem, index);
        }

        /**
         * Returns <code>TRUE</code> if this is the first group which is currently
         * visible.
         *
         * @param group group.
         *
         * @return <code>TRUE</code> if currently first.
         */
        private boolean isFirstVisibleGroup(int group)
        {
            boolean first = true;

            for (int i = group - 1; first && i >= 0; i--)
            {
                first = groupSizes[i] == 0;
            }

            return first;
        }

        /**
         * Removes all items and separators.
         */
        public void removeAll()
        {
            for (int i = 0; i < groups.length; i++)
            {
                groups[i].clear();
                groupSizes[i] = 0;
            }

            removeAllItems();
        }
    }
}
