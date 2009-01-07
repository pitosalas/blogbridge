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

package com.salas.bb.core.actions.guide;

import com.jgoodies.uif.AbstractDialog;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.opml.ImporterAdv;
import com.salas.bbutilities.opml.ImporterException;
import com.salas.bbutilities.opml.objects.OPMLGuide;

import javax.swing.*;
import java.awt.*;

/**
 * Abstract reading list dialog.
 */
public abstract class AbstractReadingListDialog extends AbstractDialog
{
    private static final String THREAD_NAME = "Verifying Reading List";

    protected JTextField tfAddress;
    protected JLabel lbStatus;
    protected JButton btnCheckAndAdd;

    /**
     * Creates a dialog.
     *
     * @param parent    parent frame.
     * @param title     title.
     */
    public AbstractReadingListDialog(Dialog parent, String title)
    {
        super(parent, title);
        initComponents();
    }

    /**
     * Creates a dialog.
     *
     * @param parent    parent frame.
     * @param title     title.
     */
    public AbstractReadingListDialog(Frame parent, String title)
    {
        super(parent, title);
        initComponents();
    }

    private void initComponents()
    {
        tfAddress = new JTextField();
        lbStatus = new JLabel();
        btnCheckAndAdd = createAcceptButton(Strings.message("guide.dialog.readinglists.check.and.add"), true);
    }

    /**
     * Returns URL entered by user.
     *
     * @return URL.
     */
    public String getURLs()
    {
        return StringUtils.fixURL(tfAddress.getText());
    }

    /**
     * Verifies the parsability of the given OPML file link.
     *
     * @param urlText URL of the OPML.
     *
     * @return <code>TRUE</code> if can be successfully imported.
     */
    private boolean isVerified(String urlText)
    {
        OPMLGuide[] opmlGuides;

        try
        {
            opmlGuides = new ImporterAdv().process(urlText, true).getGuides();
        } catch (ImporterException e)
        {
            opmlGuides = null;
        }

        return opmlGuides != null && opmlGuides.length > 0;
    }

    /**
     * Accepts entry if the resource is verified.
     */
    public void doAccept()
    {
        lbStatus.setText(Strings.message("guide.dialog.readinglists.add.status.verifying"));
        btnCheckAndAdd.setEnabled(false);

        Thread thread = new Thread(THREAD_NAME)
        {
            public void run()
            {
                final boolean verified = isVerified(getURLs());

                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        btnCheckAndAdd.setEnabled(true);
                        if (verified)
                        {
                            lbStatus.setText(Strings.message("guide.dialog.readinglists.add.status.verified"));
                            AbstractReadingListDialog.super.doAccept();
                        } else
                        {
                            lbStatus.setText(Strings.message("guide.dialog.readinglists.add.status.not.found"));
                        }
                    }
                });
            }
        };

        thread.start();
    }

    /**
     * Builds content.
     *
     * @return content.
     */
    protected JComponent buildContent()
    {
        JPanel content = new JPanel(new BorderLayout());

        content.add(buildMain(), BorderLayout.CENTER);
        content.add(buildButtons(), BorderLayout.SOUTH);

        return content;
    }

    /**
     * Builds main part.
     *
     * @return main part.
     */
    protected abstract JComponent buildMain();

    /**
     * Builds buttons.
     *
     * @return buttons.
     */
    protected abstract JComponent buildButtons();
}
