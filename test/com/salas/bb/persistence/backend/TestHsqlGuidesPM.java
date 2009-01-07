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
// $Id: TestHsqlGuidesPM.java,v 1.7 2007/10/03 11:41:55 spyromus Exp $
//

package com.salas.bb.persistence.backend;

import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.StandardGuide;
import com.salas.bb.persistence.PersistenceException;

import java.sql.SQLException;

/**
 * This suite contains tests for <code>HsqlGuidesPM</code> unit.
 */
public class TestHsqlGuidesPM extends AbstractHsqlPersistenceTestCase
{
    private HsqlGuidesPM manager;
    private GuidesSet set;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        // Init most modern database
        initManager("/resources");
        manager = new HsqlGuidesPM(pm);

        set = new GuidesSet();
    }

    /**
     * Tests adding guides.
     *
     * @throws PersistenceException if database fails.
     * @throws SQLException         if database fails.
     */
    public void testInsertGuide()
        throws PersistenceException, SQLException
    {
        StandardGuide standardGuide = new StandardGuide();
        standardGuide.setAutoFeedsDiscovery(!standardGuide.isAutoFeedsDiscovery());
        standardGuide.setIconKey("iconKey");
        standardGuide.setTitle("title");
        standardGuide.setPublishingPublic(true);
        standardGuide.setPublishingRating(2);
        standardGuide.setNotificationsAllowed(false);

        manager.insertGuide(standardGuide, 0);
        pm.commit();

        assertGuide(standardGuide);
    }

    /**
     * Tests updating the guide.
     *
     * @throws PersistenceException if database fails.
     * @throws SQLException         if database fails.
     */
    public void testUpdateGuide()
        throws PersistenceException, SQLException
    {
        StandardGuide standardGuide = new StandardGuide();
        standardGuide.setTitle("title");
        standardGuide.setPublishingPublic(true);
        standardGuide.setPublishingRating(2);
        standardGuide.setNotificationsAllowed(true);

        manager.insertGuide(standardGuide, 0);
        pm.commit();

        standardGuide.setTitle("title2");
        standardGuide.setPublishingPublic(false);
        standardGuide.setPublishingRating(1);
        standardGuide.setNotificationsAllowed(false);
        manager.updateGuide(standardGuide, 0);
        pm.commit();

        assertGuide(standardGuide);
    }

    /**
     * Testing moving guides.
     *
     * @throws PersistenceException if database fails.
     * @throws SQLException         if database fails.
     */
    public void testMovingGuide()
        throws PersistenceException, SQLException
    {
        // Add guides
        StandardGuide guide0 = new StandardGuide();
        guide0.setTitle("0");
        set.add(guide0);
        manager.insertGuide(guide0, 0);
        pm.commit();

        StandardGuide guide1 = new StandardGuide();
        guide1.setTitle("1");
        manager.insertGuide(guide1, 1);
        set.add(1, guide1);
        pm.commit();

        // Check if the order is initially right
        GuidesSet s = new GuidesSet();
        pm.loadGuidesSet(s);
        assertEquals(2, s.getGuidesCount());
        assertEquals(guide0.getID(), s.getGuideAt(0).getID());

        // Move the second guide to top and check again
        set.relocateGuide(guide1, 0);
        pm.updateGuidePositions(set);

        s = new GuidesSet();
        pm.loadGuidesSet(s);
        assertEquals(2, s.getGuidesCount());
        assertEquals(guide1.getID(), s.getGuideAt(0).getID());
    }

    /**
     * Tests removing the guide.
     *
     * @throws PersistenceException if database fails.
     * @throws SQLException         if database fails.
     */
    public void testDeleteGuide()
        throws PersistenceException, SQLException
    {
        StandardGuide standardGuide = new StandardGuide();
        standardGuide.setTitle("title");

        manager.insertGuide(standardGuide, 0);
        pm.commit();

        manager.removeGuide(standardGuide);
        pm.commit();

        assertNoGuide();
    }

    private void assertNoGuide()
        throws PersistenceException
    {
        pm.loadGuidesSet(set);
        assertEquals("No guide.", 0, set.getGuidesCount());
    }

    private void assertGuide(StandardGuide aStandardGuide)
        throws PersistenceException
    {
        pm.loadGuidesSet(set);
        assertEquals("No guide.", 1, set.getGuidesCount());

        StandardGuide loadedGuide = (StandardGuide)set.getGuideAt(0);
        assertEquals("Wrong guide.", aStandardGuide, loadedGuide);

        assertEquals(aStandardGuide.isPublishingPublic(), loadedGuide.isPublishingPublic());
        assertEquals(aStandardGuide.getPublishingRating(), loadedGuide.getPublishingRating());
    }
}
