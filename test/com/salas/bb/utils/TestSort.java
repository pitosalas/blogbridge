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
// $Id: TestSort.java,v 1.4 2006/01/08 05:28:19 kyank Exp $
//

package com.salas.bb.utils;

import junit.framework.TestCase;

import java.util.Arrays;

/**
 * @see Sort
 */
public class TestSort extends TestCase
{
    /**
     * Tests empty comparator reaction.
     */
    public void testSortWithNullComparator()
    {
        int[] src = new int[] { 5, 3, 1, 2, 4 };
        assertTrue(Arrays.equals(src, Sort.sort(src, 0, 4, null)));
    }

    /**
     * Tests ranges checking.
     */
    public void testSortRangeChecking()
    {
        checkFail("From > To", 3, 2);
        checkFail("From < 0", -1, 2);
        checkFail("From > max", 6, 6);
        checkFail("To > max", 2, 6);
    }

    /**
     * Tests sorting with different bounds.
     */
    public void testSorting()
    {
        check(new int[] { 5, 3, 1, 2, 4 }, 0, 0, new int[] { 5, 3, 1, 2, 4});
        check(new int[] { 5, 3, 1, 2, 4 }, 0, 1, new int[] { 5, 3, 1, 2, 4});
        check(new int[] { 5, 3, 1, 2, 4 }, 0, 2, new int[] { 3, 5, 1, 2, 4});
        check(new int[] { 5, 3, 1, 2, 4 }, 0, 3, new int[] { 1, 3, 5, 2, 4});
        check(new int[] { 5, 3, 1, 2, 4 }, 0, 4, new int[] { 1, 2, 3, 5, 4});
        check(new int[] { 5, 3, 1, 2, 4 }, 0, 5, new int[] { 1, 2, 3, 4, 5});

        check(new int[] { 5, 3, 1, 2, 4 }, 1, 1, new int[] { 5, 3, 1, 2, 4});
        check(new int[] { 5, 3, 1, 2, 4 }, 1, 2, new int[] { 5, 3, 1, 2, 4});
        check(new int[] { 5, 3, 1, 2, 4 }, 1, 3, new int[] { 5, 1, 3, 2, 4});
        check(new int[] { 5, 3, 1, 2, 4 }, 1, 4, new int[] { 5, 1, 2, 3, 4});
        check(new int[] { 5, 3, 1, 2, 4 }, 1, 5, new int[] { 5, 1, 2, 3, 4});

        check(new int[] { 5, 3, 1, 2, 4 }, 2, 2, new int[] { 5, 3, 1, 2, 4});
        check(new int[] { 5, 3, 1, 2, 4 }, 2, 3, new int[] { 5, 3, 1, 2, 4});
        check(new int[] { 5, 3, 1, 2, 4 }, 2, 4, new int[] { 5, 3, 1, 2, 4});
        check(new int[] { 5, 3, 1, 2, 4 }, 2, 5, new int[] { 5, 3, 1, 2, 4});

        check(new int[] { 5, 3, 1, 2, 4 }, 3, 3, new int[] { 5, 3, 1, 2, 4});
        check(new int[] { 5, 3, 1, 2, 4 }, 3, 4, new int[] { 5, 3, 1, 2, 4});
        check(new int[] { 5, 3, 1, 2, 4 }, 3, 5, new int[] { 5, 3, 1, 2, 4});

        check(new int[] { 5, 3, 1, 2, 4 }, 4, 4, new int[] { 5, 3, 1, 2, 4});
        check(new int[] { 5, 3, 1, 2, 4 }, 4, 5, new int[] { 5, 3, 1, 2, 4});
    }

    /**
     * Checks sorting of long arrays. 7 and 8 items are critical.
     */
    public void testSortingBigArrays()
    {
        check(new int[] { 5, 3, 1, 2, 4, 1, 8 }, 0, 7, new int[] { 1, 1, 2, 3, 4, 5, 8});
        check(new int[] { 5, 3, 1, 2, 4, 1, 8, 3 }, 0, 8, new int[] { 1, 1, 2, 3, 3, 4, 5, 8});

        int[] src, dst;
        src = new int[50];
        dst = new int[50];
        for (int i = 0; i < src.length; i++)
        {
            src[i] = 49 - i;
            dst[i] = i;
        }

        check(src, 0, 50, dst);
    }

    // Checks that sort succeed.
    private void check(int[] src, int from, int to, int[] dst)
    {
        int[] res = Sort.sort(src, from, to, new MyComparator());
        assertEquals(res.length, dst.length);
        for (int i = 0; i < res.length; i++)
        {
            assertEquals(dst[i], res[i]);
        }
    }

    // Checks that sort fails.
    private void checkFail(String msg, int from, int to)
    {
        try
        {
            Sort.sort(new int[] { 5, 3, 1, 2, 4}, from, to, new MyComparator());
            fail(msg);
        } catch (RuntimeException e)
        {
            // Expected
        }
    }

    // Direct integer comparator.
    private static class MyComparator implements Sort.IValueComparator
    {
        /**
         * Compares two values.
         *
         * @param value1 first value.
         * @param value2 second value.
         *
         * @return negative if first value is less than second, zero if equal, postive otherwise.
         */
        public int compare(int value1, int value2)
        {
            return value1 < value2 ? -1 : value1 == value2 ? 0 : 1;
        }
    }
}
