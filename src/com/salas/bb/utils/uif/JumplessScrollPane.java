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
// $Id: JumplessScrollPane.java,v 1.1 2007/06/22 11:34:26 spyromus Exp $
//

package com.salas.bb.utils.uif;

import javax.swing.*;
import java.awt.*;

/**
 * Scrollpane that uses an extended viewport that records the position
 * to avoid flicks and jumps during the view update.
 */
public class JumplessScrollPane extends JScrollPane
{
    /**
     * Creates a scroll pane for the view.
     *
     * @param view view.
     */
    public JumplessScrollPane(Component view)
    {
        super(view);
    }

    @Override
    protected JViewport createViewport()
    {
        return new JumplessViewport();
    }
}
