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
// $Id: ProgressPanel.java,v 1.1 2007/09/11 18:59:48 spyromus Exp $
//

package com.salas.bb.utils.uif;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Progress panel. Used to display the progress.
 */
public class ProgressPanel extends JPanel
{
    /** Progress bar component. */
    private JProgressBar progressBar;

    /**
     * Create a new panel.
     *
     * @param progressLabel a label to show above the progress bar.
     */
    public ProgressPanel(String progressLabel)
    {
        setLayout(new FormLayout("5dlu, center:pref:grow, 5dlu", "5dlu:grow, pref, 5dlu, pref, 5dlu:grow"));

        JLabel label = new JLabel(progressLabel);
        label.setBackground(Color.WHITE);
        setBackground(Color.WHITE);

        progressBar = new JProgressBar(0, 99);
        progressBar.setPreferredSize(new Dimension(250, (int)progressBar.getPreferredSize().getHeight()));

        CellConstraints cc = new CellConstraints();
        add(label, cc.xy(2, 2));
        add(progressBar, cc.xy(2, 4));
    }

    /**
     * Sets the progress in the bar.
     *
     * @param progress [0 - 100].
     */
    public void setProgress(int progress)
    {
        progress = Math.min(99, progress);
        progress = Math.max(0, progress);

        progressBar.setValue(progress);
    }
}
