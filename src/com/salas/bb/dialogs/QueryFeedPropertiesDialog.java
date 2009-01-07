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
// $Id: QueryFeedPropertiesDialog.java,v 1.11 2007/03/06 15:47:27 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.salas.bb.domain.QueryFeed;
import com.salas.bb.domain.querytypes.QueryType;

import java.awt.*;

/**
 * Dialog for displaying and chaning Query Feed properties.
 */
public class QueryFeedPropertiesDialog extends SmartFeedPropertiesDialog
{
    /**
     * Creates dialog for the parent frame.
     *
     * @param feed feed.
     * @param frame frame.
     */
    public QueryFeedPropertiesDialog(QueryFeed feed, Frame frame)
    {
        super(feed, frame);
    }

    /**
     * Show the dialog with initialized properties.
     *
     * @param title         title of the feed.
     * @param queryType     type of the query.
     * @param purgeLimit    articles limit (or -1 for global).
     * @param parameter     query parameter.
     * @param dedupEnabled  <code>TRUE</code> to remove duplicates.
     * @param dedupFrom     the first dedup word.
     * @param dedupTo       the last dedup word.
     *
     * @return TRUE if the data has changed.
     */
    public boolean open(String title, QueryType queryType, int purgeLimit, String parameter,
                        boolean dedupEnabled, int dedupFrom, int dedupTo)
    {
        initComponents();

        // Put in the initial settings
        tfTitle.setText(title);
        cbService.setSelectedItem(queryType);
        tfArticlesLimit.setText(purgeLimit == -1 ? null : Integer.toString(purgeLimit));
        setFeedParameter(parameter);
        chDedupEnabled.setSelected(dedupEnabled);
        spnDedupFrom.setValue(dedupFrom < 1 ? 1 : dedupFrom);
        spnDedupTo.setValue(dedupTo < 1 ? 1 : dedupTo);

        // Disable protected components
        cbService.setEnabled(false);

        open();

        // Check if something has changed
        return !hasBeenCanceled() && (!title.equals(getFeedTitle()) ||
            purgeLimit != getFeedArticlesLimit() ||
            !parameter.equals(getFeedParameter()) ||
            dedupEnabled != isDedupEnabled() ||
            dedupFrom != getDedupFrom());
    }
}
