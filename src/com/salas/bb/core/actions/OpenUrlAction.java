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
// $Id: OpenUrlAction.java,v 1.5 2006/05/29 12:48:29 spyromus Exp $
//

package com.salas.bb.core.actions;

import com.salas.bb.utils.BrowserLauncher;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.core.GlobalModel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.MessageFormat;

/**
 * Action opens URL in external browser.
 */
public class OpenUrlAction extends AbstractAction
{
    private static final Logger LOG = Logger.getLogger(OpenUrlAction.class.getName());

    private String url;

    /**
     * Creates action for opening URL in external browser.
     *
     * @param url       URL to open.
     * @param title     title of action.
     */
    public OpenUrlAction(String url, String title)
    {
        super(title);
        this.url = url;
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e action event.
     */
    public void actionPerformed(ActionEvent e)
    {
        try
        {
            BrowserLauncher.showDocument(new URL(url),
                    GlobalModel.SINGLETON.getUserPreferences().getInternetBrowser());
        } catch (MalformedURLException e1)
        {
            LOG.log(Level.SEVERE, MessageFormat.format(Strings.error("invalid.url"), new Object[] { url }), e);
        }
    }
}
