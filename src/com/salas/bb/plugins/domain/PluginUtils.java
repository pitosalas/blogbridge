// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2007 by R. Pito Salas
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
// $Id: PluginUtils.java,v 1.2 2007/08/24 06:08:32 spyromus Exp $
//

package com.salas.bb.plugins.domain;

import com.salas.bb.utils.StringUtils;
import org.jdom.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract plug-in supporting loading properties from the plug-in element.
 */
public abstract class PluginUtils
{
    /**
     * Takes the properties from the XML element.
     *
     * @param element   plug-in element.
     *
     * @return map of property names to property values.
     */
    protected static Map<String, String> getPluginProperties(Element element)
    {
        // Get parameters
        Map<String, String> props = new HashMap<String, String>();

        List properties = element.getChildren("property");
        for (Object o : properties)
        {
            Element propertyEl = (Element)o;
            String name = propertyEl.getAttributeValue("name");
            String value = propertyEl.getAttributeValue("value");

            if (StringUtils.isNotEmpty(name)) props.put(name, value);
        }

        return props;
    }
}
