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
// $Id: StringsPlugin.java,v 1.1 2007/06/27 08:22:08 spyromus Exp $
//

package com.salas.bb.plugins.domain;

import com.salas.bb.utils.i18n.Strings;
import org.jdom.Element;

import java.util.ResourceBundle;

/**
 * Strings plug-in re-assigns text messages.
 */
public class StringsPlugin extends ResourcesPlugin
{
    /**
     * Creates a strings plug-in.
     *
     * @param element   element.
     * @param loader    loader.
     */
    public StringsPlugin(Element element, ClassLoader loader)
    {
        super(getType(element, "Strings", "Messages", "Strings"), element, loader);
    }

    @Override
    protected ResourceBundle getParentBundle()
    {
        return Strings.getStringsBundle();
    }

    @Override
    protected void overrideBundle(ResourceBundle bundle)
    {
        Strings.setStringsBundle(bundle);
    }
}