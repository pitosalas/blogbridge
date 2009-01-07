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
// $Id: SettingsDialog.java,v 1.6 2008/02/28 10:50:24 spyromus Exp $
//

package com.salas.bb.sentiments;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.util.Resizer;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.core.GlobalController;
import com.salas.bb.utils.ResourceID;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;

/**
 * Settings dialog.
 */
public class SettingsDialog extends AbstractDialog
{
    private JCheckBox               chEnabled;
    private AdvancedSettingsDialog  advancedDialog;
    private JButton                 btnAdvanced;
    private boolean                 featureAvailable;

    /**
     * Creates a settings dialog.
     *
     * @param frame frame.
     * @param config configuration to work with.
     */
    public SettingsDialog(Frame frame, SentimentsConfig config)
    {
        super(frame, Strings.message("sentiment.analysis"));

        ValueModel trigger = getTriggerChannel();
        advancedDialog = new AdvancedSettingsDialog(SettingsDialog.this, config, trigger);

        chEnabled = ComponentsFactory.createCheckBox(Strings.message("sentiment.analysis.enable"),
            config, SentimentsConfig.PROP_ENABLED, trigger);
        btnAdvanced = new JButton(new AdvancedAction());

        featureAvailable = SentimentsFeature.isAvailable();
        chEnabled.setEnabled(featureAvailable);
        btnAdvanced.setEnabled(featureAvailable);
    }

    /**
     * Builds and answers the preference's header.
     *
     * @return JComponent header of dialog box
     */
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("sentiment.analysis"),
            Strings.message("sentiment.analysis.basic.header"),
            IconSource.getIcon(ResourceID.ICON_PREFERENCES));
    }

    /**
     * Returns the contents.
     *
     * @return contents.
     */
    protected JComponent buildContent()
    {
        JPanel content = new JPanel(new BorderLayout());
        content.add(buildMainPanel(), BorderLayout.CENTER);
        content.add(buildButtonBar(), BorderLayout.SOUTH);
        return content;
    }

    /**
     * Creates a button bar.
     *
     * @return bar.
     */
    private JComponent buildButtonBar()
    {
        JPanel bar;

        if (featureAvailable)
        {
            bar = ButtonBarFactory.buildHelpOKCancelBar(btnAdvanced, createOKButton(true), createCancelButton());
        } else
        {
            bar = ButtonBarFactory.buildHelpCloseBar(btnAdvanced, createCloseButton(true));
        }
        
        return bar;
    }

    /**
     * Returns the main panel.
     *
     * @return panel.
     */
    private Component buildMainPanel()
    {
        BBFormBuilder builder = new BBFormBuilder("7dlu, p:grow");

        if (featureAvailable)
        {
            String message = Strings.message("sentiment.analysis.available");
            builder.append(ComponentsFactory.createWrappedMultilineLabel(message), 2);
        } else
        {
            // Feature description link
            LinkLabel lnkFeature = new LinkLabel(
                Strings.message("sentiment.analysis"),
                ResourceUtils.getString("sentiment.analysis.url"));
            lnkFeature.setForeground(LinkLabel.HIGHLIGHT_COLOR);

            // Service link
            LinkLabel lnkService = new LinkLabel(
                Strings.message("spw.learn.more"),
                ResourceUtils.getString("server.plans.url"));
            lnkService.setForeground(LinkLabel.HIGHLIGHT_COLOR);

            String plan = GlobalController.SINGLETON.getFeatureManager().getPlanName();
            String message = MessageFormat.format(Strings.message("sentiment.analysis.unavailable.1"), plan);
            builder.append(ComponentsFactory.createWrappedMultilineLabel(message), 2);

            builder.appendUnrelatedComponentsGapRow(2);
            builder.append(new JLabel(Strings.message("sentiment.analysis.unavailable.2")), 2);

            builder.setLeadingColumnOffset(1);
            builder.append(lnkFeature);
            builder.append(lnkService);

            builder.setLeadingColumnOffset(0);
            builder.appendUnrelatedComponentsGapRow(2);
            builder.append(ComponentsFactory.createWrappedMultilineLabel(
                Strings.message("sentiment.analysis.unavailable.3")), 2);

            chEnabled.setSelected(false);
        }

        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(chEnabled, 2);

        return builder.getPanel();
    }

    protected void resizeHook(JComponent component)
    {
        component.setPreferredSize(Resizer.DEFAULT.fromWidth(600));
    }

    /**
     * The action that shows the advanced settings dialog.
     */
    private class AdvancedAction extends AbstractAction
    {
        private AdvancedAction()
        {
            super(Strings.message("sentiment.analysis.advanced"));
        }

        public void actionPerformed(ActionEvent e)
        {
            advancedDialog.open();
        }
    }
}
