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
// $Id: TestArticleDisplay.java,v 1.12 2007/08/29 14:29:53 spyromus Exp $
//

package com.salas.bb.views.feeds.html;

import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.domain.StandardArticle;
import com.salas.bb.domain.utils.TextRange;
import com.salas.bb.utils.uif.html.CustomHTMLEditorKit;
import com.salas.bb.views.feeds.IFeedDisplayConstants;
import junit.framework.TestCase;

import java.awt.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * This suite contains tests for <code>ArticleView</code> unit.
 */
public class TestArticleDisplay extends TestCase
{
    private HTMLArticleDisplay display;

    static
    {
        ResourceUtils.setBundle(ResourceBundle.getBundle("Resource"));
    }
    
    /** Tests init. */
    protected void setUp()
        throws Exception
    {
        super.setUp();

        StandardArticle article = new StandardArticle("a");
        article.setPublicationDate(new Date());

        display = new HTMLArticleDisplay(article, new SampleArticleDisplayConfig(),
            false, null, new CustomHTMLEditorKit());
    }

    /**
     * Tests selection of the article panel.
     */
    public void testSelection()
    {
        display.setSelected(false);
        Color unselectedColor = display.getBackground();

        display.setSelected(true);
        Color selectedColor = display.getBackground();

        assertFalse("Background colors should be different for selected and unselected views.",
            unselectedColor.equals(selectedColor));
    }

    /**
     * Tests switching to the title-only mode.
     */
    public void testTitleOnlyViewMode()
    {
        display.setViewMode(IFeedDisplayConstants.MODE_MINIMAL);
        assertFalse("Content panel should be invisible.",
            display.isContentPanelVisible());
    }

    /**
     * Tests switching to the brief mode.
     */
    public void testBriefViewMode()
    {
        display.setViewMode(IFeedDisplayConstants.MODE_BRIEF);
        assertTrue("Content panel should be visible.",
            display.isContentPanelVisible());
    }

    /**
     * Tests switching to the full mode.
     */
    public void testFullViewMode()
    {
        display.setViewMode(IFeedDisplayConstants.MODE_FULL);
        assertTrue("Content panel should be visible.",
            display.isContentPanelVisible());
    }

    /**
     * Tests adding links to map of links. Single-range links should be presented as plain
     * range objects; multi-range links should be presented as a list of ranges.
     */
    public void testAddLinkToMap()
    {
        Map<String, java.util.List<TextRange>> map = new HashMap<String, java.util.List<TextRange>>();

        TextRange range0 = new TextRange(0, 0);
        HTMLArticleDisplay.addLinkToMap(map, "a", range0);

        assertEquals("The link wasn't added.", 1, map.size());
        java.util.List<TextRange> record = map.get("a");
        assertTrue("Wrong object.", record.get(0) == range0);

        // Adding duplicate link
        TextRange range1 = new TextRange(1, 1);
        HTMLArticleDisplay.addLinkToMap(map, "a", range1);

        assertEquals("The link should be added to existing link entry.", 1, map.size());
        record = map.get("a");
        assertTrue("Wrong object.", record.get(0) == range0);
        assertTrue("Wrong object.", record.get(1) == range1);
    }
}
