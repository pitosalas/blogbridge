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
// $Id: ImportGuidesDialog.java,v 1.24 2006/05/30 14:51:22 spyromus Exp $
//

package com.salas.bb.dialogs.guide;

import com.jgoodies.uif.AbstractDialog;
import com.salas.bb.core.actions.guide.OPMLSelectionAction;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.HeaderPanelExt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.MalformedURLException;
import java.text.MessageFormat;

/**
 * Dialog for importing of Channel Guides.
 */
public class ImportGuidesDialog extends AbstractDialog
{
    private JTextField      tfUrl;
    private JButton         btnBrowse;

    private JRadioButton    rbFromURL;
    protected JRadioButton  rbFromBloglines;

    private JRadioButton    rbAsSingle;
    private JRadioButton    rbAsMultiple;

    private JLabel          lbEmail;
    private JTextField      tfBloglinesEmail;
    private JLabel          lbPass;
    private JPasswordField  tfBloglinesPassword;

    protected JCheckBox     chReplace;

    /**
     * Creates dialog box for entering properties of new guide.
     *
     * @param owner owner-frame.
     */
    public ImportGuidesDialog(final Frame owner)
    {
        super(owner, Strings.message("import.guides.dialog.title"));
    }

    /**
     * Build header panel.
     *
     * @return header panel.
     */
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("import.guides.dialog.title"),
            Strings.message("import.guides.dialog.header"));
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
        setupComponents();

        BBFormBuilder builder = new BBFormBuilder("7dlu, 7dlu, pref:grow, 2dlu, 150dlu, 1dlu, p");

        builder.append(rbFromURL, 7);
        builder.setLeadingColumnOffset(4);

        builder.append(tfUrl, btnBrowse);
        builder.nextLine();
        builder.setLeadingColumnOffset(0);

        builder.append(rbFromBloglines, 7);
        builder.setLeadingColumnOffset(2);
        builder.append(lbEmail, tfBloglinesEmail);
        builder.nextLine();
        builder.append(lbPass, tfBloglinesPassword);
        builder.appendUnrelatedComponentsGapRow(2);

        builder.setLeadingColumnOffset(0);
        builder.appendSeparator(Strings.message("import.guides.options"));

        builder.setLeadingColumnOffset(1);
        builder.append(rbAsSingle, 6);
        builder.append(rbAsMultiple, 6);
        builder.setLeadingColumnOffset(2);
        builder.append(chReplace, 5);

        return builder.getPanel();
    }

    private void setupComponents()
    {
        ButtonGroup bg;

        lbEmail = ComponentsFactory.createLabel(Strings.message("import.guides.email"));
        lbPass = ComponentsFactory.createLabel(Strings.message("import.guides.password"));

        tfUrl = new JTextField();
        btnBrowse = new JButton("\u2026");

        tfBloglinesEmail = new JTextField();
        tfBloglinesPassword = new JPasswordField();

        rbFromURL = ComponentsFactory.createRadioButton(Strings.message("import.guides.from.file.or.url"));
        rbFromURL.setToolTipText(Strings.message("import.guides.from.file.or.url.tooltip"));

        rbFromBloglines = ComponentsFactory.createRadioButton(Strings.message("import.guides.from.bloglines"));
        bg = new ButtonGroup();
        bg.add(rbFromURL);
        bg.add(rbFromBloglines);

        rbAsSingle = ComponentsFactory.createRadioButton(Strings.message("import.guides.single.guide"));
        rbAsMultiple = ComponentsFactory.createRadioButton(Strings.message("import.guides.multiple.guides"));
        bg = new ButtonGroup();
        bg.add(rbAsSingle);
        bg.add(rbAsMultiple);

        chReplace = ComponentsFactory.createCheckBox(Strings.message("import.guides.replace"));

        SingleMultiListener l = new SingleMultiListener();
        rbAsSingle.addActionListener(l);
        rbAsMultiple.addActionListener(l);
        rbFromURL.addActionListener(l);
        rbFromBloglines.addActionListener(l);

        rbFromURL.setSelected(true);
        rbAsSingle.setSelected(true);
        chReplace.setSelected(false);
        reviewOptionsState();

        btnBrowse.setAction(new OPMLSelectionAction(OPMLSelectionAction.MODE_OPEN, tfUrl));
        btnBrowse.setMargin(Constants.INSETS_NONE);
        btnBrowse.setPreferredSize(new Dimension(20, 20));
    }

    /**
     * Enables / disabled 'Append' and 'Replace' radio-buttons depending on
     * selection state of 'Multi'-mode.
     */
    private void reviewOptionsState()
    {
        chReplace.setEnabled(rbAsMultiple.isSelected());

        boolean fromBloglines = rbFromBloglines.isSelected();
        lbEmail.setEnabled(fromBloglines);
        tfBloglinesEmail.setEnabled(fromBloglines);
        lbPass.setEnabled(fromBloglines);
        tfBloglinesPassword.setEnabled(fromBloglines);
    }

    /**
     * Accepts or declines entry.
     */
    public void doAccept()
    {
        String error = validateEntry();
        if (error == null)
        {
            super.doAccept();
        } else
        {
            JOptionPane.showMessageDialog(this, MessageFormat.format(Strings.message("import.guides.error"),
                new Object[] { error }), this.getTitle(),
                JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Validates entry.
     *
     * @return entry.
     */
    private String validateEntry()
    {
        String error = null;

        if (rbFromBloglines.isSelected())
        {
            String email = tfBloglinesEmail.getText();
            if (email.trim().length() == 0)
            {
                error = Strings.message("import.guides.validation.unspecified.email");
            } else if (tfBloglinesPassword.getPassword().length == 0)
            {
                error = Strings.message("import.guides.unspecified.password");
            }
        } else
        {
            String urlString = tfUrl.getText().trim();
            if (urlString.length() == 0)
            {
                error = Strings.message("import.guides.unspecified.link.or.path");
            } else
            {
                try
                {
                    new URL(urlString);
                } catch (MalformedURLException e)
                {
                    error = Strings.message("import.guides.invalid.url");
                }
            }
        }

        return error;
    }

    /**
     * Returns <code>TRUE</code> if user selected to get OPML from URL.
     *
     * @return <code>TRUE</code> if user selected to get OPML from URL.
     */
    public boolean isFromURL()
    {
        return rbFromURL.isSelected();
    }

    /**
     * Returns Bloglines account email.
     *
     * @return Bloglines account email.
     */
    public String getBloglinesEmail()
    {
        return tfBloglinesEmail.getText();
    }

    /**
     * Returns Bloglines account password.
     *
     * @return Bloglines account password.
     */
    public String getBloglinesPassword()
    {
        return new String(tfBloglinesPassword.getPassword());
    }

    /**
     * Returns TRUE if user selected 'Single guide import' mode.
     *
     * @return TRUE if in single-guide mode.
     */
    public boolean isSingleMode()
    {
        return rbAsSingle.isSelected();
    }

    /**
     * Returns TRUE if user selected appending mode.
     *
     * @return TRUE - appending, FALSE - replacing.
     */
    public boolean isAppendingMode()
    {
        return !chReplace.isSelected();
    }

    /**
     * Returns entered string in OPML-URL field.
     *
     * @return entered URL.
     */
    public String getUrlString()
    {
        return tfUrl.getText();
    }

    /**
     * Class listens for radio-selection of single-multiple mode and enables/disabled multiple
     * mode options.
     */
    private class SingleMultiListener implements ActionListener
    {
        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e)
        {
            reviewOptionsState();
        }
    }
}
