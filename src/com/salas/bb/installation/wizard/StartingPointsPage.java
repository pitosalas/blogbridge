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
// $Id: StartingPointsPage.java,v 1.9 2008/04/07 16:35:23 spyromus Exp $
//

package com.salas.bb.installation.wizard;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.salas.bb.service.ServerService;
import com.salas.bb.utils.feedscollections.CollectionItem;
import com.salas.bb.utils.feedscollections.Picker;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.HeaderPanelExt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Page for starting points selection.
 */
class StartingPointsPage extends DefaultWizardPage
{
    private Picker picker;

    /**
     * Create page.
     *
     * @param aFinish   listener of 'Finish' button.
     * @param aPrev     listener of 'Previous' button.
     * @param aCancel   listener of 'Cancel' button.
     */
    public StartingPointsPage(ActionListener aFinish, ActionListener aPrev, ActionListener aCancel)
    {
        initComponents();
        build(buildButtonBar(aFinish, aPrev, aCancel));
    }

    /**
     * Initialize components.
     */
    private void initComponents()
    {
        picker = new Picker();
        picker.addCollection(ServerService.getStartingPointsURL(),
            Strings.message("collection.collections"), true, Picker.ITEM_TYPE_RL, false);
        picker.addCollection(ServerService.getExpertsURL(),
            Strings.message("collection.experts"), true, Picker.ITEM_TYPE_RL, true);
    }

    /**
     * Bulds bar with buttons.
     */
    private JComponent buildButtonBar(ActionListener aFinish, ActionListener aPrev, ActionListener aCancel)
    {
        JPanel bar = ButtonBarFactory.buildRightAlignedBar(
            WizardUIHelper.createPreviousButton(aPrev),
            WizardUIHelper.createFinishButton(aFinish),
            WizardUIHelper.createCancelButton(aCancel));

        bar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
        bar.setOpaque(false);

        return bar;
    }

    /**
     * Builds the panel.
     */
    public void build(JComponent buttonBar)
    {
        buttonBar.setBorder(Borders.createEmptyBorder("6dlu, 6dlu, 6dlu, 6dlu"));

        FormLayout layout = new FormLayout("15dlu, pref:grow, 15dlu",
            "pref, 17dlu, pref:grow, 8dlu, pref");

        PanelBuilder builder = new PanelBuilder(layout, this);
        CellConstraints cc = new CellConstraints();

        builder.add(buildHeader(), cc.xyw(1, 1, 3));
        builder.add(buildDataPage(), cc.xy(2, 3, "f, f"));
        builder.add(buttonBar, cc.xyw(1, 5, 3));
    }

    private Component buildDataPage()
    {
        BBFormBuilder builder = new BBFormBuilder("pref:grow");

        builder.appendRow("100px:grow");
        builder.append(picker, 1, CellConstraints.FILL, CellConstraints.FILL);

        return builder.getPanel();
    }

    /**
     * Creates the panel's header.
     */
    private JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("installer.startingpoints.title"),
            Strings.message("installer.startingpoints.header"),
            null);
    }

    /**
     * Returns selected points.
     *
     * @return list of selected points.
     */
    public CollectionItem[] getSelectedStartingPoints()
    {
        return picker.getSelectedCollectionItems();
    }
}
