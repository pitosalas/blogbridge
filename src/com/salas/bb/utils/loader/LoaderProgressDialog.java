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
// $Id: LoaderProgressDialog.java,v 1.8 2006/05/31 11:28:31 spyromus Exp $
//

package com.salas.bb.utils.loader;

import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.forms.factories.Borders;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Progress dialog box.
 */
class LoaderProgressDialog extends AbstractDialog
{
    private boolean canceled;
    private String message;
    private Object watcher;

    /**
     * Creates progress dialog box with message.
     *
     * @param aMessage message to display.
     * @param aWatcher watcher thread to notify on cancel.
     */
    public LoaderProgressDialog(String aMessage, Object aWatcher)
    {
        super((Frame)null, Strings.message("loader.dialog.title"));
        setModal(false);

        this.message = aMessage;
        this.watcher = aWatcher;
    }

    /**
     * Builds content.
     *
     * @return content of dialog.
     */
    protected JComponent buildContent()
    {
        JPanel content = new JPanel(new BorderLayout());

        content.add(buildBody(), BorderLayout.CENTER);
        content.add(buildButtonBar(), BorderLayout.SOUTH);

        return content;
    }

    /**
     * Builds button bar.
     *
     * @return button bar.
     */
    private Component buildButtonBar()
    {
        final JPanel panel = new JPanel();
        final JButton btnCancel = new JButton(Strings.message("loader.cancel"));
        panel.add(btnCancel);

        btnCancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                canceled = true;
                close();
            }
        });

        panel.setBorder(Borders.BUTTON_BAR_GAP_BORDER);

        return panel;
    }

    /**
     * Builds body of dialog.
     *
     * @return body.
     */
    private Component buildBody()
    {
        JPanel panel = new JPanel();
        panel.add(new JLabel(message));

        return panel;
    }

    /**
     * Returns true if was canceled.
     *
     * @return true if canceled.
     */
    public boolean isCanceled()
    {
        return canceled;
    }

    /**
     * Opens dialog.
     */
    public void open()
    {
        canceled = false;
        super.open();
    }

    public void close()
    {
        super.close();
        synchronized (watcher)
        {
            watcher.notifyAll();
        }
    }
}
