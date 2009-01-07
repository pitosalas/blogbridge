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
// $Id: GroupLayoutManager.java,v 1.5 2007/08/01 11:36:34 spyromus Exp $
//

package com.salas.bb.views.feeds.image;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Group layout manager. Layouts components in groups with group dividers. Group dividers
 * are also components, which are added to the container (and this layout) with the corresponding
 * constraint {@link GroupLayoutManager#DIVIDER}.
 */
public class GroupLayoutManager implements LayoutManager
{
    /** Divider component string used to identify divider components. */
    public static final String DIVIDER = "divider";

    /** Default left, right, top and bottom gap. */
    public static final int DEFAULT_GAP = 5;

    private final Set<Component> dividers;

    private int gapH;
    private int gapV;

    /**
     * Creates manager.
     */
    public GroupLayoutManager()
    {
        dividers = new HashSet<Component>();

        gapH = DEFAULT_GAP;
        gapV = DEFAULT_GAP;
    }

    /**
     * If the layout manager uses a per-component string,
     * adds the component <code>comp</code> to the layout,
     * associating it with the string specified by <code>name</code>.
     *
     * @param name the string to be associated with the component
     * @param comp the component to be added
     */
    public void addLayoutComponent(String name, Component comp)
    {
        if (DIVIDER.equals(name)) dividers.add(comp);
    }

    /**
     * Removes the specified component from the layout.
     *
     * @param comp the component to be removed
     */
    public void removeLayoutComponent(Component comp)
    {
        dividers.remove(comp);
    }

    /**
     * Calculates the minimum size dimensions for the specified
     * container, given the components it contains.
     *
     * @param parent the component to be laid out
     * @see #preferredLayoutSize
     */
    public Dimension minimumLayoutSize(Container parent)
    {
        return preferredLayoutSize(parent);
    }

    /**
     * Calculates the preferred size dimensions for the specified
     * container, given the components it contains.
     *
     * @param parent the container to be laid out
     * @see #minimumLayoutSize
     */
    public Dimension preferredLayoutSize(Container parent)
    {
        // Find first item and get its width
        int itemWidth = evalItemWidth(parent);

        // Find the number of columns we can fit
        int cols = evalAvailableColumns(parent.getBounds().width, itemWidth, gapH);

        Dimension dim = new Dimension(0, 0);

        int itemsInRow = 0;
        int count = parent.getComponentCount();

        int rowHeight = 0;
        int rowWidth = 0;
        boolean firstDiv = true;

        for (int i = 0; i < count; i++)
        {
            Component c = parent.getComponent(i);
            if (!c.isVisible()) continue;

            Dimension pref = c.getPreferredSize();
            int height = pref.height;
            int width = pref.width;

            if (isDivider(c))
            {
                // Add height and find the max between preferred div size and what we already have
                dim.height += height + (firstDiv ? 0 : gapV);
                dim.width = Math.max(dim.width, width);

                // Reset the flag to let the layout add v-gaps on every next div
                firstDiv = false;

                // Reset the items in row counter and the row height / width when starting new group
                itemsInRow = 0;
                rowHeight = 0;
                rowWidth = 0;
            } else
            {
                itemsInRow++;

                // Add h-gap for second and following items
                if (itemsInRow != 1) rowWidth += gapH;

                // Add item width and update the global width
                rowWidth += width;
                dim.width = Math.max(dim.width, rowWidth);

                // If current row height isn't enough to contain the item, increase it
                if (rowHeight < height)
                {
                    // Add a vertical gap on first item in a row
                    if (itemsInRow == 1) dim.height += gapV;

                    dim.height += height - rowHeight;
                    rowHeight = height;
                }

                // Reset the items in row counter and the row height / width when starting new row
                if (itemsInRow == cols)
                {
                    itemsInRow = 0;
                    rowHeight = 0;
                    rowWidth = 0;
                }
            }
        }
        
        return dim;
    }

    /**
     * Returns the width of the first non-divider component.
     *
     * @param container container.
     *
     * @return width or <code>0</code> if no components present.
     */
    private int evalItemWidth(Container container)
    {
        int count = container.getComponentCount();
        int width = 0;

        for (int i = 0; width == 0 && i < count; i++)
        {
            Component comp = container.getComponent(i);
            if (!isDivider(comp)) width = comp.getPreferredSize().width;
        }

        return width;
    }

    /**
     * Lays out the specified container.
     *
     * @param parent the container to be laid out
     */
    public void layoutContainer(Container parent)
    {
        int contWidth = parent.getSize().width;

        // Find first item and get its width
        int itemWidth = evalItemWidth(parent);

        // Find the number of columns we can fit
        int cols = evalAvailableColumns(contWidth, itemWidth, gapH);

        Rectangle bounds = new Rectangle();
        boolean firstDiv = true;
        int y = 0;
        int col = 0;
        int rowHeight = 0;

        int count = parent.getComponentCount();
        for (int i = 0; i < count; i++)
        {
            Component component = parent.getComponent(i);
            if (!component.isVisible()) continue;

            if (isDivider(component))
            {
                // If there's a row height not added from the last unfinished
                // row of items, add it
                y += rowHeight;

                // Shift second and further dividers a bit with the v-gap
                if (!firstDiv) y += gapV;

                Dimension size = component.getPreferredSize();
                bounds.x = 0;
                bounds.y = y;
                bounds.width = contWidth;
                bounds.height = size.height;

                y += size.height;

                col = 0;
                firstDiv = false;
                rowHeight = 0;
            } else
            {
                if (col == 0) y += gapV;
                
                Dimension size = component.getPreferredSize();
                bounds.x = col * (itemWidth + gapH);
                bounds.y = y;
                bounds.width = itemWidth;
                bounds.height = size.height;

                // Update the maximum row height if necessary
                rowHeight = Math.max(rowHeight, size.height);
                
                col++;

                // When reached the end of the row, update the y-coord and properties
                if (col == cols)
                {
                    y += rowHeight;

                    rowHeight = 0;
                    col = 0;
                }
            }

            // Set the bounds
            component.setBounds(bounds);
        }
    }

    /**
     * Returns <code>TRUE</code> if given component is marked as divider.
     *
     * @param component component.
     *
     * @return <code>TRUE</code> if given component is marked as divider.
     */
    private boolean isDivider(Component component)
    {
        return dividers.contains(component);
    }

    /**
     * Calculates maximum item dimension. Dividers do not count.
     *
     * @param components    components.
     * @param dividers      map of dividers.
     *
     * @return dimension.
     */
    static Dimension evalItemDimension(Component[] components, Set<Component> dividers)
    {
        Dimension dim = new Dimension(0, 0);

        for (Component component : components)
        {
            if (component.isVisible() && !dividers.contains(component))
            {
                Dimension cdim = component.getPreferredSize();
                dim.width = Math.max(dim.width, cdim.width);
                dim.height = Math.max(dim.height, cdim.height);
            }
        }

        return dim;
    }

    /**
     * Evaluates maximum number of columns ready to be allocated in a given width.
     *
     * @param width     width of a container.
     * @param itemWidth width of a single item.
     * @param gapH      gap between items.
     *
     * @return number of columns (greater or equal to 1).
     */
    static int evalAvailableColumns(int width, int itemWidth, int gapH)
    {
        width -= gapH;
        itemWidth += gapH;

        int cols = width / itemWidth;
        if (cols < 1) cols = 1;

        return cols;
    }
}
