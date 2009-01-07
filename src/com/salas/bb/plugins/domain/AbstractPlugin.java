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
// $Id: AbstractPlugin.java,v 1.2 2007/04/05 14:51:20 spyromus Exp $
//

package com.salas.bb.plugins.domain;

import java.util.Map;

/**
 * Abstract implementation of plug-in interface.
 */
public abstract class AbstractPlugin implements IPlugin
{
    private final String type;
    private final ClassLoader loader;

    /**
     * Creates a plug-in of a given type.
     *
     * @param type type.
     * @param loader class-loader to remember.
     */
    protected AbstractPlugin(String type, ClassLoader loader)
    {
        this.type = type;
        this.loader = loader;
    }

    /**
     * Returns the class loader of the plug-in package.
     *
     * @return loader/
     */
    public ClassLoader getLoader()
    {
        return loader;
    }

    /**
     * Returns the name of plug-in type (Theme, Actions ...).
     *
     * @return the name of plug-in type.
     */
    public String getTypeName()
    {
        return type;
    }

    /**
     * Sets the parameters before the initialization.
     *
     * @param params parameters.
     */
    public void setParameters(Map<String, String> params)
    {
        // The default implementation does nothing
    }
}
