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
// $Id: InsertUserLinkAction.java,v 1.3 2008/04/07 16:35:23 spyromus Exp $
//

package com.salas.bb.remixfeeds.editor;

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.uif.AbstractDialog;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Asks a user for a link and a title and inserts.
 */
class InsertUserLinkAction extends AbstractInsertLinkAction
{
    private final JDialog parent;

    /**
     * Creates the action.
     *
     * @param parent    parent dialog.
     * @param editor    text editor.
     * @param itemTitle item title.
     */
    public InsertUserLinkAction(JDialog parent, JEditorPane editor, String itemTitle)
    {
        super(editor, itemTitle);
        this.parent = parent;
    }

    /**
     * Invoked when a user calls the command.
     *
     * @param e action.
     */
    public void actionPerformed(ActionEvent e)
    {
        LinkDialog dialog = new LinkDialog(parent);
        dialog.open(getSelectedText());
        if (!dialog.hasBeenCanceled())
        {
            insertLink(dialog.getLink(), dialog.getText());
        }
    }

    /**
     * The dialog for asking a link and a piece of text to surround with it.
     */
    private static class LinkDialog extends AbstractDialog
    {
        private final JTextField tfLink;
        private final JTextField tfText;
        private JButton btnOK;

        /**
         * Creates the dialog.
         *
         * @param parent dialog.
         */
        public LinkDialog(JDialog parent)
        {
            super(parent, Strings.message("ptb.editor.insert.link"));

            tfLink = new JTextField();
            tfText = new JTextField();

            LinkDialog.TextFieldListener listener = new TextFieldListener();
            tfText.addKeyListener(listener);
            tfLink.addKeyListener(listener);
        }

        /**
         * Builds the content area.
         *
         * @return content.
         */
        protected JComponent buildContent()
        {
            JPanel content = new JPanel(new BorderLayout());
            content.add(buildMainPanel(), BorderLayout.CENTER);
            content.add(buildButtonBar(), BorderLayout.SOUTH);
            return content;
        }

        /**
         * Creates button bar.
         *
         * @return bar.
         */
        private JComponent buildButtonBar()
        {
            btnOK = createOKButton(true);
            updateOKButtonState();
            return ButtonBarFactory.buildOKCancelBar(btnOK, createCancelButton());
        }

        private void updateOKButtonState()
        {
            String text = tfText.getText();
            String link = tfLink.getText();
            btnOK.setEnabled(StringUtils.isNotEmpty(text) && StringUtils.isNotEmpty(link));
        }

        /**
         * Builds the main panel.
         *
         * @return main panel.
         */
        private Component buildMainPanel()
        {
            BBFormBuilder builder = new BBFormBuilder("p, 2dlu, 150dlu");
            builder.setDefaultDialogBorder();

            builder.append("&Link:", tfLink);
            builder.append("&Text:", tfText);

            return builder.getPanel();
        }

        /**
         * Opens the dialog and initialize the text field.
         *
         * @param text text to put.
         */
        public void open(String text)
        {
            tfText.setText(text);
            super.open();
        }

        /**
         * Returns the link.
         *
         * @return link.
         */
        public String getLink()
        {
            return tfLink.getText();
        }

        /**
         * Returns the text.
         *
         * @return text.
         */
        public String getText()
        {
            return tfText.getText();
        }

        /**
         * Listens to the changes in the text field and updates the OK button state.
         */
        private class TextFieldListener extends KeyAdapter
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                if (!e.isActionKey()) updateOKButtonState();
            }
        }
    }
}
