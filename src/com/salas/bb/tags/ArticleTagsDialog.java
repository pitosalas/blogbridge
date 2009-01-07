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
// $Id: ArticleTagsDialog.java,v 1.8 2006/05/31 10:39:45 spyromus Exp $
//

package com.salas.bb.tags;

import com.salas.bb.utils.uif.HeaderPanelExt;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.tags.net.ITagsStorage;

import javax.swing.*;
import java.awt.*;

/**
 * Tags dialog to manipulate article's tags.
 */
public class ArticleTagsDialog extends AbstractTagsDialog
{
    /**
     * Creates dialog.
     *
     * @param frame     parent frame.
     * @param aStorage  handler to use for service communications.
     */
    public ArticleTagsDialog(Frame frame, ITagsStorage aStorage)
    {
        super(frame, aStorage);
    }

    /**
     * Returns the label for the title field.
     *
     * @return label.
     */
    protected String getTitleLabel()
    {
        return Strings.message("tags.article.title");
    }

    /**
     * Builds header of the dialog.
     *
     * @return header.
     */
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("tags.article.dialog.title"),
            Strings.message("tags.article.dialog.header"));
    }
}
