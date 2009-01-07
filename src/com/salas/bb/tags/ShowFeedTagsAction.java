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
// $Id: ShowFeedTagsAction.java,v 1.11 2008/02/15 15:01:23 spyromus Exp $
//

package com.salas.bb.tags;

import com.salas.bb.core.GlobalController;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.ITaggable;
import com.salas.bb.tags.net.ITagsStorage;
import com.salas.bb.views.mainframe.MainFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows tags of selected feed.
 */
public class ShowFeedTagsAction extends AbstractShowTagsAction
{
    private static ShowFeedTagsAction instance;

    /**
     * Hidden singleton constructor.
     */
    protected ShowFeedTagsAction()
    {
    }

    /**
     * Returns instance of the action.
     *
     * @return instance.
     */
    public static synchronized ShowFeedTagsAction getInstance()
    {
        if (instance == null) instance = new ShowFeedTagsAction();
        return instance;
    }

    /**
     * Returns currently selected taggable objects.
     *
     * @return taggable objects or <code>NULL</code> to skip further processing.
     */
    protected ITaggable[] getSelectedTaggables()
    {
        IFeed[] selectedFeeds = getFeeds();

        List taggables = new ArrayList(selectedFeeds.length);
        for (int i = 0; i < selectedFeeds.length; i++)
        {
            IFeed feed = selectedFeeds[i];
            if (feed instanceof ITaggable) taggables.add(feed);
        }

        return taggables.size() == 0 ? null
            : (ITaggable[])taggables.toArray(new ITaggable[taggables.size()]);
    }

    /**
     * Returns the list of feeds to handle.
     *
     * @return feeds.
     */
    protected IFeed[] getFeeds()
    {
        return GlobalController.SINGLETON.getSelectedFeeds();
    }

    /**
     * Returns initialized dialog window.
     *
     * @param aMainFrame  main frame.
     * @param aNetHandler tags networker object.
     *
     * @return dialog.
     */
    protected AbstractTagsDialog getTagsDialog(MainFrame aMainFrame,
        ITagsStorage aNetHandler)
    {
        return new FeedTagsDialog(aMainFrame, aNetHandler);
    }
}
