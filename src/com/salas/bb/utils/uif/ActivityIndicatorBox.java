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
// $Id: ActivityIndicatorBox.java,v 1.7 2006/05/31 11:28:31 spyromus Exp $
//

package com.salas.bb.utils.uif;

import com.salas.bb.utils.Constants;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.*;

/**
 * Simple box for activity indication. Supports single- and multi-task modes.
 */
public class ActivityIndicatorBox extends JLabel
{
    private static final int SPACING = 5;
    private static final Dimension DIM_SINGLE = new Dimension(17 + SPACING, 19);

    private Icon active;
    private Icon passive;
    private String[] taskList;
    private boolean passiveMode;

    /**
     * Creates and initializes indicator box.
     *
     * @param aActive           icon for active mode.
     * @param aPassive          icon for passive mode.
     */
    public ActivityIndicatorBox(Icon aActive, Icon aPassive)
    {
        active = aActive;
        passive = aPassive;

        buildGui();

        setTasksList(new String[0]);
    }

    /**
     * Builds GUI.
     */
    private void buildGui()
    {
        final Dimension dim = DIM_SINGLE;
        setIconTextGap(2);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setMaximumSize(dim);
    }

    /**
     * Use this method to specify list of tasks or reset it.
     *
     * @param tasks list of tasks, 0-list or <code>NULL</code>.
     */
    public void setTasksList(String[] tasks)
    {
        taskList = tasks;

        setPassiveMode(tasks == null || tasks.length == 0);
    }

    /**
     * Sets passive or active mode.
     *
     * @param aPassiveMode <code>TRUE</code> of passive mode.
     */
    private void setPassiveMode(boolean aPassiveMode)
    {
        passiveMode = aPassiveMode;
        setIcon(passiveMode ? passive : active);
        setToolTipText(passiveMode
            ? Strings.message("ui.activity.indicator.no.activity")
            : prepareToolTipText(taskList));
    }

    /**
     * Converts list of tasks into the multi-line HTML.
     *
     * @param tasks list of tasks.
     *
     * @return HTML with line per-task.
     */
    static String prepareToolTipText(String[] tasks)
    {
        if (tasks == null || tasks.length == 0) return Constants.EMPTY_STRING;

        StringBuffer sb = new StringBuffer("<html>");

        sb.append(tasks[0]);
        for (int i = 1; i < tasks.length; i++)
        {
            sb.append("<br>").append(tasks[i]);
        }

        sb.append("</html>");

        return sb.toString();
    }

    /**
     * Changes icon to the specified state to create blink effect only
     * if in active state.
     *
     * @param toActive  <code>true</code> when to switch to active.
     */
    public void blink(final boolean toActive)
    {
        if (!passiveMode) setIcon(toActive ? active : passive);
    }
}
