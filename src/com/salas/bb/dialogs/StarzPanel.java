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
// $Id: StarzPanel.java,v 1.25 2008/02/28 15:59:52 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.jgoodies.binding.adapter.BoundedRangeAdapter;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.value.BufferedValueModel;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.salas.bb.core.GlobalController;
import com.salas.bb.domain.prefs.StarzPreferences;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.ComponentsFactory;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Builds the Behavior tab in the preferences dialog.
 */
public final class StarzPanel extends JPanel
{
    private ValueModel          triggerChannel;
    private StarzPreferences    preferences;

    private JComponent          whatIsBox;
    private JLabel              lessImportant;
    private JLabel              moreImportant;
    private JLabel              activity;
    private JLabel              popularity;
    private JLabel              clickthroughs;
    private JLabel              feedViews;
    private JSlider             activitySlider;
    private JSlider             popularitySlider;
    private JSlider             clickthroughsSlider;
    private JSlider             feedViewsSlider;

    private int                 initActivityWeight;
    private int                 initImportanceWeight;
    private int                 initClickthroughsWeight;
    private int                 initFeedViewsWeight;

    /**
     * Constructs the <i>BlogBridge Starz </i> panel for the preferences dialog.
     *
     * @param settings the behavior related settings.
     * @param aChannel triggers a commit when apply is pressed.
     */
    public StarzPanel(StarzPreferences settings, ValueModel aChannel)
    {
        triggerChannel = aChannel;
        preferences = settings;
        initComponents();
        build();

        // Record initial values of weights to be able to compare them on commit
        saveInitialWeights();
        triggerChannel.addValueChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (Boolean.TRUE.equals(evt.getNewValue())) doRepaint();
            }
        });
    }

    /**
     * Saves the weights of parameters for future comparison.
     */
    private void saveInitialWeights()
    {
        initActivityWeight = preferences.getActivityWeight();
        initImportanceWeight = preferences.getInlinksWeight();
        initClickthroughsWeight = preferences.getClickthroughsWeight();
        initFeedViewsWeight = preferences.getFeedViewsWeight();
    }

    /**
     * Performs repaint of feeds list if weights changed.
     */
    private void doRepaint()
    {
        if (preferences.getActivityWeight() != initActivityWeight ||
            preferences.getInlinksWeight() != initImportanceWeight ||
            preferences.getClickthroughsWeight() != initClickthroughsWeight ||
            preferences.getFeedViewsWeight() != initFeedViewsWeight)
        {
            saveInitialWeights();
            GlobalController.SINGLETON.getMainFrame().getFeedsPanel().repaint();
        }
    }

    /**
     * Build the dialog panel.
     */
    private void initComponents()
    {
        whatIsBox = ComponentsFactory.createWrappedMultilineLabel(Strings.message("blogstarz.settings.wording"));

        lessImportant = createLabel(
            Strings.message("blogstarz.settings.least.important"),
            Strings.message("blogstarz.settings.least.important.tooltip"));
        moreImportant = createLabel(
            Strings.message("blogstarz.settings.most.important"),
            Strings.message("blogstarz.settings.most.important.tooltip"));
        activity = createLabel(
            Strings.message("blogstarz.settings.activity"),
            Strings.message("blogstarz.settings.activity.tooltip"));
        popularity = createLabel(
            Strings.message("blogstarz.settings.inlink.count"),
            Strings.message("blogstarz.settings.inlink.count.tooltip"));
        clickthroughs = createLabel(
            Strings.message("blogstarz.settings.clickthroughs"),
            Strings.message("blogstarz.settings.clickthroughs.tooltip"));
        feedViews = createLabel(
            Strings.message("blogstarz.settings.feedviews"),
            Strings.message("blogstarz.settings.feedviews.tooltip"));

        activitySlider = createStarzSlider(StarzPreferences.PROP_ACTIVITY_WEIGHT);
        popularitySlider = createStarzSlider(StarzPreferences.PROP_INLINKS_WEIGHT);
        clickthroughsSlider = createStarzSlider(StarzPreferences.PROP_CLICKTHROUGHS_WEIGHT);
        feedViewsSlider = createStarzSlider(StarzPreferences.PROP_FEED_VIEWS_WEIGHT);
    }

    /**
     * Creates a FormLayout and adds the UI components using a PanelBuilder.
     */
    private void build()
    {
        FormLayout layout = new FormLayout(
            "p, right:p, p:grow, right:p:grow",
            "p, min:grow, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p: min:grow");

        PanelBuilder builder = new PanelBuilder(layout, this);
        CellConstraints cc = new CellConstraints();

        builder.add(whatIsBox, cc.xyw(1, 1, 4));

        builder.add(lessImportant, cc.xy(3, 3));
        builder.add(moreImportant, cc.xy(4, 3));

        builder.add(activity, cc.xy(2, 5));
        builder.add(activitySlider, cc.xyw(3, 5, 2));

        builder.add(popularitySlider, cc.xyw(3, 7, 2));
        builder.add(popularity, cc.xy(2, 7));

        builder.add(clickthroughs, cc.xy(2, 9));
        builder.add(clickthroughsSlider, cc.xyw(3, 9, 2));

        builder.add(feedViews, cc.xy(2, 11));
        builder.add(feedViewsSlider, cc.xyw(3, 11, 2));
    }

    /**
     * Helper to create and configure a JSlider.
     *
     * @param propertyName name of property to bind to.
     *
     * @return newly created JSlider.
     */
    private JSlider createStarzSlider(String propertyName)
    {
        JSlider res;
        res = new JSlider();
        res.setOrientation(SwingConstants.HORIZONTAL);
        res.setMaximum(4);
        res.setMinimum(0);
        res.setPaintTicks(true);
        res.setMajorTickSpacing(1);
        res.setSnapToTicks(true);

        res.setModel(new BoundedRangeAdapter(new BufferedValueModel(new PropertyAdapter(
            preferences, propertyName), triggerChannel), 0, 0, 4));

        return res;
    }

    /**
     * Helper to create and configure a JLabel.
     *
     * @param text      name of the Label.
     * @param tooltip   nooltip to display.
     *
     * @return JLabel that was created.
     */
    private JLabel createLabel(String text, String tooltip)
    {
        JLabel res = new JLabel(text);
        res.setToolTipText(tooltip);
        return res;
    }
}