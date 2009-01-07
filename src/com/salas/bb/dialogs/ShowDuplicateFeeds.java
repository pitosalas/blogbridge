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
// $Id: ShowDuplicateFeeds.java,v 1.5 2006/05/30 14:51:22 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.AbstractDialog;
import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.domain.utils.GuideCheckBox;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.CheckBoxList;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.HeaderPanelExt;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;

/**
 * The dialog allowing a user to select which of feeds to leave and which of
 * them to remove.
 */
public class ShowDuplicateFeeds extends AbstractDialog
{
    private final String feedTitle;
    private final CheckBoxList lstGuides;

    /**
     * Creates dialog.
     *
     * @param frame parent frame.
     * @param feed feed with duplicates.
     */
    public ShowDuplicateFeeds(Frame frame, DirectFeed feed)
    {
        super(frame);
        feedTitle = feed.getTitle();
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        lstGuides = new CheckBoxList();
        lstGuides.setListData(GuideCheckBox.wrap(feed.getParentGuides()));
    }

    /**
     * Returns feed unchecked for removal.
     *
     * @return removals.
     */
    public IGuide[] getRemovals()
    {
        IGuide[] removals = null;
        if (!hasBeenCanceled())
        {
            java.util.List deselected = GuideCheckBox.getDeselected(lstGuides.getModel());
            removals = (IGuide[])deselected.toArray(new IGuide[deselected.size()]);
        }

        return removals;
    }

    /**
     * Builds dialog content.
     *
     * @return content.
     */
    protected JComponent buildContent()
    {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(buildBody(), BorderLayout.CENTER);
        panel.add(buildButtonBarWithOKCancel(), BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates a header.
     *
     * @return header.
     */
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("duplicate.feeds.dialog.title"),
            MessageFormat.format(Strings.message("duplicate.feeds.dialog.header"),
                new Object[] { feedTitle }));
    }

    /**
     * Creates body part.
     *
     * @return body part.
     */
    private Component buildBody()
    {
        BBFormBuilder builder = new BBFormBuilder("p, 4dlu, max(p;150dlu):grow");

        JComponent wording = ComponentsFactory.createWrappedMultilineLabel(
            MessageFormat.format(Strings.message("duplicate.feeds.wording"), new Object[] { feedTitle }));

        builder.append(Strings.message("duplicate.feeds.and.guides"), 3);
        builder.appendRow("50dlu:grow");
        builder.append(new JScrollPane(lstGuides), 3,
            CellConstraints.FILL, CellConstraints.FILL);
        builder.appendUnrelatedComponentsGapRow(2);

        builder.append(wording, 3);

        return builder.getPanel();
    }

    /**
     * Handles window events depending on the state of the <code>defaultCloseOperation</code>
     * property.
     *
     * @see #setDefaultCloseOperation
     */
    protected void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);

        if (e.getID() == WindowEvent.WINDOW_OPENED) pack();
    }
}
