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
// $Id: PlanWarningsDialog.java,v 1.2 2007/02/22 11:55:45 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.util.Resizer;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.utils.ResourceID;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.*;
import com.salas.bb.utils.uif.html.CustomHTMLEditorKit;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;

/**
 * The dialog for showing the plan features violation warnings.
 */
public class PlanWarningsDialog extends AbstractDialog
{
    private JEditorPane txMessages;
    private String plan;

    /**
     * Creates the dialog.
     *
     * @param parent parent frame.
     */
    public PlanWarningsDialog(Frame parent)
    {
        super(parent, Strings.message("spw.dialog.title"));

        txMessages = new JEditorPane();
        txMessages.setEditable(false);
        txMessages.setEditorKit(new CustomHTMLEditorKit());
    }

    @Override
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("spw.dialog.title"),
            Strings.message("spw.dialog.header"),
            IconSource.getIcon(ResourceID.ABOUT_ICON));
    }

    /**
     * Builds the content pane.
     *
     * @return pane.
     */
    protected JComponent buildContent()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildMainPanel(), BorderLayout.CENTER);
        panel.add(buildButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JComponent buildButtonPanel()
    {
        JComponent panel = buildButtonBarWithClose();
        panel.setBorder(Constants.DIALOG_BUTTON_BAR_BORDER);
        return panel;
    }

    /**
     * Builds the main pane.
     *
     * @return pane.
     */
    private Component buildMainPanel()
    {
        LinkLabel link = new LinkLabel(Strings.message("spw.learn.more"), ResourceUtils.getString("server.plans.url"));

        BBFormBuilder builder = new BBFormBuilder("0:grow");
        builder.setDefaultDialogBorder();

        builder.append(new JLabel(MessageFormat.format(Strings.message("spw.your.current.plan"), plan)));
        builder.appendUnrelatedComponentsGapRow(2);
        builder.appendRow("100dlu:grow");
        builder.append(new JScrollPane(txMessages), 1, CellConstraints.FILL, CellConstraints.FILL);
        builder.append(link);

        return builder.getPanel();
    }

    /**
     * Opens the dialog with warnings.
     *
     * @param plan      current plan name.
     * @param warnings  warnings.
     */
    public void open(String plan, java.util.List<String> warnings)
    {
        this.plan = plan;

        String txt = "<html>" + StringUtils.join(warnings.toArray(), "<br><br>");
        txMessages.setText(txt);
        txMessages.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        UifUtilities.setEditorFont(txMessages, new JLabel().getFont());

        open();
    }

    @Override
    protected void resizeHook(JComponent cont)
    {
        Resizer.FOUR2THREE.resizeDialogContent(cont);
    }
}
