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
// $Id: DatabaseBackupAction.java,v 1.1 2007/03/30 11:41:58 spyromus Exp $
//

package com.salas.bb.core.actions;

import com.salas.bb.core.GlobalController;
import com.salas.bb.persistence.PersistenceException;
import com.salas.bb.persistence.PersistenceManagerConfig;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Compacts the database.
 */
public class DatabaseBackupAction extends AbstractAction
{
    private static final DatabaseBackupAction INSTANCE = new DatabaseBackupAction();
    private static final Logger LOG = Logger.getLogger(DatabaseBackupAction.class.getName());

    /**
     * Creates action.
     */
    private DatabaseBackupAction()
    {
        setEnabled(false);
    }

    /**
     * Returns instance.
     *
     * @return instance.
     */
    public static DatabaseBackupAction getInstance()
    {
        return INSTANCE;
    }

    /** Invoked when an action occurs. */
    public void actionPerformed(ActionEvent e)
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);

        int res = chooser.showDialog(GlobalController.SINGLETON.getMainFrame(), "Choose");
        if (res != JFileChooser.CANCEL_OPTION)
        {
            final File directory = chooser.getSelectedFile();

            new Thread("Database Backup")
            {
                @Override
                public void run()
                {
                    doBackup(directory);
                }
            }.start();
        }
    }

    private void doBackup(File directory)
    {
        String message;
        int type;
        try
            {
                PersistenceManagerConfig.getManager().backup(directory);
                message = "Backup is complete";
            type = JOptionPane.INFORMATION_MESSAGE;
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, "Backup failed.", e);

            message = "Backup failed.\n\n" +
                "It's recommended to export your\n" +
                "subscriptions and restart.";
            type = JOptionPane.WARNING_MESSAGE;
        }

        // Completion note
        final String fMessage = message;
        final int fType = type;
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                JOptionPane.showMessageDialog(GlobalController.SINGLETON.getMainFrame(),
                    fMessage,
                    "Database Backup",
                    fType);
            }
        });
    }
}
