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
// $Id: ThemePlugin.java,v 1.5 2007/04/02 14:49:56 spyromus Exp $
//

package com.salas.bb.plugins.domain;

import com.salas.bb.utils.StringUtils;
import com.salas.bb.views.themes.ThemeSupport;
import org.jdom.Element;

import java.net.URL;

/**
 * Theme plug-in.
 */
public class ThemePlugin extends AbstractPlugin
{
    private final URL themeURL;

    /**
     * Creates a theme plug-in wrapping a given theme resource.
     *
     * @param themeURL theme.
     */
    private ThemePlugin(URL themeURL)
    {
        super("Theme", null);
        this.themeURL = themeURL;
    }

    /**
     * Parses XML element and initializes the plug-in instance with
     * theme resource URL.
     *
     * @param element   element to parse.
     * @param loader    the class loader to use for the resource access.
     *
     * @return plug-in.
     *
     * @throws LoaderException in case if some of the data provided is incorrect.
     */
    public static IPlugin create(Element element, ClassLoader loader)
        throws LoaderException
    {
        String file = element.getAttributeValue("file");
        if (StringUtils.isEmpty(file)) throw new LoaderException("Filename must be specified");

        URL themeURL = loader.getResource(file);
        if (themeURL == null) throw new LoaderException("Theme file doesn't exist");

        return new ThemePlugin(themeURL);
    }

    /** Initializes plug-in. */
    public void initialize()
    {
        ThemeSupport.addTheme(themeURL);
    }

    /**
     * Compares the plugin to another one.
     *
     * @param o other plugin.
     *
     * @return <code>TRUE</code> if are the same.
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThemePlugin that = (ThemePlugin)o;
        return themeURL.toString().equals(that.themeURL.toString());
    }

    /**
     * Returns the hash code.
     *
     * @return code.
     */
    public int hashCode()
    {
        return themeURL.toString().hashCode();
    }
}
