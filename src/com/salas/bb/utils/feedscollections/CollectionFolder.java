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
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Folder node. Contains other nodes.
 */
class CollectionFolder extends CollectionNode
{
    protected List nodes;

    /**
     * Creates titled folder.
     *
     * @param title         folder title.
     * @param description   descriptive text.
     * @param tags          tags list.
     */
    public CollectionFolder(String title, String description, String[] tags)
    {
        super(title, description, tags);

        nodes = new ArrayList();
    }

    /**
     * Adds a node.
     *
     * @param node node.
     */
    public void addNode(CollectionNode node)
    {
        nodes.add(node);
    }

    /**
     * Returns the node at a given position.
     *
     * @param i index.
     *
     * @return node.
     */
    public CollectionNode getNode(int i)
    {
        return (CollectionNode)nodes.get(i);
    }

    // TreeNode implementation

    /**
     * Returns the number of children <code>TreeNode</code>s the receiver
     * contains.
     */
    public int getChildCount()
    {
        return nodes.size();
    }

    /**
     * Returns true if the receiver allows children.
     */
    public boolean getAllowsChildren()
    {
        return true;
    }

    /**
     * Returns true if the receiver is a leaf.
     */
    public boolean isLeaf()
    {
        return getChildCount() == 0;
    }

    /**
     * Returns the children of the receiver as an <code>Enumeration</code>.
     */
    public Enumeration children()
    {
        return null;
    }

    /**
     * Returns the child <code>TreeNode</code> at index
     * <code>childIndex</code>.
     */
    public TreeNode getChildAt(int childIndex)
    {
        return getNode(childIndex);
    }

    /**
     * Returns the index of <code>node</code> in the receivers children.
     * If the receiver does not contain <code>node</code>, -1 will be
     * returned.
     */
    public int getIndex(TreeNode node)
    {
        return nodes.indexOf(node);
    }

    /**
     * Returns the list of selected items, including this one.
     *
     * @return selected items.
     */
    public List getSelectedItems()
    {
        List selected = new ArrayList();

        int count = nodes.size();
        for (int i = 0; i < count; i++)
        {
            CollectionNode nd = getNode(i);
            selected.addAll(nd.getSelectedItems());
        }

        return selected;
    }
}
