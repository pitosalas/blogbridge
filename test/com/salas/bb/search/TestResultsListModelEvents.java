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
// $Id: TestResultsListModelEvents.java,v 1.4 2008/02/28 15:59:51 spyromus Exp $
//

package com.salas.bb.search;

import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.StandardArticle;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/** Tests the events fired by the model. */
public class TestResultsListModelEvents extends MockObjectTestCase
{
    /** Mock listener. */
    private Mock listener;
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

        listener = new Mock(IResultsListModelListener.class);

        ResourceUtils.setBundlePath("Resource");

        // Create a feed with an article
        StandardArticle article1 = new StandardArticle("1");
        DirectFeed feed = new DirectFeed();
        feed.appendArticle(article1);

        // Create a model with an item
        model = new ResultsListModel();
        model.addListener((IResultsListModelListener)listener.proxy());
        item = new ResultItem(article1);
    }

    /** Tests adding an item and the group. */
    public void testAdd()
    {
        listener.expects(once()).method("onGroupAdded").with(same(model), isA(ResultGroup.class), same(false));
        listener.expects(once()).method("onItemAdded").with(same(model), same(item), isA(ResultGroup.class));

        model.add(item);

        listener.verify();
    }

    /** Tests adding the duplicate. */
    public void testAddDuplicate()
    {
        listener.expects(once()).method("onGroupAdded").with(same(model), isA(ResultGroup.class), same(false));
        listener.expects(once()).method("onItemAdded").with(same(model), same(item), isA(ResultGroup.class));

        model.add(item);
        model.add(item);

        listener.verify();
    }

    /** Tests clearing the list. */
    public void testClear()
    {
        listener.expects(once()).method("onGroupAdded").with(same(model), isA(ResultGroup.class), same(false));
        listener.expects(once()).method("onItemAdded").with(same(model), same(item), isA(ResultGroup.class));
        listener.expects(once()).method("onClear").with(same(model));

        model.add(item);
        model.clear();

        listener.verify();
    }
}
