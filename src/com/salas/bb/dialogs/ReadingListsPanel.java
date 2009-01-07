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
// $Id: ReadingListsPanel.java,v 1.7 2006/06/13 08:13:44 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.MandatoryCheckBoxController;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.domain.ReadingList;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.binding.value.BufferedValueModel;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.adapter.ToggleButtonAdapter;
import com.jgoodies.binding.adapter.RadioButtonAdapter;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

/**
 * Panel with reading lists settings.
 */
public class ReadingListsPanel extends JPanel
{
    private static final String PERIOD_ONCE_PER_RUN = Strings.message("period.run");
    private static final String PERIOD_DAILY        = Strings.message("period.day");
    private static final String PERIOD_HOURLY       = Strings.message("period.hour");

    private final UserPreferences   prefs;
    private final ValueModel        triggerChannel;

    private JRadioButton    rbUpdateManually;
    private JRadioButton    rbUpdatePeriodically;
    private JRadioButton    rbChangeSilently;
    private JRadioButton    rbChangeWithNotification;
    private JRadioButton    rbChangeWithConfirmation;
    private JCheckBox       chUpdateFeeds;
    private JCheckBox       chUpdateReadingLists;
    private JComboBox       cbUpdatePeriod;

    /**
     * Creates panel.
     *
     * @param aUserPreferences  user preferences object.
     * @param aTriggerChannel   trigger channel.
     */
    public ReadingListsPanel(UserPreferences aUserPreferences, ValueModel aTriggerChannel)
    {
        prefs = aUserPreferences;
        triggerChannel = aTriggerChannel;

        initComponents();
        layoutComponents();
    }

    /** Lays out components. */
    private void layoutComponents()
    {
        JComponent wording = ComponentsFactory.createWrappedMultilineLabel(
            Strings.message("userprefs.tab.readinglists.wording"));

        BBFormBuilder builder = new BBFormBuilder("7dlu, p, 4dlu, p, 0:grow", this);
        builder.setDefaultDialogBorder();

        builder.append(wording, 5);
        builder.appendUnrelatedComponentsGapRow(2);

        builder.appendSeparator(Strings.message("userprefs.tab.readinglists.updates"));
        builder.setLeadingColumnOffset(1);
        builder.append(rbUpdateManually, 4);
        builder.append(rbUpdatePeriodically, cbUpdatePeriod);

        builder.setLeadingColumnOffset(0);
        builder.appendSeparator(Strings.message("userprefs.tab.readinglists.command"));
        builder.setLeadingColumnOffset(1);
        builder.append(chUpdateFeeds, 4);
        builder.append(chUpdateReadingLists, 4);

        builder.setLeadingColumnOffset(0);
        builder.appendSeparator(Strings.message("userprefs.tab.readinglists.action"));
        builder.setLeadingColumnOffset(1);
        builder.append(rbChangeSilently, 4);
        builder.append(rbChangeWithNotification, 4);
        builder.append(rbChangeWithConfirmation, 4);
    }

    /** Initializes components. */
    private void initComponents()
    {
        // Updating
        rbUpdateManually = ComponentsFactory.createRadioButton(
            Strings.message("userprefs.tab.readinglists.updates.manual"));
        rbUpdatePeriodically = ComponentsFactory.createRadioButton(
            Strings.message("userprefs.tab.readinglists.updates.periodical"));
        cbUpdatePeriod = new JComboBox(new Object[]
        {
            PERIOD_ONCE_PER_RUN, PERIOD_DAILY, PERIOD_HOURLY
        });

        ButtonGroup g1 = new ButtonGroup();
        g1.add(rbUpdateManually);
        g1.add(rbUpdatePeriodically);

        setUpdatePeriodState();

        PeriodMonitor monitor = new PeriodMonitor(new BufferedValueModel(
            new PropertyAdapter(prefs, UserPreferences.PROP_READING_LIST_UPDATE_PERIOD),
            triggerChannel));

        rbUpdateManually.addChangeListener(monitor);
        rbUpdatePeriodically.addChangeListener(monitor);
        cbUpdatePeriod.addItemListener(monitor);

        ValueModel updatesModel = new BufferedValueModel(
            new PropertyAdapter(prefs, UserPreferences.PROP_ON_READING_LIST_UPDATE_ACTIONS),
            triggerChannel);

        // Applying changes
        rbChangeSilently = ComponentsFactory.createRadioButton(
            Strings.message("userprefs.tab.readinglists.action.accept.silently"),
            new RadioButtonAdapter(updatesModel,
            new Integer(UserPreferences.RL_UPDATE_NONE)));
        rbChangeWithNotification = ComponentsFactory.createRadioButton(
            Strings.message("userprefs.tab.readinglists.action.notify"),
            new RadioButtonAdapter(updatesModel,
            new Integer(UserPreferences.RL_UPDATE_NOTIFY)));
        rbChangeWithConfirmation = ComponentsFactory.createRadioButton(
            Strings.message("userprefs.tab.readinglists.action.confirm"),
            new RadioButtonAdapter(updatesModel,
            new Integer(UserPreferences.RL_UPDATE_CONFIRM)));

        // Get Latest command actions
        chUpdateFeeds = ComponentsFactory.createCheckBox(
            Strings.message("userprefs.tab.readinglists.command.latest.articles"),
            new ToggleButtonAdapter(new BufferedValueModel(new PropertyAdapter(prefs,
                UserPreferences.PROP_UPDATE_FEEDS), triggerChannel)));
        chUpdateReadingLists = ComponentsFactory.createCheckBox(
            Strings.message("userprefs.tab.readinglists.command.lists.changes"),
            new ToggleButtonAdapter(new BufferedValueModel(new PropertyAdapter(prefs,
                UserPreferences.PROP_UPDATE_READING_LISTS), triggerChannel)));

        new MandatoryCheckBoxController(chUpdateFeeds, chUpdateReadingLists);
    }

    /**
     * Sets initial update period state.
     */
    private void setUpdatePeriodState()
    {
        long period = prefs.getReadingListUpdatePeriod();

        if (period == ReadingList.PERIOD_NEVER)
        {
            rbUpdateManually.setSelected(true);
            cbUpdatePeriod.setEnabled(false);
        } else
        {
            rbUpdatePeriodically.setSelected(true);
            cbUpdatePeriod.setEnabled(true);

            Object select;
            if (period == ReadingList.PERIOD_ONCE_PER_RUN)
            {
                select = PERIOD_ONCE_PER_RUN;
            } else if (period == ReadingList.PERIOD_DAILY)
            {
                select = PERIOD_DAILY;
            } else
            {
                select = PERIOD_HOURLY;
            }

            cbUpdatePeriod.setSelectedItem(select);
        }
    }

    /**
     * Monitors the changes in period settings and updates given property.
     */
    private class PeriodMonitor implements ChangeListener, ItemListener
    {
        private final ValueModel property;

        private boolean manual;
        private Object  selectedPeriod;

        /**
         * Creates monitor for updating given property.
         *
         * @param aProperty property.
         */
        public PeriodMonitor(ValueModel aProperty)
        {
            property = aProperty;

            manual = rbUpdateManually.isSelected();
            selectedPeriod = cbUpdatePeriod.getSelectedItem();
        }

        /**
         * Invoked when the target of the listener has changed its state.
         *
         * @param e a ChangeEvent object
         */
        public void stateChanged(ChangeEvent e)
        {
            manual = rbUpdateManually.isSelected();
            updateProperty();

            cbUpdatePeriod.setEnabled(!manual);
        }

        /**
         * Invoked when an item has been selected or deselected by the user. The code written for this
         * method performs the operations that need to occur when an item is selected (or deselected).
         */
        public void itemStateChanged(ItemEvent e)
        {
            selectedPeriod = cbUpdatePeriod.getSelectedItem();
            updateProperty();

            rbUpdatePeriodically.setSelected(true);
        }

        /** Updates property value. */
        private void updateProperty()
        {
            long period = ReadingList.PERIOD_NEVER;

            if (!manual)
            {
                period = selectedPeriod == PERIOD_ONCE_PER_RUN
                    ? ReadingList.PERIOD_ONCE_PER_RUN
                    : selectedPeriod == PERIOD_DAILY
                        ? ReadingList.PERIOD_DAILY
                        : ReadingList.PERIOD_HOURLY;
            }

            property.setValue(new Long(period));
        }
    }
}
