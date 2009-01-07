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
// $Id: TestListSelectionManager.java,v 1.2 2006/01/08 05:28:18 kyank Exp $
//

package com.salas.bb.utils.uif;

import junit.framework.TestCase;

import javax.swing.*;

/**
 * This suite contains tests for <code>ListSelectionManager</code> unit.
 */
public class TestListSelectionManager extends TestCase
{
    /**
     * Tests selecting first element.
     */
    public void testSimpleSelection()
    {
        JList list = new JList(new String[] { "a", "b", "c" });

        // Select first item
        list.setSelectionInterval(0, 0);

        assertSelection(list, new int[] { 0 }, 0, 0);
    }

    /**
     * Tests selecting first, then second element.
     */
    public void testSimpleSelection2()
    {
        JList list = new JList(new String[] { "a", "b", "c" });

        // Select first item
        list.setSelectionInterval(0, 0);

        // Select second item
        list.addSelectionInterval(1, 1);

        assertSelection(list, new int[] { 0, 1 }, 1, 0);
    }

    /**
     * Tests selecting first, then last element.
     */
    public void testSimpleSelection3()
    {
        JList list = new JList(new String[] { "a", "b", "c" });

        // Select first item
        list.setSelectionInterval(0, 0);

        // Select third item
        list.addSelectionInterval(2, 2);

        assertSelection(list, new int[] { 0, 2 }, 2, 0);
    }

    /**
     * Tests selecting first, last and then second element.
     */
    public void testSimpleSelection4()
    {
        JList list = new JList(new String[] { "a", "b", "c" });

        // Select first item
        list.setSelectionInterval(0, 0);

        // Select third item
        list.addSelectionInterval(2, 2);

        // Select second item
        list.addSelectionInterval(1, 1);

        assertSelection(list, new int[] { 0, 1, 2 }, 1, 2);
    }

    /**
     * Tests selecting first, last and second element, and deselecting last element then.
     */
    public void testSimpleSelection5()
    {
        JList list = new JList(new String[] { "a", "b", "c" });

        // Select first item
        list.setSelectionInterval(0, 0);

        // Select third item
        list.addSelectionInterval(2, 2);

        // Select second item
        list.addSelectionInterval(1, 1);

        // Deselect third item
        list.removeSelectionInterval(2, 2);

        assertSelection(list, new int[] { 0, 1 }, 1, 1);
    }

    /**
     * Tests selecting first, last and second element, and deselecting second element then.
     */
    public void testSimpleSelection6()
    {
        JList list = new JList(new String[] { "a", "b", "c" });

        // Select first item
        list.setSelectionInterval(0, 0);

        // Select third item
        list.addSelectionInterval(2, 2);

        // Select second item
        list.addSelectionInterval(1, 1);

        // Deselect second item
        list.removeSelectionInterval(1, 1);

        assertSelection(list, new int[] { 0, 2 }, 0, 1);
    }

    /**
     * Tests selecting all three elements and deselecting the first of them.
     */
    public void testSimpleSelection7()
    {
        JList list = new JList(new String[] { "a", "b", "c" });

        // Select all items
        list.setSelectionInterval(0, 2);

        // Deselect first item
        list.removeSelectionInterval(0, 0);

        assertSelection(list, new int[] { 1, 2 }, 2, 2);
    }

    /**
     * Verifies that the resulting picture is as required.
     *
     * @param list      list to check
     * @param indices   inices to be selected in list in this particular order.
     * @param main      main index to be selected, reported by <code>ListSelectionManager</code>.
     * @param oldIndex  old selection before the last operation.
     */
    private void assertSelection(JList list, int[] indices, int main, int oldIndex)
    {
        int[] selected = list.getSelectedIndices();

        assertEquals(main, ListSelectionManager.evaluateSelectionIndex(list, oldIndex));
        assertEquals(indices.length, selected.length);
        for (int i = 0; i < selected.length; i++)
        {
            assertEquals(indices[i], selected[i]);
        }
    }
}
