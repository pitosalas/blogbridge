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
// $Id: LicensePage.java,v 1.4 2006/05/31 08:55:21 spyromus Exp $
//

package com.salas.bb.installation.wizard;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.util.ResourceUtils;
import com.jgoodies.uifextras.util.UIFactory;
import com.salas.bb.utils.ResourceID;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.HeaderPanelExt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Page with licensing information.
 */
public class LicensePage extends DefaultWizardPage
{
    private static final Logger LOG = Logger.getLogger(LicensePage.class.getName());

    private JEditorPane licensePane;
    private JRadioButton rbAccept;
    private JRadioButton rbDecline;

    /**
     * Creates page.
     *
     * @param aPrev     listener of 'Previous' button.
     * @param aNext     listener of 'Next' button.
     * @param aCancel   listener of 'Cancel' button.
     */
    public LicensePage(ActionListener aPrev, ActionListener aNext, ActionListener aCancel)
    {
        initComponents();
        build(buildButtonBar(aPrev, aNext, aCancel));
    }

    /**
     * Creates bar with buttons.
     */
    private JComponent buildButtonBar(ActionListener aPrev, ActionListener aNext,
        ActionListener aCancel)
    {
        JPanel bar = ButtonBarFactory.buildRightAlignedBar(
            WizardUIHelper.createPreviousButton(aPrev),
            WizardUIHelper.createNextButton(aNext),
            WizardUIHelper.createCancelButton(aCancel));

        bar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
        bar.setOpaque(false);

        return bar;
    }

    /**
     * Creates and configures the UI components.
     */
    private void initComponents()
    {
        String licenseUrlS = ResourceUtils.getString(ResourceID.LICENSE_AGREEMENT_PATH);

        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("License URL string: " + licenseUrlS);
        }

        URL licenseUrl = ResourceUtils.getURL(licenseUrlS);

        licensePane = UIFactory.createHTMLPane(false, true);
        licensePane.setBackground(UIFactory.getLightBackground());
        licensePane.setMargin(new Insets(0, 10, 0, 5));

        try
        {
            licensePane.setPage(licenseUrl);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        rbAccept = new JRadioButton(Strings.message("installer.license.accept"));
        rbDecline = new JRadioButton(Strings.message("installer.license.decline"));

        ButtonGroup group = new ButtonGroup();
        group.add(rbAccept);
        group.add(rbDecline);
    }

    /**
     * Builds the panel.
     */
    public void build(JComponent buttonBar)
    {
        buttonBar.setBorder(Borders.createEmptyBorder("6dlu, 6dlu, 6dlu, 6dlu"));

        BBFormBuilder builder = new BBFormBuilder("15dlu, left:min:grow, 15dlu", this);

        builder.append(buildHeader(), 3);

        builder.setLeadingColumnOffset(1);
        builder.appendRow("15dlu");
        builder.appendRow("min:grow");
        builder.nextLine(2);
        builder.append(buildLicensePanel(), 1, CellConstraints.FILL, CellConstraints.FILL);
        builder.append(rbAccept);
        builder.append(rbDecline);
        builder.setLeadingColumnOffset(0);
        builder.append(buttonBar, 3);
    }

    /**
     * Creates the panel's header.
     */
    private JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("installer.license.title"),
            Strings.message("installer.license.header"),
            ResourceUtils.getIcon(ResourceID.LICENSE_ICON));
    }

    /**
     * Builds and configures the license panel.
     */
    private JComponent buildLicensePanel()
    {
        JScrollPane scrollPane = new JScrollPane(licensePane);
        scrollPane.putClientProperty("jgoodies.isEtched", Boolean.TRUE);

        // Register license keyboard actions.
        KeyStroke[] registeredKeystrokes = scrollPane.getRegisteredKeyStrokes();
        for (int i = 0; i < registeredKeystrokes.length; i++)
        {
            KeyStroke keyStroke = registeredKeystrokes[i];
            ActionListener keyboardAction = scrollPane.getActionForKeyStroke(keyStroke);

            scrollPane.unregisterKeyboardAction(keyStroke);
            scrollPane.registerKeyboardAction(keyboardAction, keyStroke,
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        }

        return scrollPane;
    }

    /**
     * Validates and returns error message to display or <code>null</code> if information on the
     * page is valid.
     *
     * @return string to display or <code>null</code>.
     */
    public String validatePage()
    {
        String msg = null;

        if (!rbAccept.isSelected()) msg = Strings.message("installer.license.warning");

        return msg;
    }
}