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
// $Id: SwitchLogLevelAction.java,v 1.4 2006/01/08 04:42:25 kyank Exp $
//

package com.salas.bb.core.actions.logging;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Toggles level of logger between the one set on start and the other.
 */
public final class SwitchLogLevelAction extends AbstractAction
{
    private Level originalLevel;
    private Level secondLevel;
    private String pkg;

    /**
     * Creates new action.
     *
     * @param pkg           name of package to get logger for or empty string for root.
     * @param secondLevel   level of log, which will be used as second state while switching.
     */
    public SwitchLogLevelAction(String pkg, Level secondLevel)
    {
        this.pkg = pkg;
        this.secondLevel = secondLevel;

        Logger logger = Logger.getLogger(pkg);
        originalLevel = logger.getLevel();
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e action event object.
     */
    public void actionPerformed(ActionEvent e)
    {
        Logger logger = Logger.getLogger(pkg);
        if (logger.getLevel().equals(originalLevel))
        {
            logger.setLevel(secondLevel);
        } else
        {
            logger.setLevel(originalLevel);
        }
    }
}
