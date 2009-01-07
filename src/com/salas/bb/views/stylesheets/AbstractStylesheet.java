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
// $Id: AbstractStylesheet.java,v 1.5 2007/10/01 17:03:27 spyromus Exp $
//

package com.salas.bb.views.stylesheets;

import com.salas.bb.views.stylesheets.domain.IRule;
import com.salas.bb.views.stylesheets.domain.Rule;
import com.salas.bb.views.stylesheets.loader.DirectLoader;
import com.salas.bb.views.stylesheets.loader.ILoader;
import com.salas.bb.views.stylesheets.parser.Parser;
import com.salas.bb.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * The stylesheet that knows how to update itself.
 */
abstract class AbstractStylesheet implements IStylesheet
{
    /** Default font, color and icon */
    private static final IRule EMPTY_RULE = new Rule();

    /** The map [element_name:map[class:rule]] */
    private Map rules = new HashMap();

    /** Default loader. */
    private static final ILoader DEFAULT_LOADER = new DirectLoader();

    /**
     * Checks for updates of the stylesheet and prerequisites.
     *
     * @throws IOException if loading failed.
     */
    public void update()
        throws IOException
    {
        String newSS = getUpdatedStylesheet();
        if (newSS != null) applyChanges(newSS);
    }

    /**
     * Gets updated stylesheet.
     *
     * @return new stylesheet or <code>NULL</code> if nothing changed.
     *
     * @throws IOException if loading failed.
     */
    protected abstract String getUpdatedStylesheet() throws IOException;

    /**
     * Parses the stylesheet and applies.
     *
     * @param newSS new stylesheet text.
     */
    protected void applyChanges(String newSS)
    {
        rules = Parser.parse(newSS);
    }

    /**
     * Returns the font for the element with the given set of classes.
     *
     * @param el        element.
     * @param classes   classes.
     *
     * @return the font object or <code>NULL</code> if the default should be used.
     */
    public Font getFont(String el, String[] classes)
    {
        return getRule(el, classes).getFont();
    }

    /**
     * Returns the color for the element with the given set of classes.
     *
     * @param el        element.
     * @param classes   classes.
     *
     * @return the color or <code>NULL</code> if the default should be used.
     */
    public Color getColor(String el, String[] classes)
    {
        return getRule(el, classes).getColor();
    }

    /**
     * Returns the icon for the element with the given set of classes.
     *
     * @param el        element.
     * @param classes   classes.
     *
     * @return the icon or <code>NULL</code> if the default should be used.
     *
     * @throws IOException if loading of icon failed.
     */
    public Icon getIcon(String el, String[] classes)
        throws IOException
    {
        IRule rule = getRule(el, classes);
        return rule != null ? rule.getIcon() : null;
    }

    /**
     * Returns base URL of this stylesheet to resolveURI relative icon addresses (can be <code>NULL</code>).
     *
     * @return base URL.
     */
    protected URL getStylesheetBaseURL()
    {
        return null;
    }

    /**
     * Returns the loader to use.
     *
     * @return loader.
     */
    protected ILoader getLoader()
    {
        return DEFAULT_LOADER;
    }

    // ----------------------------------------------------------------------------------
    // Rules map methods
    // ----------------------------------------------------------------------------------

    /**
     * Returns the consolidated rule for the given set of classes.
     *
     * @param el        element.
     * @param classes   classes.
     *
     * @return rule.
     */
    public IRule getRule(String el, String[] classes)
    {
        IRule rule;

        if (classes == null || classes.length == 0)
        {
            rule = getRule(el);
        } else
        {
            IRule rootClass;
            IRule elemDef, elemClass;

            Map rootClasses = getElementClasses(null, false);

            // Get root rules
            rule = getRule(rootClasses, null);
            for (int i = 0; i < classes.length; i++)
            {
                rootClass = getRule(rootClasses, classes[i]);
                rule = rule.overrideWith(rootClass);
            }

            // Get element rules
            if (el != null)
            {
                Map elemClasses = getElementClasses(el, false);
                elemDef = getRule(elemClasses, null);
                rule = rule.overrideWith(elemDef);
                for (int i = 0; i < classes.length; i++)
                {
                    elemClass = getRule(elemClasses, classes[i]);
                    rule = rule.overrideWith(elemClass);
                }
            }
        }

        return rule == null ? null : new AutoImageRule(rule);
    }

    /**
     * Gets no-class rule.
     *
     * @param el element.
     *
     * @return rule.
     */
    private IRule getRule(String el)
    {
        IRule rule;

        Map rootClasses = getElementClasses(null, false);

        // Get root rules
        rule = getRule(rootClasses, null);

        // Get element rules
        if (el != null)
        {
            Map elemClasses = getElementClasses(el, false);
            rule = rule.overrideWith(getRule(elemClasses, null));
        }

        return rule;
    }

    /**
     * Takes the rule from the classes. If classes are not defined, or the rule isn't set,
     * the result will be <code>EMPTY_RULE</code>.
     *
     * @param classes   classes map for some element.
     * @param clazz     class name.
     *
     * @return rule.
     */
    private IRule getRule(Map classes, String clazz)
    {
        IRule rule = null;
        if (classes != null)
        {
            rule = (IRule)classes.get(clazz);
        }

        return rule == null ? EMPTY_RULE : rule;
    }

    /**
     * Returns the element classes to rules map.
     *
     * @param el                element.
     * @param createIfMissing   when <code>TRUE</code> and the map is missing, it's created.
     *
     * @return map.
     */
    private Map getElementClasses(String el, boolean createIfMissing)
    {
        Map m = (Map)rules.get(el);

        if (m == null && createIfMissing)
        {
            m = new HashMap();
            rules.put(el, m);
        }

        return m;
    }

    // ----------------------------------------------------------------------------------
    // Loading icons
    // ----------------------------------------------------------------------------------

    private final Map cachedIcons = new HashMap();

    private Icon loadIcon(String url)
    {
        if (StringUtils.isEmpty(url)) return null;
        url = url.trim();

        // Check the icons cache first
        Icon icon;
        synchronized (cachedIcons)
        {
            icon = (Icon)cachedIcons.get(url);
        }

        // If there's no icon yet in the cache, load it
        if (icon == null)
        {
            try
            {
                icon = getLoader().loadIcon(getStylesheetBaseURL(), url);
                synchronized (cachedIcons)
                {
                    cachedIcons.put(url, icon);
                }
            } catch (IOException e)
            {
                icon = null;
            }
        }

        return icon;
    }

    /**
     * Resets the cache.
     */
    public void resetIconsCache()
    {
        synchronized (cachedIcons)
        {
            cachedIcons.clear();
        }
    }

    // ----------------------------------------------------------------------------------
    // Convenience classes
    // ----------------------------------------------------------------------------------

    /**
     * The automatic image loading rule.
     */
    private class AutoImageRule implements IRule
    {
        private IRule subrule;

        /**
         * Creates a rule.
         *
         * @param subrule sub-rule.
         */
        public AutoImageRule(IRule subrule)
        {
            this.subrule = subrule;
        }


        /**
         * Returns the font.
         *
         * @return font.
         */
        public Font getFont()
        {
            return subrule == null ? null : subrule.getFont();
        }

        /**
         * Returns the color.
         *
         * @return color.
         */
        public Color getColor()
        {
            return subrule == null ? null : subrule.getColor();
        }

        /**
         * Returns the icon URL.
         *
         * @return icon URL.
         */
        public String getIconURL()
        {
            return subrule == null ? null : subrule.getIconURL();
        }

        /**
         * Creates an overriden copy of itself.
         *
         * @param rule rule to override with.
         *
         * @return rule.
         */
        public IRule overrideWith(IRule rule)
        {
            if (subrule == null) subrule = rule; else subrule.overrideWith(rule);
            return this;
        }

        /**
         * Returns icon from cached storage.
         *
         * @return icon.
         */
        public Icon getIcon()
        {
            Icon icon = subrule.getIcon();

            if (icon == null)
            {
                String url = subrule.getIconURL();
                if (url != null)
                {
                    icon = loadIcon(url);
                    subrule.setIcon(icon);
                }
            }

            return icon;
        }

        /**
         * Sets icon for cached storage.
         *
         * @param icon icon.
         */
        public void setIcon(Icon icon)
        {
        }
    }
}
