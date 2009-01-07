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
// $Id: SyncSettingsPanel.java,v 1.19 2007/02/22 11:55:45 spyromus Exp $
//

package com.salas.bb.service;

import com.jgoodies.binding.adapter.DocumentAdapter;
import com.jgoodies.binding.adapter.ToggleButtonAdapter;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.value.BufferedValueModel;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.core.GlobalController;
import com.salas.bb.service.sync.SyncFullAction;
import com.salas.bb.service.sync.SyncInAction;
import com.salas.bb.service.sync.SyncOutAction;
import com.salas.bb.utils.DateUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

/**
 * Panel of synchronization settings.
 */
class SyncSettingsPanel extends JPanel implements IRegistrationListener
{
    private JTextField          tfEmail = new JTextField();
    private JPasswordField      tfPassword = new JPasswordField();
    private JLabel              lbLastSyncIn = new JLabel("unknown");
    private JLabel              lbLastSyncOut = new JLabel("unknown");
    private JTextField          tfSyncPeriod = new JTextField();

    private AbstractDialog      parentDialog;
    private ServicePreferences  servicePrefs;
    private JRadioButton        rbModePeriodical;
    private JRadioButton        rbModeEachRun;
    private JRadioButton        rbModeManual;
    private JCheckBox           chFeedList;
    private JCheckBox           chPreferences;
    private JButton             btnSyncNow;
    private CustomPopupButton   btnMoreSyncOptions;

    /**
     * Creates panel.
     *
     * @param parentDiag        parent dialog.
     * @param prefs             service preferences.
     * @param triggerChannel    value model.
     */
    public SyncSettingsPanel(AbstractDialog parentDiag, ServicePreferences prefs,
        ValueModel triggerChannel)
    {
        this.parentDialog = parentDiag;
        servicePrefs = prefs;

        LinkLabel lnkSignUp = new LinkLabel(Strings.message("service.registration.signup"),
            ResourceUtils.getString("server.signup.url"));

        initComponents(triggerChannel);

        JPanel buttonBar = new JPanel();
        buttonBar.add(btnSyncNow);
        buttonBar.add(btnMoreSyncOptions);

        BBFormBuilder builder = new BBFormBuilder(
            "7dlu, pref, 2dlu, pref:grow, 2dlu, 20dlu, 2dlu, 80dlu:grow");
        builder.setDefaultDialogBorder();

        builder.appendSeparator(Strings.message("service.registration.account.information"));
        builder.append(Strings.message("service.registration.email"), 2, tfEmail, 5);
        builder.append(Strings.message("service.registration.password"), 2, tfPassword, 5);
        builder.append("", 2, lnkSignUp, 5);

        builder.appendSeparator(Strings.message("service.sync.mode.of.operation"));
        builder.setLeadingColumnOffset(1);
        builder.append(rbModePeriodical, 3);
        builder.append(tfSyncPeriod);
        builder.append(Strings.message("service.sync.days"), 1);
        builder.append(rbModeEachRun, 7);
        builder.append(rbModeManual, 7);

        builder.appendSeparator(Strings.message("service.sync.what.to.synchronize"));
        builder.append(chFeedList, 7);
        builder.append(chPreferences, 7);

        builder.appendSeparator(Strings.message("service.sync.statistics"));
        builder.append(Strings.message("service.sync.last.sync.in"), lbLastSyncIn, 5);
        builder.append(Strings.message("service.sync.last.sync.out"), lbLastSyncOut, 5);

        builder.setLeadingColumnOffset(0);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(buttonBar, 8);

        setLayout(new BorderLayout());
        add(builder.getPanel(), BorderLayout.CENTER);
    }

    /**
     * Binds components to data in preferences object.
     *
     * @param triggerChannel value model.
     */
    private void initComponents(ValueModel triggerChannel)
    {
        btnSyncNow = new JButton(new SyncWrapperAction(SyncFullAction.getInstance()));
        final JPopupMenu menu = new JPopupMenu();
        menu.add(new SyncWrapperAction(SyncInAction.getInstance()));
        menu.add(new SyncWrapperAction(SyncOutAction.getInstance()));
        btnMoreSyncOptions = new CustomPopupButton(btnSyncNow,
            Strings.message("service.sync.more"), menu);

        rbModePeriodical = ComponentsFactory.createRadioButton(
            Strings.message("service.sync.synchronize.every"));
        rbModeEachRun = ComponentsFactory.createRadioButton(
            Strings.message("service.sync.synchronize.each.application.run"));
        rbModeManual = ComponentsFactory.createRadioButton(
            Strings.message("service.sync.manual.synchronization.only"));

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbModePeriodical);
        bg.add(rbModeEachRun);
        bg.add(rbModeManual);
        rbModePeriodical.addItemListener(new PeriodicalSelectionListener());

        tfSyncPeriod.setEnabled(false);

        chFeedList = ComponentsFactory.createCheckBox(
            Strings.message("service.sync.feed.list"),
            new ToggleButtonAdapter(new BufferedValueModel(new PropertyAdapter(servicePrefs,
                ServicePreferences.PROP_SYNC_FEEDS), triggerChannel)));
        chFeedList.addActionListener(new FullSyncEnabled());

        chPreferences = ComponentsFactory.createCheckBox(
            Strings.message("service.sync.preferences"),
            new ToggleButtonAdapter(new BufferedValueModel(new PropertyAdapter(servicePrefs,
                ServicePreferences.PROP_SYNC_PREFERENCES), triggerChannel)));

        new MandatoryCheckBoxController(chFeedList, chPreferences);
        
        tfEmail.setDocument(new DocumentAdapter(new BufferedValueModel(
                new PropertyAdapter(servicePrefs, "email"), triggerChannel)));

        tfPassword.setDocument(new DocumentAdapter(new BufferedValueModel(
                new PropertyAdapter(servicePrefs, "password"), triggerChannel)));

        tfSyncPeriod.setDocument(new DocumentAdapter(new BufferedValueModel(
                new PropertyAdapter(new ServicePreferencesWrapper(servicePrefs), "syncPeriod"),
                triggerChannel)));

        updateFullSyncButton();
        updateSyncDatesView();
        selectMode();
    }

    /**
     * Updates text on sync dates labels.
     */
    private void updateSyncDatesView()
    {
        Date lastSyncInDate = servicePrefs.getLastSyncInDate();
        Date lastSyncOutDate = servicePrefs.getLastSyncOutDate();

        String strSyncIn = lastSyncInDate == null
                ? Strings.message("service.sync.not.performed")
                : DateUtils.dateToString(lastSyncInDate) +
                " [" + servicePrefs.getLastSyncInStatus() + "]";
        String strSyncOut = lastSyncOutDate == null
                ? Strings.message("service.sync.not.performed")
                : DateUtils.dateToString(lastSyncOutDate) +
                " [" + servicePrefs.getLastSyncOutStatus() + "]";

        lbLastSyncIn.setText(strSyncIn);
        lbLastSyncOut.setText(strSyncOut);
    }

    /**
     * Selects mode basing on preference.
     */
    private void selectMode()
    {
        int mode = servicePrefs.getSyncMode();
        JRadioButton selected = null;

        switch (mode)
        {
            case ServicePreferences.SYNC_MODE_EACH_RUN:
                selected = rbModeEachRun;
                break;
            case ServicePreferences.SYNC_MODE_MANUAL:
                selected = rbModeManual;
                break;
            case ServicePreferences.SYNC_MODE_PERIODICAL:
                selected = rbModePeriodical;
                break;
            default:
                break;
        }

        if (selected != null) selected.setSelected(true);
    }

    /**
     * Writes selected mode back into preferences.
     */
    public void writeMode()
    {
        int mode;
        if (rbModeEachRun.isSelected())
        {
            mode = ServicePreferences.SYNC_MODE_EACH_RUN;
        } else if (rbModePeriodical.isSelected())
        {
            mode = ServicePreferences.SYNC_MODE_PERIODICAL;
        } else
        {
            mode = ServicePreferences.SYNC_MODE_MANUAL;
        }

        servicePrefs.setSyncMode(mode);
    }

    private void updateFullSyncButton()
    {
        boolean enabled = SyncFullAction.getInstance().isEnabled() &&
                          GlobalController.SINGLETON.isInitializationFinished();

        btnSyncNow.setEnabled(enabled);
    }

    /**
     * Invoked when registration finished successfully.
     *
     * @param email    email used for registration.
     * @param password password.
     */
    public void registeredSuccessfully(String email, String password)
    {
        final String textEmail = tfEmail.getText();
        if (textEmail == null || textEmail.trim().length() == 0)
        {
            // email is not set - fill with new data
            tfEmail.setText(email);
            tfPassword.setText(password);
        }
    }

    /**
     * Wrapper for action, which applies dialog changes before action event.
     */
    private class SyncWrapperAction extends AbstractAction
            implements PropertyChangeListener
    {
        private Action act;

        /**
         * Creates wrapper for action.
         *
         * @param action action to wrap.
         */
        public SyncWrapperAction(Action action)
        {
            this.act = action;
            action.addPropertyChangeListener(this);
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e action object.
         */
        public void actionPerformed(ActionEvent e)
        {
            parentDialog.doApply();

            act.actionPerformed(e);

            // When putting code here note that the above action can be (if extended from
            // ThreadedAction) executed asynchronously, which means that the code below will
            // be executed *before* actual action completion.
        }

        /**
         * Returns true if the action is enabled.
         *
         * @return true if the action is enabled, false otherwise
         * @see javax.swing.Action#isEnabled
         */
        public boolean isEnabled()
        {
            return act.isEnabled();
        }

        /**
         * Gets the <code>Object</code> associated with the specified key.
         *
         * @param key a string containing the specified <code>key</code>
         * @return the binding <code>Object</code> stored with this key; if there
         *         are no keys, it will return <code>null</code>
         * @see javax.swing.Action#getValue
         */
        public Object getValue(String key)
        {
            return act.getValue(key);
        }

        /**
         * Sets the <code>Value</code> associated with the specified key.
         *
         * @param key      the <code>String</code> that identifies the stored object
         * @param newValue the <code>Object</code> to store using this key
         * @see javax.swing.Action#putValue
         */
        public void putValue(String key, Object newValue)
        {
            act.putValue(key, newValue);
        }

        /**
         * This method gets called when a bound property is changed.
         *
         * @param evt A PropertyChangeEvent object describing the event source
         *            and the property that has changed.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());

            // Small hack to update GUI on action completion
            if (evt.getPropertyName().equals("enabled") && evt.getNewValue() == Boolean.TRUE)
            {
                updateFullSyncButton();
                updateSyncDatesView();
            }
        }
    }

    /**
     * Simple wrapper for preferences object. Makes integer properties feel like Strings.
     */
    public static class ServicePreferencesWrapper
    {
        private ServicePreferences prefs;

        /**
         * Creates new wrapper.
         *
         * @param prefobject preferences object.
         */
        public ServicePreferencesWrapper(ServicePreferences prefobject)
        {
            this.prefs = prefobject;
        }

        /**
         * Returns string representation of <code>syncPeriod</code> property.
         *
         * @return string representation.
         */
        public String getSyncPeriod()
        {
            return Integer.toString(prefs.getSyncPeriod());
        }

        /**
         * Sets <code>syncPeriod</code> property from string value.
         *
         * @param period string representation of period.
         */
        public void setSyncPeriod(String period)
        {
            prefs.setSyncPeriod(Integer.parseInt(period));
        }
    }

    /**
     * Listens for Periodical mode selection radio-button and enables / disables
     * field of period entry.
     */
    private class PeriodicalSelectionListener implements ItemListener
    {
        /**
         * Invoked when an item has been selected or deselected by the user.
         * The code written for this method performs the operations
         * that need to occur when an item is selected (or deselected).
         */
        public void itemStateChanged(ItemEvent e)
        {
            tfSyncPeriod.setEnabled(rbModePeriodical.isSelected());
        }
    }

    /**
     * Listens for changes of Feeds List check-box and enables/disables
     * Full synchronization action accordingly.
     */
    private class FullSyncEnabled implements ActionListener
    {
        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e)
        {
            updateFullSyncButton();
        }
    }
}
