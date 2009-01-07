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
// $Id: ReadingListUpdateConfirmationDialog.java,v 1.6 2006/07/06 15:34:34 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.AbstractDialog;
import com.salas.bb.domain.ReadingList;
import com.salas.bb.domain.StandardGuide;
import com.salas.bb.domain.utils.FeedCheckBox;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.CheckBoxList;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * Reading list updates confirmation dialog.
 */
public class ReadingListUpdateConfirmationDialog extends AbstractDialog
{
    private final ReadingList list;
    private final java.util.List addFeeds;
    private final java.util.List removeFeeds;

    private final CheckBoxList lstAddFeeds;
    private final CheckBoxList lstRemoveFeeds;

    /**
     * Creates reading list update confirmation dialog box.
     *
     * @param frame         parent frame.
     * @param aList         reading list going to be updated.
     * @param aAddFeeds     list of feeds to be added.
     * @param aRemoveFeeds  list of feeds to be removed.
     */
    public ReadingListUpdateConfirmationDialog(Frame frame, ReadingList aList,
        java.util.List aAddFeeds, java.util.List aRemoveFeeds)
    {
        super(frame);

        list = aList;
        addFeeds = aAddFeeds;
        removeFeeds = aRemoveFeeds;

        lstAddFeeds = new CheckBoxList();
        lstAddFeeds.setListData(FeedCheckBox.wrap(addFeeds));

        lstRemoveFeeds = new CheckBoxList();
        lstRemoveFeeds.setListData(FeedCheckBox.wrap(removeFeeds));

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
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
     * Creates body part.
     *
     * @return body part.
     */
    private Component buildBody()
    {
        BBFormBuilder builder = new BBFormBuilder("p, 4dlu, 100dlu, 0, p");

        JComponent wording = ComponentsFactory.createWrappedMultilineLabel(
            Strings.message("readinglist.updates.wording"));

        builder.append(wording, 5);
        builder.appendUnrelatedComponentsGapRow(2);

        StandardGuide guide = list.getParentGuide();
        if (guide != null)
        {
            builder.append(Strings.message("readinglist.updates.guide"), new JLabel(guide.getTitle()), 3);
        }
        builder.append(Strings.message("readinglist.updates.readinglist"), new JLabel(list.getTitle()), 3);
        builder.appendUnrelatedComponentsGapRow(2);

        if (addFeeds.size() > 0)
        {
            builder.append(Strings.message("readinglist.updates.feeds.should.be.added"), 3,
                    CheckBoxList.createAllNonePanel(lstAddFeeds), 1);
            builder.appendRow("50dlu:grow");
            builder.append(new JScrollPane(lstAddFeeds), 5,
                CellConstraints.FILL, CellConstraints.FILL);

            if (removeFeeds.size() > 0) builder.appendUnrelatedComponentsGapRow(2);
        }

        if (removeFeeds.size() > 0)
        {
            builder.append(Strings.message("readinglist.updates.feeds.should.be.removed"), 3,
                    CheckBoxList.createAllNonePanel(lstRemoveFeeds), 1);
            builder.appendRow("50dlu:grow");
            builder.append(new JScrollPane(lstRemoveFeeds), 5,
                CellConstraints.FILL, CellConstraints.FILL);
        }

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

    /**
     * Returns the list of feeds to add.
     *
     * @return feeds to add.
     */
    public List getAddFeeds()
    {
        return FeedCheckBox.getSelected(lstAddFeeds.getModel());
    }

    /**
     * Removes the list of feed to remove.
     *
     * @return feeds to remove.
     */
    public List getRemoveFeeds()
    {
        return FeedCheckBox.getSelected(lstRemoveFeeds.getModel());
    }
}
