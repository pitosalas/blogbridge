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
// $Id: SearchFeedPropertiesDialog.java,v 1.8 2007/03/06 15:47:27 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.salas.bb.domain.SearchFeed;
import com.salas.bb.domain.query.articles.Query;

import java.awt.*;

/**
 * Search feed properties dialog.
 */
public class SearchFeedPropertiesDialog extends SmartFeedPropertiesDialog
{
    /**
     * Creates dialog for the parent frame.
     *
     * @param feed feed.
     * @param frame frame.
     */
    public SearchFeedPropertiesDialog(SearchFeed feed, Frame frame)
    {
        super(feed, frame);
    }

    /**
     * Creates dialog for search feed modification.
     *
     * @param aTitle            title of feed.
     * @param aQuery            query object.
     * @param aArticlesLimit    articles limit.
     * @param dedupEnabled     <code>TRUE</code> to remove duplicates.
     * @param dedupFrom   maximum number of first duplicate words before filtering out.
     *
     * @return TRUE if something has changed.
     */
    public boolean open(String aTitle, Query aQuery, int aArticlesLimit,
                        boolean dedupEnabled, int dedupFrom, int dedupTo)
    {
        query = aQuery.getClone();

        initComponents();

        tfTitle.setText(aTitle);
        cbService.setSelectedIndex(0);
        tfArticlesLimit.setText(aArticlesLimit == -1 ? null : Integer.toString(aArticlesLimit));

        chDedupEnabled.setSelected(dedupEnabled);
        spnDedupFrom.setValue(dedupFrom < 1 ? 1 : dedupFrom);
        spnDedupTo.setValue(dedupTo < 1 ? 1 : dedupTo);

        cbService.setEnabled(false);

        open();

        return !hasBeenCanceled() && (!aTitle.equals(getFeedTitle()) ||
            aArticlesLimit != getFeedArticlesLimit() ||
            !aQuery.equals(getFeedSearchQuery()) ||
            dedupEnabled != isDedupEnabled() ||
            dedupFrom != getDedupFrom() ||
            dedupTo != getDedupTo());
    }
}
