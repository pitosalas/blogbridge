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
// $Id: IPlugin.java,v 1.4 2007/04/05 14:51:20 spyromus Exp $
//

package com.salas.bb.plugins.domain;

/**
 * Plug-in interface. The typical sequence of calls is.
 *
 * <ul>
 *   <li>Creation</li>
 *   <li>setParameters() - called to let the plug-in know of the parameters even between the initialization.</li>
 *   <li>initialize() - initialization of plug-in. Only enabled plug-ins are initialized during lading.</li>
 * </ul>
 */
public interface IPlugin
{
    /**
     * Returns the name of plug-in type (Theme, Actions ...).
     *
     * @return the name of plug-in type.
     */
    String getTypeName();

    /** Initializes plug-in. */
    void initialize();
}
