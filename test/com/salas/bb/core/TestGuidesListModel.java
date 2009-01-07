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
// $Id: TestGuidesListModel.java,v 1.5 2007/04/17 14:29:38 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.domain.*;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.jmock.core.constraint.IsEqual;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;

/**
 * Tests events of the guides list model.
 */
public class TestGuidesListModel extends MockObjectTestCase
{
    private GuidesSet s;
    private GuidesListModel m;
    private Mock listener;
    private StandardGuide g0, g1, g2, g3, g4;
    private int addPtr;
    private GuideDisplayModeManager dmm;

    public TestGuidesListModel()
    {
        dmm = GuideDisplayModeManager.getInstance();
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        s = new GuidesSet();
        m = new GuidesListModel();
        m.setGuidesSet(s);
        m.testing = true;
        
        g0 = new StandardGuide();
        g1 = new StandardGuide();
        g2 = new StandardGuide();
        g3 = new StandardGuide();
        g4 = new StandardGuide();

        listener = new Mock(ListDataListener.class);
        m.addListDataListener((ListDataListener)listener.proxy());

        // Init GDMM
        dmm.clear();
        dmm.setColor(GuideClass.READ, Color.BLACK);

        addPtr = 0;
    }

    // ------------------------------------------------------------------------
    // Basic events
    // ------------------------------------------------------------------------

    /**
     * When guide is added it's appended to the tail of the list.
     */
    public void testOnGuideAdded()
    {
        add(g0);
        assertGuides(g0);

        add(g1);
        assertGuides(g0, g1);

        listener.verify();
    }

    /**
     * When guide is removed it's no longer in the model.
     */
    public void testOnGuideDelete()
    {
        add(g0, g1);

        remove(g0);
        assertGuides(g1);

        remove(g1);
        assertGuides();

        listener.verify();
    }

    /**
     * When guide is moved it's position changes in the model.
     */
    public void testOnGuideMoved_Up_2()
    {
        add(g0, g1);

        expectContentsChanged(0, 0);
        expectContentsChanged(1, 1);
        s.relocateGuide(g1, 0);
        assertGuides(g1, g0);

        listener.verify();
    }

    /**
     * When guide is moved it's position changes in the model.
     */
    public void testOnGuideMoved_Up_5()
    {
        add(g0, g1, g2, g3, g4);

        expectContentsChanged(1, 1);
        expectContentsChanged(2, 2);
        expectContentsChanged(3, 3);
        s.relocateGuide(g3, 1);
        assertGuides(g0, g3, g1, g2, g4);

        listener.verify();
    }

    /**
     * When guide is moved it's position changes in the model.
     */
    public void testOnGuideMoved_Down_2()
    {
        add(g0, g1);

        expectContentsChanged(0, 0);
        expectContentsChanged(1, 1);
        s.relocateGuide(g0, 1);
        assertGuides(g1, g0);

        listener.verify();
    }

    /**
     * When guide is moved it's position changes in the model.
     */
    public void testOnGuideMoved_Down_5()
    {
        add(g0, g1, g2, g3, g4);

        expectContentsChanged(1, 1);
        expectContentsChanged(2, 2);
        expectContentsChanged(3, 3);
        s.relocateGuide(g1, 3);
        assertGuides(g0, g2, g3, g1, g4);

        listener.verify();
    }

    // ------------------------------------------------------------------------
    // Hiding guides
    // ------------------------------------------------------------------------

    /**
     * Hides and shows guides to see how it acts.
     */
    public void testHidingShowingGuides()
    {
        makeRead(g0, g2);
        makeUnread(g1);

        add(g0, g1, g2);
        
        // Hiding read guides: removes the second guide first and the third (on the second place)
        expectIntervalRemoved(0, 0);
        expectIntervalRemoved(1, 1);
        dmm.setColor(GuideClass.READ, null);
        assertGuides(g1);

        // Showing back
        expectContentsChanged(0, 0);
        expectIntervalAdded(1, 1);
        expectIntervalAdded(2, 2);
        dmm.setColor(GuideClass.READ, Color.BLACK);
        assertGuides(g0, g1, g2);

        listener.verify();
    }

    // ------------------------------------------------------------------------
    // Init & Assertions
    // ------------------------------------------------------------------------

    /**
     * Makes guides unread by adding a feed to it.
     *
     * @param guides guides.
     */
    private void makeUnread(StandardGuide ... guides)
    {
        for (StandardGuide guide : guides)
        {
            guide.add(new DirectFeed()
            {
                @Override
                public synchronized boolean isRead()
                {
                    return false;
                }

                @Override
                public boolean isVisible()
                {
                    return true;
                }
            });
        }
    }

    /**
     * Makes guides read by removing all feeds.
     *
     * @param guides guides.
     */
    private void makeRead(StandardGuide ... guides)
    {
        for (StandardGuide guide : guides) guide.clean();
    }

    /**
     * Adds guides to the model and configures expectations.
     *
     * @param guides guides.
     */
    private void add(IGuide ... guides)
    {
        int p = addPtr;

        for (IGuide guide : guides)
        {
            expectIntervalAdded(p, p++);
            s.add(guide);
        }

        addPtr = p;
    }

    /**
     * Removes guide from the model and configures expectations.
     *
     * @param guide guide.
     */
    private void remove(IGuide guide)
    {
        int i = m.indexOf(guide);
        expectIntervalRemoved(i, i);
        s.remove(guide);

        addPtr--;
    }

    /**
     * Checks if guides are in the set and in the correct order.
     *
     * @param guides guides.
     */
    private void assertGuides(IGuide ... guides)
    {
        assertEquals(guides.length, m.getSize());
        for (int i = 0; i < guides.length; i++)
        {
            assertTrue("Guide " + i + " is incorrect", m.getElementAt(i) == guides[i]);
        }
    }

    // ------------------------------------------------------------------------
    // Mock methods
    // ------------------------------------------------------------------------

    private void expectIntervalAdded(int i0, int i1)
    {
        expectEvent("intervalAdded", ListDataEvent.INTERVAL_ADDED, i0, i1);
    }

    private void expectIntervalRemoved(int i0, int i1)
    {
        expectEvent("intervalRemoved", ListDataEvent.INTERVAL_REMOVED, i0, i1);
    }

    private void expectContentsChanged(int i0, int i1)
    {
        expectEvent("contentsChanged", ListDataEvent.CONTENTS_CHANGED, i0, i1);
    }

    private void expectEvent(String method, int type, int i0, int i1)
    {
        ListDataEvent e = new ListDataEvent(m, type, i0, i1);
        listener.expects(once()).method(method).with(eqLDE(e));
    }

    private static Constraint eqLDE(ListDataEvent event)
    {
        return new ListDataEventConstraint(event);
    }

    /**
     * Matches list data even objects through their string
     * representations.
     */
    private static class ListDataEventConstraint extends IsEqual
    {
        public ListDataEventConstraint(ListDataEvent lde)
        {
            super(lde.toString());
        }

        public boolean eval(Object o)
        {
            return super.eval(o.toString());
        }
    }
}
