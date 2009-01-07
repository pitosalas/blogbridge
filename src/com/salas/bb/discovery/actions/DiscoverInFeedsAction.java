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
// $Id: DiscoverInFeedsAction.java,v 1.1 2008/04/01 08:20:05 spyromus Exp $
//

package com.salas.bb.discovery.actions;

import com.salas.bb.core.GlobalController;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Starts the discovery of new feeds in the selected feed(s).
 */
public class DiscoverInFeedsAction extends DiscoverAction
{
    private static final DiscoverInFeedsAction INSTANCE = new DiscoverInFeedsAction();

    /**
     * Creates an action.
     */
    private DiscoverInFeedsAction()
    {
        setEnabled(false);
    }

    /**
     * Returns the instance.
     *
     * @return instance.
     */
    public static DiscoverInFeedsAction getInstance()
    {
        return INSTANCE;
    }

    /**
     * Invoked on action.
     *
     * @param e event.
     */
    public void actionPerformed(ActionEvent e)
    {
        discover(flatten(GlobalController.SINGLETON.getSelectedFeeds()));
    }

    /**
     * Returns the list of articles taken from all feeds.
     *
     * @param feeds feeds.
     *
     * @return articles.
     */
    private static IArticle[] flatten(IFeed[] feeds)
    {
        Set<IArticle> articles = new LinkedHashSet<IArticle>();

        for (IFeed feed : feeds)
        {
            articles.addAll(Arrays.asList(feed.getArticles()));
        }

        return articles.toArray(new IArticle[articles.size()]);
    }

}