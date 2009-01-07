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
// $Id: SortableBox.java,v 1.5 2006/01/08 05:07:31 kyank Exp $
//

package com.salas.bb.utils.uif;

import javax.swing.*;
import java.awt.*;

/**
 * Emulation of original Box component, but with additional capability to sort components.
 */
public final class SortableBox extends Box
{
    private boolean         ascending;
    private Component[]     sorted;
    private boolean         valid;

    /**
     * Creates box with the given layout.
     *
     * @param axis axis.
     */
    public SortableBox(final int axis)
    {
        super(axis);

        sorted = new Component[0];

        setAscending(true);
    }

    /**
     * Register order of sorting. UI updated automatically.
     *
     * @param b TRUE - ascending, FALSE - descending.
     */
    public void setAscending(final boolean b)
    {
        if (ascending == b) return;

        ascending = b;
        setValid(false);
        updateSortedIfRequired();
        revalidate();
    }

    /**
     * Check if sorted list is invalid and recreate it.
     */
    private synchronized void updateSortedIfRequired()
    {
        while (!valid)
        {
            Component[] newSorted = super.getComponents();

            if (!ascending)
            {
                // reverse order - reverse original list
                Component[] components = new Component[newSorted.length];
                for (int i = 0; i < newSorted.length; i++) components[i] = newSorted[newSorted.length - i - 1];
                newSorted = components;
            }

            sorted = newSorted;

            // mark sorted map valid
            setValid(true);
        }
    }

    /**
     * Adds the specified component to this container at the specified
     * index.
     *
     * @param comp        the component to be added
     * @param constraints an object expressing layout constraints
     *                    for this component
     * @param index       the position in the container's list at which to
     *                    insert the component, where <code>-1</code>
     *                    means append to the end
     */
    protected void addImpl(final Component comp, final Object constraints, final int index)
    {
        try
        {
            super.addImpl(comp, constraints, index);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        // mark sorted map as invalid
        setValid(false);
    }

    /**
     * Removes the component, specified by <code>index</code>,
     * from this container.
     *
     * @param index the index of the component to be removed.
     *
     * @see #add
     * @since JDK1.1
     */
    public void remove(final int index)
    {
        super.remove(index);
        setValid(false);
    }

    /**
     * Removes all the components from this container.
     *
     * @see #add
     * @see #remove
     */
    public void removeAll()
    {
        super.removeAll();
        setValid(false);
    }

    /**
     * Marks the list of sorted components as valid/invalid.
     *
     * @param val <code>TRUE</code> to mark as valid.
     */
    private synchronized void setValid(boolean val)
    {
        this.valid = val;
    }

    /**
     * Gets the nth component in this container.
     *
     * @param n the index of the component to get.
     *
     * @return the n<sup>th</sup> component in this container.
     *
     * @throws ArrayIndexOutOfBoundsException if the n<sup>th</sup> value does not exist.
     */
    public Component getComponent(final int n)
    {
        updateSortedIfRequired();
        return sorted[n];
    }

    /**
     * Returns number of components in container.
     *
     * @return number of components in container.
     */
    public int getComponentCount()
    {
        updateSortedIfRequired();
        return sorted.length;
    }

    /**
     * Gets all the components in this container.
     *
     * @return an array of all the components in this container.
     */
    public Component[] getComponents()
    {
        updateSortedIfRequired();
        return sorted;
    }
}
