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
// $Id: TestSortGuidesByTitleAction.java,v 1.2 2006/01/08 05:28:16 kyank Exp $
//

package com.salas.bb.core.actions.guide;

import junit.framework.TestCase;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.StandardGuide;
import com.salas.bb.domain.IGuide;

/**
 * This suite contains tests for <code>SortGuidesByTitleAction</code> unit.
 */
public class TestSortGuidesByTitleAction extends TestCase
{
    /**
     * Tests sorting empty set.
     */
    public void testSortingEmpty()
    {
        GuidesSet set = new GuidesSet();
        SortGuidesByTitleAction.sortGuidesSet(set);
    }

    /**
     * Testing processing single guide.
     */
    public void testSortingOneGuide()
    {
        IGuide guide = guide("0");

        GuidesSet set = new GuidesSet();
        set.add(guide);

        SortGuidesByTitleAction.sortGuidesSet(set);
        assertEquals(1, set.getGuidesCount());
        assertTrue(set.getGuideAt(0) == guide);
    }

    /**
     * Testing processing straight order -- no changes.
     */
    public void testSortingNoChange()
    {
        IGuide guide0 = guide("0");
        IGuide guide1 = guide("1");
        IGuide guide2 = guide("2");

        GuidesSet set = new GuidesSet();
        set.add(guide0);
        set.add(guide1);
        set.add(guide2);

        SortGuidesByTitleAction.sortGuidesSet(set);
        assertEquals(3, set.getGuidesCount());
        assertTrue(set.getGuideAt(0) == guide0);
        assertTrue(set.getGuideAt(1) == guide1);
        assertTrue(set.getGuideAt(2) == guide2);
    }

    /**
     * Testing ordering from reversed position.
     */
    public void testSortingReverse()
    {
        IGuide guide0 = guide("0");
        IGuide guide1 = guide("1");
        IGuide guide2 = guide("2");

        GuidesSet set = new GuidesSet();
        set.add(guide2);
        set.add(guide1);
        set.add(guide0);

        SortGuidesByTitleAction.sortGuidesSet(set);
        assertEquals(3, set.getGuidesCount());
        assertTrue(set.getGuideAt(0) == guide0);
        assertTrue(set.getGuideAt(1) == guide1);
        assertTrue(set.getGuideAt(2) == guide2);
    }

    /**
     * Testing sensitivity to case of titles. Should be insensitive.
     */
    public void testCaseInsensitiveSorting()
    {
        IGuide guide0 = guide("a");
        IGuide guide1 = guide("B");

        GuidesSet set = new GuidesSet();
        set.add(guide1);
        set.add(guide0);

        SortGuidesByTitleAction.sortGuidesSet(set);
        assertEquals(2, set.getGuidesCount());
        assertTrue(set.getGuideAt(0) == guide0);
        assertTrue(set.getGuideAt(1) == guide1);
    }

    /**
     * Creates sample guide.
     *
     * @param title title to assign.
     *
     * @return guide.
     */
    private static IGuide guide(String title)
    {
        StandardGuide guide = new StandardGuide();
        guide.setTitle(title);
        return guide;
    }
}
