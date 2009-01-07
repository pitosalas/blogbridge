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
// $Id: IScrollContoller.java,v 1.2 2006/01/08 05:12:59 kyank Exp $
//

package com.salas.bb.views.feeds;

import java.awt.*;

/**
 * Marker interfaces which is used by scrollable component of composite feed
 * display and particlular displays to transfer the commands to scroll display
 * somewhere. The scrollable component blocks all usual scroll requests because
 * scroll pane usually does lots of undesired moves. So the component accepts
 * only scroll requests through this interface from the displays it represents.
 */
public interface IScrollContoller
{
    /**
     * Scroll the view to show given rectangle.
     *
     * @param aRect rectangle.
     */
    void scrollTo(Rectangle aRect);
}
