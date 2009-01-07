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
// $Id: TestAbstractPostEditor.java,v 1.1 2007/02/05 11:07:54 spyromus Exp $
//

package com.salas.bb.remixfeeds.editor;

import junit.framework.TestCase;

/**
 * Tests abstract Post editor.
 */
public class TestAbstractPostEditor extends TestCase
{    
    /** Replaces all entities with Unicode symbols. */
    public void testCleanHTML_deentitise()
    {
        String html = "&#1055;&#1088;&#1086;&#1095;&#1090;&#1080;&#1090;&#1077;";

        String t = AbstractPostEditor.cleanHTML(html);
        assertEquals(new String(new char[] { 
            (char)1055, (char)1088, (char)1086, (char)1095,
            (char)1090, (char)1080, (char)1090, (char)1077 }), t);
    }

    /** Removes all redundant tags. */
    public void testCleanHTML_kill_tags()
    {
        String html = "<html>\n" +
            "  <head>\n" +
            "    <style type=\"text/css\">\n" +
            "      <!--\n" +
            "        body { font-style: normal; font-weight: normal; font-family: Arial; font-variant: normal; font-size: 11pt; line-height: normal }\n" +
            "      -->\n" +
            "    </style>\n" +
            "        \n" +
            "  </head>\n" +
            "  <body>\n" +
            "abc\n" +
            "  </body>\n" +
            "</html>";

        assertEquals("abc", AbstractPostEditor.cleanHTML(html));
    }

    /** Removes leading spaces. */
    public void testCleanHTML_remove_leading_spaces()
    {
        String html = "  abc\n cde";

        assertEquals("abc cde", AbstractPostEditor.cleanHTML(html));
    }

    /** Replaces CRs with spaces. */
    public void testCleanHTML_remove_CRs()
    {
        String html = "a\nb";
        assertEquals("a b", AbstractPostEditor.cleanHTML(html));
    }

    /** Adds extra spaces before P and BR. */
    public void testCleanHTML_beautify_p_br()
    {
        String html = "<p>Test<br>is<br/>best</p><p>Testing</p>";
        assertEquals("<p>Test<br>\n" +
            "is<br/>\n" +
            "best</p>\n" +
            "\n" +
            "<p>Testing</p>", AbstractPostEditor.cleanHTML(html));
    }

    /** Adds spaces and break lines for lists. */
    public void testCleanHTL_beautify_lists()
    {
        String html = "the list<ul><li>one<li>two</li></ul><ol><li>three</li><li>four</ol>";
        assertEquals("the list\n" +
            "\n" +
            "<ul>\n" +
            " <li>one\n" +
            " <li>two</li>\n" +
            "</ul>\n" +
            "\n" +
            "<ol>\n" +
            " <li>three</li>\n" +
            " <li>four\n" +
            "</ol>",
            AbstractPostEditor.cleanHTML(html));
    }
}
