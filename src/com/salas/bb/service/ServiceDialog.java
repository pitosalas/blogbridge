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
// $Id: ServiceDialog.java,v 1.13 2007/02/01 19:08:20 spyromus Exp $
//

package com.salas.bb.service;


import com.jgoodies.uif.AbstractDialog;
import com.salas.bb.utils.uif.HeaderPanelExt;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.*;

/**
 * BlogBridge service dialog.
 */
class ServiceDialog extends AbstractDialog
{
    private ServicePreferences  preferences;
    private SyncSettingsPanel   syncPanel;
//    private RegistrationPanel   regPanel;

    /**
     * Creates dialog box.
     *
     * @param owner owner-frame.
     * @param prefs preferences object.
     */
    public ServiceDialog(final Frame owner, ServicePreferences prefs)
    {
        super(owner, Strings.message("service.dialog.title"));
        preferences = prefs;
    }

    /**
     * Returns content of the dialog box.
     *
     * @return content component.
     */
    protected JComponent buildContent()
    {
        JPanel content = new JPanel(new BorderLayout());

        content.add(buildBody(), BorderLayout.CENTER);
        content.add(buildButtonBarWithOKCancel(), BorderLayout.SOUTH);

        return content;
    }

    /**
     * Create header for dialog.
     *
     * @return header component.
     */
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("service.dialog.title"),
            Strings.message("service.dialog.header"));
    }

    /**
     * Main dialog body.
     *
     * @return body.
     */
    private JComponent buildBody()
    {
        syncPanel = new SyncSettingsPanel(this, preferences, getTriggerChannel());
        
        return syncPanel;
    }

    /**
     * Apply changes.
     */
    public void doApply()
    {
        super.doApply();

        // We don't have (well, I don't know) convenient method
        // to bind radio-buttons group to integer property, so,
        // we have to save the value manually.
        syncPanel.writeMode();
    }
}
