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
// $Id: NewPublicationDialog.java,v 1.2 2006/05/30 14:51:22 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.*;

/**
 * Simple Sync-No Sync dialog.
 */
public class NewPublicationDialog extends AbstractDialog
{
    private final JCheckBox chDoNotShowAgain;

    /**
     * Creates dialog.
     *
     * @param frame parent frame.
     */
    public NewPublicationDialog(Frame frame)
    {
        super(frame, Strings.message("new.publication.dialog.title"));
        chDoNotShowAgain = ComponentsFactory.createCheckBox(Strings.message("do.not.show.this.dialog.again"));
    }

    /**
     * Creates content panel.
     *
     * @return panel.
     */
    protected JComponent buildContent()
    {
        JPanel content = new JPanel(new BorderLayout());

        content.add(buildBody(), BorderLayout.CENTER);
        content.add(buildButtonsBar(), BorderLayout.SOUTH);

        return content;
    }

    /**
     * Creates body.
     *
     * @return body.
     */
    private Component buildBody()
    {
        BBFormBuilder builder = new BBFormBuilder("pref");
        builder.setDefaultDialogBorder();

        builder.append(new JLabel(Strings.message("new.publication.wording")));
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(new JLabel(Strings.message("new.publication.query")));
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(chDoNotShowAgain);

        return builder.getPanel();
    }

    /**
     * Creates buttons bar.
     *
     * @return bar.
     */
    private Component buildButtonsBar()
    {
        JButton ok = createAcceptButton(Strings.message("new.publication.synchronize.now"), true);

        return ButtonBarFactory.buildRightAlignedBar(ok, createCancelButton());
    }

    /**
     * Returns <code>TRUE</code> if the user has chosen not to see this dialog again.
     *
     * @return <code>TRUE</code> if the user has chosen not to see this dialog again.
     */
    public boolean isDoNotShowAgain()
    {
        return chDoNotShowAgain.isSelected();
    }
}
