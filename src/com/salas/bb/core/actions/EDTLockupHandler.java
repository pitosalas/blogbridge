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
// $Id: EDTLockupHandler.java,v 1.6 2006/05/29 12:48:29 spyromus Exp $
//

package com.salas.bb.core.actions;

import com.salas.bb.utils.i18n.Strings;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

/**
 * Handles situations when EDT is completely locked up.
 * First thing it collects information about environment:
 * <ul>
 *  <li>VM version information.</li>
 *  <li>Running threads.</li>
 * </ul>
 *
 * Then it posts the information to service and dumps in the log.
 *
 * The last thing it does is forcing application termination. Termination is done in two ways:
 * <ol>
 *  <li>If it's the first time we get this lockup we try to exit smoothly by calling for normal
 *      application exit procedure.</li>
 *  <li>If it's not the first time we get this lockup report then previous exit operation wasn't
 *      successful and we skip smooth exiting doing rude <code>System.exit(1)</code> call.</li>
 * </ol>
 */
public class EDTLockupHandler extends AbstractLockupHandler
{
    private static final Logger LOG = Logger.getLogger(EDTLockupHandler.class.getName());

    private int occurances = 0;

    /**
     * Returns logger to user for logging.
     *
     * @return logger to user for logging.
     */
    protected Logger getLogger()
    {
        return LOG;
    }

    /**
     * Invoked when some action performed.
     *
     * @param e action object.
     */
    public void actionPerformed(ActionEvent e)
    {
        LOG.warning(Strings.error("edt.lockup.detected"));

        occurances++;

        if (occurances == 1)
        {
            AWTEvent event = (AWTEvent)e.getSource();

            report(Strings.error("edt.lockup.detected"), collectDetails(event));
        }

        terminate(occurances == 1);
    }
}
