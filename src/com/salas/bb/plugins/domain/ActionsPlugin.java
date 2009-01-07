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
// $Id: ActionsPlugin.java,v 1.2 2007/04/11 10:07:47 spyromus Exp $
//

package com.salas.bb.plugins.domain;

import com.jgoodies.uif.action.ActionManager;
import com.jgoodies.uif.application.Application;
import org.jdom.Element;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Actions plug-in re-assigns actions.
 */
public class ActionsPlugin extends ResourcesPlugin
{
    /**
     * Creates actions plug-in.
     *
     * @param element   element.
     * @param loader    loader.
     */
    public ActionsPlugin(Element element, ClassLoader loader)
    {
        super(getType(element, "Actions", "Keys", "Actions", "Icons", "Messages"), element, loader);
    }

    @Override
    protected ResourceBundle getParentBundle()
    {
        String path = Application.getConfiguration().getActionsBundlePath();
        return ResourceBundle.getBundle(path, Locale.getDefault(),
            ActionManager.class.getClassLoader());
    }

    @Override
    protected void overrideBundle(ResourceBundle bundle)
    {
        ActionManager.setBundle(bundle);
    }
}
