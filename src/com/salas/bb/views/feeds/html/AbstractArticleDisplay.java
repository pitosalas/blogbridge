/*
 * BlogBridge -- RSS feed reader, manager, and web based service
 * Copyright (C) 2002-2009 by R. Pito Salas
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Contact: R. Pito Salas
 * mailto:pitosalas@users.sourceforge.net
 * More information: about BlogBridge
 * http://www.blogbridge.com
 * http://sourceforge.net/projects/blogbridge
 */

package com.salas.bb.views.feeds.html;

import com.salas.bb.views.feeds.IArticleDisplay;
import com.salas.bb.views.feeds.IFeedDisplayConstants;

import javax.swing.*;

/**
 * Abstract article display for functionality sharing.
 */
public abstract class AbstractArticleDisplay extends JPanel implements IArticleDisplay
{
    /** View mode before the article was selected. Used and updated by the selection method. */
    private int preSelectionViewMode = -1;

    /**
     * Handles expanding of articles on selection if they are in mini-mode,
     * and returning this mode when deselected.
     *
     * @param sel TRUE when the article is about to be selected.
     */
    protected void handleAutoOpeningOnSelection(boolean sel)
    {
        // Automatically expand the article if it's in the MINI mode
        if (sel)
        {
            preSelectionViewMode = getViewMode();
            if (preSelectionViewMode == IFeedDisplayConstants.MODE_MINIMAL) setViewMode(IFeedDisplayConstants.MODE_FULL);
        } else
        {
            if (preSelectionViewMode == IFeedDisplayConstants.MODE_MINIMAL) setViewMode(preSelectionViewMode);
            preSelectionViewMode = -1;
        }
    }
}
