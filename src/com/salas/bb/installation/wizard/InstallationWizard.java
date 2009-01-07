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
// $Id: InstallationWizard.java,v 1.13 2008/04/07 16:35:23 spyromus Exp $
//

package com.salas.bb.installation.wizard;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.panel.CardPanel;
import com.jgoodies.uif.util.Resizer;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Installation wizard dialog box.
 */
public class InstallationWizard extends AbstractDialog
{
    private IWizardPage[]       panels;

    private CardPanel           cpanel;
    private ServiceAccountPage  serviceAccountPage;
    private StartingPointsPage  startingPointsPage;

    private InstallationSettings installationSettings = new InstallationSettings();

    /**
     * Contains pages that should not be validated on 'doPrevious' event.
     */
    private java.util.List skipPreviousValidationPages;

    /**
     * Creates wizard dialog.
     */
    public InstallationWizard()
    {
        super((Frame)null);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    }

    /**
     * Builds content panel.
     *
     * @return content panel.
     */
    protected JComponent buildContent()
    {
        buildPanels();

        cpanel = new CardPanel();
        for (IWizardPage panel : panels) cpanel.add((Component)panel);

        Dimension size = Resizer.DEFAULT.fromWidth(600);
        cpanel.setMinimumSize(size);
        cpanel.setMaximumSize(size);
        cpanel.setPreferredSize(size);

        return cpanel;
    }

    /**
     * Handles window events depending on the state of the <code>defaultCloseOperation</code>
     * property.
     *
     * @see #setDefaultCloseOperation
     */
    protected void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);

        if (e.getID() == WindowEvent.WINDOW_OPENED) pack();
    }

    /**
     * Build panels.
     */
    private void buildPanels()
    {
        final FinishActionListener aFinish = new FinishActionListener();
        final PrevActionListener aPrev = new PrevActionListener();
        final NextActionListener aNext = new NextActionListener();
        final CancelActionListener aCancel = new CancelActionListener();

        serviceAccountPage = new ServiceAccountPage(aFinish, aPrev, aNext, aCancel);
        startingPointsPage = new StartingPointsPage(aFinish, aPrev, aCancel);

        panels = new IWizardPage[]
        {
            new WelcomePage(aNext, aCancel),
            new LicensePage(aPrev, aNext, aCancel),
            new TutorialPage(aFinish, aPrev, aNext, aCancel),
            serviceAccountPage,
            startingPointsPage
        };

        // Populate list of pages that should not be validated on 'doPrevious' event.
        skipPreviousValidationPages = Collections.synchronizedList(new ArrayList());
        skipPreviousValidationPages.add(panels[1]); // License page.
    }

    /**
     * Display next page of wizard.
     */
    private void doNext()
    {
        // Validate current page
        if (!validatePage((IWizardPage)cpanel.getVisibleCard())) return;

        cpanel.showNextCard();

        // Do another step forward to skip starting points selection page if
        // user has service account and requested synchronization
        if (cpanel.getVisibleCard() == startingPointsPage &&
            serviceAccountPage.isSynchronizing())
        {
            cpanel.showNextCard();
        }
    }

    /**
     * Display previous page of wizard.
     */
    private void doPrevious()
    {
        // Validate current page
        if (!validatePage((IWizardPage)cpanel.getVisibleCard(),true)) return;

        cpanel.showPreviousCard();

        // Do another step backward to skip starting points selection page if
        // user has service account and requested synchronization
        if (cpanel.getVisibleCard() == startingPointsPage &&
            serviceAccountPage.isSynchronizing())
        {
            cpanel.showPreviousCard();
        }
    }

    /**
     * Asks user for confirmation and cancels wizard on it.
     */
    public void doCancel()
    {
        int choice = JOptionPane.showConfirmDialog(this,
            Strings.message("installation.wizard.confirm.cancel"),
            getTitle(), JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) super.doCancel();
    }

    /**
     * Finish wizard.
     */
    private void doFinish()
    {
        // Validate current page
        if (!validatePage((IWizardPage)cpanel.getVisibleCard())) return;

        doAccept();
    }

    /**
     * Validates the page and returns the status. If the page isn't valid dialog box with
     * the message will be displayed.
     *
     * @param page  page to validate.
     *
     * @return <code>true</code> if everything is OK.
     */
    private boolean validatePage(IWizardPage page)
    {
        return validatePage(page, false);
    }

    /**
     * Validates the page and returns the status. If the page isn't valid dialog box with
     * the message will be displayed.
     *
     * @param page          page to validate.
     * @param isPrevious    <code>true</code> if validation is performed before switching
     *                      to the previous page, <code>false</code> otherwise.
     *
     * @return <code>true</code> if everything is OK.
     */
    private boolean validatePage(IWizardPage page, boolean isPrevious)
    {
        boolean result = true;

        // Perform validation if the page is not in the exceptions' list.
        String errorMessage = (isPrevious && skipPreviousValidationPages.contains(page)
            ? null : page.validatePage());

        if (errorMessage != null)
        {
            JOptionPane.showMessageDialog(this, errorMessage,
                Strings.message("installation.wizard.dialog.title.error"),
                JOptionPane.ERROR_MESSAGE);

            result = false;
        }

        return result;
    }

    /**
     * Returns dialog border.
     *
     * @return border.
     */
    protected Border getDialogBorder()
    {
        return Borders.EMPTY_BORDER;
    }

    /**
     * Returns settings selected by user in wizard for installation.
     *
     * @return installation settings.
     */
    public InstallationSettings getInstallationSettings()
    {
        // update settings

        // service account
        installationSettings.setServiceAccountEmail(serviceAccountPage.getEmail());
        installationSettings.setServiceAccountPassword(serviceAccountPage.getPassword());
        installationSettings.setServiceAccountExists(serviceAccountPage.isExistingAccount());
        installationSettings.setServiceAccountUse(serviceAccountPage.isAccountUsingSelected());

        // starting points
        installationSettings.setStartingPoints(startingPointsPage.getSelectedStartingPoints());

        // data initialization mode
        int mode = InstallationSettings.DATA_INIT_CLEAN;
        if (serviceAccountPage.isSynchronizing())
        {
            mode = InstallationSettings.DATA_INIT_SERVICE;
        } else if (installationSettings.getSelectedStartingPoints().length > 0)
        {
            mode = InstallationSettings.DATA_INIT_POINTS;
        }
        installationSettings.setDataInitMode(mode);

        return installationSettings;
    }

    /**
     * Opens dialog and returns installation setting or <code>NULL</code> if installation
     * was cancelled.
     *
     * @return settings or <code>NULL</code>.
     */
    public InstallationSettings openDialog()
    {
        super.open();

        return hasBeenCanceled() ? null : getInstallationSettings();
    }

    /**
     * Listener of next-button events.
     */
    private class NextActionListener implements ActionListener
    {
        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e)
        {
            doNext();
        }
    }

    /**
     * Listener of prev-button events.
     */
    private class PrevActionListener implements ActionListener
    {
        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e)
        {
            doPrevious();
        }
    }

    /**
     * Listener of cancel-button events.
     */
    private class CancelActionListener implements ActionListener
    {
        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e)
        {
            doCancel();
        }
    }

    /**
     * Listener of finish-button events.
     */
    private class FinishActionListener implements ActionListener
    {
        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e)
        {
            doFinish();
        }
    }
}
