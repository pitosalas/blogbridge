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
// $Id: IValueEditor.java,v 1.4 2006/01/08 05:13:00 kyank Exp $
//

package com.salas.bb.views.querybuilder.editors;

import java.awt.*;

/**
 * Value editor interface. Each value editor has corresponding type of property it is
 * ready to handle. Each editor accepts string value and ready to return it.
 */
interface IValueEditor
{
    /**
     * Returns the component to be used for displaying the editor.
     *
     * @return component.
     */
    Component getVisualComponent();
}
