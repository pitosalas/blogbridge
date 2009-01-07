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
// $Id: ProgressSpinner.java,v 1.1 2006/12/08 09:50:17 spyromus Exp $
//

package com.salas.bb.utils.uif;

import com.salas.bb.core.FeedFormatter;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Progress spinner component.
 */
public class ProgressSpinner extends JLabel
{
    /** Pause in ms between progress icon frames. */
    private static final int PROGRESS_ICON_FRAME_PAUSE = 500;
    private Timer timer;

    /**
     * Creates spinner.
     */
    public ProgressSpinner()
    {
        setVisible(false);
        timer = new Timer(PROGRESS_ICON_FRAME_PAUSE, new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setCurrentIcon();
                if (!isVisible()) timer.stop();
            }
        });
    }

    /**
     * Starts progress indication.
     */
    public void start()
    {
        setCurrentIcon();
        setVisible(true);
        timer.start();
    }

    /**
     * Stops progress indication.
     */
    public void stop()
    {
        setVisible(false);
        timer.stop();
    }

    /**
     * Shows appropriate frame.
     */
    private void setCurrentIcon()
    {
        int frames = FeedFormatter.getLoadingIconFrames();
        long time = System.currentTimeMillis();
        int frame = (int)((time / PROGRESS_ICON_FRAME_PAUSE) % frames);
        setIcon(FeedFormatter.getLoadingIcon(frame));
    }
}
