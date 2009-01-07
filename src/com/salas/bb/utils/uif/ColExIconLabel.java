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
// $Id: ColExIconLabel.java,v 1.2 2006/01/08 05:07:31 kyank Exp $
//

package com.salas.bb.utils.uif;

import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.utils.ResourceID;

import javax.swing.*;

/**
 * Icon label with two states: a collapsed and expanded.
 */
public class ColExIconLabel extends IconLabel
{
    private static final Icon ICON_COLLAPSED;
    private static final Icon ICON_COLLAPSED_OVER;
    private static final Icon ICON_EXPANDED;
    private static final Icon ICON_EXPANDED_OVER;

    /**
     * Static initialization of icons.
     */
    static
    {
        ICON_COLLAPSED = ResourceUtils.getIcon(ResourceID.ICON_GROUP_COLLAPSED);
        ICON_COLLAPSED_OVER = ResourceUtils.getIcon(ResourceID.ICON_GROUP_COLLAPSED_OVER);
        ICON_EXPANDED = ResourceUtils.getIcon(ResourceID.ICON_GROUP_EXPANDED);
        ICON_EXPANDED_OVER = ResourceUtils.getIcon(ResourceID.ICON_GROUP_EXPANDED_OVER);
    }

    /**
     * Creates icon label in collapsed state.
     */
    public ColExIconLabel()
    {
        super(ICON_COLLAPSED, ICON_COLLAPSED_OVER);
    }

    /**
     * Changes icons according to the state.
     *
     * @param collapsed <code>TRUE</code> for collapsed state.
     */
    public void setCollapsed(boolean collapsed)
    {
        setIcons(collapsed ? ICON_COLLAPSED : ICON_EXPANDED,
            collapsed ? ICON_COLLAPSED_OVER : ICON_EXPANDED_OVER);
    }
}
