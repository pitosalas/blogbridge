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
// $Id $
//

package com.salas.bb.utils.feedscollections;

import javax.swing.tree.TreeNode;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ArrayList;

/**
 * Node of collection. Contains other nodes.
 */
abstract class CollectionNode implements TreeNode
{
    /** Property -- selected. */
    public static final String PROP_SELECTED = "selected";

    private PropertyChangeSupport pcs;

    private CollectionNode parent;
    private String title;
    private String[] tags;
    private String description;
    private boolean selected;

    /**
     * Creates titled node.
     *
     * @param title         title.
     * @param description   descriptive text.
     * @param tags          the list of tags.
     */
    protected CollectionNode(String title, String description, String[] tags)
    {
        this.description = description;
        this.title = title;
        this.tags = tags;

        pcs = new PropertyChangeSupport(this);
    }

    /**
     * Sets the parent of this node.
     *
     * @param parent parent.
     */
    public void setParent(CollectionNode parent)
    {
        this.parent = parent;
    }

    /**
     * Returns the title.
     *
     * @return title.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Returns description of the node.
     *
     * @return description.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Returns tags.
     *
     * @return tags.
     */
    public String[] getTags()
    {
        return tags;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    public String toString()
    {
        return getTitle();
    }

    /**
     * Returns the selection state.
     *
     * @return state.
     */
    public boolean isSelected()
    {
        return selected;
    }

    /**
     * Sets the selection state.
     *
     * @param selected state.
     */
    public void setSelected(boolean selected)
    {
        boolean oldVal = this.selected;
        this.selected = selected;

        firePropertyChanged(PROP_SELECTED, oldVal, selected);
    }

    /**
     * Returns the parent <code>TreeNode</code> of the receiver.
     */
    public TreeNode getParent()
    {
        return parent;
    }

    /**
     * Adds property change listener.
     *
     * @param l listener.
     */
    public void addListener(PropertyChangeListener l)
    {
        pcs.addPropertyChangeListener(l);
    }

    /**
     * Removes property change listener.
     *
     * @param l listener.
     */
    public void removeListener(PropertyChangeListener l)
    {
        pcs.removePropertyChangeListener(l);
    }

    /**
     * Fires property change event.
     *
     * @param name   property name.
     * @param oldVal old value.
     * @param newVal new value.
     */
    protected void firePropertyChanged(String name, boolean oldVal, boolean newVal)
    {
        pcs.firePropertyChange(name, oldVal, newVal);
    }

    /**
     * Returns the list of selected items, including this one.
     *
     * @return selected items.
     */
    public abstract List getSelectedItems();
}
