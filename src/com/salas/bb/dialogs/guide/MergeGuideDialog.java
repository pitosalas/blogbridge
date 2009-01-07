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
// $Id: MergeGuideDialog.java,v 1.18 2006/05/30 14:51:22 spyromus Exp $
//

package com.salas.bb.dialogs.guide;

import com.jgoodies.uif.AbstractDialog;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.HeaderPanelExt;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.domain.StandardGuide;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for merging of Channel Guides.
 */
public class MergeGuideDialog extends AbstractDialog
{

    private JComboBox guideBox = new JComboBox();

    private StandardGuide[] mergeGuides = new StandardGuide[0];

    /**
     * Creates dialog box.
     *
     * @param owner owner-frame.
     */
    public MergeGuideDialog(final Frame owner)
    {
        super(owner, Strings.message("merge.guides.dialog.title"));

        setMergeGuides(new StandardGuide[0]);
    }

    /**
     * Build header panel.
     *
     * @return header panel.
     */
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(Strings.message("merge.guides.dialog.title"),
            Strings.message("merge.guides.dialog.header"));
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
        BBFormBuilder builder = new BBFormBuilder("pref:grow, 2dlu, 100dlu");

        builder.append(Strings.message("merge.guides.guide.to.merge.with"), guideBox);

        return builder.getPanel();
    }

    /**
     * Registers the list of possible guides for merging.
     *
     * @param guides list of possible guides for merging.
     */
    public void setMergeGuides(StandardGuide[] guides)
    {
        this.mergeGuides = guides;
        guideBox.removeAllItems();
        for (int i = 0; i < guides.length; i++)
        {
            final StandardGuide guide = guides[i];
            guideBox.addItem(guide.getTitle());
        }
    }

    /**
     * Returns selected guide for merging.
     *
     * @return selected guide.
     */
    public StandardGuide getSelectedMergeGuide()
    {
        final int selectedIndex = guideBox.getSelectedIndex();
        return selectedIndex == -1 ? null : mergeGuides[selectedIndex];
    }
}
