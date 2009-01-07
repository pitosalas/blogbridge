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
// $Id: FeedAutoSavePanel.java,v 1.3 2007/05/03 11:02:02 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.salas.bb.core.autosave.NameFormat;
import com.salas.bb.domain.IFeed;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.StateUpdatingToggleListener;
import com.salas.bb.utils.uif.UifUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * The panel for feed auto-saving configuration.
 */
public class FeedAutoSavePanel extends JPanel
{
    private JCheckBox   chASA;
    private JTextField  tfASAFolder;
    private JComboBox   cbASANameFormat;
    private JCheckBox   chASE;
    private JTextField  tfASEFolder;
    private JComboBox   cbASENameFormat;

    /**
     * Creates the manipulation panel.
     *
     * @param feed  feed to manipulate.
     * @param avail <code>TRUE</code> to available.
     */
    public FeedAutoSavePanel(IFeed feed, boolean avail)
    {
        // Create components
        chASA = ComponentsFactory.createCheckBox("Automatically save articles");
        JLabel lbASAFolder = new JLabel("Save to:");
        tfASAFolder = new JTextField();
        JButton btnASAFolderPick = new JButton(new DirectoryPicker(this, tfASAFolder));
        JLabel lbASANameFormat = new JLabel("Filename format:");
        cbASANameFormat = new JComboBox();
        StateUpdatingToggleListener.install(chASA, lbASAFolder, tfASAFolder,
                btnASAFolderPick, lbASANameFormat, cbASANameFormat);

        chASE = ComponentsFactory.createCheckBox("Automatically save enclosures");
        JLabel lbASEFolder = new JLabel("Save to:");
        tfASEFolder = new JTextField();
        JButton btnASEFolderPick = new JButton(new DirectoryPicker(this, tfASEFolder));
        JLabel lbASENameFormat = new JLabel("Filename format:");
        cbASENameFormat = new JComboBox();
        StateUpdatingToggleListener.install(chASE, lbASEFolder, tfASEFolder,
                btnASEFolderPick, lbASENameFormat, cbASENameFormat);

        // Init components
        chASA.setSelected(feed != null && feed.isAutoSaveArticles());
        tfASAFolder.setText(feed == null ? "" : feed.getAutoSaveArticlesFolder());
        loadNameFormats(cbASANameFormat, feed == null ? null : feed.getAutoSaveArticlesNameFormat());
        chASE.setSelected(feed != null && feed.isAutoSaveEnclosures());
        tfASEFolder.setText(feed == null ? "" : feed.getAutoSaveEnclosuresFolder());
        loadNameFormats(cbASENameFormat, feed == null ? null : feed.getAutoSaveEnclosuresNameFormat());

        // Layout components
        BBFormBuilder builder = new BBFormBuilder("7dlu, p, 2dlu, min(p;100dlu), 2dlu, p", this);

        builder.append(makeIconPanel(chASA, avail), 6);
        builder.setLeadingColumnOffset(1);
        builder.append(lbASAFolder, tfASAFolder);
        builder.append(btnASAFolderPick);
        builder.append(lbASANameFormat, cbASANameFormat);

        builder.setLeadingColumnOffset(0);
        builder.nextLine();

        builder.append(makeIconPanel(chASE, avail), 6);
        builder.setLeadingColumnOffset(1);
        builder.append(lbASEFolder, tfASEFolder);
        builder.append(btnASEFolderPick);
        builder.append(lbASENameFormat, cbASENameFormat);
        builder.nextLine();

        // Enable / disable panels
        if (!avail)
        {
            chASA.setEnabled(false);
            lbASAFolder.setEnabled(false);
            tfASAFolder.setEnabled(false);
            btnASAFolderPick.setEnabled(false);
            lbASANameFormat.setEnabled(false);
            cbASANameFormat.setEnabled(false);
            chASE.setEnabled(false);
            lbASEFolder.setEnabled(false);
            tfASEFolder.setEnabled(false);
            btnASEFolderPick.setEnabled(false);
            lbASENameFormat.setEnabled(false);
            cbASENameFormat.setEnabled(false);
        }
    }

    private JPanel makeIconPanel(JCheckBox box, boolean available)
    {
        BBFormBuilder builder = new BBFormBuilder("p, 2dlu, p");

        builder.append(box);
        builder.append(UifUtilities.makeBasicPlanIcon(!available));

        return builder.getPanel();
    }

    /**
     * Loads name formats into the combo-box and selects the given.
     *
     * @param cb        box.
     * @param selected  the format to select.
     */
    private static void loadNameFormats(JComboBox cb, String selected)
    {
        NameFormat sel = null;

        NameFormat[] fmts = NameFormat.FORMATS;
        for (NameFormat fmt : fmts)
        {
            cb.addItem(fmt);
            if (sel == null && selected != null && selected.equals(fmt.getFormat())) sel = fmt;
        }

        if (sel != null) cb.setSelectedItem(sel);
    }

    /**
     * Returns the selected format.
     *
     * @param cb combobox.
     *
     * @return format.
     */
    private static String getSelectedFormat(JComboBox cb)
    {
        NameFormat fmt = (NameFormat)cb.getSelectedItem();
        return fmt.getFormat();
    }

    /**
     * Validates the entry and returns the message.
     *
     * @return the message or <code>NULL</code> if fine.
     */
    public String validateData()
    {
        String msg = null;

        boolean asa = chASA.isSelected();
        boolean ase = chASE.isSelected();

        // TODO: Localize!!!
        if (asa)
        {
            String asaf = tfASAFolder.getText();
            if (StringUtils.isEmpty(asaf))
            {
                msg = "Please select a folder for the articles saving.";
            } else if (!new File(asaf).exists())
            {
                msg = "Please create a folder for the articles saving.";
            }
        }

        if (ase && msg == null)
        {
            String asef = tfASEFolder.getText();
            if (StringUtils.isEmpty(asef))
            {
                msg = "Please select a folder for the enclosure saving.";
            } else if (!new File(asef).exists())
            {
                msg = "Please create a folder for the enclosure saving.";
            }
        }

        return msg;
    }

    /**
     * Commits changes to the feed.
     *
     * @param feed feed to apply changes to.
     */
    public void commitChanges(IFeed feed)
    {
        feed.setAutoSaveArticles(chASA.isSelected());
        feed.setAutoSaveArticlesFolder(tfASAFolder.getText());
        feed.setAutoSaveArticlesNameFormat(getSelectedFormat(cbASANameFormat));
        feed.setAutoSaveEnclosures(chASE.isSelected());
        feed.setAutoSaveEnclosuresFolder(tfASEFolder.getText());
        feed.setAutoSaveEnclosuresNameFormat(getSelectedFormat(cbASENameFormat));
    }

    /**
     * The Picker for the directory.
     */
    private static class DirectoryPicker extends AbstractAction
    {
        private final Component parent;
        private final JTextField field;

        /**
         * Creates an action to pick directories for the given field.
         *
         * @param parent parent component.
         * @param field field.
         */
        public DirectoryPicker(Component parent, JTextField field)
        {
            super("\u2026");
            this.parent = parent;
            this.field = field;
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e event.
         */
        public void actionPerformed(ActionEvent e)
        {
            JFileChooser ch = new JFileChooser(field.getText());
            ch.setDialogType(JFileChooser.OPEN_DIALOG);
            ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            ch.setApproveButtonText("Choose");

            int res = ch.showOpenDialog(parent);
            if (res == JFileChooser.APPROVE_OPTION)
            {
                File file = ch.getSelectedFile();
                String path = file == null ? "" : file.getAbsolutePath();

                field.setText(path);
                if (path.length() > 0) field.setCaretPosition(0);
            }
        }
    }
}