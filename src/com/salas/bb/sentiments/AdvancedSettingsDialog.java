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
// $Id: AdvancedSettingsDialog.java,v 1.9 2008/03/18 06:56:43 spyromus Exp $
//

package com.salas.bb.sentiments;

import com.jgoodies.binding.adapter.BoundedRangeAdapter;
import com.jgoodies.binding.adapter.DocumentAdapter;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.value.BufferedValueModel;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.util.Resizer;
import com.salas.bb.utils.ResourceID;
import com.salas.bb.utils.SpinnerModelAdapter;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Advanced settings dialog.
 */
public class AdvancedSettingsDialog extends AbstractDialog
{
    private static final ColorListCellRenderer colorListCellRenderer =
        new ColorListCellRenderer(null);

    private JTextArea   taPositive;
    private JTextArea   taNegative;
    private JSpinner    spnPositivePercentage;
    private JSpinner    spnNegativePercentage;
    private JComboBox   cbPositiveColor;
    private JComboBox   cbNegativeColor;

    private SentimentsConfig config;
    private ValueModel  trigger;

    /**
     * Creates a settings dialog.
     *
     * @param parent    parent.
     * @param config    configuration.
     * @param trigger   trigger channel.
     */
    public AdvancedSettingsDialog(Dialog parent, SentimentsConfig config, ValueModel trigger)
    {
        super(parent, Strings.message("sentiment.analysis"));
        this.config = config;
        this.trigger = trigger;

        // Initialize components
        taPositive = createExpressionsArea(SentimentsConfig.PROP_POSITIVE_EXPRESSIONS);
        taNegative = createExpressionsArea(SentimentsConfig.PROP_NEGATIVE_EXPRESSIONS);

        spnPositivePercentage = createThresholdSpinner(SentimentsConfig.PROP_POSITIVE_THRESHOLD);
        spnNegativePercentage = createThresholdSpinner(SentimentsConfig.PROP_NEGATIVE_THRESHOLD);

        cbPositiveColor = createColorComboBox(SentimentsConfig.PROP_POSITIVE_COLOR, trigger);
        cbNegativeColor = createColorComboBox(SentimentsConfig.PROP_NEGATIVE_COLOR, trigger);
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
            Strings.message("sentiment.analysis.advanced.header"),
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
     * Creates button bar.
     *
     * @return bar.
     */
    private JComponent buildButtonBar()
    {
        return ButtonBarFactory.buildHelpCloseBar(new JButton(new ResetAction()), createCloseButton(true));
    }

    /**
     * Returns the main panel.
     *
     * @return panel.
     */
    private Component buildMainPanel()
    {
        // Areas
        BBFormBuilder builder = new BBFormBuilder("p, 14dlu:grow, p, 2dlu, p, 2dlu, p, 7dlu, p, 2dlu, p");

        builder.append(ComponentsFactory.createWrappedMultilineLabel(
            Strings.message("sentiment.analysis.advanced.instructions")), 11);

        builder.appendUnrelatedComponentsGapRow(2);

        builder.append(Strings.message("sentiment.analysis.advanced.positive"));
        builder.append(Strings.message("sentiment.analysis.advanced.positive.factor"),
            spnPositivePercentage, new JLabel(""));
        builder.append(Strings.message("sentiment.analysis.advanced.color"), cbPositiveColor);
        builder.appendRelatedComponentsGapRow(2);
        builder.appendRow("50dlu:grow");
        builder.append(new JScrollPane(taPositive), 11, CellConstraints.FILL, CellConstraints.FILL);

        builder.appendUnrelatedComponentsGapRow(2);

        builder.append(Strings.message("sentiment.analysis.advanced.negative"));
        builder.append(Strings.message("sentiment.analysis.advanced.negative.factor"),
            spnNegativePercentage, new JLabel(""));
        builder.append(Strings.message("sentiment.analysis.advanced.color"), cbNegativeColor);
        builder.appendRelatedComponentsGapRow(2);
        builder.appendRow("50dlu:grow");
        builder.append(new JScrollPane(taNegative), 11, CellConstraints.FILL, CellConstraints.FILL);

        builder.appendUnrelatedComponentsGapRow(2);
        
        return builder.getPanel();
    }

    protected void resizeHook(JComponent component)
    {
        component.setPreferredSize(Resizer.DEFAULT.fromWidth(600));
    }

    /**
     * Helps to create spinners.
     *
     * @param property property to spin.
     *
     * @return spinner.
     */
    private JSpinner createThresholdSpinner(String property)
    {
        SpinnerModelAdapter model = new SpinnerModelAdapter(new BoundedRangeAdapter(
                new BufferedValueModel(new PropertyAdapter(config, property), trigger), 0,-100, 100));
        model.setStepSize(1);
        return new JSpinner(model);
    }

    /**
     * Helps to create expression areas.
     *
     * @param property property to wrap.
     *
     * @return area.
     */
    private JTextArea createExpressionsArea(String property)
    {
        JTextArea area = new JTextArea();
        area.setDocument(new DocumentAdapter(new BufferedValueModel(new PropertyAdapter(config, property), trigger)));
        return area;
    }

    /**
     * Creates a color combo-box.
     *
     * @param property  property to wrap.
     * @param trigger   trigger to use for data commit.
     *
     * @return combo-box.
     */
    private JComboBox createColorComboBox(String property, ValueModel trigger)
    {
        JComboBox box = new JComboBox(new ColorAdapter(new BufferedValueModel(
                        new PropertyAdapter(config, property), trigger)));
        box.setRenderer(colorListCellRenderer);
        return box;
    }

    /**
     * Resets the settings to factory defaults.
     */
    private class ResetAction extends AbstractAction
    {
        public ResetAction()
        {
            super(Strings.message("sentiment.analysis.advanced.reset"));
        }

        public void actionPerformed(ActionEvent e)
        {
            taPositive.setText(SentimentsConfig.DEFAULT_POSITIVE_EXPRESSIONS);
            taNegative.setText(SentimentsConfig.DEFAULT_NEGATIVE_EXPRESSIONS);
            spnPositivePercentage.setValue(SentimentsConfig.DEFAULT_POSITIVE_THRESHOLD);
            spnNegativePercentage.setValue(SentimentsConfig.DEFAULT_NEGATIVE_THRESHOLD);
            cbPositiveColor.setSelectedItem(SentimentsConfig.DEFAULT_POSITIVE_COLOR);
            cbNegativeColor.setSelectedItem(SentimentsConfig.DEFAULT_NEGATIVE_COLOR);
        }
    }

    /**
     * Color adapter.
     */
    private static class ColorAdapter extends ColorComboBoxAdapter
    {
        /**
         * Creates an adapter.
         *
         * @param model model.
         */
        public ColorAdapter(ValueModel model)
        {
            super(new Color[] {
                Color.decode("#FF344E"), Color.decode("#FFAF2F"), Color.decode("#F6E12A"), Color.decode("#99DE26"),
                Color.decode("#24A4E0"), Color.decode("#F361B4"), Color.decode("#9E9E9E")
            }, model);
        }
    }
}