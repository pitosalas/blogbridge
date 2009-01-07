// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2007 by R. Pito Salas
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
// $Id: TestResultsListModel.java,v 1.1 2007/06/08 09:53:26 spyromus Exp $
//

package com.salas.bb.search;

import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.StandardArticle;
import junit.framework.TestCase;

/** Tests {@link com.salas.bb.search.ResultsListModel}. */
public class TestResultsListModel extends TestCase
{
    /** Model to operate. */
    private ResultsListModel model;
    /** Sample item. */
    private ResultItem item;

    /**
     * Configures the environment.
     *
     * @throws Exception if something goes wrong.
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        ResourceUtils.setBundlePath("Resource");

        // Create a feed with an article
        StandardArticle article1 = new StandardArticle("1");
        DirectFeed feed = new DirectFeed();
        feed.appendArticle(article1);

        // Create a model with an item
        model = new ResultsListModel();
        item = new ResultItem(article1);
    }

    /** Tests adding the item to the model. */
    public void testAdd()
    {
        assertEquals("Should be empty", 0, model.size());
        
        // Add item
        model.add(item);
        assertEquals("Should be one item", 1, model.size());
    }

    /** Tests adding the duplicate item to the model. */
    public void testAddDuplicate()
    {
        assertEquals("Should be empty", 0, model.size());

        // Add item
        model.add(item);
        model.add(item);
        assertEquals("Should be one item", 1, model.size());
    }

    /** Tests clearing the model. */
    public void testClear()
    {
        // Add item
        model.add(item);
        assertEquals("Should be one item", 1, model.size());

        // Clear
        model.clear();
        assertEquals("Should be empty", 0, model.size());
    }

    /** Tests clearing the model second time. */
    public void testClearTwice()
    {
        // Clear
        model.clear();
        model.clear();
        assertEquals("Should be empty", 0, model.size());
    }
}
