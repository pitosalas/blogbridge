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
// $Id: TagsPreferencesPanel.java,v 1.10 2007/04/19 11:40:04 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.jgoodies.binding.adapter.DocumentAdapter;
import com.jgoodies.binding.adapter.RadioButtonAdapter;
import com.jgoodies.binding.adapter.ToggleButtonAdapter;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.value.BufferedValueModel;
import com.jgoodies.binding.value.ValueModel;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.service.ServicePreferences;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Panel which is used to tune tags preferences.
 */
public class TagsPreferencesPanel extends JPanel
{
    private UserPreferences userPrefs;

    private JRadioButton    rbStorageNone;
    private JRadioButton    rbStorageBBS;
    private JRadioButton    rbStorageDelicious;

    private JCheckBox       chAutoFetch;
    private JLabel          lbDeliciousUser;
    private JTextField      tfDeliciousUser;
    private JLabel          lbDeliciousPassword;
    private JPasswordField  tfDeliciousPassword;
    private JTextArea       lbBBSWording;
    private JTextArea       lbDIUWording;

    private JCheckBox       chPinTagging;
    private JTextField      tfPinTags;

    /**
     * Creates tags preferences panel.
     *
     * @param aUserPrefs        user preferences.
     * @param aServicePrefs     service preferences.
     * @param triggerChannel    trigger channel.
     */
    public TagsPreferencesPanel(UserPreferences aUserPrefs, ServicePreferences aServicePrefs,
        ValueModel triggerChannel)
    {
        userPrefs = aUserPrefs;

        initComponents(aServicePrefs.isAccountInformationEntered(), triggerChannel);
        build();

        storageTypeSelected(userPrefs.getTagsStorage());
    }

    /**
     * Initializes components.
     *
     * @param bbsAllowed        <code>TRUE</code> if BB Service selection is allowed.
     * @param triggerChannel    changes commit trigger channel.
     */
    private void initComponents(boolean bbsAllowed, ValueModel triggerChannel)
    {
        initRadioButtons(bbsAllowed, triggerChannel);

        tfDeliciousUser = new JTextField();
        tfDeliciousUser.setDocument(new DocumentAdapter(new BufferedValueModel(
            new PropertyAdapter(this.userPrefs, UserPreferences.PROP_TAGS_DELICIOUS_USER),
            triggerChannel)));

        tfDeliciousPassword = new JPasswordField();
        tfDeliciousPassword.setDocument(new DocumentAdapter(new BufferedValueModel(
            new PropertyAdapter(this.userPrefs, UserPreferences.PROP_TAGS_DELICIOUS_PASSWORD),
            triggerChannel)));

        ValueModel autoFetchModel = new BufferedValueModel(
            new PropertyAdapter(userPrefs, UserPreferences.PROP_TAGS_AUTOFETCH),
            triggerChannel);

        chAutoFetch = ComponentsFactory.createCheckBox(
            Strings.message("userprefs.tab.tags.autofetch"),
            new ToggleButtonAdapter(autoFetchModel));

        lbBBSWording = ComponentsFactory.createWrappedMultilineLabel(
            Strings.message("userprefs.tab.tags.wording.bbservice"));

        lbDIUWording = ComponentsFactory.createWrappedMultilineLabel(
            Strings.message("userprefs.tab.tags.wording.delicious"));

        chPinTagging = ComponentsFactory.createCheckBox(
            "Tag pins with",
            new ToggleButtonAdapter(new BufferedValueModel(
                new PropertyAdapter(userPrefs, UserPreferences.PROP_PIN_TAGGING),
                triggerChannel)));

        tfPinTags = new JTextField();
        tfPinTags.setDocument(new DocumentAdapter(new BufferedValueModel(
            new PropertyAdapter(userPrefs, UserPreferences.PROP_PIN_TAGS),
            triggerChannel)));
    }

    /**
     * Initializes radio-buttons.
     *
     * @param bbsAllowed        <code>TRUE</code> if BB Service selection is allowed.
     * @param triggerChannel    changes commit trigger channel.
     */
    private void initRadioButtons(boolean bbsAllowed, ValueModel triggerChannel)
    {
        ValueModel storageModel = new BufferedValueModel(
            new PropertyAdapter(userPrefs, UserPreferences.PROP_TAGS_STORAGE), triggerChannel);
        storageModel.addValueChangeListener(new StorageTypeListener());

        rbStorageNone = ComponentsFactory.createRadioButton(
            Strings.message("userprefs.tab.tags.dont.share"));
        rbStorageNone.setModel(new RadioButtonAdapter(storageModel,
            UserPreferences.TAGS_STORAGE_NONE));

        rbStorageBBS = ComponentsFactory.createRadioButton(
            Strings.message("userprefs.tab.tags.bbservice"));
        rbStorageBBS.setModel(new RadioButtonAdapter(storageModel,
            UserPreferences.TAGS_STORAGE_BB_SERVICE));

        rbStorageDelicious = ComponentsFactory.createRadioButton(
            Strings.message("userprefs.tab.tags.delicious"));
        rbStorageDelicious.setModel(new RadioButtonAdapter(storageModel,
            UserPreferences.TAGS_STORAGE_DELICIOUS));

        rbStorageBBS.setEnabled(bbsAllowed);
    }

    /**
     * Builds the layout of the panel.
     */
    private void build()
    {
        BBFormBuilder builder = new BBFormBuilder("7dlu, 7dlu, max(p;60dlu), 4dlu, 50dlu, 0:grow", this);
        builder.setDefaultDialogBorder();

        builder.appendSeparator(Strings.message("userprefs.tab.tags.integration.type"));
        builder.setLeadingColumnOffset(1);
        builder.append(rbStorageNone, 5);
        builder.appendUnrelatedComponentsGapRow(2);

        builder.append(rbStorageBBS, 5);
        builder.setLeadingColumnOffset(2);
        builder.append(lbBBSWording, 4);
        builder.appendUnrelatedComponentsGapRow(2);

        builder.setLeadingColumnOffset(1);
        builder.append(rbStorageDelicious, 5);
        builder.setLeadingColumnOffset(2);
        builder.append(lbDIUWording, 4);
        lbDeliciousUser = builder.append(Strings.message("userprefs.tab.tags.delicious.user"), 1);
        builder.append(tfDeliciousUser);
        builder.nextLine();
        lbDeliciousPassword = builder.append(Strings.message("userprefs.tab.tags.delicious.password"), 1);
        builder.append(tfDeliciousPassword);

        builder.appendSeparator(Strings.message("userprefs.tab.tags.options"));
        builder.setLeadingColumnOffset(1);
        builder.append(chAutoFetch, 5);
        builder.append(chPinTagging, 2);
        builder.append(tfPinTags, 2);

        lbDeliciousUser.setLabelFor(tfDeliciousUser);
        lbDeliciousPassword.setLabelFor(tfDeliciousPassword);
    }

    /**
     * Changes controls availability depending on the selected storage type.
     *
     * @param type storage type.
     */
    private void storageTypeSelected(int type)
    {
        boolean enableBBSControls = (type == UserPreferences.TAGS_STORAGE_BB_SERVICE) && rbStorageBBS.isEnabled();
        boolean enableDeliciousControls = (type == UserPreferences.TAGS_STORAGE_DELICIOUS);

        lbBBSWording.setEnabled(enableBBSControls);

        lbDeliciousUser.setEnabled(enableDeliciousControls);
        tfDeliciousUser.setEnabled(enableDeliciousControls);
        lbDeliciousPassword.setEnabled(enableDeliciousControls);
        tfDeliciousPassword.setEnabled(enableDeliciousControls);
        lbDIUWording.setEnabled(enableDeliciousControls);

        boolean enableTaggingControls = enableDeliciousControls || enableBBSControls;
        chAutoFetch.setEnabled(enableTaggingControls);
        chPinTagging.setEnabled(enableTaggingControls);
        tfPinTags.setEnabled(enableTaggingControls);
    }

    /**
     * Listens to changes in storage type.
     */
    private class StorageTypeListener implements PropertyChangeListener
    {
        /**
         * Invoked when property changes.
         *
         * @param evt event object.
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            storageTypeSelected((Integer)evt.getNewValue());
        }
    }
}
