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
// $Id: TestParser.java,v 1.1 2006/10/16 08:40:12 spyromus Exp $
//

package com.salas.bb.views.stylesheets.parser;

import junit.framework.TestCase;

import java.awt.*;
import java.util.Map;

import com.salas.bb.views.stylesheets.domain.Rule;
import com.salas.bb.views.stylesheets.domain.IRule;

/**
 * Tests parser.
 */
public class TestParser extends TestCase
{
    /** Empty stylesheet. */
    public void testParseRules_Empty()
    {
        assertEquals(0, Parser.parseRules("").size());
    }

    /** NULL stylesheet. */
    public void testParseRules_NULL()
    {
        assertEquals(0, Parser.parseRules(null).size());
    }

    /** Default rule defined. */
    public void testParseRules_Default()
    {
        String text = "~ { font: bold; }";
        Map rules = Parser.parseRules(text);

        assertEquals(1, rules.size());

        IRule rule = (IRule)rules.get("~");
        assertNotNull(rule);
        assertTrue(rule.getFont().isBold());
    }

    /** Element rule defined. */
    public void testParseRules_Element()
    {
        String text = "el { font: bold; }";
        Map rules = Parser.parseRules(text);

        assertEquals(1, rules.size());

        IRule rule = (IRule)rules.get("el");
        assertNotNull(rule);
        assertTrue(rule.getFont().isBold());
    }

    /** Element with class defined. */
    public void testParseRules_ElementAndClass()
    {
        String text = "el.cl { font: bold; }";
        Map rules = Parser.parseRules(text);

        assertEquals(1, rules.size());

        IRule rule = (IRule)rules.get("el.cl");
        assertNotNull(rule);
        assertTrue(rule.getFont().isBold());
    }

    /** Class-only defined. */
    public void testParseRules_Class()
    {
        String text = ".cl { font: bold; }";
        Map rules = Parser.parseRules(text);

        assertEquals(1, rules.size());

        IRule rule = (IRule)rules.get(".cl");
        assertNotNull(rule);
        assertTrue(rule.getFont().isBold());
    }

    /** Default rule, element, class, and element with class are defined. */
    public void testParseRules_CompleteCombination()
    {
        String text =
            "~ { color: #000001; }\n" +
            "el { color: #000002; }\n" +
            ".cl { color: #000003; }\n" +
            "el.cl { color: #000004; }";
        Map rules = Parser.parseRules(text);

        assertEquals(4, rules.size());

        IRule rule;
        rule = (IRule)rules.get("~");
        assertTrue(rule.getColor().equals(Color.decode("#000001")));

        rule = (IRule)rules.get("el");
        assertTrue(rule.getColor().equals(Color.decode("#000002")));

        rule = (IRule)rules.get(".cl");
        assertTrue(rule.getColor().equals(Color.decode("#000003")));

        rule = (IRule)rules.get("el.cl");
        assertTrue(rule.getColor().equals(Color.decode("#000004")));
    }

    /** Complete parse test. */
    public void testParse_Complete()
    {
        String text =
            "\n" +
            " ~ {\n" +
            "\tcolor: #000001;\n" +
            "}\n" +
            "\n" +
            "e1\n" +
            "{ color : #000002; font:bold; }\n" +
            "\n" +
            "e2.c1 { color :#000003} .c2 { icon: url(theme/icon.gif )}";

        Map rules = Parser.parse(text);

        assertEquals(3, rules.size());

        Rule rule;
        rule = (Rule)((Map)rules.get(null)).get(null);
        assertTrue(rule.getColor().equals(Color.decode("#000001")));
        assertNull(rule.getFont());
        assertNull(rule.getIconURL());

        rule = (Rule)((Map)rules.get("e1")).get(null);
        assertTrue(rule.getColor().equals(Color.decode("#000002")));
        assertTrue(rule.getFont().isBold());
        assertNull(rule.getIconURL());

        rule = (Rule)((Map)rules.get("e2")).get("c1");
        assertTrue(rule.getColor().equals(Color.decode("#000003")));
        assertNull(rule.getFont());
        assertNull(rule.getIconURL());

        rule = (Rule)((Map)rules.get(null)).get("c2");
        assertNull(rule.getColor());
        assertNull(rule.getFont());
        assertEquals("theme/icon.gif", rule.getIconURL());
    }
}
