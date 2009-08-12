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
// $Id: EditGuideDialog.java,v 1.23 2007/01/24 14:46:38 spyromus Exp $
//

package com.salas.bb.dialogs.guide;

import com.jgoodies.forms.layout.CellConstraints;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.GuidesSet;
import com.salas.bb.domain.IGuide;
import com.salas.bb.domain.ReadingList;
import com.salas.bb.domain.StandardGuide;
import com.salas.bb.utils.DateUtils;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.notification.NotificationArea;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.HeaderPanelExt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dialog for changing properties of Channel Guide.
 */
public class EditGuideDialog extends BasicGuideDialog
{
    private static final Logger LOG = Logger.getLogger(EditGuideDialog.class.getName());

    private JSpinner                    tfPosition = new JSpinner();
    private JCheckBox                   chAutoFeedsDiscovery;
    private JList                       iconsList = new JList();

    private static SpinnerNumberModel   positionModel = new SpinnerNumberModel(1, 1, 1, -1);

    private String                      keyToSelect;
    private IGuide guide;

    /**
     * Creates dialog box for entering properties of new guide.
     *
     * @param owner owner-frame.
     * @param aPublishingAvailable      <code>TRUE</code> if publishing is available.
     * @param aPublishingLimit          the number of guides the user can have published.
     * @param aPublishingLimitReached   <code>TRUE</code> if the limit is reached.
     */
    public EditGuideDialog(Frame owner, boolean aPublishingAvailable, int aPublishingLimit,
                          boolean aPublishingLimitReached)
    {
        super(owner, Strings.message("edit.guide.dialog.title"), aPublishingAvailable, aPublishingLimit,
            aPublishingLimitReached);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        chAutoFeedsDiscovery = ComponentsFactory.createCheckBox(
            Strings.message("edit.guide.scan.new.articles"));
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
        content.add(buildButtonBarWithOKCancel(), BorderLayout.SOUTH);

        return content;
    }

    /**
     * Create header for dialog.
     *
     * @return header component.
     */
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("edit.guide.dialog.title"),
            Strings.message("edit.guide.dialog.header"));
    }

    /**
     * Main dialog body.
     *
     * @return body.
     */
    private JComponent buildBody()
    {
        initComponents();

        JTabbedPane pane = new JTabbedPane();

        pane.addTab(Strings.message("edit.guide.general"), buildGeneralTab());
        pane.addTab(Strings.message("guide.dialog.readinglists"), buildReadingListsTab());
        pane.addTab(Strings.message("guide.dialog.publishing"), buildPublishingTab());
        if (NotificationArea.isSupported())
        {
            pane.addTab(Strings.message("guide.dialog.notifications"), buildNotificationsTab());
        }

        return pane;
    }

    /**
     * Builds general tab.
     *
     * @return tab.
     */
    private JComponent buildGeneralTab()
    {
        BBFormBuilder builder = new BBFormBuilder("p, 4dlu, 30dlu, 7dlu, p:grow");
        builder.setDefaultDialogBorder();

        builder.append(Strings.message("guide.dialog.title"), tfTitle, 3);
        builder.append(Strings.message("edit.guide.general.position"), tfPosition, chAutoFeedsDiscovery);
        builder.appendRelatedComponentsGapRow(2);
        builder.appendRow("70dlu:grow");
        builder.append(Strings.message("edit.guide.general.icon"), 1,
            CellConstraints.FILL, CellConstraints.TOP).setLabelFor(iconsList);
        builder.append(new JScrollPane(iconsList), 3, CellConstraints.FILL, CellConstraints.FILL);

        return builder.getPanel();
    }

    private void initComponents()
    {
        tfPosition.setModel(positionModel);

        iconsList.setModel(model);
        iconsList.setCellRenderer(renderer);
        iconsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        iconsList.setVisibleRowCount(0);
        iconsList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        if (keyToSelect == null)
        {
            iconsList.setSelectedIndex(0);
        } else
        {
            iconsList.setSelectedValue(keyToSelect, true);
            int selectedIndex = iconsList.getSelectedIndex();
            if (selectedIndex != -1) iconsList.ensureIndexIsVisible(selectedIndex);
        }
    }

    /**
     * Returns position selected by user.
     *
     * @return position.
     */
    public int getPosition()
    {
        return positionModel.getNumber().intValue() - 1;
    }

    /**
     * Returns TRUE if automatic feed discovery is selected.
     * 
     * @return TRUE if automatic feed discovery is selected.
     */
    public boolean isAutoFeedDiscovery()
    {
        return chAutoFeedsDiscovery.isSelected();
    }

    /**
     * Calls dialog for editing existing guide properties.
     *
     * @param set       guides set.
     * @param aGuide    guide.
     */
    public void open(GuidesSet set, IGuide aGuide)
    {
        guide = aGuide;

        String title = aGuide.getTitle();
        Set presTitles = set.getGuidesTitles();
        int position = set.indexOf(aGuide);
        String iconKey = aGuide.getIconKey();
        boolean autoFeedDiscovery = aGuide.isAutoFeedsDiscovery();
        ReadingList[] aReadingLists = aGuide instanceof StandardGuide
            ? ((StandardGuide)aGuide).getReadingLists() : null;

        originalTitle = title;
        setPresentTitles(presTitles);
        tfTitle.setText(title);
        positionModel.setMaximum(presTitles.size());
        positionModel.setValue(new Integer(position + 1));
        keyToSelect = (iconKey == null) ? "cg.default.icon" : iconKey;
        chAutoFeedsDiscovery.setSelected(autoFeedDiscovery);

        setReadingLists(aReadingLists);

        // Publishing
        chPublishingEnabled.setSelected(aGuide.isPublishingEnabled());
        tfPublishingTitle.setText(aGuide.getPublishingTitle());
        tfPublishingTags.setText(aGuide.getPublishingTags());
        chPublishingPublic.setSelected(aGuide.isPublishingPublic());
        setPublishingURL(aGuide.getPublishingURL());
        setLastPublishingDate(aGuide.getLastPublishingTime());
        vhPublishingRating.setValue(aGuide.getPublishingRating() + 1);
        chMobile.setSelected(aGuide.isMobile());

        boolean en = GlobalModel.SINGLETON.getUserPreferences().isNotificationsEnabled();
        chAllowNotifications.setSelected(guide.isNotificationsAllowed());
        chAllowNotifications.setEnabled(en);

        super.openDialog(set);
    }

    /**
     * The guide we are looking at.
     *
     * @return the guide.
     */
    protected IGuide getGuide()
    {
        return guide;
    }

    /**
     * Sets the last publication date.
     *
     * @param time timestamp.
     */
    private void setLastPublishingDate(long time)
    {
        if (time == -1)
        {
            tfLastPublishingDate.setText(Strings.message("guide.dialog.not.published.yet"));
        } else
        {
            tfLastPublishingDate.setText(DateUtils.dateToString(new Date(time)));
        }
    }

    /**
     * Sets publishing URL and enables / disabled copy-button.
     *
     * @param url URL to set.
     */
    private void setPublishingURL(String url)
    {
        if (StringUtils.isEmpty(url))
        {
            btnCopyToClipboard.setEnabled(false);
            lnkPublishingURL.setText(Strings.message("guide.dialog.not.published.yet"));
        } else
        {
            btnCopyToClipboard.setEnabled(true);
            lnkPublishingURL.setText(url);
            try
            {
                lnkPublishingURL.setLink(new URL(url));
            } catch (MalformedURLException e)
            {
                LOG.log(Level.SEVERE, MessageFormat.format(Strings.error("invalid.url.0"), url), e);
            }
        }
    }

    /**
     * Returns resource key of selected icon.
     *
     * @return resource key.
     */
    public String getIconKey()
    {
        return (String)iconsList.getSelectedValue();
    }
}
