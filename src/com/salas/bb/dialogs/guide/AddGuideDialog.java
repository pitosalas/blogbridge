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
// $Id: AddGuideDialog.java,v 1.37 2007/01/24 15:47:13 spyromus Exp $
//

package com.salas.bb.dialogs.guide;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.component.UIFButton;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.IGuide;
import com.salas.bb.domain.ReadingList;
import com.salas.bb.domain.utils.GuideIcons;
import com.salas.bb.service.ServerService;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.feedscollections.CollectionItem;
import com.salas.bb.utils.feedscollections.Picker;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.notification.NotificationArea;
import com.salas.bb.utils.uif.ActionLabel;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.HeaderPanelExt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Set;

/**
 * Dialog for addition of Channel Guide.
 */
public class AddGuideDialog extends BasicGuideDialog
{
    private JComboBox   iconsList = new JComboBox();

    private Picker      picker;

    private JPanel      pane;
    private JPanel      reloadPanel;

    /**
     * Creates dialog box for entering properties of new guide.
     *
     * @param owner                     owner frame.
     * @param aPublishingAvailable      <code>TRUE</code> if publishing is available.
     * @param aPublishingLimit          the number of guides the user can have published.
     * @param aPublishingLimitReached   <code>TRUE</code> if the limit is reached.
     */
    public AddGuideDialog(Frame owner, boolean aPublishingAvailable, int aPublishingLimit,
                          boolean aPublishingLimitReached)
    {
        super(owner, Strings.message("add.guide.dialog.title"), aPublishingAvailable,
            aPublishingLimit, aPublishingLimitReached);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    }

    /**
     * The guide we are looking at.
     *
     * @return the guide.
     */
    protected IGuide getGuide()
    {
        return null;
    }

    /**
     * Handles window events depending on the state of the <code>defaultCloseOperation</code>
     * property.
     *
     * @see #setDefaultCloseOperation
     */
    protected void processWindowEvent(WindowEvent e)
    {
        if (e.getID() == WindowEvent.WINDOW_OPENED)
        {
            tfTitle.requestFocusInWindow();
        }
    }

    /**
     * Returns content of the dialog box.
     *
     * @return content component.
     */
    protected JComponent buildContent()
    {
        JPanel content = new JPanel(new BorderLayout());

        content.add(buildBody(), BorderLayout.CENTER);
        content.add(buildButtons(), BorderLayout.SOUTH);

        return content;
    }

    private JComponent buildButtons()
    {
        UIFButton btnSelect = createAcceptButton(Strings.message("add.guide.add"), true);
        UIFButton btnCancel = createCancelButton();

        JPanel panel = ButtonBarFactory.buildOKCancelBar(btnSelect, btnCancel);
        panel.setBorder(Borders.BUTTON_BAR_GAP_BORDER);

        return panel;
    }

    /**
     * Create header for dialog.
     *
     * @return header component.
     */
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("add.guide.dialog.title"),
            Strings.message("add.guide.dialog.header"));
    }

    /**
     * Main dialog body.
     *
     * @return body.
     */
    private JComponent buildBody()
    {
        JTabbedPane pane = new JTabbedPane();

        pane.addTab(Strings.message("add.guide.feeds"), buildFeedsTab());
        pane.addTab(Strings.message("guide.dialog.readinglists"), buildReadingListsTab());
        pane.addTab(Strings.message("guide.dialog.publishing"), buildPublishingTab());
        if (NotificationArea.isSupported())
        {
            pane.addTab(Strings.message("guide.dialog.notifications"), buildNotificationsTab());
        }

        BBFormBuilder builder = new BBFormBuilder("pref, 4dlu, pref:grow, 7dlu, pref");

        builder.append(Strings.message("guide.dialog.title"), tfTitle, iconsList);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.appendRow("min:grow");
        builder.append(pane, 5, CellConstraints.FILL, CellConstraints.FILL);

        return builder.getPanel();
    }

    /**
     * Creates feeds tab.
     *
     * @return component.
     */
    private JComponent buildFeedsTab()
    {
        BBFormBuilder builder = new BBFormBuilder("pref:grow");
        builder.setDefaultDialogBorder();

        builder.append(Strings.message("add.guide.feeds.wording"), 1);
        builder.appendRelatedComponentsGapRow(2);
        builder.appendRow("min:grow");
        builder.append(pane, 1, CellConstraints.FILL, CellConstraints.FILL);

        return builder.getPanel();
    }

    /**
     * Returns title entered by user.
     *
     * @return title.
     */
    public String getGuideTitle()
    {
        return tfTitle.getText();
    }

    /**
     * Returns resource key of selected icon.
     *
     * @return resource key.
     */
    public String getIconKey()
    {
        return (String)iconsList.getSelectedItem();
    }

    /**
     * Returns TRUE if automatic feed discovery is selected.
     *
     * @return TRUE if automatic feed discovery is selected.
     */
    public boolean isAutoFeedDiscovery()
    {
        return false;
    }

    /**
     * Calls dialog for addition of a new guide.
     *
     * @param set global guides set.
     *
     * @return list of selected feeds URL's.
     */
    public String open(GuidesSet set)
    {
        if (pane == null)
        {
            pane = new JPanel(new BorderLayout());

            iconsList.setModel(model);
            iconsList.setRenderer(renderer);
        }

        // Find and select first unused icon
        Set usedIconKeys = set.getGuidesIconKeys();
        int index = GuideIcons.findUnusedIconName(usedIconKeys);
        if (index < 0) index = 0;
        iconsList.setSelectedIndex(index);

        // Register present titles
        setPresentTitles(set.getGuidesTitles());

        setVisibleView();
        setReadingLists(new ReadingList[0]);

        boolean en = GlobalModel.SINGLETON.getUserPreferences().isNotificationsEnabled();
        chAllowNotifications.setSelected(true);
        chAllowNotifications.setEnabled(en);
        
        super.openDialog(set);

        String selectedUrls = "";
        if (picker != null)
        {
            CollectionItem[] selected = picker.getSelectedCollectionItems();
            String[] urls = new String[selected.length];
            for (int i = 0; i < selected.length; i++)
            {
                CollectionItem item = selected[i];
                urls[i] = item.getXmlURL();
            }

            selectedUrls = StringUtils.join(urls, Constants.URL_SEPARATOR);
        }

        return selectedUrls;
    }

    private void setVisibleView()
    {
        boolean isPickerVisible = picker != null;

        pane.removeAll();
        pane.add(isPickerVisible ? picker : getReloadPanel(), BorderLayout.CENTER);
        validate();
        repaint();
    }

    private synchronized Component getReloadPanel()
    {
        if (reloadPanel == null)
        {
            String text = Strings.message("click.here.to.load.our.collection.of.interesting.feeds");
            String overText = Strings.message("load.our.collection.of.interesting.feeds");
            ActionLabel label = new ActionLabel(text, new ReloadAction(), overText);

            label.setForeground(Color.BLUE);
            reloadPanel = new JPanel(new FormLayout("pref:grow", "pref:grow"));
            reloadPanel.add(label, new CellConstraints().xy(1, 1, "c, c"));
        }

        return reloadPanel;
    }

    /**
     * Checks if title is valid.
     *
     * @return error message or NULL.
     */
    protected String validateTitle()
    {
        String message = null;

        final String title = tfTitle.getText();
        if (title == null || title.trim().length() == 0)
        {
            message = Strings.message("guide.dialog.validation.empty.title");
        } else if (presentTitles.contains(title))
        {
            message = Strings.message("guide.dialog.validation.already.present");
        }
        return message;
    }

    /**
     * Simple action for reloading of feeds collections.
     */
    private class ReloadAction extends AbstractAction
    {
        public ReloadAction()
        {
            super(Strings.message("add.guide.reload.feeds"));
        }

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e)
        {
            if (picker == null)
            {
                picker = new Picker();
                picker.addCollection(ServerService.getStartingPointsURL(),
                    Strings.message("collection.collections"), true, Picker.ITEM_TYPE_FEED, false);
                picker.addCollection(ServerService.getExpertsURL(),
                    Strings.message("collection.experts"), true, Picker.ITEM_TYPE_FEED, true);

                setVisibleView();
            }
        }
    }
}
