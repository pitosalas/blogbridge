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
// $Id: TestTemplate.java,v 1.3 2008/04/03 08:53:25 spyromus Exp $
//

package com.salas.bb.remixfeeds.templates;

import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.StandardArticle;
import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Tests template rendering.
 */
@SuppressWarnings({"MagicNumber"})
public class TestTemplate extends TestCase
{
    private static final String TEMPLATE_EMPTY = "";
    private static final String TEMPLATE_SINGLE =
        "PURL: ${article.url} PTitle: ${article.title} PText: ${article.text} PDate: ${article.date} " +
        "FURL: ${feed.url} FTitle: ${feed.title}";
    private static final String TEMPLATE_MULTI =
        "# for each article\n" +
        TEMPLATE_SINGLE + "\n" +
        "# endfor";

    private Template template;
    private Set<IArticle> singleArticle;
    private Set<IArticle> multipleArticles;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        template = new Template();

        // Sample date
        Calendar c = new GregorianCalendar(2008, 0, 1, 10, 50, 30);
        Date date = c.getTime();

        // Sample feed
        DirectFeed feed = new DirectFeed();
        feed.setCustomTitle("F1 <b>Title</b>");
        feed.setXmlURL(new URL("http://f 1/"));

        // Sample article
        StandardArticle a1 = article(feed, 1, date);
        StandardArticle a2 = article(feed, 2, date);

        // Sample single-article set
        LinkedHashSet<IArticle> set = new LinkedHashSet<IArticle>();
        set.add(a1);
        singleArticle = set;

        set = new LinkedHashSet<IArticle>();
        set.add(a1);
        set.add(a2);
        multipleArticles = set;
    }

    /**
     * Creates a sample article.
     *
     * @param feed  feed to link to.
     * @param id    id.
     * @param date  pub. date.
     *
     * @return article.
     *
     * @throws MalformedURLException if URL is invalid (never).
     */
    private StandardArticle article(DirectFeed feed, int id, Date date)
        throws MalformedURLException
    {
        StandardArticle a = new StandardArticle("A" + id + " <i>Text</i>");
        a.setTitle("A" + id + " <b>Title</b>");
        a.setPublicationDate(date);
        a.setLink(new URL("http://a " + id + "/"));
        feed.appendArticle(a);
        return a;
    }

    // ------------------------------------------------------------------------
    // Basics
    // ------------------------------------------------------------------------

    /** Setting correct template text. */
    public void testSetText_Valid()
    {
        template.setText("a");
        assertEquals("a", template.getText());
    }

    /** Setting incorrect template text. */
    public void testSetText_Invalid()
    {
        try
        {
            template.setText("# if");
            fail("InvalidSyntaxException is expected");
        } catch (InvalidSyntaxException e)
        {
            // Expected
        }
    }

    // ------------------------------------------------------------------------
    // Rendering
    // ------------------------------------------------------------------------

    /** Rendering of the empty template. */
    public void testRenderTemplate_Empty()
    {
        template.setText(TEMPLATE_EMPTY);
        assertEquals("", template.render(singleArticle));
    }

    /** Rendering of a single-mode template. */
    public void testRenderTemplate_Single()
    {
        template.setText(TEMPLATE_SINGLE);
        String html = template.render(singleArticle);
        assertEquals(result(1), html);
    }

    /** Rendering of a multi-mode template. */
    public void testRenderTemplate_Multiple()
    {
        template.setText(TEMPLATE_MULTI);
        String html = template.render(multipleArticles);
        assertEquals(result(1, 2), html);
    }

    public void testRenderTemplate_SingleWithElse()
    {
        template.setText(
            "before\n" +
            "# if single article\n" +
            "single1\n" +
            "single2\n" +
            "# else\n" +
            "multiple\n" +
            "# endif\n" +
            "after");

        assertEquals("before\nsingle1\nsingle2\nafter\n", template.render(singleArticle));
    }

    public void testRenderTemplate_SingleWithoutElse()
    {
        template.setText(
            "before\n" +
            "# if single article\n" +
            "single1\n" +
            "single2\n" +
            "# endif\n" +
            "after");

        assertEquals("before\nsingle1\nsingle2\nafter\n", template.render(singleArticle));
    }

    public void testRenderTemplate_MultipleWithoutLoop()
    {
        template.setText(
            "before\n" +
            "# if single article\n" +
            "single\n" +
            "# else\n" +
            "multiple1\n" +
            "multiple2\n" +
            "# endif\n" +
            "after");

        assertEquals("before\nmultiple1\nmultiple2\nafter\n", template.render(multipleArticles));
    }

    public void testRenderTemplate_MultipleWithLoop()
    {
        template.setText(
            "before\n" +
            "# if single article\n" +
            "single\n" +
            "# else\n" +
            "# for each article\n" +
            "multiple1\n" +
            "multiple2\n" +
            "# endfor\n" +
            "# endif\n" +
            "after");

        assertEquals("before\nmultiple1\nmultiple2\nmultiple1\nmultiple2\nafter\n", template.render(multipleArticles));
    }

    /**
     * Returns the expected result of the template work.
     *
     * @param ids ids.
     *
     * @return result.
     */
    private Object result(int ... ids)
    {
        String res = "";

        for (int id : ids)
        {
            res += "PURL: http://a " + id + "/ PTitle: A" + id + " <b>Title</b> PText: A" + id + " <i>Text</i> PDate: Jan 1, 2008 " +
                   "FURL: http://f 1/ FTitle: F1 <b>Title</b>\n";
        }

        return res;
    }
}
