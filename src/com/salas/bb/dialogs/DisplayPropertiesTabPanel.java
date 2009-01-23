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
// $Id: DisplayPropertiesTabPanel.java,v 1.5 2007/04/30 11:12:56 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.FeedType;
import com.salas.bb.domain.IFeed;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.StateUpdatingToggleListener;
import com.salas.bb.views.settings.FeedRenderingSettings;

import javax.swing.*;

/**
 * Reusable display tab panel with display properties of the feed.
 */
public class DisplayPropertiesTabPanel extends JPanel
{
    private final IFeed feed;
    private final FeedType initialFeedType;

    private final JComboBox cbFeedType;
    private final JCheckBox chCustomViewModeEnabled;
    private final JComboBox cbViewMode;
    private final JComboBox cbAscendingSorting;

    /**
     * Creates the tab panel for a feed.
     *
     * @param feed feed.
     */
    public DisplayPropertiesTabPanel(IFeed feed)
    {
        this.feed = feed;
        initialFeedType = feed == null ? FeedType.TEXT : feed.getType();

        cbFeedType = new JComboBox(FeedType.getAllTypes());
        cbFeedType.setSelectedItem(initialFeedType);

        // Custom view mode
        chCustomViewModeEnabled = ComponentsFactory.createCheckBox(
            Strings.message("show.feed.properties.tab.display.custom.view.mode"));
        chCustomViewModeEnabled.setSelected(feed != null && feed.isCustomViewModeEnabled());

        int selectedViewMode = feed == null ? -1 : feed.getCustomViewMode();
        if (selectedViewMode == -1)
        {
            FeedRenderingSettings frs = GlobalModel.SINGLETON.getGlobalRenderingSettings();
            selectedViewMode = frs.getArticleViewMode();
        }

        cbViewMode = new JComboBox(new String[] {
            Strings.message("viewmode.minimal"),
            Strings.message("viewmode.brief"),
            Strings.message("viewmode.full") });
        cbViewMode.setSelectedIndex(selectedViewMode);

        int ascendingSortingInd = sort2index(feed == null ? null : feed.getAscendingSorting());
        cbAscendingSorting = new JComboBox(new String[] {
            "Global", "Ascending", "Descending"});
        cbAscendingSorting.setSelectedIndex(ascendingSortingInd);

        BBFormBuilder builder = new BBFormBuilder("7dlu, p, 4dlu, p, 0:grow", this);
        builder.setDefaultDialogBorder();

        builder.append(Strings.message("show.feed.properties.tab.display.type"), 2, cbFeedType, 1);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(chCustomViewModeEnabled, 5);
        builder.setLeadingColumnOffset(1);
        JLabel lbViewMode = builder.append(Strings.message("show.feed.properties.tab.display.mode"), 1);
        builder.append(cbViewMode);
        lbViewMode.setLabelFor(cbViewMode);

        builder.setLeadingColumnOffset(0);
        builder.nextLine();
        builder.append("Sorting order: ", 2, cbAscendingSorting, 1);

        StateUpdatingToggleListener.install(chCustomViewModeEnabled, lbViewMode, cbViewMode);
    }

    /**
     * Returns initial feeds type we saw during initialization.
     *
     * @return feed type.
     */
    public FeedType getInitialFeedType()
    {
        return initialFeedType;
    }

    /**
     * Returns selected feed type.
     *
     * @return feed type.
     */
    public FeedType getFeedType()
    {
        return (FeedType)cbFeedType.getSelectedItem();
    }

    /**
     * Selects the feed type.
     *
     * @param type new type.
     */
    public void setFeedType(FeedType type)
    {
        cbFeedType.setSelectedItem(type);
    }

    /**
     * Returns selected view mode.
     *
     * @return view mode.
     */
    public int getViewMode()
    {
        return cbViewMode.getSelectedIndex();
    }

    /**
     * Sets the selected view mode.
     *
     * @param mode mode.
     */
    public void setViewMode(int mode)
    {
        cbViewMode.setSelectedIndex(mode);
    }

    /**
     * Returns <code>TRUE</code> if custom view mode is selected.
     *
     * @return <code>TRUE</code> if custom view mode is selected. 
     */
    public boolean isCustomViewModeEnabled()
    {
        return chCustomViewModeEnabled.isSelected();
    }

    /**
     * Commits all the changes to feed.
     */
    public void commitChanges()
    {
        if (feed == null) return;

        FeedType feedType = getFeedType();
        if (getInitialFeedType() != feedType) feed.setType(feedType);

        // Custom view mode
        int mode = getViewMode();
        feed.setCustomViewModeEnabled(isCustomViewModeEnabled());
        if (isCustomViewModeEnabled())
        {
            GlobalModel.SINGLETON.getViewModeValueModel().setValue(mode);
        } else feed.setCustomViewMode(mode);

        // Sorting override
        feed.setAscendingSorting(index2sort(cbAscendingSorting.getSelectedIndex()));
    }

    /**
     * Converts ascending sorting override property value into the sorting box index.
     *
     * @param asc   property value.
     *
     * @return index.
     */
    private static int sort2index(Boolean asc)
    {
        return asc == null ? 0 : asc ? 1 : 2;
    }

    /**
     * Converts index of the selected item in the sorting override box to the property value.
     *
     * @param ind   index.
     *
     * @return ascending sorting override value.
     */
    private static Boolean index2sort(int ind)
    {
        return ind == 0 ? null : ind == 1;
    }
}
