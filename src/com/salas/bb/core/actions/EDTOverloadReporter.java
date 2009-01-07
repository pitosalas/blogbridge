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
// $Id: EDTOverloadReporter.java,v 1.5 2006/05/29 12:48:29 spyromus Exp $
//

package com.salas.bb.core.actions;

import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.MessageFormat;

/**
 * Utility action to report overload of EDT.
 */
public class EDTOverloadReporter extends AbstractAction
{
    private static final Logger LOG = Logger.getLogger(EDTOverloadReporter.class.getName());

    private Level level;

    /**
     * Creates reporter.
     *
     * @param aLevel level to use for reporting.
     */
    public EDTOverloadReporter(Level aLevel)
    {
        level = aLevel;
    }

    /**
     * Invoked when some action performed.
     *
     * @param e action object.
     */
    public void actionPerformed(ActionEvent e)
    {
        AWTEvent event = (AWTEvent)e.getSource();

        if (LOG.isLoggable(level))
        {
            LOG.log(level, MessageFormat.format(Strings.error("edt.is.overloaded.with"), new Object[] { event }));
        }
    }
}
