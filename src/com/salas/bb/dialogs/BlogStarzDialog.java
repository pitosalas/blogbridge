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
// $Id: BlogStarzDialog.java,v 1.13 2006/06/13 09:44:34 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.AbstractFrame;
import com.jgoodies.uif.util.Resizer;
import com.salas.bb.domain.prefs.StarzPreferences;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.utils.ResourceID;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.HeaderPanelExt;
import com.salas.bb.utils.uif.IconSource;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for BlogStarz setup.
 */
public final class BlogStarzDialog extends AbstractDialog
{
    private GlobalModel model;

    private StarzPanel  starzPanel;

    /**
     * Creates keywords dialog.
     *
     * @param owner     owning frame.
     * @param aModel    global Model of BlogBridge.
     */
    public BlogStarzDialog(AbstractFrame owner, GlobalModel aModel)
    {
        super(owner, Strings.message("blogstarz.settings.dialog.title"));
        this.model = aModel;
    }

    /**
     * Builds and answers the preference's header.
     *
     * @return JComponent header of dialog box
     */
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("blogstarz.settings.dialog.title"),
            Strings.message("blogstarz.settings.dialog.header"),
            IconSource.getIcon(ResourceID.ICON_PREFERENCES));
    }

    /**
     * Builds and answers the preference's content pane.
     *
     * @return JComponent of content part of the dialog box
     */
    protected JComponent buildContent()
    {
        JPanel content = new JPanel(new BorderLayout());
        content.add(buildMainPanel(), BorderLayout.CENTER);
        content.add(buildButtonBarWithOKCancelApply(), BorderLayout.SOUTH);
        return content;
    }

    /**
     * Builds content component.
     *
     * @return main content component.
     */
    protected JComponent buildMainPanel()
    {
        if (starzPanel == null)
        {
            final StarzPreferences settings = model.getStarzPreferences();
            final ValueModel triggerChannel = getTriggerChannel();

            starzPanel = new StarzPanel(settings, triggerChannel);
        }

        return starzPanel;
    }

    protected void resizeHook(JComponent component)
    {
        component.setPreferredSize(new Dimension(400, 400));
    }

    /**
     * Closes the window.
     */
    protected void doCloseWindow()
    {
        doCancel();
    }
}