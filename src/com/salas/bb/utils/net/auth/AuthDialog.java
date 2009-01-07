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
// $Id: AuthDialog.java,v 1.10 2007/02/09 10:26:30 spyromus Exp $
//

package com.salas.bb.utils.net.auth;

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.uif.AbstractDialog;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.HeaderPanelExt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

/**
 * Authentication dialog.
 */
public class AuthDialog extends AbstractDialog
{
    private JLabel          lbWording;
    private JLabel          lbContext;
    private JLabel          lbHost;
    private JTextField      tfUsername;
    private JPasswordField  tfPassword;
    private JCheckBox       chSave;

    /**
     * Creates dialog.
     *
     * @param parent parent frame.
     */
    public AuthDialog(Frame parent)
    {
        super(parent, Strings.message("net.authentication.dialog.title"), true);

        initComponents();
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    }

    // Builds header panel.
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("net.authentication.dialog.title"),
            Strings.message("net.authentication.dialog.header"));
    }

    // Builds the content pane
    protected JComponent buildContent()
    {
        BBFormBuilder builder = new BBFormBuilder("7dlu, pref, 2dlu, pref:grow");

        builder.append(lbWording, 4);

        builder.setLeadingColumnOffset(1);
        if (lbContext != null) builder.append(lbContext, 3);
        if (lbHost != null) builder.append(lbHost, 3);

        builder.setLeadingColumnOffset(1);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(Strings.message("net.authentication.username"), 1, tfUsername, 1);
        builder.append(Strings.message("net.authentication.password"), 1, tfPassword, 1);

        builder.appendRelatedComponentsGapRow(2);
        builder.append(chSave, 3);

        builder.setLeadingColumnOffset(0);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(buildButtonBar(), 4);

        return builder.getPanel();
    }

    private JComponent buildButtonBar()
    {
        JButton btnCancel = createCancelButton();
        btnCancel.setText(Strings.message("net.authentication.i.dont.know"));
        return ButtonBarFactory.buildOKCancelBar(createOKButton(true), btnCancel);
    }

    /** Initialize components. */
    private void initComponents()
    {
        lbWording = new JLabel(Strings.message("net.authentication.wording"));
        lbContext = null;
        lbHost = null;

        tfUsername = new JTextField();
        tfPassword = new JPasswordField();

        chSave = ComponentsFactory.createCheckBox(Strings.message("net.authentication.remember.this.information"));
    }

    /**
     * Shows authentication dialog.
     *
     * @param host      host requesting password.
     * @param context   name of the context.
     * @param username  username to put in the box by default.
     * @param password  password to put in the box by default.
     * @param save      TRUE to save password by default.
     */
    public void open(String host, String context, String username, char[] password, boolean save)
    {
        lbContext = new JLabel("Context: " + context);
        lbHost = new JLabel("Host: " + host);

        tfUsername.setText(username);
        tfPassword.setText(password == null ? null : new String(password));

        chSave.setSelected(save);

        super.open();
    }

    /**
     * Repacks the window each time it is displayed to adjust size to fit
     * wording and buttons.
     *
     * @param e event.
     */
    protected void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);

        if (e.getID() == WindowEvent.WINDOW_OPENED) pack();
    }

    /**
     * Returns username from the field.
     *
     * @return username.
     */
    public String getUsername()
    {
        return tfUsername.getText();
    }

    /**
     * Returns password from the field.
     *
     * @return password.
     */
    public char[] getPassword()
    {
        return tfPassword.getPassword();
    }

    /**
     * Returns TRUE if user requested to save password.
     *
     * @return TRUE to save password.
     */
    public boolean isSavingRequired()
    {
        return chSave.isSelected();
    }

    /**
     * Resizing hook.
     *
     * @param content component to take care of.
     */
    protected void resizeHook(JComponent content)
    {
        // We just disable the default resizer with this empty hook
    }
}
