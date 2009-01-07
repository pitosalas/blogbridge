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
// $Id: GeneralPreferencesPanel.java,v 1.32 2006/10/31 13:17:18 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.jgoodies.binding.adapter.DocumentAdapter;
import com.jgoodies.binding.adapter.ToggleButtonAdapter;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.value.BufferedValueModel;
import com.jgoodies.binding.value.ValueModel;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.views.settings.FeedRenderingSettings;
import com.salas.bb.views.settings.RenderingSettingsNames;
import com.salas.bb.views.themes.Theme;
import com.salas.bb.views.themes.ThemeSupport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Builds the Behavior tab in the preferences dialog.
 */
public final class GeneralPreferencesPanel extends JPanel
{
    private JCheckBox   chReadOnChanChange;
    private JCheckBox   chReadOnGuideChange;
    private JTextField  purgeCount;
    private JCheckBox   chDoNotRemoveUnread;
    private JTextField  rssPollInterval;
    private JCheckBox   chReadOnDelay;
    private JTextField  tfReadOnDelaySeconds;

    private JComboBox   cbTheme;
    private JComboBox   cbFontFamilies;
    private JCheckBox   chShowToolbar;
    private JCheckBox   chShowToolbarLabels;

    // Instance Creation ****************************************************

    /**
     * Constructs the <i>General </i> panel for the preferences dialog.
     *
     * @param settings       the behavior related settings
     * @param aCrs           channel rendering settings.
     * @param triggerChannel triggers a commit when apply is pressed
     */
    public GeneralPreferencesPanel(UserPreferences settings, FeedRenderingSettings aCrs,
        ValueModel triggerChannel)
    {
        initComponents(settings, aCrs, triggerChannel);
        build();
    }

    // Component Creation and Initialization ********************************

    private void initComponents(UserPreferences settings, FeedRenderingSettings frs,
        ValueModel triggerChannel)
    {
        chReadOnChanChange = ComponentsFactory.createCheckBox(
            Strings.message("userprefs.tab.general.when.changing.feeds.mark.all.articles.read"),
            new ToggleButtonAdapter(
                new BufferedValueModel(new PropertyAdapter(settings,
                    UserPreferences.PROP_MARK_READ_WHEN_CHANGING_CHANNELS), triggerChannel)));

        chReadOnGuideChange = ComponentsFactory.createCheckBox(
            Strings.message("userprefs.tab.general.when.changing.guides.mark.all.articles.read"),
            new ToggleButtonAdapter(
                new BufferedValueModel(new PropertyAdapter(settings,
                    UserPreferences.PROP_MARK_READ_WHEN_CHANGING_GUIDES), triggerChannel)));

        chReadOnDelay = ComponentsFactory.createCheckBox(
            Strings.message("userprefs.tab.general.mark.article.as.read.on.delay"),
            new ToggleButtonAdapter(
                new BufferedValueModel(new PropertyAdapter(settings,
                    UserPreferences.PROP_MARK_READ_AFTER_DELAY), triggerChannel)));

        tfReadOnDelaySeconds = new JTextField();
        tfReadOnDelaySeconds.setDocument(new DocumentAdapter(new BufferedValueModel(
            new PropertyAdapter(settings, "markReadAfterSecondsString"), triggerChannel)));

        configurePurgeControls(settings, triggerChannel);

        rssPollInterval = new JTextField();
        rssPollInterval.setDocument(new DocumentAdapter(new BufferedValueModel(
            new PropertyAdapter(settings, "rssPollIntervalString"), triggerChannel)));

        cbTheme = new JComboBox();
        ValueModel valueModel = new BufferedValueModel(
            new PropertyAdapter(frs, RenderingSettingsNames.THEME), triggerChannel);
        cbTheme.setModel(new ThemeListModel(valueModel));

        cbFontFamilies = new JComboBox();
        final BufferedValueModel vmMainFont = new BufferedValueModel(
            new MainFontFamilyValueModel(frs), triggerChannel);
        cbFontFamilies.setModel(new FontFamiliesListModel(vmMainFont));

        valueModel.addValueChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                Theme theme = (Theme)evt.getNewValue();
                Font newFont = theme.getMainFontDirect();
                vmMainFont.setValue(newFont.getFamily());
            }
        });

        chShowToolbar = ComponentsFactory.createCheckBox(
            Strings.message("userprefs.tab.general.show.toolbar"),
            new ToggleButtonAdapter(new BufferedValueModel(new PropertyAdapter(settings,
                UserPreferences.PROP_SHOW_TOOLBAR), triggerChannel)));
        chShowToolbarLabels = ComponentsFactory.createCheckBox(
            Strings.message("userprefs.tab.general.show.toolbar.labels"),
            new ToggleButtonAdapter(new BufferedValueModel(new PropertyAdapter(settings,
            UserPreferences.PROP_SHOW_TOOLBAR_LABELS), triggerChannel)));

        chShowToolbar.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onShowToolbar();
            }
        });
        onShowToolbar();
    }

    /**
     * Configures the purge controls.
     *
     * @param settings          settings.
     * @param triggerChannel    trigger.
     */
    private void configurePurgeControls(UserPreferences settings, ValueModel triggerChannel)
    {
        PropertyAdapter propCheck, propCount;
        propCheck = new PropertyAdapter(settings, UserPreferences.PROP_PRESERVE_UNREAD);
        propCount = new PropertyAdapter(settings, "purgeCountString");

        BufferedValueModel modelCheck = null, modelCount;

        // The order of initialization dictates the order of events upon change.
        // We need checkbox event to be fired before the count when it wasn't selected initially,
        // and vice versa when it was.
        if (!settings.isPreserveUnread()) modelCheck = new BufferedValueModel(propCheck, triggerChannel);
        modelCount = new BufferedValueModel(propCount, triggerChannel);
        if (settings.isPreserveUnread()) modelCheck = new BufferedValueModel(propCheck, triggerChannel);

        chDoNotRemoveUnread = ComponentsFactory.createCheckBox(
            Strings.message("userprefs.tab.general.do.not.purge.unread.articles"),
            new ToggleButtonAdapter(modelCheck));

        purgeCount = new JTextField();
        purgeCount.setDocument(new DocumentAdapter(modelCount));
    }

    private void onShowToolbar()
    {
        chShowToolbarLabels.setEnabled(chShowToolbar.isSelected());
    }

    // Building *************************************************************

    /**
     * Creates a FormLayout and adds the UI components using a PanelBuilder.
     */
    private void build()
    {
        BBFormBuilder builder = new BBFormBuilder("7dlu, left:p, 2dlu, 20dlu, 2dlu, 0:grow", this);
        builder.setDefaultDialogBorder();

        builder.setLeadingColumnOffset(1);

        builder.appendSeparator(Strings.message("userprefs.tab.general.separator.theme"));
        builder.append(buildThemePanel(), 5);
        builder.nextLine();
        builder.append(createToolbarPanel(), 5);

        builder.appendSeparator(Strings.message("userprefs.tab.general.separator.behavior"));
        builder.append(chReadOnChanChange, 5);
        builder.append(chReadOnGuideChange, 5);
        builder.append(chReadOnDelay, tfReadOnDelaySeconds);
        builder.append(Strings.message("userprefs.tab.general.seconds"), 1);

        builder.appendSeparator(Strings.message("userprefs.tab.general.separator.updates.and.cleanups"));
        builder.append(Strings.message("userprefs.tab.general.feed.polling.interval"), rssPollInterval);
        builder.nextLine();
        builder.append(Strings.message("userprefs.tab.general.articles.remaining.after.purge"), purgeCount);
        builder.nextLine();
        builder.append(chDoNotRemoveUnread, 5);
    }

    /** Creates toolbar options panel. */
    private JComponent createToolbarPanel()
    {
        BBFormBuilder builder = new BBFormBuilder("p, 4dlu, p");
        builder.append(chShowToolbar);
        builder.append(chShowToolbarLabels);

        return builder.getPanel();
    }

    /**
     * Creates theme panel.
     *
     * @return panel.
     */
    private JComponent buildThemePanel()
    {
        BBFormBuilder builder = new BBFormBuilder("min(75dlu;p), 14dlu, p, 5dlu, min(75dlu;p)");

        builder.append(cbTheme);
        builder.append(Strings.message("userprefs.tab.general.font"), cbFontFamilies);

        return builder.getPanel();
    }

    /**
     * Themes model.
     */
    private static class ThemeListModel extends ThemeSupport.ThemesComboBoxModel
    {
        private ValueModel model;

        /**
         * Create model.
         *
         * @param aModel property to wrap.
         */
        public ThemeListModel(ValueModel aModel)
        {
            model = aModel;
        }

        /**
         * Returns currently selected item.
         *
         * @return item.
         */
        public Object getSelectedItem()
        {
            return model.getValue();
        }

        /**
         * Changes selection.
         *
         * @param anItem new selection.
         */
        public void setSelectedItem(Object anItem)
        {
            model.setValue(anItem);
        }
    }
    /**
     * Themes model.
     */

    private static class FontFamiliesListModel extends ThemeSupport.FontsComboBoxModel
    {
        private ValueModel model;

        /**
         * Create model.
         *
         * @param aModel property to wrap.
         */
        public FontFamiliesListModel(ValueModel aModel)
        {
            model = aModel;
        }

        /**
         * Returns currently selected item.
         *
         * @return item.
         */
        public Object getSelectedItem()
        {
            return model.getValue();
        }

        /**
         * Changes selection.
         *
         * @param anItem new selection.
         */
        public void setSelectedItem(Object anItem)
        {
            model.setValue(anItem);
        }
    }

    /**
     * Main font value model.
     */
    private static class MainFontFamilyValueModel implements ValueModel
    {
        private final FeedRenderingSettings feedRS;

        /**
         * Fetches currently selected main font from the rendering settings.
         *
         * @param frs settings.
         */
        public MainFontFamilyValueModel(FeedRenderingSettings frs)
        {
            feedRS = frs;
        }

        /**
         * Returns the family of currently selected font.
         *
         * @return family.
         */
        public Object getValue()
        {
            return getFont().getFamily();
        }

        /**
         * Returns currently set main content font.
         *
         * @return font.
         */
        private Font getFont()
        {
            return feedRS.getMainContentFont();
        }

        /**
         * Sets new value.
         *
         * @param object new value.
         */
        public void setValue(Object object)
        {
            String name = (String)object;
            int size = getFont().getSize();
            feedRS.setMainContentFont(Font.decode(name + "-" + size));
        }

        /**
         * Adds listener.
         *
         * @param listener listener.
         */
        public void addValueChangeListener(PropertyChangeListener listener)
        {
        }

        /**
         * Removes listener.
         *
         * @param listener listener.
         */
        public void removeValueChangeListener(PropertyChangeListener listener)
        {
        }
    }
}
