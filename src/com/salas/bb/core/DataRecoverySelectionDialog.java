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
// $Id $
//

package com.salas.bb.core;

import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.UifUtilities;
import com.salas.bb.utils.uif.HeaderPanelExt;
import com.salas.bb.utils.DateUtils;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.io.File;
import java.awt.*;
import java.util.Date;
import java.text.MessageFormat;

/**
 * Data recovery dialog.
 */
class DataRecoverySelectionDialog extends AbstractDialog
{
    private static final String LAYOUT_COLUMNS = "14dlu, p, 2dlu, max(50dlu;p), 0:grow";

    private final Date lastSuccessfulSync;
    private final File[] availableBackups;

    protected JRadioButton rbLoadFromBackup;
    protected JRadioButton rbLoadFromService;
    protected JComboBox cbBackups;

    /**
     * Creates data recovery dialog.
     *
     * @param frame                 parent frame.
     * @param lastSuccessfulSync    the date of last successful synchronization attempt.
     * @param availableBackups      the list of available backup files.
     */
    private DataRecoverySelectionDialog(Frame frame, Date lastSuccessfulSync,
                                        File[] availableBackups)
    {
        super(frame, Strings.message("data.recovery.title"));

        this.lastSuccessfulSync = lastSuccessfulSync;
        this.availableBackups = availableBackups;
    }

    /**
     * Builds header panel.
     *
     * @return header panel.
     */
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("data.recovery.title"),
            Strings.message("data.recovery.header"));
    }

    /**
     * Builds contents pane.
     *
     * @return pane.
     */
    protected JComponent buildContent()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildMainPanel(), BorderLayout.CENTER);
        panel.add(buildButtonsBar(), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Biilds main panel.
     *
     * @return panel.
     */
    private Component buildMainPanel()
    {
        JRadioButton rbLeaveClean = ComponentsFactory.createRadioButton(Strings.message("data.recovery.leave.clean"));

        // From Backups
        rbLoadFromBackup = ComponentsFactory.createRadioButton(Strings.message("data.recovery.load.from.backup"));
        rbLoadFromBackup.setEnabled(availableBackups.length > 0);

        JLabel lbFile = ComponentsFactory.createLabel(Strings.message("data.recovery.file"));
        lbFile.setLabelFor(cbBackups);
        lbFile.setEnabled(availableBackups.length > 0);

        cbBackups = new JComboBox();
        initBackupsBox();
        cbBackups.setEnabled(availableBackups.length > 0);

        // From Service

        rbLoadFromService = ComponentsFactory.createRadioButton(
            Strings.message("data.recovery.load.from.service.account"));
        rbLoadFromService.setEnabled(lastSuccessfulSync != null);

        JLabel lbDate = new JLabel(Strings.message("data.recovery.date"));
        JLabel lbDateValue = new JLabel(lastSuccessfulSync == null
            ? Strings.message("data.recovery.never.synchronized")
            : DateUtils.dateToString(lastSuccessfulSync));
        lbDate.setEnabled(lastSuccessfulSync != null);
        lbDateValue.setEnabled(lastSuccessfulSync != null);

        // Grouping

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbLeaveClean);
        bg.add(rbLoadFromBackup);
        bg.add(rbLoadFromService);

        // Select the best choice

        if (rbLoadFromBackup.isEnabled()) rbLoadFromBackup.setSelected(true);
        else if (rbLoadFromService.isEnabled()) rbLoadFromService.setSelected(true);
        else rbLeaveClean.setSelected(true);

        // ---

        BBFormBuilder builder = new BBFormBuilder(LAYOUT_COLUMNS);
        builder.setDefaultDialogBorder();

        builder.append(rbLoadFromBackup, 5);

        builder.setLeadingColumnOffset(1);
        builder.nextLine();
        builder.append(lbFile);
        builder.append(cbBackups, 2);

        builder.setLeadingColumnOffset(0);
        builder.nextLine();
        builder.append(rbLoadFromService, 5);

        builder.setLeadingColumnOffset(1);
        builder.nextLine();
        builder.append(lbDate);
        builder.append(lbDateValue);

        builder.setLeadingColumnOffset(0);
        builder.nextLine();
        builder.append(rbLeaveClean, 5);

        return builder.getPanel();
    }

    /** Loads backup files from the set to the drop-down box. */
    private void initBackupsBox()
    {
        if (availableBackups.length > 0)
        {
            for (int i = 0; i < availableBackups.length; i++)
            {
                File file = availableBackups[i];
                BackupFileItem item = new BackupFileItem(file);
                if (item.getSize() > 0) cbBackups.addItem(item);
            }
        } else
        {
            cbBackups.addItem(Strings.message("data.recovery.no.backups.available"));
        }
    }

    /**
     * Creates OK-bar.
     *
     * @return bar.
     */
    private Component buildButtonsBar()
    {
        return ButtonBarFactory.buildOKBar(createOKButton(true));
    }

    /**
     * Invokes dialog, waits for the choice and returns the results. The dialog is
     * opened in EDT.
     *
     * @param frame                 parent frame.
     * @param lastSuccessfulSync    the date of last successful synchronization attempt.
     * @param availableBackups      the list of available backup files.
     *
     * @return data recovery choice.
     */
    public static DataRecoveryChoice ask(final Frame frame, final Date lastSuccessfulSync,
                                         final File[] availableBackups)
    {
        final DataRecoveryChoice choice = new DataRecoveryChoice();

        UifUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                DataRecoverySelectionDialog dialog = new DataRecoverySelectionDialog(frame,
                    lastSuccessfulSync, availableBackups);
                dialog.open();

                if (dialog.hasBeenCanceled())
                {
                    choice.setMode(DataRecoveryChoice.MODE_LEAVE);
                } else
                {
                    // Set resulting mode
                    choice.setMode(dialog.rbLoadFromBackup.isSelected()
                        ? DataRecoveryChoice.MODE_FROM_BACKUP
                        : dialog.rbLoadFromService.isSelected()
                            ? DataRecoveryChoice.MODE_FROM_SERVICE
                            : DataRecoveryChoice.MODE_LEAVE);

                    // Set resulting item
                    Object selectedItem = dialog.cbBackups.getSelectedItem();
                    choice.setBackupFile(selectedItem instanceof BackupFileItem
                        ? ((BackupFileItem)selectedItem).getFile()
                        : null);
                }
            }
        });

        return choice;
    }

    /**
     * Item, representing backup file in the list.
     */
    private static class BackupFileItem
    {
        private final File file;
        private final String text;
        protected final long size;

        /**
         * Creates backup file item.
         *
         * @param file file.
         */
        public BackupFileItem(File file)
        {
            this.file = file;

            size = file != null && file.exists() ? file.length() : -1;
            long date = file.lastModified();

            text = MessageFormat.format(Strings.message("data.recovery.backup.item.name"),
                new Object[] { DateUtils.dateToString(new Date(date)), new Long(size / 1024) });
        }

        /**
         * Returns associated file.
         *
         * @return file.
         */
        public File getFile()
        {
            return file;
        }

        /**
         * Returns size of the file.
         *
         * @return size.
         */
        public long getSize()
        {
            return size;
        }

        /**
         * Returns a string representation of the object.
         *
         * @return a string representation of the object.
         */
        public String toString()
        {
            return text;
        }
    }

    /**
     * Data recovery choice holder.
     */
    static class DataRecoveryChoice
    {
        public static final int MODE_FROM_BACKUP    = 0;
        public static final int MODE_FROM_SERVICE   = 1;
        public static final int MODE_LEAVE          = 2;

        private int mode;
        private File backupFile;

        /**
         * Sets mode.
         *
         * @param mode mode.
         */
        public void setMode(int mode)
        {
            this.mode = mode;
        }

        /**
         * Sets backup file selected.
         *
         * @param backupFile file.
         */
        public void setBackupFile(File backupFile)
        {
            this.backupFile = backupFile;
        }

        /**
         * Returns mode.
         *
         * @return mode.
         */
        public int getMode()
        {
            return mode;
        }

        /**
         * Returns backup file selected.
         *
         * @return file.
         */
        public File getBackupFile()
        {
            return backupFile;
        }
    }
}
