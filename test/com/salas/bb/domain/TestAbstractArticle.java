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
// $Id: TestAbstractArticle.java,v 1.6 2006/12/14 10:40:51 spyromus Exp $
//

package com.salas.bb.domain;

import junit.framework.TestCase;

import java.util.Collection;

import com.salas.bb.utils.swinghtml.TextProcessor;
import com.salas.bb.utils.StringUtils;

/**
 * This suite contains tests for <code>AbstractArticle</code> unit.
 * It covers: getting plain text version of content and collecting links.
 */
public class TestAbstractArticle extends TestCase
{
    private DummyAbstractArticle article = new DummyAbstractArticle();

    /**
     * Tests how article returns the plain text equivalent of source text.
     * Target: NULL checks.
     */
    public void testGetPlainTextNull()
    {
        article.setText(null);
        assertNull("NULL should be returned as plain text isn't set.", article.getPlainText());
    }

    /**
     * Tests how article returns the plain text equivalent of source text.
     * Target: removal of decorations.
     */
    public void testGetPlainTextNoDecorations()
    {
        article.setText("<b>test</b>");
        assertEquals("Decoration should be removed.", "test", article.getPlainText());

        article.setText("<i>test</i>");
        assertEquals("Decoration should be removed.", "test", article.getPlainText());

        article.setText("<u>test</u>");
        assertEquals("Decoration should be removed.", "test", article.getPlainText());
    }

    /**
     * Tests how article returns the plain text equivalent of source text.
     * Target: removal of images.
     */
    public void testGetPlainTextNoImages()
    {
        article.setText("<p>test<img src='a'>");
        assertEquals("Images should be removed.", "<p>test", article.getPlainText());
    }

    /**
     * Tests how article returns the plain text equivalent of source text.
     * Target: leaving HTML entities.
     */
    public void testGetPlainTextEntities()
    {
        article.setText("test&nbsp;&amp;&lt;&gt;&apos;&quot;");
        assertEquals("Entities should be left.",
            "test&nbsp;&amp;&lt;&gt;&apos;&quot;", article.getPlainText());
    }

    /**
     * Tests finding the links.
     */
    public void testCollectLinks()
    {
        Collection p1 = AbstractArticle.collectLinks("<body> a <a href='test'> a </a> </body>");
        assertEquals("Wrong links count.", 1, p1.size());
        assertTrue("Link wasn't found.", p1.contains("test"));

        Collection p2 = AbstractArticle.collectLinks("<body> a <A href='test'> a </a> </body>");
        assertEquals("Wrong links count.", 1, p2.size());
        assertTrue("Link wasn't found.", p2.contains("test"));

        Collection p3 = AbstractArticle.collectLinks("<body> a <A href = \"test\">");
        assertEquals("Wrong links count.", 1, p3.size());
        assertTrue("Link wasn't found.", p3.contains("test"));

        Collection p4 = AbstractArticle.collectLinks("<body> a <A id='a' href = \"test\"> " +
            "</a> <a href='test2'/>");
        assertEquals("Wrong links count.", 2, p4.size());
        assertTrue("Link wasn't found.", p4.contains("test"));
        assertTrue("Link wasn't found.", p4.contains("test2"));

        Collection n1 = AbstractArticle.collectLinks("<a id='a'> </a>");
        assertEquals("There are no links.", 0, n1.size());
    }

    /**
     * Tests handling of failure situations.
     */
    public void testCollectLinksFailure()
    {
        Collection n2 = AbstractArticle.collectLinks(null);
        assertEquals("Wrong links count.", 0, n2.size());
    }

    public void testHashCode()
    {
        String title = "Xeni on NPR: Google ices CNET over privacy story";
        String text = "<strong>Xeni Jardin</strong>:\n" +
            "Today on NPR's \"<a href=\"http://www.npr.org/programs/day/\">Day to Day</a>,\" I speak with host <a href=\"http://www.npr.org/templates/story/story.php?storyId=2100281\">Madeleine Brand</a> and guest <a href=\"http://www.dangillmor.com/\">Dan Gillmor</a> about a very public flap over private data between Google and CNET News.com. <p>\n" +
            "\n" +
            "It all goes back to a <a href=\"http://news.com/Google+balances+privacy,+reach/2100-1032_3-5787483.html\">July, 2005 CNET story</a>. The volume of personal data available on the web through Google and other search tools has long been a cause for concern among privacy advocates. But Google also gathers data about its users through its email service, map apps, and shopping search tools (as do its competitors). The CNET story asked what the consequences might be if Google user info fell into the hands of government investigators, ill-wishing hackers -- even a Google employee violating company privacy practices. <p>\n" +
            "To illustrate the concern, reporter Elinor Mills lead her story with an anecdote revealing personal data about Google CEO Eric Schmidt -- data she obtained from Google searches -- including the value of his Google shares, where he lives, his wife's name, and the fact that he's been to Burning Man. In response, <a href=\"http://news.com.com/Wanted+at+Google+A+few+good+chefs/2100-1030_3-5819085.html\">CNET says Google retaliated</a> by declaring it will not speak to CNET reporters until August 2006. <p>\n" +
            "<a href=\"http://www.npr.org/templates/story/story.php?storyId=4794016\">Link</a> to NPR \"Day to Day\" report on l'affaire Google/CNET; archived audio (Real/Win streams) online after 12pm PT.";

        text = TextProcessor.filterText(text);
        title = TextProcessor.filterTitle(title, text);

        // Title + text
        StandardArticle article = new StandardArticle(text);
        article.setTitle(title);

        System.out.println("Full  : " + article.getSimpleMatchKey());

        // Title Only
        StandardArticle articleTitleOnly = new StandardArticle("");
        articleTitleOnly.setTitle(title);

        System.out.println("Title : " + articleTitleOnly.getSimpleMatchKey());

        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i];
            StandardArticle art = new StandardArticle(line);
            System.out.println("Line " + i + ": " + art.getSimpleMatchKey());
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------

    private static class DummyAbstractArticle extends AbstractArticle
    {
        private String text;

        /**
         * Sets the source text of article.
         *
         * @param aText text of article.
         */
        public void setText(String aText)
        {
            text = aText;
        }

        /**
         * Returns HTML version of article text.
         *
         * @return HTML version of text.
         */
        public String getHtmlText()
        {
            return text;
        }
    }
}
