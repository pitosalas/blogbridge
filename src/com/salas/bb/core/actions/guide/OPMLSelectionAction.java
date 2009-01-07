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
// $Id: OPMLSelectionAction.java,v 1.8 2006/02/17 13:58:37 spyromus Exp $
//
package com.salas.bb.core.actions.guide;

import com.salas.bb.core.GlobalController;
import com.salas.bbutilities.opml.utils.FileFilter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Listens for 'browse' button presses and invokes file selection dialog.
 */
public final class OPMLSelectionAction extends AbstractAction
{
    /** Open dialog mode. */
    public static final int MODE_OPEN = 0;

    /** Save dialog mode. */
    public static final int MODE_SAVE = 1;

    private static File lastSaveDir;

    private int mode;
    private JTextField field;

    /**
     * Constructs action.
     *
     * @param mode  mode of dialog (see <code>MODE_xxxx</code> constants).
     * @param field field to put URL into.
     */
    public OPMLSelectionAction(int mode, JTextField field)
    {
        super("\u2026");

        this.mode = mode;
        this.field = field;
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e event object.
     */
    public void actionPerformed(ActionEvent e)
    {
        File dir = lastSaveDir == null ? new File(System.getProperty("user.home")) : lastSaveDir;

        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(dir, "."));
        fc.setFileFilter(FileFilter.getInstance());

        int result = mode == MODE_OPEN
                ? fc.showOpenDialog(GlobalController.SINGLETON.getMainFrame())
                : fc.showSaveDialog(GlobalController.SINGLETON.getMainFrame());

        if (result == JFileChooser.APPROVE_OPTION)
        {
            lastSaveDir = fc.getSelectedFile().getParentFile();

            try
            {
                File selectedFile = fc.getSelectedFile();

                String text;
                if (mode == MODE_OPEN)
                {
                    text = selectedFile.toURL().toString();
                } else
                {
                    text = selectedFile.getCanonicalPath();

                    // If file doesn't exist and its name has no extension add ".opml"
                    if (!selectedFile.exists() && selectedFile.getName().indexOf(".") == -1)
                    {
                        text += ".opml";
                    }
                }

                field.setText(text);
            } catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
    }
}
