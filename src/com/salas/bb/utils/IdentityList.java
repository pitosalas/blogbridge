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
// $Id: IdentityList.java,v 1.4 2007/01/24 14:46:38 spyromus Exp $
//

package com.salas.bb.utils;

import java.util.ArrayList;

/**
 * The list acting like <code>ArrayList</code> in all respects except that the
 * two elements considered equal only if they are the same objects.
 */
public class IdentityList<T> extends ArrayList<T>
{
    /**
     * Searches for the first occurence of the given argument, testing for equality using the
     * <tt>equals</tt> method.
     *
     * @param elem an object.
     *
     * @return the index of the first occurrence of the argument in this list; returns <tt>-1</tt>
     *         if the object is not found.
     *
     * @see Object#equals(Object)
     */
    public int indexOf(Object elem)
    {
        int index = -1;

        if (elem == null)
        {
            index = super.indexOf(elem);
        } else
        {
            for (int i = 0; index == -1 && i < size(); i++)
            {
                if (elem == get(i)) index = i;
            }
        }

        return index;
    }

    /**
     * Returns the index of the last occurrence of the specified object in this list.
     *
     * @param elem the desired element.
     *
     * @return the index of the last occurrence of the specified object in this list; returns -1 if
     *         the object is not found.
     */
    public int lastIndexOf(Object elem)
    {
        int index = -1;

        if (elem == null)
        {
            index = super.lastIndexOf(elem);
        } else
        {
            for (int i = size() - 1; index == -1 && i >= 0; i--)
            {
                if (elem == get(i)) index = i;
            }
        }

        return index;
    }

    /**
     * Removes a single instance of the specified element from this
     * collection, if it is present (optional operation).
     *
     * @param o element to be removed from this collection, if present.
     * @return <tt>true</tt> if the collection contained the specified
     *         element.
     */
    public boolean remove(Object o)
    {
        boolean removed = false;

        int index = indexOf(o);
        if (index != -1)
        {
            removed = true;
            remove(index);
        }

        return removed;
    }
}
