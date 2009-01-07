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
// $Id: DeleteGuideDialog.java,v 1.18 2007/04/20 08:00:02 spyromus Exp $
//

package com.salas.bb.dialogs.guide;

import com.jgoodies.uif.AbstractDialog;
import com.salas.bb.domain.StandardGuide;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.HeaderPanelExt;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;

/**
 * Dialog for deletion of Channel Guide.
 */
public class DeleteGuideDialog extends AbstractDialog
{
    private JComboBox       guideBox;
    private JRadioButton    rbReassign;
    private JRadioButton    rbDeleteAll;

    private StandardGuide[] reassignGuides;

    /**
     * Creates dialog box.
     *
     * @param owner             owner-frame.
     * @param aMultipleGuides   <code>TRUE</code> when removing multipl guides.
     */
    public DeleteGuideDialog(final Frame owner, boolean aMultipleGuides)
    {
        super(owner, (aMultipleGuides
            ? Strings.message("delete.guide.dialog.title.multiple")
            : Strings.message("delete.guide.dialog.title.singular")));

        guideBox = new JComboBox();
        rbReassign = ComponentsFactory.createRadioButton(Strings.message("delete.guide.reassign.feeds"));
        rbDeleteAll = ComponentsFactory.createRadioButton(MessageFormat.format(
            Strings.message("delete.guide.delete.0.with.all.feeds"),
            aMultipleGuides
                    ? Strings.message("delete.guide.guides")
                : Strings.message("delete.guide.guide")));

        setReassignGuides(new StandardGuide[0]);
    }

    /**
     * Build header panel.
     *
     * @return header panel.
     */
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(getTitle(),
            Strings.message("delete.guide.dialog.header"));
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
     * Main dialog body.
     *
     * @return body.
     */
    private JComponent buildBody()
    {
        initComponents();

        BBFormBuilder builder = new BBFormBuilder("pref:grow, 2dlu, 50dlu");

        builder.append(rbDeleteAll, 3);
        builder.append(rbReassign, guideBox);

        return builder.getPanel();
    }

    private void initComponents()
    {
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbDeleteAll);
        bg.add(rbReassign);
    }

    /**
     * Opens dialog.
     */
    public void open()
    {
        rbDeleteAll.setSelected(true);
        super.open();
    }

    /**
     * Registers the list of possible guides for reassignment. If the list is empty then
     * reassignment feature gets disabled.
     *
     * @param guides list of possible guides for reassignment.
     */
    public void setReassignGuides(StandardGuide[] guides)
    {
        this.reassignGuides = guides;
        guideBox.removeAllItems();
        for (final StandardGuide guide : guides)
        {
            guideBox.addItem(guide.getTitle());
        }

        enableReassign(guides.length > 0);
    }

    private void enableReassign(boolean b)
    {
        rbReassign.setEnabled(b);
        if (!b) guideBox.addItem(Strings.message("delete.guide.no.available.guides"));
        guideBox.setEnabled(b);
    }

    /**
     * Returns TRUE if user decided to reassign channels to other guide.
     *
     * @return TRUE if so.
     */
    public boolean isReassigning()
    {
        return rbReassign.isSelected();
    }

    /**
     * Returns selected guides for reassignment. Note that if user selected 'Delete all' and
     * some guide was selected, this method will still return NULL.
     *
     * @return selected guide or NULL in case when 'Delete all' was selected.
     */
    public StandardGuide getSelectedReassignGuide()
    {
        return rbReassign.isSelected() ? reassignGuides[guideBox.getSelectedIndex()] : null;
    }
}
