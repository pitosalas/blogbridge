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
// $Id: HyperListener.java,v 1.7 2006/01/08 05:13:00 kyank Exp $
//

package com.salas.bb.views;

import com.salas.bb.core.GlobalModel;
import com.salas.bb.utils.BrowserLauncher;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Hyper-link listener which is capable of opening them in default user browser.
 */
public final class HyperListener implements HyperlinkListener
{
    private static HyperListener instance;

    /**
     * Creates object.
     */
    private HyperListener()
    {
    }

    /**
     * Returns initialized instance.
     *
     * @return instance.
     */
    public static HyperListener getInstance()
    {
        if (instance == null)
        {
            instance = new HyperListener();
        }
        return instance;
    }

    /**
     * Called when a hypertext link is updated.
     *
     * @param e the event responsible for the update
     */
    public void hyperlinkUpdate(final HyperlinkEvent e)
    {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
        {
            // Launch browser and show document
            BrowserLauncher.showDocument(e.getURL(),
                    GlobalModel.SINGLETON.getUserPreferences().getInternetBrowser());
        }
    }
}
