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
// $Id$
//

package com.salas.bb.twitter;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.binding.value.BufferedValueModel;
import com.jgoodies.binding.adapter.ToggleButtonAdapter;
import com.jgoodies.binding.adapter.DocumentAdapter;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.StateUpdatingToggleListener;

import javax.swing.*;
import java.awt.*;

/**
 * Twitter preferences.
 */
public class TwitterPreferencesPanel extends JPanel
{
    private final TwitterPreferences prefs;

    /**
     * Creates the panel.
     *
     * @param parent        parent dialog.
     * @param trigger       trigger.
     * @param preferences   preferences object to manipulate.
     */
    public TwitterPreferencesPanel(JDialog parent, ValueModel trigger, TwitterPreferences preferences)
    {
        prefs = preferences;

        Component tfWording = ComponentsFactory.createWrappedMultilineLabel(
            Strings.message("userprefs.tab.twitter.wording"));

        // Layout
        BBFormBuilder builder = new BBFormBuilder("7dlu, p, 4dlu, p:grow", this);
        builder.setDefaultDialogBorder();

        JCheckBox chEnabled = ComponentsFactory.createCheckBox(
            Strings.message("userprefs.tab.twitter.enable"),
            new ToggleButtonAdapter(new BufferedValueModel(
                new PropertyAdapter(preferences, TwitterPreferences.PROP_ENABLED),
                trigger)));

        JLabel lbScreenName = new JLabel(Strings.message("userprefs.tab.twitter.screenname"));
        JLabel lbPassword   = new JLabel(Strings.message("userprefs.tab.twitter.password"));

        JTextField tfScreenName = new JTextField();
        tfScreenName.setDocument(new DocumentAdapter(new BufferedValueModel(
            new PropertyAdapter(preferences, TwitterPreferences.PROP_SCREEN_NAME), trigger)));

        JPasswordField tfPassword   = new JPasswordField();
        tfPassword.setDocument(new DocumentAdapter(new BufferedValueModel(
            new PropertyAdapter(preferences, TwitterPreferences.PROP_PASSWORD), trigger)));
        
        StateUpdatingToggleListener.install(chEnabled, lbScreenName, tfScreenName, lbPassword, tfPassword);

        builder.append(tfWording, 4);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(chEnabled, 4);

        builder.setLeadingColumnOffset(1);
        builder.nextLine();
        builder.append(lbScreenName, tfScreenName);
        builder.append(lbPassword, tfPassword);
    }
}
