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
// $Id: TestHTMLFeedDisplay.java,v 1.10 2008/02/28 15:59:50 spyromus Exp $
//

package com.salas.bb.views.feeds.html;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.StandardArticle;
import com.salas.bb.views.feeds.AbstractFeedDisplayTestCase;
import com.salas.bb.views.feeds.GroupsSetup;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

/**
 * This suite contains tests for <code>HTMLFeedView</code> unit.
 */
public class TestHTMLFeedDisplay extends AbstractFeedDisplayTestCase
{
    private HTMLFeedDisplay    view;
    private StandardArticle article1;
    private StandardArticle article2;
    private StandardArticle article3;
    private DirectFeed      feed;

    static {
        ResourceUtils.setBundle(ResourceBundle.getBundle("Resource"));
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        feed = new DirectFeed();

        view = new HTMLFeedDisplay(new SampleHTMLFeedDisplayConfig(), new ValueHolder(0), new ValueHolder(0));
        view.setFeed(feed);

        article1 = createArticle(DELTA_TOMORROW);
        article2 = createArticle(DELTA_30_DAYS_AGO);
        article3 = createArticle(DELTA_30_DAYS_AGO + DAY);
    }

    /**
     * Tests initial state.
     */
    public void testInitialState()
    {
        Component[] components = view.getComponents();

        // Adding one for no-content pane
        assertEquals("Not all groups are there.", GroupsSetup.getGroupsCount() + 1, components.length);
        for (int i = 0; i < components.length - 1; i++)
        {
            Component component = components[i];
            assertTrue("Component isn't a group.", component instanceof ArticlesGroup);
            assertEquals("Wrong group name.", GroupsSetup.getGroupTitle(i), component.getName());
        }
    }

    /**
     * Waits for all EDT events to be processed.
     */
    private void waitForEDT()
    {
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                public void run() { }
            });
        } catch (InterruptedException e)
        {
            e.printStackTrace();
            fail();
        } catch (InvocationTargetException e)
        {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Checks the structure of view.
     *
     * @param aView         view.
     * @param aStructure    list of structure elements: 'a' - article view, 'g' - group view.
     */
    private void assertView(HTMLFeedDisplay aView, char[] aStructure)
    {
        Component[] components = aView.getComponents();

        assertEquals("Wrong number of structural elements.", aStructure.length, components.length);

        for (int i = 0; i < components.length; i++)
        {
            Component component = components[i];
            char el = aStructure[i];

            if (el == 'a')
            {
                assertTrue("Article view should be found at position " + i,
                    component instanceof HTMLArticleDisplay);
            } else if (el == 'n')
            {
                assertTrue("No-content pane should be found at position " + i,
                    component instanceof JPanel);
            } else
            {
                assertTrue("Group view should be found at position " + i,
                    component instanceof ArticlesGroup);
            }
        }
    }
}
