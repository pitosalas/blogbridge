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
// $Id: SmartFeedPropertiesDialog.java,v 1.10 2007/03/06 15:47:27 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.salas.bb.domain.IFeed;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.HeaderPanelExt;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for displaying and chaning smart feed properties.
 */
public class SmartFeedPropertiesDialog extends SmartFeedDialog
{
    /**
     * Creates dialog for the parent frame.
     *
     * @param feed feed.
     * @param frame frame.
     */
    public SmartFeedPropertiesDialog(IFeed feed, Frame frame)
    {
        super(feed, frame, Strings.message("show.feed.properties.dialog.title.smartfeed"));
    }

    /**
     * Builds a pretty XP-style white header.
     *
     * @return header.
     */
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("show.feed.properties.dialog.title.smartfeed"),
            Strings.message("show.feed.properties.dialog.header.smartfeed"));
    }
}
