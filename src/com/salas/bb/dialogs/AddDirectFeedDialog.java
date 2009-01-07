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
// $Id: AddDirectFeedDialog.java,v 1.7 2006/11/13 10:34:22 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.uif.AbstractDialog;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.HeaderPanelExt;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Dialog for addition of a direct feed.
 */
public class AddDirectFeedDialog extends AbstractDialog
{
    private JTextField      chanURL;
    private ValueHolder     urlValHold;
    private JLabel lbExamples;
    private JButton btnSuggest;

    /**
     * @param owner owning frame.
     * @param url   value holder for the xml URL.
     */
    public AddDirectFeedDialog(Frame owner, ValueHolder url)
    {
        super(owner, Strings.message("subscribe.to.feed.dialog.title"));
        urlValHold = url;
    }

    /**
     * Builds a pretty XP-style white header.
     *
     * @return header.
     */
    protected synchronized JComponent buildHeader()
    {
        return new HeaderPanelExt(Strings.message("subscribe.to.feed.dialog.title"),
            Strings.message("subscribe.to.feed.dialog.header"));
    }

    /**
     * The content is everything other than the header, including the body and the
     * buttons at the bottom.
     *
     * @return Content part of this dialog box.
     */
    protected JComponent buildContent()
    {
        JPanel content = new JPanel(new BorderLayout());
        content.add(buildBody(), BorderLayout.CENTER);
        content.add(buildButtonBarWithOKCancel(), BorderLayout.SOUTH);
        return content;
    }

    /**
     * Build the actual body of the modal dialog.
     *
     * @return body.
     */
    protected JComponent buildBody()
    {
        initComponents();

        BBFormBuilder builder = new BBFormBuilder("right:pref, 4dlu, 150dlu:grow, 2dlu, pref");

        builder.append(Strings.message("subscribe.to.feed.wording"), 5);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(Strings.message("subscribe.to.feed.address"), chanURL, btnSuggest);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.appendRow("top:pref");
        builder.append(Strings.message("subscribe.to.feed.examples"), 1);
        builder.append(lbExamples, 3);

        return builder.getPanel();
    }

    private void initComponents()
    {
        chanURL = new JTextField();
        Object value = urlValHold.getValue();
        if (value != null)
        {
            chanURL.setText(value.toString());
            chanURL.selectAll();
            chanURL.requestFocus();
        }

        btnSuggest = new JButton(new SuggestAction());

        lbExamples = new JLabel(Strings.message("subscribe.to.feed.examples.text"));

        lbExamples.setForeground(Color.BLUE);
        Font font = lbExamples.getFont();
        lbExamples.setFont(font.deriveFont(Font.PLAIN, font.getSize() - 1));
    }

    /**
     * Called when the OK button is pressed.
     */
    public void doApply()
    {
        super.doApply();
        urlValHold.setValue(chanURL.getText());
    }

    /**
     * Suggestion action.
     */
    private class SuggestAction extends AbstractAction
    {
        /**
         * Creates action.
         */
        public SuggestAction()
        {
            super(Strings.message("subscribe.to.feed.suggest"));
        }

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e)
        {
            CollectionItemsSelectionDialog dialog =
                new CollectionItemsSelectionDialog(AddDirectFeedDialog.this);

            if (!dialog.hasBeenCanceled())
            {
                chanURL.setText(dialog.open(chanURL.getText(),
                    GlobalModel.SINGLETON.getGuidesSet().getFeedsXmlURLs(), false));
            }
        }
    }
}
