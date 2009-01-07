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
// $Id: LaunchPage.java,v 1.4 2006/05/31 08:55:21 spyromus Exp $
//

package com.salas.bb.installation.wizard;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.salas.bb.utils.uif.HeaderPanelExt;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Simple installation page.
 */
class LaunchPage extends DefaultWizardPage
{
    private JTextPane textPanel;

    /**
     * Creates installation page.
     *
     * @param aFinish   listener of 'Finish' button.
     * @param aPrev     listener of 'Previous' button.
     * @param aCancel   listener of 'Cancel' button.
     */
    public LaunchPage(ActionListener aFinish, ActionListener aPrev, ActionListener aCancel)
    {
        build(buildButtonBar(aFinish, aPrev, aCancel));
    }

    /**
     * Builds the panel.
     */
    private void build(JComponent buttonBar)
    {
        buttonBar.setBorder(Borders.createEmptyBorder("6dlu, 6dlu, 6dlu, 6dlu"));

        FormLayout layout = new FormLayout("15dlu, left:min:grow, 15dlu",
            "pref, 17dlu, min:grow, 2dlu, pref");
        PanelBuilder builder = new PanelBuilder(layout, this);
        CellConstraints cc = new CellConstraints();

        builder.add(buildHeader(), cc.xyw(1, 1, 3));
        builder.add(buildTutorialPanel(), cc.xy(2, 3, "f, f"));
        builder.add(buttonBar, cc.xyw(1, 5, 3));
    }

    /**
     * Creates the panel's header.
     */
    private JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("installer.launch.title"),
            Strings.message("installer.launch.header"));
    }

    /**
     * Builds and configures the tutorial panel.
     */
    private JComponent buildTutorialPanel()
    {
        textPanel = new JTextPane();
        textPanel.setMargin(new Insets(0, 10, 0, 5));
        textPanel.setOpaque(false);
        textPanel.setEditable(false);
        textPanel.setSelectionColor(textPanel.getBackground());
        textPanel.setSelectedTextColor(textPanel.getForeground());
        textPanel.setText(Strings.message("installer.launch.wording"));

        return textPanel;
    }

    /**
     * Builds bar with buttons.
     */
    private JComponent buildButtonBar(ActionListener aFinish, ActionListener aPrev,
        ActionListener aCancel)
    {
        JPanel bar = ButtonBarFactory.buildRightAlignedBar(
            WizardUIHelper.createPreviousButton(aPrev),
            WizardUIHelper.createFinishButton(aFinish),
            WizardUIHelper.createCancelButton(aCancel));

        bar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
        bar.setOpaque(false);

        return bar;
    }
}
