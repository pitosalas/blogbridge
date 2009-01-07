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
// $Id: TestArticlesGroup.java,v 1.10 2007/05/11 12:54:40 spyromus Exp $
//

package com.salas.bb.views.feeds.html;

import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.domain.StandardArticle;
import com.salas.bb.utils.uif.html.CustomHTMLEditorKit;
import junit.framework.TestCase;

import java.util.Date;
import java.util.ResourceBundle;

/**
 * This suite contains tests for <code>ArticlesGroup</code> unit.
 */
public class TestArticlesGroup extends TestCase
{
    private final SampleArticleDisplayConfig sampleArticleConfig;

    private ArticlesGroup group;

    /**
     * Creates test suite.
     */
    public TestArticlesGroup()
    {
        ResourceUtils.setBundle(ResourceBundle.getBundle("Resource"));
        sampleArticleConfig = new SampleArticleDisplayConfig();
    }

    /** Tests init. */
    protected void setUp()
        throws Exception
    {
        super.setUp();

        group = new ArticlesGroup("a", null);
    }

    /**
     * Tests initial state.
     */
    public void testInitialState()
    {
        assertEquals("Wrong name.", "a", group.getName());
        assertTrue("Wrong expansion state.", group.isExpanded());
    }

    /**
     * Tests expansion state changes when empty.
     */
    public void testEmptyGroupExpandCollapse()
    {
        group.setExpanded(false);
        assertFalse("Wrong expansion state.", group.isExpanded());

        group.setExpanded(true);
        assertTrue("Wrong expansion state.", group.isExpanded());
    }

    /**
     * Tests expanding and collapsing the views.
     */
    public void testExpandCollapse()
    {
        StandardArticle article1 = new StandardArticle("1");
        article1.setPublicationDate(new Date());
        StandardArticle article2 = new StandardArticle("2");
        article2.setPublicationDate(new Date());

        CustomHTMLEditorKit editorKit = new CustomHTMLEditorKit();
        
        HTMLArticleDisplay view1 = new HTMLArticleDisplay(article1,
            sampleArticleConfig, false, null, editorKit);
        HTMLArticleDisplay view2 = new HTMLArticleDisplay(article2,
            sampleArticleConfig, false, null, editorKit);

        group.register(view1);
        group.register(view2);

        group.setExpanded(false);
        assertFalse("View 1 should be hidden.", view1.isVisible());
        assertFalse("View 2 should be hidden.", view2.isVisible());

        group.setExpanded(true);
        assertTrue("View 1 should be shown.", view1.isVisible());
        assertTrue("View 2 should be shown.", view2.isVisible());
    }
}
