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
// $Id: Sort.java,v 1.7 2006/01/08 05:04:21 kyank Exp $
//

package com.salas.bb.utils;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Sorting utilities.
 */
public final class Sort
{
    /**
     * Hidden utility class constructor.
     */
    private Sort()
    {
    }

    /**
     * Sorts the array of integers using the comparator provided. The array may contain
     * the ID's of some records or indexes in the other arrays or something which doesn't
     * have direct meaning for sorting. Comparator takes care of determining of the order
     * of object refered to by the value of integers in array being sorted.
     *
     * @param src       the array to be sorted.
     * @param fromIndex the index of the first element (inclusive) to be sorted.
     * @param toIndex   the index of the last element (exclusive) to be sorted.
     * @param comparator comparator to use.
     *
     * @return sorted source array.
     *
     * @throws IllegalArgumentException       if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or <tt>toIndex &gt;
     *                                        src.length</tt>
     */
    public static int[] sort(int[] src, int fromIndex, int toIndex,
                             final IValueComparator comparator)
    {
        int[] dest = new int[src.length];

        if (comparator != null)
        {
            Integer[] source = new Integer[src.length];
            for (int i = 0; i < src.length; i++) source[i] = new Integer(src[i]);

            Arrays.sort(source, fromIndex, toIndex, new Comparator()
            {
                public int compare(Object o1, Object o2)
                {
                    int i1 = ((Integer)o1).intValue();
                    int i2 = ((Integer)o2).intValue();

                    return comparator.compare(i1, i2);
                }
            });

            for (int i = 0; i < source.length; i++) dest[i] = source[i].intValue();
        } else
        {
            for (int i = 0; i < src.length; i++) dest[i] = src[i];
        }

        return dest;
    }

    /**
     * Comparator of values.
     */
    public static interface IValueComparator
    {
        /**
         * Compares two values.
         *
         * @param value1    first value.
         * @param value2    second value.
         *
         * @return negative if first value is less than second, zero if equal, postive otherwise.
         */
        int compare(int value1, int value2);
    }
}
