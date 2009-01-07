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
// $Id: ServiceAccountPage.java,v 1.8 2007/10/15 08:48:34 spyromus Exp $
//

package com.salas.bb.installation.wizard;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.uif.application.Application;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.service.ServicePreferences;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.HeaderPanelExt;
import com.salas.bb.utils.uif.LinkLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

/**
 * Service account page.
 */
class ServiceAccountPage extends DefaultWizardPage
{
    private JTextField tfUseEmail;
    private JPasswordField tfUsePassword;

    private JCheckBox chSynchronize;

    private boolean isExistingAccountMode = false;
    private String existingEmail;
    private String existingPassword;
    private JTextArea taWording;
    private LinkLabel lnkSignup;
    private LinkLabel lbReadMore;

    /**
     * Creates service account page.
     *
     * @param aFinish   listener of 'Finish' button.
     * @param aPrev     listener of 'Previous' button.
     * @param aNext     listener of 'Next' button.
     * @param aCancel   listener of 'Cancel' button.
     */
    public ServiceAccountPage(ActionListener aFinish, ActionListener aPrev, ActionListener aNext,
                              ActionListener aCancel)
    {
        // Detect existing account
        final Preferences prefs = Application.getUserPreferences();
        existingEmail = prefs.get(ServicePreferences.KEY_EMAIL, null);
        existingPassword = prefs.get(ServicePreferences.KEY_PASSWORD, null);
        isExistingAccountMode = StringUtils.isNotEmpty(existingEmail) && StringUtils.isNotEmpty(existingPassword);

        initComponents();
        build(buildButtonBar(aFinish, aPrev, aNext, aCancel));
    }

    /**
     * Initializes components.
     */
    private void initComponents()
    {
        if (isExistingAccountMode)
        {
            taWording = ComponentsFactory.createWrappedMultilineLabel(
                Strings.message("installer.service.wording.existing"));
            chSynchronize = ComponentsFactory.createCheckBox(
                Strings.message("installer.service.synchronize.with.account"));
        } else
        {
            taWording = ComponentsFactory.createWrappedMultilineLabel(
                Strings.message("installer.service.wording.missing"));

            lbReadMore = new LinkLabel(Strings.message("installer.service.read.more"),
                ResourceUtils.getString("server.plans.url"));

            tfUseEmail = new JTextField();
            tfUsePassword = new JPasswordField();


            lnkSignup = new LinkLabel(Strings.message("service.registration.signup"),
                ResourceUtils.getString("server.signup.url"));
            lnkSignup.setForeground(LinkLabel.HIGHLIGHT_COLOR);
        }
    }

    /**
     * Builds button bar.
     *
     * @param aFinish   finish button listener.
     * @param aPrev     previous button listener.
     * @param aNext     next button listener.
     * @param aCancel   cancel button listener.
     *
     * @return bar component.
     */
    private JComponent buildButtonBar(ActionListener aFinish, ActionListener aPrev,
        ActionListener aNext, ActionListener aCancel)
    {
        JPanel bar = ButtonBarFactory.buildRightAlignedBar(
            WizardUIHelper.createFinishButton(aFinish),
            WizardUIHelper.createPreviousButton(aPrev),
            WizardUIHelper.createNextButton(aNext),
            WizardUIHelper.createCancelButton(aCancel));

        bar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
        bar.setOpaque(false);

        return bar;
    }

    /**
     * Builds the panel.
     *
     * @param buttonBar bar.
     */
    public void build(JComponent buttonBar)
    {
        buttonBar.setBorder(Borders.createEmptyBorder("6dlu, 6dlu, 6dlu, 6dlu"));

        BBFormBuilder builder = new BBFormBuilder("15dlu, pref:grow, 15dlu", this);

        builder.append(buildHeader(), 3);
        builder.setLeadingColumnOffset(1);
        builder.appendRow("15dlu");
        builder.nextLine(2);
        builder.append(taWording);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(buildMainPanel());
        builder.setLeadingColumnOffset(0);
        builder.appendRow("min:grow");
        builder.nextLine(2);
        builder.append(buttonBar, 3);
    }

    /**
     * Builds main panel with content.
     *
     * @return main panel.
     */
    private JPanel buildMainPanel()
    {
        BBFormBuilder builder;

        if (isExistingAccountMode)
        {
            builder = new BBFormBuilder("15dlu, pref");

            builder.nextColumn();
            builder.append(chSynchronize);
        } else
        {
            builder = new BBFormBuilder("15dlu, pref, 2dlu, pref:grow, 2dlu, pref, 2dlu, pref:grow");

            builder.append(lbReadMore, 8);
            builder.appendUnrelatedComponentsGapRow(2);

            builder.append(ComponentsFactory.createWrappedMultilineLabel(
                "If you already have a BlogBridge Service account, " +
                "please enter your BlogBridge Service account's email and password here:"), 8);

            builder.setLeadingColumnOffset(1);
            builder.appendUnrelatedComponentsGapRow(2);
            JLabel lbUseEmail = builder.append(Strings.message("installer.service.email"), 1, tfUseEmail, 1);
            builder.nextLine();
            JLabel lbUsePassword = builder.append(Strings.message("installer.service.password"), 1, tfUsePassword, 1);

            builder.setLeadingColumnOffset(0);

            builder.appendUnrelatedComponentsGapRow(2);
            builder.append(buildSignupLine(), 8);
        }

        return builder.getPanel();
    }

    private Component buildSignupLine()
    {
        BBFormBuilder builder = new BBFormBuilder("p, 2dlu, p");
        builder.append(new JLabel(
            Strings.message("installer.service.signup.wording")),
            lnkSignup);

        return builder.getPanel();
    }

    /**
     * Creates the panel's header.
     *
     * @return header.
     */
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("installer.service.title"),
            Strings.message("installer.service.header"));
    }

    /**
     * Returns <code>true</code> if information is marked as from existing account.
     *
     * @return <code>true</code> if account exists.
     */
    public boolean isExistingAccount()
    {
        return isExistingAccountMode;
    }

    /**
     * Returns email address entered by user.
     *
     * @return email address.
     */
    public String getEmail()
    {
        return isExistingAccountMode
            ? existingEmail
            : tfUseEmail.getText();
    }

    /**
     * Returns password entered by user.
     *
     * @return password.
     */
    public String getPassword()
    {
        return isExistingAccountMode
            ? existingPassword
            : new String(tfUsePassword.getPassword());
    }

    /**
     * Returns <code>true</code> when user requested synchronization with his service account.
     *
     * @return <code>true</code> on synchronization.
     */
    public boolean isSynchronizing()
    {
        return isExistingAccountMode && chSynchronize.isSelected();
    }

    /**
     * Validates and returns error message to display or <code>null</code> if information on the
     * page is valid.
     *
     * @return string to display or <code>null</code>.
     */
    public String validatePage()
    {
        return null;
    }

    /**
     * Checks if entry is valid.
     *
     * @param fullName             name entered by user.
     * @param email                email entered by user.
     * @param password             first password.
     * @param passwordConfirmation second password.
     * @return message in case of error or NULL if everything just fine.
     */
    public static String isValidEntry(String fullName, String email, String password,
                                      String passwordConfirmation)
    {
        boolean result = true;
        String message = Strings.message("service.registration.validation.please.correct.errors.below");

        if (StringUtils.isEmpty(fullName))
        {
            result = false;
            message += Strings.message("service.registration.validation.empty.name");
        }

        if (StringUtils.isEmpty(email) || email.indexOf('@') == -1)
        {
            result = false;
            message += Strings.message("service.registration.validation.empty.email");
        }

        if (StringUtils.isEmpty(password) || StringUtils.isEmpty(passwordConfirmation))
        {
            result = false;
            message += Strings.message("service.registration.validation.empty.password");
        } else if (!password.equals(passwordConfirmation))
        {
            result = false;
            message += Strings.message("service.registration.validation.passwords.do.not.match");
        }

        return result ? null : message;
    }

    /**
     * Returns <code>true</code> if user requested to use existing account.
     *
     * @return <code>true</code> if user requested to use existing account.
     */
    public boolean isAccountUsingSelected()
    {
        return !isExistingAccountMode && StringUtils.isNotEmpty(getEmail()) && StringUtils.isNotEmpty(getPassword());
    }
}
