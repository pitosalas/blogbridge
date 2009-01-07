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
// $Id: BlockImageAction.java,v 1.1 2008/02/22 12:23:37 spyromus Exp $
//

package com.salas.bb.imageblocker;

import com.salas.bb.core.GlobalController;
import com.salas.bb.views.mainframe.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * Shows the URL of an image and offers to add it to the list of blocked images.
 */
public class BlockImageAction extends AbstractAction
{
    private static BlockImageAction instance;
    private static URL url;

    /**
     * Singleton hidden constructor.
     */
    private BlockImageAction()
    {
        setEnabled(false);
    }

    public static synchronized BlockImageAction getInstance()
    {
        if (instance == null) instance = new BlockImageAction();
        return instance;
    }

    /**
     * Invoked when action is performed.
     *
     * @param event event.
     */
    public void actionPerformed(ActionEvent event)
    {
        if (url == null) return;

        MainFrame frame = GlobalController.SINGLETON.getMainFrame();
        ImageBlockerDialog dialog = new ImageBlockerDialog(frame, url);
        dialog.open();
    }

    /**
     * Registers the block URL.
     *
     * @param url block URL.
     */
    public static void setBlockURL(URL url)
    {
        BlockImageAction.url = url;
        instance.setEnabled(url != null);
    }
}
