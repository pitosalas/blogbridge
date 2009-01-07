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
// $Id: Parser.java,v 1.2 2006/10/18 08:00:04 spyromus Exp $
//

package com.salas.bb.views.stylesheets.parser;

import com.salas.bb.views.stylesheets.domain.Rule;
import com.salas.bb.views.stylesheets.domain.IRule;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Stylesheet parser.
 */
public class Parser
{
    /** The registry of handlers. */
    private static final Map HANDLERS = new HashMap()
    {
        {
            put("font", new FontPropertyHandler());
            put("color", new ColorPropertyHandler());
            put("icon", new IconPropertyHandler());
        }
    };
    private static final Pattern PAT_PROPS = Pattern.compile("([^:\\s]+)\\s*:\\s*([^;]+)\\s*(;|$)");
    private static final Pattern PAT_BLOCKS = Pattern.compile("([^\\s]+)*\\s+\\{([^\\}]+)}");
    private static final Pattern PAT_SIGNATURE = Pattern.compile("^\\s*([\\p{Alnum}~]+)?(\\.(\\p{Alnum}+))?\\s*$");

    /**
     * Parses text into the map of elements to maps of classes to rules.
     *
     * @param text text.
     *
     * @return map.
     */
    public static Map parse(String text)
    {
        Map rules = new HashMap();

        if (text != null)
        {
            Map signToRules = parseRules(text);

            Set entries = signToRules.entrySet();
            for (Iterator it = entries.iterator(); it.hasNext();)
            {
                Map.Entry entry = (Map.Entry)it.next();

                String signature = (String)entry.getKey();
                IRule rule = (IRule)entry.getValue();

                setRule(rules, signature, rule);
            }
        }

        return rules;
    }

    /**
     * Parses <code>signature</code> and adds the <code>rule</code> to the
     * corresponding place in <code>rules</code> map.
     *
     * @param rules     target map.
     * @param signature signature to parse.
     * @param rule      rule to set.
     */
    static void setRule(Map rules, String signature, IRule rule)
    {
        Matcher m = PAT_SIGNATURE.matcher(signature);
        if (m.find())
        {
            String el = m.group(1);
            String cl = m.group(3);

            if (el != null && (el.length() == 0 || el.equals("~"))) el = null;
            if (cl != null && cl.length() == 0) cl = null;

            Map classes = (Map)rules.get(el);
            if (classes == null)
            {
                classes = new HashMap();
                rules.put(el, classes);
            }

            classes.put(cl, rule);
        }
    }

    /**
     * Parses rules text into the map of signatures to rules.
     *
     * @param text text.
     *
     * @return rules map.
     */
    static Map parseRules(String text)
    {
        Map rules = new HashMap();

        if (text != null)
        {
            Matcher matBlocks = PAT_BLOCKS.matcher(text);
            while (matBlocks.find())
            {
                String signature = matBlocks.group(1);
                String props = matBlocks.group(2);

                rules.put(signature, parseRule(props));
            }
        }

        return rules;
    }

    /**
     * Parses properties of the rule and create rule object.
     *
     * @param props properties.
     *
     * @return rule.
     */
    private static IRule parseRule(String props)
    {
        Rule rule = new Rule();

        Matcher m = PAT_PROPS.matcher(props);
        while (m.find())
        {
            String prop = m.group(1);
            String value = m.group(2);

            IPropertyHandler handler = (IPropertyHandler)HANDLERS.get(prop);
            if (handler != null) handler.handle(rule, value);
        }

        return rule;
    }
}
