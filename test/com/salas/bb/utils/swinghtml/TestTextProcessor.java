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
// $Id: TestTextProcessor.java,v 1.12 2008/04/04 14:03:27 spyromus Exp $
//

package com.salas.bb.utils.swinghtml;

import com.salas.bb.utils.Constants;
import junit.framework.TestCase;

/**
 * This suite contains tests for <code>TextProcessor</code> unit.
 * It covers:
 * TODO put list
 */
public class TestTextProcessor extends TestCase
{
    /**
     * Tests conversion of HTML entities in the text.
     */
    public void testConvertEntities()
    {
        assertEquals("& &   ' '\" \"",
            TextProcessor.convertHTMLEntities("&amp &amp;&nbsp &nbsp;&apos &apos;&quot &quot;"));

        assertEquals("&", TextProcessor.convertHTMLEntities("&amp"));
        assertEquals("&", TextProcessor.convertHTMLEntities("&amp;"));
        assertEquals("'", TextProcessor.convertHTMLEntities("&apos"));
        assertEquals("'", TextProcessor.convertHTMLEntities("&apos;"));
        assertEquals(" ", TextProcessor.convertHTMLEntities("&nbsp"));
        assertEquals(" ", TextProcessor.convertHTMLEntities("&nbsp;"));
        assertEquals("\"", TextProcessor.convertHTMLEntities("&quot"));
        assertEquals("\"", TextProcessor.convertHTMLEntities("&quot;"));
        assertEquals("", TextProcessor.convertHTMLEntities(""));

        assertNull("NULL should be returned.", TextProcessor.convertHTMLEntities(null));
    }

    /**
     * Tests conversion of some numeric entities into strings.
     */
    public void testConvertNumericEntities()
    {
        assertEquals("Apple's and ' Shmapples'",
            TextProcessor.convertNumericHTMLEntities("Apple&#0039;s and &#39 Shmapples&#X27"));
        assertEquals("\\", TextProcessor.convertNumericHTMLEntities("&#92;"));
        assertEquals("$", TextProcessor.convertNumericHTMLEntities("&#36;"));

        assertNull("NULL should be returned.", TextProcessor.convertNumericHTMLEntities(null));
    }

    /**
     * Tests removing of HTML entities from text.
     */
    public void testRemoveHTMLEntities()
    {
        assertEquals("Entities weren't remove.",
            "a b c", TextProcessor.removeHTMLEntities("a&amp;b&nbsp;c"));

        assertNull("NULL should be returned.", TextProcessor.removeHTMLEntities(null));
    }

    /**
     * Tests removing of tags from the text.
     */
    public void testRemoveTags()
    {
        assertEquals("Tags weren't removed.",
            "abc", TextProcessor.removeTags("<p>a<br/><i >b</i>c</p>"));

        assertNull("NULL should be returned.", TextProcessor.removeTags(null));
    }

    /**
     * Tests getting the excerpt from the text.
     */
    public void testGetExcerpt()
    {
        assertEquals("First words should be selected",
            "a bb, c.", TextProcessor.getExcerpt("a bb, c. d e", 4));
        assertEquals("First words should be selected",
            "a b, c!", TextProcessor.getExcerpt(" a b, c! d e", 4));
        assertEquals("First words should be selected",
            "[Quote] a b, c?", TextProcessor.getExcerpt(" [Quote] a b, c? d e", 5));

        assertEquals("First words on the second line should be selected.",
            "a b c...", TextProcessor.getExcerpt("\na b c d\n e", 3));

        assertEquals("First line is empty.",
            "a b...", TextProcessor.getExcerpt("\na b\n c d e", 3));

        assertEquals("First line is empty.",
            "a b", TextProcessor.getExcerpt("\na b", 3));

        // No text
        assertNull("No text given.", TextProcessor.getExcerpt(null, 3));

        // No words
        assertEquals("No words in the text.", "", TextProcessor.getExcerpt("", 3));
        assertEquals("No words in the text.", "", TextProcessor.getExcerpt("\n", 3));

        // Using excerpt from the text, but skipping tags and converting entities
        assertEquals("Art Mobs: \"If a paintings could speak, what would they...",
            TextProcessor.getExcerpt("Art Mobs: \"If a paintings could speak, " +
            "what would they say?\"", 10));

        // One word
        assertEquals("The word should be returned.",
            "First", TextProcessor.getExcerpt("First", 3));
    }

    /**
     * Tests how the text is filtered from the garbage and undesired staff.
     * Target: inline scripts.
     */
    public void testFilterTextScripts()
    {
        assertEquals("test", TextProcessor.filterText("te<script>\n" +
            "<!--\nalert('test');\n--></script>st"));

        assertEquals("", TextProcessor.filterText(
            "<P><SPAN lang=EN-US>\n" +
            "<SCRIPT>\n" +
            "<!--\n" +
            "D(['mb','The',1]\n" +
            ");\n" +
            "\n" +
            "<a href='//'>//</a>-->\n" +
            "</SCRIPT>\n" +
            "</SPAN>"));
    }

    /**
     * Tests how the text is filtered from the garbage and undesired staff.
     * Target: tags and entities.
     */
    public void testFilterTextConversions()
    {
        assertEquals(null, TextProcessor.filterText(null));

        // Extra spaces trimming
        assertEquals("test", TextProcessor.filterText(" test "));

        // Conversion of STRONG into B tags
        assertEquals("<b>test</b>", TextProcessor.filterText("<strong>test</strong>"));

        // Removing extra P tags
        assertEquals("test", TextProcessor.filterText("<p> <p> <p/>test</p>"));

        // Handling empty tags (BR and IMG in our case)
        assertEquals("test<br>", TextProcessor.filterText("<p>test<br/>"));
        assertEquals("test<img src='aaa'>", TextProcessor.filterText("<p>test<img src='aaa'/>"));

        // Converting numeric HTML entities to text and handling known HTML entities
        assertEquals("test's", TextProcessor.filterText("test&#39;s"));
        assertEquals("test&nbsp;&amp;&lt;&gt;&apos;&quot;",
            TextProcessor.filterText("test&nbsp;&amp;&lt;&gt;&apos;&quot;"));
    }

    /**
     * Tests converting unknown entities into Unicode chars.
     */
    public void testFilterTextEntities()
    {
        assertEquals("\u2014 \u2014 \u2014", TextProcessor.filterText("&mdash; &mdash &mdash"));
    }

    /**
     * Tests filtering the title.
     */
    public void testFilterTitle()
    {
        // Converting HTML entities to plain text
        assertEquals("a b", TextProcessor.filterTitle("a&nbsp;b", null));
        // Skipping unknown entities
        assertEquals("Q&A:", TextProcessor.filterTitle("Q&A:", null));
        // Incorrect numeric HTML entity -- kipping
        assertEquals("test&12;1", TextProcessor.filterTitle("test&12;1", null));
        // Extra long (more than 6 chars) HTML entity -- skipping
        assertEquals("test&qwertyu;1", TextProcessor.filterTitle("test&qwertyu;1", null));

        // Converting numeric HTML entities to string
        assertEquals("Apple's", TextProcessor.filterTitle("Apple&#39;s", null));

        // Converting unknown, but looking valid HTML entities to space
        assertEquals("test 1", TextProcessor.filterTitle("test&nb;1", null));
        assertEquals("test 1", TextProcessor.filterTitle("test&qwerty;1", null));

        // Using excerpt from the text, but skipping tags and converting entities
        assertEquals("Art Mobs: \"If a paintings could speak, what would they...",
            TextProcessor.filterTitle(null, "<a>Art Mobs</a>: \"If a paintings could speak, " +
            "what would they say?\""));
    }

    /**
     * Tests filtering of the title when there's not title, but there's text.
     */
    public void testFilterTitleNull()
    {
        String textString;
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < Constants.WORDS_IN_EXCERPT; i++) buf.append(i).append(" ");

        // Prepare the look of the title
        String ethalon = buf.toString();
        ethalon = ethalon.substring(0, ethalon.length() - 1) + "...";

        // Add some more "words" to mess things up
        buf.append("a b c");
        textString = buf.toString();

        assertEquals("Title should be taken from excerpt.",
            ethalon, TextProcessor.filterTitle(null, textString));

        assertEquals("Title should be taken from excerpt.",
            null, TextProcessor.filterTitle(null, null));
    }

    /**
     * Tests removing buggy background attribute where there's no actual link specified.
     */
    public void testRemoveBackgroundAttr()
    {
        assertEquals("BACKGROUND should be removed", "<i >a</i>",
            TextProcessor.processHTML("<i background=''>a</i>", -1));
        assertEquals("BACKGROUND should be removed", "<i >a</i>",
            TextProcessor.processHTML("<i background = ' '>a</i>", -1));
        assertEquals("BACKGROUND should be removed", "<i >a</i>",
            TextProcessor.processHTML("<i bAckGroUnd=\" \">a</i>", -1));

        assertEquals("BACKGROUND should stay", "<i BACKGROUND='a'>a</i>",
            TextProcessor.processHTML("<i BACKGROUND='a'>a</i>", -1));
    }

    /**
     * Tests removing heading paragraph signs.
     */
    public void testRemoveLeadingParagraphs()
    {
        assertEquals("a", TextProcessor.removeLeadingParagraphs("<p>a"));
        assertEquals("a", TextProcessor.removeLeadingParagraphs(" <p> a"));
        assertEquals("a<p>", TextProcessor.removeLeadingParagraphs(" <p> a<p>"));
        assertEquals("a<p>", TextProcessor.removeLeadingParagraphs("<p> <p> a<p>"));
    }

    /**
     * Tests processing text like this: "&lt;p&gt;&lt;strong&gt;Some&lt;/strong&gt; text".
     * Those STRONG tags should be converted into B tags.
     */
    public void testProcessingStrongText()
    {
        assertEquals("<b>Some</b> text", TextProcessor.processHTML("<strong>Some</strong> text", -1));
        assertEquals("<p><b>Some</b> text", TextProcessor.processHTML("<p><strong>Some</strong> text", -1));
    }

    /**
     * Tests converting entities to plain text.
     */
    public void testToPlainText()
    {
        assertEquals(" ", TextProcessor.toPlainText("&nbsp;"));
        assertEquals("<", TextProcessor.toPlainText("&lt;"));
        assertEquals(">", TextProcessor.toPlainText("&gt;"));
        assertEquals("\"", TextProcessor.toPlainText("&quot;"));
        assertEquals("'", TextProcessor.toPlainText("&apos;"));
        assertEquals("&", TextProcessor.toPlainText("&amp;"));
    }

    /**
     * Tests converting entities to plain text.
     */
    public void testToPlainTextComposite()
    {
        assertEquals("a b<c", TextProcessor.toPlainText("a&nbsp;b&lt;c"));
    }
}
