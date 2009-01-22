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
// $Id$
//

package com.salas.bb.twitter;

import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.util.ResourceUtils;
import com.jgoodies.forms.layout.CellConstraints;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Tweet This dialog box.
 */
public class TweetThisDialog extends AbstractDialog
{
    private JTextArea taMessage;
    private JTextField tfLink;
    private JButton btnShorten;

    /**
     * Creates the dialog.
     *
     * @param frame dialog.
     */
    public TweetThisDialog(Frame frame)
    {
        super(frame, Strings.message("tweetthis.dialog.title"));
    }

    /**
     * Builds the dialog content pane.
     *
     * @return dialog.
     */
    protected JComponent buildContent()
    {
        JPanel content = new JPanel(new BorderLayout());
        content.add(buildBody(), BorderLayout.CENTER);
        content.add(buildButtonBarWithOKCancel(), BorderLayout.SOUTH);
        return content;
    }

    /**
     * Creates the main area.
     *
     * @return component.
     */
    private Component buildBody()
    {
        initComponents();

        BBFormBuilder builder = new BBFormBuilder("right:pref, 4dlu, 150dlu, 2dlu, pref");

        // Build shifted label
        BBFormBuilder pb = new BBFormBuilder("pref");
        pb.appendRow("3px");
        pb.nextLine();
        pb.append(Strings.message("tweetthis.your.message"));

        builder.append(pb.getPanel(), 1, CellConstraints.LEFT, CellConstraints.TOP);
        builder.append(taMessage, 3);
        builder.append(Strings.message("tweetthis.shorten"), tfLink, btnShorten);

        return builder.getPanel();
    }

    /**
     * Initializes components.
     */
    private void initComponents()
    {
        tfLink     = new JTextField();
        taMessage  = new JTextArea(5, 70);
        btnShorten = new JButton(Strings.message("tweetthis.shorten.btn"));

        Border spacing = BorderFactory.createLineBorder(new JLabel().getBackground(), 3);

        taMessage.setWrapStyleWord(true);
        taMessage.setLineWrap(true);
        taMessage.setDocument(new TwitterMessage());
        taMessage.setBorder(BorderFactory.createCompoundBorder(spacing, tfLink.getBorder()));
    }

    public static void main(String[] args)
    {
        ResourceUtils.setBundlePath("Strings");
        TweetThisDialog tt = new TweetThisDialog(null);
        tt.open();
    }
}
