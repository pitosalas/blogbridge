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
// $Id: FeedUpdatePeriodPanel.java,v 1.1 2007/04/30 11:45:47 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.salas.bb.utils.Constants;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.StateUpdatingToggleListener;

import javax.swing.*;

/**
 * Feed update period manipulation panel.
 */
public class FeedUpdatePeriodPanel extends JPanel
{
    private final long initialUpdatePeriod;

    private final JRadioButton  rbUPCustom;
    private final JTextField    tfUPValue;

    /**
     * Creates the period manipulation panel.
     *
     * @param period initial period.
     */
    public FeedUpdatePeriodPanel(long period)
    {
        initialUpdatePeriod = period;
        JRadioButton rbUPGlobal = new JRadioButton(Strings.message("show.feed.properties.tab.advanced.global"));
        rbUPCustom = new JRadioButton(Strings.message("show.feed.properties.tab.advanced.every"));
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbUPGlobal);
        bg.add(rbUPCustom);
        tfUPValue = new JTextField();

        StateUpdatingToggleListener.install(rbUPCustom, tfUPValue);

        // Calculate period of updates
        if (period <= 0)
        {
            // Global
            rbUPGlobal.setSelected(true);
            tfUPValue.setText("");
        } else
        {
            rbUPCustom.setSelected(true);
            tfUPValue.setText(Long.toString(period / Constants.MILLIS_IN_MINUTE));
        }

        BBFormBuilder builder = new BBFormBuilder("p, 2dlu, 30dlu", this);
        builder.append(rbUPGlobal, 3);
        builder.append(rbUPCustom, tfUPValue);
    }


    /**
     * Converts the user selection / entry into the period.
     * If the entry isn't value, the initial period is used.
     *
     * @return period.
     */
    public long getUpdatePeriod()
    {
        long period = -1;

        if (rbUPCustom.isSelected())
        {
            try
            {
                period = Long.parseLong(tfUPValue.getText()) * Constants.MILLIS_IN_MINUTE;
            } catch (NumberFormatException e)
            {
                // Invalid entry -- not a big deal.
                // Use the initial value
                period = initialUpdatePeriod;
            }
        }

        return period;
    }
}
