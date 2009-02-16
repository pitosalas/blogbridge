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
// $Id: TestGroupLayoutManager.java,v 1.3 2007/08/01 11:36:36 spyromus Exp $
//

package com.salas.bb.views.feeds.image;

import junit.framework.TestCase;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * This suite contains tests for <code>GroupLayoutManager</code> unit.
 */
public class TestGroupLayoutManager extends TestCase
{
    private static final int ITEM_HEIGHT    = 20;
    private static final int ITEM_WIDTH     = 20;
    private static final Dimension ITEM_DIM = new Dimension(ITEM_WIDTH, ITEM_HEIGHT);

    private static final int GAP_V          = 5;
    private static final int GAP_H          = 5;

//    /**
//     * Tests updating dimension object with items.
//     */
//    public void testUpdateDimensionWithItems()
//    {
//        assertDim(0, 1, 0, 0);
//        assertDim(1, 1, 2 * GAP_H + ITEM_WIDTH, 2 * GAP_V + ITEM_HEIGHT);
//        assertDim(2, 1, 2 * GAP_H + ITEM_WIDTH, 3 * GAP_V + 2 * ITEM_HEIGHT);
//        assertDim(2, 2, 3 * GAP_H + 2 * ITEM_WIDTH, 2 * GAP_V + ITEM_HEIGHT);
//        assertDim(3, 2, 3 * GAP_H + 2 * ITEM_WIDTH, 3 * GAP_V + 2 * ITEM_HEIGHT);
//    }
//
//    private static void assertDim(int items, int cols, int width, int height)
//    {
//        Dimension dim = GroupLayoutManager.updateDimensionWithItems(new Dimension(0, 0),
//            items, ITEM_DIM, cols, GAP_H, GAP_V);
//
//        assertEquals("Wrong width.", width, dim.width);
//        assertEquals("Wrong height.", height, dim.height);
//    }

    /**
     * Tests evaluation of available columns.
     */
    public void testEvalAvailableColumns()
    {
        assertAvailCols(0, 1);
        assertAvailCols(ITEM_WIDTH, 1);
        assertAvailCols(ITEM_WIDTH + GAP_H, 1);
        assertAvailCols(ITEM_WIDTH + 2 * GAP_H, 1);
        assertAvailCols(2 * ITEM_WIDTH + 2 * GAP_H, 1);
        assertAvailCols(2 * ITEM_WIDTH + 3 * GAP_H, 2);
    }

    private static void assertAvailCols(int width, int targetCols)
    {
        assertEquals("Wrong number of columns.", targetCols,
            GroupLayoutManager.evalAvailableColumns(width, ITEM_WIDTH, GAP_H));
    }

    /**
     * Tests evaluation of maximum item dimensions.
     */
    public void testEvalItemDimension()
    {
        Set<Component> dividers = new HashSet<Component>();

        assertItemDim(new Component[] { }, dividers, 0, 0);
        assertItemDim(new Component[] { makePanel(10) }, dividers, 10, 10);
        assertItemDim(new Component[] { makePanel(10), makePanel(5) }, dividers, 10, 10);
        assertItemDim(new Component[] { makePanel(10), makePanel(25) }, dividers, 25, 25);

        Component panel = makePanel(20);
        dividers.add(panel);

        assertItemDim(new Component[] { makePanel(10), panel }, dividers, 10, 10);
    }

    private Component makePanel(int widthHeight)
    {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(widthHeight, widthHeight));

        return panel;
    }

    private static void assertItemDim(Component[] components, Set<Component> dividers,
        int w, int h)
    {
        Dimension dim = GroupLayoutManager.evalItemDimension(components, dividers);
        assertEquals("Wrong width.", w, dim.width);
        assertEquals("Wrong height.", h, dim.height);
    }

    /**
     * Tests the layout of container.
     */
    public void testLayoutContainer()
    {
        int gv = GAP_V;
        int gh = GAP_H;
        int iw = ITEM_WIDTH;
        int ih = iw;
        int dh = 10;

        GroupLayoutManager layout = new GroupLayoutManager();
        JPanel cont = new JPanel(layout);
        Component div0 = makePanel(dh);
        Component div1 = makePanel(dh);
        Component div2 = makePanel(dh);
        Component pan01 = makePanel(iw);
        Component pan02 = makePanel(iw);
        Component pan03 = makePanel(iw);
        Component pan21 = makePanel(iw);

        cont.add(div0, GroupLayoutManager.DIVIDER);
        cont.add(pan01);
        cont.add(pan02);
        cont.add(pan03);
        cont.add(div1, GroupLayoutManager.DIVIDER);
        cont.add(div2, GroupLayoutManager.DIVIDER);
        cont.add(pan21);

        // Configure panel to be narrow
        int width = gh + iw;
        cont.setSize(width, 1);
        layout.layoutContainer(cont);
        assertEquals(new Rectangle(0, 0, width, dh), div0.getBounds());
        assertEquals(new Rectangle(0, dh + gv, iw, ih), pan01.getBounds());
        assertEquals(new Rectangle(0, dh + gv + ih + gv, iw, ih), pan02.getBounds());
        assertEquals(new Rectangle(0, dh + gv + ih + gv + ih + gv, iw, ih), pan03.getBounds());
        assertEquals(new Rectangle(0, dh + gv + ih + gv + ih + gv + ih + gv, width, dh), div1.getBounds());
        assertEquals(new Rectangle(0, dh + gv + ih + gv + ih + gv + ih + gv + dh + gv, width, dh), div2.getBounds());
        assertEquals(new Rectangle(0, dh + gv + ih + gv + ih + gv + ih + gv + dh + gv + dh + gv, iw, ih), pan21.getBounds());

        // Container can fit two columns
        width = gh + iw + gh + iw + gh;
        cont.setSize(width, 1);
        layout.layoutContainer(cont);
        assertEquals(new Rectangle(0, 0, width, dh), div0.getBounds());
        assertEquals(new Rectangle(0, dh + gv, iw, ih), pan01.getBounds());
        assertEquals(new Rectangle(iw + gh, dh + gv, iw, ih), pan02.getBounds());
        assertEquals(new Rectangle(0, dh + gv + ih + gv, iw, ih), pan03.getBounds());
        assertEquals(new Rectangle(0, dh + gv + ih + gv + ih + gv, width, dh), div1.getBounds());
        assertEquals(new Rectangle(0, dh + gv + ih + gv + ih + gv + dh + gv, width, dh), div2.getBounds());
        assertEquals(new Rectangle(0, dh + gv + ih + gv + ih + gv + dh + gv + dh + gv, iw, ih), pan21.getBounds());

        // Container can fit two columns and div1 & div2 become invisible, so the two groups should be merged
        width = gh + iw + gh + iw + gh;
        div1.setVisible(false);
        div2.setVisible(false);
        cont.setSize(width, 1);
        layout.layoutContainer(cont);
        assertEquals(new Rectangle(0, 0, width, dh), div0.getBounds());
        assertEquals(new Rectangle(0, dh + gv, iw, ih), pan01.getBounds());
        assertEquals(new Rectangle(iw + gh, dh + gv, iw, ih), pan02.getBounds());
        assertEquals(new Rectangle(0, dh + gv + ih + gv, iw, ih), pan03.getBounds());
        assertEquals(new Rectangle(iw + gh, dh + gv + ih + gv, iw, ih), pan21.getBounds());
    }
}
