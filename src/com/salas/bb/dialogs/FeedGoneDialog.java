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
// $Id: FeedGoneDialog.java,v 1.4 2006/05/30 14:51:22 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.component.UIFButton;
import com.salas.bb.domain.DirectFeed;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.HeaderPanelExt;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * The dialog for querying user about what to do with dead feed.
 */
public class FeedGoneDialog extends AbstractDialog
{
    private DirectFeed feed;
    private JLabel lbFeedName;
    private JLabel lbSiteURL;
    private JLabel lbFeedURL;

    /**
     * Creates dialog for a given feed.
     *
     * @param parent    parent frame.
     * @param aFeed     feed.
     */
    public FeedGoneDialog(Frame parent, DirectFeed aFeed)
    {
        super(parent, Strings.message("feed.has.gone.dialog.title"), true);

        feed = aFeed;

        initComponents();

        URL siteURL = feed.getSiteURL();
        URL xmlURL = feed.getXmlURL();
        lbFeedName.setText(feed.getTitle());
        lbSiteURL.setText(siteURL == null ? null : siteURL.toString());
        lbFeedURL.setText(xmlURL == null ? null : xmlURL.toString());
    }

    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("feed.has.gone.dialog.title"),
            Strings.message("feed.has.gone.dialog.header"));
    }

    // Builds content pane.
    protected JComponent buildContent()
    {
        JTextArea lbWording = ComponentsFactory.createWrappedMultilineLabel(
            Strings.message("feed.has.gone.wording"));

        BBFormBuilder builder = new BBFormBuilder("7dlu, pref, 2dlu, pref:grow");

        builder.append(lbWording, 4);

        builder.setLeadingColumnOffset(1);

        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(Strings.message("feed.has.gone.feed.name"), lbFeedName);
        builder.append(Strings.message("feed.has.gone.siteurl"), lbSiteURL);
        builder.append(Strings.message("feed.has.gone.feedurl"), lbFeedURL);

        builder.setLeadingColumnOffset(0);

        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(Strings.message("feed.has.gone.query"), 4);

        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(ButtonBarFactory.buildOKCancelBar(
            createUnsubscribeButton(),
            createKeepButton()), 4);

        return builder.getPanel();
    }

    /**
     * Creates "Keep Feed" button.
     *
     * @return button.
     */
    private JButton createKeepButton()
    {
        UIFButton btn = createCancelButton();
        btn.setText(Strings.message("feed.has.gone.keep.feed"));

        return btn;
    }

    /**
     * Creates "Unsubscribe" button.
     *
     * @return button.
     */
    private JButton createUnsubscribeButton()
    {
        UIFButton btn = createOKButton(true);
        btn.setText(Strings.message("feed.has.gone.unsubscribe"));

        return btn;
    }

    // Initializes components.
    private void initComponents()
    {
        lbFeedName = new JLabel();
        lbSiteURL = new JLabel();
        lbFeedURL = new JLabel();
    }

    // Resizing hook.
    protected void resizeHook(JComponent content)
    {
        Constants.RESIZER_3TO2.resizeDialogContent(content);
    }
}
