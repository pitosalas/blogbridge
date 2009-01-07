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

package com.salas.bb.utils.feedscollections;

import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;

/**
 * Collection loading progress page.
 */
class CLoadingProgressPage extends JPanel implements IProgressListener
{
    private JLabel lbStatus;
    private JProgressBar pbProgress;

    /**
     * Creates a new <code>JPanel</code> with a double buffer
     * and a flow layout.
     */
    public CLoadingProgressPage()
    {
        lbStatus = new JLabel();
        pbProgress = new JProgressBar();

        BBFormBuilder builder = new BBFormBuilder("0:grow", this);
        builder.setDefaultDialogBorder();

        builder.appendRow("0:grow");
        builder.appendRow("p");
        builder.appendRelatedComponentsGapRow();
        builder.appendRow("p");
        builder.appendRow("0:grow");

        builder.nextLine();
        builder.append(lbStatus);
        builder.nextLine(2);
        builder.append(pbProgress);

        pbProgress.setMinimum(0);
        pbProgress.setMaximum(100);
        pbProgress.setVisible(false);
        lbStatus.setText(Strings.message("collections.loading.please.wair"));
    }

    /**
     * Invoked when loading of collection started.
     */
    public void started()
    {
        setStatus(Strings.message("collections.loading.collection"));

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                pbProgress.setValue(0);
                pbProgress.setVisible(true);
            }
        });
    }

    /**
     * Invoked when progress changed.
     *
     * @param percentage progress percentage [0;100].
     */
    public void progress(final int percentage)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                pbProgress.setValue(percentage);
            }
        });
    }

    /**
     * Sets explicit status of loader.
     *
     * @param status status message.
     */
    public void status(String status)
    {
        setStatus(status);
    }

    /**
     * Invoked when loading finished.
     *
     * @param error NULL if no error.
     */
    public void finished(String error)
    {
        progress(100);
        setStatus(error == null ? Strings.message("collections.loading.successful") : error);
    }

    /**
     * Sets status message.
     *
     * @param text text.
     */
    private void setStatus(final String text)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                lbStatus.setText(text);
            }
        });
    }
}
